/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.Vec3d
 */
package moscow.rockstar.entity.tracking;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public final class EntityPositionCache {
    private static final Map<Integer, Vec3d> positionByEntityId = new ConcurrentHashMap<Integer, Vec3d>();
    private static final Set<Integer> trackedEntityIds = ConcurrentHashMap.newKeySet();

    public static Vec3d getTrackedPosition(Entity class_12972) {
        Vec3d VanillaChestLootTableGenerator = positionByEntityId.get(class_12972.getId());
        return VanillaChestLootTableGenerator != null ? VanillaChestLootTableGenerator : class_12972.getPos();
    }

    public static void storePosition(Entity class_12972, Vec3d VanillaChestLootTableGenerator) {
        positionByEntityId.put(class_12972.getId(), VanillaChestLootTableGenerator);
    }

    public static boolean hasTrackedEntity(Entity class_12972) {
        return trackedEntityIds.contains(class_12972.getId());
    }

    public static boolean trackEntity(Entity class_12972) {
        return trackedEntityIds.add(class_12972.getId());
    }

    public static void removeEntity(int n) {
        positionByEntityId.remove(n);
    }

    public static void clear() {
        positionByEntityId.clear();
        trackedEntityIds.clear();
    }

    @Generated
    private EntityPositionCache() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

