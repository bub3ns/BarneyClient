package moscow.rockstar.network.bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import moscow.rockstar.items.InventoryState;
import moscow.rockstar.render.shader.ShaderEntitySnapshot;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;

/**
 * State received by a remote bot connection.  This was previously confused
 * with one of the local path-navigation strategies by the decompiler.
 */
public final class BotWorldState {
    private final Map<BlockPos, BlockState> blockStates = new HashMap<>();
    private final Map<Integer, ShaderEntitySnapshot> entitySnapshots = new HashMap<>();
    private String serverAddress;
    private int serverPort;
    private int localEntityId = -1;
    private long tick;
    private InventoryState latestInventoryState;

    public BotWorldState(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void configureEndpoint(String address, int port) {
        this.serverAddress = address;
        this.serverPort = port;
    }

    public void start() {
        this.tick = 0L;
    }

    public void stop() {
        this.blockStates.clear();
        this.entitySnapshots.clear();
        this.latestInventoryState = null;
        this.localEntityId = -1;
    }

    public void update(InventoryState inventoryState) {
        this.latestInventoryState = inventoryState;
        this.tick++;
    }

    public void applyServerCorrection(InventoryState inventoryState) {
        this.latestInventoryState = inventoryState;
    }

    public void setLocalEntityId(int entityId) {
        this.localEntityId = entityId;
    }

    public void acceptPacket(Object packet) {
        if (packet instanceof BlockUpdateS2CPacket blockUpdate) {
            this.blockStates.put(blockUpdate.getPos().toImmutable(), blockUpdate.getState());
        }
        this.tick++;
    }

    public BlockState getBlockState(BlockPos position) {
        if (position == null) {
            return Blocks.AIR.getDefaultState();
        }
        return this.blockStates.getOrDefault(position, Blocks.AIR.getDefaultState());
    }

    public boolean isKnownSolid(BlockPos position) {
        return !this.getBlockState(position).isAir();
    }

    public boolean isValidBlock(BlockPos position) {
        return position != null && this.isKnownSolid(position);
    }

    public boolean isAlternativeBlock(BlockPos position) {
        return position != null && this.getBlockState(position) != Blocks.AIR.getDefaultState();
    }

    public int findInventorySlot(String[] searchTerms) {
        if (this.latestInventoryState == null || searchTerms == null || searchTerms.length == 0) {
            return -1;
        }
        ItemStack[] items = this.latestInventoryState.getInventoryItems();
        for (int slot = 0; slot < items.length; slot++) {
            ItemStack stack = items[slot];
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            String itemName = stack.getName().getString().toLowerCase(Locale.ROOT);
            for (String term : searchTerms) {
                if (term != null && itemName.contains(term.toLowerCase(Locale.ROOT))) {
                    return slot;
                }
            }
        }
        return -1;
    }

    public List<ShaderEntitySnapshot> getEntitySnapshots() {
        return Collections.unmodifiableList(new ArrayList<>(this.entitySnapshots.values()));
    }

    public int getTrackedEntityId() {
        return this.localEntityId;
    }

    public long getTick() {
        return this.tick;
    }

    public String getServerAddress() {
        return this.serverAddress;
    }

    public int getServerPort() {
        return this.serverPort;
    }
}
