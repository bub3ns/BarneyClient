/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.ScreenHandler
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.BrewingStandScreenHandler
 *  net.minecraft.Slot
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Potion
 *  net.minecraft.PotionContentsComponent
 *  net.minecraft.Potions
 *  net.minecraft.Direction
 *  net.minecraft.Position
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockEntity
 *  net.minecraft.BrewingStandBlockEntity
 *  net.minecraft.ChestBlockEntity
 *  net.minecraft.BlockHitResult
 *  net.minecraft.BrewingStandScreen
 *  net.minecraft.RegistryEntry
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.modules.player.automation.brewing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.player.automation.brewing.BrewingPhase;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationType;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.potion.Potions;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.DataComponentTypes;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Brew", category=ModuleCategory.PLAYER, description="modules.descriptions.auto_brew")
public class AutoBrew
extends Module {
    private ModeSetting brew;
    private ModeSetting.Option strength;
    private ModeSetting.Option speed;
    private ModeSetting.Option fireResistance;
    private ModeSetting.Option invisibility;
    private BooleanSetting enhance;
    private NumberSetting delay;
    private final Timer cooldownTimer = new Timer();
    private BrewingPhase brewType = BrewingPhase.FIND_BREWING_STAND;
    private final Timer actionTimer = new Timer();
    private BrewingStandBlockEntity brewingStand;
    private ChestBlockEntity brewingInventory;
    private final List<BlockPos> brewSettings = new ArrayList<BlockPos>();
    private List<BrewingStandBlockEntity> brewItems = new ArrayList<BrewingStandBlockEntity>();
    private static final Item[] brewableItems = new Item[]{Items.NETHER_WART, Items.BLAZE_POWDER, Items.SUGAR, Items.MAGMA_CREAM, Items.GLOWSTONE_DUST, Items.REDSTONE, Items.GOLDEN_CARROT, Items.FERMENTED_SPIDER_EYE};
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        switch (this.brewType.ordinal()) {
            case 0: {
                this.prepareBrewing();
                break;
            }
            case 1: {
                this.updateBrewState();
                break;
            }
            case 2: {
                this.loadBrewingIngredients();
                break;
            }
            case 3: {
                this.resetBrew();
                break;
            }
            case 4: {
                this.resetBrewingInventory();
            }
        }
    };

    public AutoBrew() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.brew = new ModeSetting(this, "modules.settings.auto_brew.brew");
        this.strength = new ModeSetting.Option(this.brew, "modules.settings.auto_brew.potion.strength").select();
        this.speed = new ModeSetting.Option(this.brew, "modules.settings.auto_brew.potion.speed");
        this.fireResistance = new ModeSetting.Option(this.brew, "modules.settings.auto_brew.potion.fire_resistance");
        this.invisibility = new ModeSetting.Option(this.brew, "modules.settings.auto_brew.potion.invisibility");
        this.enhance = new BooleanSetting((SettingOwner)this, "modules.settings.auto_brew.enhance", () -> !this.invisibility.isSelected());
        this.delay = new NumberSetting((SettingOwner)this, "modules.settings.auto_brew.delay", "modules.settings.auto_brew.delay.description").setStep(10.0f).setMinValue(100.0f).setMaxValue(1000.0f).setValue(100.0f);
    }

    private void updateBrewState() {
        if (AutoBrew.minecraftClient.currentScreen instanceof BrewingStandScreen) {
            this.brewType = BrewingPhase.LOAD_INGREDIENTS;
            return;
        }
        if (this.actionTimer.hasElapsed(500L)) {
            BlockPos adminsky = this.brewingStand.getPos();
            Vec3d VanillaChestLootTableGenerator = new Vec3d((double)adminsky.getX() + 0.5, (double)adminsky.getY() + 0.5, (double)adminsky.getZ() + 0.5);
            BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky, false);
            AutoBrew.minecraftClient.interactionManager.interactBlock(AutoBrew.minecraftClient.player, Hand.MAIN_HAND, class_39652);
            this.actionTimer.reset();
        }
    }

    private void loadBrewingIngredients() {
        ScreenHandler class_17032 = AutoBrew.minecraftClient.player.currentScreenHandler;
        if (!(class_17032 instanceof BrewingStandScreenHandler)) {
            this.brewType = BrewingPhase.FIND_BREWING_STAND;
            return;
        }
        BrewingStandScreenHandler class_17082 = (BrewingStandScreenHandler)class_17032;
        if (class_17082.getFuel() > 0 && class_17082.getSlot(3).getStack().getItem() != Items.AIR) {
            return;
        }
        if (class_17082.getSlot(4).getStack().getItem() == Items.AIR && class_17082.getFuel() == 0) {
            if (this.getIngredientCount(Items.BLAZE_POWDER) == -1) {
                return;
            }
            this.addBrewingIngredient(Items.BLAZE_POWDER, 4);
        }
        for (int i = 0; i < 3; ++i) {
            if (class_17082.getSlot(i).getStack().getItem() != Items.AIR) continue;
            if (this.getEmptyBrewingSlot(class_17082) == -1) {
                return;
            }
            InventoryUtils.quickMoveItem(this.getEmptyBrewingSlot(class_17082));
        }
        if (class_17082.getSlot(3).getStack().getItem() == Items.AIR) {
            if (this.isBrewingRecipeSelected(class_17082, Potions.WATER.value())) {
                if (this.getIngredientCount(Items.NETHER_WART) == -1) {
                    RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.ERROR, Localization.translate("autobrew.item_not_found"), Localization.translateFormatted("autobrew.need_item", Items.NETHER_WART.getName().getString()));
                }
                this.selectBrewItem(Items.NETHER_WART, 3);
            }
            if (this.strength.isSelected() && this.isBrewingRecipeSelected(class_17082, Potions.AWKWARD.value())) {
                this.selectBrewItem(Items.BLAZE_POWDER, 3);
            } else if (this.speed.isSelected() && this.isBrewingRecipeSelected(class_17082, Potions.AWKWARD.value())) {
                this.selectBrewItem(Items.SUGAR, 3);
            } else if (this.fireResistance.isSelected() && this.isBrewingRecipeSelected(class_17082, Potions.AWKWARD.value())) {
                this.selectBrewItem(Items.MAGMA_CREAM, 3);
            } else if (this.invisibility.isSelected() && this.isBrewingRecipeSelected(class_17082, Potions.AWKWARD.value())) {
                this.selectBrewItem(Items.GOLDEN_CARROT, 3);
            }
            if (this.isBrewingRecipeSelected(class_17082, Potions.STRENGTH.value()) || this.isBrewingRecipeSelected(class_17082, Potions.SWIFTNESS.value())) {
                this.selectBrewItem(Items.GLOWSTONE_DUST, 3);
            }
            if (this.isBrewingRecipeSelected(class_17082, Potions.FIRE_RESISTANCE.value())) {
                this.selectBrewItem(Items.REDSTONE, 3);
            }
            if (this.invisibility.isSelected() && this.isBrewingRecipeSelected(class_17082, Potions.NIGHT_VISION.value())) {
                this.selectBrewItem(Items.FERMENTED_SPIDER_EYE, 3);
            }
            if (this.invisibility.isSelected() && this.isBrewingRecipeSelected(class_17082, Potions.INVISIBILITY.value()) && this.enhance.isEnabled()) {
                this.selectBrewItem(Items.REDSTONE, 3);
            }
            if (this.isBrewingRecipeSelected(class_17082, Potions.STRONG_STRENGTH.value()) || this.isBrewingRecipeSelected(class_17082, Potions.STRONG_SWIFTNESS.value()) || this.isBrewingRecipeSelected(class_17082, Potions.LONG_FIRE_RESISTANCE.value()) || this.invisibility.isSelected() && this.isBrewingRecipeSelected(class_17082, Potions.INVISIBILITY.value()) || this.invisibility.isSelected() && this.isBrewingRecipeSelected(class_17082, Potions.LONG_INVISIBILITY.value()) && this.enhance.isEnabled()) {
                this.readBrewingScreen(class_17082);
                this.brewType = BrewingPhase.BREW_POTIONS;
                this.actionTimer.reset();
            }
        }
    }

    private void prepareBrewing() {
        if (this.actionTimer.hasElapsed(1000L)) {
            if (this.brewItems.isEmpty()) {
                this.brewItems = this.collectPotionItems();
            }
            if (!this.brewItems.isEmpty()) {
                this.brewingStand = this.brewItems.removeFirst();
                this.brewType = BrewingPhase.OPEN_BREWING_STAND;
                this.actionTimer.reset();
            }
        }
    }

    private void selectBrewItem(Item class_17922, int n) {
        if (this.getIngredientCount(class_17922) == -1) {
            RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.ERROR, Localization.translate("autobrew.item_not_found"), Localization.translateFormatted("autobrew.need_item", class_17922.getName().getString()));
            this.toggle();
            return;
        }
        this.addBrewingIngredient(class_17922, n);
        AutoBrew.minecraftClient.player.closeHandledScreen();
    }

    private void resetBrew() {
        if (AutoBrew.minecraftClient.player.currentScreenHandler instanceof BrewingStandScreenHandler) {
            if (this.actionTimer.hasElapsed(200L)) {
                AutoBrew.minecraftClient.player.closeHandledScreen();
                this.actionTimer.reset();
            }
            return;
        }
        if (this.brewingInventory == null) {
            List<ChestBlockEntity> list = this.collectBrewingItems();
            if (!list.isEmpty()) {
                this.brewingInventory = list.getFirst();
            } else {
                this.brewType = BrewingPhase.CLOSE_BREWING_STAND;
                this.actionTimer.reset();
                return;
            }
        }
        if (!(AutoBrew.minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
            if (this.actionTimer.hasElapsed(500L)) {
                this.readBrewingInventory(this.brewingInventory);
                this.actionTimer.reset();
            }
            return;
        }
        if (!this.actionTimer.hasElapsed(200L)) {
            return;
        }
        this.finishBrewing();
        this.brewType = BrewingPhase.CLOSE_BREWING_STAND;
        this.actionTimer.reset();
    }

    private void resetBrewingInventory() {
        if (this.actionTimer.hasElapsed(500L)) {
            AutoBrew.minecraftClient.player.closeHandledScreen();
            if (this.brewingStand != null) {
                this.brewSettings.add(this.brewingStand.getPos());
            }
            this.brewType = BrewingPhase.FIND_BREWING_STAND;
            this.brewingStand = null;
            this.brewingInventory = null;
            this.actionTimer.reset();
        }
    }

    private List<BrewingStandBlockEntity> collectPotionItems() {
        ArrayList<BrewingStandBlockEntity> arrayList = new ArrayList<BrewingStandBlockEntity>();
        int n = 10;
        BlockPos adminsky = BlockPos.ofFloored((Position)AutoBrew.minecraftClient.player.getPos());
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockPos adminsky2 = adminsky.add(i, j, k);
                    BlockEntity class_25862 = AutoBrew.minecraftClient.world.getBlockEntity(adminsky2);
                    if (!(class_25862 instanceof BrewingStandBlockEntity)) continue;
                    BrewingStandBlockEntity class_25892 = (BrewingStandBlockEntity)class_25862;
                    arrayList.add(class_25892);
                }
            }
        }
        return arrayList;
    }

    private List<ChestBlockEntity> collectBrewingItems() {
        ArrayList<ChestBlockEntity> arrayList = new ArrayList<ChestBlockEntity>();
        int n = 10;
        BlockPos adminsky = BlockPos.ofFloored((Position)AutoBrew.minecraftClient.player.getPos());
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockPos adminsky2 = adminsky.add(i, j, k);
                    BlockEntity class_25862 = AutoBrew.minecraftClient.world.getBlockEntity(adminsky2);
                    if (!(class_25862 instanceof ChestBlockEntity)) continue;
                    ChestBlockEntity class_25953 = (ChestBlockEntity)class_25862;
                    arrayList.add(class_25953);
                }
            }
        }
        arrayList.sort(Comparator.comparingDouble(class_25952 -> class_25952.getPos().getSquaredDistance((Vec3i)adminsky)));
        return arrayList;
    }

    private void finishBrewing() {
        ScreenHandler class_17032 = AutoBrew.minecraftClient.player.currentScreenHandler;
        if (!(class_17032 instanceof GenericContainerScreenHandler)) {
            return;
        }
        GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)class_17032;
        java.util.List<Slot> containerSlots = class_17072.slots;
        for (int i = 0; i < containerSlots.size(); ++i) {
            ItemStack class_17992;
            Slot class_17352 = containerSlots.get(i);
            if (class_17352.inventory != AutoBrew.minecraftClient.player.getInventory() || (class_17992 = class_17352.getStack()).isEmpty() || !this.isBrewItemValid(class_17992) && !this.isIngredientValid(class_17992)) continue;
            InventoryUtils.quickMoveItem(i);
        }
    }

    private boolean isBrewItemValid(ItemStack class_17992) {
        return class_17992.getItem() == Items.POTION || class_17992.getItem() == Items.SPLASH_POTION || class_17992.getItem() == Items.LINGERING_POTION;
    }

    private boolean isIngredientValid(ItemStack class_17992) {
        Item class_17922 = class_17992.getItem();
        for (Item class_17923 : brewableItems) {
            if (class_17922 != class_17923) continue;
            return true;
        }
        return false;
    }

    private void readBrewingScreen(BrewingStandScreenHandler class_17082) {
        for (int i = 0; i < 3; ++i) {
            if (class_17082.getSlot(i).getStack().isEmpty()) continue;
            InventoryUtils.quickMoveItem(i);
        }
    }

    private void readBrewingInventory(ChestBlockEntity class_25952) {
        BlockPos adminsky = class_25952.getPos();
        Vec3d VanillaChestLootTableGenerator = new Vec3d((double)adminsky.getX() + 0.5, (double)adminsky.getY() + 0.5, (double)adminsky.getZ() + 0.5);
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky, false);
        AutoBrew.minecraftClient.interactionManager.interactBlock(AutoBrew.minecraftClient.player, Hand.MAIN_HAND, class_39652);
    }

    private void addBrewingIngredient(Item class_17922, int n) {
        int n2;
        if (this.cooldownTimer.hasElapsed((long)(this.delay.getValue() * 2.0f)) && (n2 = this.getIngredientCount(class_17922)) != -1) {
            InventoryUtils.swapWithPutback(n2, n);
            this.cooldownTimer.reset();
        }
    }

    private int getIngredientCount(Item class_17922) {
        for (int i = 5; i < 41; ++i) {
            if (((Slot)AutoBrew.minecraftClient.player.currentScreenHandler.slots.get(i)).getStack().getItem() != class_17922) continue;
            return i;
        }
        return -1;
    }

    private boolean isBrewingRecipeSelected(BrewingStandScreenHandler class_17082, Potion class_18422) {
        boolean bl = true;
        for (int i = 0; i < 3; ++i) {
            ItemStack class_17992 = ((Slot)class_17082.slots.get(i)).getStack();
            if (class_17992.getItem() != Items.POTION || ((PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS)).potion().get().value() == class_18422) continue;
            bl = false;
        }
        return bl;
    }

    private int getEmptyBrewingSlot(BrewingStandScreenHandler class_17082) {
        for (int i = 5; i < 41; ++i) {
            ItemStack class_17992 = ((Slot)class_17082.slots.get(i)).getStack();
            if (class_17992.getItem() != Items.POTION || !((PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS)).potion().isPresent() || ((PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS)).potion().get() != Potions.WATER) continue;
            return i;
        }
        return -1;
    }
}
