package moscow.rockstar.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.render.text.glyph.GlyphAtlas;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

/**
 * The Slug font shader needs the glyph atlas bound before the shader is
 * selected.  This is part of the original renderer contract; it is not a
 * fallback renderer.
 */
public final class SlugFontShader extends ShaderProgramBase {
    public SlugFontShader(Identifier resource) {
        super(resource, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
    }

    @Override
    public ShaderProgram bindShaderProgram() {
        GlyphAtlas atlas = GlyphAtlas.getInstance();
        atlas.uploadPending();
        RenderSystem.setShaderTexture(0, atlas.getCurveTexture());
        RenderSystem.setShaderTexture(1, atlas.getBandTexture());
        RenderSystem.setShaderTexture(2, atlas.getGlyphTableTexture());
        return super.bindShaderProgram();
    }
}
