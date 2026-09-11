package moscow.rockstar.combat.critical;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

public final class MeleeDamage {
    private MeleeDamage() {
    }

    public static boolean isFullStrength(PlayerEntity player) {
        if (player == null) {
            return false;
        }
        return player.getAttackCooldownProgress(0.5f) > 0.9f;
    }

    public static boolean criticalStateAllowed(PlayerEntity player, LivingEntity target) {
        if (player == null || target == null || !target.isAlive()) {
            return false;
        }
        return !player.isClimbing()
                && !player.isTouchingWater()
                && !player.isInLava()
                && !player.hasStatusEffect(StatusEffects.BLINDNESS)
                && !player.hasStatusEffect(StatusEffects.SLOW_FALLING)
                && !player.hasVehicle()
                && !player.getAbilities().flying;
    }

    public static boolean isCriticalAttack(PlayerEntity player, LivingEntity target, boolean isFullStrength) {
        if (player == null || target == null || !isFullStrength) {
            return false;
        }
        boolean isFalling = player.fallDistance > 0.0f;
        return !player.isOnGround() && isFalling && criticalStateAllowed(player, target);
    }
}
