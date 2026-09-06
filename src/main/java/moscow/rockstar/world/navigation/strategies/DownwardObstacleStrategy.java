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
 * Clears a short vertical column while approaching a lower adjacent block.
 */
public final class DownwardObstacleStrategy extends ExcavationController {
    private static final double BASE_CLEARANCE_DELAY = 1.5;
    private static final double CLEARANCE_DELAY_PER_BLOCK = 0.4;
    private final BlockBreakValidator blockBreakValidator = new BlockBreakValidator();
    private double breakDuration = Double.POSITIVE_INFINITY;
    private int processedBlockCount;
    private BlockPos activeBreakingPosition;

    public DownwardObstacleStrategy(BlockPositionOffset start, BlockPositionOffset end) {
        super(start, end);
    }

    protected double getLookaheadDistance() {
        return this.breakDuration;
    }

    public int getEstimatedTicks() {
        return Math.min(600, this.processedBlockCount * 2 + 120);
    }

    public boolean isValidDownwardObstacle(BlockNavigationFactory navigation) {
        BlockPositionOffset start = BlockPositionOffset.fromBlockPosition(this.getStartPosition());
        BlockPositionOffset end = BlockPositionOffset.fromBlockPosition(this.getEndPosition());
        if (start.getY() - end.getY() != 1
            || Math.abs(end.getX() - start.getX()) + Math.abs(end.getZ() - start.getZ()) != 1) {
            return false;
        }

        int breakTicks = 0;
        int blocksToClear = 0;
        for (BlockPos position : this.obstaclePositions()) {
            int ticks = navigation.getBreakingTicks(position.getX(), position.getY(), position.getZ());
            if (ticks == Integer.MAX_VALUE) {
                return false;
            }
            if (ticks > 0) {
                breakTicks += ticks;
                blocksToClear++;
            }
        }
        if (blocksToClear == 0 || !navigation.isWalkable(end.getX(), end.getY(), end.getZ())) {
            return false;
        }
        this.processedBlockCount = breakTicks;
        this.breakDuration = BASE_CLEARANCE_DELAY
            + breakTicks / 20.0 * 1.5
            + blocksToClear * CLEARANCE_DELAY_PER_BLOCK;
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

        BlockPos obstruction = this.findObstruction(client);
        if (obstruction != null) {
            if (this.activeBreakingPosition != null && !this.activeBreakingPosition.equals(obstruction)) {
                client.interactionManager.cancelBlockBreaking();
            }
            this.activeBreakingPosition = obstruction;
            Direction face = this.getBestBlockFace(player, obstruction);
            if (this.blockBreakValidator.canBreak(obstruction, face)) {
                client.interactionManager.updateBlockBreakingProgress(obstruction, face);
                player.swingHand(player.getActiveHand());
            }
            return NavigationResult.IN_PROGRESS;
        }

        if (this.activeBreakingPosition != null) {
            client.interactionManager.cancelBlockBreaking();
            this.activeBreakingPosition = null;
        }
        if (this.hasReachedEnd(0.55, 1.0)) {
            return NavigationResult.REACHED_STEP;
        }
        this.moveTowardEnd(false, false);
        return NavigationResult.IN_PROGRESS;
    }

    public NavigationResult getNavigationResult() {
        return this.updateRotation();
    }

    public Direction getBestBlockFace(ClientPlayerEntity player, BlockPos position) {
        double dx = player.getX() - (position.getX() + 0.5);
        double dy = player.getEyeY() - (position.getY() + 0.5);
        double dz = player.getZ() - (position.getZ() + 0.5);
        if (Math.abs(dx) >= Math.abs(dy) && Math.abs(dx) >= Math.abs(dz)) {
            return dx >= 0.0 ? Direction.EAST : Direction.WEST;
        }
        if (Math.abs(dz) >= Math.abs(dy)) {
            return dz >= 0.0 ? Direction.SOUTH : Direction.NORTH;
        }
        return dy >= 0.0 ? Direction.UP : Direction.DOWN;
    }

    private BlockPos[] obstaclePositions() {
        BlockPositionOffset start = BlockPositionOffset.fromBlockPosition(this.getStartPosition());
        BlockPositionOffset end = BlockPositionOffset.fromBlockPosition(this.getEndPosition());
        return new BlockPos[]{
            new BlockPos(end.getX(), start.getY() + 1, end.getZ()),
            new BlockPos(end.getX(), start.getY(), end.getZ()),
            new BlockPos(end.getX(), end.getY(), end.getZ())
        };
    }

    private BlockPos findObstruction(MinecraftClient client) {
        for (BlockPos position : this.obstaclePositions()) {
            BlockState state = client.world.getBlockState(position);
            if (!state.isAir() && !state.isReplaceable()
                && !state.getCollisionShape(client.world, position).isEmpty()) {
                return position;
            }
        }
        return null;
    }

    @Override
    public void stopNavigation() {
        this.activeBreakingPosition = null;
        super.stopNavigation();
    }
}
