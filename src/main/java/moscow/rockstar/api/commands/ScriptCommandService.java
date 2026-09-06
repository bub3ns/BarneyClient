package moscow.rockstar.api.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import moscow.rockstar.api.data.ClientConfigManager;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.api.scripts.ScriptRegistry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.scripts.python.PythonRuntime;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import ua.mintantileak.spk.Compile;

/**
 * ORIGINAL: {@code rockstar/ilIlil/IIiiIiiI}, slot 23 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 */
public final class ScriptCommandService {
    @Compile
    public ServiceRegistry getCommandRegistration() {
        List<String> scriptNames = RockstarClient.create().getScriptRegistry().getScripts().stream()
            .map(ScriptDescriptor::getScriptName)
            .toList();
        return CommandBuilder.command("script")
            .aliases("py", "python")
            .description("commands.lua.description")
            .argument("action", argument -> argument
                .validator(value -> ScriptAction.find(value)
                    .<PluginResolver>map(PluginResolver::resolveValue)
                    .orElseGet(() -> PluginResolver.resolveMessage(
                        Localization.translate("commands.config.invalid_action"))))
                .choices(ScriptAction.actionNames()))
            .argument("id", argument -> argument
                .optional()
                .validator(PluginResolver::resolveValue)
                .choices(scriptNames))
            .handler(this::executeScriptCommand)
            .build();
    }

    @Compile
    private void executeScriptCommand(DispatchContext dispatchContext) {
        ScriptAction action = (ScriptAction) dispatchContext.getArguments().get(0);
        String id = (String) dispatchContext.getArguments().get(1);
        action.action().accept(id);
    }

    /** ORIGINAL: the nested {@code enum I} of {@code rockstar/ilIlil/IIiiIiiI}. */
    enum ScriptAction {
        SAVE("save", "\u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c", "\u044b\u0444\u043c\u0443"),
        CREATE("create", "add"),
        REMOVE("remove", "delete", "del", "\u0443\u0434\u0430\u043b\u0438\u0442\u044c", "\u0432\u0443\u0434\u0443\u0435\u0443"),
        LIST("list", "\u0434\u0448\u044b\u0435"),
        LOAD("load", "use", "enable", "\u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c", "true", "\u0434\u0449\u0444\u0432"),
        UNLOAD("unload", "disable", "off", "false"),
        RELOAD("reload", "update"),
        TOGGLE("toggle"),
        INSTALL("install", "pip", "i"),
        DIR("dir", "direction");

        private final List<String> aliases;

        ScriptAction(String... aliases) {
            this.aliases = Arrays.stream(aliases).map(String::toLowerCase).toList();
        }

        @Compile
        Consumer<String> action() {
            return switch (this.ordinal()) {
                case 0 -> this::showSaveHint;
                case 1 -> this::createScript;
                case 6 -> name -> this.reloadScripts();
                case 2 -> this::deleteScript;
                case 4 -> this::loadScript;
                case 7 -> this::toggleScript;
                case 5 -> this::unloadScript;
                case 8 -> this::installPackage;
                case 3 -> name -> this.listScripts();
                case 9 -> name -> {
                    try {
                        File directory = new File(ClientConfigManager.CONFIG_DIRECTORY, "scripts");
                        String[] command = new String[]{"explorer", directory.getAbsolutePath()};
                        Runtime.getRuntime().exec(command);
                    } catch (Exception exception) {
                        RockstarClient.LOGGER.error(Localization.translateFormatted(
                            "commands.lua.dir.error", exception.getMessage()));
                    }
                };
                default -> throw new MatchException(null, null);
            };
        }

        @Compile
        private void showSaveHint(String name) {
            Notification.info(Text.of((String) Localization.translate("commands.lua.save.edit_directly")));
        }

        @Compile
        private void createScript(String name) {
            if (name == null) {
                return;
            }
            ScriptRegistry registry = RockstarClient.create().getScriptRegistry();
            Optional<ScriptDescriptor> existing = registry.getScripts().stream()
                .filter(script -> script.getScriptName().equals(name)).findFirst();
            if (existing.isPresent()) {
                Notification.info(Text.of((String) Localization.translateFormatted("commands.lua.create.exists", name)));
            } else {
                ScriptDescriptor descriptor = new ScriptDescriptor(name);
                if (descriptor.getScriptFile().exists()) {
                    Notification.info(Text.of((String) Localization.translateFormatted("commands.lua.create.exists", name)));
                    return;
                }
                registry.getScripts().add(descriptor.ensureScriptFile());
                Notification.info(Text.of((String) Localization.translateFormatted("commands.lua.create.success", name)));
            }
        }

        private void listScripts() {
            ScriptRegistry registry = RockstarClient.create().getScriptRegistry();
            if (registry.getScripts().isEmpty()) {
                Notification.info(Text.of((String) Localization.translate("commands.lua.not_found_list")));
                return;
            }
            Notification.info(Text.of((String) Localization.translate("commands.lua.list")));
            int index = 1;
            for (ScriptDescriptor script : registry.getScripts()) {
                String name = script.getScriptName();
                String toggleCommand = ".py toggle \"" + name.replace("\"", "\\\"") + "\"";
                MutableText line = Text.literal((String) ("[" + index++ + "] "))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromFormatting((Formatting) Formatting.GRAY)))
                    .append((Text) Text.literal((String) name).setStyle(Style.EMPTY
                        .withColor(TextColor.fromFormatting((Formatting) Formatting.WHITE))));
                if (script.isLoaded()) {
                    line.append((Text) Text.literal((String) (" " + Localization.translate("status.enabled")))
                        .setStyle(Style.EMPTY
                            .withColor(TextColor.fromFormatting((Formatting) Formatting.GREEN))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, toggleCommand))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                (Text) Text.literal((String) Localization.translate("python.hover_disable"))))));
                } else {
                    line.append((Text) Text.literal((String) (" " + Localization.translate("status.disabled")))
                        .setStyle(Style.EMPTY
                            .withColor(TextColor.fromFormatting((Formatting) Formatting.RED))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, toggleCommand))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                (Text) Text.literal((String) Localization.translate("python.hover_enable"))))));
                }
                Notification.sendChat((Text) line);
            }
        }

        @Compile
        private void toggleScript(String name) {
            if (name == null) {
                return;
            }
            ScriptRegistry registry = RockstarClient.create().getScriptRegistry();
            Optional<ScriptDescriptor> existing = registry.getScripts().stream()
                .filter(script -> script.getScriptName().equals(name)).findFirst();
            if (existing.isPresent()) {
                registry.setScriptEnabled(name, !existing.get().isLoaded());
                this.listScripts();
            } else {
                Notification.info(Text.of((String) Localization.translateFormatted("commands.lua.not_found", name)));
            }
        }

        @Compile
        private void deleteScript(String name) {
            if (name == null) {
                return;
            }
            ScriptRegistry registry = RockstarClient.create().getScriptRegistry();
            Optional<ScriptDescriptor> existing = registry.getScripts().stream()
                .filter(script -> script.getScriptName().equals(name)).findFirst();
            if (existing.isPresent()) {
                if (existing.get().deleteScriptFile()) {
                    registry.setScriptEnabled(name, false);
                    Notification.info(Text.of((String) Localization.translateFormatted("commands.lua.delete.success", name)));
                } else {
                    Notification.info(Text.of((String) Localization.translateFormatted("commands.lua.delete.error", name)));
                }
            } else {
                Notification.info(Text.of((String) Localization.translateFormatted("commands.lua.not_found", name)));
            }
        }

        @Compile
        private void loadScript(String name) {
            if (name == null) {
                return;
            }
            ScriptRegistry registry = RockstarClient.create().getScriptRegistry();
            Optional<ScriptDescriptor> existing = registry.getScripts().stream()
                .filter(script -> script.getScriptName().equals(name)).findFirst();
            if (existing.isPresent()) {
                registry.setScriptEnabled(name, true);
                Notification.info(Text.of((String) Localization.translateFormatted("commands.lua.load.success", name)));
            } else {
                Notification.info(Text.of((String) Localization.translateFormatted("commands.lua.not_found", name)));
            }
        }

        @Compile
        private void unloadScript(String name) {
            if (name == null) {
                return;
            }
            ScriptRegistry registry = RockstarClient.create().getScriptRegistry();
            Optional<ScriptDescriptor> existing = registry.getScripts().stream()
                .filter(script -> script.getScriptName().equals(name)).findFirst();
            if (existing.isPresent()) {
                if (existing.get().isLoaded()) {
                    registry.setScriptEnabled(name, false);
                    Notification.info(Text.of((String) Localization.translateFormatted("commands.lua.unload.success", name)));
                } else {
                    Notification.info(Text.of((String) Localization.translateFormatted("commands.lua.unload.not_loaded", name)));
                }
            } else {
                Notification.info(Text.of((String) Localization.translateFormatted("commands.lua.not_found", name)));
            }
        }

        @Compile
        private void reloadScripts() {
            ScriptRegistry registry = RockstarClient.create().getScriptRegistry();
            registry.discoverScripts();
        }

        @Compile
        private void installPackage(String packageName) {
            if (packageName == null) {
                Notification.info(Text.of((String) Localization.translate("python.pip.usage")));
                return;
            }
            File executable = new File(PythonRuntime.getEnvironmentDirectory(), "python.exe");
            if (!executable.exists()) {
                Notification.error(Text.of((String) Localization.translate("python.runtime_missing")));
                return;
            }
            Notification.info(Text.of((String) Localization.translateFormatted("python.pip.installing", packageName)));
            Thread thread = new Thread(() -> {
                String summary = "";
                int exitCode = -1;
                try {
                    Process process = new ProcessBuilder(executable.getAbsolutePath(), "-m", "pip", "install",
                        packageName, "--no-warn-script-location").redirectErrorStream(true).start();
                    try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            RockstarClient.LOGGER.info("[pip] {}", (Object) line);
                            if (!line.startsWith("Successfully installed") && !line.startsWith("ERROR")) {
                                continue;
                            }
                            summary = line;
                        }
                    }
                    exitCode = process.waitFor();
                } catch (Exception exception) {
                    summary = exception.getMessage() != null
                        ? exception.getMessage()
                        : exception.getClass().getSimpleName();
                }
                int finalExitCode = exitCode;
                String finalSummary = summary;
                ClientAccess.minecraftClient.execute(() -> reportInstall(finalExitCode, packageName, finalSummary));
            }, "pip-install-" + packageName);
            thread.setDaemon(true);
            thread.start();
        }

        @Compile
        static Optional<ScriptAction> find(String value) {
            String lowered = value.toLowerCase();
            return Arrays.stream(ScriptAction.values()).filter(action -> action.aliases.contains(lowered)).findFirst();
        }

        @Compile
        static List<String> actionNames() {
            return Arrays.stream(ScriptAction.values()).map(action -> action.aliases.getFirst()).toList();
        }

        private static void reportInstall(int exitCode, String packageName, String summary) {
            if (exitCode == 0) {
                Notification.info(Text.of((String) Localization.translateFormatted("python.pip.done", packageName)));
            } else {
                Notification.error(Text.of((String) Localization.translateFormatted("python.pip.fail", packageName, summary)));
            }
        }
    }
}
