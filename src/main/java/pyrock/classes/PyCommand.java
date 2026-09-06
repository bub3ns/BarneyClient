/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Text
 *  org.jetbrains.annotations.Nullable
 */
package pyrock.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jep.python.PyCallable;
import moscow.rockstar.api.commands.ClientCommandDispatcher;
import moscow.rockstar.api.commands.CommandBuilder;
import moscow.rockstar.api.commands.CommandSuggestionProvider;
import moscow.rockstar.api.commands.NavigationCommandService;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.network.session.BotPacketListener;
import moscow.rockstar.world.BlockNameResolver;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class PyCommand {
    private final PyCommand root;
    private final ScriptDescriptor owner;
    private final String name;
    private final List<String> aliases = new ArrayList<String>();
    private final List<Arg> args = new ArrayList<Arg>();
    private final List<PyCommand> subs = new ArrayList<PyCommand>();
    private String desc = "";
    @Nullable
    private PyCallable handler;
    @Nullable
    private ServiceRegistry registered;

    public PyCommand(String string) {
        this(string, null);
    }

    private PyCommand(String string, @Nullable PyCommand pyCommand) {
        this.name = string.toLowerCase(Locale.ROOT);
        this.root = pyCommand == null ? this : pyCommand;
        this.owner = pyCommand == null ? ScriptDescriptor.getCurrentScript() : pyCommand.owner;
    }

    public PyCommand alias(String string) {
        if (string != null && !string.isBlank()) {
            this.aliases.add(string.toLowerCase(Locale.ROOT));
        }
        return this;
    }

    public PyCommand desc(String string) {
        this.desc = string == null ? "" : string;
        return this;
    }

    public PyCommand handler(PyCallable pyCallable) {
        this.handler = pyCallable;
        return this;
    }

    public PyCommand arg(String string, String string2, boolean bl, boolean bl2, Object object) {
        ArrayList<String> arrayList = null;
        PyCallable pyCallable = null;
        if (object instanceof PyCallable) {
            PyCallable pyCallable2;
            pyCallable = pyCallable2 = (PyCallable)object;
        } else if (object instanceof List) {
            List list = (List)object;
            arrayList = new ArrayList<String>();
            for (Object e : list) {
                if (e == null) continue;
                arrayList.add(String.valueOf(e));
            }
        }
        this.args.add(new Arg(string, string2 == null ? "str" : string2.toLowerCase(Locale.ROOT), bl, bl2, pyCallable, arrayList));
        return this;
    }

    public PyCommand sub(String string) {
        PyCommand pyCommand = new PyCommand(string, this.root);
        this.subs.add(pyCommand);
        return pyCommand;
    }

    public String name() {
        return this.name;
    }

    public PyCommand install() {
        if (this.root != this) {
            return this.root.install();
        }
        NavigationCommandService navigationCommandService = RockstarClient.create().getNavigationCommandService();
        if (this.registered == null) {
            String string = this.firstTakenName(navigationCommandService);
            if (string != null) {
                throw new IllegalStateException("\u043a\u043e\u043c\u0430\u043d\u0434\u0430 '" + string + "' \u0443\u0436\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442 \u0432 \u043a\u043b\u0438\u0435\u043d\u0442\u0435");
            }
        } else {
            navigationCommandService.unregisterCommand(this.registered);
        }
        this.registered = this.build();
        navigationCommandService.registerCommand(this.registered);
        ScriptDescriptor.registerCommand(this);
        return this;
    }

    public boolean remove() {
        if (this.root != this) {
            return this.root.remove();
        }
        if (this.registered == null) {
            return false;
        }
        RockstarClient.create().getNavigationCommandService().unregisterCommand(this.registered);
        this.registered = null;
        return true;
    }

    public boolean installed() {
        return this.root.registered != null;
    }

    /**
     * ORIGINAL: {@code firstTakenName(Lrockstar/ilIlil/IIIiiiIi;)Ljava/lang/String;} - walks the
     * dispatcher's own command list, skips {@code this.registered}, and compares every name the
     * registered command exposes against this command's name + aliases, returning the *candidate*
     * that collided. The remap's {@code isCommandRegistered(name)} loop reversed the iteration
     * order, so a collision on an alias could report the wrong name.
     */
    @Nullable
    private String firstTakenName(NavigationCommandService navigationCommandService) {
        ArrayList<String> names = new ArrayList<String>();
        names.add(this.name);
        names.addAll(this.aliases);
        for (ServiceRegistry command : ClientCommandDispatcher.getRegisteredCommands()) {
            if (command == this.registered) continue;
            for (String registered : command.getRegisteredServices()) {
                for (String candidate : names) {
                    if (!registered.equalsIgnoreCase(candidate)) continue;
                    return candidate;
                }
            }
        }
        return null;
    }

    private ServiceRegistry build() {
        CommandBuilder commandBuilder = CommandBuilder.command(this.name).description(this.desc);
        if (!this.aliases.isEmpty()) {
            commandBuilder.aliases(this.aliases.toArray(new String[0]));
        }
        for (Arg object : this.args) {
            commandBuilder.argument(object.name(), argumentBuilder -> {
                if (!object.required()) {
                    argumentBuilder.optional();
                }
                if (object.vararg()) {
                    argumentBuilder.vararg();
                }
                argumentBuilder.validator(this.validator(object));
            });
        }
        if (!this.subs.isEmpty()) {
            ArrayList<ServiceRegistry> children = new ArrayList<>();
            for (PyCommand pyCommand : this.subs) {
                children.add(pyCommand.build());
            }
            commandBuilder.children(children.toArray(new ServiceRegistry[0]));
        }
        return commandBuilder.handler(this::execute).build();
    }

    private void execute(DispatchContext dispatchContext) {
        if (this.handler == null) {
            Notification.info(Text.of((String)this.usage()));
            return;
        }
        ArrayList<Object> arrayList = new ArrayList<Object>(dispatchContext.getArguments());
        if (moscow.rockstar.scripts.python.PythonRuntime.isAvailable()) {
            this.call(arrayList);
        } else {
            ClientAccess.minecraftClient.execute(() -> this.call(arrayList));
        }
    }

    private void call(List<Object> list) {
        if (this.handler == null || this.owner != null && !this.owner.isLoaded()) {
            return;
        }
        try (AutoCloseable autoCloseable = ScriptDescriptor.pushCurrentScript(this.owner);){
            this.handler.call(new Object[]{this.name, list});
        }
        catch (Exception exception) {
            String string = exception.getMessage();
            if (string != null && string.contains(":")) {
                string = string.substring(string.indexOf(":") + 1).trim();
            }
            Notification.error(Text.of((String)("[Python Error] " + string)));
            RockstarClient.LOGGER.error("Python error in command '" + this.name + "':", (Throwable)exception);
        }
    }

    private String usage() {
        StringBuilder stringBuilder = new StringBuilder(RockstarClient.create().getNavigationCommandService().getCommandPrefix()).append(this.name);
        for (PyCommand object : this.subs) {
            stringBuilder.append(' ').append(object.name);
        }
        for (Arg arg : this.args) {
            stringBuilder.append(arg.required() ? " <" : " [").append(arg.name()).append(arg.required() ? (char)'>' : ']');
        }
        return stringBuilder.toString();
    }

    private CommandSuggestionProvider validator(final Arg arg) {
        return new CommandSuggestionProvider(){

            @Override
            public PluginResolver validate(String string) {
                return switch (arg.type()) {
                    case "int" -> {
                        try {
                            PluginResolver.ValueResult<Integer> var4_4 = PluginResolver.resolveValue(Integer.parseInt(string));
                            yield var4_4;
                        }
                        catch (NumberFormatException var5_13) {
                            PluginResolver.MessageResult var4_5 = PluginResolver.resolveMessage("'" + string + "' \u043d\u0435 \u0446\u0435\u043b\u043e\u0435 \u0447\u0438\u0441\u043b\u043e (\u0430\u0440\u0433\u0443\u043c\u0435\u043d\u0442 " + arg.name() + ")");
                            yield var4_5;
                        }
                    }
                    case "float", "number" -> {
                        try {
                            PluginResolver.ValueResult<Double> var4_6 = PluginResolver.resolveValue(Double.parseDouble(string));
                            yield var4_6;
                        }
                        catch (NumberFormatException var5_14) {
                            PluginResolver.MessageResult var4_7 = PluginResolver.resolveMessage("'" + string + "' \u043d\u0435 \u0447\u0438\u0441\u043b\u043e (\u0430\u0440\u0433\u0443\u043c\u0435\u043d\u0442 " + arg.name() + ")");
                            yield var4_7;
                        }
                    }
                    case "bool" -> {
                        Boolean var5_15 = PyCommand.parseBool(string);
                        PluginResolver var4_8 = var5_15 == null ? PluginResolver.resolveMessage("'" + string + "' \u043d\u0435 \u0434\u0430/\u043d\u0435\u0442 (\u0430\u0440\u0433\u0443\u043c\u0435\u043d\u0442 " + arg.name() + ")") : PluginResolver.resolveValue(var5_15);
                        yield var4_8;
                    }
                    case "block" -> {
                        yield BlockNameResolver.resolveArgument(string);
                    }
                    case "module" -> {
                        if (PyCommand.findModule(string) == null) {
                            PluginResolver.MessageResult var4_10 = PluginResolver.resolveMessage("\u043c\u043e\u0434\u0443\u043b\u044f '" + string + "' \u043d\u0435\u0442 \u0432 \u043a\u043b\u0438\u0435\u043d\u0442\u0435");
                            yield var4_10;
                        }
                        PluginResolver.ValueResult<String> var4_11 = PluginResolver.resolveValue(string);
                        yield var4_11;
                    }
                    default -> {
                        PluginResolver.ValueResult<String> var4_12;
                        yield var4_12 = PluginResolver.resolveValue(string);
                    }
                };
            }

            @Override
            public List<String> suggestions(String string) {
                String string2;
                String string3 = string2 = string == null ? "" : string.toLowerCase(Locale.ROOT);
                if (arg.suggest() != null) {
                    return PyCommand.this.fromScript(arg.suggest(), string);
                }
                if (arg.suggestions() != null) {
                    return PyCommand.filter(arg.suggestions(), string2);
                }
                return switch (arg.type()) {
                    case "player" -> PyCommand.filter(PyCommand.playerNames(), string2);
                    case "module" -> PyCommand.filter(PyCommand.moduleNames(), string2);
                    case "block" -> BlockNameResolver.suggestions(string2);
                    case "bool" -> PyCommand.filter(List.of("true", "false"), string2);
                    default -> List.of();
                };
            }
        };
    }

    List<String> fromScript(PyCallable pyCallable, String string) {
        if (this.owner != null && !this.owner.isLoaded()) {
            return List.of();
        }
        if (!moscow.rockstar.scripts.python.PythonRuntime.isAvailable()) {
            return List.of();
        }
        try (AutoCloseable ignored = ScriptDescriptor.pushCurrentScript(this.owner)) {
            Object result = pyCallable.call(new Object[]{string == null ? "" : string});
            if (!(result instanceof List<?> values)) {
                return List.of();
            }
            ArrayList<String> suggestions = new ArrayList<>(values.size());
            for (Object value : values) {
                if (value != null) {
                    suggestions.add(String.valueOf(value));
                }
            }
            return suggestions;
        } catch (Throwable ignored) {
            return List.of();
        }
    }

    static List<String> filter(List<String> list, String string) {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string2 : list) {
            if (!string2.toLowerCase(Locale.ROOT).startsWith(string)) continue;
            arrayList.add(string2);
        }
        return arrayList;
    }

    static List<String> playerNames() {
        ArrayList<String> arrayList = new ArrayList<String>();
        if (ClientAccess.minecraftClient.world == null) {
            return arrayList;
        }
        for (PlayerEntity class_16572 : ClientAccess.minecraftClient.world.getPlayers()) {
            arrayList.add(class_16572.getNameForScoreboard());
        }
        return arrayList;
    }

    static List<String> moduleNames() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (ModuleContract moduleContract : RockstarClient.create().getModuleRegistry().getModules()) {
            arrayList.add(moduleContract.getName());
        }
        return arrayList;
    }

    @Nullable
    static ModuleContract findModule(String string) {
        for (ModuleContract moduleContract : RockstarClient.create().getModuleRegistry().getModules()) {
            if (!moduleContract.getName().equalsIgnoreCase(string)) continue;
            return moduleContract;
        }
        return null;
    }

    @Nullable
    static Boolean parseBool(String string) {
        return switch (string.toLowerCase(Locale.ROOT)) {
            case "true", "1", "on", "yes", "\u0434\u0430", "\u0432\u043a\u043b" -> Boolean.TRUE;
            case "false", "0", "off", "no", "\u043d\u0435\u0442", "\u0432\u044b\u043a\u043b" -> Boolean.FALSE;
            default -> null;
        };
    }

    record Arg(String name, String type, boolean required, boolean vararg, @Nullable PyCallable suggest, @Nullable List<String> suggestions) {
    }
}
