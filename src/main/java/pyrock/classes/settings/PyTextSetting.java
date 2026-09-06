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
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.StringSetting;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PyTextSetting {
    private final StringSetting setting;

    public PyTextSetting(PyModule pyModule, String string) {
        this.setting = new StringSetting(pyModule.getModule(), string);
        this.setting.setValue("");
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyTextSetting(PyHudElement pyHudElement, String string) {
        this.setting = new StringSetting((SettingOwner)((Object)pyHudElement), string);
        this.setting.setValue("");
    }

    public PyTextSetting(PyEspElement pyEspElement, String string) {
        this.setting = new StringSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
        this.setting.setValue("");
    }

    public PyTextSetting(StringSetting stringSetting) {
        this.setting = stringSetting;
    }

    public PyTextSetting set(String string) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setValue(string == null ? "" : string);
        return this;
    }

    public String get() {
        String string = this.setting.getValue();
        return string == null ? "" : string;
    }

    public PyTextSetting maxLength(int n) {
        this.setting.setMaxLength(n);
        return this;
    }

    public PyTextSetting numberOnly(boolean bl) {
        this.setting.setNumericOnly(bl);
        return this;
    }

    public boolean isEmpty() {
        return this.get().isEmpty();
    }

    @Generated
    public StringSetting getSetting() {
        return this.setting;
    }
}

