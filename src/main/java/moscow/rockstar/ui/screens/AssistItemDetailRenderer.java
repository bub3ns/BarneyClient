package moscow.rockstar.ui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.items.assist.AssistItemProvider;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.state.UiScissorStack;
import moscow.rockstar.settings.SettingComponent;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.KeyBindingUtil;
import moscow.rockstar.ui.input.KeyDisplayFormatter;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.util.math.MatrixStack;

/** Renders the settings panel belonging to a selected Assist item. */
public final class AssistItemDetailRenderer implements WindowMetricsProvider {

    public AssistItemDetailBounds render(RockstarDrawContext drawContext,
                                         float dialogX, float dialogY, float mainPanelWidth,
                                         float gap, float panelWidth, float contentTop,
                                         float cornerRadius, Animation panelAnimation,
                                         Animation panelVisibility, Animation contentAnimation,
                                         AssistItemSettingsState settingsState,
                                         AssistItemProvider provider, float removalProgress) {
        float settingsHeight = settingsState.getContentHeight();
        float panelHeight = contentTop + cornerRadius * 2.0f + settingsHeight - 2.0f;
        float rightPanelX = dialogX + mainPanelWidth + gap;
        boolean openRight = rightPanelX + panelWidth <= INSTANCE.width() - 6.0f;
        float panelX = openRight ? rightPanelX : dialogX - panelWidth - gap;
        float panelY = Math.max(6.0f, Math.min(dialogY, INSTANCE.height() - panelHeight - 6.0f));
        settingsState.getComponentHost().setBounds(panelX, panelY, panelWidth, panelHeight);
        float alpha = panelAnimation.getValue() * removalProgress * panelVisibility.getValue();

        float clipX;
        float clipWidth;
        if (openRight) {
            clipX = dialogX + mainPanelWidth;
            clipWidth = INSTANCE.width() - clipX;
        } else {
            clipX = 0.0f;
            clipWidth = dialogX;
        }
        UiScissorStack.push((MatrixStack)drawContext.getMatrices(), clipX, 0.0f, clipWidth, INSTANCE.height());

        drawContext.drawShadow(panelX, panelY, panelWidth, panelHeight, 25.0f,
            WidgetState.uniform(11.0f), ColorPalette.BLACK.mulAlpha(0.5f * alpha));
        drawContext.drawBlurredRect(panelX, panelY, panelWidth, panelHeight, 5.0f, 3.0f,
            WidgetState.uniform(11.0f), ColorPalette.WHITE.mulAlpha(alpha));
        drawContext.drawSquircle(panelX, panelY, panelWidth, panelHeight, 3.0f,
            WidgetState.uniform(11.0f), ColorPalette.PANEL_COLOR.mulAlpha(alpha));
        drawContext.drawSquircleBorder(panelX, panelY, panelWidth, panelHeight, 0.5f, 3.0f,
            WidgetState.uniform(11.0f), ColorPalette.BORDER_COLOR.mulAlpha(alpha));

        float[] keyRect = this.drawHeader(drawContext, settingsState, provider, alpha,
            panelX, panelY, panelWidth, contentTop, cornerRadius, contentAnimation);

        float rowX = panelX;
        float rowY = panelY + contentTop + cornerRadius + 1.0f;
        float rowWidth = panelWidth;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        for (SettingComponent component : settingsState.getSettingComponents()) {
            component.setBounds(rowX, rowY, rowWidth, component.getHeight());
            component.setVisibleRegionTop(panelY);
            component.setVisibleRegionHeight(panelHeight);
            component.render(drawContext);
            rowY += component.getHeight();
        }
        for (SettingComponent component : settingsState.getSettingComponents()) {
            component.renderDivider(drawContext);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        UiScissorStack.pop();

        return new AssistItemDetailBounds(panelX, panelY, panelWidth, panelHeight,
            keyRect[0], keyRect[1], keyRect[2], keyRect[3]);
    }

    /** Returns the key-bind chip rect {x, y, w, h}. */
    private float[] drawHeader(RockstarDrawContext drawContext, AssistItemSettingsState settingsState,
                               AssistItemProvider provider, float alpha,
                               float panelX, float panelY, float panelWidth,
                               float contentTop, float cornerRadius, Animation contentAnimation) {
        FontMetrics titleMetrics = Font.REGULAR.metrics(7.0f);
        FontMetrics keyMetrics = Font.REGULAR.metrics(6.0f);
        float headerX = panelX + cornerRadius;
        float headerY = panelY + cornerRadius;
        float headerWidth = panelWidth - cornerRadius * 2.0f;
        float headerHeight = contentTop;
        drawContext.drawRoundedRect(headerX, headerY, headerWidth, headerHeight,
            WidgetState.uniform(5.0f), ColorPalette.getPanelBackgroundColor().mulAlpha(0.4f * alpha));

        float iconBoxSize = 10.0f;
        float itemScale = 0.6875f;
        float itemDrawSize = 16.0f * itemScale;
        float iconLeft = headerX + 4.0f;
        float iconTop = headerY + (headerHeight - iconBoxSize) / 2.0f;
        float itemX = iconLeft + (iconBoxSize - itemDrawSize) / 2.0f;
        float itemY = iconTop + (iconBoxSize - itemDrawSize) / 2.0f;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        drawContext.drawItem(provider.getItemStack().getItem(), itemX, itemY, itemScale);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        String keyLabel = settingsState.isCapturingKeyBinding()
            ? KeyBindingUtil.modifierPrefix(KeyBindingUtil.currentModifiers()) + "..."
            : KeyDisplayFormatter.formatKey(provider.getKeyCode());
        if (keyLabel == null || keyLabel.isEmpty()) {
            keyLabel = "-";
        }
        float keyPadX = 4.0f;
        float keyPadY = 2.0f;
        float keyTextWidth = keyMetrics.measureText(keyLabel);
        float keyWidth = Math.max(12.0f, keyTextWidth + keyPadX * 2.0f);
        float keyX = headerX + headerWidth - 5.0f - keyWidth;
        float keyHeight = keyMetrics.getFontTopOffset() + keyPadY * 2.0f;
        float keyY = headerY + (headerHeight - keyHeight) / 2.0f;
        boolean keyHovered = UiUtils.contains(keyX, keyY, keyWidth, keyHeight, drawContext);
        contentAnimation.setReverse(keyHovered || settingsState.isCapturingKeyBinding());
        drawContext.drawRoundedRect(keyX, keyY, keyWidth, keyHeight, WidgetState.uniform(3.0f),
            ColorPalette.getPanelBackgroundColor()
                .mulAlpha((0.45f + 0.25f * contentAnimation.getValue()) * alpha));
        drawContext.drawText(keyMetrics, keyLabel, keyX + keyPadX,
            keyY + (keyHeight - keyMetrics.getFontTopOffset()) / 2.0f,
            ColorPalette.getPrimaryTextColor().mulAlpha(0.75f * alpha));

        String title = Localization.translate(provider.getSettingKey());
        float titleX = iconLeft + iconBoxSize + 6.0f;
        float titleY = headerY + (headerHeight - titleMetrics.getFontTopOffset()) / 2.0f;
        float titleWidth = Math.max(10.0f, keyX - 6.0f - titleX);
        drawContext.drawFadeoutText(titleMetrics, title, titleX, titleY,
            ColorPalette.getPrimaryTextColor().mulAlpha(0.9f * alpha), 0.85f, 1.0f, titleWidth);

        return new float[]{keyX, keyY, keyWidth, keyHeight};
    }
}
