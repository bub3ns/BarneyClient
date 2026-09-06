package moscow.rockstar.render.target;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.shaders.TintShader;
import moscow.rockstar.util.Timer;
import net.minecraft.client.gl.Framebuffer;

/**
 * Owns the two ping-pong targets used for the full-screen Kawase blur.
 *
 * <p>The pass order intentionally follows the original client: the down
 * shader is bound once for the down passes, the up shader is bound once for
 * the up passes, and the Minecraft framebuffer remains the final read source
 * until all passes are complete.</p>
 */
public final class RenderTargetManager implements ClientAccess, WindowHandle {
    private static final long MIN_RENDER_INTERVAL_MILLIS = 25L;
    private static final float TARGET_SCALE = 0.5f;

    public static final Supplier<RenderTarget> DOWN_TARGET = Suppliers.memoize(
        () -> new RenderTarget(false).enableLinearFiltering()
    );
    public static final Supplier<RenderTarget> UP_TARGET = Suppliers.memoize(
        () -> new RenderTarget(false).enableLinearFiltering()
    );

    /** The framebuffer copied by the most recent blur pass. */
    public static Framebuffer sourceFramebuffer;

    private static TintShader downShader;
    private static TintShader upShader;
    private static boolean initialized;
    private static boolean alternatePassActive;

    private final Timer renderTimer = new Timer();
    private float blurRadius = 1.0f;
    private float resolutionScale = TARGET_SCALE;

    /** Kept as a singleton-compatible static facade for existing call sites. */
    private static final RenderTargetManager INSTANCE = new RenderTargetManager();

    public static void initialize() {
        if (!initialized) {
            downShader = new TintShader(RockstarClient.resourceId("kawase_down/data"));
            upShader = new TintShader(RockstarClient.resourceId("kawase_up/data"));
            initialized = true;
        }
    }

    public static void beginMainPass() {
        Framebuffer framebuffer = minecraftClient.getFramebuffer();
        if (framebuffer != null) {
            framebuffer.beginWrite(false);
        }
    }

    public static void render() {
        INSTANCE.renderBlur(moscow.rockstar.modules.visuals.hud.Interface.isLiquidGlassEnabled() ? 0.1f : 4.0f);
    }

    public static void render(float blurRadius) {
        INSTANCE.renderBlur(blurRadius);
    }

    /**
     * Sets the shared kawase blur radius. The original client writes this
     * immediately before every backdrop/blur/projected-squircle draw.
     */
    public static void setBlurRadius(float radius) {
        INSTANCE.blurRadius = radius;
    }

    private void renderBlur(float radius) {
        if (!this.renderTimer.hasElapsed(MIN_RENDER_INTERVAL_MILLIS)
            || RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }

        sourceFramebuffer = minecraftClient.getFramebuffer();
        this.blurRadius = radius;

        RenderTarget downTarget = DOWN_TARGET.get();
        RenderTarget upTarget = UP_TARGET.get();
        downTarget.setResolutionScale(this.resolutionScale).enableLinearFiltering();
        upTarget.setResolutionScale(this.resolutionScale).enableLinearFiltering();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        downShader.bindShaderProgram();
        downShader.setTintParameters(
            this.blurRadius,
            sourceFramebuffer.textureWidth,
            sourceFramebuffer.textureHeight
        );
        downTarget.beginPass();
        sourceFramebuffer.beginRead();
        RenderSystem.setShaderTexture(0, sourceFramebuffer.getColorAttachment());
        ShaderRenderer.drawFullscreenQuad(
            0.0f,
            0.0f,
            WINDOW.getScaledWidth(),
            WINDOW.getScaledHeight(),
            true
        );
        downTarget.endPass();

        RenderTarget[] targets = {downTarget, upTarget};
        int passCount = this.blurRadius > 5.0f ? 7 : this.blurRadius > 3.0f ? 5 : 3;

        for (int pass = 1; pass < passCount; ++pass) {
            int targetIndex = pass % 2;
            RenderTarget destination = targets[targetIndex];
            RenderTarget source = targets[(targetIndex + 1) % 2];
            destination.beginPass();
            source.beginRead();
            RenderSystem.setShaderTexture(0, source.getColorAttachment());
            downShader.setTintParameters(
                this.blurRadius,
                source.textureWidth,
                source.textureHeight
            );
            ShaderRenderer.drawFullscreenQuad(
                0.0f,
                0.0f,
                WINDOW.getScaledWidth(),
                WINDOW.getScaledHeight(),
                true
            );
            source.endRead();
            destination.endPass();
        }

        upShader.bindShaderProgram();
        for (int pass = 0; pass < passCount; ++pass) {
            int targetIndex = pass % 2;
            RenderTarget destination = targets[(targetIndex + 1) % 2];
            RenderTarget source = targets[targetIndex];
            destination.beginPass();
            source.beginRead();
            RenderSystem.setShaderTexture(0, source.getColorAttachment());
            upShader.setTintParameters(
                this.blurRadius,
                source.textureWidth,
                source.textureHeight
            );
            ShaderRenderer.drawFullscreenQuad(
                0.0f,
                0.0f,
                WINDOW.getScaledWidth(),
                WINDOW.getScaledHeight(),
                true
            );
            source.endRead();
            destination.endPass();
        }

        sourceFramebuffer.endRead();
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.disableBlend();
        this.renderTimer.reset();
    }

    public static int textureId() {
        return UP_TARGET.get().getColorAttachment();
    }

    public static boolean isAlternatePassActive() {
        return alternatePassActive;
    }

    public static void setAlternatePassActive(boolean active) {
        alternatePassActive = active;
    }

    /**
     * Shared pass helper used by the embedded panel registry. Its state order
     * is the same as the original blur passes, while the registry supplies
     * its own destination target.
     */
    static void renderPass(Framebuffer source, RenderTarget destination,
                           TintShader shader, float radius) {
        shader.bindShaderProgram();
        shader.setTintParameters(
            radius,
            Math.max(1, source.textureWidth),
            Math.max(1, source.textureHeight)
        );
        destination.beginPass(true);
        source.beginRead();
        RenderSystem.setShaderTexture(0, source.getColorAttachment());
        ShaderRenderer.drawFullscreenQuad(
            0.0f,
            0.0f,
            WINDOW.getScaledWidth(),
            WINDOW.getScaledHeight(),
            true
        );
        source.endRead();
        destination.endPass();
    }

    private RenderTargetManager() {
    }
}
