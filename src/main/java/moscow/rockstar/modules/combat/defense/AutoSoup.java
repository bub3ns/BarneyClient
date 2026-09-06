/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 */
package moscow.rockstar.modules.combat.defense;

import java.util.List;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.InventorySlotRule;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Soup", category=ModuleCategory.COMBAT)
public class AutoSoup
extends Module {
    int previousHotbarSlot = -1;
    int soupHotbarSlot = -1;
    int swapStep = -1;
    private NumberSetting healthThreshold;
    private final Timer soupCooldownTimer = new Timer();

    public AutoSoup() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.healthThreshold = new NumberSetting(this, "modules.settings.auto_soup.health").setStep(1.0f).setMinValue(1.0f).setMaxValue(20.0f).setValue(10.0f);
    }

    @Override
    public void onTick() {
        if (this.swapStep >= 0) {
            if (this.swapStep == 2) {
                AutoSoup.minecraftClient.player.getInventory().selectedSlot = this.soupHotbarSlot;
            } else if (this.swapStep == 1) {
                AutoSoup.minecraftClient.interactionManager.interactItem((PlayerEntity)AutoSoup.minecraftClient.player, Hand.MAIN_HAND);
            } else if (this.swapStep == 0) {
                AutoSoup.minecraftClient.player.dropSelectedItem(true);
                AutoSoup.minecraftClient.player.getInventory().selectedSlot = this.previousHotbarSlot;
            }
            --this.swapStep;
            return;
        }
        if (AutoSoup.minecraftClient.player.getHealth() >= this.healthThreshold.getValue() || !this.soupCooldownTimer.hasElapsed(300L)) {
            return;
        }
        HotbarSlot hotbarSlot = ItemRuleSets.getHotbarRules().findByItem(Items.MUSHROOM_STEW);
        if (hotbarSlot != null) {
            this.previousHotbarSlot = AutoSoup.minecraftClient.player.getInventory().selectedSlot;
            AutoSoup.minecraftClient.player.getInventory().selectedSlot = this.soupHotbarSlot = hotbarSlot.getSlotIndex();
            this.swapStep = 1;
        } else {
            List<InventorySlotRule> list = ItemRuleSets.getInventoryRules().findAllByItem(Items.MUSHROOM_STEW);
            List<HotbarSlot> list2 = ItemRuleSets.getHotbarRules().findAllByStack(ItemStack::isEmpty);
            if (!list.isEmpty() && !list2.isEmpty()) {
                int n = Math.min(list.size(), list2.size());
                n = Math.min(n, 8);
                for (int i = 0; i < n; ++i) {
                    InventorySlotRule inventorySlotRule = list.get(i);
                    HotbarSlot hotbarSlot2 = list2.get(i);
                    InventoryUtils.dropItem(inventorySlotRule.getClickSlot(), hotbarSlot2.getSlotIndex());
                }
                this.previousHotbarSlot = AutoSoup.minecraftClient.player.getInventory().selectedSlot;
                this.soupHotbarSlot = list2.get(0).getSlotIndex();
                this.swapStep = 2;
            }
        }
        this.soupCooldownTimer.reset();
    }
}

