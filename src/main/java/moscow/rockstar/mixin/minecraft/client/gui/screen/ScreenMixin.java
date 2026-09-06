/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ClickEvent
 *  net.minecraft.ClickEvent$Action
 *  net.minecraft.Style
 *  net.minecraft.Screen
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.text.TextCaptureController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Screen.class})
public class ScreenMixin
implements ClientAccess {
    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void rockstar$markScreenRender(CallbackInfo callbackInfo) {
        TextCaptureController.setScreenRendering(true);
    }

    @Inject(method={"render"}, at={@At(value="RETURN")})
    private void rockstar$unmarkScreenRender(CallbackInfo callbackInfo) {
        TextCaptureController.setScreenRendering(false);
    }

    @Inject(method={"handleTextClick"}, at={@At(value="HEAD")}, cancellable=true)
    private void onHandleTextClick(Style class_25832, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        String string;
        ClickEvent class_25582 = class_25832.getClickEvent();
        if (class_25582 != null && class_25582.getAction() == ClickEvent.Action.RUN_COMMAND && (string = class_25582.getValue()).startsWith(RockstarClient.create().getNavigationCommandService().getCommandPrefix())) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                RockstarClient.create().getNavigationCommandService().executeCommand(string);
                callbackInfoReturnable.setReturnValue(true);
                callbackInfoReturnable.cancel();
            }
        }
    }
}
