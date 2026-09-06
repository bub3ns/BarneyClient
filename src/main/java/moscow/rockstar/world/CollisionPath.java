/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.world;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.mining.ExcavationController;

public final class CollisionPath {
    private final List<BlockPositionOffset> nodes;
    private final List<ExcavationController> movements;

    public CollisionPath(List<BlockPositionOffset> list, List<ExcavationController> list2) {
        this.nodes = list;
        this.movements = list2;
    }

    public BlockPositionOffset getFirstNode() {
        return this.nodes.get(0);
    }

    public BlockPositionOffset getFirstMovement() {
        return this.nodes.get(this.nodes.size() - 1);
    }

    public int getNodeCount() {
        return this.movements.size();
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "nodes", "movements");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "nodes", "movements");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "nodes", "movements");
    }

    public List<BlockPositionOffset> getNodes() {
        return this.nodes;
    }

    public List<ExcavationController> getMovements() {
        return this.movements;
    }
}

