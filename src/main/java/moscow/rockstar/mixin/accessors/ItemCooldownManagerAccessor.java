/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemCooldownManager
 *  net.minecraft.ItemStack
 *  net.minecraft.Identifier
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package moscow.rockstar.mixin.accessors;

import java.util.Map;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ItemCooldownManager.class})
public interface ItemCooldownManagerAccessor {
    @Accessor(value="entries")
    public Map<Identifier, Object> rockstar$getEntries();

    @Accessor(value="tick")
    public int rockstar$getTick();

    @Invoker(value="getGroup")
    public Identifier rockstar$getGroup(ItemStack var1);
}

