/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.screens;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.color.ColorPickerHost;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.DragMode;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextComponent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

public class TestCategoryScreen
extends ColorPickerHost {
    private static final String[] CATEGORY_NAMES = new String[]{"Combat", "Movement", "Visuals", "Misc"};
    private final FontMetrics fontMetrics = Font.MEDIUM.metrics(7.0f);
    private final FontMetrics indexFontMetrics = Font.REGULAR.metrics(6.0f);
    private String selectedCategory = CATEGORY_NAMES[0];
    private Component rootComponent;

    @Override
    @Compile(obfuscation=4)
    protected void initializeScreen() {
        super.initializeScreen();
        this.clearRoots();
        float f = 360.0f;
        float f2 = 252.0f;
        float f3 = 10.0f;
        float f4 = 22.0f;
        float f5 = 8.0f;
        Component component2 = new Component().vertical().padding(f3).gap(f5).squircle(6.0f).cornerRadius(10.0f).background(component -> ColorPalette.getPanelColor().withAlpha(235.0f)).size(f, f2).center().draggable(DragMode.BOTH);
        Component component3 = new Component().horizontal().gap(6.0f).fillWidth().height(f4).overflowMode(JustifyContent.SPACE_BETWEEN).draggable(DragMode.BOTH);
        for (String string : CATEGORY_NAMES) {
            component3.add(this.createCategoryButton(string));
        }
        this.rootComponent = new Component().columns(2).gap(6.0f).fillWidth().height(f2 - f3 * 2.0f - f4 - f5).scrollable().exitStagger(220.0f).addAll(this.createCategoryItems(this.selectedCategory));
        component2.add(component3);
        component2.add(this.rootComponent);
        this.add(component2);
    }

    private TextComponent createCategoryButton(String string) {
        float f = this.fontMetrics.measureText(string) + 18.0f;
        return new TextComponent().height(22.0f).width(f).radius(6.0f).bind("sel", () -> this.selectedCategory.equals(string)).background(textComponent -> ColorPalette.getAccentColor().mulAlpha(0.1f + 0.2f * textComponent.sig("sel") + 0.1f * textComponent.hover())).border(1.0f, textComponent -> ColorPalette.getAccentColor().mulAlpha(0.0f + 0.7f * textComponent.sig("sel"))).text(this.fontMetrics, string, textComponent -> ColorPalette.getPrimaryTextColor().mix(ColorPalette.getAccentColor(), textComponent.sig("sel")).mulAlpha(0.6f + 0.4f * Math.max(textComponent.sig("sel"), textComponent.hover()))).textAlign(Alignment.CENTER).cursor(Cursor.HAND).onClick(() -> this.selectCategory(string)).enter(Transition.PROGRESS_ONLY).exit(Transition.PROGRESS_ONLY);
    }

    private void selectCategory(String string) {
        if (this.selectedCategory.equals(string)) {
            return;
        }
        this.selectedCategory = string;
        this.rootComponent.updateChildren(this.createCategoryItems(this.selectedCategory));
    }

    private List<TextComponent> createCategoryItems(String string) {
        ArrayList<TextComponent> arrayList = new ArrayList<TextComponent>(100);
        for (int i = 0; i < 100; ++i) {
            int n = i;
            ColorRGBA colorRGBA = ColorRGBA.fromHSB(((float)(string.hashCode() & 0xFF) / 255.0f + (float)i * 0.041f) % 1.0f, 0.55f, 0.95f);
            String string2 = string + " #" + (i + 1);
            arrayList.add(new TextComponent().fillWidth().height(34.0f).radius(6.0f).cursor(Cursor.HAND).background(textComponent -> ColorPalette.getPanelBackgroundColor().mulAlpha(0.25f + 0.2f * textComponent.hover())).textInset(18.0f).text(this.fontMetrics, string2, textComponent -> ColorPalette.getPrimaryTextColor().mulAlpha(0.85f)).paint((drawContext, textComponent) -> {
                drawContext.drawRoundedRect(textComponent.x() + 7.0f, textComponent.y() + textComponent.h() / 2.0f - 4.0f, 4.0f, 8.0f, WidgetState.uniform(2.0f), colorRGBA);
                drawContext.drawText(this.indexFontMetrics, "element index " + n, textComponent.x() + 18.0f, textComponent.y() + textComponent.h() / 2.0f + 1.5f, ColorPalette.getPrimaryTextColor().mulAlpha(0.4f));
            }).lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(500L, Easing.easeOutBack)).enter(Transition.slideUp(-30.0f)).exit(Transition.slideUp(-30.0f)));
        }
        return arrayList;
    }
}

