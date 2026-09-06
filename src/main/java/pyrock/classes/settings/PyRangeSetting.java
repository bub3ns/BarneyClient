/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.classes.settings;

import java.util.List;
import lombok.Generated;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.api.scripts.ScriptModule;
import moscow.rockstar.settings.RangeSetting;
import moscow.rockstar.settings.SettingOwner;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PyRangeSetting {
    private final RangeSetting setting;

    public PyRangeSetting(PyModule pyModule, String string) {
        this.setting = new RangeSetting(pyModule.getModule(), string);
        this.setting.setMinimum(1.0f);
        this.setting.setMaximum(10.0f);
        this.setting.setFirstValue(3.0f);
        this.setting.setSecondValue(7.0f);
        this.setting.setStep(1.0f);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyRangeSetting(PyHudElement pyHudElement, String string) {
        this.setting = new RangeSetting((SettingOwner)((Object)pyHudElement), string);
        this.setting.setMinimum(1.0f);
        this.setting.setMaximum(10.0f);
        this.setting.setFirstValue(3.0f);
        this.setting.setSecondValue(7.0f);
        this.setting.setStep(1.0f);
    }

    public PyRangeSetting(PyEspElement pyEspElement, String string) {
        this.setting = new RangeSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
        this.setting.setMinimum(1.0f);
        this.setting.setMaximum(10.0f);
        this.setting.setFirstValue(3.0f);
        this.setting.setSecondValue(7.0f);
        this.setting.setStep(1.0f);
    }

    public PyRangeSetting(RangeSetting rangeSetting) {
        this.setting = rangeSetting;
    }

    public PyRangeSetting min(double d) {
        this.setting.setMinimum((float)d);
        return this;
    }

    public PyRangeSetting max(double d) {
        this.setting.setMaximum((float)d);
        return this;
    }

    public PyRangeSetting step(double d) {
        this.setting.setStep((float)d);
        return this;
    }

    public PyRangeSetting first(double d) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setFirstValue((float)d);
        return this;
    }

    public PyRangeSetting second(double d) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setSecondValue((float)d);
        return this;
    }

    public float getFirst() {
        return this.setting.getFirstValue();
    }

    public float getSecond() {
        return this.setting.getSecondValue();
    }

    public float getMin() {
        return this.setting.getMinimum();
    }

    public float getMax() {
        return this.setting.getMaximum();
    }

    public float getStep() {
        return this.setting.getStep();
    }

    public List<Float> get() {
        return List.of(Float.valueOf(this.setting.getFirstValue()), Float.valueOf(this.setting.getSecondValue()));
    }

    @Generated
    public RangeSetting getSetting() {
        return this.setting;
    }
}

