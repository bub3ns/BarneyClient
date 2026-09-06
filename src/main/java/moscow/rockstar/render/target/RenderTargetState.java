package moscow.rockstar.render.target;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.shaders.GlowCompositeShader;
import moscow.rockstar.render.shaders.ShaderRenderer;

/** Performs the original separable glow blur and composite passes on a capture. */
public final class RenderTargetState implements ClientAccess, WindowHandle {
    private static final float TARGET_SCALE = 0.5f;
    private static final int DEFAULT_SAMPLES_PER_PASS = 3;

    /* The original shared state uses these suppliers; individual effects get a
       fresh memoized set through create(). */
    private static final Supplier<RenderTarget> SHARED_HORIZONTAL_TARGET = Suppliers.memoize(
        () -> new RenderTarget(false).enableLinearFiltering().setResolutionScale(TARGET_SCALE)
    );
    private static final Supplier<RenderTarget> SHARED_VERTICAL_TARGET = Suppliers.memoize(
        () -> new RenderTarget(false).enableLinearFiltering().setResolutionScale(TARGET_SCALE)
    );
    private static final Supplier<RenderTarget> SHARED_COMPOSITE_TARGET = Suppliers.memoize(
        () -> new RenderTarget(false).enableLinearFiltering()
    );

    private static GlowCompositeShader horizontalBlurShader;
    private static GlowCompositeShader verticalBlurShader;
    private static GlowCompositeShader compositeShader;

    private final Supplier<RenderTarget> horizontalTarget;
    private final Supplier<RenderTarget> verticalTarget;
    private final Supplier<RenderTarget> compositeTarget;

    private int blurPasses = 8;
    private int samplesPerPass = DEFAULT_SAMPLES_PER_PASS;
    // Matches the original state constructor's default radius.
    private float blurRadius = 2.5f;
    private float compositeOffset = 7.0f;
    private float outlineStrength = 1.0f;
    private float outlineRadius = 1.0f;
    private float glowOffsetX;
    private float glowOffsetY;

    /** Creates the state backed by the original shared render targets. */
    public RenderTargetState() {
        this(SHARED_HORIZONTAL_TARGET, SHARED_VERTICAL_TARGET, SHARED_COMPOSITE_TARGET);
    }

    private RenderTargetState(Supplier<RenderTarget> horizontalTarget,
                               Supplier<RenderTarget> verticalTarget,
                               Supplier<RenderTarget> compositeTarget) {
        this.horizontalTarget = horizontalTarget;
        this.verticalTarget = verticalTarget;
        this.compositeTarget = compositeTarget;
    }

    /** Creates an isolated state with its own lazily allocated targets. */
    public static RenderTargetState create() {
        Supplier<RenderTarget> horizontalTarget = Suppliers.memoize(
            () -> new RenderTarget(false).enableLinearFiltering().setResolutionScale(TARGET_SCALE)
        );
        Supplier<RenderTarget> verticalTarget = Suppliers.memoize(
            () -> new RenderTarget(false).enableLinearFiltering().setResolutionScale(TARGET_SCALE)
        );
        Supplier<RenderTarget> compositeTarget = Suppliers.memoize(
            () -> new RenderTarget(false).enableLinearFiltering()
        );
        return new RenderTargetState(horizontalTarget, verticalTarget, compositeTarget);
    }

    /** Initializes the shared blur shaders once during the renderer bootstrap. */
    public static void initialize() {
        if (horizontalBlurShader == null) {
            horizontalBlurShader = new GlowCompositeShader(
                RockstarClient.resourceId("glow/blur_h/data")
            );
            verticalBlurShader = new GlowCompositeShader(
                RockstarClient.resourceId("glow/blur_v/data")
            );
            compositeShader = new GlowCompositeShader(
                RockstarClient.resourceId("glow/composite/data")
            );
        }
    }

    public void setBlurPasses(int passes) {
        this.blurPasses = passes;
    }

    public void setSamplesPerPass(int samples) {
        this.samplesPerPass = samples;
    }

    public void setBlurRadius(float radius) {
        this.blurRadius = radius;
    }

    public void setCompositeOffset(float offset) {
        this.compositeOffset = offset;
    }

    public void setOutlineStrength(float strength) {
        this.outlineStrength = strength;
    }

    public void setOutlineRadius(float radius) {
        this.outlineRadius = radius;
    }

    public void setGlowOffsetX(float offset) {
        this.glowOffsetX = offset;
    }

    public void setGlowOffsetY(float offset) {
        this.glowOffsetY = offset;
    }

    public void apply(RenderTarget sourceTarget) {
        this.apply(sourceTarget, -1, -1, -1, -1);
    }

    /** Applies blur and composite, optionally clipped to a framebuffer crop. */
    public void apply(RenderTarget sourceTarget, int cropX, int cropY,
                      int cropWidth, int cropHeight) {
        RenderTarget horizontal = this.horizontalTarget.get();
        RenderTarget vertical = this.verticalTarget.get();
        RenderTarget composite = this.compositeTarget.get();
        int scaledWidth = WINDOW.getScaledWidth();
        int scaledHeight = WINDOW.getScaledHeight();
        boolean clipped = cropX >= 0;
        int framebufferWidth = WINDOW.getFramebufferWidth();
        int framebufferHeight = WINDOW.getFramebufferHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        horizontal.beginPass();
        if (clipped) {
            configureScissor(horizontal, cropX, cropY, cropWidth, cropHeight,
                framebufferWidth, framebufferHeight);
        }
        horizontalBlurShader.bindShaderProgram();
        horizontalBlurShader.setBlurParameters(
            this.blurRadius, horizontal.textureWidth, horizontal.textureHeight
        );
        sourceTarget.beginRead();
        RenderSystem.setShaderTexture(0, sourceTarget.getColorAttachment());
        for (int sample = 0; sample < this.samplesPerPass; ++sample) {
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, scaledWidth, scaledHeight);
        }
        sourceTarget.endRead();
        if (clipped) {
            RenderSystem.disableScissor();
        }
        horizontal.endPass();

        vertical.beginPass();
        if (clipped) {
            configureScissor(vertical, cropX, cropY, cropWidth, cropHeight,
                framebufferWidth, framebufferHeight);
        }
        verticalBlurShader.bindShaderProgram();
        verticalBlurShader.setBlurParameters(
            this.blurRadius, horizontal.textureWidth, horizontal.textureHeight
        );
        horizontal.beginRead();
        RenderSystem.setShaderTexture(0, horizontal.getColorAttachment());
        for (int sample = 0; sample < this.samplesPerPass; ++sample) {
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, scaledWidth, scaledHeight);
        }
        horizontal.endRead();
        if (clipped) {
            RenderSystem.disableScissor();
        }
        vertical.endPass();

        for (int pass = 1; pass < this.blurPasses; ++pass) {
            horizontal.beginPass();
            if (clipped) {
                configureScissor(horizontal, cropX, cropY, cropWidth, cropHeight,
                    framebufferWidth, framebufferHeight);
            }
            horizontalBlurShader.bindShaderProgram();
            horizontalBlurShader.setBlurParameters(
                this.blurRadius, horizontal.textureWidth, horizontal.textureHeight
            );
            vertical.beginRead();
            RenderSystem.setShaderTexture(0, vertical.getColorAttachment());
            for (int sample = 0; sample < this.samplesPerPass; ++sample) {
                ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, scaledWidth, scaledHeight);
            }
            vertical.endRead();
            if (clipped) {
                RenderSystem.disableScissor();
            }
            horizontal.endPass();

            vertical.beginPass();
            if (clipped) {
                configureScissor(vertical, cropX, cropY, cropWidth, cropHeight,
                    framebufferWidth, framebufferHeight);
            }
            verticalBlurShader.bindShaderProgram();
            verticalBlurShader.setBlurParameters(
                this.blurRadius, horizontal.textureWidth, horizontal.textureHeight
            );
            horizontal.beginRead();
            RenderSystem.setShaderTexture(0, horizontal.getColorAttachment());
            for (int sample = 0; sample < this.samplesPerPass; ++sample) {
                ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, scaledWidth, scaledHeight);
            }
            horizontal.endRead();
            if (clipped) {
                RenderSystem.disableScissor();
            }
            vertical.endPass();
        }

        composite.beginPass();
        if (clipped) {
            configureScissor(composite, cropX, cropY, cropWidth, cropHeight,
                framebufferWidth, framebufferHeight);
        }
        compositeShader.bindShaderProgram();
        compositeShader.setCompositeParameters(
            this.compositeOffset,
            sourceTarget.textureWidth,
            sourceTarget.textureHeight,
            this.outlineStrength,
            this.outlineRadius,
            this.glowOffsetX,
            this.glowOffsetY
        );
        sourceTarget.beginRead();
        RenderSystem.setShaderTexture(0, sourceTarget.getColorAttachment());
        RenderSystem.setShaderTexture(1, vertical.getColorAttachment());
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, scaledWidth, scaledHeight);
        sourceTarget.endRead();
        if (clipped) {
            RenderSystem.disableScissor();
        }
        composite.endPass();

        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.setShaderTexture(1, 0);
        RenderSystem.disableBlend();
    }

    /** Applies only the separable blur passes, matching the original helper. */
    public void applyBlurOnly(RenderTarget sourceTarget) {
        this.applyBlurOnly(sourceTarget, -1, -1, -1, -1);
    }

    public void applyBlurOnly(RenderTarget sourceTarget, int cropX, int cropY,
                              int cropWidth, int cropHeight) {
        RenderTarget horizontal = this.horizontalTarget.get();
        RenderTarget vertical = this.verticalTarget.get();
        int scaledWidth = WINDOW.getScaledWidth();
        int scaledHeight = WINDOW.getScaledHeight();
        boolean clipped = cropX >= 0;
        int framebufferWidth = WINDOW.getFramebufferWidth();
        int framebufferHeight = WINDOW.getFramebufferHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        horizontal.beginPass();
        if (clipped) {
            configureScissor(horizontal, cropX, cropY, cropWidth, cropHeight,
                framebufferWidth, framebufferHeight);
        }
        horizontalBlurShader.bindShaderProgram();
        horizontalBlurShader.setBlurParameters(
            this.blurRadius, horizontal.textureWidth, horizontal.textureHeight
        );
        sourceTarget.beginRead();
        RenderSystem.setShaderTexture(0, sourceTarget.getColorAttachment());
        for (int sample = 0; sample < this.samplesPerPass; ++sample) {
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, scaledWidth, scaledHeight);
        }
        sourceTarget.endRead();
        if (clipped) {
            RenderSystem.disableScissor();
        }
        horizontal.endPass();

        vertical.beginPass();
        if (clipped) {
            configureScissor(vertical, cropX, cropY, cropWidth, cropHeight,
                framebufferWidth, framebufferHeight);
        }
        verticalBlurShader.bindShaderProgram();
        verticalBlurShader.setBlurParameters(
            this.blurRadius, horizontal.textureWidth, horizontal.textureHeight
        );
        horizontal.beginRead();
        RenderSystem.setShaderTexture(0, horizontal.getColorAttachment());
        for (int sample = 0; sample < this.samplesPerPass; ++sample) {
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, scaledWidth, scaledHeight);
        }
        horizontal.endRead();
        if (clipped) {
            RenderSystem.disableScissor();
        }
        vertical.endPass();

        for (int pass = 1; pass < this.blurPasses; ++pass) {
            horizontal.beginPass();
            if (clipped) {
                configureScissor(horizontal, cropX, cropY, cropWidth, cropHeight,
                    framebufferWidth, framebufferHeight);
            }
            horizontalBlurShader.bindShaderProgram();
            horizontalBlurShader.setBlurParameters(
                this.blurRadius, horizontal.textureWidth, horizontal.textureHeight
            );
            vertical.beginRead();
            RenderSystem.setShaderTexture(0, vertical.getColorAttachment());
            for (int sample = 0; sample < this.samplesPerPass; ++sample) {
                ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, scaledWidth, scaledHeight);
            }
            vertical.endRead();
            if (clipped) {
                RenderSystem.disableScissor();
            }
            horizontal.endPass();

            vertical.beginPass();
            if (clipped) {
                configureScissor(vertical, cropX, cropY, cropWidth, cropHeight,
                    framebufferWidth, framebufferHeight);
            }
            verticalBlurShader.bindShaderProgram();
            verticalBlurShader.setBlurParameters(
                this.blurRadius, horizontal.textureWidth, horizontal.textureHeight
            );
            horizontal.beginRead();
            RenderSystem.setShaderTexture(0, horizontal.getColorAttachment());
            for (int sample = 0; sample < this.samplesPerPass; ++sample) {
                ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, scaledWidth, scaledHeight);
            }
            horizontal.endRead();
            if (clipped) {
                RenderSystem.disableScissor();
            }
            vertical.endPass();
        }

        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.disableBlend();
    }

    private static void configureScissor(RenderTarget target, int cropX, int cropY,
                                         int cropWidth, int cropHeight,
                                         int framebufferWidth, int framebufferHeight) {
        if (framebufferWidth <= 0 || framebufferHeight <= 0) {
            return;
        }
        float scaleX = (float) target.textureWidth / (float) framebufferWidth;
        float scaleY = (float) target.textureHeight / (float) framebufferHeight;
        int x = (int) Math.floor(cropX * scaleX);
        int y = (int) Math.floor(cropY * scaleY);
        int width = (int) Math.ceil(cropWidth * scaleX);
        int height = (int) Math.ceil(cropHeight * scaleY);
        if (x < 0) {
            width += x;
            x = 0;
        }
        if (y < 0) {
            height += y;
            y = 0;
        }
        if (x + width > target.textureWidth) {
            width = target.textureWidth - x;
        }
        if (y + height > target.textureHeight) {
            height = target.textureHeight - y;
        }
        if (width > 0 && height > 0) {
            RenderSystem.enableScissor(x, y, width, height);
        }
    }

    public RenderTarget getOutputTarget() {
        return this.compositeTarget.get();
    }

    /** Returns the vertical blur target used by effects that only need a glow mask. */
    public RenderTarget getBlurOutputTarget() {
        return this.verticalTarget.get();
    }

    public int getTextureId() {
        return this.compositeTarget.get().getColorAttachment();
    }

    public int getHorizontalTextureId() {
        return this.horizontalTarget.get().getColorAttachment();
    }

    public int getVerticalTextureId() {
        return this.verticalTarget.get().getColorAttachment();
    }
}
