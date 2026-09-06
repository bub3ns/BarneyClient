/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.ItemEntity
 *  net.minecraft.PlayerInventory
 *  net.minecraft.ScreenHandler
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.Slot
 *  net.minecraft.Item
 *  net.minecraft.HoeItem
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.BlockView
 *  net.minecraft.ItemConvertible
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.CropBlock
 *  net.minecraft.Direction
 *  net.minecraft.DefaultedList
 *  net.minecraft.Position
 *  net.minecraft.Box
 *  net.minecraft.Vec3i
 *  net.minecraft.NetherWartBlock
 *  net.minecraft.Vec3d
 *  net.minecraft.ChestBlockEntity
 *  net.minecraft.BlockState
 *  net.minecraft.Property
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.BlockHitResult
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.RegistryKey
 */
package moscow.rockstar.modules.player.farming.crops;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
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
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.texture.TextureReloadProvider;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import moscow.rockstar.world.BlockRegion;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.BlockView;
import net.minecraft.item.ItemConvertible;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.RegistryKey;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public class CropFarmMode
extends FarmModeBase {
    private final ModeSetting cropModeSetting;
    private final ModeSetting.Option netherWartOption;
    private final ModeSetting.Option wheatOption;
    private final ModeSetting.Option carrotOption;
    private final ModeSetting.Option potatoOption;
    private final ModeSetting.Option beetrootOption;
    private final ModeSetting.Option sugarCaneOption;
    private final NumberSetting scanRadiusSetting;
    private final NumberSetting verticalRangeSetting;
    private final BooleanSetting replantSetting;
    private final BooleanSetting hoeUseSetting;
    private final BooleanSetting pickupSetting;
    private final BooleanSetting autoDepositSetting;
    private BooleanSetting targetEspSetting;
    private final NumberSetting actionDelaySetting;
    private static final float ROTATION_RESET_THRESHOLD = 1.5f;
    private static final int MAX_FAILED_NAVIGATION_ATTEMPTS = 3;
    private static final float MIN_TOOL_DURABILITY_RATIO = 0.05f;
    private static final double CROP_INTERACTION_DISTANCE_SQUARED = 1.9599999999999997;
    private final List<BlockPos> matureCropPositions = new ArrayList<BlockPos>();
    private final List<BlockPos> plantableCropPositions = new ArrayList<BlockPos>();
    private final Map<BlockPos, Integer> cropRetryCounts = new HashMap<BlockPos, Integer>();
    private final List<ItemEntity> nearbyDropEntities = new ArrayList<ItemEntity>();
    private final Set<BlockPos> unreachableChestPositions = new HashSet<BlockPos>();
    private BlockPos targetCropPosition;
    private ItemEntity targetDropEntity;
    private BlockPos targetChestPosition;
    private int selectedInventorySlot = -1;
    private int selectedItemCount = -1;
    private CropAutomationState automationState = CropAutomationState.IDLE;
    private boolean navigationActive;
    private long navigationStartedAt;
    private final Timer actionCooldown = new Timer();
    private final Timer stateTransitionCooldown = new Timer();
    private Rotation targetRotation;
    private int rotationConfirmationCount;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (CropFarmMode.minecraftClient.player == null || CropFarmMode.minecraftClient.world == null) {
            return;
        }
        switch (this.automationState.ordinal()) {
            case 0: {
                this.scanAndSortCropTargets();
                break;
            }
            case 1: {
                this.navigateToCropTarget();
                break;
            }
            case 2: {
                this.processCropAction();
                break;
            }
            case 3: {
                this.plantCropAtTarget();
                break;
            }
            case 4: {
                this.navigateToDroppedItem();
                break;
            }
            case 5: {
                this.navigateToDepositChest();
                break;
            }
            case 6: {
                this.openDepositChest();
                break;
            }
            case 7: {
                this.transferCropItemsToChest();
                break;
            }
            case 8: {
                this.updateCropWaitState();
            }
        }
    };
    private static final ColorRGBA matureCropColor = new ColorRGBA(120.0f, 220.0f, 96.0f);
    private static final ColorRGBA unripeCropColor = new ColorRGBA(255.0f, 196.0f, 64.0f);
    private final EventListener<Render3DEvent> render3DListener = render3DEvent -> {
        BlockPos adminsky;
        if (!this.targetEspSetting.isEnabled()) {
            return;
        }
        if (CropFarmMode.minecraftClient.world == null || CropFarmMode.minecraftClient.player == null) {
            return;
        }
        BlockPos adminsky2 = this.automationState == CropAutomationState.NAVIGATING_TO_CROP || this.automationState == CropAutomationState.HARVESTING || this.automationState == CropAutomationState.PLANTING ? this.targetCropPosition : null;
        BlockPos adminsky3 = adminsky = this.automationState == CropAutomationState.NAVIGATING_TO_CHEST || this.automationState == CropAutomationState.RETURNING_TO_CROPS || this.automationState == CropAutomationState.TRANSFERRING_ITEMS ? this.targetChestPosition : null;
        if (adminsky2 == null && adminsky == null) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Camera class_41842 = CropFarmMode.minecraftClient.gameRenderer.getCamera();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        if (adminsky2 != null) {
            RenderUtils.drawFilledBox(class_45872, class_2872, this.getCropRenderBounds(adminsky2).offset(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z), matureCropColor.withAlpha(45.0f));
        }
        if (adminsky != null) {
            RenderUtils.drawFilledBox(class_45872, class_2872, this.getCropRenderBounds(adminsky).offset(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z), unripeCropColor.withAlpha(45.0f));
        }
        ItemRenderUtils.flushVertexConsumer(class_2872);
        BufferBuilder CreativeInventoryActionC2SPacket = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        if (adminsky2 != null) {
            RenderUtils.drawBoxOutline(class_45872, CreativeInventoryActionC2SPacket, this.getCropRenderBounds(adminsky2).offset(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z), matureCropColor.withAlpha(180.0f));
        }
        if (adminsky != null) {
            RenderUtils.drawBoxOutline(class_45872, CreativeInventoryActionC2SPacket, this.getCropRenderBounds(adminsky).offset(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z), unripeCropColor.withAlpha(180.0f));
        }
        ItemRenderUtils.flushVertexConsumer(CreativeInventoryActionC2SPacket);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    };

    public CropFarmMode(AutoFarm autoFarm, ModeSetting modeSetting) {
        super(autoFarm, modeSetting, "modules.settings.auto_farm.modes.crop");
        this.cropModeSetting = new ModeSetting((SettingOwner)autoFarm, "modules.settings.crop_farm.crop", () -> !this.isSelected());
        this.netherWartOption = new ModeSetting.Option(this.cropModeSetting, "modules.settings.crop_farm.crop.nether_wart").select();
        this.wheatOption = new ModeSetting.Option(this.cropModeSetting, "modules.settings.crop_farm.crop.wheat");
        this.carrotOption = new ModeSetting.Option(this.cropModeSetting, "modules.settings.crop_farm.crop.carrots");
        this.potatoOption = new ModeSetting.Option(this.cropModeSetting, "modules.settings.crop_farm.crop.potatoes");
        this.beetrootOption = new ModeSetting.Option(this.cropModeSetting, "modules.settings.crop_farm.crop.beetroots");
        this.sugarCaneOption = new ModeSetting.Option(this.cropModeSetting, "modules.settings.crop_farm.crop.sugar_cane");
        this.scanRadiusSetting = new NumberSetting((SettingOwner)autoFarm, "modules.settings.crop_farm.scan_radius", () -> !this.isSelected()).setStep(2.0f).setMinValue(8.0f).setMaxValue(64.0f).setValue(24.0f);
        this.verticalRangeSetting = new NumberSetting((SettingOwner)autoFarm, "modules.settings.crop_farm.vertical_range", () -> !this.isSelected()).setStep(1.0f).setMinValue(1.0f).setMaxValue(8.0f).setValue(3.0f);
        this.replantSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.crop_farm.replant", () -> !this.isSelected()).enable();
        this.hoeUseSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.crop_farm.use_hoe", () -> !this.isSelected()).enable();
        this.pickupSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.crop_farm.pickup", () -> !this.isSelected()).enable();
        this.autoDepositSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.crop_farm.auto_deposit", () -> !this.isSelected()).enable();
        this.targetEspSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.crop_farm.target_esp", () -> !this.isSelected()).enable();
        this.actionDelaySetting = new NumberSetting((SettingOwner)autoFarm, "modules.settings.crop_farm.action_delay", () -> !this.isSelected()).setStep(10.0f).setMinValue(0.0f).setMaxValue(500.0f).setValue(80.0f).setUnit("ms");
    }

    @Override
    public void startFarmAutomation() {
        if (CropFarmMode.minecraftClient.player == null || CropFarmMode.minecraftClient.world == null) {
            this.disableAutoFarm();
            return;
        }
        if (!TextureReloadProvider.isInstalled()) {
            this.showError("modules.crop_farm.newton_missing");
            this.disableAutoFarm();
            return;
        }
        this.clearCropTargets();
        this.automationState = CropAutomationState.IDLE;
    }

    @Override
    public void resetBrewingState() {
        this.cancelCropNavigation();
        this.clearCropTargets();
        this.automationState = CropAutomationState.WAITING_FOR_RESOURCES;
    }

    @Override
    public FarmState getFarmState() {
        return switch (this.automationState.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 8 -> FarmState.IDLE;
            case 1, 5 -> FarmState.MOVING;
            case 2 -> FarmState.WORKING;
            case 3 -> FarmState.PLANTING;
            case 4 -> FarmState.PICKUP;
            case 6, 7 -> FarmState.DEPOSIT;
        };
    }

    @Override
    public ItemStack getFarmDisplayItem() {
        Item class_17922 = this.getSelectedSeedItem();
        return new ItemStack((ItemConvertible)(class_17922 == null ? Items.WHEAT : class_17922));
    }

    private void clearCropTargets() {
        this.matureCropPositions.clear();
        this.plantableCropPositions.clear();
        this.cropRetryCounts.clear();
        this.nearbyDropEntities.clear();
        this.unreachableChestPositions.clear();
        this.targetCropPosition = null;
        this.targetDropEntity = null;
        this.targetChestPosition = null;
        this.selectedInventorySlot = -1;
        this.selectedItemCount = -1;
        this.navigationActive = false;
        this.clearRotationTarget();
    }

    private void updateCropWaitState() {
        if (this.stateTransitionCooldown.hasElapsed(1500L)) {
            this.automationState = CropAutomationState.IDLE;
        }
    }

    private void scanAndSortCropTargets() {
        this.matureCropPositions.clear();
        this.plantableCropPositions.clear();
        this.cropRetryCounts.clear();
        this.scanCropBlocks();
        if (this.matureCropPositions.isEmpty() && this.plantableCropPositions.isEmpty()) {
            this.automationState = CropAutomationState.WAITING_FOR_RESOURCES;
            this.stateTransitionCooldown.reset();
            return;
        }
        this.sortCropTargets();
        this.selectNextCropTarget();
    }

    private void navigateToCropTarget() {
        if (this.targetCropPosition == null || !this.isCropTargetReachable(this.targetCropPosition)) {
            this.cancelCropNavigation();
            this.selectNextCropTarget();
            return;
        }
        if (this.isCropWithinInteractionRange(this.targetCropPosition)) {
            this.cancelCropNavigation();
            this.automationState = this.isMatureCrop(this.targetCropPosition) ? CropAutomationState.HARVESTING : CropAutomationState.PLANTING;
            this.clearRotationTarget();
            this.actionCooldown.reset();
            return;
        }
        if (!this.navigationActive) {
            int n = this.cropRetryCounts.getOrDefault(this.targetCropPosition, 0);
            if (n >= 3) {
                this.selectNextCropTarget();
                return;
            }
            BlockPos adminsky = this.findSupportBlock(this.targetCropPosition);
            if (adminsky == null) {
                this.cropRetryCounts.put(this.targetCropPosition, 3);
                this.selectNextCropTarget();
                return;
            }
            TextureReloadProvider.getTextureReloadTask().registerCollisionProbe(new BlockRegion(adminsky, 1));
            this.cropRetryCounts.put(this.targetCropPosition, n + 1);
            this.navigationActive = true;
            this.navigationStartedAt = System.currentTimeMillis();
            return;
        }
        if (!TextureReloadProvider.getTextureReloadTask().isNavigationActive()) {
            this.navigationActive = false;
            return;
        }
        if (System.currentTimeMillis() - this.navigationStartedAt > 20000L) {
            this.cancelCropNavigation();
            this.selectNextCropTarget();
        }
    }

    private void processCropAction() {
        Direction class_23502;
        Vec3d VanillaChestLootTableGenerator;
        if (this.targetCropPosition == null) {
            this.automationState = CropAutomationState.IDLE;
            return;
        }
        if (!this.isMatureCrop(this.targetCropPosition)) {
            BlockState class_26802 = CropFarmMode.minecraftClient.world.getBlockState(this.targetCropPosition);
            if (class_26802.isAir() && this.replantSetting.isEnabled() && this.isPlantingSurfaceValid(this.targetCropPosition)) {
                this.automationState = CropAutomationState.PLANTING;
                this.clearRotationTarget();
                this.actionCooldown.reset();
                return;
            }
            this.selectNextCropTarget();
            return;
        }
        if (!this.isCropWithinInteractionRange(this.targetCropPosition)) {
            this.automationState = CropAutomationState.NAVIGATING_TO_CROP;
            this.navigationActive = false;
            return;
        }
        if (this.hoeUseSetting.isEnabled()) {
            this.selectBestCropTool(CropFarmMode.minecraftClient.world.getBlockState(this.targetCropPosition));
        }
        if (!this.isRotationAligned(VanillaChestLootTableGenerator = this.getCropHitPosition(this.targetCropPosition, class_23502 = this.getClosestInteractionSide(this.targetCropPosition)))) {
            return;
        }
        if (!this.actionCooldown.hasElapsed((long)this.actionDelaySetting.getValue())) {
            return;
        }
        CropFarmMode.minecraftClient.interactionManager.attackBlock(this.targetCropPosition, class_23502);
        CropFarmMode.minecraftClient.interactionManager.updateBlockBreakingProgress(this.targetCropPosition, class_23502);
        CropFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        this.actionCooldown.reset();
    }

    private void plantCropAtTarget() {
        if (this.targetCropPosition == null) {
            this.automationState = CropAutomationState.IDLE;
            return;
        }
        BlockState class_26802 = CropFarmMode.minecraftClient.world.getBlockState(this.targetCropPosition);
        if (!class_26802.isAir()) {
            this.selectNextCropTarget();
            return;
        }
        if (!this.isPlantingSurfaceValid(this.targetCropPosition)) {
            this.selectNextCropTarget();
            return;
        }
        Item class_17922 = this.getSelectedSeedItem();
        if (class_17922 == null || !this.selectCropItem(class_17922)) {
            this.selectNextCropTarget();
            return;
        }
        if (!this.isCropWithinInteractionRange(this.targetCropPosition)) {
            this.automationState = CropAutomationState.NAVIGATING_TO_CROP;
            this.navigationActive = false;
            return;
        }
        BlockPos adminsky = this.targetCropPosition.down();
        Vec3d VanillaChestLootTableGenerator = new Vec3d((double)adminsky.getX() + 0.5, (double)adminsky.getY() + 1.0, (double)adminsky.getZ() + 0.5);
        if (!this.isRotationAligned(VanillaChestLootTableGenerator)) {
            return;
        }
        if (!this.actionCooldown.hasElapsed((long)this.actionDelaySetting.getValue())) {
            return;
        }
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky, false);
        CropFarmMode.minecraftClient.interactionManager.interactBlock(CropFarmMode.minecraftClient.player, Hand.MAIN_HAND, class_39652);
        CropFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        this.actionCooldown.reset();
        this.selectNextCropTarget();
    }

    private void selectNextCropTarget() {
        double d;
        BlockPos adminsky;
        int n;
        this.cropRetryCounts.remove(this.targetCropPosition);
        this.targetCropPosition = null;
        if (this.isAutoDepositEnabled()) {
            this.selectNearestDepositChest();
            return;
        }
        Vec3d VanillaChestLootTableGenerator = CropFarmMode.minecraftClient.player.getPos();
        int n2 = -1;
        boolean bl = false;
        double d2 = Double.MAX_VALUE;
        for (n = 0; n < this.matureCropPositions.size(); ++n) {
            adminsky = this.matureCropPositions.get(n);
            if (!this.isMatureCrop(adminsky) || !((d = Vec3d.ofCenter((Vec3i)adminsky).squaredDistanceTo(VanillaChestLootTableGenerator)) < d2)) continue;
            d2 = d;
            n2 = n;
            bl = false;
        }
        for (n = 0; n < this.plantableCropPositions.size(); ++n) {
            adminsky = this.plantableCropPositions.get(n);
            if (!this.isCropPlantingLocationReady(adminsky) || !((d = Vec3d.ofCenter((Vec3i)adminsky).squaredDistanceTo(VanillaChestLootTableGenerator)) < d2)) continue;
            d2 = d;
            n2 = n;
            bl = true;
        }
        if (n2 >= 0) {
            this.targetCropPosition = bl ? this.plantableCropPositions.remove(n2) : this.matureCropPositions.remove(n2);
        } else {
            this.matureCropPositions.clear();
            this.plantableCropPositions.clear();
        }
        if (this.targetCropPosition == null) {
            this.beginDropCollection();
            return;
        }
        this.automationState = CropAutomationState.NAVIGATING_TO_CROP;
        this.navigationActive = false;
        this.clearRotationTarget();
    }

    private boolean isCropPlantingLocationReady(BlockPos adminsky) {
        BlockState class_26802 = CropFarmMode.minecraftClient.world.getBlockState(adminsky);
        return class_26802.isAir() && this.isPlantingSurfaceValid(adminsky);
    }

    private boolean isCropTargetReachable(BlockPos adminsky) {
        return this.isMatureCrop(adminsky) || this.isCropPlantingLocationReady(adminsky);
    }

    private void beginDropCollection() {
        this.navigationActive = false;
        this.clearRotationTarget();
        if (!this.pickupSetting.isEnabled()) {
            this.automationState = CropAutomationState.WAITING_FOR_RESOURCES;
            this.stateTransitionCooldown.reset();
            return;
        }
        this.scanNearbyDrops();
        this.automationState = CropAutomationState.COLLECTING_DROPS;
        this.advanceDropCollection();
    }

    private void advanceDropCollection() {
        this.targetDropEntity = null;
        this.navigationActive = false;
        if (this.isAutoDepositEnabled()) {
            this.selectNearestDepositChest();
            return;
        }
        while (!this.nearbyDropEntities.isEmpty()) {
            ItemEntity class_15422 = this.nearbyDropEntities.removeFirst();
            if (class_15422 == null || class_15422.isRemoved() || !class_15422.isAlive()) continue;
            this.targetDropEntity = class_15422;
            return;
        }
        this.automationState = CropAutomationState.WAITING_FOR_RESOURCES;
        this.stateTransitionCooldown.reset();
    }

    private void navigateToDroppedItem() {
        if (this.targetDropEntity == null) {
            this.advanceDropCollection();
            return;
        }
        if (this.targetDropEntity.isRemoved() || !this.targetDropEntity.isAlive()) {
            this.cancelCropNavigation();
            this.advanceDropCollection();
            return;
        }
        Vec3d VanillaChestLootTableGenerator = this.targetDropEntity.getPos();
        if (CropFarmMode.minecraftClient.player.getPos().squaredDistanceTo(VanillaChestLootTableGenerator) <= 1.9599999999999997) {
            this.cancelCropNavigation();
            this.advanceDropCollection();
            return;
        }
        if (!this.navigationActive) {
            TextureReloadProvider.getTextureReloadTask().registerCollisionProbe(new BlockRegion(BlockPos.ofFloored((Position)VanillaChestLootTableGenerator), 1));
            this.navigationActive = true;
            this.navigationStartedAt = System.currentTimeMillis();
            return;
        }
        if (!TextureReloadProvider.getTextureReloadTask().isNavigationActive()) {
            this.navigationActive = false;
            return;
        }
        if (System.currentTimeMillis() - this.navigationStartedAt > 10000L) {
            this.cancelCropNavigation();
            this.advanceDropCollection();
        }
    }

    private void scanNearbyDrops() {
        this.nearbyDropEntities.clear();
        if (CropFarmMode.minecraftClient.world == null || CropFarmMode.minecraftClient.player == null) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = CropFarmMode.minecraftClient.player.getPos();
        double d = this.scanRadiusSetting.getValue() + 4.0f;
        double d2 = d * d;
        ArrayList<ItemEntity> arrayList = new ArrayList<ItemEntity>();
        for (Entity class_12972 : CropFarmMode.minecraftClient.world.getEntities()) {
            ItemEntity class_15423;
            if (!(class_12972 instanceof ItemEntity) || (class_15423 = (ItemEntity)class_12972).isRemoved() || !class_15423.isAlive() || VanillaChestLootTableGenerator.squaredDistanceTo(class_15423.getPos()) > d2 || !this.isSelectedCropItem(class_15423.getStack())) continue;
            arrayList.add(class_15423);
        }
        arrayList.sort(Comparator.comparingDouble(class_15422 -> VanillaChestLootTableGenerator.squaredDistanceTo(class_15422.getPos())));
        this.nearbyDropEntities.addAll(arrayList);
    }

    private boolean isSelectedCropItem(ItemStack class_17992) {
        Item class_17922 = class_17992.getItem();
        if (this.netherWartOption.isSelected()) {
            return class_17922 == Items.NETHER_WART;
        }
        if (this.wheatOption.isSelected()) {
            return class_17922 == Items.WHEAT || class_17922 == Items.WHEAT_SEEDS;
        }
        if (this.carrotOption.isSelected()) {
            return class_17922 == Items.CARROT;
        }
        if (this.potatoOption.isSelected()) {
            return class_17922 == Items.POTATO || class_17922 == Items.POISONOUS_POTATO;
        }
        if (this.beetrootOption.isSelected()) {
            return class_17922 == Items.BEETROOT || class_17922 == Items.BEETROOT_SEEDS;
        }
        if (this.sugarCaneOption.isSelected()) {
            return class_17922 == Items.SUGAR_CANE;
        }
        return false;
    }

    private void cancelCropNavigation() {
        if (TextureReloadProvider.isInstalled() && TextureReloadProvider.getTextureReloadTask().isNavigationActive()) {
            TextureReloadProvider.getTextureReloadTask().clearNavigationTasks();
        }
        this.navigationActive = false;
    }

    private void scanCropBlocks() {
        BlockPos adminsky = CropFarmMode.minecraftClient.player.getBlockPos();
        int n = (int)this.scanRadiusSetting.getValue();
        int n2 = (int)this.verticalRangeSetting.getValue();
        Predicate<BlockState> predicate = this.getSelectedCropPredicate();
        boolean bl = this.replantSetting.isEnabled() && !this.sugarCaneOption.isSelected();
        for (int i = -n2; i <= n2; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockPos adminsky2 = adminsky.add(j, i, k);
                    BlockState class_26802 = CropFarmMode.minecraftClient.world.getBlockState(adminsky2);
                    if (predicate.test(class_26802) && this.isCropStateValid(adminsky2, class_26802)) {
                        this.matureCropPositions.add(adminsky2.toImmutable());
                        continue;
                    }
                    if (!bl || !class_26802.isAir() || !this.isPlantingSurfaceValid(adminsky2)) continue;
                    this.plantableCropPositions.add(adminsky2.toImmutable());
                }
            }
        }
    }

    private void sortCropTargets() {
        int n;
        if (this.matureCropPositions.isEmpty()) {
            return;
        }
        int n2 = this.findDominantCropAxis(this.matureCropPositions, true);
        boolean bl = n2 >= (n = this.findDominantCropAxis(this.matureCropPositions, false));
        Comparator<BlockPos> comparator = bl ? Comparator.comparingInt(Vec3i::getZ) : Comparator.comparingInt(Vec3i::getX);
        Comparator<BlockPos> comparator2 = bl ? Comparator.comparingInt(Vec3i::getX) : Comparator.comparingInt(Vec3i::getZ);
        this.matureCropPositions.sort(comparator.thenComparingInt(Vec3i::getY).thenComparing(comparator2));
        ArrayList<BlockPos> reorderedTargets = new ArrayList<>(this.matureCropPositions.size());
        int n3 = 0;
        int n4 = 0;
        while (n3 < this.matureCropPositions.size()) {
            BlockPos targetPosition;
            int n5;
            int n6 = bl ? this.matureCropPositions.get(n3).getZ() : this.matureCropPositions.get(n3).getX();
            int n7 = this.matureCropPositions.get(n3).getY();
            for (n5 = n3; n5 < this.matureCropPositions.size(); ++n5) {
                int n8;
                targetPosition = this.matureCropPositions.get(n5);
                int n9 = n8 = bl ? targetPosition.getZ() : targetPosition.getX();
                if (n8 != n6 || targetPosition.getY() != n7) break;
            }
            List<BlockPos> targetGroup = new ArrayList<>(this.matureCropPositions.subList(n3, n5));
            if ((n4 & 1) != 0) {
                Collections.reverse(targetGroup);
            }
            reorderedTargets.addAll(targetGroup);
            ++n4;
            n3 = n5;
        }
        this.matureCropPositions.clear();
        this.matureCropPositions.addAll(reorderedTargets);
    }

    private int findDominantCropAxis(List<BlockPos> list, boolean bl) {
        int n;
        HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        for (BlockPos object2 : list) {
            n = bl ? object2.getZ() : object2.getX();
            hashMap.merge(n, 1, Integer::sum);
        }
        int n2 = 0;
        Iterator iterator = hashMap.values().iterator();
        while (iterator.hasNext()) {
            n = (Integer)iterator.next();
            if (n <= n2) continue;
            n2 = n;
        }
        return n2;
    }

    private BlockPos findSupportBlock(BlockPos adminsky) {
        Direction[] class_2350Array = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        BlockPos adminsky2 = null;
        Vec3d VanillaChestLootTableGenerator = CropFarmMode.minecraftClient.player.getPos();
        double d = Double.MAX_VALUE;
        for (Direction class_23502 : class_2350Array) {
            double d2;
            BlockPos adminsky3 = adminsky.offset(class_23502);
            if (!this.isSupportBlockValid(adminsky3) || !((d2 = Vec3d.ofCenter((Vec3i)adminsky3).squaredDistanceTo(VanillaChestLootTableGenerator)) < d)) continue;
            d = d2;
            adminsky2 = adminsky3;
        }
        return adminsky2;
    }

    private boolean isSupportBlockValid(BlockPos adminsky) {
        BlockState class_26802 = CropFarmMode.minecraftClient.world.getBlockState(adminsky.down());
        BlockState class_26803 = CropFarmMode.minecraftClient.world.getBlockState(adminsky);
        BlockState class_26804 = CropFarmMode.minecraftClient.world.getBlockState(adminsky.up());
        if (class_26802.isAir()) {
            return false;
        }
        if (!class_26803.getCollisionShape((BlockView)CropFarmMode.minecraftClient.world, adminsky).isEmpty()) {
            return false;
        }
        return class_26804.getCollisionShape((BlockView)CropFarmMode.minecraftClient.world, adminsky.up()).isEmpty();
    }

    private Predicate<BlockState> getSelectedCropPredicate() {
        if (this.netherWartOption.isSelected()) {
            return class_26802 -> class_26802.getBlock() == Blocks.NETHER_WART;
        }
        if (this.wheatOption.isSelected()) {
            return class_26802 -> class_26802.getBlock() == Blocks.WHEAT;
        }
        if (this.carrotOption.isSelected()) {
            return class_26802 -> class_26802.getBlock() == Blocks.CARROTS;
        }
        if (this.potatoOption.isSelected()) {
            return class_26802 -> class_26802.getBlock() == Blocks.POTATOES;
        }
        if (this.beetrootOption.isSelected()) {
            return class_26802 -> class_26802.getBlock() == Blocks.BEETROOTS;
        }
        if (this.sugarCaneOption.isSelected()) {
            return class_26802 -> class_26802.getBlock() == Blocks.SUGAR_CANE;
        }
        return class_26802 -> false;
    }

    private boolean isMatureCrop(BlockPos adminsky) {
        BlockState class_26802 = CropFarmMode.minecraftClient.world.getBlockState(adminsky);
        return this.getSelectedCropPredicate().test(class_26802) && this.isCropStateValid(adminsky, class_26802);
    }

    private boolean isCropStateValid(BlockPos adminsky, BlockState class_26802) {
        Block class_22482 = class_26802.getBlock();
        if (class_22482 instanceof CropBlock) {
            CropBlock class_23022 = (CropBlock)class_22482;
            return class_23022.isMature(class_26802);
        }
        if (class_22482 == Blocks.NETHER_WART) {
            return (Integer)class_26802.get((Property)NetherWartBlock.AGE) >= 3;
        }
        if (class_22482 == Blocks.SUGAR_CANE) {
            return CropFarmMode.minecraftClient.world.getBlockState(adminsky.down()).getBlock() == Blocks.SUGAR_CANE;
        }
        return false;
    }

    private boolean isPlantingSurfaceValid(BlockPos adminsky) {
        if (this.sugarCaneOption.isSelected()) {
            return false;
        }
        Block class_22482 = CropFarmMode.minecraftClient.world.getBlockState(adminsky.down()).getBlock();
        if (this.netherWartOption.isSelected()) {
            return class_22482 == Blocks.SOUL_SAND;
        }
        return class_22482 == Blocks.FARMLAND;
    }

    private Item getSelectedSeedItem() {
        if (this.netherWartOption.isSelected()) {
            return Items.NETHER_WART;
        }
        if (this.wheatOption.isSelected()) {
            return Items.WHEAT_SEEDS;
        }
        if (this.carrotOption.isSelected()) {
            return Items.CARROT;
        }
        if (this.potatoOption.isSelected()) {
            return Items.POTATO;
        }
        if (this.beetrootOption.isSelected()) {
            return Items.BEETROOT_SEEDS;
        }
        if (this.sugarCaneOption.isSelected()) {
            return Items.SUGAR_CANE;
        }
        return null;
    }

    private boolean isCropWithinInteractionRange(BlockPos adminsky) {
        double d = CropFarmMode.minecraftClient.player.getBlockInteractionRange();
        return CropFarmMode.minecraftClient.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky)) <= d * d;
    }

    private Direction getClosestInteractionSide(BlockPos adminsky) {
        Vec3d VanillaChestLootTableGenerator = CropFarmMode.minecraftClient.player.getEyePos();
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

    private Vec3d getCropHitPosition(BlockPos adminsky, Direction class_23502) {
        return Vec3d.ofCenter((Vec3i)adminsky).add((double)class_23502.getOffsetX() * 0.5, (double)class_23502.getOffsetY() * 0.5, (double)class_23502.getOffsetZ() * 0.5);
    }

    private boolean isRotationAligned(Vec3d VanillaChestLootTableGenerator) {
        Rotation rotation = AimRotationMath.getRotationToPoint(VanillaChestLootTableGenerator);
        if (this.targetRotation == null || this.targetRotation.angleDistanceTo(rotation) > 0.5f) {
            this.targetRotation = rotation;
            this.rotationConfirmationCount = 0;
        }
        RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, 180.0f, 180.0f, 180.0f, RotationPriority.ITEM_USE_PRIORITY);
        Rotation rotation2 = RockstarClient.create().getRotationManager().getCurrentRotation();
        if (rotation2 != null && rotation2.angleDistanceTo(rotation) <= 1.5f) {
            ++this.rotationConfirmationCount;
            return this.rotationConfirmationCount >= 1;
        }
        return false;
    }

    private void clearRotationTarget() {
        this.targetRotation = null;
        this.rotationConfirmationCount = 0;
    }

    private void selectBestCropTool(BlockState class_26802) {
        Predicate<ItemStack> predicate = class_17992 -> class_17992.getItem() instanceof HoeItem && CropFarmMode.hasUsableToolDurability(class_17992);
        this.equipBestCropTool(class_26802, predicate);
    }

    private boolean equipBestCropTool(BlockState class_26802, Predicate<ItemStack> predicate) {
        ItemStack class_17992 = CropFarmMode.minecraftClient.player.getMainHandStack();
        ToolMiningStats toolMiningStats = this.getToolMiningStats(class_17992, class_26802, predicate);
        ItemRule itemRule = null;
        for (ItemRule itemRule2 : ItemRuleSets.getHotbarRules().combineRules(ItemRuleSets.getInventoryRules()).getRules()) {
            ItemStack class_17993 = itemRule2.getItemStack();
            ToolMiningStats toolMiningStats2 = this.getToolMiningStats(class_17993, class_26802, predicate);
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
            int n = CropFarmMode.minecraftClient.player.getInventory().selectedSlot;
            InventoryUtils.dropItem(((InventorySlotRule)itemRule2).getClickSlot(), n);
        }
        return true;
    }

    private ToolMiningStats getToolMiningStats(ItemStack class_17992, BlockState class_26802, Predicate<ItemStack> predicate) {
        if (class_17992 == null || class_17992.isEmpty() || !predicate.test(class_17992)) {
            return new ToolMiningStats(-1.0f, -1, -1, -1.0f);
        }
        float f = class_17992.getMiningSpeedMultiplier(class_26802);
        int n = EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.EFFICIENCY);
        int n2 = EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.FORTUNE);
        float f2 = CropFarmMode.getToolDurabilityRatio(class_17992);
        return new ToolMiningStats(f, n, n2, f2);
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

    private static boolean hasUsableToolDurability(ItemStack class_17992) {
        return CropFarmMode.getToolDurabilityRatio(class_17992) > 0.05f;
    }

    private boolean isAutoDepositEnabled() {
        if (!this.autoDepositSetting.isEnabled()) {
            return false;
        }
        if (CropFarmMode.minecraftClient.player == null) {
            return false;
        }
        PlayerInventory class_16612 = CropFarmMode.minecraftClient.player.getInventory();
        if (class_16612.getEmptySlot() != -1) {
            return false;
        }
        return this.hasEnoughCropItems();
    }

    private boolean hasEnoughCropItems() {
        Item class_17922 = this.getSelectedSeedItem();
        PlayerInventory class_16612 = CropFarmMode.minecraftClient.player.getInventory();
        int n = 0;
        for (int i = 0; i < class_16612.size(); ++i) {
            ItemStack class_17992 = class_16612.getStack(i);
            if (class_17992.isEmpty() || !this.isSelectedCropItem(class_17992)) continue;
            if (class_17922 != null && class_17992.getItem() == class_17922) {
                n += class_17992.getCount();
                continue;
            }
            return true;
        }
        return n > this.getRequiredCropCount();
    }

    private int getRequiredCropCount() {
        Item class_17922 = this.getSelectedSeedItem();
        return class_17922 != null ? class_17922.getDefaultStack().getMaxCount() : 64;
    }

    private void selectNearestDepositChest() {
        this.cancelCropNavigation();
        this.clearRotationTarget();
        this.navigationActive = false;
        this.selectedInventorySlot = -1;
        this.selectedItemCount = -1;
        BlockPos adminsky = this.findNearestDepositChest();
        if (adminsky == null) {
            this.showWarning("modules.crop_farm.no_chest");
            this.automationState = CropAutomationState.WAITING_FOR_RESOURCES;
            this.stateTransitionCooldown.reset();
            return;
        }
        this.targetChestPosition = adminsky;
        this.automationState = CropAutomationState.NAVIGATING_TO_CHEST;
    }

    private BlockPos findNearestDepositChest() {
        if (CropFarmMode.minecraftClient.world == null || CropFarmMode.minecraftClient.player == null) {
            return null;
        }
        BlockPos adminsky = CropFarmMode.minecraftClient.player.getBlockPos();
        int n = (int)this.scanRadiusSetting.getValue() + 4;
        BlockPos adminsky2 = null;
        double d = Double.MAX_VALUE;
        for (BlockPos adminsky3 : BlockPos.iterateOutwards((BlockPos)adminsky, (int)n, (int)n, (int)n)) {
            double d2;
            BlockPos adminsky4 = adminsky3.toImmutable();
            if (this.unreachableChestPositions.contains(adminsky4) || !(CropFarmMode.minecraftClient.world.getBlockEntity(adminsky4) instanceof ChestBlockEntity) || !((d2 = adminsky3.getSquaredDistance((Position)CropFarmMode.minecraftClient.player.getPos())) < d)) continue;
            d = d2;
            adminsky2 = adminsky4;
        }
        return adminsky2;
    }

    private void navigateToDepositChest() {
        if (this.targetChestPosition == null) {
            this.automationState = CropAutomationState.IDLE;
            this.stateTransitionCooldown.reset();
            return;
        }
        if (this.isCropWithinInteractionRange(this.targetChestPosition)) {
            this.cancelCropNavigation();
            this.automationState = CropAutomationState.RETURNING_TO_CROPS;
            this.clearRotationTarget();
            this.actionCooldown.reset();
            return;
        }
        if (!this.navigationActive) {
            TextureReloadProvider.getTextureReloadTask().registerCollisionProbe(new BlockRegion(this.targetChestPosition, 2));
            this.navigationActive = true;
            this.navigationStartedAt = System.currentTimeMillis();
            return;
        }
        if (!TextureReloadProvider.getTextureReloadTask().isNavigationActive()) {
            this.navigationActive = false;
            return;
        }
        if (System.currentTimeMillis() - this.navigationStartedAt > 20000L) {
            this.cancelCropNavigation();
            this.unreachableChestPositions.add(this.targetChestPosition);
            this.targetChestPosition = null;
            this.selectNearestDepositChest();
        }
    }

    private void openDepositChest() {
        if (this.targetChestPosition == null) {
            this.automationState = CropAutomationState.IDLE;
            this.stateTransitionCooldown.reset();
            return;
        }
        if (CropFarmMode.minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
            this.automationState = CropAutomationState.TRANSFERRING_ITEMS;
            this.actionCooldown.reset();
            this.selectedInventorySlot = -1;
            this.selectedItemCount = -1;
            return;
        }
        if (!this.isCropWithinInteractionRange(this.targetChestPosition)) {
            this.automationState = CropAutomationState.NAVIGATING_TO_CHEST;
            this.navigationActive = false;
            return;
        }
        Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)this.targetChestPosition);
        if (!this.isRotationAligned(VanillaChestLootTableGenerator)) {
            return;
        }
        if (!this.actionCooldown.hasElapsed((long)this.actionDelaySetting.getValue())) {
            return;
        }
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, this.targetChestPosition, false);
        CropFarmMode.minecraftClient.interactionManager.interactBlock(CropFarmMode.minecraftClient.player, Hand.MAIN_HAND, class_39652);
        CropFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        this.actionCooldown.reset();
    }

    private void transferCropItemsToChest() {
        ScreenHandler class_17032 = CropFarmMode.minecraftClient.player.currentScreenHandler;
        if (!(class_17032 instanceof GenericContainerScreenHandler)) {
            this.resetDepositState(false);
            return;
        }
        GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)class_17032;
        if (!this.actionCooldown.hasElapsed((long)this.actionDelaySetting.getValue())) {
            return;
        }
        ItemStack selectedStack = this.selectedInventorySlot == -1
            ? ItemStack.EMPTY
            : class_17072.slots.get(this.selectedInventorySlot).getStack();
        if (this.selectedInventorySlot != -1 && !selectedStack.isEmpty()
                && selectedStack.getCount() >= this.selectedItemCount
                && this.isSelectedCropItem(selectedStack)) {
            this.closeDepositChest();
            return;
        }
        int n = this.findBestCropHotbarSlot();
        DefaultedList<Slot> containerSlots = class_17072.slots;
        for (int i = 0; i < containerSlots.size(); ++i) {
            ItemStack class_17992;
            Slot class_17352 = containerSlots.get(i);
            if (class_17352.inventory != CropFarmMode.minecraftClient.player.getInventory() || class_17352.getIndex() == n || (class_17992 = class_17352.getStack()).isEmpty() || !this.isSelectedCropItem(class_17992)) continue;
            InventoryUtils.quickMoveItem(i);
            this.selectedInventorySlot = i;
            this.selectedItemCount = class_17992.getCount();
            this.actionCooldown.reset();
            return;
        }
        this.resetDepositState(true);
    }

    private void closeDepositChest() {
        if (this.targetChestPosition != null) {
            this.unreachableChestPositions.add(this.targetChestPosition);
        }
        CropFarmMode.minecraftClient.player.closeHandledScreen();
        this.targetChestPosition = null;
        this.selectedInventorySlot = -1;
        this.selectedItemCount = -1;
        this.selectNearestDepositChest();
    }

    private void resetDepositState(boolean bl) {
        if (bl && CropFarmMode.minecraftClient.currentScreen != null) {
            CropFarmMode.minecraftClient.player.closeHandledScreen();
        }
        this.targetChestPosition = null;
        this.selectedInventorySlot = -1;
        this.selectedItemCount = -1;
        this.automationState = CropAutomationState.IDLE;
        this.stateTransitionCooldown.reset();
        this.navigationActive = false;
        this.clearRotationTarget();
    }

    private int findBestCropHotbarSlot() {
        Item class_17922 = this.getSelectedSeedItem();
        if (class_17922 == null) {
            return -1;
        }
        PlayerInventory class_16612 = CropFarmMode.minecraftClient.player.getInventory();
        int n = -1;
        int n2 = -1;
        for (int i = 0; i < class_16612.size(); ++i) {
            ItemStack class_17992 = class_16612.getStack(i);
            if (class_17992.isEmpty() || class_17992.getItem() != class_17922 || class_17992.getCount() <= n2) continue;
            n2 = class_17992.getCount();
            n = i;
        }
        return n;
    }

    private Box getCropRenderBounds(BlockPos adminsky) {
        return new Box(adminsky).contract(0.02);
    }

    private boolean selectCropItem(Item class_17922) {
        if (CropFarmMode.minecraftClient.player.getMainHandStack().getItem() == class_17922) {
            return true;
        }
        Predicate<ItemStack> predicate = class_17992 -> class_17992.getItem() == class_17922;
        HotbarSlot hotbarSlot = ItemRuleSets.getHotbarRules().findByStack(predicate);
        if (hotbarSlot != null) {
            InventoryUtils.setSelectedHotbarSlot(hotbarSlot);
            return true;
        }
        InventorySlotRule inventorySlotRule = ItemRuleSets.getInventoryRules().findByStack(predicate);
        if (inventorySlotRule != null) {
            int n = CropFarmMode.minecraftClient.player.getInventory().selectedSlot;
            InventoryUtils.dropItem(inventorySlotRule.getClickSlot(), n);
            return true;
        }
        return false;
    }

    static enum CropAutomationState {
        IDLE,
        NAVIGATING_TO_CROP,
        HARVESTING,
        PLANTING,
        COLLECTING_DROPS,
        NAVIGATING_TO_CHEST,
        RETURNING_TO_CROPS,
        TRANSFERRING_ITEMS,
        WAITING_FOR_RESOURCES;
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
            if (this.fortuneLevel != toolMiningStats.fortuneLevel) {
                return this.fortuneLevel > toolMiningStats.fortuneLevel;
            }
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

