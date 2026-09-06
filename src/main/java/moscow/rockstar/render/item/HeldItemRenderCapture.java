package moscow.rockstar.render.item;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.modules.visuals.esp.entities.Fill;
import moscow.rockstar.modules.visuals.esp.entities.Flame;
import moscow.rockstar.modules.visuals.esp.entities.Glow;
import moscow.rockstar.modules.visuals.esp.targeting.ItemTargetType;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.esp.OverlayRegistry;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.target.RenderTarget;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

/**
 * Captures the original first-person item, block, and arm poses before the
 * client draws the overlay passes. This is the named counterpart of the
 * original held-item capture helper; it is deliberately separate from the
 * event bus and does not replace vanilla rendering.
 */
public final class HeldItemRenderCapture implements ClientAccess, WindowHandle {
    public static boolean rendering;
    public static boolean capturing;
    public static boolean leftHand;

    private static final RenderTarget CAPTURE_TARGET = new RenderTarget(true).enableLinearFiltering().setResolutionScale(1.0f);
    private static final int FULL_BRIGHT_LIGHT = 0xF000F0;
    private static final List<ItemPose> ITEM_POSES = new ArrayList<>();
    private static final List<BlockPose> BLOCK_POSES = new ArrayList<>();
    private static final List<ArmPose> ARM_POSES = new ArrayList<>();
    private static boolean captureReady;

    private HeldItemRenderCapture() {
    }

    public static boolean hasHeldItemOverlay() {
        OverlayRegistry overlays = OverlayRegistry.getInstance();
        if (overlays == null) {
            return false;
        }
        return isHeldItemOverlayEnabled(overlays.findOverlayByType(Glow.class))
            || isHeldItemOverlayEnabled(overlays.findOverlayByType(Fill.class))
            || isHeldItemOverlayEnabled(overlays.findOverlayByType(Flame.class));
    }

    private static boolean isHeldItemOverlayEnabled(moscow.rockstar.render.esp.TargetRenderModule overlay) {
        return overlay != null && overlay.isValid2(ItemTargetType.HELD);
    }

    public static void beginCapture() {
        capturing = true;
        leftHand = false;
        captureReady = false;
        ITEM_POSES.clear();
        BLOCK_POSES.clear();
        ARM_POSES.clear();
    }

    public static void endCapture() {
        capturing = false;
    }

    public static void captureItemPose(ItemStack stack, ModelTransformationMode mode, boolean leftHanded, MatrixStack matrices) {
        if (!hasHeldItemOverlay() || stack == null || stack.isEmpty()) {
            return;
        }
        ITEM_POSES.add(new ItemPose(stack.copy(), mode, leftHanded, new Matrix4f(matrices.peek().getPositionMatrix())));
    }

    public static void captureBlockPose(BlockState state, int overlay, MatrixStack matrices) {
        if (!hasHeldItemOverlay() || state == null) {
            return;
        }
        BLOCK_POSES.add(new BlockPose(state, overlay, new Matrix4f(matrices.peek().getPositionMatrix())));
    }

    public static void captureArmPose(Arm arm, MatrixStack matrices) {
        if (!hasHeldItemOverlay()) {
            return;
        }
        ARM_POSES.add(new ArmPose(arm, new Matrix4f(matrices.peek().getPositionMatrix())));
    }

    public static void renderCaptured() {
        if (ARM_POSES.isEmpty() || !hasHeldItemOverlay()) {
            return;
        }
        ClientPlayerEntity player = minecraftClient.player;
        if (player == null) {
            return;
        }
        EntityRenderer renderer = minecraftClient.getEntityRenderDispatcher().getRenderer(player);
        if (!(renderer instanceof PlayerEntityRenderer playerRenderer)) {
            return;
        }

        rendering = true;
        try {
            CAPTURE_TARGET.beginPass(true);
            try {
                VertexConsumerProvider.Immediate consumers = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
                for (ItemPose pose : ITEM_POSES) {
                    MatrixStack poseMatrices = new MatrixStack();
                    poseMatrices.multiplyPositionMatrix(pose.pose());
                    minecraftClient.getItemRenderer().renderItem(
                        (LivingEntity) player,
                        pose.stack(),
                        pose.mode(),
                        pose.leftHanded(),
                        poseMatrices,
                        consumers,
                        player.getWorld(),
                        FULL_BRIGHT_LIGHT,
                        OverlayTexture.DEFAULT_UV,
                        player.getId() + pose.mode().ordinal()
                    );
                }
                for (BlockPose pose : BLOCK_POSES) {
                    MatrixStack poseMatrices = new MatrixStack();
                    poseMatrices.multiplyPositionMatrix(pose.pose());
                    minecraftClient.getBlockRenderManager().renderBlockAsEntity(
                        pose.state(), poseMatrices, consumers, FULL_BRIGHT_LIGHT, pose.overlay()
                    );
                }
                consumers.draw();

                RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
                RenderSystem.clear(0x4000);
                Identifier skinTexture = player.getSkinTextures().texture();
                for (ArmPose pose : ARM_POSES) {
                    MatrixStack poseMatrices = new MatrixStack();
                    poseMatrices.multiplyPositionMatrix(pose.pose());
                    if (pose.arm() == Arm.LEFT) {
                        playerRenderer.renderLeftArm(
                            poseMatrices,
                            consumers,
                            FULL_BRIGHT_LIGHT,
                            skinTexture,
                            player.isPartVisible(PlayerModelPart.LEFT_SLEEVE)
                        );
                    } else {
                        playerRenderer.renderRightArm(
                            poseMatrices,
                            consumers,
                            FULL_BRIGHT_LIGHT,
                            skinTexture,
                            player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE)
                        );
                    }
                }
                consumers.draw();
                captureReady = true;
            } catch (Exception exception) {
                // The original helper keeps the vanilla hand path alive after a capture failure.
                moscow.rockstar.core.RockstarClient.LOGGER.error("Held-item capture failed", exception);
            } finally {
                CAPTURE_TARGET.endPass();
            }
        } finally {
            rendering = false;
            ITEM_POSES.clear();
            BLOCK_POSES.clear();
            ARM_POSES.clear();
        }
    }

    public static void renderOverlay(RenderTarget destination) {
        if (!captureReady || destination == null) {
            return;
        }
        destination.beginWrite(true);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.ZERO,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ZERO,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader((ShaderProgramKey) ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, CAPTURE_TARGET.getColorAttachment());
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, WINDOW.getScaledWidth(), WINDOW.getScaledHeight());
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        minecraftClient.getFramebuffer().beginWrite(true);
    }

    private record ItemPose(ItemStack stack, ModelTransformationMode mode, boolean leftHanded, Matrix4f pose) {
    }

    private record BlockPose(BlockState state, int overlay, Matrix4f pose) {
    }

    private record ArmPose(Arm arm, Matrix4f pose) {
    }
}
