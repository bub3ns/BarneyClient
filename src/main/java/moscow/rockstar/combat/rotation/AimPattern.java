/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.combat.rotation;

import java.util.concurrent.ThreadLocalRandom;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.NeuralAimModel;
import net.minecraft.util.math.MathHelper;

public final class AimPattern {
    private static final float MAX_CONSECUTIVE_MISSES = 20.0f;
    private float[] trajectoryModelWeights;
    private float consecutiveMissCount = 20.0f;
    private float stationaryTicks;
    private float targetYaw;
    private float targetPitch;
    private float remainingYawError;
    private float remainingPitchError;
    private float previousYawError;
    private float previousPitchError;
    private float previousYawStep;
    private float previousPitchStep;
    private float olderYawStep;
    private float olderPitchStep;
    private float aimErrorScale = 5.0f;
    private float stationaryErrorTolerance = 15.0f;
    private double modelDistanceLog = 3.0;
    private boolean aimAligned;

    public boolean isModelLoaded() {
        return this.trajectoryModelWeights != null;
    }

    public void initializeModelState(NeuralAimModel neuralAimModel, float f, float f2, float f3, float f4) {
        this.trajectoryModelWeights = neuralAimModel.getOutputBiases();
        this.targetYaw = f3;
        this.targetPitch = f4;
        this.stationaryTicks = 0.0f;
        this.olderPitchStep = 0.0f;
        this.olderYawStep = 0.0f;
        this.previousPitchStep = 0.0f;
        this.previousYawStep = 0.0f;
        this.remainingYawError = this.previousYawError = MathHelper.wrapDegrees((float)(f3 - f));
        this.remainingPitchError = this.previousPitchError = f4 - f2;
    }

    public void clearModelState() {
        this.trajectoryModelWeights = null;
        this.stationaryTicks = 0.0f;
    }

    public void resetConsecutiveMisses() {
        this.consecutiveMissCount = 0.0f;
    }

    public void incrementConsecutiveMisses() {
        this.consecutiveMissCount = Math.min(this.consecutiveMissCount + 1.0f, MAX_CONSECUTIVE_MISSES);
    }

    public boolean isAimAligned() {
        return this.aimAligned;
    }

    public boolean calculateAimCorrection(NeuralAimModel neuralAimModel, float f, float f2, float f3, float f4, float f5, float f6, double d, float f7, int n, float f8, int n2, float f9, float[] fArray) {
        float f10;
        boolean bl;
        if (neuralAimModel == null) {
            return false;
        }
        if (this.trajectoryModelWeights == null) {
            this.initializeModelState(neuralAimModel, f, f2, f3, f4);
        }
        this.aimErrorScale = f5;
        this.stationaryErrorTolerance = f6;
        this.modelDistanceLog = d;
        float f11 = AimRotationMath.getMouseRotationStep();
        float f12 = MathHelper.wrapDegrees((float)(f3 - this.targetYaw));
        float f13 = f4 - this.targetPitch;
        this.targetYaw = f3;
        this.targetPitch = f4;
        float f14 = MathHelper.wrapDegrees((float)(f3 - f));
        float f15 = f4 - f2;
        float[] fArray2 = neuralAimModel.addVectors(this.buildFeatureVector(neuralAimModel, f12, f13), this.trajectoryModelWeights);
        float f16 = 0.0f;
        float f17 = 0.0f;
        boolean bl2 = bl = this.stationaryTicks >= (float)Math.min(n, neuralAimModel.getHiddenSize());
        if (bl || ThreadLocalRandom.current().nextFloat() >= NeuralAimModel.normalizeInput(fArray2[0])) {
            f10 = Float.MAX_VALUE;
            for (int i = n2; i > 0; --i) {
                int n3 = 1 + 6 * neuralAimModel.argmax(fArray2, ThreadLocalRandom.current().nextFloat());
                float f18 = NeuralAimModel.denormalizeOutput(fArray2[n3 + 5]);
                float f19 = (float)Math.sqrt(Math.max(0.0f, 1.0f - f18 * f18));
                float f20 = AimPattern.sampleGaussianNoise();
                float f21 = f18 * f20 + f19 * AimPattern.sampleGaussianNoise();
                float f22 = AimPattern.quantizeToStep(f9 * neuralAimModel.blendPrediction(fArray2[n3 + 1], fArray2[n3 + 3], true, f20, f8), f11);
                float f23 = AimPattern.quantizeToStep(f9 * neuralAimModel.blendPrediction(fArray2[n3 + 2], fArray2[n3 + 4], false, f21, f8), f11);
                float f24 = Math.abs((float)Math.hypot(MathHelper.wrapDegrees((float)(f14 - f22)) / this.aimErrorScale, (f15 - f23) / this.stationaryErrorTolerance) - f7);
                if (!(f24 < f10)) continue;
                f10 = f24;
                f16 = f22;
                f17 = f23;
            }
        }
        if (bl && f16 == 0.0f && f17 == 0.0f) {
            if (Math.abs(f14) >= Math.abs(f15)) {
                f16 = Math.copySign(f11, f14);
            } else {
                f17 = Math.copySign(f11, f15);
            }
        }
        this.stationaryTicks = f16 == 0.0f && f17 == 0.0f ? this.stationaryTicks + 1.0f : 0.0f;
        f10 = MathHelper.clamp((float)(f2 + f17), (float)-90.0f, (float)90.0f);
        f17 = f10 - f2;
        this.olderYawStep = this.previousYawStep;
        this.olderPitchStep = this.previousPitchStep;
        this.previousYawStep = f16;
        this.previousPitchStep = f17;
        this.previousYawError = this.remainingYawError;
        this.previousPitchError = this.remainingPitchError;
        this.remainingYawError = MathHelper.wrapDegrees((float)(f3 - (f + f16)));
        this.remainingPitchError = f4 - f10;
        this.aimAligned = Math.abs(this.remainingYawError) <= this.aimErrorScale && Math.abs(this.remainingPitchError) <= this.stationaryErrorTolerance;
        fArray[0] = f16;
        fArray[1] = f17;
        return true;
    }

    private float[] buildFeatureVector(NeuralAimModel neuralAimModel, float f, float f2) {
        return new float[]{AimPattern.transformFeatureValue(this.remainingYawError), AimPattern.transformFeatureValue(this.remainingPitchError), AimPattern.transformFeatureValue(MathHelper.wrapDegrees((float)(this.remainingYawError - this.previousYawError))), AimPattern.transformFeatureValue(this.remainingPitchError - this.previousPitchError), AimPattern.transformFeatureValue(f), AimPattern.transformFeatureValue(f2), AimPattern.transformFeatureValue(this.previousYawStep), AimPattern.transformFeatureValue(this.previousPitchStep), AimPattern.transformFeatureValue(this.olderYawStep), AimPattern.transformFeatureValue(this.olderPitchStep), AimPattern.transformFeatureValue(this.remainingYawError / this.aimErrorScale), AimPattern.transformFeatureValue(this.remainingPitchError / this.stationaryErrorTolerance), (float)Math.log(Math.max(this.modelDistanceLog, 0.05) + 0.5) / 2.0f, (float)Math.log(this.consecutiveMissCount) / 3.0f, this.aimAligned ? 1.0f : 0.0f, this.consecutiveMissCount / MAX_CONSECUTIVE_MISSES, this.stationaryTicks / (float)neuralAimModel.getHiddenSize()};
    }

    private static float transformFeatureValue(float f) {
        return (float)(Math.log((double)f + Math.sqrt((double)(f * f) + 1.0)) / 3.0);
    }

    private static float sampleGaussianNoise() {
        return (float)ThreadLocalRandom.current().nextGaussian();
    }

    private static float quantizeToStep(float f, float f2) {
        return (float)Math.round(f / f2) * f2;
    }
}
