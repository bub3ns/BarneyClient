package moscow.rockstar.api.access;

/** Runtime access to movement state added to the client player mixin. */
public interface PlayerEntityAccess {
    int rockstar$getOnGroundTicks();

    void rockstar$syncSprinting();
}
