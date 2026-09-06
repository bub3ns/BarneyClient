/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.inventory.selection;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.inventory.selection.SelectionProgress;
import moscow.rockstar.items.donor.DonorItemSelector;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRulePredicate;
import moscow.rockstar.items.rules.ItemRuleResolver;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

public class ItemSelectionState
implements ClientAccess {
    private static final int MAX_RETRY_COUNT = 5;
    private final ItemRuleResolver itemResolver;
    private final ItemRulePredicate selectionAction;
    private SelectionProgress selectionState = SelectionProgress.NOT_STARTED;
    private ItemStack originalStack = ItemStack.EMPTY;
    private int selectedSlot = -1;
    private int retryCount = 0;
    private boolean cancelActiveUse = true;
    @Nullable
    private SelectionCallback selectionCallback;
    private boolean callbackUpdated;

    public void setSelectionCallback(@Nullable SelectionCallback selectionCallback) {
        this.selectionCallback = selectionCallback;
    }

    public ItemSelectionState(ItemRuleResolver itemRuleResolver, ItemRulePredicate itemRulePredicate) {
        this.itemResolver = Objects.requireNonNull(itemRuleResolver);
        this.selectionAction = Objects.requireNonNull(itemRulePredicate);
    }

    public ItemSelectionState setCancelActiveUse(boolean bl) {
        this.cancelActiveUse = bl;
        return this;
    }

    public int countMatchingItems(Item class_17922) {
        return this.itemResolver.countMatchingItems(class_17922);
    }

    public boolean isSelectionComplete() {
        return this.selectionState == SelectionProgress.COMPLETED;
    }

    public void resetSelection() {
        this.selectionState = SelectionProgress.NOT_STARTED;
        this.originalStack = ItemStack.EMPTY;
        this.selectedSlot = -1;
        this.retryCount = 0;
        this.callbackUpdated = false;
    }

    public void updateSelection(Item class_17922, boolean bl, Predicate<ItemStack> predicate, BooleanSupplier booleanSupplier) {
        boolean bl2;
        if (ItemSelectionState.minecraftClient.player == null || ItemSelectionState.minecraftClient.world == null || class_17922 == null) {
            return;
        }
        ItemStack class_17992 = ItemSelectionState.minecraftClient.player.getOffHandStack();
        boolean bl3 = bl2 = class_17992.getItem() == class_17922;
        if (bl) {
            if (!booleanSupplier.getAsBoolean()) {
                return;
            }
            this.selectReplacement(class_17922, bl2, class_17992, predicate);
            return;
        }
        if (this.selectionState == SelectionProgress.NOT_STARTED) {
            return;
        }
        if (!booleanSupplier.getAsBoolean()) {
            return;
        }
        this.restoreOrTrackStack(class_17992);
    }

    private void selectReplacement(Item class_17922, boolean bl, ItemStack class_17992, Predicate<ItemStack> predicate) {
        if (bl && class_17922 == Items.TOTEM_OF_UNDYING) {
            int n = DonorItemSelector.getItemPriority(class_17992);
            ItemRule itemRule = this.itemResolver.findBestByItem(class_17922);
            if (itemRule == null) {
                this.selectionState = SelectionProgress.COMPLETED;
                return;
            }
            int n2 = DonorItemSelector.getItemPriority(itemRule.getItemStack());
            if (n2 >= n) {
                this.selectionState = SelectionProgress.COMPLETED;
                return;
            }
            if (this.originalStack.isEmpty()) {
                this.originalStack = class_17992.copy();
            }
            this.selectedSlot = -1;
            this.stopUsingItem();
            if (this.selectionAction.applySelectionAndConfirm(itemRule)) {
                this.selectionState = SelectionProgress.COMPLETED;
                if (this.selectionCallback != null && ItemSelectionState.minecraftClient.player != null && class_17922 == Items.TOTEM_OF_UNDYING) {
                    this.selectionCallback.setCallbackValue(ItemSelectionState.minecraftClient.player.getHealth() + ItemSelectionState.minecraftClient.player.getAbsorptionAmount());
                }
            }
            return;
        }
        if (bl) {
            this.selectionState = SelectionProgress.COMPLETED;
            return;
        }
        if (!predicate.test(class_17992)) {
            return;
        }
        ItemRule itemRule = this.itemResolver.findBestByItem(class_17922);
        if (itemRule == null) {
            return;
        }
        this.rememberOriginalStack(class_17992);
        this.selectedSlot = itemRule.getClickSlot();
        this.stopUsingItem();
        if (this.selectionAction.applySelectionAndConfirm(itemRule)) {
            this.selectionState = SelectionProgress.COMPLETED;
            if (this.selectionCallback != null && ItemSelectionState.minecraftClient.player != null && class_17922 == Items.TOTEM_OF_UNDYING) {
                this.selectionCallback.setCallbackValue(ItemSelectionState.minecraftClient.player.getHealth() + ItemSelectionState.minecraftClient.player.getAbsorptionAmount());
            }
        }
    }

    private void restoreOrTrackStack(ItemStack class_17992) {
        int n;
        int n2;
        if (this.originalStack.isEmpty()) {
            ItemRule itemRule = this.itemResolver.findByItem(Items.TOTEM_OF_UNDYING);
            if (itemRule != null && class_17992.getItem() == Items.TOTEM_OF_UNDYING) {
                int n3 = DonorItemSelector.getItemPriority(class_17992);
                int n4 = DonorItemSelector.getItemPriority(itemRule.getItemStack());
                if (n4 > n3) {
                    this.stopUsingItem();
                    this.selectionAction.applySelectionAndConfirm(itemRule);
                }
            }
            this.resetSelection();
            return;
        }
        if (ItemStack.areEqual((ItemStack)class_17992, (ItemStack)this.originalStack)) {
            this.resetSelection();
            return;
        }
        if (class_17992.getItem() == Items.TOTEM_OF_UNDYING && this.originalStack.getItem() == Items.TOTEM_OF_UNDYING && (n2 = DonorItemSelector.getItemPriority(class_17992)) >= (n = DonorItemSelector.getItemPriority(this.originalStack))) {
            this.resetSelection();
            return;
        }
        if (this.selectionState != SelectionProgress.RESTORING) {
            this.selectionState = SelectionProgress.RESTORING;
            this.retryCount = 5;
            this.callbackUpdated = false;
        }
        if (this.retryCount-- <= 0) {
            this.resetSelection();
            return;
        }
        ItemRule itemRule = this.getSelectedRule();
        if (itemRule == null) {
            this.resetSelection();
            return;
        }
        this.stopUsingItem();
        if (this.selectionAction.applySelectionAndConfirm(itemRule) && this.selectionCallback != null && !this.callbackUpdated && !this.originalStack.isEmpty()) {
            this.selectionCallback.setCallbackStack(this.originalStack);
            this.callbackUpdated = true;
        }
    }

    private void rememberOriginalStack(ItemStack class_17992) {
        if (this.originalStack.isEmpty()) {
            this.originalStack = class_17992.copy();
        }
    }

    private void stopUsingItem() {
        if (this.cancelActiveUse && ItemSelectionState.minecraftClient.player.isUsingItem()) {
            ItemSelectionState.minecraftClient.player.stopUsingItem();
        }
    }

    @Nullable
    private ItemRule getSelectedRule() {
        ItemRule itemRule;
        if (this.selectedSlot != -1 && (itemRule = this.itemResolver.findByClickSlot(this.selectedSlot)) != null) {
            if (ItemStack.areEqual((ItemStack)itemRule.getItemStack(), (ItemStack)this.originalStack)) {
                return itemRule;
            }
            this.selectedSlot = -1;
        }
        return this.itemResolver.findByStack(this.originalStack);
    }

    public static interface SelectionCallback {
        public void setCallbackValue(float var1);

        public void setCallbackStack(ItemStack var1);
    }
}

