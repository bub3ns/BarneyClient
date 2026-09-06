/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.ItemStack
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

@ScreenController(description="pickup")
public class PickupEvent
extends Event {
    private Entity entity;
    private ItemStack itemStack;
    private int count;

    @Generated
    public Entity getEntity() {
        return this.entity;
    }

    @Generated
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Generated
    public int getCount() {
        return this.count;
    }

    @Generated
    public PickupEvent(Entity class_12972, ItemStack class_17992, int n) {
        this.entity = class_12972;
        this.itemStack = class_17992;
        this.count = n;
    }
}

