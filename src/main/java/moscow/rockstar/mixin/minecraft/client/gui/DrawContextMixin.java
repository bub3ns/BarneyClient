/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemRenderState
 *  net.minecraft.DrawContext
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package moscow.rockstar.mixin.minecraft.client.gui;

import moscow.rockstar.render.item.ItemRenderStateRenderer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import pyrock.utility.render.CustomDrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={DrawContext.class})
public class DrawContextMixin {
    @Redirect(method={"drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/item/ItemRenderState;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V"))
    private void rockstar$smoothItem(ItemRenderState class_104442, MatrixStack class_45872, VertexConsumerProvider class_45972, int n, int n2) {
        if ((Object)this instanceof CustomDrawContext) {
            ItemRenderStateRenderer.render(class_104442, class_45872, class_45972, n, n2);
        } else {
            class_104442.render(class_45872, class_45972, n, n2);
        }
    }
}
