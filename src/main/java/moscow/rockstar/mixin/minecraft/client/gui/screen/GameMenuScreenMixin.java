/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.Difficulty
 *  net.minecraft.Text
 *  net.minecraft.ConnectScreen
 *  net.minecraft.ButtonWidget
 *  net.minecraft.RealmsMainScreen
 *  net.minecraft.GameMenuScreen
 *  net.minecraft.Screen
 *  net.minecraft.TitleScreen
 *  net.minecraft.MultiplayerScreen
 *  net.minecraft.ServerAddress
 *  net.minecraft.ServerInfo
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import javax.annotation.Nullable;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.mixin.accessors.ScreenAccessor;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.ui.localization.Localization;
import net.minecraft.world.Difficulty;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GameMenuScreen.class})
public class GameMenuScreenMixin
extends Screen
implements ClientAccess {
    @Shadow
    @Nullable
    private ButtonWidget exitButton;

    protected GameMenuScreenMixin(Text class_25612) {
        super(class_25612);
    }

    @Inject(method={"initWidgets"}, at={@At(value="TAIL")})
    private void reconnectButton(CallbackInfo callbackInfo) {
        if (RockstarClient.INSTANCE.isPanicMode()) {
            return;
        }
        if (minecraftClient.isInSingleplayer()) {
            return;
        }
        if (this.exitButton == null) {
            return;
        }
        Text class_25612 = Text.of((String)Localization.translate("inventory.button.reconnect"));
        int n = 204;
        int n2 = this.exitButton.getX() + this.exitButton.getWidth() / 2 - n / 2;
        int n3 = this.exitButton.getY() + this.exitButton.getHeight() + (ServerDetector.serverAddressContains("aresmine") ? 44 : 4);
        ButtonWidget class_41853 = ButtonWidget.builder((Text)class_25612, class_41852 -> this.reconnect()).dimensions(n2, n3, n, 20).build();
        ((ScreenAccessor)((Object)this)).invokeAddDrawableChild(class_41853);
    }

    @Unique
    private void reconnect() {
        ServerInfo class_6422;
        if (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) && GameMenuScreenMixin.minecraftClient.world.getDifficulty() == Difficulty.HARD) {
            try {
                GameMenuScreenMixin.minecraftClient.player.networkHandler.sendChatCommand(".rct");
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if ((class_6422 = minecraftClient.getCurrentServerEntry()) == null) {
            return;
        }
        ServerAddress BeaconButtonWidget = ServerAddress.parse((String)class_6422.address);
        minecraftClient.getAbuseReportContext().tryShowDraftScreen(minecraftClient, (Screen)this, this::disconnect, true);
        new Thread(() -> {
            try {
                Thread.sleep(1200L);
            }
            catch (Exception exception) {
                // empty catch block
            }
            minecraftClient.execute(() -> ConnectScreen.connect((Screen)new MultiplayerScreen((Screen)new TitleScreen()), (MinecraftClient)minecraftClient, (ServerAddress)BeaconButtonWidget, (ServerInfo)class_6422, (boolean)false, null));
        }).start();
    }

    @Unique
    private void disconnect() {
        ServerInfo class_6422 = minecraftClient.getCurrentServerEntry();
        GameMenuScreenMixin.minecraftClient.world.disconnect();
        minecraftClient.disconnect();
        TitleScreen RealmsSettingsScreen = new TitleScreen();
        if (class_6422 != null && class_6422.isRealm()) {
            minecraftClient.setScreen((Screen)new RealmsMainScreen((Screen)RealmsSettingsScreen));
        } else {
            minecraftClient.setScreen((Screen)new MultiplayerScreen((Screen)RealmsSettingsScreen));
        }
    }
}
