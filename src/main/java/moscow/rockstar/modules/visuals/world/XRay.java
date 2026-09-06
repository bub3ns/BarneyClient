/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.BlockView
 *  net.minecraft.World
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.Box
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3d
 *  net.minecraft.VoxelShape
 *  net.minecraft.BlockState
 *  net.minecraft.ChunkStatus
 *  net.minecraft.WorldChunk
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.ClientChunkManager
 *  net.minecraft.BuiltBuffer
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.modules.visuals.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.BlockItemSetting;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.render.BuiltBuffer;
import org.jetbrains.annotations.NotNull;
import pyrock.events.game.AncientDebrisEvent;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="XRay", category=ModuleCategory.VISUALS, description="modules.descriptions.xray")
public class XRay
extends Module {
    private final Set<BlockPos> selectedBlocks = ConcurrentHashMap.newKeySet();
    private BlockItemSetting bigBlock;
    private static final Map<Block, ColorRGBA> savedBlockStates = new HashMap<Block, ColorRGBA>();
    private final Map<BlockPos, Long> blockColors = new ConcurrentHashMap<BlockPos, Long>();
    private static final List<Block> blockEntries = List.of(Blocks.ANCIENT_DEBRIS, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.NETHER_QUARTZ_ORE);
    private int KEY_BIND = 0;
    private int DEFAULT_KEY_BIND = 0;
    private int processedBlockCount = 0;
    private int visibleBlockCount = 0;
    private final EventListener<Render3DEvent> onRender3DEvent = render3DEvent -> {
        if (XRay.minecraftClient.world == null || XRay.minecraftClient.player == null) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Camera class_41842 = XRay.minecraftClient.gameRenderer.getCamera();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        class_45872.push();
        class_45872.translate(-VanillaChestLootTableGenerator.getX(), -VanillaChestLootTableGenerator.getY(), -VanillaChestLootTableGenerator.getZ());
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.lineWidth((float)10.0f);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        double d = 999999.0;
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (BlockPos adminsky : this.selectedBlocks) {
            if (XRay.minecraftClient.player.squaredDistanceTo(adminsky.toCenterPos()) > d) continue;
            Box HorizontalFacingBlock = this.getBoundingBox(adminsky);
            Block class_22482 = XRay.minecraftClient.world.getBlockState(adminsky).getBlock();
            if (class_22482 != Blocks.ANCIENT_DEBRIS && this.isBlockInRange(adminsky)) {
                class_22482 = Blocks.ANCIENT_DEBRIS;
            }
            RenderUtils.drawFilledBox(render3DEvent.getMatrices(), class_2872, HorizontalFacingBlock, this.getBlockRenderColor(class_22482).withAlpha(30.0f));
        }
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        class_45872.pop();
    };
    private final EventListener<WorldChangeEvent> onWorldChangeEvent = worldChangeEvent -> {
        this.selectedBlocks.clear();
        this.blockColors.clear();
    };
    private final EventListener<AncientDebrisEvent> onAncientDebrisEvent = ancientDebrisEvent -> {
        if (!this.isXrayWorldReady() || !this.bigBlock.isBlockSelected(Blocks.ANCIENT_DEBRIS)) {
            return;
        }
        long l = System.currentTimeMillis() + 12000L;
        for (BlockPos adminsky : ancientDebrisEvent.getPositions()) {
            BlockPos adminsky2 = adminsky.toImmutable();
            this.blockColors.put(adminsky2, l);
            this.selectedBlocks.add(adminsky2);
        }
    };

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.bigBlock = new BlockItemSetting(this, "modules.settings.xray.big_block");
    }

    public XRay() {
        this.initializeSettings();
        this.finishBlockScan();
    }

    public void processChunk(WorldChunk class_28182) {
        if (XRay.minecraftClient.world == null || class_28182 == null) {
            return;
        }
        int n = class_28182.getPos().getStartX();
        int n2 = class_28182.getPos().getStartZ();
        for (int i = 0; i < 16; ++i) {
            for (int j = XRay.minecraftClient.world.getBottomY(); j < XRay.minecraftClient.world.getTopYInclusive(); ++j) {
                for (int k = 0; k < 16; ++k) {
                    BlockPos adminsky = new BlockPos(n + i, j, n2 + k);
                    BlockState class_26802 = class_28182.getBlockState(adminsky);
                    if (class_26802.isAir() || !this.isBlockSelected(class_26802.getBlock())) continue;
                    this.selectedBlocks.add(adminsky);
                }
            }
        }
    }

    @Override
    public void onEnable() {
        if (!EntityUtils.isClientWorldReady()) {
            return;
        }
        this.finishBlockScan();
        this.selectedBlocks.clear();
        ClientChunkManager PackageInfo6312 = XRay.minecraftClient.world.getChunkManager();
        int n = XRay.minecraftClient.options != null ? (Integer)XRay.minecraftClient.options.getViewDistance().getValue() : 8;
        Runnable runnable = this.createChunkRenderTask(n, PackageInfo6312);
        if (minecraftClient.isOnThread()) {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.start();
        } else {
            runnable.run();
        }
        super.onEnable();
    }

    @NotNull
    private Runnable createChunkRenderTask(int n, ClientChunkManager PackageInfo6312) {
        int n2 = Math.max(1, n);
        int n3 = XRay.minecraftClient.player.getChunkPos().x;
        int n4 = XRay.minecraftClient.player.getChunkPos().z;
        Runnable runnable = () -> {
            for (int i = -n2; i <= n2; ++i) {
                for (int j = -n2; j <= n2; ++j) {
                    WorldChunk class_28182 = PackageInfo6312.getChunk(n3 + i, n4 + j, ChunkStatus.FULL, false);
                    if (class_28182 == null) continue;
                    this.processChunk(class_28182);
                }
            }
        };
        return runnable;
    }

    @Override
    public void onDisable() {
        this.selectedBlocks.clear();
        this.blockColors.clear();
        this.KEY_BIND = 0;
        this.DEFAULT_KEY_BIND = 0;
        this.processedBlockCount = 0;
        this.visibleBlockCount = 0;
        super.onDisable();
    }

    @Override
    public void onTick() {
        this.updateChunkRender();
        this.updateBlockScan();
        this.scanVisibleBlocks();
        super.onTick();
    }

    private void scanVisibleBlocks() {
        this.selectedBlocks.removeIf(adminsky -> !this.isBlockVisible((BlockPos)adminsky));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateBlockScan() {
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        Set<BlockPos> set = this.selectedBlocks;
        synchronized (set) {
            for (BlockPos adminsky : this.selectedBlocks) {
                Block class_22482 = XRay.minecraftClient.world.getBlockState(adminsky).getBlock();
                if (class_22482 == Blocks.DIAMOND_ORE && this.bigBlock.isBlockSelected(Blocks.DIAMOND_ORE)) {
                    ++n;
                    continue;
                }
                if (class_22482 == Blocks.ANCIENT_DEBRIS) {
                    ++n2;
                    continue;
                }
                if (class_22482 == Blocks.GOLD_ORE && this.bigBlock.isBlockSelected(Blocks.GOLD_ORE)) {
                    ++n3;
                    continue;
                }
                if (class_22482 != Blocks.LAPIS_ORE || !this.bigBlock.isBlockSelected(Blocks.LAPIS_ORE)) continue;
                ++n4;
            }
        }
        this.KEY_BIND = n;
        this.DEFAULT_KEY_BIND = n2;
        this.processedBlockCount = n3;
        this.visibleBlockCount = n4;
    }

    public boolean isBlockSelected(Block class_22482) {
        return this.bigBlock.isBlockSelected(class_22482);
    }

    private ColorRGBA getBlockRenderColor(Block class_22482) {
        return savedBlockStates.getOrDefault(class_22482, ColorPalette.WHITE);
    }

    private boolean isBlockVisible(BlockPos adminsky) {
        if (XRay.minecraftClient.player == null || XRay.minecraftClient.options == null) {
            return false;
        }
        int n = (Integer)XRay.minecraftClient.options.getViewDistance().getValue();
        double d = (double)Math.max(1, n + 1) * 16.0;
        double d2 = d * d;
        return XRay.minecraftClient.player.squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky)) <= d2;
    }

    public boolean isXrayWorldReady() {
        if (XRay.minecraftClient.world == null) {
            return false;
        }
        if (XRay.minecraftClient.world.getRegistryKey() != World.NETHER) {
            return false;
        }
        return ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) || ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD) || ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY);
    }

    public boolean isBlockInRange(BlockPos adminsky) {
        return this.blockColors.containsKey(adminsky);
    }

    private void updateChunkRender() {
        if (this.blockColors.isEmpty()) {
            return;
        }
        long l = System.currentTimeMillis();
        this.blockColors.entrySet().removeIf(entry -> {
            if ((Long)entry.getValue() <= l) {
                this.selectedBlocks.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    private Box getBoundingBox(BlockPos adminsky) {
        BlockState class_26802 = XRay.minecraftClient.world.getBlockState(adminsky);
        VoxelShape class_2652 = class_26802.getOutlineShape((BlockView)XRay.minecraftClient.world, adminsky);
        if (class_2652.isEmpty()) {
            return new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(adminsky);
        }
        return class_2652.getBoundingBox().offset(adminsky);
    }

    private static void renderSelectedBlock(Block class_22482, ColorRGBA colorRGBA) {
        savedBlockStates.put(class_22482, colorRGBA);
    }

    private void finishBlockScan() {
        if (this.bigBlock.getSelectedCount() > 0) {
            return;
        }
        blockEntries.forEach(this.bigBlock::selectBlock);
    }

    private static boolean isBlockConfigured(Block class_22482) {
        return class_22482 == Blocks.DIAMOND_ORE || class_22482 == Blocks.DEEPSLATE_DIAMOND_ORE;
    }

    private static boolean isBlockHidden(Block class_22482) {
        return class_22482 == Blocks.GOLD_ORE || class_22482 == Blocks.DEEPSLATE_GOLD_ORE || class_22482 == Blocks.NETHER_GOLD_ORE;
    }

    private static boolean isBlockHighlighted(Block class_22482) {
        return class_22482 == Blocks.LAPIS_ORE || class_22482 == Blocks.DEEPSLATE_LAPIS_ORE;
    }

    @Generated
    public Set<BlockPos> getSelectedBlocks() {
        return this.selectedBlocks;
    }

    @Generated
    public BlockItemSetting getBlockSelectionSetting() {
        return this.bigBlock;
    }

    @Generated
    public int getProcessedBlockCount() {
        return this.KEY_BIND;
    }

    @Generated
    public int getVisibleBlockCount() {
        return this.DEFAULT_KEY_BIND;
    }

    @Generated
    public int getScanCount() {
        return this.processedBlockCount;
    }

    @Generated
    public int getRenderCount() {
        return this.visibleBlockCount;
    }

    static {
        XRay.renderSelectedBlock(Blocks.ANCIENT_DEBRIS, new ColorRGBA(255.0f, 131.0f, 54.0f));
        XRay.renderSelectedBlock(Blocks.DIAMOND_ORE, new ColorRGBA(121.0f, 54.0f, 255.0f));
        XRay.renderSelectedBlock(Blocks.DEEPSLATE_DIAMOND_ORE, new ColorRGBA(145.0f, 92.0f, 255.0f));
        XRay.renderSelectedBlock(Blocks.EMERALD_ORE, new ColorRGBA(80.0f, 255.0f, 140.0f));
        XRay.renderSelectedBlock(Blocks.DEEPSLATE_EMERALD_ORE, new ColorRGBA(64.0f, 214.0f, 119.0f));
        XRay.renderSelectedBlock(Blocks.GOLD_ORE, new ColorRGBA(255.0f, 215.0f, 0.0f));
        XRay.renderSelectedBlock(Blocks.DEEPSLATE_GOLD_ORE, new ColorRGBA(255.0f, 191.0f, 0.0f));
        XRay.renderSelectedBlock(Blocks.NETHER_GOLD_ORE, new ColorRGBA(255.0f, 203.0f, 96.0f));
        XRay.renderSelectedBlock(Blocks.IRON_ORE, new ColorRGBA(210.0f, 210.0f, 210.0f));
        XRay.renderSelectedBlock(Blocks.DEEPSLATE_IRON_ORE, new ColorRGBA(180.0f, 180.0f, 180.0f));
        XRay.renderSelectedBlock(Blocks.LAPIS_ORE, new ColorRGBA(0.0f, 71.0f, 179.0f));
        XRay.renderSelectedBlock(Blocks.DEEPSLATE_LAPIS_ORE, new ColorRGBA(21.0f, 92.0f, 200.0f));
        XRay.renderSelectedBlock(Blocks.REDSTONE_ORE, new ColorRGBA(255.0f, 64.0f, 64.0f));
        XRay.renderSelectedBlock(Blocks.DEEPSLATE_REDSTONE_ORE, new ColorRGBA(214.0f, 48.0f, 48.0f));
        XRay.renderSelectedBlock(Blocks.COPPER_ORE, new ColorRGBA(255.0f, 140.0f, 80.0f));
        XRay.renderSelectedBlock(Blocks.DEEPSLATE_COPPER_ORE, new ColorRGBA(235.0f, 120.0f, 68.0f));
        XRay.renderSelectedBlock(Blocks.COAL_ORE, new ColorRGBA(84.0f, 84.0f, 84.0f));
        XRay.renderSelectedBlock(Blocks.DEEPSLATE_COAL_ORE, new ColorRGBA(64.0f, 64.0f, 64.0f));
        XRay.renderSelectedBlock(Blocks.NETHER_QUARTZ_ORE, new ColorRGBA(233.0f, 233.0f, 233.0f));
    }
}
