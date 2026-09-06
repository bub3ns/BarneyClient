/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.CollisionView
 *  net.minecraft.VoxelShapes
 *  net.minecraft.VoxelShape
 *  net.minecraft.BlockState
 *  net.minecraft.ShapeContext
 *  net.minecraft.BlockCollisionSpliterator
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package moscow.rockstar.mixin.minecraft.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.CollisionView;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.world.BlockCollisionSpliterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import pyrock.events.game.CollisionShapeEvent;

@Mixin(value={BlockCollisionSpliterator.class})
public abstract class BlockCollisionSpliteratorMixin {
    @WrapOperation(method={"computeNext"}, at={@At(value="INVOKE", target="Lnet/minecraft/block/ShapeContext;getCollisionShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/CollisionView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;")})
    private VoxelShape onComputeNextCollisionBox(ShapeContext class_37262, BlockState class_26802, CollisionView class_19412, BlockPos adminsky, Operation<VoxelShape> operation) {
        VoxelShape class_2652 = (VoxelShape)operation.call(new Object[]{class_37262, class_26802, class_19412, adminsky});
        if (class_19412 != MinecraftClient.getInstance().world) {
            return class_2652;
        }
        CollisionShapeEvent collisionShapeEvent = new CollisionShapeEvent(class_26802, adminsky, class_2652);
        RockstarClient.create().getEventBus().post(collisionShapeEvent);
        return collisionShapeEvent.isCancelled() ? VoxelShapes.empty() : collisionShapeEvent.getShape();
    }
}

