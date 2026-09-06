/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3d
 *  net.minecraft.WorldRenderer
 *  net.minecraft.FrameGraphBuilder
 *  net.minecraft.Fog
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.render;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.overlay.Removals;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.Fog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={WorldRenderer.class})
public abstract class WeatherRendererMixin
implements ClientAccess {
    @Inject(method={"renderWeather"}, at={@At(value="HEAD")})
    private void onRenderWeather(FrameGraphBuilder class_99092, Vec3d VanillaChestLootTableGenerator, float f, Fog class_99582, CallbackInfo callbackInfo) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.isEnabled() && removals.getWeather().isSelected() && WeatherRendererMixin.minecraftClient.world != null) {
            WeatherRendererMixin.minecraftClient.world.setRainGradient(0.0f);
            WeatherRendererMixin.minecraftClient.world.setThunderGradient(0.0f);
        }
    }
}

