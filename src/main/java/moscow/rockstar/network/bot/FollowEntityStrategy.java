/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Vec3d
 */
package moscow.rockstar.network.bot;

import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.network.bot.BotBehaviorStrategy;
import moscow.rockstar.network.bot.BotController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class FollowEntityStrategy
implements BotBehaviorStrategy {
    private final String targetName;
    private final Entity entity;
    private final double maxDistance;

    public FollowEntityStrategy(String string, double d) {
        this.targetName = string;
        this.entity = null;
        this.maxDistance = d;
    }

    public FollowEntityStrategy(Entity class_12972, double d) {
        this.targetName = class_12972 == null ? null : class_12972.getName().getString();
        this.entity = class_12972;
        this.maxDistance = d;
    }

    @Override
    public void applyBehavior(BotController botController) {
        Entity class_12972 = this.resolveEntity();
        if (botController == null) {
            return;
        }
        if (class_12972 == null || class_12972.isRemoved()) {
            botController.applyMotionDamping();
            return;
        }
        Vec3d VanillaChestLootTableGenerator = class_12972.getPos();
        botController.lookAtPosition(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y + (double)class_12972.getStandingEyeHeight(), VanillaChestLootTableGenerator.z);
        if (botController.horizontalDistanceTo(VanillaChestLootTableGenerator) > this.maxDistance) {
            botController.moveTowardPosition(VanillaChestLootTableGenerator, this.maxDistance);
        } else {
            botController.applyMotionDamping();
        }
    }

    private Entity resolveEntity() {
        if (this.entity != null && !this.entity.isRemoved()) {
            return this.entity;
        }
        if (this.targetName == null || this.targetName.isBlank()) {
            return null;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return null;
        }
        for (PlayerEntity class_16572 : client.world.getPlayers()) {
            if (!class_16572.getName().getString().equalsIgnoreCase(this.targetName)) continue;
            return class_16572;
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "Follow " + (this.targetName == null ? "target" : this.targetName);
    }

    @Generated
    public String getTargetName() {
        return this.targetName;
    }

    @Generated
    public Entity getEntity() {
        return this.entity;
    }

    @Generated
    public double getMaxDistance() {
        return this.maxDistance;
    }
}
