/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockEntity
 *  net.minecraft.ShulkerBoxBlockEntity
 *  net.minecraft.WorldChunk
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.modules.movement.jump;

import java.util.Map;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.util.math.MathHelper;

@ModuleInfo(name="High Jump", category=ModuleCategory.MOVEMENT)
public class HighJump
extends Module {
    private boolean jumpActive;
    private int jumpTicks;

    @Override
    public void onTick() {
        if (this.jumpActive) {
            ++this.jumpTicks;
            double d = Math.min((double)this.jumpTicks / 12.0, 1.5);
            Vec3d VanillaChestLootTableGenerator = HighJump.minecraftClient.player.getVelocity();
            HighJump.minecraftClient.player.setVelocity(VanillaChestLootTableGenerator.x, 1.0, VanillaChestLootTableGenerator.z);
            if (this.jumpTicks > 4) {
                this.jumpActive = false;
            }
            return;
        }
        Vec3d WallPlayerSkullBlock = HighJump.minecraftClient.player.getPos();
        int n = MathHelper.floor((double)WallPlayerSkullBlock.x) >> 4;
        int n2 = MathHelper.floor((double)WallPlayerSkullBlock.z) >> 4;
        for (int i = n - 1; i <= n + 1; ++i) {
            for (int j = n2 - 1; j <= n2 + 1; ++j) {
                WorldChunk class_28182 = HighJump.minecraftClient.world.getChunkManager().getWorldChunk(i, j);
                if (class_28182 == null) continue;
                for (Object blockEntityValue : class_28182.getBlockEntities().values()) {
                    float f;
                    double d;
                    if (!(blockEntityValue instanceof ShulkerBoxBlockEntity shulkerBox)) continue;
                    BlockPos shulkerPosition = shulkerBox.getPos();
                    double d2 = WallPlayerSkullBlock.x - ((double)shulkerPosition.getX() + 0.5);
                    double d3 = WallPlayerSkullBlock.z - ((double)shulkerPosition.getZ() + 0.5);
                    double d4 = Math.sqrt(d2 * d2 + d3 * d3);
                    double d5 = Math.abs(WallPlayerSkullBlock.y - ((double)shulkerPosition.getY() + 0.5));
                    double d6 = d = HighJump.minecraftClient.player.getVelocity().y > 1.0 ? 30.0 : 2.0;
                    if (!(d4 <= 1.5) || !(d5 <= d) || HighJump.minecraftClient.player.fallDistance != 0.0f || !((f = shulkerBox.getAnimationProgress(1.0f)) > 0.0f) || f == 1.0f) continue;
                    this.jumpActive = true;
                    this.jumpTicks = 0;
                    Vec3d VanillaEntityLootTableGenerator = HighJump.minecraftClient.player.getVelocity();
                    HighJump.minecraftClient.player.setVelocity(VanillaEntityLootTableGenerator.x, 1.0, VanillaEntityLootTableGenerator.z);
                    minecraftClient.setScreen(null);
                    return;
                }
            }
        }
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        this.jumpActive = false;
        this.jumpTicks = 0;
    }
}
