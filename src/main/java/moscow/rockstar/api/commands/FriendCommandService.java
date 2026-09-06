package moscow.rockstar.api.commands;

import java.util.List;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.social.FriendListManager;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;
import ua.mintantileak.spk.Compile;

/**
 * ORIGINAL: {@code rockstar/ilIlil/IIiIiiII}, slot 5 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 */
public class FriendCommandService {
    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("friend")
            .aliases("friends")
            .description("commands.friends.description")
            .argument("action", argument -> argument
                .choicesAndValidator("add", "remove", "del", "delete", "clear", "list")
                .choices("add", "remove", "clear", "list"))
            .argument("id", argument -> argument.optional().validator(PluginResolver::resolveValue))
            .handler(this::executeFriendCommand)
            .build();
    }

    @Compile
    private void executeFriendCommand(DispatchContext dispatchContext) {
        String action = (String) dispatchContext.getArguments().get(0);
        String id = (String) dispatchContext.getArguments().get(1);
        FriendListManager friends = RockstarClient.create().getFriendListManager();
        switch (action.toLowerCase()) {
            case "add" -> friends.addFriend(id);
            case "remove", "del", "delete" -> friends.removeFriend(id);
            case "clear" -> friends.clearFriends();
            case "list" -> this.listFriends();
            default -> { }
        }
    }

    @Compile
    private void listFriends() {
        List<String> friends = RockstarClient.create().getFriendListManager().getFriends();
        if (friends.isEmpty()) {
            Notification.info(Text.of((String) Localization.translate("commands.friends.empty")));
            return;
        }
        Notification.info(Text.of((String) Localization.translate("commands.friends.list")));
        for (int index = 0; index < friends.size(); ++index) {
            Notification.info(Text.of((String) Localization.translateFormatted(
                "commands.friends.list_item", index + 1, friends.get(index))));
        }
    }
}
