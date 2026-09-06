/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ProjectionType
 *  net.minecraft.RenderLayer
 *  net.minecraft.RenderLayer$MultiPhaseParameters
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexBuffer
 *  net.minecraft.VertexFormat
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.VertexConsumer
 *  net.minecraft.RenderPhase
 *  net.minecraft.RenderPhase$Texture
 *  net.minecraft.RenderPhase$TextureBase
 *  net.minecraft.BuiltBuffer
 *  net.minecraft.TriState
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 */
package moscow.rockstar.render.world;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.render.shaders.TimedAccentShader;
import moscow.rockstar.render.target.RenderTarget;
import com.mojang.blaze3d.systems.ProjectionType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.util.TriState;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import pyrock.utility.render.ColorRGBA;

public class SkyboxRenderer {
    private static final float CUBE_HALF_SIZE = 100.0f;
    private static final int ATLAS_COLUMNS = 3;
    private static final int ATLAS_ROWS = 2;
    private static final int RENDER_TARGET_WIDTH = 2048;
    private static final int RENDER_TARGET_HEIGHT = 1024;
    private static final int FRAME_COUNT = 12;
    private static final long CACHE_REFRESH_MILLIS = 1000L;
    private static VertexBuffer vertexBuffer;
    private static RenderTarget renderTarget;
    private static SkyboxShaderPair lastUiRegistry;
    private static int currentFrame;
    private static long lastUiRegistryUpdate;
    private static final Map<Identifier, RenderLayer> textureLayerCache;
    private static final Map<TimedAccentShader, RenderLayer> shaderLayerCache;

    public static void renderTexture(Identifier class_29602, ColorRGBA colorRGBA) {
        if (class_29602 == null || colorRGBA == null) {
            return;
        }
        SkyboxRenderer.ensureVertexBuffer();
        RenderSystem.setShaderColor((float)(colorRGBA.getRed() / 255.0f), (float)(colorRGBA.getGreen() / 255.0f), (float)(colorRGBA.getBlue() / 255.0f), (float)(colorRGBA.getAlpha() / 255.0f));
        vertexBuffer.draw(textureLayerCache.computeIfAbsent(class_29602, SkyboxRenderer::createTextureRenderLayer));
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static void renderShaderEffect(TimedAccentShader timedAccentShader, ColorRGBA colorRGBA, float f, float f2) {
        if (timedAccentShader == null || colorRGBA == null) {
            return;
        }
        SkyboxRenderer.ensureVertexBuffer();
        timedAccentShader.dispatchFromFloatAndColorRGBA(f, colorRGBA);
        float f3 = Math.max(0.0f, Math.min(1.0f, f2));
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f3);
        vertexBuffer.draw(shaderLayerCache.computeIfAbsent(timedAccentShader, SkyboxRenderer::createShaderRenderLayer));
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static void renderUiComponent(SkyboxShaderPair skyboxShaderPair, ColorRGBA colorRGBA, float f, float f2) {
        if (skyboxShaderPair == null || colorRGBA == null) {
            return;
        }
        SkyboxRenderer.ensureVertexBuffer();
        RenderTarget renderTarget = SkyboxRenderer.getRenderTarget();
        long l = System.currentTimeMillis();
        if (lastUiRegistry != skyboxShaderPair || l - lastUiRegistryUpdate > 1000L) {
            SkyboxRenderer.renderUiRegistryStrip(skyboxShaderPair, renderTarget, colorRGBA, f, 0, 12);
            lastUiRegistry = skyboxShaderPair;
            currentFrame = 0;
        } else {
            SkyboxRenderer.renderUiRegistryStrip(skyboxShaderPair, renderTarget, colorRGBA, f, currentFrame, currentFrame + 1);
            currentFrame = (currentFrame + 1) % 12;
        }
        lastUiRegistryUpdate = l;
        float f3 = Math.max(0.0f, Math.min(1.0f, f2));
        skyboxShaderPair.viewShader().dispatchFromFloatAndColorRGBA(f, colorRGBA);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f3);
        RenderSystem.setShaderTexture((int)0, (int)renderTarget.getColorAttachment());
        vertexBuffer.draw(shaderLayerCache.computeIfAbsent(skyboxShaderPair.viewShader(), SkyboxRenderer::createShaderRenderLayer));
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static void cacheShaderEffectLayer(TimedAccentShader timedAccentShader) {
        if (timedAccentShader == null) {
            return;
        }
        shaderLayerCache.put(timedAccentShader, SkyboxRenderer.createShaderRenderLayer(timedAccentShader));
    }

    public static void cacheUiShaderLayer(SkyboxShaderPair skyboxShaderPair) {
        if (skyboxShaderPair == null) {
            return;
        }
        SkyboxRenderer.cacheShaderEffectLayer(skyboxShaderPair.viewShader());
    }

    public static void clearRenderCaches() {
        if (vertexBuffer != null && !vertexBuffer.isClosed()) {
            vertexBuffer.close();
        }
        vertexBuffer = null;
        if (renderTarget != null) {
            renderTarget.delete();
            renderTarget = null;
        }
        lastUiRegistry = null;
        currentFrame = 0;
        lastUiRegistryUpdate = 0L;
        textureLayerCache.clear();
        shaderLayerCache.clear();
    }

    private static RenderTarget getRenderTarget() {
        if (renderTarget == null) {
            RenderTarget target = new RenderTarget(false);
            target.enableLinearFiltering();
            target.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            target.resize(2048, 1024);
            renderTarget = target;
            lastUiRegistry = null;
        }
        return renderTarget;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderUiRegistryStrip(SkyboxShaderPair skyboxShaderPair, RenderTarget renderTarget, ColorRGBA colorRGBA, float f, int n, int n2) {
        renderTarget.beginWrite(true);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix((Matrix4f)new Matrix4f().setOrtho(0.0f, (float)renderTarget.textureWidth, (float)renderTarget.textureHeight, 0.0f, 1000.0f, 21000.0f), (ProjectionType)ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.identity().translate(0.0f, 0.0f, -11000.0f);
        try {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask((boolean)false);
            RenderSystem.disableCull();
            skyboxShaderPair.bakeShader().bindShaderProgram();
            skyboxShaderPair.bakeShader().dispatchFromFloatAndColorRGBA(f, colorRGBA);
            SkyboxRenderer.drawTextureStrip(renderTarget.textureWidth, renderTarget.textureHeight, n, n2);
        }
        finally {
            renderTarget.endWrite();
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            RenderSystem.depthMask((boolean)true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            matrix4fStack.popMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }

    private static void drawTextureStrip(float f, float f2, int n, int n2) {
        float f3 = (float)n / 12.0f;
        float f4 = (float)n2 / 12.0f;
        float f5 = (1.0f - f3) * f2;
        float f6 = (1.0f - f4) * f2;
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        class_2872.vertex(0.0f, f5, 0.0f).texture(0.0f, f3).color(-1);
        class_2872.vertex(0.0f, f6, 0.0f).texture(0.0f, f4).color(-1);
        class_2872.vertex(f, f6, 0.0f).texture(1.0f, f4).color(-1);
        class_2872.vertex(f, f5, 0.0f).texture(1.0f, f3).color(-1);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
    }

    private static void ensureVertexBuffer() {
        if (vertexBuffer == null || vertexBuffer.isClosed()) {
            vertexBuffer = VertexBuffer.createAndUpload((VertexFormat.DrawMode)VertexFormat.DrawMode.QUADS, (VertexFormat)VertexFormats.POSITION_TEXTURE_COLOR, SkyboxRenderer::writeSkyboxCubeVertices);
        }
    }

    private static RenderLayer createTextureRenderLayer(Identifier class_29602) {
        return RenderLayer.of((String)"rockstar_skybox", (VertexFormat)VertexFormats.POSITION_TEXTURE_COLOR, (VertexFormat.DrawMode)VertexFormat.DrawMode.QUADS, (int)1536, (boolean)false, (boolean)false, (RenderLayer.MultiPhaseParameters)RenderLayer.MultiPhaseParameters.builder().program(RenderPhase.POSITION_TEXTURE_COLOR_PROGRAM).texture((RenderPhase.TextureBase)new RenderPhase.Texture(class_29602, TriState.FALSE, false)).transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY).cull(RenderPhase.DISABLE_CULLING).writeMaskState(RenderPhase.COLOR_MASK).build(false));
    }

    private static RenderLayer createShaderRenderLayer(TimedAccentShader timedAccentShader) {
        return RenderLayer.of((String)"rockstar_skybox_shader", (VertexFormat)VertexFormats.POSITION_TEXTURE_COLOR, (VertexFormat.DrawMode)VertexFormat.DrawMode.QUADS, (int)1536, (boolean)false, (boolean)false, (RenderLayer.MultiPhaseParameters)RenderLayer.MultiPhaseParameters.builder().program(timedAccentShader.createShaderLayer()).transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY).cull(RenderPhase.DISABLE_CULLING).writeMaskState(RenderPhase.COLOR_MASK).build(false));
    }

    private static void writeSkyboxCubeVertices(VertexConsumer class_45882) {
        float f = 100.0f;
        SkyboxRenderer.writeCubeFaceVertices(class_45882, 1, -f, f, -f, -f, f, f, f, f, f, f, f, -f);
        SkyboxRenderer.writeCubeFaceVertices(class_45882, 0, -f, -f, f, -f, -f, -f, f, -f, -f, f, -f, f);
        SkyboxRenderer.writeCubeFaceVertices(class_45882, 2, f, f, -f, f, -f, -f, -f, -f, -f, -f, f, -f);
        SkyboxRenderer.writeCubeFaceVertices(class_45882, 4, -f, f, f, -f, -f, f, f, -f, f, f, f, f);
        SkyboxRenderer.writeCubeFaceVertices(class_45882, 3, -f, f, -f, -f, -f, -f, -f, -f, f, -f, f, f);
        SkyboxRenderer.writeCubeFaceVertices(class_45882, 5, f, f, f, f, -f, f, f, -f, -f, f, f, -f);
    }

    private static void writeCubeFaceVertices(VertexConsumer class_45882, int n, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12) {
        int n2 = n % 3;
        int n3 = n / 3;
        float f13 = (float)n2 / 3.0f;
        float f14 = (float)(n2 + 1) / 3.0f;
        float f15 = (float)n3 / 2.0f;
        float f16 = (float)(n3 + 1) / 2.0f;
        Matrix4f matrix4f = new Matrix4f();
        class_45882.vertex(matrix4f, f, f2, f3).texture(f13, f15).color(-1);
        class_45882.vertex(matrix4f, f4, f5, f6).texture(f13, f16).color(-1);
        class_45882.vertex(matrix4f, f7, f8, f9).texture(f14, f16).color(-1);
        class_45882.vertex(matrix4f, f10, f11, f12).texture(f14, f15).color(-1);
    }

    static {
        textureLayerCache = new HashMap<Identifier, RenderLayer>();
        shaderLayerCache = new IdentityHashMap<TimedAccentShader, RenderLayer>();
    }
}
