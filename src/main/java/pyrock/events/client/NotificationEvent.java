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

@ScreenController(description="notification")
public class NotificationEvent
extends Event {
    private final String style;
    private final String type;
    private final String title;
    private final String text;

    @Generated
    public String getStyle() {
        return this.style;
    }

    @Generated
    public String getType() {
        return this.type;
    }

    @Generated
    public String getTitle() {
        return this.title;
    }

    @Generated
    public String getText() {
        return this.text;
    }

    @Generated
    public NotificationEvent(String string, String string2, String string3, String string4) {
        this.style = string;
        this.type = string2;
        this.title = string3;
        this.text = string4;
    }
}

