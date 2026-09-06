/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.widgets.settings;

import java.util.List;
import moscow.rockstar.network.http.client.ReactorNettyClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.BlockItemSetting;
import moscow.rockstar.settings.SettingComponent;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.screens.ModuleSettingsScreen;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextInputField;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

public class BlockItemSettingComponent
extends SettingComponent<BlockItemSetting> {
    private static final float HEADER_HEIGHT = 19.0f;
    private static final float SECTION_PADDING = 6.0f;
    private static final float OPTION_CELL_SIZE = 20.0f;
    private static final float OPTION_INSET = 2.0f;
    private static final float OPTION_ICON_SIZE = 16.0f;
    private static final float OPTION_ICON_SCALE = 0.9f;
    private static final float SEARCH_FIELD_HEIGHT = 14.0f;
    private static final float SEARCH_FIELD_GAP = 5.0f;
    private final TextInputField searchField = new TextInputField(Font.REGULAR.metrics(7.0f));
    private FontMetrics fontMetrics;
    private FontMetrics countFontMetrics;

    public BlockItemSettingComponent(BlockItemSetting blockItemSetting, SettingWidget settingWidget) {
        super(blockItemSetting, settingWidget);
        this.searchField.setText(blockItemSetting.getSearchText());
    }

    @Override
    public final void tick() {
        this.searchField.setPlaceholder(Localization.translate("search"));
        this.fontMetrics = Font.MEDIUM.metrics(8.0f);
        this.countFontMetrics = Font.MEDIUM.metrics(7.0f);
        super.tick();
    }

    @Override
    protected final void renderContent(RockstarDrawContext drawContext) {
        boolean bl;
        float f;
        float f2;
        float f3;
        float f4;
        int n;
        float f5 = this.x + 9.0f;
        float f6 = this.y + 1.0f;
        float f7 = this.width - 18.0f;
        this.contentAnimation.setReverse(this.contains(drawContext.mouseX(), drawContext.mouseY()));
        float f8 = 10.0f;
        String string = "%d/%d".formatted(((BlockItemSetting)this.setting).getSelectedCount(), ((BlockItemSetting)this.setting).getAllOptions().size());
        this.drawScrollableText(drawContext, this.fontMetrics, Localization.translate(((BlockItemSetting)this.setting).getName()), this.x + f8, f6 - 1.0f + UiUtils.center(this.fontMetrics.getFontTopOffset(), 19.0f), this.getSettingWidget().getWidth() - f8 - 10.0f - this.countFontMetrics.measureText(string), ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue())), 0.8f, 1.0f);
        drawContext.drawRightText(this.countFontMetrics, string, f5 + f7, f6 - 1.0f + UiUtils.center(Font.REGULAR.metrics(7.0f).getFontTopOffset(), 19.0f), ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.6f + 0.4f * this.contentAnimation.getValue())));
        this.searchField.setPlaceholder(Localization.translate("search"));
        ((BlockItemSetting)this.setting).setSearchText(this.searchField.getText());
        List<BlockItemSetting.Option> list = ((BlockItemSetting)this.setting).getFilteredOptions();
        int n2 = this.calculateIntFromFloat(f7);
        int n3 = list.size();
        int n4 = Math.max(1, (int)Math.ceil((float)n3 / (float)n2));
        float f9 = f5 - 1.0f;
        float f10 = f6 + 17.0f;
        float f11 = f5 + 6.0f;
        float f12 = f10 + 5.0f;
        float f13 = Math.max(0.0f, f7 - 12.0f);
        float f14 = f5 + 6.0f;
        float f15 = f12 + 14.0f + 6.0f;
        float f16 = (float)n4 * 20.0f;
        float f17 = 25.0f + f16 + 6.0f;
        drawContext.drawRoundedRect(f9, f10, f7 + 2.0f, f17, WidgetState.uniform(6.0f), ColorPalette.getPanelColor().withAlpha(76.5f));
        this.searchField.setBounds(f11, f12, f13, 14.0f);
        this.searchField.setTextColor(ColorPalette.getPrimaryTextColor());
        this.searchField.setOpacity(1.0f);
        this.searchField.render(drawContext);
        float f18 = this.y;
        float f19 = this.y + this.height;
        ModuleSettingsScreen moduleSettingsScreen = new ModuleSettingsScreen();
        for (int i = 0; i < n3; ++i) {
            BlockItemSetting.Option option = list.get(i);
            int n5 = i % n2;
            n = i / n2;
            float f20 = f14 + (float)n5 * 20.0f;
            f4 = f15 + (float)n * 20.0f;
            f3 = f20 + 2.0f;
            f2 = f4 + 2.0f;
            f = 0.8000002f;
            if (f2 + 16.0f < this.height || f2 > this.height + this.visibleRegionHeight * 2.0f) continue;
            boolean bl2 = UiUtils.contains(f20, f4, 20.0, 20.0, drawContext);
            bl = ((BlockItemSetting)this.setting).isOptionSelected(option);
            ColorRGBA colorRGBA = ColorPalette.getPanelColor().mulAlpha(0.22f + (bl2 ? 0.08f : 0.0f));
            ColorRGBA colorRGBA2 = ColorPalette.getAccentColor().mulAlpha(0.7f);
            option.getHoverColor().setTargetColor(bl ? colorRGBA2 : colorRGBA);
            if (bl2 && (float)drawContext.mouseY() > this.getVisibleRegionTop() && (float)drawContext.mouseY() < this.getVisibleRegionTop() + this.getVisibleRegionHeight()) {
                moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
            }
            drawContext.drawRoundedRect(f3, f2, 16.0f, 16.0f, WidgetState.uniform(4.0f), option.getHoverColor().getColor());
        }
        moduleSettingsScreen.refreshModuleRegistry();
        CustomDrawContext.ItemBatch itemBatch = drawContext.beginItemBatch();
        try {
            for (int i = 0; i < n3; ++i) {
                BlockItemSetting.Option option = list.get(i);
                n = i % n2;
                int n6 = i / n2;
                f4 = f14 + (float)n * 20.0f;
                f3 = f15 + (float)n6 * 20.0f;
                f2 = f4 + 2.0f;
                f = f3 + 2.0f;
                float f21 = 0.8000002f;
                if (f + 16.0f < this.height || f > this.height + this.visibleRegionHeight * 2.0f) continue;
                bl = UiUtils.contains(f4, f3, 20.0, 20.0, drawContext);
                if (bl) {
                    moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
                }
                drawContext.drawBatchItem(option.getItemStack(), f2 + f21, f + f21, 0.9f);
            }
        }
        finally {
            if (itemBatch != null) {
                itemBatch.restoreEnabledFlag();
            }
        }
        for (int i = 0; i < n3; ++i) {
            BlockItemSetting.Option option = list.get(i);
            int n7 = i % n2;
            n = i / n2;
            float f22 = f14 + (float)n7 * 20.0f;
            f4 = f15 + (float)n * 20.0f;
            f3 = f22 + 2.0f;
            f2 = f4 + 2.0f;
            if (f2 + 16.0f < this.height || f2 > this.height + this.visibleRegionHeight * 2.0f) continue;
            boolean bl3 = UiUtils.contains(f22, f4, 20.0, 20.0, drawContext);
            boolean bl4 = ((BlockItemSetting)this.setting).isOptionSelected(option);
            option.getHoverAnimation().update(bl3 ? 0.6f : 0.0f);
            if (bl3) {
                moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
            }
            drawContext.drawRoundedBorder(f3, f2, 16.0f, 16.0f, 1.2f, WidgetState.uniform(4.0f), ColorPalette.getAccentColor().mulAlpha(option.getHoverAnimation().getValue()));
        }
    }

    @Override
    public final void mouseClicked(double d, double d2, PointerAction pointerAction) {
        this.searchField.mouseClicked(d, d2, pointerAction);
        if (pointerAction != PointerAction.LEFT_CLICK) {
            super.mouseClicked(d, d2, pointerAction);
            return;
        }
        float f = this.x + 9.0f;
        float f2 = f + 6.0f;
        float f3 = this.y + 17.0f;
        float f4 = f3 + 5.0f;
        float f5 = this.width - 18.0f;
        float f6 = Math.max(0.0f, f5 - 12.0f);
        if (UiUtils.contains((double)f2, (double)f4, (double)f6, 14.0, d, d2)) {
            super.mouseClicked(d, d2, pointerAction);
            return;
        }
        int n = this.calculateIntFromFloat(f5);
        float f7 = f + 6.0f;
        float f8 = f4 + 14.0f + 6.0f;
        List<BlockItemSetting.Option> list = ((BlockItemSetting)this.setting).getFilteredOptions();
        for (int i = 0; i < list.size(); ++i) {
            BlockItemSetting.Option option = list.get(i);
            int n2 = i % n;
            float f9 = f7 + (float)n2 * 20.0f;
            int n3 = i / n;
            float f10 = f8 + (float)n3 * 20.0f;
            if (!UiUtils.contains((double)f9, (double)f10, 20.0, 20.0, d, d2)) continue;
            ((BlockItemSetting)this.setting).toggleOption(option);
            break;
        }
        super.mouseClicked(d, d2, pointerAction);
    }

    @Override
    public final void mouseReleased(double d, double d2, PointerAction pointerAction) {
        this.searchField.mouseReleased(d, d2, pointerAction);
        super.mouseReleased(d, d2, pointerAction);
    }

    @Override
    public final void keyPressed(int n, int n2, int n3) {
        this.searchField.keyPressed(n, n2, n3);
        super.keyPressed(n, n2, n3);
    }

    @Override
    public final boolean charTyped(char c, int n) {
        if (this.searchField.charTyped(c, n)) {
            return true;
        }
        return super.charTyped(c, n);
    }

    @Override
    public final void renderDivider(RockstarDrawContext drawContext) {
        float f = 0.5f;
        drawContext.drawRect(this.x, this.y + this.height, this.width, f, ColorPalette.getPrimaryTextColor().withAlpha(5.1f));
    }

    @Override
    public final float getHeight() {
        float f = this.width - 18.0f;
        int n = this.calculateIntFromFloat(f);
        int n2 = ((BlockItemSetting)this.setting).getFilteredOptions().size();
        int n3 = Math.max(1, (int)Math.ceil((float)n2 / (float)n));
        float f2 = (float)n3 * 20.0f;
        float f3 = 25.0f + f2 + 6.0f;
        this.height = 23.0f + f3;
        return this.height;
    }

    private int calculateIntFromFloat(float f) {
        float f2 = Math.max(f - 12.0f, 20.0f);
        int n = (int)Math.floor(f2 / 20.0f);
        return Math.max(1, n);
    }
}
