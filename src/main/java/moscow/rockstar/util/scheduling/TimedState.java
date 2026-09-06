/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.util.scheduling;

public interface TimedState {
    public long getExpiresAtMillis();

    default public boolean isPrimitiveValue() {
        return this.getExpiresAtMillis() <= System.currentTimeMillis();
    }
}

