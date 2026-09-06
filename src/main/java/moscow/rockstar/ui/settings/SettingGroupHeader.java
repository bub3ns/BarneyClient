/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.settings;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.ui.hud.DynamicIslandEntry;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.factory.UiNodeFactory;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.RollingNumberComponent;
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.modules.visuals.hud.DynamicIslandHud;
import pyrock.utility.render.ColorRGBA;

public class SettingGroupHeader
extends DynamicIslandEntry
implements ClientAccess {
    private String title = "";
    private String localizedTitle = "";
    private int itemCount = -1;
    private String categoryLabel = "text";
    private ColorRGBA accentColor = ColorPalette.getAccentColor();
    private UiNode contentNode;

    public SettingGroupHeader(MultiBooleanSetting multiBooleanSetting, String string) {
        super(multiBooleanSetting, string);
    }

    public UiNode content(DynamicIslandHud island) {
        if (this.contentNode == null) {
            Component component2 = UiNodeFactory.createRowComponent(15.0f, Insets.of(0.0f, 4.0f, 0.0f, 4.0f), 3.5f).alignment(Alignment.CENTER);
            Component component3 = UiNodeFactory.createOffsetComponent(() -> -20.0f * (1.0f - this.animation.getValue())).horizontal().height(8.0f).padding(Insets.of(0.0f, 2.5f, 0.0f, 3.0f)).alignment(Alignment.CENTER).renderHook((drawContext, component) -> drawContext.drawRoundedRect(component.x(), component.y(), component.w(), component.h(), WidgetState.uniform(3.0f), this.accentColor.withAlpha(255.0f * this.animation.getValue())));
            component3.add(UiNodeFactory.createTextNode(Font.MEDIUM.metrics(6.0f), () -> this.title, () -> ColorPalette.blendWithContrastBackground(this.accentColor).withAlpha(255.0f * this.animation.getValue())));
            component3.add(new RollingNumberComponent(Font.MEDIUM.metrics(6.0f), () -> this.itemCount).rollHeight(5.0f).padLeadingZero().setColorProvider(() -> ColorPalette.blendWithContrastBackground(this.accentColor).withAlpha(255.0f * this.animation.getValue())).interactive(false));
            component3.add(UiNodeFactory.createTextNode(Font.MEDIUM.metrics(6.0f), () -> this.localizedTitle, () -> ColorPalette.blendWithContrastBackground(this.accentColor).withAlpha(255.0f * this.animation.getValue())));
            component2.add(component3);
            component2.add(UiNodeFactory.createTextNode(Font.MEDIUM.metrics(7.0f), () -> this.categoryLabel, () -> ColorPalette.getPrimaryTextColor().withAlpha(255.0f * this.animation.getValue()), () -> 10.0f * (1.0f - this.animation.getValue())));
            this.contentNode = component2;
        }
        return this.contentNode;
    }

    public void setHeaderInfo(String string, int n, String string2, ColorRGBA colorRGBA) {
        this.setHeaderInfo("", string, n, string2, colorRGBA);
    }

    public void setHeaderInfo(String string, String string2, int n, String string3, ColorRGBA colorRGBA) {
        this.title = string;
        this.localizedTitle = string2;
        this.itemCount = n;
        this.categoryLabel = string3;
        this.accentColor = colorRGBA;
    }

    public boolean isVisible() {
        return ServerDetector.enabled;
    }
}
