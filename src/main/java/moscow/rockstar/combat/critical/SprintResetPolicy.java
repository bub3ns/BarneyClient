package moscow.rockstar.combat.critical;

import net.minecraft.entity.player.PlayerEntity;

/** Decides whether an attack should wait for the player's sprint transition. */
public final class SprintResetPolicy {
    private SprintResetPolicy() {
    }

    public static boolean shouldDeferAttack(PlayerEntity player) {
        return player != null && player.isSprinting() && !player.isUsingItem();
    }
}
