package moscow.rockstar.ui.input;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/** Tracks the cursor requested by the current UI frame and applies it on the client mouse tick. */
public final class CursorManager {
    private static Cursor requested = Cursor.ARROW;
    private static Cursor applied = Cursor.HAND;

    private CursorManager() {
    }

    public static void request(Cursor cursor) {
        requested = cursor == null ? Cursor.ARROW : cursor;
    }

    public static void applyAndReset(MinecraftClient client) {
        if (requested != applied) {
            GLFW.glfwSetCursor(client.getWindow().getHandle(), requested.getCursorLong());
            applied = requested;
        }
        requested = Cursor.ARROW;
    }
}
