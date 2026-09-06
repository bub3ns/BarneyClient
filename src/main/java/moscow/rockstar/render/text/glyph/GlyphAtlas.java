package moscow.rockstar.render.text.glyph;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import moscow.rockstar.render.gl.OpenGlStateReset;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.system.MemoryUtil;

/** GPU atlas for the curve, band-index, and glyph-table textures used by Slug. */
public final class GlyphAtlas {
    private static final GlyphAtlas INSTANCE = new GlyphAtlas();
    private final GlyphAtlasBuffer data = new GlyphAtlasBuffer();
    private int curveTexture;
    private int bandTexture;
    private int glyphTableTexture;
    private int uploadedCurveHeight;
    private int uploadedBandHeight;
    private int uploadedGlyphTableHeight;
    private boolean uploadPending;

    private GlyphAtlas() {
    }

    public static GlyphAtlas getInstance() {
        return INSTANCE;
    }

    public synchronized int register(GlyphSpatialIndex spatialIndex) {
        int index = this.data.addGlyph(spatialIndex);
        this.uploadPending = true;
        return index;
    }

    public int getCurveTexture() {
        return this.curveTexture;
    }

    public int getBandTexture() {
        return this.bandTexture;
    }

    public int getGlyphTableTexture() {
        return this.glyphTableTexture;
    }

    public synchronized void uploadPending() {
        if (!this.uploadPending || !RenderSystem.isOnRenderThread()) {
            return;
        }
        this.uploadPending = false;
        int previousTexture = GlStateManager._getInteger(32873);
        this.curveTexture = uploadFloatTexture(this.curveTexture, this.uploadedCurveHeight, this.data.getCurveTextureHeight(), GlyphAtlasBuffer.CURVE_TEXTURE_WIDTH, 34842, this.data.getCurveData(), this.data.getUsedCurveHeight(), this.data.isCurveDataDirty(), 4);
        this.uploadedCurveHeight = this.data.getCurveTextureHeight();
        this.bandTexture = uploadIntTexture(this.bandTexture, this.uploadedBandHeight, this.data.getBandTextureHeight(), this.data.getBandData(), this.data.getUsedBandHeight(), this.data.isBandDataDirty());
        this.uploadedBandHeight = this.data.getBandTextureHeight();
        this.glyphTableTexture = uploadFloatTexture(this.glyphTableTexture, this.uploadedGlyphTableHeight, this.data.getGlyphTableTextureHeight(), GlyphAtlasBuffer.GLYPH_TABLE_WIDTH, 34836, this.data.getGlyphTable(), this.data.getUsedGlyphTableHeight(), this.data.isGlyphTableDirty(), 4);
        this.uploadedGlyphTableHeight = this.data.getGlyphTableTextureHeight();
        this.data.clearDirtyFlags();
        GlStateManager._bindTexture(previousTexture);
    }

    private static int uploadFloatTexture(int texture, int previousHeight, int allocatedHeight, int width, int internalFormat, float[] values, int usedHeight, boolean resized, int channels) {
        if (usedHeight <= 0 && texture != 0 && !resized) {
            return texture;
        }
        boolean recreate = texture == 0 || resized || previousHeight != allocatedHeight;
        if (texture == 0) {
            texture = GlStateManager._genTexture();
        }
        GlStateManager._bindTexture(texture);
        OpenGlStateReset.resetPixelUnpackState();
        if (recreate) {
            configureTextureParameters();
            FloatBuffer upload = MemoryUtil.memAllocFloat(width * allocatedHeight * channels);
            upload.put(values, 0, Math.min(values.length, upload.remaining()));
            while (upload.hasRemaining()) {
                upload.put(0.0f);
            }
            upload.flip();
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, allocatedHeight, 0, GL11.GL_RGBA, GL11.GL_FLOAT, upload);
            MemoryUtil.memFree((Buffer)upload);
            return texture;
        }
        int rows = Math.min(usedHeight, allocatedHeight);
        rows = Math.min(rows, values.length / (width * channels));
        if (rows <= 0) {
            return texture;
        }
        FloatBuffer upload = MemoryUtil.memAllocFloat(width * rows * channels);
        upload.put(values, 0, width * rows * channels).flip();
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, rows, GL11.GL_RGBA, GL11.GL_FLOAT, upload);
        MemoryUtil.memFree((Buffer)upload);
        return texture;
    }

    private static int uploadIntTexture(int texture, int previousHeight, int allocatedHeight, int[] values, int usedHeight, boolean resized) {
        if (usedHeight <= 0 && texture != 0 && !resized) {
            return texture;
        }
        boolean recreate = texture == 0 || resized || previousHeight != allocatedHeight;
        if (texture == 0) {
            texture = GlStateManager._genTexture();
        }
        GlStateManager._bindTexture(texture);
        OpenGlStateReset.resetPixelUnpackState();
        int rows = recreate ? allocatedHeight : Math.min(usedHeight, allocatedHeight);
        if (rows <= 0) {
            return texture;
        }
        ShortBuffer upload = MemoryUtil.memAllocShort(GlyphAtlasBuffer.BAND_TEXTURE_WIDTH * rows * 2);
        int valuesToCopy = Math.min(values.length, GlyphAtlasBuffer.BAND_TEXTURE_WIDTH * rows * 2);
        for (int index = 0; index < valuesToCopy; index++) {
            upload.put((short) values[index]);
        }
        while (upload.hasRemaining()) {
            upload.put((short) 0);
        }
        upload.flip();
        if (recreate) {
            configureTextureParameters();
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, 33338, GlyphAtlasBuffer.BAND_TEXTURE_WIDTH, allocatedHeight, 0, 33320, GL11.GL_UNSIGNED_SHORT, upload);
        } else {
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, GlyphAtlasBuffer.BAND_TEXTURE_WIDTH, rows, 33320, GL11.GL_UNSIGNED_SHORT, upload);
        }
        MemoryUtil.memFree((Buffer)upload);
        return texture;
    }

    private static void configureTextureParameters() {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 33084, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 33085, 0);
    }
}
