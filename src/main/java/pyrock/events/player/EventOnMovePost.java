/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3d
 */
package pyrock.events.player;

import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.util.math.Vec3d;

@ScreenController(description="move_post")
public class EventOnMovePost
extends Event {
    private final float speed;
    private final Vec3d movementInput;

    public EventOnMovePost(float f, Vec3d VanillaChestLootTableGenerator) {
        this.speed = f;
        this.movementInput = VanillaChestLootTableGenerator;
    }

    public float getSpeed() {
        return this.speed;
    }

    public Vec3d getMovementInput() {
        return this.movementInput;
    }
}

