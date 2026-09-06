package moscow.rockstar.modules.player.placement;

import net.minecraft.util.math.Vec3d;
import pyrock.events.player.InputEvent;

public record PlacementContext(boolean forward, boolean backward, boolean left, boolean right) {
    public static final PlacementContext NO_INPUT = new PlacementContext(false, false, false, false);

    public static PlacementContext fromInput(InputEvent inputEvent) {
        return fromAxes(inputEvent.getForward(), inputEvent.getStrafe());
    }

    public static PlacementContext fromAxes(float forward, float strafe) {
        return new PlacementContext(forward > 0.0f, forward < 0.0f, strafe > 0.0f, strafe < 0.0f);
    }

    public boolean hasMovementInput() {
        return this.forward || this.backward || this.left || this.right;
    }

    public float getForwardAxis() {
        if (this.forward == this.backward) {
            return 0.0f;
        }
        return this.forward ? 1.0f : -1.0f;
    }

    public float getStrafeAxis() {
        if (this.left == this.right) {
            return 0.0f;
        }
        return this.left ? 1.0f : -1.0f;
    }

    public Vec3d getMovementDirection(float yaw) {
        double forwardAxis = this.getForwardAxis();
        double strafeAxis = this.getStrafeAxis();
        double length = Math.sqrt(forwardAxis * forwardAxis + strafeAxis * strafeAxis);
        if (length < 1.0E-6) {
            return Vec3d.ZERO;
        }
        double radians = Math.toRadians(yaw);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        return new Vec3d((strafeAxis * cos - forwardAxis * sin) / length, 0.0,
                (forwardAxis * cos + strafeAxis * sin) / length);
    }

    public boolean isMovingForward() {
        return this.forward;
    }

    public boolean isMovingBackward() {
        return this.backward;
    }

    public boolean isStrafingLeft() {
        return this.left;
    }

    public boolean isStrafingRight() {
        return this.right;
    }
}
