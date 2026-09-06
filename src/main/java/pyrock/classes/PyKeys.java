/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InputUtil
 *  org.lwjgl.glfw.GLFW
 */
package pyrock.classes;

import java.util.List;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.ui.input.KeyDisplayFormatter;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PyKeys
implements ClientAccess {
    public int code(String string) {
        return KeyDisplayFormatter.parseKey(string);
    }

    public String name(int n) {
        return moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(n);
    }

    public boolean down(Object object) {
        int n = this.toCode(object);
        if (n < 0) {
            return false;
        }
        long l = minecraftClient.getWindow().getHandle();
        if (n <= 7) {
            return GLFW.glfwGetMouseButton((long)l, (int)n) == 1;
        }
        return InputUtil.isKeyPressed((long)l, (int)n);
    }

    public boolean mouse(int n) {
        return GLFW.glfwGetMouseButton((long)minecraftClient.getWindow().getHandle(), (int)n) == 1;
    }

    public float x() {
        return (float)(PyKeys.minecraftClient.mouse.getX() / minecraftClient.getWindow().getScaleFactor());
    }

    public float y() {
        return (float)(PyKeys.minecraftClient.mouse.getY() / minecraftClient.getWindow().getScaleFactor());
    }

    public List<String> names() {
        return KeyDisplayFormatter.keyNames();
    }

    private int toCode(Object object) {
        if (object instanceof Number) {
            Number number = (Number)object;
            return number.intValue();
        }
        if (object instanceof String) {
            String string = (String)object;
            return KeyDisplayFormatter.parseKey(string);
        }
        return -1;
    }
}
