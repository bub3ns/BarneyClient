package moscow.rockstar.modules.player.movement;

import net.minecraft.util.math.Vec3d;

public record MotionSnapshot(Vec3d position, Vec3d previousPosition) {
    public MotionSnapshot {
        previousPosition = previousPosition.lengthSquared() < 1.0E-6
                ? new Vec3d(0.0, 0.0, 1.0)
                : previousPosition.normalize();
    }

    public Vec3d interpolatePosition(Vec3d currentPosition) {
        Vec3d delta = currentPosition.subtract(this.position);
        return this.position.add(this.previousPosition.multiply(delta.dotProduct(this.previousPosition)));
    }

    public double getDistanceTo(Vec3d position) {
        return this.interpolatePosition(position).distanceTo(position);
    }

    public Vec3d getPosition() {
        return this.position;
    }

    public Vec3d getPositionPrimary() {
        return this.previousPosition;
    }
}
