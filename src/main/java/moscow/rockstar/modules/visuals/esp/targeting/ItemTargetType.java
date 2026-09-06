/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.modules.visuals.esp.targeting;

import lombok.Generated;

public enum ItemTargetType {
    HELD("held", "esp.targets.items.held"),
    DROPPED("dropped", "esp.targets.items.dropped");
    private final String targetKey;
    private final String translationKey;

    private ItemTargetType(String string2, String string3) {
        this.targetKey = string2;
        this.translationKey = string3;
    }

    public static ItemTargetType fromTargetKey(String string) {
        for (ItemTargetType itemTargetType : ItemTargetType.values()) {
            if (!itemTargetType.targetKey.equals(string)) continue;
            return itemTargetType;
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
}

