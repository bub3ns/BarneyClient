/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.modules;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.modules.combat.attacks.AutoAnchor;
import moscow.rockstar.modules.combat.attacks.AutoExplosion;
import moscow.rockstar.modules.combat.attacks.AutoThrow;
import moscow.rockstar.modules.combat.attacks.Criticals;
import moscow.rockstar.modules.combat.attacks.TriggerBot;
import moscow.rockstar.modules.combat.attacks.potions.AutoPotion;
import moscow.rockstar.modules.combat.automation.AutoDuels;
import moscow.rockstar.modules.combat.defense.AutoSoup;
import moscow.rockstar.modules.combat.defense.AutoTotem;
import moscow.rockstar.modules.combat.defense.KnockbackTweaks;
import moscow.rockstar.modules.combat.defense.armor.AutoArmor;
import moscow.rockstar.modules.combat.defense.velocity.Velocity;
import moscow.rockstar.modules.combat.rotation.AntiAim;
import moscow.rockstar.modules.combat.targeting.AimAssist;
import moscow.rockstar.modules.combat.targeting.AimBot;
import moscow.rockstar.modules.combat.targeting.AntiBot;
import moscow.rockstar.modules.combat.targeting.BackTrack;
import moscow.rockstar.modules.combat.targeting.ElytraTarget;
import moscow.rockstar.modules.combat.targeting.Hitboxes;
import moscow.rockstar.modules.combat.trapping.WebTrap;
import moscow.rockstar.modules.movement.flight.ElytraStrafe;
import moscow.rockstar.modules.movement.flight.Flight;
import moscow.rockstar.modules.movement.flight.GrimGlide;
import moscow.rockstar.modules.movement.flight.SuperFirework;
import moscow.rockstar.modules.movement.items.NoSlow;
import moscow.rockstar.modules.movement.jump.AirStuck;
import moscow.rockstar.modules.movement.jump.HighJump;
import moscow.rockstar.modules.movement.jump.Spider;
import moscow.rockstar.modules.movement.speed.Speed;
import moscow.rockstar.modules.movement.speed.Strafe;
import moscow.rockstar.modules.movement.speed.WaterSpeed;
import moscow.rockstar.modules.movement.sprint.AutoSprint;
import moscow.rockstar.modules.movement.timing.Timer;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.other.assist.Assist;
import moscow.rockstar.modules.other.auth.AutoAuth;
import moscow.rockstar.modules.other.base.BaseFinder;
import moscow.rockstar.modules.other.games.RussianRoulette;
import moscow.rockstar.modules.other.inventory.InventoryBuilder;
import moscow.rockstar.modules.other.market.auction.Auction;
import moscow.rockstar.modules.other.market.purchase.AutoBuy;
import moscow.rockstar.modules.other.market.resell.AutoResell;
import moscow.rockstar.modules.other.network.AutoJoin;
import moscow.rockstar.modules.other.safety.Panic;
import moscow.rockstar.modules.other.social.AutoAccept;
import moscow.rockstar.modules.other.testing.Test;
import moscow.rockstar.modules.player.automation.inventory.AutoShulker;
import moscow.rockstar.modules.player.automation.inventory.AutoSwap;
import moscow.rockstar.modules.player.automation.server.AutoLeave;
import moscow.rockstar.modules.player.automation.server.ClanUpgrade;
import moscow.rockstar.modules.player.automation.survival.AutoEat;
import moscow.rockstar.modules.player.automation.survival.AutoInvisible;
import moscow.rockstar.modules.player.effects.EffectRemover;
import moscow.rockstar.modules.player.farming.core.AutoFarm;
import moscow.rockstar.modules.player.interaction.ClickThrough;
import moscow.rockstar.modules.player.interaction.FastItemUse;
import moscow.rockstar.modules.player.interaction.MiddleClick;
import moscow.rockstar.modules.player.interaction.projectiles.TargetPearl;
import moscow.rockstar.modules.player.inventory.BootsSwap;
import moscow.rockstar.modules.player.inventory.InventoryCleaner;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.modules.player.inventory.InventoryUtils;
import moscow.rockstar.modules.player.inventory.ItemPickup;
import moscow.rockstar.modules.player.inventory.Stealer;
import moscow.rockstar.modules.player.mining.MineHelper;
import moscow.rockstar.modules.player.mining.Nuker;
import moscow.rockstar.modules.player.movement.NoDelay;
import moscow.rockstar.modules.player.movement.NoFall;
import moscow.rockstar.modules.player.movement.NoInteract;
import moscow.rockstar.modules.player.movement.NoPush;
import moscow.rockstar.modules.player.movement.NoRotate;
import moscow.rockstar.modules.player.movement.camera.FreeCamera;
import moscow.rockstar.modules.player.movement.flight.ElytraUtils;
import moscow.rockstar.modules.player.movement.teleport.Blink;
import moscow.rockstar.modules.player.placement.Scaffold;
import moscow.rockstar.modules.player.tracking.Tracker;
import moscow.rockstar.modules.player.util.PlayerUtils;
import moscow.rockstar.modules.visuals.audio.Sounds;
import moscow.rockstar.modules.visuals.camera.Beautifully;
import moscow.rockstar.modules.visuals.effects.donations.DonateEffects;
import moscow.rockstar.modules.visuals.effects.kill.KillEffects;
import moscow.rockstar.modules.visuals.esp.entities.AntiInvisible;
import moscow.rockstar.modules.visuals.esp.entities.ESP;
import moscow.rockstar.modules.visuals.esp.sound.SoundESP;
import moscow.rockstar.modules.visuals.esp.storage.StorageESP;
import moscow.rockstar.modules.visuals.esp.targeting.TargetESP;
import moscow.rockstar.modules.visuals.hand.SwingAnimation;
import moscow.rockstar.modules.visuals.hand.ViewModel;
import moscow.rockstar.modules.visuals.hud.Interface;
import moscow.rockstar.modules.visuals.hud.NameProtect;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.modules.visuals.object.ObjectInfo;
import moscow.rockstar.modules.visuals.overlay.Removals;
import moscow.rockstar.modules.visuals.prediction.Prediction;
import moscow.rockstar.modules.visuals.tnt.TNTTimer;
import moscow.rockstar.modules.visuals.waypoints.DeathCords;
import moscow.rockstar.modules.visuals.waypoints.Waypoints;
import moscow.rockstar.modules.visuals.world.Ambience;
import moscow.rockstar.modules.visuals.world.CustomFog;
import moscow.rockstar.modules.visuals.world.WardenHelper;
import moscow.rockstar.modules.visuals.world.World;
import moscow.rockstar.modules.visuals.world.XRay;
import pyrock.events.game.GameTickEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.HudRenderEvent;
import pyrock.events.window.KeyPressEvent;
import pyrock.events.window.MouseEvent;
import ua.mintantileak.spk.Compile;

public class ModuleRegistry {
    private final Map<Class<? extends ModuleContract>, ModuleContract> modulesByClass = new IdentityHashMap<Class<? extends ModuleContract>, ModuleContract>();
    private final List<ModuleContract> modules = new ArrayList<ModuleContract>();
    private static int REGISTRY_INSTANCE_COUNT;
    private final EventListener<ClientPlayerTickEvent> clientTickListener;
    private final EventListener<HudRenderEvent> hudRenderListener;
    private final EventListener<KeyPressEvent> keyPressListener = v1 -> {
        if (MinecraftClient.getInstance().currentScreen != null) {
            return;
        }
        if (v1.getAction() != 1) {
            return;
        }
        int i2 = moscow.rockstar.ui.input.KeyBindingUtil.currentModifiers();
        for (ModuleContract v4 : this.getModules()) {
            if (!v4.isAvailable() || !moscow.rockstar.ui.input.KeyBindingUtil.matches(v4.getKeyBind(), v1.getKey(), i2)) continue;
            v4.toggle();
        }
    };
    private final EventListener<MouseEvent> mouseListener = v1 -> {
        if (MinecraftClient.getInstance().currentScreen != null) {
            return;
        }
        if (v1.getAction() != 1) {
            return;
        }
        int i2 = moscow.rockstar.ui.input.KeyBindingUtil.currentModifiers();
        for (ModuleContract v4 : this.getModules()) {
            if (!v4.isAvailable() || !moscow.rockstar.ui.input.KeyBindingUtil.matches(v4.getKeyBind(), v1.getButton(), i2)) continue;
            v4.toggle();
        }
    };
    private final EventListener<GameTickEvent> gameTickListener = v1 -> {
        if (!Module.isLoggedIn()) {
            return;
        }
        for (ModuleContract v3 : this.getModules()) {
            if (!v3.isEnabled() || v3.isAvailable()) continue;
            v3.setEnabled(false, true);
        }
    };

    public static void incrementRegistryCount() {
        ++REGISTRY_INSTANCE_COUNT;
    }

    public ModuleRegistry(EventListener<ClientPlayerTickEvent> v12, EventListener<HudRenderEvent> v2) {
        this.clientTickListener = v12;
        this.hudRenderListener = v2;
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    @Compile(obfuscation=4)
    public final void registerModules() {
        this.registerModule(new Aura());
        this.registerModule(new AimAssist());
        this.registerModule(new AutoTotem());
        this.registerModule(new TriggerBot());
        this.registerModule(new AimBot());
        this.registerModule(new AutoPotion());
        this.registerModule(new AutoThrow());
        this.registerModule(new AntiBot());
        this.registerModule(new Velocity());
        this.registerModule(new KnockbackTweaks());
        this.registerModule(new AutoArmor());
        this.registerModule(new AutoExplosion());
        this.registerModule(new AutoAnchor());
        this.registerModule(new BackTrack());
        this.registerModule(new Hitboxes());
        this.registerModule(new ElytraTarget());
        this.registerModule(new Criticals());
        this.registerModule(new AutoSoup());
        this.registerModule(new AutoSprint());
        this.registerModule(new SuperFirework());
        this.registerModule(new WebTrap());
        this.registerModule(new Strafe());
        this.registerModule(new Flight());
        this.registerModule(new GrimGlide());
        this.registerModule(new Speed());
        this.registerModule(new Timer());
        this.registerModule(new NoSlow());
        this.registerModule(new HighJump());
        this.registerModule(new WaterSpeed());
        this.registerModule(new AirStuck());
        this.registerModule(new Spider());
        this.registerModule(new ElytraStrafe());
        this.registerModule(new Menu());
        this.registerModule(new ESP());
        this.registerModule(new Waypoints());
        this.registerModule(new Removals());
        this.registerModule(new Ambience());
        this.registerModule(new SwingAnimation());
        this.registerModule(new SoundESP());
        this.registerModule(new TNTTimer());
        this.registerModule(new WardenHelper());
        this.registerModule(new Beautifully());
        this.registerModule(new ViewModel());
        this.registerModule(new Blink());
        this.registerModule(new Interface());
        this.registerModule(new TargetESP());
        this.registerModule(new StorageESP());
        this.registerModule(new XRay());
        this.registerModule(new AntiInvisible());
        this.registerModule(new CustomFog());
        this.registerModule(new World());
        this.registerModule(new KillEffects());
        this.registerModule(new Prediction());
        this.registerModule(new DonateEffects());
        this.registerModule(new InventoryCleaner());
        this.registerModule(new AutoFarm());
        this.registerModule(new AutoInvisible());
        this.registerModule(new ClickThrough());
        this.registerModule(new MineHelper());
        this.registerModule(new TargetPearl());
        this.registerModule(new Stealer());
        this.registerModule(new MiddleClick());
        this.registerModule(new Tracker());
        this.registerModule(new InventoryUtils());
        this.registerModule(new AutoEat());
        this.registerModule(new ClanUpgrade());
        this.registerModule(new FreeCamera());
        this.registerModule(new NoDelay());
        this.registerModule(new PlayerUtils());
        this.registerModule(new NoPush());
        this.registerModule(new BootsSwap());
        this.registerModule(new ItemPickup());
        this.registerModule(new AutoShulker());
        this.registerModule(new Scaffold());
        this.registerModule(new ObjectInfo());
        this.registerModule(new Nuker());
        this.registerModule(new NoRotate());
        this.registerModule(new NoInteract());
        this.registerModule(new NoFall());
        this.registerModule(new EffectRemover());
        this.registerModule(new NameProtect());
        this.registerModule(new ElytraUtils());
        this.registerModule(new FastItemUse());
        this.registerModule(new AutoResell());
        this.registerModule(new BaseFinder());
        this.registerModule(new Panic());
        this.registerModule(new Auction());
        this.registerModule(new InventoryBuilder());
        this.registerModule(new AutoAccept());
        this.registerModule(new DeathCords());
        this.registerModule(new AutoLeave());
        this.registerModule(new AutoSwap());
        this.registerModule(new RussianRoulette());
        this.registerModule(new AutoDuels());
        this.registerModule(new AutoAuth());
        this.registerModule(new AutoJoin());
        this.registerModule(new InventoryMove());
        this.registerModule(new Test());
        this.registerModule(new AutoBuy());
        this.registerModule(new Assist());
        this.registerModule(new Sounds());
        this.registerModule(new AntiAim());
        this.saveModuleSettings();
    }

    @Compile(obfuscation=1)
    public final void enableLockedModules() {
        for (ModuleContract v2 : this.getModules()) {
            if (!v2.isDisableLocked()) continue;
            v2.enable();
        }
    }

    public final void registerModule(Module v1) {
        this.modulesByClass.put(v1.getClass(), v1);
        this.modules.add(v1);
    }

    public final <T extends ModuleContract> T findModuleByName(String v12) {
        return (T)this.modules.stream()
            .filter(module -> module.getName().replace(" ", "").equalsIgnoreCase(v12)
                || module.getName().equalsIgnoreCase(v12))
            .findFirst()
            .orElseThrow(() -> new ModuleNotFoundException(v12));
    }

    public final <T extends ModuleContract> T getModule(Class<T> v1) {
        return (T)((ModuleContract)v1.cast(this.modulesByClass.get(v1)));
    }

    public final void saveModuleSettings() {
        for (ModuleContract v2 : this.getModules()) {
            if (!(v2 instanceof Module)) continue;
            Module v3 = (Module)v2;
            v3.saveSettings();
        }
    }

    @Generated
    public List<ModuleContract> getModules() {
        return this.modules;
    }

    @Generated
    public static int getRegistryInstanceCount() {
        return REGISTRY_INSTANCE_COUNT;
    }

    @Generated
    public EventListener<ClientPlayerTickEvent> getClientTickListener() {
        return this.clientTickListener;
    }

    @Generated
    public EventListener<HudRenderEvent> getHudRenderListener() {
        return this.hudRenderListener;
    }
}
