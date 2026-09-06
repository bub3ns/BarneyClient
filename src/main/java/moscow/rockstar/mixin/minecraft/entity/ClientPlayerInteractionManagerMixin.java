/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.ActionResult
 *  net.minecraft.Entity
 *  net.minecraft.ArmorStandEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.AbstractMinecartEntity
 *  net.minecraft.SlotActionType
 *  net.minecraft.BlockItem
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Block
 *  net.minecraft.Direction
 *  net.minecraft.BlockHitResult
 *  net.minecraft.EntityHitResult
 *  net.minecraft.ClientPlayerInteractionManager
 *  net.minecraft.ClientPlayerEntity
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.entity;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.player.movement.NoInteract;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyrock.events.game.AfterAttackEvent;
import pyrock.events.game.BlockBreakEvent;
import pyrock.events.game.BlockPlaceEvent;
import pyrock.events.game.InternalAttackEvent;
import pyrock.events.game.StartBreakBlockEvent;

@Mixin(value={ClientPlayerInteractionManager.class})
public class ClientPlayerInteractionManagerMixin
implements ClientAccess {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method={"attackEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$critPre(PlayerEntity class_16572, Entity class_12972, CallbackInfo callbackInfo) {
        InternalAttackEvent internalAttackEvent = new InternalAttackEvent(class_12972);
        RockstarClient.create().getEventBus().post(internalAttackEvent);
        if (internalAttackEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"attackEntity"}, at={@At(value="RETURN")})
    private void rockstar$critPost(PlayerEntity class_16572, Entity class_12972, CallbackInfo callbackInfo) {
        AfterAttackEvent afterAttackEvent = new AfterAttackEvent(class_12972);
        RockstarClient.create().getEventBus().post(afterAttackEvent);
    }

    @Inject(method={"breakBlock"}, at={@At(value="RETURN")}, cancellable=true)
    public void breakBlockHook(BlockPos adminsky, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(adminsky);
        RockstarClient.create().getEventBus().post(blockBreakEvent);
        if (blockBreakEvent.isCancelled()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method={"attackBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAttackBlock(BlockPos adminsky, Direction class_23502, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        StartBreakBlockEvent startBreakBlockEvent = new StartBreakBlockEvent(adminsky);
        RockstarClient.create().getEventBus().post(startBreakBlockEvent);
        if (startBreakBlockEvent.isCancelled()) {
            callbackInfoReturnable.cancel();
        }
    }

    @Inject(method={"interactBlock"}, at={@At(value="HEAD")}, cancellable=true)
    public void preventInteraction(ClientPlayerEntity class_7462, Hand class_12682, BlockHitResult class_39652, CallbackInfoReturnable<ActionResult> callbackInfoReturnable) {
        ItemStack class_17992;
        if (this.client.world == null) {
            return;
        }
        NoInteract noInteract = RockstarClient.create().getModuleRegistry().getModule(NoInteract.class);
        if (!noInteract.isEnabled()) {
            return;
        }
        Block class_22482 = this.client.world.getBlockState(class_39652.getBlockPos()).getBlock();
        if (noInteract.isBlockInteractionAllowed(class_22482, class_17992 = class_7462.getStackInHand(class_12682))) {
            callbackInfoReturnable.setReturnValue(ActionResult.PASS);
        }
        if (noInteract.isItemValid(class_17992)) {
            callbackInfoReturnable.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method={"interactEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void preventEntityInteraction(PlayerEntity class_16572, Entity class_12972, Hand class_12682, CallbackInfoReturnable<ActionResult> callbackInfoReturnable) {
        if (this.client.world == null || !(class_12972 instanceof ArmorStandEntity) && !(class_12972 instanceof AbstractMinecartEntity)) {
            return;
        }
        NoInteract noInteract = RockstarClient.create().getModuleRegistry().getModule(NoInteract.class);
        if (!noInteract.isEnabled()) {
            return;
        }
        if (noInteract.isEntityInteractionAllowed(class_12972, class_16572.getStackInHand(class_12682))) {
            callbackInfoReturnable.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method={"interactBlock"}, at={@At(value="RETURN")})
    private void onInteractBlock(ClientPlayerEntity class_7462, Hand class_12682, BlockHitResult class_39652, CallbackInfoReturnable<ActionResult> callbackInfoReturnable) {
        if (this.client.world == null) {
            return;
        }
        ActionResult class_12692 = (ActionResult)callbackInfoReturnable.getReturnValue();
        if (class_12692 == null || !class_12692.isAccepted()) {
            return;
        }
        ItemStack class_17992 = class_7462.getStackInHand(class_12682);
        if (class_17992.isEmpty()) {
            return;
        }
        if (!(class_17992.getItem() instanceof BlockItem) && !class_17992.isOf(Items.END_CRYSTAL)) {
            return;
        }
        BlockPos adminsky = class_39652.getBlockPos().offset(class_39652.getSide());
        RockstarClient.create().getEventBus().post(new BlockPlaceEvent(adminsky, class_39652.getBlockPos(), class_39652.getSide(), class_12682, class_17992));
    }

    @Inject(method={"interactEntityAtLocation"}, at={@At(value="HEAD")}, cancellable=true)
    private void preventEntityInteractionAtLocation(PlayerEntity class_16572, Entity class_12972, EntityHitResult class_39662, Hand class_12682, CallbackInfoReturnable<ActionResult> callbackInfoReturnable) {
        if (this.client.world == null || !(class_12972 instanceof ArmorStandEntity) && !(class_12972 instanceof AbstractMinecartEntity)) {
            return;
        }
        NoInteract noInteract = RockstarClient.create().getModuleRegistry().getModule(NoInteract.class);
        if (!noInteract.isEnabled()) {
            return;
        }
        if (noInteract.isEntityInteractionAllowed(class_12972, class_16572.getStackInHand(class_12682))) {
            callbackInfoReturnable.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method={"clickSlot"}, at={@At(value="HEAD")}, cancellable=true)
    private void onClickSlot(int n, int n2, int n3, SlotActionType class_17132, PlayerEntity class_16572, CallbackInfo callbackInfo) {
    }
}
