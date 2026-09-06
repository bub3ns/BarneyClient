/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.RenderLayer$MultiPhase
 *  net.minecraft.RenderLayer$MultiPhaseParameters
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={RenderLayer.MultiPhase.class})
public interface RenderLayerMultiPhaseAccessor {
    @Accessor(value="phases")
    public RenderLayer.MultiPhaseParameters rockstar$getPhases();
}

