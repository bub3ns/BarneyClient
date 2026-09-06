/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.events.dispatch;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.api.plugins.PluginEntry;

public final class EventStatistics
implements PluginEntry {
    private final String processName;
    private final int x;
    private final int y;
    private final int z;
    private final int stepIndex;
    private final int totalSteps;

    public EventStatistics(String string, int n, int n2, int n3, int n4, int n5) {
        this.processName = string;
        this.x = n;
        this.y = n2;
        this.z = n3;
        this.stepIndex = n4;
        this.totalSteps = n5;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "processName", "x", "y", "z", "stepIndex", "totalSteps");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "processName", "x", "y", "z", "stepIndex", "totalSteps");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "processName", "x", "y", "z", "stepIndex", "totalSteps");
    }

    public String getProcessName() {
        return this.processName;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public int getStepIndex() {
        return this.stepIndex;
    }

    public int getTotalSteps() {
        return this.totalSteps;
    }
}

