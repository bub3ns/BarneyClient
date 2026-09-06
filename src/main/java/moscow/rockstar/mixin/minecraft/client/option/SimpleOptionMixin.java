/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 *  net.minecraft.SimpleOption
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.option;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.visuals.world.Ambience;
import net.minecraft.text.Text;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={SimpleOption.class})
public class SimpleOptionMixin<T> {
    @Shadow
    @Final
    Text text;
    @Shadow
    T value;

    @Inject(method={"getValue"}, at={@At(value="HEAD")}, cancellable=true)
    public void getGammaValue(CallbackInfoReturnable<Double> callbackInfoReturnable) {
        if (RockstarClient.create().getModuleRegistry() == null) {
            return;
        }
        Ambience ambience = RockstarClient.create().getModuleRegistry().getModule(Ambience.class);
        if (ambience != null && ambience.isEnabled() && ambience.isAmbientLightingReady() && ambience.getGammaTimeOption().isSelected() && this.text.equals((Object)Text.translatable((String)"options.gamma"))) {
            callbackInfoReturnable.setReturnValue(1337.0);
        }
    }

    @Inject(method={"setValue"}, at={@At(value="HEAD")}, cancellable=true)
    public void setGammaValue(T t, CallbackInfo callbackInfo) {
        if (RockstarClient.create().getModuleRegistry() == null) {
            return;
        }
        Ambience ambience = RockstarClient.create().getModuleRegistry().getModule(Ambience.class);
        if (ambience != null && ambience.isEnabled() && ambience.getGammaTimeOption().isSelected() && this.text.equals((Object)Text.translatable((String)"options.gamma"))) {
            this.value = t;
            callbackInfo.cancel();
        }
    }
}
