package moscow.rockstar.modules.combat.rotation;

import moscow.rockstar.math.Rotation;
import net.minecraft.util.math.Box;

public record AimSolution(Rotation rotation, Box targetBounds, boolean hitsTarget, int targetEntityId) {
    public Rotation getRotation() {
        return this.rotation;
    }

    public Box getTargetBounds() {
        return this.targetBounds;
    }

    public boolean hitsTarget() {
        return this.hitsTarget;
    }

    public int getTargetEntityId() {
        return this.targetEntityId;
    }
}
