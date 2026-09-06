/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.StatusEffects
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Camera
 *  net.minecraft.CameraSubmersionType
 */
package moscow.rockstar.modules.visuals.world;

import lombok.Generated;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.RangeSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.block.enums.CameraSubmersionType;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Custom Fog", category=ModuleCategory.VISUALS, disableLocked=true)
public class CustomFog
extends Module {
    private RangeSetting distanceSetting;
    private BooleanSetting syncWithThemeSetting;
    private NumberSetting synchronizedAlphaSetting;
    private ColorSetting fogColorSetting;

    public CustomFog() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.distanceSetting = new RangeSetting(this, "modules.settings.custom_fog.distance").setMinimum(1.0f).setMaximum(100.0f).setStep(1.0f).setDefaultMinimum(40.0f).setFirstValue(1.0f).setSecondValue(100.0f);
        this.syncWithThemeSetting = new BooleanSetting(this, "theme.sync").enable();
        this.synchronizedAlphaSetting = new NumberSetting((SettingOwner)this, "modules.settings.custom_fog.sync_alpha", () -> !this.syncWithThemeSetting.isEnabled()).setUnit("%").setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f).setValue(46.0f);
        this.fogColorSetting = new ColorSetting(this, "modules.settings.custom_fog.color", this.syncWithThemeSetting::isEnabled).setColor(ColorPalette.getAccentColor().withAlpha(118.0f)).setAlphaEnabled(true);
    }

    public boolean shouldApplyFog(Camera class_41842) {
        if (!this.isEnabled() || CustomFog.minecraftClient.world == null || CustomFog.minecraftClient.player == null) {
            return false;
        }
        Entity class_12972 = class_41842.getFocusedEntity();
        if (class_41842.getSubmersionType() == CameraSubmersionType.WATER) {
            return false;
        }
        if (class_41842.getSubmersionType() == CameraSubmersionType.LAVA) {
            return false;
        }
        if (class_41842.getSubmersionType() == CameraSubmersionType.POWDER_SNOW) {
            return false;
        }
        if (class_12972 instanceof LivingEntity) {
            LivingEntity class_13092 = (LivingEntity)class_12972;
            if (class_13092.hasStatusEffect(StatusEffects.BLINDNESS)) {
                return false;
            }
            if (class_13092.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                return false;
            }
        }
        return true;
    }

    @Generated
    public RangeSetting getDistanceSetting() {
        return this.distanceSetting;
    }

    @Generated
    public BooleanSetting getSyncWithThemeSetting() {
        return this.syncWithThemeSetting;
    }

    @Generated
    public NumberSetting getSynchronizedAlphaSetting() {
        return this.synchronizedAlphaSetting;
    }

    @Generated
    public ColorSetting getFogColorSetting() {
        return this.fogColorSetting;
    }
}

