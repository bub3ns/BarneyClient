/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.EnchantmentHelper
 *  net.minecraft.Registry
 *  net.minecraft.Packet
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.Identifier
 *  net.minecraft.RegistryEntry
 *  net.minecraft.RegistryEntry$Reference
 *  net.minecraft.Registries
 *  net.minecraft.RegistryKeys
 *  org.jetbrains.annotations.Nullable
 */
package pyrock.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.inventory.HotbarActionService;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.ItemRule;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import org.jetbrains.annotations.Nullable;

public class PyInventory {
    public int selected() {
        return ClientAccess.minecraftClient.player == null ? 0 : ClientAccess.minecraftClient.player.getInventory().selectedSlot;
    }

    public boolean select(int n) {
        if (n < 0 || n > 8 || ClientAccess.minecraftClient.player == null || ClientAccess.minecraftClient.getNetworkHandler() == null) {
            return false;
        }
        if (ClientAccess.minecraftClient.player.getInventory().selectedSlot == n) {
            return true;
        }
        ClientAccess.minecraftClient.player.getInventory().selectedSlot = n;
        ClientAccess.minecraftClient.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(n));
        return true;
    }

    @Nullable
    public ItemStack stack(int n) {
        ItemRule itemRule = PyInventory.slot(n);
        if (itemRule == null) {
            return null;
        }
        ItemStack class_17992 = itemRule.getItemStack();
        return class_17992 == null || class_17992.isEmpty() ? null : class_17992;
    }

    @Nullable
    public String id(int n) {
        ItemStack class_17992 = this.stack(n);
        return class_17992 == null ? null : Registries.ITEM.getId(class_17992.getItem()).toString();
    }

    @Nullable
    public String label(int n) {
        ItemStack class_17992 = this.stack(n);
        return class_17992 == null ? null : class_17992.getName().getString();
    }

    public int count(int n) {
        ItemStack class_17992 = this.stack(n);
        return class_17992 == null ? 0 : class_17992.getCount();
    }

    public int find(String string, String string2) {
        Item class_17922 = PyInventory.item(string);
        if (class_17922 == null) {
            return -1;
        }
        for (int n : PyInventory.range(string2)) {
            ItemStack class_17992 = this.stack(n);
            if (class_17992 == null || class_17992.getItem() != class_17922) continue;
            return n;
        }
        return -1;
    }

    public List<Integer> findAll(String string, String string2) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        Item class_17922 = PyInventory.item(string);
        if (class_17922 == null) {
            return arrayList;
        }
        for (int n : PyInventory.range(string2)) {
            ItemStack class_17992 = this.stack(n);
            if (class_17992 == null || class_17992.getItem() != class_17922) continue;
            arrayList.add(n);
        }
        return arrayList;
    }

    public int total(String string, String string2) {
        Item class_17922 = PyInventory.item(string);
        if (class_17922 == null) {
            return 0;
        }
        int n = 0;
        for (int n2 : PyInventory.range(string2)) {
            ItemStack class_17992 = this.stack(n2);
            if (class_17992 == null || class_17992.getItem() != class_17922) continue;
            n += class_17992.getCount();
        }
        return n;
    }

    public int empty(String string) {
        for (int n : PyInventory.range(string)) {
            if (this.stack(n) != null) continue;
            return n;
        }
        return -1;
    }

    public int hold(String string) {
        int n = this.find(string, "hotbar");
        if (n >= 0 && this.select(n)) {
            return n;
        }
        return -1;
    }

    public boolean swap(int n, int n2) {
        if (PyInventory.slot(n) == null || PyInventory.slot(n2) == null || ClientAccess.minecraftClient.player == null) {
            return false;
        }
        InventoryUtils.swapMenuSlots(n, n2);
        return true;
    }

    public boolean move(int n, int n2) {
        ItemRule itemRule = PyInventory.slot(n);
        ItemRule itemRule2 = PyInventory.slot(n2);
        if (itemRule == null || itemRule2 == null) {
            return false;
        }
        InventoryUtils.swapItemRules(itemRule, itemRule2);
        return true;
    }

    public boolean quickMove(int n) {
        ItemRule itemRule = PyInventory.slot(n);
        if (itemRule == null || ClientAccess.minecraftClient.player == null) {
            return false;
        }
        InventoryUtils.quickMoveItem(itemRule.getClickSlot());
        return true;
    }

    public boolean toOffhand(int n) {
        ItemRule itemRule = PyInventory.slot(n);
        if (itemRule == null) {
            return false;
        }
        InventoryUtils.moveItemToOffhand(itemRule);
        return true;
    }

    public boolean toArmor(int n, String string) {
        ItemRule itemRule = PyInventory.slot(n);
        int n2 = PyInventory.armorIndex(string);
        if (itemRule == null || n2 < 0) {
            return false;
        }
        return InventoryUtils.moveItemToArmor(itemRule, n2);
    }

    public boolean withSlot(int n, Runnable runnable) {
        if (n < 0 || n > 8 || runnable == null) {
            return false;
        }
        HotbarSlot hotbarSlot = InventoryUtils.hotbarSlot(n);
        HotbarActionService.withTemporaryHotbarSlot(hotbarSlot, runnable);
        return true;
    }

    public double durability(int n) {
        ItemStack class_17992 = this.stack(n);
        if (class_17992 == null || !class_17992.isDamageable()) {
            return -1.0;
        }
        int n2 = class_17992.getMaxDamage();
        if (n2 <= 0) {
            return -1.0;
        }
        return 100.0 * (double)(n2 - class_17992.getDamage()) / (double)n2;
    }

    public int enchant(int n, String string) {
        ItemStack class_17992 = this.stack(n);
        if (class_17992 == null || ClientAccess.minecraftClient.world == null || string == null) {
            return 0;
        }
        Identifier class_29602 = PyInventory.identifier(string);
        if (class_29602 == null) {
            return 0;
        }
        try {
            Registry<Enchantment> enchantmentRegistry = ClientAccess.minecraftClient.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            RegistryEntry<Enchantment> enchantmentEntry = enchantmentRegistry.getEntry(class_29602).orElse(null);
            return enchantmentEntry == null ? 0 : EnchantmentHelper.getLevel(enchantmentEntry, class_17992);
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    public List<Integer> slots(String string) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (int n : PyInventory.range(string)) {
            arrayList.add(n);
        }
        return arrayList;
    }

    public boolean offhandIs(String string) {
        Item class_17922 = PyInventory.item(string);
        return class_17922 != null && InventoryUtils.isHelmetItem(class_17922);
    }

    public boolean containerOpen() {
        return ClientAccess.minecraftClient.player != null && ClientAccess.minecraftClient.currentScreen != null && ClientAccess.minecraftClient.player.currentScreenHandler != ClientAccess.minecraftClient.player.playerScreenHandler;
    }

    @Nullable
    private static ItemRule slot(int n) {
        if (ClientAccess.minecraftClient.player == null) {
            return null;
        }
        if (n >= 0 && n <= 8) {
            return InventoryUtils.hotbarSlot(n);
        }
        if (n >= 9 && n <= 35) {
            return InventoryUtils.inventorySlot(n - 9);
        }
        if (n >= 36 && n <= 39) {
            return InventoryUtils.armorSlot(39 - n);
        }
        if (n == 40) {
            return InventoryUtils.offhandRule();
        }
        return null;
    }

    private static int armorIndex(String string) {
        if (string == null) {
            return -1;
        }
        return switch (string.toLowerCase(Locale.ROOT)) {
            case "boots", "\u0431\u043e\u0442\u0438\u043d\u043a\u0438" -> 0;
            case "leggings", "\u043f\u043e\u043d\u043e\u0436\u0438", "\u0448\u0442\u0430\u043d\u044b" -> 1;
            case "chestplate", "\u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a" -> 2;
            case "helmet", "\u0448\u043b\u0435\u043c" -> 3;
            default -> -1;
        };
    }

    private static int[] range(String string) {
        int[] nArray;
        String string2;
        switch (string2 = string == null ? "all" : string.toLowerCase(Locale.ROOT)) {
            case "hotbar": {
                nArray = PyInventory.ints(0, 8);
                break;
            }
            case "main": 
            case "inventory": 
            case "backpack": {
                nArray = PyInventory.ints(9, 35);
                break;
            }
            case "armor": {
                nArray = PyInventory.ints(36, 39);
                break;
            }
            case "offhand": {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 40;
                break;
            }
            default: {
                nArray = PyInventory.ints(0, 40);
            }
        }
        return nArray;
    }

    private static int[] ints(int n, int n2) {
        int[] nArray = new int[n2 - n + 1];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = n + i;
        }
        return nArray;
    }

    @Nullable
    private static Identifier identifier(String string) {
        if (string == null || string.isBlank()) {
            return null;
        }
        return string.contains(":") ? Identifier.tryParse((String)string) : Identifier.tryParse((String)("minecraft:" + string));
    }

    @Nullable
    private static Item item(String string) {
        Identifier class_29602 = PyInventory.identifier(string);
        if (class_29602 == null) {
            return null;
        }
        Item class_17922 = (Item)Registries.ITEM.get(class_29602);
        if (class_17922 == Items.AIR && !"minecraft:air".equals(class_29602.toString())) {
            return null;
        }
        return class_17922;
    }
}
