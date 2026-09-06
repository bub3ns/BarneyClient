/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.ProjectionType
 *  net.minecraft.AbstractTexture
 *  net.minecraft.Vec3d
 *  net.minecraft.Framebuffer
 *  net.minecraft.Identifier
 *  net.minecraft.DrawContext
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL15
 *  org.lwjgl.opengl.GL20
 */
package pyrock.utility.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.core.resources.ResourceJsonLoader;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.target.RenderTarget;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import com.mojang.blaze3d.systems.ProjectionType;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.PyDynamicTexture;

public final class PyShader
implements AutoCloseable,
ClientAccess,
WindowHandle {
    public static final int MAX_PER_SCRIPT = 32;
    private static final int MAX_SOURCE_LENGTH = 262144;
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static final long START = System.nanoTime();
    private static final int POSITION_UV = 5;
    private static final int POSITION_UV_COLOR = 9;
    private static final int MAX_VERTICES = 262144;
    private static int meshVao;
    private static int meshVbo;
    private static RenderTarget fullscreenTarget;
    private static String header;
    private static String vertexHeader;
    private static final Pattern IMPORT;
    private final String name;
    private final String vertexSource;
    private final String fragmentSource;
    private final Map<String, Integer> locations = new HashMap<String, Integer>();
    private final Map<String, float[]> floats = new LinkedHashMap<String, float[]>();
    private final Map<String, int[]> ints = new LinkedHashMap<String, int[]>();
    private final Map<String, Matrix4f> matrices = new LinkedHashMap<String, Matrix4f>();
    private final Map<Integer, Object> textures = new LinkedHashMap<Integer, Object>();
    private int programId;
    private boolean disposed;

    public PyShader(String string, String string2, String string3) {
        if (string3 == null || string3.isBlank()) {
            throw new IllegalArgumentException("shader needs a fragment source");
        }
        if (string3.length() > 262144 || string2 != null && string2.length() > 262144) {
            throw new IllegalArgumentException("shader source is too large");
        }
        if (!RenderSystem.isOnRenderThread()) {
            throw new IllegalStateException("\u0448\u0435\u0439\u0434\u0435\u0440 \u043a\u043e\u043c\u043f\u0438\u043b\u0438\u0440\u0443\u0435\u0442\u0441\u044f \u0442\u043e\u043b\u044c\u043a\u043e \u043d\u0430 \u0433\u043b\u0430\u0432\u043d\u043e\u043c \u043f\u043e\u0442\u043e\u043a\u0435 \u0438\u0433\u0440\u044b");
        }
        this.name = string == null || string.isBlank() ? "shader" : string;
        this.vertexSource = string2 == null || string2.isBlank() ? PyShader.defaultVertex() : string2;
        this.fragmentSource = string3;
        PyShader.guardEndlessLoop(this.vertexSource);
        PyShader.guardEndlessLoop(this.fragmentSource);
        this.programId = this.link(this.prepare(this.vertexSource, false), this.prepare(this.fragmentSource, true));
    }

    public String name() {
        return this.name;
    }

    public String vertexSource() {
        return this.vertexSource;
    }

    public String fragmentSource() {
        return this.fragmentSource;
    }

    public boolean valid() {
        return !this.disposed && this.programId != 0;
    }

    public PyShader set(String string, float f) {
        this.floats.put(string, new float[]{f});
        return this;
    }

    public PyShader set(String string, float f, float f2) {
        this.floats.put(string, new float[]{f, f2});
        return this;
    }

    public PyShader set(String string, float f, float f2, float f3) {
        this.floats.put(string, new float[]{f, f2, f3});
        return this;
    }

    public PyShader set(String string, float f, float f2, float f3, float f4) {
        this.floats.put(string, new float[]{f, f2, f3, f4});
        return this;
    }

    public PyShader setInt(String string, int n) {
        this.ints.put(string, new int[]{n});
        return this;
    }

    public PyShader setMatrix(String string, Matrix4f matrix4f) {
        if (matrix4f != null) {
            this.matrices.put(string, new Matrix4f((Matrix4fc)matrix4f));
        }
        return this;
    }

    public PyShader setColor(String string, ColorRGBA colorRGBA) {
        if (colorRGBA == null) {
            return this;
        }
        return this.set(string, colorRGBA.getRed() / 255.0f, colorRGBA.getGreen() / 255.0f, colorRGBA.getBlue() / 255.0f, colorRGBA.getAlpha() / 255.0f);
    }

    public PyShader texture(int n, Object object) {
        if (n < 0 || n > 7) {
            throw new IllegalArgumentException("texture unit out of range: " + n);
        }
        if (object == null) {
            this.textures.remove(n);
        } else {
            this.textures.put(n, object);
        }
        return this;
    }

    public void rect(DrawContext ServerConfigException, float f, float f2, float f3, float f4) {
        if (!this.valid() || f3 <= 0.0f || f4 <= 0.0f) {
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        Matrix4f matrix4f = ServerConfigException == null ? IDENTITY : ServerConfigException.getMatrices().peek().getPositionMatrix();
        float[] fArray = this.quad(matrix4f, f, f2, f3, f4, 0.0f, 0.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        this.bind();
        this.builtin("Size", f3, f4);
        this.builtin("ModelViewMat", RenderSystem.getModelViewMatrix());
        this.builtin("ProjMat", RenderSystem.getProjectionMatrix());
        this.applyUniforms();
        PyShader.drawQuad(fArray);
        this.unbind();
        RenderSystem.disableBlend();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void fullscreen(Render3DEvent render3DEvent) {
        if (!this.valid()) {
            return;
        }
        Framebuffer class_2762 = minecraftClient.getFramebuffer();
        if (class_2762 == null) {
            return;
        }
        int n = WINDOW.getScaledWidth();
        int n2 = WINDOW.getScaledHeight();
        if (n <= 0 || n2 <= 0) {
            return;
        }
        if (fullscreenTarget == null) {
            // pyrock is NOT obfuscated, so the jar is literal ground truth here. At label E of
            // PyShader#fullscreen the bytecode is: new RenderTarget(false) -> <init>(Z) ->
            // invokevirtual (no-arg, returns RenderTarget) -> putstatic. That no-arg method is
            // enableLinearFiltering (its body sets linearFiltering and re-applies the filter).
            // There is no setResolutionScale call in the original chain at all; substituting one
            // left the scratch target on GL_NEAREST, so every fullscreen script shader sampled
            // the scene unfiltered.
            fullscreenTarget = new RenderTarget(false).enableLinearFiltering();
        }
        RenderSystem.disableBlend();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix((Matrix4f)new Matrix4f().setOrtho(0.0f, (float)n, (float)n2, 0.0f, 1000.0f, 21000.0f), (ProjectionType)ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.identity().translate(0.0f, 0.0f, -11000.0f);
        try {
            fullscreenTarget.beginPass(true);
            this.bind();
            this.builtin("Size", n, n2);
            this.builtin("ModelViewMat", RenderSystem.getModelViewMatrix());
            this.builtin("ProjMat", RenderSystem.getProjectionMatrix());
            if (render3DEvent != null) {
                this.builtin("InvViewProj", new Matrix4f((Matrix4fc)render3DEvent.getProjectionMatrix()).mul((Matrix4fc)render3DEvent.getPositionMatrix()).invert());
                if (render3DEvent.getCamera() != null) {
                    Vector3f vector3f = render3DEvent.getCamera().getPos().toVector3f();
                    this.builtin("CamPos", vector3f.x, vector3f.y, vector3f.z);
                }
            }
            this.bindTexture(0, class_2762.getColorAttachment());
            if (class_2762.getDepthAttachment() != 0) {
                this.bindTexture(1, class_2762.getDepthAttachment());
            }
            this.applyUniforms();
            PyShader.drawQuad(this.quad(IDENTITY, 0.0f, 0.0f, n, n2, 0.0f, 1.0f, 1.0f, 0.0f));
            this.unbind();
            fullscreenTarget.endPass();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            fullscreenTarget.bindForRead();
            RenderSystem.setShaderTexture((int)0, (int)fullscreenTarget.getColorAttachment());
            ShaderRenderer.renderQuadWithBounds(0.0f, 0.0f, n, n2);
            fullscreenTarget.unbindForRead();
            RenderSystem.setShaderTexture((int)0, (int)0);
        }
        finally {
            matrix4fStack.popMatrix();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.depthMask((boolean)true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    public void quad3d(Render3DEvent render3DEvent, double d, double d2, double d3, float f, float f2, String string, boolean bl, boolean bl2) {
        if (!this.valid() || render3DEvent == null || render3DEvent.getCamera() == null || f <= 0.0f || f2 <= 0.0f) {
            return;
        }
        Vector3f vector3f = render3DEvent.getCamera().getPos().toVector3f().negate().add((float)d, (float)d2, (float)d3);
        Vector3f vector3f2 = new Vector3f();
        Vector3f vector3f3 = new Vector3f();
        this.axes(render3DEvent, string, vector3f2, vector3f3);
        vector3f2.mul(f * 0.5f);
        vector3f3.mul(f2 * 0.5f);
        Matrix4f matrix4f = render3DEvent.getMatrices().peek().getPositionMatrix();
        float[] fArray = new float[20];
        this.corner(fArray, 0, matrix4f, vector3f, vector3f2, vector3f3, -1.0f, 1.0f, 0.0f, 0.0f);
        this.corner(fArray, 5, matrix4f, vector3f, vector3f2, vector3f3, -1.0f, -1.0f, 0.0f, 1.0f);
        this.corner(fArray, 10, matrix4f, vector3f, vector3f2, vector3f3, 1.0f, 1.0f, 1.0f, 0.0f);
        this.corner(fArray, 15, matrix4f, vector3f, vector3f2, vector3f3, 1.0f, -1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        if (bl) {
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        if (bl2) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        this.bind();
        this.builtin("Size", f, f2);
        this.builtin("ModelViewMat", RenderSystem.getModelViewMatrix());
        this.builtin("ProjMat", RenderSystem.getProjectionMatrix());
        this.builtin("InvViewProj", new Matrix4f((Matrix4fc)render3DEvent.getProjectionMatrix()).mul((Matrix4fc)render3DEvent.getPositionMatrix()).invert());
        this.applyUniforms();
        PyShader.drawQuad(fArray);
        this.unbind();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public void mesh3d(Render3DEvent render3DEvent, Object object, double d, double d2, double d3, String string, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        int n;
        if (!this.valid() || render3DEvent == null || render3DEvent.getCamera() == null) {
            return;
        }
        float[] fArray = PyShader.floats(object);
        int n2 = n = bl ? 9 : 5;
        if (fArray.length < n) {
            return;
        }
        if (fArray.length / n > 262144) {
            throw new IllegalArgumentException("\u0432 \u043c\u0435\u0448\u0435 \u0431\u043e\u043b\u044c\u0448\u0435 262144 \u0432\u0435\u0440\u0448\u0438\u043d");
        }
        Vec3d VanillaChestLootTableGenerator = render3DEvent.getCamera().getPos();
        Matrix4f matrix4f = new Matrix4f((Matrix4fc)RenderSystem.getModelViewMatrix()).mul((Matrix4fc)render3DEvent.getMatrices().peek().getPositionMatrix()).translate((float)(d - VanillaChestLootTableGenerator.x), (float)(d2 - VanillaChestLootTableGenerator.y), (float)(d3 - VanillaChestLootTableGenerator.z));
        RenderSystem.enableBlend();
        if (bl2) {
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }
        if (bl4) {
            RenderSystem.enableCull();
        } else {
            RenderSystem.disableCull();
        }
        RenderSystem.depthMask((boolean)false);
        if (bl3) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        this.bind();
        this.builtin("Size", 1.0f, 1.0f);
        this.builtin("ModelViewMat", matrix4f);
        this.builtin("ProjMat", RenderSystem.getProjectionMatrix());
        this.builtin("InvViewProj", new Matrix4f((Matrix4fc)render3DEvent.getProjectionMatrix()).mul((Matrix4fc)render3DEvent.getPositionMatrix()).invert());
        this.applyUniforms();
        PyShader.draw(fArray, n, PyShader.primitive(string));
        this.unbind();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public void mesh2d(DrawContext ServerConfigException, Object object, String string, boolean bl) {
        int n;
        if (!this.valid()) {
            return;
        }
        float[] fArray = PyShader.floats(object);
        int n2 = n = bl ? 9 : 5;
        if (fArray.length < n) {
            return;
        }
        if (fArray.length / n > 262144) {
            throw new IllegalArgumentException("\u0432 \u043c\u0435\u0448\u0435 \u0431\u043e\u043b\u044c\u0448\u0435 262144 \u0432\u0435\u0440\u0448\u0438\u043d");
        }
        WidgetBatchRenderer.flushCurrentBatch();
        Matrix4f matrix4f = new Matrix4f((Matrix4fc)RenderSystem.getModelViewMatrix());
        if (ServerConfigException != null) {
            matrix4f.mul((Matrix4fc)ServerConfigException.getMatrices().peek().getPositionMatrix());
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        this.bind();
        this.builtin("Size", WINDOW.getScaledWidth(), WINDOW.getScaledHeight());
        this.builtin("ModelViewMat", matrix4f);
        this.builtin("ProjMat", RenderSystem.getProjectionMatrix());
        this.applyUniforms();
        PyShader.draw(fArray, n, PyShader.primitive(string));
        this.unbind();
        RenderSystem.disableBlend();
    }

    public void restoreEnabledFlag() {
        this.dispose();
    }

    public void dispose() {
        if (this.disposed) {
            return;
        }
        this.disposed = true;
        int n = this.programId;
        this.programId = 0;
        if (n == 0) {
            return;
        }
        if (RenderSystem.isOnRenderThread()) {
            GlStateManager.glDeleteProgram((int)n);
        } else {
            minecraftClient.execute(() -> GlStateManager.glDeleteProgram((int)n));
        }
    }

    @Override
    public void close() {
        this.dispose();
    }

    private void axes(Render3DEvent render3DEvent, String string, Vector3f vector3f, Vector3f vector3f2) {
        String string2;
        switch (string2 = string == null ? "billboard" : string.toLowerCase()) {
            case "ground": 
            case "floor": 
            case "flat": {
                vector3f.set(1.0f, 0.0f, 0.0f);
                vector3f2.set(0.0f, 0.0f, 1.0f);
                break;
            }
            case "wall": 
            case "upright": 
            case "yaw": {
                float f = (float)Math.toRadians(render3DEvent.getCamera().getYaw());
                vector3f.set(-((float)Math.cos(f)), 0.0f, -((float)Math.sin(f)));
                vector3f2.set(0.0f, 1.0f, 0.0f);
                break;
            }
            default: {
                Quaternionf quaternionf = render3DEvent.getCamera().getRotation();
                vector3f.set(1.0f, 0.0f, 0.0f).rotate((Quaternionfc)quaternionf);
                vector3f2.set(0.0f, 1.0f, 0.0f).rotate((Quaternionfc)quaternionf);
            }
        }
    }

    private void corner(float[] fArray, int n, Matrix4f matrix4f, Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3, float f, float f2, float f3, float f4) {
        Vector4f vector4f = new Vector4f(vector3f.x + vector3f2.x * f + vector3f3.x * f2, vector3f.y + vector3f2.y * f + vector3f3.y * f2, vector3f.z + vector3f2.z * f + vector3f3.z * f2, 1.0f).mul((Matrix4fc)matrix4f);
        fArray[n] = vector4f.x;
        fArray[n + 1] = vector4f.y;
        fArray[n + 2] = vector4f.z;
        fArray[n + 3] = f3;
        fArray[n + 4] = f4;
    }

    private float[] quad(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        float[] fArray = new float[20];
        this.point(fArray, 0, matrix4f, f, f2, f5, f6);
        this.point(fArray, 5, matrix4f, f, f2 + f4, f5, f8);
        this.point(fArray, 10, matrix4f, f + f3, f2, f7, f6);
        this.point(fArray, 15, matrix4f, f + f3, f2 + f4, f7, f8);
        return fArray;
    }

    private void point(float[] fArray, int n, Matrix4f matrix4f, float f, float f2, float f3, float f4) {
        Vector4f vector4f = new Vector4f(f, f2, 0.0f, 1.0f).mul((Matrix4fc)matrix4f);
        fArray[n] = vector4f.x;
        fArray[n + 1] = vector4f.y;
        fArray[n + 2] = vector4f.z;
        fArray[n + 3] = f3;
        fArray[n + 4] = f4;
    }

    private void bind() {
        GlStateManager._glUseProgram((int)this.programId);
        this.builtin("Time", (float)((double)(System.nanoTime() - START) / 1.0E9 % 3600.0));
        this.builtin("Resolution", WINDOW.getScaledWidth(), WINDOW.getScaledHeight());
        this.builtin("GuiScale", (float)WINDOW.getScaleFactor());
        this.builtin("MousePos", (float)(PyShader.minecraftClient.mouse.getX() * (double)WINDOW.getScaledWidth() / (double)Math.max(1, WINDOW.getWidth())), (float)(PyShader.minecraftClient.mouse.getY() * (double)WINDOW.getScaledHeight() / (double)Math.max(1, WINDOW.getHeight())));
    }

    private void unbind() {
        GlStateManager._glBindVertexArray((int)0);
        GlStateManager._glUseProgram((int)0);
        GlStateManager._activeTexture((int)33984);
    }

    private void applyUniforms() {
        int n;
        for (Map.Entry<Integer, Object> entry : this.textures.entrySet()) {
            this.bindTexture(entry.getKey(), this.glId(entry.getValue()));
        }
        block6: for (Map.Entry<String, float[]> entry : this.floats.entrySet()) {
            n = this.location((String)entry.getKey());
            if (n < 0) continue;
            float[] fArray = (float[])entry.getValue();
            switch (fArray.length) {
                case 1: {
                    GL20.glUniform1f((int)n, (float)fArray[0]);
                    continue block6;
                }
                case 2: {
                    GL20.glUniform2f((int)n, (float)fArray[0], (float)fArray[1]);
                    continue block6;
                }
                case 3: {
                    GL20.glUniform3f((int)n, (float)fArray[0], (float)fArray[1], (float)fArray[2]);
                    continue block6;
                }
            }
            GL20.glUniform4f((int)n, (float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
        }
        for (Map.Entry<String, int[]> entry : this.ints.entrySet()) {
            n = this.location((String)entry.getKey());
            if (n < 0) continue;
            GL20.glUniform1i((int)n, (int)((int[])entry.getValue())[0]);
        }
        for (Map.Entry<String, Matrix4f> entry : this.matrices.entrySet()) {
            n = this.location((String)entry.getKey());
            if (n < 0) continue;
            GL20.glUniformMatrix4fv((int)n, (boolean)false, (float[])((Matrix4f)entry.getValue()).get(new float[16]));
        }
    }

    private void bindTexture(int n, int n2) {
        if (n2 <= 0) {
            return;
        }
        GlStateManager._activeTexture((int)(33984 + n));
        GlStateManager._bindTexture((int)n2);
        int n3 = this.location("Sampler" + n);
        if (n3 >= 0) {
            GL20.glUniform1i((int)n3, (int)n);
        }
        GlStateManager._activeTexture((int)33984);
    }

    private int glId(Object object) {
        if (object instanceof Integer) {
            Integer n = (Integer)object;
            return n;
        }
        if (object instanceof PyDynamicTexture) {
            PyDynamicTexture pyDynamicTexture = (PyDynamicTexture)object;
            return this.glId(pyDynamicTexture.identifier());
        }
        if (object instanceof Identifier) {
            Identifier class_29602 = (Identifier)object;
            AbstractTexture ItemModelTypes = minecraftClient.getTextureManager().getTexture(class_29602);
            return ItemModelTypes == null ? 0 : ItemModelTypes.getGlId();
        }
        return 0;
    }

    private void builtin(String string, float f) {
        int n = this.location(string);
        if (n >= 0) {
            GL20.glUniform1f((int)n, (float)f);
        }
    }

    private void builtin(String string, float f, float f2) {
        int n = this.location(string);
        if (n >= 0) {
            GL20.glUniform2f((int)n, (float)f, (float)f2);
        }
    }

    private void builtin(String string, float f, float f2, float f3) {
        int n = this.location(string);
        if (n >= 0) {
            GL20.glUniform3f((int)n, (float)f, (float)f2, (float)f3);
        }
    }

    private void builtin(String string, Matrix4f matrix4f) {
        int n = this.location(string);
        if (n >= 0) {
            GL20.glUniformMatrix4fv((int)n, (boolean)false, (float[])matrix4f.get(new float[16]));
        }
    }

    private int location(String string2) {
        return this.locations.computeIfAbsent(string2, string -> GlStateManager._glGetUniformLocation((int)this.programId, (CharSequence)string));
    }

    private static void drawQuad(float[] fArray) {
        PyShader.draw(fArray, 5, 5);
    }

    private static void draw(float[] fArray, int n, int n2) {
        int n3 = fArray.length / n;
        if (n3 <= 0) {
            return;
        }
        if (!RenderSystem.isOnRenderThread()) {
            throw new IllegalStateException("\u0448\u0435\u0439\u0434\u0435\u0440 \u0440\u0438\u0441\u0443\u0435\u0442\u0441\u044f \u0442\u043e\u043b\u044c\u043a\u043e \u043d\u0430 \u0433\u043b\u0430\u0432\u043d\u043e\u043c \u043f\u043e\u0442\u043e\u043a\u0435 \u0438\u0433\u0440\u044b");
        }
        if (meshVao == 0) {
            meshVao = GlStateManager._glGenVertexArrays();
            meshVbo = GlStateManager._glGenBuffers();
        }
        GlStateManager._glBindVertexArray((int)meshVao);
        GlStateManager._glBindBuffer((int)34962, (int)meshVbo);
        GL15.glBufferData((int)34962, (float[])fArray, (int)35048);
        int n4 = n * 4;
        GL20.glEnableVertexAttribArray((int)0);
        GL20.glVertexAttribPointer((int)0, (int)3, (int)5126, (boolean)false, (int)n4, (long)0L);
        GL20.glEnableVertexAttribArray((int)1);
        GL20.glVertexAttribPointer((int)1, (int)2, (int)5126, (boolean)false, (int)n4, (long)12L);
        if (n >= 9) {
            GL20.glEnableVertexAttribArray((int)2);
            GL20.glVertexAttribPointer((int)2, (int)4, (int)5126, (boolean)false, (int)n4, (long)20L);
        } else {
            GL20.glDisableVertexAttribArray((int)2);
            GL20.glVertexAttrib4f((int)2, (float)255.0f, (float)255.0f, (float)255.0f, (float)255.0f);
        }
        GL11.glDrawArrays((int)n2, (int)0, (int)n3);
        GlStateManager._glBindBuffer((int)34962, (int)0);
    }

    private static int primitive(String string) {
        String string2;
        return switch (string2 = string == null ? "triangles" : string.toLowerCase()) {
            case "triangle_strip", "strip" -> 5;
            case "triangle_fan", "fan" -> 6;
            case "lines" -> 1;
            case "line_strip" -> 3;
            case "line_loop" -> 2;
            case "points" -> 0;
            case "triangles", "tris" -> 4;
            default -> throw new IllegalArgumentException("\u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u044b\u0439 \u0440\u0435\u0436\u0438\u043c \u043f\u0440\u0438\u043c\u0438\u0442\u0438\u0432\u0430: " + string);
        };
    }

    private static float[] floats(Object object) {
        if (object == null) {
            return new float[0];
        }
        if (object instanceof float[]) {
            float[] fArray = (float[])object;
            return fArray;
        }
        if (object instanceof byte[]) {
            byte[] byArray = (byte[])object;
            FloatBuffer floatBuffer = ByteBuffer.wrap(byArray).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
            float[] fArray = new float[floatBuffer.remaining()];
            floatBuffer.get(fArray);
            return fArray;
        }
        if (object instanceof double[]) {
            double[] dArray = (double[])object;
            float[] fArray = new float[dArray.length];
            for (int i = 0; i < dArray.length; ++i) {
                fArray[i] = (float)dArray[i];
            }
            return fArray;
        }
        if (object instanceof List) {
            List list = (List)object;
            float[] fArray = new float[list.size()];
            for (int i = 0; i < fArray.length; ++i) {
                Object e = list.get(i);
                if (!(e instanceof Number)) {
                    throw new IllegalArgumentException("\u0432\u0435\u0440\u0448\u0438\u043d\u044b \u043c\u0435\u0448\u0430 \u044d\u0442\u043e \u0447\u0438\u0441\u043b\u0430");
                }
                Number number = (Number)e;
                fArray[i] = number.floatValue();
            }
            return fArray;
        }
        throw new IllegalArgumentException("\u0432\u0435\u0440\u0448\u0438\u043d\u044b \u043c\u0435\u0448\u0430: \u043f\u043b\u043e\u0441\u043a\u0438\u0439 \u0441\u043f\u0438\u0441\u043e\u043a \u0447\u0438\u0441\u0435\u043b \u0438\u043b\u0438 array('f').tobytes()");
    }

    private int link(String string, String string2) {
        int n;
        int n2 = this.compile(35633, string, "vertex");
        try {
            n = this.compile(35632, string2, "fragment");
        }
        catch (RuntimeException runtimeException) {
            GlStateManager.glDeleteShader((int)n2);
            throw runtimeException;
        }
        int n3 = GlStateManager.glCreateProgram();
        GlStateManager.glAttachShader((int)n3, (int)n2);
        GlStateManager.glAttachShader((int)n3, (int)n);
        GlStateManager._glBindAttribLocation((int)n3, (int)0, (CharSequence)"Position");
        GlStateManager._glBindAttribLocation((int)n3, (int)1, (CharSequence)"UV");
        GlStateManager.glLinkProgram((int)n3);
        GlStateManager.glDeleteShader((int)n2);
        GlStateManager.glDeleteShader((int)n);
        if (GlStateManager.glGetProgrami((int)n3, (int)35714) == 0) {
            String string3 = GlStateManager.glGetProgramInfoLog((int)n3, (int)4096);
            GlStateManager.glDeleteProgram((int)n3);
            throw new RuntimeException("\u0448\u0435\u0439\u0434\u0435\u0440 " + this.name + " \u043d\u0435 \u0441\u043b\u0438\u043d\u043a\u043e\u0432\u0430\u043b\u0441\u044f: " + string3.trim());
        }
        return n3;
    }

    private int compile(int n, String string, String string2) {
        int n2 = GlStateManager.glCreateShader((int)n);
        GlStateManager.glShaderSource((int)n2, (String)string);
        GlStateManager.glCompileShader((int)n2);
        if (GlStateManager.glGetShaderi((int)n2, (int)35713) == 0) {
            String string3 = GlStateManager.glGetShaderInfoLog((int)n2, (int)4096);
            GlStateManager.glDeleteShader((int)n2);
            throw new RuntimeException("\u0448\u0435\u0439\u0434\u0435\u0440 " + this.name + " (" + string2 + ") \u043d\u0435 \u0441\u043e\u0431\u0440\u0430\u043b\u0441\u044f: " + string3.trim());
        }
        return n2;
    }

    private String prepare(String string, boolean bl) {
        if (PyShader.hasVersion(string)) {
            return string;
        }
        return (bl ? this.fragmentHeader() : "#version 150\n") + "\n#line 0\n" + string;
    }

    private boolean ownVertex() {
        return this.vertexSource.equals(PyShader.defaultVertex());
    }

    private static boolean hasVersion(String string) {
        int n = 0;
        int n2 = string.length();
        while (n < n2) {
            int n3;
            char c = string.charAt(n);
            if (Character.isWhitespace(c)) {
                ++n;
                continue;
            }
            if (c == '/' && n + 1 < n2 && string.charAt(n + 1) == '/') {
                n3 = string.indexOf(10, n);
                if (n3 < 0) {
                    return false;
                }
                n = n3 + 1;
                continue;
            }
            if (c == '/' && n + 1 < n2 && string.charAt(n + 1) == '*') {
                n3 = string.indexOf("*/", n + 2);
                if (n3 < 0) {
                    return false;
                }
                n = n3 + 2;
                continue;
            }
            return string.startsWith("#version", n);
        }
        return false;
    }

    private String fragmentHeader() {
        if (header == null) {
            header = PyShader.resource("shaders/scripts/header.fsh") + "\n" + PyShader.glsl("shaders/include/common.glsl");
        }
        return this.ownVertex() ? header + "\nin vec4 VertexColor;\n" : header;
    }

    private static String glsl(String string) {
        Matcher matcher = IMPORT.matcher(PyShader.resource(string));
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(stringBuilder, Matcher.quoteReplacement(PyShader.glsl("shaders/include/" + matcher.group(1))));
        }
        return matcher.appendTail(stringBuilder).toString();
    }

    private static String defaultVertex() {
        if (vertexHeader == null) {
            vertexHeader = PyShader.resource("shaders/scripts/vertex.vsh");
        }
        return vertexHeader;
    }

    private static String resource(String string) {
        try {
            return ResourceJsonLoader.readResourceText(RockstarClient.resourceId(string));
        }
        catch (Throwable throwable) {
            throw new IllegalStateException("\u043d\u0435 \u0447\u0438\u0442\u0430\u0435\u0442\u0441\u044f \u0440\u0435\u0441\u0443\u0440\u0441 \u0448\u0435\u0439\u0434\u0435\u0440\u0430 " + string, throwable);
        }
    }

    private static void guardEndlessLoop(String string) {
        String string2 = string.replaceAll("\\s+", "");
        if (string2.contains("while(true)") || string2.contains("for(;;)")) {
            throw new IllegalArgumentException("\u0431\u0435\u0441\u043a\u043e\u043d\u0435\u0447\u043d\u044b\u0439 \u0446\u0438\u043a\u043b \u0432 \u0448\u0435\u0439\u0434\u0435\u0440\u0435 \u043f\u043e\u0432\u0435\u0441\u0438\u0442 \u0432\u0438\u0434\u0435\u043e\u0434\u0440\u0430\u0439\u0432\u0435\u0440: \u0434\u0430\u0439\u0442\u0435 \u0441\u0447\u0451\u0442\u0447\u0438\u043a\u0443 \u0446\u0438\u043a\u043b\u0430 \u043f\u0440\u0435\u0434\u0435\u043b");
        }
    }

    static {
        IMPORT = Pattern.compile("(?m)^[ \\t]*#moj_import[ \\t]*<rockstar:([\\w.]+)>[ \\t]*$");
    }
}
