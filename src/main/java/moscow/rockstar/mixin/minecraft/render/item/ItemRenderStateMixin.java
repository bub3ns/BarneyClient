/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemRenderState$LayerRenderState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package moscow.rockstar.mixin.minecraft.render.item;

import moscow.rockstar.modules.visuals.esp.entities.Glow;
import moscow.rockstar.modules.visuals.esp.entities.Fill;
import moscow.rockstar.modules.visuals.esp.entities.Flame;
import net.minecraft.client.render.item.ItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={ItemRenderState.LayerRenderState.class})
public class ItemRenderStateMixin {
    @ModifyArg(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II[ILnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V"), index=3)
    private int chams$modifyItemLight(int n) {
        if (Glow.itemGlowRendering || Fill.fillRendering || Flame.itemRenderInProgress) {
            return 0xF000F0;
        }
        return n;
    }

    @ModifyArg(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/item/model/special/SpecialModelRenderer;render(Ljava/lang/Object;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V"), index=4)
    private int chams$modifySpecialModelLight(int n) {
        if (Glow.itemGlowRendering || Fill.fillRendering || Flame.itemRenderInProgress) {
            return 0xF000F0;
        }
        return n;
    }
}
