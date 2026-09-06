package moscow.rockstar.combat.rotation;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Produces the short waypoint list used by the elytra steering controller.
 */
public final class FlightPathPlanner {
    private FlightPathPlanner() {
    }

    public static CompletableFuture<List<Vec3d>> plan(World world, Vec3d start, Vec3d destination) {
        if (start == null || destination == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.completedFuture(List.of(start, destination));
    }
}
