/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 */
package moscow.rockstar.render.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientFeatureFlags;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.events.EventListener;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.navigation.CollisionPositionFinder;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import pyrock.events.render.Render3DEvent;

public final class BlockSelectionRenderer {
    private static final int OUTLINE_COLOR = 0x66FF66;
    private static final float FACE_INSET = 0.9f;
    private static final float VERTEX_INSET = 0.01f;
    private final EventListener<Render3DEvent> render3DEventListener = render3DEvent -> this.renderBlockSelection(render3DEvent.getMatrices(), render3DEvent.getCamera());

    public static BlockSelectionRenderer register(ClientServiceRegistry clientServiceRegistry) {
        BlockSelectionRenderer blockSelectionRenderer = new BlockSelectionRenderer();
        clientServiceRegistry.getEventBus().registerListeners(blockSelectionRenderer);
        return blockSelectionRenderer;
    }

    private void renderBlockSelection(MatrixStack class_45872, Camera class_41842) {
        if (!ClientFeatureFlags.blockSelectionEnabled) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        BlockPositionOffset blockPositionOffset = CollisionPositionFinder.findAlternateStandingPosition(client);
        if (blockPositionOffset == null) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        double d = (float)blockPositionOffset.getX() + 0.01f;
        double d2 = (float)blockPositionOffset.getY() + 0.01f;
        double d3 = (float)blockPositionOffset.getZ() + 0.01f;
        double d4 = (float)(blockPositionOffset.getX() + 1) - 0.01f;
        double d5 = (float)(blockPositionOffset.getY() + 1) - 0.01f;
        double d6 = (float)(blockPositionOffset.getZ() + 1) - 0.01f;
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.lineWidth((float)2.0f);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        class_45872.push();
        class_45872.translate(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z);
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        int n = -446234778;
        BlockSelectionRenderer.addLineSegment(class_2872, matrix4f, d, d2, d3, d4, d2, d3, n);
        BlockSelectionRenderer.addLineSegment(class_2872, matrix4f, d4, d2, d3, d4, d2, d6, n);
        BlockSelectionRenderer.addLineSegment(class_2872, matrix4f, d4, d2, d6, d, d2, d6, n);
        BlockSelectionRenderer.addLineSegment(class_2872, matrix4f, d, d2, d6, d, d2, d3, n);
        BlockSelectionRenderer.addLineSegment(class_2872, matrix4f, d, d5, d3, d4, d5, d3, n);
        BlockSelectionRenderer.addLineSegment(class_2872, matrix4f, d4, d5, d3, d4, d5, d6, n);
        BlockSelectionRenderer.addLineSegment(class_2872, matrix4f, d4, d5, d6, d, d5, d6, n);
        BlockSelectionRenderer.addLineSegment(class_2872, matrix4f, d, d5, d6, d, d5, d3, n);
        BlockSelectionRenderer.addLineSegment(class_2872, matrix4f, d, d2, d3, d, d5, d3, n);
        BlockSelectionRenderer.addLineSegment(class_2872, matrix4f, d4, d2, d3, d4, d5, d3, n);
        BlockSelectionRenderer.addLineSegment(class_2872, matrix4f, d4, d2, d6, d4, d5, d6, n);
        BlockSelectionRenderer.addLineSegment(class_2872, matrix4f, d, d2, d6, d, d5, d6, n);
        class_45872.pop();
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        RenderSystem.lineWidth((float)1.0f);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void addLineSegment(BufferBuilder class_2872, Matrix4f matrix4f, double d, double d2, double d3, double d4, double d5, double d6, int n) {
        class_2872.vertex(matrix4f, (float)d, (float)d2, (float)d3).color(n);
        class_2872.vertex(matrix4f, (float)d4, (float)d5, (float)d6).color(n);
    }
}
