/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Items
 *  net.minecraft.BlockView
 *  net.minecraft.Box
 *  net.minecraft.BooleanBiFunction
 *  net.minecraft.VoxelShapes
 *  net.minecraft.VoxelShape
 *  net.minecraft.BlockState
 */
package moscow.rockstar.modules.movement.jump;

import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.items.rules.OffhandRule;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.settings.ModeSetting;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.Box;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.player.EventMotion;
import pyrock.events.player.InputEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Spider", category=ModuleCategory.MOVEMENT, description="modules.descriptions.spider")
public class Spider
extends Module {
    private ModeSetting mode;
    private ModeSetting.Option vanilla;
    private ModeSetting.Option funtime;
    private ModeSetting.Option water;
    private ModeSetting.Option sphere;
    private int climbToolSlot = -1;
    private int previousHotbarSlot = -1;
    private int climbTicks = -1;
    private int jumpTicks = -1;
    private Hand hand = Hand.MAIN_HAND;
    private boolean climbing;
    private boolean overlayVisible;
    private final EventListener<EventMotion> onEventMotionListener = eventMotion -> {
        if (this.funtime.isSelected()) {
            if (!Spider.minecraftClient.player.horizontalCollision || !this.hasClimbableWall()) {
                return;
            }
            eventMotion.setGround(true);
            Spider.minecraftClient.player.setOnGround(true);
        }
    };
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        if (Spider.minecraftClient.player == null || Spider.minecraftClient.world == null) {
            return;
        }
        if (!this.water.isSelected() && this.climbing) {
            this.resetClimbState();
        }
        if (!this.sphere.isSelected() && this.overlayVisible) {
            this.resetJumpState();
        }
        if (this.vanilla.isSelected()) {
            if (!Spider.minecraftClient.player.horizontalCollision) {
                return;
            }
            Spider.minecraftClient.player.prevY -= 2.0E-232;
            if (Spider.minecraftClient.player.isOnGround()) {
                Spider.minecraftClient.player.setVelocity(Spider.minecraftClient.player.getVelocity().getX(), 0.42, Spider.minecraftClient.player.getVelocity().getZ());
            }
        } else if (this.funtime.isSelected()) {
            if (!Spider.minecraftClient.player.horizontalCollision || !this.hasClimbableWall()) {
                return;
            }
            Spider.minecraftClient.player.prevY -= 2.0E-232;
            if (Spider.minecraftClient.player.isOnGround()) {
                Spider.minecraftClient.player.setVelocity(Spider.minecraftClient.player.getVelocity().getX(), 0.42, Spider.minecraftClient.player.getVelocity().getZ());
            }
        } else if (this.water.isSelected()) {
            if (!this.climbing && !this.isModeReady()) {
                return;
            }
            if (!Spider.minecraftClient.player.horizontalCollision) {
                return;
            }
            if (this.previousHotbarSlot >= 0 && this.previousHotbarSlot <= 8) {
                Spider.minecraftClient.player.getInventory().selectedSlot = this.previousHotbarSlot;
            }
            Spider.minecraftClient.player.setPitch(75.0f);
            if (Spider.minecraftClient.player.age % 3 == 0) {
                ((moscow.rockstar.mixin.minecraft.client.IMinecraftClient)(Object)minecraftClient).idoItemUse();
            }
        } else if (this.sphere.isSelected()) {
            if (!this.overlayVisible && !this.prepareClimbState()) {
                return;
            }
            if (this.hand == Hand.MAIN_HAND && this.jumpTicks >= 0 && this.jumpTicks <= 8) {
                Spider.minecraftClient.player.getInventory().selectedSlot = this.jumpTicks;
            }
            if (Spider.minecraftClient.player.horizontalCollision) {
                RockstarClient.create().getRotationManager().requestRotation(new Rotation((double)Spider.minecraftClient.player.getYaw(), 78.5), RotationCorrectionMode.DIRECT, 180.0f, 180.0f, 180.0f, RotationPriority.ITEM_USE_PRIORITY);
                Spider.minecraftClient.interactionManager.interactItem((PlayerEntity)Spider.minecraftClient.player, this.hand);
                ((moscow.rockstar.mixin.minecraft.client.IMinecraftClient)(Object)minecraftClient).idoItemUse();
            }
        }
    };
    private final EventListener<InputEvent> onInputEvent = inputEvent -> {
        if (Spider.minecraftClient.player == null) {
            return;
        }
        if (this.water.isSelected() && Spider.minecraftClient.player.horizontalCollision) {
            inputEvent.setJump(true);
            return;
        }
        if (this.sphere.isSelected() && Spider.minecraftClient.player.horizontalCollision) {
            inputEvent.setJump(true);
        }
    };

    public Spider() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.mode = new ModeSetting(this, "modules.settings.spider.mode");
        this.vanilla = new ModeSetting.Option(this.mode, "modules.settings.flight.vanilla");
        this.funtime = new ModeSetting.Option(this.mode, "FunTime");
        this.water = new ModeSetting.Option(this.mode, "modules.settings.spider.mode.water");
        this.sphere = new ModeSetting.Option(this.mode, "modules.settings.spider.mode.sphere");
    }

    @Override
    public void onEnable() {
        if (Spider.minecraftClient.player == null) {
            return;
        }
        if (this.water.isSelected()) {
            this.isModeReady();
        } else if (this.sphere.isSelected()) {
            this.prepareClimbState();
        }
    }

    @Override
    public void onDisable() {
        this.resetClimbState();
        this.resetJumpState();
        this.climbToolSlot = -1;
        this.previousHotbarSlot = -1;
        this.climbTicks = -1;
        this.jumpTicks = -1;
        this.hand = Hand.MAIN_HAND;
        this.climbing = false;
        this.overlayVisible = false;
        Spider.minecraftClient.options.useKey.setPressed(false);
    }

    private boolean isModeReady() {
        if (this.climbing) {
            return true;
        }
        if (Spider.minecraftClient.player == null) {
            return false;
        }
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules());
        ItemRule itemRule = itemRuleCollection.findByItem(Items.WATER_BUCKET);
        if (itemRule == null || itemRule.isEmpty()) {
            this.setEnabled(false, true);
            return false;
        }
        this.previousHotbarSlot = Spider.minecraftClient.player.getInventory().selectedSlot;
        if (this.previousHotbarSlot < 0 || this.previousHotbarSlot > 8) {
            Spider.minecraftClient.player.getInventory().selectedSlot = this.previousHotbarSlot = 0;
        }
        this.climbToolSlot = itemRule.getClickSlot();
        InventoryUtils.dropItem(this.climbToolSlot, this.previousHotbarSlot);
        this.climbing = true;
        return true;
    }

    private void resetClimbState() {
        if (!this.climbing) {
            this.climbToolSlot = -1;
            this.previousHotbarSlot = -1;
            return;
        }
        if (Spider.minecraftClient.player != null && this.climbToolSlot != -1 && this.previousHotbarSlot >= 0 && this.previousHotbarSlot <= 8) {
            InventoryUtils.dropItem(this.climbToolSlot, this.previousHotbarSlot);
            Spider.minecraftClient.player.getInventory().selectedSlot = this.previousHotbarSlot;
        }
        this.climbToolSlot = -1;
        this.previousHotbarSlot = -1;
        this.climbing = false;
    }

    private boolean prepareClimbState() {
        if (this.overlayVisible) {
            return true;
        }
        if (Spider.minecraftClient.player == null) {
            return false;
        }
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getOffhandRules().combineRules(ItemRuleSets.getInventoryRules()).combineRules(ItemRuleSets.getHotbarRules());
        ItemRule itemRule = itemRuleCollection.findByItem(Items.PLAYER_HEAD);
        if (itemRule == null || itemRule.isEmpty()) {
            this.setEnabled(false, true);
            return false;
        }
        if (itemRule instanceof OffhandRule) {
            this.hand = Hand.OFF_HAND;
            this.climbTicks = -1;
            this.jumpTicks = -1;
        } else {
            this.hand = Hand.MAIN_HAND;
            this.jumpTicks = Spider.minecraftClient.player.getInventory().selectedSlot;
            if (this.jumpTicks < 0 || this.jumpTicks > 8) {
                Spider.minecraftClient.player.getInventory().selectedSlot = this.jumpTicks = 0;
            }
            this.climbTicks = itemRule.getClickSlot();
            InventoryUtils.dropItem(this.climbTicks, this.jumpTicks);
        }
        this.overlayVisible = true;
        return true;
    }

    private void resetJumpState() {
        if (!this.overlayVisible) {
            this.climbTicks = -1;
            this.jumpTicks = -1;
            this.hand = Hand.MAIN_HAND;
            return;
        }
        if (Spider.minecraftClient.player != null && this.hand == Hand.MAIN_HAND && this.climbTicks != -1 && this.jumpTicks >= 0 && this.jumpTicks <= 8) {
            InventoryUtils.dropItem(this.climbTicks, this.jumpTicks);
            Spider.minecraftClient.player.getInventory().selectedSlot = this.jumpTicks;
        }
        this.climbTicks = -1;
        this.jumpTicks = -1;
        this.hand = Hand.MAIN_HAND;
        this.overlayVisible = false;
    }

    private boolean hasClimbableWall() {
        if (Spider.minecraftClient.world == null || Spider.minecraftClient.player == null) {
            return false;
        }
        Box HorizontalFacingBlock = Spider.minecraftClient.player.getBoundingBox();
        double d = Math.max((double)Spider.minecraftClient.player.getWidth() * 0.15, 0.03);
        Box InfestedBlock = HorizontalFacingBlock.expand(d, 0.0, d);
        BlockPos adminsky = BlockPos.ofFloored((double)InfestedBlock.minX, (double)HorizontalFacingBlock.minY, (double)InfestedBlock.minZ);
        BlockPos adminsky2 = BlockPos.ofFloored((double)InfestedBlock.maxX, (double)HorizontalFacingBlock.maxY, (double)InfestedBlock.maxZ);
        for (BlockPos adminsky3 : BlockPos.iterate((BlockPos)adminsky, (BlockPos)adminsky2)) {
            BlockState class_26802 = Spider.minecraftClient.world.getBlockState(adminsky3);
            if (!this.isBlockValid(class_26802, adminsky3, HorizontalFacingBlock, InfestedBlock)) continue;
            return true;
        }
        return false;
    }

    private boolean isBlockValid(BlockState class_26802, BlockPos adminsky, Box HorizontalFacingBlock, Box InfestedBlock) {
        if (class_26802.isAir()) {
            return false;
        }
        VoxelShape class_2652 = class_26802.getCollisionShape((BlockView)Spider.minecraftClient.world, adminsky);
        if (class_2652.isEmpty() || !VoxelShapes.matchesAnywhere((VoxelShape)class_2652, (VoxelShape)VoxelShapes.fullCube(), (BooleanBiFunction)BooleanBiFunction.NOT_SAME)) {
            return false;
        }
        for (Box MutableRegistry : class_2652.getBoundingBoxes()) {
            Box IceBlock = MutableRegistry.offset(adminsky);
            if (!IceBlock.intersects(InfestedBlock) || !(IceBlock.maxY > HorizontalFacingBlock.minY) || !(IceBlock.minY < HorizontalFacingBlock.maxY)) continue;
            return true;
        }
        return false;
    }
}
