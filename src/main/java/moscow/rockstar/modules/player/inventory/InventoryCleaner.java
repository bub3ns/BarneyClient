/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.EquipmentSlot
 *  net.minecraft.PlayerEntity
 *  net.minecraft.SlotActionType
 *  net.minecraft.ArmorItem
 *  net.minecraft.ArmorMaterial
 *  net.minecraft.AxeItem
 *  net.minecraft.BowItem
 *  net.minecraft.CrossbowItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.SwordItem
 *  net.minecraft.TridentItem
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.RegistryKey
 *  net.minecraft.EquipmentType
 *  net.minecraft.MaceItem
 */
package moscow.rockstar.modules.player.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.BlockItemSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.api.access.ArmorItemAccess;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKey;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.MaceItem;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Inventory Cleaner", category=ModuleCategory.OTHER, description="modules.descriptions.inventory_cleaner")
public class InventoryCleaner
extends Module {
    private final Timer cleaningTimer = new Timer();
    private ModeSetting cleaningMode;
    private ModeSetting.Option legacyRulesMode;
    private ModeSetting.Option customRulesMode;
    private BlockItemSetting protectedBlockItems;
    private final List<ItemRule> discardCandidates = new ArrayList<ItemRule>();
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (!this.isEnabled() || InventoryCleaner.minecraftClient.player == null || InventoryCleaner.minecraftClient.player.currentScreenHandler == null) {
            return;
        }
        if (this.cleaningTimer.hasElapsed(150L)) {
            this.discardCandidates.clear();
            if (this.legacyRulesMode.isSelected()) {
                this.collectLowPriorityItems(ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules()));
            } else {
                this.collectProtectedItemRules(ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules()));
            }
            if (this.discardCandidates.isEmpty()) {
                return;
            }
            ItemRule itemRule = this.discardCandidates.removeFirst();
            InventoryCleaner.minecraftClient.interactionManager.clickSlot(InventoryCleaner.minecraftClient.player.currentScreenHandler.syncId, itemRule.getClickSlot(), 1, SlotActionType.THROW, (PlayerEntity)InventoryCleaner.minecraftClient.player);
            this.cleaningTimer.reset();
        }
    };

    public InventoryCleaner() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.cleaningMode = new ModeSetting(this, "modules.settings.inventory_cleaner.mode");
        this.legacyRulesMode = new ModeSetting.Option(this.cleaningMode, "modules.settings.inventory_cleaner.mode.1_8");
        this.customRulesMode = new ModeSetting.Option(this.cleaningMode, "modules.settings.inventory_cleaner.mode.custom").select();
        this.protectedBlockItems = new BlockItemSetting((SettingOwner)this, "modules.settings.inventory_cleaner.blocks", () -> !this.customRulesMode.isSelected()).enableAllItems();
    }

    private void collectProtectedItemRules(ItemRuleCollection<ItemRule> itemRuleCollection) {
        Set<Item> set = this.protectedBlockItems.getSelectedItems();
        if (set.isEmpty()) {
            return;
        }
        for (ItemRule itemRule : itemRuleCollection.getRules()) {
            if (itemRule.isEmpty() || !set.contains(itemRule.getItem())) continue;
            this.discardCandidates.add(itemRule);
        }
    }

    private void collectLowPriorityItems(ItemRuleCollection<ItemRule> itemRuleCollection) {
        for (ItemRule itemRule : itemRuleCollection.getRules()) {
            ItemStack class_17992 = itemRule.getItemStack();
            if (class_17992.isEmpty() || this.isSelectedHotbarItem(itemRule) || !this.isInferiorArmor(class_17992, itemRuleCollection) && !this.isInferiorCombatItem(class_17992, itemRuleCollection)) continue;
            this.discardCandidates.add(itemRule);
        }
    }

    private boolean isSelectedHotbarItem(ItemRule itemRule) {
        return InventoryCleaner.minecraftClient.player != null && itemRule.getClickSlot() == 36 + InventoryCleaner.minecraftClient.player.getInventory().selectedSlot;
    }

    private boolean isInferiorArmor(ItemStack class_17992, ItemRuleCollection<ItemRule> itemRuleCollection) {
        Item class_17922 = class_17992.getItem();
        if (!(class_17922 instanceof ArmorItem)) {
            return false;
        }
        ArmorItem class_17382 = (ArmorItem)class_17922;
        EquipmentSlot equipmentSlot = ((ArmorItemAccess)class_17382).rockstar$getType().getEquipmentSlot();
        ItemStack class_17993 = InventoryCleaner.minecraftClient.player.getEquippedStack(equipmentSlot);
        int n = this.calculateItemScore(class_17992);
        if (!class_17993.isEmpty() && class_17993.getItem() instanceof ArmorItem) {
            n = Math.max(n, this.calculateItemScore(class_17993));
        }
        for (ItemRule itemRule : itemRuleCollection.getRules()) {
            ArmorItem class_17383;
            Item class_17923;
            ItemStack class_17994 = itemRule.getItemStack();
            if (class_17994 == class_17992 || !((class_17923 = class_17994.getItem()) instanceof ArmorItem) || ((ArmorItemAccess)(class_17383 = (ArmorItem)class_17923)).rockstar$getType().getEquipmentSlot() != equipmentSlot) continue;
            n = Math.max(n, this.calculateItemScore(class_17994));
        }
        return this.calculateItemScore(class_17992) < n;
    }

    private boolean isInferiorCombatItem(ItemStack class_17992, ItemRuleCollection<ItemRule> itemRuleCollection) {
        ItemStack class_17993;
        int n;
        String string = this.getCombatItemType(class_17992);
        if (string == null) {
            return false;
        }
        int n2 = n = this.calculateCombatItemScore(class_17992);
        ItemStack class_17994 = InventoryCleaner.minecraftClient.player.getMainHandStack();
        if (string.equals(this.getCombatItemType(class_17994))) {
            n2 = Math.max(n2, this.calculateCombatItemScore(class_17994));
        }
        if (string.equals(this.getCombatItemType(class_17993 = InventoryCleaner.minecraftClient.player.getOffHandStack()))) {
            n2 = Math.max(n2, this.calculateCombatItemScore(class_17993));
        }
        for (ItemRule itemRule : itemRuleCollection.getRules()) {
            ItemStack class_17995 = itemRule.getItemStack();
            if (class_17995 == class_17992 || !string.equals(this.getCombatItemType(class_17995))) continue;
            n2 = Math.max(n2, this.calculateCombatItemScore(class_17995));
        }
        return n < n2;
    }

    private String getCombatItemType(ItemStack class_17992) {
        if (class_17992.isEmpty()) {
            return null;
        }
        Item class_17922 = class_17992.getItem();
        if (class_17922 instanceof SwordItem) {
            return "sword";
        }
        if (class_17922 instanceof AxeItem) {
            return "axe";
        }
        if (class_17922 instanceof BowItem) {
            return "bow";
        }
        if (class_17922 instanceof CrossbowItem) {
            return "crossbow";
        }
        if (class_17922 instanceof TridentItem) {
            return "trident";
        }
        if (class_17922 instanceof MaceItem) {
            return "mace";
        }
        return null;
    }

    private int calculateItemScore(ItemStack class_17992) {
        Object object = class_17992.getItem();
        if (!(object instanceof ArmorItem)) {
            return 0;
        }
        ArmorItem class_17382 = (ArmorItem)object;
        object = DonorItemParser.parseDonorItem(class_17992);
        if (object != null && "SunHelmet".equals(((DonorItemParser.DonorItem)object).getRawName())) {
            return Integer.MAX_VALUE;
        }
        ArmorMaterial class_17412 = ((ArmorItemAccess)class_17382).rockstar$getMaterial();
        EquipmentType class_80512 = ((ArmorItemAccess)class_17382).rockstar$getType();
        int n = class_17412.defense().getOrDefault(class_80512, 0);
        int n2 = (int)class_17412.toughness();
        int n3 = EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.PROTECTION);
        int n4 = EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.FIRE_PROTECTION) + EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.BLAST_PROTECTION) + EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.PROJECTILE_PROTECTION);
        int n5 = EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.FEATHER_FALLING) + EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.RESPIRATION) + EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.AQUA_AFFINITY) + EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.DEPTH_STRIDER) + EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.THORNS) + EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.UNBREAKING) + EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.MENDING);
        return n * 50 + n3 * 30 + n2 * 10 + n4 * 12 + n5 * 4 + this.getDurabilityPriority(class_17992);
    }

    private int calculateCombatItemScore(ItemStack class_17992) {
        Item class_17922 = class_17992.getItem();
        int n = this.getItemCategoryPriority(class_17922);
        if (DonorItemParser.parseDonorItem(class_17992) != null) {
            n += 1000;
        }
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.SHARPNESS) * 30;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.POWER) * 30;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.DENSITY) * 30;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.BREACH) * 24;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.SMITE) * 12;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.BANE_OF_ARTHROPODS) * 12;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.IMPALING) * 18;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.FIRE_ASPECT) * 14;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.KNOCKBACK) * 8;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.LOOTING) * 10;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.SWEEPING_EDGE) * 8;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.PUNCH) * 8;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.FLAME) * 12;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.INFINITY) * 16;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.MULTISHOT) * 16;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.QUICK_CHARGE) * 14;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.PIERCING) * 10;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.LOYALTY) * 8;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.RIPTIDE) * 10;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.WIND_BURST) * 18;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.UNBREAKING) * 4;
        n += EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.MENDING) * 6;
        return n += this.getDurabilityPriority(class_17992);
    }

    private int getItemCategoryPriority(Item class_17922) {
        if (class_17922 == Items.NETHERITE_SWORD || class_17922 == Items.NETHERITE_AXE || class_17922 == Items.MACE) {
            return 90;
        }
        if (class_17922 == Items.DIAMOND_SWORD || class_17922 == Items.DIAMOND_AXE || class_17922 == Items.TRIDENT) {
            return 80;
        }
        if (class_17922 == Items.IRON_SWORD || class_17922 == Items.IRON_AXE || class_17922 == Items.CROSSBOW) {
            return 70;
        }
        if (class_17922 == Items.STONE_SWORD || class_17922 == Items.STONE_AXE || class_17922 == Items.BOW) {
            return 60;
        }
        if (class_17922 == Items.GOLDEN_SWORD || class_17922 == Items.GOLDEN_AXE) {
            return 50;
        }
        if (class_17922 == Items.WOODEN_SWORD || class_17922 == Items.WOODEN_AXE) {
            return 40;
        }
        return 0;
    }

    private int getDurabilityPriority(ItemStack class_17992) {
        if (!class_17992.isDamageable()) {
            return 0;
        }
        return (int)((double)(class_17992.getMaxDamage() - class_17992.getDamage()) / (double)class_17992.getMaxDamage() * 10.0);
    }
}
