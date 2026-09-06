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
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.shaders.ShockwaveShader;
import moscow.rockstar.render.target.RenderTarget;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import com.mojang.blaze3d.systems.ProjectionType;
import net.minecraft.client.gl.Framebuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class JumpCirclePostProcessor
implements ClientAccess,
WindowHandle {
    private final RenderTarget primaryRenderTarget = new RenderTarget(false);
    private final RenderTarget secondaryRenderTarget = new RenderTarget(false);
    private ShockwaveShader shockwaveShader;

    public void refreshRenderOutput() {
        if (this.shockwaveShader != null) {
            return;
        }
        this.shockwaveShader = new ShockwaveShader(RockstarClient.resourceId("jump_shockwave/data"));
    }

    public void renderJumpCircles(Matrix4f matrix4f, List<JumpCircleInstance> list) {
        if (this.shockwaveShader == null || list == null || list.isEmpty()) {
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
        int n4 = Math.min(list.size(), 12);
        RenderSystem.disableBlend();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.backupProjectionMatrix();
        Matrix4f matrix4f2 = new Matrix4f().setOrtho(0.0f, (float)n, (float)n2, 0.0f, 1000.0f, 21000.0f);
        RenderSystem.setProjectionMatrix((Matrix4f)matrix4f2, (ProjectionType)ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.identity().translate(0.0f, 0.0f, -11000.0f);
        RenderTarget[] renderTargets = new RenderTarget[]{this.primaryRenderTarget, this.secondaryRenderTarget};
        Framebuffer source = class_2762;
        int n5 = 0;
        for (int i = 0; i < n4; ++i) {
            RenderTarget target = renderTargets[n5];
            target.beginPass(true);
            this.shockwaveShader.bindShaderProgram();
            this.shockwaveShader.setShockwaveParameters(matrix4f, list.get(i));
            source.beginRead();
            RenderSystem.setShaderTexture((int)0, (int)source.getColorAttachment());
            RenderSystem.setShaderTexture((int)1, (int)n3);
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
            source.endRead();
            target.endPass();
            source = target;
            n5 ^= 1;
        }
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        source.beginRead();
        RenderSystem.setShaderTexture((int)0, (int)source.getColorAttachment());
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
        source.endRead();
        RenderSystem.setShaderTexture((int)1, (int)0);
        RenderSystem.setShaderTexture((int)0, (int)0);
        matrix4fStack.popMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableBlend();
    }

    public static final class JumpCircleInstance {
        private final float centerX;
        private final float centerY;
        private final float centerZ;
        private final float worldRadius;
        private final float thickness;
        private final float red;
        private final float green;
        private final float blue;
        private final float intensity;
        private final float strength;

        public JumpCircleInstance(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10) {
            this.centerX = f;
            this.centerY = f2;
            this.centerZ = f3;
            this.worldRadius = f4;
            this.thickness = f5;
            this.red = f6;
            this.green = f7;
            this.blue = f8;
            this.intensity = f9;
            this.strength = f10;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "centerX", "centerY", "centerZ", "worldRadius", "thickness", "red", "green", "blue", "intensity", "strength");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "centerX", "centerY", "centerZ", "worldRadius", "thickness", "red", "green", "blue", "intensity", "strength");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "centerX", "centerY", "centerZ", "worldRadius", "thickness", "red", "green", "blue", "intensity", "strength");
        }

        public float getCenterX() {
            return this.centerX;
        }

        public float getCenterY() {
            return this.centerY;
        }

        public float getCenterZ() {
            return this.centerZ;
        }

        public float getWorldRadius() {
            return this.worldRadius;
        }

        public float getThickness() {
            return this.thickness;
        }

        public float getRed() {
            return this.red;
        }

        public float getGreen() {
            return this.green;
        }

        public float getBlue() {
            return this.blue;
        }

        public float getIntensity() {
            return this.intensity;
        }

        public float getStrength() {
            return this.strength;
        }
    }
}
