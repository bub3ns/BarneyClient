/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.ProjectionType
 *  net.minecraft.Framebuffer
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 */
package moscow.rockstar.render.postprocess;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.shaders.TimedEffectShader;
import moscow.rockstar.render.target.RenderTarget;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import com.mojang.blaze3d.systems.ProjectionType;
import net.minecraft.client.gl.Framebuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class HeatHazeRenderer
implements ClientAccess,
WindowHandle {
    private final RenderTarget sceneCaptureTarget = new RenderTarget(false).enableLinearFiltering();
    private final RenderTarget distortionTarget = new RenderTarget(false).enableLinearFiltering();
    private TimedEffectShader heatHazeShader;

    public void initializeHeatHazeShader() {
        if (this.heatHazeShader != null) {
            return;
        }
        this.heatHazeShader = new TimedEffectShader(RockstarClient.resourceId("heat_haze/data"));
    }

    public void renderHeatHaze(int n, float f, float f2, float f3) {
        if (this.heatHazeShader == null || n == 0 || f2 <= 0.001f) {
            return;
        }
        Framebuffer class_2762 = minecraftClient.getFramebuffer();
        if (class_2762 == null) {
            return;
        }
        int n2 = WINDOW.getScaledWidth();
        int n3 = WINDOW.getScaledHeight();
        if (n2 <= 0 || n3 <= 0) {
            return;
        }
        RenderSystem.disableBlend();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.backupProjectionMatrix();
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0f, (float)n2, (float)n3, 0.0f, 1000.0f, 21000.0f);
        RenderSystem.setProjectionMatrix((Matrix4f)matrix4f, (ProjectionType)ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.identity().translate(0.0f, 0.0f, -11000.0f);
        this.sceneCaptureTarget.beginPass(true);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        class_2762.beginRead();
        RenderSystem.setShaderTexture((int)0, (int)class_2762.getColorAttachment());
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n2, n3);
        class_2762.endRead();
        this.sceneCaptureTarget.endPass();
        this.distortionTarget.beginPass(true);
        this.heatHazeShader.bindShaderProgram();
        this.heatHazeShader.setEffectParameters(f3, f2, f);
        this.sceneCaptureTarget.beginRead();
        RenderSystem.setShaderTexture((int)0, (int)this.sceneCaptureTarget.getColorAttachment());
        RenderSystem.setShaderTexture((int)1, (int)n);
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n2, n3);
        this.sceneCaptureTarget.endRead();
        this.distortionTarget.endPass();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.distortionTarget.beginRead();
        RenderSystem.setShaderTexture((int)0, (int)this.distortionTarget.getColorAttachment());
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n2, n3);
        this.distortionTarget.endRead();
        RenderSystem.setShaderTexture((int)1, (int)0);
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrix4fStack.popMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.depthMask((boolean)true);
    }
}
