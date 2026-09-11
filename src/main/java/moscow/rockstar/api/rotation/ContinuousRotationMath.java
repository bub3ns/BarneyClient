package moscow.rockstar.api.rotation;

import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.math.Rotation;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Pure mathematical utilities for continuous, unwrapped Euler rotations.
 * Guarantees absence of 360-degree phase jumps, smooth interpolation,
 * mouse sensitivity GCD quantization, and directional continuity.
 */
public final class ContinuousRotationMath {

    private ContinuousRotationMath() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Unwraps targetYaw relative to referenceYaw, guaranteeing that:
     * |targetYawUnwrapped - referenceYaw| <= 180.0 degrees.
     *
     * @param referenceYaw The anchor/current yaw angle.
     * @param targetYaw    The desired target yaw angle (wrapped or in any revolution).
     * @return An equivalent angle to targetYaw in the same revolution as referenceYaw.
     */
    public static float unwrapYaw(float referenceYaw, float targetYaw) {
        return referenceYaw + MathHelper.wrapDegrees(targetYaw - referenceYaw);
    }

    /**
     * Calculates the shortest angular distance from one yaw to another.
     * Result is strictly in [-180.0, 180.0].
     *
     * @param from Starting yaw.
     * @param to   Target yaw.
     * @return Shortest angular difference (to - from).
     */
    public static float shortestDeltaYaw(float from, float to) {
        return MathHelper.wrapDegrees(to - from);
    }

    /**
     * Steps a yaw angle toward an unwrapped target angle without exceeding maxStep.
     *
     * @param currentYaw Target's current yaw angle.
     * @param targetYaw  Target yaw (already unwrapped or shortest delta applied).
     * @param maxStep    Maximum degrees allowed to rotate this step.
     * @return The next continuous yaw.
     */
    public static float stepYawToward(float currentYaw, float targetYaw, float maxStep) {
        float delta = shortestDeltaYaw(currentYaw, targetYaw);
        if (Math.abs(delta) <= maxStep) {
            return currentYaw + delta;
        }
        return currentYaw + Math.signum(delta) * maxStep;
    }

    /**
     * Steps a pitch angle toward targetPitch without exceeding maxStep.
     *
     * @param currentPitch Current pitch [-90, 90].
     * @param targetPitch  Target pitch [-90, 90].
     * @param maxStep      Maximum degrees allowed to change pitch.
     * @return Clamped next pitch.
     */
    public static float stepPitchToward(float currentPitch, float targetPitch, float maxStep) {
        float delta = targetPitch - currentPitch;
        if (Math.abs(delta) <= maxStep) {
            return MathHelper.clamp(currentPitch + delta, -90.0f, 90.0f);
        }
        return MathHelper.clamp(currentPitch + Math.signum(delta) * maxStep, -90.0f, 90.0f);
    }

    /**
     * Interpolates continuous rotation between previous and current over tickDelta,
     * following the shortest angular path on yaw.
     *
     * @param prev      Previous tick rotation.
     * @param current   Current tick rotation.
     * @param tickDelta Render tick delta [0.0, 1.0].
     * @return Interpolated smooth rotation.
     */
    @NotNull
    public static Rotation interpolate(@NotNull Rotation prev, @NotNull Rotation current, float tickDelta) {
        float deltaYaw = shortestDeltaYaw(prev.getYaw(), current.getYaw());
        float yaw = prev.getYaw() + deltaYaw * tickDelta;
        float pitch = prev.getPitch() + (current.getPitch() - prev.getPitch()) * tickDelta;
        return new Rotation(yaw, MathHelper.clamp(pitch, -90.0f, 90.0f));
    }

    /**
     * Snaps the delta between current and target to the client mouse sensitivity step (GCD),
     * ensuring perfectly natural, vanilla-like rotational increments without phase jumps.
     *
     * @param current Current continuous rotation.
     * @param target  Target rotation.
     * @return Snapped continuous rotation.
     */
    @NotNull
    public static Rotation snapToMouseStep(@NotNull Rotation current, @NotNull Rotation target) {
        float gcd = AimRotationMath.getMouseRotationStep();
        if (gcd <= 0.0001f) {
            return target;
        }
        float deltaYaw = shortestDeltaYaw(current.getYaw(), target.getYaw());
        float deltaPitch = target.getPitch() - current.getPitch();

        float snappedDeltaYaw = (float) Math.round(deltaYaw / gcd) * gcd;
        float snappedDeltaPitch = (float) Math.round(deltaPitch / gcd) * gcd;

        return new Rotation(
                current.getYaw() + snappedDeltaYaw,
                MathHelper.clamp(current.getPitch() + snappedDeltaPitch, -90.0f, 90.0f)
        );
    }

    /**
     * Calculates the total angular distance (Manhattan or Euclidean on spherical coordinates)
     * between two rotations, wrapping yaw difference to [-180, 180].
     */
    public static float angleDistance(@NotNull Rotation r1, @NotNull Rotation r2) {
        float dy = Math.abs(shortestDeltaYaw(r1.getYaw(), r2.getYaw()));
        float dp = Math.abs(r2.getPitch() - r1.getPitch());
        return dy + dp;
    }
}
