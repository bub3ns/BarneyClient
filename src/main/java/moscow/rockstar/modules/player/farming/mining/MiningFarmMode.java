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
 *  net.minecraft.PlayerEntity
 *  net.minecraft.PlayerInventory
 *  net.minecraft.ScreenHandler
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.Slot
 *  net.minecraft.ArmorItem
 *  net.minecraft.AxeItem
 *  net.minecraft.Item
 *  net.minecraft.Item$TooltipContext
 *  net.minecraft.HoeItem
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.PickaxeItem
 *  net.minecraft.ShovelItem
 *  net.minecraft.SwordItem
 *  net.minecraft.TooltipType
 *  net.minecraft.BlockView
 *  net.minecraft.World
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.Direction
 *  net.minecraft.Position
 *  net.minecraft.Box
 *  net.minecraft.Vec3i
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.Text
 *  net.minecraft.BlockState
 *  net.minecraft.BufferBuilder
 *  net.minecraft.PlayerInteractItemC2SPacket
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.MathHelper
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  net.minecraft.Camera
 *  net.minecraft.Screen
 *  net.minecraft.MatrixStack
 *  net.minecraft.HandledScreen
 *  net.minecraft.GameMessageS2CPacket
 *  net.minecraft.LoreComponent
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.modules.player.farming.mining;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.market.MarketInventoryAnalyzer;
import moscow.rockstar.math.Rotation;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.player.farming.core.AutoFarm;
import moscow.rockstar.modules.player.farming.core.FarmModeBase;
import moscow.rockstar.modules.player.farming.mining.MineLocation;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.StringSetting;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.util.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.render.Camera;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.DataComponentTypes;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.HudRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

public class MiningFarmMode
extends FarmModeBase {
    private static final BlockPos MINE_REGION_MIN_CORNER = new BlockPos(-75, 84, 29);
    private static final BlockPos MINE_REGION_MAX_CORNER = new BlockPos(-54, 93, 50);
    private static final double MAX_INTERACTION_DISTANCE = 12.0;
    private static final long RESOURCE_WARP_TIMEOUT_MILLIS = 25000L;
    private static final int MAX_MINE_INDEX = 1000;
    private static final BlockState stoneState = Blocks.STONE.getDefaultState();
    private static final int ANARCHY_SERVER_ID = 2;
    private static final String WARP_MINE_COMMAND = "warp mine";
    private static final long API_REFRESH_INTERVAL_MILLIS = 5000L;
    private static final long MINE_SELECTION_THRESHOLD_SECONDS = 10L;
    private static final long MINE_IDLE_TIMEOUT_MILLIS = 60000L;
    private final MultiBooleanSetting raritySetting;
    private final MultiBooleanSetting.Option commonRarityOption;
    private final MultiBooleanSetting.Option legendaryRarityOption;
    private final MultiBooleanSetting.Option mythicalRarityOption;
    private final BooleanSetting digAllSetting;
    private final MultiBooleanSetting oreTypeFilter;
    private final MultiBooleanSetting.Option diamondOreOption;
    private final MultiBooleanSetting.Option lapisOreOption;
    private final MultiBooleanSetting.Option redstoneOreOption;
    private final MultiBooleanSetting.Option ironOreOption;
    private final MultiBooleanSetting.Option goldOreOption;
    private final MultiBooleanSetting.Option debrisOreOption;
    private final BooleanSetting cleanInventorySetting;
    private final BooleanSetting dropExceptSetting;
    private final MultiBooleanSetting keepOnlySetting;
    private final MultiBooleanSetting.Option diamondKeepOption;
    private final MultiBooleanSetting.Option debrisKeepOption;
    private final MultiBooleanSetting.Option ironKeepOption;
    private final MultiBooleanSetting.Option goldKeepOption;
    private final BooleanSetting autoSellSetting;
    private final StringSetting sellServerSetting;
    private final NumberSetting sellThresholdSetting;
    private final BooleanSetting autoRepairSetting;
    private final NumberSetting repairThresholdSetting;
    private final NumberSetting repairTargetPercentageSetting;
    private final NumberSetting repairBottleTargetSetting;
    private BooleanSetting showHudSetting;
    private NumberSetting hudMineCountSetting;
    private static final Set<Item> sellableMineItems = Set.of(Items.DIAMOND, Items.EMERALD, Items.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP, Items.RAW_GOLD, Items.RAW_IRON, Items.RAW_COPPER, Items.GOLD_INGOT, Items.IRON_INGOT, Items.COPPER_INGOT, Items.LAPIS_LAZULI, Items.REDSTONE, Items.COAL, Items.QUARTZ);
    private static final Set<Item> protectedMineItems = Set.of(Items.DIAMOND, Items.EMERALD, Items.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP, Items.NETHERITE_INGOT, Items.RAW_GOLD, Items.RAW_IRON, Items.RAW_COPPER, Items.GOLD_INGOT, Items.IRON_INGOT, Items.COPPER_INGOT, Items.GOLD_NUGGET, Items.IRON_NUGGET, Items.LAPIS_LAZULI, Items.REDSTONE, Items.COAL, Items.QUARTZ, Items.DIAMOND_BLOCK, Items.EMERALD_BLOCK, Items.NETHERITE_BLOCK, Items.GOLD_BLOCK, Items.IRON_BLOCK);
    private static final Pattern RUSSIAN_QUANTITY_PATTERN = Pattern.compile("\u0437\u0430\\s+(\\d+)\\s*\u0448\u0442", 66);
    private MiningOperationState operationState = MiningOperationState.LOCATING_RESOURCE;
    private volatile List<MineLocation> availableMineLocations = new ArrayList<MineLocation>();
    private volatile long mineDataFetchedAt;
    private volatile String mineApiError;
    private volatile long apiWorkerGeneration;
    private Thread apiWorkerThread;
    private final Timer apiRefreshTimer = new Timer();
    private MineLocation selectedMineLocation;
    private int selectedMineServerIndex;
    private boolean resourceWarpActive;
    private int miningWorkflowAttemptCount;
    private final Timer commandCooldownTimer = new Timer();
    private boolean mineWarpActive;
    private long mineWarpRequestedAt;
    private boolean blockMiningActive;
    private boolean mineAreaHasBlocks;
    private long miningDeadlineMillis;
    private final Timer miningTimeoutTimer = new Timer();
    private final Timer blockActionTimer = new Timer();
    private BlockPos currentMiningBlock;
    private BlockPos lastRotationTargetBlock;
    private int miningRotationConfirmationCount;
    private int miningMovementStallTicks;
    private double lastPlayerX;
    private double lastPlayerZ;
    private final Set<BlockPos> visitedMiningBlocks = new HashSet<BlockPos>();
    private final Timer stateScanTimer = new Timer();
    private final Timer inventoryTransferTimer = new Timer();
    private int sellServerIndex;
    private boolean serverTravelActive;
    private int sellServerTravelAttempts;
    private boolean buyerCommandActive;
    private int buyerCommandAttempts = -1;
    private final Timer inventorySlotActionTimer = new Timer();
    private final Timer serverMenuTimer = new Timer();
    private final Timer inventoryWorkflowTimeoutTimer = new Timer();
    private int inventoryItemsRemaining = -1;
    private boolean saleItemSelected;
    private boolean inventoryItemActionPending;
    private boolean preserveConfiguredItems;
    private final Timer skippedItemTimer = new Timer();
    private Item lastProcessedItem;
    private int saleConfirmationCount;
    private int inventoryTransferStallTicks;
    private final Timer itemRetryTimer = new Timer();
    private final Set<Item> skippedInventoryItems = new HashSet<Item>();
    private boolean marketSearchActive;
    private int marketSearchAttempts = -1;
    private int marketPurchaseAttempts;
    private int marketPurchaseCount;
    private boolean inventoryFullNoticeActive;
    private final Timer marketMenuTimer = new Timer();
    private final Timer marketActionTimer = new Timer();
    private final Timer inventoryFullCooldownTimer = new Timer();
    private final Timer repairActionTimer = new Timer();
    private final Timer statusMessageTimer = new Timer();
    private int lastRepairBottleCount = -1;
    private boolean repairCycleActive;
    private final Timer repairProgressTimer = new Timer();
    private final Timer repairSessionTimer = new Timer();
    private static final long MAX_REPAIR_SESSION_MILLIS = 300000L;
    private final Timer spawnCommandTimer = new Timer();
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (MiningFarmMode.minecraftClient.player == null || MiningFarmMode.minecraftClient.world == null) {
            return;
        }
        if (!ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)) {
            this.showError("modules.mine_farm.not_funtime");
            this.disableAutoFarm();
            return;
        }
        this.refreshMineDataWorker();
        if (this.isPlayerAtMineSpawn()) {
            return;
        }
        switch (this.operationState.ordinal()) {
            case 0: {
                this.advanceMiningWorkflow();
                break;
            }
            case 1: {
                this.requestMineServerTeleport();
                break;
            }
            case 2: {
                this.travelToMine();
                break;
            }
            case 3: {
                this.processMiningCycle();
                break;
            }
            case 4: {
                this.travelToSellServer();
                break;
            }
            case 5: {
                this.requestBuyerCommand();
                break;
            }
            case 6: {
                this.processBuyerScreenState();
                break;
            }
            case 7: {
                this.processInventoryItemTransfer();
                break;
            }
            case 8: {
                this.processToolRepair();
                break;
            }
            case 9: {
                this.searchMarketplaceForBottles();
                break;
            }
            case 10: {
                this.selectBottleAuction();
                break;
            }
            case 11: {
                this.confirmBottlePurchase();
                break;
            }
            case 12: {
                this.finishBottlePurchase();
            }
        }
    };
    private final EventListener<ReceivePacketEvent> receivePacketListener = receivePacketEvent -> {
        Object object = receivePacketEvent.getPacket();
        if (!(object instanceof GameMessageS2CPacket)) {
            return;
        }
        GameMessageS2CPacket class_74392 = (GameMessageS2CPacket)object;
        object = class_74392.content().getString().toLowerCase();
        if (((String)object).contains("\u0434\u043e\u0441\u0442\u0430\u0442\u043e\u0447\u043d\u043e \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432")) {
            this.saleItemSelected = false;
            this.inventoryItemActionPending = false;
            return;
        }
        if (!((String)object).contains("\u043f\u0440\u043e\u0434\u0430\u0432\u0430\u0442\u044c")) {
            return;
        }
        if (((String)object).contains("\u0432\u0441\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b") || ((String)object).contains("\u0432\u0441\u0451 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b")) {
            this.saleItemSelected = true;
            this.inventoryItemActionPending = false;
        } else if (((String)object).contains("\u043f\u0440\u043e\u0434\u0430\u0432\u0430\u0442\u044c \u043f\u043e")) {
            this.saleItemSelected = false;
            this.inventoryItemActionPending = false;
        }
    };
    private static final ColorRGBA mineHighlightColor = new ColorRGBA(96.0f, 180.0f, 255.0f);
    private final EventListener<Render3DEvent> render3DListener = render3DEvent -> {
        if (MiningFarmMode.minecraftClient.world == null || MiningFarmMode.minecraftClient.player == null) {
            return;
        }
        if (this.operationState != MiningOperationState.WAITING_FOR_SERVER_DATA && this.operationState != MiningOperationState.MINING_RESOURCE) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Camera class_41842 = MiningFarmMode.minecraftClient.gameRenderer.getCamera();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        Box HorizontalFacingBlock = new Box((double)MINE_REGION_MIN_CORNER.getX(), (double)MINE_REGION_MIN_CORNER.getY(), (double)MINE_REGION_MIN_CORNER.getZ(), (double)(MINE_REGION_MAX_CORNER.getX() + 1), (double)(MINE_REGION_MAX_CORNER.getY() + 1), (double)(MINE_REGION_MAX_CORNER.getZ() + 1)).offset(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        RenderUtils.drawFilledBox(class_45872, class_2872, HorizontalFacingBlock, mineHighlightColor.mulAlpha(0.1f));
        ItemRenderUtils.flushVertexConsumer(class_2872);
        BufferBuilder CreativeInventoryActionC2SPacket = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        RenderUtils.drawBoxOutline(class_45872, CreativeInventoryActionC2SPacket, HorizontalFacingBlock, mineHighlightColor.mulAlpha(0.7f));
        ItemRenderUtils.flushVertexConsumer(CreativeInventoryActionC2SPacket);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    };
    private final EventListener<HudRenderEvent> hudRenderListener = hudRenderEvent -> {
        if (!this.showHudSetting.isEnabled()) {
            return;
        }
        if (MiningFarmMode.minecraftClient.player == null) {
            return;
        }
        CustomDrawContext customDrawContext = hudRenderEvent.getContext();
        FontMetrics fontMetrics = Font.SEMIBOLD.metrics(8.0f);
        FontMetrics fontMetrics2 = Font.MEDIUM.metrics(7.0f);
        ArrayList<MineLocation> arrayList = new ArrayList<MineLocation>(this.availableMineLocations);
        arrayList.sort(Comparator.comparingLong(this::getRemainingMineSeconds));
        int n = Math.min(arrayList.size(), (int)this.hudMineCountSetting.getValue());
        float f = 6.0f;
        float f2 = 44.0f;
        float f3 = 5.0f;
        float f4 = 11.0f;
        float f5 = 188.0f;
        float f6 = 13.0f;
        float f7 = 11.0f;
        float f8 = f3 * 2.0f + f6 + f7 + (float)n * f4;
        customDrawContext.drawRoundedRect(f, f2, f5, f8, WidgetState.uniform(5.0f), new ColorRGBA(14.0f, 14.0f, 16.0f).mulAlpha(0.78f));
        float f9 = f2 + f3;
        customDrawContext.drawText(fontMetrics, "Mine Farm", f + f3, f9, ColorPalette.getPrimaryTextColor());
        String string = this.getMiningStatusText();
        customDrawContext.drawText(fontMetrics2, string, f + f3, f9 += f6, ColorPalette.getAccentColor());
        f9 += f7;
        for (int i = 0; i < n; ++i) {
            MineLocation mineLocation = (MineLocation)arrayList.get(i);
            boolean bl = this.selectedMineLocation != null && MiningFarmMode.parseMineIndex(mineLocation.getServerId()) == this.selectedMineServerIndex;
            ColorRGBA colorRGBA = bl ? ColorPalette.getAccentColor() : ColorPalette.getPrimaryTextColor();
            String string2 = mineLocation.getServerName() + "  " + MiningFarmMode.formatRarityLabel(mineLocation.getMineRarity()) + "\u2192" + MiningFarmMode.formatRarityLabel(mineLocation.getNextMineRarity());
            customDrawContext.drawText(fontMetrics2, string2, f + f3, f9, colorRGBA);
            customDrawContext.drawRightText(fontMetrics2, MiningFarmMode.formatDuration(this.getRemainingMineSeconds(mineLocation)), f + f5 - f3, f9, colorRGBA);
            f9 += f4;
        }
    };

    public MiningFarmMode(AutoFarm autoFarm, ModeSetting modeSetting) {
        super(autoFarm, modeSetting, "modules.settings.auto_farm.modes.mine");
        this.raritySetting = new MultiBooleanSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.rarities", () -> !this.isSelected());
        this.commonRarityOption = new MultiBooleanSetting.Option(this.raritySetting, "modules.settings.mine_farm.rarities.default").select();
        this.legendaryRarityOption = new MultiBooleanSetting.Option(this.raritySetting, "modules.settings.mine_farm.rarities.legendary").select();
        this.mythicalRarityOption = new MultiBooleanSetting.Option(this.raritySetting, "modules.settings.mine_farm.rarities.mythical").select();
        this.digAllSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.dig_all", () -> !this.isSelected());
        this.oreTypeFilter = new MultiBooleanSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.ore_types", () -> !this.isSelected() || this.digAllSetting.isEnabled());
        this.diamondOreOption = new MultiBooleanSetting.Option(this.oreTypeFilter, "modules.settings.mine_farm.ore.diamond").select();
        this.lapisOreOption = new MultiBooleanSetting.Option(this.oreTypeFilter, "modules.settings.mine_farm.ore.lapis");
        this.redstoneOreOption = new MultiBooleanSetting.Option(this.oreTypeFilter, "modules.settings.mine_farm.ore.redstone");
        this.ironOreOption = new MultiBooleanSetting.Option(this.oreTypeFilter, "modules.settings.mine_farm.ore.iron");
        this.goldOreOption = new MultiBooleanSetting.Option(this.oreTypeFilter, "modules.settings.mine_farm.ore.gold").select();
        this.debrisOreOption = new MultiBooleanSetting.Option(this.oreTypeFilter, "modules.settings.mine_farm.ore.debris").select();
        this.cleanInventorySetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.clean_inventory", () -> !this.isSelected()).enable();
        this.dropExceptSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.drop_except", () -> !this.isSelected());
        this.keepOnlySetting = new MultiBooleanSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.keep_only", () -> !this.isSelected() || !this.dropExceptSetting.isEnabled());
        this.diamondKeepOption = new MultiBooleanSetting.Option(this.keepOnlySetting, "modules.settings.mine_farm.keep.diamond").select();
        this.debrisKeepOption = new MultiBooleanSetting.Option(this.keepOnlySetting, "modules.settings.mine_farm.keep.debris").select();
        this.ironKeepOption = new MultiBooleanSetting.Option(this.keepOnlySetting, "modules.settings.mine_farm.keep.iron").select();
        this.goldKeepOption = new MultiBooleanSetting.Option(this.keepOnlySetting, "modules.settings.mine_farm.keep.gold").select();
        this.autoSellSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.auto_sell", () -> !this.isSelected());
        this.sellServerSetting = new StringSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.sell_anarchy", () -> !this.isSelected() || !this.autoSellSetting.isEnabled()).setNumericOnly(true).setValue("");
        this.sellThresholdSetting = new NumberSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.sell_threshold", () -> !this.isSelected() || !this.autoSellSetting.isEnabled()).setStep(1.0f).setMinValue(1.0f).setMaxValue(36.0f).setValue(10.0f).setUnit(" st");
        this.autoRepairSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.auto_repair", () -> !this.isSelected()).enable();
        this.repairThresholdSetting = new NumberSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.repair_threshold", () -> !this.isSelected() || !this.autoRepairSetting.isEnabled()).setStep(1.0f).setMinValue(1.0f).setMaxValue(50.0f).setValue(10.0f).setUnit("%");
        this.repairTargetPercentageSetting = new NumberSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.repair_until", () -> !this.isSelected() || !this.autoRepairSetting.isEnabled()).setStep(1.0f).setMinValue(20.0f).setMaxValue(100.0f).setValue(90.0f).setUnit("%");
        this.repairBottleTargetSetting = new NumberSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.bottle_target", () -> !this.isSelected() || !this.autoRepairSetting.isEnabled()).setStep(1.0f).setMinValue(1.0f).setMaxValue(128.0f).setValue(64.0f).setUnit(" pcs");
        this.showHudSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.show_hud", () -> !this.isSelected()).enable();
        this.hudMineCountSetting = new NumberSetting((SettingOwner)autoFarm, "modules.settings.mine_farm.hud_count", () -> !this.isSelected() || !this.showHudSetting.isEnabled()).setStep(1.0f).setMinValue(3.0f).setMaxValue(15.0f).setValue(8.0f);
    }

    @Override
    public void startFarmAutomation() {
        if (MiningFarmMode.minecraftClient.player == null || MiningFarmMode.minecraftClient.world == null) {
            this.disableAutoFarm();
            return;
        }
        if (!ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)) {
            this.showError("modules.mine_farm.not_funtime");
            this.disableAutoFarm();
            return;
        }
        this.resetMiningWorkflow();
        this.startMineDataWorker();
        this.operationState = MiningOperationState.LOCATING_RESOURCE;
    }

    @Override
    public void resetBrewingState() {
        this.stopMineDataWorker();
        this.releaseMovementKeys();
        this.resetMiningWorkflow();
        this.operationState = MiningOperationState.LOCATING_RESOURCE;
    }

    private void resetMiningWorkflow() {
        this.selectedMineLocation = null;
        this.selectedMineServerIndex = -1;
        this.resourceWarpActive = false;
        this.miningWorkflowAttemptCount = 0;
        this.mineWarpActive = false;
        this.blockMiningActive = false;
        this.mineAreaHasBlocks = false;
        this.sellServerIndex = -1;
        this.marketSearchActive = false;
        this.marketPurchaseCount = 0;
        this.inventoryFullNoticeActive = false;
        this.repairCycleActive = false;
        this.lastRepairBottleCount = -1;
    }

    private void startMineDataWorker() {
        this.stopMineDataWorker();
        this.mineApiError = "remote mine data disabled";
    }

    private void stopMineDataWorker() {
        ++this.apiWorkerGeneration;
        if (this.apiWorkerThread != null) {
            this.apiWorkerThread.interrupt();
            this.apiWorkerThread = null;
        }
    }

    private void refreshMineDataWorker() {
        // Mine data is intentionally local-only; do not contact a third-party API.
    }

    private long getRemainingMineSeconds(MineLocation mineLocation) {
        long l = (System.currentTimeMillis() - this.mineDataFetchedAt) / 1000L;
        return Math.max(0L, mineLocation.getResetSecondsLeft() - l);
    }

    private boolean matchesMineRarity(String string) {
        if (string == null) {
            return true;
        }
        return switch (string.toLowerCase()) {
            case "default" -> this.commonRarityOption.isSelected();
            case "legendary" -> this.legendaryRarityOption.isSelected();
            case "mythical" -> this.mythicalRarityOption.isSelected();
            default -> true;
        };
    }

    private static int parseMineIndex(String string) {
        if (string == null) {
            return -1;
        }
        String string2 = string.replaceAll("[^0-9]", "");
        if (string2.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(string2);
        }
        catch (NumberFormatException numberFormatException) {
            return -1;
        }
    }

    private MineLocation selectAvailableMine() {
        ArrayList<MineLocation> arrayList = new ArrayList<MineLocation>(this.availableMineLocations);
        arrayList.sort(Comparator.comparingLong(this::getRemainingMineSeconds));
        int n = ServerDetector.defaultServerIndex;
        long l = 10L;
        for (MineLocation mineLocation : arrayList) {
            int n2;
            if (!this.matchesMineRarity(mineLocation.getNextMineRarity()) || (n2 = MiningFarmMode.parseMineIndex(mineLocation.getServerId())) <= 0 || n2 > 1000 || n2 != n && this.getRemainingMineSeconds(mineLocation) < l) continue;
            return mineLocation;
        }
        return null;
    }

    private void advanceMiningWorkflow() {
        this.processInventoryCleanup();
        if (!this.stateScanTimer.hasElapsed(500L)) {
            return;
        }
        this.stateScanTimer.reset();
        if (this.isRepairNeeded() && this.isMiningResourcesReady()) {
            this.beginSellingWorkflow();
            return;
        }
        if (this.isInventoryReadyForSelling()) {
            return;
        }
        MineLocation mineLocation = this.selectAvailableMine();
        if (mineLocation == null) {
            return;
        }
        this.selectedMineLocation = mineLocation;
        this.selectedMineServerIndex = MiningFarmMode.parseMineIndex(mineLocation.getServerId());
        if (this.selectedMineServerIndex <= 0) {
            this.selectedMineLocation = null;
            return;
        }
        if (ServerDetector.defaultServerIndex == this.selectedMineServerIndex) {
            this.awaitMineServerResponse();
        } else {
            this.resourceWarpActive = false;
            this.miningWorkflowAttemptCount = 0;
            this.commandCooldownTimer.reset();
            this.operationState = MiningOperationState.TRAVELING_TO_RESOURCE;
        }
    }

    private void requestMineServerTeleport() {
        if (this.selectedMineLocation == null) {
            this.operationState = MiningOperationState.LOCATING_RESOURCE;
            return;
        }
        if (ServerDetector.defaultServerIndex == this.selectedMineServerIndex) {
            this.awaitMineServerResponse();
            return;
        }
        if (!this.resourceWarpActive || this.commandCooldownTimer.hasElapsed(25000L)) {
            MiningFarmMode.minecraftClient.player.networkHandler.sendChatCommand("an" + this.selectedMineServerIndex);
            this.resourceWarpActive = true;
            this.commandCooldownTimer.reset();
            ++this.miningWorkflowAttemptCount;
            if (this.miningWorkflowAttemptCount > 6) {
                this.selectedMineLocation = null;
                this.operationState = MiningOperationState.LOCATING_RESOURCE;
                this.stateScanTimer.reset();
            }
        }
    }

    private void awaitMineServerResponse() {
        this.mineWarpActive = false;
        this.mineWarpRequestedAt = System.currentTimeMillis();
        this.miningWorkflowAttemptCount = 0;
        this.operationState = MiningOperationState.WAITING_FOR_SERVER_DATA;
    }

    private void travelToMine() {
        if (this.selectedMineLocation == null) {
            this.operationState = MiningOperationState.LOCATING_RESOURCE;
            return;
        }
        if (ServerDetector.defaultServerIndex != this.selectedMineServerIndex) {
            this.resourceWarpActive = false;
            this.commandCooldownTimer.reset();
            this.operationState = MiningOperationState.TRAVELING_TO_RESOURCE;
            return;
        }
        if (this.getMineRegionDistance() <= 12.0) {
            this.beginResourceMining();
            return;
        }
        if (!this.mineWarpActive || System.currentTimeMillis() - this.mineWarpRequestedAt > 8000L) {
            MiningFarmMode.minecraftClient.player.networkHandler.sendChatCommand(WARP_MINE_COMMAND);
            this.mineWarpActive = true;
            this.mineWarpRequestedAt = System.currentTimeMillis();
            ++this.miningWorkflowAttemptCount;
            if (this.miningWorkflowAttemptCount > 8) {
                this.selectedMineLocation = null;
                this.operationState = MiningOperationState.LOCATING_RESOURCE;
                this.stateScanTimer.reset();
            }
        }
    }

    private void beginResourceMining() {
        this.blockMiningActive = false;
        this.mineAreaHasBlocks = this.hasRemainingMineableBlocks();
        this.blockActionTimer.reset();
        this.recordPlayerMiningPosition();
        this.visitedMiningBlocks.clear();
        this.currentMiningBlock = null;
        long l = this.selectedMineLocation != null ? this.getRemainingMineSeconds(this.selectedMineLocation) : 0L;
        this.miningDeadlineMillis = Math.max(30000L, Math.min(300000L, (l + 45L) * 1000L));
        this.miningTimeoutTimer.reset();
        this.operationState = MiningOperationState.MINING_RESOURCE;
    }

    private void processMiningCycle() {
        Iterator<BlockPos> blockIterator;
        Direction miningFace;
        List<BlockPos> list;
        int n;
        this.releaseMovementKeys();
        if (ServerDetector.defaultServerIndex != this.selectedMineServerIndex) {
            this.resourceWarpActive = false;
            this.commandCooldownTimer.reset();
            this.operationState = MiningOperationState.TRAVELING_TO_RESOURCE;
            return;
        }
        if (this.isRepairNeeded() && this.isMiningResourcesReady()) {
            this.beginSellingWorkflow();
            return;
        }
        if (this.isInventoryReadyForSelling()) {
            this.releaseMovementKeys();
            return;
        }
        if (!this.mineAreaHasBlocks && this.hasRemainingMineableBlocks()) {
            this.mineAreaHasBlocks = true;
        }
        if ((n = (list = this.findMineableBlocks()).size()) == 0) {
            if (this.mineAreaHasBlocks) {
                this.resetCurrentMine();
                return;
            }
            this.processInventoryCleanup();
            if (this.miningTimeoutTimer.hasElapsed(this.miningDeadlineMillis)) {
                this.selectedMineLocation = null;
                this.operationState = MiningOperationState.LOCATING_RESOURCE;
                this.stateScanTimer.reset();
            }
            return;
        }
        if (this.blockActionTimer.hasElapsed(25000L)) {
            this.resetCurrentMine();
            return;
        }
        this.equipBestMiningTool();
        double d = MiningFarmMode.minecraftClient.player.getBlockInteractionRange();
        double d2 = (d + 0.6) * (d + 0.6);
        int n2 = MiningFarmMode.minecraftClient.player.getBlockY();
        BlockPos adminsky = null;
        if (this.currentMiningBlock != null && !this.visitedMiningBlocks.contains(this.currentMiningBlock) && this.isMineableTargetBlock(this.currentMiningBlock)) {
            adminsky = this.currentMiningBlock;
        } else {
            blockIterator = list.iterator();
            while (blockIterator.hasNext()) {
                BlockPos adminsky2 = blockIterator.next();
                if (this.visitedMiningBlocks.contains(adminsky2)) continue;
                adminsky = adminsky2;
                break;
            }
            this.currentMiningBlock = adminsky;
        }
        if (adminsky == null) {
            this.resetCurrentMine();
            return;
        }
        miningFace = this.getBlockDistanceSquared(adminsky) <= d2 ? this.findMiningFace(adminsky, d) : null;
        if (miningFace != null) {
            this.recordPlayerMiningPosition();
            if (this.isRotationAligned(adminsky, this.getBlockFaceCenter(adminsky, miningFace))) {
                MiningFarmMode.minecraftClient.interactionManager.updateBlockBreakingProgress(adminsky, miningFace);
                MiningFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
                this.blockActionTimer.reset();
            }
            this.blockMiningActive = true;
            this.processInventoryCleanup();
            return;
        }
        int n3 = adminsky.getY() - n2;
        if (n3 > 1) {
            if ((double)n3 > d) {
                this.visitedMiningBlocks.add(adminsky);
                this.currentMiningBlock = null;
                this.processInventoryCleanup();
                return;
            }
            if (Math.sqrt(this.getBlockDistanceSquared(adminsky)) <= d + 2.5) {
                if (!this.isBlockVisibleWithinReach(adminsky, d)) {
                    this.visitedMiningBlocks.add(adminsky);
                    this.currentMiningBlock = null;
                }
                this.processInventoryCleanup();
                return;
            }
        }
        this.attemptBlockMining(adminsky, d);
        this.processInventoryCleanup();
    }

    private boolean isBlockVisibleWithinReach(BlockPos adminsky, double d) {
        Vec3d VanillaChestLootTableGenerator;
        Vec3d WallPlayerSkullBlock;
        double d2;
        Vec3d VanillaEntityLootTableGenerator;
        Vec3d PlayerSkullBlock = MiningFarmMode.minecraftClient.player.getEyePos();
        BlockHitResult class_39652 = MiningFarmMode.minecraftClient.world.raycast(new RaycastContext(PlayerSkullBlock, VanillaEntityLootTableGenerator = (d2 = (WallPlayerSkullBlock = (VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)adminsky)).subtract(PlayerSkullBlock)).length()) <= d ? VanillaChestLootTableGenerator : PlayerSkullBlock.add(WallPlayerSkullBlock.multiply(d / Math.max(d2, 1.0E-4))), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)MiningFarmMode.minecraftClient.player));
        if (class_39652.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos adminsky2 = class_39652.getBlockPos();
        if (!this.isMineableBlock(adminsky2)) {
            return false;
        }
        if (this.getBlockDistanceSquared(adminsky2) > (d + 0.6) * (d + 0.6)) {
            return false;
        }
        if (this.isRotationAligned(adminsky2, class_39652.getPos())) {
            MiningFarmMode.minecraftClient.interactionManager.updateBlockBreakingProgress(adminsky2, class_39652.getSide());
            MiningFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
            this.blockActionTimer.reset();
        }
        this.blockMiningActive = true;
        return true;
    }

    private void attemptBlockMining(BlockPos adminsky, double d) {
        double d2;
        double d3;
        Vec3d VanillaChestLootTableGenerator;
        BlockPos adminsky2;
        Vec3d WallPlayerSkullBlock = MiningFarmMode.minecraftClient.player.getEyePos();
        int n = MiningFarmMode.minecraftClient.player.getBlockY();
        double d4 = (double)adminsky.getX() + 0.5 - MiningFarmMode.minecraftClient.player.getX();
        double d5 = (double)adminsky.getZ() + 0.5 - MiningFarmMode.minecraftClient.player.getZ();
        double d6 = Math.sqrt(d4 * d4 + d5 * d5);
        float f = (float)(Math.toDegrees(Math.atan2(d5, d4)) - 90.0);
        double d7 = (d + 0.6) * (d + 0.6);
        BlockPos adminsky3 = null;
        Direction class_23502 = null;
        Vec3d VanillaEntityLootTableGenerator = null;
        if (d6 > 1.0E-4) {
            VanillaChestLootTableGenerator = new Vec3d(d4 / d6, 0.0, d5 / d6);
            BlockHitResult raycastHit = MiningFarmMode.minecraftClient.world.raycast(new RaycastContext(
                WallPlayerSkullBlock,
                WallPlayerSkullBlock.add(VanillaChestLootTableGenerator.multiply(d)),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                MiningFarmMode.minecraftClient.player));
            if (raycastHit.getType() == HitResult.Type.BLOCK
                && this.isMineableBlock(raycastHit.getBlockPos())
                && WallPlayerSkullBlock.squaredDistanceTo(Vec3d.ofCenter(raycastHit.getBlockPos())) <= d7) {
                adminsky3 = raycastHit.getBlockPos();
                class_23502 = raycastHit.getSide();
                VanillaEntityLootTableGenerator = raycastHit.getPos();
            }
        }
        boolean bl = false;
        if (adminsky3 == null && adminsky.getY() < n - 1 && d6 < 1.4 && this.isMineableBlock(adminsky2 = MiningFarmMode.minecraftClient.player.getBlockPos().down())) {
            adminsky3 = adminsky2;
            class_23502 = Direction.UP;
            VanillaEntityLootTableGenerator = Vec3d.ofCenter((Vec3i)adminsky2);
            bl = true;
        }
        if (VanillaEntityLootTableGenerator == null && d6 > 0.5) {
            MiningFarmMode.minecraftClient.player.setYaw(f);
            MiningFarmMode.minecraftClient.player.setPitch(0.0f);
            MiningFarmMode.minecraftClient.player.setHeadYaw(f);
            MiningFarmMode.minecraftClient.player.setBodyYaw(f);
        }
        if (!bl && d6 > 0.5) {
            MiningFarmMode.minecraftClient.options.forwardKey.setPressed(true);
            MiningFarmMode.minecraftClient.options.sprintKey.setPressed(true);
        }
        if (adminsky3 != null && this.isRotationAligned(adminsky3, VanillaEntityLootTableGenerator)) {
            MiningFarmMode.minecraftClient.interactionManager.updateBlockBreakingProgress(adminsky3, class_23502);
            MiningFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
            this.blockActionTimer.reset();
        }
        if (((d3 = MiningFarmMode.minecraftClient.player.getX()) - this.lastPlayerX) * (d3 - this.lastPlayerX) + ((d2 = MiningFarmMode.minecraftClient.player.getZ()) - this.lastPlayerZ) * (d2 - this.lastPlayerZ) > 0.0225) {
            this.lastPlayerX = d3;
            this.lastPlayerZ = d2;
            this.miningMovementStallTicks = 0;
        } else {
            ++this.miningMovementStallTicks;
        }
        if (this.miningMovementStallTicks >= 8 && adminsky3 == null && MiningFarmMode.minecraftClient.player.isOnGround()) {
            MiningFarmMode.minecraftClient.options.jumpKey.setPressed(true);
            this.miningMovementStallTicks = 0;
        }
        this.blockMiningActive = true;
    }

    private boolean isPlayerAtMineSpawn() {
        boolean bl;
        if (MiningFarmMode.minecraftClient.player == null || MiningFarmMode.minecraftClient.world == null) {
            return false;
        }
        BlockPos adminsky = BlockPos.ofFloored((Position)MiningFarmMode.minecraftClient.player.getEyePos());
        BlockState class_26802 = MiningFarmMode.minecraftClient.world.getBlockState(adminsky);
        if (!class_26802.shouldSuffocate((BlockView)MiningFarmMode.minecraftClient.world, adminsky)) {
            this.spawnCommandTimer.reset();
            return false;
        }
        this.releaseMovementKeys();
        this.equipBestMiningTool();
        Block class_22482 = class_26802.getBlock();
        boolean bl2 = bl = class_22482 != Blocks.BEDROCK && class_22482 != Blocks.BARRIER && class_26802.getFluidState().isEmpty();
        if (bl) {
            this.rotateToward(Vec3d.ofCenter((Vec3i)adminsky));
            MiningFarmMode.minecraftClient.interactionManager.updateBlockBreakingProgress(adminsky, Direction.UP);
            MiningFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        }
        if (this.spawnCommandTimer.hasElapsed(1500L)) {
            MiningFarmMode.minecraftClient.player.networkHandler.sendChatCommand("spawn");
            this.spawnCommandTimer.reset();
        }
        return true;
    }

    private boolean isMineableBlock(BlockPos adminsky) {
        BlockState class_26802 = MiningFarmMode.minecraftClient.world.getBlockState(adminsky);
        if (class_26802.isAir()) {
            return false;
        }
        Block class_22482 = class_26802.getBlock();
        if (class_22482 == Blocks.BEDROCK || class_22482 == Blocks.BARRIER) {
            return false;
        }
        return class_26802.getFluidState().isEmpty();
    }

    private void recordPlayerMiningPosition() {
        this.miningMovementStallTicks = 0;
        if (MiningFarmMode.minecraftClient.player != null) {
            this.lastPlayerX = MiningFarmMode.minecraftClient.player.getX();
            this.lastPlayerZ = MiningFarmMode.minecraftClient.player.getZ();
        }
    }

    private void resetCurrentMine() {
        this.releaseMovementKeys();
        this.selectedMineLocation = null;
        this.selectedMineServerIndex = -1;
        this.blockMiningActive = false;
        this.mineAreaHasBlocks = false;
        this.operationState = MiningOperationState.LOCATING_RESOURCE;
        this.stateScanTimer.reset();
    }

    private boolean isInventoryReadyForSelling() {
        boolean bl;
        if (!this.autoSellSetting.isEnabled()) {
            return false;
        }
        boolean bl2 = bl = this.countInventoryItem(Items.DIAMOND) >= (int)this.sellThresholdSetting.getValue() * 64;
        if (bl) {
            return this.beginInventoryProcessing(false);
        }
        if (this.countEmptyInventorySlots() <= 1 && this.hasSellableMineItems()) {
            return this.beginInventoryProcessing(true);
        }
        return false;
    }

    private boolean hasSellableMineItems() {
        PlayerInventory class_16612 = MiningFarmMode.minecraftClient.player.getInventory();
        for (int i = 0; i < class_16612.size(); ++i) {
            Item class_17922 = class_16612.getStack(i).getItem();
            if (class_17922 == Items.DIAMOND || !sellableMineItems.contains(class_17922)) continue;
            return true;
        }
        return false;
    }

    private boolean beginInventoryProcessing(boolean bl) {
        int n = MiningFarmMode.parseMineIndex(this.sellServerSetting.getValue());
        if (n <= 0 || n > 1000) {
            return false;
        }
        this.sellServerIndex = n;
        this.preserveConfiguredItems = bl;
        this.serverTravelActive = false;
        this.sellServerTravelAttempts = 0;
        this.buyerCommandActive = false;
        this.buyerCommandAttempts = 0;
        this.commandCooldownTimer.reset();
        this.serverMenuTimer.reset();
        this.inventorySlotActionTimer.reset();
        this.inventoryWorkflowTimeoutTimer.reset();
        this.inventoryItemsRemaining = -1;
        this.saleItemSelected = false;
        this.inventoryItemActionPending = false;
        this.skippedItemTimer.reset();
        this.lastProcessedItem = null;
        this.inventoryTransferStallTicks = 0;
        this.itemRetryTimer.reset();
        this.skippedInventoryItems.clear();
        this.operationState = MiningOperationState.PROCESSING_INVENTORY;
        return true;
    }

    private void travelToSellServer() {
        if (this.sellServerIndex <= 0) {
            this.resetInventoryWorkflow();
            return;
        }
        if (ServerDetector.defaultServerIndex == this.sellServerIndex) {
            this.buyerCommandActive = false;
            this.buyerCommandAttempts = 0;
            this.serverMenuTimer.reset();
            this.operationState = MiningOperationState.HARVESTING_RESOURCE;
            return;
        }
        if (!this.serverTravelActive || this.commandCooldownTimer.hasElapsed(25000L)) {
            MiningFarmMode.minecraftClient.player.networkHandler.sendChatCommand("an" + this.sellServerIndex);
            this.serverTravelActive = true;
            this.commandCooldownTimer.reset();
            ++this.sellServerTravelAttempts;
            if (this.sellServerTravelAttempts > 6) {
                this.resetInventoryWorkflow();
            }
        }
    }

    private void requestBuyerCommand() {
        if (ServerDetector.defaultServerIndex != this.sellServerIndex) {
            this.serverTravelActive = false;
            this.operationState = MiningOperationState.PROCESSING_INVENTORY;
            return;
        }
        if (this.isBuyerScreenOpen() || this.isAuctionSectionOpen()) {
            this.serverMenuTimer.reset();
            this.operationState = MiningOperationState.WAITING_FOR_MINING_COOLDOWN;
            return;
        }
        if (!this.buyerCommandActive || this.serverMenuTimer.hasElapsed(4000L)) {
            String string = "buyer";
            if (string == null || string.isBlank()) {
                string = "buyer";
            }
            if (string.startsWith("/")) {
                string = string.substring(1);
            }
            MiningFarmMode.minecraftClient.player.networkHandler.sendChatCommand(string);
            this.buyerCommandActive = true;
            this.serverMenuTimer.reset();
            ++this.buyerCommandAttempts;
            if (this.buyerCommandAttempts > 6) {
                this.resetInventoryWorkflow();
            }
        }
    }

    private void processBuyerScreenState() {
        if (this.isBuyerScreenOpen()) {
            this.operationState = MiningOperationState.PROCESSING_MINE_ACTION;
            this.serverMenuTimer.reset();
            this.inventorySlotActionTimer.reset();
            this.inventoryWorkflowTimeoutTimer.reset();
            this.inventoryItemsRemaining = -1;
            return;
        }
        if (this.isAuctionSectionOpen()) {
            Slot class_17352;
            if (this.inventorySlotActionTimer.hasElapsed(350L) && (class_17352 = this.findContainerSlotForItem(Items.LAPIS_LAZULI)) != null) {
                this.clickInventorySlot(class_17352.id, 0);
                this.inventorySlotActionTimer.reset();
            }
            if (this.serverMenuTimer.hasElapsed(8000L)) {
                this.resetInventoryWorkflow();
            }
            return;
        }
        if (this.serverMenuTimer.hasElapsed(2500L)) {
            this.buyerCommandActive = false;
            this.operationState = MiningOperationState.HARVESTING_RESOURCE;
        }
    }

    private void processInventoryItemTransfer() {
        if (!this.isBuyerScreenOpen()) {
            if (this.serverMenuTimer.hasElapsed(2000L)) {
                this.buyerCommandActive = false;
                this.operationState = MiningOperationState.HARVESTING_RESOURCE;
            }
            return;
        }
        int n = this.countSellableInventoryItems();
        if (n == 0) {
            this.resetInventoryWorkflow();
            return;
        }
        if (this.inventoryItemsRemaining < 0 || n < this.inventoryItemsRemaining) {
            this.inventoryItemsRemaining = n;
            this.inventoryWorkflowTimeoutTimer.reset();
        }
        if (this.inventoryWorkflowTimeoutTimer.hasElapsed(15000L)) {
            this.resetInventoryWorkflow();
            return;
        }
        if (!this.inventorySlotActionTimer.hasElapsed(250L)) {
            return;
        }
        Slot class_17352 = this.findFirstSellableSlot();
        if (class_17352 == null) {
            this.resetInventoryWorkflow();
            return;
        }
        if (!this.saleItemSelected) {
            if (this.isSaleButtonItem(class_17352.getStack())) {
                this.saleItemSelected = true;
            } else {
                if (this.inventoryItemActionPending && !this.skippedItemTimer.hasElapsed(2000L)) {
                    return;
                }
                this.clickInventorySlot(class_17352.id, 1);
                this.inventoryItemActionPending = true;
                this.skippedItemTimer.reset();
                this.inventorySlotActionTimer.reset();
                return;
            }
        }
        Item class_17922 = class_17352.getStack().getItem();
        int n2 = this.countInventoryItem(class_17922);
        if (class_17922 == this.lastProcessedItem) {
            if (n2 < this.saleConfirmationCount) {
                this.lastProcessedItem = null;
                this.inventoryTransferStallTicks = 0;
            } else {
                if (!this.itemRetryTimer.hasElapsed(1200L)) {
                    return;
                }
                ++this.inventoryTransferStallTicks;
                this.itemRetryTimer.reset();
                if (this.inventoryTransferStallTicks >= 3) {
                    this.skippedInventoryItems.add(class_17922);
                    this.lastProcessedItem = null;
                    this.inventoryTransferStallTicks = 0;
                }
                return;
            }
        }
        this.clickInventorySlot(class_17352.id, 0);
        this.lastProcessedItem = class_17922;
        this.saleConfirmationCount = n2;
        this.itemRetryTimer.reset();
        this.inventorySlotActionTimer.reset();
    }

    private void resetInventoryWorkflow() {
        this.closeInventoryScreen();
        this.sellServerIndex = -1;
        this.operationState = MiningOperationState.LOCATING_RESOURCE;
        this.stateScanTimer.reset();
    }

    private void closeInventoryScreen() {
        if (MiningFarmMode.minecraftClient.player != null && MiningFarmMode.minecraftClient.player.currentScreenHandler != MiningFarmMode.minecraftClient.player.playerScreenHandler) {
            MiningFarmMode.minecraftClient.player.closeHandledScreen();
        }
    }

    private String getOpenScreenTitle() {
        String string;
        Screen class_4372 = MiningFarmMode.minecraftClient.currentScreen;
        if (class_4372 instanceof HandledScreen) {
            HandledScreen BlockStateProviderType = (HandledScreen)class_4372;
            string = BlockStateProviderType.getTitle().getString().toLowerCase();
        } else {
            string = "";
        }
        return string;
    }

    private boolean isBuyerScreenOpen() {
        return this.getOpenScreenTitle().contains("\u043a\u0443\u043f\u0449\u0438\u043a");
    }

    private boolean isAuctionSectionOpen() {
        return this.getOpenScreenTitle().contains("\u0441\u0435\u043a\u0446\u0438");
    }

    private Slot findContainerSlotForItem(Item class_17922) {
        ScreenHandler class_17032 = MiningFarmMode.minecraftClient.player.currentScreenHandler;
        if (!(class_17032 instanceof GenericContainerScreenHandler)) {
            return null;
        }
        GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)class_17032;
        for (Slot class_17352 : class_17072.slots) {
            if (class_17352.inventory == MiningFarmMode.minecraftClient.player.getInventory() || class_17352.getStack().getItem() != class_17922) continue;
            return class_17352;
        }
        return null;
    }

    private Slot findFirstSellableSlot() {
        ScreenHandler class_17032 = MiningFarmMode.minecraftClient.player.currentScreenHandler;
        if (!(class_17032 instanceof GenericContainerScreenHandler)) {
            return null;
        }
        GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)class_17032;
        for (Slot class_17352 : class_17072.slots) {
            if (class_17352.inventory == MiningFarmMode.minecraftClient.player.getInventory() || !class_17352.hasStack()) continue;
            ItemStack class_17992 = class_17352.getStack();
            if (this.preserveConfiguredItems && class_17992.getItem() == Items.DIAMOND || this.skippedInventoryItems.contains(class_17992.getItem()) || this.countInventoryItem(class_17992.getItem()) <= 0 || !this.isSellableItemStack(class_17992)) continue;
            return class_17352;
        }
        return null;
    }

    private boolean isSellableItemStack(ItemStack class_17992) {
        LoreComponent class_92902 = (LoreComponent)class_17992.get(DataComponentTypes.LORE);
        if (class_92902 == null) {
            return false;
        }
        for (Text class_25612 : class_92902.lines()) {
            String string = class_25612.getString().toLowerCase();
            if (!string.contains("\u043f\u0440\u043e\u0434\u0430\u0442\u044c") && !string.contains("\u0446\u0435\u043d\u0430 \u0437\u0430")) continue;
            return true;
        }
        return false;
    }

    private int countSellableInventoryItems() {
        ScreenHandler currentScreenHandler = MiningFarmMode.minecraftClient.player.currentScreenHandler;
        if (!(currentScreenHandler instanceof GenericContainerScreenHandler)) {
            return 0;
        }
        GenericContainerScreenHandler containerHandler = (GenericContainerScreenHandler)currentScreenHandler;
        Set<Item> sellableInventoryItems = new HashSet<>();
        for (Slot slot : containerHandler.slots) {
            if (slot.inventory == MiningFarmMode.minecraftClient.player.getInventory() || !slot.hasStack()) continue;
            ItemStack stack = slot.getStack();
            Item item = stack.getItem();
            if (this.preserveConfiguredItems && item == Items.DIAMOND || this.skippedInventoryItems.contains(item) || !this.isSellableItemStack(stack)) continue;
            sellableInventoryItems.add(item);
        }
        int n = 0;
        for (Item item : sellableInventoryItems) {
            n += this.countInventoryItem(item);
        }
        return n;
    }

    private void clickInventorySlot(int n, int n2) {
        if (MiningFarmMode.minecraftClient.player.currentScreenHandler == null || MiningFarmMode.minecraftClient.interactionManager == null) {
            return;
        }
        MiningFarmMode.minecraftClient.interactionManager.clickSlot(MiningFarmMode.minecraftClient.player.currentScreenHandler.syncId, n, n2, SlotActionType.PICKUP, (PlayerEntity)MiningFarmMode.minecraftClient.player);
    }

    private int countInventoryItem(Item class_17922) {
        int n = 0;
        PlayerInventory class_16612 = MiningFarmMode.minecraftClient.player.getInventory();
        for (int i = 0; i < class_16612.size(); ++i) {
            ItemStack class_17992 = class_16612.getStack(i);
            if (class_17992.getItem() != class_17922) continue;
            n += class_17992.getCount();
        }
        return n;
    }

    private int countSellableMineItems() {
        int n = 0;
        PlayerInventory class_16612 = MiningFarmMode.minecraftClient.player.getInventory();
        for (int i = 0; i < class_16612.size(); ++i) {
            ItemStack class_17992 = class_16612.getStack(i);
            if (!sellableMineItems.contains(class_17992.getItem())) continue;
            n += class_17992.getCount();
        }
        return n;
    }

    private int readSaleQuantity(ItemStack class_17992) {
        LoreComponent class_92902 = (LoreComponent)class_17992.get(DataComponentTypes.LORE);
        if (class_92902 != null) {
            for (Text class_25612 : class_92902.lines()) {
                String string = class_25612.getString();
                String string2 = string.toLowerCase();
                if (!string2.contains("\u0446\u0435\u043d\u0430 \u0437\u0430")) continue;
                if (string2.contains("\u0432\u0441\u0451") || string2.contains("\u0432\u0441\u0435")) {
                    return Integer.MAX_VALUE;
                }
                Matcher matcher = RUSSIAN_QUANTITY_PATTERN.matcher(string);
                if (!matcher.find()) continue;
                try {
                    return Integer.parseInt(matcher.group(1));
                }
                catch (NumberFormatException numberFormatException) {
                }
            }
        }
        return 0;
    }

    private boolean isSaleButtonItem(ItemStack class_17992) {
        LoreComponent class_92902 = (LoreComponent)class_17992.get(DataComponentTypes.LORE);
        if (class_92902 == null) {
            return false;
        }
        for (Text class_25612 : class_92902.lines()) {
            String string = class_25612.getString();
            String string2 = string.toLowerCase();
            if (!string2.contains("\u043f\u0440\u043e\u0434\u0430") || !string2.contains("\u0432\u0441\u0451") && !string2.contains("\u0432\u0441\u0435") || !string.contains(">") && !string.contains("\u25b6") && !string.contains("\u27a4") && !string.contains("\u279c")) continue;
            return true;
        }
        return false;
    }

    private Rotation rotateToward(Vec3d VanillaChestLootTableGenerator) {
        Vec3d WallPlayerSkullBlock = MiningFarmMode.minecraftClient.player.getEyePos();
        double d = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d2 = VanillaChestLootTableGenerator.y - WallPlayerSkullBlock.y;
        double d3 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = (float)(Math.toDegrees(Math.atan2(d3, d)) - 90.0);
        float f2 = MathHelper.clamp((float)((float)(-Math.toDegrees(Math.atan2(d2, d4)))), (float)-90.0f, (float)90.0f);
        MiningFarmMode.minecraftClient.player.setYaw(f);
        MiningFarmMode.minecraftClient.player.setPitch(f2);
        MiningFarmMode.minecraftClient.player.setHeadYaw(f);
        MiningFarmMode.minecraftClient.player.setBodyYaw(f);
        return new Rotation(f, f2);
    }

    private boolean isRotationAligned(BlockPos adminsky, Vec3d VanillaChestLootTableGenerator) {
        Rotation rotation;
        Rotation rotation2 = this.rotateToward(VanillaChestLootTableGenerator);
        if (!adminsky.equals(this.lastRotationTargetBlock)) {
            this.lastRotationTargetBlock = adminsky;
            this.miningRotationConfirmationCount = 0;
        }
        if ((rotation = RockstarClient.create().getRotationManager().getPacketRotation()).angleDistanceTo(rotation2) <= 1.5f) {
            ++this.miningRotationConfirmationCount;
            return this.miningRotationConfirmationCount >= 2;
        }
        return false;
    }

    private void releaseMovementKeys() {
        if (MiningFarmMode.minecraftClient.options == null) {
            return;
        }
        MiningFarmMode.minecraftClient.options.forwardKey.setPressed(false);
        MiningFarmMode.minecraftClient.options.backKey.setPressed(false);
        MiningFarmMode.minecraftClient.options.sprintKey.setPressed(false);
        MiningFarmMode.minecraftClient.options.jumpKey.setPressed(false);
    }

    private void equipBestMiningTool() {
        ItemStack class_17992 = MiningFarmMode.minecraftClient.player.getMainHandStack();
        float f = class_17992.getItem() instanceof PickaxeItem ? class_17992.getMiningSpeedMultiplier(stoneState) : -1.0f;
        HotbarSlot hotbarSlot = null;
        for (HotbarSlot hotbarSlot2 : ItemRuleSets.getHotbarRules().getRules()) {
            float f2;
            ItemStack class_17993 = hotbarSlot2.getItemStack();
            if (!(class_17993.getItem() instanceof PickaxeItem) || !((f2 = class_17993.getMiningSpeedMultiplier(stoneState)) > f)) continue;
            f = f2;
            hotbarSlot = hotbarSlot2;
        }
        if (hotbarSlot != null) {
            InventoryUtils.setSelectedHotbarSlot(hotbarSlot);
        }
    }

    private List<BlockPos> findMineableBlocks() {
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        for (int i = MINE_REGION_MAX_CORNER.getY(); i >= MINE_REGION_MIN_CORNER.getY(); --i) {
            for (int j = MINE_REGION_MIN_CORNER.getX(); j <= MINE_REGION_MAX_CORNER.getX(); ++j) {
                for (int k = MINE_REGION_MIN_CORNER.getZ(); k <= MINE_REGION_MAX_CORNER.getZ(); ++k) {
                    BlockPos adminsky = new BlockPos(j, i, k);
                    if (!this.isMineableTargetBlock(adminsky)) continue;
                    arrayList.add(adminsky);
                }
            }
        }
        arrayList.sort(Comparator.comparingDouble(this::getBlockDistanceSquared));
        return arrayList;
    }

    private boolean isMineableTargetBlock(BlockPos adminsky) {
        BlockState class_26802 = MiningFarmMode.minecraftClient.world.getBlockState(adminsky);
        if (class_26802.isAir()) {
            return false;
        }
        Block class_22482 = class_26802.getBlock();
        if (class_22482 == Blocks.BEDROCK || class_22482 == Blocks.BARRIER) {
            return false;
        }
        if (!class_26802.getFluidState().isEmpty()) {
            return false;
        }
        if (this.digAllSetting.isEnabled()) {
            return true;
        }
        return this.isSelectedOreBlock(class_22482);
    }

    private boolean isSelectedOreBlock(Block class_22482) {
        if (this.diamondOreOption.isSelected() && (class_22482 == Blocks.DIAMOND_ORE || class_22482 == Blocks.DEEPSLATE_DIAMOND_ORE)) {
            return true;
        }
        if (this.lapisOreOption.isSelected() && (class_22482 == Blocks.LAPIS_ORE || class_22482 == Blocks.DEEPSLATE_LAPIS_ORE)) {
            return true;
        }
        if (this.redstoneOreOption.isSelected() && (class_22482 == Blocks.REDSTONE_ORE || class_22482 == Blocks.DEEPSLATE_REDSTONE_ORE)) {
            return true;
        }
        if (this.ironOreOption.isSelected() && (class_22482 == Blocks.IRON_ORE || class_22482 == Blocks.DEEPSLATE_IRON_ORE)) {
            return true;
        }
        if (this.goldOreOption.isSelected() && (class_22482 == Blocks.GOLD_ORE || class_22482 == Blocks.DEEPSLATE_GOLD_ORE)) {
            return true;
        }
        return this.debrisOreOption.isSelected() && class_22482 == Blocks.ANCIENT_DEBRIS;
    }

    private boolean hasRemainingMineableBlocks() {
        for (int i = MINE_REGION_MAX_CORNER.getY(); i >= MINE_REGION_MIN_CORNER.getY(); --i) {
            for (int j = MINE_REGION_MIN_CORNER.getX(); j <= MINE_REGION_MAX_CORNER.getX(); ++j) {
                for (int k = MINE_REGION_MIN_CORNER.getZ(); k <= MINE_REGION_MAX_CORNER.getZ(); ++k) {
                    Block class_22482;
                    BlockState class_26802 = MiningFarmMode.minecraftClient.world.getBlockState(new BlockPos(j, i, k));
                    if (class_26802.isAir() || (class_22482 = class_26802.getBlock()) == Blocks.BEDROCK || class_22482 == Blocks.BARRIER || !class_26802.getFluidState().isEmpty()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private double getBlockDistanceSquared(BlockPos adminsky) {
        return MiningFarmMode.minecraftClient.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky));
    }

    private Direction findMiningFace(BlockPos adminsky, double d) {
        Vec3d VanillaChestLootTableGenerator = MiningFarmMode.minecraftClient.player.getEyePos();
        double d2 = d * d;
        Direction class_23502 = null;
        double d3 = Double.MAX_VALUE;
        for (Direction class_23503 : Direction.values()) {
            BlockHitResult class_39652;
            Vec3d WallPlayerSkullBlock;
            double d4;
            if (!this.isSafePathBlock(adminsky.offset(class_23503)) || (d4 = VanillaChestLootTableGenerator.squaredDistanceTo(WallPlayerSkullBlock = Vec3d.ofCenter((Vec3i)adminsky).add((double)class_23503.getOffsetX() * 0.49, (double)class_23503.getOffsetY() * 0.49, (double)class_23503.getOffsetZ() * 0.49))) > d2 || d4 >= d3 || (class_39652 = MiningFarmMode.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, WallPlayerSkullBlock, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)MiningFarmMode.minecraftClient.player))).getType() != HitResult.Type.BLOCK || !class_39652.getBlockPos().equals(adminsky)) continue;
            class_23502 = class_23503;
            d3 = d4;
        }
        return class_23502;
    }

    private boolean isSafePathBlock(BlockPos adminsky) {
        BlockState class_26802 = MiningFarmMode.minecraftClient.world.getBlockState(adminsky);
        if (class_26802.isAir() || class_26802.isReplaceable()) {
            return true;
        }
        return class_26802.getCollisionShape((BlockView)MiningFarmMode.minecraftClient.world, adminsky).isEmpty();
    }

    private Vec3d getBlockFaceCenter(BlockPos adminsky, Direction class_23502) {
        return Vec3d.ofCenter((Vec3i)adminsky).add((double)class_23502.getOffsetX() * 0.5, (double)class_23502.getOffsetY() * 0.5, (double)class_23502.getOffsetZ() * 0.5);
    }

    private double getMineRegionDistance() {
        double d = MiningFarmMode.minecraftClient.player.getX();
        double d2 = MiningFarmMode.minecraftClient.player.getZ();
        double d3 = MathHelper.clamp((double)d, (double)MINE_REGION_MIN_CORNER.getX(), (double)MINE_REGION_MAX_CORNER.getX());
        double d4 = MathHelper.clamp((double)d2, (double)MINE_REGION_MIN_CORNER.getZ(), (double)MINE_REGION_MAX_CORNER.getZ());
        double d5 = d - d3;
        double d6 = d2 - d4;
        return Math.sqrt(d5 * d5 + d6 * d6);
    }

    private void processInventoryCleanup() {
        long l;
        boolean bl;
        if (!this.cleanInventorySetting.isEnabled() && !this.dropExceptSetting.isEnabled()) {
            return;
        }
        if (MiningFarmMode.minecraftClient.player.currentScreenHandler == null) {
            return;
        }
        double d = Math.hypot(MiningFarmMode.minecraftClient.player.getVelocity().x, MiningFarmMode.minecraftClient.player.getVelocity().z);
        boolean bl2 = d > 0.06;
        boolean bl3 = bl = this.countEmptyInventorySlots() <= 2;
        if (bl2 && !bl) {
            return;
        }
        long l2 = l = bl2 ? 150L : 0L;
        if (!this.inventoryTransferTimer.hasElapsed(l)) {
            return;
        }
        int n = bl2 ? 1 : 3;
        int n2 = 36 + MiningFarmMode.minecraftClient.player.getInventory().selectedSlot;
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules());
        int n3 = 0;
        for (ItemRule itemRule : itemRuleCollection.getRules()) {
            if (n3 >= n) break;
            ItemStack class_17992 = itemRule.getItemStack();
            if (class_17992.isEmpty() || itemRule.getClickSlot() == n2 || this.isProtectedInventoryStack(class_17992)) continue;
            MiningFarmMode.minecraftClient.interactionManager.clickSlot(MiningFarmMode.minecraftClient.player.currentScreenHandler.syncId, itemRule.getClickSlot(), 1, SlotActionType.THROW, (PlayerEntity)MiningFarmMode.minecraftClient.player);
            ++n3;
        }
        if (n3 > 0) {
            this.inventoryTransferTimer.reset();
        }
    }

    private boolean isProtectedInventoryStack(ItemStack class_17992) {
        Item class_17922 = class_17992.getItem();
        if (class_17922 instanceof PickaxeItem || class_17922 instanceof SwordItem || class_17922 instanceof AxeItem || class_17922 instanceof ShovelItem || class_17922 instanceof HoeItem || class_17922 instanceof ArmorItem) {
            return true;
        }
        if (class_17992.contains(DataComponentTypes.FOOD)) {
            return true;
        }
        if (class_17922 == Items.EXPERIENCE_BOTTLE || class_17922 == Items.TOTEM_OF_UNDYING || class_17922 == Items.ELYTRA || class_17922 == Items.ENDER_PEARL || class_17922 == Items.ENDER_CHEST) {
            return true;
        }
        if (this.dropExceptSetting.isEnabled()) {
            return this.isSelectedKeepItem(class_17922);
        }
        return protectedMineItems.contains(class_17922);
    }

    private boolean isSelectedKeepItem(Item class_17922) {
        if (this.diamondKeepOption.isSelected() && (class_17922 == Items.DIAMOND || class_17922 == Items.DIAMOND_BLOCK)) {
            return true;
        }
        if (this.debrisKeepOption.isSelected() && (class_17922 == Items.ANCIENT_DEBRIS || class_17922 == Items.NETHERITE_SCRAP || class_17922 == Items.NETHERITE_INGOT || class_17922 == Items.NETHERITE_BLOCK)) {
            return true;
        }
        if (this.ironKeepOption.isSelected() && (class_17922 == Items.RAW_IRON || class_17922 == Items.IRON_INGOT || class_17922 == Items.IRON_NUGGET || class_17922 == Items.IRON_BLOCK)) {
            return true;
        }
        return this.goldKeepOption.isSelected() && (class_17922 == Items.RAW_GOLD || class_17922 == Items.GOLD_INGOT || class_17922 == Items.GOLD_NUGGET || class_17922 == Items.GOLD_BLOCK);
    }

    private boolean isRepairNeeded() {
        if (!this.autoRepairSetting.isEnabled()) {
            return false;
        }
        if (this.repairCycleActive && !this.repairSessionTimer.hasElapsed(300000L)) {
            return false;
        }
        this.repairCycleActive = false;
        this.equipBestMiningTool();
        ItemStack class_17992 = MiningFarmMode.minecraftClient.player.getMainHandStack();
        if (!(class_17992.getItem() instanceof PickaxeItem) || !class_17992.isDamageable()) {
            return false;
        }
        return this.getItemDurabilityPercent(class_17992) < (double)this.repairThresholdSetting.getValue();
    }

    private boolean isMiningResourcesReady() {
        if (this.countExperienceBottles() > 0) {
            return true;
        }
        if (this.inventoryFullNoticeActive && !this.inventoryFullCooldownTimer.hasElapsed(60000L)) {
            return false;
        }
        if (this.hasEmptyInventorySlot()) {
            return true;
        }
        return this.autoSellSetting.isEnabled() && this.countSellableMineItems() > 0 && MiningFarmMode.parseMineIndex(this.sellServerSetting.getValue()) > 0;
    }

    private void beginSellingWorkflow() {
        this.releaseMovementKeys();
        this.repairActionTimer.reset();
        this.lastRepairBottleCount = -1;
        this.repairProgressTimer.reset();
        this.operationState = MiningOperationState.SELLING_RESOURCES;
    }

    private void resetMiningCycle() {
        this.operationState = MiningOperationState.LOCATING_RESOURCE;
        this.stateScanTimer.reset();
    }

    private void processToolRepair() {
        this.releaseMovementKeys();
        this.equipBestMiningTool();
        ItemStack class_17993 = MiningFarmMode.minecraftClient.player.getMainHandStack();
        if (!(class_17993.getItem() instanceof PickaxeItem) || !class_17993.isDamageable()) {
            this.resetMiningCycle();
            return;
        }
        if (this.getItemDurabilityPercent(class_17993) >= (double)this.repairTargetPercentageSetting.getValue()) {
            this.resetMiningCycle();
            return;
        }
        if (this.countExperienceBottles() <= 0) {
            this.openMarketplaceForRepairBottles();
            return;
        }
        if (MiningFarmMode.minecraftClient.player.getOffHandStack().getItem() != Items.EXPERIENCE_BOTTLE) {
            ItemRule itemRule = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules()).findByStack(class_17992 -> class_17992.getItem() == Items.EXPERIENCE_BOTTLE);
            if (itemRule == null) {
                this.openMarketplaceForRepairBottles();
                return;
            }
            InventoryUtils.swapItemRules(itemRule, InventoryUtils.offhandRule());
            this.repairActionTimer.reset();
            return;
        }
        int n2 = class_17993.getDamage();
        if (this.lastRepairBottleCount < 0 || n2 < this.lastRepairBottleCount) {
            this.lastRepairBottleCount = n2;
            this.repairProgressTimer.reset();
        } else if (this.repairProgressTimer.hasElapsed(12000L)) {
            this.repairCycleActive = true;
            this.repairSessionTimer.reset();
            if (this.statusMessageTimer.hasElapsed(5000L)) {
                this.showWarning("modules.mine_farm.repair_stuck");
                this.statusMessageTimer.reset();
            }
            this.resetMiningCycle();
            return;
        }
        float f = MiningFarmMode.minecraftClient.player.getYaw();
        MiningFarmMode.minecraftClient.player.setPitch(90.0f);
        MiningFarmMode.minecraftClient.player.setHeadYaw(f);
        if (this.repairActionTimer.hasElapsed(120L)) {
            ((moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)MiningFarmMode.minecraftClient.interactionManager).rockstar$sendSequencedPacket(MiningFarmMode.minecraftClient.world, n -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, n, f, 90.0f));
            this.repairActionTimer.reset();
        }
    }

    private void openMarketplaceForRepairBottles() {
        if (!this.hasEmptyInventorySlot()) {
            if (this.autoSellSetting.isEnabled() && this.countSellableMineItems() > 0 && MiningFarmMode.parseMineIndex(this.sellServerSetting.getValue()) > 0 && this.beginInventoryProcessing(true)) {
                return;
            }
            this.sendStatusMessageAndReset("modules.mine_farm.inventory_full");
            return;
        }
        this.marketSearchActive = false;
        this.marketSearchAttempts = 0;
        this.marketPurchaseAttempts = 0;
        this.marketPurchaseCount = 0;
        this.marketMenuTimer.reset();
        this.marketActionTimer.reset();
        this.operationState = MiningOperationState.OPENING_SELL_MENU;
    }

    private void sendStatusMessageAndReset(String string) {
        if (MiningFarmMode.minecraftClient.currentScreen != null) {
            MiningFarmMode.minecraftClient.player.closeHandledScreen();
        }
        this.inventoryFullNoticeActive = true;
        this.inventoryFullCooldownTimer.reset();
        if (this.statusMessageTimer.hasElapsed(5000L)) {
            this.showWarning(string);
            this.statusMessageTimer.reset();
        }
        this.resetMiningCycle();
    }

    private String getExperienceBottleName() {
        String string = "\u0411\u0443\u0442\u044b\u043b\u043e\u0447\u043a\u0430 \u043e\u043f\u044b\u0442\u0430";
        if (string == null || string.isBlank()) {
            string = "\u0431\u0443\u0442\u044b\u043b\u043e\u0447\u043a\u0430 \u043e\u043f\u044b\u0442\u0430";
        }
        return string;
    }

    private boolean isAuctionSearchScreen() {
        if (!(MiningFarmMode.minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
            return false;
        }
        if (MarketInventoryAnalyzer.isPriceOrMarketText(this.getOpenScreenTitle())) {
            return true;
        }
        return this.getOpenScreenTitle().contains("\u0431\u0443\u0442\u044b\u043b");
    }

    private void searchMarketplaceForBottles() {
        if (this.isAuctionSearchScreen()) {
            this.marketMenuTimer.reset();
            this.marketActionTimer.reset();
            this.operationState = MiningOperationState.PROCESSING_SELL_MENU;
            return;
        }
        if (MiningFarmMode.minecraftClient.currentScreen != null) {
            if (this.marketActionTimer.hasElapsed(2500L)) {
                MiningFarmMode.minecraftClient.player.closeHandledScreen();
                this.marketActionTimer.reset();
            }
            return;
        }
        if (!this.marketSearchActive || this.marketActionTimer.hasElapsed(3500L)) {
            MiningFarmMode.minecraftClient.player.networkHandler.sendChatCommand("ah search " + this.getExperienceBottleName());
            this.marketSearchActive = true;
            this.marketActionTimer.reset();
            ++this.marketSearchAttempts;
            if (this.marketSearchAttempts > 6) {
                this.sendStatusMessageAndReset("modules.mine_farm.auction_failed");
            }
        }
    }

    private void selectBottleAuction() {
        if (!this.isAuctionSearchScreen()) {
            this.marketSearchActive = false;
            this.marketActionTimer.reset();
            this.operationState = MiningOperationState.OPENING_SELL_MENU;
            return;
        }
        if (!this.marketMenuTimer.hasElapsed(400L)) {
            return;
        }
        int n = this.findCheapestAuctionSlot();
        if (n < 0) {
            this.sendStatusMessageAndReset("modules.mine_farm.bottles_not_found");
            return;
        }
        this.clickInventorySlot(n, 0);
        this.marketActionTimer.reset();
        this.operationState = MiningOperationState.CONFIRMING_MARKET_ACTION;
    }

    private void confirmBottlePurchase() {
        String string = this.getOpenScreenTitle();
        if (string.contains("\u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043d\u0438\u0435 \u043f\u043e\u043a\u0443\u043f\u043a\u0438")) {
            if (this.marketActionTimer.hasElapsed(300L)) {
                this.clickInventorySlot(2, 0);
                this.marketActionTimer.reset();
                this.operationState = MiningOperationState.COMPLETING_MARKET_ACTION;
            }
            return;
        }
        if (this.isAuctionSearchScreen()) {
            if (this.marketActionTimer.hasElapsed(600L)) {
                this.marketMenuTimer.reset();
                this.operationState = MiningOperationState.PROCESSING_SELL_MENU;
            }
            return;
        }
        if (this.marketActionTimer.hasElapsed(2500L)) {
            ++this.marketPurchaseAttempts;
            if (this.marketPurchaseAttempts > 4) {
                this.sendStatusMessageAndReset("modules.mine_farm.buy_failed");
                return;
            }
            this.marketSearchActive = false;
            this.operationState = MiningOperationState.OPENING_SELL_MENU;
        }
    }

    private void finishBottlePurchase() {
        if (!this.marketActionTimer.hasElapsed(400L)) {
            return;
        }
        if (MiningFarmMode.minecraftClient.currentScreen != null) {
            MiningFarmMode.minecraftClient.player.closeHandledScreen();
        }
        this.inventoryFullNoticeActive = false;
        ++this.marketPurchaseCount;
        if (this.countExperienceBottles() >= (int)this.repairBottleTargetSetting.getValue() || !this.hasEmptyInventorySlot() || this.marketPurchaseCount >= 20) {
            this.repairActionTimer.reset();
            this.operationState = MiningOperationState.SELLING_RESOURCES;
            return;
        }
        this.marketSearchActive = false;
        this.marketSearchAttempts = 0;
        this.marketActionTimer.reset();
        this.operationState = MiningOperationState.OPENING_SELL_MENU;
    }

    private double getItemDurabilityPercent(ItemStack class_17992) {
        if (!class_17992.isDamageable() || class_17992.getMaxDamage() <= 0) {
            return 100.0;
        }
        return (double)(class_17992.getMaxDamage() - class_17992.getDamage()) / (double)class_17992.getMaxDamage() * 100.0;
    }

    private int countExperienceBottles() {
        int n = 0;
        PlayerInventory class_16612 = MiningFarmMode.minecraftClient.player.getInventory();
        for (int i = 0; i < class_16612.size(); ++i) {
            ItemStack class_17992 = class_16612.getStack(i);
            if (class_17992.getItem() != Items.EXPERIENCE_BOTTLE) continue;
            n += class_17992.getCount();
        }
        return n;
    }

    private boolean hasEmptyInventorySlot() {
        return this.countEmptyInventorySlots() > 0;
    }

    private int countEmptyInventorySlots() {
        PlayerInventory class_16612 = MiningFarmMode.minecraftClient.player.getInventory();
        int n = 0;
        for (int i = 0; i < 36; ++i) {
            if (!class_16612.getStack(i).isEmpty()) continue;
            ++n;
        }
        return n;
    }

    private long readAuctionPrice(ItemStack class_17992) {
        for (Text class_25612 : class_17992.getTooltip(Item.TooltipContext.create((World)MiningFarmMode.minecraftClient.world), (PlayerEntity)MiningFarmMode.minecraftClient.player, (TooltipType)(MiningFarmMode.minecraftClient.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC))) {
            String string;
            String string2 = class_25612.getString();
            String string3 = string2.toLowerCase();
            if (!string2.contains("$") && !string3.contains("\u0446\u0435\u043d\u0430") && !string3.contains("\u0441\u0442\u043e\u0438\u043c") || string2.contains("%") || (string = string2.replaceAll("[^0-9]", "")).isEmpty()) continue;
            try {
                long l = Long.parseLong(string);
                if (l <= 0L) continue;
                return l;
            }
            catch (NumberFormatException numberFormatException) {
            }
        }
        return -1L;
    }

    private int findCheapestAuctionSlot() {
        ScreenHandler class_17032 = MiningFarmMode.minecraftClient.player.currentScreenHandler;
        if (!(class_17032 instanceof GenericContainerScreenHandler)) {
            return -1;
        }
        GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)class_17032;
        int n = class_17072.slots.size() - 36;
        double d = Double.MAX_VALUE;
        int n2 = -1;
        for (int i = 0; i < n; ++i) {
            int n3;
            double d2;
            long l;
            ItemStack class_17992;
            Slot class_17352 = class_17072.getSlot(i);
            if (class_17352 == null || !class_17352.hasStack() || (class_17992 = class_17352.getStack()).getItem() != Items.EXPERIENCE_BOTTLE || (l = this.readAuctionPrice(class_17992)) <= 0L || !((d2 = (double)l / (double)(n3 = Math.max(1, class_17992.getCount()))) < d)) continue;
            d = d2;
            n2 = class_17352.id;
        }
        return n2;
    }

    private String getMiningStatusText() {
        String string = this.selectedMineLocation != null ? String.valueOf(this.selectedMineServerIndex) : "-";
        return switch (this.operationState.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (this.mineApiError != null) {
                    yield "API: " + this.mineApiError;
                }
                yield "\u0412\u044b\u0431\u043e\u0440 \u0448\u0430\u0445\u0442\u044b...";
            }
            case 1 -> "\u041f\u0435\u0440\u0435\u0445\u043e\u0434 \u043d\u0430 \u0430\u043d\u0430\u0440\u0445\u0438\u044e " + string;
            case 2 -> "\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442 \u043d\u0430 \u0448\u0430\u0445\u0442\u0443 (\u0430\u043d. " + string + ")";
            case 3 -> {
                if (this.blockMiningActive) {
                    yield "\u041a\u043e\u043f\u0430\u044e \u0448\u0430\u0445\u0442\u0443 (\u0430\u043d. " + string + ")";
                }
                yield "\u0416\u0434\u0443 \u0441\u0431\u0440\u043e\u0441\u0430 (\u0430\u043d. " + string + ")";
            }
            case 4 -> "\u0418\u0434\u0443 \u043f\u0440\u043e\u0434\u0430\u0432\u0430\u0442\u044c (\u0430\u043d. " + String.valueOf(this.sellServerIndex > 0 ? Integer.valueOf(this.sellServerIndex) : "?") + ")";
            case 5 -> "\u041e\u0442\u043a\u0440\u044b\u0432\u0430\u044e \u0441\u043a\u0443\u043f\u0449\u0438\u043a\u0430";
            case 6 -> "\u0412\u044b\u0431\u043e\u0440 \u0441\u0435\u043a\u0446\u0438\u0438";
            case 7 -> "\u041f\u0440\u043e\u0434\u0430\u044e \u0440\u0443\u0434\u0443";
            case 8 -> "\u0427\u0438\u043d\u044e \u043a\u0438\u0440\u043a\u0443 (" + this.countExperienceBottles() + " \u0431\u0443\u0442.)";
            case 9, 10, 11, 12 -> "\u0417\u0430\u043a\u0443\u043f\u0430\u044e \u0431\u0443\u0442\u044b\u043b\u044c\u043a\u0438 (" + this.countExperienceBottles() + ")";
        };
    }

    private static String formatRarityLabel(String string) {
        if (string == null) {
            return "?";
        }
        return switch (string.toLowerCase()) {
            case "default" -> "def";
            case "legendary" -> "leg";
            case "mythical" -> "myth";
            default -> string;
        };
    }

    private static String formatDuration(long l) {
        long l2 = l / 60L;
        long l3 = l % 60L;
        return String.format("%02d:%02d", l2, l3);
    }

    static enum MiningOperationState {
        LOCATING_RESOURCE,
        TRAVELING_TO_RESOURCE,
        WAITING_FOR_SERVER_DATA,
        MINING_RESOURCE,
        PROCESSING_INVENTORY,
        HARVESTING_RESOURCE,
        WAITING_FOR_MINING_COOLDOWN,
        PROCESSING_MINE_ACTION,
        SELLING_RESOURCES,
        OPENING_SELL_MENU,
        PROCESSING_SELL_MENU,
        CONFIRMING_MARKET_ACTION,
        COMPLETING_MARKET_ACTION;
}
}
