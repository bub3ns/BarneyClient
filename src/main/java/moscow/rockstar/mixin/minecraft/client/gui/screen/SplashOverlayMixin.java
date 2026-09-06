/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Util
 *  net.minecraft.RenderLayer
 *  net.minecraft.DrawContext
 *  net.minecraft.MathHelper
 *  net.minecraft.ResourceReload
 *  net.minecraft.SplashOverlay
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import java.util.Optional;
import java.util.function.Consumer;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.gif.AnimatedGifRenderer;
import moscow.rockstar.render.state.RenderEventScope;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SplashOverlay.class})
public class SplashOverlayMixin implements ClientAccess {
    @Unique
    private AnimatedGifRenderer rockstar$loadingGif;

    @Unique
    private Animation rockstar$fadeOutAnimation;

    @Shadow
    private long reloadCompleteTime;

    @Shadow
    @Final
    private Consumer<Optional<Throwable>> exceptionHandler;

    @Shadow
    @Final
    private ResourceReload reload;

    @Shadow
    @Final
    private boolean reloading;

    @Shadow
    private long reloadStartTime;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initializeCustomSplash(
        MinecraftClient client,
        ResourceReload reload,
        Consumer<Optional<Throwable>> exceptionHandler,
        boolean reloading,
        CallbackInfo callbackInfo
    ) {
        // Ekran ladowania z animacja litery R zostal usuniety
        /*
        if (RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }
        Identifier loadingResource = RockstarClient.resourceId("gifs/loading.gif");
        rockstar$loadingGif = new AnimatedGifRenderer(loadingResource, 100.0f, 100.0f, 100.0f, 100.0f);
        rockstar$fadeOutAnimation = new Animation(3000L, 1.0f, Easing.easeInOutCubicPolynomial);
        */
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderCustomSplash(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo callbackInfo) {
        // Ekran ladowania z animacja litery R zostal usuniety - przywracamy domyslny ekran gry
        return;
        /*
        if (RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }
        callbackInfo.cancel();

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        RockstarDrawContext drawContext = RockstarDrawContext.create(context, 0, 0, delta);
        long now = Util.getMeasuringTimeMs();

        if (reloading && reloadStartTime == -1L) {
            reloadStartTime = now;
        }
        float completeSeconds = reloadCompleteTime > -1L
            ? (float) (now - reloadCompleteTime) / 1000.0f
            : -1.0f;
        float startSeconds = reloadStartTime > -1L
            ? (float) (now - reloadStartTime) / 500.0f
            : -1.0f;

        if (completeSeconds >= 1.0f) {
            if (minecraftClient.currentScreen != null) {
                minecraftClient.currentScreen.render(context, 0, 0, delta);
            }
            int alpha = MathHelper.ceil(MathHelper.clamp(1.0f - (completeSeconds - 1.0f), 0.0f, 1.0f) * 255.0f);
            context.fill(RenderLayer.getGuiOverlay(), 0, 0, width, height,
                ColorPalette.getPrimaryTextColor().withAlpha(alpha).getRGB());
        } else if (reloading && minecraftClient.currentScreen != null && startSeconds < 1.0f) {
            minecraftClient.currentScreen.render(context, mouseX, mouseY, delta);
            int alpha = MathHelper.floor(MathHelper.clamp(startSeconds, 0.15f, 1.0f) * 255.0f);
            context.fill(RenderLayer.getGuiOverlay(), 0, 0, width, height,
                ColorPalette.getPrimaryTextColor().withAlpha(alpha).getRGB());
        }

        if (completeSeconds < 1.0f) {
            float framebufferWidth = WindowMetricsProvider.INSTANCE.width();
            float framebufferHeight = WindowMetricsProvider.INSTANCE.height();
            float aspectRatio = framebufferWidth / framebufferHeight;
            float targetAspectRatio = 1.7777778f;
            float gifWidth;
            float gifHeight;
            float gifX;
            float gifY;
            if (aspectRatio > targetAspectRatio) {
                gifWidth = framebufferWidth;
                gifHeight = framebufferWidth / targetAspectRatio;
                gifX = 0.0f;
                gifY = (framebufferHeight - gifHeight) / 2.0f;
            } else {
                gifHeight = framebufferHeight;
                gifWidth = framebufferHeight * targetAspectRatio;
                gifX = (framebufferWidth - gifWidth) / 2.0f;
                gifY = 0.0f;
            }

            rockstar$loadingGif.setBounds(gifX, gifY, gifWidth, gifHeight);
            rockstar$loadingGif.setOpacity(1.0f);
            RenderEventScope.begin();
            try {
                rockstar$loadingGif.render(drawContext);
            } finally {
                RenderEventScope.end();
            }
        }

        if (completeSeconds >= 2.0f) {
            minecraftClient.setOverlay(null);
            rockstar$loadingGif.cleanup();
        }

        if (reloadCompleteTime == -1L && reload.isComplete() && (!reloading || startSeconds >= 2.0f)) {
            try {
                reload.throwException();
                exceptionHandler.accept(Optional.empty());
            } catch (Throwable throwable) {
                exceptionHandler.accept(Optional.of(throwable));
            }
            reloadCompleteTime = now;
            if (minecraftClient.currentScreen != null) {
                minecraftClient.currentScreen.init(minecraftClient, width, height);
            }
        }
        */
    }
}
