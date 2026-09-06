/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.Direction
 *  net.minecraft.Position
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 *  net.minecraft.BlockHitResult
 */
package moscow.rockstar.modules.player.interaction;

import java.util.HashSet;
import java.util.Set;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.hit.BlockHitResult;

@ModuleInfo(name="Click Through", category=ModuleCategory.PLAYER)
public class ClickThrough
extends Module {
    private final Set<BlockPos> visitedBlocks = new HashSet<BlockPos>();

    @Override
    public void onTick() {
        if (ClickThrough.minecraftClient.player == null || ClickThrough.minecraftClient.world == null || ClickThrough.minecraftClient.interactionManager == null) {
            return;
        }
        if (!ClickThrough.minecraftClient.options.useKey.isPressed() || ClickThrough.minecraftClient.options.sneakKey.isPressed()) {
            return;
        }
        this.visitedBlocks.clear();
        for (int i = 1; i < 16; ++i) {
            Vec3d VanillaChestLootTableGenerator = ClickThrough.minecraftClient.player.getRotationVec(1.0f);
            Vec3d WallPlayerSkullBlock = this.calculatePositionExtra(minecraftClient.getRenderTickCounter().getTickDelta(true)).add(VanillaChestLootTableGenerator.multiply((double)i * 0.25));
            BlockPos adminsky = BlockPos.ofFloored((Position)WallPlayerSkullBlock);
            if (this.visitedBlocks.contains(adminsky)) continue;
            this.visitedBlocks.add(adminsky);
            if (ClickThrough.minecraftClient.player.getPos().distanceTo(Vec3d.ofCenter((Vec3i)adminsky)) > 4.25) continue;
            Vec3d VanillaEntityLootTableGenerator = this.calculatePositionExtra(minecraftClient.getRenderTickCounter().getTickDelta(true));
            Direction class_23502 = Direction.getFacing((double)(VanillaEntityLootTableGenerator.x - (double)adminsky.getX()), (double)(VanillaEntityLootTableGenerator.y - (double)adminsky.getY()), (double)(VanillaEntityLootTableGenerator.z - (double)adminsky.getZ()));
            if (class_23502 == Direction.UP || class_23502 == Direction.DOWN) {
                class_23502 = Direction.NORTH;
            }
            BlockHitResult class_39652 = new BlockHitResult(Vec3d.ofCenter((Vec3i)adminsky), class_23502, adminsky, true);
            ClickThrough.minecraftClient.interactionManager.interactBlock(ClickThrough.minecraftClient.player, Hand.MAIN_HAND, class_39652);
            ClickThrough.minecraftClient.player.swingHand(Hand.MAIN_HAND);
            return;
        }
    }

    public Vec3d calculatePositionExtra(float f) {
        if (ClickThrough.minecraftClient.player == null) {
            return Vec3d.ZERO;
        }
        double d = MathHelper.lerp((double)f, (double)ClickThrough.minecraftClient.player.prevX, (double)ClickThrough.minecraftClient.player.getX());
        double d2 = MathHelper.lerp((double)f, (double)ClickThrough.minecraftClient.player.prevY, (double)ClickThrough.minecraftClient.player.getY()) + (double)ClickThrough.minecraftClient.player.getEyeHeight(ClickThrough.minecraftClient.player.getPose());
        double d3 = MathHelper.lerp((double)f, (double)ClickThrough.minecraftClient.player.prevZ, (double)ClickThrough.minecraftClient.player.getZ());
        return new Vec3d(d, d2, d3);
    }
}
