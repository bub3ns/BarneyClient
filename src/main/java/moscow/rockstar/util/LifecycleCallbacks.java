/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.util;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;

public final class LifecycleCallbacks {
    private final Runnable enterAction;
    private final Runnable tabAction;

    public LifecycleCallbacks(Runnable runnable, Runnable runnable2) {
        this.enterAction = runnable;
        this.tabAction = runnable2;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "enterAction", "tabAction");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "enterAction", "tabAction");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "enterAction", "tabAction");
    }

    public Runnable getEnterAction() {
        return this.enterAction;
    }

    public Runnable getTabAction() {
        return this.tabAction;
    }
}

