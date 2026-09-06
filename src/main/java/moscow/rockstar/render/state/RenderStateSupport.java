package moscow.rockstar.render.state;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.render.text.glyph.GlyphAtlas;

/** Small, named render-state operations shared by the batched renderers. */
public final class RenderStateSupport {
    private RenderStateSupport() {
    }

    /** Binds the glyph atlas textures to the requested shader texture units. */
    public static void bindGlyphAtlasTextures(int curveUnit, int bandUnit, int tableUnit) {
        GlyphAtlas atlas = GlyphAtlas.getInstance();
        atlas.uploadPending();
        RenderSystem.setShaderTexture(curveUnit, atlas.getCurveTexture());
        RenderSystem.setShaderTexture(bandUnit, atlas.getBandTexture());
        RenderSystem.setShaderTexture(tableUnit, atlas.getGlyphTableTexture());
    }

    public static void resetTextureUnits() {
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.setShaderTexture(1, 0);
        RenderSystem.setShaderTexture(2, 0);
    }

    public static void resetPixelStore() {
        GlStateManager._pixelStore(3314, 0);
        GlStateManager._pixelStore(3315, 0);
        GlStateManager._pixelStore(3316, 0);
        GlStateManager._pixelStore(3317, 4);
    }

    public static void finishBatch() {
        resetTextureUnits();
    }
}
