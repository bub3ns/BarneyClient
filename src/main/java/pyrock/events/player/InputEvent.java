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
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.Event;
import net.minecraft.util.math.MathHelper;

@ScreenController(description="input")
public class InputEvent
extends Event
implements ClientAccess {
    private float forward;
    private float strafe;
    private boolean jump;
    private boolean sneak;
    private boolean sprint;
    private double sneakSlowDownMultiplier;

    public InputEvent(float f, float f2, boolean bl, boolean bl2, boolean bl3) {
        this.forward = f;
        this.strafe = f2;
        this.jump = bl;
        this.sneak = bl2;
        this.sprint = bl3;
        this.sneakSlowDownMultiplier = 0.3;
    }

    public void setYaw(float f, float f2) {
        float f3 = this.getForward();
        float f4 = this.getStrafe();
        double d = MathHelper.wrapDegrees((double)Math.toDegrees(EntityUtils.getDirectionRadians(f2, f3, f4)));
        if (f3 == 0.0f && f4 == 0.0f) {
            return;
        }
        float f5 = 0.0f;
        float f6 = 0.0f;
        float f7 = Float.MAX_VALUE;
        for (float f8 = -1.0f; f8 <= 1.0f; f8 += 1.0f) {
            for (float f9 = -1.0f; f9 <= 1.0f; f9 += 1.0f) {
                double d2;
                double d3;
                if (f9 == 0.0f && f8 == 0.0f || !((d3 = Math.abs(d - (d2 = MathHelper.wrapDegrees((double)Math.toDegrees(EntityUtils.getDirectionRadians(f, f8, f9)))))) < (double)f7)) continue;
                f7 = (float)d3;
                f5 = f8;
                f6 = f9;
            }
        }
        this.setForward(f5);
        this.setStrafe(f6);
    }

    public void setYaw(float f) {
        if (InputEvent.minecraftClient.player == null) {
            return;
        }
        this.setYaw(f, InputEvent.minecraftClient.player.getYaw());
    }

    public void setYawSmooth(float f, float f2) {
        float f3 = this.getForward();
        float f4 = this.getStrafe();
        if (f3 == 0.0f && f4 == 0.0f) {
            return;
        }
        double d = Math.toRadians(f2);
        double d2 = Math.sin(d);
        double d3 = Math.cos(d);
        double d4 = (double)f4 * d3 - (double)f3 * d2;
        double d5 = (double)f3 * d3 + (double)f4 * d2;
        double d6 = Math.toRadians(f);
        double d7 = Math.sin(d6);
        double d8 = Math.cos(d6);
        double d9 = d5 * d8 - d4 * d7;
        double d10 = d4 * d8 + d5 * d7;
        double d11 = Math.max(Math.abs(d9), Math.abs(d10));
        if (d11 > 1.0) {
            d9 /= d11;
            d10 /= d11;
        }
        this.setForward((float)d9);
        this.setStrafe((float)d10);
    }

    public void setYawSmooth(float f) {
        if (InputEvent.minecraftClient.player == null) {
            return;
        }
        this.setYawSmooth(f, InputEvent.minecraftClient.player.getYaw());
    }

    @Generated
    public float getForward() {
        return this.forward;
    }

    @Generated
    public float getStrafe() {
        return this.strafe;
    }

    @Generated
    public boolean isJump() {
        return this.jump;
    }

    @Generated
    public boolean isSneak() {
        return this.sneak;
    }

    @Generated
    public boolean isSprint() {
        return this.sprint;
    }

    @Generated
    public double getSneakSlowDownMultiplier() {
        return this.sneakSlowDownMultiplier;
    }

    @Generated
    public void setForward(float f) {
        this.forward = f;
    }

    @Generated
    public void setStrafe(float f) {
        this.strafe = f;
    }

    @Generated
    public void setJump(boolean bl) {
        this.jump = bl;
    }

    @Generated
    public void setSneak(boolean bl) {
        this.sneak = bl;
    }

    @Generated
    public void setSprint(boolean bl) {
        this.sprint = bl;
    }

    @Generated
    public void setSneakSlowDownMultiplier(double d) {
        this.sneakSlowDownMultiplier = d;
    }
}

