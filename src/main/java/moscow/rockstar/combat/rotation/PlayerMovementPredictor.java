/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Vec3d
 *  net.minecraft.PlayerListEntry
 */
package moscow.rockstar.combat.rotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.network.PlayerListEntry;

public final class PlayerMovementPredictor {
    private static final int MAX_SAMPLES = 15;
    private static final double GRAVITY_ACCELERATION = -0.08;
    private static final double HORIZONTAL_DRAG = 0.99;
    private static final double VERTICAL_DRAG = 0.98;
    private static final double HORIZONTAL_VELOCITY_DAMPING = 0.99;
    private static final double LOOK_VECTOR_VERTICAL_CORRECTION = 0.06;
    private static final double VELOCITY_ALIGNMENT_FACTOR = 0.1;
    private static final long SAMPLE_RETENTION_MILLIS = 30000L;
    private static final Map<UUID, List<MovementSample>> samplesByEntity = new ConcurrentHashMap<UUID, List<MovementSample>>();
    private static final Map<UUID, PredictionState> samplingStatesByEntity = new ConcurrentHashMap<UUID, PredictionState>();
    private static final Map<UUID, Integer> predictionTicksByEntity = new ConcurrentHashMap<UUID, Integer>();
    private static long lastPruneTimeMillis = System.currentTimeMillis();

    public static Vec3d predictPlayerPosition(PlayerEntity class_16572) {
        if (class_16572 == null) {
            return Vec3d.ZERO;
        }
        PlayerMovementPredictor.recordEntitySample(class_16572);
        if (!PlayerMovementPredictor.hasReliableSamples(class_16572)) {
            return class_16572.getPos();
        }
        int n = PlayerMovementPredictor.getPredictionTickCount(class_16572);
        return PlayerMovementPredictor.extrapolatePosition(class_16572, n);
    }

    public static void recordEntitySample(PlayerEntity class_16572) {
        if (class_16572 == null) {
            return;
        }
        UUID uUID2 = class_16572.getUuid();
        long l = System.currentTimeMillis();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        double d = class_16572.distanceTo((Entity)client.player);
        MovementSample movementSample = new MovementSample(class_16572.getPos(), class_16572.getVelocity(), class_16572.getPitch(), class_16572.getYaw(), class_16572.isGliding(), l, d);
        List<MovementSample> list = samplesByEntity.computeIfAbsent(uUID2, uUID -> new ArrayList<>());
        list.add(movementSample);
        if (list.size() > 15) {
            list.removeFirst();
        }
        PlayerMovementPredictor.updateSamplingState(uUID2, movementSample);
        PlayerMovementPredictor.pruneExpiredSamples(l);
    }

    public static boolean hasReliableSamples(PlayerEntity class_16572) {
        if (!class_16572.isGliding()) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }
        UUID uUID = class_16572.getUuid();
        List<MovementSample> list = samplesByEntity.get(uUID);
        if (list == null || list.size() < 3) {
            return false;
        }
        boolean bl = PlayerMovementPredictor.samplesShowIncreasingDistance(list);
        boolean bl2 = PlayerMovementPredictor.movementMatchesViewDirection(class_16572, (PlayerEntity)client.player);
        boolean bl3 = PlayerMovementPredictor.hasSufficientSpeed(class_16572);
        int n = 0;
        if (bl) {
            ++n;
        }
        if (bl2) {
            ++n;
        }
        if (bl3) {
            ++n;
        }
        return n >= 2;
    }

    private static boolean samplesShowIncreasingDistance(List<MovementSample> list) {
        if (list.size() < 3) {
            return false;
        }
        int n = Math.min(5, list.size());
        List<MovementSample> list2 = list.subList(list.size() - n, list.size());
        int n2 = 0;
        for (int i = 1; i < list2.size(); ++i) {
            if (!(list2.get((int)i).distanceFromClient > list2.get((int)(i - 1)).distanceFromClient)) continue;
            ++n2;
        }
        return n2 >= (list2.size() - 1) / 2;
    }

    private static boolean movementMatchesViewDirection(PlayerEntity class_16572, PlayerEntity class_16573) {
        Vec3d VanillaChestLootTableGenerator;
        Vec3d WallPlayerSkullBlock = class_16572.getPos();
        Vec3d VanillaEntityLootTableGenerator = class_16573.getPos();
        Vec3d PlayerSkullBlock = class_16572.getVelocity();
        Vec3d RedstoneBlock = WallPlayerSkullBlock.subtract(VanillaEntityLootTableGenerator).normalize();
        double d = RedstoneBlock.dotProduct(VanillaChestLootTableGenerator = PlayerSkullBlock.normalize());
        return d > 0.3;
    }

    private static boolean hasSufficientSpeed(PlayerEntity class_16572) {
        double d = class_16572.getVelocity().length();
        return d > 0.8;
    }

    private static Vec3d extrapolatePosition(PlayerEntity class_16572, int n) {
        Vec3d VanillaChestLootTableGenerator = class_16572.getPos();
        Vec3d WallPlayerSkullBlock = class_16572.getVelocity();
        float f = class_16572.getPitch();
        float f2 = class_16572.getYaw();
        boolean bl = class_16572.isGliding();
        for (int i = 0; i < n; ++i) {
            if (bl) {
                VanillaChestLootTableGenerator = PlayerMovementPredictor.advanceGlidingPosition(VanillaChestLootTableGenerator, WallPlayerSkullBlock, f, f2);
                WallPlayerSkullBlock = PlayerMovementPredictor.simulateFlightStep(WallPlayerSkullBlock, f, f2);
                continue;
            }
            WallPlayerSkullBlock = WallPlayerSkullBlock.add(0.0, -0.08, 0.0).multiply(0.98);
            VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock);
        }
        return VanillaChestLootTableGenerator;
    }

    private static Vec3d advanceGlidingPosition(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, float f, float f2) {
        return VanillaChestLootTableGenerator.add(WallPlayerSkullBlock);
    }

    private static Vec3d simulateFlightStep(Vec3d VanillaChestLootTableGenerator, float f, float f2) {
        double d;
        double d2 = VanillaChestLootTableGenerator.x;
        double d3 = VanillaChestLootTableGenerator.y;
        double d4 = VanillaChestLootTableGenerator.z;
        float f3 = (float)Math.toRadians(f);
        float f4 = (float)Math.toRadians(f2);
        Vec3d WallPlayerSkullBlock = new Vec3d(-Math.sin(f4) * Math.cos(f3), -Math.sin(f3), Math.cos(f4) * Math.cos(f3));
        double d5 = Math.sqrt(d2 * d2 + d4 * d4);
        double d6 = Math.sqrt(WallPlayerSkullBlock.x * WallPlayerSkullBlock.x + WallPlayerSkullBlock.z * WallPlayerSkullBlock.z);
        float f5 = (float)Math.cos(f3);
        float f6 = f5 * f5;
        if ((d3 += -0.08 + (double)f6 * 0.06) < 0.0 && d6 > 0.0) {
            d = d3 * -0.1 * (double)f6;
            d3 += d;
            d2 += WallPlayerSkullBlock.x * d / d6;
            d4 += WallPlayerSkullBlock.z * d / d6;
        }
        if (f < 0.0f && d6 > 0.0) {
            d = d5 * -Math.sin(f3) * 0.04;
            d3 += d * 3.2;
            d2 -= WallPlayerSkullBlock.x * d / d6;
            d4 -= WallPlayerSkullBlock.z * d / d6;
        }
        if (d6 > 0.0) {
            d2 += (WallPlayerSkullBlock.x / d6 * d5 - d2) * 0.1;
            d4 += (WallPlayerSkullBlock.z / d6 * d5 - d4) * 0.1;
        }
        return new Vec3d(d2 *= 0.99, d3 *= 0.98, d4 *= 0.99);
    }

    private static int getPredictionTickCount(PlayerEntity class_16572) {
        UUID uUID = class_16572.getUuid();
        if (predictionTicksByEntity.containsKey(uUID)) {
            return predictionTicksByEntity.get(uUID);
        }
        int n = PlayerMovementPredictor.estimateNetworkDelayTicks(class_16572);
        List<MovementSample> list = samplesByEntity.get(uUID);
        if (list == null || list.size() < 3) {
            return n;
        }
        double d = PlayerMovementPredictor.calculateSpeedVariance(list);
        double d2 = PlayerMovementPredictor.calculateAverageRotationChange(list);
        if (class_16572.isGliding()) {
            double d3 = class_16572.getVelocity().length();
            if (d3 > 2.0) {
                n += Math.min(4, (int)(d3 * 1.2));
            }
            if (d2 > 30.0) {
                n += 2;
            }
        }
        return Math.max(1, Math.min(15, n));
    }

    private static int estimateNetworkDelayTicks(PlayerEntity class_16572) {
        MinecraftClient client = MinecraftClient.getInstance();
        int n = 100;
        if (client.getNetworkHandler() != null) {
            try {
                PlayerListEntry ServerSamplerSource = client.getNetworkHandler().getPlayerListEntry(class_16572.getUuid());
                if (ServerSamplerSource != null) {
                    n = ServerSamplerSource.getLatency();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        int n2 = Math.max(1, n / 50);
        int n3 = 2;
        return n2 + n3;
    }

    private static double calculateSpeedVariance(List<MovementSample> list) {
        if (list.size() < 2) {
            return 0.0;
        }
        double[] dArray = list.stream().mapToDouble(movementSample -> movementSample.velocity.length()).toArray();
        double d = Arrays.stream(dArray).average().orElse(0.0);
        double d3 = Arrays.stream(dArray).map(d2 -> Math.pow(d2 - d, 2.0)).average().orElse(0.0);
        return Math.sqrt(d3);
    }

    private static double calculateAverageRotationChange(List<MovementSample> list) {
        if (list.size() < 2) {
            return 0.0;
        }
        double d = 0.0;
        for (int i = 1; i < list.size(); ++i) {
            MovementSample movementSample = list.get(i - 1);
            MovementSample movementSample2 = list.get(i);
            double d2 = Math.abs(movementSample2.yaw - movementSample.yaw);
            double d3 = Math.abs(movementSample2.pitch - movementSample.pitch);
            if (d2 > 180.0) {
                d2 = 360.0 - d2;
            }
            d += Math.sqrt(d2 * d2 + d3 * d3);
        }
        return d / (double)(list.size() - 1);
    }

    private static void updateSamplingState(UUID uUID2, MovementSample movementSample) {
        PredictionState predictionState = samplingStatesByEntity.computeIfAbsent(uUID2, uUID -> new PredictionState());
        predictionState.recordSample(movementSample);
    }

    private static void pruneExpiredSamples(long l) {
        if (l - lastPruneTimeMillis < 30000L) {
            return;
        }
        lastPruneTimeMillis = l;
        samplesByEntity.entrySet().removeIf(entry -> {
            List<MovementSample> list = (List<MovementSample>)entry.getValue();
            list.removeIf(movementSample -> l - movementSample.recordedAt > 30000L);
            return list.isEmpty();
        });
        samplingStatesByEntity.entrySet().removeIf(entry -> l - ((PredictionState)entry.getValue()).lastUpdatedAtMillis > 30000L);
    }

    @Generated
    private PlayerMovementPredictor() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static class MovementSample {
        public final Vec3d position;
        public final Vec3d velocity;
        public final float pitch;
        public final float yaw;
        public final boolean gliding;
        public final long recordedAt;
        public final double distanceFromClient;

        public MovementSample(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, float f, float f2, boolean bl, long l, double d) {
            this.position = VanillaChestLootTableGenerator;
            this.velocity = WallPlayerSkullBlock;
            this.pitch = f;
            this.yaw = f2;
            this.gliding = bl;
            this.recordedAt = l;
            this.distanceFromClient = d;
        }
    }

    static class PredictionState {
        private double averageSpeed = 0.0;
        private int sampleCount = 0;
        long lastUpdatedAtMillis = System.currentTimeMillis();

        PredictionState() {
        }

        public void recordSample(MovementSample movementSample) {
            double d = movementSample.velocity.length();
            this.averageSpeed = (this.averageSpeed * (double)this.sampleCount + d) / (double)(this.sampleCount + 1);
            ++this.sampleCount;
            this.lastUpdatedAtMillis = System.currentTimeMillis();
        }
    }
}
