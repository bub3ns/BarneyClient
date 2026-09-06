/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.core;

public record WidgetState(float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
    public static final WidgetState NONE = new WidgetState(0.0f, 0.0f, 0.0f, 0.0f);

    public static WidgetState uniform(float f) {
        return new WidgetState(f, f, f, f);
    }

    public static WidgetState topLeft(float f) {
        return new WidgetState(f, 0.0f, 0.0f, 0.0f);
    }

    public static WidgetState topRight(float f) {
        return new WidgetState(0.0f, f, 0.0f, 0.0f);
    }

    public static WidgetState bottomRight(float f) {
        return new WidgetState(0.0f, 0.0f, f, 0.0f);
    }

    public static WidgetState bottomLeft(float f) {
        return new WidgetState(0.0f, 0.0f, 0.0f, f);
    }

    public static WidgetState top(float f, float f2) {
        return new WidgetState(f, f2, 0.0f, 0.0f);
    }

    public static WidgetState bottom(float f, float f2) {
        return new WidgetState(0.0f, 0.0f, f2, f);
    }

    public static WidgetState left(float f, float f2) {
        return new WidgetState(f, 0.0f, 0.0f, f2);
    }

    public static WidgetState right(float f, float f2) {
        return new WidgetState(0.0f, f, f2, 0.0f);
    }

    @Override
    public String toString() {
        return "BorderRadius{topLeftRadius=" + this.topLeftRadius + ", topRightRadius=" + this.topRightRadius + ", bottomRightRadius=" + this.bottomRightRadius + ", bottomLeftRadius=" + this.bottomLeftRadius + "}";
    }
}

