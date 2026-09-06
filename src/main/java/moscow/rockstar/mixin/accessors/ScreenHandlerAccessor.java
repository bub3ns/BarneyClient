/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ScreenHandler
 *  net.minecraft.ItemStack
 *  net.minecraft.DefaultedList
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ScreenHandler.class})
public interface ScreenHandlerAccessor {
    @Accessor(value="trackedStacks")
    public DefaultedList<ItemStack> getTrackedStacks();
}

