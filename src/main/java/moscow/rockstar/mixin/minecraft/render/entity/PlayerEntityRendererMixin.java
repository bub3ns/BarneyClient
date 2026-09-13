/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PlayerEntityRenderState
 *  net.minecraft.PlayerEntityRenderer
 *  net.minecraft.AbstractClientPlayerEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.render.entity;

import moscow.rockstar.render.texture.TextureOverrideRegistry;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import moscow.rockstar.render.entity.feature.AccessoriesFeatureRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PlayerEntityRenderer.class})
public abstract class PlayerEntityRendererMixin
extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityRenderState, PlayerEntityModel> {

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void rockstar$addAccessoriesFeature(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        this.addFeature(new AccessoriesFeatureRenderer(this));
    }
    @Inject(method={"updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V"}, at={@At(value="TAIL")})
    private void rockstar$forceCape(AbstractClientPlayerEntity TrackedPosition, PlayerEntityRenderState class_100552, float f, CallbackInfo callbackInfo) {
        if (TextureOverrideRegistry.hasNoOverrides()) {
            return;
        }
        if (TextureOverrideRegistry.hasSecondaryTexture(TrackedPosition.getGameProfile().getName())) {
            class_100552.capeVisible = true;
        }
    }
}

