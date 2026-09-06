/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  moscow.rockstar.render.world.BlockParticleRenderer$BlockParticleBatch
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Blocks
 *  net.minecraft.Direction
 *  net.minecraft.FlowerBlock
 *  net.minecraft.Position
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.MatrixStack
 *  net.minecraft.BuiltBuffer
 */
package moscow.rockstar.render.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.render.world.BlockParticleRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.block.FlowerBlock;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BuiltBuffer;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public class BlockParticleRenderer
implements ClientAccess {
    public static BlockParticleRenderer INSTANCE = new BlockParticleRenderer();
    private final List<BlockParticleBatch> pendingParticles = new CopyOnWriteArrayList<BlockParticleBatch>();
    private final EventListener<Render3DEvent> render3DEventListener = render3DEvent -> {
        float f;
        float f2;
        float f3;
        BufferBuilder outlineBuffer;
        if (BlockParticleRenderer.minecraftClient.world == null || BlockParticleRenderer.minecraftClient.player == null || this.pendingParticles.isEmpty()) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Vec3d VanillaChestLootTableGenerator = BlockParticleRenderer.minecraftClient.gameRenderer.getCamera().getPos();
        class_45872.push();
        class_45872.translate(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc((int)515);
        RenderSystem.depthMask((boolean)false);
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (BlockParticleBatch batch : this.pendingParticles) {
            float f4 = batch.getRadius();
            float f5 = batch.getOpacity();
            f3 = batch.getShellWidth();
            f2 = f4 - f3;
            f = f4 + f3;
            ColorRGBA colorRGBA = batch.getColor();
            for (BlockParticle particle : batch.getParticles()) {
                float f6;
                float f7;
                if (!(particle.getDistance() >= (double)f2) || !(particle.getDistance() <= (double)f) || !((f7 = f5 * (f6 = (float)Math.pow(Math.max(0.0f, Math.min(1.0f, 1.0f - Math.abs((float)(particle.getDistance() - (double)f4)) / f3)), 0.5)) * 0.15f) > 0.005f)) continue;
                RenderUtils.drawFilledBox(class_45872, class_2872, particle.getBox(), colorRGBA.withAlpha(f7 * 255.0f));
            }
        }
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        outlineBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (BlockParticleBatch batch : this.pendingParticles) {
            f3 = batch.getRadius();
            f2 = batch.getOpacity();
            f = batch.getShellWidth();
            float f8 = f3 - f;
            float f9 = f3 + f;
            ColorRGBA color = batch.getColor();
            for (BlockParticle blockParticle : batch.getParticles()) {
                float f10;
                float f11;
                if (!(blockParticle.distance >= (double)f8) || !(blockParticle.distance <= (double)f9) || !((f11 = f2 * (f10 = (float)Math.pow(Math.max(0.0f, Math.min(1.0f, 1.0f - Math.abs((float)(blockParticle.distance - (double)f3)) / f)), 0.5)) * 0.5f) > 0.005f)) continue;
                RenderUtils.drawBoxOutline(class_45872, outlineBuffer, blockParticle.box, color.withAlpha(f11 * 255.0f));
            }
        }
        BuiltBuffer class_98013 = outlineBuffer.endNullable();
        if (class_98013 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98013);
        }
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        class_45872.pop();
        this.pendingParticles.removeIf(BlockParticleBatch::isExpired);
    };
    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> this.pendingParticles.clear();

    private BlockParticleRenderer() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public void enqueueBlockParticles(Vec3d VanillaChestLootTableGenerator, float f, float f2, float f3) {
        this.enqueueBlockParticlesWithColor(VanillaChestLootTableGenerator, f, f2, f3, ColorPalette.getAccentColor());
    }

    public void enqueueBlockParticlesWithColor(Vec3d VanillaChestLootTableGenerator, float f, float f2, float f3, ColorRGBA colorRGBA) {
        if (BlockParticleRenderer.minecraftClient.world == null) {
            return;
        }
        BlockPos adminsky = BlockPos.ofFloored((Position)VanillaChestLootTableGenerator);
        int n = (int)Math.ceil(f);
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>(n * n * n / 2);
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockPos adminsky2 = adminsky.add(i, j, k);
                    if (BlockParticleRenderer.minecraftClient.world.getBlockState(adminsky2).getBlock() == Blocks.AIR || BlockParticleRenderer.minecraftClient.world.getBlockState(adminsky2).getBlock() instanceof FlowerBlock || BlockParticleRenderer.minecraftClient.world.getBlockState(adminsky2).getBlock() == Blocks.SHORT_GRASS || BlockParticleRenderer.minecraftClient.world.getBlockState(adminsky2).getBlock() == Blocks.TALL_GRASS) continue;
                    hashSet.add(adminsky2);
                }
            }
        }
        ArrayList<BlockParticle> arrayList = new ArrayList<BlockParticle>(hashSet.size() / 3);
        for (BlockPos adminsky3 : hashSet) {
            boolean bl = false;
            for (Direction class_23502 : Direction.values()) {
                if (hashSet.contains(adminsky3.offset(class_23502))) continue;
                bl = true;
                break;
            }
            if (!bl) continue;
            double d = VanillaChestLootTableGenerator.distanceTo(adminsky3.toCenterPos());
            arrayList.add(new BlockParticle(new Box(adminsky3).expand(0.002), d));
        }
        if (arrayList.isEmpty()) {
            for (BlockPos adminsky4 : hashSet) {
                double d = VanillaChestLootTableGenerator.distanceTo(adminsky4.toCenterPos());
                arrayList.add(new BlockParticle(new Box(adminsky4).expand(0.002), d));
            }
        }
        this.pendingParticles.add(new BlockParticleBatch(arrayList, (long)(f2 * 1000.0f), f, f3, colorRGBA));
    }

    @Generated
    public List<BlockParticleBatch> getPendingParticles() {
        return this.pendingParticles;
    }

    @Generated
    public EventListener<Render3DEvent> getRender3DEventListener() {
        return this.render3DEventListener;
    }

    @Generated
    public EventListener<WorldChangeEvent> getWorldChangeListener() {
        return this.worldChangeListener;
    }

    public static final class BlockParticleBatch {
        private final List<BlockParticle> particles;
        private final long lifetimeMillis;
        private final float radius;
        private final float opacity;
        private final float shellWidth = 1.0f;
        private final ColorRGBA color;
        private final long createdAt = System.currentTimeMillis();

        BlockParticleBatch(List<BlockParticle> particles, long lifetimeMillis, float radius, float opacity, ColorRGBA color) {
            this.particles = particles;
            this.lifetimeMillis = lifetimeMillis;
            this.radius = radius;
            this.opacity = opacity;
            this.color = color;
        }

        public List<BlockParticle> getParticles() {
            return this.particles;
        }

        public float getRadius() {
            return this.radius;
        }

        public float getOpacity() {
            return this.opacity;
        }

        public float getShellWidth() {
            return this.shellWidth;
        }

        public ColorRGBA getColor() {
            return this.color;
        }

        public boolean isExpired() {
            return this.lifetimeMillis >= 0L && System.currentTimeMillis() - this.createdAt >= this.lifetimeMillis;
        }
    }

    static final class BlockParticle {
        private final Box box;
        private final double distance;

        BlockParticle(Box HorizontalFacingBlock, double d) {
            this.box = HorizontalFacingBlock;
            this.distance = d;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "box", "distance");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "box", "distance");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "box", "distance");
        }

        public Box getBox() {
            return this.box;
        }

        public double getDistance() {
            return this.distance;
        }
    }
}
