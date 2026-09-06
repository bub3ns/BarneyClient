/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  lombok.Generated
 *  net.minecraft.CrashReport
 *  net.minecraft.CrashReportSection
 *  net.minecraft.NetworkPhase
 *  net.minecraft.PacketListener
 *  net.minecraft.Packet
 *  net.minecraft.ClientLoginPacketListener
 *  net.minecraft.LoginQueryRequestS2CPacket
 *  net.minecraft.LoginSuccessS2CPacket
 *  net.minecraft.LoginHelloS2CPacket
 *  net.minecraft.LoginCompressionS2CPacket
 *  net.minecraft.LoginDisconnectS2CPacket
 *  net.minecraft.LoginQueryResponseC2SPacket
 *  net.minecraft.EnterConfigurationC2SPacket
 *  net.minecraft.CookieRequestS2CPacket
 *  net.minecraft.ConfigurationStates
 *  net.minecraft.DisconnectionInfo
 */
package moscow.rockstar.network.session;

import com.mojang.authlib.GameProfile;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.network.ConnectionState;
import moscow.rockstar.network.bot.BotController;
import moscow.rockstar.network.session.BotConnection;
import moscow.rockstar.network.session.BotPacketListener;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.c2s.login.EnterConfigurationC2SPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.network.DisconnectionInfo;

public class BotLoginPacketListener
implements ClientLoginPacketListener {
    private final BotConnection botConnection;
    private final BotController botController;
    private GameProfile gameProfile;

    public BotLoginPacketListener(BotConnection botConnection, BotController botController) {
        this.botConnection = botConnection;
        this.botController = botController;
    }

    public void onHello(LoginHelloS2CPacket class_29052) {
        this.botConnection.reportDisconnect("Server requires online mode authentication");
        this.botConnection.disconnectGracefully();
    }

    public void onSuccess(LoginSuccessS2CPacket class_29012) {
        this.gameProfile = class_29012.profile();
        BotPacketListener botPacketListener = new BotPacketListener(this.botConnection, this.botController, this.gameProfile);
        this.botConnection.getConnection().transitionInbound(ConfigurationStates.S2C, botPacketListener);
        this.botConnection.getConnection().send((Packet)EnterConfigurationC2SPacket.INSTANCE);
        this.botConnection.getConnection().transitionOutbound(ConfigurationStates.C2S);
        this.botConnection.setConnectionState(ConnectionState.CONFIGURING);
        RockstarClient.LOGGER.info("Bot {} logged in successfully", (Object)this.botController.getBotName());
    }

    public void onDisconnect(LoginDisconnectS2CPacket class_29092) {
        String string = class_29092.getReason().getString();
        this.botConnection.reportDisconnect("Login failed: " + string);
        RockstarClient.LOGGER.warn("Bot {} login disconnect: {}", (Object)this.botController.getBotName(), (Object)string);
    }

    public void onCompression(LoginCompressionS2CPacket class_29072) {
        this.botConnection.setCompressionThreshold(class_29072.getCompressionThreshold());
    }

    public void onQueryRequest(LoginQueryRequestS2CPacket class_28992) {
        this.botConnection.getConnection().send((Packet)new LoginQueryResponseC2SPacket(class_28992.queryId(), null));
    }

    public void onCookieRequest(CookieRequestS2CPacket class_90882) {
    }

    public void onDisconnected(DisconnectionInfo class_98122) {
        this.botConnection.reportDisconnect("Disconnected during login: " + class_98122.reason().getString());
        this.botConnection.setConnectionState(ConnectionState.DISCONNECTED);
    }

    public boolean isConnectionOpen() {
        return this.botConnection.getConnection() != null && this.botConnection.getConnection().isOpen();
    }

    public NetworkPhase getPhase() {
        return NetworkPhase.LOGIN;
    }

    public NetworkSide getSide() {
        return NetworkSide.CLIENTBOUND;
    }

    public void fillCrashReport(CrashReport DamageTracker, CrashReportSection StatusEffectUtil) {
        StatusEffectUtil.add("Bot", (Object)this.botController.getBotName());
        StatusEffectUtil.add("Connection State", (Object)this.botConnection.getConnectionState().toString());
    }

    @Generated
    public BotConnection getBotConnection() {
        return this.botConnection;
    }

    @Generated
    public BotController getBotController() {
        return this.botController;
    }

    @Generated
    public GameProfile getGameProfile() {
        return this.gameProfile;
    }
}
