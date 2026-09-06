/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.Items
 *  net.minecraft.Packet
 *  net.minecraft.ClientCommandC2SPacket
 *  net.minecraft.ClientCommandC2SPacket$Mode
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.PlayerInteractItemC2SPacket
 */
package moscow.rockstar.modules.movement.flight;

import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.InventorySlotRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import pyrock.events.player.InputEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Elytra Strafe", category=ModuleCategory.MOVEMENT)
public class ElytraStrafe
extends Module {
    private NumberSetting fireworkSlotSetting;
    private NumberSetting fireworkDelaySetting;
    private BooleanSetting autoTakeoffSetting;
    private final Timer fireworkCooldownTimer = new Timer();
    private final EventListener<InputEvent> inputListener = inputEvent -> {
        if (ElytraStrafe.minecraftClient.player == null || !ElytraStrafe.minecraftClient.player.isGliding()) {
            return;
        }
        float movementYawDegrees = (float)Math.toDegrees(EntityUtils.getDirectionRadians(ElytraStrafe.minecraftClient.player.getYaw(), inputEvent.getForward(), inputEvent.getStrafe()));
        float pitchDegrees = ElytraStrafe.minecraftClient.options.sneakKey.isPressed() || ElytraStrafe.minecraftClient.options.jumpKey.isPressed() ? (inputEvent.getStrafe() + inputEvent.getForward() > 0.1f ? -45.0f : -90.0f) : 0.0f;
        if (ElytraStrafe.minecraftClient.options.sneakKey.isPressed()) {
            pitchDegrees *= -1.0f;
        }
        RockstarClient.create().getRotationManager().requestRotation(new Rotation(movementYawDegrees, pitchDegrees), RotationCorrectionMode.DIRECT, 180.0f, 180.0f, 180.0f, RotationPriority.LOW_PRIORITY);
    };

    public ElytraStrafe() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.fireworkSlotSetting = new NumberSetting(this, "modules.settings.elytra_target.fireworkSlot").setMinValue(1.0f).setMaxValue(9.0f).setStep(1.0f).setValue(7.0f).setUnit(" slot");
        this.fireworkDelaySetting = new NumberSetting(this, "modules.settings.elytra_target.fireworkDelay").setMinValue(0.1f).setMaxValue(2.0f).setStep(0.1f).setValue(1.0f).setUnit(" sec");
        this.autoTakeoffSetting = new BooleanSetting(this, "modules.settings.elytra_strafe.autoTakeoff").enable();
    }

    @Override
    public void onTick() {
        if (ElytraStrafe.minecraftClient.player == null) {
            return;
        }
        if (this.autoTakeoffSetting.isEnabled()) {
            boolean bl;
            boolean bl2 = bl = InventoryUtils.chestplateRule().getItem() == Items.ELYTRA;
            if (!ElytraStrafe.minecraftClient.player.isGliding() && bl && !ElytraStrafe.minecraftClient.player.isOnGround() && !ElytraStrafe.minecraftClient.player.isInFluid()) {
                ElytraStrafe.minecraftClient.player.startGliding();
                ElytraStrafe.minecraftClient.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraStrafe.minecraftClient.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            } else if (ElytraStrafe.minecraftClient.player.isOnGround() && bl && !ElytraStrafe.minecraftClient.player.isInFluid() && !ElytraStrafe.minecraftClient.player.isGliding()) {
                ElytraStrafe.minecraftClient.player.jump();
            }
        }
        if (!ElytraStrafe.minecraftClient.player.isGliding()) {
            return;
        }
        if (this.fireworkCooldownTimer.hasElapsed((long)(this.fireworkDelaySetting.getValue() * 1000.0f)) && !ElytraStrafe.minecraftClient.player.isUsingItem()) {
            this.useFirework();
        }
    }

    private void useFirework() {
        ItemRuleCollection<HotbarSlot> itemRuleCollection = ItemRuleSets.getHotbarRules();
        HotbarSlot hotbarSlot = itemRuleCollection.findByItem(Items.FIREWORK_ROCKET);
        if (hotbarSlot != null) {
            ElytraStrafe.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(hotbarSlot.getSlotIndex()));
            ((moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)ElytraStrafe.minecraftClient.interactionManager).rockstar$sendSequencedPacket(ElytraStrafe.minecraftClient.world, n -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, n, ElytraStrafe.minecraftClient.player.getYaw(), ElytraStrafe.minecraftClient.player.getPitch()));
            ElytraStrafe.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(ElytraStrafe.minecraftClient.player.getInventory().selectedSlot));
            this.fireworkCooldownTimer.reset();
            return;
        }
        ItemRuleCollection<InventorySlotRule> itemRuleCollection2 = ItemRuleSets.getInventoryRules();
        InventorySlotRule inventorySlotRule = itemRuleCollection2.findByItem(Items.FIREWORK_ROCKET);
        if (inventorySlotRule != null) {
            InventoryUtils.dropItem(inventorySlotRule.getClickSlot(), (int)(this.fireworkSlotSetting.getValue() - 1.0f));
            this.fireworkCooldownTimer.reset();
        }
    }
}
