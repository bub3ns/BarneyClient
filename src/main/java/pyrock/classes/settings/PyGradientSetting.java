/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.classes.settings;

import lombok.Generated;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.api.scripts.ScriptModule;
import moscow.rockstar.settings.ColorRangeSetting;
import moscow.rockstar.settings.SettingOwner;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;
import pyrock.utility.render.ColorRGBA;

public class PyGradientSetting {
    private final ColorRangeSetting setting;

    public PyGradientSetting(PyModule pyModule, String string) {
        this.setting = new ColorRangeSetting(pyModule.getModule(), string);
        this.defaults();
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyGradientSetting(PyHudElement pyHudElement, String string) {
        this.setting = new ColorRangeSetting((SettingOwner)((Object)pyHudElement), string);
        this.defaults();
    }

    public PyGradientSetting(PyEspElement pyEspElement, String string) {
        this.setting = new ColorRangeSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
        this.defaults();
    }

    public PyGradientSetting(ColorRangeSetting colorRangeSetting) {
        this.setting = colorRangeSetting;
    }

    private void defaults() {
        this.setting.setColorRange(new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f), new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f));
        this.setting.setFirstColorSelected(true);
    }

    public PyGradientSetting set(ColorRGBA colorRGBA, ColorRGBA colorRGBA2) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setColorRange(colorRGBA, colorRGBA2);
        return this;
    }

    public PyGradientSetting first(ColorRGBA colorRGBA) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setFirstColorAndReturn(colorRGBA);
        return this;
    }

    public PyGradientSetting second(ColorRGBA colorRGBA) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setSecondColorAndReturn(colorRGBA);
        return this;
    }

    public ColorRGBA getFirst() {
        return this.setting.getColorRangeSettingColorRGBA();
    }

    public ColorRGBA getSecond() {
        return this.setting.getSecondColor();
    }

    public ColorRGBA at(double d) {
        float f = (float)Math.max(0.0, Math.min(1.0, d));
        return this.getFirst().mix(this.getSecond(), f);
    }

    public PyGradientSetting alpha(boolean bl) {
        this.setting.setFirstColorSelected(bl);
        return this;
    }

    public boolean hasAlpha() {
        return this.setting.isFirstColorSelected();
    }

    @Generated
    public ColorRangeSetting getSetting() {
        return this.setting;
    }
}

