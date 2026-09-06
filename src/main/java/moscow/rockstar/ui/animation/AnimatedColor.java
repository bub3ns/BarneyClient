/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.NonNull
 */
package moscow.rockstar.ui.animation;

import lombok.NonNull;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import pyrock.utility.render.ColorRGBA;

public class AnimatedColor {
    private static final Easing DEFAULT_EASING = Easing.easeInOutCubicPolynomial;
    private final long durationMillis;
    private final Animation redAnimation;
    private final Animation greenAnimation;
    private final Animation blueAnimation;
    private final Animation alphaAnimation;

    public AnimatedColor(long l, Easing easing) {
        this.durationMillis = l;
        this.redAnimation = new Animation(l, easing);
        this.greenAnimation = new Animation(l, easing);
        this.blueAnimation = new Animation(l, easing);
        this.alphaAnimation = new Animation(l, easing);
    }

    public AnimatedColor(long l) {
        this(l, DEFAULT_EASING);
    }

    public AnimatedColor(long l, ColorRGBA colorRGBA, Easing easing) {
        this.durationMillis = l;
        this.redAnimation = new Animation(l, colorRGBA.getRed(), easing);
        this.greenAnimation = new Animation(l, colorRGBA.getGreen(), easing);
        this.blueAnimation = new Animation(l, colorRGBA.getBlue(), easing);
        this.alphaAnimation = new Animation(l, colorRGBA.getAlpha(), easing);
    }

    public AnimatedColor(long l, ColorRGBA colorRGBA) {
        this(l, colorRGBA, DEFAULT_EASING);
    }

    public void setTargetColor(@NonNull ColorRGBA colorRGBA) {
        if (colorRGBA == null) {
            throw new NullPointerException("targetColor is marked non-null but is null");
        }
        this.redAnimation.update(colorRGBA.getRed());
        this.greenAnimation.update(colorRGBA.getGreen());
        this.blueAnimation.update(colorRGBA.getBlue());
        this.alphaAnimation.update(colorRGBA.getAlpha());
    }

    public ColorRGBA getColor() {
        return new ColorRGBA((int)this.redAnimation.getValue(), (int)this.greenAnimation.getValue(), (int)this.blueAnimation.getValue(), (int)this.alphaAnimation.getValue());
    }

    public void setEasing(Easing easing) {
        this.redAnimation.setEasing(easing);
        this.greenAnimation.setEasing(easing);
        this.blueAnimation.setEasing(easing);
        this.alphaAnimation.setEasing(easing);
    }

    public void setDuration(long l) {
        this.redAnimation.setDuration(l);
        this.greenAnimation.setDuration(l);
        this.blueAnimation.setDuration(l);
        this.alphaAnimation.setDuration(l);
    }

    public void setCurrentColor(@NonNull ColorRGBA colorRGBA) {
        if (colorRGBA == null) {
            throw new NullPointerException("color is marked non-null but is null");
        }
        this.redAnimation.setValue(colorRGBA.getRed());
        this.greenAnimation.setValue(colorRGBA.getGreen());
        this.blueAnimation.setValue(colorRGBA.getBlue());
        this.alphaAnimation.setValue(colorRGBA.getAlpha());
    }
}

