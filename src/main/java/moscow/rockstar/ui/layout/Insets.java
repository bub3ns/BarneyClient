/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.layout;

public final class Insets {
    public static final Insets NONE = new Insets(0.0f, 0.0f, 0.0f, 0.0f);
    public final float top;
    public final float right;
    public final float bottom;
    public final float left;

    private Insets(float f, float f2, float f3, float f4) {
        this.top = Math.max(0.0f, f);
        this.right = Math.max(0.0f, f2);
        this.bottom = Math.max(0.0f, f3);
        this.left = Math.max(0.0f, f4);
    }

    public static Insets uniform(float f) {
        return f <= 0.0f ? NONE : new Insets(f, f, f, f);
    }

    public static Insets of(float f, float f2, float f3, float f4) {
        return new Insets(f, f2, f3, f4);
    }

    public static Insets symmetric(float f, float f2) {
        return new Insets(f, f2, f, f2);
    }

    public static Insets horizontal(float f) {
        return new Insets(0.0f, f, 0.0f, f);
    }

    public static Insets vertical(float f) {
        return new Insets(f, 0.0f, f, 0.0f);
    }

    public float horizontalSize() {
        return this.left + this.right;
    }

    public float verticalSize() {
        return this.top + this.bottom;
    }
}

