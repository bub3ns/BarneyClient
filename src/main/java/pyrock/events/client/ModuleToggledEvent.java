/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.client;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import pyrock.classes.PyModule;

@ScreenController(description="module_toggled")
public class ModuleToggledEvent
extends Event {
    private final PyModule module;
    private final boolean state;

    @Generated
    public PyModule getModule() {
        return this.module;
    }

    @Generated
    public boolean isState() {
        return this.state;
    }

    @Generated
    public ModuleToggledEvent(PyModule pyModule, boolean bl) {
        this.module = pyModule;
        this.state = bl;
    }
}

