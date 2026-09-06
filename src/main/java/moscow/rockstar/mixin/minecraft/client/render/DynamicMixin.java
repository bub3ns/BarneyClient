/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.RenderTickCounter$Dynamic
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.render;

import moscow.rockstar.entity.utility.EntityUtils;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={RenderTickCounter.Dynamic.class})
public class DynamicMixin {
    @Shadow
    private float lastFrameDuration;
    @Shadow
    private float tickDelta;
    @Shadow
    private long prevTimeMillis;
    @Final
    @Shadow
    private float tickTime;

    @Inject(at={@At(value="FIELD", target="Lnet/minecraft/client/render/RenderTickCounter$Dynamic;prevTimeMillis:J", opcode=181, ordinal=0)}, method={"beginRenderTick(J)I"}, cancellable=true)
    public void onBeginRenderTick(long l, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        if (EntityUtils.getMovementFactor() == 1.0f) {
            return;
        }
        this.lastFrameDuration = (float)(l - this.prevTimeMillis) / this.tickTime * EntityUtils.getMovementFactor();
        this.prevTimeMillis = l;
        this.tickDelta += this.lastFrameDuration;
        int n = (int)this.tickDelta;
        this.tickDelta -= (float)n;
        callbackInfoReturnable.setReturnValue(n);
    }
}
