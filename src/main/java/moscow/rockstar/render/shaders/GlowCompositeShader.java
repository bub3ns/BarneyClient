package moscow.rockstar.render.shaders;

import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

/** Shader wrapper for compositing a sharp capture with its blurred glow. */
public final class GlowCompositeShader extends ShaderProgramBase {
    private GlUniform resolutionUniform;
    private GlUniform offsetUniform;
    private GlUniform outlineStrengthUniform;
    private GlUniform outlineRadiusUniform;
    private GlUniform glowOffsetUniform;

    public GlowCompositeShader(Identifier identifier) {
        super(identifier, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    public void setBlurParameters(float offset, int width, int height) {
        this.offsetUniform.set(offset);
        this.resolutionUniform.set(
            width > 0 ? 1.0f / width : 0.0f,
            height > 0 ? 1.0f / height : 0.0f
        );
    }

    public void setCompositeParameters(float offset, int width, int height,
                                       float outlineStrength, float outlineRadius,
                                       float glowOffsetX, float glowOffsetY) {
        this.setBlurParameters(offset, width, height);
        if (this.outlineStrengthUniform != null) {
            this.outlineStrengthUniform.set(outlineStrength);
        }
        if (this.outlineRadiusUniform != null) {
            this.outlineRadiusUniform.set(outlineRadius);
        }
        if (this.glowOffsetUniform != null) {
            this.glowOffsetUniform.set(glowOffsetX, glowOffsetY);
        }
    }

    @Override
    protected void initializeShaderUniforms() {
        this.resolutionUniform = this.getUniform("Resolution");
        this.offsetUniform = this.getUniform("Offset");
        this.outlineStrengthUniform = this.getUniform("OutlineStrength");
        this.outlineRadiusUniform = this.getUniform("OutlineRadius");
        this.glowOffsetUniform = this.getUniform("GlowOffset");
        super.initializeShaderUniforms();
    }
}
