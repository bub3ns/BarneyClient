/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.EntityRenderState
 *  net.minecraft.LivingEntityRenderState
 *  net.minecraft.Entity
 *  net.minecraft.ArmorStandEntity
 *  net.minecraft.RenderLayer
 *  net.minecraft.Identifier
 *  net.minecraft.FeatureRenderer
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumer
 *  net.minecraft.EntityModel
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package moscow.rockstar.mixin.minecraft.render.entity.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.esp.entities.Glow;
import moscow.rockstar.modules.visuals.esp.entities.AntiInvisible;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.esp.OverlayRegistry;
import moscow.rockstar.render.layers.EntityGlowRenderLayer;
import moscow.rockstar.api.access.EntityAccess;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import pyrock.utility.render.ColorRGBA;

@Mixin(value={FeatureRenderer.class})
public abstract class FeatureRendererMixin {
    @Unique
    private static final AntiInvisible ANTI_INVISIBLE_MODULE = RockstarClient.create().getModuleRegistry().getModule(AntiInvisible.class);

    @WrapOperation(method={"renderModel"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V")})
    private static void changeModelColor(EntityModel<?> Impl, MatrixStack class_45872, VertexConsumer class_45882, int n, int n2, int n3, Operation<Void> operation, @Local(argsOnly=true) LivingEntityRenderState class_100422) {
        Entity renderedEntity;
        if (ANTI_INVISIBLE_MODULE.isEnabled() && ANTI_INVISIBLE_MODULE.isEntityInvisible((EntityRenderState)class_100422)) {
            renderedEntity = ((EntityAccess)class_100422).rockstar$getEntity();
            n3 = renderedEntity instanceof ArmorStandEntity ? ColorPalette.WHITE.withAlpha(0.0f).getRGB() : ColorPalette.WHITE.withAlpha(ANTI_INVISIBLE_MODULE.getOpacitySetting().getValue() / 100.0f * 255.0f).getRGB();
        }
        if (Glow.entityGlowRendering) {
            ColorRGBA colorRGBA;
            Glow glowRenderer = OverlayRegistry.getInstance().findOverlayByType(Glow.class);
            Entity class_12972 = ((EntityAccess)class_100422).rockstar$getEntity();
            if (glowRenderer != null && glowRenderer.isGlowTarget(class_12972) && (colorRGBA = glowRenderer.getGlowColor(class_12972)) != null) {
                n3 = colorRGBA.getRGB();
            }
        }
        operation.call(new Object[]{Impl, class_45872, class_45882, n, n2, n3});
    }

    @WrapOperation(method={"renderModel"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/RenderLayer;getEntityCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;")})
    private static RenderLayer changeModelRenderLayer(Identifier class_29602, Operation<RenderLayer> operation, @Local(argsOnly=true) LivingEntityRenderState class_100422) {
        if (ANTI_INVISIBLE_MODULE.isEnabled() && ANTI_INVISIBLE_MODULE.isEntityInvisible((EntityRenderState)class_100422)) {
            return RenderLayer.getItemEntityTranslucentCull((Identifier)class_29602);
        }
        if (Glow.entityGlowRendering) {
            Glow glowRenderer = OverlayRegistry.getInstance().findOverlayByType(Glow.class);
            Entity class_12972 = ((EntityAccess)class_100422).rockstar$getEntity();
            if (glowRenderer != null && glowRenderer.isGlowTarget(class_12972) && glowRenderer.getGlowColor(class_12972) != null) {
                return EntityGlowRenderLayer.getGlowLayer(class_29602);
            }
        }
        return (RenderLayer)operation.call(new Object[]{class_29602});
    }
}
