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

public class AttackPlayerStrategy
implements BotBehaviorStrategy {
    private final String playerName;
    private final double attackRange;
    private long lastUpdateMillis;

    public AttackPlayerStrategy(String string, double d) {
        this.playerName = string;
        this.attackRange = d;
    }

    @Override
    public void applyBehavior(BotController botController) {
        PlayerEntity class_16572 = this.findPlayer();
        if (botController == null) {
            return;
        }
        if (class_16572 == null || class_16572.isRemoved()) {
            botController.applyMotionDamping();
            return;
        }
        Vec3d VanillaChestLootTableGenerator = class_16572.getPos();
        botController.lookAtPosition(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y + (double)class_16572.getStandingEyeHeight(), VanillaChestLootTableGenerator.z);
        if (botController.horizontalDistanceTo(VanillaChestLootTableGenerator) > this.attackRange) {
            botController.moveTowardPosition(VanillaChestLootTableGenerator, this.attackRange * 0.8);
            return;
        }
        botController.applyMotionDamping();
        long l = System.currentTimeMillis();
        if (l - this.lastUpdateMillis >= botController.getControlState().getAttackIntervalMillis()) {
            botController.attackEntity((Entity)class_16572);
            this.lastUpdateMillis = l;
        }
    }

    private PlayerEntity findPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || this.playerName == null || this.playerName.isBlank()) {
            return null;
        }
        for (PlayerEntity class_16572 : client.world.getPlayers()) {
            if (!class_16572.getName().getString().equalsIgnoreCase(this.playerName)) continue;
            return class_16572;
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "Attack " + this.playerName;
    }

    @Generated
    public String getPlayerName() {
        return this.playerName;
    }

    @Generated
    public double getAttackRange() {
        return this.attackRange;
    }

    @Generated
    public long getLastUpdateMillis() {
        return this.lastUpdateMillis;
    }
}
