/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  moscow.rockstar.modules.other.admin.BlockPos$Mutable
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.ExperienceOrbEntity
 *  net.minecraft.ItemEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.PlayerInventory
 *  net.minecraft.SlotActionType
 *  net.minecraft.Item
 *  net.minecraft.Items
 *  net.minecraft.BlockView
 *  net.minecraft.Block
 *  net.minecraft.Direction
 *  net.minecraft.Box
 *  net.minecraft.Vec3i
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockState
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  net.minecraft.ClientPlayerEntity
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.world;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashSet;
import java.util.Set;
import moscow.rockstar.api.validation.ScreenStateService;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.combat.rotation.RotationRequestHelper;
import moscow.rockstar.combat.rotation.RotationState;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.math.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.ui.notifications.NotificationBridge;
import moscow.rockstar.world.BlockCollisionProbe;
import moscow.rockstar.world.BlockDropResolver;
import moscow.rockstar.world.BlockRegion;
import moscow.rockstar.world.navigation.PathNavigator;
import moscow.rockstar.world.mining.BlockBreakValidator;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.world.BlockView;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class BlockInteractionState
implements ScreenStateService {
    private static final Direction[] INTERACTION_DIRECTIONS = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP};
    private final BlockPos startPosition;
    private final BlockPos endPosition;
    private final Block targetBlock;
    private final Item targetItem;
    private final int targetBlockCount;
    private int processedBlockCount;
    @Nullable
    private BlockPos currentPosition;
    @Nullable
    private PathNavigator pathNavigator;
    private final BlockBreakValidator blockBreakValidator = new BlockBreakValidator();
    private boolean paused;
    private boolean targetReached;
    @Nullable
    private String errorMessage;
    private int currentTargetIndex;
    private final Set<BlockPos> visitedPositions = new HashSet<BlockPos>();
    @Nullable
    private Rotation lastTargetRotation;
    private int rotationConfirmations;

    public BlockInteractionState(BlockPos adminsky, BlockPos adminsky2, Block class_22482) {
        this.startPosition = adminsky;
        this.endPosition = adminsky2;
        this.targetBlock = class_22482;
        this.targetItem = class_22482.asItem();
        this.targetBlockCount = this.countTargetBlockReplacements();
    }

    private int countTargetBlockReplacements() {
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
                    if (client.world.getBlockState((BlockPos)class_23392).getBlock() == this.targetBlock) continue;
                    ++n;
                }
            }
        }
        return n;
    }

    private boolean isBlockStateInteractable(MinecraftClient client, BlockPos adminsky, BlockState class_26802) {
        if (class_26802.getBlock() == this.targetBlock) {
            return false;
        }
        if (class_26802.isAir() || class_26802.isReplaceable()) {
            return false;
        }
        return class_26802.getHardness((BlockView)client.world, adminsky) >= 0.0f;
    }

    private boolean canInteractAt(MinecraftClient client, BlockPos adminsky) {
        BlockState class_26802 = client.world.getBlockState(adminsky);
        if (class_26802.getBlock() == this.targetBlock) {
            return false;
        }
        if (this.isBlockStateInteractable(client, adminsky, class_26802)) {
            return true;
        }
        return (class_26802.isAir() || class_26802.isReplaceable()) && !this.hasNeighboringSupport(client, adminsky);
    }

    @Override
    public String getCommandName() {
        return "fill";
    }

    @Override
    public String getStatusMessage() {
        if (this.paused) {
            return "\u043f\u0430\u0443\u0437\u0430";
        }
        if (this.targetReached) {
            return "\u0433\u043e\u0442\u043e\u0432\u043e";
        }
        String string = this.processedBlockCount + "/" + this.targetBlockCount;
        if (this.currentPosition != null && this.pathNavigator == null) {
            return "\u0440\u0430\u0431\u043e\u0442\u0430\u0435\u043c " + String.valueOf(this.currentPosition) + " (" + string + ")";
        }
        if (this.pathNavigator != null) {
            return "\u0438\u0434\u0451\u043c \u043a " + String.valueOf(this.currentPosition) + " (" + string + ")";
        }
        return "\u0437\u0430\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u0435 " + string;
    }

    @Override
    public boolean tickNavigation() {
        if (this.paused) {
            return false;
        }
        if (this.targetReached) {
            return true;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.interactionManager == null) {
            return false;
        }
        RotationRequestHelper.refreshRotation();
        if (this.pathNavigator != null) {
            boolean bl = this.pathNavigator.tickNavigation();
            if (bl) {
                double d;
                this.pathNavigator = null;
                if (this.currentPosition != null && (d = client.player.getEyePos().distanceTo(Vec3d.ofCenter((Vec3i)this.currentPosition))) > 5.0) {
                    this.visitedPositions.add(this.currentPosition);
                    this.currentPosition = null;
                }
            }
            return false;
        }
        if (this.currentPosition != null) {
            return this.hasSupportingBlock(client, this.currentPosition);
        }
        BlockPos adminsky = this.findNextTargetPosition(client);
        if (adminsky == null) {
            NotificationBridge.showMessage("\u0417\u0430\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u0435 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u043e (" + this.processedBlockCount + " \u0431\u043b\u043e\u043a\u043e\u0432)");
            this.stopNavigation();
            this.targetReached = true;
            return true;
        }
        this.currentPosition = adminsky;
        this.resetRotationTracking();
        return false;
    }

    private boolean hasSupportingBlock(MinecraftClient client, BlockPos adminsky) {
        Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)adminsky);
        double d = client.player.getEyePos().distanceTo(VanillaChestLootTableGenerator);
        if (d > 5.0) {
            this.cancelInteraction();
            this.resetRotationTracking();
            this.processBlockPosition(client, adminsky);
            return false;
        }
        BlockState class_26802 = client.world.getBlockState(adminsky);
        if (class_26802.getBlock() == this.targetBlock) {
            ++this.processedBlockCount;
            this.currentPosition = null;
            this.cancelInteraction();
            this.resetRotationTracking();
            return false;
        }
        if (this.isBlockStateInteractable(client, adminsky, class_26802)) {
            return this.hasPassableSupport(client, adminsky, class_26802);
        }
        if (this.hasNeighboringSupport(client, adminsky)) {
            this.currentPosition = null;
            this.cancelInteraction();
            this.resetRotationTracking();
            return false;
        }
        if (this.isPositionExposed(client, adminsky)) {
            this.cancelInteraction();
            this.resetRotationTracking();
            BlockPos adminsky2 = this.findAdjacentPosition(client, adminsky);
            ++this.currentTargetIndex;
            if (adminsky2 == null || this.currentTargetIndex >= 4) {
                this.visitedPositions.add(adminsky);
                this.currentPosition = null;
                this.currentTargetIndex = 0;
                return false;
            }
            this.pathNavigator = new PathNavigator(new BlockCollisionProbe(adminsky2));
            return false;
        }
        InteractionTarget interactionTarget = this.createInteractionTarget(client, adminsky);
        if (interactionTarget == null) {
            ++this.currentTargetIndex;
            if (this.currentTargetIndex >= 3) {
                this.visitedPositions.add(adminsky);
                this.currentPosition = null;
                this.currentTargetIndex = 0;
                return false;
            }
            this.pathNavigator = new PathNavigator(new BlockRegion(adminsky, 1));
            return false;
        }
        this.currentTargetIndex = 0;
        if (!this.ensureTargetItemSelected(client)) {
            NotificationBridge.showPersistentMessage("\u041d\u0435\u0442 \u0431\u043b\u043e\u043a\u0430 " + String.valueOf(this.targetBlock) + " \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435");
            this.errorMessage = "\u043d\u0435\u0442 \u0431\u043b\u043e\u043a\u0430 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435";
            this.stopNavigation();
            this.targetReached = true;
            return true;
        }
        if (!this.confirmRotationToTarget(interactionTarget.hitVector)) {
            return false;
        }
        BlockHitResult class_39652 = new BlockHitResult(interactionTarget.hitVector, interactionTarget.clickFace, interactionTarget.blockPosition, false);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, class_39652);
        client.player.swingHand(Hand.MAIN_HAND);
        return false;
    }

    private boolean hasPassableSupport(MinecraftClient client, BlockPos adminsky, BlockState class_26802) {
        if (!BlockInteractionState.isInteractionRayValid(client, adminsky)) {
            this.cancelInteraction();
            ++this.currentTargetIndex;
            if (this.currentTargetIndex >= 3) {
                this.visitedPositions.add(adminsky);
                this.currentPosition = null;
                this.currentTargetIndex = 0;
                return false;
            }
            this.pathNavigator = new PathNavigator(new BlockRegion(adminsky, 1));
            return false;
        }
        this.currentTargetIndex = 0;
        BlockDropResolver.isBreakable(class_26802);
        Direction class_23502 = this.findBestInteractionFace(client, adminsky);
        if (!this.blockBreakValidator.canBreak(adminsky, class_23502)) {
            return false;
        }
        client.interactionManager.updateBlockBreakingProgress(adminsky, class_23502);
        client.player.swingHand(client.player.getActiveHand());
        return false;
    }

    @Nullable
    private BlockPos findNextTargetPosition(MinecraftClient client) {
        if (client.player == null) {
            return null;
        }
        double d = client.player.getX();
        double d2 = client.player.getZ();
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        for (int i = this.startPosition.getY(); i <= this.endPosition.getY(); ++i) {
            BlockPos adminsky = null;
            double d3 = Double.MAX_VALUE;
            for (int j = this.startPosition.getX(); j <= this.endPosition.getX(); ++j) {
                for (int k = this.startPosition.getZ(); k <= this.endPosition.getZ(); ++k) {
                    double d4;
                    double d5;
                    double d6;
                    class_23392.set(j, i, k);
                    if (this.visitedPositions.contains(class_23392) || !this.canInteractAt(client, (BlockPos)class_23392) || !((d6 = (d5 = (double)j + 0.5 - d) * d5 + (d4 = (double)k + 0.5 - d2) * d4) < d3)) continue;
                    d3 = d6;
                    adminsky = class_23392.toImmutable();
                }
            }
            if (adminsky == null) continue;
            return adminsky;
        }
        return null;
    }

    @Nullable
    private InteractionTarget createInteractionTarget(MinecraftClient client, BlockPos adminsky) {
        if (client.world == null || client.player == null) {
            return null;
        }
        Vec3d VanillaChestLootTableGenerator = client.player.getEyePos();
        for (Direction class_23502 : INTERACTION_DIRECTIONS) {
            Vec3d WallPlayerSkullBlock;
            BlockPos adminsky2 = adminsky.offset(class_23502);
            Direction class_23503 = class_23502.getOpposite();
            BlockState class_26802 = client.world.getBlockState(adminsky2);
            if (!class_26802.isSideSolidFullSquare((BlockView)client.world, adminsky2, class_23503) || VanillaChestLootTableGenerator.distanceTo(WallPlayerSkullBlock = Vec3d.ofCenter((Vec3i)adminsky2).add((double)class_23503.getOffsetX() * 0.5, (double)class_23503.getOffsetY() * 0.5, (double)class_23503.getOffsetZ() * 0.5)) > 5.0 || !BlockInteractionState.hasClearLineOfSight(client, WallPlayerSkullBlock, adminsky2)) continue;
            return new InteractionTarget(adminsky2, class_23503, WallPlayerSkullBlock);
        }
        return null;
    }

    private void processBlockPosition(MinecraftClient client, BlockPos adminsky) {
        BlockPos adminsky2 = this.findAdjacentPosition(client, adminsky);
        this.pathNavigator = adminsky2 != null ? new PathNavigator(new BlockCollisionProbe(adminsky2)) : new PathNavigator(new BlockRegion(adminsky, 2));
    }

    private boolean hasNeighboringSupport(MinecraftClient client, BlockPos adminsky) {
        if (client.world == null) {
            return false;
        }
        Box HorizontalFacingBlock = new Box(adminsky);
        for (Entity class_12972 : client.world.getOtherEntities((Entity)client.player, HorizontalFacingBlock)) {
            if (!class_12972.isAlive() || class_12972.isSpectator() || class_12972 instanceof ItemEntity || class_12972 instanceof ExperienceOrbEntity) continue;
            return true;
        }
        return false;
    }

    private boolean isPositionExposed(MinecraftClient client, BlockPos adminsky) {
        if (client.player == null) {
            return false;
        }
        return client.player.getBoundingBox().intersects(new Box(adminsky));
    }

    @Nullable
    private BlockPos findAdjacentPosition(MinecraftClient client, BlockPos adminsky) {
        Direction[] class_2350Array;
        if (client.player == null || client.world == null) {
            return null;
        }
        Vec3d VanillaChestLootTableGenerator = client.player.getPos();
        BlockPos adminsky2 = null;
        double d = Double.MAX_VALUE;
        for (Direction class_23502 : class_2350Array = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos adminsky3 = adminsky.offset(class_23502);
            for (BlockPos adminsky4 : new BlockPos[]{adminsky3, adminsky3.up()}) {
                double d2;
                if (!this.isSupportFaceVisible(client, adminsky4) || !((d2 = adminsky4.getSquaredDistance(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z)) < d)) continue;
                d = d2;
                adminsky2 = adminsky4;
            }
        }
        return adminsky2;
    }

    private boolean isSupportFaceVisible(MinecraftClient client, BlockPos adminsky) {
        if (client.world == null) {
            return false;
        }
        if (!this.hasItemPlacementSpace(client, adminsky) || !this.hasItemPlacementSpace(client, adminsky.up())) {
            return false;
        }
        BlockPos adminsky2 = adminsky.down();
        return client.world.getBlockState(adminsky2).isSideSolidFullSquare((BlockView)client.world, adminsky2, Direction.UP);
    }

    private boolean hasItemPlacementSpace(MinecraftClient client, BlockPos adminsky) {
        return client.world.getBlockState(adminsky).getCollisionShape((BlockView)client.world, adminsky).isEmpty();
    }

    private static boolean hasClearLineOfSight(MinecraftClient client, Vec3d VanillaChestLootTableGenerator, BlockPos adminsky) {
        if (client.world == null || client.player == null) {
            return false;
        }
        RaycastContext class_39592 = new RaycastContext(client.player.getEyePos(), VanillaChestLootTableGenerator, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)client.player);
        BlockHitResult class_39652 = client.world.raycast(class_39592);
        return class_39652.getType() != HitResult.Type.BLOCK || class_39652.getBlockPos().equals(adminsky);
    }

    private static boolean isInteractionRayValid(MinecraftClient client, BlockPos adminsky) {
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

    private Direction findBestInteractionFace(MinecraftClient client, BlockPos adminsky) {
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

    private boolean confirmRotationToTarget(Vec3d VanillaChestLootTableGenerator) {
        Rotation rotation = AimRotationMath.getRotationToPoint(VanillaChestLootTableGenerator);
        if (this.lastTargetRotation == null || this.lastTargetRotation.angleDistanceTo(rotation) > 0.5f) {
            this.lastTargetRotation = rotation;
            this.rotationConfirmations = 0;
        }
        ClientServiceRegistry.getInstance().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, 180.0f, 180.0f, 180.0f, RotationPriority.ITEM_USE_PRIORITY);
        Rotation rotation2 = ClientServiceRegistry.getInstance().getRotationManager().getCurrentRotation();
        if (rotation2.angleDistanceTo(rotation) <= 1.0f) {
            ++this.rotationConfirmations;
            return this.rotationConfirmations >= 1;
        }
        return false;
    }

    private void resetRotationTracking() {
        this.lastTargetRotation = null;
        this.rotationConfirmations = 0;
    }

    private void cancelInteraction() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager != null) {
            client.interactionManager.cancelBlockBreaking();
        }
    }

    private boolean ensureTargetItemSelected(MinecraftClient client) {
        int n;
        ClientPlayerEntity class_7462 = client.player;
        if (class_7462 == null || this.targetItem == Items.AIR) {
            return false;
        }
        PlayerInventory class_16612 = class_7462.getInventory();
        if (class_16612.getStack(class_16612.selectedSlot).getItem() == this.targetItem) {
            return true;
        }
        for (n = 0; n < 9; ++n) {
            if (class_16612.getStack(n).getItem() != this.targetItem) continue;
            BlockDropResolver.setSelectedToolSlot(n);
            return true;
        }
        for (n = 9; n < 36; ++n) {
            if (class_16612.getStack(n).getItem() != this.targetItem) continue;
            client.interactionManager.clickSlot(class_7462.currentScreenHandler.syncId, n, class_16612.selectedSlot, SlotActionType.SWAP, (PlayerEntity)class_7462);
            return true;
        }
        return false;
    }

    @Override
    public void stopNavigation() {
        if (this.pathNavigator != null) {
            this.pathNavigator.stopNavigation();
            this.pathNavigator = null;
        }
        this.cancelInteraction();
        RotationState rotationState = ClientServiceRegistry.getInstance().getRotationState();
        rotationState.disableMovementOverride();
        this.currentPosition = null;
        this.resetRotationTracking();
    }

    @Override
    public void pauseNavigation() {
        this.paused = true;
        if (this.pathNavigator != null) {
            this.pathNavigator.pauseNavigation();
        }
        this.cancelInteraction();
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
        return this.targetReached && this.errorMessage == null;
    }

    @Override
    @Nullable
    public String getErrorMessage() {
        return this.errorMessage;
    }

    static final class InteractionTarget {
        final BlockPos blockPosition;
        final Direction clickFace;
        final Vec3d hitVector;

        InteractionTarget(BlockPos adminsky, Direction class_23502, Vec3d VanillaChestLootTableGenerator) {
            this.blockPosition = adminsky;
            this.clickFace = class_23502;
            this.hitVector = VanillaChestLootTableGenerator;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "blockPosition", "clickFace", "hitVector");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "blockPosition", "clickFace", "hitVector");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "blockPosition", "clickFace", "hitVector");
        }

        public BlockPos getBlockPosition() {
            return this.blockPosition;
        }

        public Direction getClickFace() {
            return this.clickFace;
        }

        public Vec3d getHitVector() {
            return this.hitVector;
        }
    }
}
