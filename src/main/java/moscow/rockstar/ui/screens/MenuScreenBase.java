/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.screens;

import lombok.Generated;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.color.ColorPickerHost;

public abstract class MenuScreenBase
extends ColorPickerHost {
    protected final Animation menuAnimation = new Animation(500L, Easing.linear);
    protected boolean closing = true;

    @Generated
    public Animation getMenuAnimation() {
        return this.menuAnimation;
    }

    @Generated
    public boolean isClosing() {
        return this.closing;
    }

    @Generated
    public void setClosing(boolean bl) {
        this.closing = bl;
    }
}
