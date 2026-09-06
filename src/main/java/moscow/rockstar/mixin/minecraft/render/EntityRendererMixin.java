/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.EntityRenderState
 *  net.minecraft.Entity
 *  net.minecraft.EntityRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.render;

import moscow.rockstar.api.access.EntityAccess;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.client.render.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EntityRenderer.class})
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method={"updateRenderState"}, at={@At(value="HEAD")})
    private void updateRenderingEntity(T t, S s, float f, CallbackInfo callbackInfo) {
        ((EntityAccess)s).rockstar$setEntity(t);
    }
}
