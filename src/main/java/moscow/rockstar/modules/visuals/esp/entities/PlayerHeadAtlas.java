package moscow.rockstar.modules.visuals.esp.entities;

import com.mojang.blaze3d.platform.GlStateManager;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.gl.OpenGlStateReset;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * Port of {@code rockstar/ilIlil/iIiiIiiIi}.
 *
 * <p>A 128x128 atlas of 8x8 player heads.  Each entry is the skin's face layer
 * (8,8) composited under the hat layer (40,8) so the nametag batch can draw the
 * head as one textured quad inside the same draw call as the background.</p>
 *
 * <p>The original client uses this class only from the ESP nametag overlay.</p>
 */
public class PlayerHeadAtlas implements ClientAccess {
    private static final int HEAD_SIZE = 8;
    private static final int COLUMNS = 16;
    private static final int ATLAS_SIZE = 128;

    private NativeImage atlasImage;
    private NativeImageBackedTexture atlasTexture;
    private final Map<Identifier, Integer> headIndices = new HashMap<Identifier, Integer>();
    private int nextIndex = 0;
    private boolean dirty = false;
    private boolean uploaded = false;

    private void ensureAtlas() {
        if (this.atlasImage == null) {
            this.atlasImage = new NativeImage(128, 128, true);
            this.atlasTexture = new NativeImageBackedTexture(this.atlasImage);
            this.atlasTexture.setFilter(false, false);
        }
    }

    public int getHeadIndex(Identifier class_29602) {
        this.ensureAtlas();
        Integer n = this.headIndices.get(class_29602);
        if (n != null) {
            return n;
        }
        if (this.headIndices.size() >= 256) {
            this.headIndices.clear();
            this.nextIndex = 0;
        }
        n = this.nextIndex++;
        if (!this.captureHead(class_29602, n)) {
            this.headIndices.remove(class_29602);
            --this.nextIndex;
            return -1;
        }
        this.headIndices.put(class_29602, n);
        this.dirty = true;
        return n;
    }

    public void upload() {
        if (this.atlasTexture == null) {
            return;
        }
        if (this.dirty || !this.uploaded) {
            this.atlasTexture.upload();
            this.dirty = false;
            this.uploaded = true;
        }
    }

    public int getTextureId() {
        this.ensureAtlas();
        return this.atlasTexture.getGlId();
    }

    public float getU(int n) {
        return (float)(n % 16 * 8) / 128.0f;
    }

    public float getV(int n) {
        return (float)(n / 16 * 8) / 128.0f;
    }

    public float getTileSize() {
        return 0.0625f;
    }

    private boolean captureHead(Identifier class_29602, int n) {
        AbstractTexture class_10442 = minecraftClient.getTextureManager().getTexture(class_29602);
        int n2 = class_10442.getGlId();
        if (n2 <= 0) {
            return false;
        }
        GlStateManager._bindTexture((int)n2);
        int n3 = GL11.glGetTexLevelParameteri((int)3553, (int)0, (int)4096);
        int n4 = GL11.glGetTexLevelParameteri((int)3553, (int)0, (int)4097);
        if (n3 < 64 || n4 < 64) {
            return false;
        }
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer((int)(n3 * n4 * 4));
        OpenGlStateReset.resetPixelPackState();
        GL11.glGetTexImage((int)3553, (int)0, (int)6408, (int)5121, (ByteBuffer)byteBuffer);
        int n5 = n % 16 * 8;
        int n6 = n / 16 * 8;
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                int n7 = this.readPixel(byteBuffer, n3, 8 + j, 8 + i);
                int n8 = this.readPixel(byteBuffer, n3, 40 + j, 8 + i);
                this.atlasImage.setColorArgb(n5 + j, n6 + i, this.compositeOver(n8, n7));
            }
        }
        return true;
    }

    private int readPixel(ByteBuffer byteBuffer, int n, int n2, int n3) {
        int n4 = (n3 * n + n2) * 4;
        int n5 = byteBuffer.get(n4) & 0xFF;
        int n6 = byteBuffer.get(n4 + 1) & 0xFF;
        int n7 = byteBuffer.get(n4 + 2) & 0xFF;
        int n8 = byteBuffer.get(n4 + 3) & 0xFF;
        return n8 << 24 | n5 << 16 | n6 << 8 | n7;
    }

    private int compositeOver(int n, int n2) {
        float f = (float)(n >> 24 & 0xFF) / 255.0f;
        float f2 = (float)(n2 >> 24 & 0xFF) / 255.0f;
        float f3 = f + f2 * (1.0f - f);
        if (f3 <= 0.0f) {
            return 0;
        }
        int n3 = n >> 16 & 0xFF;
        int n4 = n >> 8 & 0xFF;
        int n5 = n & 0xFF;
        int n6 = n2 >> 16 & 0xFF;
        int n7 = n2 >> 8 & 0xFF;
        int n8 = n2 & 0xFF;
        int n9 = Math.round(((float)n3 * f + (float)n6 * f2 * (1.0f - f)) / f3);
        int n10 = Math.round(((float)n4 * f + (float)n7 * f2 * (1.0f - f)) / f3);
        int n11 = Math.round(((float)n5 * f + (float)n8 * f2 * (1.0f - f)) / f3);
        int n12 = Math.round(f3 * 255.0f);
        return n12 << 24 | n9 << 16 | n10 << 8 | n11;
    }
}
