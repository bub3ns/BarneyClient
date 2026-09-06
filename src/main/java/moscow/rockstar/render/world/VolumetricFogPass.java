package moscow.rockstar.render.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.shaders.ShaderProgramBase;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.shaders.VolumetricFogShader;
import moscow.rockstar.render.target.RenderTarget;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

/**
 * The volumetric fog compositor: ray-marches the fog into an off-screen target, optionally runs the
 * low-quality tent upsample, and blends the result over the frame with premultiplied alpha.
 */
public final class VolumetricFogPass implements ClientAccess, WindowHandle {
    private static final float LOW_QUALITY_SCALE = 0.5f;
    private final RenderTarget fogRenderTarget = new RenderTarget(false).enableLinearFiltering();
    private final RenderTarget tentRenderTarget = new RenderTarget(false).enableLinearFiltering().setResolutionScale(LOW_QUALITY_SCALE);
    private VolumetricFogShader fogShader;
    private ShaderProgramBase tentShader;

    /** Lazily creates both shader programs; idempotent. */
    public void load() {
        if (this.fogShader != null) {
            return;
        }
        this.fogShader = new VolumetricFogShader(RockstarClient.resourceId("volumetric_fog/data"));
        this.tentShader = new ShaderProgramBase(RockstarClient.resourceId("volumetric_fog/tent/data"), VertexFormats.POSITION_TEXTURE_COLOR);
    }

    public void render(Matrix4f matrix4f, VolumetricFogShader.FogUniforms fogUniforms) {
        if (this.fogShader == null || this.tentShader == null || fogUniforms.noiseTexture == 0) {
            return;
        }
        Framebuffer class_2762 = VolumetricFogPass.minecraftClient.getFramebuffer();
        if (class_2762 == null || class_2762.getDepthAttachment() == 0) {
            return;
        }
        int n = WINDOW.getScaledWidth();
        int n2 = WINDOW.getScaledHeight();
        if (n <= 0 || n2 <= 0) {
            return;
        }
        int n3 = class_2762.getDepthAttachment();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.backupProjectionMatrix();
        Matrix4f matrix4f2 = new Matrix4f().setOrtho(0.0f, n, n2, 0.0f, 1000.0f, 21000.0f);
        RenderSystem.setProjectionMatrix(matrix4f2, ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.identity().translate(0.0f, 0.0f, -11000.0f);
        try {
            this.fogRenderTarget.setResolutionScale(fogUniforms.highQuality ? 1.0f : LOW_QUALITY_SCALE);
            this.fogRenderTarget.beginPass(true);
            this.fogShader.bindShaderProgram();
            this.fogShader.uploadUniforms(matrix4f, fogUniforms);
            RenderSystem.setShaderTexture(0, n3);
            RenderSystem.setShaderTexture(1, fogUniforms.noiseTexture);
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
            this.fogRenderTarget.endPass();
            RenderTarget renderTarget = this.fogRenderTarget;
            if (!fogUniforms.highQuality) {
                this.tentRenderTarget.beginPass(true);
                this.tentShader.bindShaderProgram();
                GlUniform class_2842 = this.tentShader.getUniform("TexelSize");
                if (class_2842 != null) {
                    class_2842.set(1.0f / (float)Math.max(this.fogRenderTarget.textureWidth, 1), 1.0f / (float)Math.max(this.fogRenderTarget.textureHeight, 1));
                }
                this.fogRenderTarget.beginRead();
                RenderSystem.setShaderTexture(0, this.fogRenderTarget.getColorAttachment());
                ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
                this.fogRenderTarget.endRead();
                this.tentRenderTarget.endPass();
                renderTarget = this.tentRenderTarget;
            }
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            renderTarget.beginRead();
            RenderSystem.setShaderTexture(0, renderTarget.getColorAttachment());
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
            renderTarget.endRead();
            RenderSystem.setShaderTexture(1, 0);
            RenderSystem.setShaderTexture(0, 0);
        }
        finally {
            matrix4fStack.popMatrix();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }
}
