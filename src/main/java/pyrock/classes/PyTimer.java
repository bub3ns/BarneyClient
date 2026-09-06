/*
 * Decompiled with CFR 0.152.
 */
package pyrock.classes;

import moscow.rockstar.util.Timer;

public class PyTimer {
    private final Timer timer = new Timer();

    public PyTimer() {
        this.timer.reset();
    }

    public void reset() {
        this.timer.reset();
    }

    public boolean finished(long l) {
        return this.timer.hasElapsed(l);
    }

    public boolean passed(long l) {
        return this.timer.hasElapsed(l);
    }

    public long elapsed() {
        return this.timer.getElapsedMillis();
    }
}

