/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.events;

import moscow.rockstar.events.Event;

public interface EventListener<T extends Event> {
    public void onEvent(T var1);

    default public int getPriority() {
        return 0;
    }

    public static <T extends Event> EventListener<T> withPriority(final int n, final EventListener<T> eventListener) {
        return new EventListener<T>(){

            @Override
            public void onEvent(T t) {
                eventListener.onEvent(t);
            }

            @Override
            public int getPriority() {
                return n;
            }
        };
    }
}

