/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.render.particles;

public final class ParticlePhysics {
    public static final double GRAVITY_ACCELERATION = 0.08;
    private static final double GROUND_BOUNCE_INCREMENT = 0.13;
    private static final double GROUND_BOUNCE_DAMPING = 0.546;
    private static final double AIRBORNE_ACCELERATION = 0.026;
    private static final double AIRBORNE_DAMPING = 0.91;
    public static final double MIN_BOUNCE_VELOCITY = 0.42;
    public static final double GROUND_CONTACT_OFFSET = 0.2;
    public static final double GROUND_CLEARANCE = 0.6;

    private ParticlePhysics() {
    }

    public static boolean advanceParticle(ParticleState particleState, double d, double d2, double d3, double d4, int n) {
        double d5;
        boolean bl;
        boolean bl2;
        boolean bl3 = particleState.grounded || !particleState.airborne;
        boolean bl4 = bl2 = bl3 && !particleState.colliding;
        if (bl2 && particleState.grounded) {
            particleState.horizontalVelocity = Math.max(0.42, particleState.horizontalVelocity);
            particleState.verticalVelocity += 0.2;
        }
        if (bl2 && !particleState.grounded && !particleState.airborne) {
            particleState.airborne = true;
        }
        if (particleState.airborne) {
            ParticlePhysics.applyMotionForAngle(particleState, d);
        } else if (particleState.grounded) {
            particleState.verticalVelocity = (particleState.verticalVelocity + 0.13) * 0.546;
            particleState.horizontalVelocity = (particleState.horizontalVelocity - 0.08) * 0.98;
        } else {
            particleState.verticalVelocity = (particleState.verticalVelocity + 0.026) * 0.91;
            particleState.horizontalVelocity = (particleState.horizontalVelocity - 0.08) * 0.98;
        }
        if (particleState.remainingBoostTicks >= 0 && --particleState.remainingBoostTicks < 0) {
            particleState.airborne = false;
        }
        double d6 = particleState.verticalPosition + particleState.horizontalVelocity;
        boolean bl5 = bl = d4 > d2 + 0.6 && d4 > Math.max(particleState.verticalPosition, d6);
        if (bl) {
            particleState.verticalVelocity = 0.0;
        } else {
            particleState.position += particleState.verticalVelocity;
        }
        double d7 = d5 = bl ? d2 : d3;
        if (d6 <= d5) {
            particleState.verticalPosition = d5;
            particleState.horizontalVelocity = 0.0;
            particleState.grounded = true;
            if (particleState.remainingBoostTicks < 0 && particleState.airborne) {
                particleState.remainingBoostTicks = n;
            }
        } else {
            particleState.verticalPosition = d6;
            particleState.grounded = false;
        }
        particleState.colliding = bl2;
        return bl;
    }

    private static void applyMotionForAngle(ParticleState particleState, double d) {
        double d2;
        double d3 = Math.toRadians(d);
        double d4 = Math.cos(d3);
        double d5 = d4 * d4;
        double d6 = Math.abs(particleState.verticalVelocity);
        particleState.horizontalVelocity += 0.08 * (-1.0 + d5 * 0.75);
        if (particleState.horizontalVelocity < 0.0 && d4 > 0.0) {
            d2 = particleState.horizontalVelocity * -0.1 * d5;
            particleState.verticalVelocity += d2;
            particleState.horizontalVelocity += d2;
        }
        if (d3 < 0.0 && d4 > 0.0) {
            d2 = d6 * -Math.sin(d3) * 0.04;
            particleState.verticalVelocity -= d2;
            particleState.horizontalVelocity += d2 * 3.2;
        }
        if (d4 > 0.0) {
            particleState.verticalVelocity += (d6 - particleState.verticalVelocity) * 0.1;
        }
        particleState.verticalVelocity *= 0.99;
        particleState.horizontalVelocity *= 0.98;
    }

    public static final class ParticleState {
        public double position;
        public double verticalPosition;
        public double verticalVelocity;
        public double horizontalVelocity;
        public boolean airborne;
        public boolean grounded;
        public boolean colliding;
        public int remainingBoostTicks;

        public void copyFrom(ParticleState particleState) {
            this.position = particleState.position;
            this.verticalPosition = particleState.verticalPosition;
            this.verticalVelocity = particleState.verticalVelocity;
            this.horizontalVelocity = particleState.horizontalVelocity;
            this.airborne = particleState.airborne;
            this.grounded = particleState.grounded;
            this.colliding = particleState.colliding;
            this.remainingBoostTicks = particleState.remainingBoostTicks;
        }
    }
}

