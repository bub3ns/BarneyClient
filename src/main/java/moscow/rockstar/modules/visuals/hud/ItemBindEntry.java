/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Item
 */
package moscow.rockstar.modules.visuals.hud;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.item.Item;

public final class ItemBindEntry {
    private final Item item;
    private final int key;

    public ItemBindEntry(Item class_17922, int n) {
        this.item = class_17922;
        this.key = n;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "item", "key");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "item", "key");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "item", "key");
    }

    public Item getItem() {
        return this.item;
    }

    public int getKey() {
        return this.key;
    }
}

