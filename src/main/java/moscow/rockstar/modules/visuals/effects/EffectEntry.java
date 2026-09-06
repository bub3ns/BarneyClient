package moscow.rockstar.modules.visuals.effects;

import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.particles.AmbientParticleRenderer;

/**
 * A single entry rendered by the effects panel.
 *
 * The decompiled bytecode still contains the old compiler-facing bridge names
 * ({@code getValue}, {@code handle}, and {@code isValid}).  Keeping those as
 * interface adapters lets the remapped implementations expose their actual
 * responsibilities without changing their rendering behaviour.
 */
public interface EffectEntry {
    float getHeight();

    void render(RockstarDrawContext drawContext, float x, float y, float width);

    boolean handleClick(AmbientParticleRenderer input, double mouseX, double mouseY, int button);

    default float getValue() {
        return this.getHeight();
    }

    default void handle(RockstarDrawContext drawContext, float x, float y, float width) {
        this.render(drawContext, x, y, width);
    }

    default boolean isValid(AmbientParticleRenderer input, double mouseX, double mouseY, int button) {
        return this.handleClick(input, mouseX, mouseY, button);
    }
}
