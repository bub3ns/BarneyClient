/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.PickaxeItem
 *  net.minecraft.PlayerInteractItemC2SPacket
 */
package moscow.rockstar.modules.player.mining;

import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationType;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import pyrock.events.game.StartBreakBlockEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.window.KeyPressEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Mine Helper", category=ModuleCategory.PLAYER, description="modules.descriptions.mine_helper")
public class MineHelper
extends Module {
    private BooleanSetting savePickaxe;
    public NumberSetting percent;
    private BooleanSetting autoReplace;
    private BooleanSetting autoRepair;
    private IntegerSetting fixKey;
    private final Timer cooldownTimer = new Timer();
    private boolean repairing;
    private boolean overlayVisible = false;
    private final EventListener<KeyPressEvent> onKeyPressEvent = keyPressEvent -> {
        if (this.fixKey.isIntValid(keyPressEvent.getKey()) && keyPressEvent.getAction() == 1) {
            this.repairing = true;
        }
    };
    private final EventListener<StartBreakBlockEvent> onStartBreakBlockEvent = startBreakBlockEvent -> {
        if (MineHelper.minecraftClient.player == null) {
            return;
        }
        ItemStack class_17992 = MineHelper.minecraftClient.player.getMainHandStack();
        if (!this.isRepairItem(class_17992)) {
            return;
        }
        double d = this.calculateRepairProgress(class_17992);
        if (!this.savePickaxe.isEnabled() || d >= (double)this.percent.getValue()) {
            return;
        }
        startBreakBlockEvent.cancel();
        this.repairItem(class_17992);
    };
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        if (MineHelper.minecraftClient.player == null || !this.repairing) {
            return;
        }
        ItemStack class_17992 = MineHelper.minecraftClient.player.getMainHandStack();
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        if (this.calculateRepairProgress(class_17992) >= 30.0) {
            this.repairing = false;
            return;
        }
        if (this.isRepairItem(class_17992) && this.isRepairReady()) {
            rotationManager.requestRotation(new Rotation(MineHelper.minecraftClient.player.getYaw(), 88.0f), RotationCorrectionMode.UNSPECIFIED, 180.0f, 80.0f, 80.0f, RotationPriority.ITEM_USE_PRIORITY);
            if (this.cooldownTimer.hasElapsed(70L)) {
                ((moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)MineHelper.minecraftClient.interactionManager).rockstar$sendSequencedPacket(MineHelper.minecraftClient.world, n -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, n, rotationManager.getPacketRotation().getYaw(), 90.0f));
                this.cooldownTimer.reset();
            }
        }
    };

    public MineHelper() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.savePickaxe = new BooleanSetting((SettingOwner)this, "modules.settings.mine_helper.save_pickaxe", "modules.settings.mine_helper.save_pickaxe.description").enable();
        this.percent = new NumberSetting(this, "modules.settings.mine_helper.percent").setStep(1.0f).setMinValue(1.0f).setMaxValue(70.0f).setValue(10.0f).setUnit("%");
        this.autoReplace = new BooleanSetting((SettingOwner)this, "modules.settings.mine_helper.auto_replace", "modules.settings.mine_helper.auto_replace.description");
        this.autoRepair = new BooleanSetting((SettingOwner)this, "modules.settings.mine_helper.auto_repair", "modules.settings.mine_helper.auto_repair.description");
        this.fixKey = new IntegerSetting(this, "modules.settings.mine_helper.fix_key", () -> !this.autoRepair.isEnabled());
    }

    @Compile
    private void repairItem(ItemStack class_17992) {
        boolean bl = false;
        if (this.autoReplace.isEnabled()) {
            bl = this.isItemValid(class_17992);
        }
        if (!bl && this.cooldownTimer.hasElapsed(800L)) {
            RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.ERROR, Localization.translate("mine_helper.pickaxe_almost_broken"), Localization.translate("mine_helper.no_replacement"));
            this.cooldownTimer.reset();
        }
    }

    private boolean isRepairReady() {
        if (MineHelper.minecraftClient.player.getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE) {
            this.overlayVisible = false;
            return true;
        }
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules()).combineRules(ItemRuleSets.getOffhandRules());
        ItemRule itemRule = itemRuleCollection.findByStack(class_17992 -> class_17992.getItem() == Items.EXPERIENCE_BOTTLE);
        if (itemRule == null) {
            if (!this.overlayVisible) {
                RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.ERROR, Localization.translate("mine_helper.no_bottles"), Localization.translate("mine_helper.need_bottles"));
                this.overlayVisible = true;
            }
            return false;
        }
        InventoryUtils.swapItemRules(itemRule, InventoryUtils.offhandRule());
        return true;
    }

    @Compile
    private boolean isItemValid(ItemStack class_17992) {
        HotbarSlot hotbarSlot = this.findRepairSlot(class_17992);
        if (hotbarSlot == null) {
            return false;
        }
        InventoryUtils.setSelectedHotbarSlot(hotbarSlot);
        if (this.cooldownTimer.hasElapsed(800L)) {
            ItemStack class_17993 = hotbarSlot.getItemStack();
            RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.SUCCESS, Localization.translate("mine_helper.pickaxe_swap"), Localization.translateFormatted("mine_helper.pickaxe_swapped", this.calculateRepairProgress(class_17992), this.calculateRepairProgress(class_17993)));
            this.cooldownTimer.reset();
        }
        return true;
    }

    @Compile
    private HotbarSlot findRepairSlot(ItemStack class_17992) {
        double d = this.calculateRepairProgress(class_17992);
        HotbarSlot hotbarSlot = null;
        double d2 = d;
        for (int i = 0; i < 9; ++i) {
            double d3;
            HotbarSlot hotbarSlot2 = InventoryUtils.hotbarSlot(i);
            ItemStack class_17993 = hotbarSlot2.getItemStack();
            if (!this.isRepairItem(class_17993) || !((d3 = this.calculateRepairProgress(class_17993)) > d2)) continue;
            d2 = d3;
            hotbarSlot = hotbarSlot2;
        }
        return hotbarSlot;
    }

    private boolean isRepairItem(ItemStack class_17992) {
        return class_17992 != null && class_17992.isDamageable() && class_17992.getItem() instanceof PickaxeItem;
    }

    private double calculateRepairProgress(ItemStack class_17992) {
        return (double)(class_17992.getMaxDamage() - class_17992.getDamage()) / (double)class_17992.getMaxDamage() * 100.0;
    }

    @Override
    public void onDisable() {
        this.repairing = false;
        this.overlayVisible = false;
    }
}
