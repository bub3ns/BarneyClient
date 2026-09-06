/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec2f
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.MathHelper
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 */
package moscow.rockstar.modules.visuals.esp.targeting;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.geometry.CubeRenderer;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.shaders.DynamicLightShader;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.postprocess.ProjectedLensPoint;
import moscow.rockstar.render.postprocess.TargetLensRenderer;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Target ESP", category=ModuleCategory.VISUALS, disableLocked=true, description="modules.descriptions.target_esp")
public class TargetESP
extends Module {
    private static final int TARGET_POINT_STEP_DEGREES = 60;
    private static final int DAMAGE_RING_STEP_DEGREES = 30;
    private static final float SOUL_RING_SCALE = 1.0f;
    private static final float CRYSTAL_HEIGHT = 3.5f;
    private static final float CIRCLE_RADIUS = 1.0f;
    private ModeSetting mode;
    private ModeSetting.Option souls;
    private ModeSetting.Option crystals;
    private ModeSetting.Option circle;
    private ModeSetting.Option jello;
    private BooleanSetting rayTrace;
    private BooleanSetting distortion;
    private NumberSetting distortionStrength;
    private BooleanSetting sync;
    private ColorSetting color;
    private BooleanSetting lighting;
    private final Animation toggleAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubic);
    private final Animation rotationAnimation = new Animation(70L, 0.0f, Easing.linear);
    private final Animation damageAnimation = new Animation(100L, 0.0f, Easing.easeInOutSine);
    private final Animation targetScaleAnimation = new Animation(250L, 0.0f, Easing.easeInOutCubic);
    private float targetAlpha = 1.0f;
    private LivingEntity currentTarget;
    private final TargetLensRenderer targetLensRenderer = new TargetLensRenderer();
    private Vec2f targetScreenPosition = Vec2f.ZERO;
    private final Animation distortionAnimation = new Animation(250L, Easing.easeInOutCubic);
    private final Animation circleAnimation = new Animation(150L, Easing.easeInOutCubic);
    private final Animation soulAnimation = new Animation(150L, Easing.easeInOutCubic);
    private final Animation crystalGlowAnimation = new Animation(350L, Easing.easeInOutCubic);
    private final Animation jelloAnimation = new Animation(350L, Easing.easeInOutCubic);
    private final Animation ringAnimation = new Animation(250L, Easing.easeOutBack);
    private final EventListener<Render3DEvent> onRender3DEvent = render3DEvent -> {
        LivingEntity class_13092;
        LivingEntity class_13093;
        Entity class_12972;
        if (!EntityUtils.isClientWorldReady()) {
            return;
        }
        Entity class_12973 = RockstarClient.create().getFriendManager().getTargetEntity();
        LivingEntity class_13094 = this.rayTrace.isEnabled() && (class_12972 = TargetESP.minecraftClient.targetedEntity) instanceof LivingEntity ? (class_13093 = (LivingEntity)class_12972) : (class_12973 instanceof LivingEntity ? (class_13092 = (LivingEntity)class_12973) : null);
        this.targetScaleAnimation.setEasing(Easing.easeInOutCubic);
        this.targetScaleAnimation.setDuration(350L);
        this.toggleAnimation.setEasing(Easing.easeInOutCubicBezier);
        this.toggleAnimation.setReverse(class_13094 != null);
        this.rotationAnimation.update(this.rotationAnimation.getValue() + 10.0f + 50.0f);
        if (this.rotationAnimation.getValue() > Float.MAX_VALUE || Float.isNaN(this.rotationAnimation.getValue())) {
            this.rotationAnimation.setValue(0.0f);
        }
        if (class_13094 != null) {
            this.currentTarget = class_13094;
        }
        if (this.currentTarget == null || this.toggleAnimation.getValue() == 0.0f) {
            return;
        }
        this.damageAnimation.setReverse(this.currentTarget.hurtTime > 0);
        this.handleSecondaryRender3DEvent((Render3DEvent)render3DEvent);
        MatrixStack matrices = render3DEvent.getMatrices();
        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        boolean bl = TargetESP.minecraftClient.world.raycast(new RaycastContext(TargetESP.minecraftClient.gameRenderer.getCamera().getPos(), this.currentTarget.getEyePos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)TargetESP.minecraftClient.player)).getType() != HitResult.Type.MISS || this.currentTarget.isTouchingWater();
        this.targetScaleAnimation.setReverse(bl);
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc((int)515);
        this.targetAlpha = 1.0f;
        matrices.push();
        this.renderTargetOverlay(matrices, this.currentTarget);
        matrices.pop();
        float f = this.targetScaleAnimation.getValue();
        if (f > 0.01f) {
            RenderSystem.depthFunc((int)516);
            this.targetAlpha = f;
            matrices.push();
            this.renderTargetOverlay(matrices, this.currentTarget);
            matrices.pop();
            RenderSystem.depthFunc((int)515);
        }
        this.targetAlpha = 1.0f;
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        matrices.pop();
        this.handleRender3DEvent((Render3DEvent)render3DEvent);
    };

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.mode = new ModeSetting(this, "modules.settings.target_esp.mode");
        this.souls = new ModeSetting.Option(this.mode, "modules.settings.target_esp.mode.souls");
        this.crystals = new ModeSetting.Option(this.mode, "modules.settings.target_esp.mode.crystals").select();
        this.circle = new ModeSetting.Option(this.mode, "modules.settings.target_esp.mode.circle");
        this.jello = new ModeSetting.Option(this.mode, "modules.settings.target_esp.mode.jello");
        this.rayTrace = new BooleanSetting(this, "modules.settings.target_esp.ray_trace");
        this.lighting = new BooleanSetting(this, "modules.settings.target_esp.lighting");
        this.distortion = new BooleanSetting((SettingOwner)this, "modules.settings.target_esp.distortion", () -> !this.crystals.isSelected()).enable();
        this.distortionStrength = new NumberSetting((SettingOwner)this, "modules.settings.target_esp.distortion_strength", () -> !this.crystals.isSelected() || !this.distortion.isEnabled()).setMinValue(0.05f).setMaxValue(0.15f).setStep(0.01f).setValue(0.05f);
        this.sync = new BooleanSetting(this, "theme.sync").enable();
        this.color = new ColorSetting(this, "modules.settings.target_esp.color", this.sync::isEnabled).setColor(ColorPalette.getAccentColor());
    }

    public TargetESP() {
        this.initializeSettings();
    }

    private void handleRender3DEvent(Render3DEvent render3DEvent) {
        if (!this.distortion.isEnabled()) {
            return;
        }
        if (!this.crystals.isSelected()) {
            return;
        }
        float f = this.toggleAnimation.getValue();
        if (f <= 0.01f) {
            return;
        }
        float f2 = this.distortionStrength.getValue() * f;
        if (f2 <= 0.001f) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = render3DEvent.getCamera().getPos();
        Vec3d WallPlayerSkullBlock = this.getTargetPosition(this.currentTarget);
        float f3 = this.currentTarget.getWidth() * 1.5f;
        float f4 = this.currentTarget.getHeight();
        float f5 = this.rotationAnimation.getValue();
        Matrix4f matrix4f = render3DEvent.getProjectionMatrix();
        Matrix4f matrix4f2 = new Matrix4f((Matrix4fc)matrix4f).mul((Matrix4fc)render3DEvent.getPositionMatrix());
        float f6 = matrix4f.m11() / matrix4f.m00();
        ArrayList<ProjectedLensPoint> arrayList = new ArrayList<>();
        for (int i = 0; i < 360; i += 20) {
            float f7 = 1.2f - 0.5f * f;
            float f8 = (float)(MathUtils.lookupSine((float)Math.toRadians((float)i + f5 * 0.3f)) * (double)f3 * (double)f7);
            float f9 = (float)(MathUtils.lookupCosine((float)Math.toRadians((float)i + f5 * 0.3f)) * (double)f3 * (double)f7);
            float f10 = 0.1f + f4 * (float)Math.abs(MathUtils.lookupSine(i));
            this.projectTargetPoint(arrayList, matrix4f2, matrix4f, WallPlayerSkullBlock, VanillaChestLootTableGenerator, f8, f10, f9, 0.45f);
        }
        if (arrayList.isEmpty()) {
            return;
        }
        this.targetLensRenderer.render(f6, f2, arrayList);
    }

    private void projectTargetPoint(List<ProjectedLensPoint> list, Matrix4f matrix4f, Matrix4f matrix4f2, Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, float f, float f2, float f3, float f4) {
        Vector4f vector4f = matrix4f.transform(new Vector4f((float)(VanillaChestLootTableGenerator.x + (double)f - WallPlayerSkullBlock.x), (float)(VanillaChestLootTableGenerator.y + (double)f2 - WallPlayerSkullBlock.y), (float)(VanillaChestLootTableGenerator.z + (double)f3 - WallPlayerSkullBlock.z), 1.0f));
        if (vector4f.w <= 0.05f) {
            return;
        }
        float f5 = vector4f.x / vector4f.w * 0.5f + 0.5f;
        float f6 = vector4f.y / vector4f.w * 0.5f + 0.5f;
        float f7 = vector4f.z / vector4f.w * 0.5f + 0.5f;
        float f8 = Math.min(f4 * matrix4f2.m11() / vector4f.w * 0.5f, 0.35f);
        if (f5 < -f8 * 2.0f || f5 > 1.0f + f8 * 2.0f || f6 < -f8 * 2.0f || f6 > 1.0f + f8 * 2.0f) {
            return;
        }
        list.add(new ProjectedLensPoint(f5, f6, f7, f8));
    }

    private void renderTargetOverlay(MatrixStack class_45872, LivingEntity class_13092) {
        if (this.circle.isSelected()) {
            this.renderCrystalEffect(class_45872, class_13092);
        } else if (this.crystals.isSelected()) {
            this.renderCircleEffect(class_45872, class_13092);
        } else if (this.jello.isSelected()) {
            this.renderSoulEffect(class_45872, class_13092);
        } else if (this.souls.isSelected()) {
            this.renderJelloEffect(class_45872, class_13092);
        }
    }

    private void renderSoulEffect(MatrixStack class_45872, LivingEntity class_13092) {
        float f;
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        double d;
        float f7;
        int n;
        Camera class_41842 = TargetESP.minecraftClient.gameRenderer.getCamera();
        ColorRGBA colorRGBA = this.sync.isEnabled() ? ColorPalette.getAccentColor() : this.color.getColor();
        Identifier class_29602 = RockstarClient.resourceId("textures/bloom.png");
        float f8 = this.currentTarget.getWidth() * 1.45f;
        class_45872.push();
        ItemRenderUtils.translateToWorldPosition(class_45872, this.getTargetPosition(this.currentTarget));
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        float f9 = 0.0f;
        float f10 = this.toggleAnimation.getValue();
        for (n = 0; n < 360; n += 2) {
            f7 = Math.max(0.5f, 0.7f - 0.2f * f9 + 0.2f - 0.2f * f10);
            d = ((float)n + this.rotationAnimation.getValue()) * ((float)Math.PI / 180);
            f6 = (float)(MathUtils.lookupSine(d) * (double)f8 * (double)f7);
            f5 = (float)(MathUtils.lookupCosine(d) * (double)f8 * (double)f7);
            f4 = class_13092.getHeight() / 1.75f + (float)((double)(class_13092.getHeight() / 2.0f) * MathUtils.lookupSine(Math.toRadians(this.rotationAnimation.getValue() / 1.5f + 30.0f)));
            f3 = 0.2f;
            f2 = 0.8f;
            f = 0.2f;
            for (int i = 0; i < 15; ++i) {
                f4 = class_13092.getHeight() / 1.75f + (float)((double)(class_13092.getHeight() / 2.0f) * MathUtils.lookupSine(Math.toRadians(this.rotationAnimation.getValue() / 1.5f + (float)i * 2.0f)));
                class_45872.push();
                class_45872.translate(f6, f4, f5);
                class_45872.multiply(class_41842.getRotation());
                ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f / 2.0f, -f / 2.0f, -f3 / 2.0f, f, f, colorRGBA.withAlpha(colorRGBA.getAlpha() * f10 * ((float)i / 15.0f) * 0.05f * this.targetAlpha));
                class_45872.pop();
            }
        }
        ItemRenderUtils.flushVertexConsumer(class_2872);
        class_45872.pop();
        class_45872.push();
        ItemRenderUtils.translateToWorldPosition(class_45872, this.getTargetPosition(this.currentTarget));
        RenderSystem.setShaderTexture((int)0, (Identifier)RockstarClient.resourceId("textures/glowing.png"));
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (n = 0; n < 360; n += 2) {
            f7 = Math.max(0.5f, 0.7f - 0.2f * f9 + 0.2f - 0.2f * f10);
            d = ((float)n + this.rotationAnimation.getValue()) * ((float)Math.PI / 180);
            f6 = (float)(MathUtils.lookupSine(d) * (double)f8 * (double)f7);
            f5 = (float)(MathUtils.lookupCosine(d) * (double)f8 * (double)f7);
            f4 = class_13092.getHeight() / 1.75f + (float)((double)(class_13092.getHeight() / 2.0f) * MathUtils.lookupSine(Math.toRadians(this.rotationAnimation.getValue() / 1.5f + 30.0f)));
            f3 = 0.2f;
            f2 = 0.2f;
            f = 0.2f;
            class_45872.push();
            class_45872.translate(f6, f4, f5);
            class_45872.multiply(class_41842.getRotation());
            ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f2 / 2.0f, -f2 / 2.0f, -f3 / 2.0f, f2, f2, colorRGBA.withAlpha(colorRGBA.getAlpha() * f10 * 0.2f * this.targetAlpha));
            class_45872.pop();
        }
        ItemRenderUtils.flushVertexConsumer(class_2872);
        class_45872.pop();
    }

    private void renderCrystalEffect(MatrixStack class_45872, LivingEntity class_13092) {
        Camera class_41842 = TargetESP.minecraftClient.gameRenderer.getCamera();
        ColorRGBA colorRGBA = this.sync.isEnabled() ? ColorPalette.getAccentColor() : this.color.getColor();
        Identifier class_29602 = RockstarClient.resourceId("textures/glowing.png");
        float f = this.currentTarget.getWidth() * 1.5f;
        ItemRenderUtils.translateToWorldPosition(class_45872, this.getTargetPosition(this.currentTarget));
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        float f2 = this.damageAnimation.getValue();
        float f3 = this.toggleAnimation.getValue();
        for (int i = 0; i < 360; i += 2) {
            if (i / 45 % 2 == 0) continue;
            float f4 = Math.max(0.5f, 0.7f - 0.2f * f2 + 0.2f - 0.2f * f3);
            double d = ((float)i + this.rotationAnimation.getValue()) * ((float)Math.PI / 180);
            float f5 = (float)(MathUtils.lookupSine(d) * (double)f * (double)f4);
            float f6 = (float)(MathUtils.lookupCosine(d) * (double)f * (double)f4);
            float f7 = class_13092.getHeight() / 1.75f + (float)((double)(class_13092.getHeight() / 2.0f) * MathUtils.lookupSine(Math.toRadians(this.rotationAnimation.getValue() / 2.0f)));
            float f8 = 0.3f;
            float f9 = 0.8f;
            class_45872.push();
            class_45872.translate(f5, f7, f6);
            class_45872.multiply(class_41842.getRotation());
            ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f9 / 2.0f, -f9 / 2.0f, -f8 / 2.0f, f9, f9, colorRGBA.withAlpha(colorRGBA.getAlpha() * f3 * 0.03f * this.targetAlpha));
            ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f8 / 2.0f, -f8 / 2.0f, -f8 / 2.0f, f8, f8, colorRGBA.withAlpha(colorRGBA.getAlpha() * f3 * 0.7f * this.targetAlpha));
            class_45872.pop();
        }
        ItemRenderUtils.flushVertexConsumer(class_2872);
    }

    private void renderCircleEffect(MatrixStack class_45872, LivingEntity class_13092) {
        float f;
        float f2;
        Camera class_41842 = TargetESP.minecraftClient.gameRenderer.getCamera();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        ColorRGBA colorRGBA = this.sync.isEnabled() ? ColorPalette.getAccentColor() : this.color.getColor();
        float f3 = this.currentTarget.getWidth() * 1.5f;
        ItemRenderUtils.translateToWorldPosition(class_45872, this.getTargetPosition(this.currentTarget));
        BufferBuilder class_2872 = CubeRenderer.beginCubeBatch();
        for (int i = 0; i < 360; i += 20) {
            float f4 = 1.2f - 0.5f * this.toggleAnimation.getValue();
            f2 = (float)(MathUtils.lookupSine((float)Math.toRadians((float)i + this.rotationAnimation.getValue() * 0.3f)) * (double)f3 * (double)f4);
            float f5 = (float)(MathUtils.lookupCosine((float)Math.toRadians((float)i + this.rotationAnimation.getValue() * 0.3f)) * (double)f3 * (double)f4);
            f = 0.1f;
            class_45872.push();
            class_45872.translate((double)f2, (double)0.1f + (double)class_13092.getHeight() * Math.abs(MathUtils.lookupSine(i)), (double)f5);
            Vec3d WallPlayerSkullBlock = this.getTargetPosition(this.currentTarget).add((double)f2, 1.0, (double)f5);
            Vec3d VanillaEntityLootTableGenerator = class_13092.getPos().add(0.0, (double)class_13092.getHeight() / 2.0, 0.0);
            Vector3f vector3f = new Vector3f((float)(VanillaEntityLootTableGenerator.x - WallPlayerSkullBlock.x), (float)(VanillaEntityLootTableGenerator.y - WallPlayerSkullBlock.y), (float)(VanillaEntityLootTableGenerator.z - WallPlayerSkullBlock.z)).normalize();
            Vector3f vector3f2 = new Vector3f(0.0f, 1.0f, 0.0f);
            Quaternionf quaternionf = new Quaternionf().rotationTo((Vector3fc)vector3f2, (Vector3fc)vector3f);
            class_45872.multiply(quaternionf);
            CubeRenderer.drawCube(class_45872, class_2872, 0.0f, 0.0f, 0.0f, f, colorRGBA.withAlpha(255.0f * this.toggleAnimation.getValue() * this.targetAlpha));
            class_45872.pop();
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        Identifier class_29602 = RockstarClient.resourceId("textures/bloom.png");
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder CreativeInventoryActionC2SPacket = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        f2 = 1.0f;
        for (int i = 0; i < 360; i += 20) {
            f = 1.2f - 0.5f * this.toggleAnimation.getValue();
            float f6 = (float)(MathUtils.lookupSine((float)Math.toRadians((float)i + this.rotationAnimation.getValue() * 0.3f)) * (double)f3 * (double)f);
            float f7 = (float)(MathUtils.lookupCosine((float)Math.toRadians((float)i + this.rotationAnimation.getValue() * 0.3f)) * (double)f3 * (double)f);
            float f8 = 0.1f;
            class_45872.push();
            class_45872.translate((double)f6, (double)0.1f + (double)class_13092.getHeight() * Math.abs(MathUtils.lookupSine(i)), (double)f7);
            class_45872.multiply(class_41842.getRotation());
            ShaderRenderer.appendTexturedQuadVertices(class_45872, CreativeInventoryActionC2SPacket, -f2 / 2.0f, -f2 / 2.0f, 0.0, f2, f2, colorRGBA.withAlpha(255.0f * this.toggleAnimation.getValue() * 0.2f * this.targetAlpha));
            class_45872.pop();
        }
        ItemRenderUtils.flushVertexConsumer(CreativeInventoryActionC2SPacket);
    }

    private void renderJelloEffect(MatrixStack class_45872, LivingEntity class_13092) {
        Camera class_41842 = TargetESP.minecraftClient.gameRenderer.getCamera();
        ColorRGBA colorRGBA = this.sync.isEnabled() ? ColorPalette.getAccentColor() : this.color.getColor();
        Identifier class_29602 = RockstarClient.resourceId("textures/glowing.png");
        float f = this.currentTarget.getWidth() * 1.5f;
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        ItemRenderUtils.translateToWorldPosition(class_45872, this.getTargetPosition(this.currentTarget));
        int n = 2;
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        for (int i = 0; i < 360; i += n) {
            float f2 = 0.23f + 0.005f * (float)n2;
            float f3 = 0.7f + 0.005f * (float)n2;
            if (n3 > 0) {
                n3 -= n;
                continue;
            }
            if ((n2 += n) > 50) {
                n3 = 100;
                n2 = 0;
                ++n4;
                continue;
            }
            float f4 = Math.max(0.5f, 1.2f - 0.5f * this.toggleAnimation.getValue());
            float f5 = (float)(MathUtils.lookupSine((float)Math.toRadians((float)i + this.rotationAnimation.getValue() * 1.0f)) * (double)f * (double)f4);
            float f6 = (float)(MathUtils.lookupCosine((float)Math.toRadians((float)i + this.rotationAnimation.getValue() * 1.0f)) * (double)f * (double)f4);
            class_45872.push();
            class_45872.translate((double)f5, (double)(this.currentTarget.getHeight() / 1.5f) + (double)(this.currentTarget.getHeight() / 3.0f) * MathUtils.lookupSine(Math.toRadians((float)i / 2.0f + this.rotationAnimation.getValue() / 5.0f)), (double)f6);
            class_45872.multiply(class_41842.getRotation());
            ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f3 / 2.0f, -f3 / 2.0f, -f2 / 2.0f, f3, f3, colorRGBA.withAlpha(colorRGBA.getAlpha() * this.toggleAnimation.getValue() * 0.02f * this.targetAlpha));
            ShaderRenderer.appendTexturedQuadVertices(class_45872, class_2872, -f2 / 2.0f, -f2 / 2.0f, -f2 / 2.0f, f2, f2, colorRGBA.withAlpha(colorRGBA.getAlpha() * this.toggleAnimation.getValue() * 0.5f * this.targetAlpha));
            class_45872.pop();
        }
        ItemRenderUtils.flushVertexConsumer(class_2872);
    }

    private void handleSecondaryRender3DEvent(Render3DEvent render3DEvent) {
        if (!this.lighting.isEnabled()) {
            return;
        }
        if (ShaderRenderer.particleLightRenderer == null) {
            return;
        }
        float f = 1.0f * this.toggleAnimation.getValue();
        if (f <= 0.001f) {
            return;
        }
        List<Vec3d> list = this.collectTargets(this.currentTarget);
        if (list.isEmpty()) {
            return;
        }
        ColorRGBA colorRGBA = this.sync.isEnabled() ? ColorPalette.getAccentColor() : this.color.getColor();
        float f2 = colorRGBA.getRed() / 255.0f;
        float f3 = colorRGBA.getGreen() / 255.0f;
        float f4 = colorRGBA.getBlue() / 255.0f;
        float f5 = f / (float)list.size();
        Vec3d VanillaChestLootTableGenerator = render3DEvent.getCamera().getPos();
        Vec3d WallPlayerSkullBlock = this.getTargetPosition(this.currentTarget);
        ArrayList<DynamicLightShader.LightSource> arrayList = new ArrayList<DynamicLightShader.LightSource>(list.size());
        for (Vec3d VanillaEntityLootTableGenerator : list) {
            arrayList.add(new DynamicLightShader.LightSource((float)(WallPlayerSkullBlock.x + VanillaEntityLootTableGenerator.x - VanillaChestLootTableGenerator.x), (float)(WallPlayerSkullBlock.y + VanillaEntityLootTableGenerator.y - VanillaChestLootTableGenerator.y), (float)(WallPlayerSkullBlock.z + VanillaEntityLootTableGenerator.z - VanillaChestLootTableGenerator.z), 3.5f, f2, f3, f4, f5));
        }
        Matrix4f matrix4f = new Matrix4f((Matrix4fc)render3DEvent.getProjectionMatrix()).mul((Matrix4fc)render3DEvent.getPositionMatrix()).invert();
        ShaderRenderer.particleLightRenderer.renderParticleLights(matrix4f, 1.0f, arrayList);
    }

    private List<Vec3d> collectTargets(LivingEntity class_13092) {
        float f = this.toggleAnimation.getValue();
        float f2 = this.rotationAnimation.getValue();
        double d = Math.max(0.1, (double)class_13092.getHeight());
        ArrayList<Vec3d> arrayList = new ArrayList<Vec3d>(6);
        if (this.crystals.isSelected()) {
            double d2 = (double)class_13092.getWidth() * 1.5 * (1.2 - 0.5 * (double)f);
            for (int i = 0; i < 360; i += 60) {
                double d3 = Math.toRadians((double)i + (double)f2 * 0.3);
                arrayList.add(new Vec3d(MathUtils.lookupSine(d3) * d2, 0.1 + d * Math.abs(MathUtils.lookupSine(i)), MathUtils.lookupCosine(d3) * d2));
            }
            return arrayList;
        }
        if (this.souls.isSelected()) {
            double d4 = (double)class_13092.getWidth() * 1.5 * Math.max(0.5, 1.2 - 0.5 * (double)f);
            for (int i = 0; i < 360; i += 60) {
                double d5 = Math.toRadians((float)i + f2);
                arrayList.add(new Vec3d(MathUtils.lookupSine(d5) * d4, d / 1.5 + d / 3.0 * MathUtils.lookupSine(Math.toRadians((double)i / 2.0 + (double)f2 / 5.0)), MathUtils.lookupCosine(d5) * d4));
            }
            return arrayList;
        }
        boolean bl = this.jello.isSelected();
        double d6 = bl ? 0.0 : (double)this.damageAnimation.getValue();
        double d7 = (double)class_13092.getWidth() * (bl ? 1.45 : 1.5) * Math.max(0.5, 0.7 - 0.2 * d6 + 0.2 - 0.2 * (double)f);
        double d8 = bl ? (double)f2 / 1.5 + 30.0 : (double)f2 / 2.0;
        double d9 = d / 1.75 + d / 2.0 * MathUtils.lookupSine(Math.toRadians(d8));
        int n = bl ? 30 : 15;
        for (int i = 0; i < 360; i += n) {
            if (!bl && i / 45 % 2 == 0) continue;
            double d10 = Math.toRadians((float)i + f2);
            arrayList.add(new Vec3d(MathUtils.lookupSine(d10) * d7, d9, MathUtils.lookupCosine(d10) * d7));
        }
        return arrayList;
    }

    private Vec3d getTargetPosition(LivingEntity class_13092) {
        float f = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
        return new Vec3d(MathHelper.lerp((double)f, (double)class_13092.prevX, (double)class_13092.getX()), MathHelper.lerp((double)f, (double)class_13092.prevY, (double)class_13092.getY()), MathHelper.lerp((double)f, (double)class_13092.prevZ, (double)class_13092.getZ()));
    }

    @Override
    public void onTick() {
        super.onTick();
    }
}
