package moscow.rockstar.api.access;

import moscow.rockstar.math.Rotation;

/** Callback used to transform one requested rotation into the next step. */
@FunctionalInterface
public interface RotationStepAccess {
    Rotation returnStep(Rotation current, Rotation target);
}
