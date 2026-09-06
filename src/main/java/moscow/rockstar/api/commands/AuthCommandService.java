package moscow.rockstar.api.commands;

import java.util.Map;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.other.auth.AutoAuth;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;
import ua.mintantileak.spk.Compile;

/**
 * ORIGINAL: {@code rockstar/ilIlil/IIiIIIii}, slot 15 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 */
public class AuthCommandService {
    @Compile
    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("auth")
            .aliases("autoAuth", "\u043f\u0430\u0440\u043e\u043b\u0438", "passwords")
            .description("commands.auth.description")
            .handler(this::executeAuthCommand)
            .build();
    }

    @Compile
    private void executeAuthCommand(DispatchContext dispatchContext) {
        Map<String, String> passwords = RockstarClient.create()
            .getModuleRegistry().getModule(AutoAuth.class).getSavedPasswords();
        int index = 1;
        if (passwords.isEmpty()) {
            Notification.error(Text.of((String) Localization.translate("commands.auth.empty")));
            return;
        }
        Notification.info(Text.of((String) Localization.translate("commands.auth.passwords")));
        for (Map.Entry<String, String> entry : passwords.entrySet()) {
            String nickname = entry.getKey();
            String password = entry.getValue();
            Notification.info(Text.of(index++ + ") " + Localization.translate("commands.auth.nick")
                + " " + nickname + " | " + Localization.translate("commands.auth.password") + password));
        }
    }
}
