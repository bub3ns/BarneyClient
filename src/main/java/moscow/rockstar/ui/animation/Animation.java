/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.animation;

import lombok.Generated;
import moscow.rockstar.ui.animation.Easing;

public class Animation {
    private long duration;
    private float currentValue;
    private Easing easing;
    private long lastChangeTime;
    private float startValue;
    private float targetValue;
    private boolean atTarget;
    private boolean reverse;

    public Animation(long l, float f, Easing easing) {
        this.duration = l;
        this.easing = easing;
        this.currentValue = f;
        this.startValue = f;
        this.targetValue = f;
        this.atTarget = true;
    }

    public Animation(long l, Easing easing) {
        this(l, 0.0f, easing);
    }

    public void setReverse(boolean bl) {
        this.update(bl ? 1.0f : 0.0f);
    }

    public float update(float f) {
        long l;
        long l2 = System.currentTimeMillis();
        if (f != this.targetValue) {
            this.startValue = this.currentValue;
            this.targetValue = f;
            this.lastChangeTime = l2;
            this.atTarget = false;
        }
        if ((l = l2 - this.lastChangeTime) >= this.duration) {
            this.currentValue = this.targetValue;
            this.atTarget = true;
            return this.currentValue;
        }
        float f2 = (float)l / (float)this.duration;
        float f3 = this.easing.ease(f2, 0.0f, 1.0f, 1.0f);
        this.currentValue = this.startValue + (this.targetValue - this.startValue) * f3;
        return this.currentValue;
    }

    public boolean isAtTarget(boolean bl) {
        return bl ? this.currentValue == this.targetValue : this.currentValue == 0.0f;
    }

    public void setValue(float f) {
        this.currentValue = f;
        this.startValue = f;
        this.targetValue = f;
        this.atTarget = true;
    }

    public void setTargetValue(float f) {
        this.currentValue = f;
        this.startValue = f;
        this.targetValue = f;
        this.atTarget = true;
    }

    public void reset() {
        this.setTargetValue(0.0f);
    }

    public void toggleDirection() {
        if (this.reverse) {
            this.update(1.0f);
        } else {
            this.update(0.0f);
        }
        if (this.currentValue == 1.0f) {
            this.reverse = false;
        } else if (this.currentValue == 0.0f) {
            this.reverse = true;
        }
    }

    @Generated
    public long getDuration() {
        return this.duration;
    }

    @Generated
    public float getValue() {
        return this.currentValue;
    }

    @Generated
    public Easing getEasing() {
        return this.easing;
    }

    @Generated
    public long getLastChangeTime() {
        return this.lastChangeTime;
    }

    @Generated
    public float getStartValue() {
        return this.startValue;
    }

    @Generated
    public float getTargetValue() {
        return this.targetValue;
    }

    @Generated
    public boolean isAtTarget() {
        return this.atTarget;
    }

    @Generated
    public boolean isReverse() {
        return this.reverse;
    }

    @Generated
    public void setDuration(long l) {
        this.duration = l;
    }

    @Generated
    public void setEasing(Easing easing) {
        this.easing = easing;
    }

    @Generated
    public void setLastChangeTime(long l) {
        this.lastChangeTime = l;
    }

    @Generated
    public void setStartValueField(float f) {
        this.startValue = f;
    }

    @Generated
    public void setTargetValueField(float f) {
        this.targetValue = f;
    }

    @Generated
    public void setAtTarget(boolean bl) {
        this.atTarget = bl;
    }

    @Generated
    public void setReverse1(boolean bl) {
        this.reverse = bl;
    }
}

