package moscow.rockstar.ui.text;
import moscow.rockstar.ui.localization.Localization;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistItemProvider;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.KeyBindingUtil;
import moscow.rockstar.ui.input.KeyDisplayFormatter;
import moscow.rockstar.ui.screens.AssistGroup;
import moscow.rockstar.ui.screens.AssistItem;
import moscow.rockstar.ui.screens.AssistItemSelection;
import moscow.rockstar.ui.screens.AssistSearchResult;
import moscow.rockstar.ui.state.ScrollOffset;
import moscow.rockstar.ui.state.TextScrollState;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;

public class TextLayoutManager {
    /** Receives world-space glyph/effect quads while a text batch is open. */
    public interface WorldEffectRenderer {
        void renderWorldEffect(moscow.rockstar.render.world.WorldEffectDispatcher effect,
                               float x, float y, float width, float height, int color);
    }

    public LayoutBounds renderCategoryTabs(RockstarDrawContext drawContext, float x, float y, float width,
                                           float opacity, Animation panelAnimation, ItemCategory selectedCategory,
                                           Map<ItemCategory, Animation> categoryAnimations, Animation addButtonAnimation) {
        FontMetrics fontMetrics = Font.REGULAR.metrics(7.0f);
        float tabX = x + 7.0f;
        float tabY = y + 24.0f;
        drawContext.drawText(Font.MEDIUM.metrics(8.0f), Localization.translate("macro.title"), tabX + 1.0f, tabY - 14.0f,
            ColorPalette.getPrimaryTextColor().mulAlpha(0.95f * panelAnimation.getValue() * opacity));
        for (ItemCategory category : ItemCategory.values()) {
            String label = category.getCategoryLabel();
            float tabWidth = fontMetrics.measureText(label) + 8.0f;
            boolean selected = selectedCategory == category;
            boolean hovered = UiUtils.contains(tabX, tabY, tabWidth, 13.0, drawContext);
            Animation hoverAnimation = categoryAnimations.computeIfAbsent(category,
                ignored -> new Animation(200L, 0.0f, Easing.easeInOutCubicBezier));
            hoverAnimation.update(selected ? 1.0f : (hovered ? 0.67f : 0.0f));
            float backgroundAlpha = 0.4f + 0.6f * hoverAnimation.getValue();
            float textAlpha = selected ? 1.0f : 0.75f;
            drawContext.drawRoundedRect(tabX, tabY, tabWidth, 13.0f, WidgetState.uniform(3.0f),
                ColorPalette.getPanelBackgroundColor().mulAlpha(backgroundAlpha * panelAnimation.getValue() * opacity));
            drawContext.drawText(fontMetrics, label, tabX + 4.0f,
                tabY + (13.0f - fontMetrics.getFontTopOffset()) / 2.0f,
                ColorPalette.getPrimaryTextColor().mulAlpha(textAlpha * panelAnimation.getValue() * opacity));
            tabX += tabWidth + 4.0f;
        }
        String addLabel = Localization.translate("macro.add");
        float addWidth = fontMetrics.measureText(addLabel) + 8.0f;
        float addHeight = 13.0f;
        float addX = x + width - addWidth - 7.0f;
        float addY = y + 24.0f;
        boolean addHovered = UiUtils.contains(addX, addY, addWidth, addHeight, drawContext);
        addButtonAnimation.setReverse(addHovered);
        float addBackgroundAlpha = 0.4f + 0.6f * addButtonAnimation.getValue();
        drawContext.drawRoundedRect(addX, addY, addWidth, addHeight, WidgetState.uniform(3.0f),
            ColorPalette.getPanelBackgroundColor().mulAlpha(addBackgroundAlpha * panelAnimation.getValue() * opacity));
        drawContext.drawText(fontMetrics, addLabel, addX + 4.0f,
            addY + (addHeight - fontMetrics.getFontTopOffset()) / 2.0f,
            ColorPalette.getPrimaryTextColor().mulAlpha((0.75f + 0.25f * addButtonAnimation.getValue())
                * panelAnimation.getValue() * opacity));
        return new LayoutBounds(addX, addY, addWidth, addHeight);
    }

    public void renderAssistItemGrid(RockstarDrawContext drawContext, float x, float y, float width, float height,
                                     float opacity, Animation panelAnimation, ScrollOffset scrollOffset,
                                     ItemCategory selectedCategory, List<AssistItemProvider> providers,
                                     AssistItemProvider selectedProvider, AssistItemProvider pendingProvider,
                                     int mouseX, int mouseY,
                                     Map<AssistItemProvider, TextScrollState> labelScrollStates,
                                     Map<AssistItemProvider, Animation> hoverAnimations,
                                     Map<AssistItemProvider, Animation> selectionAnimations,
                                     ToDoubleFunction<AssistItemProvider> visibilityProgress) {
        FontMetrics categoryMetrics = Font.MEDIUM.metrics(8.0f);
        FontMetrics itemMetrics = Font.REGULAR.metrics(7.0f);
        FontMetrics keyMetrics = Font.REGULAR.metrics(6.0f);
        float contentX = x + 7.0f;
        float contentY = y + 44.0f;
        float contentWidth = width - 14.0f;
        float viewportHeight = height - 40.0f;
        if (providers.isEmpty()) {
            String emptyLabel = Localization.translate("macro.empty");
            float emptyWidth = itemMetrics.measureText(emptyLabel);
            drawContext.drawText(itemMetrics, emptyLabel, contentX + (contentWidth - emptyWidth) / 2.0f,
                contentY + viewportHeight / 2.0f - itemMetrics.getFontTopOffset() / 2.0f,
                ColorPalette.getPrimaryTextColor().mulAlpha(0.35f * opacity));
            return;
        }
        float columnGap = 4.0f;
        float cardHeight = 20.0f;
        float rowGap = 4.0f;
        float cardWidth = (contentWidth - columnGap) / 2.0f;
        float cardStartY = contentY - (float)scrollOffset.getOffset();
        float sectionGap = 14.0f;
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(),
            contentX, contentY - 2.0f, contentWidth, Math.max(0.0f, viewportHeight - 4.0f));
        float cursorY = cardStartY;
        if (selectedCategory == ItemCategory.ALL) {
            for (ItemCategory category : ItemCategory.values()) {
                if (category == ItemCategory.ALL) {
                    continue;
                }
                List<AssistItemProvider> categoryProviders = providers.stream()
                    .filter(provider -> provider.getCategory() == category).toList();
                if (categoryProviders.isEmpty()) {
                    continue;
                }
                drawContext.drawText(categoryMetrics, category.getCategoryLabel(), contentX + 1.0f, cursorY + 2.0f,
                    ColorPalette.getPrimaryTextColor().mulAlpha(0.95f * panelAnimation.getValue() * opacity));
                cursorY += categoryMetrics.getFontTopOffset() + 10.0f;
                for (int index = 0; index < categoryProviders.size(); index++) {
                    AssistItemProvider provider = categoryProviders.get(index);
                    float cardX = contentX + index % 2 * (cardWidth + columnGap);
                    float cardY = cursorY + index / 2 * (cardHeight + rowGap);
                    if (cardY + cardHeight < contentY - 10.0f || cardY > contentY + viewportHeight + 10.0f) {
                        continue;
                    }
                    renderProviderCard(drawContext, cardX, cardY, cardWidth, cardHeight, provider,
                        hoverAnimations, selectionAnimations, selectedProvider, pendingProvider,
                        mouseX, mouseY, labelScrollStates, keyMetrics, itemMetrics, opacity,
                        panelAnimation, visibilityProgress.applyAsDouble(provider));
                }
                int rows = (int)Math.ceil(categoryProviders.size() / 2.0f);
                cursorY += rows * cardHeight + Math.max(0, rows - 1) * rowGap + sectionGap;
            }
        } else {
            List<AssistItemProvider> categoryProviders = providers.stream()
                .filter(provider -> provider.getCategory() == selectedCategory).toList();
            if (categoryProviders.isEmpty()) {
                String emptyLabel = Localization.translate("macro.empty");
                float emptyWidth = itemMetrics.measureText(emptyLabel);
                drawContext.drawText(itemMetrics, emptyLabel, contentX + (contentWidth - emptyWidth) / 2.0f,
                    contentY + viewportHeight / 2.0f - itemMetrics.getFontTopOffset() / 2.0f,
                    ColorPalette.getPrimaryTextColor().mulAlpha(0.35f * opacity));
            } else {
                for (int index = 0; index < categoryProviders.size(); index++) {
                    AssistItemProvider provider = categoryProviders.get(index);
                    float cardX = contentX + index % 2 * (cardWidth + columnGap);
                    float cardY = cursorY + index / 2 * (cardHeight + rowGap);
                    if (cardY + cardHeight < contentY - 10.0f || cardY > contentY + viewportHeight + 10.0f) {
                        continue;
                    }
                    renderProviderCard(drawContext, cardX, cardY, cardWidth, cardHeight, provider,
                        hoverAnimations, selectionAnimations, selectedProvider, pendingProvider,
                        mouseX, mouseY, labelScrollStates, keyMetrics, itemMetrics, opacity,
                        panelAnimation, visibilityProgress.applyAsDouble(provider));
                }
            }
        }
        moscow.rockstar.render.state.UiScissorStack.pop();
        scrollOffset.setContentBounds(viewportHeight,
            calculateAssistGridHeight(selectedCategory, providers, categoryMetrics, cardHeight, rowGap));
    }

    public AssistItemSelection findAssistItemAt(float mouseX, float mouseY, float x, float y, float width, float height,
                                                ScrollOffset scrollOffset, ItemCategory selectedCategory,
                                                List<AssistItemProvider> providers) {
        float contentX = x + 7.0f;
        float contentY = y + 44.0f;
        float contentWidth = width - 14.0f;
        float viewportHeight = height - 40.0f;
        if (!UiUtils.contains(contentX, contentY, contentWidth, viewportHeight, (int)mouseX, (int)mouseY)) {
            return null;
        }
        FontMetrics categoryMetrics = Font.MEDIUM.metrics(8.0f);
        FontMetrics keyMetrics = Font.REGULAR.metrics(6.0f);
        float columnGap = 4.0f;
        float cardHeight = 20.0f;
        float rowGap = 4.0f;
        float cardWidth = (contentWidth - columnGap) / 2.0f;
        float cursorY = contentY - (float)scrollOffset.getOffset();
        if (selectedCategory == ItemCategory.ALL) {
            for (ItemCategory category : ItemCategory.values()) {
                if (category == ItemCategory.ALL) {
                    continue;
                }
                List<AssistItemProvider> categoryProviders = providers.stream()
                    .filter(provider -> provider.getCategory() == category).toList();
                if (categoryProviders.isEmpty()) {
                    continue;
                }
                cursorY += categoryMetrics.getFontTopOffset() + 10.0f;
                AssistItemSelection selection = findInProviderList(mouseX, mouseY, cursorY, contentX, cardWidth,
                    columnGap, cardHeight, rowGap, categoryProviders, keyMetrics);
                if (selection != null) {
                    return selection;
                }
                int rows = (int)Math.ceil(categoryProviders.size() / 2.0f);
                cursorY += rows * cardHeight + Math.max(0, rows - 1) * rowGap + 14.0f;
            }
        } else {
            List<AssistItemProvider> categoryProviders = providers.stream()
                .filter(provider -> provider.getCategory() == selectedCategory).toList();
            return findInProviderList(mouseX, mouseY, cursorY, contentX, cardWidth, columnGap, cardHeight,
                rowGap, categoryProviders, keyMetrics);
        }
        return null;
    }

    private AssistItemSelection findInProviderList(float mouseX, float mouseY, float startY, float contentX,
                                                   float cardWidth, float columnGap, float cardHeight, float rowGap,
                                                   List<AssistItemProvider> providers, FontMetrics keyMetrics) {
        for (int index = 0; index < providers.size(); index++) {
            AssistItemProvider provider = providers.get(index);
            float cardX = contentX + index % 2 * (cardWidth + columnGap);
            float cardY = startY + index / 2 * (cardHeight + rowGap);
            if (!UiUtils.contains(cardX, cardY, cardWidth, cardHeight, (int)mouseX, (int)mouseY)) {
                continue;
            }
            String keyLabel = KeyDisplayFormatter.formatKey(provider.getKeyCode());
            float keyHeight = 12.0f;
            float keyWidth = Math.max(16.0f, keyMetrics.measureText(keyLabel) + 8.0f);
            float keyX = cardX + cardWidth - keyWidth - 5.0f;
            float keyY = cardY + (cardHeight - keyHeight) / 2.0f;
            return new AssistItemSelection(provider, cardX, cardY, cardWidth, cardHeight,
                keyX, keyY, keyWidth, keyHeight);
        }
        return null;
    }

    private void renderProviderCard(RockstarDrawContext drawContext, float x, float y, float width, float height,
                                    AssistItemProvider provider, Map<AssistItemProvider, Animation> hoverAnimations,
                                    Map<AssistItemProvider, Animation> selectionAnimations,
                                    AssistItemProvider selectedProvider, AssistItemProvider pendingProvider,
                                    int mouseX, int mouseY, Map<AssistItemProvider, TextScrollState> labelScrollStates,
                                    FontMetrics keyMetrics, FontMetrics itemMetrics, float opacity,
                                    Animation panelAnimation, double progress) {
        Animation hoverAnimation = hoverAnimations.computeIfAbsent(provider,
            ignored -> new Animation(200L, 0.0f, Easing.easeInOutCubicBezier));
        boolean selected = provider == selectedProvider;
        boolean hovered = UiUtils.contains(x, y, width, height, drawContext);
        hoverAnimation.setReverse(hovered || selected);
        float itemOpacity = (float)progress;
        float backgroundAlpha = (selected ? 0.85f : 0.55f + 0.3f * itemOpacity)
            * panelAnimation.getValue() * opacity * itemOpacity;
        ItemRenderUtils.translateAndScale(drawContext.getMatrices(), x + width / 2.0f, y + height / 2.0f,
            0.85f + 0.15f * opacity);
        drawContext.drawRoundedRect(x, y, width, height, WidgetState.uniform(5.0f),
            ColorPalette.getPanelBackgroundColor().mulAlpha(backgroundAlpha));
        float iconBoxSize = 14.0f;
        float iconScale = 0.75f;
        float iconX = x + 4.0f + (iconBoxSize - 16.0f * iconScale) / 2.0f;
        float iconY = y + (height - iconBoxSize) / 2.0f + (iconBoxSize - 16.0f * iconScale) / 2.0f;
        float iconOpacity = panelAnimation.getValue() * opacity * itemOpacity;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, iconOpacity);
        drawContext.drawItem(provider.getItemStack().getItem(), iconX, iconY, iconScale);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        String keyLabel = selected
            ? KeyBindingUtil.modifierPrefix(KeyBindingUtil.currentModifiers()) + "..."
            : KeyDisplayFormatter.formatKey(provider.getKeyCode());
        float keyHeight = 12.0f;
        float keyWidth = Math.max(16.0f, keyMetrics.measureText(keyLabel) + 8.0f);
        float keyX = x + width - keyWidth - 5.0f;
        float keyY = y + (height - keyHeight) / 2.0f;
        Animation keyAnimation = selectionAnimations.computeIfAbsent(provider,
            ignored -> new Animation(200L, 0.0f, Easing.easeInOutCubicBezier));
        keyAnimation.setReverse(UiUtils.contains(keyX, keyY, keyWidth, keyHeight, mouseX, mouseY)
            || selected || pendingProvider == provider);
        float keyOpacity = (0.6f + 0.4f * keyAnimation.getValue())
            * panelAnimation.getValue() * opacity * itemOpacity;
        drawContext.drawRoundedRect(keyX, keyY, keyWidth, keyHeight, WidgetState.uniform(4.0f),
            ColorPalette.getPanelBackgroundColor().mulAlpha(keyOpacity));
        drawContext.drawText(keyMetrics, keyLabel, keyX + (keyWidth - keyMetrics.measureText(keyLabel)) / 2.0f,
            keyY + (keyHeight - keyMetrics.getFontTopOffset()) / 2.0f,
            ColorPalette.getPrimaryTextColor().mulAlpha(0.95f * panelAnimation.getValue() * opacity * itemOpacity));

        float labelX = x + 4.0f + iconBoxSize + 2.0f;
        float labelWidth = keyX - 4.0f - labelX;
        TextScrollState scrollState = labelScrollStates.computeIfAbsent(provider,
            ignored -> new TextScrollState());
        float labelOffset = scrollState.update(itemMetrics.measureText(provider.getDisplayName()), labelWidth,
            itemOpacity > 0.05f);
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(),
            labelX - 3.0f, y - 3.0f, Math.max(1.0f, labelWidth) + 6.0f, height + 6.0f);
        drawContext.pushMatrix();
        drawContext.getMatrices().translate(-labelOffset, 0.0f, 0.0f);
        drawContext.drawFadeoutText(itemMetrics, provider.getDisplayName(), labelX,
            y + (height - itemMetrics.getFontTopOffset()) / 2.0f,
            ColorPalette.getPrimaryTextColor().mulAlpha((0.75f + 0.25f * itemOpacity)
                * panelAnimation.getValue() * opacity * itemOpacity), 0.95f, 1.0f, labelWidth + 10.0f);
        drawContext.popMatrix();
        moscow.rockstar.render.state.UiScissorStack.pop();
        ItemRenderUtils.popMatrix(drawContext.getMatrices());
    }

    private float calculateAssistGridHeight(ItemCategory selectedCategory, List<AssistItemProvider> providers,
                                            FontMetrics categoryMetrics, float cardHeight, float rowGap) {
        float totalHeight = 0.0f;
        if (selectedCategory == ItemCategory.ALL) {
            for (ItemCategory category : ItemCategory.values()) {
                if (category == ItemCategory.ALL) {
                    continue;
                }
                List<AssistItemProvider> categoryProviders = providers.stream()
                    .filter(provider -> provider.getCategory() == category).toList();
                if (categoryProviders.isEmpty()) {
                    continue;
                }
                totalHeight += categoryMetrics.getFontTopOffset() + 10.0f;
                int rows = (int)Math.ceil(categoryProviders.size() / 2.0f);
                totalHeight += rows * cardHeight + Math.max(0, rows - 1) * rowGap + 14.0f;
            }
        } else {
            int count = (int)providers.stream().filter(provider -> provider.getCategory() == selectedCategory).count();
            int rows = (int)Math.ceil(count / 2.0f);
            totalHeight = rows * cardHeight + Math.max(0, rows - 1) * rowGap;
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
