package moscow.rockstar.render.core;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.text.SlugTextRenderer;
import moscow.rockstar.render.state.RenderStateSupport;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

/** Immediate Slug-font context used by the original world-label passes. */
public final class FontRenderContext extends RenderBufferContext {
    public FontRenderContext(VertexFormat ignoredFormat, MatrixStack matrixStack) {
        super(VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, matrixStack);
    }

    public void render() {
        WidgetBatchRenderer.prepareBlendState();
        RenderSystem.disableCull();
        ShaderProgram shader = SlugTextRenderer.bind(0.0f, 1.0f);
        shader.getUniform("EnableFadeout").set(0);
        this.finish();
        RenderStateSupport.resetTextureUnits();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        this.pop();
    }
}
