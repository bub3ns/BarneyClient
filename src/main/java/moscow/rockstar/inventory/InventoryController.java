/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  lombok.Generated
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.PlayerInventory
 *  net.minecraft.SlotActionType
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.ClientStatusC2SPacket
 *  net.minecraft.ClientStatusC2SPacket$Mode
 *  net.minecraft.CloseHandledScreenC2SPacket
 *  net.minecraft.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.PlayerMoveC2SPacket$Full
 *  net.minecraft.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.PlayerMoveC2SPacket$OnGroundOnly
 *  net.minecraft.HandSwingC2SPacket
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.inventory;

import com.mojang.authlib.GameProfile;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import lombok.Generated;
import moscow.rockstar.items.InventoryState;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.network.bot.BotController;
import moscow.rockstar.network.bot.BotWorldState;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.OtherClientPlayerEntity;

public class InventoryController {
    private final BotController botController;
    private final BotWorldState botWorldState;
    private Vec3d playerPosition = Vec3d.ZERO;
    private float currentYaw;
    private float currentPitch;
    private boolean screenReady;
    private boolean inventoryReady;
    private boolean jumpPressed;
    private boolean cooldownReady = true;
    private boolean actionReady;
    private boolean worldReady;
    private float movementSpeed = 20.0f;
    private int stateUpdateTick;
    private int slotIndex;
    private ItemStack mainHandStack = ItemStack.EMPTY;
    private ItemStack offHandStack = ItemStack.EMPTY;
    private boolean simulationActive;
    private OtherClientPlayerEntity fakePlayer;
    private Entity previousPlayer;
    private ItemStack[] inventorySnapshot;
    private int selectedHotbarSlot;
    private long lastAttackPacketTime;
    private long lastUsePacketTime;
    private long lastPickBlockTime;
    private double lastX;
    private double lastY;
    private double lastZ;
    private float lastSentYaw;
    private float lastSentPitch;
    private boolean lastOnGround = true;
    private int movementPacketTick = 20;
    private final List<ClickAction> pendingClickActions = new ArrayList<ClickAction>();

    public InventoryController(BotController botController, BotWorldState botWorldState) {
        this.botController = botController;
        this.botWorldState = botWorldState;
    }

    public void updateControllerState() {
        this.processQueuedClicks();
        this.updateInventoryState();
        this.sendMovementPackets();
    }

    public void updateInventoryState() {
        InventoryState inventoryState = this.botController.getInventoryState();
        this.playerPosition = inventoryState.getPosition();
        this.currentYaw = inventoryState.getYaw();
        this.currentPitch = inventoryState.getPitch();
        this.screenReady = inventoryState.isPrimaryActionActive();
        this.inventoryReady = inventoryState.isSecondaryActionActive();
        this.cooldownReady = inventoryState.isOnGround();
        this.actionReady = inventoryState.isFlying();
        this.worldReady = inventoryState.isCreativeMode();
        this.movementSpeed = inventoryState.getHealth();
        this.stateUpdateTick = inventoryState.getUpdateTick();
        this.slotIndex = inventoryState.getSelectedHotbarSlot();
        this.mainHandStack = inventoryState.getSelectedHotbarItem();
        this.offHandStack = inventoryState.getOffhandStack();
        if (this.simulationActive) {
            this.applyInventoryChanges(MinecraftClient.getInstance());
        }
        this.botWorldState.update(inventoryState);
        this.updateFakePlayerState();
    }

    public boolean startSimulation(MinecraftClient client) {
        if (client == null || client.world == null || client.player == null) {
            return false;
        }
        this.previousPlayer = client.getCameraEntity();
        this.cacheInventory(client);
        this.simulationActive = true;
        this.updateFakePlayer(client);
        this.applyInventoryChanges(client);
        this.updateFakePlayerState();
        client.setCameraEntity((Entity)this.fakePlayer);
        return true;
    }

    public void stopSimulation(MinecraftClient client) {
        this.simulationActive = false;
        this.restoreInventory(client);
        if (client != null && this.fakePlayer != null && client.getCameraEntity() == this.fakePlayer) {
            client.setCameraEntity((Entity)(client.player != null ? client.player : this.previousPlayer));
        }
        if (this.fakePlayer != null) {
            this.fakePlayer.discard();
            this.fakePlayer = null;
        }
        this.previousPlayer = null;
    }

    public void updateMovementAndPackets(MinecraftClient client) {
        if (!this.simulationActive || client == null || client.player == null || client.options == null) {
            return;
        }
        InventoryState inventoryState = this.botController.getInventoryState();
        inventoryState.setRotation(client.player.getYaw(), client.player.getPitch());
        double d = this.getInputDirection(client.options.forwardKey.isPressed()) - this.getInputDirection(client.options.backKey.isPressed());
        double d2 = this.getInputDirection(client.options.rightKey.isPressed()) - this.getInputDirection(client.options.leftKey.isPressed());
        boolean bl = client.options.sprintKey.isPressed() && d > 0.0 && !client.options.sneakKey.isPressed();
        boolean bl2 = client.options.sneakKey.isPressed();
        this.botController.setSecondaryActionActive(bl);
        this.botController.setPrimaryActionActive(bl2);
        this.syncSelectedSlot(client, inventoryState);
        this.applyMovementState(inventoryState, d, d2, bl, bl2, client.options.jumpKey.isPressed());
        this.sendPeriodicPackets(client);
    }

    private void applyMovementState(InventoryState inventoryState, double d, double d2, boolean bl, boolean bl2, boolean bl3) {
        double d3 = Math.sqrt(d * d + d2 * d2);
        if (d3 > 1.0) {
            d /= d3;
            d2 /= d3;
        }
        double d4 = this.botController.getControlState().getBaseMovementSpeed();
        if (bl) {
            d4 *= this.botController.getControlState().getSprintSpeedMultiplier();
        } else if (bl2) {
            d4 *= this.botController.getControlState().getSneakSpeedMultiplier();
        }
        double d5 = Math.toRadians(inventoryState.getYaw());
        double d6 = Math.sin(d5);
        double d7 = Math.cos(d5);
        Vec3d VanillaChestLootTableGenerator = inventoryState.getPosition();
        double d8 = (-d6 * d + d7 * d2) * d4;
        double d9 = (d7 * d + d6 * d2) * d4;
        this.jumpPressed = bl3;
        if (inventoryState.isFlying()) {
            double d10 = 0.0;
            double d11 = Math.max(this.botController.getControlState().getVerticalMovementStep(), (double)inventoryState.getFlySpeed());
            if (bl3) {
                d10 += d11;
            }
            if (bl2) {
                d10 -= d11;
            }
            double d12 = this.botController.getControlState().getMovementEpsilon();
            if (Math.abs(d8) > d12 || Math.abs(d9) > d12 || Math.abs(d10) > d12) {
                inventoryState.setPosition(VanillaChestLootTableGenerator.x + d8, VanillaChestLootTableGenerator.y + d10, VanillaChestLootTableGenerator.z + d9);
                inventoryState.setOnGround(false);
            }
            return;
        }
        if (bl3) {
            this.botController.processInventoryActions();
        }
        double d13 = this.botController.getControlState().getMovementEpsilon();
        if (Math.abs(d8) > d13 || Math.abs(d9) > d13) {
            inventoryState.setPosition(VanillaChestLootTableGenerator.x + d8, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z + d9);
        }
    }

    private void sendPeriodicPackets(MinecraftClient client) {
        long l = System.currentTimeMillis();
        long l2 = this.botController.getControlState().getActionRepeatIntervalMillis();
        boolean bl = client.options.attackKey.isPressed();
        boolean bl2 = client.options.useKey.isPressed();
        if (bl) {
            boolean bl3 = this.botController.isBlockTargetReachable();
            if (bl3 || l - this.lastAttackPacketTime >= l2) {
                this.botController.attackTargetAtCrosshair();
                this.lastAttackPacketTime = l;
            }
        } else {
            this.botController.abortBlockBreaking();
        }
        if (bl2 && l - this.lastUsePacketTime >= l2) {
            this.botController.interactWithTargetBlock();
            this.lastUsePacketTime = l;
        }
        if (client.options.dropKey.isPressed() && l - this.lastPickBlockTime >= l2) {
            this.botController.setUsingItem(false);
            this.lastPickBlockTime = l;
        }
    }

    public void setPlayerPosition(Vec3d VanillaChestLootTableGenerator) {
        if (VanillaChestLootTableGenerator != null) {
            this.botController.lookAtPosition(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z);
        }
    }

    public void updatePosition(Vec3d VanillaChestLootTableGenerator, double d) {
        this.botController.moveTowardPosition(VanillaChestLootTableGenerator, d);
    }

    public void setPosition(double d, double d2, double d3) {
        this.botController.setPositionAndEnableUse(d, d2, d3);
    }

    public void sendPositionPacket() {
        this.botController.processInventoryActions();
    }

    public void setSprinting(boolean bl) {
        this.botController.setPrimaryActionActive(bl);
    }

    public void setSneaking(boolean bl) {
        this.botController.setSecondaryActionActive(bl);
    }

    public void sendHandSwing(Hand class_12682) {
        this.botController.getConnection().sendPacket((Packet<?>)new HandSwingC2SPacket(class_12682 == null ? Hand.MAIN_HAND : class_12682));
    }

    public void setUsingItem(boolean bl) {
        this.botController.setUsingItem(bl);
    }

    public void sendReleaseUseItem() {
        this.botController.getConnection().sendPacket((Packet<?>)new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
    }

    public void sendSelectedSlot() {
        if (!this.botController.isConnected() || !this.botController.getInventoryState().hasOpenContainer()) {
            return;
        }
        int n = this.botController.getInventoryState().getContainerId();
        this.botController.getConnection().sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(n));
        this.botController.getInventoryState().closeContainerIfMatches(n);
    }

    public boolean isSlotAvailable(int n) {
        return this.botController.clickInventorySlot(n);
    }

    public void queueClickAction(int n, int n2, SlotActionType class_17132, long l) {
        long l2 = System.currentTimeMillis() + Math.max(0L, l);
        this.pendingClickActions.add(new ClickAction(n, n2, class_17132 == null ? SlotActionType.PICKUP : class_17132, l2));
    }

    public boolean canUseItem(Item class_17922) {
        return this.botController.selectHotbarItem(class_17922);
    }

    public ItemStack getHandStack(Hand class_12682) {
        return class_12682 == Hand.OFF_HAND ? this.offHandStack : this.mainHandStack;
    }

    public Vec3d getPlayerPosition() {
        return this.botController.getEyePosition();
    }

    public Vec3d getServerPosition() {
        return this.botController.getLookDirection();
    }

    public boolean hasMovementState() {
        return this.movementSpeed > 0.0f;
    }

    public boolean isMovementStateStale() {
        return !this.hasMovementState();
    }

    private double getInputDirection(boolean bl) {
        return bl ? 1.0 : 0.0;
    }

    private void processQueuedClicks() {
        if (this.pendingClickActions.isEmpty()) {
            return;
        }
        long l = System.currentTimeMillis();
        Iterator<ClickAction> iterator = this.pendingClickActions.iterator();
        while (iterator.hasNext()) {
            ClickAction clickAction = iterator.next();
            if (l < clickAction.getExecuteAt()) continue;
            this.botController.clickInventorySlotWithAction(clickAction.getSlot(), clickAction.getButton(), clickAction.getClickType());
            iterator.remove();
        }
    }

    private void sendMovementPackets() {
        boolean bl;
        if (!this.botController.isConnected()) {
            return;
        }
        InventoryState inventoryState = this.botController.getInventoryState();
        ++this.movementPacketTick;
        double d = inventoryState.getPositionX() - this.lastX;
        double d2 = inventoryState.getPositionY() - this.lastY;
        double d3 = inventoryState.getPositionZ() - this.lastZ;
        float f = MathHelper.wrapDegrees((float)(inventoryState.getYaw() - this.lastSentYaw));
        float f2 = inventoryState.getPitch() - this.lastSentPitch;
        boolean bl2 = this.movementPacketTick >= Math.max(1, this.botController.getControlState().getMovementPacketIntervalTicks());
        boolean bl3 = d * d + d2 * d2 + d3 * d3 > this.botController.getControlState().getPositionChangeThreshold() || bl2;
        boolean bl4 = (double)Math.abs(f) > this.botController.getControlState().getRotationChangeThreshold() || (double)Math.abs(f2) > this.botController.getControlState().getRotationChangeThreshold();
        boolean bl5 = bl = inventoryState.isOnGround() != this.lastOnGround;
        if (bl3 && bl4) {
            this.botController.getConnection().sendPacket((Packet<?>)new PlayerMoveC2SPacket.Full(inventoryState.getPositionX(), inventoryState.getPositionY(), inventoryState.getPositionZ(), inventoryState.getYaw(), inventoryState.getPitch(), inventoryState.isOnGround(), false));
        } else if (bl3) {
            this.botController.getConnection().sendPacket((Packet<?>)new PlayerMoveC2SPacket.PositionAndOnGround(inventoryState.getPositionX(), inventoryState.getPositionY(), inventoryState.getPositionZ(), inventoryState.isOnGround(), false));
        } else if (bl4) {
            this.botController.getConnection().sendPacket((Packet<?>)new PlayerMoveC2SPacket.LookAndOnGround(inventoryState.getYaw(), inventoryState.getPitch(), inventoryState.isOnGround(), false));
        } else if (bl) {
            this.botController.getConnection().sendPacket((Packet<?>)new PlayerMoveC2SPacket.OnGroundOnly(inventoryState.isOnGround(), false));
        } else {
            return;
        }
        if (bl3) {
            this.lastX = inventoryState.getPositionX();
            this.lastY = inventoryState.getPositionY();
            this.lastZ = inventoryState.getPositionZ();
            this.movementPacketTick = 0;
        }
        if (bl4) {
            this.lastSentYaw = inventoryState.getYaw();
            this.lastSentPitch = inventoryState.getPitch();
        }
        this.lastOnGround = inventoryState.isOnGround();
        inventoryState.savePositionSnapshot();
    }

    private void updateFakePlayer(MinecraftClient client) {
        if (this.fakePlayer != null && this.fakePlayer.getWorld() == client.world && !this.fakePlayer.isRemoved()) {
            return;
        }
        if (this.fakePlayer != null) {
            this.fakePlayer.discard();
        }
        UUID uUID = UUID.nameUUIDFromBytes(("BarneyBotCamera:" + this.botController.getBotName()).getBytes(StandardCharsets.UTF_8));
        this.fakePlayer = new OtherClientPlayerEntity(client.world, new GameProfile(uUID, this.botController.getBotName()));
        client.world.addEntity(this.fakePlayer);
    }

    private void cacheInventory(MinecraftClient client) {
        if (client == null || client.player == null || this.inventorySnapshot != null) {
            return;
        }
        PlayerInventory class_16612 = client.player.getInventory();
        this.inventorySnapshot = new ItemStack[class_16612.size()];
        for (int i = 0; i < this.inventorySnapshot.length; ++i) {
            this.inventorySnapshot[i] = this.copyItemStack(class_16612.getStack(i));
        }
        this.selectedHotbarSlot = class_16612.selectedSlot;
    }

    private void restoreInventory(MinecraftClient client) {
        if (client == null || client.player == null || this.inventorySnapshot == null) {
            this.inventorySnapshot = null;
            return;
        }
        PlayerInventory class_16612 = client.player.getInventory();
        for (int i = 0; i < this.inventorySnapshot.length && i < class_16612.size(); ++i) {
            class_16612.setStack(i, this.copyItemStack(this.inventorySnapshot[i]));
        }
        class_16612.selectedSlot = MathHelper.clamp((int)this.selectedHotbarSlot, (int)0, (int)8);
        class_16612.markDirty();
        this.inventorySnapshot = null;
    }

    private void applyInventoryChanges(MinecraftClient client) {
        if (client == null || client.player == null) {
            return;
        }
        InventoryState inventoryState = this.botController.getInventoryState();
        PlayerInventory class_16612 = client.player.getInventory();
        ItemStack[] class_1799Array = inventoryState.getInventoryItems();
        for (int i = 0; i < class_1799Array.length && i < 36 && i < class_16612.size(); ++i) {
            class_16612.setStack(i, this.copyItemStack(class_1799Array[i]));
        }
        ItemStack[] class_1799Array2 = inventoryState.getArmorItems();
        for (int i = 0; i < class_1799Array2.length; ++i) {
            int n = 36 + i;
            if (n >= class_16612.size()) continue;
            class_16612.setStack(n, this.copyItemStack(class_1799Array2[i]));
        }
        if (40 < class_16612.size()) {
            class_16612.setStack(40, this.copyItemStack(inventoryState.getOffhandStack()));
        }
        class_16612.selectedSlot = MathHelper.clamp((int)inventoryState.getSelectedHotbarSlot(), (int)0, (int)8);
        class_16612.markDirty();
    }

    private void syncSelectedSlot(MinecraftClient client, InventoryState inventoryState) {
        if (client == null || client.player == null || inventoryState == null) {
            return;
        }
        int n = MathHelper.clamp((int)client.player.getInventory().selectedSlot, (int)0, (int)8);
        if (n != inventoryState.getSelectedHotbarSlot()) {
            this.botController.selectHotbarSlot(n);
        }
    }

    private ItemStack copyItemStack(ItemStack class_17992) {
        return class_17992 == null ? ItemStack.EMPTY : class_17992.copy();
    }

    private void updateFakePlayerState() {
        if (!this.simulationActive || this.fakePlayer == null || this.fakePlayer.isRemoved()) {
            return;
        }
        this.fakePlayer.updatePositionAndAngles(
            this.botController.getInventoryState().getPositionX(),
            this.botController.getInventoryState().getPositionY(),
            this.botController.getInventoryState().getPositionZ(),
            this.botController.getInventoryState().getYaw(),
            this.botController.getInventoryState().getPitch()
        );
        this.fakePlayer.setOnGround(this.botController.getInventoryState().isOnGround());
    }

    @Generated
    public BotController getBotController() {
        return this.botController;
    }

    @Generated
    public BotWorldState getBotWorldState() {
        return this.botWorldState;
    }

    @Generated
    public Vec3d getCachedPosition() {
        return this.playerPosition;
    }

    @Generated
    public float getYaw() {
        return this.currentYaw;
    }

    @Generated
    public float getPitch() {
        return this.currentPitch;
    }

    @Generated
    public boolean isScreenReady() {
        return this.screenReady;
    }

    @Generated
    public boolean isInventoryReady() {
        return this.inventoryReady;
    }

    @Generated
    public boolean isJumpPressed() {
        return this.jumpPressed;
    }

    @Generated
    public boolean isCooldownReady() {
        return this.cooldownReady;
    }

    @Generated
    public boolean isActionReady() {
        return this.actionReady;
    }

    @Generated
    public boolean isWorldReady() {
        return this.worldReady;
    }

    @Generated
    public float getMovementSpeed() {
        return this.movementSpeed;
    }

    @Generated
    public int getUpdateTick() {
        return this.stateUpdateTick;
    }

    @Generated
    public int getSlotIndex() {
        return this.slotIndex;
    }

    @Generated
    public ItemStack getMainHandStack() {
        return this.mainHandStack;
    }

    @Generated
    public ItemStack getOffHandStack() {
        return this.offHandStack;
    }

    @Generated
    public boolean isSimulationActive() {
        return this.simulationActive;
    }

    @Generated
    public OtherClientPlayerEntity getFakePlayer() {
        return this.fakePlayer;
    }

    @Generated
    public Entity getPreviousPlayer() {
        return this.previousPlayer;
    }

    @Generated
    public ItemStack[] getInventorySnapshot() {
        return this.inventorySnapshot;
    }

    @Generated
    public int getSelectedHotbarSlot() {
        return this.selectedHotbarSlot;
    }

    @Generated
    public long getLastAttackPacketTime() {
        return this.lastAttackPacketTime;
    }

    @Generated
    public long getLastUsePacketTime() {
        return this.lastUsePacketTime;
    }

    @Generated
    public long getLastPickBlockTime() {
        return this.lastPickBlockTime;
    }

    @Generated
    public double getLastX() {
        return this.lastX;
    }

    @Generated
    public double getLastY() {
        return this.lastY;
    }

    @Generated
    public double getLastZ() {
        return this.lastZ;
    }

    @Generated
    public float getLastYaw() {
        return this.lastSentYaw;
    }

    @Generated
    public float getLastPitch() {
        return this.lastSentPitch;
    }

    @Generated
    public boolean isGrounded() {
        return this.lastOnGround;
    }

    @Generated
    public int getUpdateCounter() {
        return this.movementPacketTick;
    }

    @Generated
    public List<ClickAction> getPendingClickActions() {
        return this.pendingClickActions;
    }

    static final class ClickAction {
        private final int slot;
        private final int button;
        private final SlotActionType clickType;
        private final long executeAt;

        ClickAction(int n, int n2, SlotActionType class_17132, long l) {
            this.slot = n;
            this.button = n2;
            this.clickType = class_17132;
            this.executeAt = l;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "slot", "button", "clickType", "executeAt");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "slot", "button", "clickType", "executeAt");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "slot", "button", "clickType", "executeAt");
        }

        public int getSlot() {
            return this.slot;
        }

        public int getButton() {
            return this.button;
        }

        public SlotActionType getClickType() {
            return this.clickType;
        }

        public long getExecuteAt() {
            return this.executeAt;
        }
    }
}
