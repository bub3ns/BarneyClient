package moscow.rockstar.world.navigation.strategies;

import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.BlockNavigationFactory;

/**
 * Moves across a short gap and down to a supported landing position.
 */
public final class DropStrategy extends ExcavationController {
    public static final int TARGET_BLOCK_COUNT = 3;
    public static final int MAX_DROP_DISTANCE = 12;

    public DropStrategy(BlockPositionOffset start, BlockPositionOffset end) {
        super(start, end);
    }

    protected double getLookaheadDistance() {
        int dropDistance = this.getStartPosition().getY() - this.getEndPosition().getY();
        return 1.2 + dropDistance * 0.15;
    }

    @Override
    public boolean tickNavigation() {
        return this.updateRotation() == NavigationResult.REACHED_STEP;
    }

    public boolean isDropComplete() {
        return this.hasReachedEnd(0.65, 1.5);
    }

    public boolean isValidDrop(BlockNavigationFactory navigation) {
        BlockPositionOffset start = BlockPositionOffset.fromBlockPosition(this.getStartPosition());
        BlockPositionOffset end = BlockPositionOffset.fromBlockPosition(this.getEndPosition());
        int dropDistance = start.getY() - end.getY();
        if (dropDistance < 2 || dropDistance > MAX_DROP_DISTANCE
            || Math.abs(end.getX() - start.getX()) + Math.abs(end.getZ() - start.getZ()) != 1) {
            return false;
        }
        for (int y = start.getY() - 1; y > end.getY(); y--) {
            if (!navigation.isOpenSpace(end.getX(), y, end.getZ())) {
                return false;
            }
        }
        if (!navigation.isWalkable(end.getX(), end.getY(), end.getZ())) {
            return false;
        }

        double startHeight = navigation.surfaceHeightAt(start.getX(), start.getY(), start.getZ());
        if (Double.isNaN(startHeight)) {
            startHeight = start.getY();
        }
        this.setSurfaceHeights(startHeight, end.getY());
        this.setMovementCost(1.5 + dropDistance * 0.35);
        return navigation.isClearPath(
            start.getX(), start.getZ(), end.getX(), end.getZ(),
            end.getY() + 0.05, startHeight + 1.8);
    }

    public NavigationResult updateRotation() {
        if (this.getClientPlayer() == null) {
            return NavigationResult.UNAVAILABLE;
        }
        if (this.isDropComplete()) {
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
