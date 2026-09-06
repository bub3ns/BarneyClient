package moscow.rockstar.modules.combat.rotation;

import moscow.rockstar.entity.tracking.EntityPositionCache;
import moscow.rockstar.settings.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class ElytraPrediction {
    private ElytraPrediction() {
    }

    public static Vec3d predictEntityPosition(LivingEntity entity, ModeSetting modeSetting,
                                               ModeSetting.Option predicted, ModeSetting.Option tracked,
                                               double leadTicks) {
        if (entity == null) {
            return null;
        }
        if (modeSetting.isSelected(predicted)) {
            if (entity instanceof PlayerEntity player) {
                Vec3d movement = player.getPos().subtract(player.prevX, player.prevY, player.prevZ);
                return player.getEyePos().add(movement.multiply(leadTicks));
            }
            return entity.getEyePos();
        }
        if (modeSetting.isSelected(tracked)) {
            return EntityPositionCache.getTrackedPosition((Entity) entity);
        }
        return entity.getPos();
    }
}
