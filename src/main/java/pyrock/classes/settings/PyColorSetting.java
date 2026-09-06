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
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.SettingOwner;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;
import pyrock.utility.render.ColorRGBA;

public class PyColorSetting {
    private final ColorSetting setting;

    public PyColorSetting(PyModule pyModule, String string) {
        this.setting = new ColorSetting(pyModule.getModule(), string);
        this.setting.setColor(new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f));
        this.setting.setAlphaEnabled(true);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyColorSetting(PyHudElement pyHudElement, String string) {
        this.setting = new ColorSetting((SettingOwner)((Object)pyHudElement), string);
        this.setting.setColor(new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f));
        this.setting.setAlphaEnabled(true);
    }

    public PyColorSetting(PyEspElement pyEspElement, String string) {
        this.setting = new ColorSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
        this.setting.setColor(new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f));
        this.setting.setAlphaEnabled(true);
    }

    public PyColorSetting(ColorSetting colorSetting) {
        this.setting = colorSetting;
    }

    public PyColorSetting color(ColorRGBA colorRGBA) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setColor(colorRGBA);
        return this;
    }

    public PyColorSetting color(int n, int n2, int n3, int n4) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setColor(new ColorRGBA(n, n2, n3, n4));
        return this;
    }

    public PyColorSetting color(int n, int n2, int n3) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setColor(new ColorRGBA(n, n2, n3, 255.0f));
        return this;
    }

    public PyColorSetting alpha(boolean bl) {
        this.setting.setAlphaEnabled(bl);
        return this;
    }

    public ColorRGBA get() {
        return this.setting.getColor();
    }

    public boolean hasAlpha() {
        return this.setting.isAlphaEnabled();
    }

    @Generated
    public ColorSetting getSetting() {
        return this.setting;
    }
}

