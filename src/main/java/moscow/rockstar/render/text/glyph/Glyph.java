package moscow.rockstar.render.text.glyph;

import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

/** A single glyph and its location in the Slug curve atlas. */
public final class Glyph {
    private static final float MINIMUM_GLYPH_SCALE = 1.0f;

    private final int codePoint;
    private final int atlasIndex;
    private final float advanceWidth;
    private final float minX;
    private final float minY;
    private final float maxX;
    private final float maxY;

    public Glyph(int codePoint, int atlasIndex, float advanceWidth, float minX, float minY, float maxX, float maxY) {
        this.codePoint = codePoint;
        this.atlasIndex = atlasIndex;
        this.advanceWidth = advanceWidth;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public int getCodePoint() {
        return this.codePoint;
    }

    public int getAtlasIndex() {
        return this.atlasIndex;
    }

    public float getAdvanceWidth() {
        return this.advanceWidth;
    }

    public float getMinX() {
        return this.minX;
    }

    public float getMinY() {
        return this.minY;
    }

    public float getMaxX() {
        return this.maxX;
    }

    public float getMaxY() {
        return this.maxY;
    }

    public boolean isWhitespace() {
        return this.atlasIndex < 0;
    }

    public float getScaledAdvance(float scale) {
        return this.advanceWidth * scale;
    }

    public static float getMatrixScale(Matrix4f matrix) {
        float determinant = matrix.m00() * matrix.m11() - matrix.m01() * matrix.m10();
        float scale = (float)Math.sqrt(Math.abs(determinant));
        return scale > 1.0E-4f ? scale : MINIMUM_GLYPH_SCALE;
    }

    /** Appends a glyph quad using baseline coordinates. */
    public float appendQuad(Matrix4f matrix, VertexConsumer vertices, float scale, float x, float baselineY, float z, int color) {
        return this.appendQuad(matrix, vertices, scale, x, baselineY, z, color, MINIMUM_GLYPH_SCALE);
    }

    /** Appends a glyph quad and returns its advance in the requested scale. */
    public float appendQuad(Matrix4f matrix, VertexConsumer vertices, float scale, float x, float baselineY, float z, int color, float edgePadding) {
        if (!this.isWhitespace()) {
            float padding = Math.max(MINIMUM_GLYPH_SCALE, edgePadding) / (scale * getMatrixScale(matrix));
            float left = this.minX - padding;
            float right = this.maxX + padding;
            float top = this.maxY + padding;
            float bottom = this.minY - padding;
            float x1 = x + left * scale;
            float x2 = x + right * scale;
            float y1 = baselineY - top * scale;
            float y2 = baselineY - bottom * scale;
            int packedIndex = getPackedAtlasIndex();
            vertices.vertex(matrix, x1, y1, z).texture(left, top).color(color).light(packedIndex);
            vertices.vertex(matrix, x1, y2, z).texture(left, bottom).color(color).light(packedIndex);
            vertices.vertex(matrix, x2, y2, z).texture(right, bottom).color(color).light(packedIndex);
            vertices.vertex(matrix, x2, y1, z).texture(right, top).color(color).light(packedIndex);
        }
        return this.advanceWidth * scale;
    }

    /** Appends a UI quad whose y coordinate is the top of the em square. */
    public void appendUiQuad(Matrix4f matrix, VertexConsumer vertices, float x, float topY, float scale, int color) {
        if (this.isWhitespace()) {
            return;
        }
        float padding = MINIMUM_GLYPH_SCALE / (scale * getMatrixScale(matrix));
        float left = this.minX - padding;
        float right = this.maxX + padding;
        float top = this.maxY + padding;
        float bottom = this.minY - padding;
        float x1 = x + left * scale;
        float x2 = x + right * scale;
        float y1 = topY + (1.0f - top) * scale;
        float y2 = topY + (1.0f - bottom) * scale;
        int packedIndex = getPackedAtlasIndex();
        vertices.vertex(matrix, x1, y1, 0.0f).texture(left, top).color(color).light(packedIndex);
        vertices.vertex(matrix, x1, y2, 0.0f).texture(left, bottom).color(color).light(packedIndex);
        vertices.vertex(matrix, x2, y2, 0.0f).texture(right, bottom).color(color).light(packedIndex);
        vertices.vertex(matrix, x2, y1, 0.0f).texture(right, top).color(color).light(packedIndex);
    }

    public int getPackedAtlasIndex() {
        int encoded = this.atlasIndex + 1;
        return (encoded & Short.MAX_VALUE) | (encoded >> 15 << 16);
    }
}
