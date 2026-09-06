/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Blocks
 *  net.minecraft.BlockState
 *  net.minecraft.ChunkRendererRegion
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.render.chunk;

import moscow.rockstar.render.world.CameraClipManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ChunkRendererRegion.class})
public class ChunkRendererRegionMixin {
    @Inject(method={"getBlockState"}, at={@At(value="HEAD")}, cancellable=true)
    private void hideCameraClipBlocks(BlockPos adminsky, CallbackInfoReturnable<BlockState> callbackInfoReturnable) {
        if (CameraClipManager.shouldHideBlock(adminsky)) {
            callbackInfoReturnable.setReturnValue(Blocks.AIR.getDefaultState());
        }
    }
}
