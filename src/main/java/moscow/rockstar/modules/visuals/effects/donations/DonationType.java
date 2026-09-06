package moscow.rockstar.modules.visuals.effects.donations;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.shaders.ShaderRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

final class DonationType {
    double x;
    double y;
    double z;
    double velocityX;
    double velocityY;
    double velocityZ;
    long spawnTimeMillis;
    long lifetimeMillis;
    ColorRGBA color;
    Identifier texture;
    boolean rotated;

    DonationType(Vec3d position, ColorRGBA color, Identifier texture, boolean rotated) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        this.color = color;
        this.texture = texture;
        this.rotated = rotated;
        this.velocityX = 0.0;
        this.velocityY = 0.003 + Math.random() * 0.003;
        this.velocityZ = 0.0;
        this.spawnTimeMillis = System.currentTimeMillis();
        this.lifetimeMillis = 800L;
    }

    boolean isExpired() {
        return System.currentTimeMillis() - this.spawnTimeMillis > this.lifetimeMillis;
    }

    void updatePosition() {
        long now = System.currentTimeMillis();
        long elapsed = now - this.spawnTimeMillis;
        if (elapsed < 0L) {
            return;
        }
        this.x += this.velocityX;
        this.y += this.velocityY;
        this.z += this.velocityZ;
    }

    float getFadeProgress() {
        long elapsed = System.currentTimeMillis() - this.spawnTimeMillis;
        float progress = net.minecraft.util.math.MathHelper.clamp(
                (float) elapsed / (float) this.lifetimeMillis, 0.0f, 1.0f);
        return 1.0f - progress;
    }

    void render(Render3DEvent event, BufferBuilder buffer) {
        MatrixStack matrices = event.getMatrices();
        Camera camera = ClientAccess.minecraftClient.gameRenderer.getCamera();
        float size = 0.15f;
        float fade = this.getFadeProgress();
        matrices.push();
        ItemRenderUtils.translateToWorldPosition(matrices, new Vec3d(this.x, this.y, this.z));
        matrices.multiply(camera.getRotation());
        int rotation = this.rotated ? 180 : 0;
        ColorRGBA drawColor = this.color.mulAlpha(0.9f * fade);
        ShaderRenderer.appendRotatedTexturedQuadVertices(
                matrices, buffer, -size / 2.0f, -size / 2.0f, 0.0,
                size, size, drawColor, rotation);
        matrices.pop();
    }
}
