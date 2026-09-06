package moscow.rockstar.modules.combat.defense;

import moscow.rockstar.events.EventListener;
import pyrock.events.game.InternalAttackEvent;

final class KnockbackAttackListener implements EventListener<InternalAttackEvent> {
    private final KnockbackTweaks knockbackModule;

    KnockbackAttackListener(KnockbackTweaks knockbackModule) {
        this.knockbackModule = knockbackModule;
    }

    public void onAttack(InternalAttackEvent event) {
        if (KnockbackTweaks.minecraftClient.player == null
                || KnockbackTweaks.minecraftClient.world == null
                || event.getEntity() == null
                || event.isCancelled()) {
            return;
        }
        if (this.knockbackModule.fakeSprint.isEnabled()) {
            if (KnockbackTweaks.minecraftClient.player.isSprinting()) {
                KnockbackTweaks.minecraftClient.player.networkHandler.sendPacket(
                        new net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket(
                                KnockbackTweaks.minecraftClient.player,
                                net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
            KnockbackTweaks.minecraftClient.player.networkHandler.sendPacket(
                    new net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket(
                            KnockbackTweaks.minecraftClient.player,
                            net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.START_SPRINTING));
            if (KnockbackTweaks.minecraftClient.player.isSprinting()) {
                this.knockbackModule.targetVisible = true;
            }
        } else {
            boolean wasSprinting = KnockbackTweaks.minecraftClient.player.isSprinting();
            this.knockbackModule.clearKnockbackTarget();
            if (!this.knockbackModule.attackInProgress && !wasSprinting && this.knockbackModule.isPlayerReadyForAttack()) {
                this.knockbackModule.updateKnockbackTarget(event.getEntity());
                event.cancel();
                return;
            }
            if (!KnockbackTweaks.minecraftClient.player.isSprinting()) {
                return;
            }
        }
        this.knockbackModule.applyKnockbackTarget(event.getEntity());
    }

    @Override
    public int getPriority() {
        return -100;
    }

    @Override
    public void onEvent(InternalAttackEvent event) {
        this.onAttack(event);
    }
}
