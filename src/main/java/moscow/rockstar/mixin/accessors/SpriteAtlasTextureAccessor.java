/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SpriteAtlasTexture
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={SpriteAtlasTexture.class})
public interface SpriteAtlasTextureAccessor {
    @Accessor(value="width")
    public int rockstar$getWidth();

    @Accessor(value="height")
    public int rockstar$getHeight();
}

