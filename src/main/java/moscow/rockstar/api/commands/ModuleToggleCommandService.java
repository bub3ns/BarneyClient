package moscow.rockstar.api.commands;

import java.util.List;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;
import ua.mintantileak.spk.Compile;

public class ModuleToggleCommandService {
    @Compile
    public ServiceRegistry getCommandRegistration() {
        List<String> moduleNames = RockstarClient.create().getModuleRegistry().getModules().stream()
            .map(module -> module.getName().replace(" ", ""))
            .toList();
        return CommandBuilder.command("toggle")
            .aliases("t")
            .description("commands.toggle.description")
            .argument("module", argument -> argument
                .choices(moduleNames)
                .validator(ModuleToggleCommandService::resolveModule))
            .handler(this::toggleModule)
            .build();
    }

    private static PluginResolver resolveModule(String name) {
        String normalized = name == null ? "" : name.replace(" ", "");
        for (ModuleContract module : RockstarClient.create().getModuleRegistry().getModules()) {
            if (module.getName().replace(" ", "").equalsIgnoreCase(normalized)) {
                return PluginResolver.resolveValue(module);
            }
        }
        return PluginResolver.resolveMessage(Localization.translateFormatted("commands.toggle.not_found", name));
    }

    private void toggleModule(DispatchContext dispatchContext) {
        ModuleContract module = (ModuleContract) dispatchContext.getArguments().getFirst();
        module.toggle();
        String state = module.isEnabled() ? "enabled" : "disabled";
        Notification.info(Text.of(Localization.translateFormatted("commands.toggle." + state, module.getName())));
    }
}
