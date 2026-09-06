/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.LivingEntityRenderState
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.ProjectionType
 *  net.minecraft.LivingEntity
 *  net.minecraft.Vec2f
 *  net.minecraft.Framebuffer
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.MatrixStack
 *  net.minecraft.EntityModel
 *  net.minecraft.AbstractClientPlayerEntity
 *  net.minecraft.MatrixUtil
 *  net.minecraft.EntityRenderer
 *  net.minecraft.LivingEntityRenderer
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Vector3f
 */
package moscow.rockstar.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.visuals.hud.Interface;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.layers.BlockGhostRenderLayer;
import moscow.rockstar.render.layers.EntityGlowRenderLayer;
import moscow.rockstar.render.layers.SmoothItemRenderLayer;
import moscow.rockstar.render.postprocess.ParticleLightRenderer;
import moscow.rockstar.render.postprocess.SaturationShader;
import moscow.rockstar.render.postprocess.WetWorldShader;
import moscow.rockstar.render.shaders.FloorShader;
import moscow.rockstar.render.shaders.ItemEffectShader;
import moscow.rockstar.render.shaders.ShaderProgramBase;
import moscow.rockstar.render.shaders.TimedAccentShader;
import moscow.rockstar.render.texture.TextureRegion;
import moscow.rockstar.render.core.ColorRenderContext;
import moscow.rockstar.render.core.RenderBufferContext;
import moscow.rockstar.render.core.TextureRenderContext;
import moscow.rockstar.render.target.RenderTarget;
import moscow.rockstar.render.target.RenderTargetManager;
import moscow.rockstar.render.target.RenderTargetState;
import moscow.rockstar.render.target.RenderTextureRegistry;
import moscow.rockstar.render.world.SkyboxRenderer;
import moscow.rockstar.render.world.SkyboxShaderPair;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.theme.IconStyle;
import moscow.rockstar.util.timing.FrameScheduler;
import moscow.rockstar.render.colors.GradientColors;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import com.mojang.blaze3d.systems.ProjectionType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.MatrixUtil;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.Rect;
import ua.mintantileak.spk.Compile;

public final class ShaderRenderer
implements ClientAccess,
WindowHandle {
    public static final float DEFAULT_CORNER_SMOOTHNESS = 0.5f;
    public static final FrameScheduler frameScheduler = new FrameScheduler(true);
    public static ShaderProgramBase rectangleShader;
    private static ShaderProgramBase squircleShader;
    private static ShaderProgramBase textureShader;
    private static ShaderProgramBase texturedSquircleShader;
    private static ShaderProgramBase projectedSquircleShader;
    private static ShaderProgramBase borderShader;
    private static ShaderProgramBase dashedBorderShader;
    private static ShaderProgramBase bezierShader;
    private static ShaderProgramBase squircleBorderShader;
    private static ShaderProgramBase loadingShader;
    private static ShaderProgramBase liquidGlassShader;
    private static ShaderProgramBase gradientRectangleShader;
    public static final RenderTextureRegistry textureRegistry = new RenderTextureRegistry();
    public static ShaderProgramBase entityGlowShader;
    /** Shared full-entity blur state used by the Glow overlay. */
    public static RenderTargetState entityGlowState;
    public static ShaderProgramBase slugFontShader;
    public static TimedAccentShader nebulaSkyShader;
    public static TimedAccentShader sunsetSkyShader;
    public static TimedAccentShader radiantSkyShader;
    public static TimedAccentShader skyCausticShader;
    public static SkyboxShaderPair galaxySkyRegistry;
    public static SkyboxShaderPair spaceSkyRegistry;
    public static SaturationShader saturationTextureBatch;
    public static WetWorldShader wetWorldPostProcessor;
    public static ParticleLightRenderer particleLightRenderer;
    public static FloorShader mirrorCompositeShader;
    public static ItemEffectShader itemCausticShader;
    public static ItemEffectShader itemPlasmaShader;
    public static ItemEffectShader itemLavaShader;
    private static ShaderProgramBase metaballShader;
    private static ShaderProgramBase islandBlobShader;
    private static ShaderProgramBase arcShader;
    public static ShaderProgramBase mappedTextureShader;
    public static ShaderProgramBase universalUiShader;
    public static ShaderProgramBase adaptiveUiShader;
    public static UiAdaptiveState uiRootComponent;
    private static ShaderProgramBase backdropBlurShader;
    public static ShaderProgramBase uiStreamShader;
    private static final RenderTarget defaultRenderTarget;
    private static int requestedTextureId;

    public static void setRequestedTextureId(int n) {
        requestedTextureId = n;
    }

    public static void resetRequestedTexture() {
        requestedTextureId = -1;
    }

    private static int getActiveTextureId() {
        return requestedTextureId >= 0 && !WidgetBatchRenderer.textureRenderingActive && textureRegistry.hasTexture(requestedTextureId)
            ? textureRegistry.textureId(requestedTextureId)
            : RenderTargetManager.textureId();
    }

    private static boolean isTooTransparent(ColorRGBA colorRGBA) {
        return colorRGBA == null || colorRGBA.getAlpha() <= 0.5f;
    }

    private static boolean hasInvalidSize(float f, float f2) {
        return f <= 0.0f || f2 <= 0.0f;
    }

    @Compile(obfuscation=4)
    public static void initializeShaders() {
        rectangleShader = new ShaderProgramBase(RockstarClient.resourceId("rectangle/data"), VertexFormats.POSITION_COLOR);
        squircleShader = new ShaderProgramBase(RockstarClient.resourceId("squircle/data"), VertexFormats.POSITION_COLOR);
        texturedSquircleShader = new ShaderProgramBase(RockstarClient.resourceId("squircle_texture/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        projectedSquircleShader = new ShaderProgramBase(RockstarClient.resourceId("proj_squircle/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        textureShader = new ShaderProgramBase(RockstarClient.resourceId("texture/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        borderShader = new ShaderProgramBase(RockstarClient.resourceId("border/data"), VertexFormats.POSITION_COLOR);
        dashedBorderShader = new ShaderProgramBase(RockstarClient.resourceId("dashed_border/data"), VertexFormats.POSITION_COLOR);
        bezierShader = new ShaderProgramBase(RockstarClient.resourceId("bezier/data"), VertexFormats.POSITION_COLOR);
        squircleBorderShader = new ShaderProgramBase(RockstarClient.resourceId("squircle_border/data"), VertexFormats.POSITION_COLOR);
        loadingShader = new ShaderProgramBase(RockstarClient.resourceId("loading/data"), VertexFormats.POSITION_COLOR);
        liquidGlassShader = new ShaderProgramBase(RockstarClient.resourceId("liquidglass/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        gradientRectangleShader = new ShaderProgramBase(RockstarClient.resourceId("gradient_rectangle/data"), VertexFormats.POSITION_COLOR);
        metaballShader = new ShaderProgramBase(RockstarClient.resourceId("metaball/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        islandBlobShader = new ShaderProgramBase(RockstarClient.resourceId("islandblob/data"), VertexFormats.POSITION_COLOR);
        mappedTextureShader = new ShaderProgramBase(RockstarClient.resourceId("mapped_texture/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        universalUiShader = new ShaderProgramBase(RockstarClient.resourceId("ui_universal/data"), VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        adaptiveUiShader = new ShaderProgramBase(RockstarClient.resourceId("adaptive_ui/data"), VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        slugFontShader = new SlugFontShader(RockstarClient.resourceId("slug_font/data"));
        uiRootComponent = new UiAdaptiveState();
        uiRootComponent.initialize();
        backdropBlurShader = new ShaderProgramBase(RockstarClient.resourceId("backdrop_blur/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        uiStreamShader = new ShaderProgramBase(RockstarClient.resourceId("ui_stream/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        arcShader = new ShaderProgramBase(RockstarClient.resourceId("arc/data"), VertexFormats.POSITION_COLOR);
        RenderTargetManager.initialize();
        textureRegistry.initialize();
        particleLightRenderer = new ParticleLightRenderer();
        particleLightRenderer.initializeParticleLightShader();
        entityGlowState = new RenderTargetState();
        RenderTargetState.initialize();
        entityGlowShader = new ShaderProgramBase(RockstarClient.resourceId("glow/entity_solid/data"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
        EntityGlowRenderLayer.initialize(entityGlowShader);
        nebulaSkyShader = new TimedAccentShader(RockstarClient.resourceId("sky_nebula/data"));
        SkyboxRenderer.cacheShaderEffectLayer(nebulaSkyShader);
        sunsetSkyShader = new TimedAccentShader(RockstarClient.resourceId("sky_sunset/data"));
        SkyboxRenderer.cacheShaderEffectLayer(sunsetSkyShader);
        radiantSkyShader = new TimedAccentShader(RockstarClient.resourceId("sky_radiant/data"));
        SkyboxRenderer.cacheShaderEffectLayer(radiantSkyShader);
        skyCausticShader = new TimedAccentShader(RockstarClient.resourceId("sky_caustic/data"));
        SkyboxRenderer.cacheShaderEffectLayer(skyCausticShader);
        galaxySkyRegistry = new SkyboxShaderPair("sky_galaxy/bake/data", "sky_galaxy/view/data");
        SkyboxRenderer.cacheUiShaderLayer(galaxySkyRegistry);
        spaceSkyRegistry = new SkyboxShaderPair("sky_space/bake/data", "sky_space/view/data");
        SkyboxRenderer.cacheUiShaderLayer(spaceSkyRegistry);
        saturationTextureBatch = new SaturationShader(RockstarClient.resourceId("saturation/data"));
        wetWorldPostProcessor = new WetWorldShader(RockstarClient.resourceId("wet_world/data"));
        mirrorCompositeShader = new FloorShader(RockstarClient.resourceId("mirror_composite/data"));
        itemCausticShader = new ItemEffectShader(RockstarClient.resourceId("item_caustic/data"));
        itemPlasmaShader = new ItemEffectShader(RockstarClient.resourceId("item_plasma/data"));
        itemLavaShader = new ItemEffectShader(RockstarClient.resourceId("item_lava/data"));
        SmoothItemRenderLayer.initialize(new ShaderProgramBase(RockstarClient.resourceId("item_smooth/translucent/data"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), new ShaderProgramBase(RockstarClient.resourceId("item_smooth/cutout/data"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL));
        BlockGhostRenderLayer.initialize(new ShaderProgramBase(RockstarClient.resourceId("block_ghost/data"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL));
    }

    public static void renderDefaultFramebuffer() {
        defaultRenderTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        defaultRenderTarget.beginPass();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        minecraftClient.getFramebuffer().beginRead();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (int)minecraftClient.getFramebuffer().getColorAttachment());
        ShaderRenderer.drawFullscreenQuad(0.0f, 0.0f, WINDOW.getScaledWidth(), WINDOW.getScaledHeight(), true);
        minecraftClient.getFramebuffer().endRead();
        RenderSystem.disableBlend();
        minecraftClient.getFramebuffer().beginWrite(true);
        defaultRenderTarget.endPass();
    }

    /**
     * Return rendering to Minecraft's framebuffer after the custom screen
     * framebuffer has been used.  The original client performs this handoff
     * immediately before HUD rendering.
     */
    public static void finishDefaultFramebuffer() {
        defaultRenderTarget.endPass();
    }

    /**
     * Select the resolution used when the default framebuffer is captured for
     * screen effects.  The title screen changes this while it is displayed.
     */
    public static void setDefaultFramebufferResolutionScale(float scale) {
        defaultRenderTarget.setResolutionScale(scale);
    }

    public static void renderScreenTexture() {
        Framebuffer class_2762 = minecraftClient.getFramebuffer();
        if (class_2762 == null) {
            return;
        }
        int n = WINDOW.getScaledWidth();
        int n2 = WINDOW.getScaledHeight();
        RenderSystem.backupProjectionMatrix();
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0f, (float)n, (float)n2, 0.0f, 1000.0f, 21000.0f);
        RenderSystem.setProjectionMatrix((Matrix4f)matrix4f, (ProjectionType)ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.identity().translate(0.0f, 0.0f, -11000.0f);
        defaultRenderTarget.beginPass(true);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShaderTexture((int)0, (int)class_2762.getColorAttachment());
        ShaderRenderer.drawFullscreenQuad(0.0f, 0.0f, n, n2, true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        defaultRenderTarget.endPass();
        matrix4fStack.popMatrix();
        RenderSystem.restoreProjectionMatrix();
    }

    public static void renderMetaballEffect(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, float f6, float f7, int n, Runnable runnable) {
        if (ShaderRenderer.hasInvalidSize(f3, f4)) {
            return;
        }
        defaultRenderTarget.beginPass(true);
        runnable.run();
        defaultRenderTarget.endPass();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        ShaderRenderer.prepareShaderState();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture((int)0, (int)defaultRenderTarget.getColorAttachment());
        metaballShader.bindShaderProgram();
        metaballShader.getUniform("Size").set(f3, f4);
        metaballShader.getUniform("Threshold").set(f5);
        metaballShader.getUniform("Smooth").set(f6);
        metaballShader.getUniform("RadiusPx").set(f7);
        metaballShader.getUniform("Iterations").set(n);
        int n2 = WINDOW.getScaledWidth();
        int n3 = WINDOW.getScaledHeight();
        float f8 = f / (float)n2;
        float f9 = ((float)n3 - f2 - f4) / (float)n3;
        float f10 = f3 / (float)n2;
        float f11 = f4 / (float)n3;
        int n4 = -1;
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        class_2872.vertex(matrix4f, f, f2, 0.0f).texture(f8, f9 + f11).color(n4);
        class_2872.vertex(matrix4f, f, f2 + f4, 0.0f).texture(f8, f9).color(n4);
        class_2872.vertex(matrix4f, f + f3, f2 + f4, 0.0f).texture(f8 + f10, f9).color(n4);
        class_2872.vertex(matrix4f, f + f3, f2, 0.0f).texture(f8 + f10, f9 + f11).color(n4);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.enableCull();
        ShaderRenderer.restoreRenderState();
    }

    public static void drawFullscreenQuad(float f, float f2, float f3, float f4, boolean bl) {
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        int n = -1;
        float f5 = bl ? 0.0f : 1.0f;
        float f6 = bl ? 1.0f : 0.0f;
        class_2872.vertex(f, f2, 0.0f).texture(0.0f, f6).color(-1);
        class_2872.vertex(f, f2 + f4, 0.0f).texture(0.0f, f5).color(-1);
        class_2872.vertex(f + f3, f2 + f4, 0.0f).texture(1.0f, f5).color(-1);
        class_2872.vertex(f + f3, f2, 0.0f).texture(1.0f, f6).color(-1);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawLineSegment(MatrixStack class_45872, Vec2f VanillaAdventureTabAdvancementGenerator, Vec2f MagmaBlock, ColorRGBA colorRGBA) {
        class_45872.push();
        try {
            Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.lineWidth((float)1.0f);
            ShaderRenderer.prepareShaderState();
            BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            class_2872.vertex(matrix4f, VanillaAdventureTabAdvancementGenerator.x, VanillaAdventureTabAdvancementGenerator.y, 0.0f).color(colorRGBA.getRGB());
            class_2872.vertex(matrix4f, MagmaBlock.x, MagmaBlock.y, 0.0f).color(colorRGBA.getRGB());
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
            ShaderRenderer.restoreRenderState();
        }
        finally {
            RenderSystem.disableBlend();
            RenderSystem.lineWidth((float)1.0f);
            class_45872.pop();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawBezierCurve(MatrixStack class_45872, Vec2f VanillaAdventureTabAdvancementGenerator, Vec2f MagmaBlock, Vec2f VanillaHusbandryTabAdvancementGenerator, Vec2f BlockMirror, ColorRGBA colorRGBA, int n) {
        class_45872.push();
        try {
            Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
            int n2 = ShaderRenderer.getBezierSampleCount(VanillaAdventureTabAdvancementGenerator, MagmaBlock, VanillaHusbandryTabAdvancementGenerator, BlockMirror, n);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.lineWidth((float)1.0f);
            ShaderRenderer.prepareShaderState();
            BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= n2; ++i) {
                float f = (float)i / (float)n2;
                float f2 = (float)MathUtils.interpolateCubicBezier(f, VanillaAdventureTabAdvancementGenerator.x, MagmaBlock.x, VanillaHusbandryTabAdvancementGenerator.x, BlockMirror.x);
                float f3 = (float)MathUtils.interpolateCubicBezier(f, VanillaAdventureTabAdvancementGenerator.y, MagmaBlock.y, VanillaHusbandryTabAdvancementGenerator.y, BlockMirror.y);
                class_2872.vertex(matrix4f, f2, f3, 0.0f).color(colorRGBA.getRGB());
            }
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
            ShaderRenderer.restoreRenderState();
        }
        finally {
            RenderSystem.disableBlend();
            RenderSystem.lineWidth((float)1.0f);
            class_45872.pop();
        }
    }

    public static void drawBezierStroke(MatrixStack class_45872, float f, float f2, float f3, float f4, Vec2f VanillaAdventureTabAdvancementGenerator, Vec2f MagmaBlock, Vec2f VanillaHusbandryTabAdvancementGenerator, Vec2f BlockMirror, float f5, ColorRGBA colorRGBA) {
        if (ShaderRenderer.hasInvalidSize(f3, f4) || f5 <= 0.0f || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        bezierShader.bindShaderProgram();
        bezierShader.getUniform("Size").set(f3, f4);
        bezierShader.getUniform("P0").set(VanillaAdventureTabAdvancementGenerator.x - f, VanillaAdventureTabAdvancementGenerator.y - f2);
        bezierShader.getUniform("P1").set(MagmaBlock.x - f, MagmaBlock.y - f2);
        bezierShader.getUniform("P2").set(VanillaHusbandryTabAdvancementGenerator.x - f, VanillaHusbandryTabAdvancementGenerator.y - f2);
        bezierShader.getUniform("P3").set(BlockMirror.x - f, BlockMirror.y - f2);
        bezierShader.getUniform("Thickness").set(f5);
        ShaderRenderer.prepareShaderState();
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f, f2, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f, f2 + f4, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f + f3, f2 + f4, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f + f3, f2, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawGradientPolyline(MatrixStack class_45872, float[] fArray, float[] fArray2, float f, ColorRGBA colorRGBA, ColorRGBA colorRGBA2) {
        if (fArray == null || fArray2 == null || fArray.length < 2 || fArray.length != fArray2.length) {
            return;
        }
        if (ShaderRenderer.isTooTransparent(colorRGBA) && ShaderRenderer.isTooTransparent(colorRGBA2)) {
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        class_45872.push();
        try {
            Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            ShaderRenderer.prepareShaderState();
            BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            int n = colorRGBA.getRGB();
            int n2 = colorRGBA2.getRGB();
            for (int i = 0; i < fArray.length - 1; ++i) {
                float f2 = fArray[i];
                float f3 = fArray[i + 1];
                float f4 = Math.min(fArray2[i], f);
                float f5 = Math.min(fArray2[i + 1], f);
                class_2872.vertex(matrix4f, f2, f4, 0.0f).color(n);
                class_2872.vertex(matrix4f, f2, f, 0.0f).color(n2);
                class_2872.vertex(matrix4f, f3, f, 0.0f).color(n2);
                class_2872.vertex(matrix4f, f3, f5, 0.0f).color(n);
            }
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
            ShaderRenderer.restoreRenderState();
        }
        finally {
            class_45872.pop();
        }
    }

    private static int getBezierSampleCount(Vec2f VanillaAdventureTabAdvancementGenerator, Vec2f MagmaBlock, Vec2f VanillaHusbandryTabAdvancementGenerator, Vec2f BlockMirror, int n) {
        if (n <= 1) {
            return 1;
        }
        float f = ShaderRenderer.distanceBetweenPoints(VanillaAdventureTabAdvancementGenerator, MagmaBlock) + ShaderRenderer.distanceBetweenPoints(MagmaBlock, VanillaHusbandryTabAdvancementGenerator) + ShaderRenderer.distanceBetweenPoints(VanillaHusbandryTabAdvancementGenerator, BlockMirror);
        if (!Float.isFinite(f) || f <= 0.0f) {
            return 1;
        }
        int n2 = Math.max(1, Math.round(f));
        int n3 = Math.max(1, WINDOW.getScaledWidth() + WINDOW.getScaledHeight());
        return Math.min(n, Math.min(n2, n3));
    }

    private static float distanceBetweenPoints(Vec2f VanillaAdventureTabAdvancementGenerator, Vec2f MagmaBlock) {
        return (float)Math.hypot(MagmaBlock.x - VanillaAdventureTabAdvancementGenerator.x, MagmaBlock.y - VanillaAdventureTabAdvancementGenerator.y);
    }

    private static float evaluateCubicBezier(float f, float f2, float f3, float f4, float f5) {
        float f6 = 1.0f - f;
        float f7 = f * f;
        float f8 = f6 * f6;
        return f8 * f6 * f2 + 3.0f * f8 * f * f3 + 3.0f * f6 * f7 * f4 + f7 * f * f5;
    }

    public static void drawRectangle(MatrixStack class_45872, float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
        if (ShaderRenderer.hasInvalidSize(f3, f4) || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        RenderBufferContext activeRenderContext = RenderBufferContext.current();
        if (activeRenderContext instanceof ColorRenderContext colorRenderContext) {
            BufferBuilder bufferBuilder = colorRenderContext.bufferBuilder();
            Matrix4f matrix4f = colorRenderContext.matrixStack().peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).color(colorRGBA.getRGB());
            bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).color(colorRGBA.getRGB());
            bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).color(colorRGBA.getRGB());
            bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(colorRGBA.getRGB());
            return;
        }
        WidgetBatchRenderer widgetBatchRenderer = WidgetBatchRenderer.getCurrentBatch();
        if (widgetBatchRenderer != null) {
            widgetBatchRenderer.queueRectangle(class_45872.peek().getPositionMatrix(), f, f2, f3, f4, colorRGBA);
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        ShaderRenderer.prepareShaderState();
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f, f2 + f4, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f + f3, f2 + f4, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f + f3, f2, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f, f2, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    public static void drawRoundedRectangle(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        if (ShaderRenderer.hasInvalidSize(f3, f4) || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f6 = 0.5f;
        WidgetBatchRenderer widgetBatchRenderer = WidgetBatchRenderer.getCurrentBatch();
        if (widgetBatchRenderer != null) {
            widgetBatchRenderer.queueSquircleRectangle(matrix4f, f, f2, f3, f4, f5, widgetState, colorRGBA);
            class_45872.pop();
            return;
        }
        squircleShader.bindShaderProgram();
        squircleShader.getUniform("Size").set(f3, f4);
        squircleShader.getUniform("Radius").set(widgetState.topLeftRadius() * f5 / 2.0f, widgetState.bottomLeftRadius() * f5 / 2.0f, widgetState.topRightRadius() * f5 / 2.0f, widgetState.bottomRightRadius() * f5 / 2.0f);
        squircleShader.getUniform("Smoothness").set(f6);
        squircleShader.getUniform("CornerSmoothness").set(f5);
        ShaderRenderer.prepareShaderState();
        float f7 = -f6 / 2.0f + f6 * 2.0f;
        float f8 = f6 / 2.0f + f6;
        float f9 = f - f7 / 2.0f;
        float f10 = f2 - f8 / 2.0f;
        float f11 = f3 + f7;
        float f12 = f4 + f8;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f9, f10, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f9, f10 + f12, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f9 + f11, f10 + f12, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f9 + f11, f10, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    public static void drawGradientRoundedRectangle(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA, ColorRGBA colorRGBA2, ColorRGBA colorRGBA3, ColorRGBA colorRGBA4) {
        if (ShaderRenderer.hasInvalidSize(f3, f4) || ShaderRenderer.isTooTransparent(colorRGBA) && ShaderRenderer.isTooTransparent(colorRGBA2) && ShaderRenderer.isTooTransparent(colorRGBA3) && ShaderRenderer.isTooTransparent(colorRGBA4)) {
            return;
        }
        WidgetBatchRenderer widgetBatchRenderer = WidgetBatchRenderer.getCurrentBatch();
        if (widgetBatchRenderer != null && widgetBatchRenderer.usesGeometryBatch()) {
            class_45872.push();
            Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
            WidgetState widgetState2 = new WidgetState(widgetState.topLeftRadius() * f5 / 2.0f, widgetState.topRightRadius() * f5 / 2.0f, widgetState.bottomRightRadius() * f5 / 2.0f, widgetState.bottomLeftRadius() * f5 / 2.0f);
            widgetBatchRenderer.renderRoundedRectangleWithGeometry(matrix4f, f, f2, f3, f4, widgetState2, 0.5f, f5, colorRGBA, colorRGBA2, colorRGBA3, colorRGBA4, false);
            class_45872.pop();
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f6 = 0.5f;
        squircleShader.bindShaderProgram();
        squircleShader.getUniform("Size").set(f3, f4);
        squircleShader.getUniform("Radius").set(widgetState.topLeftRadius() * f5 / 2.0f, widgetState.bottomLeftRadius() * f5 / 2.0f, widgetState.topRightRadius() * f5 / 2.0f, widgetState.bottomRightRadius() * f5 / 2.0f);
        squircleShader.getUniform("Smoothness").set(f6);
        squircleShader.getUniform("CornerSmoothness").set(f5);
        ShaderRenderer.prepareShaderState();
        float f7 = -f6 / 2.0f + f6 * 2.0f;
        float f8 = f6 / 2.0f + f6;
        float f9 = f - f7 / 2.0f;
        float f10 = f2 - f8 / 2.0f;
        float f11 = f3 + f7;
        float f12 = f4 + f8;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f9, f10, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f9, f10 + f12, 0.0f).color(colorRGBA2.getRGB());
        class_2872.vertex(matrix4f, f9 + f11, f10 + f12, 0.0f).color(colorRGBA3.getRGB());
        class_2872.vertex(matrix4f, f9 + f11, f10, 0.0f).color(colorRGBA4.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    public static void drawNotificationBackground(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, WidgetState widgetState, GradientColors gradientColors) {
        ShaderRenderer.drawGradientRoundedRectangle(class_45872, f, f2, f3, f4, f5, widgetState, gradientColors.topLeft(), gradientColors.bottomLeft(), gradientColors.bottomRight(), gradientColors.topRight());
    }

    public static void drawLoadingBar(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        if (ShaderRenderer.hasInvalidSize(f3, f4) || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f6 = 0.5f;
        loadingShader.bindShaderProgram();
        loadingShader.getUniform("Size").set(f3, f4);
        loadingShader.getUniform("Radius").set(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius());
        loadingShader.getUniform("Smoothness").set(f6);
        loadingShader.getUniform("Progress").set(f5);
        loadingShader.getUniform("StripeWidth").set(0.0f);
        loadingShader.getUniform("Fade").set(0.5f);
        ShaderRenderer.prepareShaderState();
        float f7 = -f6 / 2.0f + f6 * 2.0f;
        float f8 = f6 / 2.0f + f6;
        float f9 = f - f7 / 2.0f;
        float f10 = f2 - f8 / 2.0f;
        float f11 = f3 + f7;
        float f12 = f4 + f8;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f9, f10, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f9, f10 + f12, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f9 + f11, f10 + f12, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f9 + f11, f10, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    public static void drawLiquidGlass(MatrixStack class_45872, float f, float f2, float f3, float f4, WidgetState widgetState, ColorRGBA colorRGBA, float f5, float f6, ColorRGBA colorRGBA2, float f7, boolean bl, float f8, float f9, float f10, boolean bl2) {
        float f11;
        float f12;
        float f13;
        float f14;
        Vector3f vector3f;
        if (ShaderRenderer.hasInvalidSize(f3, f4) || ShaderRenderer.isTooTransparent(colorRGBA) || f5 <= 0.0f) {
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        liquidGlassShader.bindShaderProgram();
        RenderSystem.setShaderTexture((int)0, (int)(bl2 ? defaultRenderTarget.getColorAttachment() : ShaderRenderer.getActiveTextureId()));
        liquidGlassShader.getUniform("GlobalAlpha").set(f5);
        liquidGlassShader.getUniform("Size").set(f3, f4);
        liquidGlassShader.getUniform("Radius").set(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius());
        liquidGlassShader.getUniform("Smoothness").set(0.5f);
        liquidGlassShader.getUniform("FresnelPower").set(f6);
        liquidGlassShader.getUniform("FresnelColor").set(new float[]{colorRGBA2.getRed() / 255.0f, colorRGBA2.getGreen() / 255.0f, colorRGBA2.getBlue() / 255.0f});
        liquidGlassShader.getUniform("FresnelAlpha").set(colorRGBA2.getAlpha() / 255.0f);
        liquidGlassShader.getUniform("BaseAlpha").set(f7);
        liquidGlassShader.getUniform("FresnelInvert").set(bl ? 1 : 0);
        liquidGlassShader.getUniform("FresnelMix").set(f8);
        liquidGlassShader.getUniform("DistortStrength").set(Interface.getDistortionStrength());
        liquidGlassShader.getUniform("DistortRadius").set(Interface.getDistortionRadius());
        liquidGlassShader.getUniform("Aberration").set(Interface.getAberrationStrength());
        liquidGlassShader.getUniform("Saturation").set(Interface.getSaturation());
        liquidGlassShader.getUniform("CornerSmoothness").set(f10);
        ShaderRenderer.prepareShaderState();
        RenderSystem.disableCull();
        int n = WINDOW.getScaledWidth();
        int n2 = WINDOW.getScaledHeight();
        if (!MatrixUtil.isIdentity((Matrix4f)matrix4f)) {
            vector3f = new Vector3f(f, f2, 0.0f);
            Vector3f vector3f2 = new Vector3f(f + f3, f2 + f4, 0.0f);
            matrix4f.transformPosition(vector3f);
            matrix4f.transformPosition(vector3f2);
            f14 = vector3f.x / (float)n;
            f13 = ((float)n2 - vector3f.y - (vector3f2.y - vector3f.y)) / (float)n2;
            f12 = (vector3f2.x - vector3f.x) / (float)n;
            f11 = (vector3f2.y - vector3f.y) / (float)n2;
        } else {
            f14 = f / (float)n;
            f13 = ((float)n2 - f2 - f4) / (float)n2;
            f12 = f3 / (float)n;
            f11 = f4 / (float)n2;
        }
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).texture(f14, f13 + f11).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).texture(f14, f13).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).texture(f14 + f12, f13).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).texture(f14 + f12, f13 + f11).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.enableCull();
        ShaderRenderer.restoreRenderState();
    }

    public static void drawLiquidGlassRegion(Rect rect, MatrixStack class_45872, float f, float f2, float f3, float f4, WidgetState widgetState, ColorRGBA colorRGBA, float f5, float f6, ColorRGBA colorRGBA2, float f7, boolean bl, float f8, float f9, float f10, int n) {
        float f11;
        float f12;
        float f13;
        float f14;
        Vector3f vector3f;
        WidgetBatchRenderer.flushCurrentBatch();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        liquidGlassShader.bindShaderProgram();
        RenderSystem.setShaderTexture((int)0, (int)n);
        liquidGlassShader.getUniform("GlobalAlpha").set(f5);
        liquidGlassShader.getUniform("Size").set(f3, f4);
        liquidGlassShader.getUniform("Radius").set(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius());
        liquidGlassShader.getUniform("Smoothness").set(0.5f);
        liquidGlassShader.getUniform("FresnelPower").set(f6);
        liquidGlassShader.getUniform("FresnelColor").set(new float[]{colorRGBA2.getRed() / 255.0f, colorRGBA2.getGreen() / 255.0f, colorRGBA2.getBlue() / 255.0f});
        liquidGlassShader.getUniform("FresnelAlpha").set(colorRGBA2.getAlpha() / 255.0f);
        liquidGlassShader.getUniform("BaseAlpha").set(f7);
        liquidGlassShader.getUniform("FresnelInvert").set(bl ? 1 : 0);
        liquidGlassShader.getUniform("FresnelMix").set(f8);
        liquidGlassShader.getUniform("DistortStrength").set(Interface.getDistortionStrength());
        liquidGlassShader.getUniform("DistortRadius").set(Interface.getDistortionRadius());
        liquidGlassShader.getUniform("Aberration").set(Interface.getAberrationStrength());
        liquidGlassShader.getUniform("Saturation").set(Interface.getSaturation());
        liquidGlassShader.getUniform("CornerSmoothness").set(f10);
        ShaderRenderer.prepareShaderState();
        RenderSystem.disableCull();
        float f15 = rect.getWidth();
        float f16 = rect.getHeight();
        if (!MatrixUtil.isIdentity((Matrix4f)matrix4f)) {
            vector3f = new Vector3f(f, f2, 0.0f);
            Vector3f vector3f2 = new Vector3f(f + f3, f2 + f4, 0.0f);
            matrix4f.transformPosition(vector3f);
            matrix4f.transformPosition(vector3f2);
            float f17 = vector3f.x;
            float f18 = vector3f.y;
            float f19 = vector3f2.x - vector3f.x;
            float f20 = vector3f2.y - vector3f.y;
            f14 = (f17 - rect.getX()) / f15;
            f13 = (f18 - rect.getY()) / f16 + f20 / f16;
            f12 = f19 / f15;
            f11 = -f20 / f16;
        } else {
            f14 = (f - rect.getX()) / f15;
            f13 = (f2 - rect.getY()) / f16 + f4 / f16;
            f12 = f3 / f15;
            f11 = -f4 / f16;
        }
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).texture(f14, f13 + f11).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).texture(f14, f13).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).texture(f14 + f12, f13).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).texture(f14 + f12, f13 + f11).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.enableCull();
        ShaderRenderer.restoreRenderState();
    }

    public static void drawRoundedRectangle(MatrixStack class_45872, float f, float f2, float f3, float f4, WidgetState widgetState, ColorRGBA colorRGBA) {
        if (ShaderRenderer.hasInvalidSize(f3, f4) || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f5 = 0.5f;
        WidgetBatchRenderer widgetBatchRenderer = WidgetBatchRenderer.getCurrentBatch();
        if (widgetBatchRenderer != null) {
            widgetBatchRenderer.queueRoundedRectangle(matrix4f, f, f2, f3, f4, widgetState, colorRGBA);
            class_45872.pop();
            return;
        }
        rectangleShader.bindShaderProgram();
        rectangleShader.getUniform("Size").set(f3, f4);
        rectangleShader.getUniform("Radius").set(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius());
        rectangleShader.getUniform("Smoothness").set(f5);
        ShaderRenderer.prepareShaderState();
        float f6 = -f5 / 2.0f + f5 * 2.0f;
        float f7 = f5 / 2.0f + f5;
        float f8 = f - f6 / 2.0f;
        float f9 = f2 - f7 / 2.0f;
        float f10 = f3 + f6;
        float f11 = f4 + f7;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f8, f9, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f8, f9 + f11, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f8 + f10, f9 + f11, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f8 + f10, f9, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    public static void drawGradientRoundedRectangle(MatrixStack class_45872, float f, float f2, float f3, float f4, WidgetState widgetState, ColorRGBA colorRGBA, ColorRGBA colorRGBA2, ColorRGBA colorRGBA3, ColorRGBA colorRGBA4) {
        if (ShaderRenderer.hasInvalidSize(f3, f4) || ShaderRenderer.isTooTransparent(colorRGBA) && ShaderRenderer.isTooTransparent(colorRGBA2) && ShaderRenderer.isTooTransparent(colorRGBA3) && ShaderRenderer.isTooTransparent(colorRGBA4)) {
            return;
        }
        WidgetBatchRenderer widgetBatchRenderer = WidgetBatchRenderer.getCurrentBatch();
        if (widgetBatchRenderer != null && widgetBatchRenderer.usesGeometryBatch()) {
            class_45872.push();
            Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
            widgetBatchRenderer.renderRoundedRectangleWithGeometry(matrix4f, f, f2, f3, f4, widgetState, 0.5f, 2.0f, colorRGBA, colorRGBA2, colorRGBA3, colorRGBA4, true);
            class_45872.pop();
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f5 = 0.5f;
        gradientRectangleShader.bindShaderProgram();
        gradientRectangleShader.getUniform("Size").set(f3, f4);
        gradientRectangleShader.getUniform("Radius").set(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius());
        gradientRectangleShader.getUniform("Smoothness").set(f5);
        gradientRectangleShader.getUniform("TopLeftColor").set(colorRGBA.getRed() / 255.0f, colorRGBA.getGreen() / 255.0f, colorRGBA.getBlue() / 255.0f, colorRGBA.getAlpha() / 255.0f);
        gradientRectangleShader.getUniform("BottomLeftColor").set(colorRGBA2.getRed() / 255.0f, colorRGBA2.getGreen() / 255.0f, colorRGBA2.getBlue() / 255.0f, colorRGBA2.getAlpha() / 255.0f);
        gradientRectangleShader.getUniform("BottomRightColor").set(colorRGBA3.getRed() / 255.0f, colorRGBA3.getGreen() / 255.0f, colorRGBA3.getBlue() / 255.0f, colorRGBA3.getAlpha() / 255.0f);
        gradientRectangleShader.getUniform("TopRightColor").set(colorRGBA4.getRed() / 255.0f, colorRGBA4.getGreen() / 255.0f, colorRGBA4.getBlue() / 255.0f, colorRGBA4.getAlpha() / 255.0f);
        ShaderRenderer.prepareShaderState();
        float f6 = -f5 / 2.0f + f5 * 2.0f;
        float f7 = f5 / 2.0f + f5;
        float f8 = f - f6 / 2.0f;
        float f9 = f2 - f7 / 2.0f;
        float f10 = f3 + f6;
        float f11 = f4 + f7;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f8, f9, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f8, f9 + f11, 0.0f).color(colorRGBA2.getRGB());
        class_2872.vertex(matrix4f, f8 + f10, f9 + f11, 0.0f).color(colorRGBA3.getRGB());
        class_2872.vertex(matrix4f, f8 + f10, f9, 0.0f).color(colorRGBA4.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    public static void drawNotificationBackground(MatrixStack class_45872, float f, float f2, float f3, float f4, WidgetState widgetState, GradientColors gradientColors) {
        ShaderRenderer.drawGradientRoundedRectangle(class_45872, f, f2, f3, f4, widgetState, gradientColors.topLeft(), gradientColors.bottomLeft(), gradientColors.bottomRight(), gradientColors.topRight());
    }

    public static void drawBorderedRoundedRectangle(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        if (ShaderRenderer.hasInvalidSize(f3, f4) || f5 <= 0.0f || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f6 = 0.5f;
        float f7 = 1.0f;
        WidgetBatchRenderer widgetBatchRenderer = WidgetBatchRenderer.getCurrentBatch();
        if (widgetBatchRenderer != null) {
            widgetBatchRenderer.queueBorderedRoundedRectangle(matrix4f, f, f2, f3, f4, f5, widgetState, colorRGBA);
            class_45872.pop();
            return;
        }
        borderShader.bindShaderProgram();
        borderShader.getUniform("Size").set(f3, f4);
        borderShader.getUniform("Radius").set(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius());
        borderShader.getUniform("Smoothness").set(f6, f7);
        borderShader.getUniform("Thickness").set(f5);
        ShaderRenderer.prepareShaderState();
        float f8 = -f7 / 2.0f + f7 * 2.0f;
        float f9 = f7 / 2.0f + f7;
        float f10 = f - f8 / 2.0f;
        float f11 = f2 - f9 / 2.0f;
        float f12 = f3 + f8;
        float f13 = f4 + f9;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f10, f11, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f10, f11 + f13, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f10 + f12, f11 + f13, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f10 + f12, f11, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    public static void drawDashedRoundedBorder(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, WidgetState widgetState, float f6, float f7, ColorRGBA colorRGBA, float f8, float f9, float f10, float f11) {
        if (ShaderRenderer.hasInvalidSize(f3, f4) || f5 <= 0.0f || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f12 = 0.5f;
        float f13 = 1.0f;
        dashedBorderShader.bindShaderProgram();
        dashedBorderShader.getUniform("Size").set(f3, f4);
        dashedBorderShader.getUniform("Radius").set(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius());
        dashedBorderShader.getUniform("Smoothness").set(f12, f13);
        dashedBorderShader.getUniform("Thickness").set(f5);
        dashedBorderShader.getUniform("DashLength").set(f6);
        dashedBorderShader.getUniform("GapLength").set(f7);
        dashedBorderShader.getUniform("Dashed").set(f8, f9, f10, f11);
        ShaderRenderer.prepareShaderState();
        float f14 = -f13 / 2.0f + f13 * 2.0f;
        float f15 = f13 / 2.0f + f13;
        float f16 = f - f14 / 2.0f;
        float f17 = f2 - f15 / 2.0f;
        float f18 = f3 + f14;
        float f19 = f4 + f15;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f16, f17, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f16, f17 + f19, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f16 + f18, f17 + f19, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f16 + f18, f17, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    public static void drawSquircleBorder(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, float f6, WidgetState widgetState, ColorRGBA colorRGBA) {
        if (ShaderRenderer.hasInvalidSize(f3, f4) || f5 <= 0.0f || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f7 = 0.5f;
        float f8 = 0.5f;
        WidgetBatchRenderer widgetBatchRenderer = WidgetBatchRenderer.getCurrentBatch();
        if (widgetBatchRenderer != null) {
            widgetBatchRenderer.queueSquircleBorderRectangle(matrix4f, f, f2, f3, f4, f5, f6, widgetState, colorRGBA);
            class_45872.pop();
            return;
        }
        squircleBorderShader.bindShaderProgram();
        squircleBorderShader.getUniform("Size").set(f3, f4);
        squircleBorderShader.getUniform("Radius").set(widgetState.topLeftRadius() * f6 / 2.0f, widgetState.bottomLeftRadius() * f6 / 2.0f, widgetState.topRightRadius() * f6 / 2.0f, widgetState.bottomRightRadius() * f6 / 2.0f);
        squircleBorderShader.getUniform("Smoothness").set(f7, f8);
        squircleBorderShader.getUniform("Thickness").set(f5);
        squircleBorderShader.getUniform("CornerSmoothness").set(f6);
        ShaderRenderer.prepareShaderState();
        float f9 = -f8 / 2.0f + f8 * 2.0f;
        float f10 = f8 / 2.0f + f8;
        float f11 = f - f9 / 2.0f;
        float f12 = f2 - f10 / 2.0f;
        float f13 = f3 + f9;
        float f14 = f4 + f10;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f11, f12, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f11, f12 + f14, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f11 + f13, f12 + f14, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f11 + f13, f12, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    public static void drawTexturedRectangle(MatrixStack class_45872, Identifier class_29602, float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
        if (class_29602 == null || ShaderRenderer.hasInvalidSize(f3, f4) || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        RenderBufferContext activeRenderContext = RenderBufferContext.current();
        if (activeRenderContext instanceof TextureRenderContext textureRenderContext) {
            BufferBuilder bufferBuilder = textureRenderContext.bufferBuilder();
            Matrix4f matrix4f = textureRenderContext.matrixStack().peek().getPositionMatrix();
            RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
            bufferBuilder.vertex(matrix4f, f, f2, 0.0f).texture(0.0f, 0.0f).color(colorRGBA.getRGB());
            bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).texture(0.0f, 1.0f).color(colorRGBA.getRGB());
            bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).texture(1.0f, 1.0f).color(colorRGBA.getRGB());
            bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).texture(1.0f, 0.0f).color(colorRGBA.getRGB());
            return;
        }
        WidgetBatchRenderer widgetBatchRenderer = WidgetBatchRenderer.getCurrentBatch();
        if (widgetBatchRenderer != null) {
            widgetBatchRenderer.queueTexturedRectangle(minecraftClient.getTextureManager().getTexture(class_29602).getGlId(), class_45872.peek().getPositionMatrix(), f, f2, f3, f4, 0.0f, 0.0f, 1.0f, 1.0f, colorRGBA);
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        ShaderRenderer.prepareShaderState();
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        class_2872.vertex(matrix4f, f, f2, 0.0f).texture(0.0f, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f, f2 + f4, 0.0f).texture(0.0f, 1.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f + f3, f2 + f4, 0.0f).texture(1.0f, 1.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f + f3, f2, 0.0f).texture(1.0f, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        RenderSystem.setShaderTexture((int)0, (int)0);
        class_45872.pop();
    }

    public static void drawTexturedRectangleWithUv(MatrixStack class_45872, Identifier class_29602, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, ColorRGBA colorRGBA) {
        if (class_29602 == null || ShaderRenderer.hasInvalidSize(f3, f4) || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        RenderBufferContext activeRenderContext = RenderBufferContext.current();
        if (activeRenderContext instanceof TextureRenderContext textureRenderContext) {
            BufferBuilder bufferBuilder = textureRenderContext.bufferBuilder();
            Matrix4f matrix4f = textureRenderContext.matrixStack().peek().getPositionMatrix();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
            int n = colorRGBA.getRGB();
            float f9 = f + f3;
            float f10 = f2 + f4;
            bufferBuilder.vertex(matrix4f, f, f2, 0.0f).texture(f5, f7).color(n);
            bufferBuilder.vertex(matrix4f, f, f10, 0.0f).texture(f5, f8).color(n);
            bufferBuilder.vertex(matrix4f, f9, f10, 0.0f).texture(f6, f8).color(n);
            bufferBuilder.vertex(matrix4f, f9, f2, 0.0f).texture(f6, f7).color(n);
            return;
        }
        WidgetBatchRenderer widgetBatchRenderer = WidgetBatchRenderer.getCurrentBatch();
        if (widgetBatchRenderer != null) {
            widgetBatchRenderer.queueTexturedRectangle(minecraftClient.getTextureManager().getTexture(class_29602).getGlId(), class_45872.peek().getPositionMatrix(), f, f2, f3, f4, f5, f7, f6, f8, colorRGBA);
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        class_45872.push();
        int n = colorRGBA.getRGB();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f11 = f + f3;
        float f12 = f2 + f4;
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        class_2872.vertex(matrix4f, f, f2, 0.0f).texture(f5, f7).color(n);
        class_2872.vertex(matrix4f, f, f12, 0.0f).texture(f5, f8).color(n);
        class_2872.vertex(matrix4f, f11, f12, 0.0f).texture(f6, f8).color(n);
        class_2872.vertex(matrix4f, f11, f2, 0.0f).texture(f6, f7).color(n);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        RenderSystem.setShaderTexture((int)0, (int)0);
        class_45872.pop();
        RenderSystem.disableBlend();
    }

    public static void drawTextureRegion(MatrixStack class_45872, TextureRegion textureRegion, float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
        if (textureRegion == null) {
            return;
        }
        ShaderRenderer.drawTexturedRectangleWithUv(class_45872, textureRegion.getTexture(), f, f2, f3, f4, textureRegion.getU1(), textureRegion.getU2(), textureRegion.getV1(), textureRegion.getV2(), colorRGBA);
    }

    public static void drawIcon(MatrixStack class_45872, IconStyle iconStyle, float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
        ShaderRenderer.drawTexturedRectangleWithUv(class_45872, RockstarClient.resourceId(iconStyle.getIconSet().getResourcePath()), f, f2, f3, f4, iconStyle.iconOffset / iconStyle.getIconSet().getAtlasSize(), (iconStyle.iconOffset + iconStyle.getIconSet().getIconHeight()) / iconStyle.getIconSet().getAtlasSize(), 0.0f, 1.0f, colorRGBA);
    }

    public static void drawTexturedRoundedRectangle(MatrixStack class_45872, Identifier class_29602, float f, float f2, float f3, float f4, WidgetState widgetState) {
        ShaderRenderer.drawTexturedRoundedRectangle(class_45872, class_29602, f, f2, f3, f4, widgetState, ColorPalette.WHITE);
    }

    public static void drawTexturedRoundedRectangle(MatrixStack class_45872, Identifier class_29602, float f, float f2, float f3, float f4, WidgetState widgetState, ColorRGBA colorRGBA) {
        if (class_29602 == null || ShaderRenderer.hasInvalidSize(f3, f4) || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f5 = 0.5f;
        float f6 = -f5 / 2.0f + f5 * 2.0f;
        float f7 = f5 / 2.0f + f5;
        float f8 = f - f6 / 2.0f;
        float f9 = f2 - f7 / 2.0f;
        float f10 = f3 + f6;
        float f11 = f4 + f7;
        WidgetBatchRenderer widgetBatchRenderer = WidgetBatchRenderer.getCurrentBatch();
        if (widgetBatchRenderer != null) {
            widgetBatchRenderer.queueTexturedRectangleWithDefaults(minecraftClient.getTextureManager().getTexture(class_29602).getGlId(), matrix4f, f8, f9, f10, f11, 0.0f, 0.0f, 1.0f, 1.0f, f3, f4, widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius(), f5, colorRGBA);
            class_45872.pop();
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        textureShader.bindShaderProgram();
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        textureShader.getUniform("Size").set(f3, f4);
        textureShader.getUniform("Radius").set(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius());
        textureShader.getUniform("Smoothness").set(f5);
        ShaderRenderer.prepareShaderState();
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        class_2872.vertex(matrix4f, f8, f9, 0.0f).texture(0.0f, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f8, f9 + f11, 0.0f).texture(0.0f, 1.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f8 + f10, f9 + f11, 0.0f).texture(1.0f, 1.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f8 + f10, f9, 0.0f).texture(1.0f, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        RenderSystem.setShaderTexture((int)0, (int)0);
        class_45872.pop();
    }

    public static void drawSmoothRoundedRectangle(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        if (ShaderRenderer.hasInvalidSize(f3, f4) || f5 <= 0.0f || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        RenderBufferContext activeRenderContext = RenderBufferContext.current();
        if (activeRenderContext instanceof TextureRenderContext textureRenderContext) {
            BufferBuilder bufferBuilder = textureRenderContext.bufferBuilder();
            float f6 = -f5 / 2.0f + f5 * 2.0f;
            float f7 = f5 / 2.0f + f5;
            float f8 = f - f6 / 2.0f;
            float f9 = f2 - f7 / 2.0f;
            float f10 = f3 + f6;
            float f11 = f4 + f7;
            bufferBuilder.vertex(matrix4f, f8, f9, 0.0f).color(colorRGBA.getRGB());
            bufferBuilder.vertex(matrix4f, f8, f9 + f11, 0.0f).color(colorRGBA.getRGB());
            bufferBuilder.vertex(matrix4f, f8 + f10, f9 + f11, 0.0f).color(colorRGBA.getRGB());
            bufferBuilder.vertex(matrix4f, f8 + f10, f9, 0.0f).color(colorRGBA.getRGB());
            class_45872.pop();
            return;
        }
        WidgetBatchRenderer widgetBatchRenderer = WidgetBatchRenderer.getCurrentBatch();
        if (widgetBatchRenderer != null) {
            widgetBatchRenderer.queueSmoothRoundedRectangle(matrix4f, f, f2, f3, f4, f5, widgetState, colorRGBA);
            class_45872.pop();
            return;
        }
        rectangleShader.bindShaderProgram();
        rectangleShader.getUniform("Size").set(f3, f4);
        rectangleShader.getUniform("Radius").set(widgetState.topLeftRadius() * 3.0f, widgetState.bottomLeftRadius() * 3.0f, widgetState.topRightRadius() * 3.0f, widgetState.bottomRightRadius() * 3.0f);
        rectangleShader.getUniform("Smoothness").set(f5);
        ShaderRenderer.prepareShaderState();
        float f12 = -f5 / 2.0f + f5 * 2.0f;
        float f13 = f5 / 2.0f + f5;
        float f14 = f - f12 / 2.0f;
        float f15 = f2 - f13 / 2.0f;
        float f16 = f3 + f12;
        float f17 = f4 + f13;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f14, f15, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f14, f15 + f17, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f14 + f16, f15 + f17, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f14 + f16, f15, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    public static void prepareProjectedSquircle(float f, float f2, WidgetState widgetState) {
        if (f <= 0.0f) {
            return;
        }
        RenderTargetManager.setBlurRadius(2.0f);
        projectedSquircleShader.bindShaderProgram();
        RenderSystem.setShaderTexture((int)0, (int)ShaderRenderer.getActiveTextureId());
        projectedSquircleShader.getUniform("Size").set((float)minecraftClient.getWindow().getScaledWidth() * 2.0f, (float)minecraftClient.getWindow().getScaledHeight() * 2.0f);
        projectedSquircleShader.getUniform("Radius").set(widgetState.topLeftRadius() * f2 / 2.0f, widgetState.bottomLeftRadius() * f2 / 2.0f, widgetState.topRightRadius() * f2 / 2.0f, widgetState.bottomRightRadius() * f2 / 2.0f);
        projectedSquircleShader.getUniform("Smoothness").set(0.1f);
        projectedSquircleShader.getUniform("CornerSmoothness").set(f2);
        projectedSquircleShader.getUniform("ScreenSize").set((float)minecraftClient.getWindow().getScaledWidth(), (float)minecraftClient.getWindow().getScaledHeight());
        ShaderRenderer.prepareShaderState();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
    }

    public static void resetRenderState() {
        ShaderRenderer.restoreRenderState();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.setShaderTexture((int)0, (int)0);
    }

    public static void drawTexturedQuad(MatrixStack class_45872, float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f5 = 0.03f;
        float f6 = -f5 / 2.0f + f5 * 2.0f;
        float f7 = f5 / 2.0f + f5;
        float f8 = f - f6 / 2.0f;
        float f9 = f2 - f7 / 2.0f;
        float f10 = f3 + f6;
        float f11 = f4 + f7;
        int n = minecraftClient.getWindow().getScaledWidth();
        int n2 = minecraftClient.getWindow().getScaledHeight();
        float f12 = f8 / (float)n;
        float f13 = ((float)n2 - f9 - f11) / (float)n2;
        float f14 = f10 / (float)n;
        float f15 = f11 / (float)n2;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        class_2872.vertex(matrix4f, f8, f9, 0.0f).texture(f12, f13 + f15).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f8, f9 + f11, 0.0f).texture(f12, f13).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f8 + f10, f9 + f11, 0.0f).texture(f12 + f14, f13).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f8 + f10, f9, 0.0f).texture(f12 + f14, f13 + f15).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
    }

    public static void renderTexturedWidgetBatch(WidgetBatchRenderer.ShapeBatchKey shapeBatchKey, List<WidgetBatchRenderer.TexturedWidgetEntry> list) {
        if (shapeBatchKey == null || list == null || list.isEmpty()) {
            return;
        }
        if (shapeBatchKey.isSquircle()) {
            texturedSquircleShader.bindShaderProgram();
            texturedSquircleShader.getUniform("Size").set(shapeBatchKey.getWidth(), shapeBatchKey.getHeight());
            texturedSquircleShader.getUniform("Radius").set(shapeBatchKey.getTopLeftRadius(), shapeBatchKey.getBottomLeftRadius(), shapeBatchKey.getTopRightRadius(), shapeBatchKey.getBottomRightRadius());
            texturedSquircleShader.getUniform("Smoothness").set(shapeBatchKey.getEdgeSmoothness());
            texturedSquircleShader.getUniform("CornerSmoothness").set(shapeBatchKey.getCornerSmoothness());
        } else {
            textureShader.bindShaderProgram();
            textureShader.getUniform("Size").set(shapeBatchKey.getWidth(), shapeBatchKey.getHeight());
            textureShader.getUniform("Radius").set(shapeBatchKey.getTopLeftRadius(), shapeBatchKey.getBottomLeftRadius(), shapeBatchKey.getTopRightRadius(), shapeBatchKey.getBottomRightRadius());
            textureShader.getUniform("Smoothness").set(shapeBatchKey.getEdgeSmoothness());
        }
        RenderSystem.setShaderTexture((int)0, (int)shapeBatchKey.getTextureId());
        ShaderRenderer.prepareShaderState();
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (WidgetBatchRenderer.TexturedWidgetEntry texturedWidgetEntry : list) {
            class_2872.vertex(texturedWidgetEntry.getTransform(), texturedWidgetEntry.getX(), texturedWidgetEntry.getY(), 0.0f).texture(texturedWidgetEntry.getU1(), texturedWidgetEntry.getV1()).color(texturedWidgetEntry.getColor());
            class_2872.vertex(texturedWidgetEntry.getTransform(), texturedWidgetEntry.getX(), texturedWidgetEntry.getY() + texturedWidgetEntry.getHeight(), 0.0f).texture(texturedWidgetEntry.getU1(), texturedWidgetEntry.getV2()).color(texturedWidgetEntry.getColor());
            class_2872.vertex(texturedWidgetEntry.getTransform(), texturedWidgetEntry.getX() + texturedWidgetEntry.getWidth(), texturedWidgetEntry.getY() + texturedWidgetEntry.getHeight(), 0.0f).texture(texturedWidgetEntry.getU2(), texturedWidgetEntry.getV2()).color(texturedWidgetEntry.getColor());
            class_2872.vertex(texturedWidgetEntry.getTransform(), texturedWidgetEntry.getX() + texturedWidgetEntry.getWidth(), texturedWidgetEntry.getY(), 0.0f).texture(texturedWidgetEntry.getU2(), texturedWidgetEntry.getV1()).color(texturedWidgetEntry.getColor());
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        RenderSystem.setShaderTexture((int)0, (int)0);
    }

    public static void drawBackdropBlur(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawBackdropBlur(class_45872, 0, f, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, widgetState, colorRGBA);
    }

    public static void drawBackdropBlur(MatrixStack class_45872, int n, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, WidgetState widgetState, ColorRGBA colorRGBA) {
        float f12;
        float f13;
        float f14;
        float f15;
        Vector3f vector3f;
        if (ShaderRenderer.hasInvalidSize(f3, f4) || ShaderRenderer.isTooTransparent(colorRGBA) || !textureRegistry.hasTexture(n)) {
            return;
        }
        if (WidgetBatchRenderer.textureRenderingActive) {
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        int n2 = minecraftClient.getWindow().getScaledWidth();
        int n3 = minecraftClient.getWindow().getScaledHeight();
        if (!MatrixUtil.isIdentity((Matrix4f)matrix4f)) {
            vector3f = new Vector3f(f, f2, 0.0f);
            Vector3f vector3f2 = new Vector3f(f + f3, f2 + f4, 0.0f);
            matrix4f.transformPosition(vector3f);
            matrix4f.transformPosition(vector3f2);
            f15 = vector3f.x / (float)n2;
            f14 = ((float)n3 - vector3f.y - (vector3f2.y - vector3f.y)) / (float)n3;
            f13 = (vector3f2.x - vector3f.x) / (float)n2;
            f12 = (vector3f2.y - vector3f.y) / (float)n3;
        } else {
            f15 = f / (float)n2;
            f14 = ((float)n3 - f2 - f4) / (float)n3;
            f13 = f3 / (float)n2;
            f12 = f4 / (float)n3;
        }
        backdropBlurShader.bindShaderProgram();
        RenderSystem.setShaderTexture((int)0, (int)textureRegistry.textureId(n));
        backdropBlurShader.getUniform("Size").set(f3, f4);
        backdropBlurShader.getUniform("Radius").set(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius());
        backdropBlurShader.getUniform("Smoothness").set(f5);
        backdropBlurShader.getUniform("FadeStart").set(f6);
        backdropBlurShader.getUniform("FadeEnd").set(f7);
        backdropBlurShader.getUniform("ClampMin").set(f8 / (float)n2, ((float)n3 - (f9 + f11)) / (float)n3);
        backdropBlurShader.getUniform("ClampMax").set((f8 + f10) / (float)n2, ((float)n3 - f9) / (float)n3);
        ShaderRenderer.prepareShaderState();
        BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).texture(f15, f14 + f12).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).texture(f15, f14).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).texture(f15 + f13, f14).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).texture(f15 + f13, f14 + f12).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        ShaderRenderer.restoreRenderState();
        RenderSystem.setShaderTexture((int)0, (int)0);
        class_45872.pop();
    }

    public static void drawBackdropBlur(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, float f6, WidgetState widgetState, ColorRGBA colorRGBA) {
        float f7;
        float f8;
        float f9;
        float f10;
        Vector3f vector3f;
        Object object;
        if (ShaderRenderer.hasInvalidSize(f3, f4) || f5 <= 0.0f || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        if ((f5 /= 22.5f) <= 0.0f) {
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f11 = 0.03f;
        float f12 = -f11 / 2.0f + f11 * 2.0f;
        float f13 = f11 / 2.0f + f11;
        float f14 = f - f12 / 2.0f;
        float f15 = f2 - f13 / 2.0f;
        float f16 = f3 + f12;
        float f17 = f4 + f13;
        int n = minecraftClient.getWindow().getScaledWidth();
        int n2 = minecraftClient.getWindow().getScaledHeight();
        if (!MatrixUtil.isIdentity((Matrix4f)matrix4f)) {
            object = new Vector3f(f14, f15, 0.0f);
            vector3f = new Vector3f(f14 + f16, f15 + f17, 0.0f);
            matrix4f.transformPosition((Vector3f)object);
            matrix4f.transformPosition(vector3f);
            f10 = ((Vector3f)object).x / (float)n;
            f9 = ((float)n2 - ((Vector3f)object).y - (vector3f.y - ((Vector3f)object).y)) / (float)n2;
            f8 = (vector3f.x - ((Vector3f)object).x) / (float)n;
            f7 = (vector3f.y - ((Vector3f)object).y) / (float)n2;
        } else {
            f10 = f14 / (float)n;
            f9 = ((float)n2 - f15 - f17) / (float)n2;
            f8 = f16 / (float)n;
            f7 = f17 / (float)n2;
        }
        RenderTargetManager.setBlurRadius(2.0f);
        object = WidgetBatchRenderer.getCurrentBatch();
        if (object != null) {
            ((WidgetBatchRenderer)object).queueTexturedWidget(ShaderRenderer.getActiveTextureId(), matrix4f, f14, f15, f16, f17, f10, f9 + f7, f10 + f8, f9, f3, f4, widgetState.topLeftRadius() * f6 / 2.0f, widgetState.bottomLeftRadius() * f6 / 2.0f, widgetState.topRightRadius() * f6 / 2.0f, widgetState.bottomRightRadius() * f6 / 2.0f, 0.1f, f6, colorRGBA);
            class_45872.pop();
            return;
        }
        texturedSquircleShader.bindShaderProgram();
        RenderSystem.setShaderTexture((int)0, (int)ShaderRenderer.getActiveTextureId());
        texturedSquircleShader.getUniform("Size").set(f3, f4);
        texturedSquircleShader.getUniform("Radius").set(widgetState.topLeftRadius() * f6 / 2.0f, widgetState.bottomLeftRadius() * f6 / 2.0f, widgetState.topRightRadius() * f6 / 2.0f, widgetState.bottomRightRadius() * f6 / 2.0f);
        texturedSquircleShader.getUniform("Smoothness").set(0.1f);
        texturedSquircleShader.getUniform("CornerSmoothness").set(f6);
        ShaderRenderer.prepareShaderState();
        BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix4f, f14, f15, 0.0f).texture(f10, f9 + f7).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f14, f15 + f17, 0.0f).texture(f10, f9).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f14 + f16, f15 + f17, 0.0f).texture(f10 + f8, f9).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f14 + f16, f15, 0.0f).texture(f10 + f8, f9 + f7).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        ShaderRenderer.restoreRenderState();
        RenderSystem.setShaderTexture((int)0, (int)0);
        class_45872.pop();
    }

    public static void drawBlurredRectangle(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        float f6;
        float f7;
        float f8;
        float f9;
        Vector3f vector3f;
        Object object;
        if (ShaderRenderer.hasInvalidSize(f3, f4) || f5 <= 0.0f || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        if ((f5 /= 22.5f) <= 0.0f) {
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        int n = minecraftClient.getWindow().getScaledWidth();
        int n2 = minecraftClient.getWindow().getScaledHeight();
        if (!MatrixUtil.isIdentity((Matrix4f)matrix4f)) {
            object = new Vector3f(f, f2, 0.0f);
            vector3f = new Vector3f(f + f3, f2 + f4, 0.0f);
            matrix4f.transformPosition((Vector3f)object);
            matrix4f.transformPosition(vector3f);
            f9 = ((Vector3f)object).x / (float)n;
            f8 = ((float)n2 - ((Vector3f)object).y - (vector3f.y - ((Vector3f)object).y)) / (float)n2;
            f7 = (vector3f.x - ((Vector3f)object).x) / (float)n;
            f6 = (vector3f.y - ((Vector3f)object).y) / (float)n2;
        } else {
            f9 = f / (float)n;
            f8 = ((float)n2 - f2 - f4) / (float)n2;
            f7 = f3 / (float)n;
            f6 = f4 / (float)n2;
        }
        RenderTargetManager.setBlurRadius(2.0f);
        object = WidgetBatchRenderer.getCurrentBatch();
        if (object != null) {
            ((WidgetBatchRenderer)object).queueTexturedRectangleWithDefaults(ShaderRenderer.getActiveTextureId(), matrix4f, f, f2, f3, f4, f9, f8 + f6, f9 + f7, f8, f3, f4, widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius(), 0.01f, colorRGBA);
            class_45872.pop();
            return;
        }
        textureShader.bindShaderProgram();
        RenderSystem.setShaderTexture((int)0, (int)ShaderRenderer.getActiveTextureId());
        textureShader.getUniform("Size").set(f3, f4);
        textureShader.getUniform("Radius").set(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius());
        textureShader.getUniform("Smoothness").set(0.01f);
        ShaderRenderer.prepareShaderState();
        BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).texture(f9, f8 + f6).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).texture(f9, f8).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).texture(f9 + f7, f8).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).texture(f9 + f7, f8 + f6).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        ShaderRenderer.restoreRenderState();
        RenderSystem.setShaderTexture((int)0, (int)0);
        class_45872.pop();
    }

    public static void drawBlurredTextureRegion(MatrixStack class_45872, Rect rect, float f, float f2, float f3, float f4, WidgetState widgetState, float f5) {
        float f6;
        float f7;
        float f8;
        float f9;
        Vector3f vector3f;
        if (ShaderRenderer.hasInvalidSize(f3, f4) || f5 <= 0.0f) {
            return;
        }
        ColorRGBA colorRGBA = ColorRGBA.WHITE.mulAlpha(f5);
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        textureShader.bindShaderProgram();
        RenderSystem.setShaderTexture((int)0, (Identifier)RockstarClient.resourceId("textures/blank.png"));
        textureShader.getUniform("Size").set(f3, f4);
        textureShader.getUniform("Radius").set(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius());
        textureShader.getUniform("Smoothness").set(0.5f);
        ShaderRenderer.prepareShaderState();
        float f10 = rect.getWidth();
        float f11 = rect.getHeight();
        if (!MatrixUtil.isIdentity((Matrix4f)matrix4f)) {
            vector3f = new Vector3f(f, f2, 0.0f);
            Vector3f vector3f2 = new Vector3f(f + f3, f2 + f4, 0.0f);
            matrix4f.transformPosition(vector3f);
            matrix4f.transformPosition(vector3f2);
            float f12 = vector3f.x;
            float f13 = vector3f.y;
            float f14 = vector3f2.x - vector3f.x;
            float f15 = vector3f2.y - vector3f.y;
            f9 = (f12 - rect.getX()) / f10;
            f8 = (f13 - rect.getY()) / f11 + f15 / f11;
            f7 = f14 / f10;
            f6 = -f15 / f11;
        } else {
            f9 = (f - rect.getX()) / f10;
            f8 = (f2 - rect.getY()) / f11 + f4 / f11;
            f7 = f3 / f10;
            f6 = -f4 / f11;
        }
        BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).texture(f9, f8 + f6).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).texture(f9, f8).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).texture(f9 + f7, f8).color(colorRGBA.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).texture(f9 + f7, f8 + f6).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bufferBuilder.end());
        ShaderRenderer.restoreRenderState();
        RenderSystem.setShaderTexture((int)0, (int)0);
        class_45872.pop();
    }

    public static void appendTexturedQuadVertices(MatrixStack class_45872, BufferBuilder class_2872, double d, double d2, double d3, double d4, double d5, ColorRGBA colorRGBA) {
        if (d4 <= 0.0 || d5 <= 0.0 || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        class_2872.vertex(matrix4f, (float)d, (float)(d2 + d5), (float)d3).texture(0.0f, 1.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, (float)(d + d4), (float)(d2 + d5), (float)d3).texture(1.0f, 1.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, (float)(d + d4), (float)d2, (float)d3).texture(1.0f, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, (float)d, (float)d2, (float)d3).texture(0.0f, 0.0f).color(colorRGBA.getRGB());
    }

    public static void appendRotatedTexturedQuadVertices(MatrixStack class_45872, BufferBuilder class_2872, double d, double d2, double d3, double d4, double d5, ColorRGBA colorRGBA, int n) {
        if (d4 <= 0.0 || d5 <= 0.0 || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f = (float)d;
        float f2 = (float)(d + d4);
        float f3 = (float)d2;
        float f4 = (float)(d2 + d5);
        float f5 = (float)d3;
        float f6 = 0.0f;
        float f7 = 0.0f;
        float f8 = 1.0f;
        float f9 = 1.0f;
        n = (n % 360 + 360) % 360;
        switch (n) {
            case 0: {
                class_2872.vertex(matrix4f, f, f4, f5).texture(f6, f9).color(colorRGBA.getRGB());
                class_2872.vertex(matrix4f, f2, f4, f5).texture(f8, f9).color(colorRGBA.getRGB());
                class_2872.vertex(matrix4f, f2, f3, f5).texture(f8, f7).color(colorRGBA.getRGB());
                class_2872.vertex(matrix4f, f, f3, f5).texture(f6, f7).color(colorRGBA.getRGB());
                break;
            }
            case 180: {
                class_2872.vertex(matrix4f, f, f4, f5).texture(f8, f7).color(colorRGBA.getRGB());
                class_2872.vertex(matrix4f, f2, f4, f5).texture(f6, f7).color(colorRGBA.getRGB());
                class_2872.vertex(matrix4f, f2, f3, f5).texture(f6, f9).color(colorRGBA.getRGB());
                class_2872.vertex(matrix4f, f, f3, f5).texture(f8, f9).color(colorRGBA.getRGB());
            }
        }
    }

    public static void drawResourceTexture(MatrixStack class_45872, Identifier class_29602, double d, double d2, double d3, double d4, double d5, ColorRGBA colorRGBA) {
        if (class_29602 == null || d4 <= 0.0 || d5 <= 0.0 || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        class_2872.vertex(matrix4f, (float)d, (float)(d2 + d5), (float)d3).texture(0.0f, 1.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, (float)(d + d4), (float)(d2 + d5), (float)d3).texture(1.0f, 1.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, (float)(d + d4), (float)d2, (float)d3).texture(1.0f, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, (float)d, (float)d2, (float)d3).texture(0.0f, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
    }

    public static void drawPlayerPreview(MatrixStack class_45872, AbstractClientPlayerEntity TrackedPosition, float f, float f2, float f3, WidgetState widgetState, ColorRGBA colorRGBA) {
        if (TrackedPosition == null || f3 <= 0.0f || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        Identifier class_29602 = TrackedPosition.getSkinTextures().texture();
        ShaderRenderer.drawEntityTexture(class_45872, class_29602, f, f2, f3, widgetState, colorRGBA);
        ShaderRenderer.drawEntityTextureWithBlend(class_45872, class_29602, f, f2, f3, widgetState, colorRGBA);
    }

    public static <T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> void drawLivingEntityPreview(MatrixStack class_45872, T t, float f, float f2, float f3, WidgetState widgetState, ColorRGBA colorRGBA) {
        if (t == null || f3 <= 0.0f || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        EntityRenderer BreezeAnimations = minecraftClient.getEntityRenderDispatcher().getRenderer(t);
        if (BreezeAnimations instanceof LivingEntityRenderer) {
            LivingEntityRenderer Builder = (LivingEntityRenderer)BreezeAnimations;
            LivingEntityRenderer SynchronizeRegistriesTask = (LivingEntityRenderer)BreezeAnimations;
            LivingEntityRenderState class_100422 = (LivingEntityRenderState)SynchronizeRegistriesTask.createRenderState();
            Identifier class_29602 = SynchronizeRegistriesTask.getTexture(class_100422);
            ShaderRenderer.drawEntityTexture(class_45872, class_29602, f, f2, f3, widgetState, colorRGBA);
            ShaderRenderer.drawEntityTextureWithBlend(class_45872, class_29602, f, f2, f3, widgetState, colorRGBA);
        }
    }

    public static void drawEntityTexture(MatrixStack class_45872, Identifier class_29602, float f, float f2, float f3, WidgetState widgetState, ColorRGBA colorRGBA) {
        if (class_29602 == null || f3 <= 0.0f || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        ShaderRenderer.drawTexturedRegionWithUv(class_45872, class_29602, f, f2, f3, f3, widgetState, colorRGBA, 0.125f, 0.125f, 0.25f, 0.25f);
    }

    private static void drawEntityTextureWithBlend(MatrixStack class_45872, Identifier class_29602, float f, float f2, float f3, WidgetState widgetState, ColorRGBA colorRGBA) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ShaderRenderer.drawTexturedRegionWithUv(class_45872, class_29602, f, f2, f3, f3, widgetState, colorRGBA, 0.625f, 0.125f, 0.75f, 0.25f);
        RenderSystem.disableBlend();
    }

    public static void drawTexturedRegionWithUv(MatrixStack class_45872, Identifier class_29602, float f, float f2, float f3, float f4, WidgetState widgetState, ColorRGBA colorRGBA, float f5, float f6, float f7, float f8) {
        if (class_29602 == null || ShaderRenderer.hasInvalidSize(f3, f4) || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f9 = 0.5f;
        float f10 = -f9 / 2.0f + f9 * 2.0f;
        float f11 = f9 / 2.0f + f9;
        float f12 = f - f10 / 2.0f;
        float f13 = f2 - f11 / 2.0f;
        float f14 = f3 + f10;
        float f15 = f4 + f11;
        WidgetBatchRenderer widgetBatchRenderer = WidgetBatchRenderer.getCurrentBatch();
        if (widgetBatchRenderer != null) {
            widgetBatchRenderer.queueTexturedRectangleWithDefaults(minecraftClient.getTextureManager().getTexture(class_29602).getGlId(), matrix4f, f12, f13, f14, f15, f5, f6, f7, f8, f3, f4, widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius(), f9, colorRGBA);
            class_45872.pop();
            return;
        }
        textureShader.bindShaderProgram();
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        textureShader.getUniform("Size").set(f3, f4);
        textureShader.getUniform("Radius").set(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius());
        textureShader.getUniform("Smoothness").set(f9);
        ShaderRenderer.prepareShaderState();
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        class_2872.vertex(matrix4f, f12, f13, 0.0f).texture(f5, f6).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f12, f13 + f15, 0.0f).texture(f5, f8).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f12 + f14, f13 + f15, 0.0f).texture(f7, f8).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f12 + f14, f13, 0.0f).texture(f7, f6).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        RenderSystem.setShaderTexture((int)0, (int)0);
        class_45872.pop();
    }

    public static void drawRawQuad(MatrixStack class_45872, float f, float f2, float f3, float f4) {
        if (ShaderRenderer.hasInvalidSize(f3, f4)) {
            return;
        }
        int n = -1;
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        class_2872.vertex(matrix4f, f, f2, 0.0f).texture(0.0f, 1.0f).color(n);
        class_2872.vertex(matrix4f, f, f2 + f4, 0.0f).texture(0.0f, 0.0f).color(n);
        class_2872.vertex(matrix4f, f + f3, f2 + f4, 0.0f).texture(1.0f, 0.0f).color(n);
        class_2872.vertex(matrix4f, f + f3, f2, 0.0f).texture(1.0f, 1.0f).color(n);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
    }

    public static void renderQuadWithBounds(float f, float f2, float f3, float f4) {
        if (ShaderRenderer.hasInvalidSize(f3, f4)) {
            return;
        }
        int n = -1;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        class_2872.vertex(f, f2, 0.0f).texture(0.0f, 1.0f).color(n);
        class_2872.vertex(f, f2 + f4, 0.0f).texture(0.0f, 0.0f).color(n);
        class_2872.vertex(f + f3, f2 + f4, 0.0f).texture(1.0f, 0.0f).color(n);
        class_2872.vertex(f + f3, f2, 0.0f).texture(1.0f, 1.0f).color(n);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
    }

    public static void prepareShaderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void drawArc(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, float f6, ColorRGBA colorRGBA) {
        if (f3 <= 0.0f || f4 <= 0.0f || ShaderRenderer.isTooTransparent(colorRGBA)) {
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f7 = 0.5f;
        arcShader.bindShaderProgram();
        arcShader.getUniform("Size").set(f3, f3);
        arcShader.getUniform("Thickness").set(f4);
        arcShader.getUniform("StartAngle").set(f5);
        arcShader.getUniform("EndAngle").set(f6);
        arcShader.getUniform("Smoothness").set(f7);
        ShaderRenderer.prepareShaderState();
        float f8 = f4 + f7 * 2.0f;
        float f9 = f - f8 / 2.0f;
        float f10 = f2 - f8 / 2.0f;
        float f11 = f3 + f8;
        float f12 = f3 + f8;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f9, f10, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f9, f10 + f12, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f9 + f11, f10 + f12, 0.0f).color(colorRGBA.getRGB());
        class_2872.vertex(matrix4f, f9 + f11, f10, 0.0f).color(colorRGBA.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    public static void drawArc(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, float f6, ColorRGBA colorRGBA, boolean bl) {
        if (bl) {
            float f7 = f3 * 2.0f;
            float f8 = (float)Math.toRadians(f5 - 90.0f);
            float f9 = (float)Math.toRadians(f6 - 90.0f);
            ShaderRenderer.drawArc(class_45872, f - f3, f2 - f3, f7, f4, f8, f9, colorRGBA);
        } else {
            float f10 = (float)Math.toRadians(f5 - 90.0f);
            float f11 = (float)Math.toRadians(f6 - 90.0f);
            ShaderRenderer.drawArc(class_45872, f, f2, f3 * 2.0f, f4, f10, f11, colorRGBA);
        }
    }

    public static void drawCircleProgress(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, ColorRGBA colorRGBA) {
        float f6 = 0.0f;
        float f7 = f5 * 360.0f;
        ShaderRenderer.drawArc(class_45872, f, f2, f3, f4, f6, f7, colorRGBA, true);
    }

    public static void drawIslandBlob(MatrixStack class_45872, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, ColorRGBA colorRGBA, ColorRGBA colorRGBA2, float f15) {
        if (ShaderRenderer.hasInvalidSize(f3, f4)) {
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        class_45872.push();
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        islandBlobShader.bindShaderProgram();
        islandBlobShader.getUniform("Size").set(f3, f4);
        islandBlobShader.getUniform("RectCenter").set(f5, f6);
        islandBlobShader.getUniform("RectHalf").set(f7, f8);
        islandBlobShader.getUniform("RectRadius").set(f9, f9, f9, f9);
        islandBlobShader.getUniform("CircleCenter").set(f10, f11);
        islandBlobShader.getUniform("CircleRadius").set(f12);
        islandBlobShader.getUniform("Smooth").set(f13);
        islandBlobShader.getUniform("Outline").set(f14);
        islandBlobShader.getUniform("FillColor").set(colorRGBA.getRed() / 255.0f, colorRGBA.getGreen() / 255.0f, colorRGBA.getBlue() / 255.0f, colorRGBA.getAlpha() / 255.0f);
        islandBlobShader.getUniform("OutlineColor").set(colorRGBA2.getRed() / 255.0f, colorRGBA2.getGreen() / 255.0f, colorRGBA2.getBlue() / 255.0f, colorRGBA2.getAlpha() / 255.0f);
        islandBlobShader.getUniform("GlobalAlpha").set(f15);
        ShaderRenderer.prepareShaderState();
        int n = -1;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(matrix4f, f, f2, 0.0f).color(n);
        class_2872.vertex(matrix4f, f, f2 + f4, 0.0f).color(n);
        class_2872.vertex(matrix4f, f + f3, f2 + f4, 0.0f).color(n);
        class_2872.vertex(matrix4f, f + f3, f2, 0.0f).color(n);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShaderRenderer.restoreRenderState();
        class_45872.pop();
    }

    public static void restoreRenderState() {
        RenderSystem.disableBlend();
    }

    @Generated
    private ShaderRenderer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static ShaderProgramBase getRectangleShader() {
        return squircleShader;
    }

    static {
        defaultRenderTarget = new RenderTarget(false);
        requestedTextureId = -1;
    }

    static final class TextureCoordinates {
        private final float u;
        private final float v;
        private final float uSize;
        private final float vSize;

        TextureCoordinates(float f, float f2, float f3, float f4) {
            this.u = f;
            this.v = f2;
            this.uSize = f3;
            this.vSize = f4;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "u", "v", "uSize", "vSize");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "u", "v", "uSize", "vSize");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "u", "v", "uSize", "vSize");
        }

        public float getU() {
            return this.u;
        }

        public float getV() {
            return this.v;
        }

        public float getUSize() {
            return this.uSize;
        }

        public float getVSize() {
            return this.vSize;
        }
    }
}
