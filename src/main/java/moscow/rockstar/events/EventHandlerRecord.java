/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.events;

import lombok.Generated;
import moscow.rockstar.events.Event;
import moscow.rockstar.modules.ModuleContract;

public class EventHandlerRecord
extends Event {
    private final ModuleContract module;

    @Generated
    public ModuleContract getModule() {
        return this.module;
    }

    @Generated
    public EventHandlerRecord(ModuleContract moduleContract) {
        this.module = moduleContract;
    }
}

