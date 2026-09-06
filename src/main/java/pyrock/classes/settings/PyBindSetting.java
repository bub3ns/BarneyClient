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
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.input.KeyDisplayFormatter;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PyBindSetting {
    private final IntegerSetting setting;

    public PyBindSetting(PyModule pyModule, String string) {
        this.setting = new IntegerSetting(pyModule.getModule(), string);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyBindSetting(PyHudElement pyHudElement, String string) {
        this.setting = new IntegerSetting((SettingOwner)((Object)pyHudElement), string);
    }

    public PyBindSetting(PyEspElement pyEspElement, String string) {
        this.setting = new IntegerSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
    }

    public PyBindSetting(IntegerSetting integerSetting) {
        this.setting = integerSetting;
    }

    public PyBindSetting set(Object object) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setValue(PyBindSetting.toCode(object));
        return this;
    }

    public PyBindSetting clear() {
        return this.set(-1);
    }

    public int get() {
        return this.setting.getValue();
    }

    public String name() {
        return moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(this.setting.getValue());
    }

    public boolean isSet() {
        return this.setting.getValue() != -1;
    }

    public boolean isKey(Object object) {
        return this.setting.isIntValid(PyBindSetting.toCode(object));
    }

    public boolean matches(Object object) {
        return this.isKey(object);
    }

    static int toCode(Object object) {
        if (object instanceof Number) {
            Number number = (Number)object;
            return number.intValue();
        }
        if (object == null) {
            return -1;
        }
        return KeyDisplayFormatter.parseKey(object.toString());
    }

    @Generated
    public IntegerSetting getSetting() {
        return this.setting;
    }
}
