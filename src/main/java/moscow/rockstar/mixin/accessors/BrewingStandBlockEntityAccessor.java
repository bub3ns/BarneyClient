/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BrewingStandBlockEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={BrewingStandBlockEntity.class})
public interface BrewingStandBlockEntityAccessor {
    @Accessor(value="brewTime")
    public int getBrewTime();

    @Accessor(value="fuel")
    public int getFuelLevel();
}

