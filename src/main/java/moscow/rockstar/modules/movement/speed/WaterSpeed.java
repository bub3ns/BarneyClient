/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.BlockItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.BlockView
 *  net.minecraft.Direction
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockState
 *  net.minecraft.MathHelper
 *  net.minecraft.BlockHitResult
 */
package moscow.rockstar.modules.movement.speed;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Hand;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.hit.BlockHitResult;

@ModuleInfo(name="Water Speed", category=ModuleCategory.MOVEMENT)
public class WaterSpeed
extends Module {
    private int previousHotbarSlot = -1;
    private int targetHotbarSlot = -1;
    private int interactionStartTick = -1;
    private int rotationConfirmationCount;

    @Override
    public void onTick() {
        if (WaterSpeed.minecraftClient.player != null && WaterSpeed.minecraftClient.world != null && WaterSpeed.minecraftClient.interactionManager != null && WaterSpeed.minecraftClient.player.isTouchingWater()) {
            BlockPos adminsky = WaterSpeed.minecraftClient.player.getBlockPos().up();
            if (this.isReplaceableBlock(adminsky)) {
                WaterSpeed.minecraftClient.player.setVelocity(WaterSpeed.minecraftClient.player.getVelocity().x * 1.05, WaterSpeed.minecraftClient.player.getVelocity().y, WaterSpeed.minecraftClient.player.getVelocity().z * 1.05);
                this.resetInteractionState();
            } else {
                this.placeWaterAtTarget(adminsky);
            }
        } else {
            this.resetInteractionState();
        }
        super.onTick();
    }

    @Override
    public void onDisable() {
        this.resetInteractionState();
    }

    private void placeWaterAtTarget(BlockPos adminsky) {
        if (!WaterSpeed.minecraftClient.world.getBlockState(adminsky).isReplaceable()) {
            this.resetInteractionState();
            return;
        }
        WaterPlacementTarget waterPlacementTarget = this.findNearestWaterPlacement(adminsky);
        if (waterPlacementTarget == null) {
            this.resetInteractionState();
            return;
        }
        if (this.isWaterPlacementItem(WaterSpeed.minecraftClient.player.getOffHandStack())) {
            this.performWaterPlacement(waterPlacementTarget, Hand.OFF_HAND);
            return;
        }
        HotbarSlot hotbarSlot = ItemRuleSets.getHotbarRules().findByStack(this::isWaterPlacementItem);
        if (hotbarSlot == null) {
            this.resetInteractionState();
            return;
        }
        if (!this.isHotbarSlotReady(hotbarSlot)) {
            return;
        }
        this.performWaterPlacement(waterPlacementTarget, Hand.MAIN_HAND);
    }

    private WaterPlacementTarget findNearestWaterPlacement(BlockPos adminsky) {
        Vec3d VanillaChestLootTableGenerator = WaterSpeed.minecraftClient.player.getEyePos();
        double d = WaterSpeed.minecraftClient.player.getBlockInteractionRange();
        WaterPlacementTarget waterPlacementTarget = null;
        double d2 = Double.MAX_VALUE;
        for (Direction class_23502 : Direction.values()) {
            Vec3d WallPlayerSkullBlock;
            double d3;
            BlockPos adminsky2 = adminsky.offset(class_23502);
            Direction class_23503 = class_23502.getOpposite();
            BlockState class_26802 = WaterSpeed.minecraftClient.world.getBlockState(adminsky2);
            if (!class_26802.isSideSolidFullSquare((BlockView)WaterSpeed.minecraftClient.world, adminsky2, class_23503) || (d3 = VanillaChestLootTableGenerator.squaredDistanceTo(WallPlayerSkullBlock = adminsky2.toCenterPos().add((double)class_23503.getOffsetX() * 0.5, (double)class_23503.getOffsetY() * 0.5, (double)class_23503.getOffsetZ() * 0.5))) > d * d || d3 >= d2) continue;
            d2 = d3;
            waterPlacementTarget = new WaterPlacementTarget(adminsky2, class_23503, WallPlayerSkullBlock);
        }
        return waterPlacementTarget;
    }

    private boolean isHotbarSlotReady(HotbarSlot hotbarSlot) {
        int n = WaterSpeed.minecraftClient.player.getInventory().selectedSlot;
        if (this.targetHotbarSlot != hotbarSlot.getSlotIndex() || n != hotbarSlot.getSlotIndex()) {
            if (this.previousHotbarSlot == -1) {
                this.previousHotbarSlot = n;
            }
            this.targetHotbarSlot = hotbarSlot.getSlotIndex();
            this.interactionStartTick = WaterSpeed.minecraftClient.player.age;
            this.rotationConfirmationCount = 0;
            InventoryUtils.setSelectedHotbarSlot(this.targetHotbarSlot);
            return false;
        }
        return WaterSpeed.minecraftClient.player.age > this.interactionStartTick;
    }

    private void performWaterPlacement(WaterPlacementTarget waterPlacementTarget, Hand class_12682) {
        Rotation rotation = this.calculateRotationToHitPosition(WaterSpeed.minecraftClient.player.getEyePos(), waterPlacementTarget.getHitPosition());
        RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, 100.0f, 100.0f, 100.0f, RotationPriority.ITEM_USE_PRIORITY);
        Rotation rotation2 = RockstarClient.create().getRotationManager().getCurrentRotation();
        if (Math.abs(MathHelper.wrapDegrees((float)(rotation2.getYaw() - rotation.getYaw()))) > 3.0f || Math.abs(rotation2.getPitch() - rotation.getPitch()) > 3.0f) {
            this.rotationConfirmationCount = 0;
            return;
        }
        if (++this.rotationConfirmationCount < 2) {
            return;
        }
        WaterSpeed.minecraftClient.interactionManager.interactBlock(WaterSpeed.minecraftClient.player, class_12682, new BlockHitResult(waterPlacementTarget.getHitPosition(), waterPlacementTarget.getHitSide(), waterPlacementTarget.getTargetBlockPosition(), false));
        WaterSpeed.minecraftClient.player.swingHand(class_12682);
        this.rotationConfirmationCount = 0;
    }

    private boolean isReplaceableBlock(BlockPos adminsky) {
        return WaterSpeed.minecraftClient.world.getBlockState(adminsky).isFullCube((BlockView)WaterSpeed.minecraftClient.world, adminsky);
    }

    private boolean isWaterPlacementItem(ItemStack class_17992) {
        if (class_17992.isEmpty() || !(class_17992.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        BlockState blockState = blockItem.getBlock().getDefaultState();
        return blockState.isFullCube((BlockView)WaterSpeed.minecraftClient.world, BlockPos.ORIGIN);
    }

    private Rotation calculateRotationToHitPosition(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock) {
        double d = WallPlayerSkullBlock.x - VanillaChestLootTableGenerator.x;
        double d2 = WallPlayerSkullBlock.y - VanillaChestLootTableGenerator.y;
        double d3 = WallPlayerSkullBlock.z - VanillaChestLootTableGenerator.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        return new Rotation((float)Math.toDegrees(Math.atan2(d3, d)) - 90.0f, (float)(-Math.toDegrees(Math.atan2(d2, d4))));
    }

    private void resetInteractionState() {
        if (WaterSpeed.minecraftClient.player != null && this.previousHotbarSlot != -1 && WaterSpeed.minecraftClient.player.getInventory().selectedSlot != this.previousHotbarSlot) {
            InventoryUtils.setSelectedHotbarSlot(this.previousHotbarSlot);
        }
        this.previousHotbarSlot = -1;
        this.targetHotbarSlot = -1;
        this.interactionStartTick = -1;
        this.rotationConfirmationCount = 0;
    }

    static final class WaterPlacementTarget {
        private final BlockPos targetBlockPosition;
        private final Direction hitSide;
        private final Vec3d hitPosition;

        WaterPlacementTarget(BlockPos adminsky, Direction class_23502, Vec3d VanillaChestLootTableGenerator) {
            this.targetBlockPosition = adminsky;
            this.hitSide = class_23502;
            this.hitPosition = VanillaChestLootTableGenerator;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "targetBlockPosition", "hitSide", "hitPosition");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "targetBlockPosition", "hitSide", "hitPosition");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "targetBlockPosition", "hitSide", "hitPosition");
        }

        public BlockPos getTargetBlockPosition() {
            return this.targetBlockPosition;
        }

        public Direction getHitSide() {
            return this.hitSide;
        }

        public Vec3d getHitPosition() {
            return this.hitPosition;
        }
    }
}
