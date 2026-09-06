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

public class TimedAccentShader
extends ShaderProgramBase {
    private GlUniform shaderBufferExtra;
    private GlUniform shaderBufferSecondary;

    public TimedAccentShader(Identifier class_29602) {
        super(class_29602, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    public void dispatchFromFloatAndColorRGBA(float f, ColorRGBA colorRGBA) {
        if (this.shaderBufferExtra != null) {
            this.shaderBufferExtra.set(f);
        }
        if (this.shaderBufferSecondary != null && colorRGBA != null) {
            this.shaderBufferSecondary.set(colorRGBA.getRed() / 255.0f, colorRGBA.getGreen() / 255.0f, colorRGBA.getBlue() / 255.0f);
        }
    }

    @Override
    protected void initializeShaderUniforms() {
        this.shaderBufferExtra = this.getUniform("Time");
        this.shaderBufferSecondary = this.getUniform("Accent");
        super.initializeShaderUniforms();
    }
}

