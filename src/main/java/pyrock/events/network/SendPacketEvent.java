/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Packet
 */
package pyrock.events.network;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import net.minecraft.network.packet.Packet;
import pyrock.events.EventCancellable;

@ScreenController(description="send_packet")
public class SendPacketEvent
extends EventCancellable {
    private Packet<?> packet;

    @Generated
    public Packet<?> getPacket() {
        return this.packet;
    }

    @Generated
    public void setPacket(Packet<?> class_25962) {
        this.packet = class_25962;
    }

    @Generated
    public SendPacketEvent(Packet<?> class_25962) {
        this.packet = class_25962;
    }
}

