package moscow.rockstar.modules.visuals.hud;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.colors.GradientColors;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.server.staff.StaffListManager;
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
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import pyrock.utility.render.ColorRGBA;

/** Online staff / prefix list. 1:1 with rockstar/ilIlil/IiIiIiiIi. */
public class StaffListHud extends HudElement {
    private static final Set<String> STAFF_PREFIXES = Set.of(
        "\u029c\u1d07\u029f\u1d18\u1d07\u0280", "developer", "moder", "moder+", "moderator",
        "ml.moder", "st.moder", "gl.moder", "admin", "administrator", "st helper", "d helper",
        "d.helper", "helper", "media", "owner", "staff", "support", "yt", "youtube", "youtuber",
        "tiktok", "\u0445\u0435\u043b\u043f\u0435\u0440", "\u0441\u0442. \u043c\u043e\u0434\u0435\u0440",
        "\u0441\u0442. \u0441\u043e\u0442\u0440\u0443\u0434\u043d\u0438\u043a",
        "\u0441\u0442. \u0441\u0442\u0430\u0436\u0435\u0440", "\u0433\u043b. \u043c\u043e\u0434\u0435\u0440",
        "\u0433\u043b. \u0430\u0434\u043c\u0438\u043d", "\u0441\u0442.\u043c\u043e\u0434\u0435\u0440",
        "\u0441\u0442.\u0441\u043e\u0442\u0440\u0443\u0434\u043d\u0438\u043a",
        "\u0441\u0442.\u0441\u0442\u0430\u0436\u0435\u0440", "\u0433\u043b.\u043c\u043e\u0434\u0435\u0440",
        "\u0433\u043b.\u0430\u0434\u043c\u0438\u043d", "\u0441\u0442. \u043c\u043e\u0434\u0451\u0440",
        "\u0441\u0442. \u0441\u0442\u0430\u0436\u0451\u0440", "\u0433\u043b. \u043c\u043e\u0434\u0451\u0440",
        "\u043c\u043e\u0434\u0435\u0440", "\u043co\u0434e\u0440", "\u0441\u0442\u0430\u0436\u0435\u0440",
        "\u0441\u0442\u0430\u0436\u0451\u0440", "\u0430\u0434\u043c\u0438\u043d",
        "\u0441\u043e\u0442\u0440\u0443\u0434\u043d\u0438\u043a", "\u043a\u0443\u0440\u0430\u0442\u043e\u0440",
        "\u043c\u043b.\u0441\u043e\u0442\u0440\u0443\u0434\u043d\u0438\u043a",
        "\u043f\u043e\u0434\u0434\u0435\u0440\u0436\u043a\u0430",
        "\u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440",
        "\u0441\u043f\u0435\u043a\u0442\u0430\u0442\u043e\u0440",
        "\ua509", "\ua513", "\ua517", "\ua521", "\ua525");

    private final BooleanSetting alwaysDisplay = new BooleanSetting(this, "hud.always_display");
    private final BooleanSetting uniformWidth = new BooleanSetting(this, "hud.uniform_width");
    private final List<StaffRow> entries = new ArrayList<>();
    private final Map<String, StaffRow> byName = new HashMap<>();
    private final Map<String, UiNode> rows = new HashMap<>();
    private final Set<String> lastKeys = new HashSet<>();
    private boolean rightSide;
    private Component list;
    private UiNode header;
    private final Transition rowTransition;

    public StaffListHud() {
        super("hud.staff_list", "hud/staff");
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
                .icon("hud/staff", 7.0f, ColorPalette.ACCENT_COLOR))
            .add(new TextComponent().interactive(false)
                .text(Font.MEDIUM.metrics(6.0f), () -> Localization.translate(this.getName()),
                      node -> ColorPalette.PRIMARY_TEXT_COLOR));
        this.list.add(this.header);
        this.refreshRows();
        return this.list;
    }

    private UiNode row(final String name) {
        UiNode node = this.rows.computeIfAbsent(name, key -> {
            FontMetrics font = Font.REGULAR.metrics(6.0f);
            return new Component().horizontal().alignment(Alignment.CENTER).gap(1.0f)
                .transition(this.rowTransition)
                .add(new TextComponent().interactive(false).height(10.0f).radius(2.0f)
                    .padding(Insets.horizontal(3.0f))
                    .background(n -> ColorPalette.PANEL_COLOR.mulAlpha(0.889f))
                    .text(font, () -> this.labelText(key), n -> this.labelColor(key))
                    .visibleWhen(() -> !this.labelText(key).isEmpty()))
                .add(new TextComponent().interactive(false).fillWidth().height(10.0f).radius(2.0f)
                    .padding(Insets.horizontal(3.0f))
                    .background(n -> ColorPalette.PANEL_COLOR.mulAlpha(0.94f))
                    .text(font, () -> this.displayName(key), n -> ColorPalette.PRIMARY_TEXT_COLOR))
                .add(new TextComponent().size(10.0f, 10.0f).interactive(false).radius(2.0f)
                    .background(n -> ColorPalette.PANEL_COLOR.mulAlpha(0.94f))
                    .paint((ctx, n) -> {
                        StaffRow staffRow = this.byName.get(key);
                        ColorRGBA color = staffRow != null && staffRow.isSpec()
                            ? new ColorRGBA(220.0f, 70.0f, 70.0f)
                            : new ColorRGBA(70.0f, 210.0f, 120.0f);
                        float dot = 4.0f;
                        ctx.drawRoundedRect(n.x() + n.w() / 2.0f - dot / 2.0f,
                            n.y() + n.h() / 2.0f - dot / 2.0f, dot, dot,
                            WidgetState.uniform(dot / 2.0f), color);
                    }));
        });
        if (node.phase() == UiNode.LifecyclePhase.EXITING
                || node.phase() == UiNode.LifecyclePhase.DISCARDED
                || node.phase() == UiNode.LifecyclePhase.HIDDEN) {
            node.beginEnter(0.0f);
        }
        return node;
    }

    private void refreshRows() {
        HashSet<String> keys = new HashSet<>();
        for (StaffRow staffRow : this.entries) {
            keys.add(staffRow.name());
        }
        if (keys.equals(this.lastKeys)) {
            return;
        }
        this.lastKeys.clear();
        this.lastKeys.addAll(keys);
        FontMetrics font = Font.REGULAR.metrics(6.0f);
        Map<String, Float> widths = new HashMap<>();
        for (StaffRow staffRow : this.entries) {
            widths.put(staffRow.name(),
                font.measureText(this.label(staffRow).text()) + font.measureText(this.protectedName(staffRow)));
        }
        List<StaffRow> sorted = new ArrayList<>(this.entries);
        sorted.sort(Comparator.comparingDouble((StaffRow staffRow) -> widths.get(staffRow.name()).floatValue()).reversed());
        List<UiNode> children = new ArrayList<>();
        children.add(this.header);
        Set<String> seen = new HashSet<>();
        for (StaffRow staffRow : sorted) {
            if (!seen.add(staffRow.name())) {
                continue;
            }
            children.add(this.row(staffRow.name()));
        }
        this.list.updateChildren(children);
    }

    @Override
    public void update(RockstarDrawContext drawContext) {
        this.rightSide = this.x + this.width / 2.0f >= WindowMetricsProvider.INSTANCE.width() / 2.0f;
        this.collect();
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

    private void collect() {
        this.entries.clear();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.networkHandler == null) {
            this.byName.clear();
            return;
        }
        if (client.player.networkHandler.getServerInfo() == null) {
            this.entries.add(new StaffRow(
                Text.literal("[ADMIN] ").setStyle(Style.EMPTY.withColor(Formatting.RED)),
                Localization.translate("staff.herobrine"), false));
        } else {
            client.player.networkHandler.getPlayerList().forEach(entry -> {
                Team team = client.world.getScoreboard().getScoreHolderTeam(entry.getProfile().getName());
                if (team == null) {
                    return;
                }
                MutableText display = team.getPrefix().copy();
                String name = entry.getProfile().getName().replace("\u26a1 ", "");
                boolean spectator = entry.getGameMode() == GameMode.SPECTATOR;
                String prefix = this.normalisePrefix(display.getString());
                if (prefix != null && !name.isBlank()) {
                    MutableText chip = Text.literal(prefix).setStyle(
                        Style.EMPTY.withColor(display.getStyle().getColor()));
                    this.entries.add(new StaffRow(chip, name.trim(), spectator));
                }
            });
        }
        for (StaffListManager.StaffEntry member : RockstarClient.create().getStaffListManager().getStaffMembers()) {
            if (member.getName().isBlank()) {
                continue;
            }
            String prefix = member.getPrefix().isBlank() ? "MODER" : member.getPrefix().trim();
            MutableText chip = Text.literal("[" + prefix + "] ").setStyle(
                Style.EMPTY.withColor(Formatting.BLUE));
            this.entries.add(new StaffRow(chip, member.getName(), false));
        }
        this.byName.clear();
        for (StaffRow staffRow : this.entries) {
            this.byName.put(staffRow.name(), staffRow);
        }
    }

    private String labelText(String name) {
        StaffRow staffRow = this.byName.get(name);
        return staffRow == null ? "" : this.label(staffRow).text();
    }

    private ColorRGBA labelColor(String name) {
        StaffRow staffRow = this.byName.get(name);
        return staffRow == null ? ColorPalette.getPrimaryTextColor() : this.label(staffRow).color();
    }

    private String displayName(String name) {
        StaffRow staffRow = this.byName.get(name);
        return staffRow == null ? "" : this.protectedName(staffRow);
    }

    @Override
    public boolean show() {
        return !this.entries.isEmpty()
            || MinecraftClient.getInstance().currentScreen instanceof ChatScreen
            || this.alwaysDisplay.isEnabled();
    }

    private String normalisePrefix(String raw) {
        if (raw == null) {
            return null;
        }
        String lower = Normalizer.normalize(raw, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).trim();
        int end = lower.length();
        int start = 0;
        int cp;
        while (start < end && !Character.isLetterOrDigit(cp = lower.codePointAt(start))) {
            start += Character.charCount(cp);
        }
        while (end > start && !Character.isLetterOrDigit(cp = lower.codePointBefore(end)) && cp != 43) {
            end -= Character.charCount(cp);
        }
        StringBuilder out = new StringBuilder(end - start);
        boolean pendingSpace = false;
        int i = start;
        while (i < end) {
            int c = lower.codePointAt(i);
            i += Character.charCount(c);
            if (Character.isWhitespace(c) || Character.isSpaceChar(c)) {
                pendingSpace = out.length() > 0;
                continue;
            }
            if (pendingSpace) {
                out.append(' ');
            }
            out.appendCodePoint(c);
            pendingSpace = false;
        }
        String result = out.toString();
        if (!STAFF_PREFIXES.contains(result)) {
            return null;
        }
        return result.replace("\ua509", "helper").replace("\ua513", "ml.moder")
                     .replace("\ua517", "moder").replace("\ua521", "moder+")
                     .replace("\ua525", "st.moder");
    }

    private StaffLabel label(StaffRow staffRow) {
        Text text = this.styledPrefix(staffRow);
        String value = text.getString().trim().toUpperCase();
        TextColor textColor = text.getStyle().getColor();
        ColorRGBA color = textColor != null
            ? StaffListHud.fromRgb(textColor.getRgb())
            : ColorPalette.getPrimaryTextColor();
        return new StaffLabel(value, color);
    }

    private static ColorRGBA fromRgb(int rgb) {
        return new ColorRGBA(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
    }

    private Text styledPrefix(StaffRow staffRow) {
        String raw = staffRow.prefix().getString();
        String display = raw.toLowerCase()
            .replace("\ua509", "HELPER")
            .replace("\ua513", "ML.MODER")
            .replace("\ua517", "MODER")
            .replace("\ua521", "MODER+")
            .replace("\ua525", "st.MODER")
            .trim();
        boolean glyph = raw.contains("\ua509") || raw.contains("\ua513") || raw.contains("\ua517")
            || raw.contains("\ua521") || raw.contains("\ua525");
        TextColor textColor = staffRow.prefix().getStyle().getColor();
        if (glyph || textColor == null) {
            Formatting formatting = display.contains("HELPER")
                ? Formatting.YELLOW
                : (display.contains("MODER") ? Formatting.BLUE : Formatting.GRAY);
            textColor = TextColor.fromFormatting(formatting);
        }
        return Text.literal(display).setStyle(Style.EMPTY.withColor(textColor));
    }

    private String protectedName(StaffRow staffRow) {
        NameProtect nameProtect = RockstarClient.create().getModuleRegistry().getModule(NameProtect.class);
        return nameProtect.isEnabled() ? nameProtect.replacePlayerOrServerName(staffRow.name()) : staffRow.name();
    }

    /** rockstar/ilIlil/IiIiIiiIi$i */
    record StaffRow(Text prefix, String name, boolean isSpec) {
    }

    /** rockstar/ilIlil/IiIiIiiIi$I */
    record StaffLabel(String text, ColorRGBA color) {
    }
}
