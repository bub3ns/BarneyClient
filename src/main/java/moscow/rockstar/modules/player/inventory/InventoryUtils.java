/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.modules.player.inventory;

import lombok.Generated;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Inventory Utils", category=ModuleCategory.PLAYER)
public class InventoryUtils
extends Module {
    private MultiBooleanSetting targetSettings;
    private MultiBooleanSetting.Option itemScrollerOption;
    private MultiBooleanSetting.Option slotLockOption;
    private MultiBooleanSetting lockedSlotsSetting;
    private MultiBooleanSetting.Option lockedSlot1Option;
    private MultiBooleanSetting.Option lockedSlot2Option;
    private MultiBooleanSetting.Option lockedSlot3Option;
    private MultiBooleanSetting.Option lockedSlot4Option;
    private MultiBooleanSetting.Option lockedSlot5Option;
    private MultiBooleanSetting.Option lockedSlot6Option;
    private MultiBooleanSetting.Option lockedSlot7Option;
    private MultiBooleanSetting.Option lockedSlot8Option;
    private MultiBooleanSetting.Option lockedSlot9Option;
    private BooleanSetting onlyPvPSetting;
    private NumberSetting scrollDelaySetting;
    private final Timer scrollTimer = new Timer();
    private final Timer slotLockTimer = new Timer();
    private float scrollAmount = -1.0f;
    private boolean utilityActive;

    public InventoryUtils() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.targetSettings = new MultiBooleanSetting(this, "modules.settings.inv_utils.targets").setMaxSelections(1);
        this.itemScrollerOption = new MultiBooleanSetting.Option(this.targetSettings, "modules.settings.inv_utils.item_scroller").select();
        this.slotLockOption = new MultiBooleanSetting.Option(this.targetSettings, "modules.settings.inv_utils.slot_lock").select();
        this.lockedSlotsSetting = new MultiBooleanSetting((SettingOwner)this, "modules.settings.slot_lock.lock", () -> !this.slotLockOption.isSelected()).setMaxSelections(1);
        this.lockedSlot1Option = new MultiBooleanSetting.Option(this.lockedSlotsSetting, "modules.settings.slot_lock.lock.slot1").select();
        this.lockedSlot2Option = new MultiBooleanSetting.Option(this.lockedSlotsSetting, "modules.settings.slot_lock.lock.slot2");
        this.lockedSlot3Option = new MultiBooleanSetting.Option(this.lockedSlotsSetting, "modules.settings.slot_lock.lock.slot3");
        this.lockedSlot4Option = new MultiBooleanSetting.Option(this.lockedSlotsSetting, "modules.settings.slot_lock.lock.slot4");
        this.lockedSlot5Option = new MultiBooleanSetting.Option(this.lockedSlotsSetting, "modules.settings.slot_lock.lock.slot5");
        this.lockedSlot6Option = new MultiBooleanSetting.Option(this.lockedSlotsSetting, "modules.settings.slot_lock.lock.slot6");
        this.lockedSlot7Option = new MultiBooleanSetting.Option(this.lockedSlotsSetting, "modules.settings.slot_lock.lock.slot7");
        this.lockedSlot8Option = new MultiBooleanSetting.Option(this.lockedSlotsSetting, "modules.settings.slot_lock.lock.slot8");
        this.lockedSlot9Option = new MultiBooleanSetting.Option(this.lockedSlotsSetting, "modules.settings.slot_lock.lock.slot9");
        this.onlyPvPSetting = new BooleanSetting(this, "modules.settings.slot_lock.only_pvp", "modules.settings.slot_lock.only_pvp.desc", () -> !this.slotLockOption.isSelected()).setActiveExtra(false);
        this.scrollDelaySetting = new NumberSetting((SettingOwner)this, "modules.settings.inv_utils.delay", () -> !this.itemScrollerOption.isSelected()).setValue(0.0f).setMaxValue(250.0f).setMinValue(0.0f).setStep(1.0f).setUnit(" ms");
    }

    public boolean isSlotActionAllowed(int n) {
        MultiBooleanSetting.Option[] optionArray = new MultiBooleanSetting.Option[]{this.lockedSlot1Option, this.lockedSlot2Option, this.lockedSlot3Option, this.lockedSlot4Option, this.lockedSlot5Option, this.lockedSlot6Option, this.lockedSlot7Option, this.lockedSlot8Option, this.lockedSlot9Option};
        if (this.onlyPvPSetting.isEnabled() && !ServerDetector.enabled) {
            return false;
        }
        return n >= 0 && n < optionArray.length && optionArray[n].isSelected() && this.isEnabled();
    }

    @Generated
    public MultiBooleanSetting getTargetSettings() {
        return this.targetSettings;
    }

    @Generated
    public MultiBooleanSetting.Option getItemScrollerOption() {
        return this.itemScrollerOption;
    }

    @Generated
    public MultiBooleanSetting.Option getSlotLockOption() {
        return this.slotLockOption;
    }

    @Generated
    public MultiBooleanSetting getLockedSlotsSetting() {
        return this.lockedSlotsSetting;
    }

    @Generated
    public MultiBooleanSetting.Option getLockedSlot1Option() {
        return this.lockedSlot1Option;
    }

    @Generated
    public MultiBooleanSetting.Option getLockedSlot2Option() {
        return this.lockedSlot2Option;
    }

    @Generated
    public MultiBooleanSetting.Option getLockedSlot3Option() {
        return this.lockedSlot3Option;
    }

    @Generated
    public MultiBooleanSetting.Option getLockedSlot4Option() {
        return this.lockedSlot4Option;
    }

    @Generated
    public MultiBooleanSetting.Option getLockedSlot5Option() {
        return this.lockedSlot5Option;
    }

    @Generated
    public MultiBooleanSetting.Option getLockedSlot6Option() {
        return this.lockedSlot6Option;
    }

    @Generated
    public MultiBooleanSetting.Option getLockedSlot7Option() {
        return this.lockedSlot7Option;
    }

    @Generated
    public MultiBooleanSetting.Option getLockedSlot8Option() {
        return this.lockedSlot8Option;
    }

    @Generated
    public MultiBooleanSetting.Option getLockedSlot9Option() {
        return this.lockedSlot9Option;
    }

    @Generated
    public BooleanSetting getOnlyPvPSetting() {
        return this.onlyPvPSetting;
    }

    @Generated
    public NumberSetting getScrollDelaySetting() {
        return this.scrollDelaySetting;
    }

    @Generated
    public Timer getScrollTimer() {
        return this.scrollTimer;
    }

    @Generated
    public Timer getSlotLockTimer() {
        return this.slotLockTimer;
    }

    @Generated
    public float getScrollAmount() {
        return this.scrollAmount;
    }

    @Generated
    public boolean isUtilityActive() {
        return this.utilityActive;
    }
}

