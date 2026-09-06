/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.layout;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;

public final class ComponentGroup {
    private final boolean valid;
    private final List<String> errors;
    public static final ComponentGroup group = new ComponentGroup(true, null);

    public ComponentGroup(boolean bl, List<String> list) {
        this.valid = bl;
        this.errors = list;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "valid", "errors");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "valid", "errors");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "valid", "errors");
    }

    public boolean isValid() {
        return this.valid;
    }

    public List<String> getErrors() {
        return this.errors;
    }
}

