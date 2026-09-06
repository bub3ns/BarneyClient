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

public class FloorShader
extends ShaderProgramBase {
    private GlUniform floorUniform;

    public FloorShader(Identifier class_29602) {
        super(class_29602, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    public void setFloor(float f) {
        if (this.floorUniform != null) {
            this.floorUniform.set(f);
        }
    }

    @Override
    protected void initializeShaderUniforms() {
        this.floorUniform = this.getUniform("Floor");
        super.initializeShaderUniforms();
    }
}

