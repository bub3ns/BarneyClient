/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.utility.render;

import lombok.Generated;
import moscow.rockstar.ui.core.UiUtils;

public class Rect {
    public static final Rect EMPTY = new Rect(0.0f, 0.0f, 0.0f, 0.0f);
    protected float x;
    protected float y;
    protected float width;
    protected float height;

    public void set(float f, float f2, float f3, float f4) {
        this.x = f;
        this.y = f2;
        this.width = f3;
        this.height = f4;
    }

    public Rect x(float f) {
        return new Rect(f, this.y, this.width, this.height);
    }

    public Rect y(float f) {
        return new Rect(this.x, f, this.width, this.height);
    }

    public Rect width(float f) {
        return new Rect(this.x, this.y, f, this.height);
    }

    public Rect height(float f) {
        return new Rect(this.x, this.y, this.width, f);
    }

    public Rect size(float f) {
        return new Rect(this.x + f, this.y + f, this.width - f * 2.0f, this.height - f * 2.0f);
    }

    public static Rect interpolate(Rect rect, Rect rect2, double d) {
        float f = (float)((double)rect.x + (double)(rect2.x - rect.x) * d);
        float f2 = (float)((double)rect.y + (double)(rect2.y - rect.y) * d);
        float f3 = (float)((double)rect.width + (double)(rect2.width - rect.width) * d);
        float f4 = (float)((double)rect.height + (double)(rect2.height - rect.height) * d);
        return new Rect(f, f2, f3, f4);
    }

    public boolean contains(Rect rect) {
        return this.contains(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public boolean contains(float f, float f2, float f3, float f4) {
        return this.x + this.width > f && this.x < f + f3 && this.y + this.height > f2 && this.y < f2 + f4;
    }

    public boolean inside(Rect rect) {
        return this.inside(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public boolean inside(float f, float f2, float f3, float f4) {
        return this.x > f && this.x + this.width < f + f3 && this.y > f2 && this.y + this.height < f2 + f4;
    }

    public boolean hovered(double d, double d2) {
        return UiUtils.contains((double)this.x, (double)this.y, (double)this.width, (double)this.height, d, d2);
    }

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getY() {
        return this.y;
    }

    @Generated
    public float getWidth() {
        return this.width;
    }

    @Generated
    public float getHeight() {
        return this.height;
    }

    @Generated
    public void setX(float f) {
        this.x = f;
    }

    @Generated
    public void setY(float f) {
        this.y = f;
    }

    @Generated
    public void setWidth(float f) {
        this.width = f;
    }

    @Generated
    public void setHeight(float f) {
        this.height = f;
    }

    @Generated
    public Rect() {
    }

    @Generated
    public Rect(float f, float f2, float f3, float f4) {
        this.x = f;
        this.y = f2;
        this.width = f3;
        this.height = f4;
    }
}

