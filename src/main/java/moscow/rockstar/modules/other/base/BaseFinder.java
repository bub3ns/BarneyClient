package moscow.rockstar.modules.other.base;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.StringSetting;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LightStorage;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

/**
 * One module containing the five base-finding behaviours from the reference
 * client: suspicious chunks, suspicious ESP, hole ESP, seed chunk finding and
 * light finding. The data collection is shared so every chunk is scanned only
 * once and the local client's renderer is used for all visual output.
 */
@ModuleInfo(name = "Base Finder", category = ModuleCategory.OTHER)
public class BaseFinder extends Module {
    private static final int MIN_NEIGHBOUR_CHUNKS = 3;
    private static final int KELP_THRESHOLD = 10;
    private static final double[] BEDROCK_CHANCES = {1.0, 0.875, 0.75, 0.625, 0.5, 0.375, 0.25, 0.125};

    private final NumberSetting simulationDistance;
    private final NumberSetting sensitivity;
    private final NumberSetting chunkAlpha;
    private final BooleanSetting kelp;
    private final BooleanSetting caveVines;
    private final BooleanSetting vines;
    private final BooleanSetting amethyst;
    private final BooleanSetting bamboo;
    private final BooleanSetting beeNest;
    private final BooleanSetting rotatedDeepslate;

    private final BooleanSetting wanderingTraders;
    private final BooleanSetting villagers;
    private final BooleanSetting llamas;
    private final BooleanSetting pillagers;
    private final BooleanSetting deepslate;
    private final BooleanSetting espRotatedDeepslate;
    private final BooleanSetting espVines;
    private final BooleanSetting espKelp;
    private final BooleanSetting amethystClusters;
    private final NumberSetting espAlpha;
    private final BooleanSetting tracers;
    private final NumberSetting deepslateMinY;
    private final NumberSetting deepslateMaxY;

    private final NumberSetting minHoleDepth;
    private final BooleanSetting oneByOneHoles;
    private final BooleanSetting threeByOneHoles;
    private final NumberSetting holeAlpha;

    private final BooleanSetting seedFinder;
    private final StringSetting serverSeed;
    private final BooleanSetting lightFinder;

    private final ConcurrentLinkedQueue<ChunkPos> pendingChunkPositions = new ConcurrentLinkedQueue<>();
    private final Set<ChunkPos> queuedSusChunkPositions = ConcurrentHashMap.newKeySet();
    private final Set<ChunkPos> queuedSuspiciousPositions = ConcurrentHashMap.newKeySet();
    private final Set<ChunkPos> queuedHolePositions = ConcurrentHashMap.newKeySet();
    private final Set<ChunkPos> queuedSeedPositions = ConcurrentHashMap.newKeySet();
    private final Set<ChunkPos> scannedChunks = ConcurrentHashMap.newKeySet();
    private final Set<ChunkPos> scannedSuspiciousChunks = ConcurrentHashMap.newKeySet();
    private final Set<ChunkPos> scannedHoleChunks = ConcurrentHashMap.newKeySet();
    private final Set<ChunkPos> scannedSeedChunks = ConcurrentHashMap.newKeySet();
    private final AtomicInteger worldGeneration = new AtomicInteger();

    /* SusChunkFinder state. */
    private final ConcurrentMap<ChunkPos, Integer> suspicionScores = new ConcurrentHashMap<>();
    private final ConcurrentMap<ChunkPos, Integer> suppressionCounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<ChunkPos, Integer> scoreByChunk = new ConcurrentHashMap<>();
    private final ConcurrentMap<ChunkPos, Boolean> grownByChunk = new ConcurrentHashMap<>();
    private final ConcurrentMap<ChunkPos, Set<BlockPos>> rotatedByChunk = new ConcurrentHashMap<>();
    private final Set<BlockPos> rotatedDeepslatePositions = ConcurrentHashMap.newKeySet();

    /* SuspiciousESP state. */
    private final ConcurrentMap<ChunkPos, Map<BlockPos, SuspiciousType>> suspiciousBlocksByChunk = new ConcurrentHashMap<>();
    private final Set<ChunkPos> kelpChunks = ConcurrentHashMap.newKeySet();

    /* HoleESP state. */
    private final ConcurrentMap<ChunkPos, Set<Hole>> holes1x1ByChunk = new ConcurrentHashMap<>();
    private final ConcurrentMap<ChunkPos, Set<Hole>> holes3x1ByChunk = new ConcurrentHashMap<>();

    /* SeedChunkFinder and LightFinder state. */
    private final Set<ChunkPos> seedChunks = ConcurrentHashMap.newKeySet();

    private final List<BaseLocation> detectedBases = new ArrayList<>();
    private volatile ExecutorService susChunkScanner;
    private volatile ExecutorService suspiciousScanner;
    private volatile ExecutorService holeScanner;
    private volatile ExecutorService seedScanner;
    private volatile RandomSplitter randomDeriver;
    private volatile boolean seedValid;
    private String activeSeed;
    private String activeScanConfiguration;
    private int scanTick;
    private int nextBaseNumber = 1;

    private final EventListener<ReceivePacketEvent> packetListener = event -> {
        Packet<?> packet = event.getPacket();
        if (packet instanceof ChunkDataS2CPacket chunkDataPacket) {
            this.enqueueChunk(new ChunkPos(chunkDataPacket.getChunkX(), chunkDataPacket.getChunkZ()));
            return;
        }
        if (packet instanceof ChunkDeltaUpdateS2CPacket deltaUpdatePacket) {
            deltaUpdatePacket.visitUpdates((position, state) -> this.enqueueChunk(new ChunkPos(position)));
            return;
        }
        if (packet instanceof BlockUpdateS2CPacket blockUpdatePacket) {
            this.enqueueChunk(new ChunkPos(blockUpdatePacket.getPos()));
        }
    };

    private final EventListener<Render3DEvent> renderListener = this::render;
    private final EventListener<WorldChangeEvent> worldChangeListener = event -> this.resetState();

    public BaseFinder() {
        this.simulationDistance = new NumberSetting(this, "modules.settings.base_finder.simulation_distance")
            .setMinValue(2.0f).setMaxValue(16.0f).setStep(1.0f).setValue(4.0f);
        this.sensitivity = new NumberSetting(this, "modules.settings.base_finder.sensitivity")
            .setMinValue(1.0f).setMaxValue(20.0f).setStep(1.0f).setValue(3.0f);
        this.chunkAlpha = new NumberSetting(this, "modules.settings.base_finder.chunk_alpha")
            .setMinValue(10.0f).setMaxValue(255.0f).setStep(1.0f).setValue(80.0f);
        this.kelp = new BooleanSetting(this, "modules.settings.base_finder.kelp").enable();
        this.caveVines = new BooleanSetting(this, "modules.settings.base_finder.cave_vines").enable();
        this.vines = new BooleanSetting(this, "modules.settings.base_finder.vines").enable();
        this.amethyst = new BooleanSetting(this, "modules.settings.base_finder.amethyst").enable();
        this.bamboo = new BooleanSetting(this, "modules.settings.base_finder.bamboo").enable();
        this.beeNest = new BooleanSetting(this, "modules.settings.base_finder.bee_nest").enable();
        this.rotatedDeepslate = new BooleanSetting(this, "modules.settings.base_finder.rotated_deepslate").enable();

        this.wanderingTraders = new BooleanSetting(this, "modules.settings.base_finder.wandering_traders").enable();
        this.villagers = new BooleanSetting(this, "modules.settings.base_finder.villagers").enable();
        this.llamas = new BooleanSetting(this, "modules.settings.base_finder.llamas").enable();
        this.pillagers = new BooleanSetting(this, "modules.settings.base_finder.pillagers").enable();
        this.deepslate = new BooleanSetting(this, "modules.settings.base_finder.deepslate").enable();
        this.espRotatedDeepslate = new BooleanSetting(this, "modules.settings.base_finder.esp_rotated_deepslate").enable();
        this.espVines = new BooleanSetting(this, "modules.settings.base_finder.esp_vines").enable();
        this.espKelp = new BooleanSetting(this, "modules.settings.base_finder.esp_kelp").enable();
        this.amethystClusters = new BooleanSetting(this, "modules.settings.base_finder.amethyst_clusters").enable();
        this.espAlpha = new NumberSetting(this, "modules.settings.base_finder.esp_alpha")
            .setMinValue(1.0f).setMaxValue(255.0f).setStep(1.0f).setValue(100.0f);
        this.tracers = new BooleanSetting(this, "modules.settings.base_finder.tracers");
        this.deepslateMinY = new NumberSetting(this, "modules.settings.base_finder.deepslate_min_y")
            .setMinValue(-64.0f).setMaxValue(128.0f).setStep(1.0f).setValue(16.0f);
        this.deepslateMaxY = new NumberSetting(this, "modules.settings.base_finder.deepslate_max_y")
            .setMinValue(-64.0f).setMaxValue(320.0f).setStep(1.0f).setValue(128.0f);

        this.minHoleDepth = new NumberSetting(this, "modules.settings.base_finder.min_hole_depth")
            .setMinValue(1.0f).setMaxValue(20.0f).setStep(1.0f).setValue(4.0f);
        this.oneByOneHoles = new BooleanSetting(this, "modules.settings.base_finder.one_by_one_holes").enable();
        this.threeByOneHoles = new BooleanSetting(this, "modules.settings.base_finder.three_by_one_holes").enable();
        this.holeAlpha = new NumberSetting(this, "modules.settings.base_finder.hole_alpha")
            .setMinValue(1.0f).setMaxValue(255.0f).setStep(1.0f).setValue(120.0f);

        this.seedFinder = new BooleanSetting(this, "modules.settings.base_finder.seed_finder").enable();
        this.serverSeed = new StringSetting(this, "modules.settings.base_finder.server_seed")
            .setMaxLength(32).setNumericOnly(true).setValue("");
        this.lightFinder = new BooleanSetting(this, "modules.settings.base_finder.light_finder").enable();
    }

    @Override
    public void onEnable() {
        this.resetState();
        this.refreshSeedConfiguration();
        this.activeScanConfiguration = this.scanConfiguration();
        this.susChunkScanner = Executors.newSingleThreadExecutor(this.threadFactory("rockstar-sus-chunk"));
        this.suspiciousScanner = Executors.newFixedThreadPool(2, this.threadFactory("rockstar-suspicious-esp"));
        this.holeScanner = Executors.newSingleThreadExecutor(this.threadFactory("rockstar-hole-esp"));
        this.seedScanner = Executors.newSingleThreadExecutor(this.threadFactory("rockstar-seed-finder"));
        super.onEnable();
        this.queueNearbyChunks();
    }

    @Override
    public void onDisable() {
        this.shutdownScanner(this.susChunkScanner);
        this.shutdownScanner(this.suspiciousScanner);
        this.shutdownScanner(this.holeScanner);
        this.shutdownScanner(this.seedScanner);
        this.susChunkScanner = null;
        this.suspiciousScanner = null;
        this.holeScanner = null;
        this.seedScanner = null;
        this.resetState();
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (BaseFinder.minecraftClient.world == null || BaseFinder.minecraftClient.player == null) {
            super.onTick();
            return;
        }

        this.refreshSeedConfiguration();
        this.refreshScanConfiguration();
        ChunkPos pending;
        while ((pending = this.pendingChunkPositions.poll()) != null) {
            this.submitChunkScans(pending, true);
        }

        if (this.scanTick++ % 10 == 0) {
            this.queueNearbyChunks();
            this.pruneOutOfRangeData();
        }
        this.syncDetectedBases();
        super.onTick();
    }

    /** Compatibility API used by the existing /base command. */
    public List<BaseLocation> getDetectedBases() {
        synchronized (this.detectedBases) {
            return List.copyOf(this.detectedBases);
        }
    }

    /** Compatibility API used by the existing /base command. */
    public int consumeDetectedBaseCount() {
        synchronized (this.detectedBases) {
            int count = this.detectedBases.size();
            this.detectedBases.clear();
            this.nextBaseNumber = 1;
            return count;
        }
    }

    /** Compatibility API used by the existing /base command. */
    public BaseLocation removeBaseByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String normalized = this.normalizeBaseName(name);
        synchronized (this.detectedBases) {
            for (int index = 0; index < this.detectedBases.size(); index++) {
                BaseLocation location = this.detectedBases.get(index);
                if (this.normalizeBaseName(location.getBaseName()).equals(normalized)) {
                    return this.detectedBases.remove(index);
                }
            }
        }
        return null;
    }

    /** Compatibility API used by the existing /base command. */
    public BaseLocation removeBaseAtPosition(BlockPos position) {
        if (position == null) {
            return null;
        }
        synchronized (this.detectedBases) {
            for (int index = 0; index < this.detectedBases.size(); index++) {
                BaseLocation location = this.detectedBases.get(index);
                if (location.getBasePosition().equals(position)) {
                    return this.detectedBases.remove(index);
                }
            }
        }
        return null;
    }

    private void enqueueChunk(ChunkPos chunkPos) {
        if (chunkPos != null) {
            this.pendingChunkPositions.add(chunkPos);
        }
    }

    private void queueNearbyChunks() {
        ClientWorld world = BaseFinder.minecraftClient.world;
        if (world == null || BaseFinder.minecraftClient.player == null) {
            return;
        }
        ChunkPos playerChunk = BaseFinder.minecraftClient.player.getChunkPos();
        int radius = this.getChunkScanRadius();
        for (int x = playerChunk.x - radius; x <= playerChunk.x + radius; x++) {
            for (int z = playerChunk.z - radius; z <= playerChunk.z + radius; z++) {
                if (!world.isChunkLoaded(x, z)) {
                    continue;
                }
                this.submitChunkScans(new ChunkPos(x, z), false);
            }
        }
    }

    private boolean submitChunkScans(ChunkPos chunkPos, boolean force) {
        ClientWorld world = BaseFinder.minecraftClient.world;
        if (world == null || !world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
            return false;
        }
        WorldChunk chunk = this.getLoadedChunk(world, chunkPos);
        if (chunk == null) {
            return false;
        }

        boolean submitted = false;
        submitted |= this.submitSusChunkScan(world, chunk, chunkPos, force);
        submitted |= this.submitSuspiciousScan(world, chunk, chunkPos, force);
        submitted |= this.submitHoleScan(world, chunk, chunkPos, force);
        submitted |= this.submitSeedScan(world, chunk, chunkPos, force);
        return submitted;
    }

    private WorldChunk getLoadedChunk(ClientWorld world, ChunkPos chunkPos) {
        ClientChunkManager chunkManager = world.getChunkManager();
        return chunkManager.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
    }

    private ThreadFactory threadFactory(String name) {
        return runnable -> {
            Thread thread = new Thread(runnable, name);
            thread.setDaemon(true);
            return thread;
        };
    }

    private void shutdownScanner(ExecutorService scanner) {
        if (scanner != null && !scanner.isShutdown()) {
            scanner.shutdownNow();
        }
    }

    private boolean isCurrentScan(ClientWorld world, int generation) {
        return this.isEnabled()
            && generation == this.worldGeneration.get()
            && world == BaseFinder.minecraftClient.world;
    }

    private boolean hasSusChunkScanSettings() {
        return this.kelp.isEnabled()
            || this.caveVines.isEnabled()
            || this.vines.isEnabled()
            || this.amethyst.isEnabled()
            || this.bamboo.isEnabled()
            || this.beeNest.isEnabled()
            || this.rotatedDeepslate.isEnabled();
    }

    private boolean hasSuspiciousBlockScanSettings() {
        return this.amethystClusters.isEnabled()
            || this.deepslate.isEnabled()
            || this.espRotatedDeepslate.isEnabled()
            || this.espVines.isEnabled()
            || this.espKelp.isEnabled();
    }

    private boolean submitSusChunkScan(ClientWorld world, WorldChunk chunk, ChunkPos chunkPos, boolean force) {
        ExecutorService scanner = this.susChunkScanner;
        if (!this.hasSusChunkScanSettings() || scanner == null || scanner.isShutdown()) {
            return false;
        }
        if (!force && this.scannedChunks.contains(chunkPos)) {
            return false;
        }
        if (!this.queuedSusChunkPositions.add(chunkPos)) {
            return false;
        }
        if (force) {
            this.scannedChunks.remove(chunkPos);
        }
        int generation = this.worldGeneration.get();
        try {
            scanner.submit(() -> {
                try {
                    if (!this.isCurrentScan(world, generation)) {
                        return;
                    }
                    ChunkScan result = this.scanSusChunk(world, chunk, generation);
                    if (result != null && this.isCurrentScan(world, generation)) {
                        this.applySusChunkResult(world, chunkPos, result, generation);
                    }
                } catch (Throwable ignored) {
                    // A partially loaded chunk must not stop later SusChunk scans.
                } finally {
                    this.queuedSusChunkPositions.remove(chunkPos);
                }
            });
        } catch (RuntimeException exception) {
            this.queuedSusChunkPositions.remove(chunkPos);
            return false;
        }
        return true;
    }

    private boolean submitSuspiciousScan(ClientWorld world, WorldChunk chunk, ChunkPos chunkPos, boolean force) {
        ExecutorService scanner = this.suspiciousScanner;
        if (!this.hasSuspiciousBlockScanSettings() || scanner == null || scanner.isShutdown()) {
            return false;
        }
        if (!force && this.scannedSuspiciousChunks.contains(chunkPos)) {
            return false;
        }
        if (!this.queuedSuspiciousPositions.add(chunkPos)) {
            return false;
        }
        if (force) {
            this.scannedSuspiciousChunks.remove(chunkPos);
        }
        int generation = this.worldGeneration.get();
        try {
            scanner.submit(() -> {
                try {
                    if (!this.isCurrentScan(world, generation)) {
                        return;
                    }
                    SuspiciousScan result = this.scanSuspiciousBlocks(world, chunk, generation);
                    if (result != null && this.isCurrentScan(world, generation)) {
                        this.applySuspiciousResult(world, chunkPos, result, generation);
                    }
                } catch (Throwable ignored) {
                    // A partially loaded chunk must not stop later ESP scans.
                } finally {
                    this.queuedSuspiciousPositions.remove(chunkPos);
                }
            });
        } catch (RuntimeException exception) {
            this.queuedSuspiciousPositions.remove(chunkPos);
            return false;
        }
        return true;
    }

    private boolean submitHoleScan(ClientWorld world, WorldChunk chunk, ChunkPos chunkPos, boolean force) {
        ExecutorService scanner = this.holeScanner;
        if ((!this.oneByOneHoles.isEnabled() && !this.threeByOneHoles.isEnabled())
            || scanner == null || scanner.isShutdown()) {
            return false;
        }
        if (!force && this.scannedHoleChunks.contains(chunkPos)) {
            return false;
        }
        if (!this.queuedHolePositions.add(chunkPos)) {
            return false;
        }
        if (force) {
            this.scannedHoleChunks.remove(chunkPos);
        }
        int generation = this.worldGeneration.get();
        try {
            scanner.submit(() -> {
                try {
                    if (!this.isCurrentScan(world, generation)) {
                        return;
                    }
                    HoleScan result = new HoleScan(
                        this.oneByOneHoles.isEnabled() ? this.scanHoles(world, chunkPos, false) : Set.of(),
                        this.threeByOneHoles.isEnabled() ? this.scanHoles(world, chunkPos, true) : Set.of()
                    );
                    if (this.isCurrentScan(world, generation)) {
                        this.applyHoleResult(world, chunkPos, result, generation);
                    }
                } catch (Throwable ignored) {
                    // A partially loaded chunk must not stop later Hole ESP scans.
                } finally {
                    this.queuedHolePositions.remove(chunkPos);
                }
            });
        } catch (RuntimeException exception) {
            this.queuedHolePositions.remove(chunkPos);
            return false;
        }
        return true;
    }

    private boolean submitSeedScan(ClientWorld world, WorldChunk chunk, ChunkPos chunkPos, boolean force) {
        ExecutorService scanner = this.seedScanner;
        if (!this.seedFinder.isEnabled() || !this.seedValid || scanner == null || scanner.isShutdown()) {
            return false;
        }
        if (!force && this.scannedSeedChunks.contains(chunkPos)) {
            return false;
        }
        if (!this.queuedSeedPositions.add(chunkPos)) {
            return false;
        }
        if (force) {
            this.scannedSeedChunks.remove(chunkPos);
        }
        int generation = this.worldGeneration.get();
        try {
            scanner.submit(() -> {
                try {
                    if (!this.isCurrentScan(world, generation)) {
                        return;
                    }
                    boolean result = this.isSeedChunk(world, chunk, chunkPos);
                    if (this.isCurrentScan(world, generation)) {
                        this.applySeedResult(world, chunkPos, result, generation);
                    }
                } catch (Throwable ignored) {
                    // A partially loaded chunk must not stop later Seed Finder scans.
                } finally {
                    this.queuedSeedPositions.remove(chunkPos);
                }
            });
        } catch (RuntimeException exception) {
            this.queuedSeedPositions.remove(chunkPos);
            return false;
        }
        return true;
    }

    private ChunkScan scanSusChunk(ClientWorld world, WorldChunk chunk, int generation) {
        if (!this.isCurrentScan(world, generation)) {
            return null;
        }

        ChunkPos chunkPos = chunk.getPos();
        Set<BlockPos> rotated = new HashSet<>();
        int selfHeat = 0;
        boolean hasUngrown = false;
        boolean analyzeAmethyst = this.amethyst.isEnabled() && this.isDonutFolia();

        ChunkSection[] sections = chunk.getSectionArray();
        int bottomY = chunk.getBottomY();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            ChunkSection section = sections[sectionIndex];
            if (section == null || section.isEmpty()) {
                continue;
            }
            int sectionY = bottomY + sectionIndex * 16;
            for (int localX = 0; localX < 16; localX++) {
                for (int localY = 0; localY < 16; localY++) {
                    for (int localZ = 0; localZ < 16; localZ++) {
                        BlockState state = section.getBlockState(localX, localY, localZ);
                        if (state.isAir()) {
                            continue;
                        }
                        int x = chunkPos.getStartX() + localX;
                        int y = sectionY + localY;
                        int z = chunkPos.getStartZ() + localZ;
                        BlockPos position = new BlockPos(x, y, z);
                        Block block = state.getBlock();

                        if (this.kelp.isEnabled() && block == Blocks.KELP && state.contains(Properties.AGE_25)) {
                            int age = state.get(Properties.AGE_25);
                            boolean mature = age == 25 || !world.getBlockState(position.up()).isOf(Blocks.WATER);
                            if (mature) {
                                selfHeat++;
                            } else {
                                hasUngrown = true;
                            }
                        }

                        if (this.caveVines.isEnabled()
                            && (block == Blocks.CAVE_VINES || block == Blocks.CAVE_VINES_PLANT)
                            && state.contains(Properties.BERRIES)) {
                            boolean mature = Boolean.TRUE.equals(state.get(Properties.BERRIES))
                                || !world.getBlockState(position.down()).isAir();
                            if (mature) {
                                selfHeat++;
                            } else {
                                hasUngrown = true;
                            }
                        }

                        if (this.vines.isEnabled() && block == Blocks.VINE) {
                            if (!world.getBlockState(position.down()).isOf(Blocks.VINE)) {
                                selfHeat++;
                            } else {
                                hasUngrown = true;
                            }
                        }

                        if (analyzeAmethyst && this.isAmethystBud(block)) {
                            if (this.isBudGrowingFromBuddingAmethyst(world, state, position)) {
                                selfHeat++;
                            } else {
                                hasUngrown = true;
                            }
                        }
                        if (analyzeAmethyst && block == Blocks.AMETHYST_CLUSTER) {
                            selfHeat++;
                        }

                        if (this.bamboo.isEnabled() && block == Blocks.BAMBOO && state.contains(Properties.AGE_1)) {
                            int age = state.get(Properties.AGE_1);
                            if (!world.getBlockState(position.up()).isOf(Blocks.BAMBOO)) {
                                if (age == 1) {
                                    selfHeat++;
                                } else {
                                    hasUngrown = true;
                                }
                            }
                        }

                        if (this.beeNest.isEnabled() && block == Blocks.BEE_NEST && state.contains(Properties.HONEY_LEVEL)) {
                            int honeyLevel = state.get(Properties.HONEY_LEVEL);
                            if (honeyLevel == 5) {
                                selfHeat++;
                            } else {
                                hasUngrown = true;
                            }
                        }

                        if (this.rotatedDeepslate.isEnabled()
                            && this.isRotatedDeepslate(world, state, position, 0, 60, false, true)) {
                            rotated.add(position);
                        }
                    }
                }
            }
        }
        return new ChunkScan(selfHeat, hasUngrown, Set.copyOf(rotated));
    }

    private SuspiciousScan scanSuspiciousBlocks(ClientWorld world, WorldChunk chunk, int generation) {
        if (!this.isCurrentScan(world, generation)) {
            return null;
        }

        ChunkPos chunkPos = chunk.getPos();
        Map<BlockPos, SuspiciousType> suspiciousBlocks = new HashMap<>();
        int matureKelp = 0;
        ChunkSection[] sections = chunk.getSectionArray();
        int bottomY = chunk.getBottomY();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            ChunkSection section = sections[sectionIndex];
            if (section == null || section.isEmpty()) {
                continue;
            }
            int sectionY = bottomY + sectionIndex * 16;
            for (int localX = 0; localX < 16; localX++) {
                for (int localY = 0; localY < 16; localY++) {
                    for (int localZ = 0; localZ < 16; localZ++) {
                        BlockState state = section.getBlockState(localX, localY, localZ);
                        if (state.isAir()) {
                            continue;
                        }
                        int x = chunkPos.getStartX() + localX;
                        int y = sectionY + localY;
                        int z = chunkPos.getStartZ() + localZ;
                        BlockPos position = new BlockPos(x, y, z);
                        Block block = state.getBlock();

                        if (this.amethystClusters.isEnabled() && block == Blocks.AMETHYST_CLUSTER) {
                            suspiciousBlocks.put(position, SuspiciousType.AMETHYST_CLUSTER);
                        }
                        if (this.deepslate.isEnabled() && block == Blocks.DEEPSLATE
                            && y >= this.intValue(this.deepslateMinY) && y <= this.intValue(this.deepslateMaxY)) {
                            suspiciousBlocks.put(position, SuspiciousType.DEEPSLATE);
                        }
                        if (this.espVines.isEnabled() && this.isSuspiciousVine(block)) {
                            suspiciousBlocks.put(position, SuspiciousType.VINE);
                        }
                        if (this.espKelp.isEnabled() && block == Blocks.KELP && state.contains(Properties.AGE_25)
                            && state.get(Properties.AGE_25) == 25) {
                            matureKelp++;
                        }
                        if (this.espRotatedDeepslate.isEnabled()
                            && this.isRotatedDeepslate(world, state, position,
                                this.intValue(this.deepslateMinY), this.intValue(this.deepslateMaxY), true, false)) {
                            suspiciousBlocks.put(position, SuspiciousType.ROTATED_DEEPSLATE);
                        }
                    }
                }
            }
        }
        return new SuspiciousScan(suspiciousBlocks, matureKelp >= KELP_THRESHOLD);
    }

    private synchronized void applySusChunkResult(ClientWorld world, ChunkPos chunkPos,
                                                   ChunkScan chunkScan, int generation) {
        if (!this.isCurrentScan(world, generation)) {
            return;
        }
        int radius = this.intValue(this.simulationDistance);
        int previousHeat = this.scoreByChunk.getOrDefault(chunkPos, 0);
        boolean previouslyUngrown = Boolean.TRUE.equals(this.grownByChunk.get(chunkPos));

        if (previousHeat > 0) {
            this.applyScoreDelta(chunkPos, -previousHeat, radius);
        }
        if (previouslyUngrown) {
            this.applySuppressionDelta(chunkPos, -1, radius);
        }
        Set<BlockPos> oldRotated = this.rotatedByChunk.remove(chunkPos);
        if (oldRotated != null) {
            this.rotatedDeepslatePositions.removeAll(oldRotated);
        }

        this.scoreByChunk.put(chunkPos, chunkScan.selfHeat);
        this.grownByChunk.put(chunkPos, chunkScan.hasUngrown);
        if (chunkScan.hasUngrown) {
            this.applySuppressionDelta(chunkPos, 1, radius);
        }
        if (chunkScan.selfHeat > 0) {
            this.applyScoreDelta(chunkPos, chunkScan.selfHeat, radius);
        }
        if (chunkScan.rotated.isEmpty()) {
            this.rotatedByChunk.remove(chunkPos);
        } else {
            Set<BlockPos> copy = chunkScan.rotated;
            this.rotatedByChunk.put(chunkPos, copy);
            this.rotatedDeepslatePositions.addAll(copy);
        }
        this.scannedChunks.add(chunkPos);
    }

    private synchronized void applySuspiciousResult(ClientWorld world, ChunkPos chunkPos,
                                                     SuspiciousScan result, int generation) {
        if (!this.isCurrentScan(world, generation)) {
            return;
        }
        if (result.suspiciousBlocks.isEmpty()) {
            this.suspiciousBlocksByChunk.remove(chunkPos);
        } else {
            this.suspiciousBlocksByChunk.put(chunkPos, Map.copyOf(result.suspiciousBlocks));
        }
        if (result.kelpChunk) {
            this.kelpChunks.add(chunkPos);
        } else {
            this.kelpChunks.remove(chunkPos);
        }
        this.scannedSuspiciousChunks.add(chunkPos);
    }

    private synchronized void applyHoleResult(ClientWorld world, ChunkPos chunkPos,
                                               HoleScan result, int generation) {
        if (!this.isCurrentScan(world, generation)) {
            return;
        }
        if (result.holes1x1.isEmpty()) {
            this.holes1x1ByChunk.remove(chunkPos);
        } else {
            this.holes1x1ByChunk.put(chunkPos, Set.copyOf(result.holes1x1));
        }
        if (result.holes3x1.isEmpty()) {
            this.holes3x1ByChunk.remove(chunkPos);
        } else {
            this.holes3x1ByChunk.put(chunkPos, Set.copyOf(result.holes3x1));
        }
        this.scannedHoleChunks.add(chunkPos);
    }

    private synchronized void applySeedResult(ClientWorld world, ChunkPos chunkPos,
                                               boolean seedChunk, int generation) {
        if (!this.isCurrentScan(world, generation)) {
            return;
        }
        if (seedChunk) {
            this.seedChunks.add(chunkPos);
        } else {
            this.seedChunks.remove(chunkPos);
        }
        this.scannedSeedChunks.add(chunkPos);
    }

    private void applyScoreDelta(ChunkPos source, int delta, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                ChunkPos target = new ChunkPos(source.x + x, source.z + z);
                if (delta > 0) {
                    this.suspicionScores.merge(target, delta, Integer::sum);
                } else {
                    this.suspicionScores.compute(target, (ignored, current) -> {
                        if (current == null) {
                            return null;
                        }
                        int updated = current + delta;
                        return updated <= 0 ? null : updated;
                    });
                }
            }
        }
    }

    private void applySuppressionDelta(ChunkPos source, int delta, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                ChunkPos target = new ChunkPos(source.x + x, source.z + z);
                if (delta > 0) {
                    this.suppressionCounts.merge(target, delta, Integer::sum);
                } else {
                    this.suppressionCounts.compute(target, (ignored, current) -> {
                        if (current == null) {
                            return null;
                        }
                        int updated = current + delta;
                        return updated <= 0 ? null : updated;
                    });
                }
            }
        }
    }

    /* ----------------------------- SusChunkFinder ----------------------------- */

    private boolean isAmethystBud(Block block) {
        return block == Blocks.SMALL_AMETHYST_BUD
            || block == Blocks.MEDIUM_AMETHYST_BUD
            || block == Blocks.LARGE_AMETHYST_BUD;
    }

    private boolean isBudGrowingFromBuddingAmethyst(ClientWorld world, BlockState state, BlockPos position) {
        if (!state.contains(Properties.FACING)) {
            return false;
        }
        Direction facing = state.get(Properties.FACING);
        return world.getBlockState(position.offset(facing.getOpposite())).isOf(Blocks.BUDDING_AMETHYST);
    }

    /* ---------------------------- SuspiciousESP ---------------------------- */

    private boolean isDeepslateFamily(Block block) {
        return block == Blocks.DEEPSLATE
            || block == Blocks.POLISHED_DEEPSLATE
            || block == Blocks.DEEPSLATE_TILES
            || block == Blocks.DEEPSLATE_BRICKS
            || block == Blocks.CHISELED_DEEPSLATE;
    }

    /** The reference client treats all five vanilla vine block classes as "vines". */
    private boolean isSuspiciousVine(Block block) {
        return block == Blocks.VINE
            || block == Blocks.TWISTING_VINES
            || block == Blocks.TWISTING_VINES_PLANT
            || block == Blocks.WEEPING_VINES
            || block == Blocks.WEEPING_VINES_PLANT;
    }

    private boolean isRotatedDeepslate(ClientWorld world, BlockState state, BlockPos position,
                                        int minY, int maxY, boolean useFamily, boolean requireEmptyNeighbors) {
        if (position.getY() < minY || position.getY() > maxY || !state.contains(Properties.FACING)) {
            return false;
        }
        if (state.get(Properties.FACING).getAxis() == Direction.Axis.Y) {
            return false;
        }
        if (useFamily ? !this.isDeepslateFamily(state.getBlock()) : !state.isOf(Blocks.DEEPSLATE)) {
            return false;
        }
        if (requireEmptyNeighbors) {
            for (Direction direction : Direction.values()) {
                if (!world.getBlockState(position.offset(direction)).isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * SusChunkFinder only enables its amethyst signal on the same server family as
     * the reference client. The method is intentionally reflective because the
     * 1.21.4 named jar does not expose getBrand as a public method.
     */
    private boolean isDonutFolia() {
        Object networkHandler = BaseFinder.minecraftClient.getNetworkHandler();
        if (networkHandler == null) {
            return false;
        }
        Class<?> type = networkHandler.getClass();
        while (type != null) {
            for (String methodName : new String[]{"getBrand", "method_52790"}) {
                try {
                    Method method = type.getDeclaredMethod(methodName);
                    method.setAccessible(true);
                    Object brand = method.invoke(networkHandler);
                    return brand instanceof String string && string.contains("DonutFolia");
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                    // Try the intermediary name or the superclass below.
                }
            }
            type = type.getSuperclass();
        }
        return false;
    }

    private ColorRGBA suspiciousColor(SuspiciousType type, int alpha) {
        return switch (type) {
            case AMETHYST_CLUSTER -> new ColorRGBA(0.0f, 200.0f, 255.0f, alpha);
            case DEEPSLATE -> new ColorRGBA(255.0f, 0.0f, 255.0f, alpha);
            case ROTATED_DEEPSLATE -> new ColorRGBA(0.0f, 255.0f, 0.0f, alpha);
            case VINE -> new ColorRGBA(180.0f, 0.0f, 255.0f, alpha);
        };
    }

    /* ------------------------------- HoleESP ------------------------------- */

    private Set<Hole> scanHoles(ClientWorld world, ChunkPos chunkPos, boolean threeByOne) {
        Set<Hole> holes = new HashSet<>();
        HoleChunkRef chunkRef = new HoleChunkRef(world, this.getLoadedChunk(world, chunkPos));
        boolean[] orientations = threeByOne ? new boolean[]{true, false} : new boolean[]{false};
        for (boolean alongX : orientations) {
            this.scanHoleOrientation(chunkRef, chunkPos, threeByOne, alongX, holes);
        }
        return holes;
    }

    private void scanHoleOrientation(HoleChunkRef chunkRef, ChunkPos chunkPos, boolean threeByOne,
                                     boolean alongX, Set<Hole> holes) {
        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();
        int bottomY = chunkRef.world.getBottomY();
        int topY = bottomY + chunkRef.world.getHeight();
        int minimumDepth = this.intValue(this.minHoleDepth);
        int width = alongX ? 3 : 1;
        int length = alongX ? 1 : 3;

        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                int runStart = 0;
                int runLength = 0;
                for (int y = bottomY; y < topY; y++) {
                    boolean hole = threeByOne
                        ? this.isThreeByOneHole(chunkRef, x, y, z, alongX)
                        : this.isOneByOneHole(chunkRef, x, y, z);
                    if (hole) {
                        if (runLength == 0) {
                            runStart = y;
                        }
                        runLength++;
                    } else {
                        this.addHole(holes, x, z, runStart, runLength, width, length, minimumDepth);
                        runStart = 0;
                        runLength = 0;
                    }
                }
                this.addHole(holes, x, z, runStart, runLength, width, length, minimumDepth);
            }
        }
    }

    private void addHole(Set<Hole> holes, int x, int z, int startY, int length, int width,
                          int holeLength, int minimumDepth) {
        if (length >= minimumDepth) {
            holes.add(new Hole(x, z, startY, startY + length, width, holeLength));
        }
    }

    private boolean isOneByOneHole(HoleChunkRef chunkRef, int x, int y, int z) {
        if (!this.isPassable(chunkRef, x, y, z)) {
            return false;
        }
        return !this.isPassable(chunkRef, x - 1, y, z)
            && !this.isPassable(chunkRef, x + 1, y, z)
            && !this.isPassable(chunkRef, x, y, z - 1)
            && !this.isPassable(chunkRef, x, y, z + 1);
    }

    private boolean isThreeByOneHole(HoleChunkRef chunkRef, int x, int y, int z, boolean alongX) {
        if (alongX) {
            if (!this.isPassable(chunkRef, x, y, z)
                || !this.isPassable(chunkRef, x + 1, y, z)
                || !this.isPassable(chunkRef, x + 2, y, z)) {
                return false;
            }
            return !this.isPassable(chunkRef, x - 1, y, z)
                && !this.isPassable(chunkRef, x + 3, y, z)
                && !this.isPassable(chunkRef, x, y, z - 1)
                && !this.isPassable(chunkRef, x, y, z + 1)
                && !this.isPassable(chunkRef, x + 1, y, z - 1)
                && !this.isPassable(chunkRef, x + 1, y, z + 1)
                && !this.isPassable(chunkRef, x + 2, y, z - 1)
                && !this.isPassable(chunkRef, x + 2, y, z + 1);
        }
        if (!this.isPassable(chunkRef, x, y, z)
            || !this.isPassable(chunkRef, x, y, z + 1)
            || !this.isPassable(chunkRef, x, y, z + 2)) {
            return false;
        }
        return !this.isPassable(chunkRef, x, y, z - 1)
            && !this.isPassable(chunkRef, x, y, z + 3)
            && !this.isPassable(chunkRef, x - 1, y, z)
            && !this.isPassable(chunkRef, x + 1, y, z)
            && !this.isPassable(chunkRef, x - 1, y, z + 1)
            && !this.isPassable(chunkRef, x + 1, y, z + 1)
            && !this.isPassable(chunkRef, x - 1, y, z + 2)
            && !this.isPassable(chunkRef, x + 1, y, z + 2);
    }

    private boolean isPassable(HoleChunkRef chunkRef, int x, int y, int z) {
        BlockPos position = new BlockPos(x, y, z);
        BlockState state = chunkRef.getBlockState(x, y, z);
        return state.getCollisionShape(chunkRef.world, position).isEmpty();
    }

    /* ---------------------------- SeedChunkFinder ---------------------------- */

    private void refreshSeedConfiguration() {
        String configuredSeed = this.serverSeed.getValue();
        if (Objects.equals(configuredSeed, this.activeSeed)) {
            return;
        }
        this.activeSeed = configuredSeed;
        this.seedChunks.clear();
        this.seedValid = false;
        this.randomDeriver = null;
        if (configuredSeed == null || configuredSeed.isBlank()) {
            return;
        }
        try {
            long seed = Long.parseLong(configuredSeed.trim());
            this.randomDeriver = new Xoroshiro128PlusPlusRandom(seed)
                .nextSplitter()
                .split("minecraft:deepslate")
                .nextSplitter();
            this.seedValid = true;
        } catch (NumberFormatException ignored) {
            // An invalid seed disables only the seed sub-feature, not the complete module.
        }
    }

    private void refreshScanConfiguration() {
        String configuration = this.scanConfiguration();
        if (Objects.equals(configuration, this.activeScanConfiguration)) {
            return;
        }
        this.activeScanConfiguration = configuration;
        this.resetState();
        this.refreshSeedConfiguration();
        this.queueNearbyChunks();
    }

    private String scanConfiguration() {
        StringBuilder configuration = new StringBuilder(256);
        configuration.append(this.serverSeed.getValue()).append('|');
        configuration.append(this.simulationDistance.getValue()).append('|');
        configuration.append(this.sensitivity.getValue()).append('|');
        configuration.append(this.chunkAlpha.getValue()).append('|');
        configuration.append(this.kelp.isEnabled()).append('|');
        configuration.append(this.caveVines.isEnabled()).append('|');
        configuration.append(this.vines.isEnabled()).append('|');
        configuration.append(this.amethyst.isEnabled()).append('|');
        configuration.append(this.bamboo.isEnabled()).append('|');
        configuration.append(this.beeNest.isEnabled()).append('|');
        configuration.append(this.rotatedDeepslate.isEnabled()).append('|');
        configuration.append(this.wanderingTraders.isEnabled()).append('|');
        configuration.append(this.villagers.isEnabled()).append('|');
        configuration.append(this.llamas.isEnabled()).append('|');
        configuration.append(this.pillagers.isEnabled()).append('|');
        configuration.append(this.deepslate.isEnabled()).append('|');
        configuration.append(this.espRotatedDeepslate.isEnabled()).append('|');
        configuration.append(this.espVines.isEnabled()).append('|');
        configuration.append(this.espKelp.isEnabled()).append('|');
        configuration.append(this.amethystClusters.isEnabled()).append('|');
        configuration.append(this.deepslateMinY.getValue()).append('|');
        configuration.append(this.deepslateMaxY.getValue()).append('|');
        configuration.append(this.minHoleDepth.getValue()).append('|');
        configuration.append(this.oneByOneHoles.isEnabled()).append('|');
        configuration.append(this.threeByOneHoles.isEnabled()).append('|');
        configuration.append(this.seedFinder.isEnabled()).append('|');
        configuration.append(this.lightFinder.isEnabled());
        return configuration.toString();
    }

    private boolean isSeedChunk(ClientWorld world, WorldChunk chunk, ChunkPos chunkPos) {
        RandomSplitter deriver = this.randomDeriver;
        if (deriver == null || !this.seedValid) {
            return false;
        }
        SeedChunkRef chunkRef = new SeedChunkRef(world, chunk);
        for (int x = chunkPos.getStartX(); x < chunkPos.getStartX() + 16; x++) {
            for (int z = chunkPos.getStartZ(); z < chunkPos.getStartZ() + 16; z++) {
                for (int y = 1; y < 7; y++) {
                    BlockState state = chunkRef.getBlockState(x, y, z);
                    if (this.isVanillaBedrock(deriver, x, y, z) || !state.isOf(Blocks.DEEPSLATE)) {
                        continue;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isVanillaBedrock(RandomSplitter deriver, int x, int y, int z) {
        if (y >= 8) {
            return false;
        }
        if (y <= 0) {
            return true;
        }
        Random random = deriver.split(x, y, z);
        return random.nextFloat() < BEDROCK_CHANCES[y];
    }

    /* -------------------------------- Rendering -------------------------------- */

    private void render(Render3DEvent event) {
        ClientWorld world = BaseFinder.minecraftClient.world;
        if (world == null || BaseFinder.minecraftClient.player == null) {
            return;
        }
        OverlayFrame frame = new OverlayFrame(event);
        int radius = this.getChunkScanRadius();
        this.renderSuspiciousChunks(frame, world, radius);
        this.renderSuspiciousBlocks(frame, world, radius);
        this.renderKelpChunks(frame, world, radius);
        this.renderHoles(frame, world, radius);
        this.renderSeedChunks(frame, world, radius);
        this.renderLightSections(frame, world);
        this.renderEntities(frame, world, radius);
        frame.flush();
    }

    private void renderSuspiciousChunks(OverlayFrame frame, ClientWorld world, int radius) {
        int minimumScore = this.intValue(this.sensitivity);
        ColorRGBA color = new ColorRGBA(255.0f, 0.0f, 0.0f, this.intValue(this.chunkAlpha));
        for (Map.Entry<ChunkPos, Integer> entry : this.suspicionScores.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            if (entry.getValue() < minimumScore
                || this.suppressionCounts.containsKey(chunkPos)
                || !this.isNearChunk(chunkPos, radius)
                || !world.isChunkLoaded(chunkPos.x, chunkPos.z)
                || this.scannedNeighbourCount(chunkPos) < MIN_NEIGHBOUR_CHUNKS) {
                continue;
            }
            frame.addBox(new Box(chunkPos.getStartX(), 63.0, chunkPos.getStartZ(),
                chunkPos.getStartX() + 16.0, 63.1, chunkPos.getStartZ() + 16.0), color);
            this.recordBaseLocation(new BlockPos(chunkPos.getStartX() + 8, 63, chunkPos.getStartZ() + 8));
        }

        if (this.rotatedDeepslatePositions.isEmpty()) {
            return;
        }
        ColorRGBA rotatedColor = new ColorRGBA(0.0f, 255.0f, 255.0f, this.intValue(this.chunkAlpha));
        this.rotatedDeepslatePositions.removeIf(position -> !world.isChunkLoaded(position.getX() >> 4, position.getZ() >> 4));
        for (BlockPos position : this.rotatedDeepslatePositions) {
            if (!this.isNearChunk(new ChunkPos(position), radius)) {
                continue;
            }
            frame.addBox(new Box(position), rotatedColor);
        }
    }

    private void renderSuspiciousBlocks(OverlayFrame frame, ClientWorld world, int radius) {
        int alpha = this.intValue(this.espAlpha);
        for (Map.Entry<ChunkPos, Map<BlockPos, SuspiciousType>> chunkEntry : this.suspiciousBlocksByChunk.entrySet()) {
            if (!this.isNearChunk(chunkEntry.getKey(), radius) || !world.isChunkLoaded(chunkEntry.getKey().x, chunkEntry.getKey().z)) {
                continue;
            }
            for (Map.Entry<BlockPos, SuspiciousType> blockEntry : chunkEntry.getValue().entrySet()) {
                BlockPos position = blockEntry.getKey();
                ColorRGBA color = this.suspiciousColor(blockEntry.getValue(), alpha);
                frame.addBox(new Box(position), color);
                if (this.tracers.isEnabled()) {
                    frame.addTracer(position.toCenterPos(),
                        this.suspiciousColor(blockEntry.getValue(), 255));
                }
            }
        }
    }

    private void renderKelpChunks(OverlayFrame frame, ClientWorld world, int radius) {
        if (!this.espKelp.isEnabled()) {
            return;
        }
        ColorRGBA color = new ColorRGBA(0.0f, 200.0f, 255.0f, this.intValue(this.espAlpha));
        for (ChunkPos chunkPos : this.kelpChunks) {
            if (!world.isChunkLoaded(chunkPos.x, chunkPos.z) || !this.isNearChunk(chunkPos, radius)) {
                continue;
            }
            frame.addBox(new Box(chunkPos.getStartX(), 63.0, chunkPos.getStartZ(),
                chunkPos.getStartX() + 16.0, 63.1, chunkPos.getStartZ() + 16.0), color);
        }
    }

    private void renderHoles(OverlayFrame frame, ClientWorld world, int radius) {
        int alpha = this.intValue(this.holeAlpha);
        if (this.oneByOneHoles.isEnabled()) {
            ColorRGBA color = new ColorRGBA(255.0f, 0.0f, 0.0f, alpha);
            this.renderHoleSet(frame, world, radius, this.holes1x1ByChunk, color);
        }
        if (this.threeByOneHoles.isEnabled()) {
            ColorRGBA color = new ColorRGBA(255.0f, 165.0f, 0.0f, alpha);
            this.renderHoleSet(frame, world, radius, this.holes3x1ByChunk, color);
        }
    }

    private void renderHoleSet(OverlayFrame frame, ClientWorld world, int radius,
                               ConcurrentMap<ChunkPos, Set<Hole>> holesByChunk, ColorRGBA color) {
        for (Map.Entry<ChunkPos, Set<Hole>> chunkEntry : holesByChunk.entrySet()) {
            ChunkPos chunkPos = chunkEntry.getKey();
            if (!world.isChunkLoaded(chunkPos.x, chunkPos.z) || !this.isNearChunk(chunkPos, radius)) {
                continue;
            }
            for (Hole hole : chunkEntry.getValue()) {
                frame.addBox(new Box(hole.x, hole.startY, hole.z,
                    hole.x + hole.width, hole.endY, hole.z + hole.length), color);
            }
        }
    }

    private void renderSeedChunks(OverlayFrame frame, ClientWorld world, int radius) {
        if (!this.seedFinder.isEnabled() || !this.seedValid) {
            return;
        }
        ColorRGBA color = new ColorRGBA(0.0f, 255.0f, 0.0f, 100.0f);
        for (ChunkPos chunkPos : this.seedChunks) {
            if (!world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
                this.seedChunks.remove(chunkPos);
                continue;
            }
            if (!this.isNearChunk(chunkPos, radius)) {
                continue;
            }
            frame.addBox(new Box(chunkPos.getStartX(), 63.0, chunkPos.getStartZ(),
                chunkPos.getStartX() + 16.0, 63.1, chunkPos.getStartZ() + 16.0), color);
            this.recordBaseLocation(new BlockPos(chunkPos.getStartX() + 8, 63, chunkPos.getStartZ() + 8));
        }
    }

    private void renderLightSections(OverlayFrame frame, ClientWorld world) {
        if (!this.lightFinder.isEnabled()) {
            return;
        }
        try {
            ChunkLightingView lightingView = world.getLightingProvider().get(LightType.BLOCK);
            if (!(lightingView instanceof ChunkLightProvider blockLightProvider)) {
                return;
            }

            LightStorage lightStorage = blockLightProvider.lightStorage;
            ChunkToNibbleArrayMap storage = lightStorage.storage;
            Long2ObjectOpenHashMap<ChunkNibbleArray> arrays = storage.arrays;
            ChunkPos playerChunk = BaseFinder.minecraftClient.player.getChunkPos();
            int radius = this.getChunkScanRadius();
            List<List<Box>> boxesByLight = new ArrayList<>(16);
            for (int lightLevel = 0; lightLevel < 16; lightLevel++) {
                boxesByLight.add(null);
            }

            for (Long2ObjectMap.Entry<ChunkNibbleArray> entry : arrays.long2ObjectEntrySet()) {
                ChunkNibbleArray lightArray = entry.getValue();
                if (lightArray == null || lightArray.isArrayUninitialized()) {
                    continue;
                }

                ChunkSectionPos sectionPos = ChunkSectionPos.from(entry.getLongKey());
                ChunkPos chunkPos = sectionPos.toChunkPos();
                if (Math.abs(chunkPos.x - playerChunk.x) > radius
                    || Math.abs(chunkPos.z - playerChunk.z) > radius) {
                    continue;
                }
                int sectionX = sectionPos.getMinX();
                int sectionY = sectionPos.getMinY();
                int sectionZ = sectionPos.getMinZ();
                for (int localX = 0; localX < 16; localX++) {
                    for (int localY = 0; localY < 16; localY++) {
                        for (int localZ = 0; localZ < 16; localZ++) {
                            int lightLevel = lightArray.get(localX, localY, localZ);
                            if (lightLevel == 0) {
                                continue;
                            }

                            int x = sectionX + localX;
                            int y = sectionY + localY;
                            int z = sectionZ + localZ;
                            if (y < 0) {
                                continue;
                            }

                            List<Box> lightBoxes = boxesByLight.get(lightLevel);
                            if (lightBoxes == null) {
                                lightBoxes = new ArrayList<>();
                                boxesByLight.set(lightLevel, lightBoxes);
                            }
                            lightBoxes.add(new Box(
                                x, y, z,
                                x + 1.0, y + 1.0, z + 1.0
                            ));
                        }
                    }
                }
            }

            for (int lightLevel = 1; lightLevel < 16; lightLevel++) {
                List<Box> lightBoxes = boxesByLight.get(lightLevel);
                if (lightBoxes == null || lightBoxes.isEmpty()) {
                    continue;
                }
                float intensity = lightLevel / 15.0f;
                ColorRGBA color = new ColorRGBA(
                    intensity * 255.0f,
                    intensity * 255.0f,
                    0.2f * 255.0f,
                    0.3f * 255.0f
                );
                frame.addBoxes(lightBoxes, color);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void renderEntities(OverlayFrame frame, ClientWorld world, int radius) {
        int alpha = this.intValue(this.espAlpha);
        for (Entity entity : world.getEntities()) {
            ChunkPos chunkPos = new ChunkPos(entity.getBlockPos());
            if (!this.isNearChunk(chunkPos, radius)) {
                continue;
            }
            ColorRGBA color = null;
            ColorRGBA tracerColor = null;
            if (this.wanderingTraders.isEnabled() && entity instanceof WanderingTraderEntity) {
                color = new ColorRGBA(0.0f, 255.0f, 0.0f, alpha);
                tracerColor = new ColorRGBA(0.0f, 255.0f, 0.0f, 255.0f);
            } else if (this.villagers.isEnabled() && entity instanceof VillagerEntity) {
                color = new ColorRGBA(0.0f, 255.0f, 0.0f, alpha);
                tracerColor = new ColorRGBA(0.0f, 255.0f, 0.0f, 255.0f);
            } else if (this.villagers.isEnabled() && entity instanceof ZombieVillagerEntity) {
                color = new ColorRGBA(255.0f, 0.0f, 0.0f, alpha);
                tracerColor = new ColorRGBA(255.0f, 0.0f, 0.0f, 255.0f);
            } else if (this.llamas.isEnabled() && entity instanceof LlamaEntity) {
                color = new ColorRGBA(255.0f, 165.0f, 0.0f, alpha);
                tracerColor = new ColorRGBA(255.0f, 165.0f, 0.0f, 255.0f);
            } else if (this.pillagers.isEnabled() && entity instanceof PillagerEntity) {
                color = new ColorRGBA(255.0f, 0.0f, 0.0f, alpha);
                tracerColor = new ColorRGBA(255.0f, 0.0f, 0.0f, 255.0f);
            }
            if (color == null) {
                continue;
            }
            Vec3d interpolatedPosition = moscow.rockstar.render.util.ProjectionUtils.interpolateEntityPosition(
                entity, frame.event.getTickDelta());
            Vec3d entityOffset = interpolatedPosition.subtract(entity.getPos());
            frame.addBox(entity.getBoundingBox().offset(entityOffset), color);
            if (this.tracers.isEnabled()) {
                frame.addTracer(entity.getBoundingBox().getCenter().add(entityOffset), tracerColor);
            }
        }
    }

    private int scannedNeighbourCount(ChunkPos center) {
        int count = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if ((x != 0 || z != 0) && this.scannedChunks.contains(new ChunkPos(center.x + x, center.z + z))) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isNearChunk(ChunkPos chunkPos, int radius) {
        if (BaseFinder.minecraftClient.player == null) {
            return false;
        }
        ChunkPos playerChunk = BaseFinder.minecraftClient.player.getChunkPos();
        return Math.abs(chunkPos.x - playerChunk.x) <= radius && Math.abs(chunkPos.z - playerChunk.z) <= radius;
    }

    private void syncDetectedBases() {
        if (BaseFinder.minecraftClient.world == null || BaseFinder.minecraftClient.player == null) {
            return;
        }
        int radius = this.getChunkScanRadius();
        int minimumScore = this.intValue(this.sensitivity);
        for (Map.Entry<ChunkPos, Integer> entry : this.suspicionScores.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            if (entry.getValue() < minimumScore
                || this.suppressionCounts.containsKey(chunkPos)
                || !this.isNearChunk(chunkPos, radius)
                || this.scannedNeighbourCount(chunkPos) < MIN_NEIGHBOUR_CHUNKS) {
                continue;
            }
            this.recordBaseLocation(new BlockPos(chunkPos.getStartX() + 8, 63, chunkPos.getStartZ() + 8));
        }
    }

    private void recordBaseLocation(BlockPos position) {
        synchronized (this.detectedBases) {
            if (this.isNearExistingBase(position)) {
                return;
            }
            BaseLocation location = new BaseLocation("Base-" + this.nextBaseNumber++, position);
            this.detectedBases.add(location);
            Notification.info(Text.of(formatBaseNotification(location)));
        }
    }

    private boolean isNearExistingBase(BlockPos position) {
        for (BaseLocation location : this.detectedBases) {
            long dx = (long)position.getX() - location.getBasePosition().getX();
            long dy = (long)position.getY() - location.getBasePosition().getY();
            long dz = (long)position.getZ() - location.getBasePosition().getZ();
            if (dx * dx + dy * dy + dz * dz <= 2304L) {
                return true;
            }
        }
        return false;
    }

    private void pruneOutOfRangeData() {
        if (BaseFinder.minecraftClient.player == null) {
            return;
        }
        ChunkPos playerChunk = BaseFinder.minecraftClient.player.getChunkPos();
        int radius = this.getChunkScanRadius();
        this.scannedChunks.removeIf(chunk -> Math.abs(chunk.x - playerChunk.x) > radius || Math.abs(chunk.z - playerChunk.z) > radius);
        this.scannedSuspiciousChunks.removeIf(chunk -> Math.abs(chunk.x - playerChunk.x) > radius || Math.abs(chunk.z - playerChunk.z) > radius);
        this.scannedHoleChunks.removeIf(chunk -> Math.abs(chunk.x - playerChunk.x) > radius || Math.abs(chunk.z - playerChunk.z) > radius);
        this.scannedSeedChunks.removeIf(chunk -> Math.abs(chunk.x - playerChunk.x) > radius || Math.abs(chunk.z - playerChunk.z) > radius);
        this.seedChunks.removeIf(chunk -> Math.abs(chunk.x - playerChunk.x) > radius || Math.abs(chunk.z - playerChunk.z) > radius);
    }

    private int getChunkScanRadius() {
        if (BaseFinder.minecraftClient.options == null) {
            return 8;
        }
        Object value = BaseFinder.minecraftClient.options.getViewDistance().getValue();
        int viewDistance = value instanceof Integer integer ? integer : 8;
        return Math.max(1, viewDistance);
    }

    private int intValue(NumberSetting setting) {
        return Math.round(setting.getValue());
    }

    private synchronized void resetState() {
        this.worldGeneration.incrementAndGet();
        this.scanTick = 0;
        this.pendingChunkPositions.clear();
        this.queuedSusChunkPositions.clear();
        this.queuedSuspiciousPositions.clear();
        this.queuedHolePositions.clear();
        this.queuedSeedPositions.clear();
        this.scannedChunks.clear();
        this.scannedSuspiciousChunks.clear();
        this.scannedHoleChunks.clear();
        this.scannedSeedChunks.clear();
        this.suspicionScores.clear();
        this.suppressionCounts.clear();
        this.scoreByChunk.clear();
        this.grownByChunk.clear();
        this.rotatedByChunk.clear();
        this.rotatedDeepslatePositions.clear();
        this.suspiciousBlocksByChunk.clear();
        this.kelpChunks.clear();
        this.holes1x1ByChunk.clear();
        this.holes3x1ByChunk.clear();
        this.seedChunks.clear();
        this.activeSeed = null;
        this.seedValid = false;
        this.randomDeriver = null;
        synchronized (this.detectedBases) {
            this.detectedBases.clear();
            this.nextBaseNumber = 1;
        }
    }

    private String normalizeBaseName(String name) {
        String normalized = name.trim().toLowerCase();
        if (normalized.matches("\\d+")) {
            return "base-" + normalized;
        }
        return normalized.replace("база", "base").replace("baza", "base").replace(" ", "");
    }

    public static String formatBaseNotification(BaseLocation location) {
        return formatBaseCoordinates(location.getBaseName(), location.getBasePosition());
    }

    public static String formatBaseCoordinates(String name, BlockPos position) {
        return name + " - " + Localization.translate("coords") + ": "
            + position.getX() + " " + position.getY() + " " + position.getZ();
    }

    /**
     * Uses the same camera-relative outline/tracer path as the client's existing
     * Storage ESP renderer. World-space positions are converted once using the
     * camera from the current Render3DEvent, then everything is flushed together.
     */
    private static final class OverlayFrame {
        private final Render3DEvent event;
        private final Map<ColorRGBA, List<Box>> boxesByColor = new HashMap<>();
        private final Map<ColorRGBA, List<Vec3d>> tracersByColor = new HashMap<>();

        private OverlayFrame(Render3DEvent event) {
            this.event = event;
        }

        private void addBox(Box box, ColorRGBA color) {
            if (box == null || color == null || color.getAlpha() <= 0.0f) {
                return;
            }
            this.boxesByColor.computeIfAbsent(color, ignored -> new ArrayList<>()).add(box);
        }

        private void addBoxes(Iterable<Box> boxes, ColorRGBA color) {
            if (boxes == null) {
                return;
            }
            for (Box box : boxes) {
                this.addBox(box, color);
            }
        }

        private void addTracer(Vec3d target, ColorRGBA color) {
            if (target == null || color == null || color.getAlpha() <= 0.0f) {
                return;
            }
            this.tracersByColor.computeIfAbsent(color, ignored -> new ArrayList<>()).add(target);
        }

        private void flush() {
            if (this.boxesByColor.isEmpty() && this.tracersByColor.isEmpty()) {
                return;
            }

            Vec3d cameraPosition = this.event.getCamera().getPos();
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.depthMask(false);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(
                VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            for (Map.Entry<ColorRGBA, List<Box>> entry : this.boxesByColor.entrySet()) {
                ColorRGBA color = entry.getKey();
                for (Box box : entry.getValue()) {
                    Box cameraRelativeBox = box.offset(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
                    RenderUtils.drawBoxOutline(this.event.getMatrices(), buffer, cameraRelativeBox, color);
                }
            }
            for (Map.Entry<ColorRGBA, List<Vec3d>> entry : this.tracersByColor.entrySet()) {
                for (Vec3d target : entry.getValue()) {
                    RenderUtils.drawWorldLineToPoint(this.event.getMatrices(), buffer, target, entry.getKey());
                }
            }

            ItemRenderUtils.flushVertexConsumer(buffer);
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    private enum SuspiciousType {
        AMETHYST_CLUSTER,
        DEEPSLATE,
        ROTATED_DEEPSLATE,
        VINE
    }

    private static final class SuspiciousScan {
        private final Map<BlockPos, SuspiciousType> suspiciousBlocks;
        private final boolean kelpChunk;

        private SuspiciousScan(Map<BlockPos, SuspiciousType> suspiciousBlocks, boolean kelpChunk) {
            this.suspiciousBlocks = suspiciousBlocks;
            this.kelpChunk = kelpChunk;
        }
    }

    private static final class HoleScan {
        private final Set<Hole> holes1x1;
        private final Set<Hole> holes3x1;

        private HoleScan(Set<Hole> holes1x1, Set<Hole> holes3x1) {
            this.holes1x1 = holes1x1;
            this.holes3x1 = holes3x1;
        }
    }

    /** Direct equivalent of Krypton's ChunkScan record. */
    private static final class ChunkScan {
        private final int selfHeat;
        private final boolean hasUngrown;
        private final Set<BlockPos> rotated;

        private ChunkScan(int selfHeat, boolean hasUngrown, Set<BlockPos> rotated) {
            this.selfHeat = selfHeat;
            this.hasUngrown = hasUngrown;
            this.rotated = rotated;
        }
    }

    /** Direct equivalent of Krypton's Hole record, including record-style accessors. */
    private static final class Hole {
        private final int x;
        private final int z;
        private final int startY;
        private final int endY;
        private final int width;
        private final int length;

        private Hole(int x, int z, int startY, int endY, int width, int length) {
            this.x = x;
            this.z = z;
            this.startY = startY;
            this.endY = endY;
            this.width = width;
            this.length = length;
        }

        private int x() {
            return this.x;
        }

        private int z() {
            return this.z;
        }

        private int startY() {
            return this.startY;
        }

        private int endY() {
            return this.endY;
        }

        private int width() {
            return this.width;
        }

        private int length() {
            return this.length;
        }

        @Override
        public String toString() {
            return "Hole[x=" + this.x + ", z=" + this.z + ", startY=" + this.startY
                + ", endY=" + this.endY + ", width=" + this.width + ", length=" + this.length + "]";
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Hole other)) {
                return false;
            }
            return this.x == other.x && this.z == other.z && this.startY == other.startY
                && this.endY == other.endY && this.width == other.width && this.length == other.length;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.x, this.z, this.startY, this.endY, this.width, this.length);
        }
    }

    /**
     * Cached cross-chunk block lookup used by HoleESP. It deliberately returns
     * air for unloaded chunks/sections, exactly like HoleChunkRef in Krypton.
     */
    private static final class HoleChunkRef {
        private final ClientWorld world;
        private WorldChunk chunk;

        private HoleChunkRef(ClientWorld world, WorldChunk chunk) {
            this.world = world;
            this.chunk = chunk;
        }

        private BlockState getBlockState(int x, int y, int z) {
            if (y < this.world.getBottomY() || y >= this.world.getBottomY() + this.world.getHeight()) {
                return Blocks.AIR.getDefaultState();
            }
            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            WorldChunk resolved = this.chunk;
            if (resolved == null || resolved.getPos().x != chunkX || resolved.getPos().z != chunkZ) {
                resolved = this.world.getChunkManager().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            }
            if (resolved == null) {
                return Blocks.AIR.getDefaultState();
            }
            int sectionIndex = Math.floorDiv(y - resolved.getBottomY(), 16);
            ChunkSection[] sections = resolved.getSectionArray();
            if (sectionIndex < 0 || sectionIndex >= sections.length || sections[sectionIndex] == null) {
                return Blocks.AIR.getDefaultState();
            }
            this.chunk = resolved;
            return sections[sectionIndex].getBlockState(x & 15, y & 15, z & 15);
        }
    }

    /** Direct equivalent of Krypton's SeedChunkRef, sharing the same safe lookup rules. */
    private static final class SeedChunkRef {
        private final ClientWorld world;
        private WorldChunk chunk;

        private SeedChunkRef(ClientWorld world, WorldChunk chunk) {
            this.world = world;
            this.chunk = chunk;
        }

        private BlockState getBlockState(int x, int y, int z) {
            if (y < this.world.getBottomY() || y >= this.world.getBottomY() + this.world.getHeight()) {
                return Blocks.AIR.getDefaultState();
            }
            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            WorldChunk resolved = this.chunk;
            if (resolved == null || resolved.getPos().x != chunkX || resolved.getPos().z != chunkZ) {
                resolved = this.world.getChunkManager().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            }
            if (resolved == null) {
                return Blocks.AIR.getDefaultState();
            }
            int sectionIndex = Math.floorDiv(y - resolved.getBottomY(), 16);
            ChunkSection[] sections = resolved.getSectionArray();
            if (sectionIndex < 0 || sectionIndex >= sections.length || sections[sectionIndex] == null) {
                return Blocks.AIR.getDefaultState();
            }
            this.chunk = resolved;
            return sections[sectionIndex].getBlockState(x & 15, y & 15, z & 15);
        }
    }

    public static final class BaseLocation {
        private final String baseName;
        private final BlockPos basePosition;

        public BaseLocation(String baseName, BlockPos basePosition) {
            this.baseName = baseName;
            this.basePosition = basePosition;
        }

        public String getBaseName() {
            return this.baseName;
        }

        public BlockPos getBasePosition() {
            return this.basePosition;
        }

        @Override
        public String toString() {
            return this.baseName + "@" + this.basePosition;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof BaseLocation other)) {
                return false;
            }
            return Objects.equals(this.baseName, other.baseName)
                && Objects.equals(this.basePosition, other.basePosition);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.baseName, this.basePosition);
        }
    }
}
