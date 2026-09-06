/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.modules.player.farming.hud;

import moscow.rockstar.ui.localization.Localization;

public enum FarmDisplayCategory {
    BLOCKS("blocks"),
    POTIONS("potions"),
    SALES("sales"),
    ITEMS("items");
    private final String displayKey;

    private FarmDisplayCategory(String string2) {
        this.displayKey = "modules.auto_farm.unit." + string2;
    }

    public String getDisplayKey() {
        return Localization.translate(this.displayKey);
    }
}

