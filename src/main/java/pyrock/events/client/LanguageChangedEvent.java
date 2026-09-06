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

@ScreenController(description="language_changed")
public class LanguageChangedEvent
extends Event {
    private final String code;

    @Generated
    public String getCode() {
        return this.code;
    }

    @Generated
    public LanguageChangedEvent(String string) {
        this.code = string;
    }
}

