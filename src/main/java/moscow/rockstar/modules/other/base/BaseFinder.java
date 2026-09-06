/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  moscow.rockstar.modules.other.admin.BlockPos$Mutable
 *  net.minecraft.BlockView
 *  net.minecraft.ChunkPos
 *  net.minecraft.LightType
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.Text
 *  net.minecraft.BlockState
 *  net.minecraft.ChunkStatus
 *  net.minecraft.WorldChunk
 *  net.minecraft.ClientChunkManager
 */
package moscow.rockstar.modules.other.base;
import moscow.rockstar.ui.localization.Localization;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.LightType;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.client.world.ClientChunkManager;
import pyrock.events.game.WorldChangeEvent;

@ModuleInfo(name="Base Finder", category=ModuleCategory.OTHER)
public class BaseFinder
extends Module {
    private static final int BASE_SCAN_RADIUS = 7;
    private static final int CHUNK_RADIUS_MARGIN = 2;
    private static final int MAX_CHUNK_SCAN_RADIUS = 10;
    private static final int MAX_CHUNKS_PER_TICK = 2;
    private static final int CHUNK_SCAN_DIAMETER = 48;
    private static final int DUPLICATE_BASE_DISTANCE_SQUARED = 2304;
    private static final int MIN_CARDINAL_OPENINGS = 4;
    private static final int MIN_DIAGONAL_OPENINGS = 6;
    private static final Set<Block> BASE_MARKER_BLOCKS = Set.of(Blocks.AMETHYST_CLUSTER, Blocks.LARGE_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.SMALL_AMETHYST_BUD, Blocks.GLOW_LICHEN, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH);
    private final ArrayDeque<Long> pendingChunkPositions = new ArrayDeque();
    private final Set<Long> queuedChunkPositions = new HashSet<Long>();
    private final Set<Long> scannedChunkPositions = new HashSet<Long>();
    private final List<BaseLocation> detectedBases = new ArrayList<BaseLocation>();
    private int scanTick;
    private int nextBaseNumber = 1;
    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> {
        this.resetScanState();
        this.consumeDetectedBaseCount();
    };

    @Override
    public void onEnable() {
        this.resetScanState();
        super.onEnable();
    }

    @Override
    public void onTick() {
        if (BaseFinder.minecraftClient.world == null || BaseFinder.minecraftClient.player == null) {
            return;
        }
        int n = BaseFinder.minecraftClient.player.getChunkPos().x;
        int n2 = BaseFinder.minecraftClient.player.getChunkPos().z;
        int n3 = this.getChunkScanRadius();
        if (this.scanTick++ % 5 == 0 || this.pendingChunkPositions.isEmpty()) {
            this.queueChunkColumns(n, n2, n3);
            this.pruneScannedChunks(n, n2, n3 + 2);
        }
        this.processPendingChunks(n, n2, n3 + 2);
        super.onTick();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public List<BaseLocation> getDetectedBases() {
        List<BaseLocation> list = this.detectedBases;
        synchronized (list) {
            return List.copyOf(this.detectedBases);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int consumeDetectedBaseCount() {
        List<BaseLocation> list = this.detectedBases;
        synchronized (list) {
            int n = this.detectedBases.size();
            this.detectedBases.clear();
            this.nextBaseNumber = 1;
            return n;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public BaseLocation removeBaseByName(String string) {
        if (string == null || string.isBlank()) {
            return null;
        }
        String string2 = this.normalizeBaseName(string);
        List<BaseLocation> list = this.detectedBases;
        synchronized (list) {
            for (int i = 0; i < this.detectedBases.size(); ++i) {
                BaseLocation baseLocation = this.detectedBases.get(i);
                if (!this.normalizeBaseName(baseLocation.getBaseName()).equals(string2)) continue;
                return this.detectedBases.remove(i);
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public BaseLocation removeBaseAtPosition(BlockPos adminsky) {
        if (adminsky == null) {
            return null;
        }
        List<BaseLocation> list = this.detectedBases;
        synchronized (list) {
            for (int i = 0; i < this.detectedBases.size(); ++i) {
                BaseLocation baseLocation = this.detectedBases.get(i);
                if (!baseLocation.getBasePosition().equals(adminsky)) continue;
                return this.detectedBases.remove(i);
            }
        }
        return null;
    }

    private void queueChunkColumns(int n, int n2, int n3) {
        if (BaseFinder.minecraftClient.world == null) {
            return;
        }
        ClientChunkManager PackageInfo6312 = BaseFinder.minecraftClient.world.getChunkManager();
        for (int i = 0; i <= n3; ++i) {
            for (int j = -i; j <= i; ++j) {
                for (int k = -i; k <= i; ++k) {
                    if (Math.max(Math.abs(j), Math.abs(k)) != i) continue;
                    this.queueChunkPosition(PackageInfo6312, n + j, n2 + k);
                }
            }
        }
    }

    private void queueChunkPosition(ClientChunkManager PackageInfo6312, int n, int n2) {
        long l = ChunkPos.toLong((int)n, (int)n2);
        if (this.scannedChunkPositions.contains(l) || this.queuedChunkPositions.contains(l)) {
            return;
        }
        if (!BaseFinder.minecraftClient.world.isChunkLoaded(n, n2)) {
            return;
        }
        if (PackageInfo6312.getChunk(n, n2, ChunkStatus.FULL, false) == null) {
            return;
        }
        this.pendingChunkPositions.addLast(l);
        this.queuedChunkPositions.add(l);
    }

    private void processPendingChunks(int n, int n2, int n3) {
        if (BaseFinder.minecraftClient.world == null) {
            return;
        }
        ClientChunkManager PackageInfo6312 = BaseFinder.minecraftClient.world.getChunkManager();
        int n4 = 0;
        while (n4 < 2 && !this.pendingChunkPositions.isEmpty()) {
            int n5;
            int n6;
            WorldChunk class_28182;
            long l = this.pendingChunkPositions.removeFirst();
            this.queuedChunkPositions.remove(l);
            if (!this.isChunkWithinScanWindow(l, n, n2, n3) || (class_28182 = PackageInfo6312.getChunk(n6 = ChunkPos.getPackedX((long)l), n5 = ChunkPos.getPackedZ((long)l), ChunkStatus.FULL, false)) == null) continue;
            this.scanChunkForBases(class_28182);
            this.scannedChunkPositions.add(l);
            ++n4;
        }
    }

    private void scanChunkForBases(WorldChunk class_28182) {
        if (BaseFinder.minecraftClient.world == null) {
            return;
        }
        int n = class_28182.getPos().getStartX();
        int n2 = class_28182.getPos().getStartZ();
        int n3 = class_28182.getBottomY();
        int n4 = n3 + class_28182.getHeight();
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        for (int i = n; i < n + 16; ++i) {
            for (int j = n2; j < n2 + 16; ++j) {
                for (int k = n3; k < n4; ++k) {
                    class_23392.set(i, k, j);
                    if (!this.isPotentialBaseBlock(class_28182, (BlockPos)class_23392)) continue;
                    this.recordBaseLocation(class_23392.toImmutable());
                }
            }
        }
    }

    private boolean isPotentialBaseBlock(WorldChunk class_28182, BlockPos adminsky) {
        if (this.getSkyLightLevel(adminsky) != 7) {
            return false;
        }
        BlockState class_26802 = class_28182.getBlockState(adminsky);
        if (class_26802.isOf(Blocks.ENDER_CHEST)) {
            return true;
        }
        return this.isValidBaseBlock(class_26802, adminsky) && !this.hasNearbyBaseMarkers(adminsky) && this.matchesBaseStructure(adminsky);
    }

    private boolean isValidBaseBlock(BlockState class_26802, BlockPos adminsky) {
        if (class_26802.isAir() || class_26802.getLuminance() > 0) {
            return false;
        }
        return class_26802.isOpaqueFullCube() || class_26802.isFullCube((BlockView)BaseFinder.minecraftClient.world, adminsky);
    }

    private boolean hasNearbyBaseMarkers(BlockPos adminsky) {
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        for (int i = -7; i <= 7; ++i) {
            for (int j = -7; j <= 7; ++j) {
                for (int k = -7; k <= 7; ++k) {
                    int n = Math.abs(i) + Math.abs(j) + Math.abs(k);
                    if (n == 0 || n > 7) continue;
                    class_23392.set(adminsky.getX() + i, adminsky.getY() + j, adminsky.getZ() + k);
                    if (!BaseFinder.minecraftClient.world.isChunkLoaded(class_23392.getX() >> 4, class_23392.getZ() >> 4)) {
                        return true;
                    }
                    BlockState class_26802 = BaseFinder.minecraftClient.world.getBlockState((BlockPos)class_23392);
                    if (class_26802.isOf(Blocks.ENDER_CHEST) || class_26802.getLuminance() <= 0 && !BASE_MARKER_BLOCKS.contains(class_26802.getBlock())) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesBaseStructure(BlockPos adminsky) {
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        int n = 0;
        int n2 = 0;
        for (int i = -7; i <= 7; ++i) {
            for (int j = -7; j <= 7; ++j) {
                for (int k = -7; k <= 7; ++k) {
                    int n3;
                    int n4 = Math.abs(i) + Math.abs(j) + Math.abs(k);
                    if (n4 == 0 || n4 > 7) continue;
                    class_23392.set(adminsky.getX() + i, adminsky.getY() + j, adminsky.getZ() + k);
                    if (!BaseFinder.minecraftClient.world.isChunkLoaded(class_23392.getX() >> 4, class_23392.getZ() >> 4)) {
                        return false;
                    }
                    int n5 = this.getSkyLightLevel((BlockPos)class_23392);
                    if (n5 > (n3 = 7 - n4)) {
                        return false;
                    }
                    if (n4 == 1 && n5 == n3) {
                        ++n;
                        continue;
                    }
                    if (n4 != 2 || n5 != n3) continue;
                    ++n2;
                }
            }
        }
        return n >= 4 && n2 >= 6;
    }

    private int getSkyLightLevel(BlockPos adminsky) {
        return BaseFinder.minecraftClient.world == null ? 0 : BaseFinder.minecraftClient.world.getLightLevel(LightType.BLOCK, adminsky);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void recordBaseLocation(BlockPos adminsky) {
        List<BaseLocation> list = this.detectedBases;
        synchronized (list) {
            if (this.isNearExistingBase(adminsky)) {
                return;
            }
            BaseLocation baseLocation = new BaseLocation("Base-" + this.nextBaseNumber++, adminsky);
            this.detectedBases.add(baseLocation);
            Notification.info(Text.of((String)BaseFinder.formatBaseNotification(baseLocation)));
        }
    }

    private boolean isNearExistingBase(BlockPos adminsky) {
        for (BaseLocation baseLocation : this.detectedBases) {
            long l;
            long l2;
            long l3 = (long)adminsky.getX() - (long)baseLocation.getBasePosition().getX();
            if (l3 * l3 + (l2 = (long)adminsky.getY() - (long)baseLocation.getBasePosition().getY()) * l2 + (l = (long)adminsky.getZ() - (long)baseLocation.getBasePosition().getZ()) * l > 2304L) continue;
            return true;
        }
        return false;
    }

    private void pruneScannedChunks(int n, int n2, int n3) {
        this.scannedChunkPositions.removeIf(l -> !this.isChunkWithinScanWindow((long)l, n, n2, n3));
    }

    private boolean isChunkWithinScanWindow(long l, int n, int n2, int n3) {
        return Math.abs(ChunkPos.getPackedX((long)l) - n) <= n3 && Math.abs(ChunkPos.getPackedZ((long)l) - n2) <= n3;
    }

    private int getChunkScanRadius() {
        int n = BaseFinder.minecraftClient.options != null ? (Integer)BaseFinder.minecraftClient.options.getViewDistance().getValue() : 8;
        return Math.max(1, Math.min(n, 10));
    }

    private void resetScanState() {
        this.scanTick = 0;
        this.pendingChunkPositions.clear();
        this.queuedChunkPositions.clear();
        this.scannedChunkPositions.clear();
    }

    private String normalizeBaseName(String string) {
        String string2 = string.trim().toLowerCase();
        if (string2.matches("\\d+")) {
            return "base-" + string2;
        }
        return string2.replace("\u0431\u0430\u0437\u0430", "base").replace("baza", "base").replace(" ", "");
    }

    public static String formatBaseNotification(BaseLocation baseLocation) {
        return BaseFinder.formatBaseCoordinates(baseLocation.getBaseName(), baseLocation.getBasePosition());
    }

    public static String formatBaseCoordinates(String string, BlockPos adminsky) {
        return string + " - " + Localization.translate("coords") + ": " + adminsky.getX() + " " + adminsky.getY() + " " + adminsky.getZ();
    }

    public static final class BaseLocation {
        private final String baseName;
        private final BlockPos basePosition;

        public BaseLocation(String string, BlockPos adminsky) {
            this.baseName = string;
            this.basePosition = adminsky;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "baseName", "basePosition");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "baseName", "basePosition");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "baseName", "basePosition");
        }

        public String getBaseName() {
            return this.baseName;
        }

        public BlockPos getBasePosition() {
            return this.basePosition;
        }
    }
}

