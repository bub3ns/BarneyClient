package moscow.rockstar.render.world;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.shaders.TimedAccentShader;
import net.minecraft.util.Identifier;

/** The bake and view programs belonging to one procedural sky. */
public final class SkyboxShaderPair {
    private final TimedAccentShader bakeShader;
    private final TimedAccentShader viewShader;

    public SkyboxShaderPair(Identifier bakeId, Identifier viewId) {
        this.bakeShader = new TimedAccentShader(bakeId);
        this.viewShader = new TimedAccentShader(viewId);
    }

    public SkyboxShaderPair(String bakePath, String viewPath) {
        this(RockstarClient.resourceId(bakePath), RockstarClient.resourceId(viewPath));
    }

    public TimedAccentShader bakeShader() {
        return this.bakeShader;
    }

    public TimedAccentShader viewShader() {
        return this.viewShader;
    }
}
