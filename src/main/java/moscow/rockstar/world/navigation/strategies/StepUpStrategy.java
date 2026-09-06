package moscow.rockstar.world.navigation.strategies;

import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.BlockNavigationFactory;

/**
 * Handles a one-block elevation gain.
 */
public final class StepUpStrategy extends ExcavationController {
    public StepUpStrategy(BlockPositionOffset start, BlockPositionOffset end) {
        super(start, end);
    }

    protected double getLookaheadDistance() {
        return 1.6;
    }

    public int getSupportIndex() {
        return 80;
    }

    public boolean isValidStepUp(BlockNavigationFactory navigation) {
        BlockPositionOffset start = BlockPositionOffset.fromBlockPosition(this.getStartPosition());
        BlockPositionOffset end = BlockPositionOffset.fromBlockPosition(this.getEndPosition());
        if (end.getY() - start.getY() != 1
            || Math.abs(end.getX() - start.getX()) + Math.abs(end.getZ() - start.getZ()) != 1) {
            return false;
        }

        double startHeight = navigation.surfaceHeightAt(start.getX(), start.getY(), start.getZ());
        double endHeight = navigation.surfaceHeightAt(end.getX(), end.getY(), end.getZ());
        if (Double.isNaN(startHeight)) {
            startHeight = start.getY();
        }
        if (Double.isNaN(endHeight) || endHeight - startHeight <= 0.2 || endHeight - startHeight > 1.3) {
            return false;
        }
        if (!navigation.isWalkable(end.getX(), end.getY(), end.getZ())) {
            return false;
        }

        this.setSurfaceHeights(startHeight, endHeight);
        this.setMovementCost(1.0 + endHeight - startHeight);
        return navigation.isClearPath(
            start.getX(), start.getZ(), end.getX(), end.getZ(),
            endHeight + 0.05, endHeight + 1.8);
    }

    @Override
    public boolean tickNavigation() {
        return this.updateRotation() == NavigationResult.REACHED_STEP;
    }

    public NavigationResult updateRotation() {
        if (this.getClientPlayer() == null) {
            return NavigationResult.UNAVAILABLE;
        }
        if (this.hasReachedEnd(0.45, 0.75)) {
            return NavigationResult.REACHED_STEP;
        }
        this.moveTowardEnd(true, false);
        return NavigationResult.IN_PROGRESS;
    }

    @Override
    public void stopNavigation() {
        super.stopNavigation();
    }
}
