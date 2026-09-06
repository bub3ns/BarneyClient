/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.items.rules;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.items.rules.InventorySlotRule;
import moscow.rockstar.items.rules.ItemRuleCollection;

public class InventorySlotRules
extends ItemRuleCollection<InventorySlotRule> {
    public InventorySlotRules() {
        super(InventorySlotRules.createRules());
    }

    private static List<InventorySlotRule> createRules() {
        ArrayList<InventorySlotRule> arrayList = new ArrayList<InventorySlotRule>();
        for (int i = 0; i < 27; ++i) {
            arrayList.add(new InventorySlotRule(i));
        }
        return arrayList;
    }
}

