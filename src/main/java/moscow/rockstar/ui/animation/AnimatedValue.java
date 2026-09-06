/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.animation;

import moscow.rockstar.ui.animation.Motion;

public final class AnimatedValue {
    float current;
    float target;
    float delta;
    float start;
    float elapsed;
    private boolean animating;
    private Motion motion;

    public AnimatedValue(Motion motion) {
        this.motion = motion;
    }

    public AnimatedValue(float f, Motion motion) {
        this.motion = motion;
        this.snapTo(f);
    }

    public AnimatedValue motion(Motion motion) {
        this.motion = motion;
        return this;
    }

    public Motion getMotion() {
        return this.motion;
    }

    public void setTarget(float f) {
        if (!this.animating) {
            this.snapTo(f);
            return;
        }
        if (f != this.target) {
            this.target = f;
            if (this.motion != null) {
                this.motion.resetAnimationState(this);
            }
        }
    }

    public void snapTo(float f) {
        this.target = this.start = f;
        this.current = this.start;
        this.delta = 0.0f;
        this.elapsed = 0.0f;
        this.animating = true;
    }

    public void advance(float f) {
        if (!this.animating || f == 0.0f) {
            return;
        }
        this.current += f;
        this.target += f;
        this.start += f;
    }

    public void update(float f) {
        if (this.motion != null) {
            this.motion.update(this, f);
        }
    }

    public float getCurrent() {
        return this.current;
    }

    public float getTarget() {
        return this.target;
    }

    public boolean isFinished() {
        return this.motion == null || this.motion.matches(this);
    }

    public boolean isAnimating() {
        return this.animating;
    }
}
