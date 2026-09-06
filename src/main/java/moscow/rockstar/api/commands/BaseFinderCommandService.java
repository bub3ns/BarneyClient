package moscow.rockstar.api.commands;
import moscow.rockstar.ui.localization.Localization;

import java.util.List;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.other.base.BaseFinder;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import ua.mintantileak.spk.Compile;

/**
 * ORIGINAL: {@code rockstar/ilIlil/IIiIIiIi}, slot 21 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 */
public class BaseFinderCommandService {
    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("base")
            .description("Base Finder list")
            .argument("action", argument -> argument
                .choicesAndValidator("list", "clear", "remove", "delete", "del")
                .choices("list", "clear", "remove"))
            .argument("target", argument -> argument
                .optional()
                .vararg()
                .validator(PluginResolver::resolveValue))
            .handler(this::executeBaseCommand)
            .build();
    }

    @Compile
    private void executeBaseCommand(DispatchContext dispatchContext) {
        String action = (String) dispatchContext.getArguments().get(0);
        BaseFinder baseFinder = RockstarClient.create().getModuleRegistry().getModule(BaseFinder.class);
        switch (action.toLowerCase()) {
            case "list" -> this.listBases(baseFinder);
            case "clear" -> this.clearBases(baseFinder);
            case "remove", "delete", "del" -> this.removeBase(baseFinder, this.targetArguments(dispatchContext));
            default -> { }
        }
    }

    private void listBases(BaseFinder baseFinder) {
        List<BaseFinder.BaseLocation> bases = baseFinder.getDetectedBases();
        if (bases.isEmpty()) {
            Notification.info(Text.of(Localization.translate("commands.base.list_empty")));
            return;
        }
        for (int index = 0; index < bases.size(); ++index) {
            Notification.info(Text.of((String) BaseFinder.formatBaseNotification(bases.get(index))));
        }
    }

    private void clearBases(BaseFinder baseFinder) {
        int cleared = baseFinder.consumeDetectedBaseCount();
        Notification.info(Text.of(Localization.translateFormatted("commands.base.cleared", cleared)));
    }

    private void removeBase(BaseFinder baseFinder, List<String> target) {
        if (target.isEmpty()) {
            Notification.error(Text.of(Localization.translate("commands.base.usage_remove")));
            return;
        }
        BaseFinder.BaseLocation removed = this.isCoordinateTriple(target)
            ? baseFinder.removeBaseAtPosition(new BlockPos(
                Integer.parseInt(target.get(0)), Integer.parseInt(target.get(1)), Integer.parseInt(target.get(2))))
            : baseFinder.removeBaseByName(String.join(" ", target));
        if (removed == null) {
            Notification.error(Text.of(Localization.translate("commands.base.not_found")));
            return;
        }
        Notification.info(Text.of(Localization.translateFormatted("commands.base.removed", BaseFinder.formatBaseNotification(removed))));
    }

    private boolean isCoordinateTriple(List<String> target) {
        if (target.size() != 3) {
            return false;
        }
        for (String value : target) {
            if (this.isInteger(value)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private List<String> targetArguments(DispatchContext dispatchContext) {
        if (dispatchContext.getArguments().size() <= 1 || dispatchContext.getArguments().get(1) == null) {
            return List.of();
        }
        return ((List<?>) dispatchContext.getArguments().get(1)).stream().map(String::valueOf).toList();
    }
}
