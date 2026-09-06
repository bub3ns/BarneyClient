package moscow.rockstar.mixin.accessors;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the vanilla caught-fish flag for the fishing utility module. */
@Mixin(FishingBobberEntity.class)
public interface FishingBobberEntityAccessor {
    @Accessor("CAUGHT_FISH")
    static TrackedData<Boolean> rockstar$getCaughtFish() {
        throw new AssertionError();
    }
}
