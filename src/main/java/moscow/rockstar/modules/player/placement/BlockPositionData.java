package moscow.rockstar.modules.player.placement;

import net.minecraft.util.math.BlockPos;

record BlockPositionData(BlockPos blockPosition, double offsetX, double offsetZ) {
    public BlockPos getBlockPosition() {
        return this.blockPosition;
    }

    public double getOffsetX() {
        return this.offsetX;
    }

    public double getOffsetZ() {
        return this.offsetZ;
    }
}
