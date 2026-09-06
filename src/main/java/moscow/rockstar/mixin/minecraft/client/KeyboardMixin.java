/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Keyboard
 *  net.minecraft.ChatScreen
 *  net.minecraft.Screen
 *  org.lwjgl.glfw.GLFW
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.platform.WindowsImeApi;
import moscow.rockstar.ui.hud.HudElementRegistry;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.events.window.CharTypedEvent;
import pyrock.events.window.KeyEvent;
import pyrock.events.window.KeyPressEvent;

@Mixin(value={Keyboard.class})
public class KeyboardMixin
implements ClientAccess {
    @Inject(method={"onKey"}, at={@At(value="HEAD")}, cancellable=true)
    public void triggerKeyEvent(long l, int n, int n2, int n3, int n4, CallbackInfo callbackInfo) {
        if (n == -1) {
            return;
        }
        if (n != 292 && GLFW.glfwGetKey((long)l, (int)292) == 1) {
            return;
        }
        if (n3 == 0) {
            Menu.selectMenuIndex(n);
        }
        RockstarClient.create().getEventBus().post(new KeyPressEvent(n3, n));
        KeyEvent keyEvent = new KeyEvent(n, n2, n3, n4);
        RockstarClient.create().getEventBus().post(keyEvent);
        if (keyEvent.isCancelled() && !this.isEscapeHatch(n, n2)) {
            callbackInfo.cancel();
            return;
        }
        // ORIGINAL: KeyboardMixin#triggerKeyEvent block E - chat key (46) + PRESS (1) + the ALT
        // modifier bit (8) arms the Windows IME composition-cancel helper and returns WITHOUT
        // opening the chat screen. The remap had dropped the whole branch along with the helper
        // class rockstar/ilIlil/iIIIIiiIi. Placement is exact: after the KeyEvent cancel check and
        // before the "currentScreen != null" early return.
        if (n == 46 && n3 == 1 && (n4 & 8) != 0) {
            WindowsImeApi.armCompositionCancel(l);
            return;
        }
        if (KeyboardMixin.minecraftClient.currentScreen != null) {
            return;
        }
        if (n == 46 && n3 == 1) {
            minecraftClient.setScreen((Screen)new ChatScreen(""));
        }
    }

    @Inject(method={"onChar"}, at={@At(value="HEAD")}, cancellable=true)
    private void triggerCharEvent(long l, int n, int n2, CallbackInfo callbackInfo) {
        CharTypedEvent charTypedEvent = new CharTypedEvent(n, n2);
        RockstarClient.create().getEventBus().post(charTypedEvent);
        if (charTypedEvent.isCancelled()) {
            callbackInfo.cancel();
            return;
        }
        // ORIGINAL: the tail of KeyboardMixin#triggerCharEvent - every open HUD popup gets the
        // codepoint before vanilla does. Without this the popups' text fields can be focused but
        // never receive a keystroke.
        HudElementRegistry hudElementRegistry = RockstarClient.create().getHudElementRegistry();
        if (hudElementRegistry != null) {
            for (char c : Character.toChars(n)) {
                if (hudElementRegistry.charTyped(c, n2)) continue;
                return;
            }
            callbackInfo.cancel();
        }
    }

    @Unique
    private boolean isEscapeHatch(int n, int n2) {
        if (n == 256 || n == 46) {
            return true;
        }
        if (KeyboardMixin.minecraftClient.options == null) {
            return false;
        }
        return KeyboardMixin.minecraftClient.options.chatKey.matchesKey(n, n2) || KeyboardMixin.minecraftClient.options.commandKey.matchesKey(n, n2);
    }
}
