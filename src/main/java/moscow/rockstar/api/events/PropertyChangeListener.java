/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.events;

@FunctionalInterface
public interface PropertyChangeListener {
    public <T> void onChange(T var1, T var2);
}

