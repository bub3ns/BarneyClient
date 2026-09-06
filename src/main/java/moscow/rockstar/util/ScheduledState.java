/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.util;

import java.util.concurrent.TimeUnit;
import lombok.Generated;

public class ScheduledState {
    public static final ScheduledState UNSCHEDULED = new ScheduledState(false, 0L);
    private final boolean scheduled;
    private final long delayMillis;

    public static ScheduledState afterMillis(long l) {
        return ScheduledState.after(l, TimeUnit.MILLISECONDS);
    }

    public static ScheduledState after(long l, TimeUnit timeUnit) {
        return new ScheduledState(true, timeUnit.toMillis(l));
    }

    public void await() throws InterruptedException {
        Thread.sleep(this.delayMillis);
    }

    @Generated
    public boolean isScheduled() {
        return this.scheduled;
    }

    @Generated
    public long getDelayMillis() {
        return this.delayMillis;
    }

    @Generated
    private ScheduledState(boolean bl, long l) {
        this.scheduled = bl;
        this.delayMillis = l;
    }
}

