/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  moscow.rockstar.modules.other.admin.BlockPos$Mutable
 *  net.minecraft.Entity
 *  net.minecraft.BlockView
 *  net.minecraft.Block
 *  net.minecraft.Direction
 *  net.minecraft.Vec3i
 *  net.minecraft.HitResult$Type
 *  net.minecraft.FluidBlock
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockState
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.world.mining;

import java.util.HashSet;
import java.util.Set;
import moscow.rockstar.api.validation.ScreenStateService;
import moscow.rockstar.combat.rotation.RotationRequestHelper;
import moscow.rockstar.combat.rotation.RotationState;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.math.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import moscow.rockstar.ui.notifications.NotificationBridge;
import moscow.rockstar.world.BlockDropResolver;
import moscow.rockstar.world.BlockRegion;
import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.navigation.PathNavigator;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.hit.HitResult;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ExcavationController
implements ScreenStateService {
    public enum NavigationResult {
        IN_PROGRESS,
        REACHED_STEP,
        UNAVAILABLE
    }

    public enum RotationInputPreset {
        WALK_FORWARD,
        JUMP_FORWARD,
        NO_INPUT,
        SNEAK
    }

    private final BlockPos startPosition;
    private final BlockPos endPosition;
    @Nullable
    private final Block targetBlock;
    private final int targetBlockCount;
    private int processedBlockCount;
    @Nullable
    private BlockPos currentBlockPosition;
    @Nullable
    private PathNavigator pathNavigator;
    private boolean breakingCurrentBlock;
    private final BlockBreakValidator blockBreakValidator = new BlockBreakValidator();
    private boolean paused;
    private boolean completed;
    private int blockedAttempts;
    private final Set<BlockPos> skippedPositions = new HashSet<BlockPos>();
    private double movementCost = 1.0;
    private double startSurfaceHeight = Double.NaN;
    private double endSurfaceHeight = Double.NaN;

    public ExcavationController(BlockPositionOffset start, BlockPositionOffset end) {
        this(start.toBlockPosition(), end.toBlockPosition(), null);
    }

    public ExcavationController(BlockPos adminsky, BlockPos adminsky2) {
        this(adminsky, adminsky2, null);
    }

    public ExcavationController(BlockPos adminsky, BlockPos adminsky2, @Nullable Block class_22482) {
        this.startPosition = adminsky;
        this.endPosition = adminsky2;
        this.targetBlock = class_22482;
        this.targetBlockCount = this.countTargetBlocks();
    }

    public BlockPos getStartPosition() {
        return this.startPosition;
    }

    public BlockPos getEndPosition() {
        return this.endPosition;
    }

    public int getTargetBlockCount() {
        return this.targetBlockCount;
    }

    public double getMovementCost() {
        return this.movementCost;
    }

    public void setMovementCost(double movementCost) {
        this.movementCost = Math.max(0.01, movementCost);
    }

    public void setSurfaceHeights(double startSurfaceHeight, double endSurfaceHeight) {
        this.startSurfaceHeight = startSurfaceHeight;
        this.endSurfaceHeight = endSurfaceHeight;
    }

    public double getStartSurfaceHeight() {
        return this.startSurfaceHeight;
    }

    public double getEndSurfaceHeight() {
        return this.endSurfaceHeight;
    }

    protected ClientPlayerEntity getClientPlayer() {
        return MinecraftClient.getInstance().player;
    }

    protected double horizontalDistanceToEnd(double x, double z) {
        double dx = x - ((double)this.endPosition.getX() + 0.5);
        double dz = z - ((double)this.endPosition.getZ() + 0.5);
        return Math.hypot(dx, dz);
    }

    protected boolean hasReachedEnd(double horizontalRadius, double verticalTolerance) {
        ClientPlayerEntity player = this.getClientPlayer();
        return player != null
            && this.horizontalDistanceToEnd(player.getX(), player.getZ()) <= horizontalRadius
            && Math.abs(player.getY() - ((double)this.endPosition.getY() + 0.2)) <= verticalTolerance;
    }

    protected void moveTowardEnd(boolean jump, boolean sprint) {
        ClientPlayerEntity player = this.getClientPlayer();
        if (player == null) {
            return;
        }
        double dx = (double)this.endPosition.getX() + 0.5 - player.getX();
        double dz = (double)this.endPosition.getZ() + 0.5 - player.getZ();
        float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        RotationRequestHelper.requestAdaptiveRotation(new Rotation(yaw, 0.0f));
        RotationState movement = ClientServiceRegistry.getInstance().getRotationState();
        movement.enableMovementOverride();
        movement.setForwardPressed(true);
        movement.setBackwardPressed(false);
        movement.setStrafeRightPressed(false);
        movement.setStrafeLeftPressed(false);
        movement.setJumpPressed(jump);
        movement.setSneakPressed(false);
        movement.setSprintPressed(sprint);
    }

    protected void clearMovementInput() {
        RotationState movement = ClientServiceRegistry.getInstance().getRotationState();
        movement.setForwardPressed(false);
        movement.setBackwardPressed(false);
        movement.setStrafeRightPressed(false);
        movement.setStrafeLeftPressed(false);
        movement.setJumpPressed(false);
        movement.setSneakPressed(false);
        movement.setSprintPressed(false);
    }

    private int countTargetBlocks() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return 0;
        }
        int n = 0;
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        for (int i = this.startPosition.getY(); i <= this.endPosition.getY(); ++i) {
            for (int j = this.startPosition.getX(); j <= this.endPosition.getX(); ++j) {
                for (int k = this.startPosition.getZ(); k <= this.endPosition.getZ(); ++k) {
                    class_23392.set(j, i, k);
                    if (!this.isMineableBlock(client, (BlockPos)class_23392)) continue;
                    ++n;
                }
            }
        }
        return n;
    }

    private boolean isMineableBlock(MinecraftClient client, BlockPos adminsky) {
        if (client.world == null) {
            return false;
        }
        BlockState class_26802 = client.world.getBlockState(adminsky);
        if (class_26802.isAir()) {
            return false;
        }
        if (class_26802.getBlock() instanceof FluidBlock) {
            return false;
        }
        if (class_26802.getHardness((BlockView)client.world, adminsky) < 0.0f) {
            return false;
        }
        return this.targetBlock == null || class_26802.getBlock() == this.targetBlock;
    }

    @Override
    public String getCommandName() {
        return "excavate";
    }

    @Override
    public String getStatusMessage() {
        if (this.paused) {
            return "\u043f\u0430\u0443\u0437\u0430";
        }
        if (this.completed) {
            return "\u0433\u043e\u0442\u043e\u0432\u043e";
        }
        String string = this.processedBlockCount + "/" + this.targetBlockCount;
        if (this.breakingCurrentBlock) {
            return "\u043a\u043e\u043f\u0430\u0435\u043c " + String.valueOf(this.currentBlockPosition) + " (" + string + ")";
        }
        if (this.pathNavigator != null) {
            return "\u0438\u0434\u0451\u043c \u043a " + String.valueOf(this.currentBlockPosition) + " (" + string + ")";
        }
        return "\u0440\u0430\u0441\u043a\u043e\u043f " + string;
    }

    @Override
    public boolean tickNavigation() {
        boolean bl;
        if (this.paused) {
            return false;
        }
        if (this.completed) {
            return true;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.interactionManager == null) {
            return false;
        }
        RotationRequestHelper.refreshRotation();
        if (this.breakingCurrentBlock && this.currentBlockPosition != null) {
            if (!this.isMineableBlock(client, this.currentBlockPosition)) {
                if (client.world.getBlockState(this.currentBlockPosition).isAir()) {
                    ++this.processedBlockCount;
                }
                this.breakingCurrentBlock = false;
                this.currentBlockPosition = null;
                this.stopBreaking();
                return false;
            }
            Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)this.currentBlockPosition);
            double d = client.player.getEyePos().distanceTo(VanillaChestLootTableGenerator);
            if (d > 5.0) {
                this.breakingCurrentBlock = false;
                this.stopBreaking();
                this.pathNavigator = new PathNavigator(new BlockRegion(this.currentBlockPosition, 2));
                return false;
            }
            if (!ExcavationController.hasLineOfSightToBlock(client, this.currentBlockPosition)) {
                this.breakingCurrentBlock = false;
                this.stopBreaking();
                ++this.blockedAttempts;
                if (this.blockedAttempts >= 3) {
                    this.skippedPositions.add(this.currentBlockPosition);
                    this.currentBlockPosition = null;
                    this.blockedAttempts = 0;
                    return false;
                }
                this.pathNavigator = new PathNavigator(new BlockRegion(this.currentBlockPosition, 1));
                return false;
            }
            this.blockedAttempts = 0;
            BlockDropResolver.isBreakable(client.world.getBlockState(this.currentBlockPosition));
            Direction class_23502 = this.getBestBlockFace(client, this.currentBlockPosition);
            if (!this.blockBreakValidator.canBreak(this.currentBlockPosition, class_23502)) {
                return false;
            }
            client.interactionManager.updateBlockBreakingProgress(this.currentBlockPosition, class_23502);
            client.player.swingHand(client.player.getActiveHand());
            return false;
        }
        if (this.pathNavigator != null) {
            boolean bl2 = this.pathNavigator.tickNavigation();
            if (bl2) {
                this.pathNavigator = null;
                if (this.currentBlockPosition != null) {
                    double d = client.player.getEyePos().distanceTo(Vec3d.ofCenter((Vec3i)this.currentBlockPosition));
                    if (d > 5.0) {
                        this.skippedPositions.add(this.currentBlockPosition);
                        this.currentBlockPosition = null;
                    } else {
                        this.breakingCurrentBlock = true;
                    }
                }
            }
            return false;
        }
        BlockPos adminsky = this.findNextBlockPosition(client);
        if (adminsky == null) {
            NotificationBridge.showMessage("\u0420\u0430\u0441\u043a\u043e\u043f \u0437\u0430\u0432\u0435\u0440\u0448\u0451\u043d (" + this.processedBlockCount + " \u0431\u043b\u043e\u043a\u043e\u0432)");
            this.stopNavigation();
            this.completed = true;
            return true;
        }
        this.currentBlockPosition = adminsky;
        Vec3d WallPlayerSkullBlock = Vec3d.ofCenter((Vec3i)adminsky);
        boolean bl3 = bl = client.player.getEyePos().distanceTo(WallPlayerSkullBlock) <= 4.5;
        if (bl && ExcavationController.hasLineOfSightToBlock(client, adminsky)) {
            this.breakingCurrentBlock = true;
        } else {
            this.pathNavigator = new PathNavigator(new BlockRegion(adminsky, 2));
        }
        return false;
    }

    @Nullable
    private BlockPos findNextBlockPosition(MinecraftClient client) {
        if (client.player == null) {
            return null;
        }
        double d = client.player.getX();
        double d2 = client.player.getZ();
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        for (int i = this.endPosition.getY(); i >= this.startPosition.getY(); --i) {
            BlockPos adminsky = null;
            double d3 = Double.MAX_VALUE;
            for (int j = this.startPosition.getX(); j <= this.endPosition.getX(); ++j) {
                for (int k = this.startPosition.getZ(); k <= this.endPosition.getZ(); ++k) {
                    double d4;
                    double d5;
                    double d6;
                    class_23392.set(j, i, k);
                    if (this.skippedPositions.contains(class_23392) || !this.isMineableBlock(client, (BlockPos)class_23392) || !((d6 = (d5 = (double)j + 0.5 - d) * d5 + (d4 = (double)k + 0.5 - d2) * d4) < d3)) continue;
                    d3 = d6;
                    adminsky = class_23392.toImmutable();
                }
            }
            if (adminsky == null) continue;
            return adminsky;
        }
        return null;
    }

    private static boolean hasLineOfSightToBlock(MinecraftClient client, BlockPos adminsky) {
        Vec3d[] class_243Array;
        if (client.world == null || client.player == null) {
            return false;
        }
        Vec3d VanillaChestLootTableGenerator = client.player.getEyePos();
        Vec3d WallPlayerSkullBlock = Vec3d.ofCenter((Vec3i)adminsky);
        for (Vec3d VanillaEntityLootTableGenerator : class_243Array = new Vec3d[]{WallPlayerSkullBlock, WallPlayerSkullBlock.add(0.49, 0.0, 0.0), WallPlayerSkullBlock.add(-0.49, 0.0, 0.0), WallPlayerSkullBlock.add(0.0, 0.49, 0.0), WallPlayerSkullBlock.add(0.0, -0.49, 0.0), WallPlayerSkullBlock.add(0.0, 0.0, 0.49), WallPlayerSkullBlock.add(0.0, 0.0, -0.49)}) {
            RaycastContext class_39592 = new RaycastContext(VanillaChestLootTableGenerator, VanillaEntityLootTableGenerator, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)client.player);
            BlockHitResult class_39652 = client.world.raycast(class_39592);
            if (class_39652.getType() != HitResult.Type.BLOCK) {
                return true;
            }
            if (!class_39652.getBlockPos().equals(adminsky)) continue;
            return true;
        }
        return false;
    }

    private Direction getBestBlockFace(MinecraftClient client, BlockPos adminsky) {
        Vec3d VanillaChestLootTableGenerator = client.player.getEyePos();
        Direction class_23502 = Direction.UP;
        double d = -1.0;
        for (Direction class_23503 : Direction.values()) {
            Vec3d WallPlayerSkullBlock = Vec3d.ofCenter((Vec3i)adminsky).add((double)class_23503.getOffsetX() * 0.5, (double)class_23503.getOffsetY() * 0.5, (double)class_23503.getOffsetZ() * 0.5);
            Vec3d VanillaEntityLootTableGenerator = WallPlayerSkullBlock.subtract(VanillaChestLootTableGenerator).normalize();
            double d2 = VanillaEntityLootTableGenerator.x * (double)class_23503.getOffsetX() + VanillaEntityLootTableGenerator.y * (double)class_23503.getOffsetY() + VanillaEntityLootTableGenerator.z * (double)class_23503.getOffsetZ();
            double d3 = -d2;
            if (!(d3 > d)) continue;
            d = d3;
            class_23502 = class_23503;
        }
        return class_23502;
    }

    private void stopBreaking() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager != null) {
            client.interactionManager.cancelBlockBreaking();
        }
    }

    @Override
    public void stopNavigation() {
        if (this.pathNavigator != null) {
            this.pathNavigator.stopNavigation();
            this.pathNavigator = null;
        }
        this.stopBreaking();
        RotationState rotationState = ClientServiceRegistry.getInstance().getRotationState();
        rotationState.disableMovementOverride();
        this.currentBlockPosition = null;
        this.breakingCurrentBlock = false;
    }

    @Override
    public void pauseNavigation() {
        this.paused = true;
        if (this.pathNavigator != null) {
            this.pathNavigator.pauseNavigation();
        }
        this.stopBreaking();
        ClientServiceRegistry.getInstance().getRotationState().disableMovementOverride();
    }

    @Override
    public void resumeNavigation() {
        this.paused = false;
        if (this.pathNavigator != null) {
            this.pathNavigator.resumeNavigation();
        }
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public boolean isTargetReached() {
        return this.completed;
    }
}
