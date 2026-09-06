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
 *  net.minecraft.Identifier
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 */
package moscow.rockstar.render.navigation;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import moscow.rockstar.combat.rotation.RotationEngine;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.entity.CollisionProbe;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.CollisionPath;
import moscow.rockstar.world.navigation.PathExecutor;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public final class NavigationOverlayRenderer {
    private static final Identifier PATHFINDER_TEXTURE = Identifier.of((String)"rockstar", (String)"textures/pathfinder/bloom.png");
    private static final ColorRGBA PATH_COLOR = new ColorRGBA(80.0f, 220.0f, 255.0f);
    private static final ColorRGBA COLLISION_COLOR = new ColorRGBA(120.0f, 130.0f, 150.0f);
    private static final int PATH_SAMPLE_STEPS = 10;
    private final EventListener<Render3DEvent> navigationRenderListener = render3DEvent -> this.renderNavigationOverlay(render3DEvent.getMatrices(), render3DEvent.getCamera());

    public static NavigationOverlayRenderer create(ClientServiceRegistry clientServiceRegistry) {
        NavigationOverlayRenderer navigationOverlayRenderer = new NavigationOverlayRenderer();
        clientServiceRegistry.getEventBus().registerListeners(navigationOverlayRenderer);
        return navigationOverlayRenderer;
    }

    private void renderNavigationOverlay(MatrixStack class_45872, Camera class_41842) {
        Object object;
        Object object2;
        int n;
        ColorRGBA colorRGBA;
        Vec3d VanillaChestLootTableGenerator;
        PathExecutor pathExecutor = ClientServiceRegistry.getInstance().getPathExecutor();
        RotationEngine rotationEngine = ClientServiceRegistry.getInstance().getEventListenerSlot().getPendingScreenState().filter(RotationEngine.class::isInstance).map(RotationEngine.class::cast).orElse(null);
        CollisionPath collisionPath = pathExecutor != null ? pathExecutor.getPath() : null;
        CollisionProbe collisionProbe = pathExecutor != null ? pathExecutor.getCollisionProbe() : null;
        boolean bl = collisionPath != null && collisionPath.getNodes().size() > 1;
        Vec3d WallPlayerSkullBlock = collisionProbe != null ? collisionProbe.toBlockCenter() : null;
        List<Vec3d> list = rotationEngine != null ? rotationEngine.getTrajectoryPoints() : List.of();
        Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator = rotationEngine != null ? rotationEngine.getDestinationPosition() : null;
        if (!bl && WallPlayerSkullBlock == null && list.size() < 2 && VanillaChestLootTableGenerator == null) {
            return;
        }
        Vec3d PlayerSkullBlock = class_41842.getPos();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture((int)0, (Identifier)PATHFINDER_TEXTURE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        class_45872.push();
        class_45872.translate(-PlayerSkullBlock.x, -PlayerSkullBlock.y, -PlayerSkullBlock.z);
        if (list.size() >= 2) {
            colorRGBA = ColorPalette.getAccentColor();
            for (n = 0; n < list.size() - 1; ++n) {
                Vec3d point = list.get(n);
                Vec3d nextPoint = list.get(n + 1);
                for (int i = 0; i <= 10; ++i) {
                    float f = (float)i / 10.0f;
                    Vec3d interpolatedPoint = point.add(nextPoint.subtract(point).multiply((double)f));
                    this.drawPathMarker(class_45872, class_41842, class_2872, interpolatedPoint, 0.35f, 1.0f, colorRGBA);
                    this.drawPathMarker(class_45872, class_41842, class_2872, interpolatedPoint, 1.6f, 0.06f, colorRGBA);
                }
            }
        }
        if (VanillaChestLootTableGenerator != null) {
            this.drawPathMarker(class_45872, class_41842, class_2872, VanillaChestLootTableGenerator, 0.8f, 1.0f, ColorPalette.getAccentColor());
            this.drawPathMarker(class_45872, class_41842, class_2872, VanillaChestLootTableGenerator, 3.2f, 0.12f, ColorPalette.getAccentColor());
        }
        if (bl) {
            int n2 = pathExecutor.getCurrentStep();
            for (n = 0; n < collisionPath.getNodes().size() - 1; ++n) {
                object2 = collisionPath.getNodes().get(n);
                object = collisionPath.getNodes().get(n + 1);
                Vec3d VanillaFishingLootTableGenerator = new Vec3d((double)((BlockPositionOffset)object2).getX() + 0.5, (double)((BlockPositionOffset)object2).getY() + 0.5, (double)((BlockPositionOffset)object2).getZ() + 0.5);
                Vec3d LootTableProvider = new Vec3d((double)((BlockPositionOffset)object).getX() + 0.5, (double)((BlockPositionOffset)object).getY() + 0.5, (double)((BlockPositionOffset)object).getZ() + 0.5);
                float f = (float)VanillaFishingLootTableGenerator.distanceTo(LootTableProvider);
                ColorRGBA colorRGBA2 = ColorPalette.getAccentColor();
                for (int i = 0; i <= 10; ++i) {
                    float f2 = (float)i / 10.0f;
                    Vec3d PotatoesBlock = VanillaFishingLootTableGenerator.add(LootTableProvider.subtract(VanillaFishingLootTableGenerator).multiply((double)f2));
                    this.drawPathMarker(class_45872, class_41842, class_2872, PotatoesBlock, f / 3.0f, 1.0f, colorRGBA2);
                    this.drawPathMarker(class_45872, class_41842, class_2872, PotatoesBlock, f * 2.0f, 0.05f, colorRGBA2);
                }
            }
        }
        if (WallPlayerSkullBlock != null) {
            Vec3d collisionMarker = new Vec3d(WallPlayerSkullBlock.x, WallPlayerSkullBlock.y + 0.5, WallPlayerSkullBlock.z);
            this.drawPathMarker(class_45872, class_41842, class_2872, collisionMarker, 0.7f, 1.0f, ColorPalette.getAccentColor());
            this.drawPathMarker(class_45872, class_41842, class_2872, collisionMarker, 3.0f, 0.12f, ColorPalette.getAccentColor());
        }
        class_45872.pop();
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void drawPathMarker(MatrixStack class_45872, Camera class_41842, BufferBuilder class_2872, Vec3d VanillaChestLootTableGenerator, float f, float f2, ColorRGBA colorRGBA) {
        class_45872.push();
        class_45872.translate(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z);
        class_45872.multiply(class_41842.getRotation());
        int n = colorRGBA.withAlpha(255.0f * f2).getRGB();
        float f3 = f / 2.0f;
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        class_2872.vertex(matrix4f, -f3, f3, 0.0f).texture(0.0f, 1.0f).color(n);
        class_2872.vertex(matrix4f, f3, f3, 0.0f).texture(1.0f, 1.0f).color(n);
        class_2872.vertex(matrix4f, f3, -f3, 0.0f).texture(1.0f, 0.0f).color(n);
        class_2872.vertex(matrix4f, -f3, -f3, 0.0f).texture(0.0f, 0.0f).color(n);
        class_45872.pop();
    }
}
