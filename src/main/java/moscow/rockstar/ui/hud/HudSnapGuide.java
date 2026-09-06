package moscow.rockstar.ui.hud;

/** A single HUD alignment guide. 1:1 with rockstar/ilIlil/IiIiIIIiI. */
public class HudSnapGuide {
    private final Axis axis;
    private final float position;
    private boolean highlighted;

    public HudSnapGuide(Axis axis, float position) {
        this.axis = axis;
        this.position = position;
    }

    public Axis getAxis() {
        return this.axis;
    }

    public float getPosition() {
        return this.position;
    }

    public boolean isHighlighted() {
        return this.highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    /** rockstar/ilIlil/IiIiIIIiI$I — constants I (ordinal 0) and i (ordinal 1) in declaration order. */
    public static enum Axis {
        /** A horizontal line: constrains Y. */
        HORIZONTAL,
        /** A vertical line: constrains X. */
        VERTICAL;
    }
}
