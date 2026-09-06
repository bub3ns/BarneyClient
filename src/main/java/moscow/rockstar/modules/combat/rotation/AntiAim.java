/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Items
 *  net.minecraft.Blocks
 *  net.minecraft.Direction
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 *  net.minecraft.BlockHitResult
 *  net.minecraft.WorldView
 */
package moscow.rockstar.modules.combat.rotation;

import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.items.rules.OffhandRule;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.WorldView;
import pyrock.events.player.ClientPlayerTickEvent;

@ModuleInfo(name="AntiAim", category=ModuleCategory.OTHER)
public class AntiAim
extends Module {
    private int previousHotbarSlot = -1;
    private int targetHotbarSlot = -1;
    private int hotbarSwitchTick = -1;
    private int restoreSlotTick = -1;
    private int rotationConfirmationTicks;
    private int lastPlacementTick = -1000;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (AntiAim.minecraftClient.player == null || AntiAim.minecraftClient.world == null || AntiAim.minecraftClient.interactionManager == null || AntiAim.minecraftClient.player.isSpectator()) {
            return;
        }
        if (this.restoreSlotTick >= 0) {
            if (AntiAim.minecraftClient.player.age >= this.restoreSlotTick) {
                this.restorePreviousHotbarSlot();
            }
            return;
        }
        if (!this.hasEligibleTarget()) {
            this.restorePreviousHotbarSlot();
            return;
        }
        BlockPos adminsky = AntiAim.minecraftClient.player.getBlockPos();
        if (!this.isObsidianPlacementSpace(adminsky)) {
            this.rotationConfirmationTicks = 0;
            this.restorePreviousHotbarSlot();
            return;
        }
        if (AntiAim.minecraftClient.player.age - this.lastPlacementTick < 3) {
            return;
        }
        OffhandRule offhandRule = ItemRuleSets.getOffhandRules().findByItem(Items.LILAC);
        if (offhandRule != null) {
            this.placeObsidian(adminsky, Hand.OFF_HAND);
            return;
        }
        HotbarSlot hotbarSlot = ItemRuleSets.getHotbarRules().findByItem(Items.LILAC);
        if (hotbarSlot == null) {
            return;
        }
        if (!this.isHotbarSlotReady(hotbarSlot)) {
            return;
        }
        this.placeObsidian(adminsky, Hand.MAIN_HAND);
    };

    @Override
    public void onDisable() {
        this.restorePreviousHotbarSlot();
    }

    private boolean hasEligibleTarget() {
        for (PlayerEntity class_16572 : AntiAim.minecraftClient.world.getPlayers()) {
            Vec3d VanillaChestLootTableGenerator;
            if (class_16572 == AntiAim.minecraftClient.player || !class_16572.isAlive() || class_16572.isSpectator() || class_16572.squaredDistanceTo((Entity)AntiAim.minecraftClient.player) > 36.0 || class_16572.isOnGround() || class_16572.isSwimming() || class_16572.isClimbing() || (VanillaChestLootTableGenerator = AntiAim.minecraftClient.player.getBoundingBox().getCenter().subtract(class_16572.getEyePos())).lengthSquared() == 0.0 || !(class_16572.getRotationVec(1.0f).dotProduct(VanillaChestLootTableGenerator.normalize()) >= 0.9)) continue;
            return true;
        }
        return false;
    }

    private boolean isObsidianPlacementSpace(BlockPos adminsky) {
        return AntiAim.minecraftClient.world.getBlockState(adminsky).isAir() && AntiAim.minecraftClient.world.getBlockState(adminsky.up()).isAir() && Blocks.LILAC.getDefaultState().canPlaceAt((WorldView)AntiAim.minecraftClient.world, adminsky);
    }

    private boolean isHotbarSlotReady(HotbarSlot hotbarSlot) {
        int n = AntiAim.minecraftClient.player.getInventory().selectedSlot;
        if (this.targetHotbarSlot != hotbarSlot.getSlotIndex() || n != hotbarSlot.getSlotIndex()) {
            if (this.previousHotbarSlot == -1) {
                this.previousHotbarSlot = n;
            }
            this.targetHotbarSlot = hotbarSlot.getSlotIndex();
            this.hotbarSwitchTick = AntiAim.minecraftClient.player.age;
            this.rotationConfirmationTicks = 0;
            InventoryUtils.setSelectedHotbarSlot(this.targetHotbarSlot);
            return false;
        }
        return AntiAim.minecraftClient.player.age > this.hotbarSwitchTick;
    }

    private void placeObsidian(BlockPos adminsky, Hand class_12682) {
        BlockPos adminsky2 = adminsky.down();
        Vec3d VanillaChestLootTableGenerator = adminsky2.toCenterPos().add(0.0, 0.5, 0.0);
        Rotation rotation = this.getRotationTo(AntiAim.minecraftClient.player.getEyePos(), VanillaChestLootTableGenerator);
        RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, 45.0f, 45.0f, 45.0f, RotationPriority.ITEM_USE_PRIORITY);
        Rotation rotation2 = RockstarClient.create().getRotationManager().getCurrentRotation();
        if (Math.abs(MathHelper.wrapDegrees((float)(rotation2.getYaw() - rotation.getYaw()))) > 3.0f || Math.abs(rotation2.getPitch() - rotation.getPitch()) > 3.0f) {
            this.rotationConfirmationTicks = 0;
            return;
        }
        if (++this.rotationConfirmationTicks < 2) {
            return;
        }
        AntiAim.minecraftClient.interactionManager.interactBlock(AntiAim.minecraftClient.player, class_12682, new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky2, false));
        AntiAim.minecraftClient.player.swingHand(class_12682);
        this.lastPlacementTick = AntiAim.minecraftClient.player.age;
        this.rotationConfirmationTicks = 0;
        if (this.previousHotbarSlot != -1) {
            this.restoreSlotTick = AntiAim.minecraftClient.player.age + 1;
        }
    }

    private Rotation getRotationTo(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock) {
        double d = WallPlayerSkullBlock.x - VanillaChestLootTableGenerator.x;
        double d2 = WallPlayerSkullBlock.y - VanillaChestLootTableGenerator.y;
        double d3 = WallPlayerSkullBlock.z - VanillaChestLootTableGenerator.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        return new Rotation((float)Math.toDegrees(Math.atan2(d3, d)) - 90.0f, (float)(-Math.toDegrees(Math.atan2(d2, d4))));
    }

    private void restorePreviousHotbarSlot() {
        if (AntiAim.minecraftClient.player != null && this.previousHotbarSlot != -1 && AntiAim.minecraftClient.player.getInventory().selectedSlot != this.previousHotbarSlot) {
            InventoryUtils.setSelectedHotbarSlot(this.previousHotbarSlot);
        }
        this.previousHotbarSlot = -1;
        this.targetHotbarSlot = -1;
        this.hotbarSwitchTick = -1;
        this.restoreSlotTick = -1;
        this.rotationConfirmationTicks = 0;
    }
}

