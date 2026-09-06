package moscow.rockstar.api.commands;

import java.util.List;
import moscow.rockstar.api.registry.ServiceRegistry;

/** Immutable command registration produced by {@link CommandBuilder}. */
public final class CommandDefinition implements ServiceRegistry {
    private final List<String> registeredServices;
    private final String registryName;
    private final List<CommandDescriptor<?>> serviceAliases;
    private final List<ServiceRegistry> serviceDependencies;
    private final boolean available;
    private final RecordCommandHandler commandHandler;

    public CommandDefinition(
        List<String> registeredServices,
        String registryName,
        List<CommandDescriptor<?>> serviceAliases,
        List<ServiceRegistry> serviceDependencies,
        boolean available,
        RecordCommandHandler commandHandler
    ) {
        this.registeredServices = List.copyOf(registeredServices);
        this.registryName = registryName;
        this.serviceAliases = List.copyOf(serviceAliases);
        this.serviceDependencies = List.copyOf(serviceDependencies);
        this.available = available;
        this.commandHandler = commandHandler;
    }

    @Override
    public List<String> getRegisteredServices() {
        return this.registeredServices;
    }

    @Override
    public String getRegistryName() {
        return this.registryName;
    }

    @Override
    public List<CommandDescriptor<?>> getServiceAliases() {
        return this.serviceAliases;
    }

    @Override
    public List<ServiceRegistry> getServiceDependencies() {
        return this.serviceDependencies;
    }

    @Override
    public boolean isAvailable() {
        return this.available;
    }

    @Override
    public RecordCommandHandler getCommandHandler() {
        return this.commandHandler;
    }
}
