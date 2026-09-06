/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SpriteAtlasTexture
 *  net.minecraft.RenderLayer
 *  net.minecraft.RenderLayer$MultiPhaseParameters
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.RenderPhase
 *  net.minecraft.RenderPhase$Texture
 *  net.minecraft.RenderPhase$TextureBase
 *  net.minecraft.TriState
 */
package moscow.rockstar.render.layers;

import moscow.rockstar.render.shaders.ShaderProgramBase;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.TriState;

public final class BlockGhostRenderLayer {
    private static ShaderProgramBase shaderProgram;
    private static RenderLayer ghostLayer;

    private BlockGhostRenderLayer() {
    }

    public static void initialize(ShaderProgramBase shaderProgramBase) {
        shaderProgram = shaderProgramBase;
        ghostLayer = null;
    }

    public static boolean isReady() {
        return shaderProgram != null && shaderProgram.isShaderLoaded();
    }

    public static RenderLayer getGhostLayer() {
        if (ghostLayer == null) {
            ghostLayer = RenderLayer.of((String)"rockstar_block_ghost", (VertexFormat)VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, (VertexFormat.DrawMode)VertexFormat.DrawMode.QUADS, (int)1536, (boolean)true, (boolean)false, (RenderLayer.MultiPhaseParameters)RenderLayer.MultiPhaseParameters.builder().program(shaderProgram.createShaderLayer()).texture((RenderPhase.TextureBase)new RenderPhase.Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, TriState.FALSE, true)).transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY).cull(RenderPhase.ENABLE_CULLING).lightmap(RenderPhase.ENABLE_LIGHTMAP).overlay(RenderPhase.DISABLE_OVERLAY_COLOR).layering(RenderPhase.POLYGON_OFFSET_LAYERING).depthTest(RenderPhase.LEQUAL_DEPTH_TEST).writeMaskState(RenderPhase.ALL_MASK).build(false));
        }
        return ghostLayer;
    }
}

