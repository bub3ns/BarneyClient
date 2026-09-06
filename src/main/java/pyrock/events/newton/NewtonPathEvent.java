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

@ScreenController(description="newton_path")
public class NewtonPathEvent
extends Event {
    private final String process;
    private final int steps;

    @Generated
    public String getProcess() {
        return this.process;
    }

    @Generated
    public int getSteps() {
        return this.steps;
    }

    @Generated
    public NewtonPathEvent(String string, int n) {
        this.process = string;
        this.steps = n;
    }
}

