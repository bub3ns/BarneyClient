/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.PlayerEntity
 *  net.minecraft.PlayerInventory
 *  net.minecraft.ScreenHandler
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.ShulkerBoxScreenHandler
 *  net.minecraft.Slot
 *  net.minecraft.AxeItem
 *  net.minecraft.BlockItem
 *  net.minecraft.BowItem
 *  net.minecraft.CrossbowItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.TridentItem
 *  net.minecraft.ShulkerBoxBlock
 *  net.minecraft.HandledScreen
 *  net.minecraft.MaceItem
 */
package moscow.rockstar.modules.player.automation.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.items.ItemMetadataUtils;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.player.automation.inventory.ShulkerWorkflowState;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.MaceItem;
import pyrock.events.game.PickupEvent;
import pyrock.events.window.KeyPressEvent;
import pyrock.events.window.MouseEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Shulker", category=ModuleCategory.PLAYER, description="modules.descriptions.auto_shulker")
public class AutoShulker
extends Module {
    private static final String SHULKER_DISPLAY_NAME = "\u0440\u044e\u043a\u0437\u0430\u043a";
    private static final int SHULKER_HOTBAR_SLOT = 8;
    private static final int LAST_HOTBAR_INVENTORY_SLOT = 44;
    private static final Set<Item> armorItems = Set.of(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_CARROT, Items.CHORUS_FRUIT, Items.ENDER_PEARL, Items.ENDER_EYE, Items.EXPERIENCE_BOTTLE, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.FIREWORK_ROCKET, Items.SNOWBALL);
    private static final Set<Item> weaponItems = Set.of(Items.NETHERITE_INGOT, Items.NETHERITE_SCRAP, Items.NETHERITE_BLOCK, Items.ANCIENT_DEBRIS, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.NETHER_STAR, Items.BEACON, Items.ELYTRA, Items.DIAMOND, Items.DIAMOND_BLOCK, Items.EMERALD, Items.EMERALD_BLOCK, Items.ECHO_SHARD, Items.HEAVY_CORE, Items.DRAGON_EGG, Items.DRAGON_BREATH, Items.ENDER_CHEST);
    private static final Set<Item> enchantmentItems = Set.of(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
    private static final Set<Item> totemItems = Set.of(Items.GOLDEN_HELMET, Items.GOLDEN_BOOTS);
    private static final Set<Item> headItems = Set.of(Items.PLAYER_HEAD, Items.WITHER_SKELETON_SKULL, Items.SKELETON_SKULL, Items.DRAGON_HEAD, Items.CREEPER_HEAD, Items.ZOMBIE_HEAD, Items.PIGLIN_HEAD);
    private ModeSetting mode;
    private ModeSetting.Option auto;
    private ModeSetting.Option bind;
    private IntegerSetting bindKey;
    private MultiBooleanSetting valuables;
    private MultiBooleanSetting.Option armor;
    private MultiBooleanSetting.Option weapons;
    private MultiBooleanSetting.Option enchants;
    private MultiBooleanSetting.Option totems;
    private MultiBooleanSetting.Option heads;
    private MultiBooleanSetting.Option consumables;
    private MultiBooleanSetting.Option resources;
    private MultiBooleanSetting.Option donate;
    private NumberSetting delay;
    private final Timer cooldownTimer = new Timer();
    private final Timer actionTimer = new Timer();
    private final Timer targetTimer = new Timer();
    private final Map<Integer, ItemStack> savedItems = new HashMap<Integer, ItemStack>();
    private final List<ItemStack> shulkerItems = new ArrayList<ItemStack>();
    private final Set<Integer> processedItems = new HashSet<Integer>();
    private final ItemStack[] shulkerContents = new ItemStack[36];
    private final Timer updateTimer = new Timer();
    private boolean shulkerOpen;
    private ShulkerWorkflowState shulkerState = ShulkerWorkflowState.IDLE;
    private boolean overlayVisible;
    private int currentShulkerSlot = -1;
    private int currentItemIndex = -1;
    private int processedItemCount;
    private boolean privilegedMode;
    private final EventListener<PickupEvent> onPickupEvent = pickupEvent -> {
        if (AutoShulker.minecraftClient.player == null || pickupEvent.getEntity() != AutoShulker.minecraftClient.player) {
            return;
        }
        if (!this.isShulkerModeSelected(pickupEvent.getItemStack())) {
            return;
        }
        this.updateShulkerContents(pickupEvent.getItemStack());
        this.shulkerOpen = true;
        this.updateTimer.reset();
        if (!this.mode.isSelected(this.auto)) {
            return;
        }
        this.overlayVisible = true;
        this.cooldownTimer.reset();
    };
    private final EventListener<KeyPressEvent> onKeyPressEvent = keyPressEvent -> {
        if (this.bindKey.isIntValid(keyPressEvent.getKey()) && keyPressEvent.getAction() == 1) {
            this.resetShulker();
        }
    };
    private final EventListener<MouseEvent> onMouseEvent = mouseEvent -> {
        if (this.bindKey.isIntValid(mouseEvent.getButton()) && mouseEvent.getAction() == 1) {
            this.resetShulker();
        }
    };

    public AutoShulker() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.mode = new ModeSetting(this, "modules.settings.auto_shulker.mode");
        this.auto = new ModeSetting.Option(this.mode, "modules.settings.auto_shulker.mode.auto").select();
        this.bind = new ModeSetting.Option(this.mode, "modules.settings.auto_shulker.mode.bind");
        this.bindKey = new IntegerSetting(this, "modules.settings.auto_shulker.bind", () -> !this.mode.isSelected(this.bind));
        this.valuables = new MultiBooleanSetting(this, "modules.settings.auto_shulker.valuables");
        this.armor = new MultiBooleanSetting.Option(this.valuables, "modules.settings.auto_shulker.valuables.armor").select();
        this.weapons = new MultiBooleanSetting.Option(this.valuables, "modules.settings.auto_shulker.valuables.weapons").select();
        this.enchants = new MultiBooleanSetting.Option(this.valuables, "modules.settings.auto_shulker.valuables.enchants").select();
        this.totems = new MultiBooleanSetting.Option(this.valuables, "modules.settings.auto_shulker.valuables.totems").select();
        this.heads = new MultiBooleanSetting.Option(this.valuables, "modules.settings.auto_shulker.valuables.heads").select();
        this.consumables = new MultiBooleanSetting.Option(this.valuables, "modules.settings.auto_shulker.valuables.consumables").select();
        this.resources = new MultiBooleanSetting.Option(this.valuables, "modules.settings.auto_shulker.valuables.resources").select();
        this.donate = new MultiBooleanSetting.Option(this.valuables, "modules.settings.auto_shulker.valuables.donate").select();
        this.delay = new NumberSetting((SettingOwner)this, "modules.settings.auto_shulker.delay", () -> !this.mode.isSelected(this.auto)).setMinValue(0.0f).setMaxValue(2000.0f).setStep(50.0f).setValue(300.0f);
    }

    private void resetShulker() {
        if (this.shulkerState != ShulkerWorkflowState.IDLE || AutoShulker.minecraftClient.currentScreen != null) {
            return;
        }
        this.overlayVisible = true;
        this.cooldownTimer.reset();
    }

    private void updateShulkerContents(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return;
        }
        if (this.isItemValid(class_17992)) {
            return;
        }
        if (this.shulkerItems.size() >= 64) {
            this.shulkerItems.remove(0);
        }
        this.shulkerItems.add(class_17992.copy());
    }

    private boolean isItemValid(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return false;
        }
        for (ItemStack class_17993 : this.shulkerItems) {
            if (!ItemStack.areItemsAndComponentsEqual((ItemStack)class_17993, (ItemStack)class_17992)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void onTick() {
        if (AutoShulker.minecraftClient.player == null || AutoShulker.minecraftClient.world == null || AutoShulker.minecraftClient.interactionManager == null) {
            this.overlayVisible = false;
            this.resetProjectedItem();
            this.resetInterpolatedItem();
            return;
        }
        this.resetSelection();
        switch (this.shulkerState.ordinal()) {
            case 0: {
                this.resetContainerState();
                break;
            }
            case 1: {
                this.resetItemState();
                break;
            }
            case 2: {
                this.resetFallbackState();
                break;
            }
            case 3: {
                this.resetAlternateState();
                break;
            }
            case 4: {
                this.initializeShulkerState();
                break;
            }
            case 5: {
                this.finishShulkerState();
                break;
            }
            case 6: {
                this.resetCurrentItem();
            }
        }
        PlayerInventory class_16612 = AutoShulker.minecraftClient.player.getInventory();
        for (int i = 0; i < this.shulkerContents.length; ++i) {
            this.shulkerContents[i] = class_16612.getStack(i).copy();
        }
    }

    private void resetSelection() {
        PlayerInventory class_16612 = AutoShulker.minecraftClient.player.getInventory();
        boolean bl = this.shulkerOpen && !this.updateTimer.hasElapsed(1000L);
        for (int i = 0; i < this.shulkerContents.length; ++i) {
            boolean bl2;
            if (i == 8) continue;
            ItemStack class_17992 = class_16612.getStack(i);
            if (class_17992.isEmpty() || !this.isItemValid(class_17992)) {
                this.processedItems.remove(i);
                continue;
            }
            ItemStack class_17993 = this.shulkerContents[i];
            boolean bl3 = bl2 = class_17993 == null || class_17993.isEmpty() || !ItemStack.areItemsAndComponentsEqual((ItemStack)class_17993, (ItemStack)class_17992);
            if (!bl2 || !bl) continue;
            this.processedItems.add(i);
        }
    }

    private void resetContainerState() {
        if (!this.overlayVisible || AutoShulker.minecraftClient.currentScreen != null) {
            return;
        }
        if (this.mode.isSelected(this.auto) && !this.cooldownTimer.hasElapsed((long)this.delay.getValue())) {
            return;
        }
        if (!this.isShulkerReady()) {
            this.overlayVisible = false;
            return;
        }
        ItemRule itemRule = this.getShulkerItemRule();
        if (itemRule == null) {
            this.overlayVisible = false;
            return;
        }
        if (!this.isInventoryMoveUnavailable()) {
            return;
        }
        this.overlayVisible = false;
        this.currentItemIndex = InventoryUtils.getSelectedHotbarSlot().getSlotIndex();
        int n = itemRule.getClickSlot();
        if (n == 44) {
            this.currentShulkerSlot = -1;
        } else {
            this.currentShulkerSlot = n;
            InventoryUtils.dropItem(n, 8);
        }
        this.shulkerState = ShulkerWorkflowState.OPEN_SHULKER_CONTAINER;
        this.actionTimer.reset();
    }

    private void resetItemState() {
        if (!this.isInventoryMoveUnavailable()) {
            if (this.actionTimer.hasElapsed(3000L)) {
                this.resetPreviousItem();
            }
            return;
        }
        if (!this.actionTimer.hasElapsed(150L)) {
            return;
        }
        if (!this.isValuableItem(InventoryUtils.hotbarSlot(8).getItemStack())) {
            this.resetPreviousItem();
            return;
        }
        InventoryUtils.setSelectedHotbarSlot(8);
        AutoShulker.minecraftClient.interactionManager.interactItem((PlayerEntity)AutoShulker.minecraftClient.player, Hand.MAIN_HAND);
        this.shulkerState = ShulkerWorkflowState.WAIT_FOR_CONTENT_SCREEN;
        this.actionTimer.reset();
    }

    private void resetFallbackState() {
        if (this.isContainerScreenOpen()) {
            this.savedItems.clear();
            this.privilegedMode = false;
            this.shulkerState = ShulkerWorkflowState.TRANSFER_VALUABLE_ITEMS;
            this.actionTimer.reset();
            return;
        }
        if (this.actionTimer.hasElapsed(3000L)) {
            this.shulkerState = ShulkerWorkflowState.WAIT_FOR_CONTAINER_CLOSE;
            this.actionTimer.reset();
        }
    }

    private void resetAlternateState() {
        if (!this.isContainerScreenOpen()) {
            this.shulkerState = ShulkerWorkflowState.WAIT_FOR_CONTAINER_CLOSE;
            this.actionTimer.reset();
            return;
        }
        ScreenHandler class_17032 = AutoShulker.minecraftClient.player.currentScreenHandler;
        int n = this.getContainerItemCount(class_17032);
        if (n == -1 && !this.actionTimer.hasElapsed(15000L)) {
            if (!this.privilegedMode) {
                this.privilegedMode = true;
                this.targetTimer.reset();
                return;
            }
            if (!this.targetTimer.hasElapsed(500L)) {
                return;
            }
        }
        if (n == -1 || this.actionTimer.hasElapsed(15000L)) {
            this.processedItems.clear();
            this.shulkerState = ShulkerWorkflowState.CLOSE_SHULKER_CONTAINER;
            this.actionTimer.reset();
            return;
        }
        this.privilegedMode = false;
        Slot class_17352 = class_17032.getSlot(n);
        ItemStack class_17992 = class_17352.getStack().copy();
        AutoShulker.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)AutoShulker.minecraftClient.player);
        if (ItemStack.areEqual((ItemStack)class_17352.getStack(), (ItemStack)class_17992)) {
            this.savedItems.put(n, class_17992);
        }
    }

    private void initializeShulkerState() {
        if (this.isContainerScreenOpen()) {
            AutoShulker.minecraftClient.player.closeHandledScreen();
            this.actionTimer.reset();
            return;
        }
        if (AutoShulker.minecraftClient.currentScreen != null) {
            if (this.actionTimer.hasElapsed(3000L)) {
                this.resetPreviousItem();
            }
            return;
        }
        this.shulkerState = ShulkerWorkflowState.WAIT_FOR_CONTAINER_CLOSE;
        this.actionTimer.reset();
    }

    private void finishShulkerState() {
        if (AutoShulker.minecraftClient.currentScreen != null || !this.isInventoryMoveUnavailable()) {
            if (this.actionTimer.hasElapsed(3000L)) {
                this.resetPreviousItem();
            }
            return;
        }
        if (!this.actionTimer.hasElapsed(150L)) {
            return;
        }
        if (this.currentShulkerSlot == -1 || this.processedItemCount >= 3) {
            this.resetPreviousItem();
            return;
        }
        ++this.processedItemCount;
        InventoryUtils.dropItem(this.currentShulkerSlot, 8);
        this.shulkerState = ShulkerWorkflowState.RETURN_SHULKER;
        this.actionTimer.reset();
    }

    private void resetCurrentItem() {
        if (!this.isInventoryMoveUnavailable() || !this.actionTimer.hasElapsed(500L)) {
            return;
        }
        boolean bl = this.isValuableItem(AutoShulker.minecraftClient.player.playerScreenHandler.getSlot(this.currentShulkerSlot).getStack());
        boolean bl2 = this.isValuableItem(InventoryUtils.hotbarSlot(8).getItemStack());
        if (!bl && bl2) {
            this.shulkerState = ShulkerWorkflowState.WAIT_FOR_CONTAINER_CLOSE;
            this.actionTimer.reset();
            return;
        }
        this.resetPreviousItem();
    }

    private void resetPreviousItem() {
        if (this.currentItemIndex != -1) {
            InventoryUtils.setSelectedHotbarSlot(this.currentItemIndex);
        }
        this.resetInterpolatedItem();
    }

    private void resetProjectedItem() {
        this.shulkerItems.clear();
        this.processedItems.clear();
        Arrays.fill(this.shulkerContents, null);
        this.shulkerOpen = false;
    }

    private void resetInterpolatedItem() {
        this.shulkerState = ShulkerWorkflowState.IDLE;
        this.privilegedMode = false;
        this.currentShulkerSlot = -1;
        this.currentItemIndex = -1;
        this.processedItemCount = 0;
        this.savedItems.clear();
    }

    private boolean isInventoryMoveUnavailable() {
        InventoryMove inventoryMove = RockstarClient.create().getModuleRegistry().getModule(InventoryMove.class);
        return inventoryMove == null || inventoryMove.getModeEntries().isEmpty();
    }

    private ItemRule getShulkerItemRule() {
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getHotbarRules().combineRules(ItemRuleSets.getInventoryRules()).combineRules(ItemRuleSets.getOffhandRules());
        return itemRuleCollection.findByStack(this::isValuableItem);
    }

    private boolean isShulkerReady() {
        for (int n : this.processedItems) {
            if (AutoShulker.minecraftClient.player.getInventory().getStack(n).isEmpty()) continue;
            return true;
        }
        return false;
    }

    private boolean isValuableItem(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty() || !this.isShulkerItemValid(class_17992)) {
            return false;
        }
        return class_17992.getName().getString().toLowerCase().contains(SHULKER_DISPLAY_NAME);
    }

    private boolean isShulkerItemValid(ItemStack class_17992) {
        BlockItem class_17472;
        Item class_17922 = class_17992.getItem();
        return class_17922 instanceof BlockItem && (class_17472 = (BlockItem)class_17922).getBlock() instanceof ShulkerBoxBlock;
    }

    private boolean isContainerScreenOpen() {
        if (AutoShulker.minecraftClient.player == null || !(AutoShulker.minecraftClient.currentScreen instanceof HandledScreen)) {
            return false;
        }
        ScreenHandler class_17032 = AutoShulker.minecraftClient.player.currentScreenHandler;
        return class_17032 instanceof GenericContainerScreenHandler || class_17032 instanceof ShulkerBoxScreenHandler;
    }

    private int getContainerItemCount(ScreenHandler class_17032) {
        for (int i = 0; i < class_17032.slots.size(); ++i) {
            ItemStack class_17992;
            Slot class_17352 = class_17032.getSlot(i);
            if (class_17352.inventory != AutoShulker.minecraftClient.player.getInventory() || !this.processedItems.contains(class_17352.getIndex()) || (class_17992 = class_17352.getStack()).isEmpty()) continue;
            ItemStack class_17993 = this.savedItems.get(i);
            if (class_17993 != null) {
                if (ItemStack.areEqual((ItemStack)class_17993, (ItemStack)class_17992)) continue;
                this.savedItems.remove(i);
            }
            return i;
        }
        return -1;
    }

    private boolean isShulkerModeSelected(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty() || this.isValuableItem(class_17992)) {
            return false;
        }
        Item class_17922 = class_17992.getItem();
        if (this.donate.isSelected() && (DonorItemParser.parseDonorItem(class_17992) != null || ItemMetadataUtils.hasDonationMetadata(class_17992))) {
            return true;
        }
        if (this.enchants.isSelected() && class_17922 == Items.ENCHANTED_BOOK) {
            return true;
        }
        if (this.armor.isSelected() && (enchantmentItems.contains(class_17922) || totemItems.contains(class_17922) && class_17992.hasEnchantments())) {
            return true;
        }
        if (this.weapons.isSelected() && (class_17922 == Items.NETHERITE_SWORD || class_17922 instanceof AxeItem || class_17922 instanceof MaceItem || class_17922 instanceof TridentItem || class_17922 instanceof BowItem || class_17922 instanceof CrossbowItem)) {
            return true;
        }
        if (this.totems.isSelected() && class_17922 == Items.TOTEM_OF_UNDYING) {
            return true;
        }
        if (this.heads.isSelected() && headItems.contains(class_17922)) {
            return true;
        }
        if (this.consumables.isSelected() && armorItems.contains(class_17922)) {
            return true;
        }
        return this.resources.isSelected() && (weaponItems.contains(class_17922) || this.isShulkerItemValid(class_17992));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.overlayVisible = false;
        this.resetProjectedItem();
        this.resetInterpolatedItem();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (this.shulkerState != ShulkerWorkflowState.IDLE && AutoShulker.minecraftClient.player != null && AutoShulker.minecraftClient.interactionManager != null) {
            if (this.isContainerScreenOpen()) {
                AutoShulker.minecraftClient.player.closeHandledScreen();
            }
            if (this.currentShulkerSlot != -1) {
                InventoryUtils.dropItem(this.currentShulkerSlot, 8);
            }
            if (this.currentItemIndex != -1) {
                InventoryUtils.setSelectedHotbarSlot(this.currentItemIndex);
            }
        }
        this.overlayVisible = false;
        this.resetProjectedItem();
        this.resetInterpolatedItem();
    }
}

