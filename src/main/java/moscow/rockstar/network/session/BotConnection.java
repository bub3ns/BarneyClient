/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ClientConnection
 *  net.minecraft.Text
 *  net.minecraft.Packet
 *  net.minecraft.LoginHelloC2SPacket
 *  net.minecraft.ClientPacketListener
 *  net.minecraft.LoginStates
 *  net.minecraft.MultiValueDebugSampleLogImpl
 */
package moscow.rockstar.network.session;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.network.ConnectionState;
import moscow.rockstar.network.bot.BotController;
import moscow.rockstar.network.session.BotLoginPacketListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.state.LoginStates;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;

public class BotConnection {
    private final BotController clientProfile;
    private ClientConnection connection;
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private Consumer<String> errorCallback;
    private Consumer<Void> connectedCallback;
    private Consumer<String> disconnectedCallback;
    private long connectStartedAtMillis = 0L;
    private long lastActivityAtMillis = 0L;

    public BotConnection(BotController botController) {
        this.clientProfile = botController;
    }

    public void connect(String string, int n) {
        if (this.connectionState != ConnectionState.DISCONNECTED) {
            return;
        }
        this.connectionState = ConnectionState.CONNECTING;
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(string, n);
            this.connection = ClientConnection.connect((InetSocketAddress)inetSocketAddress, (boolean)false, (MultiValueDebugSampleLogImpl)null);
            BotLoginPacketListener botLoginPacketListener = new BotLoginPacketListener(this, this.clientProfile);
            this.connection.connect(string, n, LoginStates.C2S, LoginStates.S2C, botLoginPacketListener, false);
            UUID uUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.clientProfile.getBotName()).getBytes());
            this.connection.send((Packet)new LoginHelloC2SPacket(this.clientProfile.getBotName(), uUID));
            this.connectionState = ConnectionState.AUTHENTICATING;
        }
        catch (Exception exception) {
            this.connectionState = ConnectionState.DISCONNECTED;
            if (this.errorCallback != null) {
                this.errorCallback.accept("Connection failed: " + exception.getMessage());
            }
            RockstarClient.LOGGER.error("Bot {} failed to connect: {}", (Object)this.clientProfile.getBotName(), (Object)exception.getMessage());
        }
    }

    public void disconnectGracefully() {
        if (this.connection != null && this.connection.isOpen()) {
            this.connectionState = ConnectionState.CLOSING;
            this.connection.disconnect((Text)Text.literal((String)"Bot disconnected"));
        }
        this.handleDisconnect("Disconnected");
    }

    public void sendPacket(Packet<?> class_25962) {
        if (this.connection != null && this.connection.isOpen() && this.connectionState.isConnectionActive()) {
            this.connection.send(class_25962);
        }
    }

    public boolean isPlaying() {
        return this.connection != null && this.connection.isOpen() && this.connectionState == ConnectionState.PLAYING;
    }

    public void tickConnection() {
        if (this.connection != null) {
            this.connection.tick();
            if (!this.connection.isOpen() && this.connectionState != ConnectionState.DISCONNECTED) {
                this.handleDisconnect("Connection lost");
            }
        }
    }

    public void setCompressionThreshold(int n) {
        if (this.connection != null && !this.connection.isLocal()) {
            this.connection.setCompressionThreshold(n, false);
        }
    }

    public void setErrorCallback(Consumer<String> consumer) {
        this.errorCallback = consumer;
    }

    public void setConnectedCallback(Consumer<Void> consumer) {
        this.connectedCallback = consumer;
    }

    public void setDisconnectedCallback(Consumer<String> consumer) {
        this.disconnectedCallback = consumer;
    }

    public void markPlaying() {
        this.connectionState = ConnectionState.PLAYING;
        this.clientProfile.getWorldState().start();
        if (this.connectedCallback != null) {
            this.connectedCallback.accept(null);
        }
    }

    public void reportDisconnect(String string) {
        if (this.errorCallback != null) {
            this.errorCallback.accept(string);
        }
    }

    public void handleDisconnect(String string) {
        if (this.connectionState == ConnectionState.DISCONNECTED) {
            return;
        }
        this.connectionState = ConnectionState.DISCONNECTED;
        this.clientProfile.getWorldState().stop();
        if (this.disconnectedCallback != null) {
            this.disconnectedCallback.accept(string);
        }
    }

    @Generated
    public BotController getClientProfile() {
        return this.clientProfile;
    }

    @Generated
    public ClientConnection getConnection() {
        return this.connection;
    }

    @Generated
    public ConnectionState getConnectionState() {
        return this.connectionState;
    }

    @Generated
    public Consumer<String> getErrorCallback() {
        return this.errorCallback;
    }

    @Generated
    public Consumer<Void> getConnectedCallback() {
        return this.connectedCallback;
    }

    @Generated
    public Consumer<String> getDisconnectedCallback() {
        return this.disconnectedCallback;
    }

    @Generated
    public long getConnectStartedAtMillis() {
        return this.connectStartedAtMillis;
    }

    @Generated
    public long getLastActivityAtMillis() {
        return this.lastActivityAtMillis;
    }

    @Generated
    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }
}
