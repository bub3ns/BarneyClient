/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  moscow.rockstar.render.esp.TargetRenderModule
 *  moscow.rockstar.render.targets.FramebufferManager
 *  moscow.rockstar.render.targets.FramebufferTarget
 *  moscow.rockstar.render.targets.RenderTargetState
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.LivingEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.BlockState
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.MatrixStack
 *  net.minecraft.VertexConsumerProvider
 *  net.minecraft.VertexConsumerProvider$Immediate
 *  net.minecraft.AbstractClientPlayerEntity
 *  net.minecraft.HeldItemRenderer
 *  net.minecraft.BlockRenderManager
 *  net.minecraft.ModelTransformationMode
 *  net.minecraft.BuiltBuffer
 */
package moscow.rockstar.modules.visuals.esp.entities;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.visuals.esp.targeting.ItemTargetType;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.esp.TargetRenderModule;
import moscow.rockstar.render.item.HeldItemRenderCapture;
import moscow.rockstar.render.postprocess.HeatHazeRenderer;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.target.RenderTarget;
import moscow.rockstar.render.target.RenderTargetState;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorRangeSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.client.render.BuiltBuffer;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public class Flame
extends TargetRenderModule
implements ClientAccess {
    public static boolean itemRenderInProgress;
    private static final int FULL_BRIGHT_LIGHT = 0xF000F0;
    private static final int FLAME_SEGMENT_COUNT = 24;
    private static final ColorRGBA DEFAULT_FLAME_COLOR;
    private final BooleanSetting enabledSetting = this.createSetting("esp.flame");
    private final NumberSetting strengthSetting = new NumberSetting((SettingOwner)((Object)this), "esp.flame.strength").setMinValue(1.0f).setMaxValue(5.0f).setStep(1.0f).setValue(3.0f);
    private final NumberSetting riseSpeedSetting = new NumberSetting((SettingOwner)((Object)this), "esp.flame.rise_speed").setMinValue(0.5f).setMaxValue(4.0f).setStep(0.1f).setValue(2.5f);
    private final NumberSetting wobbleSetting = new NumberSetting((SettingOwner)((Object)this), "esp.flame.wobble").setMinValue(0.0f).setMaxValue(2.0f).setStep(0.1f).setValue(2.5f);
    private final NumberSetting fadeRateSetting = new NumberSetting((SettingOwner)((Object)this), "esp.flame.fade_rate").setMinValue(0.0f).setMaxValue(80.0f).setStep(1.0f).setValue(50.0f);
    private final NumberSetting intensitySetting = new NumberSetting((SettingOwner)((Object)this), "esp.flame.intensity").setMinValue(0.5f).setMaxValue(4.0f).setStep(0.1f).setValue(2.0f);
    private final BooleanSetting distortionSetting = new BooleanSetting((SettingOwner)((Object)this), "esp.flame.distortion");
    private final NumberSetting distortionStrengthSetting = new NumberSetting((SettingOwner)((Object)this), "esp.flame.distortion_strength", () -> !this.distortionSetting.isEnabled()).setMinValue(0.1f).setMaxValue(3.0f).setStep(0.1f).setValue(1.0f);
    private final BooleanSetting useItemColorSetting = new BooleanSetting((SettingOwner)((Object)this), "esp.flame.item_color");
    private final BooleanSetting syncThemeSetting = new BooleanSetting((SettingOwner)((Object)this), "theme.sync", this.useItemColorSetting::isEnabled);
    private final BooleanSetting gradientSetting = new BooleanSetting((SettingOwner)((Object)this), "esp.flame.gradient", () -> this.useItemColorSetting.isEnabled() || this.syncThemeSetting.isEnabled());
    private final ColorRangeSetting gradientColorSetting = new ColorRangeSetting((SettingOwner)((Object)this), "esp.flame.gradient_color", () -> this.useItemColorSetting.isEnabled() || this.syncThemeSetting.isEnabled() || !this.gradientSetting.isEnabled()).setColorRange(new ColorRGBA(255.0f, 220.0f, 60.0f, 255.0f), new ColorRGBA(255.0f, 60.0f, 0.0f, 255.0f));
    private final ColorSetting flameColorSetting = new ColorSetting((SettingOwner)((Object)this), "esp.flame.color", () -> this.useItemColorSetting.isEnabled() || this.syncThemeSetting.isEnabled() || this.gradientSetting.isEnabled()).setColor(new ColorRGBA(255.0f, 110.0f, 30.0f, 255.0f));
    private final RenderTarget itemRenderTarget = new RenderTarget(true).enableLinearFiltering().setResolutionScale(1.0f);
    private final RenderTarget flameRenderTarget = new RenderTarget(true).enableLinearFiltering().setResolutionScale(1.0f);
    private final RenderTarget wobbleRenderTarget = new RenderTarget(true).enableLinearFiltering().setResolutionScale(1.0f);
    private final RenderTarget overlayRenderTarget = new RenderTarget(true).enableLinearFiltering().setResolutionScale(1.0f);
    private final RenderTargetState flameRenderState = RenderTargetState.create();
    private final HeatHazeRenderer distortionRenderer = new HeatHazeRenderer();
    private boolean itemOrBlockRendered = false;
    private boolean flameVisible = false;
    private final EventListener<Render3DEvent> render3DEventListener = render3DEvent -> {
        boolean bl;
        this.itemOrBlockRendered = false;
        boolean bl2 = bl = Flame.minecraftClient.options != null && Flame.minecraftClient.options.getPerspective() != null && Flame.minecraftClient.options.getPerspective().isFirstPerson();
        if (!this.isValid2(ItemTargetType.HELD) || !bl) {
            if (this.flameVisible) {
                this.flameRenderTarget.beginPass(true);
                this.flameRenderTarget.endPass();
                this.flameVisible = false;
            }
            return;
        }
        this.itemRenderTarget.beginPass(true);
        this.itemRenderTarget.endPass();
    };
    private final EventListener<PreHudRenderEvent> preHudRenderEventListener = preHudRenderEvent -> {
        RenderTarget framebufferTarget;
        int n;
        float f;
        float f2;
        if (!this.isValid2(ItemTargetType.HELD)) {
            return;
        }
        if (this.flameVisible) {
            this.renderAnimatedFlameTexture();
            f2 = Math.max(0.0f, Math.min(1.0f, this.fadeRateSetting.getValue() / 100.0f));
            f = 0.94f + f2 * 0.055f;
            if (!this.itemOrBlockRendered) {
                f += (1.0f - f) * 0.5f;
            }
            this.renderFadeOverlay(f);
        }
        if (this.itemOrBlockRendered) {
            if (this.gradientSetting.isEnabled() && !this.useItemColorSetting.isEnabled() && !this.syncThemeSetting.isEnabled()) {
                this.renderColorOverlay(DEFAULT_FLAME_COLOR);
            } else {
                ColorRGBA colorRGBA = this.getFlameColor();
                if (colorRGBA != null) {
                    this.renderColorOverlay(colorRGBA);
                }
            }
            this.renderFlameTexture();
            this.flameVisible = true;
        }
        if (!this.flameVisible) {
            return;
        }
        if (this.distortionSetting.isEnabled() && (n = this.flameRenderTarget.getColorAttachment()) != 0) {
            f = (float)minecraftClient.getWindow().getScaledWidth() / (float)Math.max(1, minecraftClient.getWindow().getScaledHeight());
            float f3 = (float)(System.currentTimeMillis() % 100000L) / 1000.0f;
            this.distortionRenderer.renderHeatHaze(n, f, this.distortionStrengthSetting.getValue(), f3);
        }
        f2 = this.intensitySetting.getValue();
        if (this.itemOrBlockRendered) {
            this.renderUnderlyingOverlay();
            framebufferTarget = this.overlayRenderTarget;
        } else {
            framebufferTarget = this.flameRenderTarget;
        }
        if (f2 <= 0.01f) {
            return;
        }
        this.flameRenderState.setSamplesPerPass(Math.max(1, (int)(this.strengthSetting.getValue() / 2.0f)));
        // VERIFIED AGAINST BYTECODE - do not "upgrade" this to apply(...).
        // rockstar/ilIlil/iIIIIIII#I (Lpyrock/events/render/PreHudRenderEvent;)V
        // calls rockstar/ilIlil/iIiiI#i (Lrockstar/ilIlil/iIiiIIiII;)V, which
        // delegates to i(target,-1,-1,-1,-1): horizontal + vertical ping-pong
        // blur only. It never touches the third (composite) supplier and never
        // binds the glow/composite shader - that is the separate overload
        // iIiiI#I (Lrockstar/ilIlil/iIiiIIiII;)V, which Glow uses.
        this.flameRenderState.applyBlurOnly(framebufferTarget);
        // rockstar/ilIlil/IiiiiIii#I (Lrockstar/ilIlil/iIiiIIiII;)V. This is
        // one of the two passes that put the flame BEHIND the hand: it binds
        // the blur output and draws the shared arm-mask capture with
        // blendFuncSeparate(ZERO, ONE_MINUS_SRC_ALPHA, ZERO, ONE_MINUS_SRC_ALPHA),
        // multiplying the flame's RGB *and* alpha by (1 - maskAlpha). The final
        // composite below is additive weighted by that alpha, so the hole reads
        // as the hand occluding the flame. The other pass is
        // renderUnderlyingOverlay()'s 0.45-alpha item punch.
        HeldItemRenderCapture.renderOverlay(this.flameRenderState.getBlurOutputTarget());
        // iIiiI#i ()I -> the vertical (pong) target's texture, i.e. the same
        // target that was just punched. Not the composite target.
        int n2 = this.flameRenderState.getVerticalTextureId();
        if (n2 != 0) {
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.setShaderTexture((int)0, (int)n2);
            RenderSystem.setShaderColor((float)f2, (float)f2, (float)f2, (float)1.0f);
            ColorRGBA colorRGBA = this.gradientColorSetting.getColorRangeSettingColorRGBA();
            ColorRGBA colorRGBA2 = this.gradientColorSetting.getSecondColor();
            if (this.gradientSetting.isEnabled() && !this.useItemColorSetting.isEnabled() && !this.syncThemeSetting.isEnabled() && colorRGBA != null && colorRGBA2 != null) {
                Flame.renderGradientOverlay(0.0f, 0.0f, minecraftClient.getWindow().getScaledWidth(), minecraftClient.getWindow().getScaledHeight(), Flame.toArgb(colorRGBA), Flame.toArgb(colorRGBA2));
            } else {
                ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, minecraftClient.getWindow().getScaledWidth(), minecraftClient.getWindow().getScaledHeight());
            }
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
    };
    private static final int COLOR_CHANNEL_COUNT = 4;
    private static final float FLAME_SCROLL_SPEED = 80.0f;

    public Flame() {
        super("flame", new ItemTargetType[]{ItemTargetType.HELD}, new TargetGroup[]{TargetGroup.ITEMS});
        RenderTargetState.initialize();
        this.flameRenderState.setBlurPasses(3);
        this.flameRenderState.setBlurRadius(2.2f);
        this.distortionRenderer.initializeHeatHazeShader();
    }

    public ColorRGBA getFlameColor() {
        if (this.useItemColorSetting.isEnabled()) {
            return null;
        }
        return this.syncThemeSetting.isEnabled() ? ColorPalette.getAccentColor() : this.flameColorSetting.getColor();
    }

    public void renderItemWithFlame(HeldItemRenderer Icon, AbstractClientPlayerEntity TrackedPosition, ItemStack class_17992, ModelTransformationMode DeathMessageType, boolean bl, MatrixStack class_45872, int n) {
        if (!this.isValid2(ItemTargetType.HELD)) {
            return;
        }
        if (class_17992 == null || class_17992.isEmpty()) {
            return;
        }
        itemRenderInProgress = true;
        try {
            this.itemRenderTarget.beginPass(false);
            try {
                VertexConsumerProvider.Immediate class_45982 = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
                try {
                    Icon.renderItem((LivingEntity)TrackedPosition, class_17992, DeathMessageType, bl, class_45872, (VertexConsumerProvider)class_45982, 0xF000F0);
                    class_45982.draw();
                    this.itemOrBlockRendered = true;
                }
                catch (Exception exception) {
                    RockstarClient.LOGGER.error("[ESP/Flame] held-item capture failed", exception);
                }
            }
            finally {
                this.itemRenderTarget.endPass();
            }
        }
        finally {
            itemRenderInProgress = false;
        }
    }

    public void renderBlockWithFlame(BlockRenderManager class_7762, BlockState class_26802, MatrixStack class_45872, int n) {
        if (!this.isValid2(ItemTargetType.HELD)) {
            return;
        }
        if (class_26802 == null) {
            return;
        }
        itemRenderInProgress = true;
        try {
            this.itemRenderTarget.beginPass(false);
            try {
                VertexConsumerProvider.Immediate class_45982 = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
                try {
                    class_7762.renderBlockAsEntity(class_26802, class_45872, (VertexConsumerProvider)class_45982, 0xF000F0, n);
                    class_45982.draw();
                    this.itemOrBlockRendered = true;
                }
                catch (Exception exception) {
                    RockstarClient.LOGGER.error("[ESP/Flame] held-block capture failed", exception);
                }
            }
            finally {
                this.itemRenderTarget.endPass();
            }
        }
        finally {
            itemRenderInProgress = false;
        }
    }

    private void renderAnimatedFlameTexture() {
        float f = minecraftClient.getWindow().getScaledWidth();
        float f2 = minecraftClient.getWindow().getScaledHeight();
        float f3 = this.riseSpeedSetting.getValue();
        float f4 = this.wobbleSetting.getValue();
        float f5 = (float)(System.currentTimeMillis() % 100000L) / 1000.0f;
        this.wobbleRenderTarget.beginPass(true);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShaderTexture((int)0, (int)this.flameRenderTarget.getColorAttachment());
        for (int i = 0; i < 24; ++i) {
            float f6 = (float)i / 24.0f;
            float f7 = (float)(i + 1) / 24.0f;
            float f8 = f6 * f2 - f3;
            float f9 = f7 * f2 - f3;
            float f10 = 1.0f - f6;
            float f11 = 1.0f - f7;
            float f12 = (float)i * 0.45f;
            float f13 = (float)Math.sin(f5 * 4.5f + f12) * f4 + (float)Math.sin(f5 * 1.7f + f12 * 2.1f) * (f4 * 0.5f) + (float)Math.sin(f5 * 7.3f + f12 * 3.7f) * (f4 * 0.32f) + (float)Math.sin(f5 * 2.3f + f12 * 1.3f) * (f4 * 0.45f) + (float)Math.sin(f5 * 0.61f + f12 * 0.7f) * (f4 * 0.35f);
            BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            class_2872.vertex(f13, f8, 0.0f).texture(0.0f, f10).color(-1);
            class_2872.vertex(f13, f9, 0.0f).texture(0.0f, f11).color(-1);
            class_2872.vertex(f + f13, f9, 0.0f).texture(1.0f, f11).color(-1);
            class_2872.vertex(f + f13, f8, 0.0f).texture(1.0f, f10).color(-1);
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        }
        RenderSystem.setShaderTexture((int)0, (int)0);
        this.flameRenderTarget.beginPass(true);
        RenderSystem.disableBlend();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShaderTexture((int)0, (int)this.wobbleRenderTarget.getColorAttachment());
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, f, f2);
        RenderSystem.setShaderTexture((int)0, (int)0);
        minecraftClient.getFramebuffer().beginWrite(true);
    }

    private void renderFadeOverlay(float f) {
        this.flameRenderTarget.beginPass(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.SRC_ALPHA);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        int n = Math.round(f * 255.0f) & 0xFF;
        int n2 = n << 24;
        float f2 = minecraftClient.getWindow().getScaledWidth();
        float f3 = minecraftClient.getWindow().getScaledHeight();
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(0.0f, 0.0f, 0.0f).color(n2);
        class_2872.vertex(0.0f, f3, 0.0f).color(n2);
        class_2872.vertex(f2, f3, 0.0f).color(n2);
        class_2872.vertex(f2, 0.0f, 0.0f).color(n2);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        RenderSystem.defaultBlendFunc();
        minecraftClient.getFramebuffer().beginWrite(true);
    }

    private void renderFlameTexture() {
        this.flameRenderTarget.beginPass(false);
        RenderSystem.enableBlend();
        GlStateManager._blendFuncSeparate((int)770, (int)771, (int)1, (int)771);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShaderTexture((int)0, (int)this.itemRenderTarget.getColorAttachment());
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, minecraftClient.getWindow().getScaledWidth(), minecraftClient.getWindow().getScaledHeight());
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.defaultBlendFunc();
        minecraftClient.getFramebuffer().beginWrite(true);
    }

    private void renderUnderlyingOverlay() {
        this.overlayRenderTarget.beginPass(true);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShaderTexture((int)0, (int)this.flameRenderTarget.getColorAttachment());
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, minecraftClient.getWindow().getScaledWidth(), minecraftClient.getWindow().getScaledHeight());
        if (this.itemOrBlockRendered) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)0.45f);
            RenderSystem.setShaderTexture((int)0, (int)this.itemRenderTarget.getColorAttachment());
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, minecraftClient.getWindow().getScaledWidth(), minecraftClient.getWindow().getScaledHeight());
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.setShaderTexture((int)0, (int)0);
        minecraftClient.getFramebuffer().beginWrite(true);
    }

    private void renderColorOverlay(ColorRGBA colorRGBA) {
        this.itemRenderTarget.beginWrite(true);
        RenderSystem.enableBlend();
        GlStateManager._blendFuncSeparate((int)772, (int)0, (int)0, (int)1);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        int n = Flame.toPremultipliedArgb(colorRGBA);
        float f = minecraftClient.getWindow().getScaledWidth();
        float f2 = minecraftClient.getWindow().getScaledHeight();
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        class_2872.vertex(0.0f, 0.0f, 0.0f).color(n);
        class_2872.vertex(0.0f, f2, 0.0f).color(n);
        class_2872.vertex(f, f2, 0.0f).color(n);
        class_2872.vertex(f, 0.0f, 0.0f).color(n);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        RenderSystem.defaultBlendFunc();
        minecraftClient.getFramebuffer().beginWrite(true);
    }

    private static void renderGradientOverlay(float f, float f2, float f3, float f4, int n, int n2) {
        float f5 = f4 / 4.0f;
        float f6 = 2.0f * f5;
        float f7 = (float)(System.currentTimeMillis() % 1000000L) / 1000.0f;
        float f8 = f7 * 80.0f % f6;
        int n3 = 2;
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (int i = -n3; i < 4 + n3; ++i) {
            boolean bl = Math.floorMod(i, 2) == 0;
            int n4 = bl ? n : n2;
            int n5 = bl ? n2 : n;
            float f9 = f2 + (float)i * f5 - f8;
            float f10 = f9 + f5;
            float f11 = 1.0f - (f9 - f2) / f4;
            float f12 = 1.0f - (f10 - f2) / f4;
            class_2872.vertex(f, f9, 0.0f).texture(0.0f, f11).color(n4);
            class_2872.vertex(f, f10, 0.0f).texture(0.0f, f12).color(n5);
            class_2872.vertex(f + f3, f10, 0.0f).texture(1.0f, f12).color(n5);
            class_2872.vertex(f + f3, f9, 0.0f).texture(1.0f, f11).color(n4);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
    }

    private static int toPremultipliedArgb(ColorRGBA colorRGBA) {
        float f = Math.max(0.0f, Math.min(1.0f, colorRGBA.getAlpha() / 255.0f));
        int n = Math.round(colorRGBA.getRed() * f) & 0xFF;
        int n2 = Math.round(colorRGBA.getGreen() * f) & 0xFF;
        int n3 = Math.round(colorRGBA.getBlue() * f) & 0xFF;
        return 0xFF000000 | n << 16 | n2 << 8 | n3;
    }

    private static int toArgb(ColorRGBA colorRGBA) {
        int n = Math.round(colorRGBA.getRed()) & 0xFF;
        int n2 = Math.round(colorRGBA.getGreen()) & 0xFF;
        int n3 = Math.round(colorRGBA.getBlue()) & 0xFF;
        return 0xFF000000 | n << 16 | n2 << 8 | n3;
    }

    @Generated
    public BooleanSetting getEnabledSetting() {
        return this.enabledSetting;
    }

    @Generated
    public NumberSetting getStrengthSetting() {
        return this.strengthSetting;
    }

    @Generated
    public NumberSetting getRiseSpeedSetting() {
        return this.riseSpeedSetting;
    }

    @Generated
    public NumberSetting getWobbleSetting() {
        return this.wobbleSetting;
    }

    @Generated
    public NumberSetting getFadeRateSetting() {
        return this.fadeRateSetting;
    }

    @Generated
    public NumberSetting getIntensitySetting() {
        return this.intensitySetting;
    }

    @Generated
    public BooleanSetting getDistortionSetting() {
        return this.distortionSetting;
    }

    @Generated
    public NumberSetting getDistortionStrengthSetting() {
        return this.distortionStrengthSetting;
    }

    @Generated
    public BooleanSetting getUseItemColorSetting() {
        return this.useItemColorSetting;
    }

    @Generated
    public BooleanSetting getSyncThemeSetting() {
        return this.syncThemeSetting;
    }

    @Generated
    public BooleanSetting getGradientSetting() {
        return this.gradientSetting;
    }

    @Generated
    public ColorRangeSetting getGradientColorSetting() {
        return this.gradientColorSetting;
    }

    @Generated
    public ColorSetting getFlameColorSetting() {
        return this.flameColorSetting;
    }

    @Generated
    public RenderTarget getItemRenderTarget() {
        return this.itemRenderTarget;
    }

    @Generated
    public RenderTarget getFlameRenderTarget() {
        return this.flameRenderTarget;
    }

    @Generated
    public RenderTarget getWobbleRenderTarget() {
        return this.wobbleRenderTarget;
    }

    @Generated
    public RenderTarget getOverlayRenderTarget() {
        return this.overlayRenderTarget;
    }

    @Generated
    public RenderTargetState getFlameRenderState() {
        return this.flameRenderState;
    }

    @Generated
    public HeatHazeRenderer getDistortionRenderer() {
        return this.distortionRenderer;
    }

    @Generated
    public boolean isItemOrBlockRendered() {
        return this.itemOrBlockRendered;
    }

    @Generated
    public boolean isFlameVisible() {
        return this.flameVisible;
    }

    @Generated
    public EventListener<Render3DEvent> getRender3DEventListener() {
        return this.render3DEventListener;
    }

    @Generated
    public EventListener<PreHudRenderEvent> getPreHudRenderEventListener() {
        return this.preHudRenderEventListener;
    }

    static {
        DEFAULT_FLAME_COLOR = new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f);
    }
}
