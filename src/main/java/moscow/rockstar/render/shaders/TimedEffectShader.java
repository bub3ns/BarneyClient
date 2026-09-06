/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GlUniform
 *  net.minecraft.VertexFormats
 *  net.minecraft.Identifier
 *  org.joml.Matrix4f
 */
package moscow.rockstar.render.shaders;

import moscow.rockstar.render.shaders.ShaderProgramBase;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class TimedEffectShader
extends ShaderProgramBase {
    private GlUniform viewProjectionUniform;
    private GlUniform cameraPositionUniform;
    private GlUniform cameraRightUniform;
    private GlUniform cameraUpUniform;
    private GlUniform motionUniform;
    private GlUniform cellUniform;
    private GlUniform tintUniform;
    private GlUniform timeUniform;
    private GlUniform particleSizeUniform;
    private GlUniform driftUniform;
    private GlUniform flickerUniform;
    private GlUniform stretchUniform;
    private GlUniform roofUniform;
    private GlUniform roofSpanUniform;
    private GlUniform strengthUniform;
    private GlUniform aspectRatioUniform;

    public TimedEffectShader(Identifier class_29602) {
        super(class_29602, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Override
    protected void initializeShaderUniforms() {
        this.viewProjectionUniform = this.getUniform("ViewProj");
        this.cameraPositionUniform = this.getUniform("CamPos");
        this.cameraRightUniform = this.getUniform("CamRight");
        this.cameraUpUniform = this.getUniform("CamUp");
        this.motionUniform = this.getUniform("Motion");
        this.cellUniform = this.getUniform("Cell");
        this.tintUniform = this.getUniform("Tint");
        this.timeUniform = this.getUniform("Time");
        this.particleSizeUniform = this.getUniform("Size");
        this.driftUniform = this.getUniform("Drift");
        this.flickerUniform = this.getUniform("Flicker");
        this.stretchUniform = this.getUniform("Stretch");
        this.roofUniform = this.getUniform("Roof");
        this.roofSpanUniform = this.getUniform("RoofSpan");
        this.strengthUniform = this.getUniform("Strength");
        this.aspectRatioUniform = this.getUniform("Aspect");
        super.initializeShaderUniforms();
    }

    public void uploadParticleUniforms(Matrix4f matrix4f, ParticleUniformState particleUniformState) {
        if (this.viewProjectionUniform != null) {
            this.viewProjectionUniform.set(matrix4f);
        }
        if (this.cameraPositionUniform != null) {
            this.cameraPositionUniform.set(particleUniformState.cameraX, particleUniformState.cameraY, particleUniformState.cameraZ);
        }
        if (this.cameraRightUniform != null) {
            this.cameraRightUniform.set(particleUniformState.cameraRightX, particleUniformState.cameraRightY, particleUniformState.cameraRightZ);
        }
        if (this.cameraUpUniform != null) {
            this.cameraUpUniform.set(particleUniformState.cameraUpX, particleUniformState.cameraUpY, particleUniformState.cameraUpZ);
        }
        if (this.motionUniform != null) {
            this.motionUniform.set(particleUniformState.motionX, particleUniformState.motionY, particleUniformState.motionZ);
        }
        if (this.cellUniform != null) {
            this.cellUniform.set(particleUniformState.cellWidth, particleUniformState.cellHeight, particleUniformState.cellDepth);
        }
        if (this.tintUniform != null) {
            this.tintUniform.set(particleUniformState.tintRed, particleUniformState.tintGreen, particleUniformState.tintBlue, particleUniformState.tintAlpha);
        }
        if (this.timeUniform != null) {
            this.timeUniform.set(particleUniformState.animationTime);
        }
        if (this.particleSizeUniform != null) {
            this.particleSizeUniform.set(particleUniformState.particleSize);
        }
        if (this.driftUniform != null) {
            this.driftUniform.set(particleUniformState.drift);
        }
        if (this.flickerUniform != null) {
            this.flickerUniform.set(particleUniformState.flicker);
        }
        if (this.stretchUniform != null) {
            this.stretchUniform.set(particleUniformState.stretch);
        }
        if (this.roofUniform != null) {
            this.roofUniform.set(particleUniformState.roofOriginX, particleUniformState.roofOriginZ, particleUniformState.roofHeight, particleUniformState.roofEnabled);
        }
        if (this.roofSpanUniform != null) {
            this.roofSpanUniform.set(particleUniformState.roofSpan);
        }
    }

    public void setEffectParameters(float f, float f2, float f3) {
        if (this.timeUniform != null) {
            this.timeUniform.set(f);
        }
        if (this.strengthUniform != null) {
            this.strengthUniform.set(f2);
        }
        if (this.aspectRatioUniform != null) {
            this.aspectRatioUniform.set(f3);
        }
    }

    /** Values shared by the ambient-particle vertex shader and the rain overlay. */
    public static final class ParticleUniformState {
        public float cameraX;
        public float cameraY;
        public float cameraZ;
        public float cameraRightX = 1.0f;
        public float cameraRightY;
        public float cameraRightZ;
        public float cameraUpX;
        public float cameraUpY = 1.0f;
        public float cameraUpZ;
        public float motionX;
        public float motionY = -0.6f;
        public float motionZ;
        public float cellWidth = 48.0f;
        public float cellHeight = 32.0f;
        public float cellDepth = 48.0f;
        public float tintRed = 1.0f;
        public float tintGreen = 1.0f;
        public float tintBlue = 1.0f;
        public float tintAlpha = 1.0f;
        public float animationTime;
        public float particleSize = 0.12f;
        public float drift = 0.6f;
        public float flicker;
        public float stretch = 1.0f;
        public float roofOriginX;
        public float roofOriginZ;
        public float roofHeight;
        public float roofEnabled;
        public float roofSpan = 192.0f;
    }
}
