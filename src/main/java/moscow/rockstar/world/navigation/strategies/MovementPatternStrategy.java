package moscow.rockstar.world.navigation.strategies;

import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.BlockNavigationFactory;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * A single movement edge in the block-navigation graph.
 */
public class MovementPatternStrategy extends ExcavationController {
    private static final double ROTATION_DISTANCE = 1.5;
    private static final int ACTION_DURATION_TICKS = 120;
    private final MovementPattern movementPattern;

    public MovementPatternStrategy(
        BlockPositionOffset start,
        BlockPositionOffset end,
        MovementPattern movementPattern
    ) {
        super(start, end);
        this.movementPattern = movementPattern;
    }

    public MovementPattern getMovementPattern() {
        return this.movementPattern;
    }

    protected double getLookaheadDistance() {
        return switch (this.movementPattern) {
            case FLAT -> 3.0 * Math.max(1.0, this.horizontalDistanceToEnd(
                this.getStartPosition().getX(), this.getStartPosition().getZ()));
            case VERTICAL_ASCENT, VERTICAL_DESCENT, STEP_DESCENT -> 3.0;
            case DIAGONAL -> 3.5;
            case DIAGONAL_ASCENT -> 4.5;
        };
    }

    public int getActionDuration() {
        return ACTION_DURATION_TICKS;
    }

    @Override
    public boolean tickNavigation() {
        return this.updateRotation() == NavigationResult.REACHED_STEP;
    }

    public boolean isPatternFinished() {
        return this.hasReachedEnd(0.6, 0.8);
    }

    public boolean isValidMovementPattern(BlockNavigationFactory navigation) {
        BlockPositionOffset start = BlockPositionOffset.fromBlockPosition(this.getStartPosition());
        BlockPositionOffset end = BlockPositionOffset.fromBlockPosition(this.getEndPosition());
        int dx = end.getX() - start.getX();
        int dy = end.getY() - start.getY();
        int dz = end.getZ() - start.getZ();
        int horizontalSteps = Math.abs(dx) + Math.abs(dz);
        double startHeight = navigation.surfaceHeightAt(start.getX(), start.getY(), start.getZ());
        double endHeight = navigation.surfaceHeightAt(end.getX(), end.getY(), end.getZ());
        if (Double.isNaN(startHeight)) {
            startHeight = start.getY();
        }
        if (Double.isNaN(endHeight)) {
            endHeight = end.getY();
        }

        boolean valid = switch (this.movementPattern) {
            case FLAT -> dy == 0 && (horizontalSteps == 1 || Math.abs(dx) == 1 && Math.abs(dz) == 1);
            case VERTICAL_ASCENT -> dx == 0 && dz == 0 && dy == 1;
            case VERTICAL_DESCENT -> dx == 0 && dz == 0 && dy == -1;
            case STEP_DESCENT -> horizontalSteps == 1 && dy >= -1 && dy <= 0;
            case DIAGONAL -> Math.abs(dx) == 1 && Math.abs(dz) == 1 && dy == 0;
            case DIAGONAL_ASCENT -> horizontalSteps == 1 && dy == 1;
        };
        if (!valid || !navigation.isWalkable(end.getX(), end.getY(), end.getZ())) {
            return false;
        }
        if (!navigation.canTraverse(
            start.getX(), start.getZ(), end.getX(), end.getZ(),
            Math.min(startHeight, endHeight) + 0.05,
            Math.max(startHeight, endHeight) + 1.8
        )) {
            return false;
        }
        this.setSurfaceHeights(startHeight, endHeight);
        double distance = Math.max(1.0, Math.hypot(dx, dz));
        this.setMovementCost(distance * (this.movementPattern == MovementPattern.DIAGONAL ? 1.41 : 1.0));
        return true;
    }

    private boolean isTargetClear(BlockNavigationFactory navigation) {
        BlockPositionOffset start = BlockPositionOffset.fromBlockPosition(this.getStartPosition());
        BlockPositionOffset end = BlockPositionOffset.fromBlockPosition(this.getEndPosition());
        return navigation.isOpenSpace(start.getX(), start.getY() + 1, start.getZ())
            && navigation.isOpenSpace(end.getX(), end.getY() + 1, end.getZ());
    }

    public NavigationResult updateRotation() {
        ClientPlayerEntity player = this.getClientPlayer();
        if (player == null) {
            return NavigationResult.UNAVAILABLE;
        }
        if (player.isSubmergedInWater() && player.getAir() < 60) {
            return NavigationResult.UNAVAILABLE;
        }
        if (this.hasReachedEnd(0.6, 0.8)) {
            return NavigationResult.REACHED_STEP;
        }
        boolean verticalMovement = this.movementPattern == MovementPattern.VERTICAL_ASCENT
            || this.movementPattern == MovementPattern.VERTICAL_DESCENT;
        boolean jump = this.movementPattern == MovementPattern.VERTICAL_ASCENT
            || player.getY() < (double)this.getEndPosition().getY() + 0.1;
        this.moveTowardEnd(jump, !verticalMovement);
        return NavigationResult.IN_PROGRESS;
    }

    @Override
    public void stopNavigation() {
        this.clearMovementInput();
    }

    public enum MovementPattern {
        FLAT,
        VERTICAL_ASCENT,
        VERTICAL_DESCENT,
        STEP_DESCENT,
        DIAGONAL,
        DIAGONAL_ASCENT
    }
}
