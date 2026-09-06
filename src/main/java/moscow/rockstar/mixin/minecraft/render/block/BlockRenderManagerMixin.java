/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Item
 *  net.minecraft.Block
 *  net.minecraft.BlockState
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.VertexConsumerProvider$Immediate
 *  net.minecraft.ClientPlayerEntity
 *  net.minecraft.BlockRenderManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.render.block;

import moscow.rockstar.modules.visuals.esp.entities.Glow;
import moscow.rockstar.modules.visuals.esp.entities.Fill;
import moscow.rockstar.modules.visuals.esp.entities.Flame;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.visuals.esp.targeting.ItemTargetType;
import moscow.rockstar.modules.visuals.hand.ViewModel;
import moscow.rockstar.render.esp.OverlayRegistry;
import moscow.rockstar.render.item.HeldItemRenderCapture;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.block.BlockRenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockRenderManager.class})
public abstract class BlockRenderManagerMixin {
    @Unique
    private static boolean rockstar$capturingHeldBlock;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"renderBlockAsEntity"}, at={@At(value="HEAD")})
    private void rockstar$decorateHeldBlock(BlockState class_26802, MatrixStack class_45872, VertexConsumerProvider class_45972, int n, int n2, CallbackInfo callbackInfo) {
        VertexConsumerProvider.Immediate immediateProvider;
        if (rockstar$capturingHeldBlock || !HeldItemRenderCapture.capturing || HeldItemRenderCapture.rendering || ViewModel.renderingHands) {
            return;
        }
        if (class_26802 == null || !BlockRenderManagerMixin.rockstar$isHeld(class_26802.getBlock())) {
            return;
        }
        HeldItemRenderCapture.captureBlockPose(class_26802, n2, class_45872);
        Glow glowRenderer = OverlayRegistry.getInstance().findOverlayByType(Glow.class);
        Fill fillRenderer = OverlayRegistry.getInstance().findOverlayByType(Fill.class);
        Flame flameRenderer = OverlayRegistry.getInstance().findOverlayByType(Flame.class);
        boolean glowEnabled = glowRenderer != null && glowRenderer.isValid2(ItemTargetType.HELD);
        boolean fillEnabled = fillRenderer != null && fillRenderer.isValid2(ItemTargetType.HELD);
        boolean flameEnabled = flameRenderer != null && flameRenderer.isValid2(ItemTargetType.HELD);
        if (!glowEnabled && !fillEnabled && !flameEnabled) {
            return;
        }
        if (class_45972 instanceof VertexConsumerProvider.Immediate) {
            immediateProvider = (VertexConsumerProvider.Immediate)class_45972;
            immediateProvider.draw();
        }
        BlockRenderManager blockRenderManager = (BlockRenderManager)(Object)this;
        rockstar$capturingHeldBlock = true;
        try {
            if (glowEnabled) {
                glowRenderer.renderBlockGlow(blockRenderManager, class_26802, class_45872, n2);
            }
            if (flameEnabled) {
                flameRenderer.renderBlockWithFlame(blockRenderManager, class_26802, class_45872, n2);
            }
            if (fillEnabled) {
                fillRenderer.renderBlockFill(blockRenderManager, class_26802, class_45872, n2);
            }
        }
        finally {
            rockstar$capturingHeldBlock = false;
        }
    }

    @Unique
    private static boolean rockstar$isHeld(Block class_22482) {
        ClientPlayerEntity class_7462 = MinecraftClient.getInstance().player;
        if (class_7462 == null) {
            return false;
        }
        return Block.getBlockFromItem((Item)class_7462.getMainHandStack().getItem()) == class_22482 || Block.getBlockFromItem((Item)class_7462.getOffHandStack().getItem()) == class_22482;
    }
}
