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
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PySliderSetting {
    private final NumberSetting setting;

    public PySliderSetting(PyModule pyModule, String string) {
        this.setting = new NumberSetting(pyModule.getModule(), string);
        this.setting.setMinValue(1.0f);
        this.setting.setMaxValue(10.0f);
        this.setting.setValue(5.0f);
        this.setting.setStep(1.0f);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PySliderSetting(PyHudElement pyHudElement, String string) {
        this.setting = new NumberSetting((SettingOwner)((Object)pyHudElement), string);
        this.setting.setMinValue(1.0f);
        this.setting.setMaxValue(10.0f);
        this.setting.setValue(5.0f);
        this.setting.setStep(1.0f);
    }

    public PySliderSetting(PyEspElement pyEspElement, String string) {
        this.setting = new NumberSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
        this.setting.setMinValue(1.0f);
        this.setting.setMaxValue(10.0f);
        this.setting.setValue(5.0f);
        this.setting.setStep(1.0f);
    }

    public PySliderSetting(NumberSetting numberSetting) {
        this.setting = numberSetting;
    }

    public PySliderSetting min(double d) {
        this.setting.setMinValue((float)d);
        return this;
    }

    public PySliderSetting max(double d) {
        this.setting.setMaxValue((float)d);
        return this;
    }

    public PySliderSetting step(double d) {
        this.setting.setStep((float)d);
        return this;
    }

    public PySliderSetting set(double d) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setValue((float)d);
        return this;
    }

    public PySliderSetting suffix(String string) {
        this.setting.setUnit(string);
        return this;
    }

    public float get() {
        return this.setting.getValue();
    }

    public float getMin() {
        return this.setting.getMinValue();
    }

    public float getMax() {
        return this.setting.getMaxValue();
    }

    public float getStep() {
        return this.setting.getStep();
    }

    @Generated
    public NumberSetting getSetting() {
        return this.setting;
    }
}

