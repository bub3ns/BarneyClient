/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.TextRenderer
 *  net.minecraft.TextRenderer$TextLayerType
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.OrderedText
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={TextRenderer.class})
public interface TextRendererStyledInvoker {
    @Invoker(value="drawInternal")
    public int rockstar$drawInternal(OrderedText var1, float var2, float var3, int var4, boolean var5, Matrix4f var6, VertexConsumerProvider var7, TextRenderer.TextLayerType var8, int var9, int var10, boolean var11);
}

