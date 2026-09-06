/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 *  lombok.Generated
 */
package pyrock.classes.settings;

import jep.python.PyCallable;
import lombok.Generated;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.api.scripts.ScriptModule;
import moscow.rockstar.settings.ActionSetting;
import moscow.rockstar.settings.SettingOwner;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PyButtonSetting {
    private final ActionSetting setting;

    public PyButtonSetting(PyModule pyModule, String string) {
        this.setting = new ActionSetting(pyModule.getModule(), string);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyButtonSetting(PyHudElement pyHudElement, String string) {
        this.setting = new ActionSetting((SettingOwner)((Object)pyHudElement), string);
    }

    public PyButtonSetting(PyEspElement pyEspElement, String string) {
        this.setting = new ActionSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
    }

    public PyButtonSetting(ActionSetting actionSetting) {
        this.setting = actionSetting;
    }

    public PyButtonSetting action(PyCallable pyCallable) {
        this.setting.withAction(() -> {
            try {
                pyCallable.call(new Object[0]);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        return this;
    }

    public void click() {
        this.setting.getAction().run();
    }

    @Generated
    public ActionSetting getSetting() {
        return this.setting;
    }
}

