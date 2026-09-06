/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockView
 *  net.minecraft.Direction
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockState
 *  net.minecraft.EmptyBlockView
 *  net.minecraft.Identifier
 *  net.minecraft.BlockHitResult
 *  net.minecraft.Registries
 */
package moscow.rockstar.network.bot;

import net.minecraft.util.math.BlockPos;
import moscow.rockstar.network.bot.BotBehaviorStrategy;
import moscow.rockstar.network.bot.BotController;
import moscow.rockstar.network.bot.CyclicRebreakStrategy;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.registry.Registries;

public class WoodRebreakStrategy
implements BotBehaviorStrategy {
    private RebreakPhase phase = RebreakPhase.ACQUIRE_TARGET;
    private BlockPos targetPosition;
    private Direction targetFace = Direction.UP;
    private long lastRebreakMillis;
    private int searchAttempts;
    private int rebreakAttempts;
    private boolean placementStarted;
    private boolean placementConfirmed;
    private int griefServerSlot = -1;

    @Override
    public void applyBehavior(BotController botController) {
        if (botController == null) {
            return;
        }
        switch (this.phase.ordinal()) {
            case 0: {
                this.acquireTarget(botController);
                break;
            }
            case 1: {
                this.waitForBreakCompletion(botController);
                break;
            }
            case 2: {
                this.rebreakTarget(botController);
                break;
            }
            case 3: {
                this.returnToHub(botController);
                break;
            }
            case 4: {
                this.resumeCycle(botController);
            }
        }
    }

    private void acquireTarget(BotController botController) {
        BotController.BlockHitCandidate blockHitCandidate = this.findTargetHit(botController);
        if (blockHitCandidate == null) {
            this.clearTarget();
            botController.applyMotionDamping();
            return;
        }
        this.targetPosition = blockHitCandidate.getHitResult().getBlockPos().toImmutable();
        this.targetFace = blockHitCandidate.getHitResult().getSide();
        this.updateAimAtTarget(botController);
        if (!this.isTargetReachable(botController)) {
            return;
        }
        this.searchAttempts = 0;
        if (!this.placementConfirmed) {
            this.phase = RebreakPhase.BREAK_TARGET;
            this.rebreakAttempts = 0;
            this.placementStarted = false;
        } else {
            this.phase = RebreakPhase.WAIT_FOR_BREAK;
        }
    }

    private void waitForBreakCompletion(BotController botController) {
        Direction class_23502;
        if (this.targetPosition == null || !this.isValidTargetBlock(botController, this.targetPosition)) {
            this.phase = RebreakPhase.ACQUIRE_TARGET;
            this.placementStarted = false;
            return;
        }
        this.updateAimAtTarget(botController);
        if (!this.isTargetReachable(botController)) {
            return;
        }
        Direction class_23503 = class_23502 = this.targetFace == null ? botController.getBlockFacing(this.targetPosition) : this.targetFace;
        if (!this.placementStarted) {
            botController.startBlockBreaking(this.targetPosition, class_23502);
            this.placementStarted = true;
            this.rebreakAttempts = 0;
            return;
        }
        ++this.rebreakAttempts;
        if (this.rebreakAttempts < Math.max(1, botController.getControlState().getBreakAttemptLimit())) {
            return;
        }
        botController.stopBlockBreaking(this.targetPosition, class_23502);
        this.placementConfirmed = true;
        this.placementStarted = false;
        this.rebreakAttempts = 0;
        this.phase = RebreakPhase.WAIT_FOR_BREAK;
    }

    private void rebreakTarget(BotController botController) {
        BotController.BlockHitCandidate blockHitCandidate;
        if (++this.searchAttempts >= Math.max(1, botController.getControlState().getMaxSearchAttempts())) {
            this.griefServerSlot = botController.getWorldState().findInventorySlot(new String[]{"\u0413\u0420\u0418\u0424", "GRIEF"});
            if (this.griefServerSlot > 0) {
                botController.sendChatOrCommand("/sellwood");
                this.phase = RebreakPhase.SELL_WOOD;
                this.searchAttempts = 0;
                return;
            }
            this.searchAttempts = 0;
        }
        if ((blockHitCandidate = this.findTargetHit(botController)) != null) {
            this.targetPosition = blockHitCandidate.getHitResult().getBlockPos().toImmutable();
            this.targetFace = blockHitCandidate.getHitResult().getSide();
        }
        if (this.targetPosition == null || !this.isValidTargetBlock(botController, this.targetPosition)) {
            this.phase = RebreakPhase.ACQUIRE_TARGET;
            return;
        }
        this.updateAimAtTarget(botController);
        if (!this.isTargetReachable(botController)) {
            return;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastRebreakMillis < botController.getControlState().getRebreakDelayMillis()) {
            return;
        }
        Direction class_23502 = this.targetFace == null ? botController.getBlockFacing(this.targetPosition) : this.targetFace;
        botController.breakBlockAtHit(new BlockHitResult(this.targetPosition.toCenterPos(), class_23502, this.targetPosition, false));
        this.lastRebreakMillis = l;
    }

    private void returnToHub(BotController botController) {
        botController.applyMotionDamping();
        if (++this.searchAttempts >= Math.max(1, botController.getControlState().getNavigationAttemptLimit())) {
            botController.sendChatOrCommand("/hub");
            this.phase = RebreakPhase.RETURN_TO_HUB;
            this.searchAttempts = 0;
        }
    }

    private void resumeCycle(BotController botController) {
        botController.applyMotionDamping();
        if (++this.searchAttempts >= Math.max(1, botController.getControlState().getNavigationAttemptLimit())) {
            botController.setBehaviorStrategy(new CyclicRebreakStrategy(this.griefServerSlot));
        }
    }

    private BotController.BlockHitCandidate findTargetHit(BotController botController) {
        BotController.BlockHitCandidate blockHitCandidate = botController.findBlockHit(botController.getControlState().getTargetReachDistance());
        if (blockHitCandidate == null || !this.isValidTargetBlock(botController, blockHitCandidate.getHitResult().getBlockPos())) {
            return null;
        }
        return blockHitCandidate;
    }

    private boolean isValidTargetBlock(BotController botController, BlockPos adminsky) {
        if (adminsky == null || !botController.getWorldState().isValidBlock(adminsky) && !botController.getWorldState().isAlternativeBlock(adminsky)) {
            return false;
        }
        BlockState class_26802 = botController.getWorldState().getBlockState(adminsky);
        if (class_26802.isAir() || class_26802.getHardness((BlockView)EmptyBlockView.INSTANCE, adminsky) < 0.0f) {
            return false;
        }
        return !botController.getControlState().isControlInputValid() || this.isAllowedWoodBlock(class_26802);
    }

    private boolean isAllowedWoodBlock(BlockState class_26802) {
        Identifier class_29602 = Registries.BLOCK.getId(class_26802.getBlock());
        if (class_29602 == null) {
            return false;
        }
        String string = class_29602.toString();
        return string.contains("_log") || string.contains("_wood") || string.equals("minecraft:crimson_stem") || string.equals("minecraft:warped_stem") || string.equals("minecraft:stripped_crimson_stem") || string.equals("minecraft:stripped_warped_stem");
    }

    private void updateAimAtTarget(BotController botController) {
        if (this.targetPosition == null) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = this.targetPosition.toCenterPos();
        botController.lookAtPosition(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z);
        botController.applyMotionDamping();
    }

    private boolean isTargetReachable(BotController botController) {
        return this.targetPosition != null && botController.isAimWithinTolerance(this.targetPosition.toCenterPos(), botController.getControlState().getAimToleranceDegrees());
    }

    private void clearTarget() {
        this.targetPosition = null;
        this.targetFace = Direction.UP;
    }

    @Override
    public String getDescription() {
        return this.targetPosition == null ? "CyclicRebreak " + String.valueOf((Object)this.phase) : "CyclicRebreak " + String.valueOf((Object)this.phase) + " " + this.targetPosition.getX() + " " + this.targetPosition.getY() + " " + this.targetPosition.getZ();
    }

    static enum RebreakPhase {
        ACQUIRE_TARGET,
        BREAK_TARGET,
        WAIT_FOR_BREAK,
        SELL_WOOD,
        RETURN_TO_HUB;
}
}

