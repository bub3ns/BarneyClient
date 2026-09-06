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
 *  net.minecraft.BlockView
 *  net.minecraft.Position
 *  net.minecraft.Box
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.VoxelShape
 *  net.minecraft.BlockState
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.BuiltBuffer
 *  org.joml.FrustumIntersection
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 */
package moscow.rockstar.modules.visuals.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.visuals.effects.particles.ParticleInstance;
import moscow.rockstar.modules.visuals.effects.particles.ParticleState;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.shaders.DynamicLightShader;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.world.RaycastContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="World", category=ModuleCategory.VISUALS, description="modules.descriptions.world")
public class World
extends Module {
    private final List<ParticleInstance> particleSettings = new ArrayList<ParticleInstance>();
    private BooleanSetting sync;
    private ColorSetting color;
    private BooleanSetting lighting;
    private NumberSetting radius;
    private NumberSetting strength;
    private static final int KEY_BIND = 100;
    private static final float MAX_PARTICLE_COUNT = 20.0f;
    private static final float PARTICLE_SIZE = 1.6f;
    private static final float PARTICLE_SPEED = 6.0f;
    private static final double PARTICLE_SCALE = 1.2;
    private static final float PARTICLE_ALPHA = 1.0f;
    private static final int DEFAULT_KEY_BIND = 32;
    private int particleCursor;
    private final EventListener<Render3DEvent> onRender3DEvent = render3DEvent -> {
        if (this.particleSettings.isEmpty()) {
            return;
        }
        ColorRGBA colorRGBA = this.sync.isEnabled() ? ColorPalette.getAccentColor() : this.color.getColor();
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Camera class_41842 = World.minecraftClient.gameRenderer.getCamera();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        for (ParticleInstance particle : this.particleSettings) {
            particle.animation.setReverse(!particle.isAlive());
            particle.openingAnimation.setReverse(!particle.opening);
        }
        this.renderParticleBatch((Render3DEvent)render3DEvent, colorRGBA, VanillaChestLootTableGenerator);
        class_45872.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        Identifier class_29602 = RockstarClient.resourceId("textures/bloom.png");
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder particleVertexBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (ParticleInstance particle : this.particleSettings) {
            Vec3d particlePosition = ProjectionUtils.interpolatePosition(particle.currentPosition, particle.nextPosition, render3DEvent.getTickDelta());
            float f = 4.0f * particle.animationProgress;
            class_45872.push();
            ItemRenderUtils.translateToWorldPosition(class_45872, particlePosition);
            class_45872.multiply(class_41842.getRotation());
            ShaderRenderer.appendTexturedQuadVertices(class_45872, particleVertexBuffer, -f / 2.0f, -f / 2.0f, 0.0, f, f, colorRGBA.withAlpha(255.0f * particle.animation.getValue() * 0.4f));
            class_45872.pop();
        }
        BuiltBuffer particleBuffer = particleVertexBuffer.endNullable();
        if (particleBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(particleBuffer);
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        class_45872.pop();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder particleLineBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (ParticleInstance particle : this.particleSettings) {
            Vec3d particlePosition = ProjectionUtils.interpolatePosition(particle.currentPosition, particle.nextPosition, render3DEvent.getTickDelta());
            Vec3d particleRotation = ProjectionUtils.interpolatePosition(particle.previousPosition, particle.nextMovement, render3DEvent.getTickDelta());
            class_45872.push();
            class_45872.translate(particlePosition.add(-VanillaChestLootTableGenerator.getX(), -VanillaChestLootTableGenerator.getY(), -VanillaChestLootTableGenerator.getZ()));
            class_45872.multiply(new Quaternionf().rotationXYZ((float)particleRotation.x, (float)particleRotation.y, (float)particleRotation.z));
            class_45872.scale(particle.animationProgress, particle.animationProgress, particle.animationProgress);
            RenderUtils.drawBoxCorners(class_45872, particleLineBuffer, new Box(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5), colorRGBA.withAlpha(255.0f * particle.animation.getValue() * 0.4f));
            RenderUtils.drawBoxOutline(class_45872, particleLineBuffer, new Box(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5), colorRGBA.withAlpha(205.0f * particle.animation.getValue()));
            class_45872.pop();
        }
        BuiltBuffer particleLineBufferResult = particleLineBuffer.endNullable();
        if (particleLineBufferResult != null) {
            BufferRenderer.drawWithGlobalProgram(particleLineBufferResult);
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    };
    private static final double MAX_PARTICLE_DISTANCE = 48.0;
    private static final int PARTICLE_LIMIT = 40;
    private static final float PARTICLE_ALPHA_MULTIPLIER = 0.35f;
    private static final float PARTICLE_SPAWN_INTERVAL = 2.0f;
    private static final double PARTICLE_LIFETIME = 0.5;
    private static final int PARTICLE_SEED = 16;

    public World() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.sync = new BooleanSetting(this, "theme.sync").enable();
        this.color = new ColorSetting(this, "modules.settings.world.color", this.sync::isEnabled).setColor(ColorPalette.getAccentColor());
        this.lighting = new BooleanSetting(this, "modules.settings.world.lighting");
        this.radius = new NumberSetting((SettingOwner)this, "modules.settings.world.lighting.radius", () -> !this.lighting.isEnabled()).setMinValue(2.0f).setMaxValue(7.0f).setStep(0.5f).setValue(4.0f).setUnit(" blocks");
        this.strength = new NumberSetting((SettingOwner)this, "modules.settings.world.lighting.strength", () -> !this.lighting.isEnabled()).setMinValue(0.0f).setMaxValue(150.0f).setStep(10.0f).setValue(100.0f).setUnit("%");
    }

    @Override
    public void onDisable() {
        this.particleSettings.clear();
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void onTick() {
        int var2_8 = 0;
        int var2_6;
        if (World.minecraftClient.player == null || World.minecraftClient.world == null) {
            this.particleSettings.clear();
            return;
        }
        this.particleSettings.removeIf(particleInstance -> particleInstance.animation.getValue() == 0.0f && particleInstance.isAlive());
        for (ParticleInstance object : this.particleSettings) {
            object.updatePosition();
        }
        int n2 = 0;
        for (ParticleInstance particleInstance22 : this.particleSettings) {
            if (particleInstance22.persistent) continue;
            ++n2;
        }
        int lastParticleIndex = this.particleSettings.size() - 1;
        var2_6 = lastParticleIndex;
        while (var2_6 >= 0 && n2 > 100) {
            ParticleInstance particle = this.particleSettings.get(var2_6);
            if (!particle.persistent) {
                particle.persistent = true;
                --n2;
            }
            --var2_6;
        }
        boolean bl = false;
        while (var2_8 < 4 && n2 < 100) {
            Vec3d spawnPosition = this.getCameraPosition();
            if (spawnPosition == null) {
                break;
            }
            ParticleInstance particle = new ParticleInstance(spawnPosition, Vec3d.ZERO, new Vec3d((double)MathUtils.interpolateRandomDouble(-1.0, 1.0), (double)MathUtils.interpolateRandomDouble(0.0, 2.0), (double)MathUtils.interpolateRandomDouble(-1.0, 1.0)), new Vec3d((double)MathUtils.interpolateRandomDouble(-1.0, 1.0), (double)MathUtils.interpolateRandomDouble(-1.0, 1.0), (double)MathUtils.interpolateRandomDouble(-1.0, 1.0)), (long)MathUtils.interpolateRandomDouble(1500.0, 4500.0), MathUtils.interpolateRandomDouble(0.1f, 0.3f));
            this.resetParticle(particle);
            this.particleSettings.add(particle);
            ++n2;
            ++var2_8;
        }
        this.clearParticles();
    }

    private Vec3d getCameraPosition() {
        Vec3d VanillaChestLootTableGenerator = null;
        for (int i = 0; i < 8; ++i) {
            Vec3d WallPlayerSkullBlock = World.minecraftClient.player.getPos().add((double)MathUtils.interpolateRandomDouble(-20.0, 20.0), (double)MathUtils.interpolateRandomDouble(1.6f, 6.0), (double)MathUtils.interpolateRandomDouble(-20.0, 20.0));
            if (this.isParticlePositionValid(WallPlayerSkullBlock)) continue;
            if (VanillaChestLootTableGenerator == null) {
                VanillaChestLootTableGenerator = WallPlayerSkullBlock;
            }
            if (this.isParticlePositionValid(WallPlayerSkullBlock.subtract(0.0, 1.2, 0.0))) continue;
            return WallPlayerSkullBlock;
        }
        return VanillaChestLootTableGenerator;
    }

    private boolean isParticlePositionValid(Vec3d VanillaChestLootTableGenerator) {
        BlockPos adminsky = BlockPos.ofFloored((Position)VanillaChestLootTableGenerator);
        if (World.minecraftClient.world.isOutOfHeightLimit(adminsky.getY())) {
            return true;
        }
        BlockState class_26802 = World.minecraftClient.world.getBlockState(adminsky);
        if (class_26802.isAir()) {
            return false;
        }
        VoxelShape class_2652 = class_26802.getCollisionShape((BlockView)World.minecraftClient.world, adminsky);
        if (class_2652.isEmpty()) {
            return false;
        }
        Vec3d WallPlayerSkullBlock = VanillaChestLootTableGenerator.subtract((double)adminsky.getX(), (double)adminsky.getY(), (double)adminsky.getZ());
        for (Box HorizontalFacingBlock : class_2652.getBoundingBoxes()) {
            if (!HorizontalFacingBlock.expand(0.01).contains(WallPlayerSkullBlock)) continue;
            return true;
        }
        return false;
    }

    private void clearParticles() {
        if (!this.lighting.isEnabled()) {
            for (ParticleInstance particleInstance : this.particleSettings) {
                particleInstance.opening = false;
            }
            return;
        }
        Camera class_41842 = World.minecraftClient.gameRenderer.getCamera();
        if (class_41842 == null) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        int n = this.particleSettings.size();
        if (n == 0) {
            return;
        }
        int n2 = Math.min(n, 40);
        for (ParticleInstance particleInstance : this.particleSettings) {
            if (!particleInstance.closing || n2 <= 0) continue;
            --n2;
            particleInstance.opening = this.isParticleVisible(VanillaChestLootTableGenerator, particleInstance.nextPosition);
        }
        int n3 = 0;
        for (int i = 0; i < n && n2 > 0; ++i) {
            ParticleInstance particleInstance = this.particleSettings.get((this.particleCursor + i) % n);
            ++n3;
            if (particleInstance.closing) continue;
            --n2;
            particleInstance.opening = this.isParticleVisible(VanillaChestLootTableGenerator, particleInstance.nextPosition);
        }
        this.particleCursor = (this.particleCursor + n3) % n;
    }

    private void resetParticle(ParticleInstance particleInstance) {
        if (!this.lighting.isEnabled()) {
            return;
        }
        Camera class_41842 = World.minecraftClient.gameRenderer.getCamera();
        if (class_41842 == null) {
            return;
        }
        particleInstance.opening = this.isParticleVisible(class_41842.getPos(), particleInstance.nextPosition);
        particleInstance.openingAnimation.setValue(particleInstance.opening ? 0.0f : 1.0f);
    }

    private boolean isParticleVisible(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock) {
        if (WallPlayerSkullBlock.squaredDistanceTo(VanillaChestLootTableGenerator) > 2304.0) {
            return true;
        }
        return World.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, WallPlayerSkullBlock, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)World.minecraftClient.player)).getType() != HitResult.Type.MISS;
    }

    private void renderParticleBatch(Render3DEvent render3DEvent, ColorRGBA colorRGBA, Vec3d VanillaChestLootTableGenerator) {
        if (!this.lighting.isEnabled()) {
            return;
        }
        float f = this.radius.getValue();
        float f2 = this.strength.getValue() / 100.0f;
        int n = Math.min(32, 48);
        if (f <= 0.0f || f2 <= 0.0f || n <= 0) {
            return;
        }
        Matrix4f matrix4f = new Matrix4f((Matrix4fc)render3DEvent.getProjectionMatrix()).mul((Matrix4fc)render3DEvent.getPositionMatrix());
        FrustumIntersection frustumIntersection = new FrustumIntersection((Matrix4fc)matrix4f);
        ArrayList<ParticleState> arrayList = new ArrayList<ParticleState>();
        for (ParticleInstance object2 : this.particleSettings) {
            float f3 = object2.animation.getValue() * object2.openingAnimation.getValue();
            if (f3 <= 0.01f) {
                object2.closing = false;
                object2.closingAnimation.setReverse(false);
                continue;
            }
            Vec3d particlePosition = ProjectionUtils.interpolatePosition(object2.currentPosition, object2.nextPosition, render3DEvent.getTickDelta());
            float f4 = (float)(particlePosition.x - VanillaChestLootTableGenerator.x);
            float f5 = (float)(particlePosition.y - VanillaChestLootTableGenerator.y);
            float particleState = (float)(particlePosition.z - VanillaChestLootTableGenerator.z);
            double particleState3 = Math.sqrt(f4 * f4 + f5 * f5 + particleState * particleState);
            if (particleState3 - (double)f > 48.0 || !frustumIntersection.testSphere(f4, f5, particleState, f)) {
                object2.closing = false;
                object2.closingAnimation.setReverse(false);
                continue;
            }
            arrayList.add(new ParticleState(object2, f4, f5, particleState, f3 * f2, particleState3));
        }
        if (arrayList.isEmpty()) {
            return;
        }
        arrayList.sort(Comparator.comparingDouble(ParticleState::getRenderDistance));
        for (ParticleState particleState : arrayList) {
            particleState.getParticle().closing = false;
        }
        float f7 = f * 0.35f;
        float f6 = f2 * 2.0f;
        ArrayList<ParticleState> arrayList2 = new ArrayList<ParticleState>(n);
        List<ParticleState> deferredParticles = new ArrayList<>();
        for (ParticleState n3 : arrayList) {
            float f8;
            if (arrayList2.size() >= n) {
                deferredParticles.add(n3);
                continue;
            }
            int matrix4f2 = -1;
            float f9 = 0.0f;
            for (int f11 = 0; f11 < arrayList2.size(); ++f11) {
                f8 = n3.distanceTo((ParticleState)arrayList2.get(f11));
                if (!(f8 < f7)) continue;
                matrix4f2 = f11;
                f9 = f8;
                break;
            }
            if (matrix4f2 < 0) {
                arrayList2.add(n3);
                continue;
            }
            ParticleState particleState = (ParticleState)arrayList2.get(matrix4f2);
            if (particleState.getIntensity() < f6 && (f8 = n3.getIntensity() * 0.35f * (1.0f - f9 / f7) * (1.0f - n3.getParticle().closingAnimation.getValue())) > 0.0f) {
                arrayList2.set(matrix4f2, particleState.withIntensityOffset(Math.min(f8, f6 - particleState.getIntensity())));
            }
            deferredParticles.add(n3);
        }
        List<DynamicLightShader.LightSource> lightSources = new ArrayList<>(arrayList2.size() + 16);
        for (ParticleState particleState : arrayList2) {
            particleState.getParticle().closing = true;
            this.isParticleBatchReady(lightSources, particleState, colorRGBA, f, particleState.getParticle().closingAnimation.update(1.0f));
        }
        int n2 = 0;
        for (ParticleState particleState : deferredParticles) {
            float f10 = particleState.getParticle().closingAnimation.update(0.0f);
            if (f10 <= 0.001f || n2 >= 16 || !this.isParticleBatchReady(lightSources, particleState, colorRGBA, f, f10)) continue;
            ++n2;
        }
        if (lightSources.isEmpty()) {
            return;
        }
        Matrix4f inverseViewProjection = new Matrix4f((Matrix4fc)matrix4f).invert();
        ShaderRenderer.particleLightRenderer.renderParticleLights(inverseViewProjection, 1.0f, lightSources);
    }

    private boolean isParticleBatchReady(List<DynamicLightShader.LightSource> list, ParticleState particleState, ColorRGBA colorRGBA, float f, float f2) {
        float f3 = particleState.getIntensity() * f2;
        if (f3 <= 0.001f) {
            return false;
        }
        list.add(new DynamicLightShader.LightSource(particleState.getX(), particleState.getY(), particleState.getZ(), f, colorRGBA.getRed() / 255.0f, colorRGBA.getGreen() / 255.0f, colorRGBA.getBlue() / 255.0f, f3));
        return true;
    }
}
