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

public class WetWorldShader
extends ShaderProgramBase
implements ClientAccess,
WindowHandle {
    private final RenderTarget postProcessTarget = new RenderTarget(false);
    private GlUniform maxDistanceUniform;
    private GlUniform upOnlyUniform;
    private GlUniform timeUniform;
    private GlUniform skyTintUniform;
    private GlUniform sunDirectionUniform;
    private GlUniform reflectivityUniform;
    private GlUniform wetnessUniform;
    private GlUniform rippleUniform;
    private GlUniform glossUniform;
    private GlUniform stepCountUniform;
    private GlUniform hitThicknessUniform;
    private GlUniform viewProjectionUniform;
    private GlUniform inverseViewProjectionUniform;
    private GlUniform cameraPositionUniform;

    public WetWorldShader(Identifier class_29602) {
        super(class_29602, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Override
    protected void initializeShaderUniforms() {
        this.viewProjectionUniform = this.getUniform("ViewProj");
        this.inverseViewProjectionUniform = this.getUniform("InvViewProj");
        this.cameraPositionUniform = this.getUniform("CamPos");
        this.skyTintUniform = this.getUniform("SkyTint");
        this.sunDirectionUniform = this.getUniform("SunDir");
        this.reflectivityUniform = this.getUniform("Reflectivity");
        this.wetnessUniform = this.getUniform("Wetness");
        this.rippleUniform = this.getUniform("Ripple");
        this.glossUniform = this.getUniform("Gloss");
        this.stepCountUniform = this.getUniform("Steps");
        this.hitThicknessUniform = this.getUniform("HitThickness");
        this.maxDistanceUniform = this.getUniform("MaxDistance");
        this.upOnlyUniform = this.getUniform("UpOnly");
        this.timeUniform = this.getUniform("Time");
        super.initializeShaderUniforms();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderPostProcess(Uniforms uniforms) {
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
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0f, (float)n, (float)n2, 0.0f, 1000.0f, 21000.0f);
        RenderSystem.setProjectionMatrix((Matrix4f)matrix4f, (ProjectionType)ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.identity().translate(0.0f, 0.0f, -11000.0f);
        try {
            this.postProcessTarget.beginPass(false);
            this.bindShaderProgram();
            this.uploadPostProcessUniforms(uniforms);
            class_2762.beginRead();
            RenderSystem.setShaderTexture((int)0, (int)class_2762.getColorAttachment());
            RenderSystem.setShaderTexture((int)1, (int)n3);
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
            class_2762.endRead();
            this.postProcessTarget.endPass();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            this.postProcessTarget.beginRead();
            RenderSystem.setShaderTexture((int)0, (int)this.postProcessTarget.getColorAttachment());
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
            this.postProcessTarget.endRead();
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

    private void uploadPostProcessUniforms(Uniforms uniforms) {
        if (this.viewProjectionUniform != null) {
            this.viewProjectionUniform.set(uniforms.viewProjectionMatrix);
        }
        if (this.inverseViewProjectionUniform != null) {
            this.inverseViewProjectionUniform.set(uniforms.inverseViewProjectionMatrix);
        }
        if (this.cameraPositionUniform != null) {
            this.cameraPositionUniform.set(uniforms.cameraX, uniforms.cameraY, uniforms.cameraZ);
        }
        if (this.skyTintUniform != null) {
            this.skyTintUniform.set(uniforms.skyTintRed, uniforms.skyTintGreen, uniforms.skyTintBlue);
        }
        if (this.sunDirectionUniform != null) {
            this.sunDirectionUniform.set(uniforms.sunDirectionX, uniforms.sunDirectionY, uniforms.sunDirectionZ);
        }
        if (this.reflectivityUniform != null) {
            this.reflectivityUniform.set(uniforms.reflectivity);
        }
        if (this.wetnessUniform != null) {
            this.wetnessUniform.set(uniforms.wetness);
        }
        if (this.rippleUniform != null) {
            this.rippleUniform.set(uniforms.rippleStrength);
        }
        if (this.glossUniform != null) {
            this.glossUniform.set(uniforms.gloss);
        }
        if (this.stepCountUniform != null) {
            this.stepCountUniform.set(uniforms.stepCount);
        }
        if (this.hitThicknessUniform != null) {
            this.hitThicknessUniform.set(uniforms.hitThickness);
        }
        if (this.maxDistanceUniform != null) {
            this.maxDistanceUniform.set(uniforms.maxDistance);
        }
        if (this.upOnlyUniform != null) {
            this.upOnlyUniform.set(uniforms.upOnly);
        }
        if (this.timeUniform != null) {
            this.timeUniform.set(uniforms.time);
        }
    }

    public static final class Uniforms {
        public final Matrix4f viewProjectionMatrix = new Matrix4f();
        public final Matrix4f inverseViewProjectionMatrix = new Matrix4f();
        public float cameraX;
        public float cameraY;
        public float cameraZ;
        public float skyTintRed;
        public float skyTintGreen;
        public float skyTintBlue;
        public float sunDirectionX;
        public float sunDirectionY = 1.0f;
        public float sunDirectionZ;
        public float reflectivity = 0.7f;
        public float wetness = 0.6f;
        public float rippleStrength = 0.35f;
        public float gloss = 0.5f;
        public float stepCount = 24.0f;
        public float hitThickness = 0.6f;
        public float maxDistance = 24.0f;
        public float upOnly = 1.0f;
        public float time = 0.9f;
    }
}
