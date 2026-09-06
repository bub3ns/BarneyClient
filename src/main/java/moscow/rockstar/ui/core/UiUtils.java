/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Vector2f
 *  org.lwjgl.glfw.GLFW
 */
package moscow.rockstar.ui.core;

import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingComponent;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.settings.SettingWidget;
import net.minecraft.client.util.math.Vector2f;
import org.lwjgl.glfw.GLFW;
import pyrock.utility.render.Rect;

public final class UiUtils {
    public static float center(float f, float f2) {
        return (float)Math.ceil(f2 / 2.0f - f / 2.0f);
    }

    public static double center(double d, double d2) {
        return Math.ceil(d2 / 2.0 - d / 2.0);
    }

    public static boolean contains(double d, double d2, double d3, double d4, int n, int n2) {
        return (double)n >= d && (double)n < d + d3 && (double)n2 >= d2 && (double)n2 < d2 + d4;
    }

    public static boolean contains(double d, double d2, double d3, double d4, RockstarDrawContext drawContext) {
        return UiUtils.contains(d, d2, d3, d4, drawContext.mouseX(), drawContext.mouseY());
    }

    public static boolean contains(Rect rect, double d, double d2) {
        return UiUtils.contains((double)rect.getX(), (double)rect.getY(), (double)rect.getWidth(), (double)rect.getHeight(), d, d2);
    }

    public static boolean contains(SettingWidget settingWidget, double d, double d2) {
        return UiUtils.contains((double)settingWidget.getX(), (double)settingWidget.getY(), (double)settingWidget.getWidth(), (double)settingWidget.getHeight(), d, d2);
    }

    public static boolean contains(double d, double d2, double d3, double d4, double d5, double d6) {
        return d5 >= d && d5 < d + d3 && d6 >= d2 && d6 < d2 + d4;
    }

    public static float interpolateClamped(float f, float f2, float f3, float f4, double d) {
        return (float)(Math.min(1.0, Math.max(0.0, (d - (double)f3) / (double)f4)) * (double)(f2 - f)) + f;
    }

    public static float interpolate(float f, float f2, float f3, float f4, double d) {
        return (float)((d - (double)f3) / (double)f4 * (double)(f2 - f)) + f;
    }

    public static float normalize(float f, float f2, float f3) {
        return (f - f2) / (f3 - f2);
    }

    public static Vector2f mousePosition() {
        return new Vector2f((float)(ClientAccess.minecraftClient.mouse.getX() / WindowMetricsProvider.INSTANCE.scaleFactor()), (float)(ClientAccess.minecraftClient.mouse.getY() / WindowMetricsProvider.INSTANCE.scaleFactor()));
    }

    public static boolean isMouseButtonDown(int n) {
        return GLFW.glfwGetMouseButton((long)ClientAccess.minecraftClient.getWindow().getHandle(), (int)n) == 1;
    }

    public static SettingComponent createSettingWidget(Setting setting, SettingWidget settingWidget) {
        if (setting == null || settingWidget == null) {
            return null;
        }
        SettingComponent component = null;
        if (setting instanceof moscow.rockstar.settings.IntegerSetting integerSetting) {
            component = new moscow.rockstar.ui.widgets.settings.KeyBindSettingComponent(integerSetting, settingWidget);
        } else if (setting instanceof moscow.rockstar.settings.ColorSetting colorSetting) {
            component = new moscow.rockstar.ui.widgets.settings.ColorSettingComponent(colorSetting, settingWidget);
        } else if (setting instanceof moscow.rockstar.settings.ColorRangeSetting colorRangeSetting) {
            component = new moscow.rockstar.ui.widgets.settings.ColorRangeSettingComponent(colorRangeSetting, settingWidget);
        } else if (setting instanceof moscow.rockstar.settings.ModeSetting modeSetting) {
            component = new moscow.rockstar.ui.widgets.settings.ModeSettingComponent(modeSetting, settingWidget);
        } else if (setting instanceof moscow.rockstar.settings.RangeSetting rangeSetting) {
            component = new moscow.rockstar.ui.widgets.settings.RangeSettingComponent(rangeSetting, settingWidget);
        } else if (setting instanceof moscow.rockstar.settings.BlockItemSetting blockItemSetting) {
            component = new moscow.rockstar.ui.widgets.settings.BlockItemSettingComponent(blockItemSetting, settingWidget);
        } else if (setting instanceof moscow.rockstar.settings.MultiBooleanSetting multiBooleanSetting) {
            component = new moscow.rockstar.ui.widgets.settings.MultiBooleanSettingComponent(multiBooleanSetting, settingWidget);
        } else if (setting instanceof moscow.rockstar.settings.NumberSetting numberSetting) {
            component = new moscow.rockstar.ui.widgets.settings.NumberSettingComponent(numberSetting, settingWidget);
        }
        // NOTE: the original also has arms for BooleanSetting, Vector2Setting, EasingSetting,
        // ActionSetting, StringSetting and TextLabelSetting. Their component classes
        // (IiiIiiiii, IiiiIIiIi, IiiIiiiII, IiiiIIIII, IiiiIiIIi, IiiiIIIii) have not been
        // ported yet; restore them and add their arms in the order listed above.
        if (component != null) {
            component.tick();
        }
        return component;
    }

    @Generated
    private UiUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
