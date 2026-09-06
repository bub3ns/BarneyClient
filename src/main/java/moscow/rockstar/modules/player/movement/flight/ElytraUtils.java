/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.SlotActionType
 *  net.minecraft.ArmorItem
 *  net.minecraft.ArmorMaterial
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.Packet
 *  net.minecraft.ClientCommandC2SPacket
 *  net.minecraft.ClientCommandC2SPacket$Mode
 *  net.minecraft.RegistryKey
 *  net.minecraft.EquipmentType
 */
package moscow.rockstar.modules.player.movement.flight;

import java.util.function.Predicate;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.items.ItemMetadataUtils;
import moscow.rockstar.items.rules.ArmorSlotRule;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.modules.player.movement.flight.ElytraSwapResult;
import moscow.rockstar.render.esp.EntityRenderContext;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.api.access.ArmorItemAccess;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.ItemNotification;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.item.equipment.EquipmentType;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.window.KeyPressEvent;
import pyrock.events.window.MouseEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Elytra Utils", category=ModuleCategory.PLAYER, description="modules.descriptions.elytra_utils")
public class ElytraUtils
extends Module {
    private IntegerSetting swapKey;
    private IntegerSetting fireworkKey;
    private ModeSetting fireworkSwapMode;
    private ModeSetting.Option defaultFlightMode;
    private ModeSetting.Option packet;
    private BooleanSetting autoTakeoff;
    private BooleanSetting autoUse;
    private BooleanSetting chestOnGround;
    private boolean swapActive;
    private ElytraSwapResult swapResult;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        boolean bl;
        if (ElytraUtils.minecraftClient.player.isGliding()) {
            this.swapActive = true;
        }
        InventoryMove inventoryMove = RockstarClient.create().getModuleRegistry().getModule(InventoryMove.class);
        ArmorSlotRule armorSlotRule = InventoryUtils.chestplateRule();
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getHotbarRules().combineRules(ItemRuleSets.getInventoryRules()).combineRules(ItemRuleSets.getOffhandRules());
        ItemRule itemRule = this.getItemRule(itemRuleCollection);
        ItemRule itemRule2 = itemRuleCollection.findByItem(Items.FIREWORK_ROCKET);
        boolean bl2 = bl = armorSlotRule.getItem() == Items.ELYTRA;
        if (this.swapResult != null) {
            if (this.swapResult.selectedItemRule.getClickSlot() >= 36 && this.swapResult.selectedItemRule.getClickSlot() <= 44) {
                InventoryUtils.dropItem(this.swapResult.equippedArmorRule.getClickSlot(), this.swapResult.selectedItemRule.getClickSlot() - 36);
                this.swapResult = null;
            } else if (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)) {
                if (this.swapResult.swapStep == 0 && inventoryMove.getModeEntries().isEmpty()) {
                    InventoryUtils.dropItem(this.swapResult.selectedItemRule.getClickSlot(), 8);
                    InventoryUtils.dropItem(this.swapResult.equippedArmorRule.getClickSlot(), 8);
                    InventoryUtils.dropItem(this.swapResult.selectedItemRule.getClickSlot(), 8);
                    ++this.swapResult.swapStep;
                } else if (this.swapResult.swapStep == 1 && inventoryMove.getModeEntries().isEmpty()) {
                    ++this.swapResult.swapStep;
                } else if (this.swapResult.swapStep == 2 && inventoryMove.getModeEntries().isEmpty()) {
                    ++this.swapResult.swapStep;
                }
            } else if (this.swapResult.swapStep == 0 && inventoryMove.getModeEntries().isEmpty()) {
                InventoryUtils.dropItem(this.swapResult.selectedItemRule.getClickSlot(), 8);
                ++this.swapResult.swapStep;
            } else if (this.swapResult.swapStep == 1 && inventoryMove.getModeEntries().isEmpty()) {
                InventoryUtils.dropItem(this.swapResult.equippedArmorRule.getClickSlot(), 8);
                ++this.swapResult.swapStep;
            } else if (this.swapResult.swapStep == 2 && inventoryMove.getModeEntries().isEmpty()) {
                InventoryUtils.dropItem(this.swapResult.selectedItemRule.getClickSlot(), 8);
                ++this.swapResult.swapStep;
            }
            if (this.swapResult != null && this.swapResult.swapStep >= 3) {
                this.swapResult = null;
            }
        }
        if (bl) {
            if (this.autoTakeoff.isEnabled() && ElytraUtils.minecraftClient.player.isOnGround() && !ElytraUtils.minecraftClient.options.jumpKey.isPressed()) {
                ElytraUtils.minecraftClient.player.jump();
            }
            if (this.autoTakeoff.isEnabled() && !ElytraUtils.minecraftClient.player.isInFluid() && ElytraUtils.minecraftClient.player.isSprinting() && ElytraUtils.minecraftClient.player.input.hasForwardMovement() && ElytraUtils.minecraftClient.player.checkGliding()) {
                minecraftClient.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraUtils.minecraftClient.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                if (this.autoUse.isEnabled() && itemRule2 != null) {
                    InventoryUtils.selectHotbarItem(Items.FIREWORK_ROCKET);
                }
            }
        }
        if (this.chestOnGround.isEnabled() && ElytraUtils.minecraftClient.player.isOnGround() && bl && this.swapActive && ElytraUtils.minecraftClient.player.getGlidingTicks() > 18) {
            if (itemRule != null) {
                this.resetElytraSwap();
            } else {
                ElytraUtils.minecraftClient.interactionManager.clickSlot(0, 6, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)ElytraUtils.minecraftClient.player);
            }
            this.swapActive = false;
        }
    };
    private final EventListener<KeyPressEvent> onKeyPressEvent = keyPressEvent -> {
        if (this.swapKey.isIntValid(keyPressEvent.getKey()) && keyPressEvent.getAction() == 1 && ElytraUtils.minecraftClient.currentScreen == null) {
            this.resetElytraSwap();
        }
        if (this.fireworkKey.isIntValid(keyPressEvent.getKey()) && keyPressEvent.getAction() == 1 && ElytraUtils.minecraftClient.currentScreen == null && ElytraUtils.minecraftClient.player.isGliding()) {
            if (this.fireworkSwapMode.isSelected(this.defaultFlightMode)) {
                InventoryUtils.selectHotbarItem(Items.FIREWORK_ROCKET);
            } else {
                EntityRenderContext.attackTargetWithItem(this.getItemDurability(Items.FIREWORK_ROCKET, class_17992 -> true));
            }
        }
    };
    private final EventListener<MouseEvent> onMouseEvent = mouseEvent -> {
        if (this.swapKey.isIntValid(mouseEvent.getButton()) && mouseEvent.getAction() == 1 && ElytraUtils.minecraftClient.currentScreen == null) {
            this.resetElytraSwap();
        }
        if (this.fireworkKey.isIntValid(mouseEvent.getButton()) && mouseEvent.getAction() == 1 && ElytraUtils.minecraftClient.currentScreen == null && ElytraUtils.minecraftClient.player.isGliding()) {
            if (this.fireworkSwapMode.isSelected(this.defaultFlightMode)) {
                InventoryUtils.selectHotbarItem(Items.FIREWORK_ROCKET);
            } else {
                EntityRenderContext.attackTargetWithItem(this.getItemDurability(Items.FIREWORK_ROCKET, class_17992 -> true));
            }
        }
    };

    public ElytraUtils() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.swapKey = new IntegerSetting(this, "modules.settings.elytra_utils.swapKey");
        this.fireworkKey = new IntegerSetting(this, "modules.settings.elytra_utils.fireworkKey");
        this.fireworkSwapMode = new ModeSetting((SettingOwner)this, "modules.settings.firework_swap_mode", () -> this.fireworkKey.getValue() == -1);
        this.defaultFlightMode = new ModeSetting.Option(this.fireworkSwapMode, "modules.settings.elytra_utils.firework_swap_mode.default");
        this.packet = new ModeSetting.Option(this.fireworkSwapMode, "modules.settings.elytra_utils.firework_swap_mode.packet");
        this.autoTakeoff = new BooleanSetting(this, "modules.settings.elytra_utils.auto_takeoff");
        this.autoUse = new BooleanSetting((SettingOwner)this, "modules.settings.elytra_utils.auto_use", () -> !this.autoTakeoff.isEnabled()).enable();
        this.chestOnGround = new BooleanSetting(this, "modules.settings.elytra_utils.chest_on_ground");
    }

    private ItemRule getItemRule(ItemRuleCollection<ItemRule> itemRuleCollection) {
        ItemRule itemRule = null;
        int n = Integer.MIN_VALUE;
        for (ItemRule itemRule2 : itemRuleCollection.getRules()) {
            int n2;
            ArmorItem class_17382;
            Item class_17922;
            ItemStack class_17992 = itemRule2.getItemStack();
            if (class_17992.isEmpty() || !((class_17922 = class_17992.getItem()) instanceof ArmorItem) || ((ArmorItemAccess)(class_17382 = (ArmorItem)class_17922)).rockstar$getType() != EquipmentType.CHESTPLATE || (n2 = this.getArmorScore(class_17382, class_17992)) <= n) continue;
            n = n2;
            itemRule = itemRule2;
        }
        return itemRule;
    }

    private int getArmorScore(ArmorItem class_17382, ItemStack class_17992) {
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        if (donorItem != null && "SunHelmet".equals(donorItem.getRawName())) {
            return Integer.MAX_VALUE;
        }
        ArmorMaterial armorMaterial = ((ArmorItemAccess)class_17382).rockstar$getMaterial();
        EquipmentType class_80512 = ((ArmorItemAccess)class_17382).rockstar$getType();
        int n = armorMaterial.defense().getOrDefault(class_80512, 0);
        int n2 = (int)armorMaterial.toughness();
        int n3 = EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.PROTECTION);
        return n * 5 + n3 * 3 + n2;
    }

    private void resetElytraSwap() {
        boolean bl;
        ArmorSlotRule armorSlotRule = InventoryUtils.chestplateRule();
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules());
        ItemRule itemRule = itemRuleCollection.findByStack(class_17992 -> class_17992.getItem() == Items.ELYTRA && !class_17992.willBreakNextUse());
        ItemRule itemRule2 = this.getItemRule(itemRuleCollection);
        boolean bl2 = bl = armorSlotRule.getItem() == Items.ELYTRA;
        if (this.swapResult != null) {
            return;
        }
        if (!bl && itemRule != null) {
            this.swapResult = new ElytraSwapResult(itemRule, armorSlotRule);
            String string = ItemMetadataUtils.cleanDisplayName(itemRule.getItemStack());
            RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translateFormatted("alerts.equipped", string), itemRule.getItemStack()).withHighlightedText(string));
        } else if (itemRule2 != null) {
            this.swapResult = new ElytraSwapResult(itemRule2, armorSlotRule);
            String string = ItemMetadataUtils.cleanDisplayName(itemRule2.getItemStack());
            RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translateFormatted("alerts.equipped", string), itemRule2.getItemStack()).withHighlightedText(string));
        }
    }

    public float getItemDurability(Item class_17922, Predicate<ItemStack> predicate) {
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getHotbarRules().combineRules(ItemRuleSets.getInventoryRules()).combineRules(ItemRuleSets.getOffhandRules());
        Predicate<ItemStack> matchingItem = stack -> stack != null && !stack.isEmpty()
            && stack.getItem() == class_17922 && predicate.test(stack);
        ItemRule itemRule = itemRuleCollection.findByStack(matchingItem);
        if (itemRule == null) {
            itemRule = itemRuleCollection.findByStack(predicate);
        }
        if (itemRule == null) {
            return InventoryUtils.getSelectedHotbarSlot().getSlotIndex() + 1;
        }
        int n = itemRule.getClickSlot();
        if (n >= 36 && n <= 44) {
            return n - 35;
        }
        return InventoryUtils.getSelectedHotbarSlot().getSlotIndex() + 1;
    }

    private boolean isItemValid(ItemStack class_17992, Item class_17922) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return false;
        }
        if (class_17992.getItem() != class_17922) {
            return false;
        }
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        return donorItem != null && donorItem.isConsumableCategory();
    }

    @Override
    public void onDisable() {
        this.swapActive = false;
    }

    @Override
    public void onEnable() {
        this.swapActive = false;
    }

}

