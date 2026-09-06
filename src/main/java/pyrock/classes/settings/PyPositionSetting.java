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
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.Vector2Setting;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PyPositionSetting {
    private final Vector2Setting setting;

    public PyPositionSetting(PyModule pyModule, String string) {
        this.setting = new Vector2Setting(pyModule.getModule(), string);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyPositionSetting(PyHudElement pyHudElement, String string) {
        this.setting = new Vector2Setting((SettingOwner)((Object)pyHudElement), string);
    }

    public PyPositionSetting(PyEspElement pyEspElement, String string) {
        this.setting = new Vector2Setting((SettingOwner)((Object)pyEspElement.getElement()), string);
    }

    public PyPositionSetting(Vector2Setting vector2Setting) {
        this.setting = vector2Setting;
    }

    public PyPositionSetting set(double d, double d2) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setValue((float)d, (float)d2);
        return this;
    }

    public PyPositionSetting x(double d) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setX((float)d);
        return this;
    }

    public PyPositionSetting y(double d) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setY((float)d);
        return this;
    }

    public float getX() {
        return this.setting.getX();
    }

    public float getY() {
        return this.setting.getY();
    }

    public List<Float> get() {
        return List.of(Float.valueOf(this.setting.getX()), Float.valueOf(this.setting.getY()));
    }

    public PyPositionSetting bounds(double d, double d2, double d3, double d4) {
        this.setting.setMinX((float)d).setMinY((float)d2).setMaxX((float)d3).setMaxY((float)d4);
        return this;
    }

    public float getMinX() {
        return this.setting.getMinX();
    }

    public float getMaxX() {
        return this.setting.getMaxX();
    }

    public float getMinY() {
        return this.setting.getMinY();
    }

    public float getMaxY() {
        return this.setting.getMaxY();
    }

    @Generated
    public Vector2Setting getSetting() {
        return this.setting;
    }
}

