/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.PlayerEntity
 *  net.minecraft.SlotActionType
 *  net.minecraft.BlockItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.ShulkerBoxBlock
 *  net.minecraft.Packet
 *  net.minecraft.InventoryS2CPacket
 *  net.minecraft.ScreenHandlerSlotUpdateS2CPacket
 *  net.minecraft.ClickSlotC2SPacket
 *  net.minecraft.CloseHandledScreenC2SPacket
 *  net.minecraft.KeyBinding
 *  net.minecraft.InputUtil
 *  net.minecraft.ChatScreen
 *  net.minecraft.Screen
 *  net.minecraft.HandledScreen
 *  net.minecraft.AnvilScreen
 *  net.minecraft.CreativeInventoryScreen
 *  net.minecraft.InventoryScreen
 *  net.minecraft.SignEditScreen
 *  net.minecraft.ItemGroups
 */
package moscow.rockstar.modules.player.inventory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import lombok.Generated;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.ui.text.TextInputField;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.item.ItemGroups;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.network.SendPacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.player.InputEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Gui Move", category=ModuleCategory.PLAYER, disableLocked=true)
public class InventoryMove
extends Module {
    private ModeSetting obkhodMode;
    private ModeSetting.Option vanilla;
    private ModeSetting.Option cakeOption;
    private ModeSetting.Option hwRWOption;
    private ModeSetting.Option funtime;
    private ModeSetting.Option spookytimeOption;
    private final List<Packet<?>> modeEntries = new ArrayList();
    private int pendingPacketDelayTicks;
    private boolean overlayVisible;
    private final Timer cooldownTimer = new Timer();
    private int windowId;
    private boolean privilegedMode;
    private boolean disableLocked;
    private boolean alwaysEnabled;
    private int lastWindowId;
    private boolean inventoryOpen;
    private boolean screenPending;
    public int inventoryOpenTicks;
    public boolean movementInput;
    private final Queue<ClickSlotC2SPacket> queuedPackets = new LinkedList<ClickSlotC2SPacket>();
    private final EventListener<SendPacketEvent> onSendPacketEventListener = sendPacketEvent -> {
        Packet<?> class_25962;
        if (this.disableLocked) {
            return;
        }
        if (this.obkhodMode.isSelected(this.spookytimeOption) && (class_25962 = sendPacketEvent.getPacket()) instanceof ClickSlotC2SPacket) {
            ClickSlotC2SPacket class_28132 = (ClickSlotC2SPacket)class_25962;
            if (!this.movementInput && this.isModeSelectedInitial() && class_28132.getSlot() != -1) {
                this.updatePacketAction(class_28132.getActionType());
                if (!this.inventoryOpen && this.isInventoryScreen(class_28132)) {
                    this.inventoryOpenTicks = 0;
                    this.alwaysEnabled = true;
                    this.cooldownTimer.reset();
                    sendPacketEvent.cancel();
                }
            }
            return;
        }
        class_25962 = sendPacketEvent.getPacket();
        if (class_25962 instanceof ClickSlotC2SPacket) {
            Item class_17922;
            ClickSlotC2SPacket class_28133 = (ClickSlotC2SPacket)class_25962;
            if (!this.isVanillaMode()) {
                return;
            }
            if (this.movementInput) {
                return;
            }
            if (InventoryMove.minecraftClient.currentScreen == null) {
                this.modeEntries.add((Packet<?>)class_28133);
                sendPacketEvent.cancel();
                this.overlayVisible = true;
                if (this.obkhodMode.isSelected(this.cakeOption)) {
                    this.cooldownTimer.reset();
                } else if (this.isCakeMode()) {
                    this.windowId = 2;
                    this.privilegedMode = true;
                    this.cooldownTimer.reset();
                } else if (this.funtime.isSelected()) {
                    this.alwaysEnabled = true;
                } else {
                    this.pendingPacketDelayTicks = 3;
                }
            }
            if (InventoryMove.minecraftClient.currentScreen instanceof InventoryScreen && EntityUtils.hasMovementInput()) {
                this.modeEntries.add((Packet<?>)class_28133);
                sendPacketEvent.cancel();
            }
            if ((class_17922 = class_28133.getStack().getItem()) instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock && ServerDetector.isServerProfileSupported(ServerProfile.SUPPORTED_NETWORKS)) {
                InventoryMove.minecraftClient.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            }
        }
        if (sendPacketEvent.getPacket() instanceof CloseHandledScreenC2SPacket) {
            if (!this.isVanillaMode()) {
                return;
            }
            if (this.obkhodMode.isSelected(this.spookytimeOption)) {
                return;
            }
            boolean bl = EntityUtils.hasMovementInput();
            if (bl) {
                if (!this.modeEntries.isEmpty()) {
                    this.overlayVisible = true;
                    if (this.obkhodMode.isSelected(this.cakeOption)) {
                        this.cooldownTimer.reset();
                    } else if (this.isCakeMode()) {
                        this.windowId = 2;
                        this.privilegedMode = true;
                        this.cooldownTimer.reset();
                    } else {
                        this.pendingPacketDelayTicks = 3;
                    }
                }
                sendPacketEvent.cancel();
            }
        }
    };
    private final EventListener<ReceivePacketEvent> onReceivePacketEventListener = receivePacketEvent -> {
        if ((this.isHypixelMode() || this.isFuntimeMode()) && this.alwaysEnabled && !this.cooldownTimer.hasElapsed(2000L) && (receivePacketEvent.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket || receivePacketEvent.getPacket() instanceof InventoryS2CPacket)) {
            this.alwaysEnabled = false;
            receivePacketEvent.cancel();
        }
    };
    private final EventListener<InputEvent> onInputEventListener = inputEvent -> {
        if (this.obkhodMode.isSelected(this.cakeOption) && !this.cooldownTimer.hasElapsed(190L)) {
            inputEvent.setForward(0.0f);
            inputEvent.setStrafe(0.0f);
            inputEvent.setJump(false);
            inputEvent.setSneak(false);
            inputEvent.setSprint(false);
        }
        if (this.isCakeMode() && this.windowId > 0 && !this.cooldownTimer.hasElapsed(195L)) {
            inputEvent.setForward(0.0f);
            inputEvent.setStrafe(0.0f);
            inputEvent.setJump(false);
            inputEvent.setSneak(false);
            inputEvent.setSprint(false);
        }
        if (this.obkhodMode.isSelected(this.spookytimeOption) && this.inventoryOpen) {
            inputEvent.setForward(0.0f);
            inputEvent.setStrafe(0.0f);
            inputEvent.setJump(false);
            inputEvent.setSneak(false);
            inputEvent.setSprint(false);
        }
    };
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEventListener = clientPlayerTickEvent -> {
        if (this.pendingPacketDelayTicks > 0) {
            --this.pendingPacketDelayTicks;
        }
        if (this.windowId > 0) {
            --this.windowId;
        }
        if (this.obkhodMode.isSelected(this.spookytimeOption)) {
            if (this.screenPending && this.inventoryOpen && this.lastWindowId > 0) {
                this.resetPacketQueue();
            }
            this.screenPending = this.inventoryOpen;
            if (this.isModeReady()) {
                this.updatePacketAction(null);
            } else if (this.lastWindowId > 0) {
                --this.lastWindowId;
            }
            this.inventoryOpen = this.lastWindowId > 0;
            ++this.inventoryOpenTicks;
            if (this.inventoryOpen) {
                this.inventoryOpenTicks = 0;
            }
        }
        if (this.obkhodMode.isSelected(this.cakeOption) && !this.cooldownTimer.hasElapsed(100L)) {
            return;
        }
        if (this.isCakeMode() && this.windowId > 0 && !this.cooldownTimer.hasElapsed(ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) ? 120L : 99L)) {
            return;
        }
        if (this.obkhodMode.isSelected(this.spookytimeOption) && this.inventoryOpen) {
            return;
        }
        if (!(this.vanilla.isSelected() || this.obkhodMode.isSelected(this.spookytimeOption) || InventoryMove.minecraftClient.currentScreen == null || InventoryMove.minecraftClient.currentScreen instanceof InventoryScreen || InventoryMove.minecraftClient.currentScreen instanceof CreativeInventoryScreen)) {
            return;
        }
        InventoryMove.resetInputState();
        if (this.overlayVisible && this.obkhodMode.isSelected(this.cakeOption)) {
            minecraftClient.setScreen((Screen)new InventoryScreen((PlayerEntity)InventoryMove.minecraftClient.player));
            this.resetWindowState();
            this.overlayVisible = false;
            minecraftClient.setScreen(null);
        }
        if (this.privilegedMode && this.isCakeMode() && this.windowId == 0) {
            this.resetWindowState();
            this.privilegedMode = false;
            this.overlayVisible = false;
        }
    };

    public InventoryMove() {
        this.initializeMovementRules();
    }

    @Compile(obfuscation=4)
    private void initializeMovementRules() {
        this.obkhodMode = new ModeSetting(this, "Bypass");
        this.vanilla = new ModeSetting.Option(this.obkhodMode, "Vanilla").select();
        this.cakeOption = new ModeSetting.Option(this.obkhodMode, "Lony/Cake");
        this.hwRWOption = new ModeSetting.Option(this.obkhodMode, "HW&RW");
        this.funtime = new ModeSetting.Option(this.obkhodMode, "FunTime");
        this.spookytimeOption = new ModeSetting.Option(this.obkhodMode, "SpookyTime");
    }

    private boolean isVanillaMode() {
        return this.obkhodMode.isSelected(this.cakeOption) || this.isCakeMode() || this.obkhodMode.isSelected(this.spookytimeOption);
    }

    private boolean isCakeMode() {
        return this.obkhodMode.isSelected(this.hwRWOption) || this.obkhodMode.isSelected(this.funtime);
    }

    public boolean isHypixelMode() {
        return this.obkhodMode.isSelected(this.funtime);
    }

    public boolean isFuntimeMode() {
        return this.isEnabled() && this.obkhodMode.isSelected(this.spookytimeOption);
    }

    public boolean isSpookytimeMode() {
        return !this.isEnabled() || this.inventoryOpenTicks > 0;
    }

    public boolean isReallyworldMode() {
        return !this.queuedPackets.isEmpty();
    }

    private boolean isModeSelectedInitial() {
        return InventoryMove.minecraftClient.player.input.movementForward != 0.0f || InventoryMove.minecraftClient.player.input.movementSideways != 0.0f || this.inventoryOpen;
    }

    private int getClickDelay(SlotActionType class_17132) {
        return class_17132 == SlotActionType.PICKUP ? 1 : (this.lastWindowId > 1 ? 2 : 3);
    }

    private void updatePacketAction(SlotActionType class_17132) {
        if (this.isEnabled() && this.obkhodMode.isSelected(this.spookytimeOption)) {
            this.lastWindowId = this.getClickDelay(class_17132 == null ? SlotActionType.PICKUP : class_17132) + 1;
        }
    }

    public void resetInventoryMovement() {
        if (this.isEnabled() && this.obkhodMode.isSelected(this.spookytimeOption)) {
            this.lastWindowId = 8;
        }
    }

    private boolean isModeReady() {
        if (!(InventoryMove.minecraftClient.currentScreen instanceof HandledScreen)) {
            return false;
        }
        if (!this.isModeSelectedInitial()) {
            return false;
        }
        return !InventoryMove.minecraftClient.player.currentScreenHandler.getCursorStack().isEmpty();
    }

    private void resetPacketQueue() {
        if (this.queuedPackets.isEmpty()) {
            return;
        }
        this.disableLocked = true;
        while (!this.queuedPackets.isEmpty()) {
            ClickSlotC2SPacket class_28132 = this.queuedPackets.poll();
            if (class_28132 == null) continue;
            InventoryMove.minecraftClient.player.networkHandler.sendPacket((Packet)class_28132);
        }
        this.disableLocked = false;
        this.inventoryOpenTicks = 0;
    }

    private boolean isInventoryScreen(ClickSlotC2SPacket class_28132) {
        return !this.queuedPackets.contains(class_28132) && this.queuedPackets.add(class_28132);
    }

    public void updateInventoryMovement() {
        if (!this.isVanillaMode() || this.modeEntries.isEmpty()) {
            return;
        }
        if (this.obkhodMode.isSelected(this.spookytimeOption)) {
            return;
        }
        this.overlayVisible = true;
        if (this.obkhodMode.isSelected(this.cakeOption)) {
            this.cooldownTimer.reset();
        } else if (this.isCakeMode()) {
            this.windowId = 2;
            this.privilegedMode = true;
            this.cooldownTimer.reset();
        } else {
            this.pendingPacketDelayTicks = 3;
        }
    }

    private void resetWindowState() {
        if (this.modeEntries.isEmpty()) {
            return;
        }
        this.disableLocked = true;
        for (Packet<?> class_25962 : this.modeEntries) {
            InventoryMove.minecraftClient.player.networkHandler.sendPacket(class_25962);
        }
        InventoryMove.minecraftClient.player.networkHandler.sendPacket((Packet)new CloseHandledScreenC2SPacket(InventoryMove.minecraftClient.player.currentScreenHandler.syncId));
        this.modeEntries.clear();
        this.disableLocked = false;
    }

    @Override
    public void onDisable() {
        this.resetPacketQueue();
        this.inventoryOpenTicks = 1;
        this.queuedPackets.clear();
        this.lastWindowId = 0;
        this.inventoryOpen = false;
        this.screenPending = false;
        this.modeEntries.clear();
        this.pendingPacketDelayTicks = 0;
        this.windowId = 0;
        this.overlayVisible = false;
        this.privilegedMode = false;
        this.movementInput = false;
        this.disableLocked = false;
        super.onDisable();
    }

    @Override
    public void onEnable() {
        this.resetPacketQueue();
        this.queuedPackets.clear();
        this.inventoryOpenTicks = 1;
        super.onEnable();
    }

    public static void resetInputState() {
        KeyBinding[] class_304Array = new KeyBinding[]{InventoryMove.minecraftClient.options.forwardKey, InventoryMove.minecraftClient.options.backKey, InventoryMove.minecraftClient.options.leftKey, InventoryMove.minecraftClient.options.rightKey, InventoryMove.minecraftClient.options.jumpKey};
        if (InventoryMove.isInventoryMoveReady()) {
            return;
        }
        for (KeyBinding SourceConstructor : class_304Array) {
            int n = InputUtil.fromTranslationKey((String)SourceConstructor.getBoundKeyTranslationKey()).getCode();
            boolean bl = InputUtil.isKeyPressed((long)minecraftClient.getWindow().getHandle(), (int)n);
            SourceConstructor.setPressed(bl);
        }
    }

    private static boolean isInventoryMoveReady() {
        return InventoryMove.minecraftClient.currentScreen instanceof ChatScreen || InventoryMove.minecraftClient.currentScreen != null && TextInputField.getActiveField() != null && TextInputField.getActiveField().isFocused() || InventoryMove.minecraftClient.currentScreen instanceof SignEditScreen || InventoryMove.minecraftClient.currentScreen instanceof AnvilScreen || InventoryMove.minecraftClient.currentScreen instanceof CreativeInventoryScreen creativeScreen && creativeScreen.isInventoryTabSelected();
    }

    @Generated
    public ModeSetting getModeSetting() {
        return this.obkhodMode;
    }

    @Generated
    public ModeSetting.Option getVanillaOption() {
        return this.vanilla;
    }

    @Generated
    public ModeSetting.Option getCakeOption() {
        return this.cakeOption;
    }

    @Generated
    public ModeSetting.Option getHwRwOption() {
        return this.hwRWOption;
    }

    @Generated
    public ModeSetting.Option getFuntimeOption() {
        return this.funtime;
    }

    @Generated
    public ModeSetting.Option getSpookytimeOption() {
        return this.spookytimeOption;
    }

    @Generated
    public List<Packet<?>> getModeEntries() {
        return this.modeEntries;
    }

    @Generated
    public int getWindowId() {
        return this.pendingPacketDelayTicks;
    }

    @Generated
    public boolean isMovementAllowed() {
        return this.overlayVisible;
    }

    @Generated
    public Timer getCooldownTimer() {
        return this.cooldownTimer;
    }

    @Generated
    public int getLastWindowId() {
        return this.windowId;
    }

    @Generated
    public boolean isScreenReady() {
        return this.privilegedMode;
    }

    @Generated
    public boolean isInputReady() {
        return this.disableLocked;
    }

    @Generated
    public boolean isPacketReady() {
        return this.alwaysEnabled;
    }

    @Generated
    public int getModeIndex() {
        return this.lastWindowId;
    }

    @Generated
    public boolean isInventoryOpen() {
        return this.inventoryOpen;
    }

    @Generated
    public boolean isPlayerReady() {
        return this.screenPending;
    }

    /** NOT an override: the original {@code rockstar/ilIlil/IIIiIiIII} declares no keybind
     *  getter, so the inherited {@link Module#getKeyBind()} must stay visible. */
    @Generated
    public int getInventoryOpenTicks() {
        return this.inventoryOpenTicks;
    }

    @Generated
    public boolean isInventoryMoveEnabled() {
        return this.movementInput;
    }

    @Generated
    public Queue<ClickSlotC2SPacket> getQueuedPackets() {
        return this.queuedPackets;
    }

    @Generated
    public EventListener<SendPacketEvent> getSendPacketListener() {
        return this.onSendPacketEventListener;
    }

    @Generated
    public EventListener<ReceivePacketEvent> getReceivePacketListener() {
        return this.onReceivePacketEventListener;
    }

    @Generated
    public EventListener<InputEvent> getInputListener() {
        return this.onInputEventListener;
    }

    @Generated
    public EventListener<ClientPlayerTickEvent> getClientTickListener() {
        return this.onClientPlayerTickEventListener;
    }
}
