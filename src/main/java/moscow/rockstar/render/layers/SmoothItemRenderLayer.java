/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.RenderLayer
 *  net.minecraft.RenderLayer$MultiPhaseParameters
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.RenderPhase
 *  net.minecraft.RenderPhase$Texture
 *  net.minecraft.RenderPhase$TextureBase
 *  net.minecraft.TriState
 */
package moscow.rockstar.render.layers;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.render.shaders.ShaderProgramBase;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.TriState;

public final class SmoothItemRenderLayer {
    private static final Map<String, RenderLayer> LAYERS_BY_TEXTURE = new HashMap<String, RenderLayer>();
    private static ShaderProgramBase translucentShader;
    private static ShaderProgramBase cutoutShader;

    private SmoothItemRenderLayer() {
    }

    public static void initialize(ShaderProgramBase shaderProgramBase, ShaderProgramBase shaderProgramBase2) {
        translucentShader = shaderProgramBase;
        cutoutShader = shaderProgramBase2;
        LAYERS_BY_TEXTURE.clear();
    }

    public static boolean isReady() {
        return translucentShader != null && translucentShader.isShaderLoaded() && cutoutShader != null && cutoutShader.isShaderLoaded();
    }

    public static RenderLayer getCutoutLayer(Identifier class_29602) {
        return LAYERS_BY_TEXTURE.computeIfAbsent("cutout|" + String.valueOf(class_29602), string -> RenderLayer.of((String)"rockstar_item_cutout", (VertexFormat)VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, (VertexFormat.DrawMode)VertexFormat.DrawMode.QUADS, (int)1536, (boolean)true, (boolean)false, (RenderLayer.MultiPhaseParameters)RenderLayer.MultiPhaseParameters.builder().program(cutoutShader.createShaderLayer()).texture((RenderPhase.TextureBase)new SmoothAtlasTexture(class_29602)).transparency(RenderPhase.NO_TRANSPARENCY).lightmap(RenderPhase.ENABLE_LIGHTMAP).overlay(RenderPhase.ENABLE_OVERLAY_COLOR).build(true)));
    }

    public static RenderLayer getTranslucentLayer(Identifier class_29602) {
        return LAYERS_BY_TEXTURE.computeIfAbsent("translucent|" + String.valueOf(class_29602), string -> RenderLayer.of((String)"rockstar_item_translucent", (VertexFormat)VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, (VertexFormat.DrawMode)VertexFormat.DrawMode.QUADS, (int)1536, (boolean)true, (boolean)true, (RenderLayer.MultiPhaseParameters)RenderLayer.MultiPhaseParameters.builder().program(translucentShader.createShaderLayer()).texture((RenderPhase.TextureBase)new SmoothAtlasTexture(class_29602)).transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY).target(RenderPhase.ITEM_ENTITY_TARGET).lightmap(RenderPhase.ENABLE_LIGHTMAP).overlay(RenderPhase.ENABLE_OVERLAY_COLOR).writeMaskState(RenderPhase.ALL_MASK).build(true)));
    }

    static final class SmoothAtlasTexture
    extends RenderPhase.Texture {
        private final Identifier atlasTexture;

        SmoothAtlasTexture(Identifier class_29602) {
            super(class_29602, TriState.TRUE, false);
            this.atlasTexture = class_29602;
        }

        public void endDrawing() {
            super.endDrawing();
            MinecraftClient.getInstance().getTextureManager().getTexture(this.atlasTexture).setFilter(false, false);
        }
    }
}

