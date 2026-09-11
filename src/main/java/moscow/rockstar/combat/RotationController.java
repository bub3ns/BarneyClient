/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.LivingEntity
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.Text
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.combat;

import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimPattern;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.NeuralAimModel;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationLogWriter;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.combat.aura.rotation.AuraRotationMode;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class RotationController
extends AuraRotationMode {
    private static final float BASE_RANDOMNESS = 0.45f;
    private static final float MODEL_SMOOTHING = 0.7f;
    private static final int SAMPLE_COUNT = 4;
    private static final float MAX_YAW_OFFSET = 1.1f;
    private static final float MIN_YAW_OFFSET = 0.8f;
    private static final int PREDICTION_STEPS = 12;
    private static final float BASE_AIM_WEIGHT = 0.5f;
    private static final int MAX_ROTATION_ATTEMPTS = 30;
    private static final float MIN_PITCH_OFFSET = 3.0f;
    private static final float MAX_PITCH_OFFSET = 5.0f;
    private static final float MAX_TARGET_DISTANCE = 15.0f;
    private static final float ROTATION_DISTANCE_SCALE = 3.0f;
    private final AimPattern targetAimPattern = new AimPattern();
    private final AimPattern correctionAimPattern = new AimPattern();
    private final float[] rotationOffsets = new float[2];
    private final RotationLogWriter rotationLogger = new RotationLogWriter();
    private int targetEntityId = -1;
    private int rotationRetryCount;
    private boolean attackRecentlyStarted;
    private boolean modelWarningShown;

    public RotationController(ModeSetting modeSetting) {
        super(modeSetting, "\u041d\u0435\u0439\u0440\u043e");
    }

    @Override
    public void rotate(RotationManager rotationManager, float f, boolean bl, boolean bl2, RotationCorrectionMode rotationCorrectionMode, LivingEntity class_13092) {
        if (RotationController.minecraftClient.player == null || class_13092 == null) {
            return;
        }
        NeuralAimModel neuralAimModel = NeuralAimModel.getActiveModel();
        if (neuralAimModel == null) {
            this.showModelWarning();
            rotationManager.requestRotation(AimRotationMath.getRotationToPoint(class_13092.getBoundingBox().getCenter()), rotationCorrectionMode, 180.0f, 180.0f, 180.0f, RotationPriority.TARGET_PRIORITY);
            return;
        }
        Box Vec3i = class_13092.getBoundingBox();
        Vec3d VanillaChestLootTableGenerator = RotationController.minecraftClient.player.getEyePos();
        Vec3d WallPlayerSkullBlock = Vec3i.getCenter().subtract(VanillaChestLootTableGenerator);
        double d = Math.max(Math.hypot(WallPlayerSkullBlock.x, WallPlayerSkullBlock.z), 0.05);
        float f2 = (float)Math.toDegrees(Math.atan2(WallPlayerSkullBlock.z, WallPlayerSkullBlock.x)) - 90.0f;
        float f3 = (float)(-Math.toDegrees(Math.atan2(WallPlayerSkullBlock.y, d)));
        float f4 = Math.max((float)Math.toDegrees(Math.atan2(Vec3i.getLengthX() / 2.0, d)), 0.5f);
        float f5 = Math.max((float)Math.toDegrees(Math.atan2(Vec3i.getLengthY() / 2.0, d)), 0.5f);
        double d2 = RotationController.distanceToBox(VanillaChestLootTableGenerator, Vec3i);
        Rotation rotation = rotationManager.getEffectiveRotation();
        float targetYaw = moscow.rockstar.api.rotation.ContinuousRotationMath.unwrapYaw(rotation.getYaw(), f2);
        if (class_13092.getId() != this.targetEntityId || !this.targetAimPattern.isModelLoaded()) {
            this.targetAimPattern.initializeModelState(neuralAimModel, rotation.getYaw(), rotation.getPitch(), targetYaw, f3);
            this.targetEntityId = class_13092.getId();
        }
        this.rotationRetryCount = 0;
        float f6 = 0.45f * neuralAimModel.activate(ThreadLocalRandom.current().nextFloat());
        if (!this.targetAimPattern.calculateAimCorrection(neuralAimModel, rotation.getYaw(), rotation.getPitch(), targetYaw, f3, f4, f5, d2, f6, 12, 0.7f, 4, this.getMaxYawOffset(), this.rotationOffsets)) {
            return;
        }
        rotationManager.requestRotation(new Rotation(rotation.getYaw() + this.rotationOffsets[0], MathHelper.clamp((float)(rotation.getPitch() + this.rotationOffsets[1]), (float)-90.0f, (float)90.0f)), rotationCorrectionMode, 180.0f, 180.0f, 180.0f, RotationPriority.TARGET_PRIORITY);
        if (this.attackRecentlyStarted) {
            this.attackRecentlyStarted = false;
            this.targetAimPattern.resetConsecutiveMisses();
        } else {
            this.targetAimPattern.incrementConsecutiveMisses();
        }
    }

    public Rotation calculateCorrectionRotation(Rotation rotation, Rotation rotation2) {
        NeuralAimModel neuralAimModel = NeuralAimModel.getActiveModel();
        if (neuralAimModel == null || RotationController.minecraftClient.player == null) {
            return null;
        }
        float f = Math.max(3.0f, AimRotationMath.getMouseRotationStep());
        if (Math.abs(MathHelper.wrapDegrees((float)(rotation2.getYaw() - rotation.getYaw()))) <= f && Math.abs(rotation2.getPitch() - rotation.getPitch()) <= f) {
            this.correctionAimPattern.clearModelState();
            this.rotationRetryCount = 0;
            return null;
        }
        if (++this.rotationRetryCount > 30) {
            this.correctionAimPattern.clearModelState();
            return null;
        }
        float targetYaw2 = moscow.rockstar.api.rotation.ContinuousRotationMath.unwrapYaw(rotation.getYaw(), rotation2.getYaw());
        if (!this.correctionAimPattern.isModelLoaded()) {
            this.correctionAimPattern.initializeModelState(neuralAimModel, rotation.getYaw(), rotation.getPitch(), targetYaw2, rotation2.getPitch());
        }
        if (!this.correctionAimPattern.calculateAimCorrection(neuralAimModel, rotation.getYaw(), rotation.getPitch(), targetYaw2, rotation2.getPitch(), 5.0f, 15.0f, 3.0, 0.0f, 12, 0.7f, 4, 1.1f, this.rotationOffsets)) {
            return null;
        }
        this.correctionAimPattern.incrementConsecutiveMisses();
        return new Rotation(rotation.getYaw() + this.rotationOffsets[0], MathHelper.clamp((float)(rotation.getPitch() + this.rotationOffsets[1]), (float)-90.0f, (float)90.0f));
    }

    private float getMaxYawOffset() {
        return RotationController.minecraftClient.player != null && RotationController.minecraftClient.player.isSubmergedInWater() && ServerDetector.isInventoryServer() ? 0.8f : 1.1f;
    }

    @Override
    public void onAttack() {
        this.attackRecentlyStarted = true;
    }

    @Override
    public void enabled() {
        NeuralAimModel.reloadModel();
        this.targetAimPattern.clearModelState();
        this.correctionAimPattern.clearModelState();
        this.targetEntityId = -1;
        this.modelWarningShown = false;
    }

    @Override
    public void onTargetLost() {
        this.targetAimPattern.clearModelState();
        this.targetEntityId = -1;
        this.attackRecentlyStarted = false;
    }

    public boolean isModelAvailable() {
        return NeuralAimModel.getActiveModel() != null;
    }

    private void showModelWarning() {
        if (this.modelWarningShown) {
            return;
        }
        this.modelWarningShown = true;
        Notification.error((Text)Text.literal((String)("\u041c\u043e\u0434\u0435\u043b\u044c " + NeuralAimModel.getActiveModelName() + " \u043d\u0435 \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u043b\u0430\u0441\u044c \u2014 \u043e\u0431\u0443\u0447\u0438 \u0447\u0435\u0440\u0435\u0437 .neuro train \u0438\u043b\u0438 \u0432\u044b\u0431\u0435\u0440\u0438 \u0434\u0440\u0443\u0433\u0443\u044e: .neuro list")));
    }

    public static float getModelSmoothing() {
        return 0.7f;
    }

    public static double distanceToBox(Vec3d VanillaChestLootTableGenerator, Box Vec3i) {
        double d = Math.max(Math.max(Vec3i.minX - VanillaChestLootTableGenerator.x, 0.0), VanillaChestLootTableGenerator.x - Vec3i.maxX);
        double d2 = Math.max(Math.max(Vec3i.minY - VanillaChestLootTableGenerator.y, 0.0), VanillaChestLootTableGenerator.y - Vec3i.maxY);
        double d3 = Math.max(Math.max(Vec3i.minZ - VanillaChestLootTableGenerator.z, 0.0), VanillaChestLootTableGenerator.z - Vec3i.maxZ);
        return Math.sqrt(d * d + d2 * d2 + d3 * d3);
    }

    @Generated
    public RotationLogWriter getRotationLogger() {
        return this.rotationLogger;
    }
}

