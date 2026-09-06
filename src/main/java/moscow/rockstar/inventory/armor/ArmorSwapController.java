/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.EquippableComponent
 *  net.minecraft.EquipmentSlot
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.EntityAttributes
 *  net.minecraft.RegistryKey
 *  net.minecraft.AttributeModifiersComponent
 *  net.minecraft.AttributeModifiersComponent$Entry
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.inventory.armor;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.items.rules.ArmorSlotRule;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.util.Timer;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.DataComponentTypes;
import pyrock.events.player.ClientPlayerTickEvent;

public final class ArmorSwapController
implements ClientAccess {
    private static final long SWAP_DELAY_MILLIS = 400L;
    private static ArmorSwapController INSTANCE;
    private ArmorSwapMode swapMode = ArmorSwapMode.IDLE;
    private PendingArmorSwap pendingSwap;
    private final Timer swapCooldown = new Timer();
    private final Timer completionCooldown = new Timer();
    private final EventListener<ClientPlayerTickEvent> playerTickListener = clientPlayerTickEvent -> {
        ItemRule itemRule;
        boolean bl;
        boolean bl2;
        this.completePendingSwap();
        if (this.pendingSwap != null || this.swapMode == ArmorSwapMode.IDLE || ArmorSwapController.minecraftClient.player == null) {
            return;
        }
        if (ArmorSwapController.minecraftClient.currentScreen != null) {
            return;
        }
        if (!RockstarClient.create().getModuleRegistry().getModule(InventoryMove.class).getModeEntries().isEmpty()) {
            return;
        }
        if (!this.swapCooldown.hasElapsed(400L)) {
            return;
        }
        ArmorSlotRule armorSlotRule = InventoryUtils.chestplateRule();
        boolean bl3 = bl2 = armorSlotRule.getItem() == Items.ELYTRA;
        boolean bl4 = this.swapMode == ArmorSwapMode.EQUIP_ELYTRA ? bl2 : !bl2;
        bl = bl4;
        if (bl) {
            this.swapMode = ArmorSwapMode.IDLE;
            return;
        }
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules());
        ItemRule itemRule2 = itemRule = this.swapMode == ArmorSwapMode.EQUIP_ELYTRA ? this.findMatchingArmor(itemRuleCollection) : this.findBestArmor(itemRuleCollection);
        if (itemRule == null) {
            this.swapMode = ArmorSwapMode.IDLE;
            return;
        }
        this.pendingSwap = new PendingArmorSwap(itemRule, armorSlotRule);
        this.swapCooldown.reset();
    };

    private ArmorSwapController() {
    }

    public static ArmorSwapController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ArmorSwapController();
            RockstarClient.create().getEventBus().registerListeners(INSTANCE);
        }
        return INSTANCE;
    }

    public boolean isSwapPending() {
        return this.pendingSwap != null || this.swapMode != ArmorSwapMode.IDLE;
    }

    public boolean hasCooldownElapsed(long l) {
        return this.completionCooldown.hasElapsed(l);
    }

    public void beginArmorSwap() {
        this.swapMode = ArmorSwapMode.EQUIP_ELYTRA;
        this.swapCooldown.setLastResetTimeMillis(0L);
    }

    public void endArmorSwap() {
        this.swapMode = ArmorSwapMode.RESTORE_ARMOR;
        this.swapCooldown.setLastResetTimeMillis(0L);
    }

    private void completePendingSwap() {
        if (this.pendingSwap == null || ArmorSwapController.minecraftClient.player == null) {
            return;
        }
        int n = this.pendingSwap.targetArmor.getClickSlot();
        if (n >= 36 && n <= 44) {
            InventoryUtils.dropItem(this.pendingSwap.previousArmor.getClickSlot(), n - 36);
            this.pendingSwap = null;
            this.completionCooldown.reset();
            return;
        }
        if (!RockstarClient.create().getModuleRegistry().getModule(InventoryMove.class).getModeEntries().isEmpty()) {
            return;
        }
        if (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)) {
            if (this.pendingSwap.swapStep == 0) {
                InventoryUtils.dropItem(n, 8);
                InventoryUtils.dropItem(this.pendingSwap.previousArmor.getClickSlot(), 8);
                InventoryUtils.dropItem(n, 8);
            }
        } else if (this.pendingSwap.swapStep == 0) {
            InventoryUtils.dropItem(n, 8);
        } else if (this.pendingSwap.swapStep == 1) {
            InventoryUtils.dropItem(this.pendingSwap.previousArmor.getClickSlot(), 8);
        } else if (this.pendingSwap.swapStep == 2) {
            InventoryUtils.dropItem(n, 8);
        }
        if (++this.pendingSwap.swapStep >= 3) {
            this.pendingSwap = null;
            this.completionCooldown.reset();
        }
    }

    private ItemRule findMatchingArmor(ItemRuleCollection<ItemRule> itemRuleCollection) {
        return itemRuleCollection.findByStack(class_17992 -> class_17992.getItem() == Items.ELYTRA && !class_17992.willBreakNextUse());
    }

    private ItemRule findBestArmor(ItemRuleCollection<ItemRule> itemRuleCollection) {
        ItemRule itemRule = null;
        double d = -1.0;
        for (ItemRule itemRule2 : itemRuleCollection.getRules()) {
            double d2;
            EquippableComponent class_101922;
            ItemStack class_17992 = itemRule2.getItemStack();
            if (class_17992.isEmpty() || class_17992.getItem() == Items.ELYTRA || (class_101922 = (EquippableComponent)class_17992.get(DataComponentTypes.EQUIPPABLE)) == null || class_101922.slot() != EquipmentSlot.CHEST || !((d2 = this.getArmorProtection(class_17992) + (double)EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.PROTECTION) * 0.6) > d)) continue;
            d = d2;
            itemRule = itemRule2;
        }
        return itemRule;
    }

    private double getArmorProtection(ItemStack class_17992) {
        AttributeModifiersComponent class_92852 = (AttributeModifiersComponent)class_17992.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (class_92852 == null) {
            return 0.0;
        }
        double d = 0.0;
        for (AttributeModifiersComponent.Entry class_92872 : class_92852.modifiers()) {
            if (class_92872.attribute() != EntityAttributes.ARMOR && class_92872.attribute() != EntityAttributes.ARMOR_TOUGHNESS) continue;
            d += class_92872.modifier().value();
        }
        return d;
    }

    static enum ArmorSwapMode {
        IDLE,
        EQUIP_ELYTRA,
        RESTORE_ARMOR;
}

    static final class PendingArmorSwap {
        int swapStep;
        final ItemRule targetArmor;
        final ItemRule previousArmor;

        PendingArmorSwap(ItemRule itemRule, ItemRule itemRule2) {
            this.targetArmor = itemRule;
            this.previousArmor = itemRule2;
        }
    }
}

