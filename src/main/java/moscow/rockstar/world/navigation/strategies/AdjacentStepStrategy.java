package moscow.rockstar.world.navigation.strategies;

import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.BlockNavigationFactory;

/**
 * Moves the player to a horizontally adjacent walkable block.
 */
public final class AdjacentStepStrategy extends ExcavationController {
    private final boolean breakingCurrentBlock;
    private int stationaryTicks;
    private double lastPlayerDistance = Double.NaN;

    public AdjacentStepStrategy(BlockPositionOffset start, BlockPositionOffset end) {
        this(start, end, false);
    }

    public AdjacentStepStrategy(BlockPositionOffset start, BlockPositionOffset end, boolean breakingCurrentBlock) {
        super(start, end);
        this.breakingCurrentBlock = breakingCurrentBlock;
    }

    protected double getLookaheadDistance() {
        return this.breakingCurrentBlock ? 2.0 : 1.0;
    }

    @Override
    public boolean tickNavigation() {
        return this.updateRotation() == NavigationResult.REACHED_STEP;
    }

    public boolean isValidAdjacentStep(BlockNavigationFactory navigation) {
        BlockPositionOffset start = BlockPositionOffset.fromBlockPosition(this.getStartPosition());
        BlockPositionOffset end = BlockPositionOffset.fromBlockPosition(this.getEndPosition());
        int verticalChange = end.getY() - start.getY();
        if (this.breakingCurrentBlock ? verticalChange > 0 || verticalChange < -1 : Math.abs(verticalChange) > 1) {
            return false;
        }
        if (Math.abs(end.getX() - start.getX()) + Math.abs(end.getZ() - start.getZ()) != 1) {
            return false;
        }

        double startHeight = navigation.surfaceHeightAt(start.getX(), start.getY(), start.getZ());
        double endHeight = navigation.surfaceHeightAt(end.getX(), end.getY(), end.getZ());
        if (Double.isNaN(startHeight)) {
            startHeight = start.getY();
        }
        if (Double.isNaN(endHeight)) {
            return false;
        }
        if (!navigation.isWalkable(end.getX(), end.getY(), end.getZ())) {
            return false;
        }
        if (!this.breakingCurrentBlock && Math.abs(endHeight - startHeight) > 0.62) {
            return false;
        }

        this.setSurfaceHeights(startHeight, endHeight);
        this.setMovementCost(1.0 + Math.abs(endHeight - startHeight));
        double upperHeight = Math.max(startHeight, endHeight) + 1.8;
        return navigation.isClearPath(
            start.getX(), start.getZ(), end.getX(), end.getZ(),
            Math.min(startHeight, endHeight) + 0.05, upperHeight);
    }

    public NavigationResult updateRotation() {
        if (this.getClientPlayer() == null) {
            return NavigationResult.UNAVAILABLE;
        }
        if (this.hasReachedEnd(this.breakingCurrentBlock ? 0.45 : 0.4,
            this.breakingCurrentBlock ? 1.2 : 0.7)) {
            return NavigationResult.REACHED_STEP;
        }

        double distance = this.horizontalDistanceToEnd(
            this.getClientPlayer().getX(), this.getClientPlayer().getZ());
        if (!Double.isNaN(this.lastPlayerDistance) && distance >= this.lastPlayerDistance - 0.01) {
            this.stationaryTicks++;
        } else {
            this.stationaryTicks = 0;
        }
        this.lastPlayerDistance = distance;
        if (this.stationaryTicks > 60) {
            return NavigationResult.UNAVAILABLE;
        }

        boolean jump = this.getEndPosition().getY() > this.getStartPosition().getY()
            || (this.getClientPlayer().horizontalCollision && !this.breakingCurrentBlock);
        this.moveTowardEnd(jump, false);
        return NavigationResult.IN_PROGRESS;
    }

    private boolean isPlayerPastStepBoundary(double playerX, double playerZ) {
        BlockPositionOffset start = BlockPositionOffset.fromBlockPosition(this.getStartPosition());
        double dx = playerX - (start.getX() + 0.5);
        double dz = playerZ - (start.getZ() + 0.5);
        double destinationDx = this.getEndPosition().getX() - start.getX();
        double destinationDz = this.getEndPosition().getZ() - start.getZ();
        return dx * destinationDx + dz * destinationDz > 0.9;
    }

    @Override
    public void stopNavigation() {
        this.stationaryTicks = 0;
        this.lastPlayerDistance = Double.NaN;
        super.stopNavigation();
    }
}
