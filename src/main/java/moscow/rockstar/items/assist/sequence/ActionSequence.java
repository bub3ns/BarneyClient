package moscow.rockstar.items.assist.sequence;

import java.util.ArrayList;
import java.util.List;

/** The ordered action runner used by delayed Assist items. */
public final class ActionSequence {
    private final List<SequenceAction> actions = new ArrayList<>();
    private int currentIndex;
    private boolean running;
    private boolean currentStarted;

    public ActionSequence addAction(SequenceAction action) {
        this.actions.add(action);
        return this;
    }

    public void start() {
        if (this.actions.isEmpty()) {
            return;
        }
        this.running = true;
        this.currentIndex = 0;
        this.currentStarted = false;
        for (SequenceAction action : this.actions) {
            action.reset();
        }
        this.actions.get(0).start();
        this.currentStarted = true;
    }

    public void tick() {
        if (!this.running || this.actions.isEmpty()) {
            return;
        }
        SequenceAction action = this.actions.get(this.currentIndex);
        if (!this.currentStarted) {
            action.start();
            this.currentStarted = true;
        }
        if (action.isComplete()) {
            ++this.currentIndex;
            this.currentStarted = false;
            if (this.currentIndex >= this.actions.size()) {
                this.stop();
                return;
            }
            this.actions.get(this.currentIndex).start();
            this.currentStarted = true;
        }
    }

    public void stop() {
        this.running = false;
        this.currentIndex = 0;
        this.currentStarted = false;
        this.actions.clear();
    }

    public boolean isRunning() {
        return this.running;
    }
}
