/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.LivingEntity
 *  net.minecraft.FishingBobberEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.SlotActionType
 *  net.minecraft.BlockItem
 *  net.minecraft.FishingRodItem
 *  net.minecraft.ItemStack
 *  net.minecraft.Blocks
 *  net.minecraft.Direction
 *  net.minecraft.Packet
 *  net.minecraft.BlockState
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.PlayerInteractBlockC2SPacket
 *  net.minecraft.BlockHitResult
 *  net.minecraft.DeathScreen
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.modules.player.util;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.InventorySlotRule;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.attacks.Aura;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.gui.screen.DeathScreen;
import org.jetbrains.annotations.NotNull;
import pyrock.events.game.BlockBreakEvent;
import pyrock.events.game.InternalAttackEvent;
import pyrock.events.game.StartBreakBlockEvent;
import pyrock.events.player.InputEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Player Utils", category=ModuleCategory.PLAYER, description="modules.descriptions.player_utils")
public class PlayerUtils
extends Module {
    private MultiBooleanSetting selectSetting;
    private MultiBooleanSetting.Option autoRespawn;
    private MultiBooleanSetting.Option autoFish;
    private MultiBooleanSetting.Option fastLadder;
    private MultiBooleanSetting.Option noFriendDamage;
    private MultiBooleanSetting.Option fastBreak;
    private MultiBooleanSetting.Option blockTrap;
    private MultiBooleanSetting.Option antiAfk;
    private MultiBooleanSetting.Option autoTool;
    private ModeSetting mode;
    private ModeSetting.Option send;
    private ModeSetting.Option jump;
    private ModeSetting.Option swing;
    private NumberSetting delay;
    private final Timer cooldownTimer = new Timer();
    private final Timer actionTimer = new Timer();
    private boolean utilityActive;
    private boolean overlayVisible;
    private boolean privilegedMode;
    private boolean disableLocked;
    private boolean alwaysEnabled;
    private int selectedToolSlot = -1;
    private int previousHotbarSlot = -1;
    private ItemRule itemRule;
    private long lastActionTime = -1L;
    private long lastAttackTime;
    private long lastBreakTime = -1L;
    private long lastInputTime;
    private int actionCount = -1;
    private final EventListener<InternalAttackEvent> onInternalAttackEvent = internalAttackEvent -> {
        if (this.noFriendDamage.isSelected() && internalAttackEvent.getEntity() instanceof PlayerEntity && RockstarClient.create().getFriendListManager().containsFriend(internalAttackEvent.getEntity().getName().getString())) {
            internalAttackEvent.cancel();
        }
    };
    private final EventListener<StartBreakBlockEvent> onStartBreakBlockEvent = startBreakBlockEvent -> {
        ItemRule itemRule;
        if (!this.autoTool.isSelected() || PlayerUtils.minecraftClient.player == null || PlayerUtils.minecraftClient.world == null) {
            return;
        }
        BlockState class_26802 = PlayerUtils.minecraftClient.world.getBlockState(startBreakBlockEvent.getBlockPos());
        if (class_26802.isAir()) {
            return;
        }
        ItemStack class_17992 = PlayerUtils.minecraftClient.player.getMainHandStack();
        float f = this.calculateToolEfficiency(class_17992, class_26802);
        ItemRule itemRule2 = this.getBlockItemRule(class_26802, f);
        HotbarSlot hotbarSlot = InventoryUtils.getSelectedHotbarSlot();
        if (itemRule2 == null || itemRule2.equals(hotbarSlot)) {
            return;
        }
        if (this.disableLocked) {
            this.resetActionState();
        }
        this.selectedToolSlot = PlayerUtils.minecraftClient.player.getInventory().selectedSlot;
        if (this.disableLocked) {
            itemRule = this.getBlockItemRule(class_26802, f);
            if (itemRule == null || itemRule.equals(hotbarSlot)) {
                return;
            }
            this.resetActionState();
        }
        if (itemRule2 instanceof HotbarSlot) {
            itemRule = (HotbarSlot)itemRule2;
            if (((HotbarSlot)itemRule).getSlotIndex() == this.selectedToolSlot) {
                return;
            }
            this.previousHotbarSlot = ((HotbarSlot)itemRule).getSlotIndex();
            InventoryUtils.setSelectedHotbarSlot((HotbarSlot)itemRule);
            this.alwaysEnabled = false;
            this.disableLocked = true;
            return;
        }
        if (itemRule2 instanceof InventorySlotRule) {
            this.previousHotbarSlot = this.selectedToolSlot;
            this.itemRule = itemRule2;
            InventoryUtils.dropItem(itemRule2.getClickSlot(), this.previousHotbarSlot);
            this.alwaysEnabled = true;
            this.disableLocked = true;
        }
    };
    private final EventListener<BlockBreakEvent> onBlockBreakEvent = blockBreakEvent -> {
        if (!this.autoTool.isSelected()) {
            return;
        }
        if (this.disableLocked && PlayerUtils.minecraftClient.player != null && !PlayerUtils.minecraftClient.options.attackKey.isPressed()) {
            this.resetActionState();
        }
    };
    private final EventListener<InputEvent> onInputEvent = inputEvent -> {
        if (this.antiAfk.isSelected() && this.privilegedMode && this.jump.isSelected() && PlayerUtils.minecraftClient.player.isOnGround() && (float)PlayerUtils.minecraftClient.player.age % this.delay.getValue() == 5.0f) {
            inputEvent.setJump(true);
        }
    };

    public PlayerUtils() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.selectSetting = new MultiBooleanSetting(this, "modules.settings.player_utils.select_setting").setMaxSelections(1);
        this.autoRespawn = new MultiBooleanSetting.Option(this.selectSetting, "modules.settings.auto_respawn");
        this.autoFish = new MultiBooleanSetting.Option(this.selectSetting, "modules.settings.auto_fish");
        this.fastLadder = new MultiBooleanSetting.Option(this.selectSetting, "modules.settings.fast_ladder");
        this.noFriendDamage = new MultiBooleanSetting.Option(this.selectSetting, "modules.settings.no_friend_damage");
        this.fastBreak = new MultiBooleanSetting.Option(this.selectSetting, "modules.settings.fast_break");
        this.blockTrap = new MultiBooleanSetting.Option(this.selectSetting, "modules.settings.player_utils.block_trap");
        this.antiAfk = new MultiBooleanSetting.Option(this.selectSetting, "modules.settings.anti_afk");
        this.autoTool = new MultiBooleanSetting.Option(this.selectSetting, "modules.settings.auto_tool");
        this.mode = new ModeSetting((SettingOwner)this, "modules.settings.anti_afk.mode", () -> !this.antiAfk.isSelected());
        this.send = new ModeSetting.Option(this.mode, "modules.settings.anti_afk.send");
        this.jump = new ModeSetting.Option(this.mode, "modules.settings.anti_afk.jump");
        this.swing = new ModeSetting.Option(this.mode, "modules.settings.anti_afk.swing");
        this.delay = new NumberSetting(this, "modules.settings.anti_afk.delay", "modules.settings.anti_afk.delay.description", () -> !this.antiAfk.isSelected()).setMinValue(5.0f).setMaxValue(60.0f).setStep(5.0f).setValue(50.0f);
    }

    @Override
    public void onTick() {
        ItemRuleCollection<ItemRule> itemRuleCollection;
        ItemRule itemRule;
        LivingEntity class_13092;
        if (this.disableLocked && (PlayerUtils.minecraftClient.player == null || !PlayerUtils.minecraftClient.options.attackKey.isPressed())) {
            this.resetActionState();
        }
        if (this.fastBreak.isSelected()) {
            moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor interactionManagerAccessor = (moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)PlayerUtils.minecraftClient.interactionManager;
            interactionManagerAccessor.rockstar$setBlockBreakingCooldown(0);
            if (PlayerUtils.minecraftClient.interactionManager.getBlockBreakingProgress() > 1) {
                interactionManagerAccessor.rockstar$setBlockBreakingCooldown(1);
            }
        }
        if (this.antiAfk.isSelected()) {
            if (this.cooldownTimer.hasElapsed(10000L)) {
                this.privilegedMode = true;
            }
            if (EntityUtils.hasMovementInput()) {
                this.privilegedMode = false;
                this.cooldownTimer.reset();
            }
            if (this.privilegedMode && (float)PlayerUtils.minecraftClient.player.age % this.delay.getValue() == 5.0f) {
                if (this.send.isSelected()) {
                    PlayerUtils.minecraftClient.player.networkHandler.sendChatMessage(Localization.translateFormatted("player_utils.hello_message", String.valueOf(Math.random())));
                    this.privilegedMode = false;
                    this.cooldownTimer.reset();
                } else if (this.swing.isSelected()) {
                    PlayerUtils.minecraftClient.player.swingHand(Hand.MAIN_HAND);
                    this.privilegedMode = false;
                    this.cooldownTimer.reset();
                }
            }
        }
        if (this.autoRespawn.isSelected()) {
            if (PlayerUtils.minecraftClient.currentScreen instanceof DeathScreen) {
                if (this.actionCount == -1 && !PlayerUtils.minecraftClient.player.isAlive()) {
                    this.actionCount = 0;
                }
                ++this.actionCount;
                int n = 20;
                if (this.actionCount >= n) {
                    PlayerUtils.minecraftClient.player.requestRespawn();
                    minecraftClient.setScreen(null);
                    this.actionCount = -1;
                }
            } else {
                this.actionCount = -1;
            }
        }
        if (this.autoFish.isSelected() && PlayerUtils.minecraftClient.player.getMainHandStack().getItem() instanceof FishingRodItem) {
            if (PlayerUtils.minecraftClient.player.fishHook != null) {
                if (((Boolean)PlayerUtils.minecraftClient.player.fishHook.getDataTracker().get(moscow.rockstar.mixin.accessors.FishingBobberEntityAccessor.rockstar$getCaughtFish())).booleanValue() && this.lastActionTime == -1L) {
                    this.lastActionTime = System.currentTimeMillis();
                    this.lastAttackTime = (long)(180.0f + MathUtils.interpolateRandomStrategy(0.0f, 220.0f) + (float)minecraftClient.getNetworkHandler().getPlayerListEntry(PlayerUtils.minecraftClient.player.getUuid()).getLatency() / 2.0f);
                }
                if (this.lastActionTime != -1L && System.currentTimeMillis() - this.lastActionTime >= this.lastAttackTime) {
                    this.resetUtilityState();
                    this.lastActionTime = -1L;
                    this.utilityActive = true;
                    this.actionTimer.reset();
                }
            } else {
                if (this.utilityActive && this.actionTimer.hasElapsed((long)(600.0f + MathUtils.interpolateRandomStrategy(20.0f, 70.0f)))) {
                    this.resetUtilityState();
                    this.utilityActive = false;
                    this.overlayVisible = false;
                    this.actionTimer.reset();
                } else if (!this.utilityActive && this.overlayVisible && this.actionTimer.hasElapsed((long)(3000.0f + MathUtils.interpolateRandomStrategy(40.0f, 110.0f)))) {
                    this.resetUtilityState();
                    this.overlayVisible = false;
                    this.actionTimer.reset();
                }
                this.lastActionTime = -1L;
            }
        }
        if (PlayerUtils.minecraftClient.player != null && PlayerUtils.minecraftClient.world.getBlockState(PlayerUtils.minecraftClient.player.getBlockPos()).isOf(Blocks.LADDER) && this.fastLadder.isSelected()) {
            PlayerUtils.minecraftClient.player.setVelocity(PlayerUtils.minecraftClient.player.getVelocity().multiply(1.0, 1.43, 1.0));
        }
        if (this.blockTrap.isSelected() && (class_13092 = RockstarClient.create().getFriendManager().getTargetLivingEntity()) != null && PlayerUtils.minecraftClient.player.getPos().distanceTo(class_13092.getPos()) <= (double)RockstarClient.create().getModuleRegistry().getModule(Aura.class).getAttackDistanceSetting().getValue() && (itemRule = (itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules())).findByStack(class_17992 -> class_17992.getItem() instanceof BlockItem)) != null) {
            BlockPos[] adminskyArray = this.collectNearbyBlocks(class_13092);
            this.processBlockAction(itemRule.getClickSlot(), adminskyArray);
        }
        super.onTick();
    }

    private void resetUtilityState() {
        PlayerUtils.minecraftClient.interactionManager.interactItem((PlayerEntity)PlayerUtils.minecraftClient.player, Hand.MAIN_HAND);
        PlayerUtils.minecraftClient.player.swingHand(Hand.MAIN_HAND);
    }

    private ItemRule getBlockItemRule(BlockState class_26802, float f) {
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getHotbarRules().combineRules(ItemRuleSets.getInventoryRules());
        ItemRule itemRule = null;
        float f2 = f;
        for (ItemRule itemRule2 : itemRuleCollection.getRules()) {
            float f3;
            ItemStack class_17992 = itemRule2.getItemStack();
            if (class_17992 == null || class_17992.isEmpty() || !((f3 = this.calculateToolEfficiency(class_17992, class_26802)) > f2)) continue;
            f2 = f3;
            itemRule = itemRule2;
        }
        if (itemRule == null || f2 <= 1.0f || f2 <= f) {
            return null;
        }
        return itemRule;
    }

    private float calculateToolEfficiency(ItemStack class_17992, BlockState class_26802) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return 0.0f;
        }
        return class_17992.getMiningSpeedMultiplier(class_26802);
    }

    private void resetActionState() {
        if (!this.disableLocked || PlayerUtils.minecraftClient.player == null) {
            return;
        }
        if (this.alwaysEnabled && this.itemRule != null && this.previousHotbarSlot != -1) {
            InventoryUtils.dropItem(this.itemRule.getClickSlot(), this.previousHotbarSlot);
        }
        if (this.selectedToolSlot != -1) {
            InventoryUtils.setSelectedHotbarSlot(this.selectedToolSlot);
        }
        this.disableLocked = false;
        this.alwaysEnabled = false;
        this.selectedToolSlot = -1;
        this.previousHotbarSlot = -1;
        this.itemRule = null;
    }

    private void processBlockAction(int n2, BlockPos[] adminskyArray) {
        for (BlockPos adminsky : adminskyArray) {
            BlockHitResult class_39652;
            if (!PlayerUtils.minecraftClient.world.isAir(adminsky)) continue;
            if (n2 == 40) {
                class_39652 = new BlockHitResult(adminsky.down().toCenterPos(), Direction.UP, adminsky.down(), false);
                ((moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)PlayerUtils.minecraftClient.interactionManager).rockstar$sendSequencedPacket(PlayerUtils.minecraftClient.world, n -> new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, class_39652, n));
                continue;
            }
            if (n2 >= 36) {
                PlayerUtils.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(n2 - 36));
                class_39652 = new BlockHitResult(adminsky.down().toCenterPos(), Direction.UP, adminsky.down(), false);
                ((moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)PlayerUtils.minecraftClient.interactionManager).rockstar$sendSequencedPacket(PlayerUtils.minecraftClient.world, n -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, class_39652, n));
                PlayerUtils.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(PlayerUtils.minecraftClient.player.getInventory().selectedSlot));
                continue;
            }
            PlayerUtils.minecraftClient.interactionManager.clickSlot(PlayerUtils.minecraftClient.player.currentScreenHandler.syncId, n2, 0, SlotActionType.PICKUP, (PlayerEntity)PlayerUtils.minecraftClient.player);
            PlayerUtils.minecraftClient.interactionManager.clickSlot(PlayerUtils.minecraftClient.player.currentScreenHandler.syncId, 44, 0, SlotActionType.PICKUP, (PlayerEntity)PlayerUtils.minecraftClient.player);
        }
    }

    private BlockPos @NotNull [] collectNearbyBlocks(LivingEntity class_13092) {
        BlockPos adminsky = class_13092.getBlockPos();
        return new BlockPos[]{adminsky.add(1, 0, 0), adminsky.add(-1, 0, 0), adminsky.add(0, 0, 1), adminsky.add(0, 0, -1), adminsky.add(0, 3, 0), adminsky.add(1, 2, 0), adminsky.add(-1, 2, 0), adminsky.add(0, 2, 1), adminsky.add(0, 2, -1)};
    }

    @Override
    public void onDisable() {
        this.utilityActive = false;
    }
}
