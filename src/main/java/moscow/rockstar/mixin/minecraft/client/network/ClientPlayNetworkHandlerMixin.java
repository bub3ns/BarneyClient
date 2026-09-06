/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.ItemEntity
 *  net.minecraft.ClientConnection
 *  net.minecraft.BlockEntity
 *  net.minecraft.BlockEntityUpdateS2CPacket
 *  net.minecraft.ChunkDataS2CPacket
 *  net.minecraft.GameJoinS2CPacket
 *  net.minecraft.PlayerPositionLookS2CPacket
 *  net.minecraft.PlayerRespawnS2CPacket
 *  net.minecraft.ItemPickupAnimationS2CPacket
 *  net.minecraft.WorldChunk
 *  net.minecraft.ClientPlayNetworkHandler
 *  net.minecraft.ClientCommonNetworkHandler
 *  net.minecraft.ClientConnectionState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.network;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.tracking.EntityPositionCache;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.movement.jump.AirStuck;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.visuals.world.XRay;
import moscow.rockstar.entity.tracking.BlockEntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.events.game.PickupEvent;
import pyrock.events.game.WorldChangeEvent;

@Mixin(value={ClientPlayNetworkHandler.class})
public abstract class ClientPlayNetworkHandlerMixin
extends ClientCommonNetworkHandler
implements ClientAccess {
    @Unique
    private Rotation oldRotation;

    protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection class_25352, ClientConnectionState class_86752) {
        super(client, class_25352, class_86752);
    }

    @Inject(method={"onItemPickupAnimation"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/world/ClientWorld;getEntityById(I)Lnet/minecraft/entity/Entity;", ordinal=0)})
    private void onItemPickupAnimation(ItemPickupAnimationS2CPacket class_27752, CallbackInfo callbackInfo) {
        Entity class_12972 = this.client.world.getEntityById(class_27752.getEntityId());
        Entity class_12973 = this.client.world.getEntityById(class_27752.getCollectorEntityId());
        if (class_12972 instanceof ItemEntity) {
            RockstarClient.create().getEventBus().post(new PickupEvent(class_12973, ((ItemEntity)class_12972).getStack(), class_27752.getStackAmount()));
        }
    }

    @Inject(method={"onBlockEntityUpdate"}, at={@At(value="TAIL")})
    private void onBlockEntityUpdate(BlockEntityUpdateS2CPacket class_26222, CallbackInfo callbackInfo) {
        if (ClientPlayNetworkHandlerMixin.minecraftClient.world == null) {
            return;
        }
        BlockPos adminsky = class_26222.getPos();
        BlockEntity blockEntity = ClientPlayNetworkHandlerMixin.minecraftClient.world.getBlockEntity(adminsky);
        if (blockEntity != null) {
            BlockEntityTracker.add(blockEntity);
        }
    }

    @Inject(method={"onChunkData"}, at={@At(value="TAIL")})
    private void onChunkData(ChunkDataS2CPacket class_26722, CallbackInfo callbackInfo) {
        if (ClientPlayNetworkHandlerMixin.minecraftClient.world == null) {
            return;
        }
        WorldChunk class_28182 = ClientPlayNetworkHandlerMixin.minecraftClient.world.getChunk(class_26722.getChunkX(), class_26722.getChunkZ());
        class_28182.getBlockEntities().values().forEach(BlockEntityTracker::add);
        XRay xRay = RockstarClient.create().getModuleRegistry().getModule(XRay.class);
        MinecraftClient client = MinecraftClient.getInstance();
        if (xRay == null || !xRay.isEnabled() || client.world == null) {
            return;
        }
        new Thread(() -> xRay.processChunk(class_28182)).start();
    }

    @Inject(method={"onGameJoin"}, at={@At(value="TAIL")})
    private void onGameJoin(GameJoinS2CPacket class_26782, CallbackInfo callbackInfo) {
        BlockEntityTracker.clear();
        EntityPositionCache.clear();
        RockstarClient.create().getEventBus().post(new WorldChangeEvent());
    }

    @Inject(method={"onPlayerRespawn"}, at={@At(value="TAIL")})
    private void rockstar$onPlayerRespawn(PlayerRespawnS2CPacket class_27242, CallbackInfo callbackInfo) {
        BlockEntityTracker.clear();
        EntityPositionCache.clear();
        AirStuck airStuck = RockstarClient.create().getModuleRegistry().getModule(AirStuck.class);
        if (airStuck != null) {
            airStuck.disable();
        }
    }

    @Inject(method={"onPlayerPositionLook"}, at={@At(value="HEAD")})
    public void savePlayerRotation(PlayerPositionLookS2CPacket class_27082, CallbackInfo callbackInfo) {
        if (ClientPlayNetworkHandlerMixin.minecraftClient.player == null) {
            return;
        }
        this.oldRotation = new Rotation(ClientPlayNetworkHandlerMixin.minecraftClient.player.getYaw(), ClientPlayNetworkHandlerMixin.minecraftClient.player.getPitch());
    }

    @Inject(method={"onPlayerPositionLook"}, at={@At(value="RETURN")})
    public void modifyPlayerRotation(PlayerPositionLookS2CPacket class_27082, CallbackInfo callbackInfo) {
        if (ClientPlayNetworkHandlerMixin.minecraftClient.player == null) {
            return;
        }
        Rotation rotation = new Rotation(class_27082.change().yaw(), class_27082.change().pitch());
    }
}
