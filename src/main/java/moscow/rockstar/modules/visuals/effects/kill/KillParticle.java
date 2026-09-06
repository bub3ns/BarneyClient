package moscow.rockstar.modules.visuals.effects.kill;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import pyrock.utility.render.ColorRGBA;

final class KillParticle {
    final Vec3d position;
    final ColorRGBA color;
    boolean reverseAnimation = true;
    final Animation animation = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
    final List<Vec3d> particlePositions = new ArrayList<>();

    KillParticle(Vec3d position, ColorRGBA color) {
        this.position = position;
        this.color = color;
        Vec3d current = position;
        for (int i = 0; i < 200; ++i) {
            current = current.add(MathUtils.interpolateRandomDouble(-0.4f, 0.4f), 0.25,
                    MathUtils.interpolateRandomDouble(-0.4f, 0.4f));
            this.particlePositions.add(current);
        }
    }

    void renderParticles(BufferBuilder buffer, MatrixStack matrices, Camera camera) {
        this.animation.setEasing(Easing.easeInBounce);
        this.animation.setDuration(500L);
        this.animation.setReverse(this.reverseAnimation);
        for (Vec3d particlePosition : this.particlePositions) {
            float size = (float) (2.0 + 5.0 * (particlePosition.y - this.position.y) / 50.0);
            matrices.push();
            ItemRenderUtils.translateToWorldPosition(matrices, particlePosition);
            matrices.multiply(camera.getRotation());
            ShaderRenderer.appendTexturedQuadVertices(matrices, buffer,
                    -size / 2.0f, -size / 2.0f, 0.0, size, size,
                    this.color.withAlpha(255.0f * this.animation.getValue() * 0.4f));
            matrices.pop();
        }
    }
}
