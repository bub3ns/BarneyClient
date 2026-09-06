package moscow.rockstar.render.target;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientAccess;
import net.minecraft.client.gl.Framebuffer;

/** Custom framebuffer used by the original post-processing and glow passes. */
public class RenderTarget extends Framebuffer implements ClientAccess {
    private boolean linearFiltering;
    private float resolutionScale = 1.0f;

    public RenderTarget(boolean useDepth) {
        super(useDepth);
    }

    public RenderTarget(int width, int height, boolean useDepth) {
        super(useDepth);
        resize(width, height);
    }

    public RenderTarget enableLinearFiltering() {
        linearFiltering = true;
        if (colorAttachment > 0) {
            RenderSystem.recordRenderCall(() -> {
                if (colorAttachment > 0) {
                    super.setTexFilter(9729);
                }
            });
        }
        return this;
    }

    public RenderTarget setResolutionScale(float scale) {
        resolutionScale = Math.max(0.1f, Math.min(1.0f, scale));
        return this;
    }

    /** ORIGINAL: {@code rockstar/ilIlil/iIiiIIiII#method_1231(II)V} - the override is on initFbo,
     *  NOT on resize. Vanilla {@code resize} calls {@code initFbo} then {@code endWrite}, so
     *  overriding resize added a stray framebuffer unbind on reallocation frames. */
    @Override
    public void initFbo(int width, int height) {
        super.initFbo(width, height);
        if (linearFiltering) {
            super.setTexFilter(9729);
        }
    }

    @Override
    public void setTexFilter(int filter) {
        super.setTexFilter(linearFiltering ? 9729 : filter);
    }

    public void beginPass(boolean clear) {
        updateSizeFromWindow();
        if (clear) {
            clear();
            setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        }
        beginWrite(false);
    }

    public void beginPass() {
        beginPass(true);
    }

    public void endPass() {
        endWrite();
        minecraftClient.getFramebuffer().beginWrite(true);
    }

    public void bindForRead() {
        beginRead();
    }

    public void unbindForRead() {
        endRead();
    }

    public int textureId() {
        return getColorAttachment();
    }

    private void updateSizeFromWindow() {
        if (hasDifferentWindowSize()) {
            if (fbo >= 0) {
                delete();
            }
            int width = Math.max((int) Math.floor((float) minecraftClient.getWindow().getFramebufferWidth() * resolutionScale), 1);
            int height = Math.max((int) Math.floor((float) minecraftClient.getWindow().getFramebufferHeight() * resolutionScale), 1);
            initFbo(width, height);
        }
    }

    private boolean hasDifferentWindowSize() {
        int width = Math.max((int) Math.floor((float) minecraftClient.getWindow().getFramebufferWidth() * resolutionScale), 1);
        int height = Math.max((int) Math.floor((float) minecraftClient.getWindow().getFramebufferHeight() * resolutionScale), 1);
        return textureWidth != width || textureHeight != height;
    }
}
