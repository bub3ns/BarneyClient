package moscow.rockstar.render.shaders;

import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

/** Shader wrapper for the temporal, brightness-aware UI state texture. */
public final class AdaptiveStateShader extends ShaderProgramBase {
    public AdaptiveStateShader(Identifier identifier) {
        super(identifier, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    public void setStateParameters(float thresholdLow, float thresholdHigh,
                                   float probeX, float probeY, float rate) {
        this.getUniform("Threshold").set(thresholdLow, thresholdHigh);
        this.getUniform("Probe").set(probeX, probeY);
        this.getUniform("Rate").set(rate);
    }
}
