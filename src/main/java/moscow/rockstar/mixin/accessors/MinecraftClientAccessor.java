/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Framebuffer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={MinecraftClient.class})
public interface MinecraftClientAccessor {
    @Invoker(value="updateWindowTitle")
    public void invokeUpdateWindowTitle();

    @Mutable
    @Accessor(value="framebuffer")
    public void setFramebuffer(Framebuffer var1);
}

