/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  moscow.rockstar.modules.other.admin.BlockPos$Mutable
 *  moscow.rockstar.render.world.SchematicRenderer$SchematicBlock
 *  net.minecraft.BakedModel
 *  net.minecraft.BlockRenderView
 *  net.minecraft.RenderLayer
 *  net.minecraft.BlockView
 *  net.minecraft.Block
 *  net.minecraft.BlockArgumentParser
 *  net.minecraft.Direction
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockRenderType
 *  net.minecraft.NbtCompound
 *  net.minecraft.NbtSizeTracker
 *  net.minecraft.NbtIo
 *  net.minecraft.VoxelShape
 *  net.minecraft.BlockState
 *  net.minecraft.EmptyBlockView
 *  net.minecraft.MatrixStack
 *  net.minecraft.MatrixStack$Entry
 *  net.minecraft.VertexConsumer
 *  net.minecraft.VertexConsumerProvider$Immediate
 *  net.minecraft.OverlayTexture
 *  net.minecraft.Random
 *  net.minecraft.RegistryWrapper
 *  net.minecraft.BakedQuad
 *  net.minecraft.Registries
 */
package moscow.rockstar.render.world;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import moscow.rockstar.core.ClientAccess;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.render.layers.SchematicRenderLayer;
import moscow.rockstar.render.world.SchematicRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.world.BlockRenderView;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.world.BlockView;
import net.minecraft.block.Block;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockRenderType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.math.random.Random;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.registry.Registries;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;

public final class SchematicRenderer
implements ClientAccess {
    private static final Map<String, SchematicRenderer> schematicsByName = new HashMap<String, SchematicRenderer>();
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final float HIDDEN_ALPHA = 0.35f;
    private static final SchematicRenderer EMPTY_SCHEMATIC = new SchematicRenderer(new SchematicBlock[0], Set.of(), Vec3i.ZERO);
    private final SchematicBlock[] blocks;
    private final Set<Integer> occludingPositions;
    private final Vec3i offset;

    private SchematicRenderer(SchematicBlock[] schematicBlockArray, Set<Integer> set, Vec3i class_23822) {
        this.blocks = schematicBlockArray;
        this.occludingPositions = set;
        this.offset = class_23822;
    }

    public static SchematicRenderer loadSchematic(String string) {
        return schematicsByName.computeIfAbsent(string, SchematicRenderer::loadSchematicResource);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static SchematicRenderer loadSchematicResource(String string) {
        try (InputStream inputStream = SchematicRenderer.class.getResourceAsStream("/assets/rockstar/schematics/" + string + ".schem");){
            if (inputStream == null) {
                SchematicRenderer schematicRenderer2 = EMPTY_SCHEMATIC;
                return schematicRenderer2;
            }
            NbtCompound class_24872 = NbtIo.readCompressed((InputStream)inputStream, (NbtSizeTracker)NbtSizeTracker.ofUnlimitedBytes()).getCompound("Schematic");
            int n = class_24872.getShort("Width") & 0xFFFF;
            int n2 = class_24872.getShort("Length") & 0xFFFF;
            int[] nArray = class_24872.getIntArray("Offset");
            NbtCompound class_24873 = class_24872.getCompound("Blocks");
            NbtCompound class_24874 = class_24873.getCompound("Palette");
            BlockState[] class_2680Array = new BlockState[class_24874.getKeys().size()];
            for (String blockStateId : class_24874.getKeys()) {
                class_2680Array[class_24874.getInt(blockStateId)] = BlockArgumentParser.block(Registries.BLOCK, blockStateId, false).blockState();
            }
            byte[] blockData = class_24873.getByteArray("Data");
            ArrayList<SchematicBlock> schematicBlocks = new ArrayList<>();
            HashSet<Integer> hashSet = new HashSet<Integer>();
            int n3 = 0;
            int n4 = 0;
            while (n4 < blockData.length) {
                BlockState class_26802;
                int encodedByte;
                int n5 = 0;
                int n6 = 0;
                do {
                    encodedByte = blockData[n4++] & 0xFF;
                    n5 |= (encodedByte & 0x7F) << n6;
                    n6 += 7;
                } while ((encodedByte & 0x80) != 0);
                BlockState class_26803 = class_26802 = n5 < class_2680Array.length ? class_2680Array[n5] : null;
                if (class_26802 != null && !class_26802.isAir()) {
                    int n7 = n3 % n;
                    int n8 = n3 / (n * n2);
                    int n9 = n3 / n % n2;
                    schematicBlocks.add(new SchematicBlock(n7, n8, n9, class_26802));
                    if (Block.isShapeFullCube((VoxelShape)class_26802.getCollisionShape((BlockView)EmptyBlockView.INSTANCE, BlockPos.ORIGIN))) {
                        hashSet.add(SchematicRenderer.packBlockPosition(n7, n8, n9));
                    }
                }
                ++n3;
            }
            SchematicBlock[] schematicBlockArray = schematicBlocks.toArray(new SchematicBlock[0]);
            SchematicRenderer schematicRenderer = nArray.length == 3 ? new SchematicRenderer(schematicBlockArray, hashSet, new Vec3i(nArray[0], nArray[1], nArray[2])) : new SchematicRenderer(schematicBlockArray, hashSet, Vec3i.ZERO);
            return schematicRenderer;
        }
        catch (Exception exception) {
            return EMPTY_SCHEMATIC;
        }
    }

    public void renderSchematic(Render3DEvent render3DEvent, BlockPos adminsky, ColorRGBA colorRGBA, float f) {
        if (this.blocks.length == 0 || SchematicRenderer.minecraftClient.world == null) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = SchematicRenderer.minecraftClient.gameRenderer.getCamera().getPos();
        int n = adminsky.getX() + this.offset.getX();
        int n2 = adminsky.getY() + this.offset.getY();
        int n3 = adminsky.getZ() + this.offset.getZ();
        double d = (double)n - VanillaChestLootTableGenerator.x;
        double d2 = (double)n2 - VanillaChestLootTableGenerator.y;
        double d3 = (double)n3 - VanillaChestLootTableGenerator.z;
        Arrays.sort(this.blocks, Comparator.comparingDouble(schematicBlock -> {
            double d4 = d + (double)schematicBlock.getX() + 0.5;
            double d5 = d2 + (double)schematicBlock.getY() + 0.5;
            double d6 = d3 + (double)schematicBlock.getZ() + 0.5;
            return d4 * d4 + d5 * d5 + d6 * d6;
        }));
        VertexConsumerProvider.Immediate class_45982 = minecraftClient.getBufferBuilders().getEntityVertexConsumers();
        RenderLayer class_19212 = SchematicRenderLayer.getHiddenLayer();
        this.renderSchematicBlocks(render3DEvent, new ColoredVertexConsumer(class_45982.getBuffer(class_19212), colorRGBA, 0.35f), true, n, n2, n3, d, d2, d3);
        class_45982.draw(class_19212);
        RenderLayer class_19213 = SchematicRenderLayer.getVisibleLayer();
        this.renderSchematicBlocks(render3DEvent, new ColoredVertexConsumer(class_45982.getBuffer(class_19213), colorRGBA, f), false, n, n2, n3, d, d2, d3);
        class_45982.draw(class_19213);
    }

    private void renderSchematicBlocks(Render3DEvent render3DEvent, VertexConsumer class_45882, boolean bl, int n, int n2, int n3, double d, double d2, double d3) {
        MatrixStack class_45872 = render3DEvent.getMatrices();
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        Random class_58192 = Random.create();
        for (SchematicBlock schematicBlock : this.blocks) {
            class_23392.set(n + schematicBlock.getX(), n2 + schematicBlock.getY(), n3 + schematicBlock.getZ());
            class_45872.push();
            class_45872.translate(d + (double)schematicBlock.getX(), d2 + (double)schematicBlock.getY(), d3 + (double)schematicBlock.getZ());
            this.renderSchematicBlock(schematicBlock, class_45872.peek(), class_45882, class_23392, class_58192, bl, d + (double)schematicBlock.getX() + 0.5, d2 + (double)schematicBlock.getY() + 0.5, d3 + (double)schematicBlock.getZ() + 0.5);
            class_45872.pop();
        }
    }

    private void renderSchematicBlock(SchematicBlock schematicBlock, MatrixStack.Entry class_46652, VertexConsumer class_45882, BlockPos.Mutable class_23392, Random class_58192, boolean bl, double d, double d2, double d3) {
        BlockState class_26802 = schematicBlock.getBlockState();
        if (class_26802.getRenderType() != BlockRenderType.MODEL) {
            return;
        }
        BakedModel class_10872 = minecraftClient.getBlockRenderManager().getModel(class_26802);
        int n = minecraftClient.getBlockColors().getColor(class_26802, (BlockRenderView)SchematicRenderer.minecraftClient.world, (BlockPos)class_23392, 0);
        float f = (float)(n >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(n & 0xFF) / 255.0f;
        for (Direction class_23502 : DIRECTIONS) {
            if (this.isFaceOccluded(schematicBlock, class_23502, class_23392, bl, d, d2, d3)) continue;
            class_58192.setSeed(42L);
            this.renderBakedQuads(class_46652, class_45882, class_10872.getQuads(class_26802, class_23502, class_58192), f, f2, f3, false, d, d2, d3);
        }
        class_58192.setSeed(42L);
        this.renderBakedQuads(class_46652, class_45882, class_10872.getQuads(class_26802, null, class_58192), f, f2, f3, true, d, d2, d3);
    }

    private boolean isFaceOccluded(SchematicBlock schematicBlock, Direction class_23502, BlockPos.Mutable class_23392, boolean bl, double d, double d2, double d3) {
        Vec3i class_23822 = class_23502.getVector();
        if ((double)class_23822.getX() * (d + (double)class_23822.getX() * 0.5) + (double)class_23822.getY() * (d2 + (double)class_23822.getY() * 0.5) + (double)class_23822.getZ() * (d3 + (double)class_23822.getZ() * 0.5) >= 0.0) {
            return true;
        }
        if (this.occludingPositions.contains(SchematicRenderer.packBlockPosition(schematicBlock.getX() + class_23822.getX(), schematicBlock.getY() + class_23822.getY(), schematicBlock.getZ() + class_23822.getZ()))) {
            return true;
        }
        if (bl) {
            return false;
        }
        class_23392.move(class_23502);
        boolean bl2 = SchematicRenderer.minecraftClient.world.getBlockState((BlockPos)class_23392).isOpaqueFullCube();
        class_23392.move(class_23502.getOpposite());
        return bl2;
    }

    private void renderBakedQuads(MatrixStack.Entry class_46652, VertexConsumer class_45882, List<BakedQuad> list, float f, float f2, float f3, boolean bl, double d, double d2, double d3) {
        for (BakedQuad Atlas : list) {
            if (bl && this.isQuadBackfacing(Atlas, d, d2, d3)) continue;
            boolean bl2 = Atlas.hasTint();
            class_45882.quad(class_46652, Atlas, bl2 ? f : 1.0f, bl2 ? f2 : 1.0f, bl2 ? f3 : 1.0f, 1.0f, 0xF000F0, OverlayTexture.DEFAULT_UV);
        }
    }

    private boolean isQuadBackfacing(BakedQuad Atlas, double d, double d2, double d3) {
        int[] nArray = Atlas.getVertexData();
        int n = nArray.length / 4;
        float f = Float.intBitsToFloat(nArray[0]);
        float f2 = Float.intBitsToFloat(nArray[1]);
        float f3 = Float.intBitsToFloat(nArray[2]);
        float f4 = Float.intBitsToFloat(nArray[n]);
        float f5 = Float.intBitsToFloat(nArray[n + 1]);
        float f6 = Float.intBitsToFloat(nArray[n + 2]);
        float f7 = Float.intBitsToFloat(nArray[n * 2]);
        float f8 = Float.intBitsToFloat(nArray[n * 2 + 1]);
        float f9 = Float.intBitsToFloat(nArray[n * 2 + 2]);
        float f10 = Float.intBitsToFloat(nArray[n * 3]);
        float f11 = Float.intBitsToFloat(nArray[n * 3 + 1]);
        float f12 = Float.intBitsToFloat(nArray[n * 3 + 2]);
        float f13 = f4 - f;
        float f14 = f5 - f2;
        float f15 = f6 - f3;
        float f16 = f7 - f;
        float f17 = f8 - f2;
        float f18 = f9 - f3;
        double d4 = f14 * f18 - f15 * f17;
        double d5 = f15 * f16 - f13 * f18;
        double d6 = f13 * f17 - f14 * f16;
        if (d4 == 0.0 && d5 == 0.0 && d6 == 0.0) {
            return false;
        }
        return d4 * (d - 0.5 + (double)(f + f4 + f7 + f10) / 4.0) + d5 * (d2 - 0.5 + (double)(f2 + f5 + f8 + f11) / 4.0) + d6 * (d3 - 0.5 + (double)(f3 + f6 + f9 + f12) / 4.0) >= 0.0;
    }

    private static int packBlockPosition(int n, int n2, int n3) {
        return ((n + 1) * 512 + (n2 + 1)) * 512 + (n3 + 1);
    }

    public static final class SchematicBlock {
        private final int x;
        private final int y;
        private final int z;
        private final BlockState blockState;

        SchematicBlock(int x, int y, int z, BlockState blockState) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockState = blockState;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getZ() {
            return this.z;
        }

        public BlockState getBlockState() {
            return this.blockState;
        }
    }

    static final class ColoredVertexConsumer
    implements VertexConsumer {
        private final VertexConsumer delegate;
        private final ColorRGBA tint;
        private final float alpha;

        ColoredVertexConsumer(VertexConsumer class_45882, ColorRGBA colorRGBA, float f) {
            this.delegate = class_45882;
            this.tint = colorRGBA;
            this.alpha = f;
        }

        public VertexConsumer vertex(float f, float f2, float f3) {
            this.delegate.vertex(f, f2, f3);
            return this;
        }

        public VertexConsumer color(int n, int n2, int n3, int n4) {
            this.delegate.color((int)((float)n * this.tint.getRed() / 255.0f), (int)((float)n2 * this.tint.getGreen() / 255.0f), (int)((float)n3 * this.tint.getBlue() / 255.0f), (int)((float)n4 * this.alpha));
            return this;
        }

        public VertexConsumer texture(float f, float f2) {
            this.delegate.texture(f, f2);
            return this;
        }

        public VertexConsumer overlay(int n, int n2) {
            this.delegate.overlay(n, n2);
            return this;
        }

        public VertexConsumer light(int n, int n2) {
            this.delegate.light(n, n2);
            return this;
        }

        public VertexConsumer normal(float f, float f2, float f3) {
            this.delegate.normal(f, f2, f3);
            return this;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "delegate", "tint", "alpha");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "delegate", "tint", "alpha");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "delegate", "tint", "alpha");
        }

        public VertexConsumer getDelegate() {
            return this.delegate;
        }

        public ColorRGBA getTint() {
            return this.tint;
        }

        public float getAlpha() {
            return this.alpha;
        }
    }
}
