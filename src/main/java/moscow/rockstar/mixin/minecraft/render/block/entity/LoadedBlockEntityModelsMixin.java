/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LoadedBlockEntityModels
 *  net.minecraft.SpecialModelRenderer
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.AbstractSkullBlock
 *  net.minecraft.Block
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.ClientPlayerEntity
 *  net.minecraft.ModelTransformationMode
 *  net.minecraft.ProfileComponent
 *  net.minecraft.DataComponentTypes
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.render.block.entity;

import java.util.Map;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.render.item.HeldItemRenderCapture;
import net.minecraft.client.render.block.entity.LoadedBlockEntityModels;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.component.DataComponentTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LoadedBlockEntityModels.class})
public abstract class LoadedBlockEntityModelsMixin {
    @Shadow
    @Final
    private Map<Block, SpecialModelRenderer<?>> renderers;

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$renderHeldSkullWithProfile(Block class_22482, ModelTransformationMode DeathMessageType, MatrixStack class_45872, VertexConsumerProvider class_45972, int n, int n2, CallbackInfo callbackInfo) {
        if (!HeldItemRenderCapture.capturing || !(class_22482 instanceof AbstractSkullBlock)) {
            return;
        }
        ProfileComponent class_92962 = LoadedBlockEntityModelsMixin.rockstar$heldProfile(class_22482);
        if (class_92962 == null) {
            return;
        }
        SpecialModelRenderer<?> class_105152 = this.renderers.get(class_22482);
        if (class_105152 == null) {
            return;
        }
        ((SpecialModelRenderer)class_105152).render(class_92962, DeathMessageType, class_45872, class_45972, n, n2, false);
        callbackInfo.cancel();
    }

    @Unique
    private static ProfileComponent rockstar$heldProfile(Block class_22482) {
        ClientPlayerEntity class_7462 = MinecraftClient.getInstance().player;
        if (class_7462 == null) {
            return null;
        }
        ProfileComponent class_92962 = LoadedBlockEntityModelsMixin.rockstar$profileOf(class_7462.getMainHandStack(), class_22482);
        return class_92962 != null ? class_92962 : LoadedBlockEntityModelsMixin.rockstar$profileOf(class_7462.getOffHandStack(), class_22482);
    }

    @Unique
    private static ProfileComponent rockstar$profileOf(ItemStack class_17992, Block class_22482) {
        if (class_17992 == null || class_17992.isEmpty() || Block.getBlockFromItem((Item)class_17992.getItem()) != class_22482) {
            return null;
        }
        return (ProfileComponent)class_17992.get(DataComponentTypes.PROFILE);
    }
}
