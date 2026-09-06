package moscow.rockstar.network.http;

import java.util.concurrent.TimeUnit;

/** Decision returned by a retry-after policy. */
public final class RetryDecision {
    public static final RetryDecision DO_NOT_RETRY = new RetryDecision(false, 0L);

    private final boolean scheduled;
    private final long delayMillis;

    private RetryDecision(boolean scheduled, long delayMillis) {
        this.scheduled = scheduled;
        this.delayMillis = delayMillis;
    }

    public static RetryDecision afterMillis(long delayMillis) {
        return after(delayMillis, TimeUnit.MILLISECONDS);
    }

    public static RetryDecision after(long delay, TimeUnit unit) {
        return new RetryDecision(true, unit.toMillis(delay));
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public void await() throws InterruptedException {
        Thread.sleep(delayMillis);
    }
}
