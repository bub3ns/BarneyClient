/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Inventory
 *  net.minecraft.Hand
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ScreenHandler
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.Direction
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockEntity
 *  net.minecraft.EnderChestBlockEntity
 *  net.minecraft.BlockHitResult
 *  net.minecraft.GenericContainerScreen
 */
package moscow.rockstar.modules.player.inventory;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import moscow.rockstar.entity.tracking.BlockEntityTracker;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Stealer", category=ModuleCategory.PLAYER)
public class Stealer
extends Module {
    private BooleanSetting instantStealSetting;
    private NumberSetting delaySetting;
    private BooleanSetting closeAfterStealSetting;
    private BooleanSetting disableAfterStealSetting;
    private BooleanSetting openMysticSetting;
    private ModeSetting stealOrderSetting;
    private ModeSetting.Option topToBottomOrder;
    private ModeSetting.Option bottomToTopOrder;
    private ModeSetting.Option centerOutOrder;
    private ModeSetting.Option randomOrder;
    private final Timer stealDelayTimer = new Timer();
    private final Timer mysticBlockTimer = new Timer();
    private final List<EnderChestBlockEntity> processedContainers = new ArrayList<EnderChestBlockEntity>();
    private EnderChestBlockEntity targetShulker;

    public Stealer() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.instantStealSetting = new BooleanSetting(this, "modules.settings.stealer.instant");
        this.delaySetting = new NumberSetting((SettingOwner)this, "modules.settings.stealer.delay", this.instantStealSetting::isEnabled).setMinValue(0.0f).setMaxValue(1000.0f).setStep(1.0f).setValue(400.0f);
        this.closeAfterStealSetting = new BooleanSetting((SettingOwner)this, "modules.settings.stealer.close", "modules.settings.stealer.close.description");
        this.disableAfterStealSetting = new BooleanSetting((SettingOwner)this, "modules.settings.stealer.off", "modules.settings.stealer.off.description");
        this.openMysticSetting = new BooleanSetting((SettingOwner)this, "modules.settings.stealer.open_mystic", "modules.settings.stealer.open_mystic.description");
        this.stealOrderSetting = new ModeSetting(this, "modules.settings.stealer.mode");
        this.topToBottomOrder = new ModeSetting.Option(this.stealOrderSetting, "modules.settings.stealer.mode.up").select();
        this.bottomToTopOrder = new ModeSetting.Option(this.stealOrderSetting, "modules.settings.stealer.mode.down");
        this.centerOutOrder = new ModeSetting.Option(this.stealOrderSetting, "modules.settings.stealer.mode.center");
        this.randomOrder = new ModeSetting.Option(this.stealOrderSetting, "modules.settings.stealer.mode.random");
    }

    @Override
    public void onTick() {
        ScreenHandler class_17032;
        if (Stealer.minecraftClient.currentScreen instanceof GenericContainerScreen && (class_17032 = Stealer.minecraftClient.player.currentScreenHandler) instanceof GenericContainerScreenHandler) {
            int n;
            GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)class_17032;
            int n2 = class_17072.getInventory().size();
            if (this.instantStealSetting.isEnabled()) {
                for (int i = 0; i < n2; ++i) {
                    if (class_17072.getSlot(i).getStack().isEmpty()) continue;
                    Stealer.minecraftClient.interactionManager.clickSlot(class_17072.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)Stealer.minecraftClient.player);
                }
            } else if (this.stealOrderSetting.isSelected(this.topToBottomOrder)) {
                for (int i = 0; i < n2 && this.stealDelayTimer.hasElapsed((long)(this.delaySetting.getValue() + MathUtils.interpolateRandomDouble(-100.0, 100.0))); ++i) {
                    if (class_17072.getSlot(i).getStack().isEmpty()) continue;
                    Stealer.minecraftClient.interactionManager.clickSlot(class_17072.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)Stealer.minecraftClient.player);
                    this.stealDelayTimer.reset();
                }
            } else if (this.stealOrderSetting.isSelected(this.bottomToTopOrder)) {
                for (int i = n2 - 1; i >= 0 && this.stealDelayTimer.hasElapsed((long)(this.delaySetting.getValue() + MathUtils.interpolateRandomDouble(-100.0, 100.0))); --i) {
                    if (class_17072.getSlot(i).getStack().isEmpty()) continue;
                    Stealer.minecraftClient.interactionManager.clickSlot(class_17072.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)Stealer.minecraftClient.player);
                    this.stealDelayTimer.reset();
                }
            } else if (this.stealOrderSetting.isSelected(this.centerOutOrder)) {
                int n3 = n2 / 2;
                for (int i = 0; i <= n3 && this.stealDelayTimer.hasElapsed((long)(this.delaySetting.getValue() + MathUtils.interpolateRandomDouble(-100.0, 100.0))); ++i) {
                    int n4 = n3 - i;
                    int n5 = n3 + i;
                    if (n4 >= 0 && !class_17072.getSlot(n4).getStack().isEmpty()) {
                        Stealer.minecraftClient.interactionManager.clickSlot(class_17072.syncId, n4, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)Stealer.minecraftClient.player);
                        this.stealDelayTimer.reset();
                        continue;
                    }
                    if (n5 >= n2 || class_17072.getSlot(n5).getStack().isEmpty()) continue;
                    Stealer.minecraftClient.interactionManager.clickSlot(class_17072.syncId, n5, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)Stealer.minecraftClient.player);
                    this.stealDelayTimer.reset();
                }
            } else if (this.stealOrderSetting.isSelected(this.randomOrder) && this.stealDelayTimer.hasElapsed((long)(this.delaySetting.getValue() + MathUtils.interpolateRandomDouble(-100.0, 100.0))) && !class_17072.getSlot(n = (int)MathUtils.interpolateRandomDouble(0.0, n2)).getStack().isEmpty()) {
                Stealer.minecraftClient.interactionManager.clickSlot(class_17072.syncId, n, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)Stealer.minecraftClient.player);
                this.stealDelayTimer.reset();
            }
            if (this.isContainerEmpty(class_17072)) {
                if (this.targetShulker != null && this.openMysticSetting.isEnabled()) {
                    this.processedContainers.add(this.targetShulker);
                }
                if (this.disableAfterStealSetting.isEnabled()) {
                    this.toggle();
                }
                if (this.closeAfterStealSetting.isEnabled()) {
                    Stealer.minecraftClient.player.closeHandledScreen();
                }
            }
            return;
        }
        if (!this.openMysticSetting.isEnabled()) {
            return;
        }
        if (this.targetShulker == null || !this.isValidShulker(this.targetShulker)) {
            this.targetShulker = this.findNearestShulker();
            this.mysticBlockTimer.reset();
        }
        if (this.targetShulker != null && this.mysticBlockTimer.hasElapsed(200L)) {
            BlockPos adminsky = this.targetShulker.getPos();
            Vec3d targetPosition = Vec3d.ofCenter((Vec3i)adminsky);
            Rotation rotation = AimRotationMath.getRotationToPoint(targetPosition);
            RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.NONE, 22.0f, 22.0f, 22.0f, RotationPriority.ITEM_USE_PRIORITY);
            BlockHitResult class_39652 = new BlockHitResult(targetPosition, Direction.UP, adminsky, false);
            Stealer.minecraftClient.interactionManager.interactBlock(Stealer.minecraftClient.player, Hand.MAIN_HAND, class_39652);
            Stealer.minecraftClient.player.swingHand(Hand.MAIN_HAND);
            this.mysticBlockTimer.reset();
        }
    }

    private boolean isValidShulker(EnderChestBlockEntity class_26112) {
        return Stealer.minecraftClient.player.squaredDistanceTo(class_26112.getPos().toCenterPos()) < 16.0 && !this.processedContainers.contains(class_26112);
    }

    private EnderChestBlockEntity findNearestShulker() {
        for (BlockEntity class_25862 : BlockEntityTracker.getTrackedEntities()) {
            EnderChestBlockEntity class_26112;
            if (!(class_25862 instanceof EnderChestBlockEntity) || !this.isValidShulker(class_26112 = (EnderChestBlockEntity)class_25862)) continue;
            return class_26112;
        }
        return null;
    }

    private boolean isContainerEmpty(GenericContainerScreenHandler class_17072) {
        Inventory class_12632 = class_17072.getInventory();
        for (int i = 0; i < class_12632.size(); ++i) {
            if (class_12632.getStack(i).isEmpty()) continue;
            return false;
        }
        return true;
    }
}
