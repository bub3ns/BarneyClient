package moscow.rockstar.modules.visuals.hud;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.colors.GradientColors;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextComponent;
import net.minecraft.client.MinecraftClient;

/** Shared one-line pill HUD surface. 1:1 with rockstar/ilIlil/IiiIIIiIi. */
public class HudRowElement extends HudElement {
    protected final MultiBooleanSetting elements =
        new MultiBooleanSetting(this, "elements").preserveOrder().setMaxSelections(1);

    public HudRowElement(String name, String icon) {
        super(name, icon);
        this.height = 18.0f;
    }

    @Override
    protected Component build() {
        List<HudRowEntry> entries = new ArrayList<>();
        for (MultiBooleanSetting.Option option : this.elements.getOptions()) {
            entries.add((HudRowEntry)option);
        }
        Component row = new Component().horizontal().alignment(Alignment.CENTER).gap(0.0f);
        int index = 0;
        while (index < entries.size()) {
            HudRowEntry entry = entries.get(index);
            int slot = index++;
            row.add(new TextComponent().size(10.0f, 2.0f).interactive(false)
                .visibleWhen(() -> entry.isSelected() && anySelectedBefore(entries, slot))
                .paint((ctx, node) -> ctx.drawRoundedRect(
                    node.x() + node.w() / 2.0f - 1.0f,
                    node.y() + node.h() / 2.0f - 1.0f,
                    2.0f, 2.0f, WidgetState.uniform(1.0f),
                    ColorPalette.ACCENT_COLOR.mulAlpha(0.5f))));
            row.add(this.entryNode(entry).visibleWhen(entry::isSelected));
        }
        return new Component().horizontal().alignment(Alignment.CENTER).height(15.0f).gap(3.0f)
            .padding(Insets.of(0.0f, 5.0f, 0.0f, 4.0f))
            .renderHook((ctx, node) -> {
                ctx.drawClientRect(node.x(), node.y(), node.w(), node.h(),
                    this.animation.getValue(), this.dragAnim.getValue(), 3.0f, 5.0f);
                ctx.drawSquircle(node.x(), node.y(), 23.0f, node.h(), 3.0f, WidgetState.uniform(5.0f),
                    new GradientColors(ColorPalette.VIBRANT_ACCENT_COLOR.mulAlpha(0.1f),
                                       ColorPalette.VIBRANT_ACCENT_COLOR.mulAlpha(0.0f)));
            })
            .add(new TextComponent().size(7.0f, 7.0f).interactive(false)
                .icon(this.getIcon(), 7.0f, node -> ColorPalette.ACCENT_COLOR.withAlpha(255.0f)))
            .add(row);
    }

    private UiNode entryNode(final HudRowEntry entry) {
        final FontMetrics valueFont = Font.MEDIUM.metrics(7.0f);
        final FontMetrics suffixFont = Font.MEDIUM.metrics(7.0f);
        return new UiNode() {
            @Override
            protected void measure() {
                float copyWidth = entry.getCopyValue().isEmpty() ? 0.0f : 8.0f * this.hover();
                float suffixWidth = entry.getSuffix().isEmpty()
                    ? 0.0f : suffixFont.measureText(entry.getSuffix());
                this.prefW = copyWidth + valueFont.measureText(entry.getValue()) + suffixWidth;
                // ORIGINAL rockstar/ilIlil/IiiIIIiIi#I(LIiiIIIiiI;)#measure uses the NO-ARG font accessor
                // IIiIIi.I(), which resolves BY BODY to renderer.getBaselineOffset(scale) =
                // getFontTopOffset(). getFontMetricsFloat() is IIiIIi.i() (lineHeight*scale) - a
                // different, larger metric, so the row measured too tall and the vertical centring
                // below subtracted too much, pushing the label off-centre.
                this.prefH = valueFont.getFontTopOffset();
            }

            @Override
            protected void onTick(float delta, float mouseX, float mouseY) {
                if (!this.hovered() || entry.getCopyTimer().hasElapsed(1000L)) {
                    entry.setCopied(false);
                }
                entry.getCopiedAnimation().setReverse(entry.isCopied());
            }

            @Override
            protected void drawSelf(RockstarDrawContext ctx, float alpha) {
                float hover = entry.getCopyValue().isEmpty() ? 0.0f : this.hover();
                float copyWidth = 8.0f * hover;
                float copied = entry.getCopiedAnimation().getValue();
                float iconX = this.x();
                float iconY = this.y() + this.h() / 2.0f - 3.0f;
                float copyAlpha = hover * (1.0f - copied);
                if (copyAlpha > 0.001f) {
                    ItemRenderUtils.translateAndRotate(ctx.getMatrices(), iconX + 3.0f, iconY + 3.0f, 90.0f * copied);
                    ctx.drawIcon("copy", iconX, iconY, 6.0f, ColorPalette.getPrimaryTextColor().mulAlpha(copyAlpha));
                    ItemRenderUtils.popMatrix(ctx.getMatrices());
                }
                float checkAlpha = hover * copied;
                if (checkAlpha > 0.001f) {
                    ItemRenderUtils.translateAndRotate(ctx.getMatrices(), iconX + 3.0f, iconY + 3.0f, -90.0f + 90.0f * copied);
                    ctx.drawIcon("check", iconX, iconY, 6.0f, ColorPalette.GREEN.mulAlpha(checkAlpha));
                    ItemRenderUtils.popMatrix(ctx.getMatrices());
                }
                float textX = this.x() + copyWidth;
                float textY = this.y() + this.h() / 2.0f - valueFont.getFontTopOffset() / 2.0f;
                ctx.drawText(valueFont, entry.getValue(), textX, textY, ColorPalette.getPrimaryTextColor());
                if (!entry.getSuffix().isEmpty()) {
                    ctx.drawText(suffixFont, entry.getSuffix(),
                        textX + valueFont.measureText(entry.getValue()), textY,
                        ColorPalette.getPrimaryTextColor().mulAlpha(0.5f));
                }
            }

            @Override
            public boolean mouseClicked(float mouseX, float mouseY, PointerAction action) {
                if (!(this.interactive && this.inFlow() && this.contains(mouseX, mouseY))) {
                    return false;
                }
                if (action == PointerAction.LEFT_CLICK && !entry.getCopyValue().isEmpty()) {
                    MinecraftClient.getInstance().keyboard.setClipboard(entry.getCopyValue());
                    entry.getCopyTimer().reset();
                    entry.setCopied(true);
                    return true;
                }
                return false;
            }
        }.snapSize();
    }

    private static boolean anySelectedBefore(List<HudRowEntry> entries, int index) {
        for (int i = 0; i < index; ++i) {
            if (entries.get(i).isSelected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, PointerAction action) {
        if (this.flow != null && this.isShowing() && action == PointerAction.LEFT_CLICK
                && this.flow.mouseClicked((float)mouseX, (float)mouseY, action)) {
            return;
        }
        super.onMouseClicked(mouseX, mouseY, action);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, PointerAction action) {
        if (this.flow != null) {
            this.flow.mouseReleased((float)mouseX, (float)mouseY, action);
        }
        super.onMouseReleased(mouseX, mouseY, action);
    }
}
