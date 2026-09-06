/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.world.navigation;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import moscow.rockstar.entity.CollisionProbe;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.CollisionPath;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.BlockNavigationFactory;
import org.jetbrains.annotations.Nullable;

public final class Pathfinder {
    private static final int MAX_NODES = 60000;
    private static final long SEARCH_TIMEOUT_MILLIS = 3000L;
    private static final double HEURISTIC_WEIGHT = 1.15;
    private static final ExecutorService PATHFINDING_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Newton-Pathfinder");
        thread.setDaemon(true);
        return thread;
    });

    private Pathfinder() {
    }

    public static CompletableFuture<Optional<CollisionPath>> findPathAsync(BlockPositionOffset blockPositionOffset, CollisionProbe collisionProbe) {
        return Pathfinder.findPathAsync(blockPositionOffset, collisionProbe, 60000, null);
    }

    public static CompletableFuture<Optional<CollisionPath>> findPathAsync(BlockPositionOffset blockPositionOffset, CollisionProbe collisionProbe, int n) {
        return Pathfinder.findPathAsync(blockPositionOffset, collisionProbe, n, null);
    }

    public static CompletableFuture<Optional<CollisionPath>> findPathAsync(BlockPositionOffset blockPositionOffset, CollisionProbe collisionProbe, AtomicBoolean atomicBoolean) {
        return Pathfinder.findPathAsync(blockPositionOffset, collisionProbe, 60000, atomicBoolean);
    }

    public static CompletableFuture<Optional<CollisionPath>> findPathAsync(BlockPositionOffset blockPositionOffset, CollisionProbe collisionProbe, int n, @Nullable AtomicBoolean atomicBoolean) {
        return CompletableFuture.supplyAsync(() -> Pathfinder.findPathCancellable(blockPositionOffset, collisionProbe, n, atomicBoolean), PATHFINDING_EXECUTOR);
    }

    public static Optional<CollisionPath> findPath(BlockPositionOffset blockPositionOffset, CollisionProbe collisionProbe, int n) {
        return Pathfinder.findPathCancellable(blockPositionOffset, collisionProbe, n, null);
    }

    public static Optional<CollisionPath> findPathCancellable(BlockPositionOffset blockPositionOffset, CollisionProbe collisionProbe, int n, @Nullable AtomicBoolean atomicBoolean) {
        BlockNavigationFactory blockNavigationFactory;
        try {
            blockNavigationFactory = new BlockNavigationFactory();
        }
        catch (IllegalStateException illegalStateException) {
            return Optional.empty();
        }
        return Pathfinder.searchPath(blockPositionOffset, collisionProbe, n, 3000L, blockNavigationFactory, atomicBoolean);
    }

    public static Optional<CollisionPath> findPathWithTimeout(BlockPositionOffset blockPositionOffset, CollisionProbe collisionProbe, int n, long l, BlockNavigationFactory blockNavigationFactory) {
        return Pathfinder.searchPath(blockPositionOffset, collisionProbe, n, l, blockNavigationFactory, null);
    }

    public static Optional<CollisionPath> searchPath(BlockPositionOffset blockPositionOffset, CollisionProbe collisionProbe, int n, long l, BlockNavigationFactory blockNavigationFactory, @Nullable AtomicBoolean atomicBoolean) {
        long l2 = System.nanoTime() + l * 1000000L;
        Long2DoubleOpenHashMap long2DoubleOpenHashMap = new Long2DoubleOpenHashMap(4096);
        long2DoubleOpenHashMap.defaultReturnValue(Double.POSITIVE_INFINITY);
        Long2ObjectOpenHashMap long2ObjectOpenHashMap = new Long2ObjectOpenHashMap(4096);
        PriorityQueue<SearchNode> priorityQueue = new PriorityQueue<SearchNode>();
        long l3 = Pathfinder.packBlockPosition(blockPositionOffset);
        long2DoubleOpenHashMap.put(l3, 0.0);
        double d = collisionProbe.distanceSquaredTo(blockPositionOffset.getX(), blockPositionOffset.getY(), blockPositionOffset.getZ());
        priorityQueue.add(new SearchNode(blockPositionOffset, 0.0, d * 1.15));
        BlockPositionOffset blockPositionOffset2 = blockPositionOffset;
        double d2 = d;
        double d3 = 0.0;
        int n2 = 0;
        while (!priorityQueue.isEmpty()) {
            SearchNode searchNode = (SearchNode)priorityQueue.poll();
            BlockPositionOffset blockPositionOffset3 = searchNode.position;
            long l4 = Pathfinder.packBlockPosition(blockPositionOffset3);
            double d4 = long2DoubleOpenHashMap.get(l4);
            if (searchNode.cost > d4 + 1.0E-9) continue;
            if (collisionProbe.matchesCoordinates(blockPositionOffset3.getX(), blockPositionOffset3.getY(), blockPositionOffset3.getZ())) {
                return Optional.of(Pathfinder.reconstructPath((Long2ObjectOpenHashMap<ExcavationController>)long2ObjectOpenHashMap, blockPositionOffset, blockPositionOffset3));
            }
            if (++n2 > n || (n2 & 0xFF) == 0 && (System.nanoTime() > l2 || atomicBoolean != null && atomicBoolean.get())) break;
            for (ExcavationController excavationController : BlockNavigationFactory.createNavigationCandidates(blockPositionOffset3, blockNavigationFactory)) {
                BlockPositionOffset blockPositionOffset4 = BlockPositionOffset.fromBlockPosition(excavationController.getEndPosition());
                long l5 = Pathfinder.packBlockPosition(blockPositionOffset4);
                double d5 = d4 + excavationController.getMovementCost();
                if (!(d5 < long2DoubleOpenHashMap.get(l5))) continue;
                long2DoubleOpenHashMap.put(l5, d5);
                long2ObjectOpenHashMap.put(l5, (Object)excavationController);
                double d6 = collisionProbe.distanceSquaredTo(blockPositionOffset4.getX(), blockPositionOffset4.getY(), blockPositionOffset4.getZ());
                if (d6 < d2 - 1.0E-9 || d6 < d2 + 1.0E-9 && d5 < d3) {
                    d2 = d6;
                    blockPositionOffset2 = blockPositionOffset4;
                    d3 = d5;
                }
                priorityQueue.add(new SearchNode(blockPositionOffset4, d5, d5 + d6 * 1.15));
            }
        }
        if (!blockPositionOffset2.equals(blockPositionOffset)) {
            return Optional.of(Pathfinder.reconstructPath((Long2ObjectOpenHashMap<ExcavationController>)long2ObjectOpenHashMap, blockPositionOffset, blockPositionOffset2));
        }
        return Optional.empty();
    }

    private static long packBlockPosition(BlockPositionOffset blockPositionOffset) {
        return BlockPos.asLong((int)blockPositionOffset.getX(), (int)blockPositionOffset.getY(), (int)blockPositionOffset.getZ());
    }

    private static CollisionPath reconstructPath(Long2ObjectOpenHashMap<ExcavationController> long2ObjectOpenHashMap, BlockPositionOffset blockPositionOffset, BlockPositionOffset blockPositionOffset2) {
        ArrayList<BlockPositionOffset> arrayList = new ArrayList<BlockPositionOffset>();
        ArrayList<ExcavationController> arrayList2 = new ArrayList<ExcavationController>();
        BlockPositionOffset blockPositionOffset3 = blockPositionOffset2;
        int n = 0x100000;
        while (!blockPositionOffset3.equals(blockPositionOffset)) {
            ExcavationController excavationController = (ExcavationController)long2ObjectOpenHashMap.get(Pathfinder.packBlockPosition(blockPositionOffset3));
            if (excavationController == null || --n <= 0) {
                return new CollisionPath(List.of(blockPositionOffset), List.of());
            }
            arrayList.add(blockPositionOffset3);
            arrayList2.add(excavationController);
            blockPositionOffset3 = BlockPositionOffset.fromBlockPosition(excavationController.getStartPosition());
        }
        arrayList.add(blockPositionOffset);
        Collections.reverse(arrayList);
        Collections.reverse(arrayList2);
        return new CollisionPath(arrayList, arrayList2);
    }

    static final class SearchNode
    implements Comparable<SearchNode> {
        final BlockPositionOffset position;
        final double cost;
        private final double priority;

        SearchNode(BlockPositionOffset blockPositionOffset, double d, double d2) {
            this.position = blockPositionOffset;
            this.cost = d;
            this.priority = d2;
        }

        @Override
        public int compareTo(SearchNode searchNode) {
            int n = Double.compare(this.priority, searchNode.priority);
            if (n != 0) {
                return n;
            }
            return Double.compare(searchNode.cost, this.cost);
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "position", "cost", "priority");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "position", "cost", "priority");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "position", "cost", "priority");
        }

        public BlockPositionOffset getPosition() {
            return this.position;
        }

        public double getCost() {
            return this.cost;
        }

        public double getPriority() {
            return this.priority;
        }

    }
}

