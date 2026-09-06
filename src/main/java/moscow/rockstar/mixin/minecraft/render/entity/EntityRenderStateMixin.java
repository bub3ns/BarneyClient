/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.EntityRenderState
 *  net.minecraft.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package moscow.rockstar.mixin.minecraft.render.entity;

import moscow.rockstar.api.access.EntityAccess;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={EntityRenderState.class})
public abstract class EntityRenderStateMixin
implements EntityAccess {
    @Unique
    private Entity rockstar$entity;

    @Unique
    public void rockstar$setEntity(Entity class_12972) {
        this.rockstar$entity = class_12972;
    }

    @Unique
    public Entity rockstar$getEntity() {
        return this.rockstar$entity;
    }
}
