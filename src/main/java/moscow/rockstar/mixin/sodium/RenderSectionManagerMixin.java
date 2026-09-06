/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 */
package moscow.rockstar.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.world.CustomFog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets={"net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager"}, remap=false)
public class RenderSectionManagerMixin
implements ClientAccess {
    @ModifyReturnValue(method={"getEffectiveRenderDistance(Lnet/minecraft/client/render/Fog;)F", "getEffectiveRenderDistance(Lnet/minecraft/Fog;)F"}, at={@At(value="RETURN")}, require=0)
    private float keepRenderDistance(float f) {
        if (RenderSectionManagerMixin.minecraftClient.options == null || RenderSectionManagerMixin.minecraftClient.gameRenderer == null) {
            return f;
        }
        CustomFog customFog = RockstarClient.create().getModuleRegistry().getModule(CustomFog.class);
        if (customFog == null || !customFog.shouldApplyFog(RenderSectionManagerMixin.minecraftClient.gameRenderer.getCamera())) {
            return f;
        }
        return Math.max(f, (float)RenderSectionManagerMixin.minecraftClient.options.getClampedViewDistance() * 16.0f);
    }
}

