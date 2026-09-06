/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Packet
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 */
package moscow.rockstar.inventory;

import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.inventory.HotbarSlot;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public final class HotbarActionService
implements ClientAccess {
    public static void withTemporaryHotbarSlot(HotbarSlot hotbarSlot, Runnable runnable) {
        if (hotbarSlot == null || runnable == null || HotbarActionService.minecraftClient.player == null || minecraftClient.getNetworkHandler() == null) {
            return;
        }
        int n = HotbarActionService.minecraftClient.player.getInventory().selectedSlot;
        if (hotbarSlot.getSlotIndex() == n) {
            runnable.run();
            return;
        }
        minecraftClient.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(hotbarSlot.getSlotIndex()));
        try {
            runnable.run();
        }
        finally {
            minecraftClient.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(n));
        }
    }

    @Generated
    private HotbarActionService() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

