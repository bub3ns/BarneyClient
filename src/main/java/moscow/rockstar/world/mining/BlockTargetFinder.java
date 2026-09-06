/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  moscow.rockstar.modules.other.admin.BlockPos$Mutable
 *  net.minecraft.Entity
 *  net.minecraft.ItemEntity
 *  net.minecraft.Item
 *  net.minecraft.Items
 *  net.minecraft.Blocks
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
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.world.mining;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import moscow.rockstar.api.validation.ScreenStateService;
import moscow.rockstar.combat.rotation.RotationRequestHelper;
import moscow.rockstar.combat.rotation.RotationState;
import moscow.rockstar.core.ClientServiceRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.ui.notifications.NotificationBridge;
import moscow.rockstar.world.BlockDropResolver;
import moscow.rockstar.world.BlockRegion;
import moscow.rockstar.world.navigation.PathNavigator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class BlockTargetFinder
implements ScreenStateService {
    private static final int SEARCH_RADIUS = 48;
    private static final double MAX_TARGET_DISTANCE = 6.0;
    private final Block targetBlockType;
    private final Set<Item> targetItems;
    @Nullable
    private BlockPos currentTarget;
    @Nullable
    private PathNavigator pathNavigator;
    private boolean collectingDrops;
    private boolean breakingTarget;
    private final BlockBreakValidator blockBreakValidator = new BlockBreakValidator();
    private boolean paused;
    private boolean completed;
    private int failedAttempts;
    private final Set<BlockPos> skippedBlocks = new HashSet<BlockPos>();
    private final Set<Integer> collectedEntityIds = new HashSet<Integer>();
    @Nullable
    private Integer currentDropEntityId;

    public BlockTargetFinder(Block class_22482) {
        this.targetBlockType = class_22482;
        this.targetItems = BlockTargetFinder.getTargetItems(class_22482);
    }

    private static Set<Item> getTargetItems(Block class_22482) {
        HashSet<Item> hashSet = new HashSet<Item>();
        Item class_17922 = class_22482.asItem();
        if (class_17922 != Items.AIR) {
            hashSet.add(class_17922);
        }
        if (class_22482 == Blocks.STONE) {
            hashSet.add(Items.COBBLESTONE);
        } else if (class_22482 == Blocks.DEEPSLATE) {
            hashSet.add(Items.COBBLED_DEEPSLATE);
        } else if (class_22482 == Blocks.GRASS_BLOCK || class_22482 == Blocks.DIRT_PATH || class_22482 == Blocks.FARMLAND) {
            hashSet.add(Items.DIRT);
        } else if (class_22482 == Blocks.COAL_ORE || class_22482 == Blocks.DEEPSLATE_COAL_ORE) {
            hashSet.add(Items.COAL);
        } else if (class_22482 == Blocks.IRON_ORE || class_22482 == Blocks.DEEPSLATE_IRON_ORE) {
            hashSet.add(Items.RAW_IRON);
        } else if (class_22482 == Blocks.COPPER_ORE || class_22482 == Blocks.DEEPSLATE_COPPER_ORE) {
            hashSet.add(Items.RAW_COPPER);
        } else if (class_22482 == Blocks.GOLD_ORE || class_22482 == Blocks.DEEPSLATE_GOLD_ORE) {
            hashSet.add(Items.RAW_GOLD);
        } else if (class_22482 == Blocks.NETHER_GOLD_ORE) {
            hashSet.add(Items.GOLD_NUGGET);
        } else if (class_22482 == Blocks.NETHER_QUARTZ_ORE) {
            hashSet.add(Items.QUARTZ);
        } else if (class_22482 == Blocks.DIAMOND_ORE || class_22482 == Blocks.DEEPSLATE_DIAMOND_ORE) {
            hashSet.add(Items.DIAMOND);
        } else if (class_22482 == Blocks.EMERALD_ORE || class_22482 == Blocks.DEEPSLATE_EMERALD_ORE) {
            hashSet.add(Items.EMERALD);
        } else if (class_22482 == Blocks.LAPIS_ORE || class_22482 == Blocks.DEEPSLATE_LAPIS_ORE) {
            hashSet.add(Items.LAPIS_LAZULI);
        } else if (class_22482 == Blocks.REDSTONE_ORE || class_22482 == Blocks.DEEPSLATE_REDSTONE_ORE) {
            hashSet.add(Items.REDSTONE);
        }
        return hashSet;
    }

    @Override
    public String getCommandName() {
        return "mine " + Block.getRawIdFromState((BlockState)this.targetBlockType.getDefaultState());
    }

    @Override
    public String getStatusMessage() {
        if (this.paused) {
            return "\u043f\u0430\u0443\u0437\u0430";
        }
        if (this.breakingTarget) {
            return "\u043a\u043e\u043f\u0430\u0435\u043c " + String.valueOf(this.currentTarget);
        }
        if (this.collectingDrops) {
            return "\u043f\u043e\u0434\u0431\u0438\u0440\u0430\u0435\u043c \u0434\u0440\u043e\u043f\u044b";
        }
        if (this.pathNavigator != null) {
            return "\u0438\u0434\u0451\u043c \u043a " + String.valueOf(this.currentTarget) + " (" + this.pathNavigator.getStatusMessage() + ")";
        }
        return "\u0438\u0449\u0435\u043c \u0431\u043b\u043e\u043a";
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
        if (this.breakingTarget && this.currentTarget != null) {
            if (client.world.getBlockState(this.currentTarget).getBlock() != this.targetBlockType) {
                this.breakingTarget = false;
                this.currentTarget = null;
                this.stopBreaking();
                return false;
            }
            Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)this.currentTarget);
            double d = client.player.getEyePos().distanceTo(VanillaChestLootTableGenerator);
            if (d > 5.0) {
                this.breakingTarget = false;
                this.stopBreaking();
                this.pathNavigator = new PathNavigator(new BlockRegion(this.currentTarget, 2));
                this.collectingDrops = false;
                return false;
            }
            if (!BlockTargetFinder.hasLineOfSight(client, this.currentTarget)) {
                this.breakingTarget = false;
                this.stopBreaking();
                ++this.failedAttempts;
                if (this.failedAttempts >= 3) {
                    this.skippedBlocks.add(this.currentTarget);
                    this.currentTarget = null;
                    this.failedAttempts = 0;
                    return false;
                }
                this.pathNavigator = new PathNavigator(new BlockRegion(this.currentTarget, 1));
                this.collectingDrops = false;
                return false;
            }
            this.failedAttempts = 0;
            BlockDropResolver.isBreakable(client.world.getBlockState(this.currentTarget));
            Direction class_23502 = this.getBestBlockFace(client, this.currentTarget);
            if (!this.blockBreakValidator.canBreak(this.currentTarget, class_23502)) {
                return false;
            }
            client.interactionManager.updateBlockBreakingProgress(this.currentTarget, class_23502);
            client.player.swingHand(client.player.getActiveHand());
            return false;
        }
        if (this.pathNavigator != null) {
            boolean bl2 = this.pathNavigator.tickNavigation();
            if (bl2) {
                this.pathNavigator = null;
                if (this.collectingDrops) {
                    ItemEntity class_15422;
                    Entity class_12972;
                    if (this.currentDropEntityId != null && (class_12972 = client.world.getEntityById(this.currentDropEntityId.intValue())) instanceof ItemEntity && (class_15422 = (ItemEntity)class_12972).isAlive()) {
                        this.collectedEntityIds.add(this.currentDropEntityId);
                    }
                    this.currentDropEntityId = null;
                    this.collectingDrops = false;
                } else if (this.currentTarget != null) {
                    double d = client.player.getEyePos().distanceTo(Vec3d.ofCenter((Vec3i)this.currentTarget));
                    if (d > 5.0) {
                        this.skippedBlocks.add(this.currentTarget);
                        this.currentTarget = null;
                    } else {
                        this.breakingTarget = true;
                    }
                }
            }
            return false;
        }
        ItemEntity class_15423 = this.findNearbyDrop(client);
        if (class_15423 != null) {
            BlockPos adminsky = class_15423.getBlockPos();
            this.pathNavigator = new PathNavigator(new BlockRegion(adminsky, 1));
            this.collectingDrops = true;
            this.currentDropEntityId = class_15423.getId();
            return false;
        }
        BlockPos adminsky = this.findNextTargetBlock(client);
        if (adminsky == null) {
            NotificationBridge.showMessage("\u0411\u043b\u043e\u043a " + String.valueOf(this.targetBlockType) + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u043f\u043e\u0431\u043b\u0438\u0437\u043e\u0441\u0442\u0438");
            this.stopNavigation();
            this.completed = true;
            return true;
        }
        this.currentTarget = adminsky;
        Vec3d WallPlayerSkullBlock = Vec3d.ofCenter((Vec3i)adminsky);
        boolean bl3 = bl = client.player.getEyePos().distanceTo(WallPlayerSkullBlock) <= 4.5;
        if (bl && BlockTargetFinder.hasLineOfSight(client, adminsky)) {
            this.breakingTarget = true;
        } else {
            this.pathNavigator = new PathNavigator(new BlockRegion(adminsky, 2));
        }
        return false;
    }

    private static boolean hasLineOfSight(MinecraftClient client, BlockPos adminsky) {
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

    @Nullable
    private ItemEntity findNearbyDrop(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return null;
        }
        if (this.targetItems.isEmpty()) {
            return null;
        }
        Vec3d VanillaChestLootTableGenerator = client.player.getPos();
        Box HorizontalFacingBlock = Box.of((Vec3d)VanillaChestLootTableGenerator, (double)12.0, (double)12.0, (double)12.0);
        List<ItemEntity> nearbyDrops = client.world.getEntitiesByClass(ItemEntity.class, HorizontalFacingBlock, drop -> drop.isAlive() && !drop.cannotPickup() && !this.collectedEntityIds.contains(drop.getId()) && this.targetItems.contains(drop.getStack().getItem()));
        if (nearbyDrops.isEmpty()) {
            return null;
        }
        nearbyDrops.sort(Comparator.comparingDouble(drop -> drop.getPos().squaredDistanceTo(VanillaChestLootTableGenerator)));
        return nearbyDrops.get(0);
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

    @Nullable
    private BlockPos findNextTargetBlock(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return null;
        }
        BlockPos adminsky2 = client.player.getBlockPos();
        BlockPos adminsky3 = adminsky2.down();
        if (client.world.getBlockState(adminsky3).getBlock() == this.targetBlockType && !this.skippedBlocks.contains(adminsky3)) {
            return adminsky3;
        }
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        for (int i = -48; i <= 48; ++i) {
            for (int j = -48; j <= 48; ++j) {
                for (int k = -24; k <= 24; ++k) {
                    class_23392.set(adminsky2.getX() + i, adminsky2.getY() + k, adminsky2.getZ() + j);
                    if (client.world.getBlockState((BlockPos)class_23392).getBlock() != this.targetBlockType || this.skippedBlocks.contains(class_23392)) continue;
                    arrayList.add(class_23392.toImmutable());
                }
            }
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        Vec3d VanillaChestLootTableGenerator = client.player.getEyePos();
        arrayList.sort(Comparator.comparingDouble(adminsky -> Vec3d.ofCenter((Vec3i)adminsky).squaredDistanceTo(VanillaChestLootTableGenerator)));
        return (BlockPos)arrayList.get(0);
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
        this.currentTarget = null;
        this.breakingTarget = false;
        this.collectingDrops = false;
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

    @Generated
    public Block getTargetBlockType() {
        return this.targetBlockType;
    }
}
