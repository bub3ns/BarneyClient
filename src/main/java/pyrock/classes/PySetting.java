/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Block
 *  net.minecraft.Identifier
 *  net.minecraft.Registries
 */
package pyrock.classes;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.settings.ActionSetting;
import moscow.rockstar.settings.BlockItemSetting;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorRangeSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.EasingSetting;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.RangeSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.StringSetting;
import moscow.rockstar.settings.TextLabelSetting;
import moscow.rockstar.settings.TimeSetting;
import moscow.rockstar.settings.Vector2Setting;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.localization.Localization;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import pyrock.utility.render.ColorRGBA;

public class PySetting {
    private final Setting s;

    public PySetting(Setting setting) {
        this.s = setting;
    }

    public Setting raw() {
        return this.s;
    }

    public String rawName() {
        return this.s.getName();
    }

    public String name() {
        return Localization.translate(this.s.getName());
    }

    public boolean visible() {
        return this.s.hasValidSettingValue();
    }

    public String type() {
        if (this.s instanceof BooleanSetting) {
            return "boolean";
        }
        if (this.s instanceof NumberSetting) {
            return "slider";
        }
        if (this.s instanceof RangeSetting) {
            return "range";
        }
        if (this.s instanceof ModeSetting) {
            return "mode";
        }
        if (this.s instanceof MultiBooleanSetting) {
            return "select";
        }
        if (this.s instanceof ColorSetting) {
            return "color";
        }
        if (this.s instanceof ActionSetting) {
            return "button";
        }
        if (this.s instanceof IntegerSetting) {
            return "bind";
        }
        if (this.s instanceof StringSetting) {
            return "text";
        }
        if (this.s instanceof TimeSetting) {
            return "time";
        }
        if (this.s instanceof ColorRangeSetting) {
            return "gradient";
        }
        if (this.s instanceof Vector2Setting) {
            return "position";
        }
        if (this.s instanceof EasingSetting) {
            return "bezier";
        }
        if (this.s instanceof BlockItemSetting) {
            return "blocks";
        }
        if (this.s instanceof TextLabelSetting) {
            return "info";
        }
        return "other";
    }

    public boolean boolGet() {
        return ((BooleanSetting)this.s).isEnabled();
    }

    public void boolToggle() {
        ((BooleanSetting)this.s).toggle();
    }

    public void boolSet(boolean bl) {
        ((BooleanSetting)this.s).setValueInternal(bl);
    }

    public float numGet() {
        return ((NumberSetting)this.s).getValue();
    }

    public void numSet(float f) {
        ((NumberSetting)this.s).setValue(f);
    }

    public float numMin() {
        return ((NumberSetting)this.s).getMinValue();
    }

    public float numMax() {
        return ((NumberSetting)this.s).getMaxValue();
    }

    public float numStep() {
        return ((NumberSetting)this.s).getStep();
    }

    public String[] options() {
        ArrayList<String> arrayList;
        block3: {
            Object object;
            block2: {
                arrayList = new ArrayList<String>();
                object = this.s;
                if (!(object instanceof ModeSetting)) break block2;
                ModeSetting modeSetting = (ModeSetting)object;
                for (ModeSetting.Option option : modeSetting.getOptions()) {
                    arrayList.add(option.getName());
                }
                break block3;
            }
            object = this.s;
            if (!(object instanceof MultiBooleanSetting)) break block3;
            MultiBooleanSetting multiBooleanSetting = (MultiBooleanSetting)object;
            for (MultiBooleanSetting.Option option : multiBooleanSetting.getOptions()) {
                arrayList.add(option.getName());
            }
        }
        return arrayList.toArray(new String[0]);
    }

    public String[] optionLabels() {
        String[] stringArray = this.options();
        String[] stringArray2 = new String[stringArray.length];
        for (int i = 0; i < stringArray.length; ++i) {
            stringArray2[i] = Localization.translate(stringArray[i]);
        }
        return stringArray2;
    }

    public int modeIndex() {
        ModeSetting modeSetting = (ModeSetting)this.s;
        List<ModeSetting.Option> list = modeSetting.getOptions();
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i) != modeSetting.getSelectedOption()) continue;
            return i;
        }
        return -1;
    }

    public void modeSelect(int n) {
        ModeSetting modeSetting = (ModeSetting)this.s;
        if (n >= 0 && n < modeSetting.getOptions().size()) {
            modeSetting.getOptions().get(n).select();
        }
    }

    public boolean selOn(int n) {
        MultiBooleanSetting multiBooleanSetting = (MultiBooleanSetting)this.s;
        return n >= 0 && n < multiBooleanSetting.getOptions().size() && multiBooleanSetting.getOptions().get(n).isSelected();
    }

    public void selToggle(int n) {
        MultiBooleanSetting multiBooleanSetting = (MultiBooleanSetting)this.s;
        if (n >= 0 && n < multiBooleanSetting.getOptions().size()) {
            multiBooleanSetting.getOptions().get(n).toggle();
        }
    }

    public int selCount() {
        return ((MultiBooleanSetting)this.s).getSelectedOptions().size();
    }

    public ColorRGBA colorGet() {
        return ((ColorSetting)this.s).getColor();
    }

    public void colorSet(ColorRGBA colorRGBA) {
        ((ColorSetting)this.s).setColor(colorRGBA);
    }

    public void click() {
        Runnable runnable = ((ActionSetting)this.s).getAction();
        if (runnable != null) {
            runnable.run();
        }
    }

    public int bindKey() {
        return ((IntegerSetting)this.s).getValue();
    }

    public String bindName() {
        return moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(((IntegerSetting)this.s).getValue());
    }

    public void bindSet(int n) {
        ((IntegerSetting)this.s).setValue(n);
    }

    public String textGet() {
        String string = ((StringSetting)this.s).getValue();
        return string == null ? "" : string;
    }

    public void textSet(String string) {
        ((StringSetting)this.s).setValue(string);
    }

    public int timeGet() {
        return ((TimeSetting)this.s).getTotalSeconds();
    }

    public void timeSet(int n) {
        ((TimeSetting)this.s).setSeconds(n);
    }

    public long timeMillis() {
        return ((TimeSetting)this.s).getMilliseconds();
    }

    public int timeTicks() {
        return ((TimeSetting)this.s).getTicks();
    }

    public String timeFormatted() {
        return ((TimeSetting)this.s).getFormattedValue();
    }

    public ColorRGBA gradientFirst() {
        return ((ColorRangeSetting)this.s).getColorRangeSettingColorRGBA();
    }

    public ColorRGBA gradientSecond() {
        return ((ColorRangeSetting)this.s).getSecondColor();
    }

    public void gradientSet(ColorRGBA colorRGBA, ColorRGBA colorRGBA2) {
        ((ColorRangeSetting)this.s).setColorRange(colorRGBA, colorRGBA2);
    }

    public float posX() {
        return ((Vector2Setting)this.s).getX();
    }

    public float posY() {
        return ((Vector2Setting)this.s).getY();
    }

    public void posSet(float f, float f2) {
        ((Vector2Setting)this.s).setValue(f, f2);
    }

    public float bezierEase(float f) {
        float f2 = Math.max(0.0f, Math.min(1.0f, f));
        Easing easing = ((EasingSetting)this.s).getEasing();
        return easing == null ? f2 : easing.ease(f2, 0.0f, 1.0f, 1.0f);
    }

    public List<String> blocksSelected() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (Identifier class_29602 : ((BlockItemSetting)this.s).getSelectedIds()) {
            arrayList.add(class_29602.toString());
        }
        return arrayList;
    }

    public boolean blockSelected(String string) {
        Identifier class_29602 = PySetting.blockId(string);
        return class_29602 != null && ((BlockItemSetting)this.s).isRegistryIdSelected(class_29602);
    }

    public void blockToggle(String string) {
        Identifier class_29602 = PySetting.blockId(string);
        if (class_29602 == null) {
            return;
        }
        BlockItemSetting blockItemSetting = (BlockItemSetting)this.s;
        if (blockItemSetting.isRegistryIdSelected(class_29602)) {
            blockItemSetting.toggleBlock((Block)Registries.BLOCK.get(class_29602));
        } else {
            blockItemSetting.selectRegistryId(class_29602);
        }
    }

    private static Identifier blockId(String string) {
        if (string == null || string.isBlank()) {
            return null;
        }
        String string2 = string.trim();
        return Identifier.tryParse((String)(string2.indexOf(58) < 0 ? "minecraft:" + string2 : string2));
    }
}
