/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3d
 *  net.minecraft.Text
 */
package moscow.rockstar.world.waypoints;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;

public class WaypointStore {
    private final Map<String, Vec3d> waypointsByName = new HashMap<String, Vec3d>();

    public final void addWaypoint(String string, int n, int n2, int n3) {
        Vec3d VanillaChestLootTableGenerator = new Vec3d((double)n, (double)n2, (double)n3);
        this.waypointsByName.put(string, VanillaChestLootTableGenerator);
        Notification.info(Text.of((String)Localization.translateFormatted("modules.waypoints.added", string, n, n2, n3)));
    }

    public final void removeWaypoint(String string) {
        if (this.waypointsByName.remove(string) != null) {
            Notification.info(Text.of((String)Localization.translateFormatted("modules.waypoints.deleted", string)));
        } else {
            Notification.info(Text.of((String)Localization.translateFormatted("modules.waypoints.not_found", string)));
        }
    }

    public final void clearWaypoints() {
        this.waypointsByName.clear();
        Notification.info(Text.of((String)Localization.translate("modules.waypoints.cleared")));
    }

    public final boolean containsWaypoint(String string) {
        return this.waypointsByName.containsKey(string);
    }

    public final Set<Map.Entry<String, Vec3d>> getWaypoints() {
        return this.waypointsByName.entrySet();
    }

    public final void replaceWaypoints(Map<String, Vec3d> map) {
        this.waypointsByName.clear();
        this.waypointsByName.putAll(map);
    }
}

