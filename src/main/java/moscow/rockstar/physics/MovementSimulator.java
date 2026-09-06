/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.World
 *  net.minecraft.Direction$Axis
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.VoxelShape
 *  net.minecraft.ClientPlayerEntity
 */
package moscow.rockstar.physics;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.client.network.ClientPlayerEntity;

public final class MovementSimulator {
    public static final double PLAYER_HALF_WIDTH = 0.3;
    public static final double PLAYER_HEIGHT = 1.8;
    public Vec3d position;
    public Vec3d velocity;
    public boolean onGround;
    public boolean sprinting;
    public boolean sneaking;
    public float yaw;
    private final World world;

    public MovementSimulator(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, boolean bl, boolean bl2, boolean bl3, float f, World class_19372) {
        this.position = VanillaChestLootTableGenerator;
        this.velocity = WallPlayerSkullBlock;
        this.onGround = bl;
        this.sprinting = bl2;
        this.sneaking = bl3;
        this.yaw = f;
        this.world = class_19372;
    }

    public static MovementSimulator fromClient(float f) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity class_7462 = client.player;
        if (class_7462 == null || client.world == null) {
            return new MovementSimulator(Vec3d.ZERO, Vec3d.ZERO, false, false, false, f, null);
        }
        return new MovementSimulator(class_7462.getPos(), class_7462.getVelocity(), class_7462.isOnGround(), class_7462.isSprinting(), class_7462.isSneaking(), f, (World)client.world);
    }

    public Box getBoundingBox() {
        return new Box(this.position.x - 0.3, this.position.y, this.position.z - 0.3, this.position.x + 0.3, this.position.y + 1.8, this.position.z + 0.3);
    }

    public void simulateTick(MovementInput movementInput) {
        double d;
        double d2;
        double d3;
        double d4;
        double d5;
        double d6;
        if (this.world == null) {
            return;
        }
        if (this.sprinting && movementInput.forward <= 0.0) {
            this.sprinting = false;
        }
        if (!this.sprinting && movementInput.sprint && movementInput.forward > 0.0 && !this.sneaking) {
            this.sprinting = true;
        }
        this.sneaking = movementInput.sneak;
        if (movementInput.jump && this.onGround) {
            this.velocity = new Vec3d(this.velocity.x, 0.42, this.velocity.z);
            if (this.sprinting) {
                d6 = Math.toRadians(this.yaw);
                this.velocity = this.velocity.add(-Math.sin(d6) * 0.2, 0.0, Math.cos(d6) * 0.2);
            }
            this.onGround = false;
        }
        if ((d5 = (d6 = movementInput.strafe) * d6 + (d4 = movementInput.forward) * d4) > 1.0E-6) {
            double d7 = 1.0 / Math.sqrt(d5);
            d6 *= d7;
            d4 *= d7;
            d3 = this.onGround ? (this.sprinting ? 0.13 : 0.1) : (this.sprinting ? 0.026 : 0.02);
            double d8 = Math.toRadians(this.yaw);
            d2 = Math.sin(d8);
            double d9 = Math.cos(d8);
            d = (d6 *= d3) * d9 - (d4 *= d3) * d2;
            double d10 = d4 * d9 + d6 * d2;
            this.velocity = this.velocity.add(d, 0.0, d10);
        }
        Vec3d VanillaChestLootTableGenerator = this.velocity = new Vec3d(this.velocity.x, this.velocity.y - 0.08, this.velocity.z);
        Box HorizontalFacingBlock = this.getBoundingBox();
        d3 = VanillaChestLootTableGenerator.y;
        Box InfestedBlock = HorizontalFacingBlock.stretch(0.0, d3, 0.0);
        for (VoxelShape class_2652 : this.world.getBlockCollisions(null, InfestedBlock)) {
            d3 = class_2652.calculateMaxDistance(Direction.Axis.Y, HorizontalFacingBlock, d3);
        }
        HorizontalFacingBlock = HorizontalFacingBlock.offset(0.0, d3, 0.0);
        boolean bl = d3 != VanillaChestLootTableGenerator.y;
        d2 = VanillaChestLootTableGenerator.x;
        Box MutableRegistry = HorizontalFacingBlock.stretch(d2, 0.0, 0.0);
        for (VoxelShape class_2654 : this.world.getBlockCollisions(null, MutableRegistry)) {
            d2 = class_2654.calculateMaxDistance(Direction.Axis.X, HorizontalFacingBlock, d2);
        }
        HorizontalFacingBlock = HorizontalFacingBlock.offset(d2, 0.0, 0.0);
        boolean bl2 = d2 != VanillaChestLootTableGenerator.x;
        d = VanillaChestLootTableGenerator.z;
        Box IceBlock = HorizontalFacingBlock.stretch(0.0, 0.0, d);
        for (VoxelShape class_2655 : this.world.getBlockCollisions(null, IceBlock)) {
            d = class_2655.calculateMaxDistance(Direction.Axis.Z, HorizontalFacingBlock, d);
        }
        HorizontalFacingBlock = HorizontalFacingBlock.offset(0.0, 0.0, d);
        boolean bl3 = d != VanillaChestLootTableGenerator.z;
        this.position = new Vec3d((HorizontalFacingBlock.minX + HorizontalFacingBlock.maxX) * 0.5, HorizontalFacingBlock.minY, (HorizontalFacingBlock.minZ + HorizontalFacingBlock.maxZ) * 0.5);
        if (bl) {
            if (VanillaChestLootTableGenerator.y < 0.0) {
                this.onGround = true;
            }
            this.velocity = new Vec3d(this.velocity.x, 0.0, this.velocity.z);
        } else {
            this.onGround = false;
        }
        if (bl2) {
            this.velocity = new Vec3d(0.0, this.velocity.y, this.velocity.z);
        }
        if (bl3) {
            this.velocity = new Vec3d(this.velocity.x, this.velocity.y, 0.0);
        }
        double d11 = this.onGround ? 0.546 : 0.91;
        this.velocity = new Vec3d(this.velocity.x * d11, this.velocity.y * 0.98, this.velocity.z * d11);
    }

    public void simulateTicks(int n, MovementInput movementInput) {
        for (int i = 0; i < n; ++i) {
            this.simulateTick(movementInput);
        }
    }

    public MovementSimulator copy() {
        return new MovementSimulator(this.position, this.velocity, this.onGround, this.sprinting, this.sneaking, this.yaw, this.world);
    }

    public static final class MovementInput {
        final double forward;
        final double strafe;
        final boolean jump;
        final boolean sprint;
        final boolean sneak;
        public static final MovementInput WALK_FORWARD = new MovementInput(1.0, 0.0, false, true, false);
        public static final MovementInput JUMP_FORWARD = new MovementInput(1.0, 0.0, true, true, false);
        public static final MovementInput WALK_FORWARD_NO_SPRINT = new MovementInput(1.0, 0.0, false, false, false);
        public static final MovementInput NO_INPUT = new MovementInput(0.0, 0.0, false, false, false);

        public MovementInput(double d, double d2, boolean bl, boolean bl2, boolean bl3) {
            this.forward = d;
            this.strafe = d2;
            this.jump = bl;
            this.sprint = bl2;
            this.sneak = bl3;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "forward", "strafe", "jump", "sprint", "sneak");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "forward", "strafe", "jump", "sprint", "sneak");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "forward", "strafe", "jump", "sprint", "sneak");
        }

        public double getForwardInput() {
            return this.forward;
        }

        public double getStrafeInput() {
            return this.strafe;
        }

        public boolean isJumpPressed() {
            return this.jump;
        }

        public boolean isSprintPressed() {
            return this.sprint;
        }

        public boolean isSneakPressed() {
            return this.sneak;
        }
    }
}

