/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.items.rules;

import moscow.rockstar.items.rules.ItemRule;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ItemRuleResolver {
    @Nullable
    public ItemRule findByItem(Item var1);

    @Nullable
    public ItemRule findBestByItem(Item var1);

    @Nullable
    public ItemRule findByStack(ItemStack var1);

    @Nullable
    public ItemRule findByClickSlot(int var1);

    public int countMatchingItems(Item var1);
}

