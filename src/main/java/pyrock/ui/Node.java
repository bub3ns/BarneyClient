/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 */
package pyrock.ui;

import java.util.function.BooleanSupplier;
import jep.python.PyCallable;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.color.ColorPickerHost;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.DragMode;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.input.ScrollMode;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.text.TextComponent;
import pyrock.utility.render.ColorRGBA;

public class Node {
    private static final ColorRGBA TRANSPARENT = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
    private final UiNode el;
    private static final long VIS_THROTTLE_MS = 50L;
    private static long visStagger = 0L;

    Node(UiNode uiNode) {
        this.el = uiNode;
    }

    public UiNode element() {
        return this.el;
    }

    public Node add(Node node) {
        UiNode uiNode;
        if (node != null && (uiNode = this.el) instanceof Component) {
            Component component = (Component)uiNode;
            component.add(node.el);
        }
        return this;
    }

    public Node clear() {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.clear();
        }
        return this;
    }

    public Node width(float f) {
        this.el.width(f);
        return this;
    }

    public Node height(float f) {
        this.el.height(f);
        return this;
    }

    public Node size(float f, float f2) {
        this.el.size(f, f2);
        return this;
    }

    public Node minSize(float f, float f2) {
        this.el.minSize(f, f2);
        return this;
    }

    public Node maxSize(float f, float f2) {
        this.el.maxSize(f, f2);
        return this;
    }

    public Node minWidth(float f) {
        this.el.minWidth(f);
        return this;
    }

    public Node minHeight(float f) {
        this.el.minHeight(f);
        return this;
    }

    public Node fill() {
        this.el.fill();
        return this;
    }

    public Node fillWidth() {
        this.el.fillWidth();
        return this;
    }

    public Node fillHeight() {
        this.el.fillHeight();
        return this;
    }

    public Node at(float f, float f2) {
        this.el.at(f, f2);
        return this;
    }

    public Node center() {
        this.el.center();
        return this;
    }

    public Node centerX() {
        this.el.centerX();
        return this;
    }

    public Node centerY() {
        this.el.centerY();
        return this;
    }

    public Node animatePosition() {
        this.el.animatePosition();
        return this;
    }

    public Node snapPosition() {
        this.el.snapPosition();
        return this;
    }

    public Node direction(String string) {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.layout(Node.parseDir(string));
        }
        return this;
    }

    public Node column() {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.vertical();
        }
        return this;
    }

    public Node row() {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.horizontal();
        }
        return this;
    }

    public Node gap(float f) {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.gap(f);
        }
        return this;
    }

    public Node columns(int n) {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.columns(n);
        }
        return this;
    }

    public Node wrap() {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.wrapContent();
        }
        return this;
    }

    public Node align(String string) {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.alignment(Node.parseAlign(string));
        }
        return this;
    }

    public Node justify(String string) {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.overflowMode(Node.parseJustify(string));
        }
        return this;
    }

    public Node scrollable() {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.scrollable();
        }
        return this;
    }

    public Node stack() {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.uniformLayout();
        }
        return this;
    }

    public Node scrollbar(String string) {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.computeLayout(Node.parseScroll(string));
        }
        return this;
    }

    public Node stagger(float f) {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.exitStagger(f);
        }
        return this;
    }

    public Node pad(float f) {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.padding(f);
        } else {
            uiNode = this.el;
            if (uiNode instanceof TextComponent) {
                TextComponent textComponent = (TextComponent)uiNode;
                textComponent.padding(f);
            }
        }
        return this;
    }

    public Node pad(float f, float f2) {
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.padding(f, f2);
        } else {
            uiNode = this.el;
            if (uiNode instanceof TextComponent) {
                TextComponent textComponent = (TextComponent)uiNode;
                textComponent.padding(f, f2);
            }
        }
        return this;
    }

    public Node color(ColorRGBA colorRGBA) {
        if (colorRGBA == null) {
            return this;
        }
        UiNode uiNode = this.el;
        if (uiNode instanceof TextComponent) {
            TextComponent textComponent = (TextComponent)uiNode;
            textComponent.background(colorRGBA);
        } else {
            uiNode = this.el;
            if (uiNode instanceof Component) {
                Component component = (Component)uiNode;
                component.background(colorRGBA);
            }
        }
        return this;
    }

    public Node colorFn(PyCallable pyCallable) {
        if (pyCallable == null) {
            return this;
        }
        UiNode uiNode = this.el;
        if (uiNode instanceof TextComponent) {
            TextComponent textComponent2 = (TextComponent)uiNode;
            textComponent2.background(textComponent -> Node.safeColor(pyCallable));
        } else {
            uiNode = this.el;
            if (uiNode instanceof Component) {
                Component component2 = (Component)uiNode;
                component2.background(component -> Node.safeColor(pyCallable));
            }
        }
        return this;
    }

    public Node radius(float f) {
        UiNode uiNode = this.el;
        if (uiNode instanceof TextComponent) {
            TextComponent textComponent = (TextComponent)uiNode;
            textComponent.radius(f);
        } else {
            uiNode = this.el;
            if (uiNode instanceof Component) {
                Component component = (Component)uiNode;
                component.cornerRadius(f);
            }
        }
        return this;
    }

    public Node squircle(float f) {
        UiNode uiNode = this.el;
        if (uiNode instanceof TextComponent) {
            TextComponent textComponent = (TextComponent)uiNode;
            textComponent.squircle(f);
        } else {
            uiNode = this.el;
            if (uiNode instanceof Component) {
                Component component = (Component)uiNode;
                component.squircle(f);
            }
        }
        return this;
    }

    public Node radiusBottom(float f) {
        WidgetState widgetState = WidgetState.bottom(f, f);
        UiNode uiNode = this.el;
        if (uiNode instanceof TextComponent) {
            TextComponent textComponent = (TextComponent)uiNode;
            textComponent.radius(widgetState);
        } else {
            uiNode = this.el;
            if (uiNode instanceof Component) {
                Component component = (Component)uiNode;
                component.cornerRadius(widgetState);
            }
        }
        return this;
    }

    public Node blur(float f) {
        this.el.blur(f);
        return this;
    }

    public Node blur(float f, ColorRGBA colorRGBA) {
        this.el.blur(f, colorRGBA);
        return this;
    }

    public Node glass() {
        this.el.glass();
        return this;
    }

    public Node glass(float f, boolean bl) {
        this.el.glass(f, bl);
        return this;
    }

    public Node border(float f, ColorRGBA colorRGBA) {
        UiNode uiNode = this.el;
        if (uiNode instanceof TextComponent) {
            TextComponent textComponent = (TextComponent)uiNode;
            textComponent.border(f, colorRGBA);
        }
        return this;
    }

    public Node textAlign(String string) {
        UiNode uiNode = this.el;
        if (uiNode instanceof TextComponent) {
            TextComponent textComponent = (TextComponent)uiNode;
            textComponent.textAlign(Node.parseAlign(string));
        }
        return this;
    }

    public Node textShadow(ColorRGBA colorRGBA, float f, float f2, float f3) {
        UiNode uiNode = this.el;
        if (uiNode instanceof TextComponent) {
            TextComponent textComponent = (TextComponent)uiNode;
            textComponent.textShadow(colorRGBA, f, f2, f3);
        }
        return this;
    }

    public Node icon(String string, float f, ColorRGBA colorRGBA) {
        UiNode uiNode = this.el;
        if (uiNode instanceof TextComponent) {
            TextComponent textComponent = (TextComponent)uiNode;
            textComponent.icon(string, f, colorRGBA != null ? colorRGBA : ColorPalette.PRIMARY_TEXT_COLOR);
        }
        return this;
    }

    public Node fade() {
        UiNode uiNode = this.el;
        if (uiNode instanceof TextComponent) {
            TextComponent textComponent = (TextComponent)uiNode;
            textComponent.fade();
        }
        return this;
    }

    public Node onClick(PyCallable pyCallable) {
        if (pyCallable != null) {
            this.el.onClick(() -> {
                try {
                    pyCallable.call(new Object[0]);
                }
                catch (Exception exception) {
                    RockstarClient.LOGGER.error("[PyUi] onClick", (Throwable)exception);
                }
            });
        }
        return this;
    }

    public Node onClickPos(PyCallable pyCallable) {
        if (pyCallable != null) {
            this.el.onClick((PointerAction pointerAction, float f, float f2) -> {
                try {
                    pyCallable.call(new Object[]{Float.valueOf(f), Float.valueOf(f2), pointerAction.name().toLowerCase()});
                }
                catch (Exception exception) {
                    RockstarClient.LOGGER.error("[PyUi] onClick", (Throwable)exception);
                }
            });
        }
        return this;
    }

    public Node interactive(boolean bl) {
        this.el.interactive(bl);
        return this;
    }

    public Node cursor(String string) {
        this.el.cursor(Node.parseCursor(string));
        return this;
    }

    public Node draggable() {
        this.el.draggable(DragMode.BOTH);
        return this;
    }

    public Node draggable(String string) {
        this.el.draggable(Node.parseDrag(string));
        return this;
    }

    public Node visibleWhen(PyCallable pyCallable) {
        if (pyCallable != null) {
            this.el.visibleWhen(Node.throttledBool(pyCallable));
        }
        return this;
    }

    public Node enter(String string) {
        this.el.enter(Node.parseTransition(string));
        return this;
    }

    public Node exit(String string) {
        this.el.exit(Node.parseTransition(string));
        return this;
    }

    public Node enterSlide(float f) {
        this.el.enter(Transition.slideUp(f));
        return this;
    }

    public Node exitSlide(float f) {
        this.el.exit(Transition.slideUp(f));
        return this;
    }

    public Node motion(String string) {
        this.el.motion(Node.parseMotion(string));
        return this;
    }

    public Node sticky() {
        this.el.sticky();
        return this;
    }

    public Node collapse() {
        this.el.collapse();
        return this;
    }

    public Node bind(String string, PyCallable pyCallable) {
        if (pyCallable != null) {
            this.el.bind(string, Node.boolSupplier(pyCallable));
        }
        return this;
    }

    public Node window(float f, float f2, String string) {
        this.el.size(f, f2).center().draggable(DragMode.BOTH);
        UiNode uiNode = this.el;
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            component.padding(10.0f).gap(8.0f).cornerRadius(10.0f).squircle(6.0f).background(ColorPalette.getPanelColor().withAlpha(235.0f));
        }
        return this;
    }

    public float w() {
        return this.el.w();
    }

    public float h() {
        return this.el.h();
    }

    public boolean hovered() {
        return this.el.hovered();
    }

    private static BooleanSupplier boolSupplier(PyCallable pyCallable) {
        return () -> {
            try {
                return Boolean.TRUE.equals(pyCallable.call(new Object[0]));
            }
            catch (Exception exception) {
                return false;
            }
        };
    }

    private static BooleanSupplier throttledBool(final PyCallable pyCallable) {
        final long l = visStagger++ * 7L % 50L;
        return new BooleanSupplier(){
            boolean cached;
            boolean inited;
            long nextEval;

            @Override
            public boolean getAsBoolean() {
                long l2 = ColorPickerHost.renderClock();
                if (!this.inited) {
                    this.inited = true;
                    this.cached = this.eval();
                    this.nextEval = l2 + l;
                } else if (l2 >= this.nextEval) {
                    this.cached = this.eval();
                    this.nextEval = l2 + 50L;
                }
                return this.cached;
            }

            private boolean eval() {
                try {
                    return Boolean.TRUE.equals(pyCallable.call(new Object[0]));
                }
                catch (Exception exception) {
                    return false;
                }
            }
        };
    }

    private static ColorRGBA safeColor(PyCallable pyCallable) {
        try {
            ColorRGBA colorRGBA;
            Object object = pyCallable.call(new Object[0]);
            return object instanceof ColorRGBA ? (colorRGBA = (ColorRGBA)object) : TRANSPARENT;
        }
        catch (Exception exception) {
            return TRANSPARENT;
        }
    }

    private static Alignment parseAlign(String string) {
        if (string == null) {
            return Alignment.START;
        }
        return switch (string.toLowerCase()) {
            case "center" -> Alignment.CENTER;
            case "end" -> Alignment.END;
            case "stretch" -> Alignment.STRETCH;
            default -> Alignment.START;
        };
    }

    private static JustifyContent parseJustify(String string) {
        if (string == null) {
            return JustifyContent.START;
        }
        return switch (string.toLowerCase()) {
            case "center" -> JustifyContent.CENTER;
            case "end" -> JustifyContent.END;
            case "between", "space_between" -> JustifyContent.SPACE_BETWEEN;
            case "around", "space_around" -> JustifyContent.SPACE_AROUND;
            case "evenly", "space_evenly" -> JustifyContent.SPACE_EVENLY;
            default -> JustifyContent.START;
        };
    }

    private static Layout parseDir(String string) {
        if (string == null) {
            return Layout.COLUMN;
        }
        return switch (string.toLowerCase()) {
            case "up" -> Layout.COLUMN_REVERSE;
            case "right", "row", "horizontal" -> Layout.ROW;
            case "left" -> Layout.ROW_REVERSE;
            default -> Layout.COLUMN;
        };
    }

    private static ScrollMode parseScroll(String string) {
        if (string == null) {
            return ScrollMode.AUTO;
        }
        return switch (string.toLowerCase()) {
            case "always" -> ScrollMode.ALWAYS;
            case "never" -> ScrollMode.NEVER;
            default -> ScrollMode.AUTO;
        };
    }

    private static DragMode parseDrag(String string) {
        if (string == null) {
            return DragMode.BOTH;
        }
        return switch (string.toLowerCase()) {
            case "x", "horizontal" -> DragMode.HORIZONTAL;
            case "y", "vertical" -> DragMode.VERTICAL;
            case "none" -> DragMode.NONE;
            default -> DragMode.BOTH;
        };
    }

    private static Cursor parseCursor(String string) {
        if (string == null) {
            return Cursor.ARROW;
        }
        return switch (string.toLowerCase()) {
            case "hand", "pointer" -> Cursor.HAND;
            case "text", "ibeam" -> Cursor.IBEAM;
            case "crosshair" -> Cursor.CROSSHAIR;
            case "hresize", "horizontal" -> Cursor.HORIZONTAL_RESIZE;
            case "vresize", "vertical" -> Cursor.VERTICAL_RESIZE;
            case "block", "notallowed" -> Cursor.NOT_ALLOWED;
            case "resize", "resizeall" -> Cursor.RESIZE_ALL;
            default -> Cursor.ARROW;
        };
    }

    private static Motion parseMotion(String string) {
        if (string == null) {
            return Motion.motion;
        }
        return switch (string.toLowerCase()) {
            case "fast" -> Motion.motion4;
            case "smooth" -> Motion.motion3;
            case "signal" -> Motion.motion5;
            case "spring" -> Motion.motion6;
            case "spring_snap" -> Motion.motion7;
            case "soft", "bakek_soft" -> Motion.motion2;
            default -> Motion.motion;
        };
    }

    private static Transition parseTransition(String string) {
        if (string == null) {
            return Transition.PROGRESS_ONLY;
        }
        return switch (string.toLowerCase()) {
            case "none" -> Transition.NO_OP;
            case "vanish" -> Transition.HIDDEN;
            case "fade_slide" -> Transition.SLIDE_UP_SHORT;
            case "up", "slide_up" -> Transition.SLIDE_UP;
            case "down", "slide_down" -> Transition.SLIDE_DOWN;
            case "left", "slide_left" -> Transition.SLIDE_RIGHT;
            case "right", "slide_right" -> Transition.SLIDE_LEFT;
            case "pop" -> Transition.SCALE;
            case "fade_pop" -> Transition.SCALE_UP;
            default -> Transition.PROGRESS_ONLY;
        };
    }
}

