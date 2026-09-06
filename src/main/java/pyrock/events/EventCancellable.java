/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events;

import lombok.Generated;
import moscow.rockstar.events.Event;

public class EventCancellable
extends Event {
    private boolean cancelled;

    public final void cancel() {
        this.cancelled = true;
    }

    @Generated
    public boolean isCancelled() {
        return this.cancelled;
    }
}

