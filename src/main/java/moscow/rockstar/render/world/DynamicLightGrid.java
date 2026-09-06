package moscow.rockstar.render.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

/** The bounded dynamic-light volume used by the original ambience module. */
public final class DynamicLightGrid {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final LightVolume EMPTY_VOLUME = new LightVolume(0, 0, 0, 0, new byte[0]);
    private static volatile LightVolume activeVolume = EMPTY_VOLUME;

    private DynamicLightGrid() {
    }

    public static void update(BlockRenderView world, BlockPos center, float radius, int lightLevel) {
        int integerRadius = (int)Math.ceil(radius);
        if (integerRadius <= 0 || lightLevel <= 0) {
            clear();
            return;
        }

        int size = integerRadius * 2 + 1;
        int volumeSize = size * size * size;
        int minX = center.getX() - integerRadius;
        int minY = center.getY() - integerRadius;
        int minZ = center.getZ() - integerRadius;
        byte[] levels = new byte[volumeSize];
        byte[] distances = new byte[volumeSize];
        boolean[] visited = new boolean[volumeSize];
        int[] queue = new int[volumeSize];
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        int centerIndex = index(integerRadius, integerRadius, integerRadius, size);
        int queueHead = 0;
        int queueTail = 1;
        queue[0] = centerIndex;
        visited[centerIndex] = true;
        while (queueHead < queueTail) {
            int currentIndex = queue[queueHead++];
            int localX = currentIndex % size;
            int localY = currentIndex / size % size;
            int localZ = currentIndex / (size * size);
            int distance = Byte.toUnsignedInt(distances[currentIndex]);
            levels[currentIndex] = (byte)clamp(
                Math.round(lightLevel * Math.max(0.0f, radius + 1.0f - distance) / (radius + 1.0f)),
                0, 15);
            if (distance >= integerRadius) {
                continue;
            }

            for (Direction direction : DIRECTIONS) {
                int nextX = localX + direction.getOffsetX();
                int nextY = localY + direction.getOffsetY();
                int nextZ = localZ + direction.getOffsetZ();
                if (nextX < 0 || nextY < 0 || nextZ < 0
                    || nextX >= size || nextY >= size || nextZ >= size) {
                    continue;
                }
                int nextIndex = index(nextX, nextY, nextZ, size);
                int nextDistance = distance + 1;
                if (visited[nextIndex] || nextDistance > radius) {
                    continue;
                }
                visited[nextIndex] = true;
                distances[nextIndex] = (byte)nextDistance;
                levels[nextIndex] = (byte)clamp(
                    Math.round(lightLevel * (radius + 1.0f - nextDistance) / (radius + 1.0f)),
                    0, 15);
                mutable.set(minX + nextX, minY + nextY, minZ + nextZ);
                BlockState state = world.getBlockState(mutable);
                if (state.isOpaque()) {
                    continue;
                }
                queue[queueTail++] = nextIndex;
            }
        }
        activeVolume = new LightVolume(minX, minY, minZ, size, levels);
    }

    public static void clear() {
        activeVolume = EMPTY_VOLUME;
    }

    public static int apply(BlockPos position, int lightmap) {
        int blockLight = (lightmap >> 4) & 0xF;
        int dynamicLevel = activeVolume.get(position);
        if (dynamicLevel <= blockLight) {
            return lightmap;
        }
        return lightmap & 0xFFFFFF0F | dynamicLevel << 4;
    }

    public static int max(BlockPos position, int lightLevel) {
        return Math.max(lightLevel, activeVolume.get(position));
    }

    static int index(int x, int y, int z, int size) {
        return (z * size + y) * size + x;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record LightVolume(int minX, int minY, int minZ, int size, byte[] levels) {
        int get(BlockPos position) {
            int x = position.getX() - this.minX;
            int y = position.getY() - this.minY;
            int z = position.getZ() - this.minZ;
            if (x < 0 || y < 0 || z < 0 || x >= this.size || y >= this.size || z >= this.size) {
                return 0;
            }
            return Byte.toUnsignedInt(this.levels[DynamicLightGrid.index(x, y, z, this.size)]);
        }
    }
}
