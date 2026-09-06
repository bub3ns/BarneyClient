/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.StatusEffect
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.Entity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ItemConvertible
 *  net.minecraft.World
 *  net.minecraft.EntityStatusS2CPacket
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.modules.player.tracking;

import java.util.List;
import java.util.Set;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.items.recipes.RecipeItemResolver;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.ui.notifications.ItemNotification;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemConvertible;
import net.minecraft.world.World;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.component.DataComponentTypes;
import pyrock.events.game.FinishEatEvent;
import pyrock.events.network.ReceivePacketEvent;

@ModuleInfo(name="Tracker", category=ModuleCategory.PLAYER)
public class Tracker
extends Module {
    private static final Set<Item> trackedConsumableItems = Set.of(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.CHORUS_FRUIT, Items.POTION);
    private final EventListener<FinishEatEvent> finishEatListener = finishEatEvent -> {
        ItemStack class_17992 = finishEatEvent.getStack();
        if (!trackedConsumableItems.contains(class_17992.getItem()) || finishEatEvent.getUser() == Tracker.minecraftClient.player) {
            return;
        }
        String string = class_17992.getItem() == Items.POTION ? this.formatPotionName(class_17992) : class_17992.getName().getString();
        RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(finishEatEvent.getUser().getName().getString() + (class_17992.getItem() == Items.POTION ? " \u0432\u044b\u043f\u0438\u043b " : " \u0441\u044a\u0435\u043b ") + string, class_17992).withHighlightedText(string));
    };
    private final EventListener<ReceivePacketEvent> entityStatusListener = receivePacketEvent -> {
        if (!(receivePacketEvent.getPacket() instanceof EntityStatusS2CPacket statusPacket) || statusPacket.getStatus() != 35 || Tracker.minecraftClient.world == null) {
            return;
        }
        Entity entity = statusPacket.getEntity((World)Tracker.minecraftClient.world);
        if (entity == null || entity == Tracker.minecraftClient.player || !(entity instanceof PlayerEntity player)) {
            return;
        }
        ItemStack totemStack = player.getOffHandStack().contains(DataComponentTypes.DEATH_PROTECTION) ? player.getOffHandStack().copy() : (player.getMainHandStack().contains(DataComponentTypes.DEATH_PROTECTION) ? player.getMainHandStack().copy() : new ItemStack((ItemConvertible)Items.TOTEM_OF_UNDYING));
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(totemStack);
        String itemName = donorItem != null ? donorItem.getDisplayName(totemStack) : totemStack.getName().getString();
        ItemNotification itemNotification = new ItemNotification(player.getName().getString() + " \u043f\u043e\u0442\u0435\u0440\u044f\u043b " + itemName, totemStack).withHighlightedText(itemName);
        if (donorItem != null) {
            itemNotification.withHighlightColor(donorItem.getDisplayColor(totemStack));
        }
        RockstarClient.create().getUiComponentProcessor().enqueueNotification(itemNotification);
    };

    private String formatPotionName(ItemStack class_17992) {
        List<StatusEffectInstance> list = RecipeItemResolver.getEffects(class_17992);
        if (list.isEmpty()) {
            return "\u0417\u0435\u043b\u044c\u0435";
        }
        return "\u0417\u0435\u043b\u044c\u0435 " + list.getFirst().getEffectType().value().getName().getString().toLowerCase();
    }
}
