package moscow.rockstar.render.world;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.shaders.VolumetricFogShader;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import pyrock.utility.render.ColorRGBA;

/** Builds the fog uniforms from the current camera and submits the fog pass. */
public final class VolumetricFogRenderer implements ClientAccess {
    private static final float DENSITY_SCALE = 0.07f;
    private static final float FLOW_X_SPEED = 0.0224f;
    private static final float FLOW_Z_SPEED = 0.0152f;
    private static final float PATCH_X_SPEED = -0.0091f;
    private static final float PATCH_Z_SPEED = 0.0134f;
    private static final float MAX_DISTANCE = 160.0f;
    private static final float STEP_WORLD_LOW = 5.0f;
    private static final float STEP_WORLD_HIGH = 3.5f;

    private final FogNoiseTexture noise = new FogNoiseTexture();
    private final VolumetricFogPass pass = new VolumetricFogPass();
    private final VolumetricFogShader.FogUniforms uniforms = new VolumetricFogShader.FogUniforms();
    private final Matrix4f inverseViewProjection = new Matrix4f();

    public VolumetricFogRenderer() {
        this.pass.load();
    }

    public void renderFog(Camera camera, Matrix4f modelView, Matrix4f projection, ColorRGBA color,
                           float density, float coverage, float layerY, float thickness, boolean highQuality) {
        this.uniforms.noiseTexture = this.noise.glId();
        if (this.uniforms.noiseTexture == 0) {
            return;
        }
        this.uniforms.highQuality = highQuality;
        this.uniforms.stepWorld = highQuality ? STEP_WORLD_HIGH : STEP_WORLD_LOW;
        Vec3d class_2432 = camera.getPos();
        this.uniforms.cameraX = VolumetricFogRenderer.wrap(class_2432.x, FogNoiseTexture.WORLD_PERIOD);
        this.uniforms.cameraY = (float)class_2432.y;
        this.uniforms.cameraZ = VolumetricFogRenderer.wrap(class_2432.z, FogNoiseTexture.WORLD_PERIOD);
        this.uniforms.tintRed = color.getRed() / 255.0f;
        this.uniforms.tintGreen = color.getGreen() / 255.0f;
        this.uniforms.tintBlue = color.getBlue() / 255.0f;
        this.uniforms.density = density * DENSITY_SCALE;
        this.uniforms.coverage = 0.75f - coverage * 0.55f;
        this.uniforms.layerY = layerY;
        this.uniforms.thickness = thickness;
        this.uniforms.tileScale = 0.0034722222f;
        this.uniforms.maxDistance = Math.min(MAX_DISTANCE, (float)((Integer)VolumetricFogRenderer.minecraftClient.options.getViewDistance().getValue()).intValue() * 16.0f);
        double d = (double)(System.currentTimeMillis() % 3600000L) / 1000.0;
        this.uniforms.flowX = VolumetricFogRenderer.wrap((double)FLOW_X_SPEED * d, FogNoiseTexture.NOISE_PERIOD);
        this.uniforms.flowZ = VolumetricFogRenderer.wrap((double)FLOW_Z_SPEED * d, FogNoiseTexture.NOISE_PERIOD);
        this.uniforms.patchX = VolumetricFogRenderer.wrap((double)PATCH_X_SPEED * d, FogNoiseTexture.WORLD_PERIOD);
        this.uniforms.patchZ = VolumetricFogRenderer.wrap((double)PATCH_Z_SPEED * d, FogNoiseTexture.WORLD_PERIOD);
        this.inverseViewProjection.set((Matrix4fc)projection).mul((Matrix4fc)modelView).invert();
        this.pass.render(this.inverseViewProjection, this.uniforms);
    }

    /** Positive-modulo wrap; keeps world/flow coordinates inside the noise texture's tiling period. */
    private static float wrap(double d, float f) {
        return (float)(d - Math.floor(d / (double)f) * (double)f);
    }
}
