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
package moscow.rockstar.render.world;

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

public class RainOverlayShader
extends ShaderProgramBase
implements ClientAccess,
WindowHandle {
    private final RenderTarget rainRenderTarget = new RenderTarget(false).enableLinearFiltering();
    private GlUniform inverseViewProjectionUniform;
    private GlUniform cameraPositionUniform;
    private GlUniform sceneTintUniform;
    private GlUniform animationTimeUniform;
    private GlUniform raindropStrengthUniform;
    private GlUniform splashStrengthUniform;
    private GlUniform viewportAspectUniform;
    private GlUniform roofParametersUniform;
    private GlUniform roofSpanUniform;

    public RainOverlayShader(Identifier class_29602) {
        super(class_29602, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Override
    protected void initializeShaderUniforms() {
        this.inverseViewProjectionUniform = this.getUniform("InvViewProj");
        this.cameraPositionUniform = this.getUniform("CamPos");
        this.sceneTintUniform = this.getUniform("Tint");
        this.animationTimeUniform = this.getUniform("Time");
        this.raindropStrengthUniform = this.getUniform("Drops");
        this.splashStrengthUniform = this.getUniform("Splashes");
        this.viewportAspectUniform = this.getUniform("Aspect");
        this.roofParametersUniform = this.getUniform("Roof");
        this.roofSpanUniform = this.getUniform("RoofSpan");
        super.initializeShaderUniforms();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderRainOverlay(Matrix4f matrix4f, UniformState uniformState) {
        Framebuffer class_2762 = minecraftClient.getFramebuffer();
        if (class_2762 == null || class_2762.getDepthAttachment() == 0) {
            return;
        }
        int n = WINDOW.getScaledWidth();
        int n2 = WINDOW.getScaledHeight();
        if (n <= 0 || n2 <= 0) {
            return;
        }
        int n3 = class_2762.getDepthAttachment();
        RenderSystem.disableBlend();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.backupProjectionMatrix();
        Matrix4f matrix4f2 = new Matrix4f().setOrtho(0.0f, (float)n, (float)n2, 0.0f, 1000.0f, 21000.0f);
        RenderSystem.setProjectionMatrix((Matrix4f)matrix4f2, (ProjectionType)ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.identity().translate(0.0f, 0.0f, -11000.0f);
        try {
            this.rainRenderTarget.beginPass(false);
            this.bindShaderProgram();
            this.uploadRainUniforms(matrix4f, uniformState, (float)n / (float)n2);
            class_2762.beginRead();
            RenderSystem.setShaderTexture((int)0, (int)class_2762.getColorAttachment());
            RenderSystem.setShaderTexture((int)1, (int)n3);
            RenderSystem.setShaderTexture((int)2, (int)uniformState.rainTextureId);
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
            class_2762.endRead();
            this.rainRenderTarget.endPass();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            this.rainRenderTarget.beginRead();
            RenderSystem.setShaderTexture((int)0, (int)this.rainRenderTarget.getColorAttachment());
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
            this.rainRenderTarget.endRead();
            RenderSystem.setShaderTexture((int)2, (int)0);
            RenderSystem.setShaderTexture((int)1, (int)0);
            RenderSystem.setShaderTexture((int)0, (int)0);
        }
        finally {
            matrix4fStack.popMatrix();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.depthMask((boolean)true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    private void uploadRainUniforms(Matrix4f matrix4f, UniformState uniformState, float f) {
        if (this.inverseViewProjectionUniform != null) {
            this.inverseViewProjectionUniform.set(matrix4f);
        }
        if (this.cameraPositionUniform != null) {
            this.cameraPositionUniform.set(uniformState.cameraX, uniformState.cameraY, uniformState.cameraZ);
        }
        if (this.sceneTintUniform != null) {
            this.sceneTintUniform.set(uniformState.tintRed, uniformState.tintGreen, uniformState.tintBlue);
        }
        if (this.animationTimeUniform != null) {
            this.animationTimeUniform.set(uniformState.animationTime);
        }
        if (this.raindropStrengthUniform != null) {
            this.raindropStrengthUniform.set(uniformState.raindropStrength);
        }
        if (this.splashStrengthUniform != null) {
            this.splashStrengthUniform.set(uniformState.splashStrength);
        }
        if (this.viewportAspectUniform != null) {
            this.viewportAspectUniform.set(f);
        }
        if (this.roofParametersUniform != null) {
            this.roofParametersUniform.set(uniformState.roofParameterX, uniformState.roofParameterY, uniformState.roofParameterZ, uniformState.roofEnabled);
        }
        if (this.roofSpanUniform != null) {
            this.roofSpanUniform.set(uniformState.roofHeight);
        }
    }

    public static final class UniformState {
        public float cameraX;
        public float cameraY;
        public float cameraZ;
        public float tintRed = 0.7f;
        public float tintGreen = 0.78f;
        public float tintBlue = 0.85f;
        public float animationTime;
        public float raindropStrength = 0.6f;
        public float splashStrength = 0.6f;
        public float roofParameterX;
        public float roofParameterY;
        public float roofParameterZ;
        public float roofEnabled;
        public float roofHeight = 192.0f;
        public int rainTextureId;
    }
}
