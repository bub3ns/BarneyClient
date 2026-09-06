/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.DrawContext
 *  net.minecraft.DebugHud
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.hud.NameProtect;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={DebugHud.class})
public class DebugHudMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void hideDebugHud(DrawContext ServerConfigException, CallbackInfo callbackInfo) {
        NameProtect nameProtect = RockstarClient.create().getModuleRegistry().getModule(NameProtect.class);
        if (nameProtect != null && nameProtect.isStreamerModeActive()) {
            callbackInfo.cancel();
        }
    }
}

