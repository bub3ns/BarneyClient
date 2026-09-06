package moscow.rockstar.util.timing;

/** A small named callback used by frame-rate-independent schedulers. */
@FunctionalInterface
public interface FrameTickTask {
    void tick();
}
