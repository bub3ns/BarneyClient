package moscow.rockstar.ui.input;

import moscow.rockstar.api.data.SettingDataStore;
import moscow.rockstar.api.settings.SettingEntry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.items.assist.AssistItemProvider;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.ModuleRegistry;
import moscow.rockstar.modules.other.assist.Assist;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Encoding and input-state rules used by the original key-binding system.
 *
 * <p>A binding stores the GLFW key/button in the low 16 bits and the
 * modifier mask in the next four bits.  Keeping this in one named utility
 * avoids the decompiler's unrelated helper-class collisions.</p>
 */
public final class KeyBindingUtil {
    public static final int UNBOUND = -1;
    public static final int IGNORED_EVENT = Integer.MIN_VALUE;

    private KeyBindingUtil() {
    }

    public static int encode(int keyCode, int modifiers) {
        return keyCode < 0 ? UNBOUND : (keyCode & 0xFFFF) | ((modifiers & 0xF) << 16);
    }

    public static int keyCode(int binding) {
        return binding < 0 ? UNBOUND : binding & 0xFFFF;
    }

    public static boolean isBound(int binding) {
        return binding != UNBOUND && keyCode(binding) >= 0;
    }

    public static int modifiers(int binding) {
        return binding < 0 ? 0 : binding >> 16 & 0xF;
    }

    public static boolean isMouseBinding(int binding) {
        int keyCode = keyCode(binding);
        return keyCode >= 0 && keyCode <= 7;
    }

    public static boolean hasModifiers(int binding) {
        return modifiers(binding) != 0;
    }

    public static int modifierForKey(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> 1;
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> 2;
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> 4;
            case GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER -> 8;
            default -> 0;
        };
    }

    public static int encodeKeyPress(int keyCode, int modifiers) {
        return modifierForKey(keyCode) != 0 ? IGNORED_EVENT : encode(keyCode, modifiers);
    }

    public static int encodeKeyRelease(int keyCode, int modifiers) {
        int modifier = modifierForKey(keyCode);
        return modifier == 0 ? IGNORED_EVENT : encode(keyCode, modifiers & ~modifier);
    }

    public static int withCurrentModifiers(int keyCode) {
        return encode(keyCode, currentModifiers());
    }

    public static int currentModifiers() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return 0;
        }
        long window = client.getWindow().getHandle();
        int modifiers = 0;
        if (isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            modifiers |= 1;
        }
        if (isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) || isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL)) {
            modifiers |= 2;
        }
        if (isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT) || isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT)) {
            modifiers |= 4;
        }
        if (isKeyDown(window, GLFW.GLFW_KEY_LEFT_SUPER) || isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SUPER)) {
            modifiers |= 8;
        }
        return modifiers;
    }

    public static boolean matches(int binding, int inputKeyCode) {
        return matches(binding, inputKeyCode, currentModifiers());
    }

    public static boolean matches(int binding, int inputKeyCode, int inputModifiers) {
        if (binding == UNBOUND || keyCode(binding) != inputKeyCode) {
            return false;
        }
        int required = modifiers(binding);
        if ((inputModifiers & required) != required) {
            return false;
        }
        return required != 0 || inputModifiers == 0
            || !isClaimedByModifierBindingCached(inputKeyCode, inputModifiers);
    }

    private static final long CONFLICT_CACHE_MILLIS = 50L;
    private static int conflictCacheKeyCode = Integer.MIN_VALUE;
    private static int conflictCacheModifiers;
    private static boolean conflictCacheResult;
    private static long conflictCacheStamp;

    private static boolean isClaimedByModifierBindingCached(int inputKeyCode, int inputModifiers) {
        long now = System.currentTimeMillis();
        if (inputKeyCode == conflictCacheKeyCode && inputModifiers == conflictCacheModifiers
                && now - conflictCacheStamp < CONFLICT_CACHE_MILLIS) {
            return conflictCacheResult;
        }
        conflictCacheKeyCode = inputKeyCode;
        conflictCacheModifiers = inputModifiers;
        conflictCacheStamp = now;
        conflictCacheResult = isClaimedByModifierBinding(inputKeyCode, inputModifiers);
        return conflictCacheResult;
    }

    private static boolean isClaimedByModifierBinding(int inputKeyCode, int inputModifiers) {
        RockstarClient client = RockstarClient.create();
        if (client == null) {
            return false;
        }
        ModuleRegistry registry = client.getModuleRegistry();
        if (registry != null) {
            for (ModuleContract module : registry.getModules()) {
                if (!module.isAvailable()) {
                    continue;
                }
                if (bindingClaims(module.getKeyBind(), inputKeyCode, inputModifiers)) {
                    return true;
                }
                for (Setting setting : module.getSettings()) {
                    if (setting instanceof IntegerSetting integerSetting
                            && integerSetting.hasValidSettingValue()
                            && bindingClaims(integerSetting.getValue(), inputKeyCode, inputModifiers)) {
                        return true;
                    }
                }
            }
            Assist assist = registry.getModule(Assist.class);
            if (assist != null) {
                for (AssistItemProvider provider : assist.getAssistSettings()) {
                    if (bindingClaims(provider.getKeyCode(), inputKeyCode, inputModifiers)) {
                        return true;
                    }
                }
            }
        }
        SettingDataStore store = client.getSettingDataStore();
        if (store != null) {
            for (SettingEntry entry : store.getBindings()) {
                if (bindingClaims(entry.getKeyCode(), inputKeyCode, inputModifiers)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean bindingClaims(int binding, int inputKeyCode, int inputModifiers) {
        int required = modifiers(binding);
        return required != 0 && keyCode(binding) == inputKeyCode
            && (inputModifiers & required) == required;
    }

    public static boolean isPressed(int binding) {
        if (binding == UNBOUND) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return false;
        }
        int required = modifiers(binding);
        if ((currentModifiers() & required) != required) {
            return false;
        }
        int keyCode = keyCode(binding);
        long window = client.getWindow().getHandle();
        return isMouseBinding(binding)
            ? GLFW.glfwGetMouseButton(window, keyCode) == GLFW.GLFW_PRESS
            : isKeyDown(window, keyCode);
    }

    public static String modifierPrefix(int modifiers) {
        if ((modifiers & 0xF) == 0) {
            return "";
        }
        StringBuilder prefix = new StringBuilder();
        if ((modifiers & 2) != 0) {
            prefix.append("CTRL + ");
        }
        if ((modifiers & 1) != 0) {
            prefix.append("SHIFT + ");
        }
        if ((modifiers & 4) != 0) {
            prefix.append("ALT + ");
        }
        if ((modifiers & 8) != 0) {
            prefix.append("WIN + ");
        }
        return prefix.toString();
    }

    private static boolean isKeyDown(long window, int keyCode) {
        return keyCode >= 0 && InputUtil.isKeyPressed(window, keyCode);
    }
}
