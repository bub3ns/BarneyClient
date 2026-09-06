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
import moscow.rockstar.settings.TextLabelSetting;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PyInfoSetting {
    private final TextLabelSetting setting;

    public PyInfoSetting(PyModule pyModule, String string) {
        this.setting = new TextLabelSetting(pyModule.getModule(), string);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyInfoSetting(PyHudElement pyHudElement, String string) {
        this.setting = new TextLabelSetting((SettingOwner)((Object)pyHudElement), string);
    }

    public PyInfoSetting(PyEspElement pyEspElement, String string) {
        this.setting = new TextLabelSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
    }

    public PyInfoSetting(TextLabelSetting textLabelSetting) {
        this.setting = textLabelSetting;
    }

    public PyInfoSetting level(int n) {
        this.setting.setFontSizeOffset(n);
        return this;
    }

    public PyInfoSetting splitted() {
        this.setting.asMultiline();
        return this;
    }

    public PyInfoSetting centered() {
        this.setting.asCentered();
        return this;
    }

    @Generated
    public TextLabelSetting getSetting() {
        return this.setting;
    }
}

