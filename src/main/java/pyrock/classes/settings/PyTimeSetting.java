/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.classes.settings;

import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.api.scripts.ScriptModule;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.TimeSetting;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PyTimeSetting {
    private final TimeSetting setting;

    public PyTimeSetting(PyModule pyModule, String string) {
        this.setting = new TimeSetting((SettingOwner)pyModule.getModule(), string);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyTimeSetting(PyHudElement pyHudElement, String string) {
        this.setting = new TimeSetting(pyHudElement, string);
    }

    public PyTimeSetting(PyEspElement pyEspElement, String string) {
        this.setting = new TimeSetting((SettingOwner)pyEspElement.getElement(), string);
    }

    public PyTimeSetting(TimeSetting timeSetting) {
        this.setting = timeSetting;
    }

    public PyTimeSetting set(int n) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setSeconds(n);
        return this;
    }

    public int get() {
        return this.setting.getTotalSeconds();
    }

    public long millis() {
        return this.setting.getMilliseconds();
    }

    public int ticks() {
        return this.setting.getTicks();
    }

    public String formatted() {
        return this.setting.getFormattedValue();
    }

    public int hours() {
        return this.setting.getHours();
    }

    public int minutes() {
        return this.setting.getMinutes();
    }

    public int seconds() {
        return this.setting.getSeconds();
    }

    public PyTimeSetting units(String string) {
        this.setting.setDisplayUnits(TimeSetting.parseUnit(string));
        return this;
    }

    public PyTimeSetting units(String string, String string2) {
        this.setting.setDisplayUnits(TimeSetting.parseUnit(string), TimeSetting.parseUnit(string2));
        return this;
    }

    public PyTimeSetting units(String string, String string2, String string3) {
        this.setting.setDisplayUnits(TimeSetting.parseUnit(string), TimeSetting.parseUnit(string2), TimeSetting.parseUnit(string3));
        return this;
    }

    public PyTimeSetting unit(String string, boolean bl) {
        this.setting.setUnitEnabled(TimeSetting.parseUnit(string), bl);
        return this;
    }

    public PyTimeSetting maxHours(int n) {
        this.setting.setMaximumHours(n);
        return this;
    }

    public PyTimeSetting maxMinutes(int n) {
        this.setting.setMaximumMinutes(n);
        return this;
    }

    public TimeSetting getSetting() {
        return this.setting;
    }
}
