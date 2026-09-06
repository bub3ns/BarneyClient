/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  lombok.Generated
 *  net.minecraft.CrashReport
 *  net.minecraft.CrashReportSection
 *  net.minecraft.Registry
 *  net.minecraft.NetworkPhase
 *  net.minecraft.PacketListener
 *  net.minecraft.Packet
 *  net.minecraft.CustomPayloadS2CPacket
 *  net.minecraft.DisconnectS2CPacket
 *  net.minecraft.KeepAliveS2CPacket
 *  net.minecraft.ResourcePackSendS2CPacket
 *  net.minecraft.SynchronizeTagsS2CPacket
 *  net.minecraft.KeepAliveC2SPacket
 *  net.minecraft.ResourcePackStatusC2SPacket
 *  net.minecraft.ResourcePackStatusC2SPacket$Status
 *  net.minecraft.DynamicRegistryManager
 *  net.minecraft.DynamicRegistryManager$Immutable
 *  net.minecraft.CommonPingS2CPacket
 *  net.minecraft.CommonPongC2SPacket
 *  net.minecraft.FeaturesS2CPacket
 *  net.minecraft.Registries
 *  net.minecraft.ClientConfigurationPacketListener
 *  net.minecraft.ReadyS2CPacket
 *  net.minecraft.DynamicRegistriesS2CPacket
 *  net.minecraft.ReadyC2SPacket
 *  net.minecraft.ResourcePackRemoveS2CPacket
 *  net.minecraft.CookieRequestS2CPacket
 *  net.minecraft.PlayStateFactories
 *  net.minecraft.RegistryByteBuf
 *  net.minecraft.StoreCookieS2CPacket
 *  net.minecraft.ServerTransferS2CPacket
 *  net.minecraft.SelectKnownPacksC2SPacket
 *  net.minecraft.SelectKnownPacksS2CPacket
 *  net.minecraft.ResetChatS2CPacket
 *  net.minecraft.DisconnectionInfo
 *  net.minecraft.CustomReportDetailsS2CPacket
 *  net.minecraft.ServerLinksS2CPacket
 */
package moscow.rockstar.network.session;

import com.mojang.authlib.GameProfile;
import java.util.Collections;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.network.ConnectionState;
import moscow.rockstar.network.PlayerPacketHandler;
import moscow.rockstar.network.bot.BotController;
import moscow.rockstar.network.session.BotConnection;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.registry.Registry;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.s2c.config.FeaturesS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.network.listener.ClientConfigurationPacketListener;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import net.minecraft.network.packet.s2c.config.DynamicRegistriesS2CPacket;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.network.packet.c2s.config.SelectKnownPacksC2SPacket;
import net.minecraft.network.packet.s2c.config.SelectKnownPacksS2CPacket;
import net.minecraft.network.packet.s2c.config.ResetChatS2CPacket;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.packet.s2c.common.CustomReportDetailsS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerLinksS2CPacket;

public class BotPacketListener
implements ClientConfigurationPacketListener {
    private final BotConnection botConnection;
    private final BotController botController;
    private final GameProfile gameProfile;
    private DynamicRegistryManager.Immutable registryManager;

    public BotPacketListener(BotConnection botConnection, BotController botController, GameProfile gameProfile) {
        this.botConnection = botConnection;
        this.botController = botController;
        this.gameProfile = gameProfile;
    }

    public void onReady(ReadyS2CPacket class_87332) {
        DynamicRegistryManager.Immutable class_68902 = this.registryManager != null ? this.registryManager : (MinecraftClient.getInstance().world != null ? MinecraftClient.getInstance().world.getRegistryManager().toImmutable() : DynamicRegistryManager.of((Registry)Registries.REGISTRIES));
        PlayerPacketHandler playerPacketHandler = new PlayerPacketHandler(this.botConnection, this.botController, this.gameProfile);
        this.botConnection.getConnection().transitionInbound(PlayStateFactories.S2C.bind(RegistryByteBuf.makeFactory((DynamicRegistryManager)class_68902)), playerPacketHandler);
        this.botConnection.getConnection().send((Packet)ReadyC2SPacket.INSTANCE);
        this.botConnection.getConnection().transitionOutbound(PlayStateFactories.C2S.bind(RegistryByteBuf.makeFactory((DynamicRegistryManager)class_68902)));
        this.botController.setPacketHandler(playerPacketHandler);
        this.botConnection.setConnectionState(ConnectionState.PLAYING);
        this.botConnection.markPlaying();
        RockstarClient.LOGGER.info("Bot {} entered play state", (Object)this.botController.getBotName());
    }

    public void onDynamicRegistries(DynamicRegistriesS2CPacket class_87342) {
    }

    public void onFeatures(FeaturesS2CPacket class_78322) {
    }

    public void onSelectKnownPacks(SelectKnownPacksS2CPacket class_92502) {
        this.botConnection.getConnection().send((Packet)new SelectKnownPacksC2SPacket(Collections.emptyList()));
    }

    public void onResetChat(ResetChatS2CPacket class_94482) {
    }

    public void onKeepAlive(KeepAliveS2CPacket class_26702) {
        this.botConnection.getConnection().send((Packet)new KeepAliveC2SPacket(class_26702.getId()));
    }

    public void onPing(CommonPingS2CPacket class_63732) {
        this.botConnection.getConnection().send((Packet)new CommonPongC2SPacket(class_63732.getParameter()));
    }

    public void onCustomPayload(CustomPayloadS2CPacket class_26582) {
    }

    public void onDisconnect(DisconnectS2CPacket class_26612) {
        String string = class_26612.reason().getString();
        this.botConnection.reportDisconnect("Disconnected during configuration: " + string);
        RockstarClient.LOGGER.warn("Bot {} config disconnect: {}", (Object)this.botController.getBotName(), (Object)string);
    }

    public void onResourcePackSend(ResourcePackSendS2CPacket class_27202) {
    }

    public void onResourcePackRemove(ResourcePackRemoveS2CPacket class_90532) {
    }

    public void onSynchronizeTags(SynchronizeTagsS2CPacket class_27902) {
    }

    public void onStoreCookie(StoreCookieS2CPacket class_91502) {
    }

    public void onServerTransfer(ServerTransferS2CPacket class_91512) {
    }

    public void onCustomReportDetails(CustomReportDetailsS2CPacket class_98142) {
    }

    public void onServerLinks(ServerLinksS2CPacket class_98152) {
    }

    public void onCookieRequest(CookieRequestS2CPacket class_90882) {
    }

    public void onDisconnected(DisconnectionInfo class_98122) {
        this.botConnection.reportDisconnect("Disconnected during configuration: " + class_98122.reason().getString());
        this.botConnection.setConnectionState(ConnectionState.DISCONNECTED);
    }

    public boolean isConnectionOpen() {
        return this.botConnection.getConnection() != null && this.botConnection.getConnection().isOpen();
    }

    public NetworkSide getSide() {
        return NetworkSide.CLIENTBOUND;
    }

    public void appendCrashReportDetails(CrashReport DamageTracker, CrashReportSection StatusEffectUtil) {
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

    @Generated
    public DynamicRegistryManager.Immutable getRegistryManager() {
        return this.registryManager;
    }
}
