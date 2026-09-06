/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Sprite
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.InGameOverlayRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.overlay.Removals;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={InGameOverlayRenderer.class})
public class InGameOverlayRendererMixin {
    @Inject(method={"renderFireOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private static void renderFireOverlayHook(MatrixStack class_45872, VertexConsumerProvider class_45972, CallbackInfo callbackInfo) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.isEnabled() && removals.getFire().isSelected()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"renderInWallOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private static void renderInWallOverlayHook(Sprite class_10582, MatrixStack class_45872, VertexConsumerProvider class_45972, CallbackInfo callbackInfo) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.isEnabled() && removals.getClip().isSelected()) {
            callbackInfo.cancel();
        }
    }
}

