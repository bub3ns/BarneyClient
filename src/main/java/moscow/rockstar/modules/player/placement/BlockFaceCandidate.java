package moscow.rockstar.modules.player.placement;

import net.minecraft.util.math.BlockPos;

record BlockFaceCandidate(BlockPos blockPosition, double overlapArea, double surfaceDelta,
                          double horizontalDistanceSquared) implements Comparable<BlockFaceCandidate> {
    public int compareCandidate(BlockFaceCandidate other) {
        if (this.surfaceDelta + 0.001 < other.surfaceDelta) {
            return -1;
        }
        if (other.surfaceDelta + 0.001 < this.surfaceDelta) {
            return 1;
        }
        if (this.overlapArea > other.overlapArea + 0.02) {
            return -1;
        }
        if (this.overlapArea + 0.02 < other.overlapArea) {
            return 1;
        }
        return Double.compare(this.horizontalDistanceSquared, other.horizontalDistanceSquared);
    }

    boolean isWithinTolerance(BlockFaceCandidate other) {
        if (this.surfaceDelta > other.surfaceDelta + 0.001) {
            return false;
        }
        return this.overlapArea + 0.02 >= other.overlapArea;
    }

    public BlockPos getBlockPosition() {
        return this.blockPosition;
    }

    public double getOverlapArea() {
        return this.overlapArea;
    }

    public double getSurfaceDelta() {
        return this.surfaceDelta;
    }

    public double getHorizontalDistanceSquared() {
        return this.horizontalDistanceSquared;
    }

    @Override
    public int compareTo(BlockFaceCandidate other) {
        return this.compareCandidate(other);
    }
}
