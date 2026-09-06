package moscow.rockstar.world.navigation.strategies;

import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.BlockNavigationFactory;

/**
 * Handles a one-block elevation loss without treating the step as a drop.
 */
public final class StepDownStrategy extends ExcavationController {
    public StepDownStrategy(BlockPositionOffset start, BlockPositionOffset end) {
        super(start, end);
    }

    protected double getLookaheadDistance() {
        return 1.0;
    }

    @Override
    public boolean tickNavigation() {
        return this.updateRotation() == NavigationResult.REACHED_STEP;
    }

    public boolean isStepDownComplete() {
        return this.hasReachedEnd(0.45, 0.8);
    }

    public boolean isValidStepDown(BlockNavigationFactory navigation) {
        BlockPositionOffset start = BlockPositionOffset.fromBlockPosition(this.getStartPosition());
        BlockPositionOffset end = BlockPositionOffset.fromBlockPosition(this.getEndPosition());
        if (start.getY() - end.getY() != 1
            || Math.abs(end.getX() - start.getX()) + Math.abs(end.getZ() - start.getZ()) != 1) {
            return false;
        }

        double startHeight = navigation.surfaceHeightAt(start.getX(), start.getY(), start.getZ());
        double endHeight = navigation.surfaceHeightAt(end.getX(), end.getY(), end.getZ());
        if (Double.isNaN(startHeight)) {
            startHeight = start.getY();
        }
        if (Double.isNaN(endHeight) || startHeight - endHeight <= 0.2 || startHeight - endHeight > 1.3) {
            return false;
        }
        if (!navigation.isWalkable(end.getX(), end.getY(), end.getZ())) {
            return false;
        }

        this.setSurfaceHeights(startHeight, endHeight);
        this.setMovementCost(1.0 + startHeight - endHeight);
        return navigation.isClearPath(
            start.getX(), start.getZ(), end.getX(), end.getZ(),
            endHeight + 0.05, startHeight + 1.8);
    }

    public NavigationResult updateRotation() {
        if (this.getClientPlayer() == null) {
            return NavigationResult.UNAVAILABLE;
        }
        if (this.isStepDownComplete()) {
            return NavigationResult.REACHED_STEP;
        }
        this.moveTowardEnd(false, false);
        return NavigationResult.IN_PROGRESS;
    }

    @Override
    public void stopNavigation() {
        super.stopNavigation();
    }
}
