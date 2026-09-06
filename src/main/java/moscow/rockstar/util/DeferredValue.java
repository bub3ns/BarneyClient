package moscow.rockstar.util;

/** A value that may be deliberately absent, matching the original optional state holder. */
public final class DeferredValue<T> {
    private boolean present;
    private T value;

    public DeferredValue() {
    }

    public DeferredValue(T value) {
        set(value);
    }

    public boolean isPresent() {
        return present;
    }

    public void clear() {
        present = false;
        value = null;
    }

    public T get() {
        if (!present) {
            throw new IllegalStateException("Value is not set");
        }
        return value;
    }

    public void set(T value) {
        this.value = value;
        this.present = true;
    }
}
