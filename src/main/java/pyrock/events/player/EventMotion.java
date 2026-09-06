/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.MathHelper
 */
package pyrock.events.player;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.core.ClientAccess;
import net.minecraft.util.math.MathHelper;
import pyrock.events.EventCancellable;

@ScreenController(description="motion")
public class EventMotion
extends EventCancellable
implements ClientAccess {
    public static float LAST_YAW;
    public static float LAST_PITCH;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;
    private boolean isSneaking;
    private boolean isSprinting;

    public EventMotion(double d, double d2, double d3, float f, float f2, boolean bl, boolean bl2, boolean bl3) {
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.yaw = f;
        this.pitch = MathHelper.clamp((float)f2, (float)-90.0f, (float)90.0f);
        this.onGround = bl;
        this.isSneaking = bl2;
        this.isSprinting = bl3;
    }

    public EventMotion(float f, float f2, boolean bl) {
        this(EventMotion.minecraftClient.player == null ? 0.0 : EventMotion.minecraftClient.player.getX(), EventMotion.minecraftClient.player == null ? 0.0 : EventMotion.minecraftClient.player.getY(), EventMotion.minecraftClient.player == null ? 0.0 : EventMotion.minecraftClient.player.getZ(), f, f2, bl, EventMotion.minecraftClient.player != null && EventMotion.minecraftClient.player.isSneaking(), EventMotion.minecraftClient.player != null && EventMotion.minecraftClient.player.isSprinting());
    }

    public void setX(double d) {
        this.x = d;
    }

    public void setY(double d) {
        this.y = d;
    }

    public void setZ(double d) {
        this.z = d;
    }

    public void setYaw(float f) {
        this.yaw = f;
        if (EventMotion.minecraftClient.player != null) {
            EventMotion.minecraftClient.player.setHeadYaw(f);
            EventMotion.minecraftClient.player.setBodyYaw(f);
        }
    }

    public void setPitch(float f) {
        this.pitch = MathHelper.clamp((float)f, (float)-90.0f, (float)90.0f);
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void setOnGround(boolean bl) {
        this.onGround = bl;
    }

    public boolean isGround() {
        return this.onGround;
    }

    public void setGround(boolean bl) {
        this.setOnGround(bl);
    }

    public boolean isSneaking() {
        return this.isSneaking;
    }

    public void setSneaking(boolean bl) {
        this.isSneaking = bl;
    }

    public boolean isSprinting() {
        return this.isSprinting;
    }

    public void setSprinting(boolean bl) {
        this.isSprinting = bl;
    }

    public void markSent() {
        LAST_YAW = this.yaw;
        LAST_PITCH = this.pitch;
    }

    @Generated
    public double getX() {
        return this.x;
    }

    @Generated
    public double getY() {
        return this.y;
    }

    @Generated
    public double getZ() {
        return this.z;
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public float getPitch() {
        return this.pitch;
    }
}

