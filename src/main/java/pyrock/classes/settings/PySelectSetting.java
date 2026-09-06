/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.classes.settings;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.api.scripts.ScriptModule;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.localization.Localization;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PySelectSetting {
    private final MultiBooleanSetting setting;

    public PySelectSetting(PyModule pyModule, String string) {
        this.setting = new MultiBooleanSetting(pyModule.getModule(), string);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PySelectSetting(PyHudElement pyHudElement, String string) {
        this.setting = new MultiBooleanSetting((SettingOwner)((Object)pyHudElement), string);
    }

    public PySelectSetting(PyEspElement pyEspElement, String string) {
        this.setting = new MultiBooleanSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
    }

    public PySelectSetting(MultiBooleanSetting multiBooleanSetting) {
        this.setting = multiBooleanSetting;
    }

    public PySelectSetting add(String string) {
        if (this.has(string)) {
            return this;
        }
        ScriptDescriptor.rememberSettingValue(this.setting);
        MultiBooleanSetting.Option option = new MultiBooleanSetting.Option(this.setting, string);
        ScriptDescriptor.registerMultiBooleanOption(this.setting, option);
        return this;
    }

    public boolean has(String string) {
        if (string == null) {
            return false;
        }
        for (MultiBooleanSetting.Option option : this.setting.getOptions()) {
            if (!string.equalsIgnoreCase(option.getName()) && !string.equalsIgnoreCase(Localization.translate(option.getName()))) continue;
            return true;
        }
        return false;
    }

    public List<String> getValueLabels() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (MultiBooleanSetting.Option option : this.setting.getOptions()) {
            arrayList.add(Localization.translate(option.getName()));
        }
        return arrayList;
    }

    public PySelectSetting select(String string) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        for (MultiBooleanSetting.Option option : this.setting.getOptions()) {
            if (!option.getName().equals(string)) continue;
            this.setting.selectOption(option);
            break;
        }
        return this;
    }

    public PySelectSetting min(int n) {
        this.setting.setMaxSelections(n);
        return this;
    }

    public PySelectSetting draggable() {
        this.setting.preserveOrder();
        return this;
    }

    public boolean isSelected(String string) {
        for (MultiBooleanSetting.Option option : this.setting.getSelectedOptions()) {
            if (!option.getName().equals(string)) continue;
            return true;
        }
        return false;
    }

    public List<String> getSelected() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (MultiBooleanSetting.Option option : this.setting.getSelectedOptions()) {
            arrayList.add(option.getName());
        }
        return arrayList;
    }

    public List<String> getValues() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (MultiBooleanSetting.Option option : this.setting.getOptions()) {
            arrayList.add(option.getName());
        }
        return arrayList;
    }

    @Generated
    public MultiBooleanSetting getSetting() {
        return this.setting;
    }
}

