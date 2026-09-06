/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GameMessageS2CPacket
 */
package moscow.rockstar.modules.other.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.StringSetting;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import pyrock.events.network.ReceivePacketEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Auth", category=ModuleCategory.OTHER, description="modules.descriptions.auto_auth")
public class AutoAuth
extends Module {
    private BooleanSetting randomPasswordSetting;
    private StringSetting passwordSetting;
    private final Map<String, String> savedPasswords = new HashMap<String, String>();
    private final EventListener<ReceivePacketEvent> receivePacketListener = receivePacketEvent -> {
        Object object = receivePacketEvent.getPacket();
        if (object instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket class_74392 = (GameMessageS2CPacket)object;
            if (AutoAuth.minecraftClient.player != null) {
                object = class_74392.content().getString().toLowerCase();
                String string = Integer.toString(ThreadLocalRandom.current().nextInt(100000, 1000000));
                String string2 = this.randomPasswordSetting.isEnabled() ? string : this.passwordSetting.getValue();
                this.savedPasswords.put(AutoAuth.minecraftClient.player.getDisplayName().getString(), " " + string);
                if (((String)object).contains("\u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u0443\u0439\u0442\u0435\u0441\u044c") || ((String)object).contains("/reg")) {
                    AutoAuth.minecraftClient.player.networkHandler.sendChatCommand(String.format("reg %s %s", string2, string2));
                } else if (((String)object).contains("\u0430\u0432\u0442\u043e\u0440\u0438\u0437\u0443\u0439\u0442\u0435\u0441\u044c") || ((String)object).contains("/login") || ((String)object).contains("/l") && ((String)object).matches("/l(\\s|$)")) {
                    AutoAuth.minecraftClient.player.networkHandler.sendChatCommand(String.format("l %s", string2));
                }
            }
        }
    };

    public AutoAuth() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.randomPasswordSetting = new BooleanSetting(this, "modules.settings.auto_auth.random");
        this.passwordSetting = new StringSetting((SettingOwner)this, "modules.settings.auto_auth.password", this.randomPasswordSetting::isEnabled).setValue("123123");
    }

    public Map<String, String> getSavedPasswords() {
        return Collections.unmodifiableMap(this.savedPasswords);
    }

    public void storePassword(String string, String string2) {
        this.savedPasswords.put(string, string2);
    }
}
