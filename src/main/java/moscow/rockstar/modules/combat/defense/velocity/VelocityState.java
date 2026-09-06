package moscow.rockstar.modules.combat.defense.velocity;

import net.minecraft.util.math.Vec3d;

final class VelocityState {
    Vec3d position = Vec3d.ZERO;
    int compensationTicksRemaining;
    private int grimHitCount;

    VelocityState() {
    }

    void reset() {
        this.position = Vec3d.ZERO;
        this.compensationTicksRemaining = 0;
        this.grimHitCount = 0;
    }

    public Vec3d getPosition() {
        return this.position;
    }

    public int getCompensationTicksRemaining() {
        return this.compensationTicksRemaining;
    }

    public int getGrimHitCount() {
        return this.grimHitCount;
    }

    public void setPosition(Vec3d position) {
        this.position = position;
    }

    public void setCompensationTicksRemaining(int ticks) {
        this.compensationTicksRemaining = ticks;
    }

    public void setGrimHitCount(int hitCount) {
        this.grimHitCount = hitCount;
    }
}
