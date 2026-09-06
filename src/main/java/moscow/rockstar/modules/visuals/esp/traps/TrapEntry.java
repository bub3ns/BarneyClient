package moscow.rockstar.modules.visuals.esp.traps;

import net.minecraft.util.math.BlockPos;

final class TrapEntry {
    final BlockPos blockPosition;
    final int trapDepth;
    final boolean isPrivateTrap;

    TrapEntry(BlockPos blockPosition, int trapDepth, boolean privateTrap) {
        this.blockPosition = blockPosition;
        this.trapDepth = trapDepth;
        this.isPrivateTrap = privateTrap;
    }
}
