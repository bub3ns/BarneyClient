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

import moscow.rockstar.render.postprocess.JumpCirclePostProcessor;
import moscow.rockstar.render.shaders.ShaderProgramBase;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class ShockwaveShader
extends ShaderProgramBase {
    public static final int JUMP_CIRCLE_VERTEX_COUNT = 12;
    private GlUniform inverseViewProjection;
    private GlUniform circleCenter;
    private GlUniform worldRadius;
    private GlUniform thickness;
    private GlUniform ringColor;
    private GlUniform strength;

    public ShockwaveShader(Identifier class_29602) {
        super(class_29602, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Override
    protected void initializeShaderUniforms() {
        this.inverseViewProjection = this.getUniform("InvViewProj");
        this.circleCenter = this.getUniform("CircleCenter");
        this.worldRadius = this.getUniform("WorldRadius");
        this.thickness = this.getUniform("Thickness");
        this.ringColor = this.getUniform("RingColor");
        this.strength = this.getUniform("Strength");
        super.initializeShaderUniforms();
    }

    public void setShockwaveParameters(Matrix4f matrix4f, JumpCirclePostProcessor.JumpCircleInstance jumpCircleInstance) {
        if (this.inverseViewProjection != null) {
            this.inverseViewProjection.set(matrix4f);
        }
        if (this.circleCenter != null) {
            this.circleCenter.set(jumpCircleInstance.getCenterX(), jumpCircleInstance.getCenterY(), jumpCircleInstance.getCenterZ());
        }
        if (this.worldRadius != null) {
            this.worldRadius.set(jumpCircleInstance.getWorldRadius());
        }
        if (this.thickness != null) {
            this.thickness.set(jumpCircleInstance.getThickness());
        }
        if (this.ringColor != null) {
            this.ringColor.set(jumpCircleInstance.getRed(), jumpCircleInstance.getGreen(), jumpCircleInstance.getBlue(), jumpCircleInstance.getIntensity());
        }
        if (this.strength != null) {
            this.strength.set(jumpCircleInstance.getStrength());
        }
    }
}

