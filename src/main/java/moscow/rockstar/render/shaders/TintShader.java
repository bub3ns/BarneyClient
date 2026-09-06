/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GlUniform
 *  net.minecraft.VertexFormats
 *  net.minecraft.Identifier
 */
package moscow.rockstar.render.shaders;

import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.shaders.ShaderProgramBase;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import ua.mintantileak.spk.Compile;

public class TintShader
extends ShaderProgramBase
implements WindowHandle {
    private GlUniform resolutionUniform;
    private GlUniform offsetUniform;
    private GlUniform saturationUniform;
    private GlUniform tintIntensityUniform;
    private GlUniform tintColorUniform;

    public TintShader(Identifier class_29602) {
        super(class_29602, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Compile
    public void setTintParameters(float f) {
        this.offsetUniform.set(f);
        this.resolutionUniform.set(1.0f / (float)WINDOW.getScaledWidth(), 1.0f / (float)WINDOW.getScaledHeight());
        this.saturationUniform.set(1.0f);
        this.tintIntensityUniform.set(0.0f);
        this.tintColorUniform.set(1.0f, 1.0f, 1.0f);
    }

    public void setTintParameters(float f, int n, int n2) {
        float f2 = n > 0 ? 1.0f / (float)n : 0.0f;
        float f3 = n2 > 0 ? 1.0f / (float)n2 : 0.0f;
        this.offsetUniform.set(f);
        this.resolutionUniform.set(f2, f3);
        this.saturationUniform.set(1.0f);
        this.tintIntensityUniform.set(0.0f);
        this.tintColorUniform.set(1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void initializeShaderUniforms() {
        this.resolutionUniform = this.getUniform("Resolution");
        this.offsetUniform = this.getUniform("Offset");
        this.saturationUniform = this.getUniform("Saturation");
        this.tintIntensityUniform = this.getUniform("TintIntensity");
        this.tintColorUniform = this.getUniform("TintColor");
        super.initializeShaderUniforms();
    }
}
