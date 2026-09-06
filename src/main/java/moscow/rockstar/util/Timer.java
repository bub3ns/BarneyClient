/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.util;

import lombok.Generated;

public class Timer {
    private long lastResetTimeMillis;

    public Timer() {
        this.reset();
    }

    public boolean hasElapsed(long l) {
        return System.currentTimeMillis() - this.lastResetTimeMillis >= l;
    }

    public void reset() {
        this.lastResetTimeMillis = System.currentTimeMillis();
    }

    public long getElapsedMillis() {
        return System.currentTimeMillis() - this.lastResetTimeMillis;
    }

    @Generated
    public long getLastResetTimeMillis() {
        return this.lastResetTimeMillis;
    }

    @Generated
    public void setLastResetTimeMillis(long l) {
        this.lastResetTimeMillis = l;
    }
}
