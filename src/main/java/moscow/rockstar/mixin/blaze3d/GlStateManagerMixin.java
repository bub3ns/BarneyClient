/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.blaze3d;

import com.mojang.blaze3d.platform.GlStateManager;
import moscow.rockstar.render.diagnostics.DrawCallCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GlStateManager.class})
public class GlStateManagerMixin {
    @Inject(method={"_drawElements(IIIJ)V"}, at={@At(value="HEAD")})
    private static void rockstar$countDrawCall(int n, int n2, int n3, long l, CallbackInfo callbackInfo) {
        DrawCallCounter.recordDrawCall();
    }
}
