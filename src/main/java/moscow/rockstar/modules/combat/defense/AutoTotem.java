/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.EquipmentSlot
 *  net.minecraft.EndCrystalEntity
 *  net.minecraft.TntEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.TridentEntity
 *  net.minecraft.Items
 */
package moscow.rockstar.modules.combat.defense;

import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.selection.ItemSelectionState;
import moscow.rockstar.items.rules.ItemRulePredicates;
import moscow.rockstar.items.rules.ItemRuleResolverRegistry;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.defense.totem.TotemAnimation;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Items;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Totem", category=ModuleCategory.COMBAT, description="modules.descriptions.auto_totem")
public class AutoTotem
extends Module {
    private static final int TOTEM_SWITCH_DELAY_TICKS = 20;
    private NumberSetting health;
    private NumberSetting elytraHealth;
    private BooleanSetting stopUsing;
    private MultiBooleanSetting selectWith;
    private MultiBooleanSetting.Option fall;
    private MultiBooleanSetting.Option crystal;
    private MultiBooleanSetting.Option tnt;
    private MultiBooleanSetting.Option trident;
    private NumberSetting tntDistance;
    private NumberSetting crystalDistance;
    private final ItemSelectionState totemState = new ItemSelectionState(new ItemRuleResolverRegistry(), new ItemRulePredicates());
    private int availableTotemCount;
    private int selectedSlot;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        boolean bl;
        boolean bl2;
        if (AutoTotem.minecraftClient.player == null || AutoTotem.minecraftClient.world == null) {
            return;
        }
        if (AutoTotem.minecraftClient.player.getMaxHealth() <= 2.0f) {
            return;
        }
        this.countAvailableTotems();
        this.totemState.setCancelActiveUse(false);
        boolean bl3 = this.shouldEquipTotem();
        boolean bl4 = this.isTotemOnCooldown();
        boolean bl5 = AutoTotem.minecraftClient.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
        boolean bl6 = bl2 = bl3 && (!bl4 || bl5);
        if (!bl2 && this.selectedSlot > 0) {
            --this.selectedSlot;
        }
        if (bl2) {
            this.selectedSlot = 20;
        }
        boolean bl7 = bl = bl2 || this.selectedSlot > 0;
        if (bl3 && bl4 && !bl5) {
            return;
        }
        if (this.availableTotemCount <= 0) {
            if (bl3) {
                return;
            }
            this.totemState.updateSelection(Items.TOTEM_OF_UNDYING, false, class_17992 -> true, this::canInterruptItemUse);
            return;
        }
        this.totemState.updateSelection(Items.TOTEM_OF_UNDYING, bl, class_17992 -> true, this::canInterruptItemUse);
    };

    public AutoTotem() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.health = new NumberSetting(this, "modules.settings.auto_totem.health").setMinValue(1.0f).setMaxValue(20.0f).setStep(0.5f).setValue(6.0f);
        this.elytraHealth = new NumberSetting(this, "modules.settings.auto_totem.elytra_health").setMinValue(1.0f).setMaxValue(20.0f).setStep(0.5f).setValue(6.0f);
        this.stopUsing = new BooleanSetting(this, "modules.settings.auto_totem.stop_using");
        this.selectWith = new MultiBooleanSetting(this, "modules.settings.auto_totem.select_with");
        this.fall = new MultiBooleanSetting.Option(this.selectWith, "modules.settings.auto_totem.select_with.fall").select();
        this.crystal = new MultiBooleanSetting.Option(this.selectWith, "modules.settings.auto_totem.select_with.crystal");
        this.tnt = new MultiBooleanSetting.Option(this.selectWith, "modules.settings.auto_totem.select_with.tnt");
        this.trident = new MultiBooleanSetting.Option(this.selectWith, "modules.settings.auto_totem.select_with.trident");
        this.tntDistance = new NumberSetting((SettingOwner)this, "modules.settings.auto_totem.tnt_distance", () -> !this.tnt.isSelected()).setMinValue(1.0f).setMaxValue(40.0f).setStep(1.0f).setValue(19.0f);
        this.crystalDistance = new NumberSetting((SettingOwner)this, "modules.settings.auto_totem.crystal_distance", () -> !this.crystal.isSelected()).setMinValue(1.0f).setMaxValue(40.0f).setStep(1.0f).setValue(19.0f);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.totemState.setSelectionCallback(new TotemAnimation(this));
    }

    private boolean canInterruptItemUse() {
        return this.stopUsing.isEnabled() || !AutoTotem.minecraftClient.player.isUsingItem();
    }

    public boolean isTotemReady() {
        if (AutoTotem.minecraftClient.player == null || !this.isEnabled()) {
            return false;
        }
        if (this.shouldEquipTotem()) {
            return true;
        }
        return this.totemState.isSelectionComplete() && AutoTotem.minecraftClient.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
    }

    public boolean shouldEquipTotem() {
        if (AutoTotem.minecraftClient.player == null) {
            return false;
        }
        if (this.isHealthBelowThreshold()) {
            return true;
        }
        if (this.crystal.isSelected() && this.isCrystalThreatNearby()) {
            return true;
        }
        if (this.tnt.isSelected() && this.isTntThreatNearby()) {
            return true;
        }
        if (this.trident.isSelected() && this.isTridentThreatNearby()) {
            return true;
        }
        return this.fall.isSelected() && this.isFallThreatDetected();
    }

    private boolean isTotemOnCooldown() {
        if (!ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)) {
            return false;
        }
        if (AutoTotem.minecraftClient.player == null || AutoTotem.minecraftClient.player.getItemCooldownManager() == null) {
            return false;
        }
        return AutoTotem.minecraftClient.player.getItemCooldownManager().isCoolingDown(Items.TOTEM_OF_UNDYING.getDefaultStack());
    }

    private boolean isHealthBelowThreshold() {
        float f = AutoTotem.minecraftClient.player.getHealth() + AutoTotem.minecraftClient.player.getAbsorptionAmount();
        float f2 = AutoTotem.minecraftClient.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA ? this.elytraHealth.getValue() : this.health.getValue();
        return f <= f2;
    }

    private boolean isCrystalThreatNearby() {
        double d = this.crystalDistance.getValue();
        return !AutoTotem.minecraftClient.world.getEntitiesByClass(EndCrystalEntity.class, AutoTotem.minecraftClient.player.getBoundingBox().expand(d), class_15112 -> true).isEmpty();
    }

    private boolean isTntThreatNearby() {
        double d = this.tntDistance.getValue();
        return !AutoTotem.minecraftClient.world.getEntitiesByClass(TntEntity.class, AutoTotem.minecraftClient.player.getBoundingBox().expand(d), class_15412 -> true).isEmpty();
    }

    private boolean isFallThreatDetected() {
        if (AutoTotem.minecraftClient.player.isOnGround() || AutoTotem.minecraftClient.player.isGliding() || AutoTotem.minecraftClient.player.isTouchingWater() || AutoTotem.minecraftClient.player.isClimbing() || AutoTotem.minecraftClient.player.isInLava()) {
            return false;
        }
        float f = AutoTotem.minecraftClient.player.getHealth() + AutoTotem.minecraftClient.player.getAbsorptionAmount();
        float f2 = AutoTotem.minecraftClient.player.fallDistance;
        return f2 >= f;
    }

    private boolean isTridentThreatNearby() {
        return AutoTotem.minecraftClient.world.getEntitiesByClass(TridentEntity.class, AutoTotem.minecraftClient.player.getBoundingBox().expand(5.0), class_16852 -> class_16852.isAlive() && class_16852.getOwner() != AutoTotem.minecraftClient.player).stream().anyMatch(class_16852 -> class_16852.getVelocity().lengthSquared() > 0.1);
    }

    private void countAvailableTotems() {
        this.availableTotemCount = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules()).combineRules(ItemRuleSets.getOffhandRules()).combineRules(ItemRuleSets.getArmorRules()).findAllByItem(Items.TOTEM_OF_UNDYING).size();
    }

    @Override
    public void onDisable() {
        this.totemState.setSelectionCallback(null);
        super.onDisable();
        this.totemState.resetSelection();
        this.selectedSlot = 0;
    }
}
