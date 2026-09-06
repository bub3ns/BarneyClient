/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.util.timing;

import moscow.rockstar.core.ClientAccess;

public class FrameScheduler
implements ClientAccess {
    private long lastTickTimeNanos = System.nanoTime();
    private int pendingTicks;
    private final boolean capCatchUp;
    private int targetTicksPerSecond = 0;
    private long tickIntervalNanos = 0L;

    public FrameScheduler(boolean bl) {
        this.capCatchUp = bl;
        this.pendingTicks = 0;
    }

    public void runPendingTicks(int n, FrameTickTask ... tasks) {
        if (this.targetTicksPerSecond != n) {
            this.tickIntervalNanos = 1000000000L / (long)n;
            this.targetTicksPerSecond = n;
        }
        long l = System.nanoTime();
        long l2 = l - this.lastTickTimeNanos;
        this.pendingTicks += (int)(l2 / this.tickIntervalNanos);
        this.lastTickTimeNanos += (long)this.pendingTicks * this.tickIntervalNanos;
        this.pendingTicks = Math.min(this.pendingTicks, this.capCatchUp ? Math.min(this.targetTicksPerSecond, minecraftClient.getCurrentFps()) : this.targetTicksPerSecond);
        while (this.pendingTicks > 0) {
            for (FrameTickTask task : tasks) {
                task.tick();
            }
            --this.pendingTicks;
        }
    }
}
