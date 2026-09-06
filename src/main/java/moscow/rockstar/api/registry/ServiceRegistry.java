/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.registry;

import java.util.List;
import moscow.rockstar.api.commands.CommandDescriptor;
import moscow.rockstar.api.commands.RecordCommandHandler;

public interface ServiceRegistry {
    public List<String> getRegisteredServices();

    public String getRegistryName();

    public List<CommandDescriptor<?>> getServiceAliases();

    public List<ServiceRegistry> getServiceDependencies();

    public boolean isAvailable();

    public RecordCommandHandler getCommandHandler();
}
