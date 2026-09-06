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
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.SettingOwner;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PyBooleanSetting {
    private final BooleanSetting setting;

    public PyBooleanSetting(PyModule pyModule, String string) {
        this.setting = new BooleanSetting(pyModule.getModule(), string);
        this.setting.setValueInternal(false);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyBooleanSetting(PyHudElement pyHudElement, String string) {
        this.setting = new BooleanSetting((SettingOwner)((Object)pyHudElement), string);
        this.setting.setValueInternal(false);
    }

    public PyBooleanSetting(PyEspElement pyEspElement, String string) {
        this.setting = new BooleanSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
        this.setting.setValueInternal(false);
    }

    public PyBooleanSetting(BooleanSetting booleanSetting) {
        this.setting = booleanSetting;
    }

    public PyBooleanSetting set(boolean bl) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setValueInternal(bl);
        return this;
    }

    public boolean get() {
        return this.setting.isEnabled();
    }

    public PyBooleanSetting toggle() {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.toggle();
        return this;
    }

    @Generated
    public BooleanSetting getSetting() {
        return this.setting;
    }
}

