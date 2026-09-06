/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Box
 *  net.minecraft.HitResult
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockState
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  net.minecraft.ClientPlayerEntity
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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.RotationRequest;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.combat.targeting.BacktrackPoint;
import moscow.rockstar.modules.combat.targeting.Hitboxes;
import moscow.rockstar.modules.combat.targeting.BacktrackAccess;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.player.movement.NoPush;
import moscow.rockstar.modules.visuals.overlay.Removals;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyrock.events.game.RotateCameraEvent;
import pyrock.events.player.EventOnMovePost;
import pyrock.events.player.TraceEvent;

@Mixin(value={Entity.class})
public abstract class EntityMixin
implements ClientAccess,
BacktrackAccess {
    @Shadow
    private Box boundingBox;
    @Unique
    private List<BacktrackPoint> backTracks;

    @Shadow
    public abstract Vec3d getCameraPosVec(float var1);

    @Shadow
    public abstract Vec3d getRotationVec(float var1);

    @Shadow
    public abstract float getYaw();

    @Shadow
    public abstract float getPitch();

    @Inject(method={"raycast"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRaycast(double d, float f, boolean bl, CallbackInfoReturnable<HitResult> callbackInfoReturnable) {
        TraceEvent traceEvent = new TraceEvent(this.getYaw(), this.getPitch());
        RockstarClient.create().getEventBus().post(traceEvent);
        if (traceEvent.isCancelled()) {
            Vec3d VanillaChestLootTableGenerator = this.getCameraPosVec(f);
            Vec3d WallPlayerSkullBlock = this.getCustomLook(traceEvent.getPitch(), traceEvent.getYaw());
            Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock.multiply(d));
            Entity entity = (Entity)(Object)this;
            BlockHitResult class_39652 = entity.getWorld().raycast(new RaycastContext(VanillaChestLootTableGenerator, VanillaEntityLootTableGenerator, RaycastContext.ShapeType.OUTLINE, bl ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, entity));
            callbackInfoReturnable.setReturnValue(class_39652);
        }
    }

    private Vec3d getCustomLook(float f, float f2) {
        float f3 = f * ((float)Math.PI / 180);
        float f4 = -f2 * ((float)Math.PI / 180);
        float f5 = (float)Math.cos(f4);
        float f6 = (float)Math.sin(f4);
        float f7 = (float)Math.cos(f3);
        float f8 = (float)Math.sin(f3);
        return new Vec3d((double)(f6 * f7), (double)(-f8), (double)(f5 * f7));
    }

    @ModifyExpressionValue(method={"move"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;isControlledByPlayer()Z")})
    public boolean fixFalldistanceValue(boolean bl) {
        if ((Object)this == EntityMixin.minecraftClient.player) {
            return false;
        }
        return bl;
    }

    @WrapOperation(method={"move"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;fall(DZLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V")})
    private void rockstar$fallDistanceOnly(Entity class_12972, double d, boolean bl, BlockState class_26802, BlockPos adminsky, Operation<Void> operation) {
        if (class_12972 != EntityMixin.minecraftClient.player) {
            operation.call(new Object[]{class_12972, d, bl, class_26802, adminsky});
            return;
        }
        if (bl) {
            class_12972.fallDistance = 0.0f;
        } else if (d < 0.0) {
            class_12972.fallDistance -= (float)d;
        }
    }

    @Inject(method={"isGlowing"}, at={@At(value="HEAD")}, cancellable=true)
    private void isGlowing(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.isEnabled() && removals.getGlowing().isSelected()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method={"onBubbleColumnCollision", "onBubbleColumnSurfaceCollision"}, at={@At(value="HEAD")}, cancellable=true)
    private void removePushFromBubbleColumns(boolean bl, CallbackInfo callbackInfo) {
        NoPush noPush = RockstarClient.create().getModuleRegistry().getModule(NoPush.class);
        if ((Object)this == EntityMixin.minecraftClient.player && noPush.isEnabled() && noPush.getBubbleColumns().isSelected()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"getBoundingBox"}, at={@At(value="HEAD")}, cancellable=true)
    public final void getBoundingBox(CallbackInfoReturnable<Box> callbackInfoReturnable) {
        Hitboxes hitboxes = RockstarClient.create().getModuleRegistry().getModule(Hitboxes.class);
        Entity class_12972 = (Entity)(Object)this;
        if (class_12972 instanceof LivingEntity) {
            LivingEntity class_13092 = (LivingEntity)class_12972;
            if (hitboxes.isEnabled() && hitboxes.isEntityAlive(class_13092) && class_12972.getId() != EntityMixin.minecraftClient.player.getId()) {
                callbackInfoReturnable.setReturnValue(new Box(this.boundingBox.minX - (double)hitboxes.getSize().getValue(), this.boundingBox.minY, this.boundingBox.minZ - (double)hitboxes.getSize().getValue(), this.boundingBox.maxX + (double)hitboxes.getSize().getValue(), this.boundingBox.maxY, this.boundingBox.maxZ + (double)hitboxes.getSize().getValue()));
            }
        }
    }

    @Redirect(method={"updateVelocity"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;getYaw()F"))
    public float movementCorrection(Entity class_12972) {
        RotationManager rotationManager = RockstarClient.INSTANCE.getRotationManager();
        RotationRequest rotationRequest = rotationManager.getRotationResolver();
        if (rotationRequest != null && rotationRequest.getCorrectionMode().correctsMovement() && class_12972 instanceof ClientPlayerEntity) {
            return rotationManager.getCurrentRotation().getYaw();
        }
        return class_12972.getYaw();
    }

    @Inject(method={"updateVelocity"}, at={@At(value="TAIL")})
    private void rockstar$onMovePost(float f, Vec3d VanillaChestLootTableGenerator, CallbackInfo callbackInfo) {
        Entity class_12972 = (Entity)(Object)this;
        if (EntityMixin.minecraftClient.player == null || class_12972.getId() != EntityMixin.minecraftClient.player.getId()) {
            return;
        }
        RockstarClient.create().getEventBus().post(new EventOnMovePost(f, VanillaChestLootTableGenerator));
    }

    public List<BacktrackPoint> rockstar2_0$getBackTracks() {
        if (this.backTracks == null) {
            this.backTracks = new ArrayList<BacktrackPoint>();
        }
        return this.backTracks;
    }

    @Inject(method={"changeLookDirection"}, at={@At(value="HEAD")}, cancellable=true)
    private void onChangeLookDirection(double d, double d2, CallbackInfo callbackInfo) {
        if ((Object)this == EntityMixin.minecraftClient.player) {
            RotateCameraEvent rotateCameraEvent = new RotateCameraEvent((float)(d * (double)0.15f), (float)(d2 * (double)0.15f));
            RockstarClient.create().getEventBus().post(rotateCameraEvent);
            if (rotateCameraEvent.isCancelled()) {
                callbackInfo.cancel();
            }
        }
    }
}
