/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Item
 *  net.minecraft.Items
 */
package moscow.rockstar.modules.player.movement;

import lombok.Generated;
import moscow.rockstar.mixin.minecraft.client.IMinecraftClient;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="No Delay", category=ModuleCategory.PLAYER, description="modules.descriptions.no_delay")
public class NoDelay
extends Module {
    private BooleanSetting jump;
    private BooleanSetting rightClick;
    private NumberSetting rightClickDelay;
    private BooleanSetting exp;
    private BooleanSetting potions;

    public NoDelay() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.jump = new BooleanSetting((SettingOwner)this, "modules.settings.no_delay.jump", "modules.settings.no_delay.jump.description").enable();
        this.rightClick = new BooleanSetting((SettingOwner)this, "modules.settings.no_delay.right_click", "modules.settings.no_delay.right_click.description");
        this.rightClickDelay = new NumberSetting((SettingOwner)this, "modules.settings.no_delay.right_click_delay", () -> !this.rightClick.isEnabled()).setMinValue(1.0f).setMaxValue(100.0f).setStep(1.0f).setValue(1.0f).setUnit(" ms");
        this.exp = new BooleanSetting((SettingOwner)this, "modules.settings.no_delay.exp", this.rightClick::isEnabled);
        this.potions = new BooleanSetting((SettingOwner)this, "modules.settings.no_delay.potions", this.rightClick::isEnabled);
    }

    @Override
    public void onTick() {
        if (this.rightClick.isEnabled()) {
            IMinecraftClient iMinecraftClient = (IMinecraftClient)((Object)minecraftClient);
            int n = this.getUseDelay();
            if (iMinecraftClient.getUseCooldown() > n) {
                iMinecraftClient.setUseCooldown(n);
            }
        }
        if (this.exp.isEnabled() && (NoDelay.minecraftClient.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE || NoDelay.minecraftClient.player.getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE)) {
            ((IMinecraftClient)((Object)minecraftClient)).setUseCooldown(0);
        }
        if (this.potions.isEnabled() && (this.isDelayBypassEnabled(NoDelay.minecraftClient.player.getMainHandStack().getItem()) || this.isDelayBypassEnabled(NoDelay.minecraftClient.player.getOffHandStack().getItem()))) {
            ((IMinecraftClient)((Object)minecraftClient)).setUseCooldown(0);
        }
        super.onTick();
    }

    private boolean isDelayBypassEnabled(Item class_17922) {
        return class_17922 == Items.POTION || class_17922 == Items.GLASS_BOTTLE;
    }

    public int getUseDelay() {
        return Math.max(1, (int)Math.ceil(this.rightClickDelay.getValue() / 50.0f));
    }

    @Generated
    public BooleanSetting getJumpSetting() {
        return this.jump;
    }

    @Generated
    public BooleanSetting getRightClickSetting() {
        return this.rightClick;
    }

    @Generated
    public NumberSetting getRightClickDelaySetting() {
        return this.rightClickDelay;
    }

    @Generated
    public BooleanSetting getExperienceSetting() {
        return this.exp;
    }

    @Generated
    public BooleanSetting getPotionsSetting() {
        return this.potions;
    }
}

