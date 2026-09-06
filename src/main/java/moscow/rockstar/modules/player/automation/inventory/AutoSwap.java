/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.SwordItem
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.modules.player.automation.inventory;

import java.util.Comparator;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.items.donor.DonorItemSelector;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.defense.AutoTotem;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.ItemNotification;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import org.jetbrains.annotations.Nullable;
import pyrock.events.window.KeyPressEvent;
import pyrock.events.window.MouseEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Swap", category=ModuleCategory.PLAYER)
public class AutoSwap
extends Module {
    private IntegerSetting button;
    private ModeSetting itemMode;
    private ModeSetting.Option talismanOption;
    private ModeSetting swapToMode;
    private ModeSetting.Option swapToTalismanOption;
    private ModeSetting serverMode;
    private ModeSetting.Option hwOption;
    private ModeSetting.Option vontamOption;
    private IntegerSetting autoCerber;
    private IntegerSetting autoPunisher;
    private MultiBooleanSetting takeCerberus;
    private MultiBooleanSetting.Option cerberusAlways;
    private MultiBooleanSetting.Option cerberusWhenNoSword;
    private MultiBooleanSetting.Option cerberusWhenLeaving;
    private MultiBooleanSetting.Option cerberusWhileFlying;
    private MultiBooleanSetting.Option cerberusWhenNotAttacked;
    private MultiBooleanSetting takePunisher;
    private MultiBooleanSetting.Option punisherAlways;
    private MultiBooleanSetting.Option punisherWhenNoSword;
    private MultiBooleanSetting.Option punisherWhenLeaving;
    private MultiBooleanSetting.Option punisherWhileFlying;
    private boolean swapActive;
    private boolean overlayVisible;
    private boolean privilegedMode;
    private int pendingSwapSlot = -1;
    private static final long SWAP_DELAY = 1000L;
    private static final float MIN_DISTANCE = 2.0f;
    private final Timer cooldownTimer = new Timer();
    private final Timer actionTimer = new Timer();
    private float swapProgress = -1.0f;
    private final EventListener<KeyPressEvent> onKeyPressEventListener = keyPressEvent -> {
        IntegerSetting integerSetting;
        if (keyPressEvent.getAction() != 1 || AutoSwap.minecraftClient.currentScreen != null) {
            return;
        }
        IntegerSetting integerSetting2 = integerSetting = this.serverMode.isSelected(this.hwOption) ? this.autoCerber : this.autoPunisher;
        if (integerSetting.isIntValid(keyPressEvent.getKey())) {
            this.prepareSwap();
        }
        if (this.button.isIntValid(keyPressEvent.getKey()) && !this.overlayVisible && this.isServerSwapReady()) {
            this.selectSwapItem();
        }
    };
    private final EventListener<MouseEvent> onMouseEventListener = mouseEvent -> {
        if (AutoSwap.minecraftClient.currentScreen != null) {
            return;
        }
        if (this.autoCerber.isIntValid(mouseEvent.getButton()) || this.autoPunisher.isIntValid(mouseEvent.getButton())) {
            boolean bl = this.swapActive = !this.swapActive;
        }
        if (this.button.isIntValid(mouseEvent.getButton()) && !this.overlayVisible && this.isServerSwapReady()) {
            this.selectSwapItem();
        }
    };

    public AutoSwap() {
        this.processSwap();
    }

    @Compile(obfuscation=4)
    private void processSwap() {
        this.button = new IntegerSetting(this, "modules.settings.auto_swap.button");
        this.itemMode = new ModeSetting(this, "modules.settings.auto_swap.item");
        this.talismanOption = new ModeSetting.Option(this.itemMode, "modules.settings.auto_swap.item.talisman").select();
        this.swapToMode = new ModeSetting(this, "modules.settings.auto_swap.swap_to");
        this.swapToTalismanOption = new ModeSetting.Option(this.swapToMode, "modules.settings.auto_swap.swap_to.talisman").select();
        this.serverMode = new ModeSetting(this, "Server");
        this.hwOption = new ModeSetting.Option(this.serverMode, "HW").select();
        this.vontamOption = new ModeSetting.Option(this.serverMode, "VonTam");
        this.autoCerber = new IntegerSetting(this, "modules.settings.auto_swap.auto_cerber", () -> !this.serverMode.isSelected(this.hwOption));
        this.autoPunisher = new IntegerSetting(this, "Auto rage/punisher", () -> !this.serverMode.isSelected(this.vontamOption));
        this.takeCerberus = new MultiBooleanSetting((SettingOwner)this, "Take Cerberus", () -> !this.serverMode.isSelected(this.hwOption));
        this.cerberusAlways = new MultiBooleanSetting.Option(this.takeCerberus, "Always");
        this.cerberusWhenNoSword = new MultiBooleanSetting.Option(this.takeCerberus, "When without sword", this.cerberusAlways::isSelected).select();
        this.cerberusWhenLeaving = new MultiBooleanSetting.Option(this.takeCerberus, "When leaving", this.cerberusAlways::isSelected).select();
        this.cerberusWhileFlying = new MultiBooleanSetting.Option(this.takeCerberus, "When in elytra", this.cerberusAlways::isSelected).select();
        this.cerberusWhenNotAttacked = new MultiBooleanSetting.Option(this.takeCerberus, "If not being hit");
        this.takePunisher = new MultiBooleanSetting((SettingOwner)this, "Take rage/punisher", () -> !this.serverMode.isSelected(this.vontamOption));
        this.punisherAlways = new MultiBooleanSetting.Option(this.takePunisher, "Always");
        this.punisherWhenNoSword = new MultiBooleanSetting.Option(this.takePunisher, "When without sword", this.punisherAlways::isSelected).select();
        this.punisherWhenLeaving = new MultiBooleanSetting.Option(this.takePunisher, "When leaving", this.punisherAlways::isSelected).select();
        this.punisherWhileFlying = new MultiBooleanSetting.Option(this.takePunisher, "When in elytra", this.punisherAlways::isSelected).select();
        new ModeSetting.Option(this.swapToMode, "modules.settings.auto_swap.swap_to.orb");
        new ModeSetting.Option(this.itemMode, "modules.settings.auto_swap.item.orb");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.swapActive = false;
        this.overlayVisible = false;
        this.privilegedMode = false;
        this.pendingSwapSlot = -1;
        this.resetSwapState();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.swapActive = false;
        this.overlayVisible = false;
        this.privilegedMode = false;
        this.pendingSwapSlot = -1;
        this.resetSwapState();
    }

    private void resetSwapState() {
        this.actionTimer.setLastResetTimeMillis(0L);
        this.swapProgress = -1.0f;
    }

    @Override
    public void onTick() {
        LivingEntity class_13092;
        LivingEntity class_13093;
        Entity class_12972 = RockstarClient.create().getFriendManager().getTargetEntity();
        LivingEntity class_13094 = class_13093 = class_12972 instanceof LivingEntity ? (class_13092 = (LivingEntity)class_12972) : null;
        if (AutoSwap.minecraftClient.player == null) {
            return;
        }
        this.resetSwapFallback();
        boolean bl = this.isItemValid(InventoryUtils.offhandRule().getItemStack());
        if (!bl) {
            this.overlayVisible = false;
        }
        if (this.overlayVisible && this.pendingSwapSlot != -1 && !this.isEntityValid(class_13093)) {
            InventoryUtils.dropItem(this.pendingSwapSlot, 40);
            this.privilegedMode = true;
            this.pendingSwapSlot = -1;
            this.overlayVisible = false;
            return;
        }
        if (!this.swapActive || class_13093 == null) {
            return;
        }
        if (this.isEntityValid(class_13093) && !this.overlayVisible && this.isServerSwapReady()) {
            this.finalizeSwap();
            this.overlayVisible = true;
        }
    }

    public boolean isSwapReady() {
        if (!this.isEnabled() || AutoSwap.minecraftClient.player == null) {
            return false;
        }
        return this.swapActive && (this.overlayVisible || this.isItemValid(InventoryUtils.offhandRule().getItemStack()));
    }

    private void prepareSwap() {
        boolean bl = this.swapActive = !this.swapActive;
        if (!this.swapActive && this.overlayVisible && this.pendingSwapSlot != -1) {
            InventoryUtils.dropItem(this.pendingSwapSlot, 40);
            this.pendingSwapSlot = -1;
            this.overlayVisible = false;
        }
    }

    private void selectSwapItem() {
        boolean bl;
        if (AutoSwap.minecraftClient.currentScreen != null || ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY) && !this.cooldownTimer.hasElapsed(150L)) {
            return;
        }
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules()).combineRules(ItemRuleSets.getOffhandRules());
        boolean bl2 = bl = !this.talismanOption.isSelected() && !this.swapToTalismanOption.isSelected();
        if (this.hwOption.isSelected() && bl) {
            this.processSwapRules(itemRuleCollection);
            return;
        }
        List<@Nullable ItemRule> list = itemRuleCollection.findAllByItem(this.talismanOption.isSelected() ? Items.TOTEM_OF_UNDYING : Items.PLAYER_HEAD);
        List<@Nullable ItemRule> list2 = itemRuleCollection.findAllByItem(this.swapToTalismanOption.isSelected() ? Items.TOTEM_OF_UNDYING : Items.PLAYER_HEAD);
        ItemRule itemRule3 = list.stream().min(Comparator.comparingInt(itemRule -> DonorItemSelector.getMetadataPriority(itemRule.getItemStack()) - (itemRule.getClickSlot() == 45 ? 199 : this.getRuleScore((ItemRule)itemRule)))).orElse(null);
        ItemRule itemRule4 = list2.stream().filter(itemRule2 -> itemRule3 != itemRule2).min(Comparator.comparingInt(itemRule -> DonorItemSelector.getMetadataPriority(itemRule.getItemStack()) - (itemRule.getClickSlot() == 45 ? 199 : this.getRuleScore((ItemRule)itemRule)))).orElse(null);
        if (itemRule3 == null || itemRule4 == null) {
            return;
        }
        InventoryUtils.dropItem((AutoSwap.minecraftClient.player.getOffHandStack().getItem() == itemRule3.getItem() ? itemRule4 : itemRule3).getClickSlot(), 40);
        this.cooldownTimer.reset();
        ItemStack class_17992 = AutoSwap.minecraftClient.player.getOffHandStack();
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        if (donorItem != null && class_17992.getItem() != Items.AIR) {
            String string = donorItem.getDisplayName(class_17992);
            RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translateFormatted("alerts.moved_to_offhand", string), class_17992).withHighlightedText(string).withHighlightColor(donorItem.getDisplayColor(class_17992)));
        }
    }

    private void processSwapRules(ItemRuleCollection<ItemRule> itemRuleCollection) {
        List<ItemStack> sphereStacks = itemRuleCollection.findAllByItem(Items.PLAYER_HEAD).stream().filter(itemRule -> itemRule != null && itemRule.getItemStack() != null).map(ItemRule::getItemStack).filter(itemStack -> {
            DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(itemStack);
            return donorItem != null && donorItem.isSphereCategory();
        }).toList();
        ItemStack offhandStack = AutoSwap.minecraftClient.player.getOffHandStack();
        ItemStack fastSphere = DonorItemSelector.selectFastSphere(sphereStacks);
        ItemStack fallbackSphere = DonorItemSelector.selectFallbackSphere(sphereStacks);
        ItemStack selectedSphere;
        if (fastSphere != null && fallbackSphere != null) {
            selectedSphere = ItemStack.areEqual(offhandStack, fastSphere) ? fallbackSphere : fastSphere;
        } else {
            ItemStack alternateSphere = DonorItemSelector.selectAlternateSphere(sphereStacks);
            if (fallbackSphere != null && alternateSphere != null) {
                selectedSphere = ItemStack.areEqual(offhandStack, fallbackSphere) ? alternateSphere : fallbackSphere;
            } else {
                return;
            }
        }
        if (ItemStack.areEqual(selectedSphere, offhandStack)) {
            return;
        }
        ItemRule selectedRule = itemRuleCollection.findAllByItem(Items.PLAYER_HEAD).stream().filter(itemRule -> ItemStack.areEqual(itemRule.getItemStack(), selectedSphere)).findFirst().orElse(null);
        if (selectedRule == null || selectedRule.getClickSlot() == 45) {
            return;
        }
        InventoryUtils.dropItem(selectedRule.getClickSlot(), 40);
        this.cooldownTimer.reset();
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(selectedSphere);
        if (donorItem != null) {
            String displayName = donorItem.getDisplayName(selectedSphere);
            RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translateFormatted("alerts.moved_to_offhand", displayName), selectedSphere).withHighlightedText(displayName).withHighlightColor(donorItem.getDisplayColor(selectedSphere)));
        }
    }

    private void finalizeSwap() {
        if (AutoSwap.minecraftClient.currentScreen != null) {
            return;
        }
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules()).combineRules(ItemRuleSets.getOffhandRules());
        List<@Nullable ItemRule> list = itemRuleCollection.findAllByStack(this::isItemValid);
        ItemRule itemRule2 = list.stream().filter(itemRule -> itemRule.getClickSlot() != 45).min(Comparator.comparingInt(this::getFallbackRuleScore).thenComparing(itemRule -> DonorItemSelector.getMetadataPriority(itemRule.getItemStack()))).orElse(null);
        if (itemRule2 == null) {
            return;
        }
        this.pendingSwapSlot = itemRule2.getClickSlot();
        ItemStack class_17992 = itemRule2.getItemStack();
        InventoryUtils.dropItem(itemRule2.getClickSlot(), 40);
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        if (donorItem != null && class_17992.getItem() != Items.AIR) {
            String string = donorItem.getDisplayName(class_17992);
            RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translateFormatted("alerts.moved_to_offhand", string), class_17992).withHighlightedText(string).withHighlightColor(donorItem.getDisplayColor(class_17992)));
        }
    }

    private boolean isServerSwapReady() {
        return !RockstarClient.create().getModuleRegistry().getModule(AutoTotem.class).shouldEquipTotem();
    }

    private void resetSwapFallback() {
        float f = AutoSwap.minecraftClient.player.getHealth() + AutoSwap.minecraftClient.player.getAbsorptionAmount();
        if (this.swapProgress >= 0.0f && AutoSwap.minecraftClient.player.hurtTime > 0 && this.swapProgress - f >= 2.0f) {
            this.actionTimer.reset();
        }
        this.swapProgress = f;
    }

    private boolean isEntityValid(LivingEntity class_13092) {
        PlayerEntity class_16572;
        if (!this.swapActive) {
            return false;
        }
        if (this.isModeSelected() && !this.actionTimer.hasElapsed(1000L)) {
            return false;
        }
        if (this.isModeSelectedPrimary()) {
            return true;
        }
        if (AutoSwap.minecraftClient.player.hurtTime > 0 || class_13092 == null) {
            return false;
        }
        if (this.isModeSelectedSecondary() && !(class_13092.getMainHandStack().getItem() instanceof SwordItem)) {
            return true;
        }
        if (this.isModeSelectedTertiary() && EntityUtils.isMovingTowardPlayer(class_13092) && !AutoSwap.minecraftClient.player.isTouchingWater()) {
            return true;
        }
        return this.isModeSelectedFallback() && class_13092 instanceof PlayerEntity && (class_16572 = (PlayerEntity)class_13092).getInventory().getArmorStack(2).getItem() == Items.ELYTRA;
    }

    private boolean isModeSelected() {
        return this.serverMode.isSelected(this.hwOption) && this.cerberusWhenNotAttacked.isSelected();
    }

    private boolean isModeSelectedPrimary() {
        return this.serverMode.isSelected(this.hwOption) ? this.cerberusAlways.isSelected() : this.punisherAlways.isSelected();
    }

    private boolean isModeSelectedSecondary() {
        return this.serverMode.isSelected(this.hwOption) ? this.cerberusWhenNoSword.isSelected() : this.punisherWhenNoSword.isSelected();
    }

    private boolean isModeSelectedTertiary() {
        return this.serverMode.isSelected(this.hwOption) ? this.cerberusWhenLeaving.isSelected() : this.punisherWhenLeaving.isSelected();
    }

    private boolean isModeSelectedFallback() {
        return this.serverMode.isSelected(this.hwOption) ? this.cerberusWhileFlying.isSelected() : this.punisherWhileFlying.isSelected();
    }

    private int getRuleScore(ItemRule itemRule) {
        return this.swapActive && this.isItemValid(itemRule.getItemStack()) ? -99 : 0;
    }

    private boolean isItemValid(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return false;
        }
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        if (donorItem == null) {
            return false;
        }
        String string = donorItem.getRawName();
        if (string == null) {
            return false;
        }
        if (this.serverMode.isSelected(this.vontamOption)) {
            if (!donorItem.isTalismanCategory()) {
                return false;
            }
            return string.equals("\u042f\u0440\u043e\u0441\u0442\u0438") || string.equals("\u041a\u0430\u0440\u0430\u0442\u0435\u043b\u044f");
        }
        if (!donorItem.isSphereCategory()) {
            return false;
        }
        return string.equals("Cerber");
    }

    private int getFallbackRuleScore(ItemRule itemRule) {
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(itemRule.getItemStack());
        if (donorItem == null) {
            return 2;
        }
        String string = donorItem.getRawName();
        if (string == null) {
            return 2;
        }
        if (this.serverMode.isSelected(this.vontamOption)) {
            if (!donorItem.isTalismanCategory()) {
                return 2;
            }
            if (string.equals("\u041a\u0430\u0440\u0430\u0442\u0435\u043b\u044f")) {
                return 0;
            }
            if (string.equals("\u042f\u0440\u043e\u0441\u0442\u0438")) {
                return 1;
            }
            return 2;
        }
        if (!donorItem.isSphereCategory()) {
            return 2;
        }
        return string.equals("Cerber") ? 0 : 1;
    }

    @Generated
    public boolean isCerberusReady() {
        return this.swapActive;
    }

    @Generated
    public boolean isPunisherReady() {
        return this.overlayVisible;
    }

    @Generated
    public boolean isServerModeReady() {
        return this.privilegedMode;
    }
}
