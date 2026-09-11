package moscow.rockstar.api.rotation;

import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Main public API entry point for BarneyClient's rotation engine.
 * Provides easy, thread-safe, continuous rotation control for all client modules,
 * commands, and scripting engines.
 */
public final class RotationApi {

    private RotationApi() {
        throw new UnsupportedOperationException("RotationApi cannot be instantiated");
    }

    /**
     * Obtains the core client {@link RotationManager} instance.
     */
    @NotNull
    public static RotationManager getManager() {
        return RockstarClient.create().getRotationManager();
    }

    /**
     * Starts building a new fluent rotation request.
     */
    @NotNull
    public static RotationRequestBuilder builder() {
        return new RotationRequestBuilder();
    }

    /**
     * Performs a direct rotation toward the specified entity.
     */
    public static void rotateTo(@NotNull Entity entity, float speed) {
        builder().target(entity).direct().speed(speed).execute();
    }

    /**
     * Performs a silent (packet/movement only) rotation toward the specified entity.
     */
    public static void silentRotateTo(@NotNull Entity entity, float speed) {
        builder().target(entity).silent().speed(speed).execute();
    }

    /**
     * Performs a silent rotation toward the specified entity with custom priority.
     */
    public static void silentRotateTo(@NotNull Entity entity, float speed, @NotNull RotationPriority priority) {
        builder().target(entity).silent().speed(speed).priority(priority).execute();
    }

    /**
     * Performs a silent rotation toward a 3D coordinate point.
     */
    public static void silentRotateTo(@NotNull Vec3d point, float speed) {
        builder().target(point).silent().speed(speed).execute();
    }

    /**
     * Performs a silent rotation toward a block position.
     */
    public static void silentRotateTo(@NotNull BlockPos blockPos, float speed) {
        builder().target(blockPos).silent().speed(speed).execute();
    }

    /**
     * Returns true if the client rotation system is currently idle (no active aim or return).
     */
    public static boolean isIdle() {
        return getManager().isIdle();
    }

    /**
     * Returns true if the client is actively aiming or returning to player.
     */
    public static boolean isRotating() {
        return !getManager().isIdle();
    }

    /**
     * Gets the authoritative, continuous unwrapped rotation.
     */
    @NotNull
    public static Rotation getCurrentRotation() {
        return getManager().getCurrentRotation();
    }

    /**
     * Gets the vanilla player's current view rotation.
     */
    @NotNull
    public static Rotation getPlayerRotation() {
        return getManager().getPlayerRotation();
    }

    /**
     * Gets the effective rotation (active rotation if aiming, else player view rotation).
     */
    @NotNull
    public static Rotation getEffectiveRotation() {
        return getManager().getEffectiveRotation();
    }

    /**
     * Gets the interpolated render rotation used for client-side model rendering.
     */
    @NotNull
    public static Rotation getAppliedRotation() {
        return getManager().getAppliedRotation();
    }

    /**
     * Calculates the rotation needed to look at an entity from the player's eye position.
     */
    @NotNull
    public static Rotation getRotationTo(@NotNull Entity entity) {
        return AimRotationMath.getRotationToPoint(AimRotationMath.getClosestPointOnEntityBounds(entity));
    }

    /**
     * Calculates the rotation needed to look at a 3D point.
     */
    @NotNull
    public static Rotation getRotationTo(@NotNull Vec3d point) {
        return AimRotationMath.getRotationToPoint(point);
    }

    /**
     * Initiates a smooth, continuous return back to the player's real look direction.
     */
    public static void returnToPlayer() {
        getManager().finishRotation();
    }

    /**
     * Immediately stops rotation, optionally performing a smooth return to the player.
     */
    public static void stopRotation(boolean smooth) {
        if (smooth) {
            getManager().finishRotation();
        } else {
            getManager().cancelRotation(false);
        }
    }

    /**
     * Mathematical helper: unwraps targetYaw relative to referenceYaw.
     */
    public static float unwrapYaw(float referenceYaw, float targetYaw) {
        return ContinuousRotationMath.unwrapYaw(referenceYaw, targetYaw);
    }

    /**
     * Mathematical helper: shortest difference between two yaw angles in [-180, 180].
     */
    public static float shortestDeltaYaw(float from, float to) {
        return ContinuousRotationMath.shortestDeltaYaw(from, to);
    }

    /**
     * Mathematical helper: angular distance between two rotations.
     */
    public static float angleDistance(@NotNull Rotation r1, @NotNull Rotation r2) {
        return ContinuousRotationMath.angleDistance(r1, r2);
    }
}
