package moscow.rockstar.modules.player.placement;

import moscow.rockstar.math.Rotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;

record PlacementTarget(BlockPos blockPosition, BlockPos targetBlock, Direction placementSide,
                       Vec3d position, Rotation rotation, boolean allowFallbackHit) {
    PlacementTarget withRotation(Rotation rotation) {
        return new PlacementTarget(this.blockPosition, this.targetBlock, this.placementSide,
                this.position, rotation, this.allowFallbackHit);
    }

    PlacementTarget withFallbackHit(boolean allowFallbackHit) {
        return new PlacementTarget(this.blockPosition, this.targetBlock, this.placementSide,
                this.position, this.rotation, allowFallbackHit);
    }

    BlockHitResult createHitResult() {
        return new BlockHitResult(this.position, this.placementSide, this.targetBlock, false);
    }

    public BlockPos getPlacedBlock() {
        return this.blockPosition;
    }

    public BlockPos getSupportBlock() {
        return this.targetBlock;
    }

    public Direction getPlacementSide() {
        return this.placementSide;
    }

    public Vec3d getHitPosition() {
        return this.position;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public boolean allowsFallbackHit() {
        return this.allowFallbackHit;
    }
}
