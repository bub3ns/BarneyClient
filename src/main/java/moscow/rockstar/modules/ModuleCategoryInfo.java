/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.modules;

import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.render.hand.HandSwingSettings;
import moscow.rockstar.ui.theme.IconStyle;

public enum ModuleCategoryInfo {
    COMBAT("Combat", ModuleCategory.COMBAT, IconStyle.COMBAT_ICON, IconStyle.COMBAT_SELECTED_ICON),
    MOVEMENT("Movement", ModuleCategory.MOVEMENT, IconStyle.MOVEMENT_ICON, IconStyle.MOVEMENT_SELECTED_ICON),
    VISUALS("Visuals", ModuleCategory.VISUALS, IconStyle.VISUALS_ICON, IconStyle.VISUALS_SELECTED_ICON),
    PLAYER("Player", ModuleCategory.PLAYER, IconStyle.PLAYER_ICON, IconStyle.PLAYER_SELECTED_ICON),
    OTHER("Other", ModuleCategory.OTHER, IconStyle.OTHER_ICON, IconStyle.OTHER_SELECTED_ICON);
    private final String displayName;
    private final ModuleCategory category;
    private final IconStyle iconStyle;
    private final IconStyle selectedIconStyle;
    private HandSwingSettings categorySwingSettings;
    public String getDisplayName() {
        return this.displayName;
    }

    public ModuleCategory getCategory() {
        return this.category;
    }

    public IconStyle getIconStyle() {
        return this.iconStyle;
    }

    public IconStyle getSelectedIconStyle() {
        return this.selectedIconStyle;
    }

    public HandSwingSettings getCategorySwingSettings() {
        return this.categorySwingSettings;
    }

    ModuleCategoryInfo(String string2, ModuleCategory moduleCategory, IconStyle iconStyle, IconStyle iconStyle2) {
        this.displayName = string2;
        this.category = moduleCategory;
        this.iconStyle = iconStyle;
        this.selectedIconStyle = iconStyle2;
    }

    public void setCategorySwingSettings(HandSwingSettings handSwingSettings) {
        this.categorySwingSettings = handSwingSettings;
    }
}
