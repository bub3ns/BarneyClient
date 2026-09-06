/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@ScreenController(description="finish_eat")
public class FinishEatEvent
extends Event {
    private final PlayerEntity user;
    private final ItemStack stack;

    @Generated
    public PlayerEntity getUser() {
        return this.user;
    }

    @Generated
    public ItemStack getStack() {
        return this.stack;
    }

    @Generated
    public FinishEatEvent(PlayerEntity class_16572, ItemStack class_17992) {
        this.user = class_16572;
        this.stack = class_17992;
    }
}

