/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.InactivityFpsLimiter
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package moscow.rockstar.mixin.minecraft.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={InactivityFpsLimiter.class})
public class InactivityFpsLimiterMixin {
    @ModifyReturnValue(method={"update"}, at={@At(value="RETURN")})
    private int removeMenuFpsLimit(int n) {
        return n == 60 ? (Integer)MinecraftClient.getInstance().options.getMaxFps().getValue() : n;
    }
}

