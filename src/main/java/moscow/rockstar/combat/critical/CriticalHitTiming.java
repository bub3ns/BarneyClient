package moscow.rockstar.combat.critical;

import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Encapsulates the small amount of timing state shared by critical-hit
 * modules.  This used to be represented by a decompiler collision with the
 * neural aim-adjustment class.
 */
public final class CriticalHitTiming {
    private CriticalHitTiming() {
    }

    public static boolean isCriticalWindowReady(ClientPlayerEntity player, float targetHeight, int toleranceTicks) {
        if (player == null || player.isOnGround() || player.isClimbing() || player.isTouchingWater() || player.isInLava()) {
            return false;
        }
        float maximumFallDistance = Math.max(0.3f, targetHeight) + Math.max(0, toleranceTicks) * 0.01f;
        return player.fallDistance >= 0.0f && player.fallDistance <= maximumFallDistance;
    }

    public static boolean shouldCancelJump(ClientPlayerEntity player, float targetHeight) {
        if (player == null || !player.isOnGround() || player.isTouchingWater() || player.isInLava()) {
            return false;
        }
        return player.getAttackCooldownProgress(0.0f) > 0.9f && targetHeight > 0.3f;
    }
}
