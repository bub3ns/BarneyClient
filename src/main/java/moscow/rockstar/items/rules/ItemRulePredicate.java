/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.items.rules;

import moscow.rockstar.items.rules.ItemRule;

public interface ItemRulePredicate {
    public boolean applySelection(ItemRule var1);

    public boolean applySelectionAndConfirm(ItemRule var1);
}

