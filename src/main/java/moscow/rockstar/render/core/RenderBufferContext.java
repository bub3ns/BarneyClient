package moscow.rockstar.render.core;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Collects vertices for one small immediate render pass.
 *
 * The decompiled artifact had this context mapped onto an unrelated setting
 * widget. Keeping the active buffer here makes the render helpers explicit
 * and prevents UI components from being used as vertex consumers.
 */
public class RenderBufferContext {
    private static RenderBufferContext activeContext;

    private final RenderBufferContext previousContext;
    private final BufferBuilder bufferBuilder;
    private final MatrixStack matrixStack;
    private boolean finished;

    public RenderBufferContext(VertexFormat format, MatrixStack matrixStack) {
        // Flush the currently queued widget work before taking ownership of
        // the immediate buffer.  The original context does not open a new
        // widget batch here; opening one would leave a nested batch active
        // after this context is rendered and can redirect later UI draws.
        WidgetBatchRenderer.flushCurrentBatch();
        this.previousContext = activeContext;
        this.bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, format);
        this.matrixStack = matrixStack;
        activeContext = this;
    }

    public static RenderBufferContext current() {
        return activeContext;
    }

    public BufferBuilder bufferBuilder() {
        return this.bufferBuilder;
    }

    public MatrixStack matrixStack() {
        return this.matrixStack;
    }

    public void finish() {
        if (this.finished) {
            return;
        }
        BuiltBuffer builtBuffer = this.bufferBuilder.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
        }
        this.finished = true;
    }

    /**
     * Restore the context which was active before this pass was opened.
     * The original render contexts finish their buffer first and pop their
     * owner only after restoring the shader-specific GL state.
     */
    protected void pop() {
        if (activeContext == this) {
            activeContext = this.previousContext;
        }
    }
}
