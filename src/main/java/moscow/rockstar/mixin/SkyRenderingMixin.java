/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.SkyRendering
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.world.Ambience;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.world.SkyboxRenderer;
import moscow.rockstar.render.world.SkyboxShaderPair;
import net.minecraft.client.render.SkyRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.utility.render.ColorRGBA;

@Mixin(value={SkyRendering.class})
public class SkyRenderingMixin {
    @Inject(method={"renderSky"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderCustomSkybox(float f, float f2, float f3, CallbackInfo callbackInfo) {
        Ambience ambience = SkyRenderingMixin.rockstar$ambience();
        if (ambience == null || !ambience.isFogReady()) {
            return;
        }
        if (!ambience.isShaderReady()) {
            return;
        }
        SkyboxRenderer.renderTexture(ambience.getSkyboxTexture(), SkyRenderingMixin.rockstar$skyTint(ambience));
        if (ambience.isRainReady()) {
            SkyRenderingMixin.rockstar$renderShader(ambience);
        }
        callbackInfo.cancel();
    }

    @Inject(method={"renderSky"}, at={@At(value="RETURN")})
    private void rockstar$renderShaderOverVanillaSky(float f, float f2, float f3, CallbackInfo callbackInfo) {
        Ambience ambience = SkyRenderingMixin.rockstar$ambience();
        if (ambience == null || !ambience.isFogReady()) {
            return;
        }
        if (ambience.isShaderReady() || !ambience.isRainReady()) {
            return;
        }
        SkyRenderingMixin.rockstar$renderShader(ambience);
    }

    @Unique
    private static Ambience rockstar$ambience() {
        if (RockstarClient.create().getModuleRegistry() == null) {
            return null;
        }
        return RockstarClient.create().getModuleRegistry().getModule(Ambience.class);
    }

    @Unique
    private static ColorRGBA rockstar$skyTint(Ambience ambience) {
        return ambience.getSkyChangeOption().isSelected() ? (ambience.getThemeSyncSetting().isEnabled() ? ColorPalette.getAccentColor() : ambience.getSkyColorSetting().getColor()) : ColorRGBA.WHITE;
    }

    @Unique
    private static void rockstar$renderShader(Ambience ambience) {
        float f = (float)(System.currentTimeMillis() % 100000000L) / 1000.0f;
        SkyboxShaderPair shaderPair = ambience.getShaderRenderer();
        if (shaderPair != null) {
            SkyboxRenderer.renderUiComponent(shaderPair, SkyRenderingMixin.rockstar$skyTint(ambience), f, ambience.calculateNightBrightness());
        } else {
            SkyboxRenderer.renderShaderEffect(ambience.getShaderResource(), SkyRenderingMixin.rockstar$skyTint(ambience), f, ambience.calculateNightBrightness());
        }
    }

    @Inject(method={"close"}, at={@At(value="HEAD")})
    private void closeCustomSkybox(CallbackInfo callbackInfo) {
        SkyboxRenderer.clearRenderCaches();
    }

    @Redirect(method={"renderStars"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V", ordinal=0))
    private void redirectStarColor(float f, float f2, float f3, float f4) {
        ColorRGBA colorRGBA;
        if (RockstarClient.create().getModuleRegistry() == null) {
            RenderSystem.setShaderColor((float)f, (float)f2, (float)f3, (float)f4);
            return;
        }
        Ambience ambience = RockstarClient.create().getModuleRegistry().getModule(Ambience.class);
        if (ambience == null) {
            RenderSystem.setShaderColor((float)f, (float)f2, (float)f3, (float)f4);
            return;
        }
        ColorRGBA colorRGBA2 = colorRGBA = ambience.getThemeSyncSetting().isEnabled() ? ColorPalette.getAccentColor() : ambience.getStarsColorSetting().getColor();
        if (ambience.isEnabled() && ambience.getStarsChangeOption().isSelected()) {
            RenderSystem.setShaderColor((float)(colorRGBA.getRed() / 255.0f), (float)(colorRGBA.getGreen() / 255.0f), (float)(colorRGBA.getBlue() / 255.0f), (float)(colorRGBA.getAlpha() / 255.0f));
        } else {
            RenderSystem.setShaderColor((float)f, (float)f2, (float)f3, (float)f4);
        }
    }
}
