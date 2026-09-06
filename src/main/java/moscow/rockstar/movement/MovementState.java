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
package moscow.rockstar.movement;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class MovementState {
    private static final double GRAVITY_STEP = 0.08;
    private static final double AIR_DRAG = 0.98;
    private static final double GROUND_DRAG = 0.546;
    private static final double TURNING_THRESHOLD = 0.05;
    private static final double FULL_ROTATION_RADIANS = Math.PI * 2;
    private final List<Vec3d> positionHistory = new ArrayList<Vec3d>();
    private final List<Long> sampleTimesMillis = new ArrayList<Long>();
    private int trackedEntityId = -1;
    private Vec3d predictedMovement = Vec3d.ZERO;
    private Vec3d latestVelocity = Vec3d.ZERO;
    private Vec3d estimatedAcceleration = Vec3d.ZERO;

    public void recordSample(LivingEntity class_13092, float f, int n) {
        if (class_13092 == null) {
            this.resetSamples();
            return;
        }
        int n2 = class_13092.getId();
        if (n2 != this.trackedEntityId) {
            this.trackedEntityId = n2;
            this.resetSamples();
        }
        double d = class_13092.prevX + (class_13092.getX() - class_13092.prevX) * (double)f;
        double d2 = class_13092.prevY + (class_13092.getY() - class_13092.prevY) * (double)f;
        double d3 = class_13092.prevZ + (class_13092.getZ() - class_13092.prevZ) * (double)f;
        Vec3d VanillaChestLootTableGenerator = new Vec3d(d, d2, d3);
        long l = System.currentTimeMillis();
        this.positionHistory.add(VanillaChestLootTableGenerator);
        this.sampleTimesMillis.add(l);
        while (this.positionHistory.size() > n) {
            this.positionHistory.removeFirst();
            this.sampleTimesMillis.removeFirst();
        }
        this.updateDerivedMotion();
    }

    private void updateDerivedMotion() {
        if (this.positionHistory.size() < 2) {
            this.latestVelocity = Vec3d.ZERO;
            this.estimatedAcceleration = Vec3d.ZERO;
            return;
        }
        ArrayList<Vec3d> arrayList = new ArrayList<Vec3d>();
        for (int i = 1; i < this.positionHistory.size(); ++i) {
            arrayList.add(this.positionHistory.get(i).subtract(this.positionHistory.get(i - 1)));
        }
        this.latestVelocity = (Vec3d)arrayList.getLast();
        if (arrayList.size() >= 2) {
            ArrayList<Vec3d> arrayList2 = new ArrayList<Vec3d>();
            for (int i = 1; i < arrayList.size(); ++i) {
                arrayList2.add(((Vec3d)arrayList.get(i)).subtract((Vec3d)arrayList.get(i - 1)));
            }
            Vec3d VanillaChestLootTableGenerator = Vec3d.ZERO;
            double d = 0.0;
            for (int i = 0; i < arrayList2.size(); ++i) {
                double d2 = Math.pow(0.85, arrayList2.size() - 1 - i);
                VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add(((Vec3d)arrayList2.get(i)).multiply(d2));
                d += d2;
            }
            this.estimatedAcceleration = d > 0.0 ? VanillaChestLootTableGenerator.multiply(1.0 / d) : Vec3d.ZERO;
        }
    }

    public Vec3d predictEntityPosition(LivingEntity class_13092, int n, float f, boolean bl, boolean bl2, int n2) {
        Vec3d VanillaChestLootTableGenerator;
        if (class_13092 == null || n <= 0) {
            return new Vec3d(class_13092.getX(), class_13092.getY(), class_13092.getZ());
        }
        double d = class_13092.prevX + (class_13092.getX() - class_13092.prevX) * (double)f;
        double d2 = class_13092.prevY + (class_13092.getY() - class_13092.prevY) * (double)f;
        double d3 = class_13092.prevZ + (class_13092.getZ() - class_13092.prevZ) * (double)f;
        Vec3d WallPlayerSkullBlock = new Vec3d(d, d2, d3);
        Vec3d VanillaEntityLootTableGenerator = Vec3d.ZERO;
        if (bl && this.positionHistory.size() >= 3) {
            VanillaEntityLootTableGenerator = VanillaEntityLootTableGenerator.add(this.extrapolateMovement(n));
        }
        if (bl2 && this.positionHistory.size() >= 5 && (VanillaChestLootTableGenerator = this.estimateTrajectory(n, n2)) != null) {
            VanillaEntityLootTableGenerator = VanillaEntityLootTableGenerator.multiply(0.6).add(VanillaChestLootTableGenerator.multiply(0.4));
        }
        if ((VanillaChestLootTableGenerator = this.predictPlayerMovement(class_13092, n)) != null) {
            VanillaEntityLootTableGenerator = VanillaEntityLootTableGenerator.multiply(0.7).add(VanillaChestLootTableGenerator.multiply(0.3));
        }
        this.predictedMovement = WallPlayerSkullBlock.add(VanillaEntityLootTableGenerator);
        return this.predictedMovement;
    }

    private Vec3d extrapolateMovement(int n) {
        double d = (double)n * 0.05;
        return this.latestVelocity.multiply((double)n).add(this.estimatedAcceleration.multiply(0.5 * d * d));
    }

    private Vec3d estimateTrajectory(int n, int n2) {
        int n3 = Math.min(this.positionHistory.size(), n2);
        if (n3 < 5) {
            return null;
        }
        if (this.hasConsistentTurning(n3)) {
            return this.predictCircularMotion(n, n3);
        }
        if (this.hasConsistentDirection(n3)) {
            return this.latestVelocity.multiply((double)n);
        }
        Vec3d VanillaChestLootTableGenerator = Vec3d.ZERO;
        double d = 0.0;
        for (int i = 1; i < n3; ++i) {
            Vec3d WallPlayerSkullBlock = this.positionHistory.get(i).subtract(this.positionHistory.get(i - 1));
            double d2 = Math.pow(0.9, n3 - i);
            VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock.multiply(d2));
            d += d2;
        }
        return d > 0.0 ? VanillaChestLootTableGenerator.multiply((double)n / d) : Vec3d.ZERO;
    }

    private boolean hasConsistentTurning(int n) {
        if (n < 5) {
            return false;
        }
        ArrayList<Double> arrayList = new ArrayList<Double>();
        for (int i = 2; i < n; ++i) {
            Vec3d VanillaChestLootTableGenerator = this.positionHistory.get(i - 1).subtract(this.positionHistory.get(i - 2));
            Vec3d WallPlayerSkullBlock = this.positionHistory.get(i).subtract(this.positionHistory.get(i - 1));
            double d = Math.atan2(WallPlayerSkullBlock.z, WallPlayerSkullBlock.x) - Math.atan2(VanillaChestLootTableGenerator.z, VanillaChestLootTableGenerator.x);
            d = MovementState.wrapAngle(d);
            arrayList.add(Math.abs(d));
        }
        double d = arrayList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double d3 = arrayList.stream().mapToDouble(d2 -> Math.pow(d2 - d, 2.0)).average().orElse(1.0);
        return d3 < 0.1 && d > 0.05;
    }

    private Vec3d predictCircularMotion(int n, int n2) {
        Vec3d VanillaChestLootTableGenerator = this.fitCircleCenter(n2);
        Vec3d WallPlayerSkullBlock = this.positionHistory.get(n2 - 1);
        Vec3d VanillaEntityLootTableGenerator = this.positionHistory.get(n2 - 2);
        double d = Math.atan2(WallPlayerSkullBlock.z - VanillaChestLootTableGenerator.z, WallPlayerSkullBlock.x - VanillaChestLootTableGenerator.x);
        double d2 = Math.atan2(VanillaEntityLootTableGenerator.z - VanillaChestLootTableGenerator.z, VanillaEntityLootTableGenerator.x - VanillaChestLootTableGenerator.x);
        double d3 = MovementState.wrapAngle(d - d2);
        double d4 = WallPlayerSkullBlock.subtract(VanillaChestLootTableGenerator).horizontalLength();
        double d5 = d + d3 * (double)n;
        double d6 = VanillaChestLootTableGenerator.x + d4 * Math.cos(d5);
        double d7 = VanillaChestLootTableGenerator.z + d4 * Math.sin(d5);
        double d8 = WallPlayerSkullBlock.y + this.latestVelocity.y * (double)n;
        return new Vec3d(d6 - WallPlayerSkullBlock.x, d8 - WallPlayerSkullBlock.y, d7 - WallPlayerSkullBlock.z);
    }

    private Vec3d fitCircleCenter(int n) {
        if (n < 3) {
            return this.positionHistory.get(n - 1);
        }
        Vec3d VanillaChestLootTableGenerator = this.positionHistory.get(n - 3);
        Vec3d WallPlayerSkullBlock = this.positionHistory.get(n - 2);
        Vec3d VanillaEntityLootTableGenerator = this.positionHistory.get(n - 1);
        double d = 2.0 * (VanillaChestLootTableGenerator.x * (WallPlayerSkullBlock.z - VanillaEntityLootTableGenerator.z) + WallPlayerSkullBlock.x * (VanillaEntityLootTableGenerator.z - VanillaChestLootTableGenerator.z) + VanillaEntityLootTableGenerator.x * (VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z));
        if (Math.abs(d) < 0.001) {
            return WallPlayerSkullBlock;
        }
        double d2 = ((VanillaChestLootTableGenerator.x * VanillaChestLootTableGenerator.x + VanillaChestLootTableGenerator.z * VanillaChestLootTableGenerator.z) * (WallPlayerSkullBlock.z - VanillaEntityLootTableGenerator.z) + (WallPlayerSkullBlock.x * WallPlayerSkullBlock.x + WallPlayerSkullBlock.z * WallPlayerSkullBlock.z) * (VanillaEntityLootTableGenerator.z - VanillaChestLootTableGenerator.z) + (VanillaEntityLootTableGenerator.x * VanillaEntityLootTableGenerator.x + VanillaEntityLootTableGenerator.z * VanillaEntityLootTableGenerator.z) * (VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z)) / d;
        double d3 = ((VanillaChestLootTableGenerator.x * VanillaChestLootTableGenerator.x + VanillaChestLootTableGenerator.z * VanillaChestLootTableGenerator.z) * (VanillaEntityLootTableGenerator.x - WallPlayerSkullBlock.x) + (WallPlayerSkullBlock.x * WallPlayerSkullBlock.x + WallPlayerSkullBlock.z * WallPlayerSkullBlock.z) * (VanillaChestLootTableGenerator.x - VanillaEntityLootTableGenerator.x) + (VanillaEntityLootTableGenerator.x * VanillaEntityLootTableGenerator.x + VanillaEntityLootTableGenerator.z * VanillaEntityLootTableGenerator.z) * (WallPlayerSkullBlock.x - VanillaChestLootTableGenerator.x)) / d;
        return new Vec3d(d2, WallPlayerSkullBlock.y, d3);
    }

    private boolean hasConsistentDirection(int n) {
        double d;
        Vec3d VanillaChestLootTableGenerator;
        if (n < 3) {
            return false;
        }
        Vec3d WallPlayerSkullBlock = this.positionHistory.get(n - 1).subtract(this.positionHistory.get(n - 2));
        double d2 = WallPlayerSkullBlock.dotProduct(VanillaChestLootTableGenerator = this.positionHistory.get(n - 2).subtract(this.positionHistory.get(n - 3)));
        double d3 = Math.acos(MathHelper.clamp((double)(d2 / (d = WallPlayerSkullBlock.length() * VanillaChestLootTableGenerator.length() + 0.001)), (double)-1.0, (double)1.0));
        return d3 < 0.15;
    }

    private Vec3d predictPlayerMovement(LivingEntity class_13092, int n) {
        if (!(class_13092 instanceof PlayerEntity)) {
            return null;
        }
        PlayerEntity class_16572 = (PlayerEntity)class_13092;
        double d = class_16572.getVelocity().x;
        double d2 = class_16572.getVelocity().y;
        double d3 = class_16572.getVelocity().z;
        boolean bl = class_16572.isOnGround();
        Vec3d VanillaChestLootTableGenerator = Vec3d.ZERO;
        for (int i = 0; i < n; ++i) {
            d2 -= 0.08;
            if (bl) {
                d *= 0.546;
                d3 *= 0.546;
            } else {
                d *= 0.98;
                d3 *= 0.98;
            }
            VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add(d, d2, d3);
            if (!(VanillaChestLootTableGenerator.y < class_13092.getY() - (double)class_13092.getHeight())) continue;
            bl = true;
            d2 = 0.0;
        }
        return VanillaChestLootTableGenerator;
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

    public void resetSamples() {
        this.positionHistory.clear();
        this.sampleTimesMillis.clear();
        this.trackedEntityId = -1;
        this.predictedMovement = Vec3d.ZERO;
        this.latestVelocity = Vec3d.ZERO;
        this.estimatedAcceleration = Vec3d.ZERO;
    }

    @Generated
    public Vec3d getPredictedMovement() {
        return this.predictedMovement;
    }

    @Generated
    public Vec3d getLatestVelocity() {
        return this.latestVelocity;
    }

    @Generated
    public Vec3d getEstimatedAcceleration() {
        return this.estimatedAcceleration;
    }
}

