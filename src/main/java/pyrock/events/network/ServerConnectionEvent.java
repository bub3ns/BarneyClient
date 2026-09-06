/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ServerAddress
 *  net.minecraft.ServerInfo
 *  net.minecraft.CookieStorage
 */
package pyrock.events.network;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.CookieStorage;

@ScreenController(description="connect")
public class ServerConnectionEvent
extends Event {
    private final ServerAddress address;
    private final ServerInfo info;
    private final CookieStorage cookieStorage;

    @Generated
    public ServerAddress getAddress() {
        return this.address;
    }

    @Generated
    public ServerInfo getInfo() {
        return this.info;
    }

    @Generated
    public CookieStorage getCookieStorage() {
        return this.cookieStorage;
    }

    @Generated
    public ServerConnectionEvent(ServerAddress BeaconButtonWidget, ServerInfo class_6422, CookieStorage class_91122) {
        this.address = BeaconButtonWidget;
        this.info = class_6422;
        this.cookieStorage = class_91122;
    }
}

