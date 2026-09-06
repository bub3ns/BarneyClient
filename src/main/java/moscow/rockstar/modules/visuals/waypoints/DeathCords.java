/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 *  net.minecraft.DeathScreen
 */
package moscow.rockstar.modules.visuals.waypoints;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.world.waypoints.WaypointStore;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.DeathScreen;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Death Cords", category=ModuleCategory.OTHER, description="modules.descriptions.death_cords")
public class DeathCords
extends Module {
    private boolean deathDetected;
    private BooleanSetting createDeathWaypoint;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (DeathCords.minecraftClient.currentScreen instanceof DeathScreen && DeathCords.minecraftClient.player != null) {
            if (this.deathDetected) {
                int n = (int)DeathCords.minecraftClient.player.getX();
                int n2 = (int)DeathCords.minecraftClient.player.getY();
                int n3 = (int)DeathCords.minecraftClient.player.getZ();
                Notification.info(Text.of((String)Localization.translateFormatted("death_cords.coords", n + " " + n2 + " " + n3)));
                if (this.createDeathWaypoint.isEnabled()) {
                    WaypointStore waypointStore = RockstarClient.create().getWaypointStore();
                    if (waypointStore.containsWaypoint("Death")) {
                        waypointStore.removeWaypoint("Death");
                    }
                    waypointStore.addWaypoint("Death", n, n2, n3);
                }
                this.deathDetected = false;
            }
        } else {
            this.deathDetected = true;
        }
    };

    public DeathCords() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.createDeathWaypoint = new BooleanSetting(this, "modules.settings.death_cords.waypoint");
    }
}

