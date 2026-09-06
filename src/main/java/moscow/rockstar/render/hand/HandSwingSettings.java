/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.MathHelper
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.render.hand;

import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingGroup;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.screens.ModuleSettingsScreen;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class HandSwingSettings
extends SettingGroup {
    private final NumberSetting anchorX = new MirroredNumberSetting(this, "swing.anchorX").setStep(0.05f).setMinValue(-5.0f).setMaxValue(5.0f).setValue(0.0f);
    private final NumberSetting anchorY = new MirroredNumberSetting(this, "swing.anchorY").setStep(0.05f).setMinValue(-5.0f).setMaxValue(5.0f).setValue(0.0f);
    private final NumberSetting anchorZ = new MirroredNumberSetting(this, "swing.anchorZ").setStep(0.05f).setMinValue(-5.0f).setMaxValue(5.0f).setValue(0.0f);
    private final NumberSetting moveX = new MirroredNumberSetting(this, "swing.moveX").setStep(0.05f).setMinValue(-5.0f).setMaxValue(5.0f).setValue(0.0f);
    private final NumberSetting moveY = new MirroredNumberSetting(this, "swing.moveY").setStep(0.05f).setMinValue(-5.0f).setMaxValue(5.0f).setValue(0.0f);
    private final NumberSetting moveZ = new MirroredNumberSetting(this, "swing.moveZ").setStep(0.05f).setMinValue(-3.0f).setMaxValue(3.0f).setValue(0.0f);
    private final NumberSetting rotateX = new MirroredNumberSetting(this, "swing.rotateX").setStep(15.0f).setMinValue(-360.0f).setMaxValue(360.0f).setValue(0.0f);
    private final NumberSetting rotateY = new MirroredNumberSetting(this, "swing.rotateY").setStep(15.0f).setMinValue(-360.0f).setMaxValue(360.0f).setValue(0.0f);
    private final NumberSetting rotateZ = new MirroredNumberSetting(this, "swing.rotateZ").setStep(15.0f).setMinValue(-360.0f).setMaxValue(360.0f).setValue(0.0f);

    @Generated
    public NumberSetting getAnchorXSetting() {
        return this.anchorX;
    }

    @Generated
    public NumberSetting getAnchorYSetting() {
        return this.anchorY;
    }

    @Generated
    public NumberSetting getAnchorZSetting() {
        return this.anchorZ;
    }

    @Generated
    public NumberSetting getMoveXSetting() {
        return this.moveX;
    }

    @Generated
    public NumberSetting getMoveYSetting() {
        return this.moveY;
    }

    @Generated
    public NumberSetting getMoveZSetting() {
        return this.moveZ;
    }

    @Generated
    public NumberSetting getRotateXSetting() {
        return this.rotateX;
    }

    @Generated
    public NumberSetting getRotateYSetting() {
        return this.rotateY;
    }

    @Generated
    public NumberSetting getRotateZSetting() {
        return this.rotateZ;
    }

    public static class MirroredNumberSetting
    extends NumberSetting {
        public MirroredNumberSetting(@NotNull SettingOwner settingOwner, String string) {
            super(settingOwner, string);
        }

        @Override
        public void updateValue(float f) {
            super.updateValue(f);
            if (ModuleSettingsScreen.hasShiftDown()) {
                MirroredNumberSetting mirroredNumberSetting;
                for (Setting setting : RockstarClient.create().getHandSwingPresetManager().getInitialSwing().getSettings()) {
                    if (!setting.getName().equals(this.getName()) || !(setting instanceof MirroredNumberSetting)) continue;
                    mirroredNumberSetting = (MirroredNumberSetting)setting;
                    mirroredNumberSetting.setClampedValue(f);
                }
                for (Setting setting : RockstarClient.create().getHandSwingPresetManager().getFinalSwing().getSettings()) {
                    if (!setting.getName().equals(this.getName()) || !(setting instanceof MirroredNumberSetting)) continue;
                    mirroredNumberSetting = (MirroredNumberSetting)setting;
                    mirroredNumberSetting.setClampedValue(f);
                }
            }
        }

        private void setClampedValue(float f) {
            this.value = MathHelper.clamp((float)((float)((double)Math.round((double)f * (1.0 / (double)this.step)) / (1.0 / (double)this.step))), (float)this.minValue, (float)this.maxValue);
        }
    }
}

