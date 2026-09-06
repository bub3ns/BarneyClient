/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Vec3d
 */
package moscow.rockstar.math;

import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.math.MathUtils;
import net.minecraft.util.math.Vec3d;
import ua.mintantileak.spk.Compile;

public class Rotation
implements ClientAccess {
    public static final Rotation ZERO_ROTATION = new Rotation(0.0f, 0.0f);
    private float yaw;
    private float pitch;

    public Rotation(double d, double d2) {
        this.yaw = (float)d;
        this.pitch = (float)d2;
    }

    @Compile(obfuscation=1)
    public final Rotation differenceTo(Rotation rotation) {
        float f = MathUtils.wrapAngleDifference(this.yaw, rotation.yaw);
        float f2 = MathUtils.wrapAngleDifference(this.pitch, rotation.pitch);
        return new Rotation(f, f2);
    }

    @Compile(obfuscation=1)
    public final float angleDistanceTo(Rotation rotation) {
        float f = MathUtils.wrapAngleDifference(this.yaw, rotation.yaw);
        float f2 = MathUtils.wrapAngleDifference(this.pitch, rotation.pitch);
        return Math.abs(f) + Math.abs(f2);
    }

    @Compile(obfuscation=1)
    public final Vec3d toDirectionVector() {
        return Rotation.minecraftClient.player.getRotationVector(this.pitch, this.yaw);
    }

    @Compile(obfuscation=1)
    public final Rotation offset(float f, float f2) {
        return new Rotation(this.yaw + f, this.pitch + f2);
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public float getPitch() {
        return this.pitch;
    }

    @Generated
    public void setYaw(float f) {
        this.yaw = f;
    }

    @Generated
    public void setPitch(float f) {
        this.pitch = f;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Rotation)) {
            return false;
        }
        Rotation rotation = (Rotation)object;
        if (!rotation.matches(this)) {
            return false;
        }
        if (Float.compare(this.getYaw(), rotation.getYaw()) != 0) {
            return false;
        }
        return Float.compare(this.getPitch(), rotation.getPitch()) == 0;
    }

    @Generated
    protected boolean matches(Object object) {
        return object instanceof Rotation;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        n2 = n2 * 59 + Float.floatToIntBits(this.getYaw());
        n2 = n2 * 59 + Float.floatToIntBits(this.getPitch());
        return n2;
    }

    @Generated
    public String toString() {
        return "Rotation(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
    }

    @Generated
    public Rotation(float f, float f2) {
        this.yaw = f;
        this.pitch = f2;
    }
}

