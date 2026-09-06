/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  moscow.rockstar.modules.other.admin.BlockPos$Mutable
 *  net.minecraft.StatusEffects
 *  net.minecraft.EndCrystalEntity
 *  net.minecraft.Blocks
 *  net.minecraft.Box
 *  net.minecraft.Packet
 *  net.minecraft.PlayerMoveC2SPacket$Full
 */
package moscow.rockstar.modules.combat.attacks;

import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.settings.ModeSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Box;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import pyrock.events.game.InternalAttackEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Criticals", category=ModuleCategory.COMBAT)
public class Criticals
extends Module {
    private ModeSetting criticalMode;
    private ModeSetting.Option defaultMode;
    private ModeSetting.Option reallyWorldMode;
    private int airborneTicks;
    private final EventListener<InternalAttackEvent> attackEventListener = internalAttackEvent -> {
        if (internalAttackEvent.isCancelled()) {
            return;
        }
        if (Criticals.minecraftClient.player == null || Criticals.minecraftClient.world == null) {
            return;
        }
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        if (this.criticalMode.isSelected(this.reallyWorldMode)) {
            this.sendReallyWorldCritical((InternalAttackEvent)internalAttackEvent);
            return;
        }
        if (Criticals.minecraftClient.player.isTouchingWater()) {
            return;
        }
        if (!this.isCriticalsTargetReady()) {
            return;
        }
        Rotation rotation = rotationManager.isIdle() ? rotationManager.getPlayerRotation() : rotationManager.getCurrentRotation();
        Rotation rotation2 = AimRotationMath.snapRotationToMouseStep(rotationManager.getCurrentRotation(), new Rotation(rotation.getYaw() + MathUtils.interpolateRandomDouble(-5.0, 5.0), rotation.getPitch() + MathUtils.interpolateRandomDouble(-5.0, 5.0)));
        Criticals.minecraftClient.player.fallDistance = MathUtils.interpolateRandomDouble(1.0E-5f, 1.0E-4f);
        Criticals.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(Criticals.minecraftClient.player.getX(), Criticals.minecraftClient.player.getY() - (double)Criticals.minecraftClient.player.fallDistance, Criticals.minecraftClient.player.getZ(), rotation2.getYaw(), rotation2.getPitch(), Criticals.minecraftClient.player.isOnGround(), Criticals.minecraftClient.player.horizontalCollision));
    };

    public Criticals() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.criticalMode = new ModeSetting(this, "modules.settings.criticals.mode");
        this.defaultMode = new ModeSetting.Option(this.criticalMode, "modules.settings.criticals.mode.default").select();
        this.reallyWorldMode = new ModeSetting.Option(this.criticalMode, "modules.settings.criticals.mode.reallyworld");
    }

    @Override
    public void onTick() {
        this.airborneTicks = this.isCriticalsStateReady() ? ++this.airborneTicks : 0;
    }

    public boolean isCriticalsTargetReady() {
        if (this.criticalMode.isSelected(this.reallyWorldMode)) {
            return !this.isCriticalsInventoryReady() || this.isCriticalsScreenReady();
        }
        return Criticals.minecraftClient.player != null && Criticals.minecraftClient.player.fallDistance <= 0.0f && !Criticals.minecraftClient.player.isOnGround() && this.airborneTicks > 0;
    }

    public boolean isCriticalsStateReady() {
        if (!this.isEnabled() || Criticals.minecraftClient.player == null || Criticals.minecraftClient.world == null) {
            return false;
        }
        if (this.criticalMode.isSelected(this.reallyWorldMode)) {
            return this.isCriticalsInventoryReady();
        }
        return Criticals.minecraftClient.player.fallDistance <= 0.0f && !Criticals.minecraftClient.player.isOnGround();
    }

    public boolean isCriticalsEnvironmentReady() {
        return this.isEnabled() && !this.criticalMode.isSelected(this.reallyWorldMode);
    }

    private boolean isCriticalsScreenReady() {
        if (Criticals.minecraftClient.player == null || Criticals.minecraftClient.world == null || Criticals.minecraftClient.player.isOnGround()) {
            return false;
        }
        double d = Criticals.minecraftClient.player.getY();
        return d != (double)((int)d) && (Criticals.minecraftClient.player.isInLava() || this.isCriticalsCooldownReady());
    }

    private void sendReallyWorldCritical(InternalAttackEvent internalAttackEvent) {
        float f;
        if (internalAttackEvent.getEntity() == null || internalAttackEvent.getEntity() instanceof EndCrystalEntity || !this.isCriticalsScreenReady()) {
            return;
        }
        Criticals.minecraftClient.player.fallDistance = f = MathUtils.interpolateRandomDouble(1.0E-7f, 1.0E-6f);
        Criticals.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(Criticals.minecraftClient.player.getX(), Criticals.minecraftClient.player.getY() - (double)f, Criticals.minecraftClient.player.getZ(), Criticals.minecraftClient.player.getYaw(), Criticals.minecraftClient.player.getPitch(), false, Criticals.minecraftClient.player.horizontalCollision));
    }

    public boolean isCriticalsInventoryReady() {
        return this.isEnabled() && Criticals.minecraftClient.player != null && Criticals.minecraftClient.world != null && (Criticals.minecraftClient.player.hasStatusEffect(StatusEffects.SLOW_FALLING) || this.isCriticalsCooldownReady());
    }

    public boolean isCriticalsCooldownReady() {
        if (Criticals.minecraftClient.player == null || Criticals.minecraftClient.world == null) {
            return false;
        }
        Box HorizontalFacingBlock = Criticals.minecraftClient.player.getBoundingBox();
        int n = (int)Math.floor(HorizontalFacingBlock.minX);
        int n2 = (int)Math.floor(HorizontalFacingBlock.minY);
        int n3 = (int)Math.floor(HorizontalFacingBlock.minZ);
        int n4 = (int)Math.ceil(HorizontalFacingBlock.maxX);
        int n5 = (int)Math.ceil(HorizontalFacingBlock.maxY);
        int n6 = (int)Math.ceil(HorizontalFacingBlock.maxZ);
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        for (int i = n; i < n4; ++i) {
            for (int j = n2; j < n5; ++j) {
                for (int k = n3; k < n6; ++k) {
                    if (!Criticals.minecraftClient.world.getBlockState((BlockPos)class_23392.set(i, j, k)).isOf(Blocks.COBWEB)) continue;
                    return true;
                }
            }
        }
        return false;
    }
}

