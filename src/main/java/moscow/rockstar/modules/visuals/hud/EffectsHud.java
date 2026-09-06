package moscow.rockstar.modules.visuals.hud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.colors.GradientColors;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.StatusEffectNotification;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import pyrock.utility.render.ColorRGBA;

/** Status effect list. 1:1 with rockstar/ilIlil/IiIiIiIiI. */
public class EffectsHud extends HudElement {
    private final BooleanSetting alwaysDisplay = new BooleanSetting(this, "hud.always_display");
    private final BooleanSetting grouping;
    private final BooleanSetting alert;
    private final BooleanSetting uniformWidth;
    final Map<String, List<StatusEffectInstance>> effectsByKey;
    private final List<String> orderedKeys;
    private Map<String, StatusEffectInstance> previousEffects;
    private final Map<String, UiNode> rows;
    private final Set<String> lastKeys;
    private boolean rightSide;
    private Component list;
    private UiNode header;
    private final Transition rowTransition;

    public EffectsHud() {
        super("hud.effects", "hud/potion");
        this.grouping = new BooleanSetting(this, "hud.effects.grouping");
        this.alert = new BooleanSetting(this, "hud.effects.alert");
        this.uniformWidth = new BooleanSetting(this, "hud.uniform_width");
        this.effectsByKey = new LinkedHashMap<>();
        this.orderedKeys = new ArrayList<>();
        this.previousEffects = new HashMap<>();
        this.rows = new HashMap<>();
        this.lastKeys = new HashSet<>();
        this.rowTransition = (progress, node, state) -> {
            state.progress = progress;
            state.offsetX = (this.rightSide ? 6.0f : -6.0f) * (1.0f - progress);
        };
    }

    @Override
    protected Component build() {
        this.rows.clear();
        this.lastKeys.clear();
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
                .icon("hud/potion", 7.0f, ColorPalette.ACCENT_COLOR))
            .add(new TextComponent().interactive(false)
                .text(Font.MEDIUM.metrics(6.0f), () -> Localization.translate(this.getName()),
                      node -> ColorPalette.PRIMARY_TEXT_COLOR));
        this.list.add(this.header);
        this.refreshRows();
        return this.list;
    }

    private UiNode row(final String key) {
        UiNode node = this.rows.computeIfAbsent(key, k -> {
            FontMetrics font = Font.REGULAR.metrics(6.0f);
            boolean grouped = k.startsWith("g:");
            IconStrip strip = new IconStrip(k);
            if (grouped) {
                strip.fillWidth();
            }
            Component row = new Component().horizontal().alignment(Alignment.CENTER).gap(1.0f)
                .transition(this.rowTransition).add(strip);
            if (!grouped) {
                row.add(new TextComponent().interactive(false).fillWidth().height(10.0f).radius(2.0f)
                    .padding(Insets.horizontal(3.0f))
                    .background(n -> ColorPalette.PANEL_COLOR.mulAlpha(0.889f))
                    .text(font, () -> this.effectName(k), n -> ColorPalette.PRIMARY_TEXT_COLOR));
            }
            row.add(new TextComponent().interactive(false).height(10.0f).minWidth(18.0f).radius(2.0f)
                .padding(Insets.horizontal(3.0f))
                .background(n -> ColorPalette.PANEL_COLOR.mulAlpha(0.94f))
                .textAlign(Alignment.CENTER)
                .text(font, () -> this.effectTime(k),
                      n -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f)));
            return row;
        });
        if (node.phase() == UiNode.LifecyclePhase.EXITING
                || node.phase() == UiNode.LifecyclePhase.DISCARDED
                || node.phase() == UiNode.LifecyclePhase.HIDDEN) {
            node.beginEnter(0.0f);
        }
        return node;
    }

    private void refreshRows() {
        Set<String> keys = new HashSet<>(this.orderedKeys);
        if (keys.equals(this.lastKeys)) {
            return;
        }
        this.lastKeys.clear();
        this.lastKeys.addAll(keys);
        FontMetrics font = Font.REGULAR.metrics(6.0f);
        Map<String, Float> widths = new HashMap<>();
        for (String key : this.orderedKeys) {
            widths.put(key, this.rowWidth(key, font));
        }
        List<String> sorted = new ArrayList<>(this.orderedKeys);
        sorted.sort(Comparator.comparingDouble((String k) -> widths.get(k).floatValue()).reversed());
        List<UiNode> children = new ArrayList<>();
        children.add(this.header);
        for (String key : sorted) {
            children.add(this.row(key));
        }
        this.list.updateChildren(children);
    }

    private float rowWidth(String key, FontMetrics font) {
        List<StatusEffectInstance> list = this.effectsByKey.get(key);
        int icons = list == null || list.isEmpty() ? 1 : list.size();
        float iconWidth = 4.0f + (float)icons * 10.0f;
        float nameWidth = key.startsWith("g:") ? 0.0f : font.measureText(this.effectName(key));
        float timeWidth = Math.max(18.0f, font.measureText("00:00"));
        return iconWidth + nameWidth + timeWidth;
    }

    @Override
    public void update(RockstarDrawContext drawContext) {
        this.rightSide = this.x + this.width / 2.0f >= WindowMetricsProvider.INSTANCE.width() / 2.0f;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.world != null) {
            this.collectEffects();
        }
        if (this.list != null) {
            this.list.alignment(this.alignment());
            this.refreshRows();
        }
        super.update(drawContext);
    }

    private Alignment alignment() {
        return this.uniformWidth.isEnabled()
            ? Alignment.STRETCH
            : (this.rightSide ? Alignment.END : Alignment.START);
    }

    private void collectEffects() {
        Collection<StatusEffectInstance> active = MinecraftClient.getInstance().player.getStatusEffects();
        TreeMap<String, StatusEffectInstance> current = new TreeMap<>();
        for (StatusEffectInstance instance : active) {
            StatusEffect effect = instance.getEffectType().value();
            String name = effect.getName().getString();
            if (name == null
                    || ServerDetector.isServerProfileSupported(ServerProfile.CHERRY_PIZZA)
                    || EntityUtils.isEffectActive(instance)) {
                continue;
            }
            current.put(effect.getTranslationKey() + ":" + instance.getAmplifier(), instance);
        }
        if (this.alert.isEnabled()) {
            this.previousEffects.forEach((key, previous) -> {
                StatusEffect effect = previous.getEffectType().value();
                if (!current.containsKey(key) && !effect.getCategory().equals(StatusEffectCategory.HARMFUL)) {
                    String label = effect.getName().getString() + " "
                        + String.valueOf(previous.getAmplifier() > 0
                            ? Integer.valueOf(previous.getAmplifier() + 1) : "");
                    RockstarClient.create().getUiComponentProcessor().enqueueNotification(
                        new StatusEffectNotification(
                            Localization.translateFormatted("hud.effects.ended", label),
                            previous.getEffectType()).withHighlightedText(label));
                }
            });
        }
        this.effectsByKey.clear();
        this.orderedKeys.clear();
        if (this.grouping.isEnabled()) {
            Map<Integer, List<StatusEffectInstance>> grouped = current.values().stream()
                .collect(Collectors.groupingBy(i -> i.getDuration() * 50,
                    LinkedHashMap::new, Collectors.toList()));
            for (List<StatusEffectInstance> group : grouped.values()) {
                String key = "g:" + group.stream()
                    .map(i -> i.getEffectType().value().getTranslationKey() + ":" + i.getAmplifier())
                    .sorted().collect(Collectors.joining(","));
                this.effectsByKey.put(key, group);
                this.orderedKeys.add(key);
            }
        } else {
            for (Map.Entry<String, StatusEffectInstance> entry : current.entrySet()) {
                this.effectsByKey.put(entry.getKey(), List.of(entry.getValue()));
                this.orderedKeys.add(entry.getKey());
            }
        }
        this.previousEffects = current;
    }

    private String effectName(String key) {
        List<StatusEffectInstance> list = this.effectsByKey.get(key);
        if (list == null || list.isEmpty()) {
            return "";
        }
        StatusEffectInstance instance = list.get(0);
        StatusEffect effect = instance.getEffectType().value();
        int amplifier = instance.getAmplifier();
        return effect.getName().getString() + (amplifier > 0 ? " " + (amplifier + 1) : "");
    }

    private String effectTime(String key) {
        List<StatusEffectInstance> list = this.effectsByKey.get(key);
        if (list == null || list.isEmpty()) {
            return "";
        }
        StatusEffectInstance instance = list.get(0);
        if (instance.isInfinite() || instance.getDuration() >= 999999999) {
            return "**:**";
        }
        int seconds = instance.getDuration() / 20;
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    @Override
    public boolean show() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }
        boolean any = client.player.getStatusEffects().stream().anyMatch(instance ->
            !EntityUtils.isEffectActive(instance)
                && instance.getEffectType() != null
                && !ServerDetector.isServerProfileSupported(ServerProfile.CHERRY_PIZZA));
        return (any || client.currentScreen instanceof ChatScreen || this.alwaysDisplay.isEnabled())
            && !ServerDetector.isServerProfileSupported(ServerProfile.CHERRY_PIZZA);
    }

    final class IconStrip extends UiNode {
        private final String key;

        IconStrip(String key) {
            this.key = key;
            this.interactive(false);
        }

        private List<StatusEffectInstance> effects() {
            return EffectsHud.this.effectsByKey.getOrDefault(this.key, List.of());
        }

        @Override
        protected void measure() {
            int count = Math.max(1, this.effects().size());
            this.prefW = 4 + count * 10;
            this.prefH = 10.0f;
        }

        @Override
        protected void drawSelf(RockstarDrawContext ctx, float alpha) {
            ctx.drawRoundedRect(this.x(), this.y(), this.w(), this.h(),
                WidgetState.uniform(2.0f), ColorPalette.PANEL_COLOR.mulAlpha(0.94f));
            float iconX = this.x() + 3.0f;
            float iconY = this.y() + this.h() / 2.0f - 4.0f;
            for (StatusEffectInstance instance : this.effects()) {
                Sprite sprite = MinecraftClient.getInstance()
                    .getStatusEffectSpriteManager().getSprite(instance.getEffectType());
                ctx.drawTexture(sprite.getAtlasId(), iconX, iconY, 8.0f, 8.0f,
                    sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(),
                    ColorRGBA.WHITE);
                iconX += 10.0f;
            }
        }
    }
}
