/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  net.minecraft.Entity
 *  net.minecraft.Screen
 *  net.minecraft.ClientPlayerEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.network;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.targeting.TargetActionQueue;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.modules.combat.attacks.TriggerBot;
import moscow.rockstar.modules.movement.speed.Speed;
import moscow.rockstar.modules.movement.sprint.AutoSprint;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.player.inventory.InventoryUtils;
import moscow.rockstar.modules.player.movement.NoPush;
import moscow.rockstar.api.access.PlayerEntityAccess;
import net.minecraft.entity.Entity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyrock.events.game.CloseScreenEvent;
import pyrock.events.player.ClientPlayerTickEndEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.player.EventMotion;
import pyrock.events.player.EventUpdatePostTick;
import pyrock.events.player.SlowDownEvent;

@Mixin(value={ClientPlayerEntity.class})
public class ClientPlayerEntityMixin
implements ClientAccess,
PlayerEntityAccess {
    @Unique
    private int groundTicks;
    @Unique
    private EventMotion rockstar$motionEvent;
    @Unique
    private Aura aura;

    @Shadow
    private void sendSprintingPacket() {
    }

    @Unique
    private Aura rockstar$aura() {
        if (this.aura == null) {
            this.aura = RockstarClient.create().getModuleRegistry().getModule(Aura.class);
        }
        return this.aura;
    }

    @Redirect(method={"tickMovement"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V"))
    private void rockstar$keepBounceSprint(ClientPlayerEntity class_7462, boolean bl) {
        if (!bl && Speed.isSpeedModeSelected(class_7462)) {
            return;
        }
        class_7462.setSprinting(bl);
    }

    @Redirect(method={"tickMovement"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require=0)
    private boolean onIsUsingItemRedirect(ClientPlayerEntity class_7462) {
        SlowDownEvent slowDownEvent = new SlowDownEvent();
        RockstarClient.create().getEventBus().post(slowDownEvent);
        return class_7462.isUsingItem() && class_7462.getVehicle() == null && !slowDownEvent.isCancelled();
    }

    @ModifyExpressionValue(method={"tickMovement"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/option/KeyBinding;isPressed()Z")})
    public boolean unpressSprintKey(boolean bl) {
        if (this.shouldPreventAuraSprint((ClientPlayerEntity)(Object)this)) {
            return false;
        }
        return bl;
    }

    @ModifyExpressionValue(method={"tickMovement"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;canSprint()Z")})
    private boolean disallowSprinting(boolean bl) {
        if (this.shouldPreventAuraSprint((ClientPlayerEntity)(Object)this)) {
            return false;
        }
        return bl;
    }

    @Inject(method={"canStartSprinting"}, at={@At(value="HEAD")}, cancellable=true)
    private void preventSprintStart(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (TargetActionQueue.isCurrentTarget((Entity)(ClientPlayerEntity)(Object)this)) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method={"sendSprintingPacket"}, at={@At(value="HEAD")})
    private void resetSprintBeforePacket(CallbackInfo callbackInfo) {
        ClientPlayerEntity class_7462 = (ClientPlayerEntity)(Object)this;
        if (TargetActionQueue.isCurrentTarget((Entity)class_7462)) {
            ClientPlayerEntityMixin.minecraftClient.options.sprintKey.setPressed(false);
            class_7462.setSprinting(false);
            if (this.rockstar$motionEvent != null) {
                this.rockstar$motionEvent.setSprinting(false);
            }
        }
    }

    @Inject(method={"sendSprintingPacket"}, at={@At(value="TAIL")})
    private void markSprintResetSynced(CallbackInfo callbackInfo) {
        TargetActionQueue.completeTargetAction((Entity)(ClientPlayerEntity)(Object)this);
    }

    @ModifyExpressionValue(method={"canSprint"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/player/HungerManager;getFoodLevel()I")})
    private int ignoreHungerForAutoSprint(int n) {
        AutoSprint autoSprint = RockstarClient.create().getModuleRegistry().getModule(AutoSprint.class);
        if (autoSprint.isEnabled() && autoSprint.getIgnoreHungerSetting().isEnabled()) {
            return 20;
        }
        return n;
    }

    @WrapWithCondition(method={"closeScreen"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V")})
    private boolean preventCloseScreen(MinecraftClient client, Screen class_4372) {
        RockstarClient.create().getEventBus().post(new CloseScreenEvent(class_4372));
        return true;
    }

    @Inject(method={"pushOutOfBlocks"}, at={@At(value="HEAD")}, cancellable=true)
    public void removePushOutFromBlocks(double d, double d2, CallbackInfo callbackInfo) {
        NoPush noPush = RockstarClient.create().getModuleRegistry().getModule(NoPush.class);
        if (noPush.isEnabled() && noPush.getBlocks().isSelected()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    public void triggerTickEvent(CallbackInfo callbackInfo) {
        RockstarClient.create().getEventBus().post(new ClientPlayerTickEvent());
    }

    @Inject(method={"tick"}, at={@At(value="RETURN")})
    public void triggerTickEndEvent(CallbackInfo callbackInfo) {
        RockstarClient.create().getEventBus().post(new ClientPlayerTickEndEvent());
        this.rockstar$finishMotionEvent(false);
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift=At.Shift.AFTER)})
    public void triggerUpdatePostTickEvent(CallbackInfo callbackInfo) {
        RockstarClient.create().getEventBus().post(new EventUpdatePostTick());
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;sendSneakingPacket()V", shift=At.Shift.BEFORE)})
    private void rockstar$createMotionEventBeforeActionPackets(CallbackInfo callbackInfo) {
        this.rockstar$getMotionEvent((ClientPlayerEntity)(Object)this);
    }

    @Inject(method={"tickMovement"}, at={@At(value="HEAD")})
    public void updateOnGroundTicks(CallbackInfo callbackInfo) {
        ClientPlayerEntity class_7462 = (ClientPlayerEntity)(Object)this;
        if (TargetActionQueue.isCurrentTarget((Entity)class_7462)) {
            ClientPlayerEntityMixin.minecraftClient.options.sprintKey.setPressed(false);
            class_7462.setSprinting(false);
            if (TargetActionQueue.isImmediateTarget((Entity)class_7462)) {
                this.sendSprintingPacket();
            }
        }
        this.groundTicks = ClientPlayerEntityMixin.minecraftClient.player != null && ClientPlayerEntityMixin.minecraftClient.player.isOnGround() ? ++this.groundTicks : 0;
    }

    @Inject(method={"sendSneakingPacket"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$cancelSneakingPacket(CallbackInfo callbackInfo) {
        if (this.rockstar$motionEvent != null && this.rockstar$motionEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Redirect(method={"sendSneakingPacket"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isSneaking()Z"))
    private boolean rockstar$replaceSneakingState(ClientPlayerEntity class_7462) {
        return this.rockstar$motionEvent != null ? this.rockstar$motionEvent.isSneaking() : class_7462.isSneaking();
    }

    @Inject(method={"sendSprintingPacket"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$cancelSprintingPacket(CallbackInfo callbackInfo) {
        if (this.rockstar$motionEvent != null && this.rockstar$motionEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Redirect(method={"sendSprintingPacket"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isSprinting()Z"))
    private boolean rockstar$replaceSprintingState(ClientPlayerEntity class_7462) {
        return this.rockstar$motionEvent != null ? this.rockstar$motionEvent.isSprinting() : class_7462.isSprinting();
    }

    @Inject(method={"sendMovementPackets"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$cancelMovementPackets(CallbackInfo callbackInfo) {
        EventMotion eventMotion = this.rockstar$getMotionEvent((ClientPlayerEntity)(Object)this);
        if (eventMotion.isCancelled()) {
            this.rockstar$finishMotionEvent(false);
            callbackInfo.cancel();
        }
    }

    @Redirect(method={"sendMovementPackets"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;getX()D"))
    public double replaceMovePacketX(ClientPlayerEntity class_7462) {
        return this.rockstar$getMotionEvent(class_7462).getX();
    }

    @Redirect(method={"sendMovementPackets"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;getY()D"))
    public double replaceMovePacketY(ClientPlayerEntity class_7462) {
        return this.rockstar$getMotionEvent(class_7462).getY();
    }

    @Redirect(method={"sendMovementPackets"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;getZ()D"))
    public double replaceMovePacketZ(ClientPlayerEntity class_7462) {
        return this.rockstar$getMotionEvent(class_7462).getZ();
    }

    @Redirect(method={"sendMovementPackets"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
    public float replaceMovePacketYaw(ClientPlayerEntity class_7462) {
        float f = this.rockstar$getMotionEvent(class_7462).getYaw();
        RockstarClient.create().getRotationManager().getPacketRotation().setYaw(f);
        return f;
    }

    @Redirect(method={"sendMovementPackets"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"))
    public float replaceMovePacketPitch(ClientPlayerEntity class_7462) {
        float f = this.rockstar$getMotionEvent(class_7462).getPitch();
        RockstarClient.create().getRotationManager().getPacketRotation().setPitch(f);
        return f;
    }

    @Redirect(method={"sendMovementPackets"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isOnGround()Z"))
    public boolean replaceMovePacketGround(ClientPlayerEntity class_7462) {
        return this.rockstar$getMotionEvent(class_7462).isOnGround();
    }

    @Unique
    private EventMotion rockstar$getMotionEvent(ClientPlayerEntity class_7462) {
        float f;
        if (this.rockstar$motionEvent != null) {
            return this.rockstar$motionEvent;
        }
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        Rotation rotation = rotationManager.getLastSentRotation();
        float f2 = rotation != null ? rotation.getYaw() : rotationManager.isIdle() ? class_7462.getYaw() : rotationManager.getCurrentRotation().getYaw();
        float f3 = rotation != null ? rotation.getPitch() : (rotationManager.isIdle() ? class_7462.getPitch() : rotationManager.getCurrentRotation().getPitch());
        this.rockstar$motionEvent = new EventMotion(class_7462.getX(), class_7462.getY(), class_7462.getZ(), f2, f3, class_7462.isOnGround(), class_7462.isSneaking(), class_7462.isSprinting());
        RockstarClient.create().getEventBus().post(this.rockstar$motionEvent);
        return this.rockstar$motionEvent;
    }

    @Inject(method={"sendMovementPackets"}, at={@At(value="TAIL")})
    private void rockstar$clearInteractItemRotation(CallbackInfo callbackInfo) {
        this.rockstar$finishMotionEvent(true);
    }

    @Unique
    private void rockstar$finishMotionEvent(boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = this.rockstar$motionEvent != null;
        if (this.rockstar$motionEvent != null) {
            if (bl && !this.rockstar$motionEvent.isCancelled()) {
                this.rockstar$motionEvent.markSent();
            }
            this.rockstar$motionEvent = null;
        }
        if (bl2 || bl) {
            RockstarClient.create().getRotationManager().setLastSentRotation(null);
        }
    }

    @Inject(method={"dropSelectedItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void onDropSelectedItem(boolean bl, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        InventoryUtils inventoryUtils = RockstarClient.create().getModuleRegistry().getModule(InventoryUtils.class);
        if (inventoryUtils.isEnabled() && inventoryUtils.getSlotLockOption().isSelected() && inventoryUtils.isSlotActionAllowed(ClientPlayerEntityMixin.minecraftClient.player.getInventory().selectedSlot)) {
            callbackInfoReturnable.setReturnValue(false);
            callbackInfoReturnable.cancel();
        }
    }

    public int rockstar$getOnGroundTicks() {
        return this.groundTicks;
    }

    public void rockstar$syncSprinting() {
        this.sendSprintingPacket();
    }

    @Unique
    private boolean shouldPreventAuraSprint(ClientPlayerEntity class_7462) {
        TriggerBot triggerBot = RockstarClient.create().getModuleRegistry().getModule(TriggerBot.class);
        Aura aura = this.rockstar$aura();
        return TargetActionQueue.isCurrentTarget((Entity)class_7462) || aura != null && aura.isEnabled() && aura.shouldResetSprint() || triggerBot.isEnabled() && triggerBot.isTriggerAttackReady();
    }
}
