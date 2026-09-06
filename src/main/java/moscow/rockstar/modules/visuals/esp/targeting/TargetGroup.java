/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.modules.visuals.esp.targeting;

import lombok.Generated;

public enum TargetGroup {
    PLAYERS("players", "esp.targets.players", true),
    MOBS("mobs", "esp.targets.mobs", false),
    ANIMALS("animals", "esp.targets.animals", false),
    ITEMS("items", "esp.targets.items", true);
    private final String targetKey;
    private final String translationKey;
    private final boolean enabledAtStartup;

    private TargetGroup(String string2, String string3, boolean bl) {
        this.targetKey = string2;
        this.translationKey = string3;
        this.enabledAtStartup = bl;
    }

    public boolean isEnabledAtStartup() {
        return this.enabledAtStartup;
    }

    public static TargetGroup fromTargetKey(String string) {
        for (TargetGroup targetGroup : TargetGroup.values()) {
            if (!targetGroup.targetKey.equals(string)) continue;
            return targetGroup;
        }
        return null;
    }

    @Generated
    public String getTargetKey() {
        return this.targetKey;
    }

    @Generated
    public String getTranslationKey() {
        return this.translationKey;
    }

    @Generated
    public boolean isActive() {
        return this.enabledAtStartup;
    }
}

