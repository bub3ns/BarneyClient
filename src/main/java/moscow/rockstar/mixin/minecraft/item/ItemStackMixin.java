/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.World
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.item;

import moscow.rockstar.core.RockstarClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyrock.events.game.FinishEatEvent;

@Mixin(value={ItemStack.class})
public abstract class ItemStackMixin {
    @Inject(method={"finishUsing"}, at={@At(value="TAIL")})
    private void onFinishUsing(World class_19372, LivingEntity class_13092, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
        if (!class_19372.isClient) {
            return;
        }
        if (class_13092 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_13092;
            RockstarClient.create().getEventBus().post(new FinishEatEvent(class_16572, (ItemStack)(Object)this));
        }
    }
}
