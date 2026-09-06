/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.RenderLayer$MultiPhaseParameters
 *  net.minecraft.RenderPhase$Transparency
 *  net.minecraft.RenderPhase$TextureBase
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={RenderLayer.MultiPhaseParameters.class})
public interface MultiPhaseParametersAccessor {
    @Accessor(value="texture")
    public RenderPhase.TextureBase rockstar$getTexture();

    @Accessor(value="transparency")
    public RenderPhase.Transparency rockstar$getTransparency();
}

