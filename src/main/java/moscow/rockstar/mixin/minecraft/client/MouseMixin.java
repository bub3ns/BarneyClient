/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Mouse
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
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.CursorManager;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.events.window.MouseButtonEvent;
import pyrock.events.window.MouseEvent;
import pyrock.events.window.MouseMoveEvent;
import pyrock.events.window.MouseScrollEvent;
import pyrock.events.window.ScrollEvent;

@Mixin(value={Mouse.class})
public class MouseMixin
implements ClientAccess {
    @Unique
    private double rockstar$lastCursorX;
    @Unique
    private double rockstar$lastCursorY;

    @Inject(method={"tick()V"}, at={@At(value="RETURN")})
    private void tick(CallbackInfo callbackInfo) {
        CursorManager.applyAndReset(minecraftClient);
    }

    @Inject(method={"onMouseButton"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseButton(long l, int n, int n2, int n3, CallbackInfo callbackInfo) {
        if (n2 == 1) {
            if (GLFW.glfwGetKey((long)l, (int)292) == 1) {
                return;
            }
            RockstarClient.create().getEventBus().post(new MouseEvent(n, n2));
        }
        MouseButtonEvent mouseButtonEvent = new MouseButtonEvent(n, n2, n3);
        RockstarClient.create().getEventBus().post(mouseButtonEvent);
        if (mouseButtonEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"onMouseScroll"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseScroll(long l, double d, double d2, CallbackInfo callbackInfo) {
        if (d2 != 0.0) {
            RockstarClient.create().getEventBus().post(new MouseScrollEvent(d2));
        }
        ScrollEvent scrollEvent = new ScrollEvent(d, d2);
        RockstarClient.create().getEventBus().post(scrollEvent);
        if (scrollEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"onCursorPos"}, at={@At(value="HEAD")})
    private void onCursorPos(long l, double d, double d2, CallbackInfo callbackInfo) {
        if (l != minecraftClient.getWindow().getHandle()) {
            return;
        }
        double d3 = d - this.rockstar$lastCursorX;
        double d4 = d2 - this.rockstar$lastCursorY;
        this.rockstar$lastCursorX = d;
        this.rockstar$lastCursorY = d2;
        RockstarClient.create().getEventBus().post(new MouseMoveEvent(d, d2, d3, d4));
    }
}
