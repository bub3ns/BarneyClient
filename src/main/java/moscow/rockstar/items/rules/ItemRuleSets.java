/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.items.rules;

import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.items.rules.ArmorSlotRule;
import moscow.rockstar.items.rules.ArmorSlotRules;
import moscow.rockstar.items.rules.HotbarSlotRules;
import moscow.rockstar.items.rules.InventorySlotRule;
import moscow.rockstar.items.rules.InventorySlotRules;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.OffhandRule;
import moscow.rockstar.items.rules.OffhandRules;

public class ItemRuleSets {
    private ItemRuleSets() {
    }

    public static ItemRuleCollection<HotbarSlot> getHotbarRules() {
        return new HotbarSlotRules();
    }

    public static ItemRuleCollection<InventorySlotRule> getInventoryRules() {
        return new InventorySlotRules();
    }

    public static ItemRuleCollection<ArmorSlotRule> getArmorRules() {
        return new ArmorSlotRules();
    }

    public static ItemRuleCollection<OffhandRule> getOffhandRules() {
        return new OffhandRules();
    }
}

