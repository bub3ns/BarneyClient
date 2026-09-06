package moscow.rockstar.ui.state;

/** Inertial, spring-damped scroll offset (original rockstar/ilIlil/iIIiIIIii). */
public final class ScrollOffset {
    private double limit;
    private double offset = 0.0;
    private double velocity = 0.0;
    private double friction = 0.95;
    private double stiffness = 0.08;
    private double damping = 0.7;

    public void update() {
        this.offset += this.velocity;
        this.velocity *= this.friction;
        if (Math.abs(this.velocity) < 0.01) {
            this.velocity = 0.0;
        }
        if (this.limit >= 0.0) {
            this.offset = 0.0;
            this.velocity = 0.0;
            return;
        }
        if (this.offset > 0.0) {
            double pull = -this.offset * this.stiffness;
            this.velocity += pull;
            this.velocity *= this.damping;
        } else if (this.offset < this.limit) {
            double pull = (this.limit - this.offset) * this.stiffness;
            this.velocity += pull;
            this.velocity *= this.damping;
        }
    }

    /** Original I()D - returns the negated offset as a double. */
    public double getOffset() {
        return -this.offset;
    }

    /** Original i()V - clears offset and velocity. */
    public void reset() {
        this.offset = 0.0;
        this.velocity = 0.0;
    }

    /** Original I(D)V - the wheel delta is added straight to the velocity. */
    public void scrollBy(double delta) {
        this.velocity += delta;
    }

    public boolean isScrolling() {
        if (this.velocity > 0.5) {
            return true;
        }
        return this.offset > 0.0 || this.offset < this.limit;
    }

    /** Original I(I)V - arrow-key scrolling. */
    public void handleKey(int keyCode) {
        if (keyCode == 265) {
            this.scrollBy(1.0);
        } else if (keyCode == 264) {
            this.scrollBy(-1.0);
        }
    }

    /** Original i(D)V, expressed the way the layout code calls it. */
    public void setContentBounds(float viewportHeight, float contentHeight) {
        this.limit = Math.min(0.0f, viewportHeight - contentHeight);
    }

    public double getLimit() {
        return this.limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public double getVelocity() {
        return this.velocity;
    }

    /** Original II(D)V - moves the offset without clearing the velocity. */
    public void setOffset(double offset) {
        this.offset = offset;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getFriction() {
        return this.friction;
    }

    public void setFriction(double friction) {
        this.friction = friction;
    }

    public double getStiffness() {
        return this.stiffness;
    }

    public void setStiffness(double stiffness) {
        this.stiffness = stiffness;
    }

    public double getDamping() {
        return this.damping;
    }

    public void setDamping(double damping) {
        this.damping = damping;
    }
}
