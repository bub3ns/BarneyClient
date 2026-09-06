package moscow.rockstar.api.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import moscow.rockstar.api.registry.ServiceRegistry;

/** Fluent builder corresponding to the original command-registration builder. */
public final class CommandBuilder {
    private final List<String> names = new ArrayList<>();
    private String description;
    private final List<CommandDescriptor<?>> arguments = new ArrayList<>();
    private final List<ServiceRegistry> children = new ArrayList<>();
    private RecordCommandHandler handler;

    private CommandBuilder(String name) {
        this.names.add(name);
    }

    public static CommandBuilder command(String name) {
        return new CommandBuilder(name);
    }

    public static CommandBuilder command(String name, Consumer<CommandBuilder> configure) {
        CommandBuilder builder = new CommandBuilder(name);
        configure.accept(builder);
        return builder;
    }

    public CommandBuilder aliases(String... aliases) {
        this.names.addAll(Arrays.asList(aliases));
        return this;
    }

    public CommandBuilder children(ServiceRegistry... children) {
        this.children.addAll(Arrays.asList(children));
        return this;
    }

    public CommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    public <T> CommandBuilder argument(String name, Consumer<CommandArgumentBuilder<T>> configure) {
        CommandArgumentBuilder<T> builder = CommandArgumentBuilder.create(name);
        configure.accept(builder);
        this.arguments.add(builder.build());
        return this;
    }

    public CommandBuilder handler(RecordCommandHandler handler) {
        this.handler = handler;
        return this;
    }

    public ServiceRegistry build() {
        if (this.handler == null) {
            throw new IllegalStateException("Executable command requires handler");
        }
        return new CommandDefinition(
            this.names,
            this.description,
            this.arguments,
            this.children,
            true,
            this.handler
        );
    }
}
