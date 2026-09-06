/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.render.text.glyph;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.render.text.glyph.GlyphOutline;

public final class GlyphSpatialIndex {
    private static final int SEGMENTS_PER_CELL = 4;
    private static final int MAX_GRID_CELLS = 32;
    public final GlyphOutline glyphOutline;
    public final int xCellCount;
    public final int yCellCount;
    public final int[][] yBuckets;
    public final int[][] xBuckets;
    public final float xScale;
    public final float yScale;
    public final float xOffset;
    public final float yOffset;

    private GlyphSpatialIndex(GlyphOutline glyphOutline, int n, int n2, int[][] nArray, int[][] nArray2, float f, float f2, float f3, float f4) {
        this.glyphOutline = glyphOutline;
        this.xCellCount = n;
        this.yCellCount = n2;
        this.yBuckets = nArray;
        this.xBuckets = nArray2;
        this.xScale = f;
        this.yScale = f2;
        this.xOffset = f3;
        this.yOffset = f4;
    }

    public int getEstimatedStorageSize() {
        int n = this.yCellCount + this.xCellCount;
        for (int[] nArray : this.yBuckets) {
            n += nArray.length;
        }
        for (int[] nArray : this.xBuckets) {
            n += nArray.length;
        }
        return n;
    }

    public static GlyphSpatialIndex fromGlyphOutline(GlyphOutline glyphOutline) {
        int n = glyphOutline.segmentCount;
        float f = Math.max(glyphOutline.getWidth(), 1.0E-6f);
        float f2 = Math.max(glyphOutline.getHeight(), 1.0E-6f);
        float f3 = Math.max(f, f2);
        int n2 = Math.max(1, Math.min(32, (n + 4 - 1) / 4));
        int n3 = Math.max(1, Math.min(32, Math.round((float)n2 * f / f3)));
        int n4 = Math.max(1, Math.min(32, Math.round((float)n2 * f2 / f3)));
        ArrayList<List<Integer>> arrayList = new ArrayList<List<Integer>>(n4);
        for (int i = 0; i < n4; ++i) {
            arrayList.add(new ArrayList());
        }
        ArrayList<List<Integer>> arrayList2 = new ArrayList<List<Integer>>(n3);
        for (int i = 0; i < n3; ++i) {
            arrayList2.add(new ArrayList());
        }
        float f4 = (float)n3 / f;
        float f5 = (float)n4 / f2;
        for (int i = 0; i < n; ++i) {
            int n5;
            int n6;
            int n7;
            int n8 = i * 6;
            float f6 = glyphOutline.SEGMENTS[n8];
            float f7 = glyphOutline.SEGMENTS[n8 + 1];
            float f8 = glyphOutline.SEGMENTS[n8 + 2];
            float f9 = glyphOutline.SEGMENTS[n8 + 3];
            float f10 = glyphOutline.SEGMENTS[n8 + 4];
            float f11 = glyphOutline.SEGMENTS[n8 + 5];
            float f12 = Math.min(f6, Math.min(f8, f10));
            float f13 = Math.max(f6, Math.max(f8, f10));
            float f14 = Math.min(f7, Math.min(f9, f11));
            float f15 = Math.max(f7, Math.max(f9, f11));
            if (f7 != f9 || f9 != f11) {
                n7 = GlyphSpatialIndex.clampCellIndex((f14 - glyphOutline.minY) * f5, n4);
                n6 = GlyphSpatialIndex.clampCellIndex((f15 - glyphOutline.minY) * f5, n4);
                for (n5 = n7; n5 <= n6; ++n5) {
                    ((List)arrayList.get(n5)).add(i);
                }
            }
            if (f6 == f8 && f8 == f10) continue;
            n7 = GlyphSpatialIndex.clampCellIndex((f12 - glyphOutline.minX) * f4, n3);
            n6 = GlyphSpatialIndex.clampCellIndex((f13 - glyphOutline.minX) * f4, n3);
            for (n5 = n7; n5 <= n6; ++n5) {
                ((List)arrayList2.get(n5)).add(i);
            }
        }
        return new GlyphSpatialIndex(glyphOutline, n3, n4, GlyphSpatialIndex.createBuckets(arrayList, glyphOutline, true), GlyphSpatialIndex.createBuckets(arrayList2, glyphOutline, false), f4, f5, -glyphOutline.minX * f4, -glyphOutline.minY * f5);
    }

    private static int clampCellIndex(float f, int n) {
        return Math.max(0, Math.min(n - 1, (int)f));
    }

    private static int[][] createBuckets(List<List<Integer>> list, GlyphOutline glyphOutline, boolean bl) {
        int[][] nArrayArray = new int[list.size()][];
        for (int i = 0; i < list.size(); ++i) {
            List<Integer> list2 = list.get(i);
            list2.sort((n, n2) -> Float.compare(GlyphSpatialIndex.getSegmentMaxCoordinate(glyphOutline, n2, bl), GlyphSpatialIndex.getSegmentMaxCoordinate(glyphOutline, n, bl)));
            int[] nArray = new int[list2.size()];
            for (int j = 0; j < nArray.length; ++j) {
                nArray[j] = list2.get(j);
            }
            nArrayArray[i] = nArray;
        }
        return nArrayArray;
    }

    private static float getSegmentMaxCoordinate(GlyphOutline glyphOutline, int n, boolean bl) {
        int n2 = n * 6 + (bl ? 0 : 1);
        return Math.max(glyphOutline.SEGMENTS[n2], Math.max(glyphOutline.SEGMENTS[n2 + 2], glyphOutline.SEGMENTS[n2 + 4]));
    }
}

