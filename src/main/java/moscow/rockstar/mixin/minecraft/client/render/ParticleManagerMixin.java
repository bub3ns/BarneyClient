/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Direction
 *  net.minecraft.ParticleEffect
 *  net.minecraft.ParticleTypes
 *  net.minecraft.BlockState
 *  net.minecraft.ParticleManager
 *  net.minecraft.Particle
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.render;

import moscow.rockstar.core.RockstarClient;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.visuals.overlay.Removals;
import net.minecraft.util.math.Direction;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ParticleManager.class})
public abstract class ParticleManagerMixin {
    @Inject(method={"addBlockBreakParticles"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAddBlockBreakParticles(BlockPos adminsky, BlockState class_26802, CallbackInfo callbackInfo) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.isEnabled() && removals.getBreakParticles().isSelected()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"addBlockBreakingParticles"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAddBlockBreakingParticles(BlockPos adminsky, Direction class_23502, CallbackInfo callbackInfo) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.isEnabled() && removals.getBreakParticles().isSelected()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"addParticle"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAddParticle(ParticleEffect class_23942, double d, double d2, double d3, double d4, double d5, double d6, CallbackInfoReturnable<Particle> callbackInfoReturnable) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        if (removals.isEnabled() && removals.getWeather().isSelected() && class_23942.getType() == ParticleTypes.RAIN) {
            callbackInfoReturnable.cancel();
        }
    }
}

