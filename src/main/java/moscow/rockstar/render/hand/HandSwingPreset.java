package moscow.rockstar.render.hand;

import moscow.rockstar.modules.visuals.hand.HandSwingState;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import net.minecraft.util.math.Vec2f;

/**
 * A named hand-swing animation preset (original rockstar/ilIlil/iiIIIiii).
 *
 * Not a record: besides the seven constructor components the original carries two
 * final per-preset animations used by the preset list UI (hover + active/selected).
 */
public final class HandSwingPreset {
    private final String name;
    private final Vec2f startControlPoint;
    private final Vec2f endControlPoint;
    private final boolean backSwing;
    private final float swingSpeed;
    private final HandSwingState startState;
    private final HandSwingState endState;
    private final Animation hoverAnimation = new Animation(300L, Easing.easeInOutCubicBezier);
    private final Animation activeAnimation = new Animation(300L, Easing.easeInOutCubicBezier);

    public HandSwingPreset(String name, Vec2f startControlPoint, Vec2f endControlPoint, boolean backSwing,
                           float swingSpeed, HandSwingState startState, HandSwingState endState) {
        this.name = name;
        this.startControlPoint = startControlPoint;
        this.endControlPoint = endControlPoint;
        this.backSwing = backSwing;
        this.swingSpeed = swingSpeed;
        this.startState = startState;
        this.endState = endState;
    }

    public String getName() {
        return this.name;
    }

    public Vec2f getStartControlPoint() {
        return this.startControlPoint;
    }

    public Vec2f getEndControlPoint() {
        return this.endControlPoint;
    }

    public boolean isBackSwing() {
        return this.backSwing;
    }

    public float getSwingSpeed() {
        return this.swingSpeed;
    }

    public HandSwingState getStartState() {
        return this.startState;
    }

    public HandSwingState getEndState() {
        return this.endState;
    }

    public Animation getHoverAnimation() {
        return this.hoverAnimation;
    }

    public Animation getActiveAnimation() {
        return this.activeAnimation;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof HandSwingPreset)) {
            return false;
        }
        HandSwingPreset other = (HandSwingPreset)object;
        if (this.isBackSwing() != other.isBackSwing()) {
            return false;
        }
        if (Float.compare(this.getSwingSpeed(), other.getSwingSpeed()) != 0) {
            return false;
        }
        String name = this.getName();
        String otherName = other.getName();
        if (name == null ? otherName != null : !name.equals(otherName)) {
            return false;
        }
        Vec2f start = this.getStartControlPoint();
        Vec2f otherStart = other.getStartControlPoint();
        if (start == null ? otherStart != null : !start.equals(otherStart)) {
            return false;
        }
        Vec2f end = this.getEndControlPoint();
        Vec2f otherEnd = other.getEndControlPoint();
        if (end == null ? otherEnd != null : !end.equals(otherEnd)) {
            return false;
        }
        HandSwingState from = this.getStartState();
        HandSwingState otherFrom = other.getStartState();
        if (from == null ? otherFrom != null : !from.equals(otherFrom)) {
            return false;
        }
        HandSwingState to = this.getEndState();
        HandSwingState otherTo = other.getEndState();
        if (to == null ? otherTo != null : !to.equals(otherTo)) {
            return false;
        }
        Animation hover = this.getHoverAnimation();
        Animation otherHover = other.getHoverAnimation();
        if (hover == null ? otherHover != null : !hover.equals(otherHover)) {
            return false;
        }
        Animation active = this.getActiveAnimation();
        Animation otherActive = other.getActiveAnimation();
        return !(active == null ? otherActive != null : !active.equals(otherActive));
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.isBackSwing() ? 79 : 97);
        result = result * 59 + Float.floatToIntBits(this.getSwingSpeed());
        String name = this.getName();
        result = result * 59 + (name == null ? 43 : name.hashCode());
        Vec2f start = this.getStartControlPoint();
        result = result * 59 + (start == null ? 43 : start.hashCode());
        Vec2f end = this.getEndControlPoint();
        result = result * 59 + (end == null ? 43 : end.hashCode());
        HandSwingState from = this.getStartState();
        result = result * 59 + (from == null ? 43 : from.hashCode());
        HandSwingState to = this.getEndState();
        result = result * 59 + (to == null ? 43 : to.hashCode());
        Animation hover = this.getHoverAnimation();
        result = result * 59 + (hover == null ? 43 : hover.hashCode());
        Animation active = this.getActiveAnimation();
        result = result * 59 + (active == null ? 43 : active.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "SwingPreset(name=" + this.getName()
            + ", bezierStart=" + String.valueOf(this.getStartControlPoint())
            + ", bezierEnd=" + String.valueOf(this.getEndControlPoint())
            + ", swingBack=" + this.isBackSwing()
            + ", speed=" + this.getSwingSpeed()
            + ", from=" + String.valueOf(this.getStartState())
            + ", to=" + String.valueOf(this.getEndState())
            + ", hoverAnimation=" + String.valueOf(this.getHoverAnimation())
            + ", activeAnimation=" + String.valueOf(this.getActiveAnimation()) + ")";
    }
}
