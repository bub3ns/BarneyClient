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
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.localization.Localization;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PyModeSetting {
    private final ModeSetting setting;

    public PyModeSetting(PyModule pyModule, String string) {
        this.setting = new ModeSetting(pyModule.getModule(), string);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyModeSetting(PyHudElement pyHudElement, String string) {
        this.setting = new ModeSetting((SettingOwner)((Object)pyHudElement), string);
    }

    public PyModeSetting(PyEspElement pyEspElement, String string) {
        this.setting = new ModeSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
    }

    public PyModeSetting(ModeSetting modeSetting) {
        this.setting = modeSetting;
    }

    public PyModeSetting add(String string) {
        if (this.has(string)) {
            return this;
        }
        ScriptDescriptor.rememberSettingValue(this.setting);
        ModeSetting.Option option = new ModeSetting.Option(this.setting, string);
        ScriptDescriptor.registerModeOption(this.setting, option);
        return this;
    }

    public boolean has(String string) {
        if (string == null) {
            return false;
        }
        for (ModeSetting.Option option : this.setting.getOptions()) {
            if (!string.equalsIgnoreCase(option.getName()) && !string.equalsIgnoreCase(Localization.translate(option.getName()))) continue;
            return true;
        }
        return false;
    }

    public String[] options() {
        List<ModeSetting.Option> list = this.setting.getOptions();
        String[] stringArray = new String[list.size()];
        for (int i = 0; i < stringArray.length; ++i) {
            stringArray[i] = list.get(i).getName();
        }
        return stringArray;
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
        List<ModeSetting.Option> list = this.setting.getOptions();
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i) != this.setting.getSelectedOption()) continue;
            return i;
        }
        return -1;
    }

    public boolean isKey(String string) {
        ModeSetting.Option option = this.setting.getSelectedOption();
        if (option == null || string == null) {
            return false;
        }
        return string.equalsIgnoreCase(option.getName()) || string.equalsIgnoreCase(Localization.translate(option.getName()));
    }

    public int count() {
        return this.setting.getOptions().size();
    }

    public PyModeSetting select(String string) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        for (ModeSetting.Option option : this.setting.getOptions()) {
            if (!option.getName().equals(string)) continue;
            this.setting.select(option);
            break;
        }
        return this;
    }

    public String get() {
        return this.setting.getSelectedOption() != null ? this.setting.getSelectedOption().getName() : null;
    }

    public boolean is(String string) {
        return this.setting.getSelectedOption() != null && this.setting.getSelectedOption().getName().equals(string);
    }

    @Generated
    public ModeSetting getSetting() {
        return this.setting;
    }
}

