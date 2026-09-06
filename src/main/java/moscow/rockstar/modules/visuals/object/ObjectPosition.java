package moscow.rockstar.modules.visuals.object;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.util.Timer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

final class ObjectPosition {
    final BlockPos blockPosition;
    final ObjectType objectType;
    final Timer lifetimeTimer = new Timer();

    void renderHudTimer(PreHudRenderEvent event) {
        int seconds = (int) ((this.objectType.getLifetimeMillis() - this.lifetimeTimer.getElapsedMillis()) / 1000.0f);
        MatrixStack matrices = event.getContext().getMatrices();
        Vec3d position = this.blockPosition.add(0, 1, 0).toCenterPos();
        Vec2f screenPosition = ProjectionUtils.projectToScreen(position);
        if (screenPosition == null) {
            return;
        }
        float distance = (float) ClientAccess.minecraftClient.player.getPos()
                .distanceTo(this.blockPosition.toCenterPos());
        float distanceScale = MathHelper.clamp(1.0f - distance / 20.0f, 0.5f, 1.0f) * 0.5f;
        float progress = 1.0f - (float) this.lifetimeTimer.getElapsedMillis()
                / (float) this.objectType.getLifetimeMillis();
        String label = "0:" + (seconds < 10 ? "0" + seconds : Integer.toString(seconds));
        float width = 150.0f;
        float height = 150.0f;
        matrices.push();
        matrices.translate(screenPosition.x - width / 2.0f, screenPosition.y - height / 2.0f, 0.0f);
        ItemRenderUtils.translateAndScale(matrices, width / 2.0f, height / 2.0f, distanceScale);
        event.getContext().drawBlurredRect(0.0f, 0.0f, width, height, 45.0f, 5.0f,
                WidgetState.uniform(26.0f), ColorPalette.WHITE);
        event.getContext().drawSquircle(0.0f, 0.0f, width, height, 5.0f,
                WidgetState.uniform(26.0f), new ColorRGBA(9.0f, 9.0f, 11.0f).mulAlpha(0.5f));
        event.getContext().drawCircleProgress(width / 2.0f, height / 2.0f, 48.0f, 6.0f,
                progress, ColorPalette.getAccentColor());
        event.getContext().drawItem(this.objectType.getItem(), 60.0f, 50.0f, 1.875f);
        event.getContext().drawCenteredText(Font.ROUND_BOLD.metrics(20.0f), label,
                width / 2.0f, 86.0f, ColorPalette.getPrimaryTextColor());
        ItemRenderUtils.popMatrix(matrices);
        matrices.pop();
    }

    void onRender3D(Render3DEvent event, BufferBuilder buffer) {
        if (this.objectType != ObjectType.STUN) {
            return;
        }
        float innerRadius = 1.0f;
        float outerRadius = 2.5f;
        MatrixStack matrices = event.getMatrices();
        Camera camera = ClientAccess.minecraftClient.gameRenderer.getCamera();
        ItemRenderUtils.translateToWorldPosition(matrices, this.blockPosition.toCenterPos());
        float[][] segments = {
                {15.0f, -15.0f, 15.0f, 0.0f, 1.0f, 0.0f},
                {-15.0f, -15.0f, 15.0f, 0.0f, 1.0f, 0.0f},
                {15.0f, -15.0f, -15.0f, 0.0f, 1.0f, 0.0f},
                {-15.0f, -15.0f, -15.0f, 0.0f, 1.0f, 0.0f},
                {-15.0f, 15.0f, 15.0f, 1.0f, 0.0f, 0.0f},
                {-15.0f, -15.0f, 15.0f, 1.0f, 0.0f, 0.0f},
                {-15.0f, 15.0f, -15.0f, 1.0f, 0.0f, 0.0f},
                {-15.0f, -15.0f, -15.0f, 1.0f, 0.0f, 0.0f},
                {15.0f, 15.0f, -15.0f, 0.0f, 0.0f, 1.0f},
                {-15.0f, 15.0f, -15.0f, 0.0f, 0.0f, 1.0f},
                {15.0f, -15.0f, -15.0f, 0.0f, 0.0f, 1.0f},
                {-15.0f, -15.0f, -15.0f, 0.0f, 0.0f, 1.0f}
        };
        for (float[] segment : segments) {
            for (float offset = 0.0f; offset < 30.0f; offset += 0.2f) {
                matrices.push();
                matrices.translate(segment[0] + offset * segment[3],
                        segment[1] + offset * segment[4], segment[2] + offset * segment[5]);
                matrices.multiply(camera.getRotation());
                ShaderRenderer.appendTexturedQuadVertices(matrices, buffer,
                        -innerRadius / 2.0f, -innerRadius / 2.0f, 0.0,
                        innerRadius, innerRadius, ColorPalette.getAccentColor().mulAlpha(0.9f));
                ShaderRenderer.appendTexturedQuadVertices(matrices, buffer,
                        -outerRadius / 2.0f, -outerRadius / 2.0f, 0.0,
                        outerRadius, outerRadius, ColorPalette.getAccentColor().mulAlpha(0.1f));
                matrices.pop();
            }
        }
    }

    public BlockPos getBlockPosition() {
        return this.blockPosition;
    }

    public ObjectType getObjectType() {
        return this.objectType;
    }

    public Timer getLifetimeTimer() {
        return this.lifetimeTimer;
    }

    ObjectPosition(BlockPos blockPosition, ObjectType objectType) {
        this.blockPosition = blockPosition;
        this.objectType = objectType;
    }
}
