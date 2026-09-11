package moscow.rockstar.api.rotation;

import moscow.rockstar.api.access.RotationStepAccess;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.combat.rotation.RotationRequest;
import moscow.rockstar.combat.rotation.RotationReturnMode;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fluent builder for creating, configuring, and submitting rotation requests.
 */
public class RotationRequestBuilder {

    private Rotation targetRotation;
    private RotationCorrectionMode correctionMode = RotationCorrectionMode.UNSPECIFIED;
    private RotationPriority priority = RotationPriority.STANDARD_PRIORITY;
    private RotationReturnMode returnMode = RotationReturnMode.SMOOTH;
    private float yawSpeed = 180.0f;
    private float pitchSpeed = 180.0f;
    private float returnSpeed = 180.0f;
    private boolean snapToMouse = true;
    @Nullable
    private RotationStepAccess stepCallback;

    public RotationRequestBuilder() {
    }

    public static RotationRequestBuilder create() {
        return new RotationRequestBuilder();
    }

    public RotationRequestBuilder target(@NotNull Rotation rotation) {
        this.targetRotation = new Rotation(rotation.getYaw(), rotation.getPitch());
        return this;
    }

    public RotationRequestBuilder target(float yaw, float pitch) {
        this.targetRotation = new Rotation(yaw, pitch);
        return this;
    }

    public RotationRequestBuilder target(@NotNull Vec3d point) {
        this.targetRotation = AimRotationMath.getRotationToPoint(point);
        return this;
    }

    public RotationRequestBuilder target(@NotNull BlockPos pos) {
        return target(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    public RotationRequestBuilder target(@NotNull Entity entity) {
        Vec3d aimPoint = AimRotationMath.getClosestPointOnEntityBounds(entity);
        this.targetRotation = AimRotationMath.getRotationToPoint(aimPoint);
        return this;
    }

    public RotationRequestBuilder priority(@NotNull RotationPriority priority) {
        this.priority = priority;
        return this;
    }

    public RotationRequestBuilder correction(@NotNull RotationCorrectionMode correctionMode) {
        this.correctionMode = correctionMode;
        return this;
    }

    public RotationRequestBuilder silent() {
        this.correctionMode = RotationCorrectionMode.UNSPECIFIED;
        return this;
    }

    public RotationRequestBuilder direct() {
        this.correctionMode = RotationCorrectionMode.DIRECT;
        return this;
    }

    public RotationRequestBuilder changeLook() {
        this.correctionMode = RotationCorrectionMode.CHANGE_LOOK;
        return this;
    }

    public RotationRequestBuilder targeted() {
        this.correctionMode = RotationCorrectionMode.TARGETED;
        return this;
    }

    public RotationRequestBuilder returnMode(@NotNull RotationReturnMode returnMode) {
        this.returnMode = returnMode;
        return this;
    }

    public RotationRequestBuilder smoothReturn() {
        this.returnMode = RotationReturnMode.SMOOTH;
        return this;
    }

    public RotationRequestBuilder noReturn() {
        this.returnMode = RotationReturnMode.NONE;
        return this;
    }

    public RotationRequestBuilder cameraReturn() {
        this.returnMode = RotationReturnMode.CAMERA;
        return this;
    }

    public RotationRequestBuilder speed(float speed) {
        this.yawSpeed = speed;
        this.pitchSpeed = speed;
        this.returnSpeed = speed;
        return this;
    }

    public RotationRequestBuilder speed(float yawSpeed, float pitchSpeed) {
        this.yawSpeed = yawSpeed;
        this.pitchSpeed = pitchSpeed;
        return this;
    }

    public RotationRequestBuilder returnSpeed(float returnSpeed) {
        this.returnSpeed = returnSpeed;
        return this;
    }

    public RotationRequestBuilder snapToMouse(boolean snapToMouse) {
        this.snapToMouse = snapToMouse;
        return this;
    }

    public RotationRequestBuilder stepCallback(@Nullable RotationStepAccess stepAccess) {
        this.stepCallback = stepAccess;
        return this;
    }

    /**
     * Executes this rotation request via {@link RotationManager}.
     *
     * @return The created and dispatched {@link RotationRequest}, or null if target was not set.
     */
    @Nullable
    public RotationRequest execute() {
        if (this.targetRotation == null) {
            return null;
        }
        RotationManager manager = RockstarClient.create().getRotationManager();
        manager.requestRotationInternal(
                this.targetRotation,
                this.correctionMode,
                this.yawSpeed,
                this.pitchSpeed,
                this.returnSpeed,
                this.priority,
                this.snapToMouse
        );
        RotationRequest request = manager.getRotationResolver();
        if (request != null) {
            request.setReturnMode(this.returnMode);
            request.setRotationStep(this.stepCallback);
        }
        return request;
    }
}
