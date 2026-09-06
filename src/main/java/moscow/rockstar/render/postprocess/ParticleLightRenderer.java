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
import java.util.List;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.shaders.DynamicLightShader;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.target.RenderTarget;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import com.mojang.blaze3d.systems.ProjectionType;
import net.minecraft.client.gl.Framebuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class ParticleLightRenderer
implements ClientAccess,
WindowHandle {
    private final RenderTarget renderTarget = new RenderTarget(false);
    private DynamicLightShader shaderProgram;

    public void initializeParticleLightShader() {
        if (this.shaderProgram != null) {
            return;
        }
        this.shaderProgram = new DynamicLightShader(RockstarClient.resourceId("particle_light/data"));
    }

    public void renderParticleLights(Matrix4f matrix4f, float f, List<DynamicLightShader.LightSource> list) {
        if (this.shaderProgram == null || list == null || list.isEmpty()) {
            return;
        }
        Framebuffer class_2762 = minecraftClient.getFramebuffer();
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
        RenderSystem.depthMask((boolean)false);
        RenderSystem.backupProjectionMatrix();
        Matrix4f matrix4f2 = new Matrix4f().setOrtho(0.0f, (float)n, (float)n2, 0.0f, 1000.0f, 21000.0f);
        RenderSystem.setProjectionMatrix((Matrix4f)matrix4f2, (ProjectionType)ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.identity().translate(0.0f, 0.0f, -11000.0f);
        this.renderTarget.beginPass(false);
        this.shaderProgram.bindShaderProgram();
        this.shaderProgram.setDynamicLightUniforms(matrix4f, f, list);
        class_2762.beginRead();
        RenderSystem.setShaderTexture((int)0, (int)class_2762.getColorAttachment());
        RenderSystem.setShaderTexture((int)1, (int)n3);
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
        class_2762.endRead();
        this.renderTarget.endPass();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.renderTarget.beginRead();
        RenderSystem.setShaderTexture((int)0, (int)this.renderTarget.getColorAttachment());
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
        this.renderTarget.endRead();
        RenderSystem.setShaderTexture((int)1, (int)0);
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrix4fStack.popMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableBlend();
    }
}
