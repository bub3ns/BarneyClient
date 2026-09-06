/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.suggestion.Suggestion
 *  com.mojang.brigadier.suggestion.Suggestions
 *  net.minecraft.Text
 *  net.minecraft.Packet
 *  net.minecraft.CommandSuggestionsS2CPacket
 *  net.minecraft.RequestCommandCompletionsC2SPacket
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.api.commands;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.util.Timer;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import org.jetbrains.annotations.NotNull;
import pyrock.events.network.ReceivePacketEvent;
import ua.mintantileak.spk.Compile;

public class CommandSuggestionHandler
implements ClientAccess {
    private final Pattern SUGGESTION_TOKEN_PATTERN = Pattern.compile("[A-Z0-9]\\w+");
    private final Timer suggestionRequestTimer = new Timer();
    private boolean awaitingSuggestions;
    private final EventListener<ReceivePacketEvent> suggestionPacketListener = receivePacketEvent -> {
        if (!this.awaitingSuggestions) {
            return;
        }
        if (this.suggestionRequestTimer.hasElapsed(10000L)) {
            Notification.error(Text.of((String)Localization.translate("commands.plugins.error")));
            this.awaitingSuggestions = false;
            return;
        }
        Packet<?> packet = receivePacketEvent.getPacket();
        if (packet instanceof CommandSuggestionsS2CPacket class_26392) {
            Suggestions suggestions = class_26392.getSuggestions();
            this.awaitingSuggestions = false;
            if (suggestions.getList().isEmpty()) {
                Notification.error(Text.of((String)Localization.translate("commands.plugins.empty")));
                return;
            }
            Set<String> set = this.extractPluginIds(suggestions);
            if (set.isEmpty()) {
                Notification.error(Text.of((String)Localization.translate("commands.plugins.client_error")));
                return;
            }
            Notification.info(Text.of((String)(Localization.translate("commands.plugins.counts") + " " + set.size() + " " + Localization.translate("commands.plugins.find"))));
            Notification.info(Text.of((String)String.join((CharSequence)", ", set)));
        }
    };

    public CommandSuggestionHandler() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("plugins")
            .aliases("plugin", "pl")
            .description("commands.plugins.description")
            .argument("command", argumentBuilder -> argumentBuilder.optional())
            .handler(this::requestPluginSuggestions)
            .build();
    }

    @Compile
    private void requestPluginSuggestions(DispatchContext dispatchContext) {
        String string = "";
        if (!dispatchContext.getArguments().isEmpty()) {
            string = (String)dispatchContext.getArguments().getFirst();
        }
        Object object = "/";
        if (string != null && !string.isEmpty()) {
            object = (String)object + string;
        }
        this.suggestionRequestTimer.reset();
        this.awaitingSuggestions = true;
        minecraftClient.getNetworkHandler().sendPacket((Packet)new RequestCommandCompletionsC2SPacket(0, (String)object));
    }

    @NotNull
    private Set<String> extractPluginIds(Suggestions suggestions) {
        HashSet<String> hashSet = new HashSet<String>();
        for (Suggestion suggestion : suggestions.getList()) {
            String string = suggestion.getText();
            if (string.contains(":")) {
                String[] stringArray = string.split(":");
                String string2 = stringArray[0].replaceAll("\\s*", "").replace("/", "");
                if (string2.isEmpty() || string2.equals("minecraft")) continue;
                hashSet.add(string2);
                continue;
            }
            if (!string.matches(this.SUGGESTION_TOKEN_PATTERN.pattern()) || string.startsWith("/")) continue;
            hashSet.add(string);
        }
        return hashSet;
    }
}
