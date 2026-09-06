package moscow.rockstar.modules.visuals.prediction;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public record TrajectoryResult(Entity entity, List<Vec3d> vectors, int ticks, Entity collidedEntity) {
    public Entity getEntity() {
        return entity;
    }

    public List<Vec3d> getVectors() {
        return vectors;
    }

    public int getTicks() {
        return ticks;
    }

    public Entity getCollidedEntity() {
        return collidedEntity;
    }
}
