/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Screen
 *  net.minecraft.TitleScreen
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.screens.MainMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TitleScreen.class})
public class TitleScreenMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void openCustomMainMenu(CallbackInfo callbackInfo) {
        if (RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }
        callbackInfo.cancel();
        MinecraftClient.getInstance().setScreen(new MainMenuScreen());
    }
}
