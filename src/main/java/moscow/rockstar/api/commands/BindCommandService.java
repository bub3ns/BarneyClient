package moscow.rockstar.api.commands;

import java.util.List;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.ui.input.KeyDisplayFormatter;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;
import ua.mintantileak.spk.Compile;

/**
 * ORIGINAL: {@code rockstar/ilIlil/IIiIIiiI}, slot 20 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 */
public class BindCommandService {
    public ServiceRegistry getCommandRegistration() {
        List<String> moduleNames = RockstarClient.create().getModuleRegistry().getModules().stream()
            .map(module -> module.getName().replace(" ", ""))
            .toList();
        List<String> keyNames = KeyDisplayFormatter.keyNames();
        return CommandBuilder.command("bind")
            .aliases("binds", "\u0431\u0438\u043d\u0434")
            .description("commands.bind.description")
            .argument("action", argument -> argument
                .choicesAndValidator("add", "create", "remove", "delete", "list", "clear")
                .choices("add", "remove", "list", "clear"))
            .argument("module", argument -> argument
                .optional()
                .validator(CommandArgumentBuilder.MODULE)
                .choices(moduleNames))
            .argument("key", argument -> argument
                .optional()
                .validator(value -> value.isBlank()
                    ? PluginResolver.resolveMessage("key is empty")
                    : PluginResolver.resolveValue(value))
                .choices(keyNames))
            .handler(this::executeBindCommand)
            .build();
    }

    @Compile
    private void executeBindCommand(DispatchContext dispatchContext) {
        String action = (String) dispatchContext.getArguments().getFirst();
        ModuleContract module = (ModuleContract) dispatchContext.getArguments().get(1);
        String key = (String) dispatchContext.getArguments().get(2);
        if (action.equalsIgnoreCase("clear")) {
            for (ModuleContract candidate : RockstarClient.create().getModuleRegistry().getModules()) {
                if (candidate instanceof Menu || candidate.getKeyBind() == -1) {
                    continue;
                }
                candidate.setKeyBind(-1);
            }
            Notification.info(Text.of((String) Localization.translate("commands.bind.clear")));
        }
        if (action.equalsIgnoreCase("list")) {
            this.openBindsScreen();
            return;
        }
        if (action.equalsIgnoreCase("add") || action.equalsIgnoreCase("create")) {
            if (key == null) {
                Notification.error(Text.of((String) Localization.translate("commands.bind.create.key_not")));
                return;
            }
            int keyCode = KeyDisplayFormatter.parseKey(key);
            if (keyCode == -1) {
                Notification.error(Text.of(Localization.translate("commands.bind.create.key_unknow") + key));
                return;
            }
            module.setKeyBind(keyCode);
            Notification.info(Text.of(Localization.translate("commands.bind.create.download")
                + " " + KeyDisplayFormatter.formatKey(keyCode)));
        } else if (action.equalsIgnoreCase("remove") || action.equalsIgnoreCase("delete")) {
            if (module == null) {
                Notification.error(Text.of((String) Localization.translate("commands.bind.module_required")));
                return;
            }
            module.setKeyBind(-1);
            Notification.info(Text.of(Localization.translate("commands.bind.delete") + " " + module.getName()));
        }
    }

    /**
     * OMITTED: the original schedules {@code MinecraftClient.setScreen(new IiiIiIIii())} here -
     * the dedicated key-bind list screen ({@code rockstar/ilIlil/IiiIiIIii}, a subclass of the
     * client screen base).  That screen has no remapped counterpart anywhere in the tree, and
     * writing one would be inventing UI the original does differently, so this branch is left
     * empty until the screen itself is ported.
     */
    private void openBindsScreen() {
    }
}
