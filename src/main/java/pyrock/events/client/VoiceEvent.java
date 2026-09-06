/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.client;

import lombok.Generated;
import moscow.rockstar.events.Event;

public class VoiceEvent
extends Event {
    private final String text;

    @Generated
    public String getText() {
        return this.text;
    }

    @Generated
    public VoiceEvent(String string) {
        this.text = string;
    }
}

