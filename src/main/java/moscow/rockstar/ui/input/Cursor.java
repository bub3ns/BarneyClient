/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.lwjgl.glfw.GLFW
 */
package moscow.rockstar.ui.input;

import lombok.Generated;
import org.lwjgl.glfw.GLFW;

public enum Cursor {
    ARROW(GLFW.glfwCreateStandardCursor((int)221185)),
    HAND(GLFW.glfwCreateStandardCursor((int)221188)),
    HORIZONTAL_RESIZE(GLFW.glfwCreateStandardCursor((int)221189)),
    VERTICAL_RESIZE(GLFW.glfwCreateStandardCursor((int)221190)),
    IBEAM(GLFW.glfwCreateStandardCursor((int)221186)),
    CROSSHAIR(GLFW.glfwCreateStandardCursor((int)221187)),
    NOT_ALLOWED(GLFW.glfwCreateStandardCursor((int)221194)),
    RESIZE_ALL(GLFW.glfwCreateStandardCursor((int)221193));
    private final long nativeHandle;

    @Generated
    public long getCursorLong() {
        return this.nativeHandle;
    }

    @Generated
    private Cursor(long l) {
        this.nativeHandle = l;
    }
}

