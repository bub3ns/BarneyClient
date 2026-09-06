/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PlayerEntity
 *  net.minecraft.PlayerInventory
 *  net.minecraft.SlotActionType
 *  net.minecraft.ItemStack
 *  net.minecraft.Packet
 *  net.minecraft.BlockState
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.ClientPlayerEntity
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.world;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class BlockDropResolver {
    private BlockDropResolver() {
    }

    @Nullable
    public static ToolSlotSelection selectToolSlot(BlockState class_26802) {
        float f;
        ItemStack class_17992;
        int n;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity class_7462 = client.player;
        if (class_7462 == null) {
            return null;
        }
        PlayerInventory class_16612 = class_7462.getInventory();
        int n2 = -1;
        float f2 = 0.0f;
        boolean bl = false;
        for (n = 0; n < 9; ++n) {
            class_17992 = class_16612.getStack(n);
            if (class_17992.isEmpty() || !((f = BlockDropResolver.getMiningSpeed(class_17992, class_26802)) > f2)) continue;
            f2 = f;
            n2 = n;
            bl = true;
        }
        for (n = 9; n < 36; ++n) {
            class_17992 = class_16612.getStack(n);
            if (class_17992.isEmpty() || !((f = BlockDropResolver.getMiningSpeed(class_17992, class_26802)) > f2)) continue;
            f2 = f;
            n2 = n;
            bl = false;
        }
        if (n2 < 0) {
            return null;
        }
        return new ToolSlotSelection(n2, bl, f2);
    }

    public static float getMiningSpeed(ItemStack class_17992, BlockState class_26802) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return 1.0f;
        }
        return class_17992.getMiningSpeedMultiplier(class_26802);
    }

    public static boolean isBreakable(BlockState class_26802) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity class_7462 = client.player;
        if (class_7462 == null || client.interactionManager == null) {
            return false;
        }
        ToolSlotSelection toolSlotSelection = BlockDropResolver.selectToolSlot(class_26802);
        if (toolSlotSelection == null) {
            return false;
        }
        ItemStack class_17992 = class_7462.getMainHandStack();
        float f = BlockDropResolver.getMiningSpeed(class_17992, class_26802);
        if (toolSlotSelection.breakSpeed <= f) {
            return false;
        }
        if (toolSlotSelection.inHotbar) {
            int n = class_7462.getInventory().selectedSlot;
            if (toolSlotSelection.slotIndex == n) {
                return false;
            }
            BlockDropResolver.setSelectedToolSlot(toolSlotSelection.slotIndex);
            return true;
        }
        int n = class_7462.getInventory().selectedSlot;
        client.interactionManager.clickSlot(class_7462.currentScreenHandler.syncId, toolSlotSelection.slotIndex, n, SlotActionType.SWAP, (PlayerEntity)class_7462);
        return true;
    }

    public static void setSelectedToolSlot(int n) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        if (n < 0 || n > 8) {
            return;
        }
        if (client.player.getInventory().selectedSlot == n) {
            return;
        }
        client.player.getInventory().selectedSlot = n;
        client.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(n));
    }

    public static final class ToolSlotSelection {
        final int slotIndex;
        final boolean inHotbar;
        final float breakSpeed;

        public ToolSlotSelection(int n, boolean bl, float f) {
            this.slotIndex = n;
            this.inHotbar = bl;
            this.breakSpeed = f;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "slotIndex", "inHotbar", "breakSpeed");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "slotIndex", "inHotbar", "breakSpeed");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "slotIndex", "inHotbar", "breakSpeed");
        }

        public int getSlotIndex() {
            return this.slotIndex;
        }

        public boolean isInHotbar() {
            return this.inHotbar;
        }

        public float getBreakSpeed() {
            return this.breakSpeed;
        }
    }
}

