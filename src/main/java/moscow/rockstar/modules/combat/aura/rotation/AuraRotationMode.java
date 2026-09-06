/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 */
package moscow.rockstar.modules.combat.aura.rotation;

import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.settings.ModeSetting;
import net.minecraft.entity.LivingEntity;

public abstract class AuraRotationMode
extends ModeSetting.Option
implements ClientAccess {
    public AuraRotationMode(ModeSetting modeSetting, String string) {
        super(modeSetting, string);
    }

    public abstract void rotate(RotationManager var1, float var2, boolean var3, boolean var4, RotationCorrectionMode var5, LivingEntity var6);

    public Aura aura() {
        return RockstarClient.create().getModuleRegistry().getModule(Aura.class);
    }

    public void onAttack() {
    }

    public void onTargetLost() {
    }

    public void enabled() {
    }

    public void tick() {
    }

    public boolean canAttack() {
        return true;
    }
}

