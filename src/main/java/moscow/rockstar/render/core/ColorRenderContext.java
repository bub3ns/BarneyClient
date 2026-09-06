package moscow.rockstar.render.core;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;

/** Immediate color-vertex context used by world and HUD label renderers. */
public final class ColorRenderContext extends RenderBufferContext {
    public ColorRenderContext(VertexFormat format, MatrixStack matrixStack) {
        super(format, matrixStack);
    }

    public void render() {
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        this.finish();
        RenderSystem.disableBlend();
        this.pop();
    }
}
