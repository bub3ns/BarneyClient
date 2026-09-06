/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 *  net.minecraft.Entity
 */
package pyrock.classes.aura;

import java.util.ArrayList;
import jep.python.PyCallable;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.settings.ModeSetting;
import net.minecraft.entity.Entity;
import pyrock.classes.aura.PyRotationMode;

public class PyAura {
    private Aura aura() {
        return RockstarClient.create().getModuleRegistry().getModule(Aura.class);
    }

    private ModeSetting rotationMode() {
        return this.aura().getSortingSetting();
    }

    public PyRotationMode addRotation(String string, PyCallable pyCallable, PyCallable pyCallable2, PyCallable pyCallable3, PyCallable pyCallable4, PyCallable pyCallable5) {
        ModeSetting modeSetting = this.rotationMode();
        ScriptDescriptor.rememberSettingValue(modeSetting);
        PyRotationMode pyRotationMode = new PyRotationMode(modeSetting, string, pyCallable, pyCallable2, pyCallable3, pyCallable4, pyCallable5);
        ScriptDescriptor.registerModeOption(modeSetting, pyRotationMode);
        return pyRotationMode;
    }

    public void selectRotation(String string) {
        ModeSetting modeSetting = this.rotationMode();
        for (ModeSetting.Option option : modeSetting.getOptions()) {
            if (!option.getName().equals(string)) continue;
            modeSetting.select(option);
            return;
        }
    }

    public void removeRotation(String string) {
        ModeSetting modeSetting = this.rotationMode();
        ArrayList<PyRotationMode> arrayList = new ArrayList<PyRotationMode>();
        for (ModeSetting.Option option : modeSetting.getOptions()) {
            if (!(option instanceof PyRotationMode)) continue;
            PyRotationMode pyRotationMode = (PyRotationMode)option;
            if (!option.getName().equals(string) || !ScriptDescriptor.isModeOptionRegistered(modeSetting, option)) continue;
            arrayList.add(pyRotationMode);
        }
        for (PyRotationMode pyRotationMode : arrayList) {
            this.removeRotation(pyRotationMode);
        }
    }

    public void removeRotation(PyRotationMode pyRotationMode) {
        if (pyRotationMode == null) {
            return;
        }
        ModeSetting modeSetting = this.rotationMode();
        if (!ScriptDescriptor.isModeOptionRegistered(modeSetting, pyRotationMode)) {
            return;
        }
        boolean bl = modeSetting.getSelectedOption() == pyRotationMode;
        modeSetting.getOptions().remove(pyRotationMode);
        ScriptDescriptor.unregisterModeOption(modeSetting, pyRotationMode);
        if (bl) {
            modeSetting.select(modeSetting.getOptions().isEmpty() ? null : modeSetting.getOptions().getFirst());
        }
    }

    public boolean hasRotation(String string) {
        for (ModeSetting.Option option : this.rotationMode().getOptions()) {
            if (!option.getName().equals(string)) continue;
            return true;
        }
        return false;
    }

    public String currentRotation() {
        ModeSetting.Option option = this.rotationMode().getSelectedOption();
        return option == null ? null : option.getName();
    }

    public boolean isEnabled() {
        return this.aura().isEnabled();
    }

    public void setEnabled(boolean bl) {
        this.aura().setEnabled(bl, false);
    }

    public Entity target() {
        return RockstarClient.create().getFriendManager().getTargetEntity();
    }

    public float attackDistance() {
        return this.aura().getAttackDistanceSetting().getValue();
    }
}

