package moscow.rockstar.render.text;

/** A single styled run extracted from a Minecraft text component. */
public record TextComponentRun(String text, int color) {
    public String getText() {
        return this.text;
    }

    public int getColor() {
        return this.color;
    }
}
