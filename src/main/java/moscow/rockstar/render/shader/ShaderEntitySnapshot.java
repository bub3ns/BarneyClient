package moscow.rockstar.render.shader;

import java.util.UUID;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public record ShaderEntitySnapshot(int id, UUID uuid, String typeId, Vec3d position,
                                   float yaw, float pitch, float headYaw,
                                   double velocityX, double velocityY, double velocityZ,
                                   long lastSeenTick) {
    ShaderEntitySnapshot withPositionAndRotation(Vec3d position, float yaw, float pitch,
                                                  float headYaw, long lastSeenTick) {
        return new ShaderEntitySnapshot(id, uuid, typeId, position,
            MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90.0f, 90.0f),
            headYaw, velocityX, velocityY, velocityZ, lastSeenTick);
    }

    ShaderEntitySnapshot withVelocity(double velocityX, double velocityY, double velocityZ,
                                      long lastSeenTick) {
        return new ShaderEntitySnapshot(id, uuid, typeId, position, yaw, pitch, headYaw,
            velocityX, velocityY, velocityZ, lastSeenTick);
    }

    ShaderEntitySnapshot withHeadYaw(float headYaw, long lastSeenTick) {
        return new ShaderEntitySnapshot(id, uuid, typeId, position, yaw, pitch,
            MathHelper.wrapDegrees(headYaw), velocityX, velocityY, velocityZ, lastSeenTick);
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getTypeId() {
        return typeId;
    }

    public Vec3d getPosition() {
        return position;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getHeadYaw() {
        return headYaw;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public double getVelocityZ() {
        return velocityZ;
    }

    public long getLastSeenTick() {
        return lastSeenTick;
    }
}
