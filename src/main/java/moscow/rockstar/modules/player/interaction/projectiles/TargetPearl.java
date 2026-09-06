/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.EnderPearlEntity
 *  net.minecraft.Items
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 */
package moscow.rockstar.modules.player.interaction.projectiles;

import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Target Pearl", category=ModuleCategory.PLAYER, description="modules.descriptions.target_pearl")
public class TargetPearl
extends Module {
    private static final double MIN_LANDING_DISTANCE = 1.5;
    private static final double MAX_LANDING_DISTANCE = 0.03;
    private static final double AIM_ANGLE_LIMIT = 0.99;
    private static final int MAX_TRAJECTORY_STEPS = 200;
    private NumberSetting trackRange;
    private NumberSetting minLanding;
    private NumberSetting maxLanding;
    private NumberSetting aimSpeed;
    private NumberSetting maxAngle;
    private NumberSetting cooldown;
    private BooleanSetting ownPearls;
    private BooleanSetting onlyHolding;
    private final Timer cooldownTimer = new Timer();
    private Vec3d position;
    private Rotation currentRotation;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        this.position = null;
        this.currentRotation = null;
        if (TargetPearl.minecraftClient.player == null || TargetPearl.minecraftClient.world == null) {
            return;
        }
        if (TargetPearl.minecraftClient.currentScreen != null) {
            return;
        }
        if (TargetPearl.minecraftClient.player.isUsingItem()) {
            return;
        }
        if (this.onlyHolding.isEnabled() && !TargetPearl.minecraftClient.player.getMainHandStack().isOf(Items.ENDER_PEARL) && !TargetPearl.minecraftClient.player.getOffHandStack().isOf(Items.ENDER_PEARL)) {
            return;
        }
        if (!this.hasPearlItem()) {
            return;
        }
        EnderPearlEntity class_16842 = this.findTargetPearl();
        if (class_16842 == null) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = this.getPosition(class_16842);
        if (VanillaChestLootTableGenerator == null) {
            return;
        }
        double d = TargetPearl.minecraftClient.player.getEyePos().distanceTo(VanillaChestLootTableGenerator);
        if (d < (double)this.minLanding.getValue() || d > (double)this.maxLanding.getValue()) {
            return;
        }
        Rotation rotation = this.getRotation(VanillaChestLootTableGenerator);
        if (rotation == null) {
            return;
        }
        this.position = VanillaChestLootTableGenerator;
        this.currentRotation = rotation;
        float f = this.aimSpeed.getValue();
        RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, f, f, f, RotationPriority.OVERRIDE_PRIORITY);
        if (!this.cooldownTimer.hasElapsed((long)this.cooldown.getValue())) {
            return;
        }
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        if (rotationManager.getCurrentRotation().angleDistanceTo(rotation) > this.maxAngle.getValue()) {
            return;
        }
        if (!InventoryUtils.selectHotbarItem(Items.ENDER_PEARL)) {
            return;
        }
        this.cooldownTimer.reset();
    };

    public TargetPearl() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.trackRange = new NumberSetting(this, "modules.settings.target_pearl.track_range").setMinValue(10.0f).setMaxValue(80.0f).setStep(1.0f).setValue(50.0f).setFormatter(f -> " m");
        this.minLanding = new NumberSetting(this, "modules.settings.target_pearl.min_landing").setMinValue(2.0f).setMaxValue(15.0f).setStep(0.5f).setValue(4.0f).setFormatter(f -> " m");
        this.maxLanding = new NumberSetting(this, "modules.settings.target_pearl.max_landing").setMinValue(10.0f).setMaxValue(80.0f).setStep(1.0f).setValue(45.0f).setFormatter(f -> " m");
        this.aimSpeed = new NumberSetting(this, "modules.settings.target_pearl.aim_speed").setMinValue(40.0f).setMaxValue(180.0f).setStep(5.0f).setValue(180.0f);
        this.maxAngle = new NumberSetting(this, "modules.settings.target_pearl.max_angle").setMinValue(0.5f).setMaxValue(20.0f).setStep(0.5f).setValue(5.0f).setFormatter(f -> "\u00b0");
        this.cooldown = new NumberSetting(this, "modules.settings.target_pearl.cooldown").setMinValue(0.0f).setMaxValue(1000.0f).setStep(25.0f).setValue(50.0f).setFormatter(f -> " ms");
        this.ownPearls = new BooleanSetting(this, "modules.settings.target_pearl.own_pearls");
        this.onlyHolding = new BooleanSetting(this, "modules.settings.target_pearl.only_holding");
    }

    @Override
    public void onEnable() {
        this.cooldownTimer.reset();
        this.position = null;
        this.currentRotation = null;
    }

    private boolean hasPearlItem() {
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getHotbarRules().combineRules(ItemRuleSets.getInventoryRules()).combineRules(ItemRuleSets.getOffhandRules());
        return itemRuleCollection.findByStack(class_17992 -> class_17992 != null && !class_17992.isEmpty() && class_17992.isOf(Items.ENDER_PEARL)) != null;
    }

    private EnderPearlEntity findTargetPearl() {
        EnderPearlEntity class_16842 = null;
        double d = Double.MAX_VALUE;
        float f = this.trackRange.getValue() * this.trackRange.getValue();
        for (Entity class_12972 : TargetPearl.minecraftClient.world.getEntities()) {
            double d2;
            if (!(class_12972 instanceof EnderPearlEntity)) continue;
            EnderPearlEntity class_16843 = (EnderPearlEntity)class_12972;
            if (!this.ownPearls.isEnabled() && class_16843.getOwner() == TargetPearl.minecraftClient.player || class_16843.isRemoved() || (d2 = class_16843.squaredDistanceTo((Entity)TargetPearl.minecraftClient.player)) > (double)f || !(d2 < d)) continue;
            d = d2;
            class_16842 = class_16843;
        }
        return class_16842;
    }

    private Vec3d getPosition(EnderPearlEntity class_16842) {
        Vec3d VanillaChestLootTableGenerator = class_16842.getPos();
        Vec3d WallPlayerSkullBlock = class_16842.getVelocity();
        for (int i = 0; i < 200; ++i) {
            Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock);
            BlockHitResult class_39652 = TargetPearl.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, VanillaEntityLootTableGenerator, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)class_16842));
            if (class_39652.getType() == HitResult.Type.BLOCK) {
                return class_39652.getPos();
            }
            WallPlayerSkullBlock = WallPlayerSkullBlock.multiply(0.99).add(0.0, -0.03, 0.0);
            VanillaChestLootTableGenerator = VanillaEntityLootTableGenerator;
            if (!(VanillaChestLootTableGenerator.y < (double)(TargetPearl.minecraftClient.world.getBottomY() - 16))) continue;
            return null;
        }
        return null;
    }

    private Rotation getRotation(Vec3d VanillaChestLootTableGenerator) {
        Vec3d WallPlayerSkullBlock = TargetPearl.minecraftClient.player.getEyePos();
        double d = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d2 = VanillaChestLootTableGenerator.y - WallPlayerSkullBlock.y;
        double d3 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        double d4 = Math.hypot(d, d3);
        if (d4 < 0.001) {
            return null;
        }
        float f = (float)(Math.toDegrees(Math.atan2(d3, d)) - 90.0);
        Float f2 = this.calculateHorizontalOffset(d4, d2);
        if (f2 == null) {
            return null;
        }
        return new Rotation(f, f2.floatValue());
    }

    private Float calculateHorizontalOffset(double d, double d2) {
        float f;
        float f2 = Float.NaN;
        double d3 = Double.MAX_VALUE;
        for (f = -89.0f; f <= 60.0f; f += 1.0f) {
            double d4;
            Double d5 = this.calculateVerticalOffset(d, f);
            if (d5 == null || !((d4 = Math.abs(d5 - d2)) < d3)) continue;
            d3 = d4;
            f2 = f;
        }
        if (Float.isNaN(f2)) {
            return null;
        }
        f = f2 - 1.0f;
        float f3 = f2 + 1.0f;
        for (float f4 = f; f4 <= f3; f4 += 0.05f) {
            double d6;
            Double d7 = this.calculateVerticalOffset(d, f4);
            if (d7 == null || !((d6 = Math.abs(d7 - d2)) < d3)) continue;
            d3 = d6;
            f2 = f4;
        }
        if (d3 > 1.5) {
            return null;
        }
        return Float.valueOf(MathHelper.clamp((float)f2, (float)-90.0f, (float)90.0f));
    }

    private Double calculateVerticalOffset(double d, float f) {
        double d2 = Math.toRadians(f);
        double d3 = Math.cos(d2);
        double d4 = d3 * 1.5;
        double d5 = -Math.sin(d2) * 1.5;
        if (d4 <= 1.0E-4) {
            return null;
        }
        double d6 = 0.0;
        double d7 = 0.0;
        for (int i = 0; i < 200; ++i) {
            double d8 = d6;
            double d9 = d7;
            d6 += d4;
            d7 += d5;
            if (d6 >= d) {
                double d10 = (d - d8) / d4;
                return d9 + d5 * d10;
            }
            d4 *= 0.99;
            d5 = d5 * 0.99 - 0.03;
        }
        return null;
    }
}
