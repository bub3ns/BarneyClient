package moscow.rockstar.render.text.glyph;

import java.util.Arrays;

/** CPU-side packed data consumed by the Slug glyph shader. */
final class GlyphAtlasBuffer {
    static final int CURVE_TEXTURE_WIDTH = 4096;
    static final int BAND_TEXTURE_WIDTH = 4096;
    static final int GLYPH_TABLE_WIDTH = 2048;
    /** The original table stores two glyph records per four-float texel. */
    private static final int GLYPH_RECORDS_PER_ROW = 1024;
    private static final int INITIAL_TEXTURE_HEIGHT = 16;
    private static final int GLYPH_RECORD_FLOATS = 8;

    private float[] curveData = new float[CURVE_TEXTURE_WIDTH * INITIAL_TEXTURE_HEIGHT * 4];
    private int[] bandData = new int[BAND_TEXTURE_WIDTH * INITIAL_TEXTURE_HEIGHT * 2];
    private float[] glyphTable = new float[GLYPH_TABLE_WIDTH * INITIAL_TEXTURE_HEIGHT * 4];

    private int curveTextureHeight = INITIAL_TEXTURE_HEIGHT;
    private int bandTextureHeight = INITIAL_TEXTURE_HEIGHT;
    private int glyphTableTextureHeight = INITIAL_TEXTURE_HEIGHT;

    private int curveColumn;
    private int curveRow;
    private int bandColumn;
    private int bandRow;
    private int glyphCount;
    private int usedCurveHeight;
    private int usedBandHeight;
    private int usedGlyphTableHeight;

    private boolean curveDataDirty;
    private boolean bandDataDirty;
    private boolean glyphTableDirty;

    int getGlyphCount() {
        return this.glyphCount;
    }

    float[] getCurveData() {
        return this.curveData;
    }

    int[] getBandData() {
        return this.bandData;
    }

    float[] getGlyphTable() {
        return this.glyphTable;
    }

    int getCurveTextureHeight() {
        return this.curveTextureHeight;
    }

    int getBandTextureHeight() {
        return this.bandTextureHeight;
    }

    int getGlyphTableTextureHeight() {
        return this.glyphTableTextureHeight;
    }

    int getUsedCurveHeight() {
        return this.usedCurveHeight;
    }

    int getUsedBandHeight() {
        return this.usedBandHeight;
    }

    int getUsedGlyphTableHeight() {
        return this.usedGlyphTableHeight;
    }

    boolean isCurveDataDirty() {
        return this.curveDataDirty;
    }

    boolean isBandDataDirty() {
        return this.bandDataDirty;
    }

    boolean isGlyphTableDirty() {
        return this.glyphTableDirty;
    }

    void clearDirtyFlags() {
        this.curveDataDirty = false;
        this.bandDataDirty = false;
        this.glyphTableDirty = false;
    }

    void clear() {
        this.curveColumn = 0;
        this.curveRow = 0;
        this.bandColumn = 0;
        this.bandRow = 0;
        this.glyphCount = 0;
        this.usedCurveHeight = 0;
        this.usedBandHeight = 0;
        this.usedGlyphTableHeight = 0;
        this.curveDataDirty = false;
        this.bandDataDirty = false;
        this.glyphTableDirty = false;
    }

    int addGlyph(GlyphSpatialIndex spatialIndex) {
        GlyphOutline outline = spatialIndex.glyphOutline;
        int segmentCount = outline.segmentCount;
        int[] curveColumns = new int[segmentCount];
        int[] curveRows = new int[segmentCount];
        for (int segment = 0; segment < segmentCount; segment++) {
            if (this.curveColumn + 2 > CURVE_TEXTURE_WIDTH) {
                this.curveColumn = 0;
                this.curveRow++;
            }
            this.ensureCurveRows(this.curveRow + 1);
            curveColumns[segment] = this.curveColumn;
            curveRows[segment] = this.curveRow;
            int target = (this.curveRow * CURVE_TEXTURE_WIDTH + this.curveColumn) * 4;
            int source = segment * 6;
            System.arraycopy(outline.SEGMENTS, source, this.curveData, target, 6);
            this.curveData[target + 6] = 0.0f;
            this.curveData[target + 7] = 0.0f;
            this.usedCurveHeight = Math.max(this.usedCurveHeight, this.curveRow + 1);
            this.curveColumn += 2;
        }

        int bucketCount = spatialIndex.yCellCount + spatialIndex.xCellCount;
        if (this.bandColumn + bucketCount > BAND_TEXTURE_WIDTH) {
            this.bandColumn = 0;
            this.bandRow++;
        }
        this.ensureBandRows(this.bandRow + (this.bandColumn + spatialIndex.getEstimatedStorageSize()) / BAND_TEXTURE_WIDTH + 1);
        int glyphBandColumn = this.bandColumn;
        int glyphBandRow = this.bandRow;
        int nextBandOffset = bucketCount;
        int bucketNumber = 0;
        for (int[] bucket : spatialIndex.yBuckets) {
            nextBandOffset = writeBucket(glyphBandColumn, glyphBandRow, bucketNumber++, bucket, nextBandOffset, curveColumns, curveRows);
        }
        for (int[] bucket : spatialIndex.xBuckets) {
            nextBandOffset = writeBucket(glyphBandColumn, glyphBandRow, bucketNumber++, bucket, nextBandOffset, curveColumns, curveRows);
        }
        int endBandOffset = glyphBandColumn + nextBandOffset;
        this.bandColumn = endBandOffset & (BAND_TEXTURE_WIDTH - 1);
        this.bandRow = glyphBandRow + (endBandOffset >> 12);
        this.usedBandHeight = Math.max(this.usedBandHeight, this.bandRow + 1);

        int glyphIndex = this.glyphCount++;
        this.ensureGlyphTableRows(glyphIndex / GLYPH_RECORDS_PER_ROW + 1);
        int tableOffset = glyphIndex * GLYPH_RECORD_FLOATS;
        this.glyphTable[tableOffset] = spatialIndex.xScale;
        this.glyphTable[tableOffset + 1] = spatialIndex.yScale;
        this.glyphTable[tableOffset + 2] = spatialIndex.xOffset;
        this.glyphTable[tableOffset + 3] = spatialIndex.yOffset;
        this.glyphTable[tableOffset + 4] = glyphBandColumn;
        this.glyphTable[tableOffset + 5] = glyphBandRow;
        this.glyphTable[tableOffset + 6] = spatialIndex.xCellCount - 1;
        this.glyphTable[tableOffset + 7] = spatialIndex.yCellCount - 1;
        this.usedGlyphTableHeight = Math.max(this.usedGlyphTableHeight, glyphIndex / GLYPH_RECORDS_PER_ROW + 1);
        this.curveDataDirty = true;
        this.bandDataDirty = true;
        this.glyphTableDirty = true;
        return glyphIndex;
    }

    private int writeBucket(int glyphX, int glyphY, int bucketNumber, int[] segmentIndices, int offset, int[] curveColumns, int[] curveRows) {
        int localX = (glyphX + offset) & (BAND_TEXTURE_WIDTH - 1);
        if (localX + segmentIndices.length > BAND_TEXTURE_WIDTH) {
            offset += BAND_TEXTURE_WIDTH - localX;
        }
        writeBandCell(glyphX, glyphY, bucketNumber, segmentIndices.length, offset);
        for (int index = 0; index < segmentIndices.length; index++) {
            int segment = segmentIndices[index];
            writeBandCell(glyphX, glyphY, offset + index, curveColumns[segment], curveRows[segment]);
        }
        return offset + segmentIndices.length;
    }

    private void writeBandCell(int glyphX, int glyphY, int offset, int first, int second) {
        int absoluteX = glyphX + offset;
        int row = glyphY + (absoluteX >> 12);
        int column = absoluteX & (BAND_TEXTURE_WIDTH - 1);
        this.ensureBandRows(row + 1);
        int target = (row * BAND_TEXTURE_WIDTH + column) * 2;
        this.bandData[target] = first;
        this.bandData[target + 1] = second;
        this.usedBandHeight = Math.max(this.usedBandHeight, row + 1);
    }

    private void ensureCurveRows(int rows) {
        if (rows <= this.curveTextureHeight) {
            return;
        }
        int nextHeight = growPowerOfTwo(this.curveTextureHeight, rows);
        this.curveData = Arrays.copyOf(this.curveData, CURVE_TEXTURE_WIDTH * nextHeight * 4);
        this.curveTextureHeight = nextHeight;
        this.curveDataDirty = true;
    }

    private void ensureBandRows(int rows) {
        if (rows <= this.bandTextureHeight) {
            return;
        }
        int nextHeight = growPowerOfTwo(this.bandTextureHeight, rows);
        this.bandData = Arrays.copyOf(this.bandData, BAND_TEXTURE_WIDTH * nextHeight * 2);
        this.bandTextureHeight = nextHeight;
        this.bandDataDirty = true;
    }

    private void ensureGlyphTableRows(int rows) {
        if (rows <= this.glyphTableTextureHeight) {
            return;
        }
        int nextHeight = growPowerOfTwo(this.glyphTableTextureHeight, rows);
        this.glyphTable = Arrays.copyOf(this.glyphTable, GLYPH_TABLE_WIDTH * nextHeight * 4);
        this.glyphTableTextureHeight = nextHeight;
        this.glyphTableDirty = true;
    }

    private static int growPowerOfTwo(int current, int required) {
        int next = current;
        while (next < required) {
            next *= 2;
        }
        return next;
    }
}
