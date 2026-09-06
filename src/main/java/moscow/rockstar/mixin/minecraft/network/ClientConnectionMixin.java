/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ClientConnection
 *  net.minecraft.PacketListener
 *  net.minecraft.Packet
 *  net.minecraft.CloseScreenS2CPacket
 *  net.minecraft.Team
 *  net.minecraft.TeamS2CPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.network;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.screens.MinecraftScreenBase;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.network.SendPacketEvent;

@Mixin(value={ClientConnection.class})
public class ClientConnectionMixin
implements ClientAccess {
    @Unique
    private static boolean stackOverflowFix;

    @Inject(method={"handlePacket"}, at={@At(value="HEAD")}, cancellable=true)
    private static <T extends PacketListener> void triggerReceivePacketEvent(Packet<T> class_25962, PacketListener class_25472, CallbackInfo callbackInfo) {
        if (class_25962 instanceof CloseScreenS2CPacket && ClientConnectionMixin.minecraftClient.currentScreen instanceof MinecraftScreenBase) {
            callbackInfo.cancel();
            return;
        }
        ReceivePacketEvent receivePacketEvent = new ReceivePacketEvent(class_25962);
        RockstarClient.create().getEventBus().post(receivePacketEvent);
        if (receivePacketEvent.isCancelled()) {
            callbackInfo.cancel();
        }
        if (class_25962 instanceof TeamS2CPacket) {
            TeamS2CPacket class_59002 = (TeamS2CPacket)class_25962;
            try {
                if (class_59002.getTeamName() != null && class_59002.getTeamName().startsWith("collideRule_") && ClientConnectionMixin.minecraftClient.player != null && ClientConnectionMixin.minecraftClient.player.getScoreboard() != null) {
                    Team EmptyBlockView = ClientConnectionMixin.minecraftClient.player.getScoreboard().getTeam(class_59002.getTeamName());
                    if (class_59002.getPlayerNames() != null && !class_59002.getPlayerNames().isEmpty()) {
                        if (EmptyBlockView == null) {
                            callbackInfo.cancel();
                            return;
                        }
                        for (String string : class_59002.getPlayerNames()) {
                            if (EmptyBlockView.getPlayerList().contains(string)) continue;
                            callbackInfo.cancel();
                            return;
                        }
                    }
                }
            }
            catch (Exception exception) {
                System.err.println("NetworkFix: \u041e\u0442\u043c\u0435\u043d\u0435\u043d \u043f\u0440\u043e\u0431\u043b\u0435\u043c\u043d\u044b\u0439 TeamS2CPacket: " + exception.getMessage());
                callbackInfo.cancel();
            }
        }
    }

    @Inject(method={"send(Lnet/minecraft/network/packet/Packet;)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void triggerSendPacketEvent(Packet<?> class_25962, CallbackInfo callbackInfo) {
        Packet<?> class_25963;
        SendPacketEvent sendPacketEvent = new SendPacketEvent(class_25962);
        if (stackOverflowFix) {
            return;
        }
        RockstarClient.create().getEventBus().post(sendPacketEvent);
        if (sendPacketEvent.isCancelled()) {
            callbackInfo.cancel();
        }
        if ((class_25963 = sendPacketEvent.getPacket()) != class_25962) {
            callbackInfo.cancel();
            stackOverflowFix = true;
            minecraftClient.getNetworkHandler().sendPacket(class_25963);
            stackOverflowFix = false;
        }
    }
}
