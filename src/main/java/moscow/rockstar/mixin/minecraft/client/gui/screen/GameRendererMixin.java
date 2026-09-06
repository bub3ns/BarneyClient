/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.Entity
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 *  net.minecraft.MatrixStack
 *  net.minecraft.GameRenderer
 *  net.minecraft.RenderTickCounter
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.visuals.overlay.Removals;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.render.text.TextCaptureController;
import moscow.rockstar.render.diagnostics.DrawCallCounter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.events.render.GameRendererEvent;

@Mixin(value={GameRenderer.class})
public abstract class GameRendererMixin {
    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/MinecraftClient;isFinishedLoading()Z", shift=At.Shift.AFTER)})
    public void triggerGameRendererEvent(RenderTickCounter class_97792, boolean bl, CallbackInfo callbackInfo) {
        RockstarClient.create().getEventBus().post(new GameRendererEvent());
    }

    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void rockstar$resetDrawCalls(RenderTickCounter class_97792, boolean bl, CallbackInfo callbackInfo) {
        DrawCallCounter.reset();
    }

    @Inject(method={"renderWorld"}, at={@At(value="RETURN")})
    private void rockstar$applyWorldPatches(RenderTickCounter class_97792, CallbackInfo callbackInfo) {
        TextCaptureController.applyCapturePhase(0);
    }

    @Redirect(method={"findCrosshairTarget"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;getRotationVec(F)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d rockstar$useServerRotationForEntityRaytrace(Entity class_12972, float f) {
        if (class_12972 != MinecraftClient.getInstance().player) {
            return class_12972.getRotationVec(f);
        }
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        return rotationManager.isIdle() ? class_12972.getRotationVec(f) : rotationManager.getCurrentRotation().toDirectionVector();
    }

    @Inject(method={"renderWorld"}, at={@At(value="INVOKE_STRING", target="Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args={"ldc=hand"})})
    private void onRenderWorld(RenderTickCounter class_97792, CallbackInfo callbackInfo, @Local(ordinal=0) Matrix4f matrix4f, @Local(ordinal=2) Matrix4f matrix4f2, @Local(ordinal=1) float f, @Local MatrixStack class_45872) {
        ProjectionUtils.setMatrices(matrix4f2, matrix4f);
    }

    @Inject(method={"tiltViewWhenHurt"}, at={@At(value="HEAD")}, cancellable=true)
    private void tiltViewWhenHurtHook(MatrixStack class_45872, float f, CallbackInfo callbackInfo) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.isEnabled() && removals.getHurtCam().isSelected()) {
            callbackInfo.cancel();
        }
    }

    @Redirect(method={"renderWorld"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float renderWorldHook(float f, float f2, float f3) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.isEnabled() && removals.getNausea().isSelected()) {
            return 0.0f;
        }
        return MathHelper.lerp((float)f, (float)f2, (float)f3);
    }
}
