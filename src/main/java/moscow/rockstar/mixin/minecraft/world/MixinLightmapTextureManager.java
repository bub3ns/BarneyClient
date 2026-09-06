/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.ShaderProgram
 *  net.minecraft.SimpleFramebuffer
 *  net.minecraft.LightmapTextureManager
 *  org.joml.Vector3f
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.world;

import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.overlay.Removals;
import moscow.rockstar.modules.visuals.world.Ambience;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.LightmapTextureManager;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LightmapTextureManager.class})
public class MixinLightmapTextureManager {
    @Shadow
    @Final
    private SimpleFramebuffer lightmapFramebuffer;
    @Shadow
    private boolean dirty;
    @Unique
    private boolean rockstar$nightWasActive;

    @Inject(method={"update"}, at={@At(value="HEAD")})
    private void rockstar$forceNightModeRefresh(float f, CallbackInfo callbackInfo) {
        boolean bl;
        Ambience ambience = RockstarClient.create().getModuleRegistry().getModule(Ambience.class);
        boolean bl2 = bl = ambience != null && ambience.isWeatherRenderReady();
        if (bl || this.rockstar$nightWasActive) {
            this.dirty = true;
        }
        this.rockstar$nightWasActive = bl;
    }

    @Inject(method={"getDarknessFactor"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetDarknessFactor(float f, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.isEnabled() && removals.getDarkness().isSelected()) {
            callbackInfoReturnable.setReturnValue(0.0f);
        }
    }

    @Inject(method={"update"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gl/SimpleFramebuffer;beginWrite(Z)V")})
    private void rockstar$applyNightModeUniforms(float f, CallbackInfo callbackInfo, @Local ShaderProgram class_59442) {
        Ambience ambience = RockstarClient.create().getModuleRegistry().getModule(Ambience.class);
        if (ambience == null || !ambience.isWeatherRenderReady()) {
            class_59442.getUniformOrDefault("BarneyNightStrength").set(0.0f);
            class_59442.getUniformOrDefault("BarneyNightTint").set(1.0f, 1.0f, 1.0f);
            return;
        }
        Vector3f vector3f = ambience.getCameraPositionVector();
        float f2 = ambience.getFogDistance();
        class_59442.getUniformOrDefault("BarneyNightTint").set(vector3f);
        class_59442.getUniformOrDefault("BarneyNightStrength").set(f2);
    }
}
