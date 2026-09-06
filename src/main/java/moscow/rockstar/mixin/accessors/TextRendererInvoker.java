/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.TextRenderer
 *  net.minecraft.TextRenderer$TextLayerType
 *  net.minecraft.VertexConsumerProvider
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={TextRenderer.class})
public interface TextRendererInvoker {
    @Invoker(value="drawInternal")
    public int rockstar$drawInternal(String var1, float var2, float var3, int var4, boolean var5, Matrix4f var6, VertexConsumerProvider var7, TextRenderer.TextLayerType var8, int var9, int var10, boolean var11);
}

