/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 *  net.minecraft.Identifier
 */
package pyrock.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import jep.python.PyCallable;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.ui.color.ColorPicker;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.InteractiveComponent;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.ui.text.EditableTextComponent;
import moscow.rockstar.ui.widgets.controls.Slider;
import net.minecraft.util.Identifier;
import pyrock.classes.PySetting;
import pyrock.ui.Node;
import pyrock.ui.PyScreen;
import pyrock.utility.render.ColorRGBA;

public class Ui {
    private final PyScreen screen;
    private final List<Node> collected = new ArrayList<Node>();

    public Ui(PyScreen pyScreen) {
        this.screen = pyScreen;
    }

    public Ui() {
        this.screen = null;
    }

    public void addRoot(Node node) {
        if (this.screen != null) {
            this.screen.addRoot(node);
        } else if (node != null) {
            this.collected.add(node);
        }
    }

    public Node firstRoot() {
        return this.collected.isEmpty() ? null : this.collected.get(0);
    }

    public Node column() {
        return new Node(new Component().vertical());
    }

    public Node row() {
        return new Node(new Component().horizontal());
    }

    public Node node(UiNode uiNode) {
        return new Node(uiNode);
    }

    public Node text(String string, float f, String string2, Object object) {
        TextComponent textComponent2;
        FontMetrics fontMetrics = Ui.font(string2, f);
        if (object instanceof PyCallable) {
            PyCallable pyCallable = (PyCallable)object;
            textComponent2 = new TextComponent().text(fontMetrics, string, textComponent -> Ui.callColor(pyCallable));
        } else if (object instanceof ColorRGBA) {
            ColorRGBA colorRGBA = (ColorRGBA)object;
            textComponent2 = new TextComponent().text(fontMetrics, string, colorRGBA);
        } else {
            textComponent2 = new TextComponent().text(fontMetrics, string, ColorPalette.PRIMARY_TEXT_COLOR);
        }
        textComponent2.interactive(false);
        return new Node(textComponent2);
    }

    public Node textDyn(Object object, float f, String string, Object object2) {
        TextComponent textComponent2;
        FontMetrics fontMetrics = Ui.font(string, f);
        PyCallable pyCallable = (PyCallable)object;
        Supplier<String> supplier = () -> {
            try {
                Object result = pyCallable.call(new Object[0]);
                return result == null ? "" : result.toString();
            }
            catch (Exception exception) {
                return "";
            }
        };
        if (object2 instanceof PyCallable) {
            PyCallable pyCallable2 = (PyCallable)object2;
            textComponent2 = new TextComponent().text(fontMetrics, supplier, textComponent -> Ui.callColor(pyCallable2));
        } else if (object2 instanceof ColorRGBA) {
            ColorRGBA colorRGBA = (ColorRGBA)object2;
            textComponent2 = new TextComponent().text(fontMetrics, supplier, textComponent -> colorRGBA);
        } else {
            textComponent2 = new TextComponent().text(fontMetrics, supplier, textComponent -> ColorPalette.PRIMARY_TEXT_COLOR);
        }
        textComponent2.interactive(false);
        return new Node(textComponent2);
    }

    public Node button(String string) {
        TextComponent textComponent2 = new TextComponent().background(textComponent -> ColorPalette.ACCENT_COLOR.mulAlpha(0.1f + 0.12f * textComponent.hover())).text(Ui.font("medium", 8.0f), string, ColorPalette.PRIMARY_TEXT_COLOR).radius(6.0f).padding(5.0f, 10.0f);
        textComponent2.cursor(Cursor.HAND);
        return new Node(textComponent2);
    }

    public Node switchWidget(PyCallable pyCallable, PyCallable pyCallable2) {
        InteractiveComponent interactiveComponent = new InteractiveComponent(() -> Ui.truthy(pyCallable));
        interactiveComponent.setActiveColorProvider(() -> ColorPalette.MUTED_PANEL_COLOR);
        interactiveComponent.size(16.0f, 9.0f);
        interactiveComponent.onClick(() -> {
            try {
                pyCallable2.call(new Object[]{!Ui.truthy(pyCallable)});
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.error("[PyUi] switch", (Throwable)exception);
            }
        });
        return new Node(interactiveComponent);
    }

    public Node toggle(String string, PyCallable pyCallable, PyCallable pyCallable2) {
        Component component = new Component().horizontal();
        component.fillWidth();
        component.alignment(Alignment.CENTER).overflowMode(JustifyContent.SPACE_BETWEEN).gap(6.0f);
        component.add(new TextComponent().text(Ui.font("medium", 8.0f), string, ColorPalette.PRIMARY_TEXT_COLOR).interactive(false));
        component.add(this.switchWidget(pyCallable, pyCallable2).element());
        return new Node(component);
    }

    public Node sliderBar(PyCallable pyCallable, PyCallable pyCallable2, float f2, float f3, float f4) {
        Slider slider = new Slider(() -> Ui.floatOf(pyCallable), f -> {
            try {
                pyCallable2.call(new Object[]{Float.valueOf(f)});
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.error("[PyUi] slider", (Throwable)exception);
            }
        }, f2, f3);
        if (f4 > 0.0f) {
            slider.setStepSize(f4);
        }
        return new Node(slider);
    }

    public Node slider(String string, float f2, float f3, float f4, PyCallable pyCallable, float f5) {
        float[] fArray = new float[]{f4};
        Slider slider = new Slider(() -> fArray[0], f -> {
            fArray[0] = f;
            if (pyCallable != null) {
                try {
                    pyCallable.call(new Object[]{Float.valueOf(f)});
                }
                catch (Exception exception) {
                    RockstarClient.LOGGER.error("[PyUi] slider", (Throwable)exception);
                }
            }
        }, f2, f3);
        slider.fillWidthNode();
        if (f5 > 0.0f) {
            slider.setStepSize(f5);
        }
        Component component = new Component().horizontal();
        component.fillWidth();
        component.overflowMode(JustifyContent.SPACE_BETWEEN).gap(6.0f);
        component.add(new TextComponent().text(Ui.font("medium", 8.0f), string, ColorPalette.PRIMARY_TEXT_COLOR).interactive(false));
        component.add(new TextComponent().text(Ui.font("medium", 8.0f), () -> Ui.fmt(fArray[0]), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR).interactive(false));
        Component component2 = new Component().vertical();
        component2.fillWidth();
        component2.gap(3.0f);
        component2.add(component);
        component2.add(slider);
        return new Node(component2);
    }

    public Node setting(Object object) {
        if (object instanceof PySetting) {
            PySetting pySetting = (PySetting)object;
            object = pySetting.raw();
        }
        Setting setting = (Setting)object;
        // ORIGINAL: pyrock/ui/Ui#setting calls the shared wrapper IiiIiiIIi#I(Setting), whose body is
        // rawComponent(setting).collapse().visibleWhen(setting::isVisible, easeOutQuart, 220L),
        // and rawComponent applies fillWidth + horizontal padding. Passing buildComponent() bare
        // left script-built setting rows at their natural width with no inset.
        return new Node(setting.buildComponent().fillWidth().padding(0.0f, 9.0f).collapse()
                .visibleWhen(setting::hasValidSettingValue, moscow.rockstar.ui.animation.Easing.easeOutQuart, 220L));
    }

    public Node toggleC(PyCallable pyCallable, PyCallable pyCallable2, ColorRGBA colorRGBA, ColorRGBA colorRGBA2, ColorRGBA colorRGBA3) {
        InteractiveComponent interactiveComponent = new InteractiveComponent(() -> Ui.truthy(pyCallable));
        if (colorRGBA != null) {
            interactiveComponent.setInactiveTextColor(colorRGBA);
        }
        if (colorRGBA2 != null) {
            interactiveComponent.setActiveColor(colorRGBA2);
        }
        if (colorRGBA3 != null) {
            interactiveComponent.setHoverColor(colorRGBA3);
        }
        interactiveComponent.size(38.0f, 22.0f);
        interactiveComponent.onClick(() -> {
            try {
                pyCallable2.call(new Object[0]);
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.error("[PyUi] toggle", (Throwable)exception);
            }
        });
        return new Node(interactiveComponent);
    }

    public Node sliderC(PyCallable pyCallable, PyCallable pyCallable2, float f2, float f3, float f4, ColorRGBA colorRGBA, ColorRGBA colorRGBA2, ColorRGBA colorRGBA3, ColorRGBA colorRGBA4, float f5, float f6, float f7) {
        Slider slider = new Slider(() -> Ui.floatOf(pyCallable), f -> {
            try {
                pyCallable2.call(new Object[]{Float.valueOf(f)});
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.error("[PyUi] slider", (Throwable)exception);
            }
        }, f2, f3);
        if (f4 > 0.0f) {
            slider.setStepSize(f4);
        }
        if (colorRGBA != null) {
            slider.setTrackColor(colorRGBA);
        }
        if (colorRGBA2 != null) {
            slider.setFilledTrackColor(colorRGBA2);
        }
        if (colorRGBA3 != null) {
            slider.setThumbBorderColor(colorRGBA3);
        }
        if (colorRGBA4 != null) {
            slider.setThumbFillColor(colorRGBA4);
        }
        if (f5 >= 0.0f) {
            slider.setThumbRadius(f5);
        }
        if (f6 >= 0.0f) {
            slider.setThumbInset(f6);
        }
        if (f7 > 0.0f) {
            slider.setTrackHeight(f7);
        }
        slider.fillWidthNode();
        return new Node(slider);
    }

    public Node textInput(PyCallable pyCallable, PyCallable pyCallable2, String string2, ColorRGBA colorRGBA, ColorRGBA colorRGBA2) {
        Object object;
        String string3 = "";
        try {
            object = pyCallable.call(new Object[0]);
            string3 = object == null ? "" : object.toString();
        }
        catch (Exception exception) {
            // empty catch block
        }
        EditableTextComponent textEditor = new EditableTextComponent(Ui.font("regular", 7.0f), string3, string -> {
            try {
                pyCallable2.call(new Object[]{string});
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.error("[PyUi] text", (Throwable)exception);
            }
        });
        textEditor.placeholder(string2 != null ? string2 : "");
        if (colorRGBA != null) {
            textEditor.background(colorRGBA);
        }
        if (colorRGBA2 != null) {
            textEditor.textColor(colorRGBA2);
        }
        // ORIGINAL: I(6F) = radius, i() = fillWidth (no-arg), Ii(22F) = height. There is no padding call.
        textEditor.radius(6.0f);
        textEditor.fillWidth();
        textEditor.height(22.0f);
        return new Node(textEditor);
    }

    public Node swatch(PyCallable pyCallable) {
        ColorPicker colorPicker = new ColorPicker(() -> {
            try {
                ColorRGBA colorRGBA;
                Object object = pyCallable.call(new Object[0]);
                return object instanceof ColorRGBA ? (colorRGBA = (ColorRGBA)object) : ColorPalette.WHITE;
            }
            catch (Exception exception) {
                return ColorPalette.WHITE;
            }
        });
        colorPicker.setSize(14.0f, 14.0f);
        colorPicker.interactive(false);
        return new Node(colorPicker);
    }

    public Node icon(String string, float f, ColorRGBA colorRGBA) {
        TextComponent textComponent = new TextComponent().icon(string, f, colorRGBA != null ? colorRGBA : ColorPalette.PRIMARY_TEXT_COLOR);
        textComponent.size(f, f);
        return new Node(textComponent);
    }

    public Node image(Object object, float f, float f2, Object object2) {
        Identifier class_29602 = (Identifier)object;
        TextComponent textComponent2 = new TextComponent();
        if (object2 instanceof PyCallable) {
            PyCallable pyCallable = (PyCallable)object2;
            textComponent2.image(class_29602, f, f2, (TextComponent textComponent) -> Ui.callColor(pyCallable));
        } else if (object2 instanceof ColorRGBA) {
            ColorRGBA colorRGBA = (ColorRGBA)object2;
            textComponent2.image(class_29602, f, f2, colorRGBA);
        } else {
            textComponent2.image(class_29602, f, f2, (ColorRGBA)null);
        }
        textComponent2.size(f, f);
        textComponent2.interactive(false);
        return new Node(textComponent2);
    }

    public Node space(float f) {
        return new Node(new TextComponent().size(f, f));
    }

    public Node divider() {
        TextComponent textComponent = new TextComponent().background(ColorPalette.BORDER_COLOR);
        textComponent.fillWidth();
        textComponent.height(1.0f);
        return new Node(textComponent);
    }

    public ColorRGBA color(String string) {
        if (string == null) {
            return ColorPalette.PRIMARY_TEXT_COLOR;
        }
        return switch (string.toLowerCase()) {
            case "accent" -> ColorPalette.ACCENT_COLOR;
            case "text" -> ColorPalette.PRIMARY_TEXT_COLOR;
            case "background", "bg" -> ColorPalette.getPanelColor();
            case "second" -> ColorPalette.MUTED_PANEL_COLOR;
            case "outline" -> ColorPalette.BORDER_COLOR;
            case "white" -> ColorPalette.WHITE;
            default -> ColorPalette.PRIMARY_TEXT_COLOR;
        };
    }

    private static FontMetrics font(String string, float f) {
        float f2 = f > 0.0f ? f : 8.0f;
        return Font.byName(string == null ? "medium" : string).metrics(f2);
    }

    private static String fmt(float f) {
        return (double)f == Math.rint(f) ? String.valueOf((int)f) : String.format("%.1f", Float.valueOf(f));
    }

    private static boolean truthy(PyCallable pyCallable) {
        try {
            return Boolean.TRUE.equals(pyCallable.call(new Object[0]));
        }
        catch (Exception exception) {
            return false;
        }
    }

    private static float floatOf(PyCallable pyCallable) {
        try {
            float f;
            Object object = pyCallable.call(new Object[0]);
            if (object instanceof Number) {
                Number number = (Number)object;
                f = number.floatValue();
            } else {
                f = 0.0f;
            }
            return f;
        }
        catch (Exception exception) {
            return 0.0f;
        }
    }

    private static ColorRGBA callColor(PyCallable pyCallable) {
        try {
            ColorRGBA colorRGBA;
            Object object = pyCallable.call(new Object[0]);
            return object instanceof ColorRGBA ? (colorRGBA = (ColorRGBA)object) : ColorPalette.PRIMARY_TEXT_COLOR;
        }
        catch (Exception exception) {
            return ColorPalette.PRIMARY_TEXT_COLOR;
        }
    }
}
