package moscow.rockstar.items.assist.sequence;

public interface SequenceAction {
    void start();

    boolean isComplete();

    void reset();
}
