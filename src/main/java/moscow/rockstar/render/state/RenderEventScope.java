package moscow.rockstar.render.state;

/** Tracks nested client render scopes while custom render events are dispatched. */
public final class RenderEventScope {
    private static int depth;

    private RenderEventScope() {
    }

    public static void begin() {
        depth++;
    }

    public static void end() {
        if (depth > 0) {
            depth--;
        }
    }

    public static boolean isActive() {
        return depth > 0;
    }
}
