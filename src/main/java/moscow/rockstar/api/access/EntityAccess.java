package moscow.rockstar.api.access;

import net.minecraft.entity.Entity;

/** Access to the entity associated with a rendered entity state. */
public interface EntityAccess {
    void rockstar$setEntity(Entity entity);

    Entity rockstar$getEntity();
}
