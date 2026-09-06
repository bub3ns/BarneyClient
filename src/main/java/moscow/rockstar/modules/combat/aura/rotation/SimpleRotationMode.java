/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 */
package moscow.rockstar.modules.combat.aura.rotation;

import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.combat.aura.rotation.AuraRotationMode;
import moscow.rockstar.settings.ModeSetting;
import net.minecraft.entity.LivingEntity;
import ua.mintantileak.spk.Compile;

public class SimpleRotationMode
extends AuraRotationMode {
    public SimpleRotationMode(ModeSetting modeSetting) {
        super(modeSetting, "modules.settings.aura.simpleRotation");
    }

    @Override
    @Compile(obfuscation=1)
    public void rotate(RotationManager rotationManager, float f, boolean bl, boolean bl2, RotationCorrectionMode rotationCorrectionMode, LivingEntity class_13092) {
        Rotation rotation = AimRotationMath.calculateAttackRotation(class_13092, this.aura());
        rotationManager.requestRotation(rotation, rotationCorrectionMode, 180.0f, 180.0f, 180.0f, RotationPriority.TARGET_PRIORITY);
    }
}

