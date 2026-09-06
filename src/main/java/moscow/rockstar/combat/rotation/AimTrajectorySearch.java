/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.combat.rotation;

import moscow.rockstar.math.DoubleLookupTable;
import moscow.rockstar.render.particles.ParticlePhysics;

public final class AimTrajectorySearch {
    private static final float[] ANGLE_CANDIDATES = new float[]{-60.0f, -35.0f, -15.0f, 0.0f, 15.0f, 40.0f, 65.0f, 90.0f};
    private static final double BASE_TRAJECTORY_SCORE = 50.0;
    private static final double UPWARD_VELOCITY_SCORE = 100.0;
    private static final double NO_TRAJECTORY_SCORE = Double.NEGATIVE_INFINITY;
    private final ParticlePhysics.ParticleState[] currentStates = AimTrajectorySearch.createParticleStateArray(ANGLE_CANDIDATES.length);
    private final ParticlePhysics.ParticleState[] candidateStates = AimTrajectorySearch.createParticleStateArray(ANGLE_CANDIDATES.length);
    private final double[] currentScores = new double[ANGLE_CANDIDATES.length];
    private final double[] nextScores = new double[ANGLE_CANDIDATES.length];
    private final float[] currentAngles = new float[ANGLE_CANDIDATES.length];
    private final float[] nextAngles = new float[ANGLE_CANDIDATES.length];
    private final float[] visitedAngles = new float[ANGLE_CANDIDATES.length];
    private final int maxSteps;
    private final ParticlePhysics.ParticleState scratchState = new ParticlePhysics.ParticleState();

    public AimTrajectorySearch(int n) {
        this.maxSteps = Math.max(2, n);
    }

    private static ParticlePhysics.ParticleState[] createParticleStateArray(int n) {
        ParticlePhysics.ParticleState[] particleStateArray = new ParticlePhysics.ParticleState[n];
        for (int i = 0; i < n; ++i) {
            particleStateArray[i] = new ParticlePhysics.ParticleState();
        }
        return particleStateArray;
    }

    public float findBestTrajectoryAngle(ParticlePhysics.ParticleState particleState, float f, float f2, DoubleLookupTable doubleLookupTable, int n) {
        float f3 = f;
        double d = Double.NEGATIVE_INFINITY;
        int n2 = 0;
        for (float f4 : ANGLE_CANDIDATES) {
            float f5 = AimTrajectorySearch.stepTowardAngle(f, f4, f2);
            boolean bl = false;
            for (int i = 0; i < n2; ++i) {
                if (this.visitedAngles[i] != f5) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            this.visitedAngles[n2++] = f5;
            double d2 = this.evaluateTrajectory(particleState, f5, f2, doubleLookupTable, n);
            if (!(d2 > d)) continue;
            d = d2;
            f3 = f5;
        }
        return f3;
    }

    private double evaluateTrajectory(ParticlePhysics.ParticleState particleState, float f, float f2, DoubleLookupTable doubleLookupTable, int n) {
        int n2;
        for (n2 = 0; n2 < ANGLE_CANDIDATES.length; ++n2) {
            this.currentScores[n2] = Double.NEGATIVE_INFINITY;
        }
        this.currentStates[0].copyFrom(particleState);
        this.currentScores[0] = AimTrajectorySearch.advanceParticleAndScore(this.currentStates[0], f, doubleLookupTable, n);
        this.currentAngles[0] = f;
        for (n2 = 1; n2 < this.maxSteps; ++n2) {
            int n3;
            for (n3 = 0; n3 < ANGLE_CANDIDATES.length; ++n3) {
                this.nextScores[n3] = Double.NEGATIVE_INFINITY;
            }
            for (n3 = 0; n3 < ANGLE_CANDIDATES.length; ++n3) {
                if (this.currentScores[n3] == Double.NEGATIVE_INFINITY) continue;
                for (int i = 0; i < ANGLE_CANDIDATES.length; ++i) {
                    float f3 = AimTrajectorySearch.stepTowardAngle(this.currentAngles[n3], ANGLE_CANDIDATES[i], f2);
                    this.scratchState.copyFrom(this.currentStates[n3]);
                    double d = this.currentScores[n3] + AimTrajectorySearch.advanceParticleAndScore(this.scratchState, f3, doubleLookupTable, n);
                    if (!(d > this.nextScores[i])) continue;
                    this.nextScores[i] = d;
                    this.nextAngles[i] = f3;
                    this.candidateStates[i].copyFrom(this.scratchState);
                }
            }
            for (n3 = 0; n3 < ANGLE_CANDIDATES.length; ++n3) {
                this.currentScores[n3] = this.nextScores[n3];
                this.currentAngles[n3] = this.nextAngles[n3];
                if (this.nextScores[n3] == Double.NEGATIVE_INFINITY) continue;
                this.currentStates[n3].copyFrom(this.candidateStates[n3]);
            }
        }
        double d = Double.NEGATIVE_INFINITY;
        for (double d2 : this.currentScores) {
            d = Math.max(d, d2);
        }
        return d;
    }

    private static double advanceParticleAndScore(ParticlePhysics.ParticleState particleState, float f, DoubleLookupTable doubleLookupTable, int n) {
        double d = particleState.position;
        double d2 = d + particleState.verticalVelocity;
        double d3 = particleState.verticalVelocity;
        boolean bl = ParticlePhysics.advanceParticle(particleState, f, doubleLookupTable.getLowerSample(d), doubleLookupTable.getLowerSample(d2), doubleLookupTable.getUpperSample(d2), n);
        double d4 = bl ? 50.0 + 100.0 * Math.max(0.0, d3) : 0.0;
        return particleState.position - d - d4;
    }

    private static float stepTowardAngle(float f, float f2, float f3) {
        float f4 = f2 - f;
        if (f4 > f3) {
            return f + f3;
        }
        if (f4 < -f3) {
            return f - f3;
        }
        return f2;
    }
}

