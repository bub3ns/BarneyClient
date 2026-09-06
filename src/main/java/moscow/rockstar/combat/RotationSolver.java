/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import lombok.Generated;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class RotationSolver {
    private static final double FULL_ROTATION_RADIANS = Math.PI * 2;
    private static final Random RANDOM = new Random();
    private final List<AimPointCandidate> aimPointCandidates = new ArrayList<AimPointCandidate>();
    private int candidateCount = 0;
    private long candidateGeneratedAt = 0L;
    private float baseJitterAmount = 0.12f;
    private TargetMovementCategory movementClass = TargetMovementCategory.STANDARD;
    private MotionPattern motionPattern = MotionPattern.STATIONARY;
    private RotationIntensityTier intensityTier = RotationIntensityTier.MEDIUM;
    private final Vec3d[] positionHistory = new Vec3d[15];
    private int historyWriteIndex = 0;
    private int historySampleCount = 0;
    private int baseHistorySampleCount = 8;
    private float baseAimAdjustment = 0.6f;
    private boolean adaptiveMode = true;
    private int effectiveHistorySampleCount = 8;
    private float effectiveAimAdjustment = 0.6f;
    private float effectiveJitterAmount = 0.12f;

    public void configureSampling(int n, float f, boolean bl) {
        this.baseHistorySampleCount = n;
        this.baseAimAdjustment = f;
        this.adaptiveMode = bl;
    }

    public void setBaseJitterAmount(float f) {
        this.baseJitterAmount = MathHelper.clamp((float)f, (float)0.05f, (float)0.5f);
    }

    public void generateAimPointCandidates(Vec3d VanillaChestLootTableGenerator, LivingEntity class_13092, Vec3d WallPlayerSkullBlock, float f) {
        if (class_13092 == null || VanillaChestLootTableGenerator == null) {
            this.aimPointCandidates.clear();
            this.effectiveHistorySampleCount = 0;
            return;
        }
        this.movementClass = this.classifyTargetMovement(class_13092, WallPlayerSkullBlock);
        this.recordTargetPosition(class_13092.getPos());
        this.motionPattern = this.classifyMotionPattern();
        this.intensityTier = this.classifyRotationIntensity(f);
        this.calculateEffectiveSettings();
        this.aimPointCandidates.clear();
        this.candidateCount = 0;
        this.candidateGeneratedAt = System.currentTimeMillis();
        this.populateAimPointCandidates(VanillaChestLootTableGenerator, class_13092, f);
        this.aimPointCandidates.sort((aimPointCandidate, aimPointCandidate2) -> Float.compare(aimPointCandidate2.score, aimPointCandidate.score));
    }

    private TargetMovementCategory classifyTargetMovement(LivingEntity class_13092, Vec3d VanillaChestLootTableGenerator) {
        PlayerEntity class_16572;
        if (class_13092 == null || class_13092.getWorld() == null) {
            return TargetMovementCategory.STANDARD;
        }
        if (class_13092.isTouchingWater() || class_13092.isInsideWaterOrBubbleColumn()) {
            return TargetMovementCategory.SWIMMING;
        }
        if (class_13092.isInLava() || class_13092.isInLava()) {
            return TargetMovementCategory.BURNING;
        }
        if (class_13092.isOnGround()) {
            return TargetMovementCategory.STANDARD;
        }
        if (class_13092 instanceof PlayerEntity && (class_16572 = (PlayerEntity)class_13092).isClimbing()) {
            return TargetMovementCategory.CREATIVE;
        }
        return TargetMovementCategory.AIRBORNE;
    }

    private void recordTargetPosition(Vec3d VanillaChestLootTableGenerator) {
        this.positionHistory[this.historyWriteIndex] = VanillaChestLootTableGenerator;
        this.historyWriteIndex = (this.historyWriteIndex + 1) % this.positionHistory.length;
        if (this.historySampleCount < this.positionHistory.length) {
            ++this.historySampleCount;
        }
    }

    private MotionPattern classifyMotionPattern() {
        double d;
        if (this.historySampleCount < 5) {
            return MotionPattern.STATIONARY;
        }
        ArrayList<Double> arrayList = new ArrayList<Double>();
        ArrayList<Double> arrayList2 = new ArrayList<Double>();
        for (int i = 1; i < Math.min(10, this.historySampleCount); ++i) {
            int n;
            int n2 = (this.historyWriteIndex - i + this.positionHistory.length) % this.positionHistory.length;
            int n3 = (this.historyWriteIndex - i - 1 + this.positionHistory.length) % this.positionHistory.length;
            if (this.positionHistory[n2] == null || this.positionHistory[n3] == null) continue;
            double d3 = this.positionHistory[n2].distanceTo(this.positionHistory[n3]);
            arrayList.add(d3);
            if (i < 2 || this.positionHistory[n = (this.historyWriteIndex - i - 2 + this.positionHistory.length) % this.positionHistory.length] == null) continue;
            Vec3d VanillaChestLootTableGenerator = this.positionHistory[n3].subtract(this.positionHistory[n]);
            Vec3d WallPlayerSkullBlock = this.positionHistory[n2].subtract(this.positionHistory[n3]);
            double d4 = Math.atan2(WallPlayerSkullBlock.z, WallPlayerSkullBlock.x) - Math.atan2(VanillaChestLootTableGenerator.z, VanillaChestLootTableGenerator.x);
            d4 = RotationSolver.wrapAngle(d4);
            arrayList2.add(Math.abs(d4));
        }
        if (arrayList.isEmpty()) {
            return MotionPattern.STATIONARY;
        }
        double d5 = arrayList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        if (d5 < 0.01) {
            return MotionPattern.STATIONARY;
        }
        if (arrayList2.size() >= 3) {
            double d6 = arrayList2.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double d7 = arrayList2.stream().mapToDouble(d2 -> Math.pow(d2 - d6, 2.0)).average().orElse(1.0);
            if (d7 < 0.15 && d6 > 0.05 && d6 < 0.5) {
                return MotionPattern.JITTERING;
            }
        }
        if (arrayList2.size() >= 3 && (d = arrayList2.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)) < 0.1) {
            return MotionPattern.SMOOTH;
        }
        double d8 = arrayList.stream().mapToDouble(d2 -> Math.pow(d2 - d5, 2.0)).average().orElse(1.0);
        if (d8 < 0.05) {
            return MotionPattern.ERRATIC;
        }
        return MotionPattern.CONSISTENT;
    }

    private RotationIntensityTier classifyRotationIntensity(float f) {
        if (f < 3.0f) {
            return RotationIntensityTier.LOW;
        }
        if (f < 6.0f) {
            return RotationIntensityTier.MEDIUM;
        }
        return RotationIntensityTier.HIGH;
    }

    private void calculateEffectiveSettings() {
        if (!this.adaptiveMode) {
            this.effectiveHistorySampleCount = this.baseHistorySampleCount;
            this.effectiveAimAdjustment = this.baseAimAdjustment;
            this.effectiveJitterAmount = this.baseJitterAmount;
            return;
        }
        int n = this.baseHistorySampleCount;
        float f = this.baseAimAdjustment;
        float f2 = this.baseJitterAmount;
        switch (this.movementClass.ordinal()) {
            case 2: {
                n = (int)((double)n * 0.7);
                f *= 0.6f;
                f2 *= 1.3f;
                break;
            }
            case 3: {
                n = (int)((double)n * 0.6);
                f *= 0.5f;
                f2 *= 1.5f;
                break;
            }
            case 1: {
                n = (int)((double)n * 1.2);
                f *= 1.1f;
                f2 *= 0.9f;
                break;
            }
            case 4: {
                n = (int)((double)n * 0.8);
                f *= 0.7f;
                f2 *= 1.2f;
            }
        }
        switch (this.motionPattern.ordinal()) {
            case 2: {
                n = (int)((double)n * 1.4);
                f *= 1.2f;
                f2 *= 0.8f;
                break;
            }
            case 1: {
                n = (int)((double)n * 1.1);
                f *= 0.9f;
                f2 *= 0.9f;
                break;
            }
            case 3: {
                n = (int)((double)n * 1.3);
                f *= 1.3f;
                f2 *= 0.7f;
                break;
            }
            case 4: {
                n = (int)((double)n * 0.8);
                f *= 0.8f;
                f2 *= 1.1f;
            }
        }
        switch (this.intensityTier.ordinal()) {
            case 0: {
                n = (int)((double)n * 1.3);
                f *= 0.7f;
                f2 *= 0.8f;
                break;
            }
            case 1: {
                break;
            }
            case 2: {
                n = (int)((double)n * 0.7);
                f *= 1.4f;
                f2 *= 1.2f;
            }
        }
        this.effectiveHistorySampleCount = MathHelper.clamp((int)n, (int)3, (int)50);
        this.effectiveAimAdjustment = MathHelper.clamp((float)f, (float)0.2f, (float)2.0f);
        this.effectiveJitterAmount = MathHelper.clamp((float)f2, (float)0.05f, (float)0.5f);
    }

    private void populateAimPointCandidates(Vec3d VanillaChestLootTableGenerator, LivingEntity class_13092, float f) {
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        double d = (double)class_13092.getWidth() / 2.0;
        double d2 = class_13092.getHeight();
        float f7 = switch (this.movementClass.ordinal()) {
            case 2 -> {
                f6 = 0.9f;
                f5 = 0.5f;
                f4 = 0.2f;
                f3 = 0.5f;
                f2 = 0.35f;
                yield 0.15f;
            }
            case 1 -> {
                f6 = 0.6f;
                f5 = 0.8f;
                f4 = 0.4f;
                f3 = 0.3f;
                f2 = 0.5f;
                yield 0.2f;
            }
            case 4 -> {
                f6 = 0.85f;
                f5 = 0.6f;
                f4 = 0.3f;
                f3 = 0.45f;
                f2 = 0.4f;
                yield 0.15f;
            }
            default -> {
                f6 = 0.95f;
                f5 = 0.7f;
                f4 = 0.35f;
                f3 = 0.4f;
                f2 = 0.4f;
                yield 0.2f;
            }
        };
        switch (this.motionPattern.ordinal()) {
            case 2: {
                this.addCircularAimCandidates(VanillaChestLootTableGenerator, d, d2, this.effectiveAimAdjustment, this.effectiveHistorySampleCount, f6, f5, f4, f3, f2, f7);
                break;
            }
            case 1: {
                this.addDirectionalAimCandidates(VanillaChestLootTableGenerator, d, d2, this.effectiveAimAdjustment, this.effectiveHistorySampleCount, f6, f5, f4, f3, f2, f7);
                break;
            }
            case 3: {
                this.addRandomAimCandidates(VanillaChestLootTableGenerator, d, d2, this.effectiveAimAdjustment, this.effectiveHistorySampleCount, f6, f5, f4, f3, f2, f7);
                break;
            }
            default: {
                this.addAdaptiveAimCandidates(VanillaChestLootTableGenerator, d, d2, this.effectiveAimAdjustment, this.effectiveHistorySampleCount, f6, f5, f4, f3, f2, f7);
            }
        }
    }

    private void addCircularAimCandidates(Vec3d VanillaChestLootTableGenerator, double d, double d2, float f, int n, float f2, float f3, float f4, float f5, float f6, float f7) {
        float f8;
        float f9;
        int n2;
        Vec3d WallPlayerSkullBlock = this.getRecentMovementDirection();
        int n3 = (int)((float)n * f5);
        int n4 = (int)((float)n * f6);
        int n5 = n - n3 - n4;
        for (n2 = 0; n2 < n3; ++n2) {
            if (WallPlayerSkullBlock != null && (double)n2 < (double)n3 * 0.7) {
                f9 = (float)Math.atan2(WallPlayerSkullBlock.z, WallPlayerSkullBlock.x);
                f8 = f9 + (float)((RANDOM.nextDouble() - 0.5) * 0.6);
            } else {
                f8 = (float)(RANDOM.nextDouble() * (Math.PI * 2));
            }
            f9 = (float)(d * (double)f * (0.3 + RANDOM.nextDouble() * 0.4));
            this.aimPointCandidates.add(new AimPointCandidate(VanillaChestLootTableGenerator.x + Math.cos(f8) * (double)f9, VanillaChestLootTableGenerator.y + d2 * (0.75 + RANDOM.nextDouble() * 0.2), VanillaChestLootTableGenerator.z + Math.sin(f8) * (double)f9, f2, 0.85f, this.movementClass));
        }
        for (n2 = 0; n2 < n4; ++n2) {
            if (WallPlayerSkullBlock != null && (double)n2 < (double)n4 * 0.6) {
                f9 = (float)Math.atan2(WallPlayerSkullBlock.z, WallPlayerSkullBlock.x);
                f8 = f9 + (float)((RANDOM.nextDouble() - 0.5) * 0.8);
            } else {
                f8 = (float)(RANDOM.nextDouble() * (Math.PI * 2));
            }
            f9 = (float)(d * (double)f * (0.4 + RANDOM.nextDouble() * 0.4));
            this.aimPointCandidates.add(new AimPointCandidate(VanillaChestLootTableGenerator.x + Math.cos(f8) * (double)f9, VanillaChestLootTableGenerator.y + d2 * (0.45 + RANDOM.nextDouble() * 0.25), VanillaChestLootTableGenerator.z + Math.sin(f8) * (double)f9, f3, 0.7f, this.movementClass));
        }
        for (n2 = 0; n2 < n5; ++n2) {
            f8 = (float)(RANDOM.nextDouble() * (Math.PI * 2));
            f9 = (float)(d * (double)f * RANDOM.nextDouble());
            this.aimPointCandidates.add(new AimPointCandidate(VanillaChestLootTableGenerator.x + Math.cos(f8) * (double)f9, VanillaChestLootTableGenerator.y + d2 * (0.15 + RANDOM.nextDouble() * 0.25), VanillaChestLootTableGenerator.z + Math.sin(f8) * (double)f9, f4, 0.4f, this.movementClass));
        }
    }

    private void addDirectionalAimCandidates(Vec3d VanillaChestLootTableGenerator, double d, double d2, float f, int n, float f2, float f3, float f4, float f5, float f6, float f7) {
        Vec3d WallPlayerSkullBlock = this.getRecentMovementDirection();
        if (WallPlayerSkullBlock == null) {
            WallPlayerSkullBlock = new Vec3d(1.0, 0.0, 0.0);
        }
        WallPlayerSkullBlock = WallPlayerSkullBlock.normalize();
        int n2 = (int)((float)n * f5);
        int n3 = (int)((float)n * f6);
        for (int i = 0; i < n; ++i) {
            float f8;
            float f9;
            float f10;
            float f11 = (float)i / (float)n;
            if (i < n2) {
                f10 = 0.75f + (float)(RANDOM.nextDouble() * 0.2);
                f9 = f2;
                f8 = 0.85f;
            } else if (i < n2 + n3) {
                f10 = 0.45f + (float)(RANDOM.nextDouble() * 0.25);
                f9 = f3;
                f8 = 0.7f;
            } else {
                f10 = 0.15f + (float)(RANDOM.nextDouble() * 0.25);
                f9 = f4;
                f8 = 0.4f;
            }
            float f12 = (float)((double)(f11 - 0.5f) * d * (double)f * 0.5);
            float f13 = (float)((RANDOM.nextDouble() - 0.5) * d * (double)f * 0.6);
            double d3 = VanillaChestLootTableGenerator.x + WallPlayerSkullBlock.x * (double)f12 - WallPlayerSkullBlock.z * (double)f13;
            double d4 = VanillaChestLootTableGenerator.z + WallPlayerSkullBlock.z * (double)f12 + WallPlayerSkullBlock.x * (double)f13;
            this.aimPointCandidates.add(new AimPointCandidate(d3, VanillaChestLootTableGenerator.y + d2 * (double)f10, d4, f9, f8, this.movementClass));
        }
    }

    private void addRandomAimCandidates(Vec3d VanillaChestLootTableGenerator, double d, double d2, float f, int n, float f2, float f3, float f4, float f5, float f6, float f7) {
        float f8;
        float f9;
        int n2;
        int n3 = (int)((float)n * f5);
        int n4 = (int)((float)n * f6);
        int n5 = n - n3 - n4;
        for (n2 = 0; n2 < n3; ++n2) {
            f9 = (float)(RANDOM.nextDouble() * (Math.PI * 2));
            f8 = (float)(d * (double)f * (0.2 + RANDOM.nextDouble() * 0.5));
            this.aimPointCandidates.add(new AimPointCandidate(VanillaChestLootTableGenerator.x + Math.cos(f9) * (double)f8, VanillaChestLootTableGenerator.y + d2 * (0.7 + RANDOM.nextDouble() * 0.25), VanillaChestLootTableGenerator.z + Math.sin(f9) * (double)f8, f2, 0.8f, this.movementClass));
        }
        for (n2 = 0; n2 < n4; ++n2) {
            f9 = (float)(RANDOM.nextDouble() * (Math.PI * 2));
            f8 = (float)(d * (double)f * (0.3 + RANDOM.nextDouble() * 0.5));
            this.aimPointCandidates.add(new AimPointCandidate(VanillaChestLootTableGenerator.x + Math.cos(f9) * (double)f8, VanillaChestLootTableGenerator.y + d2 * (0.4 + RANDOM.nextDouble() * 0.3), VanillaChestLootTableGenerator.z + Math.sin(f9) * (double)f8, f3, 0.65f, this.movementClass));
        }
        for (n2 = 0; n2 < n5; ++n2) {
            f9 = (float)(RANDOM.nextDouble() * (Math.PI * 2));
            f8 = (float)(d * (double)f * RANDOM.nextDouble());
            this.aimPointCandidates.add(new AimPointCandidate(VanillaChestLootTableGenerator.x + Math.cos(f9) * (double)f8, VanillaChestLootTableGenerator.y + d2 * (0.1 + RANDOM.nextDouble() * 0.3), VanillaChestLootTableGenerator.z + Math.sin(f9) * (double)f8, f4, 0.35f, this.movementClass));
        }
    }

    private void addAdaptiveAimCandidates(Vec3d VanillaChestLootTableGenerator, double d, double d2, float f, int n, float f2, float f3, float f4, float f5, float f6, float f7) {
        float f8;
        float f9;
        int n2;
        int n3 = (int)((float)n * f5);
        int n4 = (int)((float)n * f6);
        int n5 = n - n3 - n4;
        for (n2 = 0; n2 < n3; ++n2) {
            f9 = (float)(RANDOM.nextDouble() * (Math.PI * 2));
            f8 = (float)(d * (double)f * (0.15 + RANDOM.nextDouble() * 0.35));
            this.aimPointCandidates.add(new AimPointCandidate(VanillaChestLootTableGenerator.x + Math.cos(f9) * (double)f8, VanillaChestLootTableGenerator.y + d2 * (0.78 + RANDOM.nextDouble() * 0.18), VanillaChestLootTableGenerator.z + Math.sin(f9) * (double)f8, f2, 0.9f, this.movementClass));
        }
        for (n2 = 0; n2 < n4; ++n2) {
            f9 = (float)(RANDOM.nextDouble() * (Math.PI * 2));
            f8 = (float)(d * (double)f * (0.3 + RANDOM.nextDouble() * 0.5));
            this.aimPointCandidates.add(new AimPointCandidate(VanillaChestLootTableGenerator.x + Math.cos(f9) * (double)f8, VanillaChestLootTableGenerator.y + d2 * (0.42 + RANDOM.nextDouble() * 0.28), VanillaChestLootTableGenerator.z + Math.sin(f9) * (double)f8, f3, 0.7f, this.movementClass));
        }
        for (n2 = 0; n2 < n5; ++n2) {
            f9 = (float)(RANDOM.nextDouble() * (Math.PI * 2));
            f8 = (float)(d * (double)f * (0.2 + RANDOM.nextDouble() * 0.6));
            this.aimPointCandidates.add(new AimPointCandidate(VanillaChestLootTableGenerator.x + Math.cos(f9) * (double)f8, VanillaChestLootTableGenerator.y + d2 * (0.12 + RANDOM.nextDouble() * 0.28), VanillaChestLootTableGenerator.z + Math.sin(f9) * (double)f8, f4, 0.35f, this.movementClass));
        }
    }

    private Vec3d getRecentMovementDirection() {
        Vec3d VanillaChestLootTableGenerator;
        if (this.historySampleCount < 2) {
            return null;
        }
        Vec3d WallPlayerSkullBlock = this.positionHistory[(this.historyWriteIndex + 1) % this.historySampleCount];
        Vec3d VanillaEntityLootTableGenerator = this.positionHistory[(this.historyWriteIndex + this.historySampleCount - 1) % this.historySampleCount];
        if (WallPlayerSkullBlock != null && VanillaEntityLootTableGenerator != null && (VanillaChestLootTableGenerator = VanillaEntityLootTableGenerator.subtract(WallPlayerSkullBlock)).lengthSquared() > 0.001) {
            return VanillaChestLootTableGenerator.normalize();
        }
        return null;
    }

    public AimPointCandidate selectAimPoint() {
        if (this.aimPointCandidates.isEmpty()) {
            return null;
        }
        long l = System.currentTimeMillis();
        if ((float)(l - this.candidateGeneratedAt) < this.effectiveJitterAmount * 1000.0f) {
            if (this.candidateCount < this.aimPointCandidates.size()) {
                return this.aimPointCandidates.get(this.candidateCount);
            }
            return this.aimPointCandidates.get(0);
        }
        this.candidateGeneratedAt = l;
        this.candidateCount = this.aimPointCandidates.size() > 1 ? this.selectWeightedCandidateIndex() : 0;
        AimPointCandidate aimPointCandidate = this.aimPointCandidates.get(this.candidateCount);
        ++aimPointCandidate.sampleIndex;
        aimPointCandidate.generatedAt = l;
        return aimPointCandidate;
    }

    private int selectWeightedCandidateIndex() {
        float f = 0.0f;
        for (AimPointCandidate aimPointCandidate : this.aimPointCandidates) {
            f += aimPointCandidate.score * aimPointCandidate.verticalWeight;
        }
        if (f <= 0.0f) {
            return RANDOM.nextInt(this.aimPointCandidates.size());
        }
        float f2 = RANDOM.nextFloat() * f;
        float f3 = 0.0f;
        for (int i = 0; i < this.aimPointCandidates.size(); ++i) {
            AimPointCandidate aimPointCandidate = this.aimPointCandidates.get(i);
            if (!(f2 <= (f3 += aimPointCandidate.score * aimPointCandidate.verticalWeight))) continue;
            return i;
        }
        return this.aimPointCandidates.size() - 1;
    }

    public double[] getSelectedAimCoordinates() {
        AimPointCandidate aimPointCandidate = this.selectAimPoint();
        if (aimPointCandidate == null) {
            return null;
        }
        return new double[]{aimPointCandidate.x, aimPointCandidate.y, aimPointCandidate.z};
    }

    public List<AimPointCandidate> getAimPointCandidates() {
        return new ArrayList<AimPointCandidate>(this.aimPointCandidates);
    }

    private static double wrapAngle(double d) {
        if ((d %= Math.PI * 2) < -Math.PI) {
            d += Math.PI * 2;
        }
        if (d > Math.PI) {
            d -= Math.PI * 2;
        }
        return d;
    }

    public void reset() {
        this.aimPointCandidates.clear();
        this.candidateCount = 0;
        this.candidateGeneratedAt = 0L;
        this.historyWriteIndex = 0;
        this.historySampleCount = 0;
        Arrays.fill(this.positionHistory, null);
        this.movementClass = TargetMovementCategory.STANDARD;
        this.motionPattern = MotionPattern.STATIONARY;
        this.intensityTier = RotationIntensityTier.MEDIUM;
        this.effectiveHistorySampleCount = this.baseHistorySampleCount;
        this.effectiveAimAdjustment = this.baseAimAdjustment;
        this.effectiveJitterAmount = this.baseJitterAmount;
    }

    @Generated
    public MotionPattern getMotionPattern() {
        return this.motionPattern;
    }

    @Generated
    public RotationIntensityTier getRotationIntensity() {
        return this.intensityTier;
    }

    @Generated
    public int getEffectiveSampleCount() {
        return this.effectiveHistorySampleCount;
    }

    @Generated
    public float getEffectiveAimAdjustment() {
        return this.effectiveAimAdjustment;
    }

    public static enum TargetMovementCategory {
        STANDARD,
        AIRBORNE,
        SWIMMING,
        BURNING,
        CREATIVE;
}

    public static enum MotionPattern {
        STATIONARY,
        SMOOTH,
        JITTERING,
        CONSISTENT,
        ERRATIC;
}

    public static enum RotationIntensityTier {
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH,
        MAXIMUM;
}

    public static class AimPointCandidate {
        public final double x;
        public final double y;
        public final double z;
        public final float score;
        public final float verticalWeight;
        public final TargetMovementCategory movementClass;
        public int sampleIndex = 0;
        public long generatedAt = 0L;

        public AimPointCandidate(double d, double d2, double d3, float f, float f2, TargetMovementCategory targetMovementCategory) {
            this.x = d;
            this.y = d2;
            this.z = d3;
            this.score = f;
            this.verticalWeight = f2;
            this.movementClass = targetMovementCategory;
        }

        public Vec3d getPosition() {
            return new Vec3d(this.x, this.y, this.z);
        }
    }
}

