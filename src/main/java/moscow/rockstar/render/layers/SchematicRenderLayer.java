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
 *  net.minecraft.RenderPhase$Cull
 *  net.minecraft.RenderPhase$DepthTest
 *  net.minecraft.RenderPhase$Texture
 *  net.minecraft.RenderPhase$WriteMaskState
 *  net.minecraft.RenderPhase$TextureBase
 *  net.minecraft.TriState
 */
package moscow.rockstar.render.layers;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.TriState;

public final class SchematicRenderLayer {
    private static RenderLayer visibleLayer;
    private static RenderLayer hiddenLayer;

    private SchematicRenderLayer() {
    }

    public static RenderLayer getVisibleLayer() {
        if (visibleLayer == null) {
            visibleLayer = SchematicRenderLayer.createRenderLayer("rockstar_schematic", RenderPhase.LEQUAL_DEPTH_TEST, RenderPhase.ALL_MASK, RenderPhase.ENABLE_CULLING);
        }
        return visibleLayer;
    }

    public static RenderLayer getHiddenLayer() {
        if (hiddenLayer == null) {
            hiddenLayer = SchematicRenderLayer.createRenderLayer("rockstar_schematic_hidden", RenderPhase.BIGGER_DEPTH_TEST, RenderPhase.COLOR_MASK, RenderPhase.ENABLE_CULLING);
        }
        return hiddenLayer;
    }

    private static RenderLayer createRenderLayer(String string, RenderPhase.DepthTest class_46722, RenderPhase.WriteMaskState class_46862, RenderPhase.Cull class_46712) {
        RenderLayer.MultiPhaseParameters class_46882 = RenderLayer.MultiPhaseParameters.builder().program(RenderPhase.ENTITY_TRANSLUCENT_PROGRAM).texture((RenderPhase.TextureBase)new RenderPhase.Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, TriState.FALSE, true)).transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY).cull(class_46712).lightmap(RenderPhase.ENABLE_LIGHTMAP).overlay(RenderPhase.ENABLE_OVERLAY_COLOR).layering(RenderPhase.POLYGON_OFFSET_LAYERING).depthTest(class_46722).writeMaskState(class_46862).build(false);
        return RenderLayer.of((String)string, (VertexFormat)VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, (VertexFormat.DrawMode)VertexFormat.DrawMode.QUADS, (int)1536, (boolean)true, (boolean)false, (RenderLayer.MultiPhaseParameters)class_46882);
    }
}

