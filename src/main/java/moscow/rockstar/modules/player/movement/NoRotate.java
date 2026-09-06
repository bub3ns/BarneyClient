/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PlayerPosition
 *  net.minecraft.PlayerRotationS2CPacket
 *  net.minecraft.PlayerPositionLookS2CPacket
 *  net.minecraft.PositionFlag
 */
package moscow.rockstar.modules.player.movement;

import java.util.EnumSet;
import java.util.Set;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.mixin.accessors.PlayerPositionLookS2CPacketAccessor;
import moscow.rockstar.mixin.accessors.PlayerRotationS2CPacketAccessor;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.player.movement.rotation.RotationPacketListener;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.PlayerRotationS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import pyrock.events.network.ReceivePacketEvent;

@ModuleInfo(name="No Rotate", category=ModuleCategory.PLAYER, description="modules.descriptions.no_rotate")
public class NoRotate
extends Module {
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = new RotationPacketListener(this);

    public void handlePositionCorrection(PlayerPositionLookS2CPacket packet) {
        PlayerPosition correction = packet.change();
        PlayerPositionLookS2CPacketAccessor packetAccessor = (PlayerPositionLookS2CPacketAccessor)(Object)packet;
        packetAccessor.setChange(new PlayerPosition(correction.position(), correction.deltaMovement(), NoRotate.minecraftClient.player.getYaw(), NoRotate.minecraftClient.player.getPitch()));
        packetAccessor.setRelatives(this.filterPacketFields(packet.relatives()));
    }

    public void handleRotationUpdate(PlayerRotationS2CPacket packet) {
        PlayerRotationS2CPacketAccessor packetAccessor = (PlayerRotationS2CPacketAccessor)(Object)packet;
        packetAccessor.setYRot(NoRotate.minecraftClient.player.getYaw());
        packetAccessor.setXRot(NoRotate.minecraftClient.player.getPitch());
    }

    private Set<PositionFlag> filterPacketFields(Set<PositionFlag> set) {
        EnumSet<PositionFlag> enumSet = set.isEmpty() ? EnumSet.noneOf(PositionFlag.class) : EnumSet.copyOf(set);
        enumSet.remove(PositionFlag.Y_ROT);
        enumSet.remove(PositionFlag.X_ROT);
        return enumSet;
    }
}
