/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.combat.rotation;

import lombok.Generated;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationReturnMode;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.api.access.RotationStepAccess;

public class RotationRequest {
    private final Rotation targetRotation;
    private final RotationCorrectionMode correctionMode;
    private final float yawStep;
    private final float pitchStep;
    private float rotationSpeed;
    private final int priority;
    private final boolean snapToMouse;
    private RotationReturnMode returnMode = RotationReturnMode.SMOOTH;
    private RotationStepAccess rotationStep;

    public RotationRequest(Rotation rotation, RotationCorrectionMode rotationCorrectionMode, float f, float f2, float f3, int n) {
        this(rotation, rotationCorrectionMode, f, f2, f3, n, true);
    }

    public RotationRequest(Rotation rotation, RotationCorrectionMode rotationCorrectionMode, float f, float f2, float f3, int n, boolean bl) {
        this.targetRotation = rotation;
        this.correctionMode = rotationCorrectionMode;
        this.yawStep = f;
        this.pitchStep = f2;
        this.priority = n;
        this.rotationSpeed = f3;
        this.snapToMouse = bl;
    }

    public RotationRequest(Rotation rotation, float f, float f2, long l, int n) {
        this(rotation, RotationCorrectionMode.NONE, f, f2, l, n);
    }

    @Generated
    public Rotation getTargetRotation() {
        return this.targetRotation;
    }

    @Generated
    public RotationCorrectionMode getCorrectionMode() {
        return this.correctionMode;
    }

    @Generated
    public float getYawStep() {
        return this.yawStep;
    }

    @Generated
    public float getPitchStep() {
        return this.pitchStep;
    }

    @Generated
    public float getRotationSpeed() {
        return this.rotationSpeed;
    }

    @Generated
    public int getPriority() {
        return this.priority;
    }

    @Generated
    public boolean isSnapToMouse() {
        return this.snapToMouse;
    }

    @Generated
    public RotationReturnMode getReturnMode() {
        return this.returnMode;
    }

    @Generated
    public RotationStepAccess getRotationStep() {
        return this.rotationStep;
    }

    @Generated
    public void setRotationSpeed(float f) {
        this.rotationSpeed = f;
    }

    @Generated
    public void setReturnMode(RotationReturnMode rotationReturnMode) {
        this.returnMode = rotationReturnMode;
    }

    @Generated
    public void setRotationStep(RotationStepAccess rotationStepAccess) {
        this.rotationStep = rotationStepAccess;
    }
}
