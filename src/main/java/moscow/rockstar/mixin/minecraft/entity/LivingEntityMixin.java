/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.Hand
 *  net.minecraft.DamageSource
 *  net.minecraft.Entity
 *  net.minecraft.Entity$RemovalReason
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 *  net.minecraft.ClientPlayerEntity
 *  net.minecraft.DataComponentTypes
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.RotationRequest;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.tracking.EntityPositionCache;
import moscow.rockstar.modules.combat.targeting.ElytraTarget;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.player.movement.NoDelay;
import moscow.rockstar.modules.player.movement.NoPush;
import moscow.rockstar.modules.visuals.hand.SwingAnimation;
import net.minecraft.util.Hand;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyrock.events.game.BreakTotemEvent;
import pyrock.events.game.EntityDeathEvent;
import pyrock.events.game.EntityJumpEvent;
import pyrock.events.player.EventOnTravelPost;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin {
    @Shadow
    private int jumpingCooldown;

    @Shadow
    public abstract void remove(Entity.RemovalReason var1);

    @Shadow
    public abstract ItemStack getMainHandStack();

    @ModifyReturnValue(method={"getHandSwingDuration"}, at={@At(value="RETURN")})
    public int replaceSwingSpeed(int n) {
        SwingAnimation swingAnimation = RockstarClient.create().getModuleRegistry().getModule(SwingAnimation.class);
        if (!swingAnimation.isEnabled() || !swingAnimation.isSwingItem(this.getMainHandStack())) {
            return n;
        }
        return (int)((float)n * RockstarClient.create().getHandSwingPresetManager().getSwingSpeedSetting().getValue());
    }

    @Inject(method={"jump"}, at={@At(value="HEAD")}, cancellable=true)
    public void triggerJumpEvent(CallbackInfo callbackInfo) {
        LivingEntity class_13092 = (LivingEntity)(Object)this;
        EntityJumpEvent entityJumpEvent = new EntityJumpEvent(class_13092);
        RockstarClient.create().getEventBus().post(entityJumpEvent);
        if (entityJumpEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @ModifyExpressionValue(method={"jump"}, at={@At(value="NEW", target="(DDD)Lnet/minecraft/util/math/Vec3d;")})
    public Vec3d movementCorrection(Vec3d VanillaChestLootTableGenerator) {
        RotationManager rotationManager = RockstarClient.INSTANCE.getRotationManager();
        RotationRequest rotationRequest = rotationManager.getRotationResolver();
        if ((Object)this != MinecraftClient.getInstance().player) {
            return VanillaChestLootTableGenerator;
        }
        if (rotationRequest != null && rotationRequest.getCorrectionMode().correctsJumpDirection()) {
            float f = rotationManager.getCurrentRotation().getYaw() * ((float)Math.PI / 180);
            return new Vec3d((double)(-MathHelper.sin((float)f) * 0.2f), 0.0, (double)(MathHelper.cos((float)f) * 0.2f));
        }
        return VanillaChestLootTableGenerator;
    }

    @Inject(method={"tickMovement"}, at={@At(value="HEAD")})
    public void removeJumpDelay(CallbackInfo callbackInfo) {
        NoDelay noDelay = RockstarClient.create().getModuleRegistry().getModule(NoDelay.class);
        if (noDelay.isEnabled() && noDelay.getJumpSetting().isEnabled()) {
            this.jumpingCooldown = 0;
        }
    }

    @Inject(method={"isPushable"}, at={@At(value="HEAD")}, cancellable=true)
    private void removePushFromEntity(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        NoPush noPush = RockstarClient.create().getModuleRegistry().getModule(NoPush.class);
        LivingEntity class_13092 = (LivingEntity)(Object)this;
        if (class_13092 instanceof ClientPlayerEntity && noPush.isEnabled() && noPush.getEntitiesOption().isSelected()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method={"onDeath"}, at={@At(value="TAIL")})
    public void triggerEntityDeathEvent(DamageSource class_12822, CallbackInfo callbackInfo) {
        LivingEntity class_13092 = (LivingEntity)(Object)this;
        if (EntityPositionCache.trackEntity((Entity)class_13092)) {
            RockstarClient.create().getEventBus().post(new EntityDeathEvent(class_13092, class_12822));
        }
    }

    @Redirect(method={"calcGlidingVelocity(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getPitch()F"))
    private float redirectGetPitch(LivingEntity class_13092) {
        return LivingEntityMixin.rockstar$shouldCorrectMovement(class_13092) ? RockstarClient.create().getRotationManager().getCurrentRotation().getPitch() : class_13092.getPitch();
    }

    @Redirect(method={"calcGlidingVelocity(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d redirectGetRotationVector(LivingEntity class_13092) {
        return LivingEntityMixin.rockstar$shouldCorrectMovement(class_13092) ? RockstarClient.create().getRotationManager().getCurrentRotation().toDirectionVector() : class_13092.getRotationVector();
    }

    @Unique
    private static boolean rockstar$shouldCorrectMovement(LivingEntity class_13092) {
        if ((Object)class_13092 != MinecraftClient.getInstance().player) {
            return false;
        }
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        RotationRequest rotationRequest = rotationManager.getRotationResolver();
        return !rotationManager.isIdle() && rotationRequest != null && rotationRequest.getCorrectionMode().correctsMovement();
    }

    @Inject(method={"calcGlidingVelocity(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"}, at={@At(value="RETURN")}, cancellable=true)
    private void rockstar$onCalcGlidingVelocity(Vec3d VanillaChestLootTableGenerator, CallbackInfoReturnable<Vec3d> callbackInfoReturnable) {
        LivingEntity class_13092 = (LivingEntity)(Object)this;
        if ((Object)class_13092 != MinecraftClient.getInstance().player) {
            return;
        }
        EventOnTravelPost eventOnTravelPost = new EventOnTravelPost((Vec3d)callbackInfoReturnable.getReturnValue());
        RockstarClient.create().getEventBus().post(eventOnTravelPost);
        callbackInfoReturnable.setReturnValue(eventOnTravelPost.getOldVelocity());
    }

    @Inject(method={"tryUseDeathProtector"}, at={@At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;decrement(I)V")})
    private void onTotemUse(DamageSource class_12822, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        LivingEntity class_13092 = (LivingEntity)(Object)this;
        if (!(class_13092 instanceof PlayerEntity)) {
            return;
        }
        for (Hand class_12682 : Hand.values()) {
            ItemStack class_17992 = class_13092.getStackInHand(class_12682);
            RockstarClient.create().getEventBus().post(new BreakTotemEvent(class_13092, class_17992));
            if (!class_17992.contains(DataComponentTypes.DEATH_PROTECTION)) continue;
            return;
        }
    }

    @Inject(method={"travel"}, at={@At(value="HEAD")}, cancellable=true)
    private void cancelElytraMovement(Vec3d VanillaChestLootTableGenerator, CallbackInfo callbackInfo) {
        boolean bl;
        LivingEntity class_13092 = (LivingEntity)(Object)this;
        if ((Object)class_13092 != MinecraftClient.getInstance().player) {
            return;
        }
        ElytraTarget elytraTarget = RockstarClient.create().getModuleRegistry().getModule(ElytraTarget.class);
        boolean bl2 = bl = elytraTarget.isEnabled() && elytraTarget.getAirFreezeSetting().isEnabled() && elytraTarget.getTargetEntity() != null && class_13092.isGliding() && class_13092.distanceTo((Entity)elytraTarget.getTargetEntity()) <= elytraTarget.getFreezeDistance().getValue();
        if (bl) {
            class_13092.setVelocity(Vec3d.ZERO);
            callbackInfo.cancel();
        }
    }
}
