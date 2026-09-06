/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.animation;

import moscow.rockstar.ui.animation.AnimatedValue;
import moscow.rockstar.ui.animation.Easing;

public abstract class Motion {
    public static final Motion motion = Motion.resolveMotionMotionFromLongAndEasing(300L, Easing.easeOutBack);
    public static final Motion motion2 = Motion.resolveMotionMotionFromLongAndEasing(450L, Easing.easeOutBackSoft);
    public static final Motion motion3 = Motion.resolveMotionMotionFromLongAndEasing(250L, Easing.easeInOutCubicBezier);
    public static final Motion motion4 = Motion.resolveMotionMotionFromLongAndEasing(150L, Easing.easeOutQuart);
    public static final Motion motion5 = Motion.resolveMotionMotionFromLongAndEasing(200L, Easing.easeOutQuart);
    public static final Motion motion6 = Motion.resolveMotionMotionFromFloatAndFloat(220.0f, 24.0f);
    public static final Motion motion7 = Motion.resolveMotionMotionFromFloatAndFloat(380.0f, 30.0f);

    public abstract void resetAnimationState(AnimatedValue var1);

    public abstract void update(AnimatedValue var1, float var2);

    public boolean matches(AnimatedValue animatedValue) {
        return Math.abs(animatedValue.current - animatedValue.target) < 0.01f && Math.abs(animatedValue.delta) < 0.01f;
    }

    public static Motion resolveMotionMotionFromLongAndEasing(long l, Easing easing) {
        return new TimedEasing(l, easing);
    }

    public static Motion withLinearEasing(long l) {
        return Motion.resolveMotionMotionFromLongAndEasing(l, Easing.linear);
    }

    public static Motion resolveMotionMotionFromFloatAndFloat(float f, float f2) {
        return new SpringMotion(f, f2);
    }

    static final class TimedEasing
    extends Motion {
        private final long durationMillis;
        private final Easing easing;

        TimedEasing(long l, Easing easing) {
            this.durationMillis = Math.max(1L, l);
            this.easing = easing;
        }

        @Override
        public void resetAnimationState(AnimatedValue animatedValue) {
            animatedValue.start = animatedValue.current;
            animatedValue.elapsed = 0.0f;
        }

        @Override
        public void update(AnimatedValue animatedValue, float f) {
            if (animatedValue.current == animatedValue.target) {
                return;
            }
            animatedValue.elapsed += f;
            float f2 = Math.min(1.0f, animatedValue.elapsed / (float)this.durationMillis);
            float f3 = this.easing.ease(f2, 0.0f, 1.0f, 1.0f);
            animatedValue.current = animatedValue.start + (animatedValue.target - animatedValue.start) * f3;
            if (f2 >= 1.0f) {
                animatedValue.current = animatedValue.target;
                animatedValue.delta = 0.0f;
            }
        }

        @Override
        public boolean matches(AnimatedValue animatedValue) {
            return animatedValue.current == animatedValue.target;
        }
    }

    static final class SpringMotion
    extends Motion {
        private final float stiffness;
        private final float damping;

        SpringMotion(float f, float f2) {
            this.stiffness = f;
            this.damping = f2;
        }

        @Override
        public void resetAnimationState(AnimatedValue animatedValue) {
        }

        @Override
        public void update(AnimatedValue animatedValue, float f) {
            float f2 = Math.min(0.05f, f / 1000.0f);
            if (f2 <= 0.0f) {
                return;
            }
            float f3 = animatedValue.current - animatedValue.target;
            float f4 = -this.stiffness * f3 - this.damping * animatedValue.delta;
            animatedValue.delta += f4 * f2;
            animatedValue.current += animatedValue.delta * f2;
            if (Math.abs(f3) < 0.05f && Math.abs(animatedValue.delta) < 0.05f) {
                animatedValue.current = animatedValue.target;
                animatedValue.delta = 0.0f;
            }
        }
    }
}

