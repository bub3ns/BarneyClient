/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.StatusEffect
 *  net.minecraft.StatusEffects
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.SplashPotionItem
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.RegistryEntry
 */
package moscow.rockstar.modules.combat.attacks;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.recipes.RecipeItemResolver;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationType;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.window.KeyPressEvent;
import pyrock.events.window.MouseEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Throw", category=ModuleCategory.COMBAT, description="modules.descriptions.auto_throw")
public class AutoThrow
extends Module {
    private static final int THROW_HOTBAR_SLOT = 8;
    private static final int MIN_TARGET_HEALTH = 10;
    private static final long COOLDOWN_MILLIS = 5000L;
    private ModeSetting mode;
    private ModeSetting.Option automatic;
    private ModeSetting.Option bind;
    private IntegerSetting manualTriggerKey;
    private NumberSetting health;
    private NumberSetting distance;
    private ItemRule itemRule;
    private int selectedSlot = -1;
    private int lastSlot;
    private int itemSlot = -1;
    private boolean manualTrigger;
    private final Timer cooldownTimer = new Timer();
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        PlayerEntity class_16572;
        if (AutoThrow.minecraftClient.player == null || AutoThrow.minecraftClient.world == null || AutoThrow.minecraftClient.interactionManager == null || minecraftClient.getNetworkHandler() == null) {
            return;
        }
        if (this.selectedSlot >= 0) {
            this.processThrowSteps();
            return;
        }
        if (!this.mode.isSelected(this.automatic) || !this.cooldownTimer.hasElapsed(5000L)) {
            return;
        }
        LivingEntity class_13092 = RockstarClient.create().getFriendManager().getTargetLivingEntity();
        if (!(class_13092 instanceof PlayerEntity) || !(class_16572 = (PlayerEntity)class_13092).isAlive()) {
            return;
        }
        if (!this.isTargetUsingHealingItem((LivingEntity)class_16572)) {
            return;
        }
        if (EntityUtils.getPlayerHealth(class_16572) + class_16572.getAbsorptionAmount() >= this.health.getValue()) {
            return;
        }
        if (AutoThrow.minecraftClient.player.distanceTo((Entity)class_16572) > this.distance.getValue()) {
            return;
        }
        if (!this.isTargetInThrowPath((LivingEntity)class_16572)) {
            return;
        }
        this.beginThrow(false);
    };
    private final EventListener<KeyPressEvent> keyPressListener = keyPressEvent -> {
        if (keyPressEvent.getAction() != 1 || !this.manualTriggerKey.isIntValid(keyPressEvent.getKey())) {
            return;
        }
        this.beginThrow(true);
    };
    private final EventListener<MouseEvent> mouseListener = mouseEvent -> {
        if (mouseEvent.getAction() != 1 || !this.manualTriggerKey.isIntValid(mouseEvent.getButton())) {
            return;
        }
        this.beginThrow(true);
    };

    public AutoThrow() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.mode = new ModeSetting(this, "modules.settings.auto_throw.mode");
        this.automatic = new ModeSetting.Option(this.mode, "modules.settings.auto_throw.mode.automatic").select();
        this.bind = new ModeSetting.Option(this.mode, "modules.settings.auto_throw.mode.bind");
        this.manualTriggerKey = new IntegerSetting(this, "modules.settings.auto_throw.key", () -> this.mode.isSelected(this.automatic));
        this.health = new NumberSetting((SettingOwner)this, "modules.settings.auto_throw.health", () -> this.mode.isSelected(this.bind)).setMinValue(1.0f).setMaxValue(20.0f).setStep(0.5f).setValue(15.0f);
        this.distance = new NumberSetting((SettingOwner)this, "modules.settings.auto_throw.distance", () -> this.mode.isSelected(this.bind)).setMinValue(1.0f).setMaxValue(6.0f).setStep(0.1f).setValue(3.0f);
    }

    private void beginThrow(boolean bl) {
        if (AutoThrow.minecraftClient.player == null || AutoThrow.minecraftClient.currentScreen != null || this.selectedSlot >= 0) {
            return;
        }
        if (bl != this.mode.isSelected(this.bind)) {
            return;
        }
        if (!this.isInventoryActionReady(RockstarClient.create().getModuleRegistry().getModule(InventoryMove.class))) {
            return;
        }
        ItemRule itemRule = this.findThrowableItem();
        if (itemRule == null) {
            if (bl) {
                RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.ERROR, Localization.translate("swap.item_not_found"), Localization.translateFormatted("swap.item_required", Items.SPLASH_POTION.getName().getString()));
            }
            return;
        }
        this.itemRule = itemRule;
        this.itemSlot = AutoThrow.minecraftClient.player.getInventory().selectedSlot;
        this.manualTrigger = false;
        this.lastSlot = 0;
        this.selectedSlot = itemRule instanceof HotbarSlot ? 2 : 0;
    }

    private void processThrowSteps() {
        if (this.itemRule == null || AutoThrow.minecraftClient.player.isDead() || ++this.lastSlot > 60) {
            this.clearThrowState();
            return;
        }
        InventoryMove inventoryMove = RockstarClient.create().getModuleRegistry().getModule(InventoryMove.class);
        switch (this.selectedSlot) {
            case 0: {
                InventoryUtils.dropItem(this.itemRule.getClickSlot(), 8);
                this.manualTrigger = true;
                this.selectedSlot = 1;
                break;
            }
            case 1: {
                if (!this.isInventoryActionReady(inventoryMove)) break;
                this.selectedSlot = 2;
                break;
            }
            case 2: {
                int n;
                ItemRule itemRule = this.itemRule;
                if (itemRule instanceof HotbarSlot) {
                    HotbarSlot hotbarSlot = (HotbarSlot)itemRule;
                    n = hotbarSlot.getSlotIndex();
                } else {
                    n = 8;
                }
                if (!AutoThrow.isThrowableHealingPotion(InventoryUtils.hotbarSlot(n).getItemStack())) {
                    this.clearThrowState();
                    return;
                }
                this.selectHotbarSlot(n);
                AutoThrow.minecraftClient.interactionManager.interactItem((PlayerEntity)AutoThrow.minecraftClient.player, Hand.MAIN_HAND);
                this.cooldownTimer.reset();
                this.selectedSlot = 3;
                break;
            }
            case 3: {
                if (!this.isInventoryActionReady(inventoryMove)) break;
                this.clearThrowState();
            }
        }
    }

    private void clearThrowState() {
        if (this.manualTrigger && this.itemRule != null) {
            InventoryUtils.dropItem(this.itemRule.getClickSlot(), 8);
        }
        if (this.itemSlot >= 0) {
            this.selectHotbarSlot(this.itemSlot);
        }
        this.itemRule = null;
        this.selectedSlot = -1;
        this.lastSlot = 0;
        this.itemSlot = -1;
        this.manualTrigger = false;
    }

    private boolean isInventoryActionReady(InventoryMove inventoryMove) {
        if (inventoryMove == null || !inventoryMove.isEnabled()) {
            return true;
        }
        if (inventoryMove.isFuntimeMode()) {
            return !inventoryMove.isReallyworldMode() && !inventoryMove.isInventoryOpen() && inventoryMove.isSpookytimeMode();
        }
        return inventoryMove.getModeEntries().isEmpty() && !inventoryMove.isInventoryOpen();
    }

    private void selectHotbarSlot(int n) {
        if (AutoThrow.minecraftClient.player.getInventory().selectedSlot == n) {
            return;
        }
        AutoThrow.minecraftClient.player.getInventory().selectedSlot = n;
        minecraftClient.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(n));
    }

    private ItemRule findThrowableItem() {
        HotbarSlot hotbarSlot = ItemRuleSets.getHotbarRules().findByStack(AutoThrow::isThrowableHealingPotion);
        return hotbarSlot != null ? hotbarSlot : ItemRuleSets.getInventoryRules().findByStack(AutoThrow::isThrowableHealingPotion);
    }

    private boolean isTargetUsingHealingItem(LivingEntity class_13092) {
        if (!class_13092.isUsingItem() || class_13092.getItemUseTime() < 10) {
            return false;
        }
        ItemStack class_17992 = class_13092.getActiveItem();
        return !(class_17992.getItem() instanceof SplashPotionItem) && RecipeItemResolver.containsEffect(class_17992, (RegistryEntry<StatusEffect>)StatusEffects.INSTANT_HEALTH);
    }

    private boolean isTargetInThrowPath(LivingEntity class_13092) {
        Rotation rotation = RockstarClient.create().getRotationManager().getEffectiveRotation();
        Vec3d VanillaChestLootTableGenerator = AutoThrow.minecraftClient.player.getEyePos();
        Vec3d WallPlayerSkullBlock = VanillaChestLootTableGenerator.add(MathUtils.directionFromYawPitch(rotation.getPitch(), rotation.getYaw()).multiply((double)(this.distance.getValue() + 1.0f)));
        Box HorizontalFacingBlock = class_13092.getBoundingBox().expand(0.15);
        return HorizontalFacingBlock.contains(VanillaChestLootTableGenerator) || HorizontalFacingBlock.raycast(VanillaChestLootTableGenerator, WallPlayerSkullBlock).isPresent();
    }

    private static boolean isThrowableHealingPotion(ItemStack class_17992) {
        return !class_17992.isEmpty() && class_17992.getItem() instanceof SplashPotionItem && RecipeItemResolver.containsEffect(class_17992, (RegistryEntry<StatusEffect>)StatusEffects.INSTANT_HEALTH);
    }

    @Override
    public void onEnable() {
        this.cooldownTimer.setLastResetTimeMillis(0L);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (this.selectedSlot >= 0) {
            this.clearThrowState();
        }
        super.onDisable();
    }
}
