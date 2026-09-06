/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.modules.other.autobuy;

import java.util.List;
import moscow.rockstar.items.config.ItemConfigProcessor;
import moscow.rockstar.items.tooltip.ItemTooltipProvider;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.layout.ItemDefinitionRenderer;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.text.Font;
import net.minecraft.client.util.math.MatrixStack;

public class ItemGridRenderer {
    private final float ITEM_CARD_WIDTH = 30.0f;
    private final float ITEM_CARD_HEIGHT = 34.0f;
    private final float GRID_CELL_SIZE = 36.0f;
    private final float GRID_PADDING = 10.0f;
    private float scrollOffset = 0.0f;
    private float maximumScrollOffset = 0.0f;

    public void render(RockstarDrawContext drawContext, List<ItemConfigProcessor.CategoryDefinition> list, ItemTooltipProvider itemTooltipProvider, float f, float f2, float f3, float f4) {
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)f, (float)f2, (float)f3, (float)f4);
        float f5 = f2 - this.scrollOffset;
        float f6 = 0.0f;
        boolean bl = false;
        for (ItemConfigProcessor.CategoryDefinition categoryDefinition : list) {
            int n = this.countVisibleItems(categoryDefinition, itemTooltipProvider);
            if (n == 0) continue;
            bl = true;
            if (f5 > f2 + f4) {
                f6 += 12.0f + this.calculateGroupHeight(n);
                continue;
            }
            if (f5 + 12.0f >= f2) {
                drawContext.drawText(Font.REGULAR.metrics(7.0f), categoryDefinition.getDisplayName(), f + 10.0f, f5, ColorPalette.getPrimaryTextColor().mulAlpha(0.9f));
            }
            f5 += 12.0f;
            f5 = this.renderItemGroup(drawContext, categoryDefinition, itemTooltipProvider, f5, f, f2, f4);
            f6 = f5 - (f2 - this.scrollOffset);
        }
        if (!bl) {
            String string = "\u041d\u0435\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432";
            float f7 = Font.REGULAR.metrics(8.0f).measureText(string);
            float f8 = f + (f3 - f7) / 2.0f;
            float f9 = f2 + (f4 - Font.REGULAR.metrics(8.0f).getFontTopOffset()) / 2.0f;
            drawContext.drawText(Font.REGULAR.metrics(8.0f), string, f8, f9, ColorPalette.WHITE.withAlpha(0.7f));
        }
        this.maximumScrollOffset = Math.max(0.0f, f6 - f4);
        this.scrollOffset = Math.max(0.0f, Math.min(this.scrollOffset, this.maximumScrollOffset));
        moscow.rockstar.render.state.UiScissorStack.pop();
    }

    public void handleItemClick(double d, double d2, List<ItemConfigProcessor.CategoryDefinition> list, ItemTooltipProvider itemTooltipProvider, float f, float f2, float f3) {
        if (d2 < (double)f2 || d2 > (double)(f2 + f3)) {
            return;
        }
        float f4 = f2 - this.scrollOffset;
        for (ItemConfigProcessor.CategoryDefinition categoryDefinition : list) {
            if (this.countVisibleItems(categoryDefinition, itemTooltipProvider) == 0) continue;
            if (this.selectItem(categoryDefinition, itemTooltipProvider, d, d2, f4 += 12.0f, f)) {
                return;
            }
            f4 += this.calculateGroupHeight(this.countVisibleItems(categoryDefinition, itemTooltipProvider));
        }
    }

    public void scroll(double d, double d2, double d3, float f, float f2, float f3, float f4) {
        if (UiUtils.contains((double)f, (double)f2, (double)f3, (double)f4, d, d2)) {
            this.scrollOffset = Math.max(0.0f, Math.min(this.scrollOffset - (float)d3 * 20.0f, this.maximumScrollOffset));
        }
    }

    public void resetScroll() {
        this.scrollOffset = 0.0f;
        this.maximumScrollOffset = 0.0f;
    }

    private float renderItemGroup(RockstarDrawContext drawContext, ItemConfigProcessor.CategoryDefinition categoryDefinition, ItemTooltipProvider itemTooltipProvider, float f, float f2, float f3, float f4) {
        int n = 0;
        int n2 = 0;
        for (ItemConfigProcessor.ItemDefinition itemDefinition : categoryDefinition.getItems()) {
            if (itemTooltipProvider.isConfiguredItem(itemDefinition)) continue;
            int n3 = n % 5;
            int n4 = n / 5;
            n2 = Math.max(n2, n4);
            float f5 = f2 + 10.0f + (float)n3 * 36.0f;
            float f6 = f + (float)n4 * 36.0f;
            if (f6 + 34.0f >= f3 && f6 <= f3 + f4) {
                ItemDefinitionRenderer.drawTextLayoutListenerContentForFontMetricsAndInnerLowerAndFloatAndFloatAndFloatAndFloatAndBoolean(drawContext, Font.REGULAR.metrics(6.0f), itemDefinition, f5, f6, 30.0f, 34.0f, itemTooltipProvider.matchesTrackedConfig(itemDefinition));
            }
            ++n;
        }
        return f + (float)(n2 + 1) * 36.0f + 8.0f;
    }

    private boolean selectItem(ItemConfigProcessor.CategoryDefinition categoryDefinition, ItemTooltipProvider itemTooltipProvider, double d, double d2, float f, float f2) {
        int n = 0;
        for (ItemConfigProcessor.ItemDefinition itemDefinition : categoryDefinition.getItems()) {
            if (itemTooltipProvider.isConfiguredItem(itemDefinition)) continue;
            int n2 = n % 5;
            float f3 = f2 + 10.0f + (float)n2 * 36.0f;
            int n3 = n / 5;
            float f4 = f + (float)n3 * 36.0f;
            if (UiUtils.contains((double)f3, (double)f4, 30.0, 34.0, d, d2)) {
                itemTooltipProvider.setTrackedConfig(itemDefinition);
                return true;
            }
            ++n;
        }
        return false;
    }

    private int countVisibleItems(ItemConfigProcessor.CategoryDefinition categoryDefinition, ItemTooltipProvider itemTooltipProvider) {
        int n = 0;
        for (ItemConfigProcessor.ItemDefinition itemDefinition : categoryDefinition.getItems()) {
            if (itemTooltipProvider.isConfiguredItem(itemDefinition)) continue;
            ++n;
        }
        return n;
    }

    private float calculateGroupHeight(int n) {
        int n2 = (n + 5 - 1) / 5;
        return (float)n2 * 36.0f + 8.0f;
    }
}
