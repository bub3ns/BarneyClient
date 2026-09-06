/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Vec3d
 */
package moscow.rockstar.modules.movement.flight;

import lombok.Generated;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.util.math.Vec3d;
import pyrock.events.game.FireworkEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Super Firework", category=ModuleCategory.MOVEMENT, description="modules.descriptions.elytra_motion")
public class SuperFirework
extends Module {
    private ModeSetting algorithm;
    private ModeSetting.Option defaultAlgorithm;
    private ModeSetting.Option custom;
    private ModeSetting.Option advanced;
    private ModeSetting.Option customAlgorithm;
    private NumberSetting customStrength;
    private final EventListener<FireworkEvent> onFireworkEvent = fireworkEvent -> {
        double d;
        double d2;
        Rotation rotation;
        if (fireworkEvent.getEntity() != SuperFirework.minecraftClient.player || SuperFirework.minecraftClient.player == null) {
            return;
        }
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        if (rotationManager == null) {
            return;
        }
        Rotation rotation2 = rotation = rotationManager.isIdle() ? rotationManager.getPlayerRotation() : rotationManager.getCurrentRotation();
        if (rotation == null) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = rotation.toDirectionVector();
        if (this.algorithm.isSelected(this.defaultAlgorithm)) {
            double d3;
            d2 = d3 = this.calculateFireworkRotation(rotation);
            d = d3;
        } else if (!this.algorithm.isSelected(this.custom)) {
            float f = this.calculateVerticalMagnitude(rotation.getYaw());
            float f2 = Math.abs(rotation.getPitch());
            d2 = this.calculateCustomMotion(f, f2, this.algorithm.getSelectedOption());
            d = this.calculateAdvancedMotion(f2, f, this.algorithm.getSelectedOption());
        } else {
            d2 = this.customStrength.getValue();
            d = this.customStrength.getValue();
        }
        Vec3d WallPlayerSkullBlock = this.getPosition(fireworkEvent.getVelocity(), VanillaChestLootTableGenerator, d2, d);
        fireworkEvent.setVelocity(WallPlayerSkullBlock);
    };

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.algorithm = new ModeSetting(this, "modules.settings.elytra_motion.algorithm");
        this.defaultAlgorithm = new ModeSetting.Option(this.algorithm, "modules.settings.elytra_motion.algorithm.default").select();
        this.custom = new ModeSetting.Option(this.algorithm, "modules.settings.elytra_motion.algorithm.custom");
        this.advanced = new ModeSetting.Option(this.algorithm, "modules.settings.elytra_motion.algorithm.advanced");
        this.customAlgorithm = new ModeSetting.Option(this.algorithm, "modules.settings.elytra_motion.algorithm.advanced-stable");
        this.customStrength = new NumberSetting((SettingOwner)this, "modules.settings.elytra_motion.custom_strength", () -> !this.algorithm.isSelected(this.custom)).setMinValue(1.5f).setMaxValue(2.5f).setStep(0.025f).setValue(1.78f);
    }

    public SuperFirework() {
        this.initializeSettings();
    }

    private Vec3d getPosition(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, double d, double d2) {
        double d3 = 0.1;
        return VanillaChestLootTableGenerator.add(WallPlayerSkullBlock.x * d3 + (WallPlayerSkullBlock.x * d - VanillaChestLootTableGenerator.x) * 0.5, WallPlayerSkullBlock.y * d3 + (WallPlayerSkullBlock.y * d2 - VanillaChestLootTableGenerator.y) * 0.5, WallPlayerSkullBlock.z * d3 + (WallPlayerSkullBlock.z * d - VanillaChestLootTableGenerator.z) * 0.5);
    }

    private double calculateFireworkRotation(Rotation rotation) {
        double d;
        float f = rotation.getYaw();
        float f2 = rotation.getPitch();
        double d2 = 1.49;
        double d3 = SuperFirework.calculateHorizontalMagnitude(f);
        if (f2 >= -45.0f && f2 <= 20.0f) {
            double d4 = -10.0;
            double d5 = (double)f2 - d4;
            d = 1.0 + 0.05 * Math.exp(-(d5 * d5) / 800.0);
        } else if (f2 > 20.0f) {
            double d6 = (double)f2 - 20.0;
            d = 1.0 - Math.min(0.35, d6 / 70.0 * 0.35);
        } else {
            double d7 = (double)Math.abs(f2) - 45.0;
            d = 1.0 - Math.min(0.3, d7 / 45.0 * 0.3);
        }
        return Math.clamp(d3 * d, 0.65, 1.49);
    }

    private static double calculateHorizontalMagnitude(float f) {
        double d = Math.abs(f % 360.0f);
        if (d > 180.0) {
            d = 360.0 - d;
        }
        double d2 = Math.abs(d - 45.0);
        double d3 = Math.abs(d - 135.0);
        double d4 = Math.min(d2, d3);
        double d5 = 0.47 * Math.exp(-(d4 * d4) / 288.0);
        return 1.0 + d5;
    }

    private float calculateVerticalMagnitude(float f) {
        float f2 = f % 180.0f;
        if (f2 > 90.0f) {
            f2 -= 180.0f;
        } else if (f2 < -90.0f) {
            f2 += 180.0f;
        }
        return Math.abs(f2);
    }

    private double calculateCustomMotion(float f, float f2, ModeSetting.Option option) {
        int n = (int)Math.ceil(f);
        double d = f2 >= 40.0f && f2 <= 50.0f ? 2.0 : (f2 >= 38.0f && f2 <= 52.0f ? 1.98 : (f2 >= 32.0f && f2 <= 58.0f ? 1.97 : (n == 33 || n == 57 || f2 == 33.0f || f2 == 57.0f ? 1.964 : (n == 34 || n == 56 || f2 == 34.0f || f2 == 56.0f ? 1.964 : (n == 35 || n == 55 || f2 == 35.0f || f2 == 55.0f ? 1.965 : (n == 36 || n == 54 || f2 == 36.0f || f2 == 54.0f ? 1.965 : (n == 37 || n == 53 || f2 == 37.0f || f2 == 53.0f ? 1.966 : (n == 38 || n == 52 || f2 == 38.0f || f2 == 52.0f ? 1.966 : (n == 39 || n == 51 || f2 == 39.0f || f2 == 51.0f ? 1.966 : (n == 40 || n == 50 || f2 == 40.0f || f2 == 50.0f ? 1.967 : (n == 41 || n == 49 || f2 == 41.0f || f2 == 49.0f ? 1.968 : (n == 42 || n == 48 || f2 == 42.0f || f2 == 48.0f ? 1.969 : (n == 43 || n == 47 || f2 == 43.0f || f2 == 47.0f ? 1.969 : (n == 44 || n == 46 || f2 == 44.0f || f2 == 46.0f ? 1.9695 : (n == 45 || f2 == 45.0f ? 1.9695 : (n >= 29 && n <= 61 || f2 >= 29.0f && f2 <= 61.0f ? 1.963 : (n >= 27 && n <= 63 || f2 >= 27.0f && f2 <= 63.0f ? 1.84 : (n >= 26 && n <= 64 || f2 >= 26.0f && f2 <= 64.0f ? 1.8 : (n >= 15 && n <= 75 || f2 >= 15.0f && f2 <= 75.0f ? 1.74 : (n >= 13 && n <= 77 || f2 >= 13.0f && f2 <= 77.0f ? 1.7 : (n >= 12 && n <= 78 || f2 >= 12.0f && f2 <= 78.0f ? 1.671 : 1.626)))))))))))))))))))));
        if (d < 1.9 && f2 > 10.0f) {
            d += 0.05;
        }
        return d * (double)(option == this.customAlgorithm ? 0.98f : 1.0f);
    }

    private double calculateAdvancedMotion(float f, float f2, ModeSetting.Option option) {
        double d = f >= 30.0f && f <= 40.0f ? 2.0 * (double)(option == this.customAlgorithm ? 0.95f : 1.0f) : (f >= 35.0f && f <= 45.0f ? 1.99 * (double)(option == this.customAlgorithm ? 0.95f : 1.0f) : (f >= 40.0f && f <= 50.0f ? 1.97 * (double)(option == this.customAlgorithm ? 0.95f : 1.0f) : (f >= 50.0f && f <= 60.0f ? 1.96 * (double)(option == this.customAlgorithm ? 0.95f : 1.0f) : (f >= 51.0f && f <= 61.0f ? 1.89 * (double)(option == this.customAlgorithm ? 0.98f : 1.0f) : (f >= 52.0f && f <= 65.0f ? 1.7 : 1.6)))));
        return d;
    }

    @Generated
    public ModeSetting getAlgorithm() {
        return this.algorithm;
    }

    @Generated
    public ModeSetting.Option getDefaultAlgorithm() {
        return this.defaultAlgorithm;
    }

    @Generated
    public ModeSetting.Option getCustom() {
        return this.custom;
    }

    @Generated
    public ModeSetting.Option getAdvanced() {
        return this.advanced;
    }

    @Generated
    public ModeSetting.Option getCustomAlgorithm() {
        return this.customAlgorithm;
    }

    @Generated
    public NumberSetting getCustomStrengthSetting() {
        return this.customStrength;
    }
}

