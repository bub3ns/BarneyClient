package moscow.rockstar.api.commands;
import moscow.rockstar.ui.localization.Localization;

import java.util.List;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.ui.notifications.NotificationBridge;
import moscow.rockstar.world.BlockNameResolver;
import moscow.rockstar.world.mining.BlockTargetFinder;
import net.minecraft.block.Block;

public final class MiningCommandService {
    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("mine")
            .description("commands.mine.description")
            .argument("block", argument -> argument.validator(new CommandSuggestionProvider() {
                @Override
                public PluginResolver validate(String value) {
                    return BlockNameResolver.resolveArgument(value);
                }

                @Override
                public List<String> suggestions(String prefix) {
                    return BlockNameResolver.suggestions(prefix);
                }
            }))
            .handler(this::executeMineCommand)
            .build();
    }

    private void executeMineCommand(DispatchContext dispatchContext) {
        Block block = (Block) dispatchContext.getArguments().getFirst();
        ClientServiceRegistry.getInstance().getEventListenerSlot().scheduleScreenState(new BlockTargetFinder(block));
        NotificationBridge.showMessage(Localization.translateFormatted("commands.mine.going", block));
    }
}
