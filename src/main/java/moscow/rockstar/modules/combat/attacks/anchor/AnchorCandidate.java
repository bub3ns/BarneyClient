package moscow.rockstar.modules.combat.attacks.anchor;

/** State tracked for a respawn-anchor position while it is being evaluated. */
public final class AnchorCandidate {
    public int chargeCount;
    public long createdAtMillis = System.currentTimeMillis();

    public AnchorCandidate() {
    }
}
