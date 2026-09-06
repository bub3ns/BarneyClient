/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Vec2f
 */
package pyrock.classes.settings;

import java.util.List;
import lombok.Generated;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.api.scripts.ScriptModule;
import moscow.rockstar.settings.EasingSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Easing;
import net.minecraft.util.math.Vec2f;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PyBezierSetting {
    private final EasingSetting setting;

    public PyBezierSetting(PyModule pyModule, String string) {
        this.setting = new EasingSetting(pyModule.getModule(), string);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyBezierSetting(PyHudElement pyHudElement, String string) {
        this.setting = new EasingSetting((SettingOwner)((Object)pyHudElement), string);
    }

    public PyBezierSetting(PyEspElement pyEspElement, String string) {
        this.setting = new EasingSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
    }

    public PyBezierSetting(EasingSetting easingSetting) {
        this.setting = easingSetting;
    }

    public PyBezierSetting start(double d, double d2) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setStartControlPoint((float)d, (float)d2);
        return this;
    }

    public PyBezierSetting end(double d, double d2) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.setEndControlPoint((float)d, (float)d2);
        return this;
    }

    public List<Float> getStart() {
        Vec2f VanillaAdventureTabAdvancementGenerator = this.setting.getStartControlPoint();
        return List.of(Float.valueOf(VanillaAdventureTabAdvancementGenerator.x), Float.valueOf(VanillaAdventureTabAdvancementGenerator.y));
    }

    public List<Float> getEnd() {
        Vec2f VanillaAdventureTabAdvancementGenerator = this.setting.getEndControlPoint();
        return List.of(Float.valueOf(VanillaAdventureTabAdvancementGenerator.x), Float.valueOf(VanillaAdventureTabAdvancementGenerator.y));
    }

    public float ease(double d) {
        float f = (float)Math.max(0.0, Math.min(1.0, d));
        Easing easing = this.setting.getEasing();
        return easing == null ? f : easing.ease(f, 0.0f, 1.0f, 1.0f);
    }

    public float interpolate(double d, double d2, double d3) {
        return (float)(d + (d2 - d) * (double)this.ease(d3));
    }

    @Generated
    public EasingSetting getSetting() {
        return this.setting;
    }
}

