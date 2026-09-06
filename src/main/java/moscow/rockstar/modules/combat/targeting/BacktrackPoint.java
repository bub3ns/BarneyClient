package moscow.rockstar.modules.combat.targeting;

import net.minecraft.util.math.Vec3d;

/** A sampled position retained for backtrack targeting. */
public record BacktrackPoint(Vec3d position, long timestamp) {
    public Vec3d getPosition() {
        return position;
    }

    public long getTime() {
        return timestamp;
    }
}
