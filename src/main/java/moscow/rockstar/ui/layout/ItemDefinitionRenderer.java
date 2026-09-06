/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.layout;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.items.config.ItemConfigProcessor;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.text.FontMetrics;
import pyrock.utility.render.ColorRGBA;

public class ItemDefinitionRenderer {
    public static void drawTextLayoutListenerContentForFontMetricsAndInnerLowerAndFloatAndFloatAndFloatAndFloatAndBoolean(RockstarDrawContext drawContext, FontMetrics fontMetrics, ItemConfigProcessor.ItemDefinition itemDefinition, float f, float f2, float f3, float f4, boolean bl) {
        Object object;
        ColorRGBA colorRGBA = ColorPalette.getPanelBackgroundColor().withAlpha(191.25f);
        drawContext.drawRoundedRect(f, f2, f3, f4, WidgetState.uniform(2.0f), colorRGBA);
        if (bl) {
            object = ColorPalette.getAccentColor().withAlpha(110.0f);
            drawContext.drawRoundedRect(f, f2, f3, f4, WidgetState.uniform(2.0f), (ColorRGBA)object);
            colorRGBA = colorRGBA.mix(((ColorRGBA)object).withAlpha(255.0f), ((ColorRGBA)object).getAlpha() / 255.0f);
        }
        drawContext.drawItem(itemDefinition.getStack(), f + (f3 - 16.0f) / 2.0f, f2 + (f4 - 16.0f) / 2.0f - 6.0f, 1.0f);
        object = (itemDefinition.getCustomName() != null ? itemDefinition.getCustomName() : itemDefinition.getStack().getName().getString()).trim();
        if (!((String)object).isEmpty()) {
            ItemDefinitionRenderer.drawTextLayoutListenerContentForFontMetricsAndStringAndFloatAndFloatAndFloatAndColorRGBA(drawContext, fontMetrics, (String)object, f, f2 + f4 - 10.0f, f3, ColorPalette.blendWithContrastBackground(colorRGBA));
        }
    }

    private static void drawTextLayoutListenerContentForFontMetricsAndStringAndFloatAndFloatAndFloatAndColorRGBA(RockstarDrawContext drawContext, FontMetrics fontMetrics, String string, float f, float f2, float f3, ColorRGBA colorRGBA) {
        List<String> list = ItemDefinitionRenderer.createListFromFontMetricsAndStringAndFloat(fontMetrics, string, f3);
        if (list.size() == 1) {
            ItemDefinitionRenderer.drawCenteredOrClippedText(drawContext, fontMetrics, list.getFirst(), f, f2, f3, colorRGBA);
        } else {
            ItemDefinitionRenderer.drawCenteredOrClippedText(drawContext, fontMetrics, list.getFirst(), f, f2 - 2.0f, f3, colorRGBA);
            ItemDefinitionRenderer.drawCenteredOrClippedText(drawContext, fontMetrics, list.get(1), f, f2 + fontMetrics.getFontTopOffset() - 1.0f, f3, colorRGBA);
        }
    }

    private static void drawCenteredOrClippedText(RockstarDrawContext drawContext, FontMetrics fontMetrics, String string, float f, float f2, float f3, ColorRGBA colorRGBA) {
        float f4 = fontMetrics.measureText(string);
        if (f4 <= f3) {
            drawContext.drawText(fontMetrics, string, f + (f3 - f4) / 2.0f + 1.0f, f2, colorRGBA);
        } else {
            drawContext.drawFadeoutText(fontMetrics, string, f + 2.0f, f2, colorRGBA, 0.8f, 1.0f, f3 - 4.0f);
        }
    }

    private static List<String> createListFromFontMetricsAndStringAndFloat(FontMetrics fontMetrics, String string, float f) {
        String[] stringArray = string.split("\\s+");
        if (stringArray.length <= 1) {
            return List.of(string);
        }
        ArrayList<String> arrayList = new ArrayList<String>(2);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stringArray.length; ++i) {
            String string2;
            if (stringArray[i].isEmpty()) continue;
            String string3 = string2 = stringBuilder.isEmpty() ? stringArray[i] : String.valueOf(stringBuilder) + " " + stringArray[i];
            if (fontMetrics.measureText(string2) <= f || stringBuilder.isEmpty()) {
                stringBuilder.setLength(0);
                stringBuilder.append(string2);
                continue;
            }
            arrayList.add(stringBuilder.toString());
            stringBuilder.setLength(0);
            for (int j = i; j < stringArray.length; ++j) {
                if (stringArray[j].isEmpty()) continue;
                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append(' ');
                }
                stringBuilder.append(stringArray[j]);
            }
            arrayList.add(stringBuilder.toString());
            return arrayList;
        }
        if (!stringBuilder.isEmpty()) {
            arrayList.add(stringBuilder.toString());
        }
        return arrayList.isEmpty() ? List.of(string) : arrayList;
    }
}
