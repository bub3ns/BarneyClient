/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.PlayerInput
 *  net.minecraft.KeyboardInput
 *  net.minecraft.Input
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.input;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.mixin.minecraft.client.input.InputAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.PlayerInput;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.input.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.events.player.InputEvent;

@Environment(value=EnvType.CLIENT)
@Mixin(value={KeyboardInput.class})
public abstract class KeyboardInputMixin {
    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void onTick(CallbackInfo callbackInfo) {
        Input GoatHornIdFix = (Input)(Object)this;
        InputAccessor inputAccessor = (InputAccessor)GoatHornIdFix;
        PlayerInput class_101852 = inputAccessor.getInput();
        float f = inputAccessor.getMovementForward();
        float f2 = inputAccessor.getMovementSideways();
        boolean bl = class_101852.jump();
        boolean bl2 = class_101852.sneak();
        boolean bl3 = class_101852.sprint();
        InputEvent inputEvent = new InputEvent(f, f2, bl, bl2, bl3);
        RockstarClient.create().getEventBus().post(inputEvent);
        inputAccessor.setMovementForward(inputEvent.getForward());
        inputAccessor.setMovementSideways(inputEvent.getStrafe());
        boolean bl4 = inputEvent.getForward() > 0.0f;
        boolean bl5 = inputEvent.getForward() < 0.0f;
        boolean bl6 = inputEvent.getStrafe() > 0.0f;
        boolean bl7 = inputEvent.getStrafe() < 0.0f;
        inputAccessor.setInput(new PlayerInput(bl4, bl5, bl6, bl7, inputEvent.isJump(), inputEvent.isSneak(), inputEvent.isSprint()));
    }
}
