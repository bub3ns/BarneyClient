/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.modules.movement.speed;

import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.util.math.MathHelper;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Strafe", category=ModuleCategory.MOVEMENT, description="modules.descriptions.strafe")
public class Strafe
extends Module {
    private BooleanSetting autoJump;
    private BooleanSetting damageBoost;
    private NumberSetting damageSpeed;
    private double previousHorizontalSpeed;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        double d;
        if (Strafe.minecraftClient.player == null || Strafe.minecraftClient.world == null) {
            return;
        }
        if (!EntityUtils.hasMovementInput() || Strafe.minecraftClient.player.isGliding() || Strafe.minecraftClient.player.isClimbing() || Strafe.minecraftClient.player.isTouchingWater()) {
            this.previousHorizontalSpeed = 0.0;
            return;
        }
        if (this.autoJump.isEnabled() && Strafe.minecraftClient.player.isOnGround() && !Strafe.minecraftClient.options.jumpKey.isPressed()) {
            Strafe.minecraftClient.player.jump();
        }
        if ((d = this.calculateDamageBoost(this.damageBoost.isEnabled(), Strafe.minecraftClient.player.hurtTime > 0, this.autoJump.isEnabled(), this.damageSpeed.getValue())) <= 0.0) {
            return;
        }
        this.applyDamageBoost(d);
        double d2 = Math.hypot(Strafe.minecraftClient.player.getVelocity().x, Strafe.minecraftClient.player.getVelocity().z);
        this.previousHorizontalSpeed = d2;
    };

    public Strafe() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.autoJump = new BooleanSetting(this, "modules.settings.strafe.auto_jump");
        this.damageBoost = new BooleanSetting(this, "modules.settings.strafe.damage_boost");
        this.damageSpeed = new NumberSetting((SettingOwner)this, "modules.settings.strafe.damage_speed", () -> !this.damageBoost.isEnabled()).setMinValue(0.05f).setMaxValue(0.8f).setStep(0.01f).setValue(0.4f);
    }

    @Override
    public final void onDisable() {
        this.previousHorizontalSpeed = 0.0;
        super.onDisable();
    }

    private double calculateDamageBoost(boolean enabled, boolean hurt, boolean autoJumpEnabled, float configuredSpeed) {
        if (!enabled || !hurt) {
            return 0.0;
        }
        double baseSpeed = Math.max(0.0, configuredSpeed);
        if (autoJumpEnabled) {
            baseSpeed *= 1.05;
        }
        return Math.max(baseSpeed, this.previousHorizontalSpeed);
    }

    private void applyDamageBoost(double d) {
        float f = Strafe.minecraftClient.player.input.movementForward;
        float f2 = Strafe.minecraftClient.player.input.movementSideways;
        float f3 = Strafe.minecraftClient.player.getYaw();
        if (f == 0.0f && f2 == 0.0f) {
            return;
        }
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += f > 0.0f ? -45.0f : 45.0f;
            } else if (f2 < 0.0f) {
                f3 += f > 0.0f ? 45.0f : -45.0f;
            }
            f2 = 0.0f;
            f = MathHelper.clamp((float)(f > 0.0f ? 1.0f : -1.0f), (float)-1.0f, (float)1.0f);
        }
        f2 = MathHelper.clamp((float)f2, (float)-1.0f, (float)1.0f);
        double d2 = Math.toRadians(f3 + 90.0f);
        double d3 = Math.sin(d2);
        double d4 = Math.cos(d2);
        double d5 = (double)f * d * d4 + (double)f2 * d * d3;
        double d6 = (double)f * d * d3 - (double)f2 * d * d4;
        Strafe.minecraftClient.player.setVelocity(d5, Strafe.minecraftClient.player.getVelocity().y, d6);
    }
}
