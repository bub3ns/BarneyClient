/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.events;

import moscow.rockstar.api.events.PropertyChangeListener;

@FunctionalInterface
public interface PropertyChangeDispatcher
extends PropertyChangeListener {
    public void onPropertyChanged();

    @Override
    default public <T> void onChange(T t, T t2) {
        this.onPropertyChanged();
    }
}

