/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.RecipeDisplayEntry
 *  net.minecraft.NetworkRecipeId
 *  net.minecraft.ContextParameterMap
 *  net.minecraft.SlotDisplayContexts
 *  net.minecraft.Hand
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.CraftingScreenHandler
 *  net.minecraft.Slot
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.ItemConvertible
 *  net.minecraft.World
 *  net.minecraft.Blocks
 *  net.minecraft.Direction
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.CloseHandledScreenC2SPacket
 *  net.minecraft.CraftRequestC2SPacket
 *  net.minecraft.ClientRecipeBook
 *  net.minecraft.BlockHitResult
 *  net.minecraft.HandledScreen
 *  net.minecraft.RecipeResultCollection
 *  net.minecraft.RegistryKey
 *  net.minecraft.GameMessageS2CPacket
 */
package moscow.rockstar.modules.player.farming.market;

import java.util.function.Predicate;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.items.rules.InventorySlotRule;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.player.farming.core.AutoFarm;
import moscow.rockstar.modules.player.farming.core.FarmModeBase;
import moscow.rockstar.modules.player.farming.core.FarmState;
import moscow.rockstar.modules.player.farming.hud.FarmDisplayCategory;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.StringSetting;
import moscow.rockstar.util.Timer;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.util.context.ContextParameterMap;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemConvertible;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.registry.RegistryKey;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;

public class SwordFarmMode
extends FarmModeBase {
    private final StringSetting salePriceSetting;
    private final NumberSetting relistCooldownSetting;
    private final BooleanSetting craftAllSetting;
    private static final int MAX_CRAFTING_MATERIAL_TYPES = 6;
    private static final long ACTION_COOLDOWN_MILLIS = 350L;
    private static final long SCREEN_RESPONSE_TIMEOUT_MILLIS = 5000L;
    private static final int MINIMUM_MATERIAL_RESERVE = 4;
    private static final float ROTATION_TOLERANCE = 1.5f;
    private static final String EMERALD_SWORD_LABEL = "\u0418\u0437\u0443\u043c\u0440\u0443\u0434\u043d\u044b\u0439 \u043c\u0435\u0447";
    private static final int MAX_SALE_RETRY_COUNT = 3;
    private static final String STORAGE_TITLE = "\u0425\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435";
    private static final String PURCHASE_CONFIRMATION_TEXT = "\u0423 \u0412\u0430\u0441 \u043a\u0443\u043f\u0438\u043b\u0438";
    private SaleWorkflowState saleWorkflowState = SaleWorkflowState.CHECKING_MATERIALS;
    private final Timer actionCooldown = new Timer();
    private final Timer responseCooldown = new Timer();
    private final Timer saleCooldown = new Timer();
    private BlockPos craftingTablePosition;
    private Rotation targetRotation;
    private int rotationAttemptCount;
    private boolean saleActionActive;
    private NetworkRecipeId selectedRecipe;
    private int containerItemCount = -1;
    private int saleRetryCount = 0;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (SwordFarmMode.minecraftClient.player == null || SwordFarmMode.minecraftClient.world == null) {
            return;
        }
        if (this.saleWorkflowState == SaleWorkflowState.CHECKING_MATERIALS && !this.isItemAvailable(Items.STICK)) {
            this.showError("modules.sword_farm.no_sticks");
            this.disableAutoFarm();
            return;
        }
        switch (this.saleWorkflowState.ordinal()) {
            case 0: {
                this.checkCraftingMaterials();
                break;
            }
            case 1: {
                this.openShopMenu();
                break;
            }
            case 2: {
                this.purchaseBaseItem();
                break;
            }
            case 3: {
                this.purchaseUpgradeItem();
                break;
            }
            case 4: {
                this.confirmPurchase();
                break;
            }
            case 5: {
                this.setSaleWorkflowState(SaleWorkflowState.CHECKING_MATERIALS);
                break;
            }
            case 6: {
                this.selectCraftingTable();
                break;
            }
            case 7: {
                this.openCraftingTable();
                break;
            }
            case 8: {
                this.craftSword();
                break;
            }
            case 9: {
                this.setSaleWorkflowState(SaleWorkflowState.CHECKING_MATERIALS);
                break;
            }
            case 10: {
                this.openSaleScreen();
                break;
            }
            case 11: {
                this.processSaleScreen();
                break;
            }
            case 12: {
                this.selectSaleConfirmation();
                break;
            }
            case 13: {
                this.closeSaleScreen();
                break;
            }
            case 14: {
                this.submitSaleCommand();
                break;
            }
            case 15: {
                this.collectSaleConfirmation();
                break;
            }
            case 16: {
                this.collectSaleProceeds();
                break;
            }
            case 17: {
                this.setSaleWorkflowState(SaleWorkflowState.PROCESSING_STORAGE_SCREEN);
            }
        }
    };
    private final EventListener<ReceivePacketEvent> receivePacketListener = receivePacketEvent -> {
        GameMessageS2CPacket class_74392;
        Object object = receivePacketEvent.getPacket();
        if (object instanceof GameMessageS2CPacket && ((String)(object = (class_74392 = (GameMessageS2CPacket)object).content().getString())).contains(PURCHASE_CONFIRMATION_TEXT)) {
            this.saleActionActive = true;
            this.getFarmMetrics().recordBlockBreak();
        }
    };

    public SwordFarmMode(AutoFarm autoFarm, ModeSetting modeSetting) {
        super(autoFarm, modeSetting, "modules.settings.auto_farm.modes.sword");
        this.salePriceSetting = new StringSetting((SettingOwner)autoFarm, "modules.settings.auto_farm.sword.price", () -> !this.isSelected()).setValue("15000").setNumericOnly(true);
        this.relistCooldownSetting = new NumberSetting((SettingOwner)autoFarm, "modules.settings.auto_farm.sword.relist_cooldown", () -> !this.isSelected()).setStep(5.0f).setMinValue(5.0f).setMaxValue(300.0f).setValue(60.0f).setUnit("sec");
        this.craftAllSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.auto_farm.sword.craft_all", () -> !this.isSelected());
    }

    @Override
    public void startFarmAutomation() {
        if (SwordFarmMode.minecraftClient.player == null || SwordFarmMode.minecraftClient.world == null) {
            this.disableAutoFarm();
            return;
        }
        if (!this.isItemAvailable(Items.STICK)) {
            this.showError("modules.sword_farm.no_sticks");
            this.disableAutoFarm();
            return;
        }
        this.saleWorkflowState = SaleWorkflowState.CHECKING_MATERIALS;
        this.craftingTablePosition = null;
        this.targetRotation = null;
        this.rotationAttemptCount = 0;
        this.saleActionActive = false;
        this.selectedRecipe = null;
        this.actionCooldown.reset();
    }

    @Override
    public void resetBrewingState() {
        this.saleWorkflowState = SaleWorkflowState.CHECKING_MATERIALS;
        this.craftingTablePosition = null;
        this.targetRotation = null;
        this.rotationAttemptCount = 0;
        this.selectedRecipe = null;
    }

    @Override
    public FarmState getFarmState() {
        return switch (this.saleWorkflowState.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> FarmState.IDLE;
            case 1, 2, 3, 4, 5 -> FarmState.BUYING;
            case 6, 7, 8, 9 -> FarmState.CRAFTING;
            case 10, 11, 12, 13, 14, 15, 16, 17 -> FarmState.SELLING;
        };
    }

    @Override
    public FarmDisplayCategory getFarmDisplayCategory() {
        return FarmDisplayCategory.SALES;
    }

    @Override
    public ItemStack getFarmDisplayItem() {
        return new ItemStack((ItemConvertible)Items.DIAMOND_SWORD);
    }

    private void checkCraftingMaterials() {
        boolean bl;
        if (!this.actionCooldown.hasElapsed(350L)) {
            return;
        }
        boolean bl2 = bl = this.countAvailableSaleItems() > 0;
        if (bl && (!this.isItemAvailable(Items.EMERALD) || this.countEmptyInventorySlots() <= 4)) {
            this.transitionSaleState(SaleWorkflowState.PROCESSING_STORAGE_SCREEN);
            return;
        }
        if (!this.isItemAvailable(Items.EMERALD)) {
            this.transitionSaleState(SaleWorkflowState.OPENING_SHOP_MENU);
            return;
        }
        this.transitionSaleState(SaleWorkflowState.CRAFTING_BASE_ITEM);
    }

    private void openShopMenu() {
        if (!this.actionCooldown.hasElapsed(350L)) {
            return;
        }
        SwordFarmMode.minecraftClient.player.networkHandler.sendChatCommand("shop");
        this.transitionSaleState(SaleWorkflowState.PURCHASING_BASE_ITEM);
    }

    private void purchaseBaseItem() {
        if (!this.actionCooldown.hasElapsed(600L)) {
            return;
        }
        ScreenHandler class_17032 = this.getOpenContainerScreen();
        if (class_17032 == null) {
            return;
        }
        int n = SwordFarmMode.findContainerSlot(class_17032, class_17992 -> class_17992.getItem() == Items.GOLD_INGOT);
        if (n == -1) {
            if (this.responseCooldown.hasElapsed(5000L)) {
                this.showError("modules.sword_farm.shop_no_gold");
                this.disableAutoFarm();
            }
            return;
        }
        SwordFarmMode.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)SwordFarmMode.minecraftClient.player);
        this.transitionSaleState(SaleWorkflowState.PURCHASING_UPGRADE_ITEM);
    }

    private void purchaseUpgradeItem() {
        if (!this.actionCooldown.hasElapsed(600L)) {
            return;
        }
        ScreenHandler class_17032 = this.getOpenContainerScreen();
        if (class_17032 == null) {
            return;
        }
        int n = SwordFarmMode.findContainerSlot(class_17032, class_17992 -> class_17992.getItem() == Items.EMERALD);
        if (n == -1) {
            if (this.responseCooldown.hasElapsed(5000L)) {
                this.showError("modules.sword_farm.shop_no_emerald");
                this.disableAutoFarm();
            }
            return;
        }
        SwordFarmMode.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, 1, SlotActionType.PICKUP, (PlayerEntity)SwordFarmMode.minecraftClient.player);
        this.transitionSaleState(SaleWorkflowState.SELECTING_CRAFTING_RECIPE);
    }

    private void confirmPurchase() {
        if (!this.actionCooldown.hasElapsed(600L)) {
            return;
        }
        ScreenHandler class_17032 = this.getOpenContainerScreen();
        if (class_17032 == null) {
            return;
        }
        int n = SwordFarmMode.findContainerSlot(class_17032, class_17992 -> class_17992.getItem() == Items.LIME_STAINED_GLASS_PANE);
        if (n == -1) {
            if (this.responseCooldown.hasElapsed(5000L)) {
                this.showError("modules.sword_farm.shop_no_confirm");
                this.disableAutoFarm();
            }
            return;
        }
        SwordFarmMode.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)SwordFarmMode.minecraftClient.player);
        this.transitionSaleState(SaleWorkflowState.WAITING_FOR_RECIPE_RESULT);
    }

    private void selectCraftingTable() {
        if (this.craftingTablePosition == null || !this.isCraftingTableBlock(this.craftingTablePosition) || !this.isCraftingTableWithinRange(this.craftingTablePosition)) {
            this.craftingTablePosition = this.findNearestCraftingTable();
        }
        if (this.craftingTablePosition == null) {
            this.showError("modules.sword_farm.no_crafting_table");
            this.disableAutoFarm();
            return;
        }
        this.transitionSaleState(SaleWorkflowState.CRAFTING_UPGRADE_ITEM);
    }

    private void openCraftingTable() {
        if (SwordFarmMode.minecraftClient.currentScreen instanceof HandledScreen) {
            this.transitionSaleState(SaleWorkflowState.PREPARING_SALE);
            return;
        }
        if (this.craftingTablePosition == null || !this.isCraftingTableBlock(this.craftingTablePosition)) {
            this.transitionSaleState(SaleWorkflowState.CRAFTING_BASE_ITEM);
            return;
        }
        Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)this.craftingTablePosition);
        if (!this.isRotationAligned(VanillaChestLootTableGenerator)) {
            return;
        }
        if (!this.actionCooldown.hasElapsed(350L)) {
            return;
        }
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, this.craftingTablePosition, false);
        SwordFarmMode.minecraftClient.interactionManager.interactBlock(SwordFarmMode.minecraftClient.player, Hand.MAIN_HAND, class_39652);
        SwordFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        this.actionCooldown.reset();
    }

    private void craftSword() {
        boolean bl;
        if (!this.actionCooldown.hasElapsed(350L)) {
            return;
        }
        ScreenHandler class_17032 = SwordFarmMode.minecraftClient.player.currentScreenHandler;
        if (!(class_17032 instanceof CraftingScreenHandler)) {
            this.transitionSaleState(SaleWorkflowState.CRAFTING_UPGRADE_ITEM);
            return;
        }
        CraftingScreenHandler class_17142 = (CraftingScreenHandler)class_17032;
        if (!this.isItemAvailable(Items.STICK)) {
            this.showError("modules.sword_farm.no_sticks");
            this.disableAutoFarm();
            return;
        }
        boolean bl2 = this.isCraftingGridEmpty(class_17142);
        boolean bl3 = class_17142.getSlot(0).getStack().isEmpty();
        int n = this.countEmptyInventorySlots();
        boolean bl4 = bl = !this.isItemAvailable(Items.EMERALD) || this.countAvailableSaleItems() > 0 && n <= 4;
        if (bl) {
            if (!bl3 && n > 0) {
                SwordFarmMode.minecraftClient.interactionManager.clickSlot(class_17142.syncId, 0, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)SwordFarmMode.minecraftClient.player);
                this.actionCooldown.reset();
                return;
            }
            if (!bl2 && n > 0) {
                this.clearCraftingGrid(class_17142);
                this.actionCooldown.reset();
                return;
            }
            this.transitionSaleState(SaleWorkflowState.OPENING_STORAGE);
            return;
        }
        if (!bl3) {
            SwordFarmMode.minecraftClient.interactionManager.clickSlot(class_17142.syncId, 0, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)SwordFarmMode.minecraftClient.player);
            this.actionCooldown.reset();
            return;
        }
        if (this.selectedRecipe == null) {
            this.selectedRecipe = this.findSwordRecipe();
        }
        if (this.selectedRecipe == null) {
            if (this.responseCooldown.hasElapsed(5000L)) {
                this.showError("modules.sword_farm.recipe_not_found");
                this.disableAutoFarm();
            }
            return;
        }
        SwordFarmMode.minecraftClient.player.networkHandler.sendPacket((Packet)new CraftRequestC2SPacket(class_17142.syncId, this.selectedRecipe, this.craftAllSetting.isEnabled()));
        this.actionCooldown.reset();
    }

    private void clearCraftingGrid(CraftingScreenHandler class_17142) {
        for (int i = 1; i <= 9 && i < class_17142.slots.size(); ++i) {
            if (class_17142.getSlot(i).getStack().isEmpty()) continue;
            SwordFarmMode.minecraftClient.interactionManager.clickSlot(class_17142.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)SwordFarmMode.minecraftClient.player);
        }
    }

    private int countEmptyInventorySlots() {
        int n = 0;
        for (InventorySlotRule itemRule : ItemRuleSets.getInventoryRules().getRules()) {
            if (!itemRule.isEmpty()) continue;
            ++n;
        }
        for (HotbarSlot hotbarSlot : ItemRuleSets.getHotbarRules().getRules()) {
            if (!hotbarSlot.isEmpty()) continue;
            ++n;
        }
        return n;
    }

    private NetworkRecipeId findSwordRecipe() {
        if (SwordFarmMode.minecraftClient.player == null || SwordFarmMode.minecraftClient.world == null) {
            return null;
        }
        ClientRecipeBook class_2992 = SwordFarmMode.minecraftClient.player.getRecipeBook();
        if (class_2992 == null) {
            return null;
        }
        ContextParameterMap class_103522 = SlotDisplayContexts.createParameters((World)SwordFarmMode.minecraftClient.world);
        NetworkRecipeId class_102982 = null;
        for (RecipeResultCollection PalettedBlockInfoList : class_2992.getOrderedResults()) {
            for (RecipeDisplayEntry class_102972 : PalettedBlockInfoList.getAllRecipes()) {
                for (ItemStack class_17992 : class_102972.getStacks(class_103522)) {
                    if (class_17992.isEmpty()) continue;
                    if (SwordFarmMode.isEmeraldSwordItem(class_17992)) {
                        return class_102972.id();
                    }
                    if (class_102982 != null || !SwordFarmMode.isEnchantedSwordItem(class_17992)) continue;
                    class_102982 = class_102972.id();
                }
            }
        }
        return class_102982;
    }

    private boolean isCraftingGridEmpty(CraftingScreenHandler class_17142) {
        for (int i = 1; i <= 9 && i < class_17142.slots.size(); ++i) {
            if (class_17142.getSlot(i).getStack().isEmpty()) continue;
            return false;
        }
        return true;
    }

    private void openSaleScreen() {
        if (!this.actionCooldown.hasElapsed(350L)) {
            return;
        }
        if (this.countAvailableSaleItems() == 0) {
            this.transitionSaleState(SaleWorkflowState.CHECKING_MATERIALS);
            return;
        }
        String string = this.salePriceSetting.getValue() == null || this.salePriceSetting.getValue().isBlank() ? "15000" : this.salePriceSetting.getValue().trim();
        SwordFarmMode.minecraftClient.player.networkHandler.sendChatCommand("ah sellgui " + string);
        this.transitionSaleState(SaleWorkflowState.SELECTING_STORAGE_ITEM);
    }

    private void processSaleScreen() {
        if (!this.actionCooldown.hasElapsed(350L)) {
            return;
        }
        ScreenHandler class_17032 = this.getOpenContainerScreen();
        if (class_17032 == null) {
            return;
        }
        if (this.countEmptyStorageSlots(class_17032) == 0) {
            this.transitionSaleState(SaleWorkflowState.PROCESSING_STORAGE_ACTION);
            return;
        }
        int n = this.countMatchingInventoryItems(class_17032);
        if (n == 0) {
            this.transitionSaleState(SaleWorkflowState.PROCESSING_STORAGE_ACTION);
            return;
        }
        if (this.containerItemCount != -1 && n >= this.containerItemCount) {
            ++this.saleRetryCount;
            if (this.saleRetryCount >= 3) {
                this.transitionSaleState(SaleWorkflowState.PROCESSING_STORAGE_ACTION);
                return;
            }
        } else {
            this.saleRetryCount = 0;
        }
        this.containerItemCount = n;
        for (int i = 0; i < class_17032.slots.size(); ++i) {
            Slot class_17352 = (Slot)class_17032.slots.get(i);
            if (class_17352.inventory != SwordFarmMode.minecraftClient.player.getInventory() || !SwordFarmMode.isSwordItem(class_17352.getStack())) continue;
            SwordFarmMode.minecraftClient.interactionManager.clickSlot(class_17032.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)SwordFarmMode.minecraftClient.player);
            break;
        }
        this.actionCooldown.reset();
    }

    private int countMatchingInventoryItems(ScreenHandler class_17032) {
        int n = 0;
        for (Slot class_17352 : class_17032.slots) {
            if (class_17352.inventory != SwordFarmMode.minecraftClient.player.getInventory() || !SwordFarmMode.isSwordItem(class_17352.getStack())) continue;
            n += class_17352.getStack().getCount();
        }
        return n;
    }

    private int countEmptyStorageSlots(ScreenHandler class_17032) {
        int n = 0;
        for (Slot class_17352 : class_17032.slots) {
            if (class_17352.inventory == SwordFarmMode.minecraftClient.player.getInventory() || !class_17352.getStack().isEmpty()) continue;
            ++n;
        }
        return n;
    }

    private void selectSaleConfirmation() {
        if (!this.actionCooldown.hasElapsed(350L)) {
            return;
        }
        ScreenHandler class_17032 = this.getOpenContainerScreen();
        if (class_17032 == null) {
            this.transitionSaleState(SaleWorkflowState.PROCESSING_STORAGE_SCREEN);
            return;
        }
        int n = SwordFarmMode.findContainerSlot(class_17032, class_17992 -> class_17992.getItem() == Items.LIME_DYE);
        if (n == -1) {
            if (this.responseCooldown.hasElapsed(5000L)) {
                this.showError("modules.sword_farm.no_lime_dye");
                this.disableAutoFarm();
            }
            return;
        }
        SwordFarmMode.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)SwordFarmMode.minecraftClient.player);
        this.saleActionActive = false;
        this.saleCooldown.reset();
        this.closeCurrentScreen();
        this.transitionSaleState(SaleWorkflowState.CLOSING_STORAGE_SCREEN);
    }

    private void closeSaleScreen() {
        if (this.saleActionActive) {
            this.saleActionActive = false;
            this.transitionSaleState(SaleWorkflowState.CHECKING_MATERIALS);
            return;
        }
        if (this.saleCooldown.hasElapsed((long)this.relistCooldownSetting.getValue() * 1000L)) {
            this.transitionSaleState(SaleWorkflowState.SUBMITTING_STORAGE_ACTION);
        }
    }

    private void submitSaleCommand() {
        if (!this.actionCooldown.hasElapsed(350L)) {
            return;
        }
        this.closeCurrentScreen();
        SwordFarmMode.minecraftClient.player.networkHandler.sendChatCommand("ah");
        this.transitionSaleState(SaleWorkflowState.PROCESSING_STORAGE_RESULT);
    }

    private void collectSaleConfirmation() {
        if (!this.actionCooldown.hasElapsed(650L)) {
            return;
        }
        ScreenHandler class_17032 = this.getOpenContainerScreen();
        if (class_17032 == null) {
            return;
        }
        int n = SwordFarmMode.findContainerSlotByName(class_17032, STORAGE_TITLE);
        if (n == -1) {
            if (this.responseCooldown.hasElapsed(5000L)) {
                this.showError("modules.sword_farm.no_storage");
                this.disableAutoFarm();
            }
            return;
        }
        SwordFarmMode.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)SwordFarmMode.minecraftClient.player);
        this.transitionSaleState(SaleWorkflowState.FINISHING_STORAGE_CYCLE);
    }

    private void collectSaleProceeds() {
        if (!this.actionCooldown.hasElapsed(350L)) {
            return;
        }
        ScreenHandler class_17032 = this.getOpenContainerScreen();
        if (class_17032 == null) {
            return;
        }
        int n = this.countStorageItems(class_17032);
        if (n == 0) {
            this.transitionSaleState(SaleWorkflowState.COMPLETING_SALE);
            return;
        }
        if (this.containerItemCount != -1 && n >= this.containerItemCount) {
            ++this.saleRetryCount;
            if (this.saleRetryCount >= 3) {
                this.transitionSaleState(SaleWorkflowState.COMPLETING_SALE);
                return;
            }
        } else {
            this.saleRetryCount = 0;
        }
        this.containerItemCount = n;
        for (int i = 0; i < class_17032.slots.size(); ++i) {
            Slot class_17352 = (Slot)class_17032.slots.get(i);
            if (class_17352.inventory == SwordFarmMode.minecraftClient.player.getInventory() || !SwordFarmMode.isSwordItem(class_17352.getStack())) continue;
            SwordFarmMode.minecraftClient.interactionManager.clickSlot(class_17032.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)SwordFarmMode.minecraftClient.player);
            break;
        }
        this.actionCooldown.reset();
    }

    private int countStorageItems(ScreenHandler class_17032) {
        int n = 0;
        for (Slot class_17352 : class_17032.slots) {
            if (class_17352.inventory == SwordFarmMode.minecraftClient.player.getInventory() || !SwordFarmMode.isSwordItem(class_17352.getStack())) continue;
            n += class_17352.getStack().getCount();
        }
        return n;
    }

    private void setSaleWorkflowState(SaleWorkflowState saleWorkflowState) {
        if (!this.actionCooldown.hasElapsed(350L)) {
            return;
        }
        this.closeCurrentScreen();
        this.transitionSaleState(saleWorkflowState);
    }

    private void closeCurrentScreen() {
        if (SwordFarmMode.minecraftClient.player == null) {
            return;
        }
        if (SwordFarmMode.minecraftClient.player.currentScreenHandler != null && SwordFarmMode.minecraftClient.player.currentScreenHandler != SwordFarmMode.minecraftClient.player.playerScreenHandler) {
            SwordFarmMode.minecraftClient.player.networkHandler.sendPacket((Packet)new CloseHandledScreenC2SPacket(SwordFarmMode.minecraftClient.player.currentScreenHandler.syncId));
        }
        SwordFarmMode.minecraftClient.player.closeHandledScreen();
    }

    private ScreenHandler getOpenContainerScreen() {
        if (!(SwordFarmMode.minecraftClient.currentScreen instanceof HandledScreen)) {
            return null;
        }
        if (SwordFarmMode.minecraftClient.player == null) {
            return null;
        }
        ScreenHandler class_17032 = SwordFarmMode.minecraftClient.player.currentScreenHandler;
        if (class_17032 == SwordFarmMode.minecraftClient.player.playerScreenHandler) {
            return null;
        }
        return class_17032;
    }

    private boolean isRotationAligned(Vec3d VanillaChestLootTableGenerator) {
        Rotation rotation = AimRotationMath.getRotationToPoint(VanillaChestLootTableGenerator);
        if (this.targetRotation == null || this.targetRotation.angleDistanceTo(rotation) > 0.5f) {
            this.targetRotation = rotation;
            this.rotationAttemptCount = 0;
        }
        RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, 180.0f, 180.0f, 180.0f, RotationPriority.ITEM_USE_PRIORITY);
        Rotation rotation2 = RockstarClient.create().getRotationManager().getCurrentRotation();
        if (rotation2 != null && rotation2.angleDistanceTo(rotation) <= 1.5f) {
            ++this.rotationAttemptCount;
            return this.rotationAttemptCount >= 1;
        }
        return false;
    }

    private void transitionSaleState(SaleWorkflowState saleWorkflowState) {
        this.saleWorkflowState = saleWorkflowState;
        this.actionCooldown.reset();
        this.responseCooldown.reset();
        this.targetRotation = null;
        this.rotationAttemptCount = 0;
        this.containerItemCount = -1;
        this.saleRetryCount = 0;
    }

    private boolean isItemAvailable(Item class_17922) {
        return ItemRuleSets.getHotbarRules().containsItem(class_17922) || ItemRuleSets.getInventoryRules().containsItem(class_17922);
    }

    private int countAvailableSaleItems() {
        int n = 0;
        for (InventorySlotRule itemRule : ItemRuleSets.getInventoryRules().getRules()) {
            if (!SwordFarmMode.isSwordItem(itemRule.getItemStack())) continue;
            ++n;
        }
        for (HotbarSlot hotbarSlot : ItemRuleSets.getHotbarRules().getRules()) {
            if (!SwordFarmMode.isSwordItem(hotbarSlot.getItemStack())) continue;
            ++n;
        }
        return n;
    }

    private static boolean isSwordItem(ItemStack class_17992) {
        if (class_17992.isEmpty()) {
            return false;
        }
        return SwordFarmMode.isEmeraldSwordItem(class_17992) || SwordFarmMode.isEnchantedSwordItem(class_17992);
    }

    private static boolean isEmeraldSwordItem(ItemStack class_17992) {
        return class_17992.getName().getString().contains(EMERALD_SWORD_LABEL);
    }

    private static boolean isEnchantedSwordItem(ItemStack class_17992) {
        return class_17992.getItem() == Items.DIAMOND_SWORD && EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.SHARPNESS) == 3;
    }

    private static int findContainerSlot(ScreenHandler class_17032, Predicate<ItemStack> predicate) {
        for (int i = 0; i < class_17032.slots.size(); ++i) {
            Slot class_17352 = (Slot)class_17032.slots.get(i);
            if (class_17352.inventory == MinecraftClient.getInstance().player.getInventory() || !predicate.test(class_17352.getStack())) continue;
            return i;
        }
        return -1;
    }

    private static int findContainerSlotByName(ScreenHandler class_17032, String string) {
        for (int i = 0; i < class_17032.slots.size(); ++i) {
            ItemStack class_17992;
            Slot class_17352 = (Slot)class_17032.slots.get(i);
            if (class_17352.inventory == MinecraftClient.getInstance().player.getInventory() || (class_17992 = class_17352.getStack()).isEmpty() || !class_17992.getName().getString().contains(string)) continue;
            return i;
        }
        return -1;
    }

    private boolean isCraftingTableBlock(BlockPos adminsky) {
        return SwordFarmMode.minecraftClient.world.getBlockState(adminsky).getBlock() == Blocks.CRAFTING_TABLE;
    }

    private boolean isCraftingTableWithinRange(BlockPos adminsky) {
        double d = SwordFarmMode.minecraftClient.player.getBlockInteractionRange() + 0.5;
        return SwordFarmMode.minecraftClient.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky)) <= d * d;
    }

    private BlockPos findNearestCraftingTable() {
        BlockPos adminsky = SwordFarmMode.minecraftClient.player.getBlockPos();
        Vec3d VanillaChestLootTableGenerator = SwordFarmMode.minecraftClient.player.getEyePos();
        double d = SwordFarmMode.minecraftClient.player.getBlockInteractionRange();
        double d2 = d * d;
        BlockPos adminsky2 = null;
        double d3 = Double.MAX_VALUE;
        for (int i = -6; i <= 6; ++i) {
            for (int j = -6; j <= 6; ++j) {
                for (int k = -6; k <= 6; ++k) {
                    double d4;
                    BlockPos adminsky3 = adminsky.add(i, j, k);
                    if (SwordFarmMode.minecraftClient.world.getBlockState(adminsky3).getBlock() != Blocks.CRAFTING_TABLE || (d4 = VanillaChestLootTableGenerator.squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky3))) > d2 || !(d4 < d3)) continue;
                    d3 = d4;
                    adminsky2 = adminsky3;
                }
            }
        }
        return adminsky2;
    }

    static enum SaleWorkflowState {
        CHECKING_MATERIALS,
        OPENING_SHOP_MENU,
        PURCHASING_BASE_ITEM,
        PURCHASING_UPGRADE_ITEM,
        SELECTING_CRAFTING_RECIPE,
        WAITING_FOR_RECIPE_RESULT,
        CRAFTING_BASE_ITEM,
        CRAFTING_UPGRADE_ITEM,
        PREPARING_SALE,
        OPENING_STORAGE,
        PROCESSING_STORAGE_SCREEN,
        SELECTING_STORAGE_ITEM,
        PROCESSING_STORAGE_ACTION,
        CLOSING_STORAGE_SCREEN,
        SUBMITTING_STORAGE_ACTION,
        PROCESSING_STORAGE_RESULT,
        FINISHING_STORAGE_CYCLE,
        COMPLETING_SALE;
}
}

