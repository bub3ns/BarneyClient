/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Screen
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.client.gui.screen.Screen;

@ScreenController(description="close_screen")
public class CloseScreenEvent
extends Event {
    private final Screen screen;

    @Generated
    public Screen getScreen() {
        return this.screen;
    }

    @Generated
    public CloseScreenEvent(Screen class_4372) {
        this.screen = class_4372;
    }
}

