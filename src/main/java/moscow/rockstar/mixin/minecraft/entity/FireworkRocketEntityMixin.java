/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 *  net.minecraft.FireworkRocketEntity
 *  net.minecraft.Vec3d
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package moscow.rockstar.mixin.minecraft.entity;

import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.RotationType;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.Rotation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pyrock.events.game.FireworkEvent;

@Mixin(value={FireworkRocketEntity.class})
public abstract class FireworkRocketEntityMixin
implements ClientAccess {
    @Redirect(method={"tick"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void redirectSetVelocity(LivingEntity class_13092, Vec3d VanillaChestLootTableGenerator) {
        FireworkRocketEntity class_16712 = (FireworkRocketEntity)(Object)this;
        FireworkEvent fireworkEvent = new FireworkEvent(class_13092, VanillaChestLootTableGenerator, class_16712);
        RockstarClient.create().getEventBus().post(fireworkEvent);
        class_13092.setVelocity(fireworkEvent.getVelocity());
    }

    @Redirect(method={"tick"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d redirectGetRotationVector(LivingEntity class_13092) {
        RotationManager rotationManager;
        if (class_13092 == FireworkRocketEntityMixin.minecraftClient.player && (rotationManager = RockstarClient.create().getRotationManager()) != null && rotationManager.getRotationState() != RotationType.IDLE) {
            Rotation rotation = rotationManager.getCurrentRotation();
            return Vec3d.fromPolar((float)rotation.getPitch(), (float)rotation.getYaw());
        }
        return class_13092.getRotationVector();
    }
}
