/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ArmedEntityRenderState
 *  net.minecraft.Entity
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.HeldItemFeatureRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.render.entity.feature;

import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.player.movement.camera.FreeCamera;
import moscow.rockstar.modules.visuals.camera.Beautifully;
import moscow.rockstar.api.access.EntityAccess;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={HeldItemFeatureRenderer.class})
public abstract class HeldItemFeatureRendererMixin {
    @Inject(method={"render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/ArmedEntityRenderState;FF)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$hideDuringFade(MatrixStack class_45872, VertexConsumerProvider class_45972, int n, ArmedEntityRenderState class_104262, float f, float f2, CallbackInfo callbackInfo) {
        Entity class_12972 = ((EntityAccess)class_104262).rockstar$getEntity();
        if (class_12972 != MinecraftClient.getInstance().player) {
            return;
        }
        FreeCamera freeCamera = RockstarClient.create().getModuleRegistry().getModule(FreeCamera.class);
        if (freeCamera.isCameraTransitionReady()) {
            callbackInfo.cancel();
            return;
        }
        Beautifully beautifully = RockstarClient.create().getModuleRegistry().getModule(Beautifully.class);
        if (beautifully.isEnabled() && beautifully.getSmoothF5Option().isSelected() && !beautifully.getThirdPersonAnimation().isAtTarget()) {
            callbackInfo.cancel();
        }
    }
}
