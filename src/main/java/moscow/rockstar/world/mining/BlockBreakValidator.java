package moscow.rockstar.world.mining;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Checks whether the client can currently interact with a block face.
 *
 * <p>This was previously represented by a navigation strategy with a
 * decompiler-generated name. It is a small interaction guard, not a movement
 * pattern.</p>
 */
public final class BlockBreakValidator {
    private static final double MAX_REACH = 5.0;

    public boolean canBreak(BlockPos position, Direction face) {
        if (position == null || face == null) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || client.interactionManager == null) {
            return false;
        }
        BlockState state = client.world.getBlockState(position);
        if (state.isAir() || state.getHardness(client.world, position) < 0.0f) {
            return false;
        }
        PlayerEntity player = client.player;
        Vec3d target = Vec3d.ofCenter(position);
        return player.getEyePos().squaredDistanceTo(target) <= MAX_REACH * MAX_REACH;
    }
}
