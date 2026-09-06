/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Block
 *  net.minecraft.AbstractBlock$AbstractBlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package moscow.rockstar.mixin.minecraft.world;

import moscow.rockstar.core.ClientAccess;
import net.minecraft.block.Block;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={AbstractBlock.AbstractBlockState.class})
public abstract class AbstractBlockStateMixin
implements ClientAccess {
    @Shadow
    public abstract Block getBlock();
}

