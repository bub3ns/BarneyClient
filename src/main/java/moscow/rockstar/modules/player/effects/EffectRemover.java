/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.StatusEffects
 */
package moscow.rockstar.modules.player.effects;

import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.MultiBooleanSetting;
import net.minecraft.entity.effect.StatusEffects;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Effect Remover", category=ModuleCategory.OTHER)
public class EffectRemover
extends Module {
    private MultiBooleanSetting effectRemovalSetting;
    private MultiBooleanSetting.Option levitationOption;
    private MultiBooleanSetting.Option jumpBoostOption;
    private MultiBooleanSetting.Option slowFallingOption;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (EffectRemover.minecraftClient.player == null) {
            return;
        }
        if (this.levitationOption.isSelected()) {
            EffectRemover.minecraftClient.player.removeStatusEffect(StatusEffects.LEVITATION);
        }
        if (this.jumpBoostOption.isSelected()) {
            EffectRemover.minecraftClient.player.removeStatusEffect(StatusEffects.JUMP_BOOST);
        }
        if (this.slowFallingOption.isSelected()) {
            EffectRemover.minecraftClient.player.removeStatusEffect(StatusEffects.SLOW_FALLING);
        }
    };

    public EffectRemover() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.effectRemovalSetting = new MultiBooleanSetting(this, "modules.settings.effect_remover.remove");
        this.levitationOption = new MultiBooleanSetting.Option(this.effectRemovalSetting, "modules.settings.effect_remover.remove.levitation").select();
        this.jumpBoostOption = new MultiBooleanSetting.Option(this.effectRemovalSetting, "modules.settings.effect_remover.remove.jump_boost").select();
        this.slowFallingOption = new MultiBooleanSetting.Option(this.effectRemovalSetting, "modules.settings.effect_remover.remove.slow_fall").select();
    }
}

