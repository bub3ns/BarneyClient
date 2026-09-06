/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  moscow.rockstar.render.esp.TargetRenderModule
 *  moscow.rockstar.render.targets.FramebufferManager
 *  moscow.rockstar.render.targets.FramebufferTarget
 *  moscow.rockstar.render.targets.RenderTargetState
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.ProjectionType
 *  net.minecraft.LivingEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.BlockState
 *  net.minecraft.Framebuffer
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
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 */
package moscow.rockstar.modules.visuals.esp.entities;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.visuals.esp.targeting.ItemTargetType;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.esp.TargetRenderModule;
import moscow.rockstar.render.item.HeldItemRenderCapture;
import moscow.rockstar.render.shaders.ItemEffectShader;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.target.RenderTarget;
import moscow.rockstar.render.target.RenderTargetManager;
import moscow.rockstar.render.target.RenderTargetState;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import com.mojang.blaze3d.systems.ProjectionType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.Framebuffer;
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
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public class Fill
extends TargetRenderModule
implements ClientAccess {
    public static boolean fillRendering;
    private static final int FULL_BRIGHT_LIGHT = 0xF000F0;
    private static final float COLOR_INTENSITY_SCALE = 2.4f;
    private final BooleanSetting fillEnabled = this.createSetting("esp.fill");
    private final ModeSetting renderMode = new ModeSetting((SettingOwner)((Object)this), "esp.fill.mode");
    private final ModeSetting.Option mirrorMode = new ModeSetting.Option(this.renderMode, "esp.fill.mode.mirror");
    private final ModeSetting.Option shaderMode = new ModeSetting.Option(this.renderMode, "esp.fill.mode.shader");
    private final BooleanSetting themeSync = new BooleanSetting((SettingOwner)((Object)this), "theme.sync");
    private final ColorSetting fillColor = new ColorSetting((SettingOwner)((Object)this), "esp.fill.color", this.themeSync::isEnabled).setColor(new ColorRGBA(255.0f, 60.0f, 60.0f, 255.0f));
    private final BooleanSetting flatMirror = new BooleanSetting((SettingOwner)((Object)this), "esp.fill.mirror_flat", () -> this.renderMode.getSelectedOption() != this.mirrorMode);
    private final NumberSetting mirrorAlpha = new NumberSetting((SettingOwner)((Object)this), "esp.fill.mirror_alpha", () -> this.renderMode.getSelectedOption() != this.mirrorMode).setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f).setValue(100.0f);
    private final NumberSetting mirrorBlur = new NumberSetting((SettingOwner)((Object)this), "esp.fill.mirror_blur", () -> this.renderMode.getSelectedOption() != this.mirrorMode).setMinValue(0.0f).setMaxValue(10.0f).setStep(1.0f).setValue(0.0f);
    private final NumberSetting mirrorAmbient = new NumberSetting((SettingOwner)((Object)this), "esp.fill.mirror_ambient", () -> this.renderMode.getSelectedOption() != this.mirrorMode || this.flatMirror.isEnabled()).setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f).setValue(35.0f);
    private final ModeSetting shaderType = new ModeSetting((SettingOwner)((Object)this), "esp.fill.shader", () -> this.renderMode.getSelectedOption() != this.shaderMode);
    private final ModeSetting.Option causticShader = new ModeSetting.Option(this.shaderType, "esp.fill.shader.caustic");
    private final ModeSetting.Option plasmaShader = new ModeSetting.Option(this.shaderType, "esp.fill.shader.plasma");
    private final ModeSetting.Option lavaShader = new ModeSetting.Option(this.shaderType, "esp.fill.shader.lava");
    private final NumberSetting shaderSpeed = new NumberSetting((SettingOwner)((Object)this), "esp.fill.shader_speed", () -> this.renderMode.getSelectedOption() != this.shaderMode).setMinValue(0.0f).setMaxValue(0.5f).setStep(0.01f).setValue(0.25f);
    private final NumberSetting shaderAlpha = new NumberSetting((SettingOwner)((Object)this), "esp.fill.shader_alpha", () -> this.renderMode.getSelectedOption() != this.shaderMode).setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f).setValue(100.0f);
    private static final int RENDER_TARGET_COUNT = 2;
    private final RenderTarget[] renderTargets = new RenderTarget[]{new RenderTarget(true).enableLinearFiltering().setResolutionScale(1.0f), new RenderTarget(true).enableLinearFiltering().setResolutionScale(1.0f)};
    private final RenderTarget framebufferCopyTarget = new RenderTarget(false).enableLinearFiltering().setResolutionScale(0.5f);
    private final RenderTarget mainFramebufferTarget = new RenderTarget(false).enableLinearFiltering().setResolutionScale(1.0f);
    private boolean mainFramebufferReady = false;
    private final RenderTargetState blurProcessor = RenderTargetState.create();
    private final boolean[] renderTargetCaptured = new boolean[2];
    private final boolean[] renderTargetInitialized = new boolean[2];
    private final float[] projectedTargetX = new float[]{0.5f, 0.5f};
    private final float[] projectedTargetY = new float[]{0.5f, 0.5f};
    private final EventListener<Render3DEvent> render3DListener = render3DEvent -> {
        boolean bl;
        Arrays.fill(this.renderTargetCaptured, false);
        Arrays.fill(this.renderTargetInitialized, false);
        this.mainFramebufferReady = false;
        boolean bl2 = bl = Fill.minecraftClient.options != null && Fill.minecraftClient.options.getPerspective() != null && Fill.minecraftClient.options.getPerspective().isFirstPerson();
        if (!this.isValid2(ItemTargetType.HELD) || !bl) {
            return;
        }
        if (this.renderMode.getSelectedOption() == this.mirrorMode) {
            this.copyMainFramebuffer();
        }
    };
    private final EventListener<PreHudRenderEvent> preHudRenderListener = preHudRenderEvent -> {
        if (!this.isValid2(ItemTargetType.HELD)) {
            return;
        }
        boolean bl = this.renderMode.getSelectedOption() == this.shaderMode;
        for (int i = 0; i < 2; ++i) {
            if (!this.renderTargetCaptured[i]) continue;
            HeldItemRenderCapture.renderOverlay(this.renderTargets[i]);
            if (bl) {
                this.renderShaderTarget(i);
                continue;
            }
            this.renderMirrorTarget(i);
        }
    };

    public Fill() {
        super("fill", new ItemTargetType[]{ItemTargetType.HELD}, new TargetGroup[]{TargetGroup.ITEMS});
        this.mirrorMode.select();
        this.causticShader.select();
        for (RenderTarget framebufferTarget : this.renderTargets) {
            framebufferTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        }
        RenderTargetState.initialize();
        this.blurProcessor.setSamplesPerPass(1);
        this.blurProcessor.setBlurRadius(2.0f);
    }

    public ColorRGBA getFillColor() {
        return this.themeSync.isEnabled() ? ColorPalette.getAccentColor() : this.fillColor.getColor();
    }

    /**
     * 1:1 restoration of {@code rockstar/ilIlil/Iiiiiiii#i ()Z}, whose entire body is
     * {@code iconst_0 / ireturn} - an unconditional {@code false}, with no GETFIELD, no INVOKE
     * and no branch.
     *
     * <p>The sole caller is {@code HeldItemRendererMixin#rockstar$decorateHeldItem}, which
     * reproduces the original mixin's {@code blocked = fill != null && fill.i();
     * if (!blocked) operation.call(...)} shape. With a constant {@code false} the vanilla
     * {@code ItemRenderer.renderItem} pass ALWAYS runs and the fill composite paints over it,
     * exactly as in the original.</p>
     *
     * <p>This previously carried a "deliberate deviation" that suppressed the vanilla pass whenever
     * the opacity slider read above {@code 0.1f}. That was a bug, not a deviation: the sliders bound
     * in {@code Iiiiiiii#<init>} ({@code "esp.fill.mirror_alpha"} / {@code "esp.fill.shader_alpha"},
     * min 0 / max 100 / step 1 / default 100) are 0..100 percentages - {@code Iiiiiiii#I (I)V}
     * divides by 100 before use - so {@code opacity > 0.1f} was true at every slider step >= 1.
     * The vanilla item therefore never drew underneath the composite, and lowering the alpha faded
     * the mirror toward the world behind the item instead of back toward the item's own texture,
     * which is why the opacity slider appeared to do nothing. Do not reintroduce it.</p>
     */
    /**
     * DELIBERATE DEVIATION - USER-REQUESTED. Do NOT "restore" this to {@code return false}.
     *
     * <p>The original {@code rockstar/ilIlil/Iiiiiiii#i()Z} really is a hard-coded {@code false}
     * ({@code iconst_0; ireturn}), so the original always runs the vanilla held-item pass and
     * relies on the fill composite painting over it. That is not in dispute. The user asked for
     * this deviation explicitly ("can we somehow just cancel the sword by ourselves") after the
     * composite was found not to cover the item, and confirmed it behaves as intended. An audit
     * that flags this as a 1:1 divergence is CORRECT but must not act on it.</p>
     *
     * <p>The capture is unaffected: {@link #renderHeldItemFill} re-enters
     * {@code HeldItemRenderer.renderItem}, where the {@code rockstar$decoratingHeldItem} guard
     * takes the pass-through branch while this module's render target is bound, so the item is
     * still drawn into the target - just not into the main framebuffer.</p>
     *
     * <p>Units: {@code NumberSetting.getValue()} is a 0..100 percentage here, NOT a 0..1 fraction
     * ({@code esp.fill.mirror_alpha} is min 0 / max 100 / default 100). The {@code > 0.1f}
     * threshold is deliberately the same cutoff the renderers themselves use - both
     * {@link #renderMirrorTarget} and {@link #renderShaderTarget} bail out when
     * {@code clampOpacity(getValue() / 100.0f) <= 0.001f}, i.e. they draw exactly when
     * {@code getValue() > 0.1}. So the vanilla pass is suppressed precisely when the composite
     * will actually paint, and runs whenever it will not. This is not a unit-conversion bug.</p>
     */
    public boolean isFillRenderingBlocked() {
        if (!this.isValid2(ItemTargetType.HELD)) {
            return false;
        }
        if (this.renderMode.getSelectedOption() == this.shaderMode) {
            // Shader modes still fade to nothing at 0 (renderShaderTarget keeps the original's
            // <= 0.001f early-out and composites with shader_alpha as the quad alpha), so the
            // vanilla item has to come back or the held item would vanish with nothing in its
            // place. getValue() is a 0..100 percentage; > 0.1 is exactly the cutoff at which
            // renderShaderTarget starts drawing.
            return this.shaderAlpha.getValue() > 0.1f;
        }
        // Mirror mode now composites at every opacity value (mirror_alpha only tints), so there is
        // never a value at which nothing is drawn - the item stays suppressed throughout.
        return true;
    }

    public void renderHeldItemFill(HeldItemRenderer Icon, AbstractClientPlayerEntity TrackedPosition, ItemStack class_17992, ModelTransformationMode DeathMessageType, boolean bl, MatrixStack class_45872, int n) {
        if (!this.isValid2(ItemTargetType.HELD)) {
            return;
        }
        if (class_17992 == null || class_17992.isEmpty()) {
            return;
        }
        int n2 = this.selectRenderTargetIndex(bl);
        this.updateProjectedTarget(n2, class_45872);
        fillRendering = true;
        try {
            RenderTarget framebufferTarget = this.prepareRenderTarget(n2);
            try {
                VertexConsumerProvider.Immediate class_45982 = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
                try {
                    Icon.renderItem((LivingEntity)TrackedPosition, class_17992, DeathMessageType, bl, class_45872, (VertexConsumerProvider)class_45982, 0xF000F0);
                    class_45982.draw();
                    this.renderTargetCaptured[n2] = true;
                }
                catch (Exception exception) {
                    RockstarClient.LOGGER.error("[ESP/Fill] held-item capture failed", exception);
                }
            }
            finally {
                framebufferTarget.endPass();
            }
        }
        finally {
            fillRendering = false;
        }
    }

    public void renderBlockFill(BlockRenderManager class_7762, BlockState class_26802, MatrixStack class_45872, int n) {
        if (!this.isValid2(ItemTargetType.HELD)) {
            return;
        }
        if (class_26802 == null) {
            return;
        }
        int n2 = this.selectRenderTargetIndex(HeldItemRenderCapture.leftHand);
        this.updateProjectedTarget(n2, class_45872);
        fillRendering = true;
        try {
            RenderTarget framebufferTarget = this.prepareRenderTarget(n2);
            try {
                VertexConsumerProvider.Immediate class_45982 = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
                try {
                    class_7762.renderBlockAsEntity(class_26802, class_45872, (VertexConsumerProvider)class_45982, 0xF000F0, n);
                    class_45982.draw();
                    this.renderTargetCaptured[n2] = true;
                }
                catch (Exception exception) {
                    RockstarClient.LOGGER.error("[ESP/Fill] held-block capture failed", exception);
                }
            }
            finally {
                framebufferTarget.endPass();
            }
        }
        finally {
            fillRendering = false;
        }
    }

    private int selectRenderTargetIndex(boolean bl) {
        return this.renderMode.getSelectedOption() == this.shaderMode && bl ? 1 : 0;
    }

    private RenderTarget prepareRenderTarget(int n) {
        RenderTarget framebufferTarget = this.renderTargets[n];
        framebufferTarget.beginPass(!this.renderTargetInitialized[n]);
        this.renderTargetInitialized[n] = true;
        return framebufferTarget;
    }

    private void updateProjectedTarget(int n, MatrixStack class_45872) {
        try {
            Matrix4f matrix4f = new Matrix4f((Matrix4fc)RenderSystem.getProjectionMatrix()).mul((Matrix4fc)RenderSystem.getModelViewMatrix()).mul((Matrix4fc)class_45872.peek().getPositionMatrix());
            Vector4f vector4f = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
            matrix4f.transform(vector4f);
            if (vector4f.w > 1.0E-4f) {
                this.projectedTargetX[n] = vector4f.x / vector4f.w * 0.5f + 0.5f;
                this.projectedTargetY[n] = vector4f.y / vector4f.w * 0.5f + 0.5f;
            }
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("[ESP/Fill] entity capture failed", exception);
        }
    }

    private void renderMirrorTarget(int n) {
        int n2;
        // DELIBERATE DEVIATION - USER-REQUESTED. The original
        // rockstar/ilIlil/Iiiiiiii#I(I)V bails out here when clampOpacity(mirror_alpha/100)
        // <= 0.001f, and then composites using that same value as the quad's ALPHA - so at 0
        // opacity the mirror, and with it the blur, disappears completely. The user asked for the
        // blur to keep working at 0 opacity, so mirror_alpha now controls only how strongly the
        // fill colour TINTS the mirror: the quad is always composited at full alpha and the tint
        // lerps from white (untinted blurred mirror) at 0 to the original's colour*2.4 at 100.
        // An audit that flags this as a 1:1 divergence is CORRECT but must NOT revert it.
        float tint = Fill.clampOpacity(this.mirrorAlpha.getValue() / 100.0f);
        int n3 = this.mainFramebufferReady ? this.mainFramebufferTarget.getColorAttachment() : minecraftClient.getFramebuffer().getColorAttachment();
        int n4 = (int)this.mirrorBlur.getValue();
        if (n4 > 0) {
            this.renderFramebufferTexture(n3);
            this.blurProcessor.setBlurPasses(n4);
            this.blurProcessor.applyBlurOnly(this.framebufferCopyTarget);
            n2 = this.blurProcessor.getVerticalTextureId();
            if (n2 == 0) {
                n2 = n3;
            }
        } else {
            n2 = n3;
        }
        this.captureRenderTarget(n, n2);
        ColorRGBA colorRGBA = this.getFillColor();
        float f2 = Fill.lerpTint(tint, colorRGBA.getRed() / 255.0f * 2.4f);
        float f3 = Fill.lerpTint(tint, colorRGBA.getGreen() / 255.0f * 2.4f);
        float f4 = Fill.lerpTint(tint, colorRGBA.getBlue() / 255.0f * 2.4f);
        this.renderColoredQuad(this.renderTargets[n].getColorAttachment(), f2, f3, f4, 1.0f);
    }

    private void renderShaderTarget(int n) {
        float f = Fill.clampOpacity(this.shaderAlpha.getValue() / 100.0f);
        if (f <= 0.001f) {
            return;
        }
        ModeSetting.Option option = this.shaderType.getSelectedOption();
        ItemEffectShader itemEffectShader = option == this.plasmaShader ? ShaderRenderer.itemPlasmaShader : (option == this.lavaShader ? ShaderRenderer.itemLavaShader : ShaderRenderer.itemCausticShader);
        if (itemEffectShader == null) {
            return;
        }
        float f2 = this.shaderSpeed.getValue();
        float f3 = (float)((double)(System.currentTimeMillis() % 1000000L) / 1000.0 * (double)f2);
        ColorRGBA colorRGBA = this.getFillColor();
        itemEffectShader.bindShaderProgram();
        itemEffectShader.setEffectParameters(f3, colorRGBA, this.projectedTargetX[n], this.projectedTargetY[n]);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture((int)0, (int)this.renderTargets[n].getColorAttachment());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f);
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, minecraftClient.getWindow().getScaledWidth(), minecraftClient.getWindow().getScaledHeight());
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    private void renderColoredQuad(int n, float f, float f2, float f3, float f4) {
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture((int)0, (int)n);
        RenderSystem.setShaderColor((float)f, (float)f2, (float)f3, (float)f4);
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, minecraftClient.getWindow().getScaledWidth(), minecraftClient.getWindow().getScaledHeight());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    private void renderFramebufferTexture(int n) {
        this.framebufferCopyTarget.beginPass(true);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShaderTexture((int)0, (int)n);
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, minecraftClient.getWindow().getScaledWidth(), minecraftClient.getWindow().getScaledHeight());
        RenderSystem.setShaderTexture((int)0, (int)0);
        this.framebufferCopyTarget.endPass();
    }

    private void copyMainFramebuffer() {
        Framebuffer class_2762 = minecraftClient.getFramebuffer();
        if (class_2762 == null) {
            return;
        }
        int n = minecraftClient.getWindow().getScaledWidth();
        int n2 = minecraftClient.getWindow().getScaledHeight();
        RenderSystem.backupProjectionMatrix();
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0f, (float)n, (float)n2, 0.0f, 1000.0f, 21000.0f);
        RenderSystem.setProjectionMatrix((Matrix4f)matrix4f, (ProjectionType)ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.identity().translate(0.0f, 0.0f, -11000.0f);
        this.mainFramebufferTarget.beginPass(true);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShaderTexture((int)0, (int)class_2762.getColorAttachment());
        ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
        RenderSystem.setShaderTexture((int)0, (int)0);
        this.mainFramebufferTarget.endPass();
        matrix4fStack.popMatrix();
        RenderSystem.restoreProjectionMatrix();
        this.mainFramebufferReady = true;
    }

    private void captureRenderTarget(int n, int n2) {
        this.renderTargets[n].beginWrite(true);
        RenderSystem.enableBlend();
        boolean bl = this.flatMirror.isEnabled();
        if (bl) {
            GlStateManager._blendFuncSeparate((int)772, (int)0, (int)0, (int)1);
        } else {
            GlStateManager._blendFuncSeparate((int)774, (int)0, (int)0, (int)1);
        }
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        if (!bl && ShaderRenderer.mirrorCompositeShader != null) {
            ShaderRenderer.mirrorCompositeShader.bindShaderProgram();
            ShaderRenderer.mirrorCompositeShader.setFloor(Fill.clampOpacity(this.mirrorAmbient.getValue() / 100.0f));
        } else {
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShaderTexture((int)0, (int)n2);
        float f = minecraftClient.getWindow().getScaledWidth();
        float f2 = minecraftClient.getWindow().getScaledHeight();
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        class_2872.vertex(0.0f, 0.0f, 0.0f).texture(1.0f, 1.0f).color(-1);
        class_2872.vertex(0.0f, f2, 0.0f).texture(1.0f, 0.0f).color(-1);
        class_2872.vertex(f, f2, 0.0f).texture(0.0f, 0.0f).color(-1);
        class_2872.vertex(f, 0.0f, 0.0f).texture(0.0f, 1.0f).color(-1);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.defaultBlendFunc();
        minecraftClient.getFramebuffer().beginWrite(true);
    }

    /**
     * Lerps the mirror's colour multiplier from 1.0 (white - the blurred mirror shown untinted)
     * at {@code tint == 0} to the original's {@code colour/255 * 2.4f} at {@code tint == 1}.
     * Part of the user-requested deviation documented in {@link #renderMirrorTarget}.
     */
    private static float lerpTint(float tint, float target) {
        return 1.0f + tint * (target - 1.0f);
    }

    private static float clampOpacity(float f) {
        return f < 0.0f ? 0.0f : (f > 1.0f ? 1.0f : f);
    }

    @Generated
    public BooleanSetting getFillEnabledSetting() {
        return this.fillEnabled;
    }

    @Generated
    public ModeSetting getRenderModeSetting() {
        return this.renderMode;
    }

    @Generated
    public ModeSetting.Option getMirrorModeOption() {
        return this.mirrorMode;
    }

    @Generated
    public ModeSetting.Option getShaderModeOption() {
        return this.shaderMode;
    }

    @Generated
    public BooleanSetting getThemeSyncSetting() {
        return this.themeSync;
    }

    @Generated
    public ColorSetting getFillColorSetting() {
        return this.fillColor;
    }

    @Generated
    public BooleanSetting getFlatMirrorSetting() {
        return this.flatMirror;
    }

    @Generated
    public NumberSetting getMirrorAlphaSetting() {
        return this.mirrorAlpha;
    }

    @Generated
    public NumberSetting getMirrorBlurSetting() {
        return this.mirrorBlur;
    }

    @Generated
    public NumberSetting getMirrorAmbientSetting() {
        return this.mirrorAmbient;
    }

    @Generated
    public ModeSetting getShaderTypeSetting() {
        return this.shaderType;
    }

    @Generated
    public ModeSetting.Option getCausticShaderOption() {
        return this.causticShader;
    }

    @Generated
    public ModeSetting.Option getPlasmaShaderOption() {
        return this.plasmaShader;
    }

    @Generated
    public ModeSetting.Option getLavaShaderOption() {
        return this.lavaShader;
    }

    @Generated
    public NumberSetting getShaderSpeedSetting() {
        return this.shaderSpeed;
    }

    @Generated
    public NumberSetting getShaderAlphaSetting() {
        return this.shaderAlpha;
    }

    @Generated
    public RenderTarget[] getRenderTargets() {
        return this.renderTargets;
    }

    @Generated
    public RenderTarget getFramebufferCopyTarget() {
        return this.framebufferCopyTarget;
    }

    @Generated
    public RenderTarget getMainFramebufferTarget() {
        return this.mainFramebufferTarget;
    }

    @Generated
    public boolean isMainFramebufferReady() {
        return this.mainFramebufferReady;
    }

    @Generated
    public RenderTargetState getBlurProcessor() {
        return this.blurProcessor;
    }

    @Generated
    public boolean[] getRenderTargetCaptured() {
        return this.renderTargetCaptured;
    }

    @Generated
    public boolean[] getRenderTargetInitialized() {
        return this.renderTargetInitialized;
    }

    @Generated
    public float[] getProjectedTargetX() {
        return this.projectedTargetX;
    }

    @Generated
    public float[] getProjectedTargetY() {
        return this.projectedTargetY;
    }

    @Generated
    public EventListener<Render3DEvent> getRender3DListener() {
        return this.render3DListener;
    }

    @Generated
    public EventListener<PreHudRenderEvent> getPreHudRenderListener() {
        return this.preHudRenderListener;
    }
}
