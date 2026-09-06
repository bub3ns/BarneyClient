package moscow.rockstar.world.navigation.strategies;

import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.mining.BlockBreakValidator;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.BlockNavigationFactory;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Opens the block below the player before descending one block.
 */
public final class DownwardClearanceStrategy extends ExcavationController {
    private static final double BASE_CLEARANCE_DELAY = 1.5;
    private static final double CLEARANCE_DELAY_PER_BLOCK = 0.4;
    private final BlockBreakValidator blockBreakValidator = new BlockBreakValidator();
    private double breakDuration = Double.POSITIVE_INFINITY;
    private int processedBlockCount;
    private BlockPos activeBreakingPosition;

    public DownwardClearanceStrategy(BlockPositionOffset start, BlockPositionOffset end) {
        super(start, end);
    }

    protected double getLookaheadDistance() {
        return this.breakDuration;
    }

    public int getEstimatedTicks() {
        return Math.min(600, this.processedBlockCount * 2 + 120);
    }

    public boolean isValidDownwardClearance(BlockNavigationFactory navigation) {
        BlockPositionOffset start = BlockPositionOffset.fromBlockPosition(this.getStartPosition());
        BlockPositionOffset end = BlockPositionOffset.fromBlockPosition(this.getEndPosition());
        if (end.getX() != start.getX() || end.getZ() != start.getZ()
            || start.getY() - end.getY() != 1) {
            return false;
        }
        int breakTicks = navigation.getBreakingTicks(end.getX(), end.getY(), end.getZ());
        if (breakTicks <= 0 || breakTicks == Integer.MAX_VALUE
            || !navigation.isOpenSpace(end.getX(), end.getY() - 1, end.getZ())) {
            return false;
        }
        this.processedBlockCount = breakTicks;
        this.breakDuration = BASE_CLEARANCE_DELAY
            + breakTicks / 20.0 * 1.5
            + CLEARANCE_DELAY_PER_BLOCK;
        this.setMovementCost(this.breakDuration + 1.0);
        return true;
    }

    @Override
    public boolean tickNavigation() {
        return this.updateRotation() == NavigationResult.REACHED_STEP;
    }

    public NavigationResult updateRotation() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null) {
            return NavigationResult.UNAVAILABLE;
        }

        BlockPos target = this.getEndPosition();
        BlockState state = client.world.getBlockState(target);
        if (!state.isAir() && !state.isReplaceable()
            && !state.getCollisionShape(client.world, target).isEmpty()) {
            this.activeBreakingPosition = target;
            if (this.blockBreakValidator.canBreak(target, Direction.UP)) {
                client.interactionManager.updateBlockBreakingProgress(target, Direction.UP);
                player.swingHand(player.getActiveHand());
            }
            return NavigationResult.IN_PROGRESS;
        }
        if (this.activeBreakingPosition != null) {
            client.interactionManager.cancelBlockBreaking();
            this.activeBreakingPosition = null;
        }
        if (this.hasReachedEnd(0.55, 0.9)) {
            return NavigationResult.REACHED_STEP;
        }
        this.moveTowardEnd(false, false);
        return NavigationResult.IN_PROGRESS;
    }

    @Override
    public void stopNavigation() {
        this.activeBreakingPosition = null;
        super.stopNavigation();
    }
}
