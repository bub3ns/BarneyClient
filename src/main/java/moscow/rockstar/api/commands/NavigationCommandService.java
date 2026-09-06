package moscow.rockstar.api.commands;
import moscow.rockstar.ui.localization.Localization;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import com.mojang.brigadier.suggestion.Suggestions;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.combat.rotation.RotationEngine;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.entity.CollisionProbe;
import moscow.rockstar.ui.notifications.NotificationBridge;
import moscow.rockstar.world.BlockCollisionProbe;
import moscow.rockstar.world.BlockOffset;
import moscow.rockstar.world.navigation.PathNavigator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public final class NavigationCommandService {
    private static final Set<String> ELYTRA_ALIASES = Set.of(
        "elytra", "fly", "элитра", "элитру", "полёт", "полет");
    private static final Object ELYTRA_SENTINEL = new Object();

    public String getCommandPrefix() {
        return ClientCommandDispatcher.getPrefix();
    }

    public boolean executeCommand(String commandText) {
        return ClientCommandDispatcher.executeCommand(commandText);
    }

    public CompletableFuture<Suggestions> suggestCommands(String input, int cursor) {
        return ClientCommandDispatcher.suggestCommands(input, cursor);
    }

    public void registerCommand(ServiceRegistry command) {
        ClientCommandDispatcher.register(command);
    }

    public boolean unregisterCommand(ServiceRegistry command) {
        return ClientCommandDispatcher.unregister(command);
    }

    public boolean isCommandRegistered(String name) {
        return ClientCommandDispatcher.isRegisteredName(name);
    }

    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("goto")
            .description("commands.goto.description")
            .argument("coords", argument -> argument
                .vararg()
                .validator(NavigationCommandService::parseCoordinate))
            .handler(this::executeGotoCommand)
            .build();
    }

    private static PluginResolver parseCoordinate(String value) {
        if (value != null && ELYTRA_ALIASES.contains(value.toLowerCase(Locale.ROOT))) {
            return PluginResolver.resolveValue(ELYTRA_SENTINEL);
        }
        try {
            return PluginResolver.resolveValue(Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return PluginResolver.resolveMessage(Localization.translateFormatted("commands.goto.not_a_number", value));
        }
    }

    private void executeGotoCommand(DispatchContext dispatchContext) {
        List<?> values = (List<?>) dispatchContext.getArguments().getFirst();
        if (values == null || values.isEmpty()) {
            showUsage();
            return;
        }
        boolean elytra = false;
        ArrayList<Integer> coordinates = new ArrayList<>();
        for (Object value : values) {
            if (value == ELYTRA_SENTINEL) {
                elytra = true;
            } else if (value instanceof Number number) {
                coordinates.add(number.intValue());
            }
        }
        if (coordinates.size() != 2 && coordinates.size() != 3) {
            showUsage();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            NotificationBridge.showPersistentMessage(Localization.translate("commands.goto.no_world"));
            return;
        }
        if (elytra) {
            int x = coordinates.get(0);
            int z = coordinates.get(coordinates.size() - 1);
            int y = coordinates.size() == 3 ? coordinates.get(1) : (int) Math.round(client.player.getY());
            ClientServiceRegistry.getInstance().getEventListenerSlot()
                .scheduleScreenState(new RotationEngine(x, y, z, coordinates.size() == 3));
            NotificationBridge.showMessage(Localization.translateFormatted("commands.goto.elytra_to", x,
                (Object) (coordinates.size() == 3 ? Integer.valueOf(y) : "?"), z));
            return;
        }

        CollisionProbe probe = coordinates.size() == 2
            ? new BlockOffset(coordinates.get(0), coordinates.get(1))
            : new BlockCollisionProbe(new BlockPos(coordinates.get(0), coordinates.get(1), coordinates.get(2)));
        ClientServiceRegistry.getInstance().getEventListenerSlot()
            .scheduleScreenState(new PathNavigator(probe));
        NotificationBridge.showMessage(coordinates.size() == 2
            ? Localization.translateFormatted("commands.goto.walking_to", coordinates.get(0), "?", coordinates.get(1))
            : Localization.translateFormatted("commands.goto.walking_to", coordinates.get(0), coordinates.get(1), coordinates.get(2)));
    }

    private void showUsage() {
        NotificationBridge.showPersistentMessage(Localization.translate("commands.goto.usage"));
    }
}
