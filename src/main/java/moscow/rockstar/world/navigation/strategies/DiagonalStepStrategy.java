package moscow.rockstar.world.navigation.strategies;

import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.BlockNavigationFactory;

/**
 * Moves diagonally when both corner cells leave enough room for the player.
 */
public final class DiagonalStepStrategy extends ExcavationController {
    private int stationaryTicks;
    private double lastPlayerDistance = Double.NaN;

    public DiagonalStepStrategy(BlockPositionOffset start, BlockPositionOffset end) {
        super(start, end);
    }

    protected double getLookaheadDistance() {
        return 1.4142;
    }

    public boolean isValidDiagonalStep(BlockNavigationFactory navigation) {
        BlockPositionOffset start = BlockPositionOffset.fromBlockPosition(this.getStartPosition());
        BlockPositionOffset end = BlockPositionOffset.fromBlockPosition(this.getEndPosition());
        if (end.getY() != start.getY()
            || Math.abs(end.getX() - start.getX()) != 1
            || Math.abs(end.getZ() - start.getZ()) != 1) {
            return false;
        }

        double startHeight = navigation.surfaceHeightAt(start.getX(), start.getY(), start.getZ());
        double endHeight = navigation.surfaceHeightAt(end.getX(), end.getY(), end.getZ());
        if (Double.isNaN(startHeight) || Double.isNaN(endHeight)
            || Math.abs(endHeight - startHeight) > 0.62) {
            return false;
        }

        boolean xCornerClear = navigation.isOpenSpace(end.getX(), start.getY(), start.getZ())
            && navigation.isOpenSpace(end.getX(), start.getY() + 1, start.getZ());
        boolean zCornerClear = navigation.isOpenSpace(start.getX(), start.getY(), end.getZ())
            && navigation.isOpenSpace(start.getX(), start.getY() + 1, end.getZ());
        if (!xCornerClear || !zCornerClear || !navigation.isWalkable(end.getX(), end.getY(), end.getZ())) {
            return false;
        }

        this.setSurfaceHeights(startHeight, endHeight);
        this.setMovementCost(1.4142 + Math.abs(endHeight - startHeight));
        return navigation.isClearPath(
            start.getX(), start.getZ(), end.getX(), end.getZ(),
            Math.max(startHeight, endHeight) + 0.05,
            Math.max(startHeight, endHeight) + 1.8);
    }

    @Override
    public boolean tickNavigation() {
        return this.updateRotation() == NavigationResult.REACHED_STEP;
    }

    public NavigationResult updateRotation() {
        if (this.getClientPlayer() == null) {
            return NavigationResult.UNAVAILABLE;
        }
        if (this.hasReachedEnd(0.45, 0.7)) {
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
        this.moveTowardEnd(false, false);
        return NavigationResult.IN_PROGRESS;
    }

    @Override
    public void stopNavigation() {
        this.stationaryTicks = 0;
        this.lastPlayerDistance = Double.NaN;
        super.stopNavigation();
    }
}
