/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 */
package pyrock;

import java.util.List;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.network.http.client.ReactorNettyClient;
import moscow.rockstar.render.overlay.OverlayElement;
import moscow.rockstar.settings.ActionSetting;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.RangeSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.ui.screens.MinecraftScreenBase;
import moscow.rockstar.ui.text.Font;
import net.minecraft.text.Text;
import pyrock.classes.PyDynamicIsland;
import pyrock.classes.PyModule;
import pyrock.classes.PyMusic;
import pyrock.classes.settings.PyBooleanSetting;
import pyrock.classes.settings.PyButtonSetting;
import pyrock.classes.settings.PyColorSetting;
import pyrock.classes.settings.PyModeSetting;
import pyrock.classes.settings.PyRangeSetting;
import pyrock.classes.settings.PySelectSetting;
import pyrock.classes.settings.PySliderSetting;

public class Client {
    public void msg(String string) {
        Notification.info(Text.of((String)string));
    }

    public void warn(String string) {
        Notification.warning(Text.of((String)string));
    }

    public void error(String string) {
        Notification.error(Text.of((String)string));
    }

    public void overlay(String string) {
        Notification.notify(Notification.Level.INFO, Text.of((String)string));
    }

    public void cursor(String string) {
        Cursor cursor = switch (string.toLowerCase()) {
            case "arrow", "default" -> Cursor.ARROW;
            case "hand", "pointer" -> Cursor.HAND;
            case "text", "ibeam" -> Cursor.IBEAM;
            case "crosshair" -> Cursor.CROSSHAIR;
            case "hresize", "horizontal" -> Cursor.HORIZONTAL_RESIZE;
            case "vresize", "vertical" -> Cursor.VERTICAL_RESIZE;
            case "block", "notallowed" -> Cursor.NOT_ALLOWED;
            case "resize", "resizeall" -> Cursor.RESIZE_ALL;
            default -> Cursor.ARROW;
        };
        moscow.rockstar.ui.input.CursorManager.request((Cursor)cursor);
    }

    public Object find(String string, String string2) {
        ModuleContract module;
        try {
            module = RockstarClient.create().getModuleRegistry().findModuleByName(string);
        }
        catch (Exception exception) {
            return null;
        }
        if (module == null) {
            return null;
        }
        if (string2 != null && !string2.isEmpty()) {
            for (Setting setting : module.getSettings()) {
                if (!Client.matchesSetting(setting, string2)) continue;
                return this.wrapSetting(setting);
            }
            return null;
        }
        return new PyModule(module);
    }

    public PyDynamicIsland island() {
        return new PyDynamicIsland();
    }

    public PyDynamicIsland dynamicIsland() {
        return this.island();
    }

    public PyMusic music() {
        return new PyMusic();
    }

    public boolean menu_opened() {
        return ClientAccess.minecraftClient.currentScreen instanceof MinecraftScreenBase || ClientAccess.minecraftClient.currentScreen instanceof OverlayElement;
    }

    public float fontWidth(String string, float f, String string2) {
        return Font.byName(string).metrics(f).measureText(string2);
    }

    /** ORIGINAL: {@code IIiIiI.I(name).I(size).I()} = {@code IIiIIi.I()F}, the line height. */
    public float fontHeight(String string, float f) {
        return Font.byName(string).metrics(f).getFontMetricsFloat();
    }

    public Object find(String string) {
        return this.find(string, null);
    }

    private static boolean matchesSetting(Setting setting, String string) {
        String string2 = setting.getName();
        if (Client.looseEquals(string2, string) || Client.looseEquals(Localization.translate(string2), string)) {
            return true;
        }
        int n = string2.lastIndexOf(46);
        return n >= 0 && n < string2.length() - 1 && Client.looseEquals(string2.substring(n + 1), string);
    }

    private static boolean looseEquals(String string, String string2) {
        if (string == null || string2 == null) {
            return false;
        }
        return string.replace(" ", "").equalsIgnoreCase(string2.replace(" ", ""));
    }

    private Object wrapSetting(Setting setting) {
        if (setting instanceof BooleanSetting) {
            return new PyBooleanSetting((BooleanSetting)setting);
        }
        if (setting instanceof NumberSetting) {
            return new PySliderSetting((NumberSetting)setting);
        }
        if (setting instanceof ModeSetting) {
            return new PyModeSetting((ModeSetting)setting);
        }
        if (setting instanceof MultiBooleanSetting) {
            return new PySelectSetting((MultiBooleanSetting)setting);
        }
        if (setting instanceof RangeSetting) {
            return new PyRangeSetting((RangeSetting)setting);
        }
        if (setting instanceof ColorSetting) {
            return new PyColorSetting((ColorSetting)setting);
        }
        if (setting instanceof ActionSetting) {
            return new PyButtonSetting((ActionSetting)setting);
        }
        return null;
    }

    public List<PyModule> modules() {
        return RockstarClient.create().getModuleRegistry().getModules().stream().map(PyModule::new).toList();
    }

    /** ORIGINAL: {@code IiIIiIII.I Ljava/io/File;} = the Rockstar config directory, not the game root. */
    public String gameDir() {
        return moscow.rockstar.api.data.ClientConfigManager.CONFIG_DIRECTORY.getAbsolutePath();
    }

    public WidgetState border(double d) {
        return WidgetState.uniform((float)d);
    }

    public WidgetState border4(double d, double d2, double d3, double d4) {
        return new WidgetState((float)d, (float)d2, (float)d3, (float)d4);
    }
}
