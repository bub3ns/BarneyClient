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
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.font;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.hud.NameProtect;
import moscow.rockstar.render.text.TextCaptureController;
import moscow.rockstar.render.text.TextReplacementRenderer;
import moscow.rockstar.ui.text.OrderedTextTransformer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={TextRenderer.class})
public abstract class TextRendererMixin {
    @Inject(method={"drawInternal(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$splitPlainText(String string, float f, float f2, int n, boolean bl, Matrix4f matrix4f, VertexConsumerProvider class_45972, TextRenderer.TextLayerType class_64152, int n2, int n3, boolean bl2, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        if (TextReplacementRenderer.isRenderingPatch() || !TextCaptureController.isCaptureActive()) {
            return;
        }
        NameProtect nameProtect = RockstarClient.create().getModuleRegistry().getModule(NameProtect.class);
        if (nameProtect == null || !nameProtect.containsProtectedText(string)) {
            return;
        }
        String string2 = nameProtect.replaceProtectedText(string);
        if (string2.equals(string)) {
            return;
        }
        callbackInfoReturnable.setReturnValue(TextReplacementRenderer.renderPlain(
            (TextRenderer)(Object)this,
            string,
            string2,
            f,
            f2,
            n,
            bl,
            matrix4f,
            class_64152,
            n2,
            n3,
            bl2
        ));
    }

    @Inject(method={"drawInternal(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$splitStyledText(OrderedText class_54812, float f, float f2, int n, boolean bl, Matrix4f matrix4f, VertexConsumerProvider class_45972, TextRenderer.TextLayerType class_64152, int n2, int n3, boolean bl2, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        if (TextReplacementRenderer.isRenderingPatch() || !TextCaptureController.isCaptureActive()) {
            return;
        }
        NameProtect nameProtect = RockstarClient.create().getModuleRegistry().getModule(NameProtect.class);
        if (nameProtect == null || !nameProtect.containsProtectedText(OrderedTextTransformer.toPlainText(class_54812))) {
            return;
        }
        OrderedText class_54813 = OrderedTextTransformer.replaceProtectedText(class_54812, nameProtect);
        if (class_54813 == null) {
            return;
        }
        callbackInfoReturnable.setReturnValue(TextReplacementRenderer.renderStyled(
            (TextRenderer)(Object)this,
            class_54812,
            class_54813,
            f,
            f2,
            n,
            bl,
            matrix4f,
            class_64152,
            n2,
            n3,
            bl2
        ));
    }

    @Inject(method={"drawWithOutline"}, at={@At(value="HEAD")}, cancellable=true)
    private void rockstar$maskOutlinedText(OrderedText class_54812, float f, float f2, int n, int n2, Matrix4f matrix4f, VertexConsumerProvider class_45972, int n3, CallbackInfo callbackInfo) {
        if (TextReplacementRenderer.isRenderingPatch() || !TextCaptureController.isCaptureActive()) {
            return;
        }
        NameProtect nameProtect = RockstarClient.create().getModuleRegistry().getModule(NameProtect.class);
        if (nameProtect == null || !nameProtect.containsProtectedText(OrderedTextTransformer.toPlainText(class_54812))) {
            return;
        }
        OrderedText class_54813 = OrderedTextTransformer.replaceProtectedText(class_54812, nameProtect);
        if (class_54813 == null) {
            return;
        }
        TextReplacementRenderer.renderOutline(
            (TextRenderer)(Object)this,
            class_54813,
            f,
            f2,
            n,
            n2,
            matrix4f,
            class_45972,
            n3
        );
        callbackInfo.cancel();
    }
}
