/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.events.render;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.screens.MenuScreenBase;
import moscow.rockstar.ui.screens.MinecraftScreenBase;
import moscow.rockstar.ui.screens.ModuleSettingsScreen;
import net.minecraft.client.MinecraftClient;
import pyrock.events.render.HudRenderEvent;
import pyrock.events.render.Render3DEvent;

public class HudEventForwarder
implements ClientAccess {
    /**
     * ORIGINAL rockstar/ilIlil/IiiIiIIII#I (Lpyrock/events/render/HudRenderEvent;)V — the per-frame
     * menu open/close lifecycle driver. Declared FIRST so that it registers before the capture
     * listeners, matching the original registration order.
     */
    private final EventListener<HudRenderEvent> menuStateListener = hudRenderEvent -> {
        MinecraftScreenBase stored = RockstarClient.create().getMinecraftScreen();
        if (minecraftClient.currentScreen == null
                && RockstarClient.create().getModuleRegistry().getModule(Menu.class).getModernOption().isSelected()
                && !(stored instanceof ModuleSettingsScreen)) {
            RockstarClient.create().setMinecraftScreen(new ModuleSettingsScreen());
        }
        // ORIGINAL: bl = currentScreen instanceof IiiIIiiiI || currentScreen instanceof IiiIiIiii
        //           (the dropdown menu screen has no remapped counterpart).
        boolean bl = minecraftClient.currentScreen instanceof MenuScreenBase;
        if (!bl && RockstarClient.create().getModuleRegistry().getModule(Menu.class).isEnabled()) {
            RockstarClient.create().getModuleRegistry().getModule(Menu.class).setEnabled(false);
        }
        if (!(stored instanceof MenuScreenBase)) {
            return;
        }
        MenuScreenBase menuScreen = (MenuScreenBase)stored;
        menuScreen.getMenuAnimation().update(menuScreen.isClosing() ? 0.0f : 1.0f);
        /* DropdownMenuScreen closing animation render - commented out (panels mode disabled):
        if (!(menuScreen instanceof ModuleSettingsScreen)
                && menuScreen.getMenuAnimation().getValue() > 0.1f
                && !(minecraftClient.currentScreen instanceof MenuScreenBase)
                && menuScreen.isClosing()) {
            menuScreen.render(RockstarDrawContext.create(hudRenderEvent.getContext(), -1, -1,
                    MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)));
        }
        */
    };
    private final EventListener<HudRenderEvent> hudListener = ModuleSettingsScreen::renderClosingHud;
    private final EventListener<Render3DEvent> worldListener = EventListener.withPriority(Integer.MIN_VALUE, ModuleSettingsScreen::renderClosingWorld);

    public HudEventForwarder() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }
}
