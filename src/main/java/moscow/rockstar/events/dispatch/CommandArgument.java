/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.events.dispatch;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.api.plugins.PluginEntry;

public final class CommandArgument
implements PluginEntry {
    private final String processName;
    private final int totalSteps;

    public CommandArgument(String string, int n) {
        this.processName = string;
        this.totalSteps = n;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "processName", "totalSteps");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "processName", "totalSteps");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "processName", "totalSteps");
    }

    public String getProcessName() {
        return this.processName;
    }

    public int getTotalSteps() {
        return this.totalSteps;
    }
}

