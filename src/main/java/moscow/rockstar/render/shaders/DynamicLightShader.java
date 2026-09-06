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

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import moscow.rockstar.render.shaders.ShaderProgramBase;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class DynamicLightShader
extends ShaderProgramBase {
    public static final int MAX_LIGHTS = 48;
    private GlUniform inverseViewProjection;
    private GlUniform ambientLight;
    private final GlUniform[] lightPositionUniforms = new GlUniform[48];
    private final GlUniform[] lightColorUniforms = new GlUniform[48];

    public DynamicLightShader(Identifier class_29602) {
        super(class_29602, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Override
    protected void initializeShaderUniforms() {
        this.inverseViewProjection = this.getUniform("InvViewProj");
        this.ambientLight = this.getUniform("Ambient");
        for (int i = 0; i < 48; ++i) {
            this.lightPositionUniforms[i] = this.getUniform("L" + i + "Pos");
            this.lightColorUniforms[i] = this.getUniform("L" + i + "Col");
        }
        super.initializeShaderUniforms();
    }

    public void setDynamicLightUniforms(Matrix4f matrix4f, float f, List<LightSource> list) {
        if (this.inverseViewProjection != null) {
            this.inverseViewProjection.set(matrix4f);
        }
        if (this.ambientLight != null) {
            this.ambientLight.set(f);
        }
        for (int i = 0; i < 48; ++i) {
            LightSource lightSource;
            LightSource lightSource2 = lightSource = i < list.size() ? list.get(i) : null;
            if (this.lightPositionUniforms[i] != null) {
                if (lightSource == null) {
                    this.lightPositionUniforms[i].set(0.0f, 0.0f, 0.0f, 0.0f);
                } else {
                    this.lightPositionUniforms[i].set(lightSource.getX(), lightSource.getY(), lightSource.getZ(), lightSource.getRadius());
                }
            }
            if (this.lightColorUniforms[i] == null) continue;
            if (lightSource == null) {
                this.lightColorUniforms[i].set(0.0f, 0.0f, 0.0f, 0.0f);
                continue;
            }
            this.lightColorUniforms[i].set(lightSource.getRed(), lightSource.getGreen(), lightSource.getBlue(), lightSource.getIntensity());
        }
    }

    public static final class LightSource {
        private final float x;
        private final float y;
        private final float z;
        private final float radius;
        private final float red;
        private final float green;
        private final float blue;
        private final float intensity;

        public LightSource(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
            this.x = f;
            this.y = f2;
            this.z = f3;
            this.radius = f4;
            this.red = f5;
            this.green = f6;
            this.blue = f7;
            this.intensity = f8;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "x", "y", "z", "radius", "red", "green", "blue", "intensity");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "x", "y", "z", "radius", "red", "green", "blue", "intensity");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "x", "y", "z", "radius", "red", "green", "blue", "intensity");
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public float getZ() {
            return this.z;
        }

        public float getRadius() {
            return this.radius;
        }

        public float getRed() {
            return this.red;
        }

        public float getGreen() {
            return this.green;
        }

        public float getBlue() {
            return this.blue;
        }

        public float getIntensity() {
            return this.intensity;
        }
    }
}

