/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.items;

import lombok.Generated;

public enum ItemCategory {
    ALL("\u0412\u0441\u0435"),
    CONSUMABLES("\u0420\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438"),
    POTIONS("\u0417\u0435\u043b\u044c\u044f"),
    OTHER("\u0414\u0440\u0443\u0433\u043e\u0435");
    private final String categoryLabel;

    @Generated
    private ItemCategory(String string2) {
        this.categoryLabel = string2;
    }

    @Generated
    public String getCategoryLabel() {
        return this.categoryLabel;
    }
}

