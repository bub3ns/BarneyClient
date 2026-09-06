/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 *  net.minecraft.ChatHudLine$Visible
 *  net.minecraft.ChatHud
 *  net.minecraft.ChatScreen
 *  net.minecraft.MutableText
 *  net.minecraft.OrderedText
 */
package moscow.rockstar.modules.visuals.waypoints;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.mixin.accessors.ChatHudAccessor;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.world.waypoints.WaypointStore;
import net.minecraft.text.Text;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import pyrock.events.render.ChatRenderEvent;
import pyrock.events.window.ChatClickEvent;

public class ChatWaypointListener
implements ClientAccess {
    private static final Pattern THREE_COORDINATE_PATTERN = Pattern.compile("(-?\\d{1,8})(?:\\s*,\\s*|\\s+)(-?\\d{1,8})(?:\\s*,\\s*|\\s+)(-?\\d{1,8})");
    private static final Pattern TWO_COORDINATE_PATTERN = Pattern.compile("(-?\\d{1,8})(?:\\s*,\\s*|\\s+)(-?\\d{1,8})");
    private static final String WAYPOINT_ID = "ChatWaypoint";
    private static final MutableText WAYPOINT_HOVER_TEXT = Text.literal((String)Localization.translate("chat_waypoint.hover"));
    private static final int DEFAULT_WAYPOINT_Y = 70;
    private final EventListener<ChatRenderEvent> chatRenderListener = this::renderWaypointHoverText;
    private final EventListener<ChatClickEvent> chatClickListener = this::handleWaypointClick;

    public ChatWaypointListener() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    private void renderWaypointHoverText(ChatRenderEvent chatRenderEvent) {
        if (!(ChatWaypointListener.minecraftClient.currentScreen instanceof ChatScreen) || ChatWaypointListener.minecraftClient.inGameHud == null) {
            return;
        }
        double d = ChatWaypointListener.minecraftClient.mouse.getX() * (double)minecraftClient.getWindow().getScaledWidth() / (double)minecraftClient.getWindow().getWidth();
        double d2 = ChatWaypointListener.minecraftClient.mouse.getY() * (double)minecraftClient.getWindow().getScaledHeight() / (double)minecraftClient.getWindow().getHeight();
        ChatHud Entry = ChatWaypointListener.minecraftClient.inGameHud.getChatHud();
        ChatHudAccessor chatHudAccessor = (ChatHudAccessor)Entry;
        double d3 = chatHudAccessor.invokeToChatLineX(d);
        double d4 = chatHudAccessor.invokeToChatLineY(d2);
        if (d3 < 0.0 || d4 < 0.0) {
            return;
        }
        int n = chatHudAccessor.invokeGetMessageLineIndex(d3, d4);
        List<ChatHudLine.Visible> list = chatHudAccessor.getVisibleMessages();
        if (n < 0 || n >= list.size()) {
            return;
        }
        ChatHudLine.Visible class_75902 = list.get(n);
        String string = this.flattenChatText(class_75902.content());
        if (string.isBlank()) {
            return;
        }
        if (this.containsWaypointCoordinatesAtCursor(string, d3)) {
            chatRenderEvent.getContext().drawTooltip(ChatWaypointListener.minecraftClient.textRenderer, List.of(WAYPOINT_HOVER_TEXT), (int)d, (int)d2);
        }
    }

    private void handleWaypointClick(ChatClickEvent chatClickEvent) {
        if (chatClickEvent.getButton() != 0) {
            return;
        }
        if (!(ChatWaypointListener.minecraftClient.currentScreen instanceof ChatScreen) || ChatWaypointListener.minecraftClient.inGameHud == null) {
            return;
        }
        ChatHud Entry = ChatWaypointListener.minecraftClient.inGameHud.getChatHud();
        ChatHudAccessor chatHudAccessor = (ChatHudAccessor)Entry;
        double d = chatHudAccessor.invokeToChatLineX(chatClickEvent.getX());
        double d2 = chatHudAccessor.invokeToChatLineY(chatClickEvent.getY());
        if (d < 0.0 || d2 < 0.0) {
            return;
        }
        int n = chatHudAccessor.invokeGetMessageLineIndex(d, d2);
        List<ChatHudLine.Visible> list = chatHudAccessor.getVisibleMessages();
        if (n < 0 || n >= list.size()) {
            return;
        }
        ChatHudLine.Visible class_75902 = list.get(n);
        String string = this.flattenChatText(class_75902.content());
        if (string.isBlank()) {
            return;
        }
        Matcher matcher = THREE_COORDINATE_PATTERN.matcher(string);
        while (matcher.find()) {
            if (!this.isTextRangeUnderCursor(d, string, matcher.start(), matcher.end())) continue;
            this.createWaypoint(matcher.group(1), matcher.group(2), matcher.group(3));
            return;
        }
        Matcher matcher2 = TWO_COORDINATE_PATTERN.matcher(string);
        while (matcher2.find()) {
            if (!this.isTextRangeUnderCursor(d, string, matcher2.start(), matcher2.end())) continue;
            this.createWaypoint(matcher2.group(1), String.valueOf(70), matcher2.group(2));
            return;
        }
    }

    private boolean containsWaypointCoordinatesAtCursor(String string, double d) {
        Matcher matcher = THREE_COORDINATE_PATTERN.matcher(string);
        while (matcher.find()) {
            if (!this.isTextRangeUnderCursor(d, string, matcher.start(), matcher.end())) continue;
            return true;
        }
        Matcher matcher2 = TWO_COORDINATE_PATTERN.matcher(string);
        while (matcher2.find()) {
            if (!this.isTextRangeUnderCursor(d, string, matcher2.start(), matcher2.end())) continue;
            return true;
        }
        return false;
    }

    private boolean isTextRangeUnderCursor(double d, String string, int n, int n2) {
        int n3 = ChatWaypointListener.minecraftClient.textRenderer.getWidth(string.substring(0, n));
        int n4 = ChatWaypointListener.minecraftClient.textRenderer.getWidth(string.substring(0, n2));
        return d >= (double)n3 && d <= (double)n4;
    }

    private void createWaypoint(String string, String string2, String string3) {
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
        if (waypointStore.containsWaypoint(WAYPOINT_ID)) {
            waypointStore.removeWaypoint(WAYPOINT_ID);
        }
        waypointStore.addWaypoint(WAYPOINT_ID, n3, n2, n);
    }

    private String flattenChatText(OrderedText class_54812) {
        StringBuilder stringBuilder = new StringBuilder();
        class_54812.accept((n, class_25832, n2) -> {
            stringBuilder.appendCodePoint(n2);
            return true;
        });
        return stringBuilder.toString();
    }
}
