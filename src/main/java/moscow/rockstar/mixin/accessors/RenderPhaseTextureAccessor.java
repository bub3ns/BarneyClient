/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Identifier
 *  net.minecraft.RenderPhase$Texture
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.accessors;

import java.util.Optional;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={RenderPhase.Texture.class})
public interface RenderPhaseTextureAccessor {
    @Accessor(value="id")
    public Optional<Identifier> rockstar$getId();
}

