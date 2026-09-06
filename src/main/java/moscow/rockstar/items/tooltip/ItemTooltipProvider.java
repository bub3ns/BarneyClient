/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ItemStack
 */
package moscow.rockstar.items.tooltip;

import lombok.Generated;
import moscow.rockstar.items.catalog.ItemCatalog;
import moscow.rockstar.items.config.ItemConfigProcessor;
import net.minecraft.item.ItemStack;

public class ItemTooltipProvider {
    private ItemStack trackedStack;
    private ItemConfigProcessor.ItemDefinition trackedConfig;

    public void setTrackedConfig(ItemConfigProcessor.ItemDefinition itemDefinition) {
        this.trackedStack = itemDefinition.getStack();
        this.trackedConfig = itemDefinition;
    }

    public void clearTrackedConfig() {
        this.trackedStack = null;
        this.trackedConfig = null;
    }

    public void onStackChanged(ItemStack class_17992) {
        if (this.trackedStack != null && ItemStack.areItemsAndComponentsEqual((ItemStack)this.trackedStack, (ItemStack)class_17992)) {
            this.clearTrackedConfig();
        }
    }

    public String getDisplayName() {
        return this.trackedConfig != null ? this.trackedConfig.getCustomName() : null;
    }

    public String getItemId() {
        return this.trackedConfig != null ? this.trackedConfig.getItemId() : null;
    }

    public boolean matchesTrackedConfig(ItemConfigProcessor.ItemDefinition itemDefinition) {
        return this.trackedConfig != null && this.trackedConfig.getItemId() != null && this.trackedConfig.getItemId().equals(itemDefinition.getItemId());
    }

    public boolean isConfiguredItem(ItemConfigProcessor.ItemDefinition itemDefinition) {
        for (ItemCatalog.CatalogEntry catalogEntry : ItemCatalog.getEntries()) {
            if (catalogEntry.getId() == null || !catalogEntry.getId().equals(itemDefinition.getItemId())) continue;
            return true;
        }
        return false;
    }

    @Generated
    public ItemStack getTrackedStack() {
        return this.trackedStack;
    }

    @Generated
    public ItemConfigProcessor.ItemDefinition getTrackedConfig() {
        return this.trackedConfig;
    }
}

