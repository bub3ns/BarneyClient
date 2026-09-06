/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.ClientWorld
 *  net.minecraft.ClientPlayerEntity
 */
package moscow.rockstar.movement;

import java.util.concurrent.ThreadLocalRandom;
import moscow.rockstar.combat.RotationController;
import moscow.rockstar.combat.rotation.AimPattern;
import moscow.rockstar.combat.rotation.NeuralAimModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.ClientPlayerEntity;

public final class MovementUtils {
    private static final float MIN_TARGET_DISTANCE = 0.5f;
    private static final float RANDOMNESS_FACTOR = 0.45f;
    private static final int AIM_ITERATIONS = 4;
    private static final int RAY_SAMPLE_COUNT = 12;
    private static final float AIM_SCALE = 1.0f;
    private final AimPattern aimPattern = new AimPattern();
    private final float[] screenCoordinates = new float[2];
    private int lastEntityId = -1;
    private boolean alternatePass;

    public boolean isDataClientAvailable() {
        return NeuralAimModel.getActiveModel() != null;
    }

    public void enableAimAssist() {
        this.alternatePass = true;
    }

    public void resetAimAssist() {
        this.aimPattern.clearModelState();
        this.lastEntityId = -1;
        this.alternatePass = false;
    }

    public boolean calculateAimCorrection(ClientPlayerEntity class_7462, ClientWorld NarrationMessageBuilder, LivingEntity class_13092, float f, float[] fArray) {
        NeuralAimModel neuralAimModel = NeuralAimModel.getActiveModel();
        if (neuralAimModel == null || class_7462 == null || NarrationMessageBuilder == null || class_13092 == null) {
            return false;
        }
        Box HorizontalFacingBlock = class_13092.getBoundingBox();
        Vec3d VanillaChestLootTableGenerator = class_7462.getEyePos();
        Vec3d WallPlayerSkullBlock = HorizontalFacingBlock.getCenter().subtract(VanillaChestLootTableGenerator);
        double d = Math.max(Math.hypot(WallPlayerSkullBlock.x, WallPlayerSkullBlock.z), 0.05);
        float f2 = (float)Math.toDegrees(Math.atan2(WallPlayerSkullBlock.z, WallPlayerSkullBlock.x)) - 90.0f;
        float f3 = (float)(-Math.toDegrees(Math.atan2(WallPlayerSkullBlock.y, d)));
        float f4 = Math.max((float)Math.toDegrees(Math.atan2(HorizontalFacingBlock.getLengthX() / 2.0, d)), 0.5f);
        float f5 = Math.max((float)Math.toDegrees(Math.atan2(HorizontalFacingBlock.getLengthY() / 2.0, d)), 0.5f);
        if (class_13092.getId() != this.lastEntityId || !this.aimPattern.isModelLoaded()) {
            this.aimPattern.initializeModelState(neuralAimModel, class_7462.getYaw(), class_7462.getPitch(), f2, f3);
            this.lastEntityId = class_13092.getId();
        }
        float f6 = 0.45f * neuralAimModel.activate(ThreadLocalRandom.current().nextFloat());
        if (!this.aimPattern.calculateAimCorrection(neuralAimModel, class_7462.getYaw(), class_7462.getPitch(), f2, f3, f4, f5, RotationController.distanceToBox(VanillaChestLootTableGenerator, HorizontalFacingBlock), f6, 12, f, 4, 1.0f, this.screenCoordinates)) {
            return false;
        }
        if (this.alternatePass) {
            this.alternatePass = false;
            this.aimPattern.resetConsecutiveMisses();
        } else {
            this.aimPattern.incrementConsecutiveMisses();
        }
        fArray[0] = this.screenCoordinates[0];
        fArray[1] = this.screenCoordinates[1];
        return true;
    }
}

