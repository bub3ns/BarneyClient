/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.items.rules;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.items.rules.ArmorSlotRule;
import moscow.rockstar.items.rules.ItemRuleCollection;

public class ArmorSlotRules
extends ItemRuleCollection<ArmorSlotRule> {
    public ArmorSlotRules() {
        super(ArmorSlotRules.createRules());
    }

    private static List<ArmorSlotRule> createRules() {
        ArrayList<ArmorSlotRule> arrayList = new ArrayList<ArmorSlotRule>();
        for (int i = 0; i < 4; ++i) {
            arrayList.add(new ArmorSlotRule(i));
        }
        return arrayList;
    }
}

