/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GlUniform
 *  net.minecraft.VertexFormats
 *  net.minecraft.Identifier
 */
package moscow.rockstar.render.shaders;

import moscow.rockstar.render.shaders.ShaderProgramBase;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import pyrock.utility.render.ColorRGBA;

public class ItemEffectShader
extends ShaderProgramBase {
    private GlUniform timeUniform;
    private GlUniform accentUniform;
    private GlUniform itemCenterUniform;

    public ItemEffectShader(Identifier class_29602) {
        super(class_29602, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    public void setEffectParameters(float f, ColorRGBA colorRGBA, float f2, float f3) {
        if (this.timeUniform != null) {
            this.timeUniform.set(f);
        }
        if (this.accentUniform != null && colorRGBA != null) {
            this.accentUniform.set(colorRGBA.getRed() / 255.0f, colorRGBA.getGreen() / 255.0f, colorRGBA.getBlue() / 255.0f);
        }
        if (this.itemCenterUniform != null) {
            this.itemCenterUniform.set(f2, f3);
        }
    }

    @Override
    protected void initializeShaderUniforms() {
        this.timeUniform = this.getUniform("Time");
        this.accentUniform = this.getUniform("Accent");
        this.itemCenterUniform = this.getUniform("ItemCenter");
        super.initializeShaderUniforms();
    }
}

