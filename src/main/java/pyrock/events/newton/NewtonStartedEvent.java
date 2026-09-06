/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.newton;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;

@ScreenController(description="newton_started")
public class NewtonStartedEvent
extends Event {
    private final String process;

    @Generated
    public String getProcess() {
        return this.process;
    }

    @Generated
    public NewtonStartedEvent(String string) {
        this.process = string;
    }
}

