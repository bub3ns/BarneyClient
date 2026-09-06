/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.commands;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.api.commands.CommandSuggestionProvider;

public final class CommandDescriptor<T> {
    private final String name;
    private final boolean required;
    private final boolean vararg;
    private final CommandSuggestionProvider validator;

    public CommandDescriptor(String string, boolean bl, boolean bl2, CommandSuggestionProvider commandSuggestionProvider) {
        this.name = string;
        this.required = bl;
        this.vararg = bl2;
        this.validator = commandSuggestionProvider;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "name", "required", "vararg", "validator");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "name", "required", "vararg", "validator");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "name", "required", "vararg", "validator");
    }

    public String getName() {
        return this.name;
    }

    public boolean isRequired() {
        return this.required;
    }

    public boolean isVararg() {
        return this.vararg;
    }

    public CommandSuggestionProvider getValidator() {
        return this.validator;
    }
}

