/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.StatusEffects
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.MathHelper
 *  net.minecraft.Camera
 *  net.minecraft.FogShape
 *  net.minecraft.BackgroundRenderer
 *  net.minecraft.BackgroundRenderer$FogType
 *  net.minecraft.Fog
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.overlay.Removals;
import moscow.rockstar.modules.visuals.world.CustomFog;
import moscow.rockstar.render.colors.ColorPalette;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Fog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyrock.utility.render.ColorRGBA;

@Mixin(value={BackgroundRenderer.class})
public class BackgroundRendererMixin {
    @Inject(method={"getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onGetFogModifier(Entity class_12972, float f, CallbackInfoReturnable<Object> callbackInfoReturnable) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (!removals.isEnabled()) {
            return;
        }
        if (class_12972 instanceof LivingEntity) {
            LivingEntity class_13092 = (LivingEntity)class_12972;
            if (class_13092.hasStatusEffect(StatusEffects.BLINDNESS)) {
                if (removals.getBlindness().isSelected()) {
                    callbackInfoReturnable.setReturnValue(null);
                }
            } else if (class_13092.hasStatusEffect(StatusEffects.DARKNESS) && removals.getDarkness().isSelected()) {
                callbackInfoReturnable.setReturnValue(null);
            }
        }
    }

    @ModifyReturnValue(method={"applyFog"}, at={@At(value="RETURN")})
    private static Fog modifyFogProperties(Fog class_99582, @Local(argsOnly=true) Camera class_41842, @Local(argsOnly=true) BackgroundRenderer.FogType class_45962, @Local(argsOnly=true, ordinal=0) float f) {
        CustomFog customFog = RockstarClient.create().getModuleRegistry().getModule(CustomFog.class);
        if (customFog.shouldApplyFog(class_41842) && class_45962 == BackgroundRenderer.FogType.FOG_TERRAIN) {
            float f2 = MathHelper.clamp((float)customFog.getDistanceSetting().getFirstValue(), (float)-8.0f, (float)f);
            float f3 = MathHelper.clamp((float)customFog.getDistanceSetting().getSecondValue(), (float)0.0f, (float)f);
            ColorRGBA colorRGBA = customFog.getSyncWithThemeSetting().isEnabled() ? ColorPalette.getAccentColor().withAlpha(customFog.getSynchronizedAlphaSetting().getValue() / 100.0f * 255.0f) : customFog.getFogColorSetting().getColor();
            FogShape class_68542 = FogShape.SPHERE;
            float f4 = colorRGBA.getRed() / 255.0f;
            float f5 = colorRGBA.getGreen() / 255.0f;
            float f6 = colorRGBA.getBlue() / 255.0f;
            float f7 = colorRGBA.getAlpha() / 255.0f;
            return new Fog(f2, f3, class_68542, f4, f5, f6, f7);
        }
        return class_99582;
    }
}

