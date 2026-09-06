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

@ScreenController(description="newton_failed")
public class NewtonFailedEvent
extends Event {
    private final String process;
    private final String reason;

    @Generated
    public String getProcess() {
        return this.process;
    }

    @Generated
    public String getReason() {
        return this.reason;
    }

    @Generated
    public NewtonFailedEvent(String string, String string2) {
        this.process = string;
        this.reason = string2;
    }
}

