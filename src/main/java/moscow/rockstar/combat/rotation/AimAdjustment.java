/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MathHelper
 *  net.minecraft.ClientPlayerEntity
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.combat.rotation;

import moscow.rockstar.combat.rotation.AimPattern;
import moscow.rockstar.combat.rotation.NeuralAimModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class AimAdjustment {
    private static final float BASE_TARGET_DISTANCE = 3.5f;
    private static final float MIN_TARGET_DISTANCE = 0.6f;
    private static final float MAX_TARGET_DISTANCE = 1.8f;
    private static final float MIN_TARGET_HEIGHT = 0.3f;
    private static final int SAMPLE_COUNT = 4;
    private static final int PREDICTION_TICKS = 12;
    private static final float RANDOMNESS_SCALE = 1.0f;
    private static final float MAX_VERTICAL_OFFSET = 25.0f;
    private static final float MAX_YAW_DELTA = 90.0f;
    private static final float MAX_PITCH_DELTA = 30.0f;
    private static final AimAdjustment INSTANCE = new AimAdjustment();
    private final AimPattern aimPattern = new AimPattern();
    private final float[] rotationOffsets = new float[2];
    private int lastPlayerTick = Integer.MIN_VALUE;

    private AimAdjustment() {
    }

    public static boolean isModelAvailable() {
        return NeuralAimModel.getActiveModel() != null;
    }

    @Nullable
    public static String getActiveModelName() {
        return NeuralAimModel.getActiveModel() == null ? null : NeuralAimModel.getActiveModelName();
    }

    public static boolean calculateAimAdjustment(float f, float f2, float f3, float f4, float[] fArray) {
        return INSTANCE.calculateAimAdjustmentInternal(f, f2, f3, f4, fArray);
    }

    private boolean calculateAimAdjustmentInternal(float f, float f2, float f3, float f4, float[] fArray) {
        NeuralAimModel neuralAimModel = NeuralAimModel.getActiveModel();
        if (neuralAimModel == null) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity class_7462 = client.player;
        if (class_7462 == null || client.world == null) {
            return false;
        }
        if (Math.abs(MathHelper.wrapDegrees((float)(f3 - f))) > 90.0f || Math.abs(f4) > 30.0f) {
            this.aimPattern.clearModelState();
            return false;
        }
        if (class_7462.age - this.lastPlayerTick > 3) {
            this.aimPattern.clearModelState();
        }
        this.lastPlayerTick = class_7462.age;
        double d = 3.5 * Math.max(Math.cos(Math.toRadians(f4)), 0.05);
        float f5 = (float)Math.max(Math.toDegrees(Math.atan2(0.3f, d)), 0.5);
        float f6 = (float)Math.max(Math.toDegrees(Math.atan2(0.9f, d)), 0.5);
        if (!this.aimPattern.isModelLoaded()) {
            this.aimPattern.initializeModelState(neuralAimModel, f, f2, f3, f4);
        }
        if (!this.aimPattern.calculateAimCorrection(neuralAimModel, f, f2, f3, f4, f5, f6, 3.5, 0.0f, 12, 0.3f, 4, 1.0f, this.rotationOffsets)) {
            return false;
        }
        this.aimPattern.incrementConsecutiveMisses();
        float f7 = MathHelper.clamp((float)this.rotationOffsets[0], (float)-25.0f, (float)25.0f);
        float f8 = MathHelper.clamp((float)this.rotationOffsets[1], (float)-25.0f, (float)25.0f);
        if (Float.isNaN(f7) || Float.isNaN(f8)) {
            this.aimPattern.clearModelState();
            return false;
        }
        fArray[0] = f7;
        fArray[1] = f8;
        return true;
    }
}

