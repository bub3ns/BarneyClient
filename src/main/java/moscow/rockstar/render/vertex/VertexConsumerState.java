package moscow.rockstar.render.vertex;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import moscow.rockstar.modules.visuals.hand.ViewModel;
import moscow.rockstar.render.target.RenderTarget;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;

/** Captures projected hand vertices so the chat editor can select a hand. */
public final class VertexConsumerState implements VertexConsumerProvider {
    private static final float OCCLUSION_MARGIN_PIXELS = 3.0f;

    public final Animation animation = new Animation(220L, 0.0f, Easing.easeInOutCubicBezier);
    private final float[] angleMaximumScreenDistances = new float[16];
    public final RenderTarget projectionRenderTarget = new RenderTarget(true).setResolutionScale(1.0f);
    private final Map<RenderLayer, VertexConsumerProxy> consumersByRenderLayer = new IdentityHashMap<>();
    private Matrix4f projectionMatrix;
    private float minScreenX;
    private float minScreenY;
    private float maxScreenX;
    private float maxScreenY;
    private int projectedVertexCount;
    private long lastProjectionClearTime;
    public boolean projectionActive;

    public VertexConsumerState() {
    }

    public void beginProjection(Matrix4f matrix) {
        this.projectionMatrix = matrix;
        this.projectedVertexCount = 0;
        this.projectionActive = false;
        this.minScreenX = Float.MAX_VALUE;
        this.minScreenY = Float.MAX_VALUE;
        this.maxScreenX = -Float.MAX_VALUE;
        this.maxScreenY = -Float.MAX_VALUE;
        Arrays.fill(this.angleMaximumScreenDistances, -Float.MAX_VALUE);
    }

    public void endProjection() {
        this.projectionMatrix = null;
        if (this.projectedVertexCount >= 3) {
            this.lastProjectionClearTime = System.currentTimeMillis();
        }
    }

    public boolean isProjectionVisibleRecently() {
        return this.projectedVertexCount >= 3
                && System.currentTimeMillis() - this.lastProjectionClearTime < 250L;
    }

    public boolean isScreenPointInsideProjection(float x, float y) {
        return this.isProjectionVisibleRecently() && this.isScreenPointInsideAngleBounds(x, y, 0.0f);
    }

    private boolean isScreenPointInsideAngleBounds(float x, float y, float margin) {
        for (int i = 0; i < 16; ++i) {
            if (x * ViewModel.COSINE_TABLE[i] + y * ViewModel.SINE_TABLE[i]
                    > this.angleMaximumScreenDistances[i] + OCCLUSION_MARGIN_PIXELS + margin) {
                return false;
            }
        }
        return true;
    }

    public double distanceToProjectionCenter(double x, double y) {
        double dx = x - this.getProjectionCenterX();
        double dy = y - (this.minScreenY + this.maxScreenY) / 2.0f;
        return dx * dx + dy * dy;
    }

    private float getProjectionCenterX() {
        return (this.minScreenX + this.maxScreenX) / 2.0f;
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer renderLayer) {
        return this.consumersByRenderLayer.computeIfAbsent(renderLayer, ignored -> new VertexConsumerProxy(this));
    }

    void projectVertex(float x, float y, float z) {
        Matrix4f matrix = this.projectionMatrix;
        if (matrix == null) {
            return;
        }
        float clipX = matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30();
        float clipY = matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31();
        float clipW = matrix.m03() * x + matrix.m13() * y + matrix.m23() * z + matrix.m33();
        if (clipW <= 1.0E-4f) {
            return;
        }
        float screenX = (clipX / clipW * 0.5f + 0.5f) * WindowMetricsProvider.INSTANCE.width();
        float screenY = (0.5f - clipY / clipW * 0.5f) * WindowMetricsProvider.INSTANCE.height();
        for (int i = 0; i < 16; ++i) {
            float distance = screenX * ViewModel.COSINE_TABLE[i] + screenY * ViewModel.SINE_TABLE[i];
            if (distance > this.angleMaximumScreenDistances[i]) {
                this.angleMaximumScreenDistances[i] = distance;
            }
        }
        this.minScreenX = Math.min(this.minScreenX, screenX);
        this.maxScreenX = Math.max(this.maxScreenX, screenX);
        this.minScreenY = Math.min(this.minScreenY, screenY);
        this.maxScreenY = Math.max(this.maxScreenY, screenY);
        ++this.projectedVertexCount;
    }

    VertexConsumerState getState() {
        return this;
    }
}
