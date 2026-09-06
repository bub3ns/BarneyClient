/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3d
 *  net.minecraft.ClientPlayerEntity
 */
package pyrock.classes;

import java.util.List;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.network.ClientPlayerEntity;

public class PyPlayer {
    private final ClientPlayerEntity p;

    public PyPlayer(ClientPlayerEntity class_7462) {
        this.p = class_7462;
    }

    public double getX() {
        return this.p.getX();
    }

    public double getY() {
        return this.p.getY();
    }

    public double getZ() {
        return this.p.getZ();
    }

    public List<Double> pos() {
        return List.of(Double.valueOf(this.p.getX()), Double.valueOf(this.p.getY()), Double.valueOf(this.p.getZ()));
    }

    public float getYaw() {
        return this.p.getYaw();
    }

    public float getPitch() {
        return this.p.getPitch();
    }

    public void setYaw(double d) {
        this.p.setYaw((float)d);
    }

    public void setPitch(double d) {
        this.p.setPitch((float)d);
    }

    public List<Double> velocity() {
        Vec3d VanillaChestLootTableGenerator = this.p.getVelocity();
        return List.of(Double.valueOf(VanillaChestLootTableGenerator.x), Double.valueOf(VanillaChestLootTableGenerator.y), Double.valueOf(VanillaChestLootTableGenerator.z));
    }

    public void setVelocity(double d, double d2, double d3) {
        this.p.setVelocity(d, d2, d3);
    }

    public float getHealth() {
        return this.p.getHealth();
    }

    public float getMaxHealth() {
        return this.p.getMaxHealth();
    }

    public int getFood() {
        return this.p.getHungerManager().getFoodLevel();
    }

    public int getAir() {
        return this.p.getAir();
    }

    public boolean isOnGround() {
        return this.p.isOnGround();
    }

    public boolean isSneaking() {
        return this.p.isSneaking();
    }

    public boolean isSprinting() {
        return this.p.isSprinting();
    }

    public boolean isInWater() {
        return this.p.isTouchingWater();
    }

    public boolean isAlive() {
        return this.p.isAlive();
    }

    public boolean isFlying() {
        return this.p.getAbilities().flying;
    }

    public String getName() {
        return this.p.getName().getString();
    }

    public void sendChat(String string) {
        if (this.p.networkHandler != null) {
            this.p.networkHandler.sendChatMessage(string);
        }
    }
}

