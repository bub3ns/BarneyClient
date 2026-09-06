package moscow.rockstar.modules.visuals.object;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

public record CollisionResult(Entity entity, Vec3d pos, int ticks, Entity collidedEntity,
                              BlockHitResult hitResult, boolean fromHand) {
    public Entity getEntity() {
        return entity;
    }

    public Vec3d getPos() {
        return pos;
    }

    public int getTicks() {
        return ticks;
    }

    public Entity getCollidedEntity() {
        return collidedEntity;
    }

    public BlockHitResult getHitResult() {
        return hitResult;
    }

    public boolean isFromHand() {
        return fromHand;
    }
}
