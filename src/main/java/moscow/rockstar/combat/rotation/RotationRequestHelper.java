/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.combat.rotation;

import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimAdjustment;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.ClientFeatureFlags;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.math.Rotation;

public final class RotationRequestHelper {
    private RotationRequestHelper() {
    }

    public static void requestAdaptiveRotation(Rotation rotation) {
        if (ClientFeatureFlags.neuroRotationEnabled) {
            RotationManager rotationManager = ClientServiceRegistry.getInstance().getRotationManager();
            Rotation rotation2 = rotationManager.isIdle() ? rotationManager.getPlayerRotation() : rotationManager.getCurrentRotation();
            float[] fArray = new float[2];
            if (AimAdjustment.calculateAimAdjustment(rotation2.getYaw(), rotation2.getPitch(), rotation.getYaw(), rotation.getPitch(), fArray)) {
                Rotation rotation3 = new Rotation(rotation2.getYaw() + fArray[0], Math.max(-90.0f, Math.min(90.0f, rotation2.getPitch() + fArray[1])));
                rotationManager.requestRotation(rotation3, RotationCorrectionMode.DIRECT, 180.0f, 180.0f, 30.0f, RotationPriority.STANDARD_PRIORITY);
                return;
            }
        }
        RotationRequestHelper.requestDirectRotation(rotation, 30.0f, 30.0f, 30.0f);
    }

    public static void requestDirectRotation(Rotation rotation, float f, float f2, float f3) {
        ClientServiceRegistry.getInstance().getRotationManager().requestRotation(rotation, RotationCorrectionMode.DIRECT, f, f2, f3, RotationPriority.STANDARD_PRIORITY);
    }

    public static void refreshRotation() {
        ClientServiceRegistry.getInstance().getRotationManager().refreshPendingRotation();
    }
}

