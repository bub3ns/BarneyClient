/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.render.text.glyph;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

public final class GlyphOutline {
    public static final float GLYPH_SCALE = 2048.0f;
    private static final double FLATNESS_TOLERANCE = 2.44140625E-4;
    public final float[] SEGMENTS;
    public final int segmentCount;
    public final float minX;
    public final float minY;
    public final float maxX;
    public final float maxY;
    public final float advanceWidth;

    private GlyphOutline(float[] fArray, int n, float f, float f2, float f3, float f4, float f5) {
        this.SEGMENTS = fArray;
        this.segmentCount = n;
        this.minX = f;
        this.minY = f2;
        this.maxX = f3;
        this.maxY = f4;
        this.advanceWidth = f5;
    }

    public boolean isEmpty() {
        return this.segmentCount == 0;
    }

    public float getWidth() {
        return this.maxX - this.minX;
    }

    public float getHeight() {
        return this.maxY - this.minY;
    }

    public static GlyphOutline fromGlyph(Font font, FontRenderContext fontRenderContext, int n) {
        Font font2 = font.getSize2D() == 2048.0f ? font : font.deriveFont(2048.0f);
        GlyphVector glyphVector = font2.createGlyphVector(fontRenderContext, new String(Character.toChars(n)));
        float f = glyphVector.getGlyphMetrics(0).getAdvanceX() / 2048.0f;
        return GlyphOutline.fromShape(glyphVector.getOutline(), f);
    }

    public static GlyphOutline fromShape(Shape shape, float f) {
        ArrayList<double[]> arrayList = new ArrayList<double[]>();
        PathIterator pathIterator = shape.getPathIterator(null);
        double[] dArray = new double[6];
        double d = 0.0;
        double d2 = 0.0;
        double d3 = 0.0;
        double d4 = 0.0;
        while (!pathIterator.isDone()) {
            switch (pathIterator.currentSegment(dArray)) {
                case 0: {
                    GlyphOutline.appendLineIfDistinct(arrayList, d3, d4, d, d2);
                    d = d3 = dArray[0] / 2048.0;
                    d2 = d4 = -dArray[1] / 2048.0;
                    break;
                }
                case 1: {
                    double d5 = dArray[0] / 2048.0;
                    double d6 = -dArray[1] / 2048.0;
                    GlyphOutline.appendLineSegment(arrayList, d3, d4, d5, d6);
                    d3 = d5;
                    d4 = d6;
                    break;
                }
                case 2: {
                    double d5 = dArray[0] / 2048.0;
                    double d6 = -dArray[1] / 2048.0;
                    double d7 = dArray[2] / 2048.0;
                    double d8 = -dArray[3] / 2048.0;
                    GlyphOutline.appendQuadraticSegment(arrayList, d3, d4, d5, d6, d7, d8);
                    d3 = d7;
                    d4 = d8;
                    break;
                }
                case 3: {
                    double d5 = dArray[0] / 2048.0;
                    double d6 = -dArray[1] / 2048.0;
                    double d7 = dArray[2] / 2048.0;
                    double d8 = -dArray[3] / 2048.0;
                    double d9 = dArray[4] / 2048.0;
                    double d10 = -dArray[5] / 2048.0;
                    GlyphOutline.appendCubicSegment(arrayList, d3, d4, d5, d6, d7, d8, d9, d10);
                    d3 = d9;
                    d4 = d10;
                    break;
                }
                case 4: {
                    GlyphOutline.appendLineIfDistinct(arrayList, d3, d4, d, d2);
                    d3 = d;
                    d4 = d2;
                    break;
                }
            }
            pathIterator.next();
        }
        GlyphOutline.appendLineIfDistinct(arrayList, d3, d4, d, d2);
        float[] fArray = new float[arrayList.size() * 6];
        float f2 = Float.MAX_VALUE;
        float f3 = Float.MAX_VALUE;
        float f4 = -3.4028235E38f;
        float f5 = -3.4028235E38f;
        for (int i = 0; i < arrayList.size(); ++i) {
            int n;
            double[] dArray2 = (double[])arrayList.get(i);
            for (n = 0; n < 6; ++n) {
                fArray[i * 6 + n] = (float)dArray2[n];
            }
            for (n = 0; n < 6; n += 2) {
                f2 = Math.min(f2, fArray[i * 6 + n]);
                f4 = Math.max(f4, fArray[i * 6 + n]);
                f3 = Math.min(f3, fArray[i * 6 + n + 1]);
                f5 = Math.max(f5, fArray[i * 6 + n + 1]);
            }
        }
        if (arrayList.isEmpty()) {
            f5 = 0.0f;
            f4 = 0.0f;
            f3 = 0.0f;
            f2 = 0.0f;
        }
        return new GlyphOutline(fArray, arrayList.size(), f2, f3, f4, f5, f);
    }

    private static void appendLineIfDistinct(List<double[]> list, double d, double d2, double d3, double d4) {
        if (d != d3 || d2 != d4) {
            GlyphOutline.appendLineSegment(list, d, d2, d3, d4);
        }
    }

    private static void appendLineSegment(List<double[]> list, double d, double d2, double d3, double d4) {
        if (d == d3 && d2 == d4) {
            return;
        }
        list.add(new double[]{d, d2, (d + d3) * 0.5, (d2 + d4) * 0.5, d3, d4});
    }

    private static void appendQuadraticSegment(List<double[]> list, double d, double d2, double d3, double d4, double d5, double d6) {
        if (d == d5 && d2 == d6 && d == d3 && d2 == d4) {
            return;
        }
        list.add(new double[]{d, d2, d3, d4, d5, d6});
    }

    private static void appendCubicSegment(List<double[]> list, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8) {
        double d9 = d7 - 3.0 * d5 + 3.0 * d3 - d;
        double d10 = d8 - 3.0 * d6 + 3.0 * d4 - d2;
        double d11 = Math.sqrt(d9 * d9 + d10 * d10) * Math.sqrt(3.0) / 18.0;
        int n = d11 <= 2.44140625E-4 ? 1 : (int)Math.ceil(Math.cbrt(d11 / 2.44140625E-4));
        n = Math.max(1, Math.min(n, 16));
        double d12 = d;
        double d13 = d2;
        for (int i = 1; i <= n; ++i) {
            double d14 = ((double)i - 1.0) / (double)n;
            double d15 = (double)i / (double)n;
            double d16 = GlyphOutline.evaluateCubicPoint(d, d3, d5, d7, d15);
            double d17 = GlyphOutline.evaluateCubicPoint(d2, d4, d6, d8, d15);
            double d18 = (d15 - d14) * 0.5;
            double d19 = d12 + GlyphOutline.evaluateCubicDerivative(d, d3, d5, d7, d14) * d18;
            double d20 = d13 + GlyphOutline.evaluateCubicDerivative(d2, d4, d6, d8, d14) * d18;
            GlyphOutline.appendQuadraticSegment(list, d12, d13, d19, d20, d16, d17);
            d12 = d16;
            d13 = d17;
        }
    }

    private static double evaluateCubicPoint(double d, double d2, double d3, double d4, double d5) {
        double d6 = 1.0 - d5;
        return d6 * d6 * d6 * d + 3.0 * d6 * d6 * d5 * d2 + 3.0 * d6 * d5 * d5 * d3 + d5 * d5 * d5 * d4;
    }

    private static double evaluateCubicDerivative(double d, double d2, double d3, double d4, double d5) {
        double d6 = 1.0 - d5;
        return 3.0 * (d6 * d6 * (d2 - d) + 2.0 * d6 * d5 * (d3 - d2) + d5 * d5 * (d4 - d3));
    }
}

