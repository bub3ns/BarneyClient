package moscow.rockstar.ui.state;

/** Animates a long single-line label back and forth while it is hovered. */
public final class TextScrollState {
    private float offset;
    private long lastUpdateMillis = System.currentTimeMillis();
    private long pauseUntilMillis;
    private boolean movingForward;

    public float update(float textWidth, float visibleWidth, boolean active) {
        long now = System.currentTimeMillis();
        float elapsedSeconds = (now - this.lastUpdateMillis) / 1000.0f;
        this.lastUpdateMillis = now;
        float maximumOffset = Math.max(0.0f, textWidth - Math.max(1.0f, visibleWidth));
        if (maximumOffset <= 0.0f) {
            this.offset = 0.0f;
            this.movingForward = true;
            this.pauseUntilMillis = now;
        } else if (active) {
            this.offset = Math.min(this.offset, maximumOffset);
            if (now >= this.pauseUntilMillis) {
                float distance = elapsedSeconds * 35.0f;
                if (this.movingForward) {
                    this.offset = Math.min(maximumOffset, this.offset + distance);
                    if (this.offset >= maximumOffset) {
                        this.movingForward = false;
                        this.pauseUntilMillis = now + 600L;
                    }
                } else {
                    this.offset = Math.max(0.0f, this.offset - distance);
                    if (this.offset <= 0.0f) {
                        this.movingForward = true;
                        this.pauseUntilMillis = now + 600L;
                    }
                }
            }
        } else if (this.offset > 0.0f) {
            this.offset = Math.max(0.0f, this.offset - elapsedSeconds * 35.0f);
            if (this.offset == 0.0f) {
                this.movingForward = true;
                this.pauseUntilMillis = now;
            }
        }
        return this.offset;
    }

    public void reset() {
        this.offset = 0.0f;
        this.movingForward = true;
        this.pauseUntilMillis = 0L;
        this.lastUpdateMillis = System.currentTimeMillis();
    }
}
