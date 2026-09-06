/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 */
package pyrock.events;

import jep.python.PyCallable;
import pyrock.events.PyEvents;

public class PyEvent {
    private final String eventName;
    private final PyEvents events;

    public PyEvent(String string, PyEvents pyEvents) {
        this.eventName = string;
        this.events = pyEvents;
    }

    public void on(PyCallable pyCallable) {
        this.events.register(this.eventName, pyCallable);
    }

    public void set(PyCallable pyCallable) {
        this.events.register(this.eventName, pyCallable);
    }
}

