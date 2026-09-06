package moscow.rockstar.api.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;
import ua.mintantileak.spk.Compile;

/**
 * ORIGINAL: {@code rockstar/ilIlil/IIiIiiIi}, slot 6 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 */
public class HelpCommandService {
    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("help")
            .aliases("\u043f\u043e\u043c\u043e\u0449\u044c", "\u043a\u043e\u043c\u0430\u043d\u0434\u044b", "commands", "helpme")
            .description("commands.help.description")
            .handler(this::executeHelpCommand)
            .build();
    }

    @Compile
    private void executeHelpCommand(DispatchContext dispatchContext) {
        List<ServiceRegistry> commands = new ArrayList<>(ClientCommandDispatcher.getRegisteredCommands());
        commands.sort(Comparator.comparing(
            (ServiceRegistry command) -> command.getRegisteredServices().getFirst(),
            String.CASE_INSENSITIVE_ORDER));
        List<String> lines = new ArrayList<>();
        int index = 1;
        for (ServiceRegistry command : commands) {
            if (!ClientCommandDispatcher.isAccessible(command)) {
                continue;
            }
            lines.add(String.format(
                "%d) %s%s - %s",
                index++,
                ClientCommandDispatcher.getPrefix(),
                command.getRegisteredServices().getFirst(),
                Localization.translate(command.getRegistryName())));
        }
        Notification.info(Text.of((String) Localization.translateFormatted(
            "commands.help.list", String.join("\n", lines))));
    }
}
