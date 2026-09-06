/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 *  net.minecraft.Screen
 */
package moscow.rockstar.events.input;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.world.waypoints.WaypointStore;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.Screen;
import pyrock.events.window.KeyPressEvent;

public class ChatKeyBindingHandler
implements ClientAccess {
    private static final Pattern THREE_COORDINATE_PATTERN = Pattern.compile("(-?\\d{1,8})(?:\\s*,\\s*|\\s+)(-?\\d{1,8})(?:\\s*,\\s*|\\s+)(-?\\d{1,8})");
    private static final Pattern TWO_COORDINATE_PATTERN = Pattern.compile("(-?\\d{1,8})(?:\\s*,\\s*|\\s+)(-?\\d{1,8})");
    private static final int DEFAULT_WAYPOINT_Y = 70;
    private static final String WAYPOINT_CLIPBOARD_KEY = "ClipboardWaypoint";
    private final EventListener<KeyPressEvent> keyPressListener = this::handleKeyPress;

    public ChatKeyBindingHandler() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    private void handleKeyPress(KeyPressEvent keyPressEvent) {
        if (keyPressEvent.getAction() != 1) {
            return;
        }
        if (keyPressEvent.getKey() != 86) {
            return;
        }
        if (ChatKeyBindingHandler.minecraftClient.currentScreen != null || !Screen.hasControlDown()) {
            return;
        }
        String string = ChatKeyBindingHandler.minecraftClient.keyboard.getClipboard();
        if (string == null || string.isBlank()) {
            return;
        }
        Matcher matcher = THREE_COORDINATE_PATTERN.matcher(string);
        if (matcher.find()) {
            this.handleWaypointCoordinates(matcher.group(1), matcher.group(2), matcher.group(3));
            return;
        }
        Matcher matcher2 = TWO_COORDINATE_PATTERN.matcher(string);
        if (matcher2.find()) {
            this.handleWaypointCoordinates(matcher2.group(1), String.valueOf(70), matcher2.group(2));
        }
    }

    private void handleWaypointCoordinates(String string, String string2, String string3) {
        int n;
        int n2;
        int n3;
        try {
            n3 = Integer.parseInt(string);
            n2 = Integer.parseInt(string2);
            n = Integer.parseInt(string3);
        }
        catch (NumberFormatException numberFormatException) {
            n = 0;
            n2 = 0;
            n3 = 0;
            Notification.error(Text.of((String)Localization.translate("waypoints.error_coords")));
        }
        WaypointStore waypointStore = RockstarClient.create().getWaypointStore();
        if (waypointStore.containsWaypoint(WAYPOINT_CLIPBOARD_KEY)) {
            waypointStore.removeWaypoint(WAYPOINT_CLIPBOARD_KEY);
        }
        waypointStore.addWaypoint(WAYPOINT_CLIPBOARD_KEY, n3, n2, n);
    }
}

