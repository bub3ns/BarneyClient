/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LightType
 *  net.minecraft.Blocks
 *  net.minecraft.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.sodium;

import moscow.rockstar.render.world.CameraClipManager;
import moscow.rockstar.render.world.DynamicLightGrid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"net.caffeinemc.mods.sodium.client.world.LevelSlice"}, remap=false)
public class LevelSliceMixin {
    @Inject(method={"getBlockState(III)Lnet/minecraft/block/BlockState;", "getBlockState(III)Lnet/minecraft/BlockState;"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void hideCameraClipBlocks(int n, int n2, int n3, CallbackInfoReturnable<BlockState> callbackInfoReturnable) {
        if (CameraClipManager.shouldHideBlock(new BlockPos(n, n2, n3))) {
            callbackInfoReturnable.setReturnValue(Blocks.AIR.getDefaultState());
        }
    }

    @Inject(method={"getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", "getBlockState(Lnet/minecraft/BlockPos;)Lnet/minecraft/BlockState;"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void hideCameraClipBlocks(BlockPos adminsky, CallbackInfoReturnable<BlockState> callbackInfoReturnable) {
        if (CameraClipManager.shouldHideBlock(adminsky)) {
            callbackInfoReturnable.setReturnValue(Blocks.AIR.getDefaultState());
        }
    }

    @Inject(method={"getLightLevel(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/BlockPos;)I", "getLightLevel(Lnet/minecraft/LightType;Lnet/minecraft/BlockPos;)I"}, at={@At(value="RETURN")}, cancellable=true, require=0)
    private void lightCameraClipBlocks(LightType class_19442, BlockPos adminsky, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        int n = callbackInfoReturnable.getReturnValueI();
        if (class_19442 == LightType.BLOCK) {
            n = DynamicLightGrid.max(adminsky, n);
        }
        if (CameraClipManager.shouldHideBlock(adminsky)) {
            n = CameraClipManager.preserveLightLevel(class_19442, n);
        }
        callbackInfoReturnable.setReturnValue(n);
    }
}
