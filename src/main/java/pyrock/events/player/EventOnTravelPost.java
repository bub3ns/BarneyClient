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

@ScreenController(description="travel_post")
public class EventOnTravelPost
extends Event {
    private Vec3d oldVelocity;

    public EventOnTravelPost(Vec3d VanillaChestLootTableGenerator) {
        this.oldVelocity = VanillaChestLootTableGenerator;
    }

    public Vec3d getOldVelocity() {
        return this.oldVelocity;
    }

    public void setOldVelocity(Vec3d VanillaChestLootTableGenerator) {
        this.oldVelocity = VanillaChestLootTableGenerator;
    }
}

