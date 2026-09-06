package moscow.rockstar.combat.critical;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;

public class AttackCriticalHandler implements ClientAccess {
    private static final AttackCriticalHandler INSTANCE = new AttackCriticalHandler();
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static AttackCriticalHandler getInstance() {
        return INSTANCE;
    }

    @FunctionalInterface
    public interface ModeOption {
        int selected();
    }

    public static AttackCriticalHandler.Mode modeFrom(ModeOption criticalMode) {
        if (criticalMode == null) {
            return AttackCriticalHandler.Mode.NONE;
        }
        switch (criticalMode.selected()) {
            case 1 -> {
                return AttackCriticalHandler.Mode.PRIORITIZE;
            }
            case 2 -> {
                return AttackCriticalHandler.Mode.ONLY;
            }
            default -> {
                return AttackCriticalHandler.Mode.NONE;
            }
        }
    }

    public static AttackCriticalHandler.Mode modeFrom(int selected) {
        switch (selected) {
            case 1 -> {
                return AttackCriticalHandler.Mode.PRIORITIZE;
            }
            case 2 -> {
                return AttackCriticalHandler.Mode.ONLY;
            }
            default -> {
                return AttackCriticalHandler.Mode.NONE;
            }
        }
    }

    public static AttackCriticalHandler.Mode modeFrom(ModeSetting modeSetting) {
        if (modeSetting == null) {
            return AttackCriticalHandler.Mode.NONE;
        }
        return modeFrom(modeSetting.getSelectedIndex());
    }

    public boolean allowsAttack(Mode mode, LivingEntity target) {
        switch (mode) {
            case NONE -> {
                return true;
            }
            case PRIORITIZE -> { 
                return !willPlayerBeInCriticalState(target);
            }
            case ONLY -> { // Won't attack if player is not in critical state
                return isPlayerInCriticalState(target) || !canPlayerCritical(target);
            }
        }
        return true;
    }

    private boolean isPlayerInCriticalState(LivingEntity target) {
        if (mc.player == null) {
            return false;
        }
        return MeleeDamage.isCriticalAttack(mc.player, target, MeleeDamage.isFullStrength(mc.player));
    }

    private boolean willPlayerBeInCriticalState(LivingEntity target) {
        if (mc.player == null) {
            return false;
        }
        boolean isRising = mc.player.fallDistance <= 0.0f && mc.player.getVelocity().y > -0.01;
        return !mc.player.isOnGround() && isRising && canPlayerCritical(target);
    }

    private boolean canPlayerCritical(LivingEntity target) {
        if (mc.player == null) {
            return false;
        }
        return MeleeDamage.criticalStateAllowed(mc.player, target);
    }

    public enum Mode {
        NONE, PRIORITIZE, ONLY;
    }
}
