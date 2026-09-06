/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Identifier
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.util.Identifier;

@ScreenController(description="set_cooldown")
public class EventSetCooldown
extends Event {
    private int cooldown;
    private Identifier cooldownGroup;

    @Generated
    public int getCooldown() {
        return this.cooldown;
    }

    @Generated
    public Identifier getCooldownGroup() {
        return this.cooldownGroup;
    }

    @Generated
    public void setCooldown(int n) {
        this.cooldown = n;
    }

    @Generated
    public void setCooldownGroup(Identifier class_29602) {
        this.cooldownGroup = class_29602;
    }

    @Generated
    public EventSetCooldown(int n, Identifier class_29602) {
        this.cooldown = n;
        this.cooldownGroup = class_29602;
    }
}

