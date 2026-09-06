/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.animation;

import lombok.Generated;
import moscow.rockstar.math.MathUtils;

public interface Easing {
    public static final Easing easeOutBack = Easing.cubicBezier(0.45f, 1.45f, 0.49f, 1.15f);
    public static final Easing easeOutBackSoft = Easing.cubicBezier(0.45f, 1.45f, 0.43f, 0.91f);
    public static final Easing easeOutOvershoot = Easing.cubicBezier(0.1f, 1.07f, 0.34f, 1.04f);
    public static final Easing easeOutOvershootSoft = Easing.cubicBezier(0.27f, 1.09f, 0.49f, 1.06f);
    public static final Easing easeInBack = Easing.cubicBezier(0.62, -0.16, 0.8, 0.37);
    public static final Easing easeOutElastic = Easing.cubicBezier(0.25, 1.07, 0.11, 1.1);
    public static final Easing easeInOutCubicBezier = Easing.cubicBezier(0.42, 0.0, 0.58, 1.0);
    public static final Easing smoothStep = (f, f2, f3, f4) -> {
        float f5 = f3 * f / f4 + f2;
        return (float)(-2.0 * Math.pow(f5, 3.0) + 3.0 * Math.pow(f5, 2.0));
    };
    public static final Easing easeInOutCubic = (f, f2, f3, f4) -> {
        float f5 = f3 * f / f4 + f2;
        return (double)f5 < 0.5 ? 4.0f * f5 * f5 * f5 : (float)(1.0 - Math.pow(-2.0f * f5 + 2.0f, 3.0) / 2.0);
    };
    public static final Easing linear = (f, f2, f3, f4) -> f3 * f / f4 + f2;
    public static final Easing easeInQuad = (f, f2, f3, f4) -> f3 * (f /= f4) * f + f2;
    public static final Easing easeOutQuad = (f, f2, f3, f4) -> -f3 * (f /= f4) * (f - 2.0f) + f2;
    public static final Easing easeInOutQuad = (f, f2, f3, f4) -> {
        f /= f4 / 2.0f;
        float f5 = f;
        if (f5 < 1.0f) {
            return f3 / 2.0f * f * f + f2;
        }
        return -f3 / 2.0f * ((f -= 1.0f) * (f - 2.0f) - 1.0f) + f2;
    };
    public static final Easing easeInCubic = (f, f2, f3, f4) -> f3 * (f /= f4) * f * f + f2;
    public static final Easing easeOutCubic = (f, f2, f3, f4) -> {
        f = f / f4 - 1.0f;
        return f3 * (f * f * f + 1.0f) + f2;
    };
    public static final Easing easeInOutCubicPolynomial = (f, f2, f3, f4) -> {
        f /= f4 / 2.0f;
        float f5 = f;
        if (f5 < 1.0f) {
            return f3 / 2.0f * f * f * f + f2;
        }
        return f3 / 2.0f * ((f -= 2.0f) * f * f + 2.0f) + f2;
    };
    public static final Easing easeInQuart = (f, f2, f3, f4) -> f3 * (f /= f4) * f * f * f + f2;
    public static final Easing easeOutQuart = (f, f2, f3, f4) -> {
        f = f / f4 - 1.0f;
        return -f3 * (f * f * f * f - 1.0f) + f2;
    };
    public static final Easing easeInOutQuart = (f, f2, f3, f4) -> {
        f /= f4 / 2.0f;
        float f5 = f;
        if (f5 < 1.0f) {
            return f3 / 2.0f * f * f * f * f + f2;
        }
        return -f3 / 2.0f * ((f -= 2.0f) * f * f * f - 2.0f) + f2;
    };
    public static final Easing easeInQuint = (f, f2, f3, f4) -> f3 * (f /= f4) * f * f * f * f + f2;
    public static final Easing easeOutQuint = (f, f2, f3, f4) -> {
        f = f / f4 - 1.0f;
        return f3 * (f * f * f * f * f + 1.0f) + f2;
    };
    public static final Easing easeInOutQuint = (f, f2, f3, f4) -> {
        f /= f4 / 2.0f;
        float f5 = f;
        if (f5 < 1.0f) {
            return f3 / 2.0f * f * f * f * f * f + f2;
        }
        return f3 / 2.0f * ((f -= 2.0f) * f * f * f * f + 2.0f) + f2;
    };
    public static final Easing easeInSine = (f, f2, f3, f4) -> -f3 * (float)MathUtils.lookupCosine((double)(f / f4) * 1.5707963267948966) + f3 + f2;
    public static final Easing easeOutSine = (f, f2, f3, f4) -> f3 * (float)MathUtils.lookupSine((double)(f / f4) * 1.5707963267948966) + f2;
    public static final Easing easeInOutSine = (f, f2, f3, f4) -> -f3 / 2.0f * ((float)MathUtils.lookupCosine(Math.PI * (double)f / (double)f4) - 1.0f) + f2;
    public static final Easing easeInExpo = (f, f2, f3, f4) -> f == 0.0f ? f2 : f3 * (float)Math.pow(2.0, 10.0f * (f / f4 - 1.0f)) + f2;
    public static final Easing easeOutExpo = (f, f2, f3, f4) -> f == f4 ? f2 + f3 : f3 * (-((float)Math.pow(2.0, -10.0f * f / f4)) + 1.0f) + f2;
    public static final Easing easeInOutExpo = (f, f2, f3, f4) -> {
        if (f == 0.0f) {
            return f2;
        }
        if (f == f4) {
            return f2 + f3;
        }
        f /= f4 / 2.0f;
        float f5 = f;
        if (f5 < 1.0f) {
            return f3 / 2.0f * (float)Math.pow(2.0, 10.0f * (f - 1.0f)) + f2;
        }
        return f3 / 2.0f * (-((float)Math.pow(2.0, -10.0f * (f -= 1.0f))) + 2.0f) + f2;
    };
    public static final Easing easeInCirc = (f, f2, f3, f4) -> -f3 * ((float)Math.sqrt(1.0f - (f /= f4) * f) - 1.0f) + f2;
    public static final Easing easeOutCirc = (f, f2, f3, f4) -> {
        f = f / f4 - 1.0f;
        return f3 * (float)Math.sqrt(1.0f - f * f) + f2;
    };
    public static final Easing easeInOutCirc = (f, f2, f3, f4) -> {
        f /= f4 / 2.0f;
        float f5 = f;
        if (f5 < 1.0f) {
            return -f3 / 2.0f * ((float)Math.sqrt(1.0f - f * f) - 1.0f) + f2;
        }
        return f3 / 2.0f * ((float)Math.sqrt(1.0f - (f -= 2.0f) * f) + 1.0f) + f2;
    };
    public static final Circ BACK = new Back();
    public static final Circ QUART = new Quart();
    public static final Circ CUBIC = new Cubic();
    public static final Sine EXPO = new Expo();
    public static final Sine QUINT = new Quint();
    public static final Sine QUAD = new Quad();
    public static final Easing easeOutBounce = (f, f2, f3, f4) -> {
        f /= f4;
        float f5 = f;
        if (f5 < 0.36363637f) {
            return f3 * (7.5625f * f * f) + f2;
        }
        if (f < 0.72727275f) {
            return f3 * (7.5625f * (f -= 0.54545456f) * f + 0.75f) + f2;
        }
        if (f < 0.90909094f) {
            return f3 * (7.5625f * (f -= 0.8181818f) * f + 0.9375f) + f2;
        }
        return f3 * (7.5625f * (f -= 0.95454544f) * f + 0.984375f) + f2;
    };
    public static final Easing easeInBounce = (f, f2, f3, f4) -> f3 - easeOutBounce.ease(f4 - f, 0.0f, f3, f4) + f2;
    public static final Easing easeInOutBounce = (f, f2, f3, f4) -> {
        if (f < f4 / 2.0f) {
            return easeInBounce.ease(f * 2.0f, 0.0f, f3, f4) * 0.5f + f2;
        }
        return easeOutBounce.ease(f * 2.0f - f4, 0.0f, f3, f4) * 0.5f + f3 * 0.5f + f2;
    };

    public static Easing cubicBezier(final double d, final double d2, final double d3, final double d4) {
        return new Easing(){

            @Override
            public float ease(float f, float f2, float f3, float f4) {
                if (f4 <= 0.0f || f <= 0.0f) {
                    return f2;
                }
                if (f >= f4) {
                    return f2 + f3;
                }
                float f5 = f / f4;
                float f6 = this.solveCurveParameter((float)d, (float)d3, f5);
                float f7 = this.evaluateCurveY(f6, (float)d2, (float)d4);
                return f2 + f3 * f7;
            }

            private float solveCurveParameter(float f, float f2, float f3) {
                float f4 = f3;
                int n = 8;
                float f5 = 1.0E-5f;
                for (int i = 0; i < 8; ++i) {
                    float f6 = this.evaluateCurveX(f4, f, f2);
                    float f7 = this.evaluateCurveDerivative(f4, f, f2);
                    if (Math.abs(f6 - f3) < 1.0E-5f || Math.abs(f7) < 1.0E-6f) break;
                    f4 -= (f6 - f3) / f7;
                    f4 = Math.max(0.0f, Math.min(1.0f, f4));
                }
                return f4;
            }

            private float evaluateCurveX(float f, float f2, float f3) {
                return 3.0f * (1.0f - f) * (1.0f - f) * f * f2 + 3.0f * (1.0f - f) * f * f * f3 + f * f * f;
            }

            private float evaluateCurveDerivative(float f, float f2, float f3) {
                return 3.0f * ((1.0f - f) * (1.0f - 3.0f * f) * f2 + (2.0f * f - 3.0f * f * f) * f3) + 3.0f * f * f;
            }

            private float evaluateCurveY(float f, float f2, float f3) {
                return 3.0f * (1.0f - f) * (1.0f - f) * f * f2 + 3.0f * (1.0f - f) * f * f * f3 + f * f * f;
            }
        };
    }

    public float ease(float var1, float var2, float var3, float var4);

    public static class Back
    extends Circ {
        public Back(float f, float f2) {
            super(f, f2);
        }

        public Back() {
        }

        @Override
        public float ease(float f, float f2, float f3, float f4) {
            float f5 = this.getAmplitude();
            float f6 = this.getPeriod();
            if (f == 0.0f) {
                return f2;
            }
            if ((f /= f4) == 1.0f) {
                return f2 + f3;
            }
            if (f6 == 0.0f) {
                f6 = f4 * 0.3f;
            }
            float f7 = 0.0f;
            if (f5 < Math.abs(f3)) {
                f5 = f3;
                f7 = f6 / 4.0f;
            } else {
                f7 = f6 / ((float)Math.PI * 2) * (float)Math.asin(f3 / f5);
            }
            return -(f5 * (float)Math.pow(2.0, 10.0f * (f -= 1.0f)) * (float)MathUtils.lookupSine((double)(f * f4 - f7) * (Math.PI * 2) / (double)f6)) + f2;
        }
    }

    public static abstract class Circ
    implements Easing {
        private float amplitude;
        private float period;

        public Circ(float f, float f2) {
            this.amplitude = f;
            this.period = f2;
        }

        public Circ() {
            this(-1.0f, 0.0f);
        }

        @Generated
        public void setAmplitude(float f) {
            this.amplitude = f;
        }

        @Generated
        public void setPeriod(float f) {
            this.period = f;
        }

        @Generated
        public float getAmplitude() {
            return this.amplitude;
        }

        @Generated
        public float getPeriod() {
            return this.period;
        }
    }

    public static class Quart
    extends Circ {
        public Quart(float f, float f2) {
            super(f, f2);
        }

        public Quart() {
        }

        @Override
        public float ease(float f, float f2, float f3, float f4) {
            float f5 = this.getAmplitude();
            float f6 = this.getPeriod();
            if (f == 0.0f) {
                return f2;
            }
            if ((f /= f4) == 1.0f) {
                return f2 + f3;
            }
            if (f6 == 0.0f) {
                f6 = f4 * 0.3f;
            }
            float f7 = 0.0f;
            if (f5 < Math.abs(f3)) {
                f5 = f3;
                f7 = f6 / 4.0f;
            } else {
                f7 = f6 / ((float)Math.PI * 2) * (float)Math.asin(f3 / f5);
            }
            return f5 * (float)Math.pow(2.0, -10.0f * f) * (float)MathUtils.lookupSine((double)(f * f4 - f7) * (Math.PI * 2) / (double)f6) + f3 + f2;
        }
    }

    public static class Cubic
    extends Circ {
        public Cubic(float f, float f2) {
            super(f, f2);
        }

        public Cubic() {
        }

        @Override
        public float ease(float f, float f2, float f3, float f4) {
            float f5 = this.getAmplitude();
            float f6 = this.getPeriod();
            if (f == 0.0f) {
                return f2;
            }
            if ((f /= f4 / 2.0f) == 2.0f) {
                return f2 + f3;
            }
            if (f6 == 0.0f) {
                f6 = f4 * 0.45000002f;
            }
            float f7 = 0.0f;
            if (f5 < Math.abs(f3)) {
                f5 = f3;
                f7 = f6 / 4.0f;
            } else {
                f7 = f6 / ((float)Math.PI * 2) * (float)Math.asin(f3 / f5);
            }
            if (f < 1.0f) {
                return -0.5f * (f5 * (float)Math.pow(2.0, 10.0f * (f -= 1.0f)) * (float)MathUtils.lookupSine((double)(f * f4 - f7) * (Math.PI * 2) / (double)f6)) + f2;
            }
            return f5 * (float)Math.pow(2.0, -10.0f * (f -= 1.0f)) * (float)MathUtils.lookupSine((double)(f * f4 - f7) * (Math.PI * 2) / (double)f6) * 0.5f + f3 + f2;
        }
    }

    public static class Expo
    extends Sine {
        public Expo() {
        }

        public Expo(float f) {
            super(f);
        }

        @Override
        public float ease(float f, float f2, float f3, float f4) {
            float f5 = this.getOvershoot();
            return f3 * (f /= f4) * f * ((f5 + 1.0f) * f - f5) + f2;
        }
    }

    public static abstract class Sine
    implements Easing {
        public static final float BACK_OVERSHOOT = 1.70158f;
        private float overshoot;

        public Sine() {
            this(1.70158f);
        }

        public Sine(float f) {
            this.overshoot = f;
        }

        @Generated
        public void setOvershoot(float f) {
            this.overshoot = f;
        }

        @Generated
        public float getOvershoot() {
            return this.overshoot;
        }
    }

    public static class Quint
    extends Sine {
        public Quint() {
        }

        public Quint(float f) {
            super(f);
        }

        @Override
        public float ease(float f, float f2, float f3, float f4) {
            float f5 = this.getOvershoot();
            f = f / f4 - 1.0f;
            return f3 * (f * f * ((f5 + 1.0f) * f + f5) + 1.0f) + f2;
        }
    }

    public static class Quad
    extends Sine {
        public Quad() {
        }

        public Quad(float f) {
            super(f);
        }

        @Override
        public float ease(float f, float f2, float f3, float f4) {
            float f6 = this.getOvershoot();
            f /= f4 / 2.0f;
            float f5 = f;
            if (f5 < 1.0f) {
                return f3 / 2.0f * (f * f * (((f6 *= 1.525f) + 1.0f) * f - f6)) + f2;
            }
            return f3 / 2.0f * ((f -= 2.0f) * f * (((f6 *= 1.525f) + 1.0f) * f + f6) + 2.0f) + f2;
        }
    }
}
