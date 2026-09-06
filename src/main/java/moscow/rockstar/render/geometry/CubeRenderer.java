/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.MatrixStack
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package moscow.rockstar.render.geometry;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import pyrock.utility.render.ColorRGBA;

public final class CubeRenderer {
    private static final Vector3f[] CUBE_VERTICES = new Vector3f[]{new Vector3f(0.0f, 1.5f, 0.0f), new Vector3f(0.0f, -1.5f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(-1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 0.0f, -1.0f)};
    private static final int[][] CUBE_FACE_INDICES = new int[][]{{0, 2, 4}, {0, 4, 3}, {0, 3, 5}, {0, 5, 2}, {1, 4, 2}, {1, 3, 4}, {1, 5, 3}, {1, 2, 5}};
    private static final float[] FACE_BRIGHTNESS = new float[]{1.0f, 0.8f, 0.6f, 0.9f, 0.7f, 0.5f, 0.4f, 0.6f};

    public static void drawCube(MatrixStack class_45872, BufferBuilder class_2872, float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
        class_45872.push();
        class_45872.translate(f, f2, f3);
        class_45872.scale(f4, f4, f4);
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        for (int i = 0; i < CUBE_FACE_INDICES.length; ++i) {
            int[] nArray = CUBE_FACE_INDICES[i];
            float f5 = FACE_BRIGHTNESS[i];
            Vector3f vector3f = CUBE_VERTICES[nArray[0]];
            Vector3f vector3f2 = CUBE_VERTICES[nArray[1]];
            Vector3f vector3f3 = CUBE_VERTICES[nArray[2]];
            int n = CubeRenderer.applyBrightness(colorRGBA.getRGB(), f5);
            class_2872.vertex(matrix4f, vector3f.x, vector3f.y, vector3f.z).color(n);
            class_2872.vertex(matrix4f, vector3f2.x, vector3f2.y, vector3f2.z).color(n);
            class_2872.vertex(matrix4f, vector3f3.x, vector3f3.y, vector3f3.z).color(n);
        }
        class_45872.pop();
    }

    public static BufferBuilder beginCubeBatch() {
        CubeRenderer.prepareSolidColorShader();
        return Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
    }

    private static void prepareSolidColorShader() {
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private static int applyBrightness(int n, float f) {
        int n2 = n >> 24 & 0xFF;
        int n3 = (int)((float)(n >> 16 & 0xFF) * f);
        int n4 = (int)((float)(n >> 8 & 0xFF) * f);
        int n5 = (int)((float)(n & 0xFF) * f);
        n3 = Math.min(255, Math.max(0, n3));
        n4 = Math.min(255, Math.max(0, n4));
        n5 = Math.min(255, Math.max(0, n5));
        return n2 << 24 | n3 << 16 | n4 << 8 | n5;
    }

    @Generated
    private CubeRenderer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

