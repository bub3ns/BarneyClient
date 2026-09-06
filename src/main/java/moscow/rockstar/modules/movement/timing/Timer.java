/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Difficulty
 *  net.minecraft.PlayerMoveC2SPacket
 */
package moscow.rockstar.modules.movement.timing;

import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.world.Difficulty;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.network.SendPacketEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Timer", category=ModuleCategory.MOVEMENT, description="modules.descriptions.timer")
public class Timer
extends Module {
    private ModeSetting mode;
    private ModeSetting.Option normalMode;
    private ModeSetting.Option vontamMode;
    private NumberSetting speed;
    private BooleanSetting smart;
    private float timerSpeed = 100.0f;
    private float previousSpeed = 100.0f;
    private long lastUpdateTime;
    private final EventListener<SendPacketEvent> onSendPacketEvent = sendPacketEvent -> {
        if (this.smart.isEnabled() && this.normalMode.isSelected() && sendPacketEvent.getPacket() instanceof PlayerMoveC2SPacket) {
            if (System.currentTimeMillis() - this.lastUpdateTime < 1000L) {
                float f = (float)(0.05 - (double)((float)(System.currentTimeMillis() - this.lastUpdateTime) / 1000.0f)) * 400.0f;
                this.timerSpeed -= Math.max(0.0f, f);
            }
            if (EntityUtils.hasMovementInput()) {
                this.timerSpeed += 0.5f;
            }
            this.timerSpeed = Math.max(0.0f, Math.min(100.0f, this.timerSpeed));
            this.lastUpdateTime = System.currentTimeMillis();
        }
    };
    private final EventListener<WorldChangeEvent> onWorldChangeEvent = worldChangeEvent -> {
        if (this.smart.isEnabled() && this.vontamMode.isSelected()) {
            this.timerSpeed += 7.0f;
        }
    };

    public Timer() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.mode = new ModeSetting(this, "Mode");
        this.normalMode = new ModeSetting.Option(this.mode, "Normal");
        this.vontamMode = new ModeSetting.Option(this.mode, "VonTam");
        this.speed = new NumberSetting((SettingOwner)this, "modules.settings.timer.speed", this.vontamMode::isSelected).setStep(0.1f).setMinValue(0.1f).setMaxValue(15.0f).setValue(1.0f);
        this.smart = new BooleanSetting(this, "Smart");
    }

    @Override
    public void onTick() {
        if (this.normalMode.isSelected()) {
            if (this.smart.isEnabled()) {
                EntityUtils.setMovementFactor(this.timerSpeed > 10.0f ? this.speed.getValue() : 1.0f);
            } else {
                EntityUtils.setMovementFactor(this.speed.getValue());
            }
        } else {
            if (!this.smart.isEnabled() || this.timerSpeed > 10.0f) {
                Timer.minecraftClient.player.setVelocity(Timer.minecraftClient.player.getVelocity().x * 1.05, Timer.minecraftClient.player.getVelocity().y * (Timer.minecraftClient.player.fallDistance > 0.0f ? 1.05 : 1.0), Timer.minecraftClient.player.getVelocity().z * (double)1.05f);
            }
            if (Timer.minecraftClient.world.getDifficulty() == Difficulty.EASY) {
                this.timerSpeed = 100.0f;
            }
            this.timerSpeed += 0.006f;
            this.timerSpeed -= 2.5f;
            this.timerSpeed = Math.clamp(this.timerSpeed, 0.0f, 100.0f);
        }
        super.onTick();
    }

    @Override
    public void onDisable() {
        EntityUtils.resetMovementFactor();
        super.onDisable();
    }
}

