/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.Packet
 *  net.minecraft.PlayerActionC2SPacket
 *  net.minecraft.PlayerActionC2SPacket$Action
 *  net.minecraft.RegistryKey
 */
package moscow.rockstar.modules.player.interaction;

import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.RegistryKey;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Fast Item Use", category=ModuleCategory.OTHER, description="modules.descriptions.fast_item_use")
public class FastItemUse
extends Module {
    private BooleanSetting bowSetting;
    private BooleanSetting tridentSetting;
    private BooleanSetting crossbowSetting;
    private NumberSetting releaseDelayTicks;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (this.tridentSetting.isEnabled() && this.isTridentReady()) {
            this.releaseChargedItem();
        }
        if (this.bowSetting.isEnabled() && this.isBowReady()) {
            this.releaseChargedItem();
        }
        if (this.crossbowSetting.isEnabled() && this.isCrossbowReady()) {
            this.releaseChargedItem();
        }
    };

    public FastItemUse() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.bowSetting = new BooleanSetting((SettingOwner)this, "modules.settings.fast_item_use.bow", "modules.settings.fast_item_use.bow.description").enable();
        this.tridentSetting = new BooleanSetting((SettingOwner)this, "modules.settings.fast_item_use.trident", "modules.settings.fast_item_use.trident.description").enable();
        this.crossbowSetting = new BooleanSetting((SettingOwner)this, "modules.settings.fast_item_use.crossbow", "modules.settings.fast_item_use.crossbow.description").enable();
        this.releaseDelayTicks = new NumberSetting(this, "modules.settings.fast_item_use.delay").setValue(10.0f).setMaxValue(20.0f).setMinValue(1.0f).setStep(1.0f);
    }

    private void releaseChargedItem() {
        if (FastItemUse.minecraftClient.player == null) {
            return;
        }
        FastItemUse.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, FastItemUse.minecraftClient.player.getHorizontalFacing()));
        FastItemUse.minecraftClient.player.stopUsingItem();
    }

    private boolean isTridentReady() {
        if (FastItemUse.minecraftClient.player == null) {
            return false;
        }
        ItemStack class_17992 = FastItemUse.minecraftClient.player.getMainHandStack();
        return class_17992.getItem() == Items.TRIDENT && EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.RIPTIDE) > 0 && FastItemUse.minecraftClient.player.isUsingItem() && (float)FastItemUse.minecraftClient.player.getItemUseTime() >= this.releaseDelayTicks.getValue() && FastItemUse.minecraftClient.player.getAttackCooldownProgress(0.5f) > 0.92f;
    }

    private boolean isBowReady() {
        if (FastItemUse.minecraftClient.player == null) {
            return false;
        }
        return FastItemUse.minecraftClient.player.getMainHandStack().getItem() == Items.BOW && FastItemUse.minecraftClient.player.isUsingItem() && (float)FastItemUse.minecraftClient.player.getItemUseTime() >= this.releaseDelayTicks.getValue();
    }

    private boolean isCrossbowReady() {
        if (FastItemUse.minecraftClient.player == null) {
            return false;
        }
        return FastItemUse.minecraftClient.player.getMainHandStack().getItem() == Items.CROSSBOW && FastItemUse.minecraftClient.player.isUsingItem() && (float)FastItemUse.minecraftClient.player.getItemUseTime() >= this.releaseDelayTicks.getValue();
    }
}

