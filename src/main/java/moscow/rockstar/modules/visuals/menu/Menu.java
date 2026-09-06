/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Screen
 */
package moscow.rockstar.modules.visuals.menu;

import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.render.HudEventForwarder;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.visuals.audio.Sounds;
import moscow.rockstar.modules.visuals.audio.SoundEffectPlayer;
import moscow.rockstar.network.session.BotPacketListener;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.ui.screens.DropdownMenuScreen;
import moscow.rockstar.ui.screens.MinecraftScreenBase;
import moscow.rockstar.ui.screens.ModuleSettingsScreen;
import net.minecraft.client.gui.screen.Screen;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Menu", category=ModuleCategory.VISUALS, keyBind=344, description="modules.descriptions.menu")
public class Menu
extends Module {
    private static final HudEventForwarder menuState = new HudEventForwarder();
    private ModeSetting mode;
    // private ModeSetting.Option dropdown;
    private ModeSetting.Option modern;
    private IntegerSetting hideKey;
    private Screen menuScreen;
    // private DropdownMenuScreen dropdownScreen;
    private static boolean menuOpen;

    public Menu() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.mode = new ModeSetting(this, "modules.settings.menu.mode");
        // this.dropdown = new ModeSetting.Option(this.mode, "modules.settings.menu.mode.dropdown");
        this.modern = new ModeSetting.Option(this.mode, "modules.settings.menu.mode.modern").select();
        this.hideKey = new IntegerSetting(this, "modules.settings.menu.hide_key").withValue(342);
    }

    @Override
    public void onEnable() {
        // boolean bl = this.modern.isSelected();
        if (Menu.minecraftClient.currentScreen instanceof ModuleSettingsScreen) {
            return;
        }
        /*
        if (!bl && Menu.minecraftClient.currentScreen instanceof DropdownMenuScreen) {
            return;
        }
        */
        this.menuScreen = this.getMenuScreen();
        menuOpen = true;
        minecraftClient.setScreen(this.menuScreen);
        Sounds sounds = RockstarClient.create().getModuleRegistry().getModule(Sounds.class);
        if (sounds.isEnabled()) {
            SoundEffectPlayer.playClickGuiOpen(sounds.getVolume());
        }
        super.onEnable();
    }

    public Screen getMenuScreen() {
        // Only modern GUI
        MinecraftScreenBase minecraftScreenBase = RockstarClient.create().getMinecraftScreen();
        MinecraftScreenBase minecraftScreenBase2 = minecraftScreenBase instanceof ModuleSettingsScreen ? minecraftScreenBase : new ModuleSettingsScreen();
        RockstarClient.create().setMinecraftScreen(minecraftScreenBase2);
        return minecraftScreenBase2;
        /*
        if (this.modern.isSelected()) {
            MinecraftScreenBase minecraftScreenBase = RockstarClient.create().getMinecraftScreen();
            MinecraftScreenBase minecraftScreenBase2 = minecraftScreenBase instanceof ModuleSettingsScreen ? minecraftScreenBase : new ModuleSettingsScreen();
            RockstarClient.create().setMinecraftScreen(minecraftScreenBase2);
            return minecraftScreenBase2;
        }
        if (this.dropdownScreen == null) {
            this.dropdownScreen = new DropdownMenuScreen();
        }
        return this.dropdownScreen;
        */
    }

    public static boolean isMenuIndexValid(int n) {
        return !menuOpen && Menu.isMenuOptionValid(n);
    }

    public static void selectMenuIndex(int n) {
        Menu menu = RockstarClient.create().getModuleRegistry().getModule(Menu.class);
        if (menu != null && moscow.rockstar.ui.input.KeyBindingUtil.keyCode(menu.getKeyBind()) == n) {
            menuOpen = false;
        }
    }

    private static boolean isMenuOptionValid(int n) {
        Menu menu = RockstarClient.create().getModuleRegistry().getModule(Menu.class);
        return menu != null && moscow.rockstar.ui.input.KeyBindingUtil.matches(menu.getKeyBind(), n);
    }

    public static void updateMenuState() {
        Menu menu = RockstarClient.create().getModuleRegistry().getModule(Menu.class);
        MinecraftClient.getInstance().setScreen(menu.getMenuScreen());
    }

    @Override
    public void onDisable() {
        menuOpen = false;
        if (Menu.minecraftClient.currentScreen == this.menuScreen) {
            minecraftClient.setScreen(null);
        }
        super.onDisable();
    }

    @Generated
    public ModeSetting.Option getModernOption() {
        return this.modern;
    }

    @Generated
    public IntegerSetting getHideKeySetting() {
        return this.hideKey;
    }
}
