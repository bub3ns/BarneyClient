/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.FlightProfiler
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package moscow.rockstar.mixin.minecraft.util.profiling.jfr;

import java.util.Optional;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={FlightProfiler.class})
public interface JvmProfilerMixin {
    @Redirect(method={"<clinit>"}, at=@At(value="INVOKE", target="Ljava/util/Optional;isPresent()Z"))
    private static boolean mint$disableJfr(Optional<?> optional) {
        return false;
    }
}

