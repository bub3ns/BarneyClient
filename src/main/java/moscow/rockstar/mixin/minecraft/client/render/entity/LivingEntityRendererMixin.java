/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.EntityRenderState
 *  net.minecraft.LivingEntityRenderState
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.ArmorStandEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.RenderLayer
 *  net.minecraft.Identifier
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumer
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.BipedEntityModel
 *  net.minecraft.EntityModel
 *  net.minecraft.ClientPlayerEntity
 *  net.minecraft.LivingEntityRenderer
 *  org.joml.Vector3f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.render.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.esp.entities.Glow;
import moscow.rockstar.mixin.accessors.BipedEntityModelAccessor;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.player.movement.camera.FreeCamera;
import moscow.rockstar.modules.visuals.camera.Beautifully;
import moscow.rockstar.modules.visuals.esp.entities.AntiInvisible;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.esp.FriendMarkerRenderer;
import moscow.rockstar.render.esp.OverlayRegistry;
import moscow.rockstar.render.layers.EntityGlowRenderLayer;
import moscow.rockstar.api.access.EntityAccess;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.utility.render.ColorRGBA;

@Mixin(value={LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    private Entity currentEntity;
    @Shadow
    protected EntityModel<?> model;

    @Shadow
    public abstract Identifier getTexture(LivingEntityRenderState var1);

    @ModifyExpressionValue(method={"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/LivingEntityRenderer;clampBodyYaw(Lnet/minecraft/entity/LivingEntity;FF)F")})
    public float changeYaw(float f, LivingEntity class_13092) {
        if (!(class_13092 instanceof ClientPlayerEntity)) {
            return f;
        }
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        float f2 = rotationManager.isIdle() ? f : rotationManager.getAppliedRotation().getYaw();
        rotationManager.getPacketRotation().setYaw(f2);
        return f2;
    }

    @ModifyExpressionValue(method={"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F")})
    public float changeHeadYaw(float f, LivingEntity class_13092) {
        if (!(class_13092 instanceof ClientPlayerEntity)) {
            return f;
        }
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        float f2 = rotationManager.isIdle() ? f : rotationManager.getAppliedRotation().getYaw();
        rotationManager.getPacketRotation().setYaw(f2);
        return f2;
    }

    @ModifyExpressionValue(method={"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getLerpedPitch(F)F")})
    public float changePitch(float f, LivingEntity class_13092) {
        if (!(class_13092 instanceof ClientPlayerEntity)) {
            return f;
        }
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        float f2 = rotationManager.isIdle() ? f : rotationManager.getAppliedRotation().getPitch();
        rotationManager.getPacketRotation().setPitch(f2);
        return f2;
    }

    @WrapOperation(method={"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V")})
    private void changeModelColor(EntityModel<?> Impl, MatrixStack class_45872, VertexConsumer class_45882, int n, int n2, int n3, Operation<Void> operation, @Local(argsOnly=true) S s) {
        FriendMarkerRenderer friendMarkerRenderer;
        boolean bl;
        FreeCamera freeCamera;
        Beautifully beautifully;
        AntiInvisible antiInvisible = RockstarClient.create().getModuleRegistry().getModule(AntiInvisible.class);
        Entity class_12972 = ((EntityAccess)s).rockstar$getEntity();
        if (antiInvisible.isEnabled() && antiInvisible.isEntityInvisible((EntityRenderState)s)) {
            int n4 = n3 = class_12972 instanceof ArmorStandEntity ? ColorPalette.WHITE.withAlpha(0.0f).getRGB() : ColorPalette.WHITE.withAlpha(antiInvisible.getOpacitySetting().getValue() / 100.0f * 255.0f).getRGB();
        }
        if ((beautifully = RockstarClient.create().getModuleRegistry().getModule(Beautifully.class)).isEnabled() && beautifully.getSmoothF5Option().isSelected() && !beautifully.getThirdPersonAnimation().isAtTarget() && class_12972 == MinecraftClient.getInstance().player) {
            n3 = ColorRGBA.applyOpacity(n3, beautifully.getThirdPersonAnimation().getValue()).getRGB();
        }
        if ((freeCamera = RockstarClient.create().getModuleRegistry().getModule(FreeCamera.class)).isCameraTransitionReady() && class_12972 == MinecraftClient.getInstance().player) {
            n3 = ColorRGBA.applyOpacity(n3, freeCamera.getCameraProgress()).getRGB();
        }
        boolean bl2 = bl = (friendMarkerRenderer = OverlayRegistry.getInstance().findOverlayByType(FriendMarkerRenderer.class)) != null && friendMarkerRenderer.shouldRenderHeadMarker();
        if (class_12972 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            if (Impl instanceof BipedEntityModel) {
                BipedEntityModel Empty = (BipedEntityModel)Impl;
                if (bl) {
                    boolean bl3 = RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString());
                    boolean bl4 = FriendMarkerRenderer.isFriendMarkersRendering();
                    if (bl3 || bl4) {
                        BipedEntityModelAccessor bipedEntityModelAccessor = (BipedEntityModelAccessor)Empty;
                        float f = 1.05f;
                        bipedEntityModelAccessor.rockstar$getHead().scale(new Vector3f(f, f, f));
                        operation.call(new Object[]{Impl, class_45872, class_45882, n, n2, n3});
                    }
                }
            }
        }
        operation.call(new Object[]{Impl, class_45872, class_45882, n, n2, n3});
    }

    @ModifyReturnValue(method={"getRenderLayer"}, at={@At(value="RETURN")})
    private RenderLayer changeRenderLayer(RenderLayer class_19212, S s, boolean bl, boolean bl2, boolean bl3) {
        Entity class_12972;
        FreeCamera freeCamera;
        AntiInvisible antiInvisible = RockstarClient.create().getModuleRegistry().getModule(AntiInvisible.class);
        if (antiInvisible.isEnabled() && !bl && !bl2 && !bl3) {
            ((LivingEntityRenderState)s).invisible = false;
            return RenderLayer.getItemEntityTranslucentCull((Identifier)this.getTexture((LivingEntityRenderState)s));
        }
        Beautifully beautifully = RockstarClient.create().getModuleRegistry().getModule(Beautifully.class);
        Entity renderedEntity = ((EntityAccess)s).rockstar$getEntity();
        if (beautifully.isEnabled() && beautifully.getSmoothF5Option().isSelected() && !beautifully.getThirdPersonAnimation().isAtTarget() && renderedEntity == MinecraftClient.getInstance().player) {
            return RenderLayer.getItemEntityTranslucentCull((Identifier)this.getTexture((LivingEntityRenderState)s));
        }
        freeCamera = RockstarClient.create().getModuleRegistry().getModule(FreeCamera.class);
        if (freeCamera.isCameraTransitionReady() && (class_12972 = renderedEntity) == MinecraftClient.getInstance().player) {
            return RenderLayer.getItemEntityTranslucentCull((Identifier)this.getTexture((LivingEntityRenderState)s));
        }
        return class_19212;
    }

    @Inject(method={"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="HEAD")})
    private void captureEntity(LivingEntityRenderState class_100422, MatrixStack class_45872, VertexConsumerProvider class_45972, int n, CallbackInfo callbackInfo) {
        RockstarClient.CURRENT_ENTITY = this.currentEntity = ((EntityAccess)class_100422).rockstar$getEntity();
    }

    @Inject(method={"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="RETURN")})
    private void releaseEntity(LivingEntityRenderState class_100422, MatrixStack class_45872, VertexConsumerProvider class_45972, int n, CallbackInfo callbackInfo) {
        RockstarClient.CURRENT_ENTITY = null;
    }

    @WrapWithCondition(method={"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V")})
    private boolean render$render(M m, MatrixStack class_45872, VertexConsumer class_45882, int n, int n2, int n3, S s, MatrixStack class_45873, VertexConsumerProvider class_45972, int n4) {
        Object object;
        Object object2;
        Entity class_12972 = ((EntityAccess)s).rockstar$getEntity();
        if (m instanceof BipedEntityModel) {
            boolean bl;
            object2 = (BipedEntityModel)m;
            object = OverlayRegistry.getInstance().findOverlayByType(FriendMarkerRenderer.class);
            boolean bl2 = bl = object != null && ((FriendMarkerRenderer)object).shouldRenderHeadMarker();
            if (bl) {
                boolean bl3;
                boolean bl4 = RockstarClient.create().getFriendListManager().containsFriend(class_12972.getName().getString());
                boolean bl5 = bl3 = object != null && FriendMarkerRenderer.isFriendMarkersRendering();
                if (bl4 || bl3) {
                    BipedEntityModelAccessor bipedEntityModelAccessor = (BipedEntityModelAccessor)object2;
                    float f = 1.05f;
                    bipedEntityModelAccessor.rockstar$getHead().scale(new Vector3f(f, f, f));
                }
            }
        }
        if (Glow.entityGlowRendering && (object2 = OverlayRegistry.getInstance().findOverlayByType(Glow.class)) != null && ((Glow)object2).isGlowTarget(class_12972) && (object = ((Glow)object2).getGlowColor(class_12972)) != null) {
            m.render(class_45872, class_45882, n, n2, ((ColorRGBA)object).getRGB());
            return false;
        }
        return true;
    }

    @ModifyReturnValue(method={"getRenderLayer"}, at={@At(value="RETURN")})
    private RenderLayer getRenderPlayer(RenderLayer class_19212, S s, boolean bl, boolean bl2, boolean bl3) {
        Glow glowRenderer = OverlayRegistry.getInstance().findOverlayByType(Glow.class);
        Entity class_12972 = ((EntityAccess)s).rockstar$getEntity();
        if (glowRenderer == null || !glowRenderer.isGlowTarget(class_12972) || !Glow.entityGlowRendering) {
            return class_19212;
        }
        ColorRGBA colorRGBA = glowRenderer.getGlowColor(class_12972);
        if (colorRGBA == null) {
            return class_19212;
        }
        return EntityGlowRenderLayer.getGlowLayer(this.getTexture((LivingEntityRenderState)s));
    }

    @ModifyArg(method={"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"), index=2)
    private int forceModelFullbright(int n) {
        Glow glowRenderer = OverlayRegistry.getInstance().findOverlayByType(Glow.class);
        return glowRenderer != null && glowRenderer.isGlowTarget(this.currentEntity) && Glow.entityGlowRendering ? 0xF000F0 : n;
    }
}
