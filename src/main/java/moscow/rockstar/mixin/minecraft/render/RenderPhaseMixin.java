/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.RenderPhase
 *  net.minecraft.RenderPhase$Target
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.render;

import moscow.rockstar.modules.visuals.esp.entities.Glow;
import moscow.rockstar.modules.visuals.esp.entities.Fill;
import moscow.rockstar.modules.visuals.esp.entities.Flame;
import moscow.rockstar.modules.visuals.hand.ViewModel;
import moscow.rockstar.render.item.HeldItemRenderCapture;
import moscow.rockstar.render.text.TextCaptureController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={RenderPhase.class})
public abstract class RenderPhaseMixin {
    @Inject(method={"startDrawing"}, at={@At(value="HEAD")}, cancellable=true)
    private void chams$skipTargetStart(CallbackInfo callbackInfo) {
        if (this.chams$capturingElsewhere() && (Object)this instanceof RenderPhase.Target) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"endDrawing"}, at={@At(value="HEAD")}, cancellable=true)
    private void chams$skipTargetEnd(CallbackInfo callbackInfo) {
        if (this.chams$capturingElsewhere() && (Object)this instanceof RenderPhase.Target) {
            callbackInfo.cancel();
        }
    }

    @Unique
    private boolean chams$capturingElsewhere() {
        return Glow.entityGlowRendering || Glow.itemGlowRendering
            || Fill.fillRendering || Flame.itemRenderInProgress
            || HeldItemRenderCapture.rendering || ViewModel.renderingHands
            || TextCaptureController.isCaptureInProgress();
    }
}
