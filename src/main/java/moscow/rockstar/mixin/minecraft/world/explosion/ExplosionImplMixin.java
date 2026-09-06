/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.World
 *  net.minecraft.Blocks
 *  net.minecraft.ExplosionImpl
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.world.explosion;

import java.util.List;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.mixin.accessors.ExplosionImplAccessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.events.game.AncientDebrisEvent;

@Mixin(value={ExplosionImpl.class})
public abstract class ExplosionImplMixin
implements ClientAccess {
    @Inject(method={"explode"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/explosion/ExplosionImpl;damageEntities()V", shift=At.Shift.AFTER)})
    private void onAfterDamageEntities(CallbackInfo callbackInfo) {
        ExplosionImpl class_98922 = (ExplosionImpl)(Object)this;
        List<BlockPos> list = ((ExplosionImplAccessor)class_98922).invokeGetBlocksToDestroy();
        List<BlockPos> list2 = list.stream().filter(adminsky -> class_98922.getWorld().getBlockState(adminsky).isOf(Blocks.ANCIENT_DEBRIS)).toList();
        if (!list2.isEmpty() && class_98922.getWorld().getRegistryKey() == World.NETHER) {
            RockstarClient.create().getEventBus().post(new AncientDebrisEvent(list2, class_98922.getPosition()));
        }
    }
}
