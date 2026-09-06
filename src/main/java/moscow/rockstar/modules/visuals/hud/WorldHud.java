package moscow.rockstar.modules.visuals.hud;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.util.NumberFormatting;
import net.minecraft.client.MinecraftClient;

/** Coordinates / server / TPS row. 1:1 with rockstar/ilIlil/IiiIIiIIi. */
public class WorldHud extends HudRowElement {
    private final HudRowEntry coords;
    private final HudRowEntry server;
    private final HudRowEntry tps;
    private final BooleanSetting compactServer;

    public WorldHud() {
        super("hud.world", "hud/world");
        this.coords = new HudRowEntry(this.elements, "coords");
        this.server = new HudRowEntry(this.elements, "server");
        this.tps = new HudRowEntry(this.elements, "TPS", "TPS");
        this.compactServer = new BooleanSetting(this, "hud.world.compact_server").enable();
        this.showing = true;
        this.pos(2.5f, 4.175f);
    }

    @Override
    public void update(RockstarDrawContext drawContext) {
        super.update(drawContext);
        NameProtect nameProtect = RockstarClient.create().getModuleRegistry().getModule(NameProtect.class);
        if (nameProtect != null && nameProtect.isStreamerModeActive()) {
            this.coords.set("\u2014 \u2014 \u2014", "\u2014 \u2014 \u2014");
            this.server.set("\u2014", "\u2014");
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        String position = String.format("%s %s %s",
            Math.round(client.player.getX()),
            Math.round(client.player.getY()),
            Math.round(client.player.getZ()));
        this.coords.set(position, position);
        this.server.set(ServerDetector.getFormattedServerName(this.compactServer.isEnabled()),
                        ServerDetector.getConnectedServerAddress());
        this.tps.set(NumberFormatting.formatOneDecimal(
                RockstarClient.create().getServerTickRateTracker().getTicksPerSecond())
            .replace(",", ".").replace(".0", ""));
    }
}
