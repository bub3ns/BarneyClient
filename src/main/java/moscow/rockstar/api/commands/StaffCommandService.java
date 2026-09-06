package moscow.rockstar.api.commands;

import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import moscow.rockstar.api.data.ClientConfigManager;
import moscow.rockstar.api.data.ConfigEntry;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.server.staff.StaffListManager;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import ua.mintantileak.spk.Compile;

/**
 * ORIGINAL: {@code rockstar/ilIlil/IIiiiIII}, slot 25 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 */
public class StaffCommandService {
    @Compile
    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("staff")
            .description("commands.staff.description")
            .argument("action", argument -> argument
                .choicesAndValidator("add", "remove", "del", "dell", "delete", "clear", "list", "dir")
                .choices("add", "remove", "clear", "list", "dir"))
            .argument("first", argument -> argument.optional().validator(PluginResolver::resolveValue))
            .argument("second", argument -> argument.optional().validator(PluginResolver::resolveValue))
            .handler(this::executeStaffCommand)
            .build();
    }

    @Compile
    private void executeStaffCommand(DispatchContext dispatchContext) {
        ClientConfigManager.getInstance().load("staff");
        String action = (String) dispatchContext.getArguments().get(0);
        String first = (String) dispatchContext.getArguments().get(1);
        String second = (String) dispatchContext.getArguments().get(2);
        StaffListManager staff = RockstarClient.create().getStaffListManager();
        switch (action.toLowerCase()) {
            case "add" -> this.addStaffMember(staff, first, second);
            case "remove", "del", "dell", "delete" -> staff.removeStaffMember(first);
            case "clear" -> staff.clearStaffMembers();
            case "list" -> this.listStaffMembers(staff);
            case "dir" -> this.openStaffDirectory();
            default -> Notification.error(Text.of((String) Localization.translate("commands.staff.unknown_action")));
        }
    }

    private void openStaffDirectory() {
        try {
            Files.createDirectories(ClientConfigManager.CONFIG_DIRECTORY.toPath(), new FileAttribute[0]);
            ConfigEntry configEntry = ClientConfigManager.getInstance().get("staff");
            if (configEntry != null && !configEntry.getFile().exists()) {
                ClientConfigManager.getInstance().save(configEntry);
            }
            Util.getOperatingSystem().open(ClientConfigManager.CONFIG_DIRECTORY.toURI());
        } catch (Exception exception) {
            Notification.error(Text.of((String) Localization.translateFormatted(
                "commands.staff.dir_error", exception.getMessage())));
        }
    }

    private void addStaffMember(StaffListManager staff, String first, String second) {
        if (first == null || first.isBlank()) {
            Notification.error(Text.of((String) Localization.translate("commands.staff.empty_name")));
            return;
        }
        if (second == null || second.isBlank()) {
            staff.addStaffMember(first, "MODER");
            return;
        }
        staff.addStaffMember(second, first);
    }

    private void listStaffMembers(StaffListManager staff) {
        List<StaffListManager.StaffEntry> members = staff.getStaffMembers();
        if (members.isEmpty()) {
            Notification.info(Text.of((String) Localization.translate("commands.staff.empty")));
            return;
        }
        Notification.info(Text.of((String) Localization.translate("commands.staff.list")));
        int index = 1;
        for (StaffListManager.StaffEntry member : members) {
            Notification.info(Text.of((String) Localization.translateFormatted(
                "commands.staff.list_item",
                index++,
                member.getPrefix().isBlank() ? "MODER" : member.getPrefix(),
                member.getName())));
        }
    }
}
