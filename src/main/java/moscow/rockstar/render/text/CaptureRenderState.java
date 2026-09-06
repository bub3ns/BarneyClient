package moscow.rockstar.render.text;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import moscow.rockstar.core.RockstarClient;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;

/** Restores the vanilla matrices, viewport, and scissor state around a patch. */
final class CaptureRenderState {
    private final Matrix4f projection;
    private final ProjectionType projectionType;
    private final Matrix4f modelView;
    private final boolean scissorEnabled;
    private final int[] scissorBox;
    private final int[] viewport;

    private Matrix4f previousProjection;
    private ProjectionType previousProjectionType;
    private final int[] previousViewport = new int[4];

    private CaptureRenderState(Matrix4f projection, ProjectionType projectionType, Matrix4f modelView,
                               boolean scissorEnabled, int[] scissorBox, int[] viewport) {
        this.projection = projection;
        this.projectionType = projectionType;
        this.modelView = modelView;
        this.scissorEnabled = scissorEnabled;
        this.scissorBox = scissorBox;
        this.viewport = viewport;
    }

    static CaptureRenderState capture() {
        boolean scissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        int[] scissorBox = new int[4];
        GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissorBox);
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        return new CaptureRenderState(
            new Matrix4f(RenderSystem.getProjectionMatrix()),
            RenderSystem.getProjectionType(),
            new Matrix4f(RenderSystem.getModelViewMatrix()),
            scissorEnabled,
            scissorBox,
            viewport
        );
    }

    void prepare() {
        previousProjection = RenderSystem.getProjectionMatrix();
        previousProjectionType = RenderSystem.getProjectionType();
        RenderSystem.setProjectionMatrix(projection, projectionType);

        GL11.glGetIntegerv(GL11.GL_VIEWPORT, previousViewport);
        if (!Arrays.equals(previousViewport, viewport)) {
            RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            TextCaptureController.reportViewportMismatch(viewport, previousViewport);
        }

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(modelView);
        if (scissorEnabled) {
            GlStateManager._enableScissorTest();
            GlStateManager._scissorBox(scissorBox[0], scissorBox[1], scissorBox[2], scissorBox[3]);
        } else {
            GlStateManager._disableScissorTest();
        }
    }

    void restore() {
        GlStateManager._disableScissorTest();
        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.setProjectionMatrix(previousProjection, previousProjectionType);
        RenderSystem.viewport(previousViewport[0], previousViewport[1], previousViewport[2], previousViewport[3]);
    }
}
