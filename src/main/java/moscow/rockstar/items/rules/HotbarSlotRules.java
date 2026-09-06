/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.items.rules;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.items.rules.ItemRuleCollection;

public class HotbarSlotRules
extends ItemRuleCollection<HotbarSlot> {
    public HotbarSlotRules() {
        super(HotbarSlotRules.createRules());
    }

    private static List<HotbarSlot> createRules() {
        ArrayList<HotbarSlot> arrayList = new ArrayList<HotbarSlot>();
        for (int i = 0; i < 9; ++i) {
            arrayList.add(new HotbarSlot(i));
        }
        return arrayList;
    }
}

