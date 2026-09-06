/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.RenderLayer
 *  net.minecraft.RenderLayer$MultiPhaseParameters
 *  net.minecraft.RenderLayer$MultiPhaseParameters$Builder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexBuffer
 *  net.minecraft.VertexFormat
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.VertexConsumer
 *  net.minecraft.RenderPhase
 *  net.minecraft.RenderPhase$Texture
 *  net.minecraft.RenderPhase$Transparency
 *  net.minecraft.RenderPhase$TextureBase
 *  net.minecraft.TriState
 *  org.joml.Matrix4f
 */
package moscow.rockstar.render.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.shaders.TimedEffectShader;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.TriState;
import org.joml.Matrix4f;

public class AmbientParticleRenderer {
    private static final long RANDOM_PARTICLE_SEED = 1592635409L;
    private final TimedEffectShader ambientParticleShader = new TimedEffectShader(RockstarClient.resourceId("ambient_particles/data"));
    private final TimedEffectShader.ParticleUniformState particleVertexState = new TimedEffectShader.ParticleUniformState();
    private VertexBuffer particleVertexConsumer;
    private RenderLayer standardParticleLayer;
    private RenderLayer additiveParticleLayer;
    private Identifier particleTexture;
    private int particleCount;
    private float particleWidth;
    private float particleHeight;
    private float particleDepth;

    public TimedEffectShader.ParticleUniformState getParticleVertexState() {
        return this.particleVertexState;
    }

    public void renderAmbientParticles(Matrix4f matrix4f, int n, boolean bl, Identifier class_29602) {
        if (n <= 0) {
            return;
        }
        if (!this.prepareParticleRender(n)) {
            return;
        }
        RenderLayer class_19212 = this.getParticleRenderLayer(bl, class_29602);
        this.ambientParticleShader.uploadParticleUniforms(matrix4f, this.particleVertexState);
        this.particleVertexConsumer.draw(class_19212);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public void finishParticleRender() {
        if (this.particleVertexConsumer != null && !this.particleVertexConsumer.isClosed()) {
            this.particleVertexConsumer.close();
        }
        this.particleVertexConsumer = null;
        this.particleCount = 0;
    }

    private boolean prepareParticleRender(int n) {
        boolean bl;
        boolean bl2 = bl = this.particleWidth == this.particleVertexState.cellWidth && this.particleHeight == this.particleVertexState.cellHeight && this.particleDepth == this.particleVertexState.cellDepth;
        if (this.particleVertexConsumer != null && !this.particleVertexConsumer.isClosed() && this.particleCount == n && bl) {
            return true;
        }
        this.finishParticleRender();
        this.particleCount = n;
        this.particleWidth = this.particleVertexState.cellWidth;
        this.particleHeight = this.particleVertexState.cellHeight;
        this.particleDepth = this.particleVertexState.cellDepth;
        int n2 = n;
        float f = this.particleVertexState.cellWidth;
        float f2 = this.particleVertexState.cellHeight;
        float f3 = this.particleVertexState.cellDepth;
        this.particleVertexConsumer = VertexBuffer.createAndUpload((VertexFormat.DrawMode)VertexFormat.DrawMode.QUADS, (VertexFormat)VertexFormats.POSITION_TEXTURE_COLOR, class_45882 -> AmbientParticleRenderer.writeParticleVertices(class_45882, n2, f, f2, f3));
        return this.particleVertexConsumer != null;
    }

    private static void writeParticleVertices(VertexConsumer class_45882, int n, float f, float f2, float f3) {
        Random random = new Random(1592635409L);
        for (int i = 0; i < n; ++i) {
            float f4 = random.nextFloat() * f;
            float f5 = random.nextFloat() * f2;
            float f6 = random.nextFloat() * f3;
            int n2 = (int)(random.nextFloat() * 255.0f);
            int n3 = (int)(random.nextFloat() * 255.0f);
            int n4 = (int)(random.nextFloat() * 255.0f);
            int n5 = 150 + (int)(random.nextFloat() * 105.0f);
            class_45882.vertex(f4, f5, f6).texture(0.0f, 0.0f).color(n2, n3, n4, n5);
            class_45882.vertex(f4, f5, f6).texture(0.0f, 1.0f).color(n2, n3, n4, n5);
            class_45882.vertex(f4, f5, f6).texture(1.0f, 1.0f).color(n2, n3, n4, n5);
            class_45882.vertex(f4, f5, f6).texture(1.0f, 0.0f).color(n2, n3, n4, n5);
        }
    }

    private RenderLayer getParticleRenderLayer(boolean bl, Identifier class_29602) {
        if (class_29602 == null || !class_29602.equals((Object)this.particleTexture)) {
            this.particleTexture = class_29602;
            this.standardParticleLayer = null;
            this.additiveParticleLayer = null;
        }
        if (bl) {
            if (this.additiveParticleLayer == null) {
                this.additiveParticleLayer = this.createParticleRenderLayer("rockstar_ambient_particles_additive", RenderPhase.ADDITIVE_TRANSPARENCY, class_29602);
            }
            return this.additiveParticleLayer;
        }
        if (this.standardParticleLayer == null) {
            this.standardParticleLayer = this.createParticleRenderLayer("rockstar_ambient_particles", RenderPhase.TRANSLUCENT_TRANSPARENCY, class_29602);
        }
        return this.standardParticleLayer;
    }

    private RenderLayer createParticleRenderLayer(String string, RenderPhase.Transparency class_46852, Identifier class_29602) {
        RenderLayer.MultiPhaseParameters.Builder class_46892 = RenderLayer.MultiPhaseParameters.builder().program(this.ambientParticleShader.createShaderLayer()).transparency(class_46852).cull(RenderPhase.DISABLE_CULLING).writeMaskState(RenderPhase.COLOR_MASK);
        if (class_29602 != null) {
            class_46892.texture((RenderPhase.TextureBase)new RenderPhase.Texture(class_29602, TriState.FALSE, false));
        }
        return RenderLayer.of((String)string, (VertexFormat)VertexFormats.POSITION_TEXTURE_COLOR, (VertexFormat.DrawMode)VertexFormat.DrawMode.QUADS, (int)1536, (boolean)false, (boolean)true, (RenderLayer.MultiPhaseParameters)class_46892.build(false));
    }
}
