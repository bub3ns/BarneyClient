/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Defines
 *  net.minecraft.ShaderLoader$LoadException
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.GlUniform
 *  net.minecraft.VertexFormat
 *  net.minecraft.Identifier
 *  net.minecraft.RenderPhase$ShaderProgram
 *  net.minecraft.ShaderProgram
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package moscow.rockstar.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.mixin.accessors.ShaderProgramAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.gl.ShaderProgram;
import org.jetbrains.annotations.ApiStatus;

public class ShaderProgramBase {
    private static final List<Runnable> shaderReloadTasks = new ArrayList<Runnable>();
    protected ShaderProgram shaderProgram;
    protected ShaderProgramKey shaderDefinition;

    public ShaderProgramBase(Identifier class_29602, VertexFormat class_2932) {
        this.shaderDefinition = new ShaderProgramKey(class_29602.withPrefixedPath("core/"), class_2932, Defines.EMPTY);
        shaderReloadTasks.add(() -> {
            try {
                this.shaderProgram = MinecraftClient.getInstance().getShaderLoader().getProgramToLoad(this.shaderDefinition);
                this.initializeShaderUniforms();
            }
            catch (ShaderLoader.LoadException class_101522) {
                throw new RuntimeException("Failed to initialize shader program", class_101522);
            }
        });
    }

    public RenderPhase.ShaderProgram createShaderLayer() {
        return new RenderPhase.ShaderProgram(this.shaderDefinition);
    }

    public boolean isShaderLoaded() {
        return this.shaderProgram != null;
    }

    public ShaderProgram bindShaderProgram() {
        return RenderSystem.setShader((ShaderProgramKey)this.shaderDefinition);
    }

    protected void initializeShaderUniforms() {
    }

    public GlUniform getUniform(String string) {
        if (this.shaderProgram == null) {
            try {
                this.shaderProgram = MinecraftClient.getInstance().getShaderLoader().getProgramToLoad(this.shaderDefinition);
            }
            catch (Throwable throwable) {
                return null;
            }
            if (this.shaderProgram == null) {
                return null;
            }
        }
        return ((ShaderProgramAccessor)this.shaderProgram).getUniformsByName().get(string);
    }

    @ApiStatus.Internal
    public static void reloadShaders() {
        ShaderProgramBase.reloadShaderPrograms(false);
    }

    @ApiStatus.Internal
    public static void reloadShaderPrograms(boolean bl) {
        for (Runnable runnable : shaderReloadTasks) {
            try {
                runnable.run();
            }
            catch (Throwable throwable) {
                if (bl) continue;
                StringBuilder stringBuilder = new StringBuilder();
                for (Throwable throwable2 = throwable; throwable2 != null && stringBuilder.length() < 2048; throwable2 = throwable2.getCause()) {
                    stringBuilder.append(stringBuilder.length() == 0 ? "" : "\n  caused by: ").append(throwable2);
                    if (throwable2 == throwable2.getCause()) break;
                }
                System.err.println("[Barney] Failed to (re)load a shader program, skipping: " + String.valueOf(stringBuilder));
            }
        }
    }
}
