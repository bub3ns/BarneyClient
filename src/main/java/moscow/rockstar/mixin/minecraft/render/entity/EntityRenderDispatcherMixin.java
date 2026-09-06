/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.EntityRenderState
 *  net.minecraft.Entity
 *  net.minecraft.Position
 *  net.minecraft.WorldView
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.EntityRenderDispatcher
 *  org.joml.Quaternionf
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.render.entity;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.esp.entities.Glow;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.visuals.camera.Beautifully;
import moscow.rockstar.api.access.EntityAccess;
import moscow.rockstar.render.world.DynamicLightGrid;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Position;
import net.minecraft.world.WorldView;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={EntityRenderDispatcher.class})
public abstract class EntityRenderDispatcherMixin {
    @Inject(method={"renderShadow"}, at={@At(value="HEAD")}, cancellable=true)
    private static void chams$noShadow(MatrixStack class_45872, VertexConsumerProvider class_45972, EntityRenderState class_100172, float f, float f2, WorldView class_45382, float f3, CallbackInfo callbackInfo) {
        if (Glow.entityGlowRendering || Glow.itemGlowRendering) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"getLight"}, at={@At(value="RETURN")}, cancellable=true)
    private void rockstar$applyDynamicLight(Entity class_12972, float f, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(DynamicLightGrid.apply(
            BlockPos.ofFloored(class_12972.getLerpedPos(f)),
            callbackInfoReturnable.getReturnValueI()));
    }

    @WrapWithCondition(method={"render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/EntityRenderDispatcher;renderFire(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/entity/state/EntityRenderState;Lorg/joml/Quaternionf;)V")})
    private boolean rockstar$skipFireDuringF5(EntityRenderDispatcher TrialSpawnerDetectionParticle, MatrixStack class_45872, VertexConsumerProvider class_45972, EntityRenderState class_100172, Quaternionf quaternionf) {
        Entity class_12972;
        Beautifully beautifully = RockstarClient.create().getModuleRegistry().getModule(Beautifully.class);
        return !beautifully.isEnabled() || !beautifully.getSmoothF5Option().isSelected() || beautifully.getThirdPersonAnimation().isAtTarget() || (class_12972 = ((EntityAccess)class_100172).rockstar$getEntity()) != MinecraftClient.getInstance().player;
    }
}
