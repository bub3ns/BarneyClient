/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.items.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.items.rules.ItemRule;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemRuleCollection<T extends ItemRule> {
    protected final List<T> rules;

    public ItemRuleCollection(List<T> list) {
        this.rules = list;
    }

    @Nullable
    public T findByItem(Item class_17922) {
        return (T)((ItemRule)this.rules.stream().filter(itemRule -> itemRule.matchesItem(class_17922)).findFirst().orElse(null));
    }

    @Nullable
    public T findByStack(Predicate<ItemStack> predicate) {
        return (T)((ItemRule)this.rules.stream().filter(itemRule -> itemRule.matchesStack(predicate)).findFirst().orElse(null));
    }

    public List<@Nullable T> findAllByItem(Item class_17922) {
        return this.rules.stream().filter(itemRule -> itemRule.matchesItem(class_17922)).toList();
    }

    public List<@Nullable T> findAllByStack(Predicate<ItemStack> predicate) {
        return this.rules.stream().filter(itemRule -> itemRule.matchesStack(predicate)).toList();
    }

    @Nullable
    public T findFirstEmpty() {
        return (T)((ItemRule)this.rules.stream().filter(ItemRule::isEmpty).findFirst().orElse(null));
    }

    public boolean containsItem(Item class_17922) {
        return this.rules.stream().anyMatch(itemRule -> itemRule.matchesItem(class_17922));
    }

    public int countItems(Item class_17922) {
        return this.rules.stream().filter(itemRule -> itemRule.matchesItem(class_17922)).mapToInt(itemRule -> itemRule.getItemStack().getCount()).sum();
    }

    public ItemRuleCollection<ItemRule> combineRules(ItemRuleCollection<? extends ItemRule> itemRuleCollection) {
        ArrayList<ItemRule> arrayList = new ArrayList<ItemRule>(this.rules.size() + itemRuleCollection.rules.size());
        arrayList.addAll(this.rules);
        arrayList.addAll(itemRuleCollection.rules);
        return new ItemRuleCollection<ItemRule>(arrayList);
    }

    public ItemRuleCollection<ItemRule> appendRule(ItemRule itemRule) {
        ArrayList<ItemRule> arrayList = new ArrayList<ItemRule>(this.rules);
        arrayList.add(itemRule);
        return new ItemRuleCollection<ItemRule>(arrayList);
    }

    @Generated
    public List<T> getRules() {
        return this.rules;
    }
}
