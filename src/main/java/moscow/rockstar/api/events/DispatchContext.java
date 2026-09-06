/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.events;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import moscow.rockstar.api.registry.ServiceRegistry;

public final class DispatchContext {
    private final ServiceRegistry command;
    private final List<Object> arguments;

    public DispatchContext(ServiceRegistry serviceRegistry, List<Object> list) {
        this.command = serviceRegistry;
        this.arguments = list;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "command", "arguments");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "command", "arguments");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "command", "arguments");
    }

    public ServiceRegistry getCommand() {
        return this.command;
    }

    public List<Object> getArguments() {
        return this.arguments;
    }
}

