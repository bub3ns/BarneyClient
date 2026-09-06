/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  moscow.rockstar.ui.hand.SwingEditorWidget$ModelBoundsCapture
 *  net.minecraft.LivingEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ItemConvertible
 *  net.minecraft.Vec2f
 *  net.minecraft.MathHelper
 *  net.minecraft.Screen
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.VertexConsumerProvider$Immediate
 *  net.minecraft.HeldItemRenderer
 *  net.minecraft.ModelTransformationMode
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.opengl.GL11
 */
package moscow.rockstar.ui.hand;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.hand.HandSwingPresetManager;
import moscow.rockstar.render.hand.HandSwingSettings;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.modules.visuals.hand.HandSwingState;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.Font;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ModelTransformationMode;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

public class SwingEditorWidget
extends SettingWidget {
    private static final int FULL_BRIGHT_LIGHT = 0xF000F0;
    private static final float HEADER_HEIGHT = 32.0f;
    private static final float FOOTER_HEIGHT = 28.0f;
    private static final float CORNER_RADIUS = 11.0f;
    private static final float CONTROL_SIZE = 16.0f;
    private static final int COLOR_CHANNEL_COUNT = 7;
    private static final float COLOR_WHEEL_RADIUS = 0.45f;
    private static final float COLOR_WHEEL_SCALE = 0.5f;
    private static final int COLOR_WHEEL_SEGMENTS = 44;
    private static final float BORDER_WIDTH = 6.0f;
    private static final float HANDLE_RADIUS = 1.5f;
    private static final float BEZIER_CONTROL_FACTOR = 0.5522848f;
    private static final float PREVIEW_OFFSET_X = 0.56f;
    private static final float PREVIEW_OFFSET_Y = -0.52f;
    private static final float PREVIEW_OFFSET_Z = -0.72f;
    private static final float[] EMPTY_COLOR_COMPONENTS = new float[]{0.0f, 0.0f, 0.0f};
    private PreviewMode previewMode = PreviewMode.START;
    private EditSpace editSpace = EditSpace.POSITION;
    private float rotationYaw = 22.0f;
    private float rotationPitch = -12.0f;
    private float previewScale = 120.0f;
    private float interactionOffsetX;
    private float interactionOffsetY;
    private boolean resizingPanel = false;
    private final ItemStack previewItemStack = new ItemStack((ItemConvertible)Items.NETHERITE_SWORD);
    private float[] previewColorComponents;
    private int previewCaptureAttempts;
    private int selectedChannelIndex = -1;
    private final Animation panelOpenAnimation = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
    private final Animation[] modeHoverAnimations = SwingEditorWidget.createChannelAnimations(180L);
    private final Animation[] modeSelectionAnimations = SwingEditorWidget.createChannelAnimations(220L);
    private final Animation[] spaceHoverAnimations = SwingEditorWidget.createChannelAnimations(180L);
    private final Animation[] spaceSelectionAnimations = SwingEditorWidget.createChannelAnimations(220L);
    private float previewOffsetX;
    private float previewOffsetY;
    private float interactionOffset;
    private boolean previewInitialized = false;
    private final Animation previewTransitionAnimation = new Animation(260L, Easing.easeInOutCubicBezier);
    private float[] previousPreviewComponents;
    private final float[] transitionStartPosition = new float[3];
    private final float[] transitionTargetPosition = new float[3];
    private final float[] transitionPanelSize = new float[2];
    private boolean previewTransitionActive = false;
    private float[] lastColorSettings;
    private float contentOriginX;
    private float contentOriginY;
    private float contentWidth;
    private float contentViewportHeight;
    private float modeButtonY;
    private float editorControlY;
    private float dragStartChannelValue;
    private float rotationStartAngle;
    private boolean rotatingPreview = false;
    private boolean draggingColorChannel = false;
    private double dragStartX;
    private double dragStartY;
    private Vec2f rotationCenter = Vec2f.ZERO;
    private final float[] rotatedColorComponents = new float[3];
    private final float[] baseColorComponents = new float[3];
    private final float[] rotationCompensation = new float[3];
    private final Deque<float[]> undoHistory = new ArrayDeque<float[]>();
    private final Deque<float[]> redoHistory = new ArrayDeque<float[]>();
    private float[] lastCommittedSettings;
    private final Matrix4f previewTransform = new Matrix4f();
    private Vec2f colorSelectionCenter = Vec2f.ZERO;
    private final Vec2f[] channelSelectionPoints = new Vec2f[]{Vec2f.ZERO, Vec2f.ZERO, Vec2f.ZERO};
    private final Vec2f[][] colorWheelPoints = new Vec2f[3][44];
    private final float[] modeButtonX = new float[3];
    private final float[] modeButtonWidth = new float[3];
    private final float[] spaceButtonX = new float[3];
    private final float[] spaceButtonWidth = new float[3];
    private float resetButtonX;
    private final Animation resetButtonAnimation = new Animation(180L, Easing.easeInOutCubicBezier);
    private static final ColorRGBA[] channelColors = new ColorRGBA[]{ColorRGBA.RED, ColorRGBA.GREEN, ColorRGBA.BLUE};
    private static final float[] resetColorComponents = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 0.5f, 0.0f, 1.0f, 2.0f};

    private static Animation[] createChannelAnimations(long l) {
        Animation[] animationArray = new Animation[3];
        for (int i = 0; i < 3; ++i) {
            animationArray[i] = new Animation(l, Easing.easeInOutCubicBezier);
        }
        return animationArray;
    }

    private HandSwingSettings getSwingSettings() {
        HandSwingPresetManager manager = RockstarClient.create().getHandSwingPresetManager();
        return this.previewMode == PreviewMode.END ? manager.getFinalSwing() : manager.getInitialSwing();
    }

    @Override
    @Compile(obfuscation=1)
    protected void renderContent(RockstarDrawContext drawContext) {
        float f;
        if (!this.previewInitialized && ClientAccess.minecraftClient.player != null) {
            this.initializePreviewState();
        }
        this.recordEditorState();
        this.refreshPreviewTransition();
        if (this.previewTransitionActive) {
            this.previewTransitionAnimation.setReverse(true);
            f = this.previewTransitionAnimation.getValue();
            this.previewOffsetX = this.transitionStartPosition[0] + (this.transitionTargetPosition[0] - this.transitionStartPosition[0]) * f;
            this.previewOffsetY = this.transitionStartPosition[1] + (this.transitionTargetPosition[1] - this.transitionStartPosition[1]) * f;
            this.interactionOffset = this.transitionStartPosition[2] + (this.transitionTargetPosition[2] - this.transitionStartPosition[2]) * f;
            this.interactionOffsetX = this.transitionPanelSize[0] * (1.0f - f);
            this.interactionOffsetY = this.transitionPanelSize[1] * (1.0f - f);
            if (f >= 1.0f) {
                this.previewTransitionActive = false;
                this.previousPreviewComponents = null;
            }
        }
        if (this.draggingColorChannel && this.editSpace == EditSpace.ROTATION) {
            float[] fArray = this.transformColorComponents(this.getDisplayedColorComponents());
            this.previewOffsetX = this.rotatedColorComponents[0] - fArray[0];
            this.previewOffsetY = this.rotatedColorComponents[1] - fArray[1];
            this.interactionOffset = this.rotatedColorComponents[2] - fArray[2];
        }
        this.panelOpenAnimation.setReverse(true);
        f = Math.min(1.0f, this.panelOpenAnimation.getValue());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f);
        ItemRenderUtils.translateAndScale(drawContext.getMatrices(), this.x + this.width / 2.0f, this.y + this.height / 2.0f, 0.5f + this.panelOpenAnimation.getValue() * 0.5f);
        this.renderPickerBackground(drawContext);
        this.renderPreviewModeControls(drawContext);
        this.contentOriginX = this.x;
        this.contentOriginY = this.y + 32.0f;
        this.contentWidth = this.width;
        this.contentViewportHeight = this.height - 32.0f - 28.0f;
        float f2 = this.contentOriginX + this.contentWidth / 2.0f;
        float f3 = this.contentOriginY + this.contentViewportHeight / 2.0f;
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)this.contentOriginX, (float)this.contentOriginY, (float)this.contentWidth, (float)this.contentViewportHeight);
        if (ClientAccess.minecraftClient.player != null) {
            this.renderPreviewModel(drawContext, f2, f3);
            if (this.previewMode != PreviewMode.PREVIEW) {
                this.renderColorEditor(drawContext);
            }
        }
        moscow.rockstar.render.state.UiScissorStack.pop();
        this.renderEditorControls(drawContext);
        ItemRenderUtils.popMatrix(drawContext.getMatrices());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    @Compile(obfuscation=1)
    private void renderPreviewModel(RockstarDrawContext drawContext, float f, float f2) {
        float[] fArray = this.getDisplayedColorComponents();
        float f3 = fArray[0];
        float f4 = fArray[1];
        float f5 = fArray[2];
        float f6 = fArray[3];
        float f7 = fArray[4];
        float f8 = fArray[5];
        float f9 = fArray[6];
        float f10 = fArray[7];
        float f11 = fArray[8];
        MatrixStack class_45872 = drawContext.getMatrices();
        class_45872.push();
        class_45872.translate(f + this.interactionOffsetX, f2 + this.interactionOffsetY, 250.0f);
        class_45872.scale(this.previewScale, -this.previewScale, this.previewScale);
        class_45872.multiply(new Quaternionf().rotateX((float)Math.toRadians(this.rotationPitch)).rotateY((float)Math.toRadians(this.rotationYaw)));
        class_45872.translate(this.previewOffsetX, this.previewOffsetY, this.interactionOffset);
        this.previewTransform.set((Matrix4fc)class_45872.peek().getPositionMatrix());
        class_45872.translate(f3, f4, f5);
        class_45872.translate(f6, f7, f8);
        class_45872.multiply(new Quaternionf().rotationXYZ((float)Math.toRadians(f9), (float)Math.toRadians(f10), (float)Math.toRadians(f11)));
        class_45872.translate(-f3, -f4, -f5);
        class_45872.translate(0.56f, -0.52f, -0.72f);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.disableCull();
        GL11.glClear((int)256);
        HeldItemRenderer heldItemRenderer = ClientAccess.minecraftClient.getEntityRenderDispatcher().getHeldItemRenderer();
        VertexConsumerProvider.Immediate vertexConsumers = ClientAccess.minecraftClient.getBufferBuilders().getEntityVertexConsumers();
        try {
            heldItemRenderer.renderItem(ClientAccess.minecraftClient.player, this.previewItemStack, ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, false, class_45872, vertexConsumers, 0xF000F0);
            vertexConsumers.draw();
        }
        catch (Exception exception) {
            // empty catch block
        }
        RenderSystem.enableCull();
        class_45872.pop();
    }

    @Compile(obfuscation=1)
    private void renderColorEditor(RockstarDrawContext drawContext) {
        int n;
        int n2;
        float[] fArray;
        float[] fArray2 = fArray = this.editSpace == EditSpace.ANCHOR ? this.getCombinedColorComponents() : this.transformColorComponents(this.getDisplayedColorComponents());
        if (!(this.draggingColorChannel || this.rotatingPreview || this.previewTransitionActive)) {
            Vec2f VanillaAdventureTabAdvancementGenerator = this.projectColorPoint(fArray[0], fArray[1], fArray[2]);
            if (VanillaAdventureTabAdvancementGenerator.x < this.contentOriginX || VanillaAdventureTabAdvancementGenerator.x > this.contentOriginX + this.contentWidth || VanillaAdventureTabAdvancementGenerator.y < this.contentOriginY || VanillaAdventureTabAdvancementGenerator.y > this.contentOriginY + this.contentViewportHeight) {
                this.startPreviewTransition();
            }
        }
        this.colorSelectionCenter = this.projectColorPoint(fArray[0], fArray[1], fArray[2]);
        if (this.editSpace == EditSpace.ROTATION) {
            for (int i = 0; i < 3; ++i) {
                this.colorWheelPoints[i] = this.buildColorWheelPoints(fArray[0], fArray[1], fArray[2], i);
            }
        } else {
            this.channelSelectionPoints[0] = this.projectColorPoint(fArray[0] + 0.45f, fArray[1], fArray[2]);
            this.channelSelectionPoints[1] = this.projectColorPoint(fArray[0], fArray[1] + 0.45f, fArray[2]);
            this.channelSelectionPoints[2] = this.projectColorPoint(fArray[0], fArray[1], fArray[2] + 0.45f);
        }
        int n3 = n2 = this.draggingColorChannel ? this.selectedChannelIndex : this.findNearestColorChannel(drawContext.mouseX(), drawContext.mouseY());
        if (n2 >= 0 || this.draggingColorChannel) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
        }
        RenderSystem.disableDepthTest();
        WidgetBatchRenderer.flushCurrentBatch();
        if (this.editSpace == EditSpace.ROTATION) {
            for (n = 0; n < 3; ++n) {
                this.drawColorWheelChannel(drawContext, fArray, n, channelColors[n].mulAlpha(n == n2 ? 1.0f : 0.6f));
            }
        } else {
            for (n = 0; n < 3; ++n) {
                this.drawColorAxis(drawContext, this.colorSelectionCenter, this.channelSelectionPoints[n], channelColors[n].mulAlpha(n == n2 ? 1.0f : 0.7f));
            }
        }
        n = this.editSpace == EditSpace.ANCHOR ? 1 : 0;
        float f = n != 0 ? 2.5f : 1.5f;
        drawContext.drawRoundedRect(this.colorSelectionCenter.x - f, this.colorSelectionCenter.y - f, f * 2.0f, f * 2.0f, WidgetState.uniform(f), (n != 0 ? ColorPalette.getAccentColor() : ColorPalette.WHITE).mulAlpha(0.9f));
        RenderSystem.enableDepthTest();
    }

    private void drawColorAxis(RockstarDrawContext drawContext, Vec2f VanillaAdventureTabAdvancementGenerator, Vec2f MagmaBlock, ColorRGBA colorRGBA) {
        this.drawColorGradientSegment(drawContext, VanillaAdventureTabAdvancementGenerator, MagmaBlock, colorRGBA);
        float f = (float)Math.atan2(MagmaBlock.y - VanillaAdventureTabAdvancementGenerator.y, MagmaBlock.x - VanillaAdventureTabAdvancementGenerator.x);
        float f2 = 4.0f;
        this.drawColorGradientSegment(drawContext, MagmaBlock, new Vec2f(MagmaBlock.x - f2 * (float)Math.cos(f - 0.42f), MagmaBlock.y - f2 * (float)Math.sin(f - 0.42f)), colorRGBA);
        this.drawColorGradientSegment(drawContext, MagmaBlock, new Vec2f(MagmaBlock.x - f2 * (float)Math.cos(f + 0.42f), MagmaBlock.y - f2 * (float)Math.sin(f + 0.42f)), colorRGBA);
    }

    private void drawColorGradientSegment(RockstarDrawContext drawContext, Vec2f VanillaAdventureTabAdvancementGenerator, Vec2f MagmaBlock, ColorRGBA colorRGBA) {
        float f = (MagmaBlock.x - VanillaAdventureTabAdvancementGenerator.x) / 3.0f;
        float f2 = (MagmaBlock.y - VanillaAdventureTabAdvancementGenerator.y) / 3.0f;
        this.drawBezierSegment(drawContext, VanillaAdventureTabAdvancementGenerator, new Vec2f(VanillaAdventureTabAdvancementGenerator.x + f, VanillaAdventureTabAdvancementGenerator.y + f2), new Vec2f(VanillaAdventureTabAdvancementGenerator.x + 2.0f * f, VanillaAdventureTabAdvancementGenerator.y + 2.0f * f2), MagmaBlock, colorRGBA);
    }

    private void drawBezierSegment(RockstarDrawContext drawContext, Vec2f VanillaAdventureTabAdvancementGenerator, Vec2f MagmaBlock, Vec2f VanillaHusbandryTabAdvancementGenerator, Vec2f BlockMirror, ColorRGBA colorRGBA) {
        float f = 3.5f;
        float f2 = Math.min(Math.min(VanillaAdventureTabAdvancementGenerator.x, MagmaBlock.x), Math.min(VanillaHusbandryTabAdvancementGenerator.x, BlockMirror.x)) - f;
        float f3 = Math.min(Math.min(VanillaAdventureTabAdvancementGenerator.y, MagmaBlock.y), Math.min(VanillaHusbandryTabAdvancementGenerator.y, BlockMirror.y)) - f;
        float f4 = Math.max(Math.max(VanillaAdventureTabAdvancementGenerator.x, MagmaBlock.x), Math.max(VanillaHusbandryTabAdvancementGenerator.x, BlockMirror.x)) + f;
        float f5 = Math.max(Math.max(VanillaAdventureTabAdvancementGenerator.y, MagmaBlock.y), Math.max(VanillaHusbandryTabAdvancementGenerator.y, BlockMirror.y)) + f;
        drawContext.drawSmoothBezier(f2, f3, f4 - f2, f5 - f3, VanillaAdventureTabAdvancementGenerator, MagmaBlock, VanillaHusbandryTabAdvancementGenerator, BlockMirror, 1.5f, colorRGBA);
    }

    @Compile(obfuscation=1)
    private void drawColorWheelChannel(RockstarDrawContext drawContext, float[] fArray, int n, ColorRGBA colorRGBA) {
        Vector3f vector3f = SwingEditorWidget.getChannelAxis(n);
        Vector3f vector3f2 = SwingEditorWidget.getCrossAxis(n);
        Vector3f vector3f3 = new Vector3f(fArray[0], fArray[1], fArray[2]);
        float f = 0.2761424f;
        for (int i = 0; i < 4; ++i) {
            double d = (double)i * Math.PI / 2.0;
            double d2 = d + 1.5707963267948966;
            Vector3f vector3f4 = SwingEditorWidget.calculateWheelPoint(vector3f3, vector3f, vector3f2, d);
            Vector3f vector3f5 = SwingEditorWidget.calculateWheelPoint(vector3f3, vector3f, vector3f2, d2);
            Vector3f vector3f6 = SwingEditorWidget.calculateWheelTangent(vector3f, vector3f2, d);
            Vector3f vector3f7 = SwingEditorWidget.calculateWheelTangent(vector3f, vector3f2, d2);
            this.drawBezierSegment(drawContext, this.projectColorPoint(vector3f4.x, vector3f4.y, vector3f4.z), this.projectColorPoint(vector3f4.x + vector3f6.x * f, vector3f4.y + vector3f6.y * f, vector3f4.z + vector3f6.z * f), this.projectColorPoint(vector3f5.x - vector3f7.x * f, vector3f5.y - vector3f7.y * f, vector3f5.z - vector3f7.z * f), this.projectColorPoint(vector3f5.x, vector3f5.y, vector3f5.z), colorRGBA);
        }
    }

    private static Vector3f getChannelAxis(int n) {
        return n == 0 ? new Vector3f(0.0f, 1.0f, 0.0f) : (n == 1 ? new Vector3f(0.0f, 0.0f, 1.0f) : new Vector3f(1.0f, 0.0f, 0.0f));
    }

    private static Vector3f getCrossAxis(int n) {
        return n == 0 ? new Vector3f(0.0f, 0.0f, 1.0f) : (n == 1 ? new Vector3f(1.0f, 0.0f, 0.0f) : new Vector3f(0.0f, 1.0f, 0.0f));
    }

    private static Vector3f calculateWheelPoint(Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3, double d) {
        float f = (float)Math.cos(d) * 0.5f;
        float f2 = (float)Math.sin(d) * 0.5f;
        return new Vector3f(vector3f.x + vector3f2.x * f + vector3f3.x * f2, vector3f.y + vector3f2.y * f + vector3f3.y * f2, vector3f.z + vector3f2.z * f + vector3f3.z * f2);
    }

    private static Vector3f calculateWheelTangent(Vector3f vector3f, Vector3f vector3f2, double d) {
        float f = (float)Math.cos(d);
        float f2 = (float)Math.sin(d);
        return new Vector3f(-vector3f.x * f2 + vector3f2.x * f, -vector3f.y * f2 + vector3f2.y * f, -vector3f.z * f2 + vector3f2.z * f);
    }

    private Vec2f[] buildColorWheelPoints(float f, float f2, float f3, int n) {
        Vec2f[] class_241Array = new Vec2f[44];
        for (int i = 0; i < 44; ++i) {
            double d = Math.PI * 2 * (double)i / 44.0;
            float f6 = 0.5f * (float)Math.cos(d);
            float f7 = 0.5f * (float)Math.sin(d);
            float xOffset;
            float yOffset;
            float zOffset;
            switch (n) {
                case 0 -> {
                    xOffset = 0.0f;
                    yOffset = f6;
                    zOffset = f7;
                }
                case 1 -> {
                    xOffset = f7;
                    yOffset = 0.0f;
                    zOffset = f6;
                }
                default -> {
                    xOffset = f6;
                    yOffset = f7;
                    zOffset = 0.0f;
                }
            }
            class_241Array[i] = this.projectColorPoint(f + xOffset, f2 + yOffset, f3 + zOffset);
        }
        return class_241Array;
    }

    private Vec2f projectColorPoint(float f, float f2, float f3) {
        Vector4f vector4f = new Vector4f(f, f2, f3, 1.0f);
        this.previewTransform.transform(vector4f);
        return new Vec2f(vector4f.x, vector4f.y);
    }

    private float[] getCombinedColorComponents() {
        float[] fArray = this.getDisplayedColorComponents();
        return new float[]{fArray[0] + fArray[3], fArray[1] + fArray[4], fArray[2] + fArray[5]};
    }

    private float[] transformColorComponents(float[] fArray) {
        float[] fArray2 = this.capturePreviewColorComponents();
        Vector3f vector3f = new Vector3f(0.56f + fArray2[0] - fArray[0], -0.52f + fArray2[1] - fArray[1], -0.72f + fArray2[2] - fArray[2]);
        new Quaternionf().rotationXYZ((float)Math.toRadians(fArray[6]), (float)Math.toRadians(fArray[7]), (float)Math.toRadians(fArray[8])).transform(vector3f);
        return new float[]{fArray[0] + fArray[3] + vector3f.x, fArray[1] + fArray[4] + vector3f.y, fArray[2] + fArray[5] + vector3f.z};
    }

    private float[] capturePreviewColorComponents() {
        if (this.previewColorComponents != null) {
            return this.previewColorComponents;
        }
        if (ClientAccess.minecraftClient.player == null) {
            return EMPTY_COLOR_COMPONENTS;
        }
        ModelBoundsCapture colorCapture = new ModelBoundsCapture();
        try {
            VertexConsumerProvider colorCaptureProvider = renderLayer -> colorCapture;
            ClientAccess.minecraftClient.getEntityRenderDispatcher().getHeldItemRenderer().renderItem(ClientAccess.minecraftClient.player, this.previewItemStack, ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, false, new MatrixStack(), colorCaptureProvider, 0xF000F0);
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.previewColorComponents = colorCapture.getValue();
        if (this.previewColorComponents == null && ++this.previewCaptureAttempts > 20) {
            this.previewColorComponents = EMPTY_COLOR_COMPONENTS;
        }
        return this.previewColorComponents != null ? this.previewColorComponents : EMPTY_COLOR_COMPONENTS;
    }

    private float[] getTransformedColorSettings(HandSwingSettings swingSettings) {
        float[] fArray = this.readColorSettings(swingSettings);
        float[] fArray2 = this.transformColorComponents(fArray);
        if (this.editSpace == EditSpace.ANCHOR && this.previewMode != PreviewMode.PREVIEW) {
            return new float[]{-(fArray2[0] + fArray[0] + fArray[3]) / 2.0f, -(fArray2[1] + fArray[1] + fArray[4]) / 2.0f, -(fArray2[2] + fArray[2] + fArray[5]) / 2.0f};
        }
        return new float[]{-fArray2[0], -fArray2[1], -fArray2[2]};
    }

    private float[] readColorSettings(HandSwingSettings swingSettings) {
        return new float[]{
                swingSettings.getAnchorXSetting().getValue(),
                swingSettings.getAnchorYSetting().getValue(),
                swingSettings.getAnchorZSetting().getValue(),
                swingSettings.getMoveXSetting().getValue(),
                swingSettings.getMoveYSetting().getValue(),
                swingSettings.getMoveZSetting().getValue(),
                swingSettings.getRotateXSetting().getValue(),
                swingSettings.getRotateYSetting().getValue(),
                swingSettings.getRotateZSetting().getValue()};
    }

    private void initializePreviewState() {
        float[] fArray = this.getTransformedColorSettings(this.getSwingSettings());
        this.previewOffsetX = fArray[0];
        this.previewOffsetY = fArray[1];
        this.interactionOffset = fArray[2];
        this.interactionOffsetX = 0.0f;
        this.interactionOffsetY = 0.0f;
        this.previewInitialized = this.previewColorComponents != null;
        this.previewTransitionActive = false;
    }

    private void setPreviewMode(PreviewMode previewMode) {
        float[] fArray = this.getDisplayedColorComponents();
        this.previewMode = previewMode;
        this.startPreviewTransition();
        this.previousPreviewComponents = fArray;
    }

    private void startPreviewTransition() {
        this.transitionStartPosition[0] = this.previewOffsetX;
        this.transitionStartPosition[1] = this.previewOffsetY;
        this.transitionStartPosition[2] = this.interactionOffset;
        float[] fArray = this.getTransformedColorSettings(this.getSwingSettings());
        this.transitionTargetPosition[0] = fArray[0];
        this.transitionTargetPosition[1] = fArray[1];
        this.transitionTargetPosition[2] = fArray[2];
        this.transitionPanelSize[0] = this.interactionOffsetX;
        this.transitionPanelSize[1] = this.interactionOffsetY;
        this.previousPreviewComponents = null;
        this.previewInitialized = true;
        this.previewTransitionActive = true;
        this.previewTransitionAnimation.setValue(0.0f);
    }

    private void refreshPreviewTransition() {
        float[] fArray = this.readColorSettings(this.getSwingSettings());
        boolean bl = this.lastColorSettings != null && !Arrays.equals(fArray, this.lastColorSettings);
        this.lastColorSettings = fArray;
        if (bl && !this.draggingColorChannel && !this.previewTransitionActive && this.previewMode != PreviewMode.PREVIEW) {
            this.startPreviewTransition();
        }
    }

    private float[] getDisplayedColorComponents() {
        if (this.previewMode == PreviewMode.PREVIEW) {
            HandSwingState swingState = RockstarClient.create().getHandSwingPresetManager().interpolateSwingState(this.getPreviewAnimationProgress());
            return new float[]{swingState.getAnchorX(), swingState.getAnchorY(), swingState.getAnchorZ(), swingState.getMoveX(), swingState.getMoveY(), swingState.getMoveZ(), swingState.getRotateX(), swingState.getRotateY(), swingState.getRotateZ()};
        }
        float[] fArray = this.readColorSettings(this.getSwingSettings());
        if (this.previewTransitionActive && this.previousPreviewComponents != null) {
            float f = this.previewTransitionAnimation.getValue();
            float[] fArray2 = new float[9];
            for (int i = 0; i < 9; ++i) {
                fArray2[i] = this.previousPreviewComponents[i] + (fArray[i] - this.previousPreviewComponents[i]) * f;
            }
            return fArray2;
        }
        return fArray;
    }

    private float getPreviewAnimationProgress() {
        float f = RockstarClient.create().getHandSwingPresetManager().getSwingSpeedSetting().getValue();
        long l = (long)Math.max(100.0f, 300.0f * f);
        long l2 = 500L;
        long l3 = System.currentTimeMillis() % (l + l2);
        return l3 >= l ? 0.0f : (float)l3 / (float)l;
    }

    private void renderPickerBackground(RockstarDrawContext drawContext) {
        drawContext.drawShadow(this.x, this.y, this.width, this.height, 25.0f, WidgetState.uniform(11.0f), ColorPalette.BLACK.mulAlpha(0.5f));
        drawContext.drawBlurredRect(this.x, this.y, this.width, this.height, 5.0f, 3.0f, WidgetState.uniform(11.0f), ColorPalette.WHITE);
        drawContext.drawSquircle(this.x, this.y, this.width, this.height, 3.0f, WidgetState.uniform(11.0f), ColorPalette.PANEL_COLOR);
        drawContext.drawSquircleBorder(this.x, this.y, this.width, this.height, 0.5f, 3.0f, WidgetState.uniform(11.0f), ColorPalette.BORDER_COLOR);
    }

    @Compile(obfuscation=1)
    private void renderPreviewModeControls(RockstarDrawContext drawContext) {
        drawContext.drawText(Font.SEMIBOLD.metrics(7.0f), Localization.translate("swing.editor"), this.x + 9.0f, this.y + 8.5f, ColorPalette.getPrimaryTextColor());
        String[] stringArray = new String[]{Localization.translate("swing.start"), Localization.translate("swing.end"), Localization.translate("swing.preview")};
        PreviewMode[] previewModeArray = new PreviewMode[]{PreviewMode.START, PreviewMode.END, PreviewMode.PREVIEW};
        float[] fArray = new float[3];
        float f = -8.0f;
        for (int i = 0; i < 3; ++i) {
            fArray[i] = Font.REGULAR.measure(stringArray[i], 7.0f);
            f += fArray[i] + 8.0f;
        }
        float f2 = this.x + this.width / 2.0f - f / 2.0f;
        this.modeButtonY = this.y + 20.0f;
        for (int i = 0; i < 3; ++i) {
            boolean bl = this.previewMode == previewModeArray[i];
            boolean bl2 = UiUtils.contains((double)(f2 - 4.0f), (double)this.modeButtonY, (double)(fArray[i] + 8.0f), 11.0, drawContext.mouseX(), drawContext.mouseY());
            this.modeHoverAnimations[i].setReverse(bl2);
            this.modeSelectionAnimations[i].setReverse(bl);
            ColorRGBA colorRGBA = ColorPalette.getPrimaryTextColor().mulAlpha(0.45f + 0.35f * this.modeHoverAnimations[i].getValue()).mix(ColorPalette.getAccentColor(), this.modeSelectionAnimations[i].getValue());
            drawContext.drawText(Font.REGULAR.metrics(7.0f), stringArray[i], f2, this.modeButtonY, colorRGBA);
            if (bl2) {
                moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
            }
            this.modeButtonX[i] = f2 - 4.0f;
            this.modeButtonWidth[i] = fArray[i] + 8.0f;
            f2 += fArray[i] + 8.0f;
        }
    }

    @Compile(obfuscation=1)
    private void renderEditorControls(RockstarDrawContext drawContext) {
        float f = 6.0f;
        float f2 = 9.0f;
        this.editorControlY = this.y + this.height - 28.0f + 2.0f;
        this.resetButtonX = this.x + this.width - 9.0f - 16.0f;
        boolean bl = UiUtils.contains((double)this.resetButtonX, (double)this.editorControlY, 16.0, 16.0, drawContext.mouseX(), drawContext.mouseY());
        this.resetButtonAnimation.setReverse(bl);
        ColorRGBA colorRGBA = ColorPalette.getPrimaryTextColor().mulAlpha(0.5f + 0.4f * this.resetButtonAnimation.getValue());
        if (SwingEditorWidget.hasIcon("clean")) {
            drawContext.drawIcon("clean", this.resetButtonX + (16.0f - f2) / 2.0f, this.editorControlY + (16.0f - f2) / 2.0f, f2, colorRGBA);
        } else {
            drawContext.drawText(Font.REGULAR.metrics(7.0f), "C", this.resetButtonX + 8.0f - 2.0f, this.editorControlY + 8.0f - 3.0f, colorRGBA);
        }
        if (bl) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
        }
        if (this.previewMode == PreviewMode.PREVIEW) {
            return;
        }
        String[] stringArray = new String[]{"swing/position", "swing/rotation", "swing/anchor"};
        String[] stringArray2 = new String[]{"M", "R", "P"};
        EditSpace[] editSpaceArray = new EditSpace[]{EditSpace.POSITION, EditSpace.ROTATION, EditSpace.ANCHOR};
        float f3 = 48.0f + 2.0f * f;
        float f4 = this.x + this.width / 2.0f - f3 / 2.0f;
        for (int i = 0; i < 3; ++i) {
            boolean bl2 = this.editSpace == editSpaceArray[i];
            boolean bl3 = UiUtils.contains((double)f4, (double)this.editorControlY, 16.0, 16.0, drawContext.mouseX(), drawContext.mouseY());
            this.spaceHoverAnimations[i].setReverse(bl3);
            this.spaceSelectionAnimations[i].setReverse(bl2);
            ColorRGBA colorRGBA2 = ColorPalette.getPrimaryTextColor().mulAlpha(0.5f + 0.4f * this.spaceHoverAnimations[i].getValue()).mix(ColorPalette.getAccentColor(), this.spaceSelectionAnimations[i].getValue());
            if (SwingEditorWidget.hasIcon(stringArray[i])) {
                drawContext.drawIcon(stringArray[i], f4 + (16.0f - f2) / 2.0f, this.editorControlY + (16.0f - f2) / 2.0f, f2, colorRGBA2);
            } else {
                drawContext.drawText(Font.REGULAR.metrics(7.0f), stringArray2[i], f4 + 8.0f - 2.0f, this.editorControlY + 8.0f - 3.0f, colorRGBA2);
            }
            if (bl3) {
                moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
            }
            this.spaceButtonX[i] = f4;
            this.spaceButtonWidth[i] = 16.0f;
            f4 += 16.0f + f;
        }
    }

    private static boolean hasIcon(String name) {
        return moscow.rockstar.render.text.icon.SvgIconRegistry.getCodePoint(name) != null;
    }

    private int findNearestColorChannel(double d, double d2) {
        int n = -1;
        float f = 6.0f;
        if (this.editSpace == EditSpace.ROTATION) {
            for (int i = 0; i < 3; ++i) {
                for (Vec2f VanillaAdventureTabAdvancementGenerator : this.colorWheelPoints[i]) {
                    float f2 = SwingEditorWidget.distanceToColorHandle(d, d2, VanillaAdventureTabAdvancementGenerator.x, VanillaAdventureTabAdvancementGenerator.y);
                    if (!(f2 < f)) continue;
                    f = f2;
                    n = i;
                }
            }
        } else {
            for (int i = 0; i < 3; ++i) {
                float f3 = SwingEditorWidget.distanceToColorSegment((float)d, (float)d2, this.colorSelectionCenter, this.channelSelectionPoints[i]);
                if (!(f3 < f)) continue;
                f = f3;
                n = i;
            }
        }
        return n;
    }

    private boolean isInsideColorPlane(double d, double d2) {
        return UiUtils.contains((double)this.x, (double)(this.y + 32.0f), (double)this.width, (double)(this.height - 32.0f - 28.0f), d, d2);
    }

    private boolean isRotationModifierPressed() {
        return GLFW.glfwGetKey((long)ClientAccess.minecraftClient.getWindow().getHandle(), (int)32) == 1;
    }

    @Override
    @Compile(obfuscation=1)
    public void mouseClicked(double d, double d2, PointerAction pointerAction) {
        int n;
        if ((pointerAction == PointerAction.MIDDLE_CLICK || pointerAction == PointerAction.LEFT_CLICK && this.isRotationModifierPressed()) && this.isInsideColorPlane(d, d2)) {
            this.resizingPanel = true;
            return;
        }
        if (pointerAction != PointerAction.LEFT_CLICK) {
            return;
        }
        PreviewMode[] previewModeArray = new PreviewMode[]{PreviewMode.START, PreviewMode.END, PreviewMode.PREVIEW};
        for (n = 0; n < 3; ++n) {
            if (!UiUtils.contains((double)this.modeButtonX[n], (double)this.modeButtonY, (double)this.modeButtonWidth[n], 11.0, d, d2)) continue;
            if (previewModeArray[n] != this.previewMode) {
                if (previewModeArray[n] == PreviewMode.PREVIEW) {
                    this.previewMode = PreviewMode.PREVIEW;
                    this.startPreviewTransition();
                } else {
                    this.setPreviewMode(previewModeArray[n]);
                }
            }
            return;
        }
        if (UiUtils.contains((double)this.resetButtonX, (double)this.editorControlY, 16.0, 16.0, d, d2)) {
            this.resetColorSelection();
            return;
        }
        if (this.previewMode != PreviewMode.PREVIEW) {
            EditSpace[] editSpaceArray = new EditSpace[]{EditSpace.POSITION, EditSpace.ROTATION, EditSpace.ANCHOR};
            for (int i = 0; i < 3; ++i) {
                if (!UiUtils.contains((double)this.spaceButtonX[i], (double)this.editorControlY, (double)this.spaceButtonWidth[i], 16.0, d, d2)) continue;
                this.setEditSpace(editSpaceArray[i]);
                return;
            }
        }
        if (!this.isInsideColorPlane(d, d2)) {
            return;
        }
        int n2 = n = this.previewMode == PreviewMode.PREVIEW ? -1 : this.findNearestColorChannel(d, d2);
        if (n >= 0) {
            this.draggingColorChannel = true;
            this.previewTransitionActive = false;
            this.selectedChannelIndex = n;
            this.dragStartX = d;
            this.dragStartY = d2;
            this.dragStartChannelValue = this.getChannelAmount(n);
            HandSwingSettings swingSettings = this.getSwingSettings();
            this.baseColorComponents[0] = swingSettings.getAnchorXSetting().getValue();
            this.baseColorComponents[1] = swingSettings.getAnchorYSetting().getValue();
            this.baseColorComponents[2] = swingSettings.getAnchorZSetting().getValue();
            this.rotationCompensation[0] = swingSettings.getMoveXSetting().getValue();
            this.rotationCompensation[1] = swingSettings.getMoveYSetting().getValue();
            this.rotationCompensation[2] = swingSettings.getMoveZSetting().getValue();
            if (this.editSpace == EditSpace.ROTATION) {
                this.rotationCenter = this.colorSelectionCenter;
                this.rotationStartAngle = (float)Math.atan2(d2 - (double)this.rotationCenter.y, d - (double)this.rotationCenter.x);
                float[] fArray = this.transformColorComponents(this.getDisplayedColorComponents());
                this.rotatedColorComponents[0] = this.previewOffsetX + fArray[0];
                this.rotatedColorComponents[1] = this.previewOffsetY + fArray[1];
                this.rotatedColorComponents[2] = this.interactionOffset + fArray[2];
            }
        } else {
            this.rotatingPreview = true;
        }
    }

    @Override
    public void mouseReleased(double d, double d2, PointerAction pointerAction) {
        this.draggingColorChannel = false;
        this.rotatingPreview = false;
        this.resizingPanel = false;
        this.selectedChannelIndex = -1;
    }

    public void handlePointerDrag(double d, double d2, PointerAction pointerAction, double d3, double d4) {
        if (this.resizingPanel) {
            this.interactionOffsetX += (float)d3;
            this.interactionOffsetY += (float)d4;
            return;
        }
        if (this.rotatingPreview) {
            this.rotationYaw += (float)d3 * 0.6f;
            this.rotationPitch = MathHelper.clamp((float)(this.rotationPitch + (float)d4 * 0.6f), (float)-89.0f, (float)89.0f);
            return;
        }
        if (!this.draggingColorChannel || this.selectedChannelIndex < 0) {
            return;
        }
        if (this.editSpace == EditSpace.ROTATION) {
            float f = (float)Math.atan2(d2 - (double)this.rotationCenter.y, d - (double)this.rotationCenter.x);
            float f2 = (float)Math.toDegrees(SwingEditorWidget.normalizeAngle(f - this.rotationStartAngle));
            this.setChannelAmount(this.selectedChannelIndex, this.dragStartChannelValue + f2);
        } else {
            Vec2f VanillaAdventureTabAdvancementGenerator = new Vec2f(this.channelSelectionPoints[this.selectedChannelIndex].x - this.colorSelectionCenter.x, this.channelSelectionPoints[this.selectedChannelIndex].y - this.colorSelectionCenter.y);
            float f = (float)Math.sqrt(VanillaAdventureTabAdvancementGenerator.x * VanillaAdventureTabAdvancementGenerator.x + VanillaAdventureTabAdvancementGenerator.y * VanillaAdventureTabAdvancementGenerator.y);
            if (f < 0.001f) {
                return;
            }
            float f3 = (float)((d - this.dragStartX) * (double)VanillaAdventureTabAdvancementGenerator.x + (d2 - this.dragStartY) * (double)VanillaAdventureTabAdvancementGenerator.y) / f;
            float f4 = f3 * (0.45f / f);
            if (this.editSpace == EditSpace.POSITION) {
                this.setChannelAmount(this.selectedChannelIndex, this.dragStartChannelValue + f4);
            } else {
                this.applyRotatedChannelDelta(this.selectedChannelIndex, f4);
            }
        }
        this.synchronizeEditorState();
    }

    private void applyRotatedChannelDelta(int n, float f) {
        HandSwingSettings swingSettings = this.getSwingSettings();
        Quaternionf quaternionf = new Quaternionf().rotationXYZ((float)Math.toRadians(swingSettings.getRotateXSetting().getValue()), (float)Math.toRadians(swingSettings.getRotateYSetting().getValue()), (float)Math.toRadians(swingSettings.getRotateZSetting().getValue())).conjugate();
        Vector3f vector3f = new Vector3f();
        if (n == 0) {
            vector3f.x = f;
        } else if (n == 1) {
            vector3f.y = f;
        } else {
            vector3f.z = f;
        }
        Vector3f vector3f2 = quaternionf.transform(new Vector3f((Vector3fc)vector3f));
        swingSettings.getAnchorXSetting().updateValue(this.baseColorComponents[0] + vector3f2.x);
        swingSettings.getAnchorYSetting().updateValue(this.baseColorComponents[1] + vector3f2.y);
        swingSettings.getAnchorZSetting().updateValue(this.baseColorComponents[2] + vector3f2.z);
        swingSettings.getMoveXSetting().updateValue(this.rotationCompensation[0] + (vector3f.x - vector3f2.x));
        swingSettings.getMoveYSetting().updateValue(this.rotationCompensation[1] + (vector3f.y - vector3f2.y));
        swingSettings.getMoveZSetting().updateValue(this.rotationCompensation[2] + (vector3f.z - vector3f2.z));
    }

    private void synchronizeEditorState() {
        if (!Screen.hasShiftDown() && !Screen.hasControlDown()) {
            return;
        }
        HandSwingSettings swingSettings = this.getSwingSettings();
        HandSwingSettings otherSettings = this.previewMode == PreviewMode.END
                ? RockstarClient.create().getHandSwingPresetManager().getInitialSwing()
                : RockstarClient.create().getHandSwingPresetManager().getFinalSwing();
        switch (this.editSpace.ordinal()) {
            case 0: {
                otherSettings.getMoveXSetting().updateValue(swingSettings.getMoveXSetting().getValue());
                otherSettings.getMoveYSetting().updateValue(swingSettings.getMoveYSetting().getValue());
                otherSettings.getMoveZSetting().updateValue(swingSettings.getMoveZSetting().getValue());
                break;
            }
            case 1: {
                otherSettings.getRotateXSetting().updateValue(swingSettings.getRotateXSetting().getValue());
                otherSettings.getRotateYSetting().updateValue(swingSettings.getRotateYSetting().getValue());
                otherSettings.getRotateZSetting().updateValue(swingSettings.getRotateZSetting().getValue());
                break;
            }
            case 2: {
                otherSettings.getAnchorXSetting().updateValue(swingSettings.getAnchorXSetting().getValue());
                otherSettings.getAnchorYSetting().updateValue(swingSettings.getAnchorYSetting().getValue());
                otherSettings.getAnchorZSetting().updateValue(swingSettings.getAnchorZSetting().getValue());
                otherSettings.getMoveXSetting().updateValue(swingSettings.getMoveXSetting().getValue());
                otherSettings.getMoveYSetting().updateValue(swingSettings.getMoveYSetting().getValue());
                otherSettings.getMoveZSetting().updateValue(swingSettings.getMoveZSetting().getValue());
            }
        }
    }

    @Override
    public void mouseScrolled(double d, double d2, double d3, double d4) {
        if (!this.isInsideColorPlane(d, d2)) {
            return;
        }
        float f = MathHelper.clamp((float)(this.previewScale * (1.0f + (float)d4 * 0.1f)), (float)30.0f, (float)280.0f);
        float f2 = this.previewScale == 0.0f ? 1.0f : f / this.previewScale;
        float f3 = this.contentOriginX + this.contentWidth / 2.0f + this.interactionOffsetX;
        float f4 = this.contentOriginY + this.contentViewportHeight / 2.0f + this.interactionOffsetY;
        this.interactionOffsetX += ((float)d - f3) * (1.0f - f2);
        this.interactionOffsetY += ((float)d2 - f4) * (1.0f - f2);
        this.previewScale = f;
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        if (Screen.hasControlDown()) {
            if (n == 90) {
                this.undoEditorChange();
            } else if (n == 89) {
                this.redoEditorChange();
            }
            return;
        }
        switch (n) {
            case 71: {
                this.setEditSpace(EditSpace.POSITION);
                break;
            }
            case 82: {
                this.setEditSpace(EditSpace.ROTATION);
                break;
            }
            case 84: {
                this.setEditSpace(EditSpace.ANCHOR);
                break;
            }
            case 70: {
                this.initializePreviewState();
                break;
            }
        }
    }

    private void setEditSpace(EditSpace editSpace) {
        if (this.editSpace == editSpace) {
            return;
        }
        this.editSpace = editSpace;
        this.startPreviewTransition();
    }

    private NumberSetting getChannelSetting(int n) {
        HandSwingSettings swingSettings = this.getSwingSettings();
        return switch (this.editSpace.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (n == 0) {
                    yield swingSettings.getMoveXSetting();
                }
                if (n == 1) {
                    yield swingSettings.getMoveYSetting();
                }
                yield swingSettings.getMoveZSetting();
            }
            case 1 -> {
                if (n == 0) {
                    yield swingSettings.getRotateXSetting();
                }
                if (n == 1) {
                    yield swingSettings.getRotateYSetting();
                }
                yield swingSettings.getRotateZSetting();
            }
            case 2 -> n == 0 ? swingSettings.getAnchorXSetting() : (n == 1 ? swingSettings.getAnchorYSetting() : swingSettings.getAnchorZSetting());
        };
    }

    private float getChannelAmount(int n) {
        return this.getChannelSetting(n).getValue();
    }

    private void setChannelAmount(int n, float f) {
        this.getChannelSetting(n).updateValue(f);
    }

    private void resetColorSelection() {
        this.restoreEditorState(resetColorComponents);
    }

    private float[] captureEditorState() {
        HandSwingPresetManager manager = RockstarClient.create().getHandSwingPresetManager();
        HandSwingSettings start = manager.getInitialSwing();
        HandSwingSettings end = manager.getFinalSwing();
        Vec2f startControlPoint = manager.getEasingSetting().getStartControlPoint();
        Vec2f endControlPoint = manager.getEasingSetting().getEndControlPoint();
        return new float[]{
                start.getAnchorXSetting().getValue(), start.getAnchorYSetting().getValue(), start.getAnchorZSetting().getValue(),
                start.getMoveXSetting().getValue(), start.getMoveYSetting().getValue(), start.getMoveZSetting().getValue(),
                start.getRotateXSetting().getValue(), start.getRotateYSetting().getValue(), start.getRotateZSetting().getValue(),
                end.getAnchorXSetting().getValue(), end.getAnchorYSetting().getValue(), end.getAnchorZSetting().getValue(),
                end.getMoveXSetting().getValue(), end.getMoveYSetting().getValue(), end.getMoveZSetting().getValue(),
                end.getRotateXSetting().getValue(), end.getRotateYSetting().getValue(), end.getRotateZSetting().getValue(),
                startControlPoint.x, startControlPoint.y, endControlPoint.x, endControlPoint.y,
                manager.getBackSwingSetting().isEnabled() ? 1.0f : 0.0f,
                manager.getSwingSpeedSetting().getValue()};
    }

    private void restoreEditorState(float[] fArray) {
        HandSwingPresetManager manager = RockstarClient.create().getHandSwingPresetManager();
        HandSwingSettings start = manager.getInitialSwing();
        HandSwingSettings end = manager.getFinalSwing();
        start.getAnchorXSetting().updateValue(fArray[0]);
        start.getAnchorYSetting().updateValue(fArray[1]);
        start.getAnchorZSetting().updateValue(fArray[2]);
        start.getMoveXSetting().updateValue(fArray[3]);
        start.getMoveYSetting().updateValue(fArray[4]);
        start.getMoveZSetting().updateValue(fArray[5]);
        start.getRotateXSetting().updateValue(fArray[6]);
        start.getRotateYSetting().updateValue(fArray[7]);
        start.getRotateZSetting().updateValue(fArray[8]);
        end.getAnchorXSetting().updateValue(fArray[9]);
        end.getAnchorYSetting().updateValue(fArray[10]);
        end.getAnchorZSetting().updateValue(fArray[11]);
        end.getMoveXSetting().updateValue(fArray[12]);
        end.getMoveYSetting().updateValue(fArray[13]);
        end.getMoveZSetting().updateValue(fArray[14]);
        end.getRotateXSetting().updateValue(fArray[15]);
        end.getRotateYSetting().updateValue(fArray[16]);
        end.getRotateZSetting().updateValue(fArray[17]);
        manager.getEasingSetting().setStartControlPoint(new Vec2f(fArray[18], fArray[19]))
                .setEndControlPoint(new Vec2f(fArray[20], fArray[21]));
        manager.getBackSwingSetting().setActiveExtra(fArray[22] != 0.0f);
        manager.getSwingSpeedSetting().updateValue(fArray[23]);
    }

    private void recordEditorState() {
        float[] fArray = this.captureEditorState();
        if (this.lastCommittedSettings == null) {
            this.lastCommittedSettings = fArray;
            return;
        }
        if (this.isMouseButtonDown()) {
            return;
        }
        if (!Arrays.equals(fArray, this.lastCommittedSettings)) {
            this.undoHistory.push(this.lastCommittedSettings);
            this.redoHistory.clear();
            this.lastCommittedSettings = fArray;
        }
    }

    private boolean isMouseButtonDown() {
        long l = ClientAccess.minecraftClient.getWindow().getHandle();
        return GLFW.glfwGetMouseButton((long)l, (int)0) == 1 || GLFW.glfwGetMouseButton((long)l, (int)1) == 1 || GLFW.glfwGetMouseButton((long)l, (int)2) == 1;
    }

    private void undoEditorChange() {
        if (this.undoHistory.isEmpty()) {
            return;
        }
        this.redoHistory.push(this.captureEditorState());
        this.restoreEditorState(this.undoHistory.pop());
        this.lastCommittedSettings = this.captureEditorState();
    }

    private void redoEditorChange() {
        if (this.redoHistory.isEmpty()) {
            return;
        }
        this.undoHistory.push(this.captureEditorState());
        this.restoreEditorState(this.redoHistory.pop());
        this.lastCommittedSettings = this.captureEditorState();
    }

    private static float distanceToColorHandle(double d, double d2, float f, float f2) {
        double d3 = d - (double)f;
        double d4 = d2 - (double)f2;
        return (float)Math.sqrt(d3 * d3 + d4 * d4);
    }

    private static float distanceToColorSegment(float f, float f2, Vec2f VanillaAdventureTabAdvancementGenerator, Vec2f MagmaBlock) {
        float f3 = MagmaBlock.x - VanillaAdventureTabAdvancementGenerator.x;
        float f4 = MagmaBlock.y - VanillaAdventureTabAdvancementGenerator.y;
        float f5 = f - VanillaAdventureTabAdvancementGenerator.x;
        float f6 = f2 - VanillaAdventureTabAdvancementGenerator.y;
        float f7 = f3 * f3 + f4 * f4;
        float f8 = f7 < 1.0E-5f ? 0.0f : MathHelper.clamp((float)((f5 * f3 + f6 * f4) / f7), (float)0.0f, (float)1.0f);
        float f9 = VanillaAdventureTabAdvancementGenerator.x + f8 * f3;
        float f10 = VanillaAdventureTabAdvancementGenerator.y + f8 * f4;
        float f11 = f - f9;
        float f12 = f2 - f10;
        return (float)Math.sqrt(f11 * f11 + f12 * f12);
    }

    private static float normalizeAngle(float f) {
        while ((double)f > Math.PI) {
            f -= (float)Math.PI * 2;
        }
        while ((double)f < -Math.PI) {
            f += (float)Math.PI * 2;
        }
        return f;
    }

    /** Accumulates the bounding box of every vertex emitted while rendering the preview item. */
    private static final class ModelBoundsCapture implements VertexConsumer {
        private float minX = Float.MAX_VALUE;
        private float minY = Float.MAX_VALUE;
        private float minZ = Float.MAX_VALUE;
        private float maxX = -3.4028235E38f;
        private float maxY = -3.4028235E38f;
        private float maxZ = -3.4028235E38f;

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            this.minX = Math.min(this.minX, x);
            this.minY = Math.min(this.minY, y);
            this.minZ = Math.min(this.minZ, z);
            this.maxX = Math.max(this.maxX, x);
            this.maxY = Math.max(this.maxY, y);
            this.maxZ = Math.max(this.maxZ, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        private float[] getValue() {
            if (this.minX > this.maxX) {
                return null;
            }
            return new float[]{(this.minX + this.maxX) / 2.0f, (this.minY + this.maxY) / 2.0f, (this.minZ + this.maxZ) / 2.0f};
        }
    }

    static enum PreviewMode {
        START,
        END,
        PREVIEW;
}

    static enum EditSpace {
        POSITION,
        ROTATION,
        ANCHOR;
}
}

