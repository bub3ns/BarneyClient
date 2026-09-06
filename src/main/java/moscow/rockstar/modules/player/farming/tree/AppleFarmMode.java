/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.AxeItem
 *  net.minecraft.Item
 *  net.minecraft.HoeItem
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.BlockView
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.Direction
 *  net.minecraft.Vec3i
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockState
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  net.minecraft.RegistryKey
 */
package moscow.rockstar.modules.player.farming.tree;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.Event;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.items.rules.InventorySlotRule;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.Rotation;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.player.farming.core.AutoFarm;
import moscow.rockstar.modules.player.farming.core.FarmModeBase;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.BlockView;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.registry.RegistryKey;
import pyrock.events.player.ClientPlayerTickEvent;

public class AppleFarmMode
extends FarmModeBase {
    private static final int TREE_SCAN_RADIUS = 4;
    private static final int TREE_TARGET_RADIUS = 5;
    private static final float ROTATION_TOLERANCE = 0.1f;
    private static final int REQUIRED_ROTATION_CONFIRMATIONS = 2;
    private final NumberSetting bonemealDelaySetting;
    BlockPos saplingPosition;
    TreeOperationState treeOperationState;
    private int bonemealAttempts;
    private int rotationConfirmations;
    private Rotation targetRotation;
    private final Timer bonemealCooldown = new Timer();
    private final Timer growthWaitCooldown = new Timer();
    private boolean growthWaitActive;
    private static final long GROWTH_WAIT_TIMEOUT_MILLIS = 30000L;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = new EventListener<ClientPlayerTickEvent>(){

        public void onEvent(ClientPlayerTickEvent clientPlayerTickEvent) {
            if (ClientAccess.minecraftClient.player == null || ClientAccess.minecraftClient.world == null || AppleFarmMode.this.saplingPosition == null || AppleFarmMode.this.treeOperationState == null) {
                return;
            }
            if (!AppleFarmMode.this.isTreeTargetReady()) {
                return;
            }
            switch (AppleFarmMode.this.treeOperationState.ordinal()) {
                case 0: {
                    AppleFarmMode.this.plantSapling();
                    break;
                }
                case 1: {
                    AppleFarmMode.this.applyBonemeal();
                    break;
                }
                case 2: {
                    AppleFarmMode.this.processTreeBlocks(true);
                    break;
                }
                case 3: {
                    AppleFarmMode.this.processTreeBlocks(false);
                }
            }
        }

        @Override
        public int getPriority() {
            return -1;
        }

    };

    public AppleFarmMode(AutoFarm autoFarm, ModeSetting modeSetting) {
        super(autoFarm, modeSetting, "modules.settings.auto_farm.modes.apple");
        this.bonemealDelaySetting = new NumberSetting((SettingOwner)autoFarm, "modules.settings.auto_farm.apple.bonemeal_delay", () -> !this.isSelected()).setStep(10.0f).setMinValue(0.0f).setMaxValue(1000.0f).setValue(150.0f).setUnit("ms");
    }

    @Override
    public void startFarmAutomation() {
        if (AppleFarmMode.minecraftClient.player == null || AppleFarmMode.minecraftClient.world == null) {
            this.disableAutoFarm();
            return;
        }
        List<BlockPos> list = this.findSaplingPositions();
        if (list.isEmpty()) {
            this.showError("modules.apple_farm.no_dirt");
            this.disableAutoFarm();
            return;
        }
        if (list.size() > 1) {
            this.showWarning("modules.apple_farm.multiple_dirt");
        }
        if (!this.hasItemInInventory(Items.OAK_SAPLING)) {
            this.showError("modules.apple_farm.no_sapling");
            this.disableAutoFarm();
            return;
        }
        if (!this.hasItemInInventory(Items.BONE_MEAL)) {
            this.showError("modules.apple_farm.no_bonemeal");
            this.disableAutoFarm();
            return;
        }
        if (!this.hasMatchingToolInInventory(class_17992 -> class_17992.getItem() instanceof HoeItem)) {
            this.showWarning("modules.apple_farm.no_hoe_recommend");
        }
        this.saplingPosition = this.selectNearestSapling(list);
        this.setTreeOperationState(TreeOperationState.PLANTING);
        this.bonemealAttempts = 0;
    }

    private BlockPos selectNearestSapling(List<BlockPos> list) {
        Vec3d VanillaChestLootTableGenerator = AppleFarmMode.minecraftClient.player.getEyePos();
        BlockPos adminsky = list.getFirst();
        double d = Double.MAX_VALUE;
        for (BlockPos adminsky2 : list) {
            double d2 = VanillaChestLootTableGenerator.squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky2));
            if (!(d2 < d)) continue;
            d = d2;
            adminsky = adminsky2;
        }
        return adminsky;
    }

    @Override
    public void resetBrewingState() {
        this.saplingPosition = null;
        this.treeOperationState = null;
        this.targetRotation = null;
        this.rotationConfirmations = 0;
    }

    boolean isTreeTargetReady() {
        BlockState class_26802 = AppleFarmMode.minecraftClient.world.getBlockState(this.saplingPosition);
        Block class_22482 = class_26802.getBlock();
        if (class_22482 == Blocks.GRASS_BLOCK || class_22482 == Blocks.DIRT) {
            return true;
        }
        if (!class_26802.isAir()) {
            return true;
        }
        Predicate<ItemStack> predicate = class_17992 -> class_17992.getItem() == Items.DIRT || class_17992.getItem() == Items.GRASS_BLOCK;
        if (!this.hasMatchingItem(predicate)) {
            this.showError("modules.apple_farm.no_dirt_item");
            this.disableAutoFarm();
            return false;
        }
        Direction[] class_2350Array = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP};
        double d = AppleFarmMode.minecraftClient.player.getBlockInteractionRange();
        double d2 = d * d;
        for (Direction class_23502 : class_2350Array) {
            BlockPos adminsky = this.saplingPosition.offset(class_23502);
            Direction class_23503 = class_23502.getOpposite();
            BlockState class_26803 = AppleFarmMode.minecraftClient.world.getBlockState(adminsky);
            if (!class_26803.isSideSolidFullSquare((BlockView)AppleFarmMode.minecraftClient.world, adminsky, class_23503)) continue;
            Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)adminsky).add((double)class_23503.getOffsetX() * 0.5, (double)class_23503.getOffsetY() * 0.5, (double)class_23503.getOffsetZ() * 0.5);
            if (AppleFarmMode.minecraftClient.player.getEyePos().squaredDistanceTo(VanillaChestLootTableGenerator) > d2) continue;
            if (!this.isRotationOnTarget(VanillaChestLootTableGenerator)) {
                return false;
            }
            BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, class_23503, adminsky, false);
            AppleFarmMode.minecraftClient.interactionManager.interactBlock(AppleFarmMode.minecraftClient.player, Hand.MAIN_HAND, class_39652);
            AppleFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
            return false;
        }
        this.showError("modules.apple_farm.no_dirt_support");
        this.disableAutoFarm();
        return false;
    }

    void plantSapling() {
        BlockPos adminsky = this.saplingPosition.up();
        Block class_22482 = AppleFarmMode.minecraftClient.world.getBlockState(adminsky).getBlock();
        if (class_22482 == Blocks.OAK_SAPLING) {
            this.setTreeOperationState(TreeOperationState.GROWING);
            this.bonemealAttempts = 0;
            return;
        }
        if (!AppleFarmMode.minecraftClient.world.getBlockState(adminsky).isAir()) {
            this.setTreeOperationState(TreeOperationState.CLEARING);
            return;
        }
        if (!this.hasItem(Items.OAK_SAPLING)) {
            this.showError("modules.apple_farm.no_sapling");
            this.disableAutoFarm();
            return;
        }
        Vec3d VanillaChestLootTableGenerator = new Vec3d((double)this.saplingPosition.getX() + 0.5, (double)this.saplingPosition.getY() + 1.0, (double)this.saplingPosition.getZ() + 0.5);
        if (!this.isRotationOnTarget(VanillaChestLootTableGenerator)) {
            return;
        }
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, this.saplingPosition, false);
        AppleFarmMode.minecraftClient.interactionManager.interactBlock(AppleFarmMode.minecraftClient.player, Hand.MAIN_HAND, class_39652);
        AppleFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
    }

    void applyBonemeal() {
        BlockPos adminsky = this.saplingPosition.up();
        BlockState class_26802 = AppleFarmMode.minecraftClient.world.getBlockState(adminsky);
        Block class_22482 = class_26802.getBlock();
        if (this.hasTreeGrowthSpace(adminsky)) {
            this.setTreeOperationState(TreeOperationState.CLEARING);
            return;
        }
        if (class_22482 != Blocks.OAK_SAPLING) {
            this.setTreeOperationState(TreeOperationState.PLANTING);
            return;
        }
        if (this.bonemealAttempts >= 40) {
            this.showError("modules.apple_farm.no_grow_space");
            this.disableAutoFarm();
            return;
        }
        if (!this.hasItem(Items.BONE_MEAL)) {
            this.showError("modules.apple_farm.no_bonemeal");
            this.disableAutoFarm();
            return;
        }
        Vec3d VanillaChestLootTableGenerator = new Vec3d((double)adminsky.getX() + 0.5, (double)adminsky.getY() + 1.0, (double)adminsky.getZ() + 0.5);
        if (!this.isRotationOnTarget(VanillaChestLootTableGenerator)) {
            return;
        }
        if (!this.bonemealCooldown.hasElapsed((long)this.bonemealDelaySetting.getValue())) {
            return;
        }
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky, false);
        AppleFarmMode.minecraftClient.interactionManager.interactBlock(AppleFarmMode.minecraftClient.player, Hand.MAIN_HAND, class_39652);
        AppleFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        ++this.bonemealAttempts;
        this.bonemealCooldown.reset();
    }

    void processTreeBlocks(boolean bl) {
        Direction class_23502;
        Vec3d VanillaChestLootTableGenerator;
        Predicate<Block> predicate = bl ? AppleFarmMode::isTreeLeavesBlock : AppleFarmMode::isTreeLogBlock;
        BlockPos adminsky = this.findNearestTreeBlock(predicate);
        if (adminsky == null) {
            if (bl) {
                if (this.findNearestTreeBlock(AppleFarmMode::isTreeLogBlock) != null) {
                    this.setTreeOperationState(TreeOperationState.CLEARING);
                    return;
                }
                if (this.hasItemMatchingPredicate(AppleFarmMode::isTreeLeavesBlock)) {
                    if (!this.growthWaitActive) {
                        this.growthWaitActive = true;
                        this.growthWaitCooldown.reset();
                        return;
                    }
                    if (!this.growthWaitCooldown.hasElapsed(30000L)) {
                        return;
                    }
                }
                this.growthWaitActive = false;
                this.setTreeOperationState(TreeOperationState.PLANTING);
            } else {
                this.setTreeOperationState(TreeOperationState.HARVESTING);
            }
            return;
        }
        this.growthWaitActive = false;
        BlockState class_26802 = AppleFarmMode.minecraftClient.world.getBlockState(adminsky);
        if (bl) {
            if (!this.prepareToolForBlock(class_26802)) {
                return;
            }
        } else {
            this.equipBestToolForBlock(class_26802, class_17992 -> class_17992.getItem() instanceof AxeItem);
        }
        if (!this.isRotationOnTarget(VanillaChestLootTableGenerator = this.getBlockHitPosition(adminsky, class_23502 = this.getClosestBlockFace(adminsky)))) {
            return;
        }
        AppleFarmMode.minecraftClient.interactionManager.updateBlockBreakingProgress(adminsky, class_23502);
        AppleFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean isRotationOnTarget(Vec3d VanillaChestLootTableGenerator) {
        Rotation rotation = AimRotationMath.getRotationToPoint(VanillaChestLootTableGenerator);
        if (this.targetRotation == null || this.targetRotation.angleDistanceTo(rotation) > 0.5f) {
            this.targetRotation = rotation;
            this.rotationConfirmations = 0;
        }
        RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, 180.0f, 180.0f, 180.0f, RotationPriority.ITEM_USE_PRIORITY);
        Rotation rotation2 = RockstarClient.create().getRotationManager().getPacketRotation();
        if (rotation2.angleDistanceTo(rotation) <= 1.0f) {
            ++this.rotationConfirmations;
            return this.rotationConfirmations >= 2;
        }
        return false;
    }

    private void setTreeOperationState(TreeOperationState treeOperationState) {
        this.treeOperationState = treeOperationState;
        this.targetRotation = null;
        this.rotationConfirmations = 0;
        this.growthWaitActive = false;
    }

    private boolean hasItem(Item class_17922) {
        return this.hasMatchingItem(class_17992 -> class_17992.getItem() == class_17922);
    }

    private boolean hasMatchingItem(Predicate<ItemStack> predicate) {
        if (predicate.test(AppleFarmMode.minecraftClient.player.getMainHandStack())) {
            return true;
        }
        HotbarSlot hotbarSlot = ItemRuleSets.getHotbarRules().findByStack(predicate);
        if (hotbarSlot != null) {
            InventoryUtils.setSelectedHotbarSlot(hotbarSlot);
            return true;
        }
        InventorySlotRule inventorySlotRule = ItemRuleSets.getInventoryRules().findByStack(predicate);
        if (inventorySlotRule != null) {
            int n = AppleFarmMode.minecraftClient.player.getInventory().selectedSlot;
            InventoryUtils.dropItem(inventorySlotRule.getClickSlot(), n);
            return true;
        }
        return false;
    }

    private boolean prepareToolForBlock(BlockState class_26802) {
        boolean bl;
        Predicate<ItemStack> predicate = class_17992 -> class_17992.getItem() instanceof HoeItem;
        Predicate<ItemStack> predicate2 = predicate.and(AppleFarmMode::hasDurabilityRemaining);
        if (this.selectBestToolForBlock(class_26802, predicate2, true)) {
            return true;
        }
        boolean bl2 = bl = ItemRuleSets.getHotbarRules().findByStack(predicate) != null || ItemRuleSets.getInventoryRules().findByStack(predicate) != null;
        if (bl) {
            this.showError("modules.apple_farm.no_fresh_hoe");
            this.disableAutoFarm();
            return false;
        }
        return true;
    }

    private static boolean hasDurabilityRemaining(ItemStack class_17992) {
        if (class_17992.isEmpty() || !class_17992.isDamageable()) {
            return true;
        }
        int n = class_17992.getMaxDamage();
        if (n <= 0) {
            return true;
        }
        float f = (float)(n - class_17992.getDamage()) / (float)n;
        return f > 0.1f;
    }

    private void equipBestToolForBlock(BlockState class_26802, Predicate<ItemStack> predicate) {
        this.selectBestToolForBlock(class_26802, predicate, false);
    }

    private boolean selectBestToolForBlock(BlockState class_26802, Predicate<ItemStack> predicate, boolean bl) {
        ItemStack class_17992 = AppleFarmMode.minecraftClient.player.getMainHandStack();
        ToolMiningStats toolMiningStats = this.getToolMiningStats(class_17992, class_26802, predicate, bl);
        ItemRule itemRule = null;
        for (ItemRule itemRule2 : ItemRuleSets.getHotbarRules().combineRules(ItemRuleSets.getInventoryRules()).getRules()) {
            ItemStack class_17993 = itemRule2.getItemStack();
            ToolMiningStats toolMiningStats2 = this.getToolMiningStats(class_17993, class_26802, predicate, bl);
            if (!toolMiningStats2.isBetterThan(toolMiningStats)) continue;
            toolMiningStats = toolMiningStats2;
            itemRule = itemRule2;
        }
        if (!toolMiningStats.isUsable()) {
            return false;
        }
        if (itemRule instanceof HotbarSlot) {
            HotbarSlot hotbarSlot = (HotbarSlot)itemRule;
            InventoryUtils.setSelectedHotbarSlot(hotbarSlot);
        } else if (itemRule instanceof InventorySlotRule) {
            ItemRule itemRule2;
            itemRule2 = (InventorySlotRule)itemRule;
            int n = AppleFarmMode.minecraftClient.player.getInventory().selectedSlot;
            InventoryUtils.dropItem(((InventorySlotRule)itemRule2).getClickSlot(), n);
        }
        return true;
    }

    private ToolMiningStats getToolMiningStats(ItemStack class_17992, BlockState class_26802, Predicate<ItemStack> predicate, boolean bl) {
        if (class_17992 == null || class_17992.isEmpty() || !predicate.test(class_17992)) {
            return new ToolMiningStats(-1.0f, -1, -1, -1.0f);
        }
        float f = class_17992.getMiningSpeedMultiplier(class_26802);
        int n = EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.EFFICIENCY);
        if (f > 1.0f && n > 0) {
            f += (float)(n * n) + 1.0f;
        }
        int n2 = bl ? EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.FORTUNE) : 0;
        float f2 = this.getDurabilityRatio(class_17992);
        return new ToolMiningStats(f, n, n2, f2);
    }

    private float getDurabilityRatio(ItemStack class_17992) {
        if (class_17992.isEmpty() || !class_17992.isDamageable()) {
            return 1.0f;
        }
        int n = class_17992.getMaxDamage();
        if (n <= 0) {
            return 1.0f;
        }
        return (float)(n - class_17992.getDamage()) / (float)n;
    }

    private List<BlockPos> findSaplingPositions() {
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        BlockPos adminsky = AppleFarmMode.minecraftClient.player.getBlockPos();
        Vec3d VanillaChestLootTableGenerator = AppleFarmMode.minecraftClient.player.getEyePos();
        double d = 16.0;
        for (int i = -4; i <= 4; ++i) {
            for (int j = -4; j <= 4; ++j) {
                for (int k = -4; k <= 4; ++k) {
                    BlockPos adminsky2 = adminsky.add(i, j, k);
                    if (!this.isSaplingPosition(adminsky2) || VanillaChestLootTableGenerator.squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky2.up())) > d * 4.0 || Vec3d.ofCenter((Vec3i)adminsky2).squaredDistanceTo(AppleFarmMode.minecraftClient.player.getPos()) > d * 4.0) continue;
                    arrayList.add(adminsky2);
                }
            }
        }
        return arrayList;
    }

    private boolean isSaplingPosition(BlockPos adminsky) {
        Block class_22482 = AppleFarmMode.minecraftClient.world.getBlockState(adminsky).getBlock();
        if (class_22482 != Blocks.GRASS_BLOCK && class_22482 != Blocks.DIRT) {
            return false;
        }
        return AppleFarmMode.minecraftClient.world.getBlockState(adminsky.up()).isAir();
    }

    private boolean hasTreeGrowthSpace(BlockPos adminsky) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = 0; k <= 12; ++k) {
                    if (!AppleFarmMode.isTreeLogBlock(AppleFarmMode.minecraftClient.world.getBlockState(adminsky.add(i, k, j)).getBlock())) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasItemMatchingPredicate(Predicate<Block> predicate) {
        BlockPos adminsky = this.saplingPosition.up();
        for (int i = 0; i <= 12; ++i) {
            for (int j = -5; j <= 5; ++j) {
                for (int k = -5; k <= 5; ++k) {
                    if (!predicate.test(AppleFarmMode.minecraftClient.world.getBlockState(adminsky.add(j, i, k)).getBlock())) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private BlockPos findNearestTreeBlock(Predicate<Block> predicate) {
        BlockPos adminsky = this.saplingPosition.up();
        Vec3d VanillaChestLootTableGenerator = AppleFarmMode.minecraftClient.player.getEyePos();
        double d = AppleFarmMode.minecraftClient.player.getBlockInteractionRange();
        double d2 = d * d;
        BlockPos adminsky2 = null;
        int n = Integer.MAX_VALUE;
        double d3 = Double.MAX_VALUE;
        for (int i = 0; i <= 12; ++i) {
            for (int j = -5; j <= 5; ++j) {
                for (int k = -5; k <= 5; ++k) {
                    int n2;
                    Direction class_23502;
                    Vec3d WallPlayerSkullBlock;
                    double d4;
                    BlockPos adminsky3 = adminsky.add(j, i, k);
                    if (!predicate.test(AppleFarmMode.minecraftClient.world.getBlockState(adminsky3).getBlock()) || (d4 = VanillaChestLootTableGenerator.squaredDistanceTo(WallPlayerSkullBlock = this.getBlockHitPosition(adminsky3, class_23502 = this.getClosestBlockFace(adminsky3)))) > d2 || !this.hasLineOfSightToBlock(VanillaChestLootTableGenerator, WallPlayerSkullBlock, adminsky3) || (n2 = adminsky3.getY()) >= n && (n2 != n || !(d4 < d3))) continue;
                    n = n2;
                    d3 = d4;
                    adminsky2 = adminsky3;
                }
            }
        }
        return adminsky2;
    }

    private boolean hasLineOfSightToBlock(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, BlockPos adminsky) {
        BlockHitResult class_39652 = AppleFarmMode.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, WallPlayerSkullBlock, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)AppleFarmMode.minecraftClient.player));
        if (class_39652.getType() != HitResult.Type.BLOCK) {
            return true;
        }
        return class_39652.getBlockPos().equals(adminsky);
    }

    private Direction getClosestBlockFace(BlockPos adminsky) {
        Vec3d VanillaChestLootTableGenerator = AppleFarmMode.minecraftClient.player.getEyePos();
        Vec3d WallPlayerSkullBlock = Vec3d.ofCenter((Vec3i)adminsky);
        double d = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d2 = VanillaChestLootTableGenerator.y - WallPlayerSkullBlock.y;
        double d3 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        double d4 = Math.abs(d);
        double d5 = Math.abs(d2);
        double d6 = Math.abs(d3);
        if (d5 >= d4 && d5 >= d6) {
            return d2 >= 0.0 ? Direction.UP : Direction.DOWN;
        }
        if (d4 >= d6) {
            return d >= 0.0 ? Direction.EAST : Direction.WEST;
        }
        return d3 >= 0.0 ? Direction.SOUTH : Direction.NORTH;
    }

    private Vec3d getBlockHitPosition(BlockPos adminsky, Direction class_23502) {
        return Vec3d.ofCenter((Vec3i)adminsky).add((double)class_23502.getOffsetX() * 0.5, (double)class_23502.getOffsetY() * 0.5, (double)class_23502.getOffsetZ() * 0.5);
    }

    private boolean hasItemInInventory(Item class_17922) {
        return ItemRuleSets.getHotbarRules().containsItem(class_17922) || ItemRuleSets.getInventoryRules().containsItem(class_17922);
    }

    private boolean hasMatchingToolInInventory(Predicate<ItemStack> predicate) {
        return ItemRuleSets.getHotbarRules().findByStack(predicate) != null || ItemRuleSets.getInventoryRules().findByStack(predicate) != null;
    }

    private static boolean isTreeLogBlock(Block class_22482) {
        return class_22482 == Blocks.OAK_LOG;
    }

    private static boolean isTreeLeavesBlock(Block class_22482) {
        return class_22482 == Blocks.OAK_LEAVES;
    }

    static enum TreeOperationState {
        PLANTING,
        GROWING,
        HARVESTING,
        CLEARING;
}

    static final class ToolMiningStats {
        private final float miningSpeed;
        private final int efficiencyLevel;
        private final int fortuneLevel;
        private final float durabilityRatio;

        ToolMiningStats(float f, int n, int n2, float f2) {
            this.miningSpeed = f;
            this.efficiencyLevel = n;
            this.fortuneLevel = n2;
            this.durabilityRatio = f2;
        }

        boolean isUsable() {
            return this.miningSpeed >= 0.0f;
        }

        boolean isBetterThan(ToolMiningStats toolMiningStats) {
            if (this.miningSpeed != toolMiningStats.miningSpeed) {
                return this.miningSpeed > toolMiningStats.miningSpeed;
            }
            if (this.efficiencyLevel != toolMiningStats.efficiencyLevel) {
                return this.efficiencyLevel > toolMiningStats.efficiencyLevel;
            }
            if (this.fortuneLevel != toolMiningStats.fortuneLevel) {
                return this.fortuneLevel > toolMiningStats.fortuneLevel;
            }
            return this.durabilityRatio > toolMiningStats.durabilityRatio;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "miningSpeed", "efficiencyLevel", "fortuneLevel", "durabilityRatio");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "miningSpeed", "efficiencyLevel", "fortuneLevel", "durabilityRatio");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "miningSpeed", "efficiencyLevel", "fortuneLevel", "durabilityRatio");
        }

        public float getMiningSpeed() {
            return this.miningSpeed;
        }

        public int getEfficiencyLevel() {
            return this.efficiencyLevel;
        }

        public int getFortuneLevel() {
            return this.fortuneLevel;
        }

        public float getDurabilityRatio() {
            return this.durabilityRatio;
        }
    }
}

