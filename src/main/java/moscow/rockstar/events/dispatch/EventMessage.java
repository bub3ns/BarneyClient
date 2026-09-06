/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.events.dispatch;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.api.plugins.PluginEntry;

public final class EventMessage
implements PluginEntry {
    private final String processName;

    public EventMessage(String string) {
        this.processName = string;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "processName");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "processName");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "processName");
    }

    public String getProcessName() {
        return this.processName;
    }
}

