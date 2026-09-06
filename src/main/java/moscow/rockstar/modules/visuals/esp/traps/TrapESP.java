/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockView
 *  net.minecraft.ChunkPos
 *  net.minecraft.Block
 *  net.minecraft.ChestBlock
 *  net.minecraft.Direction
 *  net.minecraft.Direction$Type
 *  net.minecraft.HopperBlock
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec2f
 *  net.minecraft.PressurePlateBlock
 *  net.minecraft.TntBlock
 *  net.minecraft.TripwireHookBlock
 *  net.minecraft.TripwireBlock
 *  net.minecraft.Packet
 *  net.minecraft.BlockUpdateS2CPacket
 *  net.minecraft.ChunkDeltaUpdateS2CPacket
 *  net.minecraft.ChunkDataS2CPacket
 *  net.minecraft.BlockState
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.modules.visuals.esp.traps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.visuals.esp.traps.TrapEntry;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.util.Timer;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.block.HopperBlock;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec2f;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.block.TripwireBlock;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

@ModuleInfo(name="Trap ESP", category=ModuleCategory.VISUALS)
public class TrapESP
extends Module {
    private volatile List<TrapEntry> trapEntries = Collections.emptyList();
    private final Timer trapScanTimer = new Timer();
    private final Deque<BlockPos> pendingBlockChecks = new ConcurrentLinkedDeque<BlockPos>();
    private final Set<BlockPos> queuedBlockPositions = Collections.newSetFromMap(new ConcurrentHashMap());
    private final Map<Long, TrapEntry> detectedTraps = new ConcurrentHashMap<Long, TrapEntry>();
    private static final long ENTRY_TIMEOUT_MILLIS = 5000L;
    private static final int SCAN_GRID_RADIUS = 64;
    private static final int MAX_BLOCK_CHECKS_PER_TICK = 96;
    private static final int MAX_TRAP_DEPTH = 24;
    private static final int MIN_TRAP_HEIGHT = 5;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        BlockPos adminsky;
        if (TrapESP.minecraftClient.player == null || TrapESP.minecraftClient.world == null) {
            return;
        }
        if (this.trapScanTimer.hasElapsed(5000L) && this.pendingBlockChecks.isEmpty()) {
            this.enqueueNearbyBlocks();
            this.trapScanTimer.reset();
        }
        int n = 0;
        long l = 2000000L;
        long l2 = System.nanoTime();
        while (n < 96 && (adminsky = this.pendingBlockChecks.poll()) != null) {
            this.queuedBlockPositions.remove(adminsky);
            ++n;
            TrapEntry trapEntry = this.findTrapEntry(adminsky);
            long l3 = TrapESP.computeBlockKey(adminsky.getX(), adminsky.getZ());
            if (trapEntry != null) {
                this.detectedTraps.put(l3, trapEntry);
            } else {
                this.detectedTraps.remove(l3);
            }
            if (System.nanoTime() - l2 <= l) continue;
            break;
        }
        this.trapEntries = new ArrayList<TrapEntry>(this.detectedTraps.values());
    };
    private final EventListener<ReceivePacketEvent> packetListener = receivePacketEvent -> {
        if (TrapESP.minecraftClient.player == null || TrapESP.minecraftClient.world == null) {
            return;
        }
        try {
            Packet<?> packet = receivePacketEvent.getPacket();
            if (packet instanceof ChunkDataS2CPacket chunkDataPacket) {
                this.onChunkUpdate(new ChunkPos(chunkDataPacket.getChunkX(), chunkDataPacket.getChunkZ()));
            }
            if (packet instanceof ChunkDeltaUpdateS2CPacket deltaUpdatePacket) {
                deltaUpdatePacket.visitUpdates((updatedPosition, updatedState) -> this.enqueueUpdatedBlock(updatedPosition.getX(), updatedPosition.getZ()));
            }
            if (packet instanceof BlockUpdateS2CPacket blockUpdatePacket) {
                BlockPos updatedPosition = blockUpdatePacket.getPos();
                this.enqueueUpdatedBlock(updatedPosition.getX(), updatedPosition.getZ());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    };
    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> this.clearTrapEntries();
    private final EventListener<PreHudRenderEvent> preHudRenderListener = preHudRenderEvent -> {
        CustomDrawContext customDrawContext = preHudRenderEvent.getContext();
        MatrixStack class_45872 = customDrawContext.getMatrices();
        FontMetrics fontMetrics = Font.MEDIUM.metrics(9.0f);
        int n = (int)fontMetrics.getFontTopOffset();
        for (TrapEntry trapEntry : this.trapEntries) {
            Vec2f VanillaAdventureTabAdvancementGenerator = ProjectionUtils.projectToScreen(trapEntry.blockPosition.toCenterPos().add(0.0, 0.5, 0.0));
            if (VanillaAdventureTabAdvancementGenerator == null) continue;
            String string = Localization.translate("modules.trap_esp.label");
            String string2 = Localization.translateFormatted("modules.trap_esp.depth", trapEntry.trapDepth);
            String string3 = trapEntry.isPrivateTrap ? Localization.translate("modules.trap_esp.private") : Localization.translate("modules.trap_esp.no_private");
            int n2 = 9;
            int n3 = 6;
            int n4 = (int)(fontMetrics.measureText(string2) + (float)n3);
            float f = fontMetrics.measureText(string) + 6.0f + (float)n2 + 2.0f;
            float f2 = n * 2;
            float f3 = f2 * 3.0f;
            customDrawContext.pushMatrix();
            class_45872.translate(VanillaAdventureTabAdvancementGenerator.x, VanillaAdventureTabAdvancementGenerator.y - f3, 0.0f);
            customDrawContext.drawRect(-f / 2.0f, 0.0f, f, f2, ColorRGBA.BLACK.withAlpha(150.0f));
            customDrawContext.drawText(fontMetrics, string, -f / 2.0f + (float)n2 + 5.0f, 3.0f, ColorPalette.WHITE);
            customDrawContext.drawTexture(RockstarClient.resourceId("icons/trap.png"), -f / 2.0f + 2.0f, (float)n - (float)n2 / 2.0f, n2, n2, ColorPalette.WHITE);
            customDrawContext.drawRect((float)(-n4) / 2.0f, f2, n4, f2, ColorRGBA.BLACK.withAlpha(150.0f));
            customDrawContext.drawText(fontMetrics, string2, (float)(-n4) / 2.0f + 2.0f, f2 + 3.0f, ColorPalette.WHITE.withAlpha(200.0f));
            customDrawContext.drawRect(-(fontMetrics.measureText(string3) + 6.0f) / 2.0f, f2 * 2.0f, fontMetrics.measureText(string3) + 6.0f, f2, ColorRGBA.BLACK.withAlpha(150.0f));
            customDrawContext.drawText(fontMetrics, string3, -fontMetrics.measureText(string3) / 2.0f, f2 * 2.0f + 3.0f, ColorPalette.WHITE.withAlpha(200.0f));
            customDrawContext.popMatrix();
        }
    };

    @Override
    public void onDisable() {
        this.clearTrapEntries();
        super.onDisable();
    }

    @Override
    public void onEnable() {
        this.clearTrapEntries();
        this.enqueueNearbyBlocks();
        super.onEnable();
    }

    public void clearTrapEntries() {
        this.pendingBlockChecks.clear();
        this.queuedBlockPositions.clear();
        this.detectedTraps.clear();
        this.trapEntries = Collections.emptyList();
        this.trapScanTimer.reset();
    }

    private void enqueueNearbyBlocks() {
        if (TrapESP.minecraftClient.player == null) {
            return;
        }
        BlockPos adminsky = TrapESP.minecraftClient.player.getBlockPos();
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>(16641);
        for (int i = -64; i <= 64; ++i) {
            for (int j = -64; j <= 64; ++j) {
                BlockPos adminsky3 = new BlockPos(adminsky.getX() + i, adminsky.getY(), adminsky.getZ() + j);
                if (!this.queuedBlockPositions.add(adminsky3)) continue;
                arrayList.add(adminsky3);
            }
        }
        arrayList.sort(Comparator.comparingDouble(adminsky2 -> adminsky2.getSquaredDistance((Vec3i)adminsky)));
        this.pendingBlockChecks.addAll(arrayList);
    }

    private void onChunkUpdate(ChunkPos class_19232) {
        if (TrapESP.minecraftClient.player == null) {
            return;
        }
        int n = TrapESP.minecraftClient.player.getBlockPos().getY();
        int n2 = class_19232.getStartX();
        int n3 = class_19232.getStartZ();
        for (int i = n2; i < n2 + 16; ++i) {
            for (int j = n3; j < n3 + 16; ++j) {
                BlockPos adminsky = new BlockPos(i, n, j);
                if (!this.queuedBlockPositions.add(adminsky)) continue;
                this.pendingBlockChecks.add(adminsky);
            }
        }
    }

    private void enqueueUpdatedBlock(int n, int n2) {
        if (TrapESP.minecraftClient.player == null) {
            return;
        }
        int n3 = TrapESP.minecraftClient.player.getBlockPos().getY();
        BlockPos adminsky = new BlockPos(n, n3, n2);
        if (this.queuedBlockPositions.add(adminsky)) {
            this.pendingBlockChecks.add(adminsky);
        }
    }

    private TrapEntry findTrapEntry(BlockPos adminsky) {
        if (TrapESP.minecraftClient.player == null || TrapESP.minecraftClient.world == null) {
            return null;
        }
        int n = adminsky.getY();
        for (int i = 20; i >= -20; --i) {
            BlockPos adminsky2;
            BlockPos adminsky3;
            BlockPos adminsky4 = new BlockPos(adminsky.getX(), n + i, adminsky.getZ());
            if (!this.isTrapBlockValid(adminsky4)) continue;
            int n2 = 0;
            boolean bl = false;
            for (int j = 0; j < 24; ++j) {
                adminsky3 = adminsky4.down(j);
                BlockState class_26802 = TrapESP.minecraftClient.world.getBlockState(adminsky3);
                if (!class_26802.isAir() || !TrapESP.minecraftClient.world.getFluidState(adminsky3).isEmpty()) {
                    if (!bl) continue;
                    break;
                }
                int n3 = 0;
                for (Direction class_23502 : Direction.Type.HORIZONTAL) {
                    BlockPos adminsky5 = adminsky3.offset(class_23502);
                    BlockState class_26803 = TrapESP.minecraftClient.world.getBlockState(adminsky5);
                    if (class_26803.isAir() || class_26803.getCollisionShape((BlockView)TrapESP.minecraftClient.world, adminsky5).isEmpty()) continue;
                    ++n3;
                }
                if (n3 < 4) {
                    if (!bl) continue;
                    break;
                }
                if (!bl) {
                    bl = true;
                }
                ++n2;
            }
            if (n2 < 5) continue;
            BlockPos supportPosition = adminsky4.down(n2);
            BlockState supportState = TrapESP.minecraftClient.world.getBlockState(supportPosition);
            if (supportState.isAir() || supportState.getCollisionShape((BlockView)TrapESP.minecraftClient.world, supportPosition).isEmpty() || !this.isTrapBlockValidAtDistance(adminsky4, n2)) continue;
            boolean bl2 = this.isTrapBlockEligible(adminsky4, 6);
            return new TrapEntry(adminsky4, n2, bl2);
        }
        return null;
    }

    private boolean isTrapBlockValid(BlockPos adminsky) {
        if (TrapESP.minecraftClient.world == null) {
            return false;
        }
        int n = 0;
        for (int i = -1; i <= 1; ++i) {
            block1: for (int j = -1; j <= 1; ++j) {
                if (i == 0 && j == 0) continue;
                for (int k = 0; k <= 3; ++k) {
                    BlockPos adminsky2 = adminsky.add(i, k, j);
                    BlockState class_26802 = TrapESP.minecraftClient.world.getBlockState(adminsky2);
                    if (class_26802.isAir() || class_26802.getCollisionShape((BlockView)TrapESP.minecraftClient.world, adminsky2).isEmpty()) continue;
                    ++n;
                    continue block1;
                }
            }
        }
        return n >= 4;
    }

    private boolean isTrapBlockValidAtDistance(BlockPos adminsky, int n) {
        if (TrapESP.minecraftClient.world == null) {
            return false;
        }
        int n2 = 0;
        int n3 = 0;
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                Block class_22482;
                if (Math.abs(i) <= 1 && Math.abs(j) <= 1) continue;
                ++n3;
                BlockPos adminsky2 = adminsky.add(i, 0, j);
                BlockState class_26802 = TrapESP.minecraftClient.world.getBlockState(adminsky2);
                if (class_26802.isAir() || class_26802.getCollisionShape((BlockView)TrapESP.minecraftClient.world, adminsky2).isEmpty() || !(class_22482 = class_26802.getBlock()).getDefaultState().isOpaque()) continue;
                ++n2;
            }
        }
        return (double)n2 >= (double)n3 * 0.6;
    }

    private boolean isTrapBlockEligible(BlockPos adminsky, int n) {
        if (TrapESP.minecraftClient.player == null || TrapESP.minecraftClient.world == null) {
            return false;
        }
        BlockPos adminsky2 = adminsky.add(-n, -n, -n);
        BlockPos adminsky3 = adminsky.add(n, n, n);
        for (BlockPos adminsky4 : BlockPos.iterate((BlockPos)adminsky2, (BlockPos)adminsky3)) {
            Block class_22482;
            BlockState class_26802 = TrapESP.minecraftClient.world.getBlockState(adminsky4);
            if (class_26802.isAir() || !((class_22482 = class_26802.getBlock()) instanceof TntBlock) && !(class_22482 instanceof PressurePlateBlock) && !(class_22482 instanceof TripwireBlock) && !(class_22482 instanceof TripwireHookBlock) && !(class_22482 instanceof HopperBlock) && !(class_22482 instanceof ChestBlock)) continue;
            return true;
        }
        return false;
    }

    private static long computeBlockKey(int n, int n2) {
        return (long)n << 32 ^ (long)n2 & 0xFFFFFFFFL;
    }
}
