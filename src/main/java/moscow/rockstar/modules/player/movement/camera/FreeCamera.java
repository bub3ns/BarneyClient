/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Vec3d
 *  net.minecraft.Text
 *  net.minecraft.PlayerMoveC2SPacket
 *  net.minecraft.MathHelper
 *  net.minecraft.Perspective
 */
package moscow.rockstar.modules.player.movement.camera;

import lombok.Generated;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.option.Perspective;
import pyrock.events.game.RotateCameraEvent;
import pyrock.events.network.SendPacketEvent;
import pyrock.events.player.InputEvent;
import pyrock.events.render.HudRenderEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Free Camera", category=ModuleCategory.PLAYER, description="modules.descriptions.free_camera")
public class FreeCamera
extends Module {
    private NumberSetting speed;
    private BooleanSetting displayCoords;
    private BooleanSetting freeze;
    private BooleanSetting animation;
    private double MIN_CAMERA_DISTANCE;
    private double MAX_CAMERA_DISTANCE;
    private double CAMERA_LERP_FACTOR;
    private Vec3d position = Vec3d.ZERO;
    private Rotation currentRotation = Rotation.ZERO_ROTATION;
    Perspective cameraEntity;
    private final Animation toggleAnimation = new Animation(50L, Easing.linear);
    private final Animation enterAnimation = new Animation(50L, Easing.linear);
    private final Animation cameraAnimation = new Animation(50L, Easing.linear);
    private static final Easing enterEasing = Easing.cubicBezier(0.17, 0.85, 0.29, 0.99);
    private static final Easing exitEasing = Easing.cubicBezier(0.31, 0.87, 0.43, 0.94);
    private final Animation exitAnimation = new Animation(350L, enterEasing);
    private boolean cameraActive = false;
    private Vec3d previousPosition = Vec3d.ZERO;
    private Rotation previousRotation = Rotation.ZERO_ROTATION;
    private final EventListener<SendPacketEvent> onSendPacketEvent = sendPacketEvent -> {
        if (this.freeze.isEnabled() && !this.hasPendingScreenState() && sendPacketEvent.getPacket() instanceof PlayerMoveC2SPacket) {
            sendPacketEvent.cancel();
        }
    };
    private final EventListener<HudRenderEvent> onHudRenderEvent = hudRenderEvent -> {
        if (this.displayCoords.isEnabled()) {
            Vec3d VanillaChestLootTableGenerator = this.getPosition();
            int n = (int)VanillaChestLootTableGenerator.x - (int)this.position.x;
            int n2 = (int)VanillaChestLootTableGenerator.y - (int)this.position.y;
            int n3 = (int)VanillaChestLootTableGenerator.z - (int)this.position.z;
            String string = "X: " + n + " Y: " + n2 + " Z: " + n3;
            FontMetrics fontMetrics = Font.BOLD.metrics(8.0f);
            hudRenderEvent.getContext().drawText(fontMetrics, Text.of((String)string), INSTANCE.width() / 2.0f - fontMetrics.measureText(string) / 2.0f, INSTANCE.height() / 2.0f - 20.0f);
        }
    };
    private final EventListener<RotateCameraEvent> onRotateCameraEvent = rotateCameraEvent -> {
        this.currentRotation.setYaw(this.currentRotation.getYaw() + rotateCameraEvent.getDeltaYaw());
        this.currentRotation.setPitch(MathHelper.clamp((float)(this.currentRotation.getPitch() + rotateCameraEvent.getDeltaPitch()), (float)-90.0f, (float)90.0f));
        rotateCameraEvent.cancel();
    };
    private final EventListener<InputEvent> onInputEvent = EventListener.withPriority(150, inputEvent -> {
        float f = this.speed.getValue();
        if (inputEvent.getForward() != 0.0f || inputEvent.getStrafe() != 0.0f) {
            double d = EntityUtils.getDirectionRadians(this.currentRotation.getYaw() + 90.0f, inputEvent.getForward(), inputEvent.getStrafe());
            float f2 = (float)Math.cos(d);
            float f3 = (float)Math.sin(d);
            this.MIN_CAMERA_DISTANCE += (double)(f2 *= f);
            this.CAMERA_LERP_FACTOR += (double)(f3 *= f);
        }
        if (inputEvent.isJump()) {
            this.MAX_CAMERA_DISTANCE += (double)this.speed.getValue();
        } else if (inputEvent.isSneak()) {
            this.MAX_CAMERA_DISTANCE -= (double)this.speed.getValue();
        }
        inputEvent.setForward(0.0f);
        inputEvent.setStrafe(0.0f);
        inputEvent.setJump(false);
        inputEvent.setSneak(false);
    });

    public FreeCamera() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.speed = new NumberSetting(this, "modules.settings.free_cam.speed").setValue(1.0f).setMaxValue(15.0f).setMinValue(0.1f).setStep(0.1f).setValue(3.0f);
        this.displayCoords = new BooleanSetting(this, "modules.settings.free_cam.display_coords");
        this.freeze = new BooleanSetting(this, "modules.settings.free_cam.freeze").enable();
        this.animation = new BooleanSetting(this, "modules.settings.free_cam.animation").enable();
    }

    public Vec3d getPosition() {
        return new Vec3d((double)this.toggleAnimation.update((float)this.MIN_CAMERA_DISTANCE), (double)this.enterAnimation.update((float)this.MAX_CAMERA_DISTANCE), (double)this.cameraAnimation.update((float)this.CAMERA_LERP_FACTOR));
    }

    public boolean isCameraActive() {
        return this.isEnabled() || this.cameraActive;
    }

    public float getCameraProgress() {
        return MathHelper.clamp((float)this.exitAnimation.getValue(), (float)0.0f, (float)1.0f);
    }

    public boolean isCameraTransitionReady() {
        return this.isCameraActive() && this.exitAnimation.getValue() < 1.0f;
    }

    private Vec3d getPosition(float f) {
        if (FreeCamera.minecraftClient.player == null) {
            return Vec3d.ZERO;
        }
        return FreeCamera.minecraftClient.player.getCameraPosVec(f);
    }

    private static float calculateCameraRotation(float f, float f2, float f3) {
        float f4 = MathHelper.wrapDegrees((float)(f3 - f2));
        return f2 + f4 * f;
    }

    public void resetCamera() {
        if (this.cameraActive && this.exitAnimation.isAtTarget() && this.exitAnimation.getValue() <= 0.001f) {
            this.cameraActive = false;
            if (this.cameraEntity != null) {
                FreeCamera.minecraftClient.options.setPerspective(this.cameraEntity);
                this.cameraEntity = null;
            }
        }
    }

    public Vec3d getCameraPosition(float f) {
        Vec3d VanillaChestLootTableGenerator = this.getPosition(f);
        Vec3d WallPlayerSkullBlock = new Vec3d((double)this.toggleAnimation.update((float)this.MIN_CAMERA_DISTANCE), (double)this.enterAnimation.update((float)this.MAX_CAMERA_DISTANCE), (double)this.cameraAnimation.update((float)this.CAMERA_LERP_FACTOR));
        if (this.cameraActive) {
            float f2 = this.exitAnimation.update(0.0f);
            return this.previousPosition.lerp(VanillaChestLootTableGenerator, (double)(1.0f - f2));
        }
        float f3 = this.exitAnimation.update(1.0f);
        return VanillaChestLootTableGenerator.lerp(WallPlayerSkullBlock, (double)f3);
    }

    public Rotation getCameraRotation(float f) {
        if (FreeCamera.minecraftClient.player == null) {
            return this.currentRotation;
        }
        float f2 = MathHelper.lerp((float)f, (float)FreeCamera.minecraftClient.player.prevYaw, (float)FreeCamera.minecraftClient.player.getYaw());
        float f3 = MathHelper.lerp((float)f, (float)FreeCamera.minecraftClient.player.prevPitch, (float)FreeCamera.minecraftClient.player.getPitch());
        float f4 = this.exitAnimation.getValue();
        Rotation rotation = this.cameraActive ? this.previousRotation : new Rotation(f2, f3);
        Rotation rotation2 = this.cameraActive ? new Rotation(f2, f3) : this.currentRotation;
        float f5 = this.cameraActive ? 1.0f - f4 : f4;
        float f6 = FreeCamera.calculateCameraRotation(f5, rotation.getYaw(), rotation2.getYaw());
        float f7 = MathHelper.lerp((float)f5, (float)rotation.getPitch(), (float)rotation2.getPitch());
        return new Rotation(f6, f7);
    }

    private boolean hasPendingScreenState() {
        return ClientServiceRegistry.isInitialized() && ClientServiceRegistry.getInstance().getEventListenerSlot().hasPendingScreenState();
    }

    @Override
    public final void onTick() {
        FreeCamera.minecraftClient.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        if (this.hasPendingScreenState()) {
            this.position = FreeCamera.minecraftClient.player.getPos();
        } else if (this.freeze.isEnabled()) {
            FreeCamera.minecraftClient.player.setVelocity(Vec3d.ZERO);
            FreeCamera.minecraftClient.player.setPosition(this.position);
            if (FreeCamera.minecraftClient.player.input != null) {
                FreeCamera.minecraftClient.player.input.movementForward = 0.0f;
                FreeCamera.minecraftClient.player.input.movementSideways = 0.0f;
            }
        }
        super.onTick();
    }

    @Override
    public final void onEnable() {
        this.MIN_CAMERA_DISTANCE = FreeCamera.minecraftClient.player.getX();
        this.MAX_CAMERA_DISTANCE = FreeCamera.minecraftClient.player.getEyeY();
        this.CAMERA_LERP_FACTOR = FreeCamera.minecraftClient.player.getZ();
        this.position = FreeCamera.minecraftClient.player.getPos();
        this.toggleAnimation.setValue((float)this.MIN_CAMERA_DISTANCE);
        this.enterAnimation.setValue((float)this.MAX_CAMERA_DISTANCE);
        this.cameraAnimation.setValue((float)this.CAMERA_LERP_FACTOR);
        Rotation rotation = RockstarClient.create().getRotationManager().getPlayerRotation();
        this.currentRotation = new Rotation(rotation.getYaw(), MathHelper.clamp((float)rotation.getPitch(), (float)-90.0f, (float)90.0f));
        if (!this.cameraActive) {
            this.cameraEntity = FreeCamera.minecraftClient.options.getPerspective();
            if (this.cameraEntity == null) {
                this.cameraEntity = Perspective.FIRST_PERSON;
            }
        }
        this.cameraActive = false;
        if (this.animation.isEnabled()) {
            this.exitAnimation.setEasing(enterEasing);
            this.exitAnimation.setDuration(350L);
            if (this.exitAnimation.getValue() <= 0.0f) {
                this.exitAnimation.setValue(0.0f);
            }
        } else {
            this.exitAnimation.setValue(1.0f);
        }
    }

    @Override
    public final void onDisable() {
        if (!this.animation.isEnabled()) {
            this.exitAnimation.setValue(0.0f);
            this.cameraActive = false;
            if (this.cameraEntity != null) {
                FreeCamera.minecraftClient.options.setPerspective(this.cameraEntity);
                this.cameraEntity = null;
            }
            return;
        }
        this.cameraActive = true;
        this.exitAnimation.setEasing(exitEasing);
        this.previousPosition = new Vec3d((double)this.toggleAnimation.getValue(), (double)this.enterAnimation.getValue(), (double)this.cameraAnimation.getValue());
        this.previousRotation = new Rotation(this.currentRotation.getYaw(), this.currentRotation.getPitch());
        double d = this.previousPosition.distanceTo(this.getPosition(1.0f));
        long l = (long)MathHelper.clamp((double)(150.0 + d * 25.0), (double)180.0, (double)700.0);
        this.exitAnimation.setDuration(l);
    }

    @Generated
    public Vec3d getCameraVelocity() {
        return this.position;
    }

    @Generated
    public Rotation getCurrentRotation() {
        return this.currentRotation;
    }
}

