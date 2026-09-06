/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.items.rules;

import java.util.Comparator;
import moscow.rockstar.items.donor.DonorItemSelector;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleResolver;
import moscow.rockstar.items.rules.ItemRuleSets;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

public class ItemRuleResolverRegistry
implements ItemRuleResolver {
    private static final ItemRuleCollection<ItemRule> standardRules = ItemRuleSets.getOffhandRules().combineRules(ItemRuleSets.getInventoryRules()).combineRules(ItemRuleSets.getHotbarRules());
    private static final ItemRuleCollection<ItemRule> matchingRules = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules()).combineRules(ItemRuleSets.getOffhandRules());
    private static final ItemRuleCollection<ItemRule> specialRules = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules());
    private static final Comparator<ItemRule> itemQualityComparator = Comparator.comparingInt(itemRule -> DonorItemSelector.getItemPriority(itemRule.getItemStack()));

    @Override
    @Nullable
    public ItemRule findByItem(Item class_17922) {
        if (class_17922 == Items.TOTEM_OF_UNDYING) {
            return specialRules.findAllByItem(class_17922).stream().max(itemQualityComparator).orElse(null);
        }
        return standardRules.findAllByItem(class_17922).stream().findFirst().orElse(null);
    }

    @Override
    @Nullable
    public ItemRule findBestByItem(Item class_17922) {
        if (class_17922 == Items.TOTEM_OF_UNDYING) {
            return specialRules.findAllByItem(class_17922).stream().min(itemQualityComparator).orElse(null);
        }
        return specialRules.findAllByItem(class_17922).stream().findFirst().orElse(null);
    }

    @Override
    @Nullable
    public ItemRule findByStack(ItemStack class_17992) {
        return matchingRules.getRules().stream().filter(itemRule -> ItemStack.areEqual((ItemStack)itemRule.getItemStack(), (ItemStack)class_17992)).findFirst().orElse(null);
    }

    @Override
    @Nullable
    public ItemRule findByClickSlot(int n) {
        return standardRules.getRules().stream().filter(itemRule -> itemRule.getClickSlot() == n).findFirst().orElse(null);
    }

    @Override
    public int countMatchingItems(Item class_17922) {
        return ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules()).combineRules(ItemRuleSets.getOffhandRules()).findAllByItem(class_17922).size();
    }
}

