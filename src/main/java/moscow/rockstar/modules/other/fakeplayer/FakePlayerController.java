/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.Hand
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.StatusEffects
 *  net.minecraft.Entity
 *  net.minecraft.MovementType
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Vec3d
 *  net.minecraft.ClientPlayPacketListener
 *  net.minecraft.EntityStatusS2CPacket
 *  net.minecraft.SoundEvents
 *  net.minecraft.SoundCategory
 */
package moscow.rockstar.modules.other.fakeplayer;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import moscow.rockstar.api.commands.CommandBuilder;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationType;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import pyrock.events.game.AttackEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.window.KeyPressEvent;
import ua.mintantileak.spk.Compile;

public class FakePlayerController
implements ClientAccess {
    private OtherClientPlayerEntity fakePlayer;
    private float forwardInput;
    private float strafeInput;
    private final EventListener<AttackEvent> attackListener = attackEvent -> {
        if (this.fakePlayer != null && attackEvent.getEntity() == this.fakePlayer && this.fakePlayer.hurtTime == 0) {
            FakePlayerController.minecraftClient.world.playSound((PlayerEntity)FakePlayerController.minecraftClient.player, this.fakePlayer.getX(), this.fakePlayer.getY(), this.fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (FakePlayerController.minecraftClient.player.fallDistance > 0.0f) {
                FakePlayerController.minecraftClient.world.playSound((PlayerEntity)FakePlayerController.minecraftClient.player, this.fakePlayer.getX(), this.fakePlayer.getY(), this.fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            } else {
                FakePlayerController.minecraftClient.world.playSound((PlayerEntity)FakePlayerController.minecraftClient.player, this.fakePlayer.getX(), this.fakePlayer.getY(), this.fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            this.fakePlayer.onDamaged(FakePlayerController.minecraftClient.world.getDamageSources().generic());
            this.fakePlayer.setHealth(this.fakePlayer.getHealth() + this.fakePlayer.getAbsorptionAmount() - 1.0f);
            if (this.fakePlayer.isDead()) {
                this.fakePlayer.setHealth(10.0f);
                new EntityStatusS2CPacket((Entity)this.fakePlayer, (byte)35).apply((ClientPlayPacketListener)FakePlayerController.minecraftClient.player.networkHandler);
            }
        }
    };
    private final EventListener<KeyPressEvent> keyListener = keyPressEvent -> {
        if (this.fakePlayer == null || FakePlayerController.minecraftClient.currentScreen != null) {
            return;
        }
        int n = keyPressEvent.getKey();
        int n2 = keyPressEvent.getAction();
        float f = 2.0f;
        if (n == 265) {
            this.forwardInput = n2 == 1 || n2 == 2 ? f : 0.0f;
        } else if (n == 264) {
            this.forwardInput = n2 == 1 || n2 == 2 ? -f : 0.0f;
        } else if (n == 263) {
            this.strafeInput = n2 == 1 || n2 == 2 ? f : 0.0f;
        } else if (n == 262) {
            this.strafeInput = n2 == 1 || n2 == 2 ? -f : 0.0f;
        }
    };
    private final EventListener<ClientPlayerTickEvent> tickListener = clientPlayerTickEvent -> {
        if (this.fakePlayer == null || FakePlayerController.minecraftClient.player == null) {
            return;
        }
        if (FakePlayerController.minecraftClient.currentScreen != null) {
            this.forwardInput = 0.0f;
            this.strafeInput = 0.0f;
            this.fakePlayer.setSprinting(false);
            return;
        }
        if (this.forwardInput != 0.0f || this.strafeInput != 0.0f) {
            float f = FakePlayerController.minecraftClient.player.getYaw();
            double d = 0.2;
            double d2 = (double)this.strafeInput * Math.cos(Math.toRadians(f)) - (double)this.forwardInput * Math.sin(Math.toRadians(f));
            double d3 = (double)this.forwardInput * Math.cos(Math.toRadians(f)) + (double)this.strafeInput * Math.sin(Math.toRadians(f));
            Vec3d VanillaChestLootTableGenerator = new Vec3d(d2 * d, this.fakePlayer.getVelocity().y, d3 * d);
            this.fakePlayer.move(MovementType.SELF, VanillaChestLootTableGenerator);
            this.fakePlayer.setSprinting(true);
        } else {
            this.fakePlayer.setSprinting(false);
            this.fakePlayer.setVelocity(0.0, this.fakePlayer.getVelocity().y, 0.0);
            this.fakePlayer.limbAnimator.setSpeed(0.0f);
        }
    };

    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("fakeplayer")
            .aliases("fp")
            .description("commands.fakeplayer.description")
            .argument("action", argument -> argument.choicesAndValidator("add", "remove", "del").choices("add", "remove"))
            .handler(this::handleCommand)
            .build();
    }

    @Compile
    private void handleCommand(DispatchContext dispatchContext) {
        String string = (String)dispatchContext.getArguments().getFirst();
        switch (string.toLowerCase()) {
            case "add": {
                this.spawnFakePlayer();
                break;
            }
            case "remove": 
            case "del": {
                this.removeFakePlayer();
            }
        }
    }

    public void spawnFakePlayer() {
        RockstarClient.create().getEventBus().registerListeners(this);
        if (this.fakePlayer != null) {
            this.fakePlayer.discard();
            this.fakePlayer = null;
        }
        this.fakePlayer = new OtherClientPlayerEntity(FakePlayerController.minecraftClient.world, new GameProfile(UUID.fromString("66123666-6666-6666-6666-666666666600"), "FakePlayer"));
        this.fakePlayer.copyPositionAndRotation((Entity)FakePlayerController.minecraftClient.player);
        this.fakePlayer.setStackInHand(Hand.MAIN_HAND, FakePlayerController.minecraftClient.player.getMainHandStack().copy());
        this.fakePlayer.setStackInHand(Hand.OFF_HAND, FakePlayerController.minecraftClient.player.getOffHandStack().copy());
        this.fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
        this.fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 4));
        this.fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
        FakePlayerController.minecraftClient.world.addEntity((Entity)this.fakePlayer);
        RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.SUCCESS, Localization.translate("commands.fakeplayer.success"), Localization.translate("commands.fakeplayer.added"));
    }

    public void removeFakePlayer() {
        if (this.fakePlayer == null) {
            RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.ERROR, Localization.translate("commands.fakeplayer.error"), Localization.translate("commands.fakeplayer.not_exists"));
            return;
        }
        this.fakePlayer.discard();
        this.fakePlayer = null;
        RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.SUCCESS, Localization.translate("commands.fakeplayer.success"), Localization.translate("commands.fakeplayer.removed"));
        RockstarClient.create().getEventBus().unregisterListeners(this);
    }
}
