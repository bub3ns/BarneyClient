/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.events.render;

import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.geometry.ShapeRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public final class CameraRenderListener {
    private final EventListener<Render3DEvent> renderListener = render3DEvent -> this.renderSelectedBlockBounds(render3DEvent.getMatrices(), render3DEvent.getCamera());

    public static CameraRenderListener createRegistered(ClientServiceRegistry clientServiceRegistry) {
        CameraRenderListener cameraRenderListener = new CameraRenderListener();
        clientServiceRegistry.getEventBus().registerListeners(cameraRenderListener);
        return cameraRenderListener;
    }

    private void renderSelectedBlockBounds(MatrixStack class_45872, Camera class_41842) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || !(client.crosshairTarget instanceof BlockHitResult blockHit)
                || blockHit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos blockPosition = blockHit.getBlockPos();
        BlockState blockState = client.world.getBlockState(blockPosition);
        Box selectedBlockBounds = blockState.getOutlineShape(client.world, blockPosition)
            .getBoundingBox().offset(blockPosition);
        ColorRGBA colorRGBA = ColorPalette.getAccentColor();
        ColorRGBA colorRGBA2 = new ColorRGBA(colorRGBA.getRed(), colorRGBA.getGreen(), colorRGBA.getBlue()).withAlpha(40.0f);
        ColorRGBA colorRGBA3 = new ColorRGBA(colorRGBA.getRed(), colorRGBA.getGreen(), colorRGBA.getBlue()).withAlpha(220.0f);
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        class_45872.push();
        class_45872.translate(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z);
        ShapeRenderer.drawFilledBox(class_45872, selectedBlockBounds, colorRGBA2);
        ShapeRenderer.drawOutlinedBox(class_45872, selectedBlockBounds, colorRGBA3);
        class_45872.pop();
    }
}
