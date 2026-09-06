/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.modules.player.farming.core;

import moscow.rockstar.ui.localization.Localization;

public enum FarmState {
    IDLE("idle"),
    WORKING("working"),
    MOVING("moving"),
    PLANTING("planting"),
    GROWING("growing"),
    PICKUP("pickup"),
    DEPOSIT("deposit"),
    SELLING("selling"),
    BUYING("buying"),
    CRAFTING("crafting"),
    REPAIRING("repairing"),
    RESTOCKING("restocking");
    private final String stateKey;

    private FarmState(String string2) {
        this.stateKey = "modules.auto_farm.phase." + string2;
    }

    public String getStateKey() {
        return Localization.translate(this.stateKey);
    }
}

