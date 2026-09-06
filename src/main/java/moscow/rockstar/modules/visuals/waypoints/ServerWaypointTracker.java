/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.GameMessageS2CPacket
 */
package moscow.rockstar.modules.visuals.waypoints;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.settings.SettingGroupHeader;
import moscow.rockstar.modules.visuals.hud.DynamicIslandHud;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.utility.render.ColorRGBA;

public class ServerWaypointTracker
extends SettingGroupHeader {
    private final List<WaypointRecord> activeWaypoints = new CopyOnWriteArrayList<WaypointRecord>();
    private String title;
    private int currentServerIndex = -1;
    private final EventListener<ReceivePacketEvent> receivePacketListener = receivePacketEvent -> {
        Object object = receivePacketEvent.getPacket();
        if (object instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket class_74392 = (GameMessageS2CPacket)object;
            object = class_74392.content().getString().replaceAll("\\n", " ").replaceAll("[^\\p{L}\\p{N}\\s\\[\\]:.-]", "").replaceAll("\\s{2,}", " ").trim();
            if (class_74392.content().getString().contains("\u041f\u043e\u044f\u0432\u0438\u043b\u0441\u044f")) {
                Matcher matcher = Pattern.compile("\\[([^\\]]+)\\]").matcher((CharSequence)object);
                Matcher matcher2 = Pattern.compile("\u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u0430\u0445\\s+(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)").matcher((CharSequence)object);
                Matcher matcher3 = Pattern.compile("\u0423\u0440\u043e\u0432\u0435\u043d\u044c \u043b\u0443\u0442\u0430:\\s*(\\S+)").matcher((CharSequence)object);
                if (matcher.find() && matcher2.find()) {
                    String string = matcher.group(1);
                    String string2 = matcher3.find() ? matcher3.group(1) : null;
                    for (WaypointType waypointType : WaypointType.values()) {
                        if (!string.toLowerCase().contains(waypointType.getDisplayName().toLowerCase())) continue;
                        this.activeWaypoints.removeIf(waypointRecord -> waypointRecord.type == waypointType);
                        if (string2 != null && !string2.isEmpty()) {
                            String string3 = string2 + " " + waypointType.getDisplayName();
                            this.activeWaypoints.add(new WaypointRecord(string3, System.currentTimeMillis() + waypointType.getLifetimeMillis(), waypointType));
                            this.currentServerIndex = ServerDetector.defaultServerIndex;
                            if (this.currentServerIndex == ServerDetector.defaultServerIndex) {
                                RockstarClient.create().getWaypointStore().addWaypoint(string3, Integer.parseInt(matcher2.group(1)), Integer.parseInt(matcher2.group(2)), Integer.parseInt(matcher2.group(3)));
                            }
                        }
                        break;
                    }
                }
            } else {
                WaypointType[] waypointTypes = WaypointType.values();
                for (WaypointType waypointType : waypointTypes) {
                    if (!((String)object).equalsIgnoreCase(waypointType.getDisplayName())) continue;
                    this.title = waypointType.getDisplayName();
                    break;
                }
                Matcher coordinateMatcher = Pattern.compile("\u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b:?\\s*(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)", 2).matcher((CharSequence)object);
                if (((String)object).toLowerCase().startsWith("\u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b") && coordinateMatcher.find() && this.title != null) {
                    for (WaypointType waypointType : WaypointType.values()) {
                        if (!this.title.equalsIgnoreCase(waypointType.getDisplayName())) continue;
                        this.activeWaypoints.removeIf(waypointRecord -> waypointRecord.type == waypointType);
                        this.activeWaypoints.add(new WaypointRecord(waypointType.getDisplayName(), System.currentTimeMillis() + waypointType.getLifetimeMillis(), waypointType));
                        this.currentServerIndex = ServerDetector.defaultServerIndex;
                        RockstarClient.create().getWaypointStore().addWaypoint(waypointType.getDisplayName(), Integer.parseInt(coordinateMatcher.group(1)), Integer.parseInt(coordinateMatcher.group(2)), Integer.parseInt(coordinateMatcher.group(3)));
                        break;
                    }
                    this.title = null;
                }
            }
        }
    };
    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> {
        if (ServerDetector.defaultServerIndex != this.currentServerIndex) {
            this.activeWaypoints.forEach(waypointRecord -> RockstarClient.create().getWaypointStore().removeWaypoint(waypointRecord.getWaypointName()));
            this.activeWaypoints.clear();
            this.title = null;
        }
    };

    public ServerWaypointTracker(MultiBooleanSetting multiBooleanSetting) {
        super(multiBooleanSetting, "events");
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public void prepare(DynamicIslandHud island) {
        WaypointRecord waypointRecord;
        long l;
        long l2 = System.currentTimeMillis();
        this.removeExpiredWaypoints(l2);
        if (!this.activeWaypoints.isEmpty() && (l = (waypointRecord = this.activeWaypoints.getFirst()).getExpiresAt() - l2) > 0L) {
            int n = (int)(l / 1000L);
            int n2 = n / 60;
            int n3 = n % 60;
            String string = String.format("%d:%02d", n2, n3);
            ColorRGBA colorRGBA = this.getWaypointColor(waypointRecord.type);
            this.setHeaderInfo(Integer.parseInt(string.split(":")[0]) + ":", "", Integer.parseInt(string.split(":")[1]), waypointRecord.type.displayName, colorRGBA);
        }
        super.prepare(island);
    }

    private ColorRGBA getWaypointColor(WaypointType waypointType) {
        return switch (waypointType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> new ColorRGBA(138.0f, 43.0f, 226.0f);
            case 1 -> new ColorRGBA(255.0f, 69.0f, 0.0f);
            case 2 -> new ColorRGBA(255.0f, 140.0f, 0.0f);
            case 3 -> new ColorRGBA(70.0f, 130.0f, 180.0f);
            case 4 -> new ColorRGBA(243.0f, 196.0f, 82.0f);
            case 5 -> new ColorRGBA(139.0f, 222.0f, 221.0f);
            case 6 -> new ColorRGBA(141.0f, 99.0f, 184.0f);
            case 7 -> new ColorRGBA(41.0f, 253.0f, 5.0f);
            case 8 -> new ColorRGBA(90.0f, 158.0f, 152.0f);
        };
    }

    @Override
    public boolean isVisible() {
        this.removeExpiredWaypoints(System.currentTimeMillis());
        return !this.activeWaypoints.isEmpty();
    }

    private void removeExpiredWaypoints(long l) {
        this.activeWaypoints.removeIf(waypointRecord -> {
            if (l < waypointRecord.getExpiresAt()) {
                return false;
            }
            RockstarClient.create().getWaypointStore().removeWaypoint(waypointRecord.getWaypointName());
            return true;
        });
    }

    static final class WaypointRecord {
        private final String waypointName;
        private final long expiresAt;
        final WaypointType type;

        WaypointRecord(String string, long l, WaypointType waypointType) {
            this.waypointName = string;
            this.expiresAt = l;
            this.type = waypointType;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "waypointName", "expiresAt", "type");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "waypointName", "expiresAt", "type");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "waypointName", "expiresAt", "type");
        }

        public String getWaypointName() {
            return this.waypointName;
        }

        public long getExpiresAt() {
            return this.expiresAt;
        }

        public WaypointType getType() {
            return this.type;
        }
    }

    static enum WaypointType {
        MYSTICAL_ALTAR("\u041c\u0438\u0441\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u0439 \u0410\u043b\u0442\u0430\u0440\u044c", 360000L),
        KILLER_BEACON("\u041c\u0430\u044f\u043a \u0423\u0431\u0438\u0439\u0446\u0430", 360000L),
        VOLCANO("\u0412\u0443\u043b\u043a\u0430\u043d", 300000L),
        METEOR_SHOWER("\u041c\u0435\u0442\u0435\u043e\u0440\u0438\u0442\u043d\u044b\u0439 \u0434\u043e\u0436\u0434\u044c", 180000L),
        SUPPLY_DROP("\u041f\u043e\u0441\u044b\u043b\u043a\u0430", 180000L),
        BOSS("\u0411\u043e\u0441\u0441", 180000L),
        CONTAINER("\u041a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440", 180000L),
        CARGO("\u0413\u0440\u0443\u0437", 180000L),
        MYSTERIOUS_SHIP("\u0422\u0430\u0438\u043d\u0441\u0442\u0432\u0435\u043d\u043d\u044b\u0439 \u043a\u043e\u0440\u0430\u0431\u043b\u044c", 300000L);
        final String displayName;
        final long lifetimeMillis;

        @Generated
        public String getDisplayName() {
            return this.displayName;
        }

        @Generated
        public long getLifetimeMillis() {
            return this.lifetimeMillis;
        }

        @Generated
        private WaypointType(String string2, long l) {
            this.displayName = string2;
            this.lifetimeMillis = l;
        }
}
}

