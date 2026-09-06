/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.SoundInstance
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.client.sound.SoundInstance;

@ScreenController(description="sound")
public class SoundEvent
extends Event {
    public SoundInstance sound;

    public SoundEvent(SoundInstance class_11132) {
        this.sound = class_11132;
    }

    @Generated
    public SoundInstance getSound() {
        return this.sound;
    }
}

