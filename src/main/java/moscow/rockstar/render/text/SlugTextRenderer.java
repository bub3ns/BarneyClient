package moscow.rockstar.render.text;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.core.FontRenderContext;
import moscow.rockstar.render.core.RenderBufferContext;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.state.RenderStateSupport;
import moscow.rockstar.render.text.glyph.Glyph;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

/**
 * The original Slug text entry point.  It selects the active render contract:
 * an immediate font context, a widget batch, or a direct Slug buffer.
 */
public final class SlugTextRenderer {
    /** Mirrors rockstar/ilIlil/IiIIIi.I Z: while a capture patch re-draws its text,
     *  no further patch may be registered and neither the font context nor the widget
     *  batch may swallow the draw. */
    private static boolean patchPass;

    private SlugTextRenderer() {
    }

    private static void runPatchPass(Runnable renderer) {
        patchPass = true;
        try {
            renderer.run();
        } finally {
            patchPass = false;
        }
    }

    public static ShaderProgram bind(float weight) {
        return bind(weight, 1.0f);
    }

    public static ShaderProgram bind(float weight, float softness) {
        ShaderProgram shader = ShaderRenderer.slugFontShader.bindShaderProgram();
        shader.getUniform("Weight").set(weight);
        shader.getUniform("Softness").set(softness);
        return shader;
    }

    public static void resetTextures() {
        RenderStateSupport.resetTextureUnits();
    }

    /** Draws an explicitly registered icon glyph using the original UI-em-square coordinates. */
    public static void drawIcon(FontRenderer font, int codePoint, float x, float y, float size,
                                int color, Matrix4f matrix) {
        if (font == null || ((color >>> 24) & 0xFF) == 0) {
            return;
        }
        Glyph glyph = font.getGlyph(codePoint);
        if (glyph == null) {
            return;
        }

        WidgetBatchRenderer batch = patchPass ? null : WidgetBatchRenderer.getCurrentBatch();
        if (batch != null) {
            batch.queueIcon(font, codePoint, x, y, size, matrix, color);
            return;
        }

        WidgetBatchRenderer.prepareBlendState();
        RenderSystem.disableCull();
        ShaderProgram shader = bind(0.0f, 0.5f);
        shader.getUniform("EnableFadeout").set(0);
        shader.getUniform("FadeoutStart").set(0.0f);
        shader.getUniform("FadeoutEnd").set(1.0f);
        shader.getUniform("MaxWidth").set(0.0f);
        shader.getUniform("TextPosX").set(x);

        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        glyph.appendUiQuad(matrix, buffer, x, y, size, color);
        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
        resetTextures();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void draw(FontRenderer font, String text, float scale, int color,
                            Matrix4f matrix, float x, float y, float z) {
        render(font, text, scale, color, matrix, x, y, z,
            0.0f, 0.5f, false, 0.0f, 1.0f, 0.0f, x, 0.0f, 0.0f);
    }

    /** Draws text with the weight/softness pair used by text shadows. */
    public static void drawWithStyle(FontRenderer font, String text, float scale, int color,
                                     Matrix4f matrix, float x, float y, float z,
                                     float weight, float softness) {
        render(font, text, scale, color, matrix, x, y, z,
            weight, softness, false, 0.0f, 1.0f, 0.0f, x, 0.0f, 0.0f);
    }

    public static void drawFadeout(FontRenderer font, String text, float scale, int color,
                                   Matrix4f matrix, float x, float y, float z,
                                   float fadeStart, float fadeEnd, float maxWidth) {
        render(font, text, scale, color, matrix, x, y, z,
            0.0f, 0.5f, true, fadeStart, fadeEnd, maxWidth, x, 0.0f, 0.0f);
    }

    public static void drawFadeout(FontRenderer font, String text, float scale, int color,
                                   Matrix4f matrix, float x, float y, float z,
                                   float fadeStart, float fadeEnd, float maxWidth,
                                   float fadeinStart, float fadeinEnd) {
        render(font, text, scale, color, matrix, x, y, z,
            0.0f, 0.5f, true, fadeStart, fadeEnd, maxWidth, x, fadeinStart, fadeinEnd);
    }

    private static void render(FontRenderer font, String rawText, float scale, int color,
                               Matrix4f matrix, float x, float y, float z,
                               float weight, float softness, boolean fadeout,
                               float fadeStart, float fadeEnd, float maxWidth, float textPosX,
                               float fadeinStart, float fadeinEnd) {
        if (font == null || ((color >>> 24) & 0xFF) == 0) {
            return;
        }
        String text = rawText == null ? "" : rawText;
        if (registerCapturePatch(font, text, scale, color, matrix, x, y, z,
                weight, softness, fadeout, fadeStart, fadeEnd, maxWidth, textPosX,
                fadeinStart, fadeinEnd)) {
            return;
        }
        String renderedText = TextCaptureController.transform(text);

        RenderBufferContext activeContext = patchPass ? null : RenderBufferContext.current();
        if (activeContext != null) {
            font.draw(matrix, activeContext.bufferBuilder(), renderedText, scale, x, y, z, color);
            return;
        }

        WidgetBatchRenderer batch = patchPass ? null : WidgetBatchRenderer.getCurrentBatch();
        if (batch != null) {
            if (!fadeout && fadeinStart == 0.0f && fadeinEnd == 0.0f
                    && weight == 0.0f && softness == 0.5f) {
                batch.queueText(font, renderedText, scale, matrix, x, y, z, color);
                return;
            }
            if (batch.renderTextWithGeometry(font, renderedText, scale, matrix, x, y, z, color,
                    weight, softness, fadeout, textPosX, maxWidth,
                    fadeStart, fadeEnd, fadeinStart, fadeinEnd)) {
                return;
            }
            // Styled/fading text cannot be appended to the plain batch. Keep
            // ordering by flushing it before opening the direct Slug buffer.
            WidgetBatchRenderer.flushCurrentBatch();
        }

        drawImmediately(font, renderedText, scale, color, matrix, x, y, z,
            weight, softness, fadeout, fadeStart, fadeEnd, maxWidth, textPosX,
            fadeinStart, fadeinEnd);
    }

    /**
     * Port of {@code rockstar/ilIlil/IiIIIi#I (IIiiii,String,F,Matrix4f,FF,Consumer)Z} and its
     * inner {@code IiIIIi$1} patch: every custom-font string first asks the capture-bypass layer
     * to defer it, so the untouched text can be re-drawn into the private capture target.
     */
    private static boolean registerCapturePatch(FontRenderer font, String text, float scale, int color,
                                                Matrix4f matrix, float x, float y, float z,
                                                float weight, float softness, boolean fadeout,
                                                float fadeStart, float fadeEnd, float maxWidth, float textPosX,
                                                float fadeinStart, float fadeinEnd) {
        if (patchPass || !TextCaptureController.isCaptureActive()) {
            return false;
        }
        String replacement = TextCaptureController.transform(text);
        if (replacement == null || replacement.equals(text)) {
            return false;
        }
        final Matrix4f captured = new Matrix4f(matrix);
        final float width = Math.max(font.measure(text, scale), font.measure(replacement, scale));
        return TextCaptureController.registerPatch(new TextCapturePatch() {
            @Override
            public int[] getBounds() {
                return TextCaptureController.transformBounds(captured,
                    x - 1.0f, y - scale * 0.35f, x + width + 2.0f, y + scale * 1.45f);
            }

            @Override
            public void renderOriginal() {
                TextCaptureController.renderOriginalText(() -> runPatchPass(() ->
                    render(font, text, scale, color, captured, x, y, z,
                        weight, softness, fadeout, fadeStart, fadeEnd, maxWidth, textPosX,
                        fadeinStart, fadeinEnd)));
            }

            @Override
            public void renderReplacement() {
                runPatchPass(() ->
                    render(font, text, scale, color, captured, x, y, z,
                        weight, softness, fadeout, fadeStart, fadeEnd, maxWidth, textPosX,
                        fadeinStart, fadeinEnd));
            }
        });
    }

    private static void drawImmediately(FontRenderer font, String text, float scale, int color,
                                       Matrix4f matrix, float x, float y, float z,
                                       float weight, float softness, boolean fadeout,
                                       float fadeStart, float fadeEnd, float maxWidth, float textPosX,
                                       float fadeinStart, float fadeinEnd) {
        WidgetBatchRenderer.prepareBlendState();
        RenderSystem.disableCull();
        ShaderProgram shader = bind(weight, softness);
        shader.getUniform("EnableFadeout").set(fadeout ? 1 : 0);
        shader.getUniform("FadeoutStart").set(fadeStart);
        shader.getUniform("FadeoutEnd").set(fadeEnd);
        shader.getUniform("FadeinStart").set(fadeinStart);
        shader.getUniform("FadeinEnd").set(fadeinEnd);
        shader.getUniform("MaxWidth").set(maxWidth);
        shader.getUniform("TextPosX").set(textPosX);

        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        font.draw(matrix, buffer, text, scale, x, y, z, color, softness);
        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
        resetTextures();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
