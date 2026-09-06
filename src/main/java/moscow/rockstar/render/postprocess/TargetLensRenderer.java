package moscow.rockstar.render.postprocess;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.target.RenderTarget;
import moscow.rockstar.render.shaders.ShaderProgramBase;
import moscow.rockstar.render.shaders.ShaderRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

/** Applies the target-lens distortion to the current scene using the target_lens shader. */
public final class TargetLensRenderer extends ShaderProgramBase implements ClientAccess, WindowHandle {
    private static final int MAX_LENSES = 18;
    private final RenderTarget sceneCapture = new RenderTarget(false).enableLinearFiltering();
    private final RenderTarget distortedScene = new RenderTarget(false).enableLinearFiltering();
    private GlUniform modelViewMatrix;
    private GlUniform projectionMatrix;
    private GlUniform lensCount;
    private GlUniform aspect;
    private GlUniform strength;
    private final GlUniform[] lensUniforms = new GlUniform[MAX_LENSES];

    public TargetLensRenderer() {
        super(RockstarClient.resourceId("target_lens/data"), VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Override
    protected void initializeShaderUniforms() {
        modelViewMatrix = getUniform("ModelViewMat");
        projectionMatrix = getUniform("ProjMat");
        lensCount = getUniform("Count");
        aspect = getUniform("Aspect");
        strength = getUniform("Strength");
        for (int i = 0; i < MAX_LENSES; i++) {
            lensUniforms[i] = getUniform("Lens" + i);
        }
        super.initializeShaderUniforms();
    }

    public void render(float viewportAspect, float distortionStrength, List<ProjectedLensPoint> points) {
        if (points == null || points.isEmpty()) {
            return;
        }
        Framebuffer scene = minecraftClient.getFramebuffer();
        if (scene == null || scene.getColorAttachment() == 0 || scene.getDepthAttachment() == 0) {
            return;
        }
        int width = WINDOW.getScaledWidth();
        int height = WINDOW.getScaledHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        if (lensCount == null) {
            initializeShaderUniforms();
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.backupProjectionMatrix();
        Matrix4f orthographic = new Matrix4f().setOrtho(0.0f, width, height, 0.0f, 1000.0f, 21000.0f);
        RenderSystem.setProjectionMatrix(orthographic, ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity().translate(0.0f, 0.0f, -11000.0f);
        try {
            sceneCapture.beginPass(true);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            scene.beginRead();
            RenderSystem.setShaderTexture(0, scene.getColorAttachment());
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, width, height);
            scene.endRead();
            sceneCapture.endPass();

            distortedScene.beginPass(true);
            bindShaderProgram();
            uploadUniforms(viewportAspect, distortionStrength, points);
            sceneCapture.beginRead();
            RenderSystem.setShaderTexture(0, sceneCapture.getColorAttachment());
            RenderSystem.setShaderTexture(1, scene.getDepthAttachment());
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, width, height);
            sceneCapture.endRead();
            distortedScene.endPass();

            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            distortedScene.beginRead();
            RenderSystem.setShaderTexture(0, distortedScene.getColorAttachment());
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, width, height);
            distortedScene.endRead();
        } finally {
            RenderSystem.setShaderTexture(1, 0);
            RenderSystem.setShaderTexture(0, 0);
            modelView.popMatrix();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.enableBlend();
        }
    }

    private void uploadUniforms(float viewportAspect, float distortionStrength, List<ProjectedLensPoint> points) {
        if (modelViewMatrix != null) {
            modelViewMatrix.set(RenderSystem.getModelViewMatrix());
        }
        if (projectionMatrix != null) {
            projectionMatrix.set(RenderSystem.getProjectionMatrix());
        }
        int count = Math.min(MAX_LENSES, points.size());
        if (lensCount != null) {
            lensCount.set((float) count);
        }
        if (aspect != null) {
            aspect.set(viewportAspect);
        }
        if (strength != null) {
            strength.set(Math.max(0.0f, distortionStrength));
        }
        for (int i = 0; i < MAX_LENSES; i++) {
            ProjectedLensPoint point = i < count ? points.get(i) : null;
            if (lensUniforms[i] == null) {
                continue;
            }
            if (point == null) {
                lensUniforms[i].set(0.0f, 0.0f, 0.0f, 0.0f);
            } else {
                lensUniforms[i].set(point.getCenterX(), point.getCenterY(), point.getDepth(), point.getRadius());
            }
        }
    }
}
