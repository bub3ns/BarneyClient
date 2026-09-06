/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ConnectScreen
 *  net.minecraft.ServerAddress
 *  net.minecraft.ServerInfo
 *  net.minecraft.CookieStorage
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen.multiplayer;

import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.CookieStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.events.network.ServerConnectionEvent;

@Mixin(value={ConnectScreen.class})
public class ConnectScreenMixin {
    @Inject(method={"connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;Lnet/minecraft/client/network/CookieStorage;)V"}, at={@At(value="HEAD")})
    private void onNewConnection(MinecraftClient client, ServerAddress BeaconButtonWidget, ServerInfo class_6422, CookieStorage class_91122, CallbackInfo callbackInfo) {
        RockstarClient.create().getEventBus().post(new ServerConnectionEvent(BeaconButtonWidget, class_6422, class_91122));
    }
}

