/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.EntityRenderState
 *  net.minecraft.Entity
 */
package moscow.rockstar.modules.visuals.esp.entities;

import lombok.Generated;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.api.access.EntityAccess;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Anti Invisible", category=ModuleCategory.VISUALS, disableLocked=true)
public class AntiInvisible
extends Module {
    private NumberSetting opacitySetting;

    public AntiInvisible() {
        this.initializeOpacitySetting();
    }

    @Compile(obfuscation=4)
    private void initializeOpacitySetting() {
        this.opacitySetting = new NumberSetting(this, "modules.settings.anti_invisible.opacity").setMinValue(10.0f).setMaxValue(100.0f).setStep(1.0f).setValue(70.0f).setFormatter(f -> "%");
    }

    public boolean isEntityInvisible(EntityRenderState class_100172) {
        Entity class_12972 = ((EntityAccess)class_100172).rockstar$getEntity();
        return class_12972.isInvisible();
    }

    @Generated
    public NumberSetting getOpacitySetting() {
        return this.opacitySetting;
    }
}
