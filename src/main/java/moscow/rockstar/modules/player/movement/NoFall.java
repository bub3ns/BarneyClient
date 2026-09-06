/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.PlayerMoveC2SPacket$Full
 *  net.minecraft.PlayerInteractItemC2SPacket
 */
package moscow.rockstar.modules.player.movement;

import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="No Fall", category=ModuleCategory.PLAYER)
public class NoFall
extends Module {
    @Override
    @Compile
    public void onTick() {
        if ((double)NoFall.minecraftClient.player.fallDistance > 2.5) {
            Vec3d VanillaChestLootTableGenerator = NoFall.minecraftClient.player.getPos();
            NoFall.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z, NoFall.minecraftClient.player.getYaw(), NoFall.minecraftClient.player.getPitch(), true, true));
            NoFall.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, NoFall.minecraftClient.player.getYaw(), NoFall.minecraftClient.player.getPitch()));
            NoFall.minecraftClient.player.fallDistance = 0.0f;
        }
    }
}

