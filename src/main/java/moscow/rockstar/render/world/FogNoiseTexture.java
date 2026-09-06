package moscow.rockstar.render.world;

import moscow.rockstar.core.ClientAccess;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

/**
 * Procedural 512x512 RGB noise field sampled by the volumetric fog shader as Sampler1.
 *
 * <p>Channels: R = 4-octave value fBm, G = the same field with the two highest-detail octaves
 * flattened to a constant (used as the distance LOD), B = a 3-octave patch field with its own
 * seed offset. The bake runs once on a background daemon thread; {@link #glId()} returns 0 until
 * the image is finished and uploaded, and the fog pass must not run while it does.</p>
 */
public class FogNoiseTexture implements ClientAccess {
    public static final float NOISE_PERIOD = 288.0f;
    public static final float PATCH_FACTOR = 0.25f;
    public static final float WORLD_PERIOD = 1152.0f;
    private static final int SIZE = 512;
    private static final int BASE_FREQUENCY = 16;
    private static final int OCTAVES = 4;
    private static final int DETAIL_OCTAVES = 2;
    private static final int PATCH_OCTAVES = 3;
    private static final float BASE_AMPLITUDE = 0.6f;
    private static final float AMPLITUDE_GAIN = 0.45f;
    private static final int PATCH_SEED = 7919;

    private NativeImageBackedTexture noiseTexture;
    private volatile NativeImage bakedImage;
    private boolean bakeStarted;

    /** GL texture id of the noise field, or 0 while it is still being generated. */
    public int glId() {
        if (this.noiseTexture != null) {
            return this.noiseTexture.getGlId();
        }
        NativeImage LootTableData = this.bakedImage;
        if (LootTableData != null) {
            this.bakedImage = null;
            this.noiseTexture = new NativeImageBackedTexture(LootTableData);
            this.noiseTexture.setFilter(true, false);
            return this.noiseTexture.getGlId();
        }
        if (!this.bakeStarted) {
            this.bakeStarted = true;
            Thread thread = new Thread(this::bake, "rockstar-fog-noise");
            thread.setDaemon(true);
            thread.start();
        }
        return 0;
    }

    private void bake() {
        NativeImage LootTableData = new NativeImage(SIZE, SIZE, false);
        for (int i = 0; i < SIZE; ++i) {
            for (int j = 0; j < SIZE; ++j) {
                float f;
                float f2 = 0.0f;
                float f3 = 0.0f;
                float f4 = 0.0f;
                float f5 = BASE_AMPLITUDE;
                int n = BASE_FREQUENCY;
                for (int k = 0; k < OCTAVES; ++k) {
                    f = FogNoiseTexture.valueNoise(j, i, n, k * 131);
                    f2 += f * f5;
                    f3 += (k < DETAIL_OCTAVES ? f : 0.5f) * f5;
                    f4 += f5;
                    f5 *= AMPLITUDE_GAIN;
                    n <<= 1;
                }
                float f6 = 0.0f;
                f = 0.0f;
                f5 = BASE_AMPLITUDE;
                n = BASE_FREQUENCY;
                for (int k = 0; k < PATCH_OCTAVES; ++k) {
                    f6 += FogNoiseTexture.valueNoise(j, i, n, PATCH_SEED + k * 131) * f5;
                    f += f5;
                    f5 *= AMPLITUDE_GAIN;
                    n <<= 1;
                }
                LootTableData.setColorArgb(j, i, 0xFF000000 | FogNoiseTexture.toByte(f2 / f4) << 16 | FogNoiseTexture.toByte(f3 / f4) << 8 | FogNoiseTexture.toByte(f6 / f));
            }
        }
        this.bakedImage = LootTableData;
    }

    private static float valueNoise(int n, int n2, int n3, int n4) {
        float f = (float)(n * n3) / (float)SIZE;
        float f2 = (float)(n2 * n3) / (float)SIZE;
        int n5 = (int)Math.floor(f);
        int n6 = (int)Math.floor(f2);
        float f3 = FogNoiseTexture.smoothStep(f - (float)n5);
        float f4 = FogNoiseTexture.smoothStep(f2 - (float)n6);
        int n7 = n3 - 1;
        int n8 = n5 & n7;
        int n9 = n5 + 1 & n7;
        int n10 = n6 & n7;
        int n11 = n6 + 1 & n7;
        float f5 = FogNoiseTexture.interpolate(FogNoiseTexture.hash(n8, n10, n4), FogNoiseTexture.hash(n9, n10, n4), f3);
        float f6 = FogNoiseTexture.interpolate(FogNoiseTexture.hash(n8, n11, n4), FogNoiseTexture.hash(n9, n11, n4), f3);
        return FogNoiseTexture.interpolate(f5, f6, f4);
    }

    private static float hash(int n, int n2, int n3) {
        int n4 = n * 374761393 + n2 * 668265263 + n3 * 1274126177;
        n4 = (n4 ^ n4 >>> 13) * 1274126177;
        n4 ^= n4 >>> 16;
        return (float)(n4 & 0xFFFFFF) / 1.6777215E7f;
    }

    private static float smoothStep(float f) {
        return f * f * (3.0f - 2.0f * f);
    }

    private static float interpolate(float f, float f2, float f3) {
        return f + (f2 - f) * f3;
    }

    private static int toByte(float f) {
        return Math.max(0, Math.min(255, Math.round(f * 255.0f)));
    }
}
