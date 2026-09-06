package moscow.rockstar.api.commands;

import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;
import ua.mintantileak.spk.Compile;

/**
 * ORIGINAL: {@code rockstar/ilIlil/IIiiIiIi}, slot 10 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 */
public class PrefixCommandService {
    @Compile
    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("prefix")
            .description("commands.prefix.description")
            .argument("action", argument -> argument
                .optional()
                .choicesAndValidator("list", "clear", "default", "reset", "set", "create")
                .choices("list", "reset", "set"))
            .argument("new", argument -> argument
                .optional()
                .validator(value -> value.length() > 1
                    ? PluginResolver.resolveMessage(Localization.translate("commands.prefix.invalid_length"))
                    : PluginResolver.resolveValue(value)))
            .handler(this::executePrefixCommand)
            .build();
    }

    @Compile
    private void executePrefixCommand(DispatchContext dispatchContext) {
        String action = (String) dispatchContext.getArguments().get(0);
        String replacement = (String) dispatchContext.getArguments().get(1);
        String current = ClientCommandDispatcher.getPrefix();
        if (action == null) {
            Notification.info(Text.of((String) Localization.translateFormatted("commands.prefix.current", current)));
            return;
        }
        switch (action.toLowerCase()) {
            case "list" -> Notification.info(Text.of((String) Localization.translateFormatted("commands.prefix.current", current)));
            case "clear", "default", "reset" -> {
                ClientCommandDispatcher.setPrefix(".");
                Notification.info(Text.of((String) Localization.translate("commands.prefix.reset")));
            }
            case "set", "create" -> {
                if (replacement == null || replacement.isEmpty()) {
                    Notification.error(Text.of((String) Localization.translate("commands.prefix.empty")));
                    return;
                }
                ClientCommandDispatcher.setPrefix(replacement);
                Notification.info(Text.of((String) Localization.translateFormatted("commands.prefix.set", replacement)));
            }
            default -> { }
        }
    }
}
