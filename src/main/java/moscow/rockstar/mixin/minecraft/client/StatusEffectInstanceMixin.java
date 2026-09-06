/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.RegistryEntry
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client;

import net.minecraft.client.MinecraftClient;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={StatusEffectInstance.class})
public class StatusEffectInstanceMixin
{
    @Unique
    private Animation potionStatusAnimation;
    @Unique
    private Animation timeAnimation;

    @Inject(method={"<init>(Lnet/minecraft/registry/entry/RegistryEntry;IIZZZLnet/minecraft/entity/effect/StatusEffectInstance;)V"}, at={@At(value="TAIL")})
    public void onInit(RegistryEntry<?> class_68802, int n, int n2, boolean bl, boolean bl2, boolean bl3, StatusEffectInstance class_12932, CallbackInfo callbackInfo) {
        if (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().player == null) {
            return;
        }
        this.timeAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
    }

    public Animation rockstar$getAnimPotion() {
        if (this.potionStatusAnimation == null) {
            this.potionStatusAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
        }
        return this.potionStatusAnimation;
    }

    public Animation rockstar$getTimeAnimation() {
        return this.timeAnimation;
    }
}
