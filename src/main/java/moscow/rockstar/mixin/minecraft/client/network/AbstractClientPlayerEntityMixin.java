/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.AbstractClientPlayerEntity
 *  net.minecraft.SkinTextures
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.network;

import moscow.rockstar.render.texture.TextureOverrideRegistry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AbstractClientPlayerEntity.class})
public class AbstractClientPlayerEntityMixin {
    @Inject(method={"getSkinTextures"}, at={@At(value="RETURN")}, cancellable=true)
    private void rockstar$swapSkin(CallbackInfoReturnable<SkinTextures> callbackInfoReturnable) {
        if (TextureOverrideRegistry.hasNoOverrides()) {
            return;
        }
        AbstractClientPlayerEntity TrackedPosition = (AbstractClientPlayerEntity)(Object)this;
        SkinTextures class_86852 = TextureOverrideRegistry.applyOverride(TrackedPosition.getGameProfile().getName(), (SkinTextures)callbackInfoReturnable.getReturnValue());
        if (class_86852 != callbackInfoReturnable.getReturnValue()) {
            callbackInfoReturnable.setReturnValue(class_86852);
        }
    }
}
