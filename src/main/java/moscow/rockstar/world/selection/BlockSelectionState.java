package moscow.rockstar.world.selection;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

/** Stores the two endpoints of the world block selection used by scripting and automation. */
public final class BlockSelectionState {
    private static final BlockSelectionState INSTANCE = new BlockSelectionState();

    @Nullable
    private BlockPos firstPosition;
    @Nullable
    private BlockPos secondPosition;

    private BlockSelectionState() {
    }

    public static BlockSelectionState getInstance() {
        return INSTANCE;
    }

    /** Adds a selection endpoint; a third click starts a new selection. */
    public synchronized void select(BlockPos position) {
        if (position == null) {
            return;
        }
        BlockPos immutablePosition = position.toImmutable();
        if (this.firstPosition == null || this.secondPosition != null) {
            this.firstPosition = immutablePosition;
            this.secondPosition = null;
        } else {
            this.secondPosition = immutablePosition;
        }
    }

    public synchronized void setSelection(BlockPos startPosition, BlockPos endPosition) {
        if (startPosition == null || endPosition == null) {
            this.clearSelection();
            return;
        }
        this.firstPosition = startPosition.toImmutable();
        this.secondPosition = endPosition.toImmutable();
    }

    public synchronized void clearSelection() {
        this.firstPosition = null;
        this.secondPosition = null;
    }

    public synchronized boolean hasSelection() {
        return this.firstPosition != null && this.secondPosition != null;
    }

    @Nullable
    public synchronized BlockPos getStartPosition() {
        return this.firstPosition;
    }

    @Nullable
    public synchronized BlockPos getEndPosition() {
        return this.secondPosition;
    }

    @Nullable
    public synchronized Box getBounds() {
        if (!this.hasSelection()) {
            return null;
        }
        BlockPos start = this.firstPosition;
        BlockPos end = this.secondPosition;
        return new Box(
            Math.min(start.getX(), end.getX()),
            Math.min(start.getY(), end.getY()),
            Math.min(start.getZ(), end.getZ()),
            Math.max(start.getX(), end.getX()) + 1.0,
            Math.max(start.getY(), end.getY()) + 1.0,
            Math.max(start.getZ(), end.getZ()) + 1.0);
    }
}
