/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.LivingEntity
 *  net.minecraft.ArmorStandEntity
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.modules.visuals.effects.kill;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.visuals.effects.kill.KillEffect;
import moscow.rockstar.modules.visuals.effects.kill.KillParticle;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.events.game.EntityDeathEvent;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Kill Effects", category=ModuleCategory.VISUALS, disableLocked=true, description="modules.descriptions.kill_effects")
public class KillEffects
extends Module {
    private final List<KillParticle> killEffectEntries = new CopyOnWriteArrayList<KillParticle>();
    private final List<KillEffect> particleItems = new CopyOnWriteArrayList<KillEffect>();
    static final Random random = new Random();
    private ModeSetting mode;
    private ModeSetting.Option lightning;
    private ModeSetting.Option particles;
    private ModeSetting particlePhysics;
    private ModeSetting.Option gravity;
    private ModeSetting.Option scatter;
    private BooleanSetting sync;
    private ColorSetting color;
    private final EventListener<EntityDeathEvent> onEntityDeathEvent = entityDeathEvent -> {
        ColorRGBA colorRGBA;
        if (entityDeathEvent.getEntity().isRemoved() || entityDeathEvent.getEntity() instanceof ArmorStandEntity) {
            return;
        }
        ColorRGBA colorRGBA2 = colorRGBA = this.sync.isEnabled() ? ColorPalette.getAccentColor() : this.color.getColor();
        if (this.mode.isSelected(this.lightning)) {
            this.killEffectEntries.add(new KillParticle(entityDeathEvent.getEntity().getPos(), colorRGBA));
        } else if (this.mode.isSelected(this.particles)) {
            this.spawnEntityKillEffect(entityDeathEvent.getEntity(), colorRGBA);
        }
    };
    private final EventListener<Render3DEvent> onRender3DEvent = render3DEvent -> {
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Camera class_41842 = KillEffects.minecraftClient.gameRenderer.getCamera();
        class_45872.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        Identifier class_29602 = RockstarClient.resourceId("textures/bloom.png");
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (KillParticle object : this.killEffectEntries) {
            object.renderParticles(class_2872, render3DEvent.getMatrices(), class_41842);
            if (object.animation.getValue() != 1.0f) continue;
            object.reverseAnimation = false;
        }
        for (KillEffect killEffect : this.particleItems) {
            if (killEffect.isExpired()) continue;
            killEffect.updatePhysics();
            killEffect.render((Render3DEvent)render3DEvent, class_2872);
        }
        this.particleItems.removeIf(KillEffect::isExpired);
        BuiltBuffer builtBuffer = class_2872.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        class_45872.pop();
        this.killEffectEntries.removeIf(killParticle -> !killParticle.reverseAnimation && killParticle.animation.getValue() == 0.0f);
    };
    private final EventListener<WorldChangeEvent> onWorldChangeEvent = worldChangeEvent -> {
        this.killEffectEntries.clear();
        this.particleItems.clear();
    };

    public KillEffects() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.mode = new ModeSetting(this, "modules.settings.kill_effects.mode");
        this.lightning = new ModeSetting.Option(this.mode, "modules.settings.kill_effects.mode.lightning");
        this.particles = new ModeSetting.Option(this.mode, "modules.settings.kill_effects.mode.particles").select();
        this.particlePhysics = new ModeSetting((SettingOwner)this, "modules.settings.kill_effects.particlePhysics", () -> !this.mode.isSelected(this.particles));
        this.gravity = new ModeSetting.Option(this.particlePhysics, "modules.settings.kill_effects.particlePhysics.gravity");
        this.scatter = new ModeSetting.Option(this.particlePhysics, "modules.settings.kill_effects.particlePhysics.scatter").select();
        this.sync = new BooleanSetting(this, "theme.sync").enable();
        this.color = new ColorSetting(this, "modules.settings.kill_effects.color", this.sync::isEnabled).setColor(ColorPalette.getAccentColor());
    }

    private void spawnEntityKillEffect(LivingEntity class_13092, ColorRGBA colorRGBA) {
        Vec3d VanillaChestLootTableGenerator = class_13092.getPos();
        float f = class_13092.getHeight();
        float f2 = class_13092.getWidth();
        float f3 = (float)Math.toRadians(-class_13092.bodyYaw + 90.0f);
        boolean bl = this.particlePhysics.isSelected(this.gravity);
        int n = 250;
        float f4 = f - 0.2f;
        float f5 = f2 * 0.4f;
        this.spawnKillParticles(VanillaChestLootTableGenerator.add(0.0, (double)f4, 0.0), f5, n / 10, f3, colorRGBA, bl);
        float f6 = f * 0.85f;
        float f7 = f * 0.4f;
        float f8 = f2 * 0.4f;
        float f9 = f2 * 0.2f;
        this.spawnPhysicsParticles(VanillaChestLootTableGenerator, f7, f6, f8, f9, f3, n / 4, colorRGBA, bl);
        float f10 = f * 0.4f;
        float f11 = f2 * 0.15f;
        for (int i = -1; i <= 1; i += 2) {
            Vec3d WallPlayerSkullBlock = new Vec3d(Math.sin(f3) * (double)f2 * 0.5 * (double)i, (double)(f * 0.75f), Math.cos(f3) * (double)f2 * 0.5 * (double)i);
            this.spawnParticleBurst(VanillaChestLootTableGenerator.add(WallPlayerSkullBlock), f10, f11, f3, n / 8, colorRGBA, bl);
        }
        float f12 = f * 0.45f;
        float f13 = f2 * 0.15f;
        for (int i = -1; i <= 1; i += 2) {
            Vec3d VanillaEntityLootTableGenerator = new Vec3d(Math.sin(f3) * (double)f2 * (double)0.15f * (double)i, (double)(f * 0.4f), Math.cos(f3) * (double)f2 * (double)0.15f * (double)i);
            this.spawnParticleBurst(VanillaChestLootTableGenerator.add(VanillaEntityLootTableGenerator), f12, f13, f3, n / 6, colorRGBA, bl);
        }
    }

    private void spawnKillParticles(Vec3d VanillaChestLootTableGenerator, float f, int n, float f2, ColorRGBA colorRGBA, boolean bl) {
        for (int i = 0; i < n; ++i) {
            float f3 = random.nextFloat() * (float)Math.PI * 2.0f;
            float f4 = (float)Math.acos(2.0f * random.nextFloat() - 1.0f);
            float f5 = f * (float)Math.cbrt(random.nextFloat());
            float f6 = f5 * (float)(Math.sin(f4) * Math.cos(f3));
            float f7 = f5 * (float)(Math.sin(f4) * Math.sin(f3));
            float f8 = f5 * (float)Math.cos(f4);
            float f9 = bl ? 0.02f : 0.008f;
            float f10 = (random.nextFloat() - 0.5f) * f9;
            float f11 = bl ? 0.03f + random.nextFloat() * 0.04f : (random.nextFloat() - 0.5f) * 0.008f;
            float f12 = (random.nextFloat() - 0.5f) * f9;
            this.particleItems.add(new KillEffect(this, VanillaChestLootTableGenerator, f6, f7, f8, f10, f11, f12, colorRGBA, bl));
        }
    }

    private void spawnPhysicsParticles(Vec3d VanillaChestLootTableGenerator, float f, float f2, float f3, float f4, float f5, int n, ColorRGBA colorRGBA, boolean bl) {
        for (int i = 0; i < n; ++i) {
            float f6 = (random.nextFloat() - 0.5f) * f3 * 2.0f;
            float f7 = f + random.nextFloat() * (f2 - f);
            float f8 = (random.nextFloat() - 0.5f) * f4 * 2.0f;
            float f9 = (float)((double)f6 * Math.cos(f5) - (double)f8 * Math.sin(f5));
            float f10 = (float)((double)f6 * Math.sin(f5) + (double)f8 * Math.cos(f5));
            float f11 = bl ? 0.025f : 0.01f;
            float f12 = (random.nextFloat() - 0.5f) * f11;
            float f13 = bl ? 0.04f + random.nextFloat() * 0.05f : (random.nextFloat() - 0.5f) * 0.01f;
            float f14 = (random.nextFloat() - 0.5f) * f11;
            this.particleItems.add(new KillEffect(this, VanillaChestLootTableGenerator, f9, f7, f10, f12, f13, f14, colorRGBA, bl));
        }
    }

    private void spawnParticleBurst(Vec3d VanillaChestLootTableGenerator, float f, float f2, float f3, int n, ColorRGBA colorRGBA, boolean bl) {
        for (int i = 0; i < n; ++i) {
            float f4 = random.nextFloat();
            float f5 = (random.nextFloat() - 0.5f) * f2 * 2.0f;
            float f6 = -f4 * f;
            float f7 = (random.nextFloat() - 0.5f) * f2 * 2.0f;
            float f8 = bl ? 0.018f : 0.006f;
            float f9 = (random.nextFloat() - 0.5f) * f8;
            float f10 = bl ? 0.025f + random.nextFloat() * 0.035f : (random.nextFloat() - 0.5f) * 0.006f;
            float f11 = (random.nextFloat() - 0.5f) * f8;
            this.particleItems.add(new KillEffect(this, VanillaChestLootTableGenerator, f5, f6, f7, f9, f10, f11, colorRGBA, bl));
        }
    }
}
