/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Entity$RemovalReason
 *  net.minecraft.LivingEntity
 *  net.minecraft.World
 *  net.minecraft.Block
 *  net.minecraft.BlockState
 *  net.minecraft.Chunk
 *  net.minecraft.DimensionType
 *  net.minecraft.MutableWorldProperties
 *  net.minecraft.RegistryKey
 *  net.minecraft.DynamicRegistryManager
 *  net.minecraft.ClientWorld
 *  net.minecraft.RegistryEntry
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.world;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.tracking.EntityPositionCache;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.visuals.world.XRay;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.events.game.EntityDeathEvent;

@Mixin(value={ClientWorld.class})
public abstract class ClientWorldMixin
extends World
implements ClientAccess {
    protected ClientWorldMixin(MutableWorldProperties class_52692, RegistryKey<World> class_53212, DynamicRegistryManager class_54552, RegistryEntry<DimensionType> class_68802, boolean bl, boolean bl2, long l, int n) {
        super(class_52692, class_53212, class_54552, class_68802, bl, bl2, l, n);
    }

    @Inject(method={"handleBlockUpdate"}, at={@At(value="HEAD")})
    private void onHandleBlockUpdate(BlockPos adminsky, BlockState class_26802, int n, CallbackInfo callbackInfo) {
        XRay xRay = RockstarClient.create().getModuleRegistry().getModule(XRay.class);
        if (xRay == null || !xRay.isEnabled()) {
            return;
        }
        Block class_22482 = class_26802.getBlock();
        BlockPos adminsky2 = adminsky.toImmutable();
        if (xRay.getBlockSelectionSetting().isBlockSelected(class_22482)) {
            xRay.getSelectedBlocks().add(adminsky2);
        } else {
            xRay.getSelectedBlocks().remove(adminsky2);
        }
    }

    @Inject(method={"removeEntity"}, at={@At(value="HEAD")})
    private void removeEntityEvent(int n, Entity.RemovalReason class_55292, CallbackInfo callbackInfo) {
        LivingEntity class_13092;
        EntityPositionCache.removeEntity(n);
        if (ClientWorldMixin.minecraftClient.player == null || ClientWorldMixin.minecraftClient.player.isRemoved()) {
            return;
        }
        if (ClientWorldMixin.minecraftClient.player.getId() == n) {
            return;
        }
        Entity class_12972 = this.getEntityById(n);
        if (class_12972 instanceof LivingEntity && ClientWorldMixin.minecraftClient.player.distanceTo((Entity)(class_13092 = (LivingEntity)class_12972)) < 6.0f && !ClientWorldMixin.minecraftClient.player.isDead() && EntityPositionCache.trackEntity(class_12972)) {
            RockstarClient.create().getEventBus().post(new EntityDeathEvent(class_13092));
        }
    }

}

