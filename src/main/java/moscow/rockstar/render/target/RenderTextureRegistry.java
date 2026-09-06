package moscow.rockstar.render.target;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.shaders.TintShader;
import moscow.rockstar.util.Timer;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL11;

/**
 * Owns the lazily allocated, independently refreshed blur textures used by
 * UI panels. The pass order mirrors the original client implementation.
 */
public final class RenderTextureRegistry implements ClientAccess, WindowHandle {
    private static final long MIN_RENDER_INTERVAL_MILLIS = 25L;
    private static final float TARGET_SCALE = 0.5f;

    public static final int MAIN_SLOT = 0;
    public static final int EMBEDDED_SLOT = 1;

    private final List<RenderSlot> slots = new ArrayList<>();
    private TintShader downShader;
    private TintShader upShader;
    private float resolutionScale = TARGET_SCALE;

    public void initialize() {
        this.downShader = new TintShader(RockstarClient.resourceId("kawase_down/data"));
        this.upShader = new TintShader(RockstarClient.resourceId("kawase_up/data"));
    }

    private RenderSlot getSlot(int slotIndex) {
        while (this.slots.size() <= slotIndex) {
            this.slots.add(new RenderSlot());
        }
        return this.slots.get(slotIndex);
    }

    public void invalidateAll() {
        for (RenderSlot slot : this.slots) {
            slot.needsRender = true;
        }
    }

    public void invalidate(int slotIndex) {
        if (slotIndex >= 0) {
            this.getSlot(slotIndex).needsRender = true;
        }
    }

    public boolean hasTexture() {
        return this.hasTexture(MAIN_SLOT);
    }

    public boolean hasTexture(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return false;
        }
        RenderSlot slot = this.slots.get(slotIndex);
        RenderTarget output = slot.outputTarget();
        return slot.hasTexture && output != null && output.getColorAttachment() > 0;
    }

    public int textureId() {
        return this.textureId(MAIN_SLOT);
    }

    public int textureId(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return 0;
        }
        RenderTarget output = this.slots.get(slotIndex).outputTarget();
        return output == null ? 0 : output.getColorAttachment();
    }

    public void render(int slotIndex, float blurRadius) {
        if (slotIndex < 0 || this.downShader == null
            || RockstarClient.INSTANCE.isPanicMode()
            || WidgetBatchRenderer.textureRenderingActive) {
            return;
        }

        RenderSlot slot = this.getSlot(slotIndex);
        if (!slot.needsRender && !slot.renderTimer.hasElapsed(MIN_RENDER_INTERVAL_MILLIS)) {
            return;
        }
        slot.needsRender = false;
        slot.renderTimer.reset();

        Framebuffer sourceFramebuffer = minecraftClient.getFramebuffer();
        RenderTarget downTarget = slot.downTarget();
        RenderTarget upTarget = slot.upTarget();
        downTarget.setResolutionScale(this.resolutionScale).enableLinearFiltering();
        upTarget.setResolutionScale(this.resolutionScale).enableLinearFiltering();

        boolean scissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        if (scissorEnabled) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        try {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            this.downShader.bindShaderProgram();
            this.downShader.setTintParameters(
                blurRadius, sourceFramebuffer.textureWidth, sourceFramebuffer.textureHeight
            );
            downTarget.beginPass();
            sourceFramebuffer.beginRead();
            RenderSystem.setShaderTexture(0, sourceFramebuffer.getColorAttachment());
            ShaderRenderer.drawFullscreenQuad(
                0.0f, 0.0f, WINDOW.getScaledWidth(), WINDOW.getScaledHeight(), true
            );
            downTarget.endPass();

            RenderTarget[] targets = {downTarget, upTarget};
            int passCount = blurRadius > 5.0f ? 7 : blurRadius > 3.0f ? 5 : 3;
            for (int pass = 1; pass < passCount; ++pass) {
                int targetIndex = pass % 2;
                RenderTarget destination = targets[targetIndex];
                RenderTarget source = targets[(targetIndex + 1) % 2];
                destination.beginPass();
                source.beginRead();
                RenderSystem.setShaderTexture(0, source.getColorAttachment());
                this.downShader.setTintParameters(
                    blurRadius, source.textureWidth, source.textureHeight
                );
                ShaderRenderer.drawFullscreenQuad(
                    0.0f, 0.0f, WINDOW.getScaledWidth(), WINDOW.getScaledHeight(), true
                );
                source.endRead();
                destination.endPass();
            }

            this.upShader.bindShaderProgram();
            for (int pass = 0; pass < passCount; ++pass) {
                int targetIndex = pass % 2;
                RenderTarget destination = targets[(targetIndex + 1) % 2];
                RenderTarget source = targets[targetIndex];
                destination.beginPass();
                source.beginRead();
                RenderSystem.setShaderTexture(0, source.getColorAttachment());
                this.upShader.setTintParameters(
                    blurRadius, source.textureWidth, source.textureHeight
                );
                ShaderRenderer.drawFullscreenQuad(
                    0.0f, 0.0f, WINDOW.getScaledWidth(), WINDOW.getScaledHeight(), true
                );
                source.endRead();
                destination.endPass();
            }

            sourceFramebuffer.endRead();
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.disableBlend();
            slot.hasTexture = true;
        } finally {
            if (scissorEnabled) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
        }
    }

    private static final class RenderSlot {
        private final Supplier<RenderTarget> down = Suppliers.memoize(
            () -> new RenderTarget(false).enableLinearFiltering()
        );
        private final Supplier<RenderTarget> up = Suppliers.memoize(
            () -> new RenderTarget(false).enableLinearFiltering()
        );
        private final Timer renderTimer = new Timer();
        private boolean needsRender = true;
        private boolean hasTexture;

        private RenderTarget downTarget() {
            return this.down.get();
        }

        private RenderTarget upTarget() {
            return this.up.get();
        }

        private RenderTarget outputTarget() {
            return this.up.get();
        }
    }
}
