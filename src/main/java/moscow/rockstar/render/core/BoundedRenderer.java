package moscow.rockstar.render.core;

/** Base for renderers that own a mutable rectangular viewport. */
public abstract class BoundedRenderer {
    protected float x;
    protected float y;
    protected float width;
    protected float height;

    protected BoundedRenderer() {
        this(0.0f, 0.0f, 0.0f, 0.0f);
    }

    protected BoundedRenderer(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public final void render(RockstarDrawContext context) {
        update(context);
        draw(context);
    }

    protected abstract void draw(RockstarDrawContext context);

    public void update(RockstarDrawContext context) {
    }

    public void dispose() {
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(float pointX, float pointY) {
        return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
    }

    public boolean contains(double pointX, double pointY) {
        return contains((float) pointX, (float) pointY);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
