/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.modules.visuals.overlay;

import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.player.movement.camera.FreeCamera;
import moscow.rockstar.render.world.CameraClipManager;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.SettingOwner;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.Render3DEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Removals", category=ModuleCategory.VISUALS, disableLocked=true, description="modules.descriptions.removals")
public class Removals
extends Module {
    private double hurtCamIntensity;
    private MultiBooleanSetting effects;
    private MultiBooleanSetting.Option hurtCam;
    private MultiBooleanSetting.Option scoreboard;
    private MultiBooleanSetting.Option bossBar;
    private MultiBooleanSetting.Option portal;
    private MultiBooleanSetting.Option fire;
    private MultiBooleanSetting.Option clip;
    private MultiBooleanSetting.Option breakParticles;
    private MultiBooleanSetting.Option totem;
    private MultiBooleanSetting.Option water;
    private MultiBooleanSetting.Option nausea;
    private MultiBooleanSetting.Option blindness;
    private MultiBooleanSetting.Option darkness;
    private MultiBooleanSetting.Option pumpkin;
    private MultiBooleanSetting.Option fov;
    private MultiBooleanSetting.Option weather;
    private MultiBooleanSetting.Option glowing;
    private BooleanSetting clipTransparency;
    private MultiBooleanSetting sounds;
    private MultiBooleanSetting.Option beacon;
    private MultiBooleanSetting.Option phantoms;
    private MultiBooleanSetting.Option weatherSound;
    private MultiBooleanSetting.Option waterSound;
    private MultiBooleanSetting.Option lavaSound;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        if (this.fov.isSelected()) {
            Removals.minecraftClient.options.getFovEffectScale().setValue(0.0);
        }
    };
    private final EventListener<Render3DEvent> onRender3DEvent = render3DEvent -> {
        boolean bl = RockstarClient.create().getModuleRegistry().getModule(FreeCamera.class).isCameraActive();
        boolean enabled = this.isEnabled() && this.clip.isSelected() && this.clipTransparency.isEnabled() && !bl;
        CameraClipManager.update(render3DEvent.getTickDelta(), enabled);
        CameraClipManager.render(render3DEvent);
    };

    public Removals() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.effects = new MultiBooleanSetting(this, "modules.settings.removals.effects");
        this.hurtCam = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.hurtCam").select();
        this.scoreboard = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.scoreboard");
        this.bossBar = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.bossBar");
        this.portal = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.portal").select();
        this.fire = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.fire").select();
        this.clip = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.clip").select();
        this.breakParticles = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.breakParticles");
        this.totem = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.totem").select();
        this.water = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.water");
        this.nausea = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.nausea").select();
        this.blindness = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.blindness").select();
        this.darkness = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.darkness").select();
        this.pumpkin = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.pumpkin").select();
        this.fov = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.fov").select();
        this.weather = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.weather").select();
        this.glowing = new MultiBooleanSetting.Option(this.effects, "modules.settings.removals.glowing");
        this.clipTransparency = new BooleanSetting((SettingOwner)this, "modules.settings.removals.clipTransparency", () -> !this.clip.isSelected()).enable();
        this.sounds = new MultiBooleanSetting(this, "modules.settings.removals.sounds");
        this.beacon = new MultiBooleanSetting.Option(this.sounds, "modules.settings.removals.beacon").select();
        this.phantoms = new MultiBooleanSetting.Option(this.sounds, "modules.settings.removals.phantoms").select();
        this.weatherSound = new MultiBooleanSetting.Option(this.sounds, "modules.settings.removals.weatherSound").select();
        this.waterSound = new MultiBooleanSetting.Option(this.sounds, "modules.settings.removals.waterSound");
        this.lavaSound = new MultiBooleanSetting.Option(this.sounds, "modules.settings.removals.lavaSound");
    }

    @Override
    public void onEnable() {
        this.hurtCamIntensity = (Double)Removals.minecraftClient.options.getFovEffectScale().getValue();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        Removals.minecraftClient.options.getFovEffectScale().setValue(this.hurtCamIntensity);
        CameraClipManager.clear();
        super.onDisable();
    }

    @Generated
    public double getHurtCamIntensity() {
        return this.hurtCamIntensity;
    }

    @Generated
    public MultiBooleanSetting getEffects() {
        return this.effects;
    }

    @Generated
    public MultiBooleanSetting.Option getHurtCam() {
        return this.hurtCam;
    }

    @Generated
    public MultiBooleanSetting.Option getScoreboard() {
        return this.scoreboard;
    }

    @Generated
    public MultiBooleanSetting.Option getBossBar() {
        return this.bossBar;
    }

    @Generated
    public MultiBooleanSetting.Option getPortal() {
        return this.portal;
    }

    @Generated
    public MultiBooleanSetting.Option getFire() {
        return this.fire;
    }

    @Generated
    public MultiBooleanSetting.Option getClip() {
        return this.clip;
    }

    @Generated
    public MultiBooleanSetting.Option getBreakParticles() {
        return this.breakParticles;
    }

    @Generated
    public MultiBooleanSetting.Option getTotem() {
        return this.totem;
    }

    @Generated
    public MultiBooleanSetting.Option getWater() {
        return this.water;
    }

    @Generated
    public MultiBooleanSetting.Option getNausea() {
        return this.nausea;
    }

    @Generated
    public MultiBooleanSetting.Option getBlindness() {
        return this.blindness;
    }

    @Generated
    public MultiBooleanSetting.Option getDarkness() {
        return this.darkness;
    }

    @Generated
    public MultiBooleanSetting.Option getPumpkin() {
        return this.pumpkin;
    }

    @Generated
    public MultiBooleanSetting.Option getFov() {
        return this.fov;
    }

    @Generated
    public MultiBooleanSetting.Option getWeather() {
        return this.weather;
    }

    @Generated
    public MultiBooleanSetting.Option getGlowing() {
        return this.glowing;
    }

    @Generated
    public BooleanSetting getClipTransparency() {
        return this.clipTransparency;
    }

    @Generated
    public MultiBooleanSetting getScoreboard1() {
        return this.sounds;
    }

    @Generated
    public MultiBooleanSetting.Option getBeacon() {
        return this.beacon;
    }

    @Generated
    public MultiBooleanSetting.Option getPhantoms() {
        return this.phantoms;
    }

    @Generated
    public MultiBooleanSetting.Option getWeatherSound() {
        return this.weatherSound;
    }

    @Generated
    public MultiBooleanSetting.Option getWaterSound() {
        return this.waterSound;
    }

    @Generated
    public MultiBooleanSetting.Option getLavaSound() {
        return this.lavaSound;
    }

    @Generated
    public EventListener<ClientPlayerTickEvent> getOnClientPlayerTick() {
        return this.onClientPlayerTickEvent;
    }

    @Generated
    public EventListener<Render3DEvent> getScoreboard2() {
        return this.onRender3DEvent;
    }
}
