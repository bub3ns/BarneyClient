/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.StatusEffects
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.UseAction
 *  net.minecraft.Blocks
 *  net.minecraft.Packet
 *  net.minecraft.PlayerActionC2SPacket
 *  net.minecraft.PlayerActionC2SPacket$Action
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.PlayerInteractItemC2SPacket
 */
package moscow.rockstar.modules.movement.items;

import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.ModeSetting;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import pyrock.events.network.SendPacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.player.InputEvent;
import pyrock.events.player.SlowDownEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="No Slow", category=ModuleCategory.MOVEMENT)
public class NoSlow
extends Module {
    private int useItemTicks;
    private ModeSetting modeSetting;
    private ModeSetting.Option grimOption;
    private ModeSetting.Option grimNewOption;
    private ModeSetting.Option grimTickOption;
    private ModeSetting.Option spookyOption;
    private ModeSetting.Option hollyOption;
    private ModeSetting.Option vonTamOption;
    private ModeSetting.Option lonyGriefOption;
    private int lastSprintToggleTick = -1;
    private boolean sprintSpoofEnabled;
    private int packetResetTicks;
    private final EventListener<SlowDownEvent> slowDownListener = slowDownEvent -> {
        int n2;
        if (NoSlow.minecraftClient.player == null || NoSlow.minecraftClient.world == null || NoSlow.minecraftClient.interactionManager == null) {
            return;
        }
        if (this.spookyOption.isSelected() && (this.packetResetTicks > 0 || this.isNoSlowCooldownReady())) {
            this.resetUseCycle();
            slowDownEvent.cancel();
            return;
        }
        if (this.lonyGriefOption.isSelected()) {
            this.applyLonyGriefNoSlow((SlowDownEvent)slowDownEvent);
            return;
        }
        if (this.isNoSlowInventoryReady()) {
            this.useItemTicks = 0;
            this.resetSprintState();
            return;
        }
        if (this.vonTamOption.isSelected()) {
            if (this.isNoSlowScreenReady() || NoSlow.minecraftClient.player.getMainHandStack().isOf(Items.CROSSBOW) || NoSlow.minecraftClient.player.getMainHandStack().isOf(Items.MILK_BUCKET)) {
                NoSlow.minecraftClient.player.setSprinting(true);
                slowDownEvent.cancel();
            }
            return;
        }
        if (this.grimTickOption.isSelected() || this.hollyOption.isSelected()) {
            if (NoSlow.minecraftClient.player.isGliding()) {
                return;
            }
            if (NoSlow.minecraftClient.player.age % 2 == 0 && !NoSlow.minecraftClient.player.isSneaking()) {
                slowDownEvent.cancel();
            }
            return;
        }
        this.updateSprintState();
        if (NoSlow.minecraftClient.player.getActiveHand() == Hand.MAIN_HAND && !this.spookyOption.isSelected() && !this.grimNewOption.isSelected()) {
            ((moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)NoSlow.minecraftClient.interactionManager).rockstar$sendSequencedPacket(NoSlow.minecraftClient.world, n -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, n, NoSlow.minecraftClient.player.getYaw(), NoSlow.minecraftClient.player.getPitch()));
            slowDownEvent.cancel();
            return;
        }
        if (!this.spookyOption.isSelected() && !this.grimNewOption.isSelected()) {
            ((moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)NoSlow.minecraftClient.interactionManager).rockstar$sendSequencedPacket(NoSlow.minecraftClient.world, n -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, n, NoSlow.minecraftClient.player.getYaw(), NoSlow.minecraftClient.player.getPitch()));
        } else {
            ++this.useItemTicks;
        }
        boolean bl = NoSlow.minecraftClient.player.getActiveItem().isOf(Items.GOLDEN_APPLE) || NoSlow.minecraftClient.player.getActiveItem().isOf(Items.ENCHANTED_GOLDEN_APPLE);
        int n3 = n2 = bl ? 4 : 2;
        if (this.useItemTicks >= n2 && (!bl || !NoSlow.minecraftClient.player.isOnGround()) || this.grimOption.isSelected() || this.useItemTicks >= 2 && this.grimNewOption.isSelected()) {
            slowDownEvent.cancel();
            this.useItemTicks = 0;
        }
    };
    private final EventListener<InputEvent> inputListener = inputEvent -> {
        if (this.vonTamOption.isSelected() && NoSlow.minecraftClient.player.isUsingItem() && this.isNoSlowScreenReady()) {
            inputEvent.setJump(false);
            if (NoSlow.minecraftClient.player.hasStatusEffect(StatusEffects.SPEED)) {
                NoSlow.minecraftClient.player.setVelocity(NoSlow.minecraftClient.player.getVelocity().x * 0.71, NoSlow.minecraftClient.player.getVelocity().y, NoSlow.minecraftClient.player.getVelocity().z * 0.71);
            }
        }
    };
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (NoSlow.minecraftClient.player == null || NoSlow.minecraftClient.world == null || NoSlow.minecraftClient.player.getItemCooldownManager() == null || minecraftClient.getNetworkHandler() == null) {
            return;
        }
        if (this.packetResetTicks > 0) {
            if (this.spookyOption.isSelected()) {
                this.resetUseCycle();
            }
            --this.packetResetTicks;
        }
        if (this.lonyGriefOption.isSelected()) {
            this.sendUseReleasePacket();
        }
        if (this.grimNewOption.isSelected() && !this.isNoSlowInventoryReady()) {
            this.updateSprintState();
        } else {
            this.resetSprintState();
        }
        if (!this.grimOption.isSelected()) {
            return;
        }
        if (!NoSlow.minecraftClient.player.getItemCooldownManager().isCoolingDown(NoSlow.minecraftClient.player.getMainHandStack().getItem().getDefaultStack()) && !NoSlow.minecraftClient.player.getItemCooldownManager().isCoolingDown(NoSlow.minecraftClient.player.getOffHandStack().getItem().getDefaultStack()) && NoSlow.minecraftClient.player.isUsingItem() && NoSlow.minecraftClient.player.fallDistance < 1.0f && NoSlow.minecraftClient.player.getActiveHand() == Hand.OFF_HAND) {
            minecraftClient.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(NoSlow.minecraftClient.player.getInventory().selectedSlot % 8 + 1));
            minecraftClient.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(NoSlow.minecraftClient.player.getInventory().selectedSlot));
        }
    };
    private final EventListener<SendPacketEvent> sendPacketListener = sendPacketEvent -> {
        PlayerInteractItemC2SPacket class_28862;
        if (NoSlow.minecraftClient.player == null || NoSlow.minecraftClient.world == null || !this.spookyOption.isSelected()) {
            return;
        }
        Packet<?> class_25962 = sendPacketEvent.getPacket();
        if (class_25962 instanceof PlayerInteractItemC2SPacket && this.isNoSlowItem(this.getHeldStack((class_28862 = (PlayerInteractItemC2SPacket)class_25962).getHand()))) {
            this.packetResetTicks = 4;
            this.resetUseCycle();
        }
    };

    public NoSlow() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.modeSetting = new ModeSetting(this, "modules.settings.noslow.mode");
        this.grimOption = new ModeSetting.Option(this.modeSetting, "modules.settings.noslow.grim");
        this.grimNewOption = new ModeSetting.Option(this.modeSetting, "GrimNew");
        this.grimTickOption = new ModeSetting.Option(this.modeSetting, "modules.settings.noslow.grim_tick");
        this.spookyOption = new ModeSetting.Option(this.modeSetting, "modules.settings.noslow.spooky");
        this.hollyOption = new ModeSetting.Option(this.modeSetting, "modules.settings.noslow.holly");
        this.vonTamOption = new ModeSetting.Option(this.modeSetting, "VonTam");
        this.lonyGriefOption = new ModeSetting.Option(this.modeSetting, "LonyGrief");
    }

    private boolean isNoSlowScreenReady() {
        if (NoSlow.minecraftClient.player == null || NoSlow.minecraftClient.world == null) {
            return false;
        }
        if (EntityUtils.getBlockAtOffset(0.0, -1.0, 0.0) == Blocks.SNOW || EntityUtils.getBlockAtOffset(0.0, -1.0, 0.0) == Blocks.SHORT_GRASS) {
            return true;
        }
        return EntityUtils.getBlockAtOffset(0.0, 0.0, 0.0) == Blocks.SNOW || EntityUtils.getBlockAtOffset(0.0, 0.0, 0.0) == Blocks.SHORT_GRASS;
    }

    private boolean isNoSlowInventoryReady() {
        return (NoSlow.minecraftClient.player.getMainHandStack().getUseAction() == UseAction.BLOCK || NoSlow.minecraftClient.player.getOffHandStack().getUseAction() == UseAction.EAT) && NoSlow.minecraftClient.player.getActiveHand() == Hand.MAIN_HAND || !NoSlow.minecraftClient.player.isUsingItem();
    }

    private boolean isNoSlowItem(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return false;
        }
        return class_17992.isOf(Items.ENDER_EYE) || class_17992.isOf(Items.FIRE_CHARGE) || class_17992.isOf(Items.PHANTOM_MEMBRANE) || class_17992.isOf(Items.SUGAR) || class_17992.isOf(Items.SNOWBALL) || class_17992.isOf(Items.NETHER_STAR) || class_17992.isOf(Items.PRISMARINE_SHARD) || class_17992.isOf(Items.FIREWORK_STAR);
    }

    private boolean isNoSlowCooldownReady() {
        return NoSlow.minecraftClient.player.isUsingItem() && (this.isNoSlowItem(NoSlow.minecraftClient.player.getActiveItem()) || this.isNoSlowItem(this.getHeldStack(NoSlow.minecraftClient.player.getActiveHand())));
    }

    private ItemStack getHeldStack(Hand class_12682) {
        return class_12682 == Hand.OFF_HAND ? NoSlow.minecraftClient.player.getOffHandStack() : NoSlow.minecraftClient.player.getMainHandStack();
    }

    private void resetUseCycle() {
        this.updateSprintState();
        this.useItemTicks = 0;
    }

    private void resetSprintState() {
        this.lastSprintToggleTick = -1;
        this.sprintSpoofEnabled = false;
    }

    private void updateSprintState() {
        if (!this.grimNewOption.isSelected()) {
            this.resetSprintState();
            NoSlow.minecraftClient.player.setSprinting(!ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD));
            return;
        }
        int n = NoSlow.minecraftClient.player.age;
        if (this.lastSprintToggleTick != n) {
            this.sprintSpoofEnabled = this.lastSprintToggleTick != -1 && !this.sprintSpoofEnabled;
            this.lastSprintToggleTick = n;
        }
        NoSlow.minecraftClient.player.setSprinting(this.sprintSpoofEnabled);
    }

    private void applyLonyGriefNoSlow(SlowDownEvent slowDownEvent) {
        if (NoSlow.minecraftClient.player.isGliding() || !NoSlow.minecraftClient.player.isUsingItem()) {
            return;
        }
        if (NoSlow.minecraftClient.player.getActiveHand() == Hand.OFF_HAND) {
            if (NoSlow.minecraftClient.player.age % 2 == 0 && !NoSlow.minecraftClient.player.isSneaking()) {
                slowDownEvent.cancel();
            }
            return;
        }
        if (NoSlow.minecraftClient.player.getItemUseTime() > 0) {
            slowDownEvent.cancel();
        }
    }

    private void sendUseReleasePacket() {
        if (NoSlow.minecraftClient.player.networkHandler == null || NoSlow.minecraftClient.player.isGliding()) {
            return;
        }
        if (NoSlow.minecraftClient.player.isUsingItem() && NoSlow.minecraftClient.player.getItemUseTime() == 0) {
            NoSlow.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, NoSlow.minecraftClient.player.getHorizontalFacing()));
        }
    }

    @Override
    public void onDisable() {
        this.useItemTicks = 0;
        this.packetResetTicks = 0;
        this.resetSprintState();
    }
}
