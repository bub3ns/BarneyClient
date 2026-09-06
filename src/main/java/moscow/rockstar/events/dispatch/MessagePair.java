/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.events.dispatch;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.api.plugins.PluginEntry;

public final class MessagePair
implements PluginEntry {
    private final String processName;
    private final String reason;

    public MessagePair(String string, String string2) {
        this.processName = string;
        this.reason = string2;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "processName", "reason");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "processName", "reason");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "processName", "reason");
    }

    public String getProcessName() {
        return this.processName;
    }

    public String getReason() {
        return this.reason;
    }
}

