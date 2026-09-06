package moscow.rockstar.modules.movement.speed;

import moscow.rockstar.util.Timer;
import net.minecraft.util.math.Vec3d;

final class SpeedState {
    private final Timer stateTimer = new Timer();
    private final Vec3d position;

    SpeedState(Vec3d position) {
        this.position = position;
    }
}
