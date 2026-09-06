/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Vec3d
 */
package moscow.rockstar.network.bot;

import lombok.Generated;
import moscow.rockstar.network.bot.BotBehaviorStrategy;
import moscow.rockstar.network.bot.BotController;
import net.minecraft.util.math.Vec3d;

public class PositionNavigationStrategy
implements BotBehaviorStrategy {
    private final Vec3d position;
    private final double distance;

    public PositionNavigationStrategy(Vec3d VanillaChestLootTableGenerator, double d) {
        this.position = VanillaChestLootTableGenerator;
        this.distance = d;
    }

    public PositionNavigationStrategy(double d, double d2, double d3) {
        this(new Vec3d(d + 0.5, d2, d3 + 0.5), 0.35);
    }

    @Override
    public void applyBehavior(BotController botController) {
        if (botController == null || this.position == null) {
            return;
        }
        if (botController.distanceTo(this.position) <= this.distance) {
            botController.setBehaviorStrategy(new IdleBotBehaviorStrategy());
            return;
        }
        botController.moveTowardPosition(this.position, this.distance);
    }

    @Override
    public String getDescription() {
        return "Goto " + Math.round(this.position.x) + " " + Math.round(this.position.y) + " " + Math.round(this.position.z);
    }

    @Generated
    public Vec3d getPosition() {
        return this.position;
    }

    @Generated
    public double getDistance() {
        return this.distance;
    }
}
