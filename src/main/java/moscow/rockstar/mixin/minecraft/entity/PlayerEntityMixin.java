/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Vec3d
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.entity;

import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.RotationRequest;
import moscow.rockstar.core.ClientFeatureFlags;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.combat.targeting.Hitboxes;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.player.movement.NoPush;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.PathExecutor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyrock.events.game.AttackEvent;
import pyrock.events.game.PostAttackEvent;
import pyrock.events.player.KeepSprintEvent;

@Mixin(value={PlayerEntity.class})
public class PlayerEntityMixin {
    @Inject(method={"attack"}, at={@At(value="HEAD")}, cancellable=true)
    private void attackAHook2(Entity class_12972, CallbackInfo callbackInfo) {
        Hitboxes hitboxes = RockstarClient.create().getModuleRegistry().getModule(Hitboxes.class);
        if (class_12972 instanceof LivingEntity && hitboxes.isEnabled() && hitboxes.getSize().getValue() <= 0.0f) {
            callbackInfo.cancel();
            return;
        }
        AttackEvent attackEvent = new AttackEvent(class_12972);
        RockstarClient.create().getEventBus().post(attackEvent);
        if (attackEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"attack"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal=0)}, cancellable=true)
    private void onAttackSetVelocity(Entity class_12972, CallbackInfo callbackInfo) {
        KeepSprintEvent keepSprintEvent = new KeepSprintEvent();
        RockstarClient.create().getEventBus().post(keepSprintEvent);
        if (keepSprintEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"attack"}, at={@At(value="RETURN")}, cancellable=true)
    private void attackAHook(Entity class_12972, CallbackInfo callbackInfo) {
        PostAttackEvent postAttackEvent = new PostAttackEvent(class_12972);
        RockstarClient.create().getEventBus().post(postAttackEvent);
    }

    @Inject(method={"isPushedByFluids"}, at={@At(value="HEAD")}, cancellable=true)
    private void removePushFromFluids(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        NoPush noPush = RockstarClient.create().getModuleRegistry().getModule(NoPush.class);
        PlayerEntity class_16572 = (PlayerEntity)(Object)this;
        if (class_16572 == MinecraftClient.getInstance().player && noPush.isEnabled() && noPush.getFluids().isSelected()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method={"clipAtLedge"}, at={@At(value="RETURN")}, cancellable=true)
    private void pathfinder$safewalkClipAtLedge(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (callbackInfoReturnable.getReturnValueZ()) {
            return;
        }
        if (!ClientFeatureFlags.safewalkEnabled) {
            return;
        }
        if (!ClientServiceRegistry.isInitialized()) {
            return;
        }
        if ((Object)this != MinecraftClient.getInstance().player) {
            return;
        }
        if (!ClientServiceRegistry.getInstance().getEventListenerSlot().hasPendingScreenState()) {
            return;
        }
        if (MinecraftClient.getInstance().player.isTouchingWater()) {
            return;
        }
        PathExecutor pathExecutor = ClientServiceRegistry.getInstance().getPathExecutor();
        if (pathExecutor == null) {
            return;
        }
        ExcavationController excavationController = pathExecutor.getCurrentMovement();
        if (excavationController == null) {
            return;
        }
        if (!excavationController.tickNavigation()) {
            return;
        }
        if (excavationController.getEndPosition().getY() < excavationController.getStartPosition().getY()) {
            return;
        }
        int nextStep = pathExecutor.getCurrentStep() + 1;
        if (nextStep < pathExecutor.getPath().getMovements().size()) {
            ExcavationController nextMovement = pathExecutor.getPath().getMovements().get(nextStep);
            boolean nextMovementDescending = nextMovement.getEndPosition().getY() < nextMovement.getStartPosition().getY();
            if ((!nextMovement.tickNavigation() || nextMovementDescending)
                && this.rockstar$isNearMovementEnd(excavationController, 0.85)) {
                return;
            }
        }
        callbackInfoReturnable.setReturnValue(true);
    }

    private boolean rockstar$isNearMovementEnd(ExcavationController movement, double radius) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        BlockPos end = movement.getEndPosition();
        double dx = player.getX() - ((double)end.getX() + 0.5);
        double dz = player.getZ() - ((double)end.getZ() + 0.5);
        return dx * dx + dz * dz <= radius * radius;
    }

    @Redirect(method={"travel(Lnet/minecraft/util/math/Vec3d;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d redirectGetRotationVectorInTravel(PlayerEntity class_16572) {
        if (class_16572 != MinecraftClient.getInstance().player) {
            return class_16572.getRotationVector();
        }
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        RotationRequest rotationRequest = rotationManager.getRotationResolver();
        if (rotationManager.isIdle() || rotationRequest == null || !rotationRequest.getCorrectionMode().correctsMovement()) {
            return class_16572.getRotationVector();
        }
        return rotationManager.getCurrentRotation().toDirectionVector();
    }
}
