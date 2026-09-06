/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemRenderState
 *  net.minecraft.DrawContext
 *  net.minecraft.VertexConsumerProvider$Immediate
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={DrawContext.class})
public interface DrawContextAccessor {
    @Accessor(value="vertexConsumers")
    public VertexConsumerProvider.Immediate getVertexConsumers();

    @Accessor(value="itemRenderState")
    public ItemRenderState getItemRenderState();
}

