/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.BlockView
 *  net.minecraft.Vec3d
 *  net.minecraft.EntityPose
 *  net.minecraft.Camera
 *  net.minecraft.CameraSubmersionType
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.mixin.accessors.CameraAccessor;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.modules.movement.speed.Speed;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.player.movement.camera.FreeCamera;
import moscow.rockstar.modules.visuals.overlay.Removals;
import moscow.rockstar.server.ServerDetector;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.EntityPose;
import net.minecraft.client.render.Camera;
import net.minecraft.block.enums.CameraSubmersionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyrock.events.render.CameraUpdateEvent;

@Mixin(value={Camera.class})
public abstract class CameraMixin {
    @Shadow
    private Entity focusedEntity;
    @Shadow
    private boolean thirdPerson;
    @Shadow
    private float cameraY;
    @Shadow
    private float lastCameraY;
    @Unique
    private static final int rockstar$JITTER_FLIPS = 3;
    @Unique
    private static final int rockstar$RESET_TICKS = 10;
    @Unique
    private float rockstar$lastEyeHeight;
    @Unique
    private boolean rockstar$eyeHeightKnown;
    @Unique
    private float rockstar$heldEyeHeight;
    @Unique
    private int rockstar$eyeHeightFlips;
    @Unique
    private int rockstar$ticksSinceFlip;

    @Shadow
    public abstract void setPos(Vec3d var1);

    @Shadow
    public abstract void setRotation(float var1, float var2);

    @Inject(method={"updateEyeHeight"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$stabilizeEyeHeight(CallbackInfo callbackInfo) {
        if (this.focusedEntity == null) {
            return;
        }
        float f = CameraMixin.rockstar$eyeHeight(this.focusedEntity);
        if (!this.focusedEntity.isTouchingWater() || !CameraMixin.rockstar$auraOnHolyWorld()) {
            this.rockstar$eyeHeightKnown = false;
            this.rockstar$eyeHeightFlips = 0;
            this.rockstar$ticksSinceFlip = 0;
            this.rockstar$heldEyeHeight = f;
            this.lastCameraY = this.cameraY;
            this.cameraY += (f - this.cameraY) * 0.5f;
            callbackInfo.cancel();
            return;
        }
        if (!this.rockstar$eyeHeightKnown) {
            this.rockstar$eyeHeightKnown = true;
            this.rockstar$lastEyeHeight = f;
            this.rockstar$heldEyeHeight = f;
        }
        if (Math.abs(f - this.rockstar$lastEyeHeight) > 1.0E-4f) {
            this.rockstar$lastEyeHeight = f;
            this.rockstar$ticksSinceFlip = 0;
            ++this.rockstar$eyeHeightFlips;
        } else if (++this.rockstar$ticksSinceFlip >= 10) {
            this.rockstar$eyeHeightFlips = 0;
        }
        this.rockstar$heldEyeHeight = this.rockstar$eyeHeightFlips >= 3 ? Math.max(this.rockstar$heldEyeHeight, f) : f;
        this.lastCameraY = this.cameraY;
        this.cameraY += (this.rockstar$heldEyeHeight - this.cameraY) * 0.5f;
        callbackInfo.cancel();
    }

    @Unique
    private static boolean rockstar$auraOnHolyWorld() {
        Aura aura = RockstarClient.create().getModuleRegistry().getModule(Aura.class);
        return aura != null && aura.isEnabled() && ServerDetector.isInventoryServer();
    }

    @Unique
    private static float rockstar$eyeHeight(Entity class_12972) {
        if (class_12972 == MinecraftClient.getInstance().player && class_12972.getPose() == EntityPose.GLIDING && Speed.isCollisionReady()) {
            return class_12972.getDimensions(EntityPose.STANDING).eyeHeight();
        }
        return class_12972.getStandingEyeHeight();
    }

    @Inject(method={"getSubmersionType"}, at={@At(value="HEAD")}, cancellable=true)
    private void getSubmergedFluidState(CallbackInfoReturnable<CameraSubmersionType> callbackInfoReturnable) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals == null || !removals.isEnabled()) {
            return;
        }
        if (removals.getWater().isSelected()) {
            callbackInfoReturnable.setReturnValue(CameraSubmersionType.NONE);
            return;
        }
        if (this.thirdPerson && removals.getClip().isSelected() && !this.rockstar$focusedEntitySubmerged()) {
            callbackInfoReturnable.setReturnValue(CameraSubmersionType.NONE);
        }
    }

    @Unique
    private boolean rockstar$focusedEntitySubmerged() {
        return this.focusedEntity != null && (this.focusedEntity.isSubmergedInWater() || this.focusedEntity.isInLava() || this.focusedEntity.inPowderSnow);
    }

    @Inject(method={"clipToSpace"}, at={@At(value="HEAD")}, cancellable=true)
    private void onClipToSpace(float f, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.getClip().isSelected() && removals.isEnabled()) {
            callbackInfoReturnable.setReturnValue(f);
        }
    }

    @Inject(method={"update"}, at={@At(value="TAIL")})
    private void onUpdate(BlockView class_19222, Entity class_12972, boolean bl, boolean bl2, float f, CallbackInfo callbackInfo) {
        FreeCamera freeCamera = RockstarClient.create().getModuleRegistry().getModule(FreeCamera.class);
        freeCamera.resetCamera();
        if (freeCamera.isCameraActive()) {
            this.setPos(freeCamera.getCameraPosition(f));
            Rotation rotation = freeCamera.getCameraRotation(f);
            this.setRotation(rotation.getYaw(), rotation.getPitch());
            ((CameraAccessor)((Object)this)).setThirdPerson(true);
            return;
        }
        RockstarClient.create().getEventBus().post(new CameraUpdateEvent((Camera)(Object)this, class_12972, bl, bl2, f));
    }
}
