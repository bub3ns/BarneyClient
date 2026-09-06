package moscow.rockstar.api.commands;

import java.util.List;
import java.util.stream.Collectors;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.ModuleNotFoundException;

/** Fluent builder for one typed command argument. */
public final class CommandArgumentBuilder<T> {
    /**
     * Resolves a module by name, the way the original's shared validator does.
     *
     * <p>ORIGINAL: the {@code public static final IIiIIIII I} field on
     * {@code rockstar/ilIlil/IIIiiiii}.</p>
     */
    public static final CommandSuggestionProvider MODULE = value -> {
        try {
            ModuleContract module = RockstarClient.create().getModuleRegistry().findModuleByName(value);
            if (!module.isAvailable()) {
                return PluginResolver.resolveMessage("Module with name '%s' was not found".formatted(value));
            }
            return PluginResolver.resolveValue(module);
        } catch (ModuleNotFoundException exception) {
            return PluginResolver.resolveMessage("Module with name '%s' was not found".formatted(value));
        }
    };

    private final String name;
    private boolean required = true;
    private boolean vararg;
    private CommandSuggestionProvider validator = PluginResolver::resolveValue;
    private List<String> choices = List.of();

    private CommandArgumentBuilder(String name) {
        this.name = name;
    }

    public static <T> CommandArgumentBuilder<T> create(String name) {
        return new CommandArgumentBuilder<>(name);
    }

    public CommandArgumentBuilder<T> optional() {
        this.required = false;
        return this;
    }

    public CommandArgumentBuilder<T> vararg() {
        this.vararg = true;
        return this;
    }

    public CommandArgumentBuilder<T> validator(CommandSuggestionProvider validator) {
        this.validator = validator;
        return this;
    }

    public CommandArgumentBuilder<T> choices(String... choices) {
        this.choices = choices == null ? List.of() : List.of(choices);
        return this;
    }

    public CommandArgumentBuilder<T> choices(List<String> choices) {
        this.choices = choices == null ? List.of() : List.copyOf(choices);
        return this;
    }

    /**
     * Installs a validator that accepts only the listed values, and makes those values the
     * suggestion set. The accepted set is captured here, so a later {@link #choices} call narrows
     * the suggestions without narrowing what the validator accepts.
     */
    public CommandArgumentBuilder<T> choicesAndValidator(String... choices) {
        final List<String> allowed = choices == null ? List.of() : List.of(choices);
        final String expected = allowed.stream()
            .map(value -> "'" + value + "'")
            .collect(Collectors.joining(", "));
        this.validator = new CommandSuggestionProvider() {
            @Override
            public PluginResolver validate(String value) {
                for (String choice : allowed) {
                    if (choice.equalsIgnoreCase(value)) {
                        return PluginResolver.resolveValue(value);
                    }
                }
                return PluginResolver.resolveMessage("Expected one of: " + expected);
            }

            @Override
            public List<String> suggestions(String prefix) {
                return allowed.stream()
                    .filter(choice -> choice.toLowerCase().startsWith(prefix.toLowerCase()))
                    .toList();
            }
        };
        this.choices = allowed;
        return this;
    }

    public CommandDescriptor<T> build() {
        CommandSuggestionProvider resolvedValidator = this.validator == null
            ? PluginResolver::resolveValue
            : this.validator;
        if (this.choices != null && !this.choices.isEmpty()) {
            resolvedValidator = new ChoiceSuggestions(resolvedValidator, this.choices);
        }
        return new CommandDescriptor<>(this.name, this.required, this.vararg, resolvedValidator);
    }

    /**
     * Wraps a validator so the declared choices become its suggestion set. Validation is delegated
     * untouched: choices restrict what is offered, never what is accepted.
     */
    private static final class ChoiceSuggestions implements CommandSuggestionProvider {
        private final CommandSuggestionProvider delegate;
        private final List<String> choices;

        ChoiceSuggestions(CommandSuggestionProvider delegate, List<String> choices) {
            this.delegate = delegate;
            this.choices = choices;
        }

        @Override
        public PluginResolver validate(String value) {
            return this.delegate.validate(value);
        }

        @Override
        public List<String> suggestions(String prefix) {
            String lowerPrefix = prefix.toLowerCase();
            return this.choices.stream()
                .filter(choice -> choice.toLowerCase().startsWith(lowerPrefix))
                .toList();
        }
    }
}
