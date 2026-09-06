/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.ProjectionType
 *  net.minecraft.Framebuffer
 *  net.minecraft.GlUniform
 *  net.minecraft.VertexFormats
 *  net.minecraft.Identifier
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 */
package moscow.rockstar.render.postprocess;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.shaders.ShaderProgramBase;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.target.RenderTarget;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import com.mojang.blaze3d.systems.ProjectionType;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class SaturationShader
extends ShaderProgramBase
implements ClientAccess,
WindowHandle {
    private final RenderTarget saturationRenderTarget = new RenderTarget(false);
    private GlUniform isolationStrengthUniform;
    private GlUniform isolationColorUniform;
    private GlUniform hueToleranceUniform;
    private GlUniform minimumSaturationUniform;
    private GlUniform minimumValueUniform;
    private GlUniform backgroundSaturationUniform;

    public SaturationShader(Identifier class_29602) {
        super(class_29602, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Override
    protected void initializeShaderUniforms() {
        this.isolationStrengthUniform = this.getUniform("IsolationStrength");
        this.isolationColorUniform = this.getUniform("IsolationColor");
        this.hueToleranceUniform = this.getUniform("HueTolerance");
        this.minimumSaturationUniform = this.getUniform("MinSat");
        this.minimumValueUniform = this.getUniform("MinVal");
        this.backgroundSaturationUniform = this.getUniform("BackgroundSat");
        super.initializeShaderUniforms();
    }

    public void renderSaturationOverlay(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        Framebuffer class_2762 = minecraftClient.getFramebuffer();
        if (class_2762 == null) {
            return;
        }
        int n = WINDOW.getScaledWidth();
        int n2 = WINDOW.getScaledHeight();
        RenderSystem.disableBlend();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.backupProjectionMatrix();
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0f, (float)n, (float)n2, 0.0f, 1000.0f, 21000.0f);
        RenderSystem.setProjectionMatrix((Matrix4f)matrix4f, (ProjectionType)ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.identity().translate(0.0f, 0.0f, -11000.0f);
        this.saturationRenderTarget.beginPass(true);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        class_2762.beginRead();
        RenderSystem.setShaderTexture((int)0, (int)class_2762.getColorAttachment());
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
        class_2762.endRead();
        this.saturationRenderTarget.endPass();
        this.bindShaderProgram();
        if (this.isolationStrengthUniform != null) {
            this.isolationStrengthUniform.set(f);
        }
        if (this.isolationColorUniform != null) {
            this.isolationColorUniform.set(f2, f3, f4);
        }
        if (this.hueToleranceUniform != null) {
            this.hueToleranceUniform.set(f5);
        }
        if (this.minimumSaturationUniform != null) {
            this.minimumSaturationUniform.set(f6);
        }
        if (this.minimumValueUniform != null) {
            this.minimumValueUniform.set(f7);
        }
        if (this.backgroundSaturationUniform != null) {
            this.backgroundSaturationUniform.set(f8);
        }
        this.saturationRenderTarget.beginRead();
        RenderSystem.setShaderTexture((int)0, (int)this.saturationRenderTarget.getColorAttachment());
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
        this.saturationRenderTarget.endRead();
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrix4fStack.popMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.depthMask((boolean)true);
    }
}
