/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ArmorItem
 *  net.minecraft.Item
 *  net.minecraft.Item$TooltipContext
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.TooltipType
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.World
 *  net.minecraft.Text
 *  net.minecraft.RegistryKey
 *  net.minecraft.EquipmentType
 */
package moscow.rockstar.modules.player.inventory;

import java.util.Comparator;
import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.items.rules.ArmorSlotRule;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.player.inventory.ArmorSwapResult;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.api.access.ArmorItemAccess;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.ItemNotification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.item.equipment.EquipmentType;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.window.KeyPressEvent;
import pyrock.events.window.MouseEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Boots Swap", category=ModuleCategory.PLAYER, description="modules.descriptions.boots_swap")
public class BootsSwap
extends Module {
    private BooleanSetting automatic;
    private IntegerSetting swapKey;
    private ArmorSwapResult swapResult;
    private boolean swapActive = false;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        ItemStack mainHandStack;
        if (this.automatic.isEnabled()) {
            ArmorSlotRule armorSlotRule;
            boolean bl;
            mainHandStack = BootsSwap.minecraftClient.player.getMainHandStack();
            boolean bl2 = bl = mainHandStack.getItem() == Items.MACE;
            if (bl && !this.swapActive) {
                ArmorSlotRule armorSlotRule2 = InventoryUtils.bootsRule();
                if (!this.isItemValid(((ItemRule)armorSlotRule2).getItemStack())) {
                    this.prepareSwap();
                }
            } else if (!bl && this.swapActive && this.isItemValid(((ItemRule)(armorSlotRule = InventoryUtils.bootsRule())).getItemStack())) {
                this.finishSwap();
            }
            this.swapActive = bl;
        }
        InventoryMove inventoryMove = RockstarClient.create().getModuleRegistry().getModule(InventoryMove.class);
        if (this.swapResult != null) {
            if (this.swapResult.selectedItemRule.getClickSlot() >= 36 && this.swapResult.selectedItemRule.getClickSlot() <= 44) {
                InventoryUtils.dropItem(this.swapResult.equippedArmorRule.getClickSlot(), this.swapResult.selectedItemRule.getClickSlot() - 36);
                this.swapResult = null;
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
    };
    private final EventListener<KeyPressEvent> onKeyPressEvent = keyPressEvent -> {
        if (this.swapKey.isIntValid(keyPressEvent.getKey()) && keyPressEvent.getAction() == 1 && BootsSwap.minecraftClient.currentScreen == null) {
            this.resetSwap();
        }
    };
    private final EventListener<MouseEvent> onMouseEvent = mouseEvent -> {
        if (this.swapKey.isIntValid(mouseEvent.getButton()) && mouseEvent.getAction() == 1 && BootsSwap.minecraftClient.currentScreen == null) {
            this.resetSwap();
        }
    };

    public BootsSwap() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.automatic = new BooleanSetting(this, "modules.settings.boots_swap.automatic");
        this.swapKey = new IntegerSetting(this, "modules.settings.boots_swap.swap_key", () -> this.automatic.isEnabled());
    }

    private void resetSwap() {
        ArmorSlotRule armorSlotRule = InventoryUtils.bootsRule();
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules());
        boolean bl = this.isItemValid(((ItemRule)armorSlotRule).getItemStack());
        ItemRule itemRule = bl ? this.getItemRule(itemRuleCollection, false) : this.getItemRule(itemRuleCollection, true);
        if (this.swapResult != null || itemRule == null) {
            return;
        }
        this.swapResult = new ArmorSwapResult(itemRule, armorSlotRule);
        boolean bl2 = this.isItemValid(itemRule.getItemStack());
        String string = bl2 ? "\u041f\u043e\u043f\u0440\u044b\u0433\u0443\u043d" : "\u041e\u0431\u044b\u0447\u043d\u044b\u0435 \u0431\u043e\u0442\u0438\u043d\u043a\u0438";
        ColorRGBA colorRGBA = ColorPalette.getAccentColor();
        RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translateFormatted("alerts.equipped", string), itemRule.getItemStack()).withHighlightedText(string).withHighlightColor(colorRGBA));
    }

    private void prepareSwap() {
        ArmorSlotRule armorSlotRule = InventoryUtils.bootsRule();
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules());
        ItemRule itemRule = this.getItemRule(itemRuleCollection, true);
        if (this.swapResult != null || itemRule == null) {
            return;
        }
        this.swapResult = new ArmorSwapResult(itemRule, armorSlotRule);
        RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translateFormatted("alerts.equipped", "\u041f\u043e\u043f\u0440\u044b\u0433\u0443\u043d"), itemRule.getItemStack()).withHighlightedText("\u041f\u043e\u043f\u0440\u044b\u0433\u0443\u043d").withHighlightColor(ColorPalette.getAccentColor()));
    }

    private void finishSwap() {
        ArmorSlotRule armorSlotRule = InventoryUtils.bootsRule();
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules());
        ItemRule itemRule = this.getItemRule(itemRuleCollection, false);
        if (this.swapResult != null || itemRule == null) {
            return;
        }
        this.swapResult = new ArmorSwapResult(itemRule, armorSlotRule);
        RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translateFormatted("alerts.equipped", "\u041e\u0431\u044b\u0447\u043d\u044b\u0435 \u0431\u043e\u0442\u0438\u043d\u043a\u0438"), itemRule.getItemStack()).withHighlightedText("\u041e\u0431\u044b\u0447\u043d\u044b\u0435 \u0431\u043e\u0442\u0438\u043d\u043a\u0438").withHighlightColor(ColorPalette.getAccentColor()));
    }

    private ItemRule getItemRule(ItemRuleCollection<ItemRule> itemRuleCollection, boolean bl) {
        List<ItemRule> list = itemRuleCollection.findAllByStack(class_17992 -> {
            ArmorItem class_17382;
            Item class_17922 = class_17992.getItem();
            return class_17922 instanceof ArmorItem && ((ArmorItemAccess)(class_17382 = (ArmorItem)class_17922)).rockstar$getType() == EquipmentType.BOOTS && this.isItemValid((ItemStack)class_17992) == bl;
        });
        if (list.isEmpty()) {
            return null;
        }
        return list.stream().max(Comparator.comparingInt(itemRule -> this.getBootsScore(itemRule.getItemStack()))).orElse(null);
    }

    private int getBootsScore(ItemStack class_17992) {
        Item class_17922 = class_17992.getItem();
        if (!(class_17922 instanceof ArmorItem)) {
            return 0;
        }
        ArmorItem class_17382 = (ArmorItem)class_17922;
        ArmorMaterial armorMaterial = ((ArmorItemAccess)class_17382).rockstar$getMaterial();
        EquipmentType class_80512 = ((ArmorItemAccess)class_17382).rockstar$getType();
        int n = armorMaterial.defense().getOrDefault(class_80512, 0);
        int n2 = (int)armorMaterial.toughness();
        int n3 = EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.PROTECTION);
        return n * 5 + n3 * 3 + n2;
    }

    private boolean isItemValid(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return false;
        }
        String string = class_17992.getName().getString();
        if (string.contains("\u041f\u043e\u043f\u0440\u044b\u0433\u0443\u043d")) {
            return true;
        }
        try {
            List<Text> tooltipLines = class_17992.getTooltip(Item.TooltipContext.create((World)BootsSwap.minecraftClient.world), (PlayerEntity)BootsSwap.minecraftClient.player, (TooltipType)TooltipType.BASIC);
            for (Text class_25612 : tooltipLines) {
                String string2 = class_25612.getString();
                if (!string2.contains("\u041f\u043e\u043f\u0440\u044b\u0433\u0443\u043d")) continue;
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    @Override
    public void onDisable() {
        this.swapActive = false;
    }
}
