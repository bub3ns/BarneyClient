package moscow.rockstar.modules.visuals.effects.donations;

import net.minecraft.util.math.Vec3d;

public record DonationEffect(Vec3d position, long expiresAt) {
    public Vec3d getPosition() {
        return position;
    }

    public long getExpiresAt() {
        return expiresAt;
    }
}
