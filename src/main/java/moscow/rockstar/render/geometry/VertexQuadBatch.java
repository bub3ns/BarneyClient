package moscow.rockstar.render.geometry;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;

/** Owns the quad buffer used by a small immediate render batch. */
public abstract class VertexQuadBatch {
    private final VertexFormat vertexFormat;
    private BufferBuilder buffer;

    protected VertexQuadBatch(VertexFormat vertexFormat) {
        this.vertexFormat = vertexFormat;
        this.buffer = beginBuffer();
    }

    protected final BufferBuilder getBuffer() {
        return this.buffer;
    }

    protected final void resetBuffer() {
        this.buffer = beginBuffer();
    }

    private BufferBuilder beginBuffer() {
        return Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, this.vertexFormat);
    }
}
