/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 */
package moscow.rockstar.modules.player.inventory;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.ItemNotification;
import net.minecraft.item.ItemStack;
import pyrock.events.game.PickupEvent;
import pyrock.events.window.ContainerClickEvent;

@ModuleInfo(name="Item Pickup", category=ModuleCategory.OTHER, disableLocked=true, description="modules.descriptions.item_pickup")
public class ItemPickup
extends Module {
    private final EventListener<PickupEvent> pickupListener = pickupEvent -> {
        ItemStack class_17992 = pickupEvent.getItemStack();
        if (pickupEvent.getEntity() != ItemPickup.minecraftClient.player) {
            return;
        }
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        if (donorItem != null) {
            String string = donorItem.getDisplayName(class_17992);
            RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translate("alerts.donate_picked") + string, class_17992).withHighlightedText(string).withHighlightColor(donorItem.getDisplayColor(class_17992)));
        }
    };
    private final EventListener<ContainerClickEvent> containerClickListener = containerClickEvent -> {};
}

