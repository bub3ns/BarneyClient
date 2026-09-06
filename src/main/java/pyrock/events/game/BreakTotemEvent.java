/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.LivingEntity
 *  net.minecraft.ItemStack
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@ScreenController(description="break_totem")
public class BreakTotemEvent
extends Event {
    private final LivingEntity entity;
    private final ItemStack stack;

    @Generated
    public LivingEntity getEntity() {
        return this.entity;
    }

    @Generated
    public ItemStack getStack() {
        return this.stack;
    }

    @Generated
    public BreakTotemEvent(LivingEntity class_13092, ItemStack class_17992) {
        this.entity = class_13092;
        this.stack = class_17992;
    }
}

