/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.Direction
 *  net.minecraft.Vec3i
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 */
package moscow.rockstar.modules.player.mining;

import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Nuker", category=ModuleCategory.PLAYER, description="modules.descriptions.nuker")
public class Nuker
extends Module {
    private NumberSetting swapDelay;
    private BlockPos targetBlock = null;
    private BlockPos previousBlock = null;
    private final Timer cooldownTimer = new Timer();
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        if (Nuker.minecraftClient.player == null || Nuker.minecraftClient.world == null || Nuker.minecraftClient.interactionManager == null) {
            return;
        }
        Nuker.minecraftClient.options.attackKey.setPressed(false);
        this.targetBlock = this.getTargetBlock();
        if (this.targetBlock == null) {
            this.previousBlock = null;
            return;
        }
        Rotation rotation = this.calculateBlockRotation(this.targetBlock);
        RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, 180.0f, 180.0f, 180.0f, RotationPriority.STANDARD_PRIORITY);
        if (this.previousBlock == null || !this.previousBlock.equals(this.targetBlock)) {
            this.previousBlock = this.targetBlock;
            this.cooldownTimer.reset();
        }
        if (!this.cooldownTimer.hasElapsed((long)this.swapDelay.getValue())) {
            return;
        }
        Rotation rotation2 = RockstarClient.create().getRotationManager().getCurrentRotation();
        if (rotation2 != null && rotation2.angleDistanceTo(rotation) > 3.0f) {
            return;
        }
        Direction class_23502 = this.getBlockFace(this.targetBlock);
        Nuker.minecraftClient.interactionManager.updateBlockBreakingProgress(this.targetBlock, class_23502);
        Nuker.minecraftClient.player.swingHand(Hand.MAIN_HAND);
    };

    public Nuker() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.swapDelay = new NumberSetting(this, "modules.settings.nuker.swap_delay").setStep(1.0f).setMinValue(0.0f).setMaxValue(500.0f).setValue(5.0f);
    }

    private int getBlockHardness(BlockPos adminsky) {
        Block class_22482 = Nuker.minecraftClient.world.getBlockState(adminsky).getBlock();
        if (class_22482 == Blocks.ANCIENT_DEBRIS) {
            return 10;
        }
        if (class_22482 == Blocks.DIAMOND_ORE || class_22482 == Blocks.DEEPSLATE_DIAMOND_ORE) {
            return 9;
        }
        if (class_22482 == Blocks.EMERALD_ORE || class_22482 == Blocks.DEEPSLATE_EMERALD_ORE) {
            return 8;
        }
        if (class_22482 == Blocks.GOLD_ORE || class_22482 == Blocks.DEEPSLATE_GOLD_ORE || class_22482 == Blocks.NETHER_GOLD_ORE) {
            return 7;
        }
        if (class_22482 == Blocks.LAPIS_ORE || class_22482 == Blocks.DEEPSLATE_LAPIS_ORE) {
            return 6;
        }
        if (class_22482 == Blocks.REDSTONE_ORE || class_22482 == Blocks.DEEPSLATE_REDSTONE_ORE) {
            return 5;
        }
        if (class_22482 == Blocks.IRON_ORE || class_22482 == Blocks.DEEPSLATE_IRON_ORE) {
            return 4;
        }
        if (class_22482 == Blocks.COPPER_ORE || class_22482 == Blocks.DEEPSLATE_COPPER_ORE) {
            return 3;
        }
        if (class_22482 == Blocks.COAL_ORE || class_22482 == Blocks.DEEPSLATE_COAL_ORE) {
            return 2;
        }
        if (class_22482 == Blocks.NETHER_QUARTZ_ORE) {
            return 1;
        }
        return 0;
    }

    private boolean isBlockAllowed(Block class_22482) {
        if (!ServerDetector.isServerEnvironmentReady()) {
            return true;
        }
        return class_22482 == Blocks.DIAMOND_ORE || class_22482 == Blocks.DEEPSLATE_DIAMOND_ORE || class_22482 == Blocks.EMERALD_ORE || class_22482 == Blocks.DEEPSLATE_EMERALD_ORE || class_22482 == Blocks.GOLD_ORE || class_22482 == Blocks.DEEPSLATE_GOLD_ORE || class_22482 == Blocks.NETHER_GOLD_ORE || class_22482 == Blocks.LAPIS_ORE || class_22482 == Blocks.DEEPSLATE_LAPIS_ORE || class_22482 == Blocks.REDSTONE_ORE || class_22482 == Blocks.DEEPSLATE_REDSTONE_ORE || class_22482 == Blocks.IRON_ORE || class_22482 == Blocks.DEEPSLATE_IRON_ORE || class_22482 == Blocks.COPPER_ORE || class_22482 == Blocks.DEEPSLATE_COPPER_ORE || class_22482 == Blocks.COAL_ORE || class_22482 == Blocks.DEEPSLATE_COAL_ORE || class_22482 == Blocks.NETHER_QUARTZ_ORE || class_22482 == Blocks.ANCIENT_DEBRIS || class_22482 == Blocks.COBBLESTONE || class_22482 == Blocks.STONE || class_22482 == Blocks.GRANITE || class_22482 == Blocks.DIORITE || class_22482 == Blocks.ANDESITE || class_22482 == Blocks.DEEPSLATE || class_22482 == Blocks.COBBLED_DEEPSLATE;
    }

    private BlockPos getTargetBlock() {
        double d = Nuker.minecraftClient.player.getBlockInteractionRange();
        int n = (int)Math.ceil(d);
        Vec3d VanillaChestLootTableGenerator = Nuker.minecraftClient.player.getEyePos();
        BlockPos adminsky = Nuker.minecraftClient.player.getBlockPos();
        BlockPos adminsky2 = null;
        int n2 = -1;
        double d2 = Double.MAX_VALUE;
        for (int i = 0; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    int n3;
                    double d3;
                    BlockPos adminsky3 = adminsky.add(j, i, k);
                    Block class_22482 = Nuker.minecraftClient.world.getBlockState(adminsky3).getBlock();
                    if (class_22482 == Blocks.AIR || class_22482.getHardness() < 0.0f || !this.isBlockAllowed(class_22482) || (d3 = VanillaChestLootTableGenerator.squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky3))) > d * d || !this.isWithinRange(adminsky3, d) || (n3 = this.getBlockHardness(adminsky3)) <= n2 && (n3 != n2 || !(d3 < d2))) continue;
                    n2 = n3;
                    d2 = d3;
                    adminsky2 = adminsky3;
                }
            }
        }
        return adminsky2;
    }

    private boolean isWithinRange(BlockPos adminsky, double d) {
        Vec3d VanillaChestLootTableGenerator = Nuker.minecraftClient.player.getEyePos();
        Vec3d WallPlayerSkullBlock = Vec3d.ofCenter((Vec3i)adminsky);
        double d2 = WallPlayerSkullBlock.x - VanillaChestLootTableGenerator.x;
        double d3 = WallPlayerSkullBlock.y - VanillaChestLootTableGenerator.y;
        double d4 = WallPlayerSkullBlock.z - VanillaChestLootTableGenerator.z;
        double d5 = Math.sqrt(d2 * d2 + d3 * d3 + d4 * d4);
        Vec3d VanillaEntityLootTableGenerator = new Vec3d(d2 / d5, d3 / d5, d4 / d5);
        BlockHitResult class_39652 = Nuker.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, VanillaChestLootTableGenerator.add(VanillaEntityLootTableGenerator.multiply(d + 0.5)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)Nuker.minecraftClient.player));
        if (class_39652 == null || class_39652.getType() == HitResult.Type.MISS) {
            return false;
        }
        BlockPos adminsky2 = class_39652.getBlockPos();
        return adminsky2 != null && adminsky2.equals(adminsky);
    }

    private Rotation calculateBlockRotation(BlockPos adminsky) {
        Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)adminsky);
        Vec3d WallPlayerSkullBlock = Nuker.minecraftClient.player.getEyePos();
        double d = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d2 = VanillaChestLootTableGenerator.y - WallPlayerSkullBlock.y;
        double d3 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = (float)Math.toDegrees(Math.atan2(d3, d)) - 90.0f;
        float f2 = (float)(-Math.toDegrees(Math.atan2(d2, d4)));
        return new Rotation(f, f2);
    }

    private Direction getBlockFace(BlockPos adminsky) {
        Vec3d VanillaChestLootTableGenerator = Nuker.minecraftClient.player.getEyePos();
        BlockHitResult class_39652 = Nuker.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, Vec3d.ofCenter((Vec3i)adminsky), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)Nuker.minecraftClient.player));
        if (class_39652.getType() == HitResult.Type.BLOCK && class_39652.getBlockPos().equals(adminsky)) {
            return class_39652.getSide();
        }
        Vec3d WallPlayerSkullBlock = Vec3d.ofCenter((Vec3i)adminsky);
        double d = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d2 = VanillaChestLootTableGenerator.y - WallPlayerSkullBlock.y;
        double d3 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        double d4 = Math.abs(d);
        double d5 = Math.abs(d2);
        double d6 = Math.abs(d3);
        if (d5 >= d4 && d5 >= d6) {
            return d2 >= 0.0 ? Direction.UP : Direction.DOWN;
        }
        if (d4 >= d6) {
            return d >= 0.0 ? Direction.EAST : Direction.WEST;
        }
        return d3 >= 0.0 ? Direction.SOUTH : Direction.NORTH;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Nuker.minecraftClient.options.attackKey.setPressed(false);
        this.targetBlock = null;
        this.previousBlock = null;
    }
}

