package moscow.rockstar.ui.text;
import moscow.rockstar.ui.localization.Localization;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.screens.AssistGroup;
import moscow.rockstar.ui.screens.AssistItem;
import moscow.rockstar.ui.screens.AssistSearchResult;
import moscow.rockstar.ui.state.ScrollOffset;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;

public class TextLayout {
    public LayoutBounds renderSearchHeader(RockstarDrawContext drawContext, float x, float y, float width,
                                            float opacity, Animation panelAnimation, Animation searchAnimation,
                                            Animation cancelAnimation, TextInputField textInputField) {
        FontMetrics fontMetrics = Font.REGULAR.metrics(7.0f);
        float searchHeight = 14.0f;
        float searchWidth = 176.0f;
        float searchX = x + 7.0f;
        float searchY = y + 7.0f;
        float cornerRadius = 4.0f;
        searchAnimation.setReverse(textInputField.isFocused());
        float searchAlpha = (0.55f + 0.35f * searchAnimation.getValue()) * panelAnimation.getValue() * opacity;
        drawContext.drawRoundedRect(searchX, searchY, searchWidth, searchHeight, WidgetState.uniform(cornerRadius),
            ColorPalette.getPanelBackgroundColor().mulAlpha(searchAlpha));
        drawContext.drawIcon("search", searchX + cornerRadius, searchY + cornerRadius, 6.0f,
            ColorRGBA.WHITE.withAlpha(255.0f * panelAnimation.getValue() * opacity));
        textInputField.setBounds(searchX + 9.0f, searchY, searchWidth - 5.0f, searchHeight);
        textInputField.setTextColor(ColorPalette.getPrimaryTextColor().mulAlpha(panelAnimation.getValue() * opacity));
        textInputField.setOpacity(opacity);
        textInputField.render(drawContext);

        String cancelLabel = Localization.translate("cancel");
        float cancelWidth = fontMetrics.measureText(cancelLabel) + 16.0f;
        float cancelHeight = 14.0f;
        float cancelX = x + width - 7.0f - cancelWidth;
        float cancelY = searchY;
        cancelAnimation.setReverse(UiUtils.contains(cancelX, cancelY, cancelWidth, cancelHeight, drawContext));
        float cancelAlpha = (0.6f + 0.25f * cancelAnimation.getValue()) * panelAnimation.getValue() * opacity;
        drawContext.drawRoundedRect(cancelX, cancelY, cancelWidth, cancelHeight, WidgetState.uniform(3.0f),
            ColorPalette.getPanelBackgroundColor().mulAlpha(cancelAlpha));
        drawContext.drawText(fontMetrics, cancelLabel, cancelX + 8.0f,
            cancelY + (cancelHeight - fontMetrics.getFontTopOffset()) / 2.0f,
            ColorPalette.getPrimaryTextColor().mulAlpha(0.9f * panelAnimation.getValue() * opacity));
        return new LayoutBounds(cancelX, cancelY, cancelWidth, cancelHeight);
    }

    public void renderAssistItemGrid(RockstarDrawContext drawContext, float x, float y, float width, float height,
                                     float opacity, Animation panelAnimation, List<AssistGroup> groups,
                                     ScrollOffset scrollOffset, Map<AssistItem, Animation> itemAnimations,
                                     TextInputField searchField, Predicate<AssistItem> hiddenPredicate) {
        FontMetrics groupMetrics = Font.MEDIUM.metrics(8.0f);
        FontMetrics itemMetrics = Font.REGULAR.metrics(7.0f);
        float contentX = x + 7.0f;
        float contentY = y + 30.0f;
        float contentWidth = width - 14.0f;
        float viewportHeight = height - 33.0f;
        float columnGap = 4.0f;
        float cardHeight = 20.0f;
        float rowGap = 4.0f;
        float cardWidth = (contentWidth - columnGap) / 2.0f;
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        float cursorY = contentY - (float)scrollOffset.getOffset();
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(),
            contentX, contentY - 2.0f, contentWidth, Math.max(0.0f, viewportHeight - 4.0f));
        boolean found = false;
        for (AssistGroup group : groups) {
            List<AssistItem> visibleItems = new ArrayList<>();
            for (AssistItem item : group.getItems()) {
                if (!query.isEmpty() && !item.getDisplayName().toLowerCase(Locale.ROOT).contains(query)
                    || hiddenPredicate.test(item)) {
                    continue;
                }
                visibleItems.add(item);
            }
            if (visibleItems.isEmpty()) {
                continue;
            }
            found = true;
            drawContext.drawText(groupMetrics, group.getTitle(), contentX, cursorY,
                ColorPalette.getPrimaryTextColor().mulAlpha(0.95f * panelAnimation.getValue() * opacity));
            cursorY += groupMetrics.getFontTopOffset() + 8.0f;
            for (int index = 0; index < visibleItems.size(); index++) {
                AssistItem item = visibleItems.get(index);
                float cardX = contentX + index % 2 * (cardWidth + columnGap);
                float cardY = cursorY + index / 2 * (cardHeight + rowGap);
                boolean hovered = UiUtils.contains(cardX, cardY, cardWidth, cardHeight, drawContext);
                Animation itemAnimation = itemAnimations.computeIfAbsent(item,
                    ignored -> new Animation(200L, 0.0f, Easing.easeInOutCubicBezier));
                itemAnimation.setReverse(hovered);
                float cardAlpha = (0.55f + 0.25f * itemAnimation.getValue())
                    * panelAnimation.getValue() * opacity;
                drawContext.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, WidgetState.uniform(5.0f),
                    ColorPalette.getPanelBackgroundColor().mulAlpha(cardAlpha));
                float iconScale = 0.75f;
                // Keep the item quad centered in the original 14px icon slot:
                // the slot starts five pixels into the card and the 12px
                // rendered item is inset by one pixel on each axis.
                float iconX = cardX + 6.0f;
                float iconY = cardY + 4.0f;
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, panelAnimation.getValue() * opacity);
                drawContext.drawItem(item.getIcon().getItem(), iconX, iconY, iconScale);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                drawContext.drawText(itemMetrics, item.getDisplayName(), cardX + 24.0f,
                    cardY + (cardHeight - itemMetrics.getFontTopOffset()) / 2.0f,
                    ColorPalette.getPrimaryTextColor().mulAlpha(0.92f * panelAnimation.getValue() * opacity));
            }
            int rows = (int)Math.ceil(visibleItems.size() / 2.0f);
            cursorY += rows * cardHeight + Math.max(0, rows - 1) * rowGap + 14.0f;
        }
        if (!query.isEmpty() && !found) {
            String emptyLabel = Localization.translate("nothing_found");
            float emptyWidth = itemMetrics.measureText(emptyLabel);
            drawContext.drawText(itemMetrics, emptyLabel, x + (width - emptyWidth) / 2.0f,
                y + height / 2.0f, ColorPalette.getPrimaryTextColor().mulAlpha(0.45f * panelAnimation.getValue() * opacity));
        }
        moscow.rockstar.render.state.UiScissorStack.pop();
        scrollOffset.setContentBounds(viewportHeight,
            calculateGridHeight(query, groupMetrics, cardHeight, rowGap, groups, hiddenPredicate));
    }

    public AssistSearchResult findAssistItemAt(float mouseX, float mouseY, float x, float y, float width, float height,
                                               ScrollOffset scrollOffset, List<AssistGroup> groups,
                                               TextInputField searchField, Predicate<AssistItem> hiddenPredicate) {
        float contentX = x + 7.0f;
        float contentY = y + 30.0f;
        float contentWidth = width - 14.0f;
        float viewportHeight = height - 33.0f;
        if (!UiUtils.contains(contentX, contentY, contentWidth, viewportHeight, (int)mouseX, (int)mouseY)) {
            return null;
        }
        float columnGap = 4.0f;
        float cardHeight = 20.0f;
        float rowGap = 4.0f;
        float cardWidth = (contentWidth - columnGap) / 2.0f;
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        float cursorY = contentY - (float)scrollOffset.getOffset();
        FontMetrics groupMetrics = Font.MEDIUM.metrics(8.0f);
        for (AssistGroup group : groups) {
            List<AssistItem> visibleItems = new ArrayList<>();
            for (AssistItem item : group.getItems()) {
                if (!query.isEmpty() && !item.getDisplayName().toLowerCase(Locale.ROOT).contains(query)
                    || hiddenPredicate.test(item)) {
                    continue;
                }
                visibleItems.add(item);
            }
            if (visibleItems.isEmpty()) {
                continue;
            }
                cursorY += groupMetrics.getFontTopOffset() + 8.0f;
            for (int index = 0; index < visibleItems.size(); index++) {
                float cardX = contentX + index % 2 * (cardWidth + columnGap);
                float cardY = cursorY + index / 2 * (cardHeight + rowGap);
                if (UiUtils.contains(cardX, cardY, cardWidth, cardHeight, (int)mouseX, (int)mouseY)) {
                    return new AssistSearchResult(visibleItems.get(index));
                }
            }
            int rows = (int)Math.ceil(visibleItems.size() / 2.0f);
            cursorY += rows * cardHeight + Math.max(0, rows - 1) * rowGap + 14.0f;
        }
        return null;
    }

    private float calculateGridHeight(String query, FontMetrics groupMetrics, float cardHeight, float rowGap,
                                      List<AssistGroup> groups, Predicate<AssistItem> hiddenPredicate) {
        float totalHeight = 0.0f;
        for (AssistGroup group : groups) {
            int visibleCount = 0;
            for (AssistItem item : group.getItems()) {
                if (!query.isEmpty() && !item.getDisplayName().toLowerCase(Locale.ROOT).contains(query)
                    || hiddenPredicate.test(item)) {
                    continue;
                }
                visibleCount++;
            }
            if (visibleCount == 0) {
                continue;
            }
            totalHeight += groupMetrics.getFontTopOffset() + 8.0f;
            int rows = (int)Math.ceil(visibleCount / 2.0f);
            totalHeight += rows * cardHeight + Math.max(0, rows - 1) * rowGap + 14.0f;
        }
        return totalHeight;
    }

    public static final class LayoutBounds {
        private final float x;
        private final float y;
        private final float width;
        private final float height;

        public LayoutBounds(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public float getWidth() {
            return this.width;
        }

        public float getHeight() {
            return this.height;
        }
    }
}
