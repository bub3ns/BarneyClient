package moscow.rockstar.modules.visuals.accessories.renderers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.accessories.Accessories;
import moscow.rockstar.render.geometry.CubeRenderer;
import moscow.rockstar.render.item.ItemRenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pyrock.utility.render.ColorRGBA;

import java.util.ArrayList;
import java.util.List;

public class HeadCrystalRenderer {

    public static class LightPoint {
        public final float x;
        public final float y;
        public final float z;
        public final float fade;

        public LightPoint(float x, float y, float z, float fade) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.fade = fade;
        }
    }

    private static class CrystalParticle {
        long spawnTime;
        long lifetimeMs;
        float targetScale;
        float yawSpinDirection;
        float pitchSpinDirection;
        float yawSpinRate;
        float pitchSpinRate;
        float initialYawSpin;
        float initialPitchSpin;

        float pitchTilt;
        float yawOffset;
        float rollTilt;
        float angleOffset;
        float heightOffset;

        void respawn(long now, float minLifeSec, float maxLifeSec, float minScale, float maxScale, float baseAngle) {
            this.spawnTime = now;
            float actualMinLife = Math.max(0.1f, Math.min(minLifeSec, maxLifeSec));
            float actualMaxLife = Math.max(actualMinLife, Math.max(minLifeSec, maxLifeSec));
            float lifeSec = actualMinLife + (float) Math.random() * (actualMaxLife - actualMinLife);
            this.lifetimeMs = (long) (lifeSec * 1000.0f);

            float actualMinScale = Math.max(0.002f, Math.min(minScale, maxScale));
            float actualMaxScale = Math.max(actualMinScale, Math.max(minScale, maxScale));
            this.targetScale = actualMinScale + (float) Math.random() * (actualMaxScale - actualMinScale);

            this.yawSpinDirection = Math.random() < 0.5 ? -1.0f : 1.0f;
            this.pitchSpinDirection = Math.random() < 0.5 ? -1.0f : 1.0f;
            this.yawSpinRate = 0.75f + (float) Math.random() * 0.5f;
            this.pitchSpinRate = 0.75f + (float) Math.random() * 0.5f;
            this.initialYawSpin = (float) (Math.random() * 360.0);
            this.initialPitchSpin = (float) (Math.random() * 360.0);

            this.pitchTilt = 12.0f + (-8.0f + (float) Math.random() * 16.0f);
            this.yawOffset = -20.0f + (float) Math.random() * 40.0f;
            this.rollTilt = -10.0f + (float) Math.random() * 20.0f;
            this.angleOffset = baseAngle + (-5.0f + (float) Math.random() * 10.0f);
            this.heightOffset = -0.03f + (float) Math.random() * 0.06f;
        }
    }

    private CrystalParticle[] particles = new CrystalParticle[0];
    private final List<LightPoint> currentLights = new ArrayList<>();

    public List<LightPoint> getCurrentLights() {
        return this.currentLights;
    }

    private void ensureParticles(int count, long now, float minLife, float maxLife, float minScale, float maxScale) {
        if (this.particles.length != count) {
            CrystalParticle[] newParticles = new CrystalParticle[count];
            for (int i = 0; i < count; i++) {
                newParticles[i] = new CrystalParticle();
                float baseAngle = (float) (i * (360.0 / count));
                newParticles[i].respawn(now, minLife, maxLife, minScale, maxScale, baseAngle);
                newParticles[i].spawnTime = now - (long) (Math.random() * newParticles[i].lifetimeMs);
            }
            this.particles = newParticles;
        }
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ColorRGBA color, Accessories accessories) {
        if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }

        int count = Math.max(1, (int) accessories.getAmount().getValue());
        float radius = accessories.getRadius().getValue();
        float baseY = -accessories.getHeight().getValue();
        float minScale = accessories.getMinScale().getValue();
        float maxScale = accessories.getMaxScale().getValue();
        float minLife = accessories.getMinLifetime().getValue();
        float maxLife = accessories.getMaxLifetime().getValue();
        float orbitSpeed = accessories.getOrbitSpeed().getValue();
        float rotationSpeed = accessories.getRotationSpeed().getValue();
        boolean renderBloom = accessories.getBloom().isEnabled();
        float bloomBaseSize = accessories.getBloomSize().getValue();

        long now = System.currentTimeMillis();
        this.ensureParticles(count, now, minLife, maxLife, minScale, maxScale);

        double timeSec = (double) (now % 86400000L) / 1000.0;
        float orbitAngle = Math.abs(orbitSpeed) <= 0.001f ? 0.0f : (float) (((timeSec * (double) orbitSpeed * 60.0) % 360.0 + 360.0) % 360.0);
        float bobbing = (float) Math.sin(((now % 2500L) / 2500.0) * Math.PI * 2.0) * 0.025f;

        matrices.push();
        matrices.translate(0.0f, baseY + bobbing, 0.0f);

        float[] posX = new float[count];
        float[] posZ = new float[count];
        float[] posY = new float[count];
        float[] currentScales = new float[count];
        float[] currentFades = new float[count];
        float[] currentYaws = new float[count];
        float[] currentPitches = new float[count];
        float[] currentRolls = new float[count];
        float[] currentYawSpins = new float[count];
        float[] currentPitchSpins = new float[count];

        for (int i = 0; i < count; i++) {
            CrystalParticle p = this.particles[i];
            long elapsed = now - p.spawnTime;
            if (elapsed >= p.lifetimeMs || elapsed < 0) {
                float baseAngle = (float) (i * (360.0 / count));
                p.respawn(now, minLife, maxLife, minScale, maxScale, baseAngle);
                elapsed = 0;
            }

            float progress = p.lifetimeMs > 0 ? (float) elapsed / (float) p.lifetimeMs : 0.0f;
            progress = Math.max(0.0f, Math.min(1.0f, progress));

            float fade;
            if (progress < 0.08f) {
                float t = progress / 0.08f;
                fade = t * t * (3.0f - 2.0f * t);
            } else if (progress > 0.92f) {
                float t = (1.0f - progress) / 0.08f;
                fade = t * t * (3.0f - 2.0f * t);
            } else {
                fade = 1.0f;
            }
            currentFades[i] = fade;

            currentScales[i] = p.targetScale * (0.35f + 0.65f * fade);

            float angle = p.angleOffset + orbitAngle;
            double rad = Math.toRadians(angle);
            posX[i] = (float) (Math.cos(rad) * radius);
            posZ[i] = (float) (Math.sin(rad) * radius);
            posY[i] = p.heightOffset;

            currentYaws[i] = -angle + 90.0f + p.yawOffset;
            currentPitches[i] = p.pitchTilt;
            currentRolls[i] = p.rollTilt;

            float elapsedSec = (float) elapsed / 1000.0f;
            currentYawSpins[i] = p.initialYawSpin + p.yawSpinDirection * (rotationSpeed * p.yawSpinRate * elapsedSec * 120.0f);
            currentPitchSpins[i] = p.initialPitchSpin + p.pitchSpinDirection * (rotationSpeed * p.pitchSpinRate * elapsedSec * 100.0f);
        }

        Vector3f[] cameraRelativePositions = new Vector3f[count];

        // 1. Draw 3D crystals batch using CubeRenderer with solid alpha blending and depth writing
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515);
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();

        BufferBuilder cubeBuffer = CubeRenderer.beginCubeBatch();
        for (int i = 0; i < count; i++) {
            if (currentFades[i] <= 0.01f || currentScales[i] <= 0.001f) {
                continue;
            }
            matrices.push();
            matrices.translate(posX[i], posY[i], posZ[i]);

            Matrix4f posMat = matrices.peek().getPositionMatrix();
            cameraRelativePositions[i] = new Vector3f(posMat.m30(), posMat.m31(), posMat.m32());

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(currentYaws[i]));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(currentPitches[i]));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(currentRolls[i]));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(currentYawSpins[i]));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(currentPitchSpins[i]));

            float alpha = 255.0f * currentFades[i];
            CubeRenderer.drawCube(matrices, cubeBuffer, 0.0f, 0.0f, 0.0f, currentScales[i], color.withAlpha(alpha));
            matrices.pop();
        }
        ItemRenderUtils.flushVertexConsumer(cubeBuffer);

        // Cache positions for World Light pass
        this.currentLights.clear();
        for (int i = 0; i < count; i++) {
            if (cameraRelativePositions[i] != null && currentFades[i] > 0.05f) {
                this.currentLights.add(new LightPoint(
                    cameraRelativePositions[i].x,
                    cameraRelativePositions[i].y,
                    cameraRelativePositions[i].z,
                    currentFades[i]
                ));
            }
        }

        // 2. Draw Bloom / Glow sprites with additive blending and depth test (halo around solid crystals)
        if (renderBloom && bloomBaseSize > 0.001f) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(515);

            Identifier bloomTexture = RockstarClient.resourceId("textures/bloom.png");
            RenderSystem.setShaderTexture(0, bloomTexture);
            RenderSystem.setShader((ShaderProgramKey) ShaderProgramKeys.POSITION_TEX_COLOR);
            BufferBuilder bloomBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            Quaternionf cameraRotation = camera != null ? camera.getRotation() : new Quaternionf();

            for (int i = 0; i < count; i++) {
                if (cameraRelativePositions[i] == null || currentFades[i] <= 0.01f || currentScales[i] <= 0.001f) {
                    continue;
                }

                float bloomScale = bloomBaseSize * (0.7f + 0.3f * Math.min(2.0f, currentScales[i] / 0.10f)) * (0.8f + 0.2f * currentFades[i]);
                float bloomAlpha = color.getAlpha() * 0.30f * currentFades[i];

                MatrixStack bloomStack = new MatrixStack();
                bloomStack.translate(cameraRelativePositions[i].x, cameraRelativePositions[i].y, cameraRelativePositions[i].z);
                bloomStack.multiply(cameraRotation);

                appendCustomTexturedQuad(
                    bloomStack,
                    bloomBuffer,
                    -bloomScale / 2.0,
                    -bloomScale / 2.0,
                    0.0,
                    bloomScale,
                    bloomScale,
                    color.withAlpha(bloomAlpha),
                    0.02f, 0.98f, 0.02f, 0.98f
                );
            }
            ItemRenderUtils.flushVertexConsumer(bloomBuffer);
        }

        // Restore clean render state
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);

        matrices.pop();
    }

    private static void appendCustomTexturedQuad(
        MatrixStack matrices, BufferBuilder buffer,
        double x, double y, double z,
        double w, double h,
        ColorRGBA color,
        float u1, float u2, float v1, float v2
    ) {
        if (w <= 0.0 || h <= 0.0 || color.getAlpha() <= 0.0f) {
            return;
        }
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        int c = color.getRGB();
        buffer.vertex(matrix4f, (float) x, (float) (y + h), (float) z).texture(u1, v2).color(c);
        buffer.vertex(matrix4f, (float) (x + w), (float) (y + h), (float) z).texture(u2, v2).color(c);
        buffer.vertex(matrix4f, (float) (x + w), (float) y, (float) z).texture(u2, v1).color(c);
        buffer.vertex(matrix4f, (float) x, (float) y, (float) z).texture(u1, v1).color(c);
    }
}
