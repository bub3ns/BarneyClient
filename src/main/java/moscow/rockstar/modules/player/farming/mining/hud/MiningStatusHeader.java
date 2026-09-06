/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ArmorStandEntity
 *  net.minecraft.Vec3d
 */
package moscow.rockstar.modules.player.farming.mining.hud;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.settings.SettingGroupHeader;
import moscow.rockstar.modules.visuals.hud.DynamicIslandHud;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Vec3d;
import pyrock.utility.render.ColorRGBA;

public class MiningStatusHeader
extends SettingGroupHeader
implements ClientAccess {
    private final Pattern remainingTimePattern = Pattern.compile("(\\d{1,2}):(\\d{2})");
    private MiningStatusTime cachedMiningTime;
    private long cacheTimestamp;
    private Vec3d miningAnchorPosition;
    private MiningStatusTime pendingMiningTime;
    private boolean cachePending;

    public MiningStatusHeader(MultiBooleanSetting multiBooleanSetting) {
        super(multiBooleanSetting, "mine");
    }

    public final void prepare(DynamicIslandHud island) {
        if (!this.hasWorldAndPlayer()) {
            this.cachePending = false;
            super.prepare(island);
            return;
        }
        MiningStatusTime miningStatusTime = this.consumePendingMiningTime();
        if (miningStatusTime == null) {
            miningStatusTime = this.buildMiningTime(this.findMiningAnchor(ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD)));
        }
        if (miningStatusTime == null) {
            super.prepare(island);
            return;
        }
        this.setHeaderInfo(miningStatusTime.getMinutes() + ":", "", miningStatusTime.getSeconds(), miningStatusTime.getResourceType().getDisplayName(), miningStatusTime.getResourceType().getColor());
        super.prepare(island);
    }

    @Override
    public final boolean isVisible() {
        MiningStatusTime miningStatusTime;
        this.cachePending = false;
        if (!this.hasWorldAndPlayer()) {
            return false;
        }
        this.pendingMiningTime = miningStatusTime = this.buildMiningTime(this.findMiningAnchor(ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD)));
        this.cachePending = true;
        return miningStatusTime != null;
    }

    private MiningStatusTime consumePendingMiningTime() {
        if (!this.cachePending) {
            return null;
        }
        this.cachePending = false;
        return this.pendingMiningTime;
    }

    private boolean hasWorldAndPlayer() {
        return MiningStatusHeader.minecraftClient.world != null && MiningStatusHeader.minecraftClient.player != null;
    }

    private MiningStatusTime buildMiningTime(MiningStatusCandidates miningStatusCandidates) {
        MiningStatusTime miningStatusTime;
        long l = System.currentTimeMillis();
        if (miningStatusCandidates != null && !miningStatusCandidates.armorStandCandidates.isEmpty() && (miningStatusTime = this.parseMiningTime(miningStatusCandidates.armorStandCandidates)) != null) {
            this.cachedMiningTime = miningStatusTime;
            this.cacheTimestamp = l;
            this.miningAnchorPosition = miningStatusCandidates.anchorPosition;
            return miningStatusTime;
        }
        if (!this.hasCachedMiningTime()) {
            return null;
        }
        if (this.miningAnchorPosition == null || !this.isWithinSearchRadius(this.miningAnchorPosition)) {
            return null;
        }
        int n = (int)((l - this.cacheTimestamp) / 1000L);
        int n2 = this.cachedMiningTime.getTotalSeconds() - n;
        if (n2 <= 0) {
            this.cachedMiningTime = null;
            this.miningAnchorPosition = null;
            return null;
        }
        return new MiningStatusTime(n2 / 60, n2 % 60, this.cachedMiningTime.getResourceType());
    }

    private MiningStatusCandidates findMiningAnchor(boolean bl) {
        List<ArmorStandEntity> list = this.findNearbyMiningStands();
        if (list.isEmpty()) {
            return null;
        }
        ArmorStandEntity class_15313 = this.selectMiningStand(bl, list);
        if (class_15313 == null) {
            return null;
        }
        Vec3d VanillaChestLootTableGenerator = class_15313.getPos();
        List<ArmorStandEntity> list2 = list.stream().filter(class_15312 -> this.horizontalDistanceSquared(class_15312.getPos(), VanillaChestLootTableGenerator) <= 64.0).sorted(Comparator.comparingDouble(class_15312 -> -class_15312.getY())).toList();
        return new MiningStatusCandidates(VanillaChestLootTableGenerator, list2);
    }

    private ArmorStandEntity selectMiningStand(boolean bl, List<ArmorStandEntity> list) {
        if (list.isEmpty()) {
            return null;
        }
        List<String> list2 = bl ? List.of("\u0448\u0430\u0445\u0442\u0430", "\u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c", "\u043e\u0431\u044b\u0447\u043d\u0430\u044f", "\u0440\u0435\u0434\u043a\u0430\u044f", "\u044d\u043f\u0438\u0447\u0435\u0441\u043a\u0430\u044f", "\u043b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u0430\u044f", "\u043c\u0438\u0444\u0438\u0447\u0435\u0441\u043a\u0430\u044f") : List.of("\u0430\u0432\u0442\u043e-\u0448\u0430\u0445\u0442\u0430", "\u0448\u0430\u0445\u0442\u0430", "\u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0430\u044f", "\u043e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435", "\u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c");
        return list.stream().filter(class_15312 -> {
            String string = class_15312.getCustomName().getString().toLowerCase(Locale.ROOT);
            if (this.isEndMarker(string)) {
                return false;
            }
            return list2.stream().anyMatch(string::contains);
        }).min(Comparator.comparingDouble(this::distanceToPlayer)).orElse(null);
    }

    private List<ArmorStandEntity> findNearbyMiningStands() {
        return StreamSupport.stream(MiningStatusHeader.minecraftClient.world.getEntities().spliterator(), false).filter(class_12972 -> class_12972 instanceof ArmorStandEntity).map(class_12972 -> (ArmorStandEntity)class_12972).filter(class_15312 -> class_15312.isAlive() && class_15312.getCustomName() != null).filter(class_15312 -> this.isWithinSearchRadius(class_15312.getPos())).toList();
    }

    private MiningStatusTime parseMiningTime(List<ArmorStandEntity> list) {
        MiningResourceType detectedResource;
        String standText;
        String string = null;
        MiningResourceType resourceType = MiningResourceType.ORDINARY;
        for (int i = 0; i < list.size(); ++i) {
            String string2 = list.get(i).getCustomName().getString().toLowerCase(Locale.ROOT);
            if (this.isEndMarker(string2) || !string2.contains("\u0442\u0435\u043a\u0443\u0449")) continue;
            detectedResource = MiningResourceType.fromDisplayName(string2);
            if (detectedResource != null) {
                resourceType = detectedResource;
                break;
            }
            if (i + 1 >= list.size() || (detectedResource = MiningResourceType.fromDisplayName(list.get(i + 1).getCustomName().getString())) == null) continue;
            resourceType = detectedResource;
            break;
        }
        if (resourceType == MiningResourceType.ORDINARY) {
            for (ArmorStandEntity class_15312 : list) {
                standText = class_15312.getCustomName().getString().toLowerCase(Locale.ROOT);
                detectedResource = MiningResourceType.fromDisplayName(standText);
                if (detectedResource == null) continue;
                resourceType = detectedResource;
                break;
            }
        }
        for (ArmorStandEntity class_15313 : list) {
            standText = class_15313.getCustomName().getString().toLowerCase(Locale.ROOT);
            Matcher timeMatcher = this.remainingTimePattern.matcher(standText);
            if (timeMatcher.find()) {
                string = timeMatcher.group();
                break;
            }
            if (!standText.contains("\u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c")) continue;
            int n = this.extractNumberBetween(standText, "", "\u043c\u0438\u043d.");
            int n2 = this.extractNumberBetween(standText, "\u043c\u0438\u043d.", "\u0441\u0435\u043a.");
            if (n < 0 || n2 < 0) continue;
            string = String.format("%d:%02d", n, n2);
            break;
        }
        if (string == null) {
            return null;
        }
        String[] stringArray = string.split(":");
        return new MiningStatusTime(Integer.parseInt(stringArray[0]), Integer.parseInt(stringArray[1]), resourceType);
    }

    private boolean isEndMarker(String string) {
        return string.contains("\u044d\u043d\u0434\u0430") || string.contains("\u0430\u0434\u0430") || string.contains("\u044d\u043d\u0434") || string.contains("\u0430\u0434");
    }

    private int extractNumberBetween(String string, String string2, String string3) {
        try {
            int n = string2.isEmpty() ? 0 : string.indexOf(string2) + string2.length();
            int n2 = string.indexOf(string3, n);
            if (n >= 0 && n2 > n) {
                String string4 = string.substring(n, n2).replaceAll("[^0-9]", "");
                return string4.isEmpty() ? -1 : Integer.parseInt(string4);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return -1;
    }

    private boolean hasCachedMiningTime() {
        if (this.cachedMiningTime == null) {
            return false;
        }
        int n = (int)((System.currentTimeMillis() - this.cacheTimestamp) / 1000L);
        return this.cachedMiningTime.getTotalSeconds() - n > 0;
    }

    private boolean isWithinSearchRadius(Vec3d VanillaChestLootTableGenerator) {
        return this.horizontalDistanceSquared(VanillaChestLootTableGenerator, MiningStatusHeader.minecraftClient.player.getPos()) <= 900.0;
    }

    private double distanceToPlayer(ArmorStandEntity class_15312) {
        return this.horizontalDistanceSquared(class_15312.getPos(), MiningStatusHeader.minecraftClient.player.getPos());
    }

    private double horizontalDistanceSquared(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock) {
        double d = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d2 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        return d * d + d2 * d2;
    }

    static final class MiningStatusTime {
        private final int minutes;
        private final int seconds;
        private final MiningResourceType resourceType;

        MiningStatusTime(int n, int n2, MiningResourceType miningResourceType) {
            this.minutes = n;
            this.seconds = n2;
            this.resourceType = miningResourceType;
        }

        public int getTotalSeconds() {
            return this.minutes * 60 + this.seconds;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "minutes", "seconds", "resourceType");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "minutes", "seconds", "resourceType");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "minutes", "seconds", "resourceType");
        }

        public int getMinutes() {
            return this.minutes;
        }

        public int getSeconds() {
            return this.seconds;
        }

        public MiningResourceType getResourceType() {
            return this.resourceType;
        }
    }

    static final class MiningStatusCandidates {
        final Vec3d anchorPosition;
        final List<ArmorStandEntity> armorStandCandidates;

        MiningStatusCandidates(Vec3d VanillaChestLootTableGenerator, List<ArmorStandEntity> list) {
            this.anchorPosition = VanillaChestLootTableGenerator;
            this.armorStandCandidates = list;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "anchorPosition", "armorStandCandidates");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "anchorPosition", "armorStandCandidates");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "anchorPosition", "armorStandCandidates");
        }

        public Vec3d getAnchorPosition() {
            return this.anchorPosition;
        }

        public List<ArmorStandEntity> getArmorStandCandidates() {
            return this.armorStandCandidates;
        }
    }

    static enum MiningResourceType {
        ORDINARY("\u041e\u0431\u044b\u0447\u043d\u0430\u044f", new ColorRGBA(243.0f, 151.0f, 250.0f)),
        RARE("\u0420\u0435\u0434\u043a\u0430\u044f", new ColorRGBA(243.0f, 151.0f, 250.0f)),
        EPIC("\u042d\u043f\u0438\u0447\u0435\u0441\u043a\u0430\u044f", new ColorRGBA(231.0f, 0.0f, 250.0f)),
        LEGENDARY("\u041b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u0430\u044f", new ColorRGBA(0.0f, 128.0f, 250.0f)),
        MYTHIC("\u041c\u0438\u0444\u0438\u0447\u0435\u0441\u043a\u0430\u044f", new ColorRGBA(252.0f, 84.0f, 252.0f));
        private final String displayName;
        private final ColorRGBA color;

        public static MiningResourceType fromDisplayName(String string) {
            if (string == null) {
                return null;
            }
            String string2 = string.toLowerCase(Locale.ROOT);
            if (string2.contains("\u044d\u043d\u0434\u0430") || string2.contains("\u0430\u0434\u0430") || string2.contains("\u044d\u043d\u0434") || string2.contains("\u0430\u0434")) {
                return null;
            }
            for (MiningResourceType miningResourceType : MiningResourceType.values()) {
                if (!string2.contains(miningResourceType.displayName.toLowerCase(Locale.ROOT))) continue;
                return miningResourceType;
            }
            return null;
        }

        @Generated
        public String getDisplayName() {
            return this.displayName;
        }

        @Generated
        public ColorRGBA getColor() {
            return this.color;
        }

        @Generated
        private MiningResourceType(String string2, ColorRGBA colorRGBA) {
            this.displayName = string2;
            this.color = colorRGBA;
        }
}
}

