/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  net.minecraft.EntityPositionSyncS2CPacket
 *  net.minecraft.Entity
 *  net.minecraft.ItemStack
 *  net.minecraft.ParticleEffect
 *  net.minecraft.Vec3d
 *  net.minecraft.CooldownUpdateS2CPacket
 *  net.minecraft.EntityS2CPacket
 *  net.minecraft.ClientPlayNetworkHandler
 *  net.minecraft.ClientWorld
 *  net.minecraft.ParticleManager
 *  net.minecraft.GameRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.network;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.tracking.EntityPositionCache;
import moscow.rockstar.mixin.accessors.EntityS2CPacketAccessor;
import moscow.rockstar.modules.combat.targeting.BackTrack;
import moscow.rockstar.modules.visuals.overlay.Removals;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyrock.events.game.EventSetCooldown;

@Mixin(value={ClientPlayNetworkHandler.class})
public class ClientPlayNetworkHandlerMixin
implements ClientAccess {
    @WrapWithCondition(method={"onEntityStatus"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/particle/ParticleManager;addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V")})
    private boolean rockstar$hideTotemParticles(ParticleManager class_7022, Entity class_12972, ParticleEffect class_23942, int n) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        return !removals.isEnabled() || !removals.getTotem().isSelected();
    }

    @WrapWithCondition(method={"onEntityStatus"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/GameRenderer;showFloatingItem(Lnet/minecraft/item/ItemStack;)V")})
    private boolean rockstar$hideTotemItem(GameRenderer ThirdParty, ItemStack class_17992) {
        Removals removals = RockstarClient.create().getModuleRegistry().getModule(Removals.class);
        return !removals.isEnabled() || !removals.getTotem().isSelected();
    }

    @Inject(method={"onEntity"}, at={@At(value="TAIL")})
    public void onEntity(EntityS2CPacket class_26842, CallbackInfo callbackInfo) {
        ClientPlayNetworkHandler TrapezoidHeightProvider = (ClientPlayNetworkHandler)(Object)this;
        ClientWorld NarrationMessageBuilder = TrapezoidHeightProvider.getWorld();
        if (NarrationMessageBuilder == null) {
            return;
        }
        int n = ((EntityS2CPacketAccessor)class_26842).getId();
        Entity class_12972 = NarrationMessageBuilder.getEntityById(n);
        if (class_12972 == null) {
            return;
        }
        double d = (double)class_26842.getDeltaX() / 4096.0;
        double d2 = (double)class_26842.getDeltaY() / 4096.0;
        double d3 = (double)class_26842.getDeltaZ() / 4096.0;
        Vec3d VanillaChestLootTableGenerator = EntityPositionCache.getTrackedPosition(class_12972).add(d, d2, d3);
        EntityPositionCache.storePosition(class_12972, VanillaChestLootTableGenerator);
        BackTrack backTrack = RockstarClient.create().getModuleRegistry().getModule(BackTrack.class);
        backTrack.recordBacktrackPoint(class_12972, VanillaChestLootTableGenerator, System.currentTimeMillis());
    }

    @Inject(method={"onEntityPositionSync"}, at={@At(value="HEAD")})
    private void onEntityPositionSyncPacket(EntityPositionSyncS2CPacket class_102642, CallbackInfo callbackInfo) {
        ClientPlayNetworkHandler TrapezoidHeightProvider = (ClientPlayNetworkHandler)(Object)this;
        ClientWorld NarrationMessageBuilder = TrapezoidHeightProvider.getWorld();
        if (NarrationMessageBuilder == null) {
            return;
        }
        int n = class_102642.id();
        Entity class_12972 = NarrationMessageBuilder.getEntityById(n);
        if (class_12972 == null) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = class_102642.values().position();
        EntityPositionCache.storePosition(class_12972, VanillaChestLootTableGenerator);
        BackTrack backTrack = RockstarClient.create().getModuleRegistry().getModule(BackTrack.class);
        backTrack.resetBacktrackHistory(class_12972, VanillaChestLootTableGenerator, System.currentTimeMillis());
    }

    @Inject(method={"onCooldownUpdate"}, at={@At(value="HEAD")}, cancellable=true)
    private void handleCooldown(CooldownUpdateS2CPacket class_26562, CallbackInfo callbackInfo) {
        EventSetCooldown eventSetCooldown = new EventSetCooldown(class_26562.cooldown(), class_26562.cooldownGroup());
        RockstarClient.create().getEventBus().post(eventSetCooldown);
        if (eventSetCooldown.getCooldown() != class_26562.cooldown()) {
            callbackInfo.cancel();
            ClientPlayNetworkHandlerMixin.minecraftClient.player.getItemCooldownManager().set(class_26562.cooldownGroup(), eventSetCooldown.getCooldown());
        }
    }
}
