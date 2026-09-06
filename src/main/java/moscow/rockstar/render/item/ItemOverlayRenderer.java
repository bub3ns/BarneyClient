/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ItemRenderState
 *  net.minecraft.LivingEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.RenderLayer
 *  net.minecraft.World
 *  net.minecraft.Identifier
 *  net.minecraft.DiffuseLighting
 *  net.minecraft.DrawContext
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.VertexConsumerProvider$Immediate
 *  net.minecraft.OverlayTexture
 *  net.minecraft.RenderPhase$TextureBase
 *  net.minecraft.ModelTransformationMode
 */
package moscow.rockstar.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.IdentityHashMap;
import java.util.Map;
import moscow.rockstar.mixin.accessors.DrawContextAccessor;
import moscow.rockstar.mixin.accessors.MultiPhaseParametersAccessor;
import moscow.rockstar.mixin.accessors.RenderLayerMultiPhaseAccessor;
import moscow.rockstar.mixin.accessors.RenderPhaseAccessor;
import moscow.rockstar.mixin.accessors.RenderPhaseTextureAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.world.World;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.item.ModelTransformationMode;
import moscow.rockstar.render.batch.WidgetBatchRenderer;

public final class ItemOverlayRenderer {
    private static final Map<RenderLayer, RenderLayer> RENDER_LAYER_CACHE = new IdentityHashMap<RenderLayer, RenderLayer>();

    private ItemOverlayRenderer() {
    }

    public static void renderTranslucentItemOverlay(DrawContext ServerConfigException, ItemStack class_17992, float f, float f2, float f3, float f4) {
        if (class_17992.isEmpty() || f4 <= 0.01f) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderState class_104442 = ((DrawContextAccessor)ServerConfigException).getItemRenderState();
        VertexConsumerProvider.Immediate class_45982 = ((DrawContextAccessor)ServerConfigException).getVertexConsumers();
        client.getItemModelManager().update(class_104442, class_17992, ModelTransformationMode.GUI, false, (World)client.world, (LivingEntity)client.player, 0);
        if (class_104442.isEmpty()) {
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        MatrixStack class_45872 = ServerConfigException.getMatrices();
        class_45872.push();
        class_45872.translate(f, f2, 0.0f);
        class_45872.scale(f3, f3, f3);
        class_45872.translate(8.0f, 8.0f, 150.0f);
        class_45872.scale(16.0f, -16.0f, 16.0f);
        boolean bl = !class_104442.isSideLit();
        class_45982.draw();
        if (bl) {
            DiffuseLighting.disableGuiDepthLighting();
        } else {
            DiffuseLighting.enableGuiDepthLighting();
        }
        float[] fArray = (float[])RenderSystem.getShaderColor().clone();
        RenderSystem.setShaderColor((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)f4);
        class_104442.render(class_45872, class_45982, 0xF000F0, OverlayTexture.DEFAULT_UV);
        class_45982.draw();
        RenderSystem.setShaderColor((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
        if (bl) {
            DiffuseLighting.enableGuiDepthLighting();
        }
        class_45872.pop();
    }

    private static RenderLayer getCachedOpaqueRenderLayer(RenderLayer layer) {
        RenderLayer cachedLayer = RENDER_LAYER_CACHE.get(layer);
        if (cachedLayer != null) {
            return cachedLayer;
        }
        RenderLayer replacementLayer = layer;
        if (layer instanceof RenderLayerMultiPhaseAccessor multiPhaseLayer) {
            RenderLayer.MultiPhaseParameters phases = multiPhaseLayer.rockstar$getPhases();
            MultiPhaseParametersAccessor phasesAccessor = (MultiPhaseParametersAccessor)(Object)phases;
            if (phasesAccessor.rockstar$getTransparency() == RenderPhaseAccessor.rockstar$getNoTransparency()) {
                RenderPhase.TextureBase textureBase = phasesAccessor.rockstar$getTexture();
                if (textureBase instanceof RenderPhaseTextureAccessor textureAccessor) {
                    Identifier textureId = textureAccessor.rockstar$getId().orElse(null);
                    if (textureId != null) {
                        replacementLayer = RenderLayer.getItemEntityTranslucentCull(textureId);
                    }
                }
            }
        }
        RENDER_LAYER_CACHE.put(layer, replacementLayer);
        return replacementLayer;
    }
}
