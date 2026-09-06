/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ItemStack
 */
package moscow.rockstar.modules.other.macros;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.inventory.ItemSwapManager;
import moscow.rockstar.items.assist.AssistItemProvider;
import moscow.rockstar.settings.ActionSetting;
import moscow.rockstar.ui.input.KeyBindingUtil;
import moscow.rockstar.ui.screens.AssistScreen;
import moscow.rockstar.util.SupportProviderRegistry;
import net.minecraft.item.ItemStack;
import pyrock.events.window.KeyPressEvent;
import pyrock.events.window.MouseEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Macro Menu", category=ModuleCategory.OTHER)
public class MacroMenu
extends Module {
    private ActionSetting openMenuAction;
    private final List<AssistItemProvider> macroEntries = SupportProviderRegistry.createSupportProviders();
    private final List<AssistItemProvider> activeMacroEntries = new ArrayList<AssistItemProvider>(this.macroEntries);
    private final EventListener<KeyPressEvent> keyPressListener = keyPressEvent -> {
        if (keyPressEvent.getAction() != 1) {
            return;
        }
        if (MacroMenu.minecraftClient.currentScreen != null) {
            return;
        }
        this.triggerMacroForInput(keyPressEvent.getKey());
    };
    private final EventListener<MouseEvent> mouseListener = mouseEvent -> {
        if (mouseEvent.getAction() != 1) {
            return;
        }
        if (MacroMenu.minecraftClient.currentScreen != null) {
            return;
        }
        this.triggerMacroForInput(mouseEvent.getButton());
    };

    public MacroMenu() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.openMenuAction = new ActionSetting(this, "Open menu").withAction(() -> minecraftClient.setScreen(new AssistScreen()));
    }

    private void triggerMacroForInput(int n) {
        for (AssistItemProvider provider : this.activeMacroEntries) {
            if (!provider.isAvailable() || !KeyBindingUtil.matches(provider.getKeyCode(), n)) continue;
            ItemSwapManager.getInstance().swap(provider.getItemStack().getItem(), provider::matches, provider.getDisplayName());
            return;
        }
    }

    public final void setMacroEntries(List<AssistItemProvider> list) {
        this.activeMacroEntries.clear();
        this.activeMacroEntries.addAll(list);
    }

    @Generated
    public List<AssistItemProvider> getMacroEntries() {
        return this.macroEntries;
    }

    @Generated
    public List<AssistItemProvider> getActiveMacroEntries() {
        return this.activeMacroEntries;
    }
}
