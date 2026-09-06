/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.EquipmentSlot
 *  net.minecraft.PlayerEntity
 *  net.minecraft.PlayerInventory
 *  net.minecraft.SlotActionType
 *  net.minecraft.ArmorItem
 *  net.minecraft.ArmorMaterial
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.RegistryKey
 *  net.minecraft.EquipmentType
 */
package moscow.rockstar.modules.combat.defense.armor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.defense.armor.ArmorSlotSwitch;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.api.access.ArmorItemAccess;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKey;
import net.minecraft.item.equipment.EquipmentType;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Armor", category=ModuleCategory.COMBAT, description="modules.descriptions.auto_armor")
public class AutoArmor
extends Module {
    private final Timer cooldownTimer = new Timer();
    private NumberSetting delay;
    private BooleanSetting elytra;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        PlayerInventory class_16612 = AutoArmor.minecraftClient.player.getInventory();
        int[] nArray = new int[4];
        int[] nArray2 = new int[4];
        this.equipBestArmor(class_16612, nArray, nArray2);
        ArrayList<Integer> arrayList = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(arrayList);
        Iterator iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            ItemStack class_17992;
            int n = (Integer)iterator.next();
            int n2 = nArray[n];
            if (n2 == -1 || !(class_17992 = class_16612.getArmorStack(n)).isEmpty() && class_16612.getEmptySlot() == -1 || this.elytra.isEnabled() && AutoArmor.minecraftClient.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA && n == 2) continue;
            this.swapArmorSlot(class_16612, n2, n);
            break;
        }
    };

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.delay = new NumberSetting(this, "modules.settings.auto_armor.delay").setMinValue(50.0f).setMaxValue(1000.0f).setStep(1.0f).setValue(250.0f).setUnit(" ms");
        this.elytra = new BooleanSetting(this, "modules.settings.auto_armor.elytra");
    }

    public AutoArmor() {
        this.initializeSettings();
    }

    private void equipBestArmor(PlayerInventory class_16612, int[] nArray, int[] nArray2) {
        ArmorItem class_17382;
        Item class_17922;
        ItemStack class_17992;
        int n;
        for (n = 0; n < 4; ++n) {
            nArray[n] = -1;
            class_17992 = class_16612.getArmorStack(n);
            if (class_17992.isEmpty() || !((class_17922 = class_17992.getItem()) instanceof ArmorItem)) continue;
            class_17382 = (ArmorItem)class_17922;
            nArray2[n] = this.calculateArmorScore(class_17382, class_17992);
        }
        block7: for (n = 0; n < 36; ++n) {
            int n2;
            class_17992 = class_16612.getStack(n);
            if (class_17992.isEmpty() || !((class_17922 = class_17992.getItem()) instanceof ArmorItem)) continue;
            class_17382 = (ArmorItem)class_17922;
            EquipmentSlot equipmentSlot = ((ArmorItemAccess)class_17382).rockstar$getType().getEquipmentSlot();
            switch (ArmorSlotSwitch.VALUES[equipmentSlot.ordinal()]) {
                case 1: {
                    n2 = 3;
                    break;
                }
                case 2: {
                    n2 = 2;
                    break;
                }
                case 3: {
                    n2 = 1;
                    break;
                }
                case 4: {
                    n2 = 0;
                    break;
                }
                default: {
                    continue block7;
                }
            }
            int n3 = this.calculateArmorScore(class_17382, class_17992);
            if (n3 <= nArray2[n2]) continue;
            nArray[n2] = n;
            nArray2[n2] = n3;
        }
    }

    private void swapArmorSlot(PlayerInventory class_16612, int n, int n2) {
        if (n < 9) {
            n += 36;
        }
        if (this.cooldownTimer.hasElapsed((long)this.delay.getValue())) {
            ItemStack class_17992 = class_16612.getArmorStack(n2);
            if (!class_17992.isEmpty()) {
                AutoArmor.minecraftClient.interactionManager.clickSlot(0, 8 - n2, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)AutoArmor.minecraftClient.player);
            }
            AutoArmor.minecraftClient.interactionManager.clickSlot(0, n, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)AutoArmor.minecraftClient.player);
            this.cooldownTimer.reset();
        }
    }

    private int calculateArmorScore(ArmorItem class_17382, ItemStack class_17992) {
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        if (donorItem != null && "SunHelmet".equals(donorItem.getRawName())) {
            return Integer.MAX_VALUE;
        }
        ArmorMaterial armorMaterial = ((ArmorItemAccess)class_17382).rockstar$getMaterial();
        EquipmentType class_80512 = ((ArmorItemAccess)class_17382).rockstar$getType();
        int n = armorMaterial.defense().getOrDefault(class_80512, 0);
        int n2 = (int)armorMaterial.toughness();
        int n3 = EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.PROTECTION);
        return n * 5 + n3 * 3 + n2;
    }
}
