/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.modules;

public enum ModuleCategory {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    VISUALS("Visuals"),
    PLAYER("Player"),
    OTHER("Other");

    private final String displayName;

    public static ModuleCategory fromName(String string) {
        for (ModuleCategory moduleCategory : ModuleCategory.values()) {
            if (!moduleCategory.name().equalsIgnoreCase(string) && !moduleCategory.displayName.equalsIgnoreCase(string)) continue;
            return moduleCategory;
        }
        return null;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    ModuleCategory(String string2) {
        this.displayName = string2;
    }
}
