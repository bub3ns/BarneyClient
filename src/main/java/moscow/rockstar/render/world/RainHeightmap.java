/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.NativeImage
 *  net.minecraft.NativeImageBackedTexture
 *  net.minecraft.AbstractTexture
 *  net.minecraft.Heightmap$Type
 *  net.minecraft.Identifier
 *  net.minecraft.MathHelper
 *  net.minecraft.ClientWorld
 */
package moscow.rockstar.render.world;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.world.Heightmap;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.world.ClientWorld;

public class RainHeightmap
implements ClientAccess {
    public static final int TEXTURE_SIZE = 96;
    public static final float CELL_SIZE = 2.0f;
    public static final float WORLD_SPAN = 192.0f;
    private static final long UPDATE_INTERVAL_MILLIS = 500L;
    private static final float HEIGHT_SAMPLE_OFFSET = 8.0f;
    private static final int COLOR_OFFSET = 128;
    private final Identifier textureId = RockstarClient.resourceId("dynamic/rain_heightmap");
    private NativeImageBackedTexture heightmapTexture;
    private boolean textureRegistered;
    private float originX;
    private float originZ;
    private float waterLevel;
    private long lastUpdateTime;
    private boolean heightmapReady;

    public Identifier getTextureId() {
        return this.textureId;
    }

    public boolean isReady() {
        return this.heightmapReady;
    }

    public int getTextureWidth() {
        return this.heightmapTexture == null ? 0 : this.heightmapTexture.getGlId();
    }

    public float getOriginX() {
        return this.originX;
    }

    public float getOriginZ() {
        return this.originZ;
    }

    public float getWaterLevel() {
        return this.waterLevel;
    }

    public void updateHeightmap(double d, double d2, double d3) {
        boolean bl;
        ClientWorld NarrationMessageBuilder = RainHeightmap.minecraftClient.world;
        if (NarrationMessageBuilder == null) {
            return;
        }
        long l = System.currentTimeMillis();
        float f = (float)d - 96.0f;
        float f2 = (float)d3 - 96.0f;
        boolean bl2 = bl = Math.abs(f - this.originX) > 8.0f || Math.abs(f2 - this.originZ) > 8.0f;
        if (this.heightmapReady && !bl && l - this.lastUpdateTime < 500L) {
            return;
        }
        this.lastUpdateTime = l;
        this.originX = f;
        this.originZ = f2;
        this.waterLevel = (float)d2;
        this.ensureTexture();
        NativeImage LootTableData = this.heightmapTexture.getImage();
        if (LootTableData == null) {
            return;
        }
        for (int i = 0; i < 96; ++i) {
            int n = (int)Math.floor(this.originZ + (float)i * 2.0f);
            for (int j = 0; j < 96; ++j) {
                int n2 = (int)Math.floor(this.originX + (float)j * 2.0f);
                int n3 = NarrationMessageBuilder.getTopY(Heightmap.Type.MOTION_BLOCKING, n2, n);
                int n4 = MathHelper.clamp((int)(Math.round((float)n3 - this.waterLevel) + 128), (int)0, (int)255);
                LootTableData.setColorArgb(j, i, 0xFF000000 | n4 << 16 | n4 << 8 | n4);
            }
        }
        this.heightmapTexture.upload();
        this.heightmapReady = true;
    }

    private void ensureTexture() {
        if (this.heightmapTexture == null) {
            this.heightmapTexture = new NativeImageBackedTexture(96, 96, false);
        }
        if (!this.textureRegistered) {
            minecraftClient.getTextureManager().registerTexture(this.textureId, (AbstractTexture)this.heightmapTexture);
            this.textureRegistered = true;
        }
    }

    public void releaseTexture() {
        if (this.heightmapTexture != null) {
            this.heightmapTexture.close();
            this.heightmapTexture = null;
        }
        if (this.textureRegistered) {
            minecraftClient.getTextureManager().destroyTexture(this.textureId);
            this.textureRegistered = false;
        }
        this.heightmapReady = false;
    }

    public static int getColorOffset() {
        return 128;
    }
}
