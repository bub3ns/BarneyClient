package moscow.rockstar.render.text;

import moscow.rockstar.mixin.accessors.TextRendererInvoker;
import moscow.rockstar.mixin.accessors.TextRendererStyledInvoker;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;

/** Defers protected text replacement to the capture controller's original pass order. */
public final class TextReplacementRenderer {
    private static boolean renderingPatch;

    private TextReplacementRenderer() {
    }

    public static boolean isRenderingPatch() {
        return renderingPatch;
    }

    public static int renderPlain(TextRenderer renderer, String original, String replacement,
                                  float x, float y, int color, boolean shadow, Matrix4f matrix,
                                  TextRenderer.TextLayerType layerType, int backgroundColor,
                                  int light, boolean seeThrough) {
        TextRendererInvoker invoker = (TextRendererInvoker) renderer;
        Matrix4f transform = new Matrix4f(matrix);
        TextCaptureController.registerPatch(new TextCapturePatch() {
            @Override
            public int[] getBounds() {
                return TextCaptureController.transformBounds(
                    transform,
                    x,
                    y,
                    Math.max(renderer.getWidth(original), renderer.getWidth(replacement))
                );
            }

            @Override
            public void renderOriginal() {
                renderPatchText(() -> invoker.rockstar$drawInternal(
                    original,
                    x,
                    y,
                    color,
                    shadow,
                    transform,
                    TextCaptureController.getImmediateProvider(),
                    layerType,
                    backgroundColor,
                    light,
                    seeThrough
                ));
            }

            @Override
            public void renderReplacement() {
                renderPatchText(() -> invoker.rockstar$drawInternal(
                    replacement,
                    x,
                    y,
                    color,
                    shadow,
                    transform,
                    TextCaptureController.getImmediateProvider(),
                    layerType,
                    backgroundColor,
                    light,
                    seeThrough
                ));
            }
        });
        return (int) (x + renderer.getWidth(original)) + (shadow ? 1 : 0);
    }

    public static int renderStyled(TextRenderer renderer, OrderedText original, OrderedText replacement,
                                   float x, float y, int color, boolean shadow, Matrix4f matrix,
                                   TextRenderer.TextLayerType layerType, int backgroundColor,
                                   int light, boolean seeThrough) {
        TextRendererStyledInvoker invoker = (TextRendererStyledInvoker) renderer;
        Matrix4f transform = new Matrix4f(matrix);
        TextCaptureController.registerPatch(new TextCapturePatch() {
            @Override
            public int[] getBounds() {
                return TextCaptureController.transformBounds(
                    transform,
                    x,
                    y,
                    Math.max(renderer.getWidth(original), renderer.getWidth(replacement))
                );
            }

            @Override
            public void renderOriginal() {
                renderPatchText(() -> invoker.rockstar$drawInternal(
                    original,
                    x,
                    y,
                    color,
                    shadow,
                    transform,
                    TextCaptureController.getImmediateProvider(),
                    layerType,
                    backgroundColor,
                    light,
                    seeThrough
                ));
            }

            @Override
            public void renderReplacement() {
                renderPatchText(() -> invoker.rockstar$drawInternal(
                    replacement,
                    x,
                    y,
                    color,
                    shadow,
                    transform,
                    TextCaptureController.getImmediateProvider(),
                    layerType,
                    backgroundColor,
                    light,
                    seeThrough
                ));
            }
        });
        return (int) (x + renderer.getWidth(original)) + (shadow ? 1 : 0);
    }

    public static void renderOutline(TextRenderer renderer, OrderedText replacement,
                                     float x, float y, int color, int outlineColor,
                                     Matrix4f matrix, VertexConsumerProvider consumers, int light) {
        renderReentrant(() -> renderer.drawWithOutline(replacement, x, y, color, outlineColor, matrix, consumers, light));
    }

    private static void renderPatchText(Runnable renderer) {
        renderReentrant(() -> {
            renderer.run();
            TextCaptureController.getImmediateProvider().draw();
        });
    }

    private static void renderReentrant(Runnable renderer) {
        boolean previous = renderingPatch;
        renderingPatch = true;
        try {
            renderer.run();
        } finally {
            renderingPatch = previous;
        }
    }
}
