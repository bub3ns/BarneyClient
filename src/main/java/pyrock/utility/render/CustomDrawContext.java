/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.AbstractTexture
 *  net.minecraft.ItemRenderState
 *  net.minecraft.TextureManager
 *  net.minecraft.CrashReport
 *  net.minecraft.CrashReportSection
 *  net.minecraft.LivingEntity
 *  net.minecraft.CrashException
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.World
 *  net.minecraft.Vec2f
 *  net.minecraft.Text
 *  net.minecraft.Identifier
 *  net.minecraft.DiffuseLighting
 *  net.minecraft.DrawContext
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.VertexConsumerProvider$Immediate
 *  net.minecraft.OverlayTexture
 *  net.minecraft.InventoryScreen
 *  net.minecraft.AbstractClientPlayerEntity
 *  net.minecraft.ModelTransformationMode
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package pyrock.utility.render;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.mixin.accessors.DrawContextAccessor;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.visuals.hud.Interface;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.colors.GradientColors;
import moscow.rockstar.render.text.SlugTextRenderer;
import moscow.rockstar.render.text.icon.SvgIconRegistry;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.theme.IconStyle;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.crash.CrashException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec2f;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.ModelTransformationMode;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.Rect;

public class CustomDrawContext
extends DrawContext
implements ClientAccess {
    private final DrawContext originalContext;
    private int itemBatchDepth;
    private boolean itemBatchHasVertices;
    private boolean itemBatchFlatLighting;
    private float[] itemBatchShaderColor;

    public CustomDrawContext(DrawContext ServerConfigException) {
        super(MinecraftClient.getInstance(), ((DrawContextAccessor)ServerConfigException).getVertexConsumers());
        this.originalContext = ServerConfigException;
    }

    public static CustomDrawContext of(DrawContext ServerConfigException) {
        return new CustomDrawContext(ServerConfigException);
    }

    public ItemBatch beginItemBatch() {
        if (this.itemBatchDepth++ == 0) {
            WidgetBatchRenderer.flushCurrentBatch();
            this.itemBatchHasVertices = false;
            this.itemBatchShaderColor = null;
        }
        return new ItemBatch();
    }

    void endItemBatch() {
        if (this.itemBatchDepth <= 0) {
            return;
        }
        if (--this.itemBatchDepth == 0) {
            this.flushItemBatch();
            DiffuseLighting.disableGuiDepthLighting();
            this.itemBatchShaderColor = null;
        }
    }

    private void flushItemBatch() {
        if (!this.itemBatchHasVertices) {
            return;
        }
        float[] fArray = (float[])RenderSystem.getShaderColor().clone();
        if (this.itemBatchShaderColor != null) {
            RenderSystem.setShaderColor((float)this.itemBatchShaderColor[0], (float)this.itemBatchShaderColor[1], (float)this.itemBatchShaderColor[2], (float)this.itemBatchShaderColor[3]);
        }
        if (this.itemBatchFlatLighting) {
            DiffuseLighting.disableGuiDepthLighting();
        } else {
            DiffuseLighting.enableGuiDepthLighting();
        }
        ((DrawContextAccessor)this.originalContext).getVertexConsumers().draw();
        RenderSystem.setShaderColor((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
        this.itemBatchHasVertices = false;
        this.itemBatchShaderColor = null;
    }

    private boolean sameShaderColor(float[] fArray) {
        if (this.itemBatchShaderColor == null || fArray == null) {
            return false;
        }
        return Float.compare(this.itemBatchShaderColor[0], fArray[0]) == 0 && Float.compare(this.itemBatchShaderColor[1], fArray[1]) == 0 && Float.compare(this.itemBatchShaderColor[2], fArray[2]) == 0 && Float.compare(this.itemBatchShaderColor[3], fArray[3]) == 0;
    }

    public void drawEntity(int n, int n2, int n3, int n4, int n5, float f, float f2, float f3, LivingEntity class_13092) {
        float f4 = (float)(n + n3) / 2.0f;
        float f5 = (float)(n2 + n4) / 2.0f;
        this.enableScissor(n, n2, n3, n4);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float)Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(0.0f);
        quaternionf.mul((Quaternionfc)quaternionf2);
        float f6 = class_13092.bodyYaw;
        float f7 = class_13092.getYaw();
        float f8 = class_13092.getPitch();
        float f9 = class_13092.prevHeadYaw;
        float f10 = class_13092.headYaw;
        class_13092.bodyYaw = 180.0f;
        class_13092.setYaw(180.0f);
        class_13092.setPitch(0.0f);
        class_13092.headYaw = class_13092.getYaw();
        class_13092.prevHeadYaw = class_13092.getYaw();
        float f11 = class_13092.getScale();
        Vector3f vector3f = new Vector3f(0.0f, class_13092.getHeight() / 2.0f + f * f11, 0.0f);
        float f12 = (float)n5 / f11;
        InventoryScreen.drawEntity((DrawContext)this.originalContext, (float)f4, (float)f5, (float)f12, (Vector3f)vector3f, (Quaternionf)quaternionf, (Quaternionf)quaternionf2, (LivingEntity)class_13092);
        class_13092.bodyYaw = f6;
        class_13092.setYaw(f7);
        class_13092.setPitch(f8);
        class_13092.prevHeadYaw = f9;
        class_13092.headYaw = f10;
        this.disableScissor();
    }

    public void drawClientRect(float f, float f2, float f3, float f4, float f5, float f6, float f7) {
        this.drawClientRect(f, f2, f3, f4, f5, f6, f7, ColorPalette.getThemeColorSettings().getCornerRadius(), false);
    }

    public void drawClientRect(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        this.drawClientRect(f, f2, f3, f4, f5, f6, f7, f8, false);
    }

    public void drawClientRect(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, boolean bl) {
        this.drawClientRect(f, f2, f3, f4, f5, f6, f7, f8, bl, false);
    }

    public void drawClientRect(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, boolean bl, boolean bl2) {
        WidgetState widgetState = WidgetState.uniform(f8);
        if (Interface.isBlurEnabled()) {
            this.drawBlurredRect(f, f2, f3, f4, 45.0f, f7, widgetState, ColorRGBA.WHITE.withAlpha(255.0f * f5 * Interface.getBlurAlpha()));
        }
        if (Interface.isLiquidGlassEnabled() && !bl2) {
            this.drawLiquidGlass(f, f2, f3, f4, f7, 0.08f + 0.07f * f6, widgetState, ColorRGBA.WHITE.withAlpha(255.0f * f5 * Interface.getLiquidGlassAlpha()));
        }
        this.drawSquircle(f, f2, f3, f4, f7, widgetState, ColorPalette.getPanelColor().mulAlpha(MathUtils.interpolateDouble(1.0, 0.4f, bl2 ? 0.0 : (double)Interface.getLiquidGlassAlpha())));
        if (bl) {
            this.drawSquircleBorder(f, f2, f3, f4, 0.5f, f7, widgetState, ColorPalette.BORDER_COLOR);
        }
    }

    public void pushMatrix() {
        this.getMatrices().push();
    }

    public void popMatrix() {
        this.getMatrices().pop();
    }

    public void drawRect(float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
        ShaderRenderer.drawRectangle(this.getMatrices(), f, f2, f3, f4, colorRGBA);
    }

    public void drawLine(Vec2f VanillaAdventureTabAdvancementGenerator, Vec2f MagmaBlock, ColorRGBA colorRGBA) {
        ShaderRenderer.drawLineSegment(this.getMatrices(), VanillaAdventureTabAdvancementGenerator, MagmaBlock, colorRGBA);
    }

    public void drawBezier(Vec2f VanillaAdventureTabAdvancementGenerator, Vec2f MagmaBlock, Vec2f VanillaHusbandryTabAdvancementGenerator, Vec2f BlockMirror, ColorRGBA colorRGBA, int n) {
        ShaderRenderer.drawBezierCurve(this.getMatrices(), VanillaAdventureTabAdvancementGenerator, MagmaBlock, VanillaHusbandryTabAdvancementGenerator, BlockMirror, colorRGBA, n);
    }

    public void drawSmoothBezier(float f, float f2, float f3, float f4, Vec2f VanillaAdventureTabAdvancementGenerator, Vec2f MagmaBlock, Vec2f VanillaHusbandryTabAdvancementGenerator, Vec2f BlockMirror, float f5, ColorRGBA colorRGBA) {
        ShaderRenderer.drawBezierStroke(this.getMatrices(), f, f2, f3, f4, VanillaAdventureTabAdvancementGenerator, MagmaBlock, VanillaHusbandryTabAdvancementGenerator, BlockMirror, f5, colorRGBA);
    }

    public void drawAreaGradient(float[] fArray, float[] fArray2, float f, ColorRGBA colorRGBA, ColorRGBA colorRGBA2) {
        ShaderRenderer.drawGradientPolyline(this.getMatrices(), fArray, fArray2, f, colorRGBA, colorRGBA2);
    }

    public void drawSquircle(float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawRoundedRectangle(this.getMatrices(), f, f2, f3, f4, f5, widgetState, colorRGBA);
    }

    public void drawSquircle(float f, float f2, float f3, float f4, float f5, WidgetState widgetState, GradientColors gradientColors) {
        ShaderRenderer.drawNotificationBackground(this.getMatrices(), f, f2, f3, f4, f5, widgetState, gradientColors);
    }

    public void drawSquircle(float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA, ColorRGBA colorRGBA2, ColorRGBA colorRGBA3, ColorRGBA colorRGBA4) {
        ShaderRenderer.drawGradientRoundedRectangle(this.getMatrices(), f, f2, f3, f4, f5, widgetState, colorRGBA, colorRGBA2, colorRGBA3, colorRGBA4);
    }

    public void drawRoundedRect(float f, float f2, float f3, float f4, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawRoundedRectangle(this.getMatrices(), f, f2, f3, f4, widgetState, colorRGBA);
    }

    public void drawRoundedRect(float f, float f2, float f3, float f4, WidgetState widgetState, GradientColors gradientColors) {
        ShaderRenderer.drawNotificationBackground(this.getMatrices(), f, f2, f3, f4, widgetState, gradientColors);
    }

    public void drawLiquidGlass(float f, float f2, float f3, float f4, float f5, float f6, WidgetState widgetState, ColorRGBA colorRGBA) {
        widgetState = new WidgetState(widgetState.topLeftRadius() * f5 / 2.0f, widgetState.topRightRadius() * f5 / 2.0f, widgetState.bottomLeftRadius() * f5 / 2.0f, widgetState.bottomRightRadius() * f5 / 2.0f);
        ShaderRenderer.drawLiquidGlass(this.getMatrices(), f - 5.0f * Interface.getBlurAlpha(), f2 - 5.0f * Interface.getBlurAlpha(), f3 + 10.0f * Interface.getBlurAlpha(), f4 + 10.0f * Interface.getBlurAlpha(), widgetState, colorRGBA, colorRGBA.getAlpha() / 255.0f * Interface.getLiquidGlassAlpha(), (ColorPalette.getThemeColorSettings().getBlurRadius() + (float)(f4 == 240.0f ? 2 : 1)) * Interface.getLiquidGlassAlpha(), colorRGBA.withAlpha(255.0f), 1.0f, true, 0.0f, (f6 == 0.08f ? ColorPalette.getThemeColorSettings().getGlassDistortion() : f6) * Interface.getLiquidGlassAlpha(), f5, false);
    }

    public void drawLiquidGlass(float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA, boolean bl) {
        widgetState = new WidgetState(widgetState.topLeftRadius() * f5 / 2.0f, widgetState.topRightRadius() * f5 / 2.0f, widgetState.bottomLeftRadius() * f5 / 2.0f, widgetState.bottomRightRadius() * f5 / 2.0f);
        ShaderRenderer.drawLiquidGlass(this.getMatrices(), f, f2, f3, f4, widgetState, colorRGBA, colorRGBA.getAlpha() / 255.0f, f4 == 240.0f ? 100.0f : 50.0f, colorRGBA.withAlpha(255.0f), 1.0f, true, 0.0f, 0.08f, f5, bl);
    }

    public void drawRocknetGlass(Rect rect, float f, float f2, float f3, float f4, float f5, float f6, WidgetState widgetState, boolean bl) {
        widgetState = new WidgetState(widgetState.topLeftRadius() * f5 / 2.0f, widgetState.topRightRadius() * f5 / 2.0f, widgetState.bottomLeftRadius() * f5 / 2.0f, widgetState.bottomRightRadius() * f5 / 2.0f);
        ColorRGBA colorRGBA = ColorRGBA.WHITE;
        net.minecraft.client.texture.AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(RockstarClient.resourceId(bl ? "rocknet/background.png" : "rocknet/blur.png"));
        ShaderRenderer.drawLiquidGlassRegion(rect, this.getMatrices(), f, f2, f3, f4, widgetState, colorRGBA, colorRGBA.getAlpha() / 255.0f, f4 == 240.0f ? 100 : 50, colorRGBA.withAlpha(255.0f), 1.0f, true, 0.0f, f6, f5, texture.getGlId());
    }

    public void drawLoadingRect(float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawLoadingBar(this.getMatrices(), f, f2, f3, f4, f5, widgetState, colorRGBA);
    }

    public void drawRoundedBorder(float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawBorderedRoundedRectangle(this.getMatrices(), f, f2, f3, f4, f5, widgetState, colorRGBA);
    }

    public void drawDashedBorder(float f, float f2, float f3, float f4, float f5, WidgetState widgetState, float f6, float f7, ColorRGBA colorRGBA, float f8, float f9, float f10, float f11) {
        ShaderRenderer.drawDashedRoundedBorder(this.getMatrices(), f, f2, f3, f4, f5, widgetState, f6, f7, colorRGBA, f8, f9, f10, f11);
    }

    public void drawSquircleBorder(float f, float f2, float f3, float f4, float f5, float f6, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawSquircleBorder(this.getMatrices(), f, f2, f3, f4, f5, f6, widgetState, colorRGBA);
    }

    public void drawTexture(Identifier class_29602, Rect rect) {
        this.drawTexture(class_29602, rect, ColorRGBA.WHITE);
    }

    public void drawTexture(Identifier class_29602, Rect rect, ColorRGBA colorRGBA) {
        ShaderRenderer.drawTexturedRectangle(this.getMatrices(), class_29602, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), colorRGBA);
    }

    public void drawTexture(Identifier class_29602, float f, float f2, float f3, float f4) {
        ShaderRenderer.drawTexturedRectangle(this.getMatrices(), class_29602, f, f2, f3, f4, ColorRGBA.WHITE);
    }

    public void drawTexture(Identifier class_29602, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, ColorRGBA colorRGBA) {
        ShaderRenderer.drawTexturedRectangleWithUv(this.getMatrices(), class_29602, f, f2, f3, f4, f5, f6, f7, f8, colorRGBA);
    }

    public void drawTexture(Identifier class_29602, float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
        ShaderRenderer.drawTexturedRectangle(this.getMatrices(), class_29602, f, f2, f3, f4, colorRGBA);
    }

    public void drawSprite(IconStyle iconStyle, float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
        ShaderRenderer.drawIcon(this.getMatrices(), iconStyle, f, f2, f3, f4, colorRGBA);
    }

    public void drawRoundedTexture(Identifier class_29602, float f, float f2, float f3, float f4, WidgetState widgetState) {
        ShaderRenderer.drawTexturedRoundedRectangle(this.getMatrices(), class_29602, f, f2, f3, f4, widgetState);
    }

    public void drawRoundedTexture(Identifier class_29602, float f, float f2, float f3, float f4, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawTexturedRoundedRectangle(this.getMatrices(), class_29602, f, f2, f3, f4, widgetState, colorRGBA);
    }

    public void drawShadow(float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawSmoothRoundedRectangle(this.getMatrices(), f, f2, f3, f4, f5, widgetState, colorRGBA);
    }

    public void drawBlurredRect(float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawBlurredRectangle(this.getMatrices(), f, f2, f3, f4, f5, widgetState, colorRGBA);
    }

    public void drawBlurredRect(float f, float f2, float f3, float f4, float f5, float f6, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawBackdropBlur(this.getMatrices(), f, f2, f3, f4, f5, f6, widgetState, colorRGBA);
    }

    public void drawBackdropBlur(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawBackdropBlur(this.getMatrices(), f, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, widgetState, colorRGBA);
    }

    public void drawGlobalsBlur(Rect rect, float f, float f2, float f3, float f4, WidgetState widgetState, float f5) {
        ShaderRenderer.drawBlurredTextureRegion(this.getMatrices(), rect, f, f2, f3, f4, widgetState, f5);
    }

    public void drawGlobalsBlur(Rect rect, float f, float f2, float f3, float f4, WidgetState widgetState) {
        ShaderRenderer.drawBlurredTextureRegion(this.getMatrices(), rect, f, f2, f3, f4, widgetState, 1.0f);
    }

    public void drawText(FontMetrics fontMetrics, String string, float f, float f2, ColorRGBA colorRGBA) {
        SlugTextRenderer.draw(fontMetrics.getFontRenderer(), string, fontMetrics.getFontScale(), colorRGBA.getRGB(), this.getMatrices().peek().getPositionMatrix(), f, f2, 0.0f);
    }

    public void drawText(FontMetrics fontMetrics, Text class_25612, float f, float f2) {
        float f3 = f;
        for (moscow.rockstar.render.text.TextComponentRun textComponentRun : moscow.rockstar.render.text.TextComponentRuns.flatten(class_25612, ColorPalette.WHITE.getRGB())) {
            SlugTextRenderer.draw(fontMetrics.getFontRenderer(), textComponentRun.getText(), fontMetrics.getFontScale(), textComponentRun.getColor(), this.getMatrices().peek().getPositionMatrix(), f3, f2, 0.0f);
            f3 += fontMetrics.measureText(textComponentRun.getText());
        }
    }

    public void drawFadeoutText(FontMetrics fontMetrics, String string, float f, float f2, ColorRGBA colorRGBA, float f3, float f4) {
        float f5 = fontMetrics.getFontRenderer().measure(string, fontMetrics.getFontScale()) * 2.0f;
        SlugTextRenderer.drawFadeout(fontMetrics.getFontRenderer(), string, fontMetrics.getFontScale(), colorRGBA.getRGB(), this.getMatrices().peek().getPositionMatrix(), f, f2, 0.0f, f3, f4, f5);
    }

    public void drawFadeoutText(FontMetrics fontMetrics, String string, float f, float f2, ColorRGBA colorRGBA, float f3, float f4, float f5) {
        SlugTextRenderer.drawFadeout(fontMetrics.getFontRenderer(), string, fontMetrics.getFontScale(), colorRGBA.getRGB(), this.getMatrices().peek().getPositionMatrix(), f, f2, 0.0f, f3, f4, f5);
    }

    public void drawFadeText(FontMetrics fontMetrics, String string, float f, float f2, ColorRGBA colorRGBA, float f3, float f4, float f5) {
        float f6 = Math.max(1.0f, f5);
        float f7 = Math.max(0.0f, f3) / f6;
        float f8 = (f6 - Math.max(0.0f, f4)) / f6;
        SlugTextRenderer.drawFadeout(fontMetrics.getFontRenderer(), string, fontMetrics.getFontScale(), colorRGBA.getRGB(), this.getMatrices().peek().getPositionMatrix(), f, f2, 0.0f, f8, 1.0f, f6, 0.0f, f7);
    }

    public void drawCenteredText(FontMetrics fontMetrics, String string, float f, float f2, ColorRGBA colorRGBA) {
        this.drawText(fontMetrics, string, f - fontMetrics.getFontRenderer().measure(string, fontMetrics.getFontScale()) / 2.0f, f2, colorRGBA);
    }

    public void drawTextWithShadow(FontMetrics fontMetrics, String string, float f, float f2, ColorRGBA colorRGBA, ColorRGBA colorRGBA2, float f3, float f4, float f5) {
        if (colorRGBA2.getAlpha() > 1.0f) {
            float f6 = Math.max(1.0f, Math.min(10.0f, f5));
            float f7 = 0.0f;
            SlugTextRenderer.drawWithStyle(fontMetrics.getFontRenderer(), string, fontMetrics.getFontScale(), colorRGBA2.getRGB(), this.getMatrices().peek().getPositionMatrix(), f + f3, f2 + f4, 0.0f, f7, f6);
        }
        this.drawText(fontMetrics, string, f, f2, colorRGBA);
    }

    public void drawCenteredTextWithShadow(FontMetrics fontMetrics, String string, float f, float f2, ColorRGBA colorRGBA, ColorRGBA colorRGBA2, float f3, float f4, float f5) {
        this.drawTextWithShadow(fontMetrics, string, f - fontMetrics.getFontRenderer().measure(string, fontMetrics.getFontScale()) / 2.0f, f2, colorRGBA, colorRGBA2, f3, f4, f5);
    }

    public void drawRightText(FontMetrics fontMetrics, String string, float f, float f2, ColorRGBA colorRGBA) {
        this.drawText(fontMetrics, string, f - fontMetrics.getFontRenderer().measure(string, fontMetrics.getFontScale()), f2, colorRGBA);
    }

    public void drawIcon(String string, float f, float f2, float f3) {
        this.drawIcon(string, f, f2, f3, ColorRGBA.WHITE);
    }

    public void drawIcon(String string, float f, float f2, float f3, ColorRGBA colorRGBA) {
        Integer codePoint = SvgIconRegistry.getCodePoint(string);
        if (codePoint != null) {
            SlugTextRenderer.drawIcon(SvgIconRegistry.getFont(), codePoint, f, f2, f3,
                colorRGBA.getRGB(), this.getMatrices().peek().getPositionMatrix());
            return;
        }
        String texturePath = string.endsWith(".png") ? string : "icons/" + string + ".png";
        this.drawTexture(RockstarClient.resourceId(texturePath), f, f2, f3, f3, colorRGBA);
    }

    public void drawItem(Item class_17922, float f, float f2, float f3) {
        this.drawItem(class_17922.getDefaultStack(), f, f2, f3);
    }

    public void drawItem(ItemStack class_17992, float f, float f2, float f3) {
        WidgetBatchRenderer.flushCurrentBatch();
        this.getMatrices().push();
        this.getMatrices().translate(f, f2, 0.0f);
        this.getMatrices().scale(f3, f3, f3);
        DiffuseLighting.enableGuiDepthLighting();
        this.drawItem(class_17992, 0, 0);
        DiffuseLighting.disableGuiDepthLighting();
        this.getMatrices().pop();
    }

    public void drawHead(AbstractClientPlayerEntity TrackedPosition, float f, float f2, float f3, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawPlayerPreview(this.getMatrices(), TrackedPosition, f, f2, f3, widgetState, colorRGBA);
    }

    public void drawHead(LivingEntity class_13092, float f, float f2, float f3, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawLivingEntityPreview(this.getMatrices(), class_13092, f, f2, f3, widgetState, colorRGBA);
    }

    public void drawArc(float f, float f2, float f3, float f4, float f5, float f6, ColorRGBA colorRGBA) {
        ShaderRenderer.drawArc(this.getMatrices(), f, f2, f3, f4, f5, f6, colorRGBA);
    }

    public void drawArc(float f, float f2, float f3, float f4, float f5, float f6, ColorRGBA colorRGBA, boolean bl) {
        ShaderRenderer.drawArc(this.getMatrices(), f, f2, f3, f4, f5, f6, colorRGBA, bl);
    }

    public void drawCircleProgress(float f, float f2, float f3, float f4, float f5, ColorRGBA colorRGBA) {
        ShaderRenderer.drawCircleProgress(this.getMatrices(), f, f2, f3, f4, f5, colorRGBA);
    }

    public void drawBatchItem(ItemStack class_17992, float f, float f2, float f3) {
        this.drawBatchItem(class_17992, f, f2, f3, 0);
    }

    public void drawBatchItem(ItemStack class_17992, float f, float f2, float f3, int n) {
        this.prepareItemDraw();
        this.getMatrices().push();
        this.getMatrices().translate(f, f2, 0.0f);
        this.getMatrices().scale(f3, f3, f3);
        this.drawBatchItemInternal((LivingEntity)CustomDrawContext.minecraftClient.player, (World)CustomDrawContext.minecraftClient.world, class_17992, 0.0f, 0.0f, 0, n);
        this.getMatrices().pop();
    }

    public void drawBatchItem(ItemStack class_17992, float f, float f2) {
        this.drawBatchItem(class_17992, f, f2, 0);
    }

    public void drawBatchItem(ItemStack class_17992, float f, float f2, int n) {
        this.prepareItemDraw();
        this.drawBatchItemInternal((LivingEntity)CustomDrawContext.minecraftClient.player, (World)CustomDrawContext.minecraftClient.world, class_17992, f, f2, 0, n);
    }

    private void prepareItemDraw() {
        if (this.itemBatchDepth > 0 && WidgetBatchRenderer.hasPendingBatches()) {
            this.flushItemBatch();
        }
        WidgetBatchRenderer.flushCurrentBatch();
    }

    private void drawBatchItemInternal(@Nullable LivingEntity class_13092, @Nullable World class_19372, ItemStack class_17992, float f, float f2, int n) {
        this.drawBatchItemInternal(class_13092, class_19372, class_17992, f, f2, n, 0);
    }

    private void drawBatchItemInternal(@Nullable LivingEntity class_13092, @Nullable World class_19372, ItemStack class_17992, float f, float f2, int n, int n2) {
        MatrixStack class_45872 = this.getMatrices();
        ItemRenderState class_104442 = ((DrawContextAccessor)this.originalContext).getItemRenderState();
        VertexConsumerProvider.Immediate class_45982 = ((DrawContextAccessor)this.originalContext).getVertexConsumers();
        if (!class_17992.isEmpty()) {
            minecraftClient.getItemModelManager().update(class_104442, class_17992, ModelTransformationMode.GUI, false, class_19372, class_13092, n);
            class_45872.push();
            class_45872.translate(f + 8.0f, f2 + 8.0f, (float)(150 + (class_104442.hasDepth() ? n2 : 0)));
            try {
                boolean bl;
                class_45872.scale(16.0f, -16.0f, 16.0f);
                boolean bl2 = bl = !class_104442.isSideLit();
                if (this.itemBatchDepth > 0) {
                    float[] fArray = (float[])RenderSystem.getShaderColor().clone();
                    if (this.itemBatchHasVertices && (this.itemBatchFlatLighting != bl || !this.sameShaderColor(fArray))) {
                        this.flushItemBatch();
                    }
                    if (!this.itemBatchHasVertices) {
                        if (bl) {
                            class_45982.draw();
                        }
                        this.itemBatchFlatLighting = bl;
                        this.itemBatchShaderColor = fArray;
                    }
                    if (bl) {
                        DiffuseLighting.disableGuiDepthLighting();
                    } else {
                        DiffuseLighting.enableGuiDepthLighting();
                    }
                    class_104442.render(class_45872, class_45982, 0xF000F0, OverlayTexture.DEFAULT_UV);
                    this.itemBatchHasVertices = true;
                } else {
                    if (bl) {
                        class_45982.draw();
                        DiffuseLighting.disableGuiDepthLighting();
                    } else {
                        DiffuseLighting.enableGuiDepthLighting();
                    }
                    class_104442.render(class_45872, class_45982, 0xF000F0, OverlayTexture.DEFAULT_UV);
                    class_45982.draw();
                    if (bl) {
                        DiffuseLighting.enableGuiDepthLighting();
                    }
                }
            }
            catch (Throwable throwable) {
                CrashReport DamageTracker = CrashReport.create((Throwable)throwable, (String)"Rendering item");
                CrashReportSection StatusEffectUtil = DamageTracker.addElement("Item being rendered");
                StatusEffectUtil.add("Item Type", () -> String.valueOf(class_17992.getItem()));
                StatusEffectUtil.add("Item Components", () -> String.valueOf(class_17992.getComponents()));
                StatusEffectUtil.add("Item Foil", () -> String.valueOf(class_17992.hasGlint()));
                throw new CrashException(DamageTracker);
            }
            class_45872.pop();
            DiffuseLighting.disableGuiDepthLighting();
        }
    }

    public final class ItemBatch
    implements AutoCloseable {
        private boolean closed;

        public void restoreEnabledFlag() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            CustomDrawContext.this.endItemBatch();
        }

        @Override
        public void close() {
            this.restoreEnabledFlag();
        }
    }
}
