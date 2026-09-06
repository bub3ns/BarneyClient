package moscow.rockstar.modules.visuals.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventHandlerRecord;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.colors.GradientColors;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.input.KeyDisplayFormatter;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import pyrock.events.client.ModuleToggledEvent;

/** Live module keybind list. 1:1 with rockstar/ilIlil/IiIiIiiII. */
public class KeybindsHud extends HudElement {
    private final BooleanSetting alwaysDisplay = new BooleanSetting(this, "hud.always_display");
    private final BooleanSetting uniformWidth = new BooleanSetting(this, "hud.uniform_width");
    private boolean rightSide;
    private Component list;
    private UiNode header;
    private final Map<ModuleContract, UiNode> rows = new HashMap<>();
    private final List<ModuleContract> bound = new ArrayList<>();
    private List<ModuleContract> lastBound = List.of();
    private final Transition rowTransition;

    private final EventListener<ModuleToggledEvent> moduleToggledListener =
        event -> this.refreshModule(event.getModule().getModule(), false);
    private final EventListener<EventHandlerRecord> keyBindChangedListener =
        event -> this.refreshModule(event.getModule(), true);

    public KeybindsHud() {
        super("hud.keybinds", "keyboard");
        this.rowTransition = (progress, node, state) -> {
            state.progress = progress;
            state.offsetX = (this.rightSide ? 6.0f : -6.0f) * (1.0f - progress);
        };
        this.collectModules();
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    @Override
    protected Component build() {
        this.rows.clear();
        this.lastBound = List.of();
        this.list = new Component().vertical().alignment(this.alignment()).gap(1.0f);
        this.header = new Component().horizontal().alignment(Alignment.CENTER).gap(3.0f).height(11.0f)
            .padding(Insets.horizontal(3.0f))
            .renderHook((ctx, node) -> {
                ctx.drawClientRect(node.x(), node.y(), node.w(), node.h(),
                    this.animation.getValue(), this.dragAnim.getValue(), 3.0f, 3.0f);
                ctx.drawSquircle(node.x(), node.y(), 23.0f, node.h(), 3.0f, WidgetState.uniform(3.0f),
                    new GradientColors(ColorPalette.VIBRANT_ACCENT_COLOR.mulAlpha(0.1f),
                                       ColorPalette.VIBRANT_ACCENT_COLOR.mulAlpha(0.0f)));
            })
            .add(new TextComponent().size(7.0f, 7.0f).interactive(false)
                .icon("keyboard", 7.0f, ColorPalette.ACCENT_COLOR))
            .add(new TextComponent().interactive(false)
                .text(Font.MEDIUM.metrics(6.0f), () -> Localization.translate(this.getName()),
                      node -> ColorPalette.PRIMARY_TEXT_COLOR));
        this.list.add(this.header);
        this.collectModules();
        this.rebuildRows(true);
        return this.list;
    }

    private UiNode row(final ModuleContract module) {
        UiNode node = this.rows.computeIfAbsent(module, key -> new Component()
            .horizontal().alignment(Alignment.CENTER).gap(1.0f).transition(this.rowTransition)
            .add(new TextComponent().interactive(false).fillWidth().height(10.0f).radius(2.0f)
                .padding(Insets.horizontal(3.0f))
                .background(n -> ColorPalette.PANEL_COLOR.mulAlpha(0.889f))
                .text(Font.REGULAR.metrics(6.0f), key::getName, n -> ColorPalette.PRIMARY_TEXT_COLOR))
            .add(new TextComponent().interactive(false).height(10.0f).radius(2.0f)
                .padding(Insets.horizontal(3.0f))
                .background(n -> ColorPalette.PANEL_COLOR.mulAlpha(0.94f))
                .textAlign(Alignment.CENTER)
                .text(Font.REGULAR.metrics(6.0f),
                      () -> KeyDisplayFormatter.formatKey(key.getKeyBind()),
                      n -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f))));
        if (node.phase() == UiNode.LifecyclePhase.EXITING
                || node.phase() == UiNode.LifecyclePhase.DISCARDED
                || node.phase() == UiNode.LifecyclePhase.HIDDEN) {
            node.beginEnter(0.0f);
        }
        return node;
    }

    private void rebuildRows(boolean force) {
        if (!force && this.bound.equals(this.lastBound)) {
            return;
        }
        this.lastBound = List.copyOf(this.bound);
        FontMetrics font = Font.REGULAR.metrics(6.0f);
        Map<ModuleContract, Float> widths = new HashMap<>();
        for (ModuleContract module : this.bound) {
            widths.put(module, font.measureText(module.getName())
                + font.measureText(KeyDisplayFormatter.formatKey(module.getKeyBind())));
        }
        List<UiNode> children = new ArrayList<>();
        children.add(this.header);
        this.bound.stream()
            .sorted(Comparator.comparingDouble((ModuleContract m) -> widths.get(m).floatValue()).reversed())
            .forEach(module -> children.add(this.row(module)));
        this.list.updateChildren(children);
    }

    private void collectModules() {
        this.bound.clear();
        for (ModuleContract module : RockstarClient.create().getModuleRegistry().getModules()) {
            if (!this.isBound(module)) {
                continue;
            }
            this.bound.add(module);
        }
    }

    private void refreshModule(ModuleContract module, boolean force) {
        boolean bound = this.isBound(module);
        boolean present = this.bound.contains(module);
        if (!(bound != present || (force && bound))) {
            return;
        }
        if (bound && !present) {
            this.bound.add(module);
        } else if (!bound) {
            this.bound.remove(module);
        }
        if (this.list != null) {
            this.rebuildRows(force);
        }
    }

    private boolean isBound(ModuleContract module) {
        return !(module instanceof Menu) && module.isEnabled() && module.getKeyBind() != -1;
    }

    @Override
    public void update(RockstarDrawContext drawContext) {
        this.rightSide = this.x + this.width / 2.0f >= WindowMetricsProvider.INSTANCE.width() / 2.0f;
        if (this.list != null) {
            this.list.alignment(this.alignment());
        }
        super.update(drawContext);
    }

    private Alignment alignment() {
        return this.uniformWidth.isEnabled()
            ? Alignment.STRETCH
            : (this.rightSide ? Alignment.END : Alignment.START);
    }

    @Override
    public boolean show() {
        return this.alwaysDisplay.isEnabled()
            || MinecraftClient.getInstance().currentScreen instanceof ChatScreen
            || !this.bound.isEmpty();
    }
}
