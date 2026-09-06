/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 */
package moscow.rockstar.ui.screens;

import moscow.rockstar.items.assist.AssistItemProvider;
import net.minecraft.item.ItemStack;

public final class AssistItem {
    private final AssistItemProvider provider;
    private final String displayName;
    private final ItemStack icon;
    private final int keyCode;

    public AssistItem(AssistItemProvider provider, String displayName, ItemStack icon, int keyCode) {
        this.provider = provider;
        this.displayName = displayName;
        this.icon = icon;
        this.keyCode = keyCode;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "provider", "displayName", "icon", "keyCode");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "provider", "displayName", "icon", "keyCode");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "provider", "displayName", "icon", "keyCode");
    }

    public AssistItemProvider getProvider() {
        return this.provider;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public int getKeyCode() {
        return this.keyCode;
    }
}
