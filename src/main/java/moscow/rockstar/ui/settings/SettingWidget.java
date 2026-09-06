/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.settings;

import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.input.PointerAction;
import pyrock.utility.render.Rect;

public abstract class SettingWidget
implements ClientAccess {
    protected float x;
    protected float y;
    protected float width;
    protected float height;

    protected SettingWidget() {
        this(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public void render(RockstarDrawContext drawContext) {
        this.renderOverlay(drawContext);
        this.renderContent(drawContext);
    }

    protected abstract void renderContent(RockstarDrawContext var1);

    public void tick() {
    }

    public void renderOverlay(RockstarDrawContext drawContext) {
    }

    public void mouseClicked(double d, double d2, PointerAction pointerAction) {
    }

    public void mouseReleased(double d, double d2, PointerAction pointerAction) {
    }

    public void keyPressed(int n, int n2, int n3) {
    }

    public boolean charTyped(char c, int n) {
        return false;
    }

    public void mouseScrolled(double d, double d2, double d3, double d4) {
    }

    public void setPosition(float f, float f2) {
        this.x = f;
        this.y = f2;
    }

    public void setBounds(float f, float f2, float f3, float f4) {
        this.x = f;
        this.y = f2;
        this.width = f3;
        this.height = f4;
    }

    public void setBounds(Rect rect) {
        this.x = rect.getX();
        this.y = rect.getY();
        this.width = rect.getWidth();
        this.height = rect.getHeight();
    }

    public boolean contains(float f, float f2) {
        return UiUtils.contains((double)this.x, (double)this.y, (double)this.width, (double)this.height, f, f2);
    }

    public boolean contains(double d, double d2) {
        return UiUtils.contains((double)this.x, (double)this.y, (double)this.width, (double)this.height, d, d2);
    }

    public boolean contains(RockstarDrawContext drawContext) {
        return this.contains(drawContext.mouseX(), drawContext.mouseY());
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
    protected SettingWidget(float f, float f2, float f3, float f4) {
        this.x = f;
        this.y = f2;
        this.width = f3;
        this.height = f4;
    }
}
