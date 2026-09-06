/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.World
 *  net.minecraft.BlockEntity
 *  net.minecraft.WorldChunk
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.world.chunk;

import java.util.Map;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.entity.tracking.BlockEntityTracker;
import net.minecraft.world.World;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={WorldChunk.class})
public abstract class WorldChunkMixin {
    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract Map<BlockPos, BlockEntity> getBlockEntities();

    @Inject(method={"setBlockEntity"}, at={@At(value="INVOKE", target="Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")})
    private void onLoadBlockEntity(BlockEntity class_25862, CallbackInfo callbackInfo) {
        if (this.getWorld().isClient()) {
            BlockEntityTracker.add(class_25862);
        }
    }

    @Inject(method={"removeBlockEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/block/entity/BlockEntity;markRemoved()V")})
    private void onRemoveBlockEntity(BlockPos adminsky, CallbackInfo callbackInfo) {
        BlockEntityTracker.remove(adminsky);
    }

    @Inject(method={"clear"}, at={@At(value="HEAD")})
    private void onClearBlockEntities(CallbackInfo callbackInfo) {
        if (!this.getWorld().isClient()) {
            return;
        }
        for (BlockPos adminsky : this.getBlockEntities().keySet()) {
            BlockEntityTracker.remove(adminsky);
        }
    }
}
