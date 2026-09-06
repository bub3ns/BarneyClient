package moscow.rockstar.api.commands;
import moscow.rockstar.ui.localization.Localization;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.ui.notifications.NotificationBridge;
import moscow.rockstar.world.BlockNameResolver;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.selection.BlockSelectionState;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * ORIGINAL: {@code rockstar/ilIlil/iiIiiiIIi}, the fourth child of {@code newton}
 * ({@code rockstar/ilIlil/iiIiiiiIi#I()V}).
 */
public final class ClearAreaCommandService {
    private static final Set<String> STOP_ALIASES = Set.of(
        "stop", "cancel", "\u0441\u0442\u043e\u043f", "\u043e\u0442\u043c\u0435\u043d\u0430");

    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("cleararea")
            .description("commands.cleararea.description")
            .aliases("excavate", "dig", "\u0440\u0430\u0441\u043a\u043e\u043f")
            .argument("arg", argument -> argument.optional().validator(new CommandSuggestionProvider() {
                @Override
                public PluginResolver validate(String value) {
                    return PluginResolver.resolveValue(value);
                }

                @Override
                public List<String> suggestions(String prefix) {
                    return blockSuggestions(prefix, "stop");
                }
            }))
            .handler(this::executeClearAreaCommand)
            .build();
    }

    private void executeClearAreaCommand(DispatchContext dispatchContext) {
        String argument = (String) dispatchContext.getArguments().get(0);
        if (argument != null && STOP_ALIASES.contains(argument.toLowerCase())) {
            ClientServiceRegistry.getInstance().getEventListenerSlot().cancelPendingScreenState();
            NotificationBridge.showMessage(
                Localization.translate("commands.cleararea.stopped"));
            return;
        }
        BlockSelectionState selection = BlockSelectionState.getInstance();
        if (!selection.hasSelection()) {
            NotificationBridge.showPersistentMessage(
                Localization.translate("commands.cleararea.no_selection"));
            return;
        }
        Block block = null;
        if (argument != null && !argument.isBlank()
            && (block = BlockNameResolver.resolve(argument)) == null) {
            NotificationBridge.showPersistentMessage(
                Localization.translateFormatted("commands.cleararea.block_not_found", argument));
            return;
        }
        BlockPos minimum = SelectionCommandService.minimumPosition(selection);
        BlockPos maximum = SelectionCommandService.maximumPosition(selection);
        ClientServiceRegistry.getInstance().getEventListenerSlot()
            .scheduleScreenState(new ExcavationController(minimum, maximum, block));
        NotificationBridge.showMessage(block != null
            ? Localization.translateFormatted("commands.cleararea.digging_block", String.valueOf(block), SelectionCommandService.format(minimum), SelectionCommandService.format(maximum))
            : Localization.translateFormatted("commands.cleararea.digging_area", SelectionCommandService.format(minimum), SelectionCommandService.format(maximum)));
    }

    /**
     * ORIGINAL: {@code rockstar/ilIlil/iiIiiiIII#I (Ljava/lang/String;[Ljava/lang/String;)Ljava/util/List;}
     * - the varargs overload of the block-name suggester.  The remapped
     * {@code BlockNameResolver.suggestions} dropped both the extra literals and the
     * 50-entry cap, so the original shape is reproduced here.
     */
    static List<String> blockSuggestions(String prefix, String... extras) {
        String lowered = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String extra : extras) {
            if (extra.toLowerCase().startsWith(lowered)) {
                result.add(extra);
            }
        }
        for (Identifier identifier : Registries.BLOCK.getIds()) {
            String value = identifier.getNamespace().equals("minecraft")
                ? identifier.getPath()
                : identifier.toString();
            if (!value.startsWith(lowered)) {
                continue;
            }
            result.add(value);
            if (result.size() >= 50) {
                break;
            }
        }
        return result;
    }
}
