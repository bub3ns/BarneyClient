package moscow.rockstar.modules.combat.targeting;

import java.util.List;

/** Exposes the position history maintained for an entity by the backtrack mixin. */
public interface BacktrackAccess {
    List<BacktrackPoint> rockstar2_0$getBackTracks();
}
