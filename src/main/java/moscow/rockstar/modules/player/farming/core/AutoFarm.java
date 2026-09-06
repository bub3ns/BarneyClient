/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ScreenHandler
 *  net.minecraft.HandledScreen
 */
package moscow.rockstar.modules.player.farming.core;

import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.player.farming.core.FarmModeBase;
import moscow.rockstar.modules.player.farming.crops.CropFarmMode;
import moscow.rockstar.modules.player.farming.hud.FarmHudMetrics;
import moscow.rockstar.modules.player.farming.items.ItemFarmMode;
import moscow.rockstar.modules.player.farming.market.SwordFarmMode;
import moscow.rockstar.modules.player.farming.mining.MiningFarmMode;
import moscow.rockstar.modules.player.farming.mushroom.MushroomFarmMode;
import moscow.rockstar.modules.player.farming.potion.PotionFarmMode;
import moscow.rockstar.modules.player.farming.tree.AppleFarmMode;
import moscow.rockstar.settings.ModeSetting;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import pyrock.events.window.KeyPressEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Farm", category=ModuleCategory.PLAYER, description="modules.descriptions.auto_farm")
public class AutoFarm
extends Module {
    private final FarmHudMetrics farmState = new FarmHudMetrics(this::getInitialFarmState);
    private ModeSetting mode;
    private AppleFarmMode farmTargetState;
    private SwordFarmMode farmInventoryState;
    private PotionFarmMode farmActionState;
    private ItemFarmMode farmCropState;
    private CropFarmMode farmSearchState;
    private MushroomFarmMode farmMenuState;
    private MiningFarmMode farmFinishState;
    private FarmModeBase farmPreviousState;
    private final EventListener<KeyPressEvent> onKeyPressEvent = keyPressEvent -> {
        if (keyPressEvent.getKey() != 256 || keyPressEvent.getAction() != 1) {
            return;
        }
        if (!(AutoFarm.minecraftClient.currentScreen instanceof HandledScreen) || AutoFarm.minecraftClient.player == null) {
            return;
        }
        ScreenHandler class_17032 = AutoFarm.minecraftClient.player.currentScreenHandler;
        if (class_17032 != null && class_17032 != AutoFarm.minecraftClient.player.playerScreenHandler) {
            this.disable();
        }
    };

    public AutoFarm() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.mode = new ModeSetting(this, "modules.settings.auto_farm.mode");
        this.farmTargetState = new AppleFarmMode(this, this.mode);
        this.farmInventoryState = new SwordFarmMode(this, this.mode);
        this.farmActionState = new PotionFarmMode(this, this.mode);
        this.farmCropState = new ItemFarmMode(this, this.mode);
        this.farmSearchState = new CropFarmMode(this, this.mode);
        this.farmMenuState = new MushroomFarmMode(this, this.mode);
        this.farmFinishState = new MiningFarmMode(this, this.mode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.farmPreviousState = this.getFarmState();
        this.farmState.startTracking();
        RockstarClient.create().getEventBus().registerListeners(this.farmState);
        if (this.farmPreviousState != null) {
            RockstarClient.create().getEventBus().registerListeners(this.farmPreviousState);
            this.farmPreviousState.startFarmAutomation();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.farmState.stopTracking();
        RockstarClient.create().getEventBus().unregisterListeners(this.farmState);
        if (this.farmPreviousState != null) {
            this.farmPreviousState.resetBrewingState();
            RockstarClient.create().getEventBus().unregisterListeners(this.farmPreviousState);
            this.farmPreviousState = null;
        }
    }

    @Override
    public void onTick() {
        super.onTick();
        this.farmState.updateMetrics();
        FarmModeBase farmModeBase = this.getFarmState();
        if (farmModeBase != this.farmPreviousState) {
            if (this.farmPreviousState != null) {
                this.farmPreviousState.resetBrewingState();
                RockstarClient.create().getEventBus().unregisterListeners(this.farmPreviousState);
            }
            this.farmPreviousState = farmModeBase;
            if (this.farmPreviousState != null) {
                RockstarClient.create().getEventBus().registerListeners(this.farmPreviousState);
                this.farmPreviousState.startFarmAutomation();
            }
        }
        if (this.farmPreviousState != null) {
            this.farmPreviousState.onModeSelected();
        }
    }

    private FarmModeBase getFarmState() {
        FarmModeBase farmModeBase;
        ModeSetting.Option option = this.mode.getSelectedOption();
        return option instanceof FarmModeBase ? (farmModeBase = (FarmModeBase)option) : null;
    }

    public FarmModeBase getInitialFarmState() {
        return this.farmPreviousState;
    }

    @Generated
    public FarmHudMetrics getFarmStateManager() {
        return this.farmState;
    }
}
