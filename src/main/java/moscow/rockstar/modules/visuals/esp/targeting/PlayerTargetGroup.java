/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.modules.visuals.esp.targeting;

import lombok.Generated;

public enum PlayerTargetGroup {
    OTHERS("others", "esp.targets.players.others"),
    LOCAL_PLAYER("local", "esp.targets.players.local"),
    FRIENDS("friends", "esp.targets.players.friends"),
    ROCKSTAR_USERS("rockstar_users", "esp.targets.players.rockstar_users");
    private final String targetKey;
    private final String translationKey;

    private PlayerTargetGroup(String string2, String string3) {
        this.targetKey = string2;
        this.translationKey = string3;
    }

    public static PlayerTargetGroup fromTargetKey(String string) {
        for (PlayerTargetGroup playerTargetGroup : PlayerTargetGroup.values()) {
            if (!playerTargetGroup.targetKey.equals(string)) continue;
            return playerTargetGroup;
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

