/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.World
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.ClientPlayerEntity
 */
package moscow.rockstar.combat.rotation;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.physics.MovementSimulator;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.network.ClientPlayerEntity;

public final class AimTrajectorySampler {
    private static final double JUMP_BRANCH_WEIGHT = 0.12;
    private static final double PREDICTION_TICK_SCALE = 8.0;
    private static final int MAX_BRANCHES_PER_STEP = 16;

    private AimTrajectorySampler() {
    }

    public static TrajectoryResult predictAimTrajectory(Entity class_12972, Vec3d VanillaChestLootTableGenerator, int n, Vec3d WallPlayerSkullBlock, double d, TrajectorySettings trajectorySettings) {
        Vec3d VanillaEntityLootTableGenerator;
        double d2;
        double d3;
        List<TrajectoryBranch> list;
        int n2;
        World class_19372 = class_12972.getWorld();
        Vec3d PlayerSkullBlock = class_12972.getPos();
        boolean bl = class_12972.isOnGround();
        boolean bl2 = class_12972.isSneaking();
        boolean bl3 = class_12972.isSprinting();
        double d4 = class_12972.getWidth();
        double d5 = class_12972.getHeight();
        double d6 = Math.hypot(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.z);
        boolean bl4 = d6 > 0.048;
        float f = bl4 ? (float)Math.toDegrees(Math.atan2(-VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.z)) : class_12972.getYaw();
        int n3 = Math.max(1, trajectorySettings.getDirectionSamples());
        int n4 = Math.max(1, trajectorySettings.getBranchInterval());
        ArrayList<Vec3d> arrayList = new ArrayList<Vec3d>();
        arrayList.add(PlayerSkullBlock);
        List<TrajectoryBranch> list2 = new ArrayList<TrajectoryBranch>();
        list2.add(new TrajectoryBranch(new MovementSimulator(PlayerSkullBlock, VanillaChestLootTableGenerator, bl, bl3, bl2, f, class_19372), 1.0, f, arrayList));
        boolean bl5 = true;
        for (int i = 0; i < trajectorySettings.getPredictionTicks(); i += n2) {
            n2 = Math.min(n4, trajectorySettings.getPredictionTicks() - i);
            list2 = AimTrajectorySampler.retainHighestProbabilityBranches(list2, 16);
            list = new ArrayList<TrajectoryBranch>();
            for (TrajectoryBranch object2 : list2) {
                int n5;
                double d7 = Math.hypot(object2.motionState.velocity.x, object2.motionState.velocity.z);
                double d8 = bl5 ? Math.min(0.9, 0.05 + 0.85 * AimTrajectorySampler.clampProbability((double)n / 8.0)) : 0.06;
                double d9 = 1.0 - d8;
                boolean bl6 = trajectorySettings.isJumpPredictionEnabled() && object2.motionState.onGround;
                double d10 = bl6 ? d9 * 0.12 : 0.0;
                d3 = d9 - d10;
                d2 = 1.5 + d7 * 10.0;
                double[] dArray = new double[n3];
                double d11 = 0.0;
                for (n5 = 0; n5 < n3; ++n5) {
                    double d12 = (double)n5 * (Math.PI * 2 / (double)n3);
                    dArray[n5] = Math.exp(d2 * Math.cos(d12));
                    d11 += dArray[n5];
                }
                list.add(AimTrajectorySampler.advanceTrajectoryBranch(object2, n2, object2.yaw, BranchType.STATIONARY, object2.probability * d8, d4, d5));
                for (n5 = 0; n5 < n3; ++n5) {
                    float f2 = object2.yaw + (float)n5 * (360.0f / (float)n3);
                    list.add(AimTrajectorySampler.advanceTrajectoryBranch(object2, n2, f2, BranchType.FORWARD, object2.probability * d3 * dArray[n5] / d11, d4, d5));
                }
                if (!bl6) continue;
                list.add(AimTrajectorySampler.advanceTrajectoryBranch(object2, n2, object2.yaw, BranchType.JUMP, object2.probability * d10, d4, d5));
            }
            list = AimTrajectorySampler.retainHighestProbabilityBranches(list, trajectorySettings.getMaxBranches());
            AimTrajectorySampler.normalizeBranchProbabilities(list);
            list2 = list;
            bl5 = false;
        }
        ArrayList<TrajectoryCandidate> arrayList2 = new ArrayList<TrajectoryCandidate>(list2.size());
        TrajectoryBranch highestProbabilityBranch = null;
        for (TrajectoryBranch trajectoryBranch : list2) {
            if (highestProbabilityBranch != null && !(trajectoryBranch.probability > highestProbabilityBranch.probability)) continue;
            highestProbabilityBranch = trajectoryBranch;
        }
        ArrayList arrayList3 = new ArrayList(list2.size());
        double[] dArray = new double[list2.size()];
        Vec3d RedstoneBlock = PlayerSkullBlock;
        for (int i = 0; i < list2.size(); ++i) {
            TrajectoryBranch trajectoryBranch = list2.get(i);
            Box Vec3i = AimTrajectorySampler.createEntityBounds(trajectoryBranch.motionState.position, d4, d5);
            boolean bl7 = trajectoryBranch == highestProbabilityBranch;
            arrayList2.add(new TrajectoryCandidate(trajectoryBranch.trajectoryPoints, trajectoryBranch.motionState.position, Vec3i, trajectoryBranch.probability, bl7));
            arrayList3.add(Vec3i);
            dArray[i] = trajectoryBranch.probability;
            if (!bl7) continue;
            RedstoneBlock = Vec3i.getCenter();
        }
        Vec3d VanillaFishingLootTableGenerator = RedstoneBlock;
        double d13 = -1.0;
        double d14 = Double.MAX_VALUE;
        Iterator iterator = arrayList3.iterator();
        while (iterator.hasNext()) {
            Box HorizontalFacingBlock = (Box)iterator.next();
            VanillaEntityLootTableGenerator = HorizontalFacingBlock.getCenter();
            d3 = AimTrajectorySampler.calculateLineOfSightCoverage(WallPlayerSkullBlock, VanillaEntityLootTableGenerator, arrayList3, dArray, trajectorySettings.getRayRange());
            d2 = VanillaEntityLootTableGenerator.squaredDistanceTo(RedstoneBlock);
            if (!(d3 > d13 + 1.0E-6) && (!(Math.abs(d3 - d13) <= 1.0E-6) || !(d2 < d14))) continue;
            d13 = d3;
            d14 = d2;
            VanillaFishingLootTableGenerator = VanillaEntityLootTableGenerator;
        }
        double d15 = AimTrajectorySampler.clampProbability(d);
        VanillaEntityLootTableGenerator = RedstoneBlock.add(VanillaFishingLootTableGenerator.subtract(RedstoneBlock).multiply(d15));
        d3 = AimTrajectorySampler.calculateLineOfSightCoverage(WallPlayerSkullBlock, VanillaEntityLootTableGenerator, arrayList3, dArray, trajectorySettings.getRayRange());
        return new TrajectoryResult(arrayList2, RedstoneBlock, VanillaFishingLootTableGenerator, VanillaEntityLootTableGenerator, d3, arrayList3.size(), trajectorySettings.getPredictionTicks(), WallPlayerSkullBlock);
    }

    public static Vec3d predictPlayerPosition(int n) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity class_7462 = client.player;
        if (class_7462 == null || client.world == null) {
            return null;
        }
        if (n <= 0) {
            return class_7462.getEyePos();
        }
        Vec3d VanillaChestLootTableGenerator = new Vec3d(class_7462.getX() - class_7462.prevX, class_7462.getY() - class_7462.prevY, class_7462.getZ() - class_7462.prevZ);
        double d = Math.hypot(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.z);
        boolean bl = d > 0.048;
        float f = bl ? (float)Math.toDegrees(Math.atan2(-VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.z)) : class_7462.getYaw();
        double d2 = class_7462.getEyePos().y - class_7462.getY();
        MovementSimulator movementSimulator = new MovementSimulator(class_7462.getPos(), VanillaChestLootTableGenerator, class_7462.isOnGround(), class_7462.isSprinting(), class_7462.isSneaking(), f, (World)client.world);
        MovementSimulator.MovementInput movementInput = bl ? MovementSimulator.MovementInput.WALK_FORWARD : MovementSimulator.MovementInput.NO_INPUT;
        for (int i = 0; i < n; ++i) {
            movementSimulator.simulateTick(movementInput);
        }
        return movementSimulator.position.add(0.0, d2, 0.0);
    }

    private static TrajectoryBranch advanceTrajectoryBranch(TrajectoryBranch trajectoryBranch, int n, float f, BranchType branchType, double d, double d2, double d3) {
        TrajectoryBranch trajectoryBranch2 = trajectoryBranch.copy();
        trajectoryBranch2.probability = d;
        trajectoryBranch2.yaw = f;
        trajectoryBranch2.motionState.yaw = f;
        for (int i = 0; i < n; ++i) {
            MovementSimulator.MovementInput movementInput = switch (branchType.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> MovementSimulator.MovementInput.NO_INPUT;
                case 2 -> {
                    if (i == 0 && trajectoryBranch2.motionState.onGround) {
                        yield MovementSimulator.MovementInput.JUMP_FORWARD;
                    }
                    yield MovementSimulator.MovementInput.WALK_FORWARD;
                }
                case 1 -> MovementSimulator.MovementInput.WALK_FORWARD;
            };
            trajectoryBranch2.motionState.simulateTick(movementInput);
            trajectoryBranch2.trajectoryPoints.add(trajectoryBranch2.motionState.position);
        }
        return trajectoryBranch2;
    }

    private static double calculateLineOfSightCoverage(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, List<Box> list, double[] dArray, double d) {
        Vec3d VanillaEntityLootTableGenerator = WallPlayerSkullBlock.subtract(VanillaChestLootTableGenerator);
        if (VanillaEntityLootTableGenerator.lengthSquared() < 1.0E-9) {
            return 0.0;
        }
        Vec3d PlayerSkullBlock = VanillaChestLootTableGenerator.add(VanillaEntityLootTableGenerator.normalize().multiply(d));
        double d2 = 0.0;
        for (int i = 0; i < list.size(); ++i) {
            if (!list.get(i).raycast(VanillaChestLootTableGenerator, PlayerSkullBlock).isPresent()) continue;
            d2 += dArray[i];
        }
        return d2;
    }

    private static List<TrajectoryBranch> retainHighestProbabilityBranches(List<TrajectoryBranch> list, int n) {
        if (list.size() <= n) {
            return list;
        }
        list.sort(Comparator.comparingDouble((TrajectoryBranch branch) -> branch.probability).reversed());
        return new ArrayList<TrajectoryBranch>(list.subList(0, n));
    }

    private static void normalizeBranchProbabilities(List<TrajectoryBranch> list) {
        double d = 0.0;
        for (TrajectoryBranch trajectoryBranch : list) {
            d += trajectoryBranch.probability;
        }
        if (d <= 0.0) {
            return;
        }
        for (TrajectoryBranch trajectoryBranch : list) {
            trajectoryBranch.probability /= d;
        }
    }

    private static Box createEntityBounds(Vec3d VanillaChestLootTableGenerator, double d, double d2) {
        double d3 = d / 2.0;
        return new Box(VanillaChestLootTableGenerator.x - d3, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z - d3, VanillaChestLootTableGenerator.x + d3, VanillaChestLootTableGenerator.y + d2, VanillaChestLootTableGenerator.z + d3);
    }

    private static double clampProbability(double d) {
        return d < 0.0 ? 0.0 : Math.min(d, 1.0);
    }

    public static final class TrajectorySettings {
        private final int predictionTicks;
        private final int directionSamples;
        private final int branchInterval;
        private final boolean allowJump;
        private final double rayRange;
        private final int maxBranches;

        public TrajectorySettings(int n, int n2, int n3, boolean bl, double d, int n4) {
            this.predictionTicks = n;
            this.directionSamples = n2;
            this.branchInterval = n3;
            this.allowJump = bl;
            this.rayRange = d;
            this.maxBranches = n4;
        }

        public static TrajectorySettings defaultSettings() {
            return new TrajectorySettings(10, 8, 4, true, 64.0, 96);
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "predictionTicks", "directionSamples", "branchInterval", "allowJump", "rayRange", "maxBranches");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "predictionTicks", "directionSamples", "branchInterval", "allowJump", "rayRange", "maxBranches");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "predictionTicks", "directionSamples", "branchInterval", "allowJump", "rayRange", "maxBranches");
        }

        public int getPredictionTicks() {
            return this.predictionTicks;
        }

        public int getDirectionSamples() {
            return this.directionSamples;
        }

        public int getBranchInterval() {
            return this.branchInterval;
        }

        public boolean isJumpPredictionEnabled() {
            return this.allowJump;
        }

        public double getRayRange() {
            return this.rayRange;
        }

        public int getMaxBranches() {
            return this.maxBranches;
        }
    }

    static final class TrajectoryBranch {
        MovementSimulator motionState;
        double probability;
        float yaw;
        List<Vec3d> trajectoryPoints;

        TrajectoryBranch(MovementSimulator movementSimulator, double d, float f, List<Vec3d> list) {
            this.motionState = movementSimulator;
            this.probability = d;
            this.yaw = f;
            this.trajectoryPoints = list;
        }

        TrajectoryBranch copy() {
            return new TrajectoryBranch(this.motionState.copy(), this.probability, this.yaw, new ArrayList<Vec3d>(this.trajectoryPoints));
        }
    }

    static enum BranchType {
        STATIONARY,
        FORWARD,
        JUMP;
}

    public static final class TrajectoryCandidate {
        private final List<Vec3d> trajectoryPoints;
        private final Vec3d endPosition;
        private final Box endBounds;
        private final double probability;
        private final boolean mostLikely;

        public TrajectoryCandidate(List<Vec3d> list, Vec3d VanillaChestLootTableGenerator, Box Vec3i, double d, boolean bl) {
            this.trajectoryPoints = list;
            this.endPosition = VanillaChestLootTableGenerator;
            this.endBounds = Vec3i;
            this.probability = d;
            this.mostLikely = bl;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "trajectoryPoints", "endPosition", "endBounds", "probability", "mostLikely");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "trajectoryPoints", "endPosition", "endBounds", "probability", "mostLikely");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "trajectoryPoints", "endPosition", "endBounds", "probability", "mostLikely");
        }

        public List<Vec3d> getTrajectoryPoints() {
            return this.trajectoryPoints;
        }

        public Vec3d getEndPosition() {
            return this.endPosition;
        }

        public Box getEndBounds() {
            return this.endBounds;
        }

        public double getProbability() {
            return this.probability;
        }

        public boolean isMostLikely() {
            return this.mostLikely;
        }
    }

    public static final class TrajectoryResult {
        private final List<TrajectoryCandidate> candidates;
        private final Vec3d mostLikelyPoint;
        private final Vec3d maximumCoveragePoint;
        private final Vec3d aimPoint;
        private final double coverage;
        private final int variantCount;
        private final int predictionTicks;
        private final Vec3d eyePosition;

        public TrajectoryResult(List<TrajectoryCandidate> list, Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, Vec3d VanillaEntityLootTableGenerator, double d, int n, int n2, Vec3d PlayerSkullBlock) {
            this.candidates = list;
            this.mostLikelyPoint = VanillaChestLootTableGenerator;
            this.maximumCoveragePoint = WallPlayerSkullBlock;
            this.aimPoint = VanillaEntityLootTableGenerator;
            this.coverage = d;
            this.variantCount = n;
            this.predictionTicks = n2;
            this.eyePosition = PlayerSkullBlock;
        }

        public double getCoveragePercent() {
            return this.coverage * 100.0;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "candidates", "mostLikelyPoint", "maximumCoveragePoint", "aimPoint", "coverage", "variantCount", "predictionTicks", "eyePosition");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "candidates", "mostLikelyPoint", "maximumCoveragePoint", "aimPoint", "coverage", "variantCount", "predictionTicks", "eyePosition");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "candidates", "mostLikelyPoint", "maximumCoveragePoint", "aimPoint", "coverage", "variantCount", "predictionTicks", "eyePosition");
        }

        public List<TrajectoryCandidate> getCandidates() {
            return this.candidates;
        }

        public Vec3d getMostLikelyPoint() {
            return this.mostLikelyPoint;
        }

        public Vec3d getMaximumCoveragePoint() {
            return this.maximumCoveragePoint;
        }

        public Vec3d getAimPoint() {
            return this.aimPoint;
        }

        public double getCoverage() {
            return this.coverage;
        }

        public int getVariantCount() {
            return this.variantCount;
        }

        public int getPredictionTicks() {
            return this.predictionTicks;
        }

        public Vec3d getEyePosition() {
            return this.eyePosition;
        }
    }

    public static final class MotionHistory {
        private static final int MAX_HISTORY_LENGTH = 20;
        private static final double STATIONARY_SPEED_THRESHOLD = 0.02;
        private int trackedEntityId = -1;
        private final Deque<Vec3d> positionHistory = new ArrayDeque<Vec3d>();
        private Vec3d deltaPosition = Vec3d.ZERO;
        private int stationaryTicks = 0;

        public void recordPosition(Entity class_12972) {
            if (class_12972 == null) {
                this.reset();
                return;
            }
            if (class_12972.getId() != this.trackedEntityId) {
                this.trackedEntityId = class_12972.getId();
                this.positionHistory.clear();
                this.stationaryTicks = 0;
                this.deltaPosition = Vec3d.ZERO;
            }
            Vec3d VanillaChestLootTableGenerator = class_12972.getPos();
            if (!this.positionHistory.isEmpty()) {
                this.deltaPosition = VanillaChestLootTableGenerator.subtract(this.positionHistory.peekLast());
                double d = Math.hypot(this.deltaPosition.x, this.deltaPosition.z);
                this.stationaryTicks = d < 0.02 ? ++this.stationaryTicks : 0;
            }
            this.positionHistory.addLast(VanillaChestLootTableGenerator);
            while (this.positionHistory.size() > 20) {
                this.positionHistory.removeFirst();
            }
        }

        public void reset() {
            this.trackedEntityId = -1;
            this.positionHistory.clear();
            this.deltaPosition = Vec3d.ZERO;
            this.stationaryTicks = 0;
        }

        public Vec3d getDeltaPosition() {
            return this.deltaPosition;
        }

        public Vec3d getAverageDelta() {
            if (this.positionHistory.size() < 2) {
                return this.deltaPosition;
            }
            Vec3d[] class_243Array = this.positionHistory.toArray(new Vec3d[0]);
            int n = class_243Array.length;
            int n2 = Math.min(5, n - 1);
            Vec3d VanillaChestLootTableGenerator = Vec3d.ZERO;
            for (int i = n - n2; i < n; ++i) {
                VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add(class_243Array[i].subtract(class_243Array[i - 1]));
            }
            return VanillaChestLootTableGenerator.multiply(1.0 / (double)n2);
        }

        public int getStationaryTicks() {
            return this.stationaryTicks;
        }
    }
}

