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

@ScreenController(description="receive_packet")
public class ReceivePacketEvent
extends EventCancellable {
    private final Packet<?> packet;

    @Generated
    public Packet<?> getPacket() {
        return this.packet;
    }

    @Generated
    public ReceivePacketEvent(Packet<?> class_25962) {
        this.packet = class_25962;
    }
}

