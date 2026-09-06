package moscow.rockstar.modules.combat.targeting;

import net.minecraft.entity.LivingEntity;

record TargetPoint(LivingEntity entity, double score) {
    LivingEntity getEntity() {
        return this.entity;
    }

    double getScore() {
        return this.score;
    }
}
