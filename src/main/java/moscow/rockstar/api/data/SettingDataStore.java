/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import moscow.rockstar.api.commands.NavigationCommandService;
import moscow.rockstar.api.settings.SettingEntry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.network.session.BotPacketListener;
import pyrock.events.window.KeyPressEvent;

public class SettingDataStore
implements ClientAccess {
    private final List<SettingEntry> keyBindings = new ArrayList<SettingEntry>();
    private final EventListener<KeyPressEvent> keyPressListener = keyPressEvent -> {
        if (SettingDataStore.minecraftClient.player == null || minecraftClient.getNetworkHandler() == null) {
            return;
        }
        if (SettingDataStore.minecraftClient.currentScreen != null) {
            return;
        }
        if (keyPressEvent.getAction() != 1) {
            return;
        }
        for (SettingEntry settingEntry : this.keyBindings) {
            if (!moscow.rockstar.ui.input.KeyBindingUtil.matches(settingEntry.getKeyCode(), keyPressEvent.getKey())) continue;
            this.executeBinding(settingEntry);
        }
    };

    public SettingDataStore() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public final void registerBinding(String string, int n) {
        this.keyBindings.add(new SettingEntry(n, string));
    }

    public final boolean removeBindingByKey(int n) {
        return this.keyBindings.removeIf(settingEntry -> settingEntry.getKeyCode() == n);
    }

    public final boolean removeBindingByCommand(String string) {
        return this.keyBindings.removeIf(settingEntry -> settingEntry.getCommand().equalsIgnoreCase(string));
    }

    public final boolean removeBinding(String string, int n) {
        return this.keyBindings.removeIf(settingEntry -> settingEntry.getKeyCode() == n && settingEntry.getCommand().equalsIgnoreCase(string));
    }

    public final void clearBindings() {
        this.keyBindings.clear();
    }

    public final void replaceBindings(List<SettingEntry> list) {
        this.keyBindings.clear();
        this.keyBindings.addAll(list);
    }

    public final List<SettingEntry> getBindings() {
        return Collections.unmodifiableList(this.keyBindings);
    }

    private void executeBinding(SettingEntry settingEntry) {
        String string = settingEntry.getCommand();
        if (string == null || string.isBlank() || SettingDataStore.minecraftClient.player == null || minecraftClient.getNetworkHandler() == null) {
            return;
        }
        NavigationCommandService navigationCommandService = RockstarClient.create().getNavigationCommandService();
        if (navigationCommandService != null) {
            String string2 = navigationCommandService.getCommandPrefix();
            if (!string2.isEmpty() && string.startsWith(string2 + string2)) {
                SettingDataStore.minecraftClient.player.networkHandler.sendChatMessage(string.substring(string2.length()));
                return;
            }
            if (!string2.isEmpty() && string.startsWith(string2)) {
                navigationCommandService.executeCommand(string);
                return;
            }
        }
        if (string.startsWith("/")) {
            SettingDataStore.minecraftClient.player.networkHandler.sendChatCommand(string.substring(1));
        } else {
            SettingDataStore.minecraftClient.player.networkHandler.sendChatMessage(string);
        }
    }
}
