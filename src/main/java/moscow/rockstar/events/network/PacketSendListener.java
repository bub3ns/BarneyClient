/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatCommandSignedC2SPacket
 */
package moscow.rockstar.events.network;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.MathUtils;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import pyrock.events.network.SendPacketEvent;

public class PacketSendListener
implements ClientAccess {
    private final EventListener<SendPacketEvent> eventListenerExtra = sendPacketEvent -> {
        Object object = sendPacketEvent.getPacket();
        if (object instanceof ChatCommandSignedC2SPacket) {
            ChatCommandSignedC2SPacket class_94492 = (ChatCommandSignedC2SPacket)object;
            if (PacketSendListener.minecraftClient.player == null) {
                return;
            }
            object = class_94492.command();
            if (((String)object).startsWith("ah me")) {
                PacketSendListener.minecraftClient.player.networkHandler.sendChatMessage("/ah " + PacketSendListener.minecraftClient.player.getName().getString());
                sendPacketEvent.cancel();
            }
            if (((String)object).startsWith("ah sell ")) {
                String string = ((String)object).replaceFirst("ah sell ", "");
                String string2 = MathUtils.parseNumericExpression(string);
                PacketSendListener.minecraftClient.player.networkHandler.sendChatMessage("/ah sell " + Math.round(Float.parseFloat(string2)));
                sendPacketEvent.cancel();
            }
        }
    };

    public PacketSendListener() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }
}
