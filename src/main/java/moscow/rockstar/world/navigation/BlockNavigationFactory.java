package moscow.rockstar.world.navigation;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.strategies.AdjacentStepStrategy;
import moscow.rockstar.world.navigation.strategies.DiagonalStepStrategy;
import moscow.rockstar.world.navigation.strategies.DownwardClearanceStrategy;
import moscow.rockstar.world.navigation.strategies.DownwardObstacleStrategy;
import moscow.rockstar.world.navigation.strategies.DropStrategy;
import moscow.rockstar.world.navigation.strategies.LongStepStrategy;
import moscow.rockstar.world.navigation.strategies.MovementPatternStrategy;
import moscow.rockstar.world.navigation.strategies.StepDownStrategy;
import moscow.rockstar.world.navigation.strategies.StepUpStrategy;
import moscow.rockstar.world.navigation.strategies.UpwardClearanceStrategy;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

/**
 * Builds and validates movement edges for the block pathfinder.
 */
public final class BlockNavigationFactory {
    private static final int[][] CARDINAL_OFFSETS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] DIAGONAL_OFFSETS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    private final MinecraftClient client;

    public BlockNavigationFactory() {
        this(MinecraftClient.getInstance());
    }

    public BlockNavigationFactory(MinecraftClient client) {
        this.client = client;
    }

    public static List<ExcavationController> createNavigationCandidates(
        BlockPositionOffset origin,
        BlockNavigationFactory navigation
    ) {
        List<ExcavationController> candidates = new ArrayList<>(24);
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();

        for (int[] offset : CARDINAL_OFFSETS) {
            for (int heightOffset = -1; heightOffset <= 1; heightOffset++) {
                BlockPositionOffset destination = new BlockPositionOffset(
                    x + offset[0], y + heightOffset, z + offset[1]);
                if (heightOffset > 0) {
                    addCandidate(new StepUpStrategy(origin, destination), navigation, candidates);
                } else if (heightOffset < 0) {
                    addCandidate(new StepDownStrategy(origin, destination), navigation, candidates);
                } else {
                    addCandidate(new AdjacentStepStrategy(origin, destination), navigation, candidates);
                }
            }
            addCandidate(new LongStepStrategy(
                origin,
                new BlockPositionOffset(x + offset[0] * 2, y, z + offset[1] * 2)),
                navigation,
                candidates);
            addCandidate(new LongStepStrategy(
                origin,
                new BlockPositionOffset(x + offset[0] * 3, y, z + offset[1] * 3)),
                navigation,
                candidates);
        }

        for (int[] offset : DIAGONAL_OFFSETS) {
            addCandidate(new DiagonalStepStrategy(
                origin,
                new BlockPositionOffset(x + offset[0], y, z + offset[1])),
                navigation,
                candidates);
        }

        addCandidate(new MovementPatternStrategy(
            origin,
            new BlockPositionOffset(x, y + 1, z),
            MovementPatternStrategy.MovementPattern.VERTICAL_ASCENT),
            navigation,
            candidates);
        addCandidate(new MovementPatternStrategy(
            origin,
            new BlockPositionOffset(x, y - 1, z),
            MovementPatternStrategy.MovementPattern.VERTICAL_DESCENT),
            navigation,
            candidates);
        return candidates;
    }

    private static boolean addCandidate(
        ExcavationController candidate,
        BlockNavigationFactory navigation,
        List<ExcavationController> candidates
    ) {
        boolean valid = switch (candidate) {
            case AdjacentStepStrategy step -> step.isValidAdjacentStep(navigation);
            case DiagonalStepStrategy step -> step.isValidDiagonalStep(navigation);
            case StepUpStrategy step -> step.isValidStepUp(navigation);
            case StepDownStrategy step -> step.isValidStepDown(navigation);
            case LongStepStrategy step -> step.isValidLongStep(navigation);
            case DropStrategy step -> step.isValidDrop(navigation);
            case UpwardClearanceStrategy step -> step.isValidUpwardClearance(navigation);
            case DownwardClearanceStrategy step -> step.isValidDownwardClearance(navigation);
            case DownwardObstacleStrategy step -> step.isValidDownwardObstacle(navigation);
            case MovementPatternStrategy step -> step.isValidMovementPattern(navigation);
            default -> false;
        };
        if (!valid) {
            return false;
        }
        candidates.add(candidate);
        return true;
    }

    public boolean isOpenSpace(int x, int y, int z) {
        World world = this.client.world;
        if (world == null) {
            return true;
        }
        BlockPos position = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(position);
        return state.isAir()
            || state.isReplaceable()
            || state.getCollisionShape(world, position).isEmpty();
    }

    public boolean hasSolidSupport(int x, int y, int z) {
        World world = this.client.world;
        if (world == null) {
            return true;
        }
        BlockPos position = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(position);
        return !state.isAir()
            && !state.getCollisionShape(world, position).isEmpty()
            && state.isSideSolidFullSquare(world, position, Direction.UP);
    }

    public boolean isWalkable(int x, int y, int z) {
        return this.isOpenSpace(x, y, z)
            && this.isOpenSpace(x, y + 1, z)
            && this.hasSolidSupport(x, y - 1, z);
    }

    public double surfaceHeightAt(int x, int y, int z) {
        World world = this.client.world;
        if (world == null) {
            return y;
        }
        BlockPos position = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(position);
        VoxelShape shape = state.getCollisionShape(world, position);
        if (shape.isEmpty()) {
            return Double.NaN;
        }
        return y + shape.getMax(Direction.Axis.Y);
    }

    public int getBreakingTicks(int x, int y, int z) {
        World world = this.client.world;
        if (world == null) {
            return 1;
        }
        BlockPos position = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(position);
        if (this.isOpenSpace(x, y, z)) {
            return 0;
        }
        float hardness = state.getHardness(world, position);
        if (hardness < 0.0f) {
            return Integer.MAX_VALUE;
        }
        return Math.max(1, (int)Math.ceil(hardness * 20.0f));
    }

    public boolean canTraverse(
        int startX,
        int startZ,
        int endX,
        int endZ,
        double lowerHeight,
        double upperHeight
    ) {
        int steps = Math.max(Math.abs(endX - startX), Math.abs(endZ - startZ));
        for (int index = 0; index <= steps; index++) {
            double progress = steps == 0 ? 0.0 : (double)index / steps;
            int x = (int)Math.round(startX + (endX - startX) * progress);
            int z = (int)Math.round(startZ + (endZ - startZ) * progress);
            int lowerBlock = (int)Math.floor(lowerHeight);
            int upperBlock = Math.max(lowerBlock + 1, (int)Math.ceil(upperHeight) - 1);
            for (int y = lowerBlock; y <= upperBlock; y++) {
                if (!this.isOpenSpace(x, y, z)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasNavigationSpace() {
        return this.client.world != null && this.client.player != null;
    }

    public boolean isClearPath(
        int startX,
        int startZ,
        int endX,
        int endZ,
        double lowerHeight,
        double upperHeight
    ) {
        return this.canTraverse(startX, startZ, endX, endZ, lowerHeight, upperHeight);
    }
}
