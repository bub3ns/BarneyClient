package moscow.rockstar.api.commands;

import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.FriendManager;
import moscow.rockstar.core.RockstarClient;
import ua.mintantileak.spk.Compile;

public class TargetCommandService {
    @Compile
    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("target")
            .aliases("targets")
            .description("commands.target.description")
            .argument("action", argument -> argument
                .choicesAndValidator("add", "remove", "del", "delete", "clear", "list")
                .choices("add", "remove", "clear", "list"))
            .argument("id", argument -> argument.optional().validator(PluginResolver::resolveValue))
            .handler(this::executeTargetCommand)
            .build();
    }

    @Compile
    private void executeTargetCommand(DispatchContext dispatchContext) {
        String action = (String) dispatchContext.getArguments().get(0);
        String id = dispatchContext.getArguments().size() > 1
            ? (String) dispatchContext.getArguments().get(1)
            : null;
        FriendManager friends = RockstarClient.create().getFriendManager();
        if (action == null) {
            return;
        }
        switch (action.toLowerCase()) {
            case "add" -> {
                if (id != null) friends.addFriend(id);
            }
            case "remove", "del", "delete" -> {
                if (id != null) friends.removeFriend(id);
            }
            case "clear" -> friends.clearFriends();
            case "list" -> friends.listFriends();
            default -> { }
        }
    }
}
