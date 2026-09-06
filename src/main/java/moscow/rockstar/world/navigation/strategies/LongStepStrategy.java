package moscow.rockstar.world.navigation.strategies;

import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.BlockNavigationFactory;

/**
 * Handles a two- or three-block cardinal jump.
 */
public final class LongStepStrategy extends ExcavationController {
    private static final int MAX_STATIONARY_TICKS = 40;
    private final int horizontalDistance;
    private boolean jumpRequired;
    private int stationaryTicks;
    private double lastPlayerDistance = Double.NaN;

    public LongStepStrategy(BlockPositionOffset start, BlockPositionOffset end) {
        super(start, end);
        this.horizontalDistance = Math.abs(end.getX() - start.getX())
            + Math.abs(end.getZ() - start.getZ());
    }

    protected double getLookaheadDistance() {
        return this.horizontalDistance <= 1 ? 2.5 : 4.0;
    }

    @Override
    public boolean tickNavigation() {
        return this.updateRotation() == NavigationResult.REACHED_STEP;
    }

    public boolean isJumpRequired() {
        return this.jumpRequired;
    }

    public boolean isValidLongStep(BlockNavigationFactory navigation) {
        BlockPositionOffset start = BlockPositionOffset.fromBlockPosition(this.getStartPosition());
        BlockPositionOffset end = BlockPositionOffset.fromBlockPosition(this.getEndPosition());
        if (end.getY() != start.getY()) {
            return false;
        }
        int deltaX = end.getX() - start.getX();
        int deltaZ = end.getZ() - start.getZ();
        if (deltaX != 0 && deltaZ != 0) {
            return false;
        }
        int distance = Math.abs(deltaX) + Math.abs(deltaZ);
        if (distance < 2 || distance > 3) {
            return false;
        }

        double startHeight = navigation.surfaceHeightAt(start.getX(), start.getY(), start.getZ());
        double endHeight = navigation.surfaceHeightAt(end.getX(), end.getY(), end.getZ());
        if (Double.isNaN(startHeight) || Double.isNaN(endHeight)
            || Math.abs(endHeight - startHeight) > 0.3
            || !navigation.isWalkable(end.getX(), end.getY(), end.getZ())) {
            return false;
        }

        int directionX = Integer.signum(deltaX);
        int directionZ = Integer.signum(deltaZ);
        for (int step = 1; step < distance; step++) {
            int x = start.getX() + directionX * step;
            int z = start.getZ() + directionZ * step;
            if (!navigation.isOpenSpace(x, start.getY(), z)
                || !navigation.isOpenSpace(x, start.getY() + 1, z)) {
                return false;
            }
        }
        if (!navigation.isClearPath(
            start.getX(), start.getZ(), end.getX(), end.getZ(),
            Math.max(startHeight, endHeight) + 0.05, startHeight + 2.1)) {
            return false;
        }

        this.jumpRequired = true;
        this.setSurfaceHeights(startHeight, endHeight);
        this.setMovementCost(distance + 0.75);
        return true;
    }

    public NavigationResult updateRotation() {
        if (this.getClientPlayer() == null) {
            return NavigationResult.UNAVAILABLE;
        }
        if (this.hasReachedEnd(0.65, 0.9)) {
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
        if (this.stationaryTicks >= MAX_STATIONARY_TICKS) {
            return NavigationResult.UNAVAILABLE;
        }
        this.moveTowardEnd(this.jumpRequired, this.jumpRequired);
        return NavigationResult.IN_PROGRESS;
    }

    @Override
    public void stopNavigation() {
        this.jumpRequired = false;
        this.stationaryTicks = 0;
        this.lastPlayerDistance = Double.NaN;
        super.stopNavigation();
    }
}
