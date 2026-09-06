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
 *  net.minecraft.RenderPhase$ShaderProgram
 *  net.minecraft.TriState
 */
package moscow.rockstar.render.layers;

import java.util.HashMap;
import java.util.Map;
import moscow.rockstar.render.shaders.ShaderProgramBase;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.TriState;

public final class EntityGlowRenderLayer {
    private static final Map<Identifier, RenderLayer> LAYERS_BY_TEXTURE = new HashMap<Identifier, RenderLayer>();
    private static RenderPhase.ShaderProgram glowShader;

    private EntityGlowRenderLayer() {
    }

    public static void initialize(ShaderProgramBase shader) {
        glowShader = shader.createShaderLayer();
        LAYERS_BY_TEXTURE.clear();
    }

    public static RenderLayer getGlowLayer(Identifier class_29602) {
        if (glowShader == null) {
            return RenderLayer.getEntityCutoutNoCull((Identifier)class_29602);
        }
        return LAYERS_BY_TEXTURE.computeIfAbsent(class_29602, EntityGlowRenderLayer::createGlowLayer);
    }

    private static RenderLayer createGlowLayer(Identifier class_29602) {
        RenderLayer.MultiPhaseParameters class_46882 = RenderLayer.MultiPhaseParameters.builder().program(glowShader).texture((RenderPhase.TextureBase)new RenderPhase.Texture(class_29602, TriState.FALSE, false)).transparency(RenderPhase.NO_TRANSPARENCY).cull(RenderPhase.DISABLE_CULLING).lightmap(RenderPhase.DISABLE_LIGHTMAP).overlay(RenderPhase.DISABLE_OVERLAY_COLOR).writeMaskState(RenderPhase.ALL_MASK).build(false);
        return RenderLayer.of((String)"rockstar_glow_entity", (VertexFormat)VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, (VertexFormat.DrawMode)VertexFormat.DrawMode.QUADS, (int)1536, (boolean)true, (boolean)false, (RenderLayer.MultiPhaseParameters)class_46882);
    }
}
