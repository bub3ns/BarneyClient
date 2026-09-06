/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Packet
 *  net.minecraft.GameMessageS2CPacket
 */
package moscow.rockstar.modules.other.social;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.settings.MultiBooleanSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import pyrock.events.network.ReceivePacketEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Accept", category=ModuleCategory.OTHER, description="modules.descriptions.auto_accept")
public class AutoAccept
extends Module {
    private MultiBooleanSetting requestModeSetting;
    private MultiBooleanSetting.Option acceptAllRequestsOption;
    private MultiBooleanSetting.Option friendsOnlyOption;
    private final EventListener<ReceivePacketEvent> receivePacketListener = receivePacketEvent -> {
        Packet<?> class_25962 = receivePacketEvent.getPacket();
        if (class_25962 instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket class_74392 = (GameMessageS2CPacket)class_25962;
            if (AutoAccept.minecraftClient.player != null && (class_74392.content().getString().contains("\u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f") || class_74392.content().getString().contains("\u0437\u0430\u043f\u0440\u0430\u0448\u0438\u0432\u0430\u0435\u0442 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442")) && !ServerDetector.enabled && this.isTeleportRequestAllowed(class_74392.content().getString())) {
                AutoAccept.minecraftClient.player.networkHandler.sendChatCommand("tpaccept");
            }
        }
    };

    public AutoAccept() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.requestModeSetting = new MultiBooleanSetting(this, "modules.settings.auto_accept.mode");
        this.acceptAllRequestsOption = new MultiBooleanSetting.Option(this.requestModeSetting, "modules.settings.auto_accept.mode.all");
        this.friendsOnlyOption = new MultiBooleanSetting.Option(this.requestModeSetting, "modules.settings.auto_accept.mode.friends_only", this.acceptAllRequestsOption::isSelected).select();
    }

    private boolean isTeleportRequestAllowed(String string) {
        if (this.acceptAllRequestsOption.isSelected()) {
            return true;
        }
        String string2 = string.replaceAll("\u00a7[0-9a-fklmnor]", "").trim();
        String string3 = this.extractRequesterName(string2);
        if (string3 == null) {
            return false;
        }
        if (this.friendsOnlyOption.isSelected()) {
            return RockstarClient.create().getFriendListManager().containsFriend(string3);
        }
        return false;
    }

    private String extractRequesterName(String string) {
        String[] stringArray;
        if (string.contains("\u043f\u0440\u043e\u0441\u0438\u0442 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f") && (stringArray = string.split(" ")).length > 0) {
            return stringArray[0];
        }
        if (string.contains("\u0445\u043e\u0447\u0435\u0442 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f \u043a") && (stringArray = string.split(" ")).length > 1) {
            return stringArray[1];
        }
        if (string.contains("\u0a77 \u043f\u0440\u043e\u0441\u0438\u0442 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f") && (stringArray = string.split(" ")).length >= 2) {
            return stringArray[1];
        }
        if (string.contains("\u279d \u041d\u0438\u043a:") && (stringArray = string.split(":")).length >= 2) {
            return stringArray[1].trim();
        }
        if (string.contains("\u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f")) {
            stringArray = string.split(" ");
            for (int i = 0; i < stringArray.length - 1; ++i) {
                if (!stringArray[i].equals("\u043f\u0440\u043e\u0441\u0438\u0442") && !stringArray[i].equals("\u0437\u0430\u043f\u0440\u0430\u0448\u0438\u0432\u0430\u0435\u0442")) continue;
                return stringArray[i - 1];
            }
        }
        return null;
    }
}
