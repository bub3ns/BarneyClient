package moscow.rockstar.ui.input;

import java.util.List;
import java.util.Locale;
import moscow.rockstar.ui.localization.Localization;
import net.minecraft.client.util.InputUtil;

/** Human-readable formatting for the encoded key bindings. */
public final class KeyDisplayFormatter {
    private KeyDisplayFormatter() {
    }

    public static String formatKey(int binding) {
        return KeyBindingUtil.modifierPrefix(KeyBindingUtil.modifiers(binding))
            + formatRawKey(KeyBindingUtil.keyCode(binding));
    }

    public static String formatRawKey(int keyCode) {
        if (keyCode >= 0 && keyCode <= 7) {
            return switch (keyCode) {
                case 0 -> Localization.translate("mouse.lmb");
                case 1 -> Localization.translate("mouse.rmb");
                case 2 -> Localization.translate("mouse.mmb");
                case 3 -> "MOUSE4";
                case 4 -> "MOUSE5";
                case 5 -> "MOUSE6";
                case 6 -> "MOUSE7";
                case 7 -> "MOUSE8";
                default -> "MOUSE" + keyCode;
            };
        }
        if (keyCode < 0) {
            return "NONE";
        }
        String name = InputUtil.fromKeyCode(keyCode, -1).getTranslationKey();
        return name.replace("key.keyboard.", "")
            .replace("key.", "")
            .replace(".", "")
            .replace("left", "l")
            .replace("right", "r")
            .replace("apostrophe", "apost")
            .replace("printscreen", "prtsc")
            .replace("graveaccent", "grave")
            .replace("control", "ctrl")
            .toUpperCase();
    }

    /** Parses the compact key names exposed by scripts and configuration files. */
    public static int parseKey(String value) {
        if (value == null || value.isBlank()) {
            return KeyBindingUtil.UNBOUND;
        }
        String[] parts = value.split("\\+");
        if (parts.length > 1) {
            int modifiers = 0;
            for (int i = 0; i < parts.length - 1; i++) {
                int bit = modifierBit(normalizeKeyName(parts[i]));
                if (bit == 0) {
                    return KeyBindingUtil.UNBOUND;
                }
                modifiers |= bit;
            }
            int keyCode = resolveKeyCode(parts[parts.length - 1]);
            return keyCode == KeyBindingUtil.UNBOUND
                ? KeyBindingUtil.UNBOUND : KeyBindingUtil.encode(keyCode, modifiers);
        }
        return resolveKeyCode(value);
    }

    private static int resolveKeyCode(String value) {
        if (value == null || value.isBlank()) {
            return KeyBindingUtil.UNBOUND;
        }
        String name = normalizeKeyName(value);
        int mouse = mouseCode(name);
        if (mouse != KeyBindingUtil.UNBOUND) {
            return mouse;
        }
        try {
            return (Integer)org.lwjgl.glfw.GLFW.class.getField("GLFW_KEY_" + name).get(null);
        } catch (ReflectiveOperationException exception) {
            return KeyBindingUtil.UNBOUND;
        }
    }

    private static int modifierBit(String name) {
        return switch (name) {
            case "CTRL", "CONTROL", "LCTRL", "RCTRL", "LEFT_CONTROL", "RIGHT_CONTROL" -> 2;
            case "SHIFT", "LSHIFT", "RSHIFT", "LEFT_SHIFT", "RIGHT_SHIFT" -> 1;
            case "ALT", "LALT", "RALT", "LEFT_ALT", "RIGHT_ALT" -> 4;
            case "SUPER", "WIN", "CMD", "LEFT_SUPER", "RIGHT_SUPER" -> 8;
            default -> 0;
        };
    }

    private static String normalizeKeyName(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replace(" ", "_").replace("-", "_");
    }

    private static int mouseCode(String name) {
        switch (name) {
            case "LMB":
                return 0;
            case "RMB":
                return 1;
            case "MMB":
                return 2;
            default:
                break;
        }
        if (name.startsWith("MOUSE")) {
            String digits = name.replace("MOUSE_BUTTON_", "").replace("MOUSE", "");
            try {
                int index = Integer.parseInt(digits);
                if (index >= 1 && index <= 8) {
                    return index - 1;
                }
            } catch (NumberFormatException exception) {
                // ignored, matches the original
            }
        }
        return KeyBindingUtil.UNBOUND;
    }

    public static List<String> keyNames() {
        return java.util.stream.Stream.of(org.lwjgl.glfw.GLFW.class.getFields())
            .map(java.lang.reflect.Field::getName)
            .filter(name -> name.startsWith("GLFW_KEY_"))
            .map(name -> name.substring("GLFW_KEY_".length()))
            .filter(name -> !name.matches("LAST|UNKNOWN|WORLD_\\d+"))
            .toList();
    }
}
