/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Position
 *  net.minecraft.Vec3d
 *  net.minecraft.Camera
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package moscow.rockstar.render.world;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.render.particles.AmbientParticleRenderer;
import moscow.rockstar.render.shaders.TimedEffectShader;
import moscow.rockstar.render.world.RainHeightmap;
import moscow.rockstar.render.world.RainOverlayShader;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.localization.Localization;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class WorldOverlayRenderer
implements ClientAccess {
    /** Units understood by script time settings. */
    public enum OverlayMode {
        HOURS(3600, "time.unit.hours"),
        MINUTES(60, "time.unit.minutes"),
        SECONDS(1, "time.unit.seconds");

        private final int secondsPerUnit;
        private final String localizationKey;

        OverlayMode(int secondsPerUnit, String localizationKey) {
            this.secondsPerUnit = secondsPerUnit;
            this.localizationKey = localizationKey;
        }

        public int getSecondsPerUnit() {
            return this.secondsPerUnit;
        }

        public String getDisplayName() {
            return Localization.translate(this.localizationKey);
        }
    }

    private static final float PARTICLE_OFFSET = 24.0f;
    private static final float POSITION_EPSILON = 0.034f;
    private static final float SHADER_DETAIL = 17.0f;
    private static final float PARTICLE_WIDTH = 42.0f;
    private static final float PARTICLE_HEIGHT = 30.0f;
    private static final float ZERO_VALUE = 0.0f;
    private static final int MAX_PARTICLE_COUNT = 4000;
    private static final int PARTICLE_RED = 178;
    private static final int PARTICLE_GREEN = 196;
    private static final int PARTICLE_BLUE = 214;
    private final AmbientParticleRenderer particleRenderer = new AmbientParticleRenderer();
    private final RainHeightmap cameraNode = new RainHeightmap();
    private final RainOverlayShader rainShader = new RainOverlayShader(RockstarClient.resourceId("rain_screen/data"));
    private final RainOverlayShader.UniformState rainUniforms = new RainOverlayShader.UniformState();
    private final Matrix4f modelViewProjection = new Matrix4f();
    private final Matrix4f inverseModelViewProjection = new Matrix4f();
    private final Vector3f cameraRight = new Vector3f();
    private final Vector3f cameraUp = new Vector3f();
    private final Animation rainIntensityAnimation = new Animation(350L, 1.0f, Easing.linear);

    public void renderRainOverlay(Camera class_41842, Matrix4f matrix4f, Matrix4f matrix4f2, float f, float f2, float f3, float f4) {
        if (WorldOverlayRenderer.minecraftClient.world == null || WorldOverlayRenderer.minecraftClient.player == null) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        this.cameraNode.updateHeightmap(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z);
        this.updateCameraUniforms(class_41842, f2);
        this.modelViewProjection.set((Matrix4fc)matrix4f2).mul((Matrix4fc)matrix4f);
        this.particleRenderer.renderAmbientParticles(this.modelViewProjection, this.getParticleCount(f), false, this.cameraNode.isReady() ? this.cameraNode.getTextureId() : null);
        float f5 = this.rainIntensityAnimation.update(WorldOverlayRenderer.minecraftClient.world.isSkyVisible(BlockPos.ofFloored((Position)WorldOverlayRenderer.minecraftClient.player.getEyePos())) ? 1.0f : 0.0f);
        float f6 = f3 * f5;
        if (f6 <= 0.01f && f4 <= 0.01f) {
            return;
        }
        TimedEffectShader.ParticleUniformState particleUniformState = this.particleRenderer.getParticleVertexState();
        this.rainUniforms.cameraX = particleUniformState.cameraX;
        this.rainUniforms.cameraY = particleUniformState.cameraY;
        this.rainUniforms.cameraZ = particleUniformState.cameraZ;
        this.rainUniforms.tintRed = 0.69803923f;
        this.rainUniforms.tintGreen = 0.76862746f;
        this.rainUniforms.tintBlue = 0.8392157f;
        this.rainUniforms.animationTime = particleUniformState.animationTime;
        this.rainUniforms.raindropStrength = f6;
        this.rainUniforms.splashStrength = f4;
        this.rainUniforms.roofParameterX = this.cameraNode.getOriginX();
        this.rainUniforms.roofParameterY = this.cameraNode.getOriginZ();
        this.rainUniforms.roofParameterZ = this.cameraNode.getWaterLevel();
        this.rainUniforms.roofHeight = 192.0f;
        this.rainUniforms.roofEnabled = this.cameraNode.isReady() ? 1.0f : 0.0f;
        this.rainUniforms.rainTextureId = this.cameraNode.getTextureWidth();
        this.inverseModelViewProjection.set((Matrix4fc)this.modelViewProjection).invert();
        this.rainShader.renderRainOverlay(this.inverseModelViewProjection, this.rainUniforms);
    }

    public void finishRainRender() {
        this.particleRenderer.finishParticleRender();
        this.cameraNode.releaseTexture();
    }

    private int getParticleCount(float f) {
        return Math.max(1, Math.round(4000.0f * f));
    }

    private void updateCameraUniforms(Camera class_41842, float f) {
        TimedEffectShader.ParticleUniformState particleUniformState = this.particleRenderer.getParticleVertexState();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        particleUniformState.cameraX = (float)VanillaChestLootTableGenerator.x;
        particleUniformState.cameraY = (float)VanillaChestLootTableGenerator.y;
        particleUniformState.cameraZ = (float)VanillaChestLootTableGenerator.z;
        Quaternionf quaternionf = class_41842.getRotation();
        quaternionf.transform(this.cameraRight.set(1.0f, 0.0f, 0.0f));
        quaternionf.transform(this.cameraUp.set(0.0f, 1.0f, 0.0f));
        particleUniformState.cameraRightX = this.cameraRight.x;
        particleUniformState.cameraRightY = this.cameraRight.y;
        particleUniformState.cameraRightZ = this.cameraRight.z;
        particleUniformState.cameraUpX = this.cameraUp.x;
        particleUniformState.cameraUpY = this.cameraUp.y;
        particleUniformState.cameraUpZ = this.cameraUp.z;
        particleUniformState.motionX = 0.0f;
        particleUniformState.motionY = -24.0f;
        particleUniformState.motionZ = 0.0f;
        particleUniformState.cellWidth = 42.0f;
        particleUniformState.cellHeight = 30.0f;
        particleUniformState.cellDepth = 42.0f;
        particleUniformState.particleSize = 0.034f;
        particleUniformState.stretch = 17.0f;
        particleUniformState.drift = 0.05f;
        particleUniformState.flicker = 0.0f;
        particleUniformState.tintRed = 0.69803923f;
        particleUniformState.tintGreen = 0.76862746f;
        particleUniformState.tintBlue = 0.8392157f;
        particleUniformState.tintAlpha = f;
        particleUniformState.roofOriginX = this.cameraNode.getOriginX();
        particleUniformState.roofOriginZ = this.cameraNode.getOriginZ();
        particleUniformState.roofHeight = this.cameraNode.getWaterLevel();
        particleUniformState.roofSpan = 192.0f;
        particleUniformState.roofEnabled = this.cameraNode.isReady() ? 1.0f : 0.0f;
        particleUniformState.animationTime = (float)(System.currentTimeMillis() % 3600000L) / 1000.0f;
    }
}
