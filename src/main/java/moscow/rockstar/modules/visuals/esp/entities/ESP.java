/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.modules.visuals.esp.entities;

import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.ActionSetting;
import moscow.rockstar.ui.screens.EspSettingsScreen;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="ESP", category=ModuleCategory.VISUALS, disableLocked=true, description="modules.descriptions.esp")
public class ESP
extends Module {
    private ActionSetting openMenu;

    public ESP() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.openMenu = new ActionSetting(this, "modules.settings.esp.open_menu").withAction(() -> minecraftClient.setScreen(new EspSettingsScreen()));
    }
}

