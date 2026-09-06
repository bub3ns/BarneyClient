package moscow.rockstar.items.assist.sequence;

import moscow.rockstar.util.Timer;

public final class DelayAction implements SequenceAction {
    private final Timer timer = new Timer();
    private final long durationMillis;

    public DelayAction(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    @Override
    public void start() {
        this.timer.reset();
    }

    @Override
    public boolean isComplete() {
        return this.timer.hasElapsed(this.durationMillis);
    }

    @Override
    public void reset() {
        this.timer.reset();
    }
}
