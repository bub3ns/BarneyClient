/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.EquipmentRenderer
 *  net.minecraft.Entity
 *  net.minecraft.RenderLayer
 *  net.minecraft.Identifier
 *  net.minecraft.Model
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package moscow.rockstar.mixin.minecraft.render.entity.equipment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.esp.entities.Glow;
import moscow.rockstar.modules.Module;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.player.movement.camera.FreeCamera;
import moscow.rockstar.modules.visuals.camera.Beautifully;
import moscow.rockstar.render.esp.OverlayRegistry;
import moscow.rockstar.render.layers.EntityGlowRenderLayer;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.model.Model;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import pyrock.utility.render.ColorRGBA;

@Mixin(value={EquipmentRenderer.class})
public abstract class EquipmentRendererMixin {
    @WrapOperation(method={"render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/RenderLayer;getArmorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;")})
    private RenderLayer rockstar$glowArmorLayer(Identifier class_29602, Operation<RenderLayer> operation) {
        Beautifully beautifully = RockstarClient.create().getModuleRegistry().getModule(Beautifully.class);
        if (beautifully.isEnabled() && beautifully.getSmoothF5Option().isSelected() && !beautifully.getThirdPersonAnimation().isAtTarget() && RockstarClient.CURRENT_ENTITY == MinecraftClient.getInstance().player) {
            return RenderLayer.getItemEntityTranslucentCull((Identifier)class_29602);
        }
        FreeCamera freeCamera = RockstarClient.create().getModuleRegistry().getModule(FreeCamera.class);
        if (freeCamera.isCameraTransitionReady() && RockstarClient.CURRENT_ENTITY == MinecraftClient.getInstance().player) {
            return RenderLayer.getItemEntityTranslucentCull((Identifier)class_29602);
        }
        if (!Glow.entityGlowRendering) {
            return (RenderLayer)operation.call(new Object[]{class_29602});
        }
        Entity class_12972 = Glow.currentEntity;
        if (class_12972 == null) {
            return (RenderLayer)operation.call(new Object[]{class_29602});
        }
        Glow glowRenderer = OverlayRegistry.getInstance().findOverlayByType(Glow.class);
        if (glowRenderer == null || !glowRenderer.isGlowTarget(class_12972)) {
            return (RenderLayer)operation.call(new Object[]{class_29602});
        }
        if (glowRenderer.getGlowColor(class_12972) == null) {
            return (RenderLayer)operation.call(new Object[]{class_29602});
        }
        return EntityGlowRenderLayer.getGlowLayer(class_29602);
    }

    @WrapOperation(method={"render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/model/Model;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V")})
    private void rockstar$glowArmorColor(Model class_38792, MatrixStack class_45872, VertexConsumer class_45882, int n, int n2, int n3, Operation<Void> operation) {
        ColorRGBA colorRGBA;
        ClientAccess clientAccess;
        Object object;
        if (Glow.entityGlowRendering && (object = Glow.currentEntity) != null && (clientAccess = OverlayRegistry.getInstance().findOverlayByType(Glow.class)) != null && ((Glow)clientAccess).isGlowTarget((Entity)object) && (colorRGBA = ((Glow)clientAccess).getGlowColor((Entity)object)) != null) {
            n3 = colorRGBA.getRGB();
        }
        if (((Module)(object = RockstarClient.create().getModuleRegistry().getModule(Beautifully.class))).isEnabled() && ((Beautifully)object).getSmoothF5Option().isSelected() && !((Beautifully)object).getThirdPersonAnimation().isAtTarget() && RockstarClient.CURRENT_ENTITY == MinecraftClient.getInstance().player) {
            n3 = ColorRGBA.applyOpacity(n3, ((Beautifully)object).getThirdPersonAnimation().getValue()).getRGB();
        }
        if (((FreeCamera)(clientAccess = RockstarClient.create().getModuleRegistry().getModule(FreeCamera.class))).isCameraTransitionReady() && RockstarClient.CURRENT_ENTITY == MinecraftClient.getInstance().player) {
            n3 = ColorRGBA.applyOpacity(n3, ((FreeCamera)clientAccess).getCameraProgress()).getRGB();
        }
        operation.call(new Object[]{class_38792, class_45872, class_45882, n, n2, n3});
    }
}
