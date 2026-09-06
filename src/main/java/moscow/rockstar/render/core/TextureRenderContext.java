package moscow.rockstar.render.core;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;

/** Immediate textured-vertex context used by icon and preview renderers. */
public final class TextureRenderContext extends RenderBufferContext {
    public TextureRenderContext(VertexFormat format, MatrixStack matrixStack) {
        super(format, matrixStack);
    }

    public void render() {
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.enableBlend();
        this.finish();
        RenderSystem.disableBlend();
        RenderSystem.setShaderTexture(0, 0);
        this.pop();
    }
}
