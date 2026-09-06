/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Blocks
 *  net.minecraft.Vec3i
 *  net.minecraft.Registries
 */
package moscow.rockstar.modules.player.automation.server;

import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.render.texture.TextureReloadProvider;
import moscow.rockstar.render.texture.TextureReloadTask;
import moscow.rockstar.util.Timer;
import moscow.rockstar.world.selection.BlockSelectionState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3i;
import net.minecraft.registry.Registries;

@ModuleInfo(name="Auto Bot", category=ModuleCategory.PLAYER)
public class AutoBot
extends Module {
    private final Timer scanCooldown = new Timer();
    private OperationState operationState = OperationState.IDLE;

    @Override
    public void onEnable() {
        this.operationState = OperationState.IDLE;
        BlockSelectionState.getInstance().clearSelection();
    }

    @Override
    public void onDisable() {
        if (ClientServiceRegistry.isInitialized()) {
            this.getNavigationService().clearNavigationTasks();
        }
        BlockSelectionState.getInstance().clearSelection();
    }

    @Override
    public void onTick() {
        if (AutoBot.minecraftClient.player == null || AutoBot.minecraftClient.world == null || !ClientServiceRegistry.isInitialized()) {
            return;
        }
        if (this.scanCooldown.hasElapsed(200L)) {
            BlockPos adminsky = AutoBot.minecraftClient.player.getBlockPos();
            int n = 10;
            for (int i = 0; i < n * 2; ++i) {
                for (int j = 0; j < n * 2; ++j) {
                    for (int k = 0; k < n * 2; ++k) {
                        BlockPos adminsky2 = new BlockPos((i % 2 == 0 ? -i : i) / 2, (k % 2 == 0 ? -k : k) / 2, (j % 2 == 0 ? -j : j) / 2);
                        BlockPos adminsky3 = adminsky.add((Vec3i)adminsky2);
                        if (!this.inspectCandidatePosition(adminsky3)) continue;
                        return;
                    }
                }
            }
            this.scanCooldown.reset();
        }
    }

    private boolean inspectCandidatePosition(BlockPos adminsky) {
        BlockPos adminsky2 = adminsky.up();
        if (AutoBot.minecraftClient.world.getBlockState(adminsky).getBlock() != Blocks.OBSIDIAN || AutoBot.minecraftClient.world.getBlockState(adminsky.east()).getBlock() != Blocks.OBSIDIAN || AutoBot.minecraftClient.world.getBlockState(adminsky.north()).getBlock() != Blocks.OBSIDIAN || AutoBot.minecraftClient.world.getBlockState(adminsky.south()).getBlock() != Blocks.OBSIDIAN || AutoBot.minecraftClient.world.getBlockState(adminsky.west()).getBlock() != Blocks.OBSIDIAN || AutoBot.minecraftClient.world.getBlockState(adminsky2.east()).getBlock() != Blocks.AIR || AutoBot.minecraftClient.world.getBlockState(adminsky2.north()).getBlock() != Blocks.AIR || AutoBot.minecraftClient.world.getBlockState(adminsky2.south()).getBlock() != Blocks.AIR || AutoBot.minecraftClient.world.getBlockState(adminsky2.west()).getBlock() != Blocks.AIR) {
            return false;
        }
        if (adminsky2.equals(AutoBot.minecraftClient.player.getBlockPos())) {
            if (AutoBot.minecraftClient.world.getBlockState(adminsky2).getBlock() == Blocks.TORCH) {
                if (this.operationState != OperationState.CLEARING) {
                    this.startStepDownNavigation(adminsky2);
                    this.issueNavigationCommand("newton cleararea");
                    this.operationState = OperationState.CLEARING;
                    this.scanCooldown.reset();
                    return true;
                }
            } else if (this.operationState != OperationState.FILLING) {
                this.startStepDownNavigation(adminsky2);
                this.issueNavigationCommand("newton fill " + String.valueOf(Registries.BLOCK.getId(Blocks.TORCH)));
                this.operationState = OperationState.FILLING;
                this.scanCooldown.reset();
                return true;
            }
        } else if (this.operationState != OperationState.NAVIGATING && this.operationState != OperationState.FILLING || this.operationState == OperationState.NAVIGATING && !this.getNavigationService().isNavigationActive()) {
            this.getNavigationService().registerCollisionProbe(adminsky2);
            this.operationState = OperationState.NAVIGATING;
            this.scanCooldown.reset();
            return true;
        }
        return false;
    }

    private void startStepDownNavigation(BlockPos adminsky) {
        BlockSelectionState.getInstance().setSelection(adminsky, adminsky);
    }

    private boolean issueNavigationCommand(String string) {
        return this.getNavigationService().isCommandAllowed(string);
    }

    private TextureReloadTask getNavigationService() {
        return TextureReloadProvider.getTextureReloadTask();
    }

    static enum OperationState {
        IDLE,
        NAVIGATING,
        FILLING,
        CLEARING;
}
}

