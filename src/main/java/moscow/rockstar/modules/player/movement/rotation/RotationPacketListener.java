package moscow.rockstar.modules.player.movement.rotation;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.player.movement.NoRotate;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRotationS2CPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import pyrock.events.network.ReceivePacketEvent;

/** Receives server rotation corrections for the No Rotate module. */
public final class RotationPacketListener implements EventListener<ReceivePacketEvent> {
    private final NoRotate noRotateModule;

    public RotationPacketListener(NoRotate noRotateModule) {
        this.noRotateModule = noRotateModule;
    }

    public void onReceivePacket(ReceivePacketEvent event) {
        if (ClientAccess.minecraftClient.player == null || ClientAccess.minecraftClient.world == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof PlayerPositionLookS2CPacket positionCorrection) {
            this.noRotateModule.handlePositionCorrection(positionCorrection);
        } else if (packet instanceof PlayerRotationS2CPacket rotationUpdate) {
            this.noRotateModule.handleRotationUpdate(rotationUpdate);
        } else if (packet instanceof LookAtS2CPacket) {
            event.cancel();
        }
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void onEvent(ReceivePacketEvent event) {
        this.onReceivePacket(event);
    }
}
