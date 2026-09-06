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
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ScreenHandler
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.BrewingStandScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.Slot
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Potion
 *  net.minecraft.PotionContentsComponent
 *  net.minecraft.Potions
 *  net.minecraft.ItemConvertible
 *  net.minecraft.ChestBlock
 *  net.minecraft.Direction
 *  net.minecraft.Position
 *  net.minecraft.Box
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3d
 *  net.minecraft.BrewingStandBlockEntity
 *  net.minecraft.ChestBlockEntity
 *  net.minecraft.BlockState
 *  net.minecraft.ChestType
 *  net.minecraft.Property
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.BlockHitResult
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.BrewingStandScreen
 *  net.minecraft.RegistryEntry
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.modules.player.farming.potion;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.math.Rotation;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.player.farming.core.AutoFarm;
import moscow.rockstar.modules.player.farming.core.FarmModeBase;
import moscow.rockstar.modules.player.farming.core.FarmState;
import moscow.rockstar.modules.player.farming.hud.FarmDisplayCategory;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationType;
import moscow.rockstar.util.Timer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.potion.Potions;
import net.minecraft.item.ItemConvertible;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.ChestType;
import net.minecraft.state.property.Property;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.DataComponentTypes;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public class PotionFarmMode
extends FarmModeBase {
    private final ModeSetting potionTypeSetting;
    private final ModeSetting.Option strengthPotionOption;
    private final ModeSetting.Option speedPotionOption;
    private final ModeSetting.Option fireResistancePotionOption;
    private final ModeSetting.Option invisibilityPotionOption;
    private final ModeSetting.Option regenerationPotionOption;
    private final ModeSetting.Option healingPotionOption;
    private final ModeSetting.Option strongHealingPotionOption;
    private final ModeSetting stackModeSetting;
    private final ModeSetting.Option multipleStackOption;
    private final ModeSetting.Option singleStackOption;
    private BooleanSetting enhancePotionsSetting;
    private BooleanSetting useChestsSetting;
    private BooleanSetting targetEspSetting;
    private NumberSetting brewDelaySetting;
    private NumberSetting singleStackDelaySetting;
    private static final int MAX_POTION_TARGETS = 5;
    private static final int MAX_POTION_SCAN_RADIUS = 8;
    private static final int MAX_BREWING_QUEUE_SIZE = 400;
    private static final float ROTATION_TOLERANCE = 1.5f;
    private final Timer actionCooldown = new Timer();
    private final Timer containerCooldown = new Timer();
    private final Timer inventoryTransferCooldown = new Timer();
    private PotionAutomationState automationState = PotionAutomationState.SELECTING_POTION_TARGET;
    private BlockPos potionStandPosition;
    private BlockPos containerPosition;
    private final List<BlockPos> potionStandPositions = new ArrayList<BlockPos>();
    private final Map<BlockPos, Long> potionStandRetryTimes = new HashMap<BlockPos, Long>();
    private final Map<Item, BlockPos> ingredientContainerByItem = new HashMap<Item, BlockPos>();
    private final Set<BlockPos> visitedContainerPositions = new HashSet<BlockPos>();
    private Item requiredIngredientItem;
    private Rotation targetRotation;
    private int rotationConfirmationCount;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (PotionFarmMode.minecraftClient.player == null || PotionFarmMode.minecraftClient.world == null) {
            return;
        }
        switch (this.automationState.ordinal()) {
            case 0: {
                this.selectNextPotionStand();
                break;
            }
            case 1: {
                this.processPotionStand();
                break;
            }
            case 2: {
                this.brewPotionBatch();
                break;
            }
            case 3: {
                this.processContainerTransfer(true);
                break;
            }
            case 4: {
                this.transferPotionItems();
                break;
            }
            case 5: {
                this.processContainerTransfer(false);
                break;
            }
            case 6: {
                this.transferIngredientToBrewingStand();
                break;
            }
            case 7: {
                this.finishPotionCycle();
            }
        }
    };
    private static final ColorRGBA potionStandColor = new ColorRGBA(99.0f, 196.0f, 255.0f);
    private static final ColorRGBA containerColor = new ColorRGBA(255.0f, 196.0f, 64.0f);
    private final EventListener<Render3DEvent> render3DListener = render3DEvent -> {
        BlockPos adminsky;
        if (!this.targetEspSetting.isEnabled()) {
            return;
        }
        if (PotionFarmMode.minecraftClient.world == null || PotionFarmMode.minecraftClient.player == null) {
            return;
        }
        BlockPos adminsky2 = this.automationState == PotionAutomationState.NAVIGATING_TO_POTION_TARGET || this.automationState == PotionAutomationState.BREWING_POTIONS ? this.potionStandPosition : null;
        BlockPos adminsky3 = adminsky = this.automationState == PotionAutomationState.OPENING_DEPOSIT_CONTAINER || this.automationState == PotionAutomationState.DEPOSITING_POTIONS || this.automationState == PotionAutomationState.OPENING_RESTOCK_CONTAINER || this.automationState == PotionAutomationState.RESTOCKING_INGREDIENTS ? this.containerPosition : null;
        if (adminsky2 == null && adminsky == null) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Camera class_41842 = PotionFarmMode.minecraftClient.gameRenderer.getCamera();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        if (adminsky2 != null) {
            RenderUtils.drawFilledBox(class_45872, class_2872, this.getRenderBounds(adminsky2).offset(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z), potionStandColor.withAlpha(45.0f));
        }
        if (adminsky != null) {
            RenderUtils.drawFilledBox(class_45872, class_2872, this.getRenderBounds(adminsky).offset(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z), containerColor.withAlpha(45.0f));
        }
        ItemRenderUtils.flushVertexConsumer(class_2872);
        BufferBuilder CreativeInventoryActionC2SPacket = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        if (adminsky2 != null) {
            RenderUtils.drawBoxOutline(class_45872, CreativeInventoryActionC2SPacket, this.getRenderBounds(adminsky2).offset(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z), potionStandColor.withAlpha(180.0f));
        }
        if (adminsky != null) {
            RenderUtils.drawBoxOutline(class_45872, CreativeInventoryActionC2SPacket, this.getRenderBounds(adminsky).offset(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z), containerColor.withAlpha(180.0f));
        }
        ItemRenderUtils.flushVertexConsumer(CreativeInventoryActionC2SPacket);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    };

    public PotionFarmMode(AutoFarm autoFarm, ModeSetting modeSetting) {
        super(autoFarm, modeSetting, "modules.settings.auto_farm.modes.potion");
        this.potionTypeSetting = new ModeSetting((SettingOwner)autoFarm, "modules.settings.potion_farm.brew", () -> !this.isSelected());
        this.strengthPotionOption = new ModeSetting.Option(this.potionTypeSetting, "modules.settings.potion_farm.potion.strength").select();
        this.speedPotionOption = new ModeSetting.Option(this.potionTypeSetting, "modules.settings.potion_farm.potion.speed");
        this.fireResistancePotionOption = new ModeSetting.Option(this.potionTypeSetting, "modules.settings.potion_farm.potion.fire_resistance");
        this.invisibilityPotionOption = new ModeSetting.Option(this.potionTypeSetting, "modules.settings.potion_farm.potion.invisibility");
        this.regenerationPotionOption = new ModeSetting.Option(this.potionTypeSetting, "modules.settings.potion_farm.potion.regen");
        this.healingPotionOption = new ModeSetting.Option(this.potionTypeSetting, "modules.settings.potion_farm.potion.healing");
        this.strongHealingPotionOption = new ModeSetting.Option(this.potionTypeSetting, "modules.settings.potion_farm.potion.strong_healing");
        this.stackModeSetting = new ModeSetting((SettingOwner)autoFarm, "modules.settings.potion_farm.stack_mode", () -> !this.isSelected());
        this.multipleStackOption = new ModeSetting.Option(this.stackModeSetting, "modules.settings.potion_farm.stack_mode.multiple").select();
        this.singleStackOption = new ModeSetting.Option(this.stackModeSetting, "modules.settings.potion_farm.stack_mode.single");
        this.enhancePotionsSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.potion_farm.enhance", () -> !this.isSelected() || !this.invisibilityPotionOption.isSelected());
        this.useChestsSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.potion_farm.use_chests", () -> !this.isSelected()).enable();
        this.targetEspSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.potion_farm.target_esp", () -> !this.isSelected()).enable();
        this.brewDelaySetting = new NumberSetting(autoFarm, "modules.settings.potion_farm.delay", "modules.settings.potion_farm.delay.description", () -> !this.isSelected()).setStep(10.0f).setMinValue(0.0f).setMaxValue(1500.0f).setValue(250.0f);
        this.singleStackDelaySetting = new NumberSetting(autoFarm, "modules.settings.potion_farm.single_stack_delay", "modules.settings.potion_farm.single_stack_delay.description", () -> !this.isSelected() || !this.singleStackOption.isSelected()).setStep(10.0f).setMinValue(50.0f).setMaxValue(2000.0f).setValue(250.0f);
    }

    @Override
    public void startFarmAutomation() {
        this.resetPotionState();
    }

    @Override
    public void resetBrewingState() {
        this.resetPotionState();
        if (PotionFarmMode.minecraftClient.player != null && PotionFarmMode.minecraftClient.currentScreen instanceof BrewingStandScreen) {
            PotionFarmMode.minecraftClient.player.closeHandledScreen();
        }
    }

    private void resetPotionState() {
        this.automationState = PotionAutomationState.SELECTING_POTION_TARGET;
        this.potionStandPosition = null;
        this.containerPosition = null;
        this.potionStandPositions.clear();
        this.visitedContainerPositions.clear();
        this.requiredIngredientItem = null;
        this.actionCooldown.reset();
        this.targetRotation = null;
        this.rotationConfirmationCount = 0;
        this.inventoryTransferCooldown.reset();
    }

    @Override
    public FarmState getFarmState() {
        return switch (this.automationState.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 7 -> FarmState.IDLE;
            case 1, 2 -> FarmState.WORKING;
            case 3, 4 -> FarmState.DEPOSIT;
            case 5, 6 -> FarmState.RESTOCKING;
        };
    }

    @Override
    public FarmDisplayCategory getFarmDisplayCategory() {
        return FarmDisplayCategory.POTIONS;
    }

    @Override
    public ItemStack getFarmDisplayItem() {
        return new ItemStack((ItemConvertible)Items.POTION);
    }

    private void selectNextPotionStand() {
        if (!this.actionCooldown.hasElapsed((long)this.brewDelaySetting.getValue())) {
            return;
        }
        if (this.potionStandPositions.isEmpty()) {
            this.potionStandPositions.addAll(this.scanBrewingStandPositions());
            if (this.potionStandPositions.isEmpty()) {
                return;
            }
        }
        long l = System.currentTimeMillis();
        BlockPos adminsky = null;
        for (BlockPos adminsky2 : this.potionStandPositions) {
            Long l2 = this.potionStandRetryTimes.get(adminsky2);
            if (l2 != null && l2 > l) continue;
            adminsky = adminsky2;
            break;
        }
        if (adminsky == null) {
            if (this.actionCooldown.hasElapsed(2000L)) {
                this.potionStandPositions.clear();
                this.actionCooldown.reset();
            }
            return;
        }
        this.potionStandPositions.remove(adminsky);
        this.potionStandPosition = adminsky;
        this.resetRotationTarget();
        this.automationState = PotionAutomationState.NAVIGATING_TO_POTION_TARGET;
        this.actionCooldown.reset();
    }

    private void processPotionStand() {
        if (this.potionStandPosition == null) {
            this.automationState = PotionAutomationState.SELECTING_POTION_TARGET;
            return;
        }
        if (PotionFarmMode.minecraftClient.currentScreen instanceof BrewingStandScreen) {
            this.automationState = PotionAutomationState.BREWING_POTIONS;
            this.actionCooldown.reset();
            return;
        }
        if (!this.isBlockWithinInteractionRange(this.potionStandPosition)) {
            this.potionStandRetryTimes.put(this.potionStandPosition, System.currentTimeMillis() + 5000L);
            this.automationState = PotionAutomationState.SELECTING_POTION_TARGET;
            return;
        }
        if (!this.isPotionRotationAligned(Vec3d.ofCenter((Vec3i)this.potionStandPosition))) {
            return;
        }
        if (!this.actionCooldown.hasElapsed((long)this.brewDelaySetting.getValue())) {
            return;
        }
        this.interactWithBlockPosition(this.potionStandPosition);
        this.actionCooldown.reset();
    }

    private void brewPotionBatch() {
        int n;
        ScreenHandler class_17032 = PotionFarmMode.minecraftClient.player.currentScreenHandler;
        if (!(class_17032 instanceof BrewingStandScreenHandler)) {
            this.automationState = PotionAutomationState.SELECTING_POTION_TARGET;
            return;
        }
        BrewingStandScreenHandler class_17082 = (BrewingStandScreenHandler)class_17032;
        if (!this.actionCooldown.hasElapsed((long)this.brewDelaySetting.getValue() / 2L)) {
            return;
        }
        if (class_17082.getBrewTime() > 0) {
            long l = (long)((double)class_17082.getBrewTime() / 20.0 * 1000.0);
            this.potionStandRetryTimes.put(this.potionStandPosition, System.currentTimeMillis() + l + 250L);
            this.automationState = PotionAutomationState.FINISHING_POTION_CYCLE;
            this.actionCooldown.reset();
            return;
        }
        Potion targetPotion = this.getPotionContents();
        for (n = 0; n < 3; ++n) {
            ItemStack class_17992 = class_17082.getSlot(n).getStack();
            if (class_17992.isEmpty() || !this.isPotionStackCompatible(class_17992, targetPotion)) continue;
            InventoryUtils.quickMoveItem(n);
            this.getFarmMetrics().addBlockCount(Math.max(1, class_17992.getCount()));
            this.actionCooldown.reset();
            return;
        }
        if (class_17082.getFuel() == 0 && class_17082.getSlot(4).getStack().isEmpty()) {
            if (!this.hasPotionItemInSlot(Items.BLAZE_POWDER, 4)) {
                this.queueRequiredIngredient(Items.BLAZE_POWDER);
                return;
            }
            return;
        }
        if (this.singleStackOption.isSelected()) {
            int n2;
            int[] nArray = new int[3];
            int n3 = 0;
            for (n2 = 0; n2 < 3; ++n2) {
                if (!class_17082.getSlot(n2).getStack().isEmpty()) continue;
                nArray[n3++] = n2;
            }
            if (n3 > 0) {
                if (!this.inventoryTransferCooldown.hasElapsed((long)this.singleStackDelaySetting.getValue())) {
                    return;
                }
                n2 = this.findBrewingIngredientSlot(class_17082);
                if (n2 == -1) {
                    this.queueRequiredIngredient(Items.POTION);
                    return;
                }
                int[] nArray2 = new int[n3];
                System.arraycopy(nArray, 0, nArray2, 0, n3);
                this.movePotionItemsToSlots(n2, nArray2);
                this.inventoryTransferCooldown.reset();
                this.actionCooldown.reset();
                return;
            }
        } else {
            for (n = 0; n < 3; ++n) {
                if (!class_17082.getSlot(n).getStack().isEmpty()) continue;
                int n4 = this.findBrewingIngredientSlot(class_17082);
                if (n4 == -1) {
                    this.queueRequiredIngredient(Items.POTION);
                    return;
                }
                InventoryUtils.quickMoveItem(n4);
                this.actionCooldown.reset();
                return;
            }
        }
        if (class_17082.getSlot(3).getStack().isEmpty()) {
            Item class_17922 = this.findMissingPotionIngredient(class_17082);
            if (class_17922 == null) {
                this.automationState = PotionAutomationState.FINISHING_POTION_CYCLE;
                this.actionCooldown.reset();
                return;
            }
            if (!this.hasPotionItemInSlot(class_17922, 3)) {
                this.queueRequiredIngredient(class_17922);
                return;
            }
            this.potionStandRetryTimes.put(this.potionStandPosition, System.currentTimeMillis() + 20000L + 500L);
            this.automationState = PotionAutomationState.FINISHING_POTION_CYCLE;
            this.actionCooldown.reset();
        }
    }

    private void processContainerTransfer(boolean bl) {
        if (PotionFarmMode.minecraftClient.player.currentScreenHandler instanceof BrewingStandScreenHandler) {
            PotionFarmMode.minecraftClient.player.closeHandledScreen();
            this.actionCooldown.reset();
            return;
        }
        if (this.containerPosition == null) {
            this.containerPosition = bl ? this.findNearestContainerPosition() : this.findIngredientContainer();
            this.resetRotationTarget();
            if (this.containerPosition == null) {
                RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.ERROR, bl ? Localization.translate("potion_farm.no_chest_title") : Localization.translate("potion_farm.no_item_title"), bl ? Localization.translate("potion_farm.no_chest") : Localization.translateFormatted("potion_farm.no_item", this.getPotionItemName(this.requiredIngredientItem)));
                if (!bl) {
                    this.requiredIngredientItem = null;
                }
                this.visitedContainerPositions.clear();
                this.automationState = PotionAutomationState.FINISHING_POTION_CYCLE;
                this.actionCooldown.reset();
                return;
            }
        }
        if (PotionFarmMode.minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
            this.automationState = bl ? PotionAutomationState.DEPOSITING_POTIONS : PotionAutomationState.RESTOCKING_INGREDIENTS;
            this.actionCooldown.reset();
            return;
        }
        if (!this.isBlockWithinInteractionRange(this.containerPosition)) {
            if (bl) {
                this.containerPosition = null;
                this.automationState = PotionAutomationState.FINISHING_POTION_CYCLE;
            } else {
                this.recordContainerSearchPosition(this.containerPosition);
                this.containerPosition = null;
                this.resetRotationTarget();
            }
            return;
        }
        if (!this.isPotionRotationAligned(Vec3d.ofCenter((Vec3i)this.containerPosition))) {
            return;
        }
        if (!this.actionCooldown.hasElapsed((long)this.brewDelaySetting.getValue())) {
            return;
        }
        this.interactWithBlockPosition(this.containerPosition);
        this.actionCooldown.reset();
    }

    private void transferPotionItems() {
        ScreenHandler class_17032 = PotionFarmMode.minecraftClient.player.currentScreenHandler;
        if (!(class_17032 instanceof GenericContainerScreenHandler)) {
            this.automationState = PotionAutomationState.FINISHING_POTION_CYCLE;
            return;
        }
        GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)class_17032;
        if (!this.actionCooldown.hasElapsed((long)this.brewDelaySetting.getValue())) {
            return;
        }
        DefaultedList<Slot> containerSlots = class_17072.slots;
        for (int i = 0; i < containerSlots.size(); ++i) {
            ItemStack class_17992;
            Slot class_17352 = containerSlots.get(i);
            if (class_17352.inventory != PotionFarmMode.minecraftClient.player.getInventory() || (class_17992 = class_17352.getStack()).isEmpty() || !this.isPotionItem(class_17992)) continue;
            InventoryUtils.quickMoveItem(i);
            this.actionCooldown.reset();
            return;
        }
        this.automationState = PotionAutomationState.FINISHING_POTION_CYCLE;
        this.actionCooldown.reset();
    }

    private void queueRequiredIngredient(Item class_17922) {
        if (!this.useChestsSetting.isEnabled()) {
            RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.ERROR, Localization.translate("potion_farm.no_item_title"), Localization.translateFormatted("potion_farm.no_item", this.getPotionItemName(class_17922)));
            this.automationState = PotionAutomationState.FINISHING_POTION_CYCLE;
            return;
        }
        this.requiredIngredientItem = class_17922;
        this.visitedContainerPositions.clear();
        this.automationState = PotionAutomationState.OPENING_RESTOCK_CONTAINER;
        this.actionCooldown.reset();
    }

    private String getPotionItemName(Item class_17922) {
        if (class_17922 == null) {
            return "";
        }
        if (class_17922 == Items.POTION) {
            return Localization.translate("potion_farm.item.water_bottle");
        }
        return class_17922.getName().getString();
    }

    private void transferIngredientToBrewingStand() {
        ScreenHandler class_17032 = PotionFarmMode.minecraftClient.player.currentScreenHandler;
        if (!(class_17032 instanceof GenericContainerScreenHandler)) {
            this.automationState = PotionAutomationState.FINISHING_POTION_CYCLE;
            return;
        }
        GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)class_17032;
        if (this.requiredIngredientItem == null) {
            this.automationState = PotionAutomationState.FINISHING_POTION_CYCLE;
            return;
        }
        if (!this.actionCooldown.hasElapsed((long)this.brewDelaySetting.getValue())) {
            return;
        }
        DefaultedList<Slot> containerSlots = class_17072.slots;
        for (int i = 0; i < containerSlots.size(); ++i) {
            ItemStack class_17992;
            Slot class_17352 = containerSlots.get(i);
            if (class_17352.inventory == PotionFarmMode.minecraftClient.player.getInventory() || (class_17992 = class_17352.getStack()).isEmpty() || !this.isMatchingPotionIngredient(class_17992, this.requiredIngredientItem)) continue;
            InventoryUtils.quickMoveItem(i);
            if (this.containerPosition != null) {
                this.ingredientContainerByItem.put(this.requiredIngredientItem, this.containerPosition);
            }
            this.requiredIngredientItem = null;
            this.visitedContainerPositions.clear();
            this.actionCooldown.reset();
            this.automationState = PotionAutomationState.FINISHING_POTION_CYCLE;
            return;
        }
        if (this.containerPosition != null) {
            this.recordContainerSearchPosition(this.containerPosition);
            BlockPos adminsky = this.ingredientContainerByItem.get(this.requiredIngredientItem);
            if (this.containerPosition.equals(adminsky) || this.containerPosition.equals(this.getAdjacentBrewingPosition(adminsky))) {
                this.ingredientContainerByItem.remove(this.requiredIngredientItem);
            }
        }
        PotionFarmMode.minecraftClient.player.closeHandledScreen();
        this.containerPosition = null;
        this.resetRotationTarget();
        this.automationState = PotionAutomationState.OPENING_RESTOCK_CONTAINER;
        this.actionCooldown.reset();
    }

    private BlockPos findIngredientContainer() {
        if (PotionFarmMode.minecraftClient.player == null || PotionFarmMode.minecraftClient.world == null || this.requiredIngredientItem == null) {
            return null;
        }
        BlockPos adminsky = this.ingredientContainerByItem.get(this.requiredIngredientItem);
        if (adminsky != null && !this.isVisitedContainerPosition(adminsky) && PotionFarmMode.minecraftClient.world.getBlockEntity(adminsky) instanceof ChestBlockEntity) {
            return adminsky;
        }
        BlockPos adminsky2 = BlockPos.ofFloored((Position)PotionFarmMode.minecraftClient.player.getPos());
        BlockPos adminsky3 = null;
        double d = Double.MAX_VALUE;
        for (BlockPos adminsky4 : BlockPos.iterateOutwards((BlockPos)adminsky2, (int)8, (int)8, (int)8)) {
            double d2;
            BlockPos adminsky5;
            if (!(PotionFarmMode.minecraftClient.world.getBlockEntity(adminsky4) instanceof ChestBlockEntity) || this.isVisitedContainerPosition(adminsky5 = adminsky4.toImmutable()) || !((d2 = adminsky4.getSquaredDistance((Position)PotionFarmMode.minecraftClient.player.getPos())) < d)) continue;
            d = d2;
            adminsky3 = adminsky5;
        }
        return adminsky3;
    }

    private void recordContainerSearchPosition(BlockPos adminsky) {
        this.visitedContainerPositions.add(adminsky);
        BlockPos adminsky2 = this.getAdjacentBrewingPosition(adminsky);
        if (adminsky2 != null) {
            this.visitedContainerPositions.add(adminsky2);
        }
    }

    private boolean isVisitedContainerPosition(BlockPos adminsky) {
        if (this.visitedContainerPositions.contains(adminsky)) {
            return true;
        }
        BlockPos adminsky2 = this.getAdjacentBrewingPosition(adminsky);
        return adminsky2 != null && this.visitedContainerPositions.contains(adminsky2);
    }

    private BlockPos getAdjacentBrewingPosition(BlockPos adminsky) {
        if (adminsky == null || PotionFarmMode.minecraftClient.world == null) {
            return null;
        }
        BlockState class_26802 = PotionFarmMode.minecraftClient.world.getBlockState(adminsky);
        if (!(class_26802.getBlock() instanceof ChestBlock)) {
            return null;
        }
        ChestType class_27452 = (ChestType)class_26802.get((Property)ChestBlock.CHEST_TYPE);
        if (class_27452 == ChestType.SINGLE) {
            return null;
        }
        Direction class_23502 = (Direction)class_26802.get((Property)ChestBlock.FACING);
        Direction class_23503 = class_27452 == ChestType.LEFT ? class_23502.rotateYClockwise() : class_23502.rotateYCounterclockwise();
        return adminsky.offset(class_23503);
    }

    private boolean isMatchingPotionIngredient(ItemStack class_17992, Item class_17922) {
        if (class_17922 == Items.POTION) {
            PotionContentsComponent class_18442 = (PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS);
            return class_17992.getItem() == Items.POTION && class_18442 != null && class_18442.potion().isPresent() && class_18442.potion().get() == Potions.WATER;
        }
        return class_17992.getItem() == class_17922;
    }

    private void finishPotionCycle() {
        if (PotionFarmMode.minecraftClient.currentScreen != null) {
            PotionFarmMode.minecraftClient.player.closeHandledScreen();
        }
        if (!this.actionCooldown.hasElapsed((long)this.brewDelaySetting.getValue())) {
            return;
        }
        if (this.useChestsSetting.isEnabled() && this.isChestTransferEnabled()) {
            this.containerPosition = null;
            this.resetRotationTarget();
            this.automationState = PotionAutomationState.OPENING_DEPOSIT_CONTAINER;
            this.actionCooldown.reset();
            return;
        }
        this.potionStandPosition = null;
        this.containerPosition = null;
        this.resetRotationTarget();
        this.automationState = PotionAutomationState.SELECTING_POTION_TARGET;
        this.actionCooldown.reset();
    }

    private void resetRotationTarget() {
        this.targetRotation = null;
        this.rotationConfirmationCount = 0;
    }

    private boolean isPotionRotationAligned(Vec3d VanillaChestLootTableGenerator) {
        Rotation rotation = AimRotationMath.getRotationToPoint(VanillaChestLootTableGenerator);
        if (this.targetRotation == null || this.targetRotation.angleDistanceTo(rotation) > 0.5f) {
            this.targetRotation = rotation;
            this.rotationConfirmationCount = 0;
        }
        RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, 180.0f, 180.0f, 180.0f, RotationPriority.ITEM_USE_PRIORITY);
        Rotation rotation2 = RockstarClient.create().getRotationManager().getCurrentRotation();
        if (rotation2 != null && rotation2.angleDistanceTo(rotation) <= 1.5f) {
            ++this.rotationConfirmationCount;
            return this.rotationConfirmationCount >= 2;
        }
        return false;
    }

    private boolean isChestTransferEnabled() {
        for (int i = 0; i < PotionFarmMode.minecraftClient.player.getInventory().size(); ++i) {
            ItemStack class_17992 = PotionFarmMode.minecraftClient.player.getInventory().getStack(i);
            if (!this.isPotionItem(class_17992)) continue;
            return true;
        }
        return false;
    }

    private boolean isPotionItem(ItemStack class_17992) {
        if (class_17992.getItem() != Items.POTION) {
            return false;
        }
        PotionContentsComponent class_18442 = (PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS);
        if (class_18442 == null || class_18442.potion().isEmpty()) {
            return false;
        }
        return class_18442.potion().get().value() == this.getPotionContents();
    }

    private Potion getPotionContents() {
        if (this.strengthPotionOption.isSelected()) {
            return Potions.STRONG_STRENGTH.value();
        }
        if (this.speedPotionOption.isSelected()) {
            return Potions.STRONG_SWIFTNESS.value();
        }
        if (this.fireResistancePotionOption.isSelected()) {
            return Potions.LONG_FIRE_RESISTANCE.value();
        }
        if (this.regenerationPotionOption.isSelected()) {
            return Potions.STRONG_REGENERATION.value();
        }
        if (this.healingPotionOption.isSelected()) {
            return Potions.HEALING.value();
        }
        if (this.strongHealingPotionOption.isSelected()) {
            return Potions.STRONG_HEALING.value();
        }
        if (this.invisibilityPotionOption.isSelected() && this.enhancePotionsSetting.isEnabled()) {
            return Potions.LONG_INVISIBILITY.value();
        }
        return Potions.INVISIBILITY.value();
    }

    private Item findMissingPotionIngredient(BrewingStandScreenHandler class_17082) {
        if (this.isBrewingStandReady(class_17082, Potions.WATER.value())) {
            return Items.NETHER_WART;
        }
        if (this.isBrewingStandReady(class_17082, Potions.AWKWARD.value())) {
            if (this.strengthPotionOption.isSelected()) {
                return Items.BLAZE_POWDER;
            }
            if (this.speedPotionOption.isSelected()) {
                return Items.SUGAR;
            }
            if (this.fireResistancePotionOption.isSelected()) {
                return Items.MAGMA_CREAM;
            }
            if (this.invisibilityPotionOption.isSelected()) {
                return Items.GOLDEN_CARROT;
            }
            if (this.regenerationPotionOption.isSelected()) {
                return Items.GHAST_TEAR;
            }
            if (this.healingPotionOption.isSelected() || this.strongHealingPotionOption.isSelected()) {
                return Items.GLISTERING_MELON_SLICE;
            }
        }
        if (this.isBrewingStandReady(class_17082, Potions.NIGHT_VISION.value()) && this.invisibilityPotionOption.isSelected()) {
            return Items.FERMENTED_SPIDER_EYE;
        }
        if (this.isBrewingStandReady(class_17082, Potions.STRENGTH.value()) || this.isBrewingStandReady(class_17082, Potions.SWIFTNESS.value())) {
            return Items.GLOWSTONE_DUST;
        }
        if (this.isBrewingStandReady(class_17082, Potions.FIRE_RESISTANCE.value())) {
            return Items.REDSTONE;
        }
        if (this.invisibilityPotionOption.isSelected() && this.enhancePotionsSetting.isEnabled() && this.isBrewingStandReady(class_17082, Potions.INVISIBILITY.value())) {
            return Items.REDSTONE;
        }
        if (this.strongHealingPotionOption.isSelected() && this.isBrewingStandReady(class_17082, Potions.HEALING.value())) {
            return Items.GLOWSTONE_DUST;
        }
        if (this.regenerationPotionOption.isSelected() && this.isBrewingStandReady(class_17082, Potions.REGENERATION.value())) {
            return Items.GLOWSTONE_DUST;
        }
        return null;
    }

    private boolean isBrewingStandReady(BrewingStandScreenHandler class_17082, Potion class_18422) {
        boolean bl = false;
        for (int i = 0; i < 3; ++i) {
            ItemStack class_17992 = class_17082.getSlot(i).getStack();
            if (class_17992.isEmpty()) continue;
            if (class_17992.getItem() != Items.POTION) {
                return false;
            }
            PotionContentsComponent class_18442 = (PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS);
            if (class_18442 == null || class_18442.potion().isEmpty()) {
                return false;
            }
            if (class_18442.potion().get().value() != class_18422) {
                return false;
            }
            bl = true;
        }
        return bl;
    }

    private boolean isPotionStackCompatible(ItemStack class_17992, Potion class_18422) {
        if (class_17992.getItem() != Items.POTION) {
            return false;
        }
        PotionContentsComponent class_18442 = (PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS);
        if (class_18442 == null || class_18442.potion().isEmpty()) {
            return false;
        }
        return class_18442.potion().get().value() == class_18422;
    }

    private boolean hasPotionItemInSlot(Item class_17922, int n) {
        if (!this.containerCooldown.hasElapsed((long)((double)this.brewDelaySetting.getValue() * 1.2))) {
            return true;
        }
        int n2 = this.findPotionItemInventorySlot(class_17922);
        if (n2 == -1) {
            return false;
        }
        if (this.singleStackOption.isSelected()) {
            this.movePotionItemsToSlots(n2, n);
        } else {
            InventoryUtils.swapWithPutback(n2, n);
        }
        this.containerCooldown.reset();
        return true;
    }

    private void movePotionItemsToSlots(int n, int ... nArray) {
        int n2 = PotionFarmMode.minecraftClient.player.currentScreenHandler.syncId;
        PotionFarmMode.minecraftClient.interactionManager.clickSlot(n2, n, 0, SlotActionType.PICKUP, (PlayerEntity)PotionFarmMode.minecraftClient.player);
        PotionFarmMode.minecraftClient.interactionManager.clickSlot(n2, -999, 4, SlotActionType.QUICK_CRAFT, (PlayerEntity)PotionFarmMode.minecraftClient.player);
        for (int n3 : nArray) {
            PotionFarmMode.minecraftClient.interactionManager.clickSlot(n2, n3, 5, SlotActionType.QUICK_CRAFT, (PlayerEntity)PotionFarmMode.minecraftClient.player);
        }
        PotionFarmMode.minecraftClient.interactionManager.clickSlot(n2, -999, 6, SlotActionType.QUICK_CRAFT, (PlayerEntity)PotionFarmMode.minecraftClient.player);
        PotionFarmMode.minecraftClient.interactionManager.clickSlot(n2, n, 0, SlotActionType.PICKUP, (PlayerEntity)PotionFarmMode.minecraftClient.player);
    }

    private int findPotionItemInventorySlot(Item class_17922) {
        for (int i = 5; i < 41; ++i) {
            if (((Slot)PotionFarmMode.minecraftClient.player.currentScreenHandler.slots.get(i)).getStack().getItem() != class_17922) continue;
            return i;
        }
        return -1;
    }

    private int findBrewingIngredientSlot(BrewingStandScreenHandler class_17082) {
        for (int i = 5; i < 41; ++i) {
            PotionContentsComponent class_18442;
            ItemStack class_17992 = ((Slot)class_17082.slots.get(i)).getStack();
            if (class_17992.getItem() != Items.POTION || (class_18442 = (PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS)) == null || class_18442.potion().isEmpty() || class_18442.potion().get() != Potions.WATER) continue;
            return i;
        }
        return -1;
    }

    private List<BlockPos> scanBrewingStandPositions() {
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        BlockPos adminsky2 = BlockPos.ofFloored((Position)PotionFarmMode.minecraftClient.player.getPos());
        for (BlockPos adminsky3 : BlockPos.iterateOutwards((BlockPos)adminsky2, (int)5, (int)5, (int)5)) {
            if (!(PotionFarmMode.minecraftClient.world.getBlockEntity(adminsky3) instanceof BrewingStandBlockEntity)) continue;
            arrayList.add(adminsky3.toImmutable());
        }
        arrayList.sort(Comparator.comparingDouble(adminsky -> adminsky.getSquaredDistance((Position)PotionFarmMode.minecraftClient.player.getPos())));
        return arrayList;
    }

    private BlockPos findNearestContainerPosition() {
        BlockPos adminsky = BlockPos.ofFloored((Position)PotionFarmMode.minecraftClient.player.getPos());
        BlockPos adminsky2 = null;
        double d = Double.MAX_VALUE;
        for (BlockPos adminsky3 : BlockPos.iterateOutwards((BlockPos)adminsky, (int)8, (int)8, (int)8)) {
            double d2;
            if (!(PotionFarmMode.minecraftClient.world.getBlockEntity(adminsky3) instanceof ChestBlockEntity) || !((d2 = adminsky3.getSquaredDistance((Position)PotionFarmMode.minecraftClient.player.getPos())) < d)) continue;
            d = d2;
            adminsky2 = adminsky3.toImmutable();
        }
        return adminsky2;
    }

    private boolean isBlockWithinInteractionRange(BlockPos adminsky) {
        double d = PotionFarmMode.minecraftClient.player.getBlockInteractionRange();
        return PotionFarmMode.minecraftClient.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky)) <= d * d;
    }

    private void interactWithBlockPosition(BlockPos adminsky) {
        Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)adminsky);
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky, false);
        PotionFarmMode.minecraftClient.interactionManager.interactBlock(PotionFarmMode.minecraftClient.player, Hand.MAIN_HAND, class_39652);
        PotionFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
    }

    private Box getRenderBounds(BlockPos adminsky) {
        return new Box(adminsky).contract(0.02);
    }

    static enum PotionAutomationState {
        SELECTING_POTION_TARGET,
        NAVIGATING_TO_POTION_TARGET,
        BREWING_POTIONS,
        OPENING_DEPOSIT_CONTAINER,
        DEPOSITING_POTIONS,
        OPENING_RESTOCK_CONTAINER,
        RESTOCKING_INGREDIENTS,
        FINISHING_POTION_CYCLE;
}
}

