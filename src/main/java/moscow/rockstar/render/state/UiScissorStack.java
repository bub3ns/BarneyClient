package moscow.rockstar.render.state;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MatrixUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import moscow.rockstar.render.batch.WidgetBatchRenderer;

/** Maintains the nested screen-space clip rectangles used by the UI renderer. */
public final class UiScissorStack {
    private static final Deque<ClipRect> CLIP_RECTS = new ArrayDeque<>();
    private static final Deque<Boolean> GEOMETRY_CLIPS = new ArrayDeque<>();
    private static boolean scissorAppliedForBatch;

    private UiScissorStack() {
    }

    public static void push(MatrixStack matrices, float x, float y, float width, float height) {
        push(matrices == null ? null : matrices.peek().getPositionMatrix(), x, y, width, height);
    }

    public static void push(float x, float y, float width, float height) {
        push((Matrix4f)null, x, y, width, height);
    }

    public static void push(Matrix4f matrix, float x, float y, float width, float height) {
        boolean geometryBatch = WidgetBatchRenderer.isGeometryBatchActive();
        if (geometryBatch) {
            WidgetBatchRenderer.flushCurrentBatchIfNeeded();
        } else {
            WidgetBatchRenderer.flushCurrentBatch();
        }
        ClipRect next = new ClipRect(x, y, width, height).transform(matrix);
        if (!CLIP_RECTS.isEmpty()) {
            next = ClipRect.intersection(CLIP_RECTS.peek(), next);
        }
        CLIP_RECTS.push(next);
        GEOMETRY_CLIPS.push(geometryBatch);
        if (geometryBatch) {
            WidgetBatchRenderer current = WidgetBatchRenderer.getCurrentBatch();
            if (current != null) {
                current.submitGeometryQuad(next.x, next.y, next.width, next.height);
            }
            if (scissorAppliedForBatch) {
                apply(next);
            }
        } else {
            apply(next);
        }
    }

    private static void apply(ClipRect clip) {
        Window window = MinecraftClient.getInstance().getWindow();
        double scale = window.getScaleFactor();
        float left = clip.x * (float)scale;
        float top = clip.y * (float)scale;
        float right = (clip.x + clip.width) * (float)scale;
        float bottom = (clip.y + clip.height) * (float)scale;
        int scissorX = (int)Math.floor(left);
        int scissorY = (int)Math.floor((double)window.getFramebufferHeight() - Math.ceil(bottom) + 0.5);
        int scissorWidth = Math.max(0, (int)Math.ceil(right) - scissorX);
        int scissorHeight = Math.max(0, (int)Math.ceil(bottom) - (int)Math.floor(top) - 1);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    public static void pop() {
        boolean geometryBatch = !GEOMETRY_CLIPS.isEmpty() && GEOMETRY_CLIPS.peek();
        if (geometryBatch) {
            WidgetBatchRenderer.flushCurrentBatchIfNeeded();
        } else {
            WidgetBatchRenderer.flushCurrentBatch();
        }
        if (!CLIP_RECTS.isEmpty()) {
            CLIP_RECTS.pop();
        }
        if (!GEOMETRY_CLIPS.isEmpty()) {
            GEOMETRY_CLIPS.pop();
        }
        if (geometryBatch) {
            WidgetBatchRenderer current = WidgetBatchRenderer.getCurrentBatch();
            if (current != null) {
                current.flushGeometryBatch();
            }
        }
        if (!CLIP_RECTS.isEmpty()) {
            if (!geometryBatch || scissorAppliedForBatch) {
                apply(CLIP_RECTS.peek());
            }
        } else {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            scissorAppliedForBatch = false;
        }
    }

    /** Temporarily disables the real GL scissor while the stream shader draws. */
    public static void disableForBatch() {
        if (!scissorAppliedForBatch) {
            return;
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        scissorAppliedForBatch = false;
    }

    /** Restores the current real GL scissor after a geometry batch has flushed. */
    public static void resumeForBatch() {
        if (CLIP_RECTS.isEmpty() || GEOMETRY_CLIPS.isEmpty() || !GEOMETRY_CLIPS.peek()) {
            return;
        }
        apply(CLIP_RECTS.peek());
        scissorAppliedForBatch = true;
    }

    public static boolean isActive() {
        return !CLIP_RECTS.isEmpty();
    }

    public static int depth() {
        return CLIP_RECTS.size();
    }

    public static void clear() {
        WidgetBatchRenderer.flushCurrentBatchIfNeeded();
        WidgetBatchRenderer current = WidgetBatchRenderer.getCurrentBatch();
        if (current != null) {
            for (Boolean geometryBatch : GEOMETRY_CLIPS) {
                if (geometryBatch) {
                    current.flushGeometryBatch();
                }
            }
        }
        CLIP_RECTS.clear();
        GEOMETRY_CLIPS.clear();
        scissorAppliedForBatch = false;
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private record ClipRect(float x, float y, float width, float height) {
        private ClipRect transform(Matrix4f matrix) {
            if (matrix == null || MatrixUtil.isIdentity(matrix)) {
                return this;
            }
            Vector3f topLeft = matrix.transformPosition(new Vector3f(this.x, this.y, 0.0f));
            Vector3f bottomRight = matrix.transformPosition(new Vector3f(this.x + this.width, this.y + this.height, 0.0f));
            return new ClipRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
        }

        private static ClipRect intersection(ClipRect first, ClipRect second) {
            float left = Math.max(first.x, second.x);
            float top = Math.max(first.y, second.y);
            float right = Math.min(first.x + first.width, second.x + second.width);
            float bottom = Math.min(first.y + first.height, second.y + second.height);
            return new ClipRect(left, top, Math.max(0.0f, right - left), Math.max(0.0f, bottom - top));
        }
    }
}
