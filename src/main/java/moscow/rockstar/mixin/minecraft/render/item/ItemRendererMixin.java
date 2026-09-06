/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.BakedQuad
 *  net.minecraft.ItemRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package moscow.rockstar.mixin.minecraft.render.item;

import moscow.rockstar.render.item.ItemRenderStateRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={ItemRenderer.class})
public class ItemRendererMixin {
    /**
     * The {@code ItemRenderStateRenderer.insetQuad(...)} call is the ORIGINAL behaviour and is
     * preserved verbatim. The injector itself is unchanged - same {@code @ModifyArg}, same target
     * descriptor, same index 1.
     *
     * <p>NOTE: a FillSilhouetteFilter that collapsed the builtin/generated extrusion rim
     * quads during the ESP Fill capture was tried here and REVERTED at the user's request.
     * The rim adds no model-space area, but under the first-person display transform it
     * projects beside the sprite face, so removing it shrank the fill silhouette below the
     * item's real outline and left visibly missing pixels along the edge ("now some back
     * pixels are missing"). Do not reintroduce it without solving that.</p>
     */
    @ModifyArg(method={"renderBakedItemQuads"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/VertexConsumer;quad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;FFFFII)V"), index=1)
    private static BakedQuad rockstar$insetUv(BakedQuad Atlas) {
        return ItemRenderStateRenderer.insetQuad(Atlas);
    }
}
