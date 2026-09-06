/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.StatusEffects
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ScreenHandler
 *  net.minecraft.AnvilScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.PotionItem
 *  net.minecraft.ItemConvertible
 *  net.minecraft.AnvilBlock
 *  net.minecraft.Direction
 *  net.minecraft.Position
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.RenameItemC2SPacket
 *  net.minecraft.PlayerInteractItemC2SPacket
 *  net.minecraft.BlockHitResult
 *  net.minecraft.Screen
 *  net.minecraft.AnvilScreen
 *  net.minecraft.RegistryEntry
 */
package moscow.rockstar.modules.player.farming.items;

import java.util.function.Predicate;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.recipes.RecipeItemResolver;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.Rotation;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.player.farming.core.AutoFarm;
import moscow.rockstar.modules.player.farming.core.FarmModeBase;
import moscow.rockstar.modules.player.farming.core.FarmState;
import moscow.rockstar.modules.player.farming.hud.FarmDisplayCategory;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.block.AnvilBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.registry.entry.RegistryEntry;

public class ItemFarmMode
extends FarmModeBase {
    private final ModeSetting potionModeSetting;
    private final ModeSetting.Option strengthPotionOption;
    private final ModeSetting.Option speedPotionOption;
    private final ModeSetting.Option strengthSpeedPotionOption;
    private final BooleanSetting autoOpenBrewingSetting;
    private final BooleanSetting autoExperienceSetting;
    private final NumberSetting refillLevelSetting;
    private static final int MAX_PROGRESS_MARKER_LENGTH = 50;
    private static final int MIN_EXPERIENCE_LEVEL = 5;
    private final Timer inventoryActionTimer = new Timer();
    private final Timer brewingActionTimer = new Timer();
    private final Timer autoOpenTimer = new Timer();
    private String progressMarker = "";
    private boolean brewingActive;
    private int brewingFuelLevel;

    public ItemFarmMode(AutoFarm autoFarm, ModeSetting modeSetting) {
        super(autoFarm, modeSetting, "modules.settings.auto_farm.modes.potion_combiner");
        this.potionModeSetting = new ModeSetting((SettingOwner)autoFarm, "modules.settings.potion_combiner.potions", () -> !this.isSelected());
        this.strengthPotionOption = new ModeSetting.Option(this.potionModeSetting, "modules.settings.potion_combiner.potion.strength").select();
        this.speedPotionOption = new ModeSetting.Option(this.potionModeSetting, "modules.settings.potion_combiner.potion.speed");
        this.strengthSpeedPotionOption = new ModeSetting.Option(this.potionModeSetting, "modules.settings.potion_combiner.potion.strength_speed");
        this.autoOpenBrewingSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.potion_combiner.auto_open", () -> !this.isSelected()).enable();
        this.autoExperienceSetting = new BooleanSetting((SettingOwner)autoFarm, "modules.settings.potion_combiner.auto_exp", () -> !this.isSelected());
        this.refillLevelSetting = new NumberSetting((SettingOwner)autoFarm, "modules.settings.potion_combiner.refill_to", () -> !this.isSelected() || !this.autoExperienceSetting.isEnabled()).setMinValue(5.0f).setMaxValue(100.0f).setStep(1.0f).setValue(40.0f);
    }

    @Override
    public void resetBrewingState() {
        this.progressMarker = "";
        this.brewingActive = false;
        this.brewingFuelLevel = 0;
    }

    @Override
    public void onModeSelected() {
        if (ItemFarmMode.minecraftClient.player == null || ItemFarmMode.minecraftClient.world == null || ItemFarmMode.minecraftClient.interactionManager == null) {
            return;
        }
        this.processBrewingInventory();
        this.openNearbyBrewingStand();
        this.processBrewingCycle();
    }

    @Override
    public FarmState getFarmState() {
        if (this.brewingActive) {
            return FarmState.REPAIRING;
        }
        return ItemFarmMode.minecraftClient.currentScreen instanceof AnvilScreen ? FarmState.CRAFTING : FarmState.IDLE;
    }

    @Override
    public FarmDisplayCategory getFarmDisplayCategory() {
        return FarmDisplayCategory.POTIONS;
    }

    @Override
    public ItemStack getFarmDisplayItem() {
        return new ItemStack((ItemConvertible)Items.ANVIL);
    }

    private void processBrewingInventory() {
        int n;
        if (this.brewingActive) {
            return;
        }
        Screen currentScreen = ItemFarmMode.minecraftClient.currentScreen;
        if (!(currentScreen instanceof AnvilScreen anvilScreen)) {
            this.progressMarker = "";
            return;
        }
        AnvilScreenHandler anvilHandler = anvilScreen.getScreenHandler();
        this.fillPotionSlots(anvilHandler);
        if (!this.hasPotionInputs(anvilHandler)) {
            return;
        }
        this.brewingFuelLevel = n = anvilHandler.getLevelCost();
        this.sendBrewingProgressMarker();
        if (n > 0 && ItemFarmMode.minecraftClient.player.experienceLevel >= n && !anvilHandler.getSlot(2).getStack().isEmpty()) {
            this.collectBrewedPotion(anvilHandler);
        }
    }

    private void openNearbyBrewingStand() {
        if (!this.autoOpenBrewingSetting.isEnabled()) {
            return;
        }
        if (this.brewingActive) {
            return;
        }
        if (ItemFarmMode.minecraftClient.currentScreen != null) {
            return;
        }
        if (!this.autoOpenTimer.hasElapsed(500L)) {
            return;
        }
        if (!this.hasRequiredPotionIngredients()) {
            return;
        }
        BlockPos adminsky = this.findNearbyBrewingStand();
        if (adminsky == null) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = new Vec3d((double)adminsky.getX() + 0.5, (double)adminsky.getY() + 0.5, (double)adminsky.getZ() + 0.5);
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky, false);
        ItemFarmMode.minecraftClient.interactionManager.interactBlock(ItemFarmMode.minecraftClient.player, Hand.MAIN_HAND, class_39652);
        this.autoOpenTimer.reset();
    }

    private boolean hasRequiredPotionIngredients() {
        if (this.strengthSpeedPotionOption.isSelected()) {
            boolean bl = false;
            boolean bl2 = false;
            for (int i = 0; i < ItemFarmMode.minecraftClient.player.getInventory().size(); ++i) {
                ItemStack class_17992 = ItemFarmMode.minecraftClient.player.getInventory().getStack(i);
                if (this.matchesPotionIngredient(class_17992, 0)) {
                    bl = true;
                }
                if (this.matchesPotionIngredient(class_17992, 1)) {
                    bl2 = true;
                }
                if (!bl || !bl2) continue;
                return true;
            }
            return false;
        }
        for (int i = 0; i < ItemFarmMode.minecraftClient.player.getInventory().size(); ++i) {
            if (!this.matchesPotionIngredient(ItemFarmMode.minecraftClient.player.getInventory().getStack(i), 0)) continue;
            return true;
        }
        return false;
    }

    private BlockPos findNearbyBrewingStand() {
        BlockPos adminsky = BlockPos.ofFloored((Position)ItemFarmMode.minecraftClient.player.getPos());
        BlockPos adminsky2 = null;
        double d = Double.MAX_VALUE;
        double d2 = ItemFarmMode.minecraftClient.player.getBlockInteractionRange();
        for (int i = -5; i <= 5; ++i) {
            for (int j = -5; j <= 5; ++j) {
                for (int k = -5; k <= 5; ++k) {
                    double d3;
                    BlockPos adminsky3 = adminsky.add(i, j, k);
                    if (!(ItemFarmMode.minecraftClient.world.getBlockState(adminsky3).getBlock() instanceof AnvilBlock) || (d3 = ItemFarmMode.minecraftClient.player.getEyePos().squaredDistanceTo((double)adminsky3.getX() + 0.5, (double)adminsky3.getY() + 0.5, (double)adminsky3.getZ() + 0.5)) > d2 * d2 || !(d3 < d)) continue;
                    d = d3;
                    adminsky2 = adminsky3;
                }
            }
        }
        return adminsky2;
    }

    private void processBrewingCycle() {
        if (!this.autoExperienceSetting.isEnabled()) {
            this.brewingActive = false;
            return;
        }
        int n2 = Math.max((int)this.refillLevelSetting.getValue(), this.brewingFuelLevel);
        int n3 = Math.max(5, this.brewingFuelLevel);
        if (!this.brewingActive && ItemFarmMode.minecraftClient.player.experienceLevel < n3 && this.findExperienceBottle() != null) {
            this.brewingActive = true;
        }
        if (!this.brewingActive) {
            return;
        }
        if (ItemFarmMode.minecraftClient.currentScreen instanceof AnvilScreen) {
            ItemFarmMode.minecraftClient.player.closeHandledScreen();
            return;
        }
        if (ItemFarmMode.minecraftClient.player.getMainHandStack().getItem() != Items.EXPERIENCE_BOTTLE) {
            ItemRule itemRule = this.findExperienceBottle();
            if (itemRule == null) {
                this.brewingActive = false;
                return;
            }
            int n4 = ItemFarmMode.minecraftClient.player.getInventory().selectedSlot;
            if (itemRule instanceof HotbarSlot) {
                HotbarSlot hotbarSlot = (HotbarSlot)itemRule;
                if (hotbarSlot.getSlotIndex() != n4) {
                    InventoryUtils.setSelectedHotbarSlot(hotbarSlot.getSlotIndex());
                }
            } else {
                InventoryUtils.dropItem(itemRule.getClickSlot(), n4);
            }
            return;
        }
        RockstarClient.create().getRotationManager().requestRotation(new Rotation(ItemFarmMode.minecraftClient.player.getYaw(), 87.0f), RotationCorrectionMode.DIRECT, 180.0f, 180.0f, 180.0f, RotationPriority.STANDARD_PRIORITY);
        if (this.brewingActionTimer.hasElapsed(100L)) {
            ((moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)ItemFarmMode.minecraftClient.interactionManager).rockstar$sendSequencedPacket(ItemFarmMode.minecraftClient.world, n -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, n, ItemFarmMode.minecraftClient.player.getYaw(), 87.0f));
            ItemFarmMode.minecraftClient.player.swingHand(Hand.MAIN_HAND);
            this.brewingActionTimer.reset();
            if (ItemFarmMode.minecraftClient.player.experienceLevel >= n2) {
                this.brewingActive = false;
            }
        }
    }

    private boolean hasPotionInputs(AnvilScreenHandler class_17062) {
        return this.matchesPotionIngredient(class_17062.getSlot(0).getStack(), 0) && this.matchesPotionIngredient(class_17062.getSlot(1).getStack(), 1);
    }

    private void fillPotionSlots(AnvilScreenHandler class_17062) {
        if (!this.inventoryActionTimer.hasElapsed(300L)) {
            return;
        }
        for (int i = 0; i < 2; ++i) {
            if (this.matchesPotionIngredient(class_17062.getSlot(i).getStack(), i)) continue;
            if (!class_17062.getSlot(i).getStack().isEmpty()) {
                ItemFarmMode.minecraftClient.interactionManager.clickSlot(class_17062.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)ItemFarmMode.minecraftClient.player);
                this.inventoryActionTimer.reset();
                return;
            }
            int n = this.findPotionIngredientSlot((ScreenHandler)class_17062, i);
            if (n == -1) {
                return;
            }
            this.moveIngredientToPotionSlot((ScreenHandler)class_17062, n, i);
            this.inventoryActionTimer.reset();
            return;
        }
    }

    private int findPotionIngredientSlot(ScreenHandler class_17032, int n) {
        for (int i = 3; i < class_17032.slots.size(); ++i) {
            ItemStack class_17992 = class_17032.getSlot(i).getStack();
            if (!this.matchesPotionIngredient(class_17992, n)) continue;
            return i;
        }
        return -1;
    }

    private void moveIngredientToPotionSlot(ScreenHandler class_17032, int n, int n2) {
        ItemFarmMode.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)ItemFarmMode.minecraftClient.player);
        ItemFarmMode.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n2, 1, SlotActionType.PICKUP, (PlayerEntity)ItemFarmMode.minecraftClient.player);
        if (!ItemFarmMode.minecraftClient.player.currentScreenHandler.getCursorStack().isEmpty()) {
            ItemFarmMode.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)ItemFarmMode.minecraftClient.player);
        }
    }

    private void collectBrewedPotion(AnvilScreenHandler class_17062) {
        if (!this.inventoryActionTimer.hasElapsed(120L)) {
            return;
        }
        if (class_17062.getSlot(2).getStack().isEmpty()) {
            return;
        }
        int n = Math.max(1, class_17062.getSlot(2).getStack().getCount());
        ItemFarmMode.minecraftClient.interactionManager.clickSlot(class_17062.syncId, 2, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)ItemFarmMode.minecraftClient.player);
        this.getFarmMetrics().addBlockCount(n);
        this.progressMarker = "";
        this.inventoryActionTimer.reset();
    }

    private void sendBrewingProgressMarker() {
        if (this.progressMarker.length() >= 50) {
            this.progressMarker = "";
        }
        this.progressMarker = this.progressMarker + "!";
        if (minecraftClient.getNetworkHandler() != null) {
            minecraftClient.getNetworkHandler().sendPacket((Packet)new RenameItemC2SPacket(this.progressMarker));
        }
    }

    private boolean matchesPotionIngredient(ItemStack class_17992, int n) {
        if (class_17992.isEmpty() || !(class_17992.getItem() instanceof PotionItem)) {
            return false;
        }
        for (StatusEffectInstance class_12932 : RecipeItemResolver.getEffects(class_17992)) {
            RegistryEntry class_68802 = class_12932.getEffectType();
            int n2 = class_12932.getAmplifier();
            if (this.strengthSpeedPotionOption.isSelected()) {
                if (n2 != 2) continue;
                if (n == 0 && class_68802 == StatusEffects.STRENGTH) {
                    return true;
                }
                if (n != 1 || class_68802 != StatusEffects.SPEED) continue;
                return true;
            }
            if (n2 >= 2) continue;
            if (this.strengthPotionOption.isSelected() && class_68802 == StatusEffects.STRENGTH) {
                return true;
            }
            if (!this.speedPotionOption.isSelected() || class_68802 != StatusEffects.SPEED) continue;
            return true;
        }
        return false;
    }

    private ItemRule findExperienceBottle() {
        Predicate<ItemStack> predicate = class_17992 -> class_17992.getItem() == Items.EXPERIENCE_BOTTLE;
        HotbarSlot hotbarSlot = ItemRuleSets.getHotbarRules().findByStack(predicate);
        if (hotbarSlot != null) {
            return hotbarSlot;
        }
        return ItemRuleSets.getInventoryRules().findByStack(predicate);
    }
}
