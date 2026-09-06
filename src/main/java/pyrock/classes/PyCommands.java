/*
 * Decompiled with CFR 0.152.
 */
package pyrock.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import moscow.rockstar.api.commands.ClientCommandDispatcher;
import moscow.rockstar.api.commands.NavigationCommandService;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.RockstarClient;
import pyrock.classes.PyCommand;

public class PyCommands {
    private final List<PyCommand> mine = new ArrayList<PyCommand>();

    public PyCommand create(String string) {
        PyCommand pyCommand = new PyCommand(string);
        this.mine.add(pyCommand);
        return pyCommand;
    }

    public boolean exists(String string) {
        if (string == null || string.isBlank()) {
            return false;
        }
        for (ServiceRegistry serviceRegistry : ClientCommandDispatcher.getRegisteredCommands()) {
            for (String string2 : serviceRegistry.getRegisteredServices()) {
                if (!string2.equalsIgnoreCase(string)) continue;
                return true;
            }
        }
        return false;
    }

    public List<String> names() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (ServiceRegistry serviceRegistry : ClientCommandDispatcher.getRegisteredCommands()) {
            if (serviceRegistry.getRegisteredServices().isEmpty()) continue;
            arrayList.add(serviceRegistry.getRegisteredServices().getFirst());
        }
        return arrayList;
    }

    public boolean remove(String string) {
        if (string == null) {
            return false;
        }
        String string2 = string.toLowerCase(Locale.ROOT);
        for (PyCommand pyCommand : new ArrayList<PyCommand>(this.mine)) {
            if (!pyCommand.name().equals(string2)) continue;
            this.mine.remove(pyCommand);
            return pyCommand.remove();
        }
        return false;
    }

    public boolean run(String string) {
        if (string == null || string.isBlank()) {
            return false;
        }
        String string2 = PyCommands.registry().getCommandPrefix();
        String string3 = string.startsWith(string2) ? string : string2 + string;
        try {
            return PyCommands.registry().executeCommand(string3);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public String prefix() {
        return PyCommands.registry().getCommandPrefix();
    }

    private static NavigationCommandService registry() {
        return RockstarClient.create().getNavigationCommandService();
    }
}
