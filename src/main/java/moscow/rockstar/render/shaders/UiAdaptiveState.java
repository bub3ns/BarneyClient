package moscow.rockstar.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.target.RenderTarget;
import moscow.rockstar.render.target.RenderTargetManager;
import org.lwjgl.opengl.GL11;

/** Maintains the quarter-resolution adaptive state texture used by UI geometry. */
public final class UiAdaptiveState implements ClientAccess {
    private static final float TARGET_SCALE = 0.25f;
    private static final float TRANSITION_TIME_MILLIS = 100.0f;
    private static final float MAX_FRAME_TIME_MILLIS = 64.0f;

    private float strength;
    private float softness;
    private float radius;
    private RenderTarget currentTarget;
    private RenderTarget previousTarget;
    private AdaptiveStateShader stateShader;
    private long lastFrameTimeMillis;
    private boolean initialized;

    public void initialize() {
        if (this.stateShader == null) {
            this.stateShader = new AdaptiveStateShader(RockstarClient.resourceId("adaptive_state/data"));
        }
    }

    public void update(float strength, float softness, float radius) {
        this.strength = strength;
        this.softness = softness;
        this.radius = radius;
        if (this.stateShader == null || RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }
        this.ensureTargets();

        if (this.currentTarget.getColorAttachment() <= 0) {
            this.currentTarget.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            this.currentTarget.beginPass(true);
            this.currentTarget.endPass();
        }

        long now = System.currentTimeMillis();
        float elapsed = this.lastFrameTimeMillis == 0L
            ? MAX_FRAME_TIME_MILLIS
            : Math.min(MAX_FRAME_TIME_MILLIS, Math.max(0L, now - this.lastFrameTimeMillis));
        if (this.initialized && elapsed <= 0.0f) {
            return;
        }
        this.lastFrameTimeMillis = now;

        boolean sizeChanged = !this.initialized
            || this.currentTarget.textureWidth != this.previousTarget.textureWidth
            || this.currentTarget.textureHeight != this.previousTarget.textureHeight;
        float rate = sizeChanged
            ? 1.0f
            : 1.0f - (float)Math.exp(-elapsed / TRANSITION_TIME_MILLIS);
        int framebufferWidth = minecraftClient.getWindow().getFramebufferWidth();
        int framebufferHeight = minecraftClient.getWindow().getFramebufferHeight();
        boolean scissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        if (scissorEnabled) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
        boolean rendered = false;
        try {
            RenderSystem.disableBlend();
            this.stateShader.bindShaderProgram();
            this.stateShader.setStateParameters(
                strength,
                softness,
                framebufferWidth > 0 ? radius / framebufferWidth : 0.0f,
                framebufferHeight > 0 ? radius / framebufferHeight : 0.0f,
                rate
            );

            this.previousTarget.beginPass(true);
            RenderSystem.setShaderTexture(0, RenderTargetManager.textureId());
            RenderSystem.setShaderTexture(1, this.currentTarget.getColorAttachment());
            ShaderRenderer.drawFullscreenQuad(
                0.0f, 0.0f,
                minecraftClient.getWindow().getScaledWidth(),
                minecraftClient.getWindow().getScaledHeight(),
                true
            );
            this.previousTarget.endPass();
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.setShaderTexture(1, 0);
            rendered = true;
        } finally {
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.setShaderTexture(1, 0);
            if (scissorEnabled) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
        }
        if (rendered) {
            RenderTarget oldCurrent = this.currentTarget;
            this.currentTarget = this.previousTarget;
            this.previousTarget = oldCurrent;
            this.initialized = true;
        }
    }

    public int textureId() {
        return this.currentTarget == null ? 0 : this.currentTarget.getColorAttachment();
    }

    public float strength() {
        return this.strength;
    }

    public float softness() {
        return this.softness;
    }

    public float radius() {
        return this.radius;
    }

    private void ensureTargets() {
        if (this.currentTarget == null) {
            this.currentTarget = new RenderTarget(false).setResolutionScale(TARGET_SCALE);
            this.previousTarget = new RenderTarget(false).setResolutionScale(TARGET_SCALE);
        } else {
            this.currentTarget.setResolutionScale(TARGET_SCALE);
            this.previousTarget.setResolutionScale(TARGET_SCALE);
        }
    }
}
