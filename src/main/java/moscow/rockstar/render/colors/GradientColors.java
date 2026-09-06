package moscow.rockstar.render.colors;

import pyrock.utility.render.ColorRGBA;

/**
 * Four corner colors used by the gradient rectangle renderer.
 *
 * <p>1:1 port of {@code rockstar/ilIlil/IiIIi}. The original stores its four colours in the
 * declaration order {@code (topLeft, bottomLeft, topRight, bottomRight)} and its wrapper
 * ({@code iIiiIIiii.I(MatrixStack,FFFF,IIiii,IiIIi)}) forwards them to the gradient primitive as
 * {@code (TopLeftColor, BottomLeftColor, BottomRightColor, TopRightColor)} -- i.e. fields
 * {@code (I, i, Ii, II)}.  Keep the constructor argument order below identical to the original;
 * every call site was transcribed positionally from {@code IiIIi.I(a, b, c, d)}.
 *
 * <p>The two-colour form is {@code rockstar/ilIlil/IiIii}, whose constructor is
 * {@code IiIii(a, b) -> IiIIi(a, a, b, b)}: a LEFT-to-RIGHT fade, not a top-to-bottom one.
 */
public final class GradientColors {
    private final ColorRGBA topLeft;
    private final ColorRGBA bottomLeft;
    private final ColorRGBA topRight;
    private final ColorRGBA bottomRight;

    /** Two-colour horizontal fade: {@code left} on the left edge, {@code right} on the right. */
    public GradientColors(ColorRGBA left, ColorRGBA right) {
        this(left, left, right, right);
    }

    public GradientColors(ColorRGBA topLeft, ColorRGBA bottomLeft, ColorRGBA topRight, ColorRGBA bottomRight) {
        this.topLeft = topLeft;
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
    }

    public ColorRGBA topLeft() {
        return this.topLeft;
    }

    public ColorRGBA bottomLeft() {
        return this.bottomLeft;
    }

    public ColorRGBA topRight() {
        return this.topRight;
    }

    public ColorRGBA bottomRight() {
        return this.bottomRight;
    }
}
