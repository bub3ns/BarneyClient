/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 */
package moscow.rockstar.items.catalog;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.items.ItemCategory;
import net.minecraft.item.ItemStack;

public final class ItemEntry {
    private final String name;
    private final int key;
    private final ItemStack icon;
    private final ItemCategory category;

    public ItemEntry(String string, int n, ItemStack class_17992, ItemCategory itemCategory) {
        this.name = string;
        this.key = n;
        this.icon = class_17992;
        this.category = itemCategory;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "name", "key", "icon", "category");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "name", "key", "icon", "category");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "name", "key", "icon", "category");
    }

    public String getName() {
        return this.name;
    }

    public int getKey() {
        return this.key;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public ItemCategory getCategory() {
        return this.category;
    }
}

