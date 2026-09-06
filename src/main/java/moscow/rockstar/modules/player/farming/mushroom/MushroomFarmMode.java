/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.ItemEntity
 *  net.minecraft.PlayerInventory
 *  net.minecraft.ScreenHandler
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.Slot
 *  net.minecraft.AxeItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.BlockView
 *  net.minecraft.ItemConvertible
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.Direction
 *  net.minecraft.DefaultedList
 *  net.minecraft.Position
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3d
 *  net.minecraft.ChestBlockEntity
 *  net.minecraft.BlockState
 *  net.minecraft.BlockHitResult
 *  net.minecraft.RegistryKey
 */
package moscow.rockstar.modules.player.farming.mushroom;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import moscow.rockstar.modules.player.farming.core.FarmState;
import moscow.rockstar.render.texture.TextureReloadProvider;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import moscow.rockstar.world.BlockOffset;
import moscow.rockstar.world.BlockRegion;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.BlockView;
import net.minecraft.item.ItemConvertible;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.registry.RegistryKey;
import pyrock.events.player.ClientPlayerTickEvent;

public class MushroomFarmMode
extends FarmModeBase {
    private static final int MAX_INITIAL_PLANTING_ATTEMPTS = 4;
    private static final int MAX_MUSHROOM_SCAN_ROWS = 7;
    private static final int MAX_MUSHROOM_SCAN_COLUMNS = 12;
    private static final int MAX_GROWTH_SEARCH_HEIGHT = 64;
    private static final int MAX_CONTAINER_RETRY_COUNT = 20;
    private static final long DROP_NAVIGATION_TIMEOUT_MILLIS = 30000L;
    private static final float ROTATION_TOLERANCE = 1.0f;
    private static final int MAX_PLANTING_ATTEMPTS = 20;
    private static final double MUSHROOM_INTERACTION_DISTANCE_SQUARED = 1.9599999999999997;
    private static final long CONTAINER_OPEN_DELAY_MILLIS = 60L;
    private static final long CONTAINER_ACTION_DELAY_MILLIS = 50L;
    private final BooleanSetting pickupMushroomsSetting;
    private final BooleanSetting autoDepositSetting;
    private final BooleanSetting autoRefillSetting;
    private static final int MAX_CONTAINER_SLOTS = 192;
    private static final int MAX_CONTAINER_ITEM_COUNT = 16;
    BlockPos farmBasePosition;
    private BlockPos playerStartPosition;
    MushroomAutomationState automationState;
    private int plantingAttempts;
    private final List<ItemEntity> nearbyMushroomEntities = new ArrayList<ItemEntity>();
    private ItemEntity targetMushroomEntity;
    private final Set<BlockPos> unreachableContainerPositions = new HashSet<BlockPos>();
    private BlockPos targetContainerPosition;
    private ContainerNavigationMode containerNavigationMode = ContainerNavigationMode.DIRECT_ACCESS;
    private int containerSlotIndex = -1;
    private int containerItemCount = -1;
    private int lowestDropCount = Integer.MAX_VALUE;
    private long dropNavigationStartedAt;
    private boolean navigationActive;
    private long containerNavigationStartedAt;
    private final Timer growthActionCooldown = new Timer();
    private final Timer containerActionCooldown = new Timer();
    private Rotation targetRotation;
    private int rotationSuccessCount;
    private int rotationAttemptCount;
    private int mushroomPlantingCount;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = new EventListener<ClientPlayerTickEvent>(){

        public void onEvent(ClientPlayerTickEvent clientPlayerTickEvent) {
            if (ClientAccess.minecraftClient.player == null || ClientAccess.minecraftClient.world == null || MushroomFarmMode.this.farmBasePosition == null || MushroomFarmMode.this.automationState == null) {
                return;
            }
            switch (MushroomFarmMode.this.automationState.ordinal()) {
                case 0: {
                    MushroomFarmMode.this.plantMushroom();
                    break;
                }
                case 1: {
                    MushroomFarmMode.this.growMushroom();
                    break;
                }
                case 2: {
                    MushroomFarmMode.this.harvestMushroom();
                    break;
                }
                case 3: {
                    MushroomFarmMode.this.navigateToMushroomDrop();
                    break;
                }
                case 4: {
                    MushroomFarmMode.this.navigateToContainer();
                    break;
                }
                case 5: {
                    MushroomFarmMode.this.openContainerScreen();
                    break;
                }
                case 6: {
                    MushroomFarmMode.this.depositMushrooms();
                    break;
                }
                case 7: {
                    MushroomFarmMode.this.withdrawBonemeal();
                    break;
                }
                case 8: {
                    MushroomFarmMode.this.navigateToMushroomFarm();
                }
            }
        }

        @Override
        public int getPriority() {
            return -1;
        }

    };

    public MushroomFarmMode(AutoFarm autoFarm, ModeSetting modeSetting) {
        super(autoFarm, modeSetting, "modules.settings.auto_farm.modes.mushroom");
        this.pickupMushroomsSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.auto_farm.mushroom.pickup", () -> !this.isSelected()).enable();
        this.autoDepositSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.auto_farm.mushroom.auto_deposit", () -> !this.isSelected());
        this.autoRefillSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.auto_farm.mushroom.auto_refill", () -> !this.isSelected()).enable();
    }

    @Override
    public void startFarmAutomation() {
        if (MushroomFarmMode.minecraftClient.player == null || MushroomFarmMode.minecraftClient.world == null) {
            this.disableAutoFarm();
            return;
        }
        BlockPos adminsky = this.findNearestPlantingPosition();
        if (adminsky == null) {
            this.showError("modules.mushroom_farm.no_dirt");
            this.disableAutoFarm();
            return;
        }
        if (!this.isAvailableItem(Items.BROWN_MUSHROOM)) {
            this.showError("modules.mushroom_farm.no_mushroom");
            this.disableAutoFarm();
            return;
        }
        if (!this.isAvailableItem(Items.BONE_MEAL)) {
            this.showError("modules.mushroom_farm.no_bonemeal");
            this.disableAutoFarm();
            return;
        }
        if (!this.hasAvailableMatchingItem(class_17992 -> class_17992.getItem() instanceof AxeItem)) {
            this.showWarning("modules.mushroom_farm.no_axe_recommend");
        }
        this.farmBasePosition = adminsky;
        this.playerStartPosition = MushroomFarmMode.minecraftClient.player.getBlockPos();
        this.plantingAttempts = 0;
        this.mushroomPlantingCount = 0;
        this.lowestDropCount = Integer.MAX_VALUE;
        this.unreachableContainerPositions.clear();
        this.setAutomationState(MushroomAutomationState.PLANTING_MUSHROOM);
    }

    @Override
    public void resetBrewingState() {
        this.cancelNavigation();
        this.nearbyMushroomEntities.clear();
        this.unreachableContainerPositions.clear();
        this.targetMushroomEntity = null;
        this.targetContainerPosition = null;
        this.farmBasePosition = null;
        this.playerStartPosition = null;
        this.automationState = null;
        this.resetRotationState();
    }

    @Override
    public FarmState getFarmState() {
        if (this.automationState == null) {
            return FarmState.IDLE;
        }
        return switch (this.automationState.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> FarmState.PLANTING;
            case 1 -> FarmState.GROWING;
            case 2 -> FarmState.WORKING;
            case 3 -> FarmState.PICKUP;
            case 4, 8 -> FarmState.MOVING;
            case 5, 6 -> FarmState.DEPOSIT;
            case 7 -> FarmState.RESTOCKING;
        };
    }

    @Override
    public ItemStack getFarmDisplayItem() {
        return new ItemStack((ItemConvertible)Items.BROWN_MUSHROOM);
    }

    void plantMushroom() {
        if (!this.prepareMushroomPlantingSurface()) {
            return;
        }
        BlockPos adminsky = this.farmBasePosition.up();
        if (this.hasMushroomGrowthSpace()) {
            this.lowestDropCount = Integer.MAX_VALUE;
            this.setAutomationState(MushroomAutomationState.HARVESTING_MUSHROOM);
            return;
        }
        Block class_22482 = MushroomFarmMode.minecraftClient.world.getBlockState(adminsky).getBlock();
        if (class_22482 == Blocks.BROWN_MUSHROOM) {
            this.plantingAttempts = 0;
            this.mushroomPlantingCount = 0;
            this.setAutomationState(MushroomAutomationState.GROWING_MUSHROOM);
            return;
        }
        if (!MushroomFarmMode.minecraftClient.world.getBlockState(adminsky).isAir()) {
            this.setAutomationState(MushroomAutomationState.HARVESTING_MUSHROOM);
            return;
        }
        if (!this.isAvailableItem(Items.BROWN_MUSHROOM)) {
            if (this.prepareContainerRestock()) {
                return;
            }
            this.showError("modules.mushroom_farm.no_mushroom");
            this.disableAutoFarm();
            return;
        }
        if (!this.selectFarmItem(Items.BROWN_MUSHROOM)) {
            this.showError("modules.mushroom_farm.no_mushroom");
            this.disableAutoFarm();
            return;
        }
        if (this.mushroomPlantingCount >= 20) {
            this.showError("modules.mushroom_farm.cant_place");
            this.disableAutoFarm();
            return;
        }
        Vec3d VanillaChestLootTableGenerator = new Vec3d((double)this.farmBasePosition.getX() + 0.5, (double)this.farmBasePosition.getY() + 1.0, (double)this.farmBasePosition.getZ() + 0.5);
        if (!this.isRotationAligned(VanillaChestLootTableGenerator)) {
            return;
        }
        if (!this.containerActionCooldown.hasElapsed(50L)) {
            return;
        }
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, this.farmBasePosition, false);
        MushroomFarmMode.minecraftClient.interactionManager.interactBlock(MushroomFarmMode.minecraftClient.player, Hand.MAIN_HAND, class_39652);
        MushroomFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        this.containerActionCooldown.reset();
        ++this.mushroomPlantingCount;
    }

    void growMushroom() {
        if (!this.prepareMushroomPlantingSurface()) {
            return;
        }
        if (this.hasMushroomGrowthSpace()) {
            this.lowestDropCount = Integer.MAX_VALUE;
            this.setAutomationState(MushroomAutomationState.HARVESTING_MUSHROOM);
            return;
        }
        BlockPos adminsky = this.farmBasePosition.up();
        if (MushroomFarmMode.minecraftClient.world.getBlockState(adminsky).getBlock() != Blocks.BROWN_MUSHROOM) {
            this.setAutomationState(MushroomAutomationState.PLANTING_MUSHROOM);
            return;
        }
        if (this.plantingAttempts >= 64) {
            this.showError("modules.mushroom_farm.no_grow_space");
            this.disableAutoFarm();
            return;
        }
        if (!this.isAvailableItem(Items.BONE_MEAL)) {
            if (this.prepareContainerRestock()) {
                return;
            }
            this.showError("modules.mushroom_farm.no_bonemeal");
            this.disableAutoFarm();
            return;
        }
        if (!this.selectFarmItem(Items.BONE_MEAL)) {
            this.showError("modules.mushroom_farm.no_bonemeal");
            this.disableAutoFarm();
            return;
        }
        Vec3d VanillaChestLootTableGenerator = new Vec3d((double)adminsky.getX() + 0.5, (double)adminsky.getY() + 0.5, (double)adminsky.getZ() + 0.5);
        if (!this.isRotationAligned(VanillaChestLootTableGenerator)) {
            return;
        }
        if (!this.growthActionCooldown.hasElapsed(60L)) {
            return;
        }
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky, false);
        MushroomFarmMode.minecraftClient.interactionManager.interactBlock(MushroomFarmMode.minecraftClient.player, Hand.MAIN_HAND, class_39652);
        MushroomFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        ++this.plantingAttempts;
        this.growthActionCooldown.reset();
    }

    void harvestMushroom() {
        BlockPos adminsky;
        int n = this.countGrowthBlocks();
        if (n == 0) {
            this.cancelNavigation();
            this.prepareDropCollection();
            return;
        }
        if (this.lowestDropCount == Integer.MAX_VALUE || n < this.lowestDropCount) {
            this.lowestDropCount = n;
            this.dropNavigationStartedAt = System.currentTimeMillis();
        }
        if ((adminsky = this.findLowestGrowthBlock()) != null) {
            this.cancelNavigation();
            this.selectBestAxe(MushroomFarmMode.minecraftClient.world.getBlockState(adminsky));
            Direction class_23502 = this.getClosestInteractionSide(adminsky);
            Vec3d VanillaChestLootTableGenerator = this.getBlockHitPosition(adminsky, class_23502);
            if (!this.isRotationAligned(VanillaChestLootTableGenerator)) {
                return;
            }
            if (!this.containerActionCooldown.hasElapsed(50L)) {
                return;
            }
            MushroomFarmMode.minecraftClient.interactionManager.attackBlock(adminsky, class_23502);
            MushroomFarmMode.minecraftClient.interactionManager.updateBlockBreakingProgress(adminsky, class_23502);
            MushroomFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
            this.containerActionCooldown.reset();
            return;
        }
        if (!this.isNavigationServiceReady()) {
            this.showWarning("modules.mushroom_farm.unreachable");
            this.disableAutoFarm();
            return;
        }
        if (System.currentTimeMillis() - this.dropNavigationStartedAt > 30000L) {
            this.cancelNavigation();
            this.showWarning("modules.mushroom_farm.unreachable");
            this.disableAutoFarm();
            return;
        }
        BlockPos adminsky2 = this.findNearestGrowthBlock();
        if (adminsky2 == null) {
            this.prepareDropCollection();
            return;
        }
        BlockPos adminsky3 = new BlockPos(adminsky2.getX(), this.playerStartPosition.getY(), adminsky2.getZ());
        if (!this.navigationActive) {
            TextureReloadProvider.getTextureReloadTask().registerCollisionProbe(new BlockRegion(adminsky3, 1));
            this.navigationActive = true;
            this.containerNavigationStartedAt = System.currentTimeMillis();
        } else if (!TextureReloadProvider.getTextureReloadTask().isNavigationActive()) {
            this.navigationActive = false;
        }
    }

    private void prepareDropCollection() {
        this.cancelNavigation();
        this.resetRotationState();
        this.plantingAttempts = 0;
        this.lowestDropCount = Integer.MAX_VALUE;
        if (!this.pickupMushroomsSetting.isEnabled() || !this.isNavigationServiceReady()) {
            this.beginContainerCollection();
            return;
        }
        this.scanNearbyMushroomDrops();
        this.setAutomationState(MushroomAutomationState.NAVIGATING_TO_DROPS);
        this.selectNextMushroomDrop();
    }

    private void selectNextMushroomDrop() {
        this.targetMushroomEntity = null;
        this.navigationActive = false;
        while (!this.nearbyMushroomEntities.isEmpty()) {
            ItemEntity class_15422 = this.nearbyMushroomEntities.removeFirst();
            if (class_15422 == null || class_15422.isRemoved() || !class_15422.isAlive()) continue;
            this.targetMushroomEntity = class_15422;
            return;
        }
        this.beginContainerCollection();
    }

    void navigateToMushroomDrop() {
        if (this.targetMushroomEntity == null) {
            this.selectNextMushroomDrop();
            return;
        }
        if (this.targetMushroomEntity.isRemoved() || !this.targetMushroomEntity.isAlive()) {
            this.cancelNavigation();
            this.selectNextMushroomDrop();
            return;
        }
        Vec3d VanillaChestLootTableGenerator = this.targetMushroomEntity.getPos();
        if (MushroomFarmMode.minecraftClient.player.getPos().squaredDistanceTo(VanillaChestLootTableGenerator) <= 1.9599999999999997) {
            this.cancelNavigation();
            this.selectNextMushroomDrop();
            return;
        }
        if (!this.navigationActive) {
            BlockPos adminsky = BlockPos.ofFloored((Position)VanillaChestLootTableGenerator);
            TextureReloadProvider.getTextureReloadTask().registerCollisionProbe(new BlockOffset(adminsky.getX(), adminsky.getZ()));
            this.navigationActive = true;
            this.containerNavigationStartedAt = System.currentTimeMillis();
            return;
        }
        if (!TextureReloadProvider.getTextureReloadTask().isNavigationActive()) {
            this.navigationActive = false;
            return;
        }
        if (System.currentTimeMillis() - this.containerNavigationStartedAt > 10000L) {
            this.cancelNavigation();
            this.selectNextMushroomDrop();
        }
    }

    private void scanNearbyMushroomDrops() {
        this.nearbyMushroomEntities.clear();
        Vec3d VanillaChestLootTableGenerator = MushroomFarmMode.minecraftClient.player.getPos();
        double d = 15.0;
        double d2 = d * d;
        ArrayList<ItemEntity> arrayList = new ArrayList<ItemEntity>();
        for (Entity class_12972 : MushroomFarmMode.minecraftClient.world.getEntities()) {
            ItemEntity class_15423;
            if (!(class_12972 instanceof ItemEntity) || (class_15423 = (ItemEntity)class_12972).isRemoved() || !class_15423.isAlive() || VanillaChestLootTableGenerator.squaredDistanceTo(class_15423.getPos()) > d2 || !this.isMushroomStack(class_15423.getStack())) continue;
            arrayList.add(class_15423);
        }
        arrayList.sort(Comparator.comparingDouble(class_15422 -> VanillaChestLootTableGenerator.squaredDistanceTo(class_15422.getPos())));
        this.nearbyMushroomEntities.addAll(arrayList);
    }

    private void beginContainerCollection() {
        if (this.shouldDepositMushrooms()) {
            this.selectContainerTarget();
            return;
        }
        this.resetContainerWorkflow();
    }

    private boolean shouldDepositMushrooms() {
        if (!this.autoDepositSetting.isEnabled()) {
            return false;
        }
        PlayerInventory class_16612 = MushroomFarmMode.minecraftClient.player.getInventory();
        if (class_16612.getEmptySlot() != -1) {
            return false;
        }
        return this.hasExcessMushrooms();
    }

    private boolean hasExcessMushrooms() {
        PlayerInventory class_16612 = MushroomFarmMode.minecraftClient.player.getInventory();
        int n = 0;
        for (int i = 0; i < class_16612.size(); ++i) {
            ItemStack class_17992 = class_16612.getStack(i);
            if (class_17992.getItem() != Items.BROWN_MUSHROOM) continue;
            n += class_17992.getCount();
        }
        return n > Items.BROWN_MUSHROOM.getDefaultStack().getMaxCount();
    }

    private void selectContainerTarget() {
        this.cancelNavigation();
        this.resetRotationState();
        this.navigationActive = false;
        this.containerSlotIndex = -1;
        this.containerItemCount = -1;
        this.containerNavigationMode = ContainerNavigationMode.DIRECT_ACCESS;
        BlockPos adminsky = this.findNearestContainer();
        if (adminsky == null) {
            this.showWarning("modules.mushroom_farm.no_chest");
            this.resetContainerWorkflow();
            return;
        }
        this.targetContainerPosition = adminsky;
        if (this.isBlockWithinInteractionRange(adminsky)) {
            this.setAutomationState(MushroomAutomationState.OPENING_CONTAINER);
            this.containerActionCooldown.reset();
        } else if (this.isNavigationServiceReady()) {
            this.setAutomationState(MushroomAutomationState.SELECTING_CONTAINER_SLOT);
        } else {
            this.showWarning("modules.mushroom_farm.no_chest");
            this.resetContainerWorkflow();
        }
    }

    private boolean prepareContainerRestock() {
        if (!this.autoRefillSetting.isEnabled()) {
            return false;
        }
        BlockPos adminsky = this.findNearestContainer();
        if (adminsky == null) {
            return false;
        }
        if (!this.isBlockWithinInteractionRange(adminsky) && !this.isNavigationServiceReady()) {
            return false;
        }
        this.cancelNavigation();
        this.resetRotationState();
        this.navigationActive = false;
        this.containerSlotIndex = -1;
        this.containerItemCount = -1;
        this.containerNavigationMode = ContainerNavigationMode.NAVIGATE_TO_CONTAINER;
        this.targetContainerPosition = adminsky;
        if (this.isBlockWithinInteractionRange(adminsky)) {
            this.setAutomationState(MushroomAutomationState.OPENING_CONTAINER);
            this.containerActionCooldown.reset();
        } else {
            this.setAutomationState(MushroomAutomationState.SELECTING_CONTAINER_SLOT);
        }
        return true;
    }

    private void beginContainerWorkflow() {
        if (this.containerNavigationMode == ContainerNavigationMode.NAVIGATE_TO_CONTAINER) {
            if (!this.prepareContainerRestock()) {
                this.showWarning("modules.mushroom_farm.no_chest");
                this.resetContainerWorkflow();
            }
        } else {
            this.selectContainerTarget();
        }
    }

    private BlockPos findNearestContainer() {
        BlockPos adminsky = MushroomFarmMode.minecraftClient.player.getBlockPos();
        int n = 15;
        BlockPos adminsky2 = null;
        double d = Double.MAX_VALUE;
        for (BlockPos adminsky3 : BlockPos.iterateOutwards((BlockPos)adminsky, (int)n, (int)n, (int)n)) {
            double d2;
            BlockPos adminsky4 = adminsky3.toImmutable();
            if (this.unreachableContainerPositions.contains(adminsky4) || !(MushroomFarmMode.minecraftClient.world.getBlockEntity(adminsky4) instanceof ChestBlockEntity) || !((d2 = adminsky3.getSquaredDistance((Position)MushroomFarmMode.minecraftClient.player.getPos())) < d)) continue;
            d = d2;
            adminsky2 = adminsky4;
        }
        return adminsky2;
    }

    void navigateToContainer() {
        if (this.targetContainerPosition == null) {
            this.resetContainerWorkflow();
            return;
        }
        if (this.isBlockWithinInteractionRange(this.targetContainerPosition)) {
            this.cancelNavigation();
            this.setAutomationState(MushroomAutomationState.OPENING_CONTAINER);
            this.containerActionCooldown.reset();
            return;
        }
        if (!this.navigationActive) {
            TextureReloadProvider.getTextureReloadTask().registerCollisionProbe(new BlockRegion(this.targetContainerPosition, 2));
            this.navigationActive = true;
            this.containerNavigationStartedAt = System.currentTimeMillis();
            return;
        }
        if (!TextureReloadProvider.getTextureReloadTask().isNavigationActive()) {
            this.navigationActive = false;
            return;
        }
        if (System.currentTimeMillis() - this.containerNavigationStartedAt > 20000L) {
            this.cancelNavigation();
            this.unreachableContainerPositions.add(this.targetContainerPosition);
            this.targetContainerPosition = null;
            this.beginContainerWorkflow();
        }
    }

    void openContainerScreen() {
        if (this.targetContainerPosition == null) {
            this.resetContainerWorkflow();
            return;
        }
        if (MushroomFarmMode.minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
            this.containerSlotIndex = -1;
            this.containerItemCount = -1;
            this.containerActionCooldown.reset();
            this.setAutomationState(this.containerNavigationMode == ContainerNavigationMode.NAVIGATE_TO_CONTAINER ? MushroomAutomationState.TRANSFERRING_CONTAINER_CONTENTS : MushroomAutomationState.PROCESSING_CONTAINER_ITEMS);
            return;
        }
        if (!this.isBlockWithinInteractionRange(this.targetContainerPosition)) {
            this.navigationActive = false;
            if (this.isNavigationServiceReady()) {
                this.setAutomationState(MushroomAutomationState.SELECTING_CONTAINER_SLOT);
            } else {
                this.resetContainerWorkflow();
            }
            return;
        }
        Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)this.targetContainerPosition);
        if (!this.isRotationAligned(VanillaChestLootTableGenerator)) {
            return;
        }
        if (!this.containerActionCooldown.hasElapsed(50L)) {
            return;
        }
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, this.targetContainerPosition, false);
        MushroomFarmMode.minecraftClient.interactionManager.interactBlock(MushroomFarmMode.minecraftClient.player, Hand.MAIN_HAND, class_39652);
        MushroomFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        this.containerActionCooldown.reset();
    }

    void depositMushrooms() {
        ScreenHandler class_17032 = MushroomFarmMode.minecraftClient.player.currentScreenHandler;
        if (!(class_17032 instanceof GenericContainerScreenHandler)) {
            this.finishDepositTransfer(false);
            return;
        }
        GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)class_17032;
        if (!this.containerActionCooldown.hasElapsed(50L)) {
            return;
        }
        ItemStack containerStack = this.containerSlotIndex == -1
            ? ItemStack.EMPTY
            : class_17072.slots.get(this.containerSlotIndex).getStack();
        if (this.containerSlotIndex != -1 && !containerStack.isEmpty()
                && containerStack.getCount() >= this.containerItemCount
                && containerStack.getItem() == Items.BROWN_MUSHROOM) {
            if (this.targetContainerPosition != null) {
                this.unreachableContainerPositions.add(this.targetContainerPosition);
            }
            MushroomFarmMode.minecraftClient.player.closeHandledScreen();
            this.targetContainerPosition = null;
            this.containerSlotIndex = -1;
            this.containerItemCount = -1;
            this.selectContainerTarget();
            return;
        }
        int n = this.findMushroomHotbarSlot();
        DefaultedList<Slot> containerSlots = class_17072.slots;
        for (int i = 0; i < containerSlots.size(); ++i) {
            ItemStack class_17992;
            Slot class_17352 = containerSlots.get(i);
            if (class_17352.inventory != MushroomFarmMode.minecraftClient.player.getInventory() || class_17352.getIndex() == n || (class_17992 = class_17352.getStack()).getItem() != Items.BROWN_MUSHROOM) continue;
            InventoryUtils.quickMoveItem(i);
            this.containerSlotIndex = i;
            this.containerItemCount = class_17992.getCount();
            this.containerActionCooldown.reset();
            return;
        }
        this.finishDepositTransfer(true);
    }

    private int findMushroomHotbarSlot() {
        PlayerInventory class_16612 = MushroomFarmMode.minecraftClient.player.getInventory();
        int n = -1;
        int n2 = -1;
        for (int i = 0; i < class_16612.size(); ++i) {
            ItemStack class_17992 = class_16612.getStack(i);
            if (class_17992.getItem() != Items.BROWN_MUSHROOM || class_17992.getCount() <= n2) continue;
            n2 = class_17992.getCount();
            n = i;
        }
        return n;
    }

    private void finishDepositTransfer(boolean bl) {
        if (bl && MushroomFarmMode.minecraftClient.currentScreen != null) {
            MushroomFarmMode.minecraftClient.player.closeHandledScreen();
        }
        this.targetContainerPosition = null;
        this.containerSlotIndex = -1;
        this.containerItemCount = -1;
        this.resetContainerWorkflow();
    }

    void withdrawBonemeal() {
        ScreenHandler class_17032 = MushroomFarmMode.minecraftClient.player.currentScreenHandler;
        if (!(class_17032 instanceof GenericContainerScreenHandler)) {
            this.finishRestockTransfer(false);
            return;
        }
        GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)class_17032;
        if (!this.containerActionCooldown.hasElapsed(50L)) {
            return;
        }
        ItemStack containerStack = this.containerSlotIndex == -1
            ? ItemStack.EMPTY
            : class_17072.slots.get(this.containerSlotIndex).getStack();
        if (this.containerSlotIndex != -1 && !containerStack.isEmpty()
                && containerStack.getCount() >= this.containerItemCount
                && this.isContainerTransferItem(containerStack)) {
            this.finishRestockTransfer(true);
            return;
        }
        DefaultedList<Slot> containerSlots = class_17072.slots;
        for (int i = 0; i < containerSlots.size(); ++i) {
            ItemStack class_17992;
            Slot class_17352 = containerSlots.get(i);
            if (class_17352.inventory == MushroomFarmMode.minecraftClient.player.getInventory() || !this.isContainerTransferItem(class_17992 = class_17352.getStack())) continue;
            InventoryUtils.quickMoveItem(i);
            this.containerSlotIndex = i;
            this.containerItemCount = class_17992.getCount();
            this.containerActionCooldown.reset();
            return;
        }
        this.finishRestockTransfer(true);
    }

    private boolean isContainerTransferItem(ItemStack class_17992) {
        if (class_17992.isEmpty()) {
            return false;
        }
        if (class_17992.getItem() == Items.BONE_MEAL) {
            return this.countInventoryItems(Items.BONE_MEAL) < 192;
        }
        if (class_17992.getItem() == Items.BROWN_MUSHROOM) {
            return this.countInventoryItems(Items.BROWN_MUSHROOM) < 16;
        }
        return false;
    }

    private void finishRestockTransfer(boolean bl) {
        if (bl && MushroomFarmMode.minecraftClient.currentScreen != null) {
            MushroomFarmMode.minecraftClient.player.closeHandledScreen();
        }
        this.targetContainerPosition = null;
        this.containerSlotIndex = -1;
        this.containerItemCount = -1;
        if (this.countInventoryItems(Items.BONE_MEAL) == 0) {
            this.showError("modules.mushroom_farm.no_bonemeal");
            this.disableAutoFarm();
            return;
        }
        this.resetContainerWorkflow();
    }

    private int countInventoryItems(Item class_17922) {
        PlayerInventory class_16612 = MushroomFarmMode.minecraftClient.player.getInventory();
        int n = 0;
        for (int i = 0; i < class_16612.size(); ++i) {
            ItemStack class_17992 = class_16612.getStack(i);
            if (class_17992.getItem() != class_17922) continue;
            n += class_17992.getCount();
        }
        return n;
    }

    private void resetContainerWorkflow() {
        this.cancelNavigation();
        this.resetRotationState();
        this.navigationActive = false;
        if (this.isNavigationServiceReady() && !this.isBlockWithinInteractionRange(this.farmBasePosition)) {
            this.setAutomationState(MushroomAutomationState.MOVING_CONTAINER_ITEM);
        } else {
            this.containerActionCooldown.reset();
            this.setAutomationState(MushroomAutomationState.PLANTING_MUSHROOM);
        }
    }

    void navigateToMushroomFarm() {
        if (this.isBlockWithinInteractionRange(this.farmBasePosition) || !this.isNavigationServiceReady()) {
            this.cancelNavigation();
            this.containerActionCooldown.reset();
            this.setAutomationState(MushroomAutomationState.PLANTING_MUSHROOM);
            return;
        }
        if (!this.navigationActive) {
            TextureReloadProvider.getTextureReloadTask().registerCollisionProbe(new BlockRegion(this.playerStartPosition, 1));
            this.navigationActive = true;
            this.containerNavigationStartedAt = System.currentTimeMillis();
            return;
        }
        if (!TextureReloadProvider.getTextureReloadTask().isNavigationActive()) {
            this.navigationActive = false;
            return;
        }
        if (System.currentTimeMillis() - this.containerNavigationStartedAt > 20000L) {
            this.cancelNavigation();
            this.setAutomationState(MushroomAutomationState.PLANTING_MUSHROOM);
        }
    }

    private boolean prepareMushroomPlantingSurface() {
        BlockState class_26802 = MushroomFarmMode.minecraftClient.world.getBlockState(this.farmBasePosition);
        Block class_22482 = class_26802.getBlock();
        if (MushroomFarmMode.isPlantingSurfaceBlock(class_22482)) {
            return true;
        }
        if (!class_26802.isAir()) {
            return true;
        }
        Predicate<ItemStack> predicate = class_17992 -> class_17992.getItem() == Items.DIRT || class_17992.getItem() == Items.GRASS_BLOCK;
        if (!this.selectItemMatching(predicate)) {
            this.showError("modules.mushroom_farm.no_dirt_item");
            this.disableAutoFarm();
            return false;
        }
        Direction[] class_2350Array = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP};
        double d = MushroomFarmMode.minecraftClient.player.getBlockInteractionRange();
        double d2 = d * d;
        for (Direction class_23502 : class_2350Array) {
            BlockPos adminsky = this.farmBasePosition.offset(class_23502);
            Direction class_23503 = class_23502.getOpposite();
            BlockState class_26803 = MushroomFarmMode.minecraftClient.world.getBlockState(adminsky);
            if (!class_26803.isSideSolidFullSquare((BlockView)MushroomFarmMode.minecraftClient.world, adminsky, class_23503)) continue;
            Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)adminsky).add((double)class_23503.getOffsetX() * 0.5, (double)class_23503.getOffsetY() * 0.5, (double)class_23503.getOffsetZ() * 0.5);
            if (MushroomFarmMode.minecraftClient.player.getEyePos().squaredDistanceTo(VanillaChestLootTableGenerator) > d2) continue;
            if (!this.isRotationAligned(VanillaChestLootTableGenerator)) {
                return false;
            }
            BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, class_23503, adminsky, false);
            MushroomFarmMode.minecraftClient.interactionManager.interactBlock(MushroomFarmMode.minecraftClient.player, Hand.MAIN_HAND, class_39652);
            MushroomFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
            return false;
        }
        this.showError("modules.mushroom_farm.no_dirt_support");
        this.disableAutoFarm();
        return false;
    }

    private BlockPos findNearestPlantingPosition() {
        BlockPos adminsky = MushroomFarmMode.minecraftClient.player.getBlockPos();
        Vec3d VanillaChestLootTableGenerator = MushroomFarmMode.minecraftClient.player.getEyePos();
        double d = MushroomFarmMode.minecraftClient.player.getBlockInteractionRange();
        double d2 = d * d;
        BlockPos adminsky2 = null;
        double d3 = Double.MAX_VALUE;
        for (int i = -4; i <= 4; ++i) {
            for (int j = -4; j <= 4; ++j) {
                for (int k = -4; k <= 4; ++k) {
                    double d4;
                    BlockPos adminsky3 = adminsky.add(i, j, k);
                    if (!this.isMushroomPlantingPosition(adminsky3) || (d4 = VanillaChestLootTableGenerator.squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky3.up()))) > d2 || !(d4 < d3)) continue;
                    d3 = d4;
                    adminsky2 = adminsky3.toImmutable();
                }
            }
        }
        return adminsky2;
    }

    private boolean isMushroomPlantingPosition(BlockPos adminsky) {
        if (!MushroomFarmMode.isPlantingSurfaceBlock(MushroomFarmMode.minecraftClient.world.getBlockState(adminsky).getBlock())) {
            return false;
        }
        return MushroomFarmMode.minecraftClient.world.getBlockState(adminsky.up()).isAir();
    }

    private boolean hasMushroomGrowthSpace() {
        BlockPos adminsky = this.farmBasePosition.up();
        for (int i = -1; i <= 12; ++i) {
            for (int j = -7; j <= 7; ++j) {
                for (int k = -7; k <= 7; ++k) {
                    if (!MushroomFarmMode.isMushroomGrowthBlock(MushroomFarmMode.minecraftClient.world.getBlockState(adminsky.add(j, i, k)).getBlock())) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private int countGrowthBlocks() {
        BlockPos adminsky = this.farmBasePosition.up();
        int n = 0;
        for (int i = -1; i <= 12; ++i) {
            for (int j = -7; j <= 7; ++j) {
                for (int k = -7; k <= 7; ++k) {
                    if (!MushroomFarmMode.isMushroomGrowthBlock(MushroomFarmMode.minecraftClient.world.getBlockState(adminsky.add(j, i, k)).getBlock())) continue;
                    ++n;
                }
            }
        }
        return n;
    }

    private BlockPos findLowestGrowthBlock() {
        BlockPos adminsky = this.farmBasePosition.up();
        Vec3d VanillaChestLootTableGenerator = MushroomFarmMode.minecraftClient.player.getEyePos();
        double d = MushroomFarmMode.minecraftClient.player.getBlockInteractionRange();
        double d2 = d * d;
        BlockPos adminsky2 = null;
        int n = Integer.MAX_VALUE;
        double d3 = Double.MAX_VALUE;
        for (int i = -1; i <= 12; ++i) {
            for (int j = -7; j <= 7; ++j) {
                for (int k = -7; k <= 7; ++k) {
                    int n2;
                    double d4;
                    BlockPos adminsky3 = adminsky.add(j, i, k);
                    if (!MushroomFarmMode.isMushroomGrowthBlock(MushroomFarmMode.minecraftClient.world.getBlockState(adminsky3).getBlock()) || (d4 = VanillaChestLootTableGenerator.squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky3))) > d2 || (n2 = adminsky3.getY()) >= n && (n2 != n || !(d4 < d3))) continue;
                    n = n2;
                    d3 = d4;
                    adminsky2 = adminsky3.toImmutable();
                }
            }
        }
        return adminsky2;
    }

    private BlockPos findNearestGrowthBlock() {
        BlockPos adminsky = this.farmBasePosition.up();
        Vec3d VanillaChestLootTableGenerator = MushroomFarmMode.minecraftClient.player.getPos();
        BlockPos adminsky2 = null;
        int n = Integer.MAX_VALUE;
        double d = Double.MAX_VALUE;
        for (int i = -1; i <= 12; ++i) {
            for (int j = -7; j <= 7; ++j) {
                for (int k = -7; k <= 7; ++k) {
                    BlockPos adminsky3 = adminsky.add(j, i, k);
                    if (!MushroomFarmMode.isMushroomGrowthBlock(MushroomFarmMode.minecraftClient.world.getBlockState(adminsky3).getBlock())) continue;
                    double d2 = Vec3d.ofCenter((Vec3i)adminsky3).squaredDistanceTo(VanillaChestLootTableGenerator);
                    int n2 = adminsky3.getY();
                    if (n2 >= n && (n2 != n || !(d2 < d))) continue;
                    n = n2;
                    d = d2;
                    adminsky2 = adminsky3.toImmutable();
                }
            }
        }
        return adminsky2;
    }

    private void selectBestAxe(BlockState class_26802) {
        Predicate<ItemStack> predicate = class_17992 -> class_17992.getItem() instanceof AxeItem;
        ItemStack class_17993 = MushroomFarmMode.minecraftClient.player.getMainHandStack();
        ToolMiningStats toolMiningStats = this.getToolMiningStats(class_17993, class_26802, predicate);
        ItemRule itemRule = null;
        for (ItemRule itemRule2 : ItemRuleSets.getHotbarRules().combineRules(ItemRuleSets.getInventoryRules()).getRules()) {
            ItemStack class_17994 = itemRule2.getItemStack();
            ToolMiningStats toolMiningStats2 = this.getToolMiningStats(class_17994, class_26802, predicate);
            if (!toolMiningStats2.isBetterThan(toolMiningStats)) continue;
            toolMiningStats = toolMiningStats2;
            itemRule = itemRule2;
        }
        if (!toolMiningStats.isUsable()) {
            return;
        }
        if (itemRule instanceof HotbarSlot) {
            HotbarSlot hotbarSlot = (HotbarSlot)itemRule;
            InventoryUtils.setSelectedHotbarSlot(hotbarSlot);
        } else if (itemRule instanceof InventorySlotRule) {
            ItemRule itemRule2;
            itemRule2 = (InventorySlotRule)itemRule;
            int n = MushroomFarmMode.minecraftClient.player.getInventory().selectedSlot;
            InventoryUtils.dropItem(((InventorySlotRule)itemRule2).getClickSlot(), n);
        }
    }

    private ToolMiningStats getToolMiningStats(ItemStack class_17992, BlockState class_26802, Predicate<ItemStack> predicate) {
        if (class_17992 == null || class_17992.isEmpty() || !predicate.test(class_17992)) {
            return new ToolMiningStats(-1.0f, -1, -1.0f);
        }
        float f = class_17992.getMiningSpeedMultiplier(class_26802);
        int n = EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.EFFICIENCY);
        if (f > 1.0f && n > 0) {
            f += (float)(n * n) + 1.0f;
        }
        return new ToolMiningStats(f, n, MushroomFarmMode.getToolDurabilityRatio(class_17992));
    }

    private static float getToolDurabilityRatio(ItemStack class_17992) {
        if (class_17992.isEmpty() || !class_17992.isDamageable()) {
            return 1.0f;
        }
        int n = class_17992.getMaxDamage();
        if (n <= 0) {
            return 1.0f;
        }
        return (float)(n - class_17992.getDamage()) / (float)n;
    }

    private boolean selectFarmItem(Item class_17922) {
        return this.selectItemMatching(class_17992 -> class_17992.getItem() == class_17922);
    }

    private boolean selectItemMatching(Predicate<ItemStack> predicate) {
        if (predicate.test(MushroomFarmMode.minecraftClient.player.getMainHandStack())) {
            return true;
        }
        HotbarSlot hotbarSlot = ItemRuleSets.getHotbarRules().findByStack(predicate);
        if (hotbarSlot != null) {
            InventoryUtils.setSelectedHotbarSlot(hotbarSlot);
            return true;
        }
        InventorySlotRule inventorySlotRule = ItemRuleSets.getInventoryRules().findByStack(predicate);
        if (inventorySlotRule != null) {
            int n = MushroomFarmMode.minecraftClient.player.getInventory().selectedSlot;
            InventoryUtils.dropItem(inventorySlotRule.getClickSlot(), n);
            return true;
        }
        return false;
    }

    private boolean isMushroomStack(ItemStack class_17992) {
        return class_17992.getItem() == Items.BROWN_MUSHROOM;
    }

    private boolean isAvailableItem(Item class_17922) {
        return ItemRuleSets.getHotbarRules().containsItem(class_17922) || ItemRuleSets.getInventoryRules().containsItem(class_17922);
    }

    private boolean hasAvailableMatchingItem(Predicate<ItemStack> predicate) {
        return ItemRuleSets.getHotbarRules().findByStack(predicate) != null || ItemRuleSets.getInventoryRules().findByStack(predicate) != null;
    }

    private boolean isRotationAligned(Vec3d VanillaChestLootTableGenerator) {
        Rotation rotation = AimRotationMath.getRotationToPoint(VanillaChestLootTableGenerator);
        if (this.targetRotation == null || this.targetRotation.angleDistanceTo(rotation) > 0.5f) {
            this.targetRotation = rotation;
            this.rotationSuccessCount = 0;
            this.rotationAttemptCount = 0;
        }
        RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, 180.0f, 180.0f, 180.0f, RotationPriority.ITEM_USE_PRIORITY);
        ++this.rotationAttemptCount;
        Rotation rotation2 = RockstarClient.create().getRotationManager().getCurrentRotation();
        if (rotation2 != null && rotation2.angleDistanceTo(rotation) <= 1.0f) {
            ++this.rotationSuccessCount;
            return this.rotationSuccessCount >= 1;
        }
        return this.rotationAttemptCount > 20;
    }

    private void resetRotationState() {
        this.targetRotation = null;
        this.rotationSuccessCount = 0;
        this.rotationAttemptCount = 0;
    }

    private boolean isBlockWithinInteractionRange(BlockPos adminsky) {
        double d = MushroomFarmMode.minecraftClient.player.getBlockInteractionRange();
        return MushroomFarmMode.minecraftClient.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky)) <= d * d;
    }

    private Direction getClosestInteractionSide(BlockPos adminsky) {
        Vec3d VanillaChestLootTableGenerator = MushroomFarmMode.minecraftClient.player.getEyePos();
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

    private void cancelNavigation() {
        if (TextureReloadProvider.isInstalled() && TextureReloadProvider.getTextureReloadTask().isNavigationActive()) {
            TextureReloadProvider.getTextureReloadTask().clearNavigationTasks();
        }
        this.navigationActive = false;
    }

    private boolean isNavigationServiceReady() {
        return TextureReloadProvider.isInstalled();
    }

    private void setAutomationState(MushroomAutomationState mushroomAutomationState) {
        this.automationState = mushroomAutomationState;
        this.resetRotationState();
    }

    private static boolean isPlantingSurfaceBlock(Block class_22482) {
        return class_22482 == Blocks.GRASS_BLOCK || class_22482 == Blocks.DIRT;
    }

    private static boolean isMushroomGrowthBlock(Block class_22482) {
        return class_22482 == Blocks.BROWN_MUSHROOM_BLOCK || class_22482 == Blocks.MUSHROOM_STEM;
    }

    static enum ContainerNavigationMode {
        DIRECT_ACCESS,
        NAVIGATE_TO_CONTAINER;
}

    static enum MushroomAutomationState {
        PLANTING_MUSHROOM,
        GROWING_MUSHROOM,
        HARVESTING_MUSHROOM,
        NAVIGATING_TO_DROPS,
        SELECTING_CONTAINER_SLOT,
        OPENING_CONTAINER,
        PROCESSING_CONTAINER_ITEMS,
        TRANSFERRING_CONTAINER_CONTENTS,
        MOVING_CONTAINER_ITEM;
}

    static final class ToolMiningStats {
        private final float miningSpeed;
        private final int efficiencyLevel;
        private final float durabilityRatio;

        ToolMiningStats(float f, int n, float f2) {
            this.miningSpeed = f;
            this.efficiencyLevel = n;
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
            return this.durabilityRatio > toolMiningStats.durabilityRatio;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "miningSpeed", "efficiencyLevel", "durabilityRatio");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "miningSpeed", "efficiencyLevel", "durabilityRatio");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "miningSpeed", "efficiencyLevel", "durabilityRatio");
        }

        public float getMiningSpeed() {
            return this.miningSpeed;
        }

        public int getEfficiencyLevel() {
            return this.efficiencyLevel;
        }

        public float getDurabilityRatio() {
            return this.durabilityRatio;
        }
    }
}

