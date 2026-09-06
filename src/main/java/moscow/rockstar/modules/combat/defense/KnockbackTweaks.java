/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.ClientCommandC2SPacket
 *  net.minecraft.ClientCommandC2SPacket$Mode
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.modules.combat.defense;

import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.defense.KnockbackAttackListener;
import moscow.rockstar.settings.BooleanSetting;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import pyrock.events.game.InternalAttackEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Knockback Tweaks", category=ModuleCategory.COMBAT, description="modules.descriptions.knockback_tweaks")
public class KnockbackTweaks
extends Module {
    BooleanSetting fakeSprint;
    private boolean sprintResetActive;
    boolean targetVisible;
    private Entity attackTarget;
    private int attackDelayTicks;
    boolean attackInProgress;
    private final EventListener<InternalAttackEvent> attackEventListener = new KnockbackAttackListener(this);

    public KnockbackTweaks() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.fakeSprint = new BooleanSetting(this, "modules.settings.knockback_tweaks.fake_sprint");
    }

    @Override
    public void onTick() {
        if (KnockbackTweaks.minecraftClient.player == null || KnockbackTweaks.minecraftClient.world == null) {
            return;
        }
        if (!this.fakeSprint.isEnabled()) {
            this.clearKnockbackTarget();
            this.resetSprintState();
        }
        if (this.sprintResetActive) {
            this.sprintResetActive = false;
            Rotation rotation = this.getRotation();
            KnockbackTweaks.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.LookAndOnGround(rotation.getYaw(), rotation.getPitch(), KnockbackTweaks.minecraftClient.player.isOnGround(), KnockbackTweaks.minecraftClient.player.horizontalCollision));
        }
        if (this.targetVisible) {
            this.targetVisible = false;
            KnockbackTweaks.minecraftClient.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)KnockbackTweaks.minecraftClient.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    }

    @Override
    public void onDisable() {
        this.sprintResetActive = false;
        this.targetVisible = false;
        this.attackTarget = null;
        this.attackDelayTicks = 0;
        this.attackInProgress = false;
    }

    void applyKnockbackTarget(Entity class_12972) {
        Rotation rotation = this.getRotation(class_12972);
        KnockbackTweaks.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.LookAndOnGround(rotation.getYaw(), rotation.getPitch(), KnockbackTweaks.minecraftClient.player.isOnGround(), KnockbackTweaks.minecraftClient.player.horizontalCollision));
        this.sprintResetActive = true;
    }

    private Rotation getRotation(Entity class_12972) {
        Vec3d VanillaChestLootTableGenerator = class_12972.getPos().subtract(KnockbackTweaks.minecraftClient.player.getPos());
        float f = MathHelper.wrapDegrees((float)((float)Math.toDegrees(Math.atan2(VanillaChestLootTableGenerator.z, VanillaChestLootTableGenerator.x)) + 90.0f));
        return new Rotation(f, this.getRotation().getPitch());
    }

    private Rotation getRotation() {
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        return rotationManager.isIdle() ? rotationManager.getPlayerRotation() : rotationManager.getCurrentRotation();
    }

    void clearKnockbackTarget() {
        KnockbackTweaks.minecraftClient.options.sprintKey.setPressed(true);
        if (!KnockbackTweaks.minecraftClient.player.isSprinting() && this.isPlayerReadyForAttack()) {
            KnockbackTweaks.minecraftClient.player.setSprinting(true);
        }
    }

    void updateKnockbackTarget(Entity class_12972) {
        this.attackTarget = class_12972;
        this.attackDelayTicks = 1;
    }

    private void resetSprintState() {
        if (this.attackTarget == null || KnockbackTweaks.minecraftClient.interactionManager == null) {
            return;
        }
        if (this.attackTarget.isRemoved() || !this.isPlayerReadyForAttack()) {
            this.attackTarget = null;
            this.attackDelayTicks = 0;
            return;
        }
        if (this.attackDelayTicks > 0) {
            --this.attackDelayTicks;
            return;
        }
        Entity class_12972 = this.attackTarget;
        this.attackTarget = null;
        this.attackInProgress = true;
        try {
            KnockbackTweaks.minecraftClient.interactionManager.attackEntity((PlayerEntity)KnockbackTweaks.minecraftClient.player, class_12972);
            KnockbackTweaks.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        }
        finally {
            this.attackInProgress = false;
        }
    }

    boolean isPlayerReadyForAttack() {
        return KnockbackTweaks.minecraftClient.player.input.hasForwardMovement() && !KnockbackTweaks.minecraftClient.player.horizontalCollision && !KnockbackTweaks.minecraftClient.player.isSneaking() && !KnockbackTweaks.minecraftClient.player.isTouchingWater() && !KnockbackTweaks.minecraftClient.player.isSubmergedInWater() && (KnockbackTweaks.minecraftClient.player.getHungerManager().getFoodLevel() > 6 || KnockbackTweaks.minecraftClient.player.getAbilities().allowFlying);
    }
}
