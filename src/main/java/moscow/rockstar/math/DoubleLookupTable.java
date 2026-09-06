/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.math;

public final class DoubleLookupTable {
    public static final double NO_SAMPLE = -4096.0;
    private final double[] samples;
    private final double sampleSpacing;
    private int sampleCount;

    public DoubleLookupTable(int n, double d) {
        this.samples = new double[n];
        this.sampleSpacing = d;
    }

    public void clear() {
        this.sampleCount = 0;
    }

    public void addSample(double d) {
        if (this.sampleCount < this.samples.length) {
            this.samples[this.sampleCount++] = d;
        }
    }

    public int getCapacity() {
        return this.samples.length;
    }

    public double getSampleSpacing() {
        return this.sampleSpacing;
    }

    public double getLowerSample(double d) {
        if (this.sampleCount == 0) {
            return -4096.0;
        }
        int n = this.findSampleIndex(d);
        if (n < 0) {
            return this.samples[0];
        }
        if (n >= this.sampleCount - 1) {
            return this.samples[this.sampleCount - 1];
        }
        return Math.min(this.samples[n], this.samples[n + 1]);
    }

    public double getUpperSample(double d) {
        if (this.sampleCount == 0) {
            return -4096.0;
        }
        int n = this.findSampleIndex(d);
        if (n < 0) {
            return this.samples[0];
        }
        if (n >= this.sampleCount - 1) {
            return this.samples[this.sampleCount - 1];
        }
        return Math.max(this.samples[n], this.samples[n + 1]);
    }

    private int findSampleIndex(double d) {
        double d2 = d / this.sampleSpacing;
        return d2 <= 0.0 ? -1 : (int)d2;
    }
}

