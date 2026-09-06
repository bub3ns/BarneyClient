/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import pyrock.events.EventCancellable;

@ScreenController(description="send_message")
public class SendMessageEvent
extends EventCancellable {
    private String message;

    @Generated
    public void setMessage(String string) {
        this.message = string;
    }

    @Generated
    public String getMessage() {
        return this.message;
    }

    @Generated
    public SendMessageEvent(String string) {
        this.message = string;
    }
}

