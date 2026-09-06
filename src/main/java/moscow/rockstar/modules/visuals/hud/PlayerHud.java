package moscow.rockstar.modules.visuals.hud;

import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import net.minecraft.client.MinecraftClient;

/** FPS / BPS row. 1:1 with rockstar/ilIlil/IiiIIiIII. */
public class PlayerHud extends HudRowElement {
    private final HudRowEntry fps;
    private final HudRowEntry speed;
    private final BooleanSetting includeVerticalSpeed;
    private final Animation fpsAnimation;

    public PlayerHud() {
        super("hud.player", "hud/player");
        this.fps = new HudRowEntry(this.elements, "FPS", "FPS");
        this.speed = new HudRowEntry(this.elements, "speed", "BPS");
        this.includeVerticalSpeed = new BooleanSetting(this, "hud.player.speedY").enable();
        this.fpsAnimation = new Animation(300L, 0.0f, Easing.smoothStep);
        this.showing = true;
        this.pos(2.5f, 22.667f);
    }

    @Override
    public void update(RockstarDrawContext drawContext) {
        super.update(drawContext);
        MinecraftClient client = MinecraftClient.getInstance();
        double horizontal = Math.hypot(
            client.player.getX() - client.player.prevX,
            client.player.getZ() - client.player.prevZ);
        double value = !this.includeVerticalSpeed.isEnabled()
            ? horizontal
            : Math.hypot(client.player.getY() - client.player.prevY, horizontal);
        this.speed.set(String.format("%.2f", value * 20.0).replace(",", "."));
        this.fps.set("" + Math.round(this.fpsAnimation.update(MinecraftClient.getInstance().getCurrentFps())));
    }
}
