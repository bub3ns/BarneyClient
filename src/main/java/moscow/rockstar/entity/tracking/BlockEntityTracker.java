package moscow.rockstar.entity.tracking;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

/** Client-side index of block entities received in loaded chunks. */
public final class BlockEntityTracker {
    private static final Set<BlockEntity> trackedEntities = ConcurrentHashMap.newKeySet();

    private BlockEntityTracker() {
    }

    public static void add(BlockEntity blockEntity) {
        if (blockEntity != null && !blockEntity.isRemoved()) {
            trackedEntities.add(blockEntity);
        }
    }

    public static void remove(BlockPos position) {
        if (position == null) {
            return;
        }
        trackedEntities.removeIf(blockEntity -> position.equals(blockEntity.getPos()));
    }

    public static Set<BlockEntity> getTrackedEntities() {
        return Collections.unmodifiableSet(trackedEntities);
    }

    public static void clear() {
        trackedEntities.clear();
    }
}
