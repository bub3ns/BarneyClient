package moscow.rockstar.api.commands;

import java.util.List;
import java.util.Locale;
import moscow.rockstar.api.data.ClientConfigManager;
import moscow.rockstar.api.data.SettingDataStore;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.api.settings.SettingEntry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.input.KeyDisplayFormatter;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

/**
 * ORIGINAL: {@code rockstar/ilIlil/IIiiIIII}, slot 14 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 */
public class MacroCommandService {
    public final ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("macro")
            .aliases("macros")
            .description("commands.macro.description")
            .argument("action", argument -> argument
                .choicesAndValidator("add", "remove", "delete", "list", "clear")
                .choices("add", "remove", "list", "clear"))
            .argument("arguments", argument -> argument
                .optional()
                .vararg()
                .validator(PluginResolver::resolveValue))
            .handler(this::executeMacroCommand)
            .build();
    }

    @Compile
    private void executeMacroCommand(DispatchContext dispatchContext) {
        String action = (String) dispatchContext.getArguments().getFirst();
        List<String> arguments = dispatchContext.getArguments().size() > 1
            ? castArguments(dispatchContext.getArguments().get(1))
            : List.of();
        SettingDataStore store = RockstarClient.create().getSettingDataStore();
        switch (action.toLowerCase(Locale.ROOT)) {
            case "add" -> this.addMacro(store, arguments);
            case "remove", "delete" -> this.removeMacro(store, arguments);
            case "list" -> this.listMacros(store);
            case "clear" -> this.clearMacros(store);
            default -> { }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> castArguments(Object value) {
        return value == null ? List.of() : (List<String>) value;
    }

    private void addMacro(SettingDataStore store, List<String> arguments) {
        if (arguments.size() < 2) {
            Notification.error(Text.of((String) Localization.translate("commands.macro.error_arguments")));
            return;
        }
        MacroEntry entry = this.parseMacro(arguments);
        if (entry == null) {
            Notification.error(Text.of((String) Localization.translate("commands.macro.error_key")));
            return;
        }
        store.registerBinding(entry.command(), entry.keyCode());
        ClientConfigManager.getInstance().save("client");
        Notification.info(Text.of((String) Localization.translateFormatted(
            "commands.macro.added", entry.command(), KeyDisplayFormatter.formatKey(entry.keyCode()))));
    }

    private void removeMacro(SettingDataStore store, List<String> arguments) {
        boolean removed;
        if (arguments.isEmpty()) {
            Notification.error(Text.of((String) Localization.translate("commands.macro.error_arguments")));
            return;
        }
        MacroEntry entry = this.parseMacro(arguments);
        if (entry != null) {
            removed = store.removeBinding(entry.command(), entry.keyCode());
            if (removed) {
                Notification.info(Text.of((String) Localization.translateFormatted(
                    "commands.macro.removed_specific", entry.command(), KeyDisplayFormatter.formatKey(entry.keyCode()))));
            }
        } else {
            String joined = String.join(" ", arguments);
            int keyCode = KeyDisplayFormatter.parseKey(joined);
            if (keyCode != -1) {
                removed = store.removeBindingByKey(keyCode);
                if (removed) {
                    Notification.info(Text.of((String) Localization.translateFormatted(
                        "commands.macro.removed_key", KeyDisplayFormatter.formatKey(keyCode))));
                }
            } else {
                removed = store.removeBindingByCommand(joined);
                if (removed) {
                    Notification.info(Text.of((String) Localization.translateFormatted(
                        "commands.macro.removed_command", joined)));
                }
            }
        }
        if (!removed) {
            Notification.error(Text.of((String) Localization.translate("commands.macro.not_found")));
            return;
        }
        ClientConfigManager.getInstance().save("client");
    }

    private void listMacros(SettingDataStore store) {
        List<SettingEntry> bindings = store.getBindings();
        if (bindings.isEmpty()) {
            Notification.info(Text.of((String) Localization.translate("commands.macro.list_empty")));
            return;
        }
        Notification.info(Text.of((String) Localization.translate("commands.macro.list_header")));
        int index = 1;
        for (SettingEntry binding : bindings) {
            String command = binding.getCommand();
            String removeCommand = ".macro remove " + command;
            MutableText line = Text.literal((String) ("[" + index++ + "] "))
                .setStyle(Style.EMPTY.withColor(TextColor.fromFormatting((Formatting) Formatting.GRAY)))
                .append((Text) Text.literal((String) (command + " ")).setStyle(Style.EMPTY
                    .withColor(TextColor.fromRgb((int) new ColorRGBA(87.0f, 126.0f, 255.0f).getRGB()))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, removeCommand))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        (Text) Text.literal((String) Localization.translate("macro.hover_delete"))))))
                .append((Text) Text.literal((String) ("(" + KeyDisplayFormatter.formatKey(binding.getKeyCode()) + ")")))
                .setStyle(Style.EMPTY.withColor(TextColor.fromFormatting((Formatting) Formatting.GRAY)));
            Notification.sendChat((Text) line);
        }
    }

    private void clearMacros(SettingDataStore store) {
        if (store.getBindings().isEmpty()) {
            Notification.error(Text.of((String) Localization.translate("commands.macro.list_empty")));
            return;
        }
        store.clearBindings();
        ClientConfigManager.getInstance().save("client");
        Notification.info(Text.of((String) Localization.translate("commands.macro.cleared")));
    }

    private MacroEntry parseMacro(List<String> arguments) {
        String trailing;
        String leading;
        if (arguments.isEmpty()) {
            return null;
        }
        int firstKey = KeyDisplayFormatter.parseKey(arguments.getFirst());
        if (firstKey != -1 && arguments.size() > 1
            && !(leading = String.join(" ", arguments.subList(1, arguments.size())).trim()).isEmpty()) {
            return new MacroEntry(leading, firstKey);
        }
        int lastKey = KeyDisplayFormatter.parseKey(arguments.getLast());
        if (lastKey != -1 && arguments.size() > 1
            && !(trailing = String.join(" ", arguments.subList(0, arguments.size() - 1)).trim()).isEmpty()) {
            return new MacroEntry(trailing, lastKey);
        }
        return null;
    }

    /** ORIGINAL: the nested {@code record I(String command, int keyCode)} of {@code IIiiIIII}. */
    private record MacroEntry(String command, int keyCode) {
    }
}
