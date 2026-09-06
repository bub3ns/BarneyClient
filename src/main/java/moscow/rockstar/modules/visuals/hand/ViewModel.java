/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.PlayerEntityRenderer
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Entity
 *  net.minecraft.Arm
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerModelPart
 *  net.minecraft.ItemStack
 *  net.minecraft.World
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.ChatScreen
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.VertexConsumerProvider$Immediate
 *  net.minecraft.Vector2f
 *  net.minecraft.ClientPlayerEntity
 *  net.minecraft.ModelTransformationMode
 *  net.minecraft.ItemRenderer
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 *  org.lwjgl.opengl.GL11
 */
package moscow.rockstar.modules.visuals.hand;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.visuals.hand.HandRenderListener;
import moscow.rockstar.network.http.client.ReactorNettyClient;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.postprocess.ShaderPostProcessor;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.vertex.VertexConsumerState;
import moscow.rockstar.settings.ActionSetting;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.Vector2Setting;
import moscow.rockstar.ui.color.ColorPickerScreen;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.hud.HudElementRegistry;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.settings.SettingSnapshotCache;
import moscow.rockstar.ui.settings.SettingSnapshotContext;
import moscow.rockstar.ui.settings.SettingWidget;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.util.Arm;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import pyrock.events.render.ChatRenderEvent;
import pyrock.events.render.HandRenderEvent;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.events.window.ChatClickEvent;
import pyrock.events.window.ChatReleaseEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="View Model", category=ModuleCategory.VISUALS, description="modules.descriptions.view_model")
public class ViewModel
extends Module {
    public static boolean renderingHands;
    private static final int FULL_BRIGHT_LIGHT_COORDINATE = 0xF000F0;
    private static final float CHAT_EDIT_SCALE = 0.4f;
    private static final int TRIG_TABLE_SIZE = 16;
    public static final float[] COSINE_TABLE;
    public static final float[] SINE_TABLE;
    Vector2Setting mainTranslateX;
    Vector2Setting offTranslateX;
    NumberSetting sizeLeft;
    NumberSetting sizeRight;
    private BooleanSetting chatEdit;
    private ActionSetting reset;
    private final VertexConsumerState mainHandState = new VertexConsumerState();
    private final VertexConsumerState offHandState = new VertexConsumerState();
    private float chatScale;
    private float chatOpacity;
    Arm editingHand;
    private float dragStartX;
    private float dragStartY;
    private float initialOffsetX;
    private float initialOffsetY;
    private boolean overlayVisible;
    private final EventListener<HandRenderEvent> onHandRenderEvent = new HandRenderListener(this);
    private final EventListener<ChatRenderEvent> onChatRenderEvent = chatRenderEvent -> {
        if (!this.isChatEditReady()) {
            this.editingHand = null;
            return;
        }
        Vector2f class_56112 = UiUtils.mousePosition();
        if (this.editingHand != null) {
            this.updateChatOffset(class_56112.getX(), class_56112.getY());
        }
        if (this.getActiveVertexState() != null) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
        }
    };
    private final EventListener<PreHudRenderEvent> onPreHudRenderEvent = preHudRenderEvent -> {
        VertexConsumerState vertexConsumerState = this.isChatEditReady() ? this.getActiveVertexState() : null;
        this.renderVertexState(this.mainHandState, vertexConsumerState == this.mainHandState);
        this.renderVertexState(this.offHandState, vertexConsumerState == this.offHandState);
    };
    private final EventListener<ChatClickEvent> onChatClickEvent = chatClickEvent -> {
        if (!this.isChatEditReady() || chatClickEvent.getButton() != 0 || this.isChatPositionValid(chatClickEvent.getX(), chatClickEvent.getY())) {
            return;
        }
        VertexConsumerState vertexConsumerState = this.selectVertexState(chatClickEvent.getX(), chatClickEvent.getY());
        if (vertexConsumerState == null) {
            return;
        }
        this.editingHand = vertexConsumerState == this.mainHandState ? Arm.RIGHT : Arm.LEFT;
        Vector2Setting vector2Setting = this.getHandTranslation(this.editingHand);
        this.dragStartX = chatClickEvent.getX();
        this.dragStartY = chatClickEvent.getY();
        this.initialOffsetX = vector2Setting.getX();
        this.initialOffsetY = vector2Setting.getY();
    };
    private final EventListener<ChatReleaseEvent> onChatReleaseEvent = chatReleaseEvent -> {
        this.editingHand = null;
    };

    public ViewModel() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.offTranslateX = new Vector2Setting(this, "modules.settings.view_model.off_translate_x").setMinX(-2.0f).setMaxX(2.0f).setMinY(-2.0f).setMaxY(2.0f).setValue(0.0f, 0.0f);
        this.mainTranslateX = new Vector2Setting(this, "modules.settings.view_model.main_translate_x").setMinX(-2.0f).setMaxX(2.0f).setMinY(-2.0f).setMaxY(2.0f).setValue(0.0f, 0.0f);
        this.sizeLeft = new NumberSetting(this, "modules.settings.view_model.size_left").setMinValue(0.1f).setMaxValue(1.5f).setStep(0.025f).setValue(1.0f);
        this.sizeRight = new NumberSetting(this, "modules.settings.view_model.size_right").setMinValue(0.1f).setMaxValue(1.5f).setStep(0.025f).setValue(1.0f);
        this.chatEdit = new BooleanSetting(this, "modules.settings.view_model.chat_edit").setActiveExtra(true);
        this.reset = new ActionSetting(this, "modules.settings.view_model.reset").withAction(this::resetTransformState);
    }

    private void handleRenderException(String string, Exception exception) {
        if (this.overlayVisible) {
            return;
        }
        this.overlayVisible = true;
        RockstarClient.LOGGER.error("[viewmodel] {}", (Object)string, (Object)exception);
    }

    private void resetTransformState() {
        SettingSnapshotContext settingSnapshotContext = SettingSnapshotCache.getInner2();
        try {
            this.mainTranslateX.updateValue(0.0f, 0.0f);
            this.offTranslateX.updateValue(0.0f, 0.0f);
            this.sizeRight.updateValue(1.0f);
            this.sizeLeft.updateValue(1.0f);
        }
        finally {
            if (settingSnapshotContext != null) {
                settingSnapshotContext.restoreEnabledFlag();
            }
        }
        this.editingHand = null;
    }

    void onHandRender(HandRenderEvent handRenderEvent) {
        Matrix4f matrix4f = this.createModelMatrix().mul((Matrix4fc)handRenderEvent.getMatrices().peek().getPositionMatrix());
        float f = -0.72f;
        Vector2f class_56112 = this.createTransformedModel(matrix4f, 0.0f, 0.0f, f);
        Vector2f class_56113 = this.createTransformedModel(matrix4f, 1.0f, 0.0f, f);
        Vector2f class_56114 = this.createTransformedModel(matrix4f, 0.0f, 1.0f, f);
        if (class_56112 == null || class_56113 == null || class_56114 == null) {
            return;
        }
        this.chatScale = Math.abs(class_56113.getX() - class_56112.getX());
        this.chatOpacity = Math.abs(class_56114.getY() - class_56112.getY());
    }

    private Matrix4f createModelMatrix() {
        return new Matrix4f((Matrix4fc)RenderSystem.getProjectionMatrix()).mul((Matrix4fc)RenderSystem.getModelViewMatrix());
    }

    private Vector2f createTransformedModel(Matrix4f matrix4f, float f, float f2, float f3) {
        Vector4f vector4f = new Vector4f(f, f2, f3, 1.0f);
        matrix4f.transform(vector4f);
        if (vector4f.w <= 1.0E-4f) {
            return null;
        }
        return new Vector2f((vector4f.x / vector4f.w * 0.5f + 0.5f) * INSTANCE.width(), (0.5f - vector4f.y / vector4f.w * 0.5f) * INSTANCE.height());
    }

    public void renderHandItem(ItemRenderer HorseChestIndexingFix, LivingEntity class_13092, ItemStack class_17992, ModelTransformationMode DeathMessageType, boolean bl, MatrixStack class_45872, World class_19372, int n, int n2, int n3) {
        if (!this.isChatEditReady() || class_17992 == null || class_17992.isEmpty()) {
            return;
        }
        if (DeathMessageType != ModelTransformationMode.FIRST_PERSON_RIGHT_HAND && DeathMessageType != ModelTransformationMode.FIRST_PERSON_LEFT_HAND) {
            return;
        }
        VertexConsumerState vertexConsumerState = DeathMessageType == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND ? this.mainHandState : this.offHandState;
        MatrixStack class_45873 = new MatrixStack();
        class_45873.multiplyPositionMatrix(class_45872.peek().getPositionMatrix());
        vertexConsumerState.beginProjection(this.createModelMatrix());
        try {
            HorseChestIndexingFix.renderItem(class_13092, class_17992, DeathMessageType, bl, class_45873, (VertexConsumerProvider)vertexConsumerState, class_19372, n, n2, n3);
        }
        catch (Exception exception) {
            this.handleRenderException("shape probe failed for " + String.valueOf(class_17992.getItem()), exception);
        }
        vertexConsumerState.endProjection();
        this.submitVertexConsumer(vertexConsumerState, class_45982 -> {
            MatrixStack itemMatrices = new MatrixStack();
            itemMatrices.multiplyPositionMatrix(class_45872.peek().getPositionMatrix());
            HorseChestIndexingFix.renderItem(class_13092, class_17992, DeathMessageType, bl, itemMatrices, (VertexConsumerProvider)class_45982, class_19372, 0xF000F0, n2, n3);
        });
    }

    public void applyHandTransform(Arm class_13062, MatrixStack class_45872) {
        if (!this.isChatEditReady()) {
            return;
        }
        ClientPlayerEntity class_7462 = ViewModel.minecraftClient.player;
        if (class_7462 == null) {
            return;
        }
        Object object = minecraftClient.getEntityRenderDispatcher().getRenderer((Entity)class_7462);
        if (!(object instanceof PlayerEntityRenderer)) {
            return;
        }
        PlayerEntityRenderer FlyingItemEntityRenderState = (PlayerEntityRenderer)object;
        object = class_13062 == Arm.RIGHT ? this.mainHandState : this.offHandState;
        MatrixStack class_45873 = new MatrixStack();
        class_45873.multiplyPositionMatrix(class_45872.peek().getPositionMatrix());
        Identifier class_29602 = class_7462.getSkinTextures().texture();
        boolean bl = class_7462.isPartVisible(class_13062 == Arm.LEFT ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE);
        ((VertexConsumerState)object).beginProjection(this.createModelMatrix());
        try {
            this.renderArmModel(FlyingItemEntityRenderState, class_13062, class_45873, (VertexConsumerProvider)object, class_29602, bl);
        }
        catch (Exception exception) {
            // empty catch block
        }
        ((VertexConsumerState)object).endProjection();
        this.submitVertexConsumer((VertexConsumerState)object, class_45982 -> {
            MatrixStack armMatrices = new MatrixStack();
            armMatrices.multiplyPositionMatrix(class_45872.peek().getPositionMatrix());
            this.renderArmModel(FlyingItemEntityRenderState, class_13062, armMatrices, (VertexConsumerProvider)class_45982, class_29602, bl);
        });
    }

    private void renderArmModel(PlayerEntityRenderer FlyingItemEntityRenderState, Arm class_13062, MatrixStack class_45872, VertexConsumerProvider class_45972, Identifier class_29602, boolean bl) {
        if (class_13062 == Arm.LEFT) {
            FlyingItemEntityRenderState.renderLeftArm(class_45872, class_45972, 0xF000F0, class_29602, bl);
        } else {
            FlyingItemEntityRenderState.renderRightArm(class_45872, class_45972, 0xF000F0, class_29602, bl);
        }
    }

    private void submitVertexConsumer(VertexConsumerState vertexConsumerState, Consumer<VertexConsumerProvider.Immediate> consumer) {
        VertexConsumerProvider.Immediate class_45982 = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
        class_45982.draw();
        renderingHands = true;
        boolean passStarted = false;
        try {
            vertexConsumerState.projectionRenderTarget.beginPass(true);
            passStarted = true;
            try {
                consumer.accept(class_45982);
                class_45982.draw();
                vertexConsumerState.projectionActive = true;
            }
            catch (Exception exception) {
                this.handleRenderException("capture failed", exception);
            }
            finally {
                if (passStarted) {
                    vertexConsumerState.projectionRenderTarget.endPass();
                }
            }
        }
        finally {
            renderingHands = false;
        }
    }

    private VertexConsumerState getActiveVertexState() {
        if (this.editingHand != null) {
            return this.getHandVertexState(this.editingHand);
        }
        Vector2f class_56112 = UiUtils.mousePosition();
        return this.selectVertexState(class_56112.getX(), class_56112.getY());
    }

    public boolean isChatDragPositionValid(float f, float f2, float f3) {
        Object object;
        if (!this.isChatEditReady() || f3 == 0.0f) {
            return false;
        }
        Arm class_13062 = this.editingHand;
        if (class_13062 == null) {
            object = this.selectVertexState(f, f2);
            if (object == null || this.isChatPositionValid(f, f2)) {
                return false;
            }
            class_13062 = object == this.mainHandState ? Arm.RIGHT : Arm.LEFT;
        }
        object = this.getHandScale(class_13062);
        ((NumberSetting)object).updateValue(((NumberSetting)object).getValue() + f3 * ((NumberSetting)object).getStep() * 2.0f);
        return true;
    }

    private void updateChatOffset(float f, float f2) {
        if (this.chatScale <= 0.001f || this.chatOpacity <= 0.001f) {
            return;
        }
        this.getHandTranslation(this.editingHand).updateValue(this.initialOffsetX + (f - this.dragStartX) / this.chatScale, this.initialOffsetY + (f2 - this.dragStartY) / this.chatOpacity);
    }

    private void renderVertexState(VertexConsumerState vertexConsumerState, boolean bl) {
        float f = vertexConsumerState.animation.update(bl && vertexConsumerState.isProjectionVisibleRecently() ? 1.0f : 0.0f);
        if (f <= 0.01f || !vertexConsumerState.projectionActive) {
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        boolean bl2 = GL11.glIsEnabled((int)2929);
        this.renderChatElement(vertexConsumerState.projectionRenderTarget);
        RenderSystem.setShaderTexture((int)0, (int)vertexConsumerState.projectionRenderTarget.getColorAttachment());
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(0.4f * f));
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, INSTANCE.width(), INSTANCE.height());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        if (bl2) {
            RenderSystem.enableDepthTest();
        }
    }

    private void renderChatElement(moscow.rockstar.render.target.RenderTarget renderTarget) {
        renderTarget.beginWrite(true);
        RenderSystem.enableBlend();
        GlStateManager._blendFuncSeparate((int)772, (int)0, (int)0, (int)1);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        float f = INSTANCE.width();
        float f2 = INSTANCE.height();
        int n = ColorRGBA.WHITE.getRGB();
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(0.0f, 0.0f, 0.0f).color(n);
        class_2872.vertex(0.0f, f2, 0.0f).color(n);
        class_2872.vertex(f, f2, 0.0f).color(n);
        class_2872.vertex(f, 0.0f, 0.0f).color(n);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        RenderSystem.defaultBlendFunc();
        minecraftClient.getFramebuffer().beginWrite(true);
    }

    private VertexConsumerState selectVertexState(double d, double d2) {
        boolean bl = this.mainHandState.isScreenPointInsideProjection((float)d, (float)d2);
        boolean bl2 = this.offHandState.isScreenPointInsideProjection((float)d, (float)d2);
        if (bl && bl2) {
            return this.mainHandState.distanceToProjectionCenter(d, d2) <= this.offHandState.distanceToProjectionCenter(d, d2) ? this.mainHandState : this.offHandState;
        }
        if (bl) {
            return this.mainHandState;
        }
        return bl2 ? this.offHandState : null;
    }

    private boolean isChatPositionValid(float f, float f2) {
        if (f2 > INSTANCE.height() - 16.0f) {
            return true;
        }
        HudElementRegistry hudElementRegistry = RockstarClient.create().getHudElementRegistry();
        if (hudElementRegistry == null) {
            return false;
        }
        for (ColorPickerScreen popup : hudElementRegistry.popups()) {
            if (!popup.isOpen() || !popup.contains(f, f2)) continue;
            return true;
        }
        for (HudElement hudElement : hudElementRegistry.elements()) {
            if (!hudElement.isDragging() && (!hudElement.isShowing() || !hudElement.isHovered(f, f2))) continue;
            return true;
        }
        return false;
    }

    public boolean isViewModelReady() {
        return this.isEnabled() && this.chatEdit.isEnabled() && ViewModel.minecraftClient.currentScreen instanceof ChatScreen;
    }

    boolean isChatEditReady() {
        return this.isViewModelReady();
    }

    private VertexConsumerState getHandVertexState(Arm class_13062) {
        return class_13062 == Arm.RIGHT ? this.mainHandState : this.offHandState;
    }

    private Vector2Setting getHandTranslation(Arm class_13062) {
        return class_13062 == Arm.RIGHT ? this.mainTranslateX : this.offTranslateX;
    }

    private NumberSetting getHandScale(Arm class_13062) {
        return class_13062 == Arm.RIGHT ? this.sizeRight : this.sizeLeft;
    }

    static {
        COSINE_TABLE = new float[16];
        SINE_TABLE = new float[16];
        for (int i = 0; i < 16; ++i) {
            double d = Math.PI * 2 * (double)i / 16.0;
            ViewModel.COSINE_TABLE[i] = (float)Math.cos(d);
            ViewModel.SINE_TABLE[i] = (float)Math.sin(d);
        }
    }
}
