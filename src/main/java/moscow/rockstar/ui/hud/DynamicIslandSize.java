package moscow.rockstar.ui.hud;

/**
 * Mutable width / height / corner-radius triple carried by every dynamic-island entry.
 * 1:1 with rockstar/ilIlil/IiIiiIiiI (fields I = width, i = height, II = radius;
 * methods I(FFF)V and I(FF)V).
 */
public class DynamicIslandSize {
    public float width;
    public float height;
    public float radius;

    public DynamicIslandSize(float width, float height, float radius) {
        this.width = width;
        this.height = height;
        this.radius = radius;
    }

    /** ORIGINAL: I(FFF)V */
    public void set(float width, float height, float radius) {
        this.width = width;
        this.height = height;
        this.radius = radius;
    }

    /** ORIGINAL: I(FF)V */
    public void set(float width, float height) {
        this.width = width;
        this.height = height;
    }
}
