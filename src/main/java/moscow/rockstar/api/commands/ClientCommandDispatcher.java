package moscow.rockstar.api.commands;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import moscow.rockstar.api.data.RecordQueue;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.combat.rotation.RotationEventProcessor;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventHandlerFactory;
import moscow.rockstar.events.EventHandlerListener;
import moscow.rockstar.inventory.ContainerSelectionController;
import moscow.rockstar.modules.other.fakeplayer.FakePlayerController;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;

/**
 * Owns client-side command registrations and dispatches chat command text.
 *
 * <p>Holds the top-level command list only; sub-commands are reached by walking
 * {@link ServiceRegistry#getServiceDependencies()} while a command path is resolved,
 * exactly like the original dispatcher.</p>
 */
public final class ClientCommandDispatcher {
    /** The prefix the original ships with; {@code .prefix reset} restores it. */
    public static final String DEFAULT_PREFIX = ".";
    /** ORIGINAL: the mutable {@code java.lang.String} field on {@code rockstar/ilIlil/IIIiiiIi}. */
    private static String prefix = DEFAULT_PREFIX;
    private static final List<ServiceRegistry> COMMANDS = new ArrayList<>();

    private ClientCommandDispatcher() {
    }

    public static void clear() {
        COMMANDS.clear();
    }

    /** ORIGINAL: {@code IIIiiiIi#I ()Ljava/lang/String;}. */
    public static String getPrefix() {
        return prefix;
    }

    /** ORIGINAL: {@code IIIiiiIi#I (Ljava/lang/String;)V}. */
    public static void setPrefix(String value) {
        prefix = value;
    }

    public static void register(ServiceRegistry command) {
        COMMANDS.add(command);
    }

    public static boolean unregister(ServiceRegistry command) {
        return COMMANDS.remove(command);
    }

    /**
     * Builds and installs every built-in client command, in the original registration order.
     *
     * <p>The slot comments are the positions in {@code rockstar/ilIlil/IIIiiiIi#I()V}, which
     * registers 28 commands.  Four of them are not installed here:</p>
     * <ul>
     *   <li>slot 3 {@code config} ({@code IIiIiIIi}) - its manager {@code IIiiiiiI} is the
     *       cloud-synced config service (it takes {@code globals.shared.proto.Packets$ConfigData}),
     *       which is excluded; the local {@link moscow.rockstar.api.data.ClientConfigManager} has no
     *       remove/rename/duplicate/list/reset/undo entry points to drive it with.</li>
     *   <li>slot 7 {@code admin} ({@code IIiIIIiI}) - every branch sends a
     *       {@code globals.shared.proto.Packets} record over {@code globals.client.api.RockNetClient},
     *       and it is the one command gated on {@code ua.mintantileak.profile.Role.ADMIN}.</li>
     *   <li>slot 24 {@code bot} ({@code IIiIIiii}) - 38 sub-actions over a headless bot manager
     *       ({@code IIIIIIIi}/{@code IIIIIiII}) and a dozen behaviour strategies that the remap
     *       does not expose.</li>
     *   <li>slot 28 {@code mcp} ({@code IIiiIIIi}) - drives the local MCP server
     *       {@code IiIiiIII}, which has no remapped counterpart.</li>
     * </ul>
     */
    public static void registerDefaults() {
        register(new CatCommandService().getCommandRegistration());                 // 1
        register(new RotationEventProcessor().buildAutopilotCommand());             // 2
        register(new FakePlayerController().getCommandRegistration());              // 4
        register(new FriendCommandService().getCommandRegistration());              // 5
        register(new HelpCommandService().getCommandRegistration());                // 6
        register(new InventoryCommandService().getCommandRegistration());           // 8
        register(new ContainerSelectionController().getCommandRegistration());      // 9
        register(new PrefixCommandService().getCommandRegistration());              // 10
        register(new EventHandlerFactory().getCommandRegistration());               // 11
        register(new TargetCommandService().getCommandRegistration());              // 12
        register(new VerticalClipCommandService().getCommandRegistration());        // 13
        register(new MacroCommandService().getCommandRegistration());               // 14
        register(new AuthCommandService().getCommandRegistration());                // 15
        register(new EventHandlerListener().buildInvseeCommand());                  // 16
        register(new ModuleToggleCommandService().getCommandRegistration());        // 17
        register(new CommandSuggestionHandler().getCommandRegistration());          // 18
        register(new WaypointCommandService().getCommandRegistration());            // 19
        register(new BindCommandService().getCommandRegistration());                // 20
        register(new BaseFinderCommandService().getCommandRegistration());          // 21
        register(new RecordQueue().createParserCommand());                          // 22
        register(new ScriptCommandService().getCommandRegistration());              // 23
        register(new StaffCommandService().getCommandRegistration());               // 25
        register(new NeuroCommandService().getCommandRegistration());               // 26
        register(new PathfindingCommandService().getCommandRegistration());         // 27
    }

    public static boolean isRegisteredName(String name) {
        if (name == null) {
            return false;
        }
        for (ServiceRegistry command : COMMANDS) {
            for (String registered : command.getRegisteredServices()) {
                if (registered.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns the installed top-level command registrations. */
    public static List<ServiceRegistry> getRegisteredCommands() {
        return Collections.unmodifiableList(COMMANDS);
    }

    /**
     * The original gates a command behind the {@code ua.mintantileak.profile.Role}s declared on its
     * registration. The remapped {@link ServiceRegistry} carries no role list, so every command is
     * reachable here; the only role-gated command in the original (".admin") has no remapped
     * counterpart.
     */
    public static boolean isAccessible(ServiceRegistry command) {
        return command != null;
    }

    public static boolean executeCommand(String input) {
        if (!input.startsWith(prefix) || RockstarClient.create().isPanicMode()) {
            return false;
        }
        String commandText = input.substring(prefix.length()).trim();
        if (commandText.isEmpty()) {
            return true;
        }
        String[] tokens = tokenize(commandText);
        if (tokens.length == 0) {
            return true;
        }
        List<String> tokenList = Arrays.asList(tokens);
        Resolution resolution = resolve(tokenList, null, 0, true);
        if (resolution == null) {
            Notification.error(Text.of((String)Localization.translate("commands.unknown")));
            return false;
        }
        if (!resolution.accessible()) {
            Notification.error(Text.of((String)Localization.translate("commands.no_access")));
            return false;
        }
        ServiceRegistry command = resolution.command();
        int index = resolution.index();
        if (!command.isAvailable()) {
            Notification.error(Text.of((String)Localization.translate("commands.not_executable")));
            return false;
        }
        List<Object> arguments = resolveArguments(command, tokens, index);
        if (arguments == null) {
            return true;
        }
        command.getCommandHandler().execute(new DispatchContext(command, arguments));
        return true;
    }

    private static Resolution resolve(List<String> tokens, ServiceRegistry parent, int index, boolean accessible) {
        List<ServiceRegistry> candidates = parent == null ? COMMANDS : parent.getServiceDependencies();
        if (index >= tokens.size()) {
            return finish(parent, index - 1, accessible);
        }
        String token = tokens.get(index);
        for (ServiceRegistry candidate : candidates) {
            for (String name : candidate.getRegisteredServices()) {
                if (!name.equalsIgnoreCase(token)) {
                    continue;
                }
                boolean childAccessible = accessible && isAccessible(candidate);
                Resolution deeper = resolve(tokens, candidate, index + 1, childAccessible);
                if (deeper != null) {
                    return deeper;
                }
                return new Resolution(candidate, index, childAccessible);
            }
        }
        return finish(parent, index - 1, accessible);
    }

    private static Resolution finish(ServiceRegistry command, int index, boolean accessible) {
        return command != null ? new Resolution(command, index, accessible) : null;
    }

    private static List<Object> resolveArguments(ServiceRegistry command, String[] tokens, int index) {
        List<CommandDescriptor<?>> descriptors = command.getServiceAliases();
        List<Object> resolved = new ArrayList<>();
        int valueIndex = index + 1;
        int tokenCount = tokens.length;
        for (CommandDescriptor<?> descriptor : descriptors) {
            if (descriptor.isVararg()) {
                List<Object> vararg = new ArrayList<>();
                for (int i = valueIndex; i < tokenCount; ++i) {
                    PluginResolver result = descriptor.getValidator().validate(tokens[i]);
                    if (result instanceof PluginResolver.MessageResult) {
                        return null;
                    }
                    vararg.add(((PluginResolver.ValueResult<?>)result).getValue());
                }
                if (descriptor.isRequired() && vararg.isEmpty()) {
                    PluginResolver.resolveMessage("Missing value for argument '" + descriptor.getName() + "'");
                    return null;
                }
                resolved.add(vararg);
                return resolved;
            }
            if (valueIndex >= tokenCount) {
                if (descriptor.isRequired()) {
                    PluginResolver.resolveMessage("Missing value for argument '" + descriptor.getName() + "'");
                    return null;
                }
                resolved.add(null);
                continue;
            }
            PluginResolver result = descriptor.getValidator().validate(tokens[valueIndex]);
            if (result instanceof PluginResolver.MessageResult) {
                return null;
            }
            resolved.add(((PluginResolver.ValueResult<?>)result).getValue());
            ++valueIndex;
        }
        if (valueIndex < tokenCount) {
            Notification.error(Text.of((String)Localization.translate("commands.too_many_args")));
            return null;
        }
        return resolved;
    }

    private static String[] tokenize(String input) {
        List<Token> tokens = tokenize(input, 0);
        String[] values = new String[tokens.size()];
        for (int i = 0; i < values.length; ++i) {
            values[i] = tokens.get(i).text();
        }
        return values;
    }

    private static List<Token> tokenize(String input, int offset) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        int start = -1;
        boolean quoted = false;
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            if (quoted) {
                if (c == '\\' && i + 1 < input.length() && input.charAt(i + 1) == '\"') {
                    builder.append('\"');
                    ++i;
                    continue;
                }
                if (c == '\"') {
                    quoted = false;
                    continue;
                }
                builder.append(c);
                continue;
            }
            if (c == '\"') {
                quoted = true;
                if (start >= 0) {
                    continue;
                }
                start = i;
                continue;
            }
            if (Character.isWhitespace(c)) {
                if (start < 0) {
                    continue;
                }
                tokens.add(new Token(builder.toString(), offset + start, offset + i));
                builder.setLength(0);
                start = -1;
                continue;
            }
            builder.append(c);
            if (start >= 0) {
                continue;
            }
            start = i;
        }
        if (start >= 0) {
            tokens.add(new Token(builder.toString(), offset + start, offset + input.length()));
        }
        return tokens;
    }

    public static CompletableFuture<Suggestions> suggestCommands(String input, int cursor) {
        if (!input.startsWith(prefix) || cursor < prefix.length() || RockstarClient.create().isPanicMode()) {
            return Suggestions.empty();
        }
        String text = input.substring(0, Math.min(cursor, input.length()));
        List<Token> tokens = tokenize(text.substring(prefix.length()), prefix.length());
        Token current = tokens.isEmpty() || tokens.getLast().end() < text.length() ? null : tokens.getLast();
        boolean freshToken = current == null;
        List<ServiceRegistry> candidates = COMMANDS;
        ServiceRegistry matched = null;
        int consumed = 0;
        ServiceRegistry step;
        for (int i = 0; i < tokens.size() && (step = findByName(candidates, tokens.get(i).text())) != null; ++i) {
            matched = step;
            consumed = i + 1;
            candidates = step.getServiceDependencies();
            if (candidates.isEmpty()) {
                break;
            }
        }
        int remaining = tokens.size() - consumed;
        String prefixText = current != null ? current.text() : "";
        int rangeStart = current != null ? current.start() : cursor;
        StringRange range = StringRange.between((int)rangeStart, (int)cursor);
        List<Suggestion> suggestions = new ArrayList<>();
        if (matched == null) {
            String lowered = prefixText.toLowerCase();
            for (ServiceRegistry candidate : candidates) {
                String name;
                if (!isAccessible(candidate) || !(name = candidate.getRegisteredServices().getFirst()).toLowerCase().startsWith(lowered)) {
                    continue;
                }
                addSuggestion(suggestions, range, name);
            }
        } else if (!candidates.isEmpty() && (remaining == 0 || remaining == 1 && !freshToken)) {
            String lowered = prefixText.toLowerCase();
            for (ServiceRegistry candidate : candidates) {
                String name;
                if (!isAccessible(candidate) || !(name = candidate.getRegisteredServices().getFirst()).toLowerCase().startsWith(lowered)) {
                    continue;
                }
                addSuggestion(suggestions, range, name);
            }
        } else {
            if (remaining == 0 && !freshToken) {
                return Suggestions.empty();
            }
            List<CommandDescriptor<?>> descriptors = matched.getServiceAliases();
            int argumentIndex = remaining - (freshToken ? 0 : 1);
            if (argumentIndex < 0) {
                argumentIndex = 0;
            }
            CommandDescriptor<?> descriptor = null;
            if (argumentIndex >= descriptors.size()) {
                if (!descriptors.isEmpty() && descriptors.getLast().isVararg()) {
                    descriptor = descriptors.getLast();
                }
            } else {
                descriptor = descriptors.get(argumentIndex);
            }
            if (descriptor != null && descriptor.getValidator() != null) {
                String lowered = prefixText.toLowerCase();
                for (String suggestion : descriptor.getValidator().suggestions(lowered)) {
                    addSuggestion(suggestions, range, suggestion);
                }
            }
        }
        if (!suggestions.isEmpty()) {
            return CompletableFuture.completedFuture(new Suggestions(range, suggestions));
        }
        return Suggestions.empty();
    }

    private static ServiceRegistry findByName(List<ServiceRegistry> candidates, String token) {
        for (ServiceRegistry candidate : candidates) {
            if (!isAccessible(candidate)) {
                continue;
            }
            for (String name : candidate.getRegisteredServices()) {
                if (name.equalsIgnoreCase(token)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static void addSuggestion(List<Suggestion> suggestions, StringRange range, String text) {
        String quoted = quoteIfNeeded(text);
        for (Suggestion suggestion : suggestions) {
            if (suggestion.getText().equalsIgnoreCase(quoted)) {
                return;
            }
        }
        suggestions.add(new Suggestion(range, quoted));
    }

    private static String quoteIfNeeded(String text) {
        boolean needsQuotes = false;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (!Character.isWhitespace(c) && c != '\"') {
                continue;
            }
            needsQuotes = true;
            break;
        }
        if (!needsQuotes) {
            return text;
        }
        return "\"" + text.replace("\"", "\\\"") + "\"";
    }

    /** One resolved command-path step: the command, the token index it matched at, and role access. */
    private record Resolution(ServiceRegistry command, int index, boolean accessible) {
    }

    /** One parsed input token with its absolute character span. */
    private record Token(String text, int start, int end) {
    }
}
