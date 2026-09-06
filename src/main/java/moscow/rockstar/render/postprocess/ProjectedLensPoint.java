package moscow.rockstar.render.postprocess;

/** One projected target point consumed by the target-lens post-process shader. */
public record ProjectedLensPoint(float centerX, float centerY, float depth, float radius) {
    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public float getDepth() {
        return depth;
    }

    public float getRadius() {
        return radius;
    }
}
