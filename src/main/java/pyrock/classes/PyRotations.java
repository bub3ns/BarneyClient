/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 */
package pyrock.classes;

import java.util.List;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.combat.attacks.Aura;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class PyRotations {
    private RotationManager handler() {
        return RockstarClient.create().getRotationManager();
    }

    private Aura aura() {
        return RockstarClient.create().getModuleRegistry().getModule(Aura.class);
    }

    private Rotation defaultRotation(Entity class_12972) {
        if (class_12972 instanceof LivingEntity) {
            LivingEntity class_13092 = (LivingEntity)class_12972;
            return AimRotationMath.calculateAttackRotation(class_13092, this.aura());
        }
        if (class_12972 == null) {
            return this.handler().getEffectiveRotation();
        }
        Vec3d VanillaChestLootTableGenerator = AimRotationMath.getClosestPointOnEntityBounds(class_12972);
        return AimRotationMath.getRotationToPoint(VanillaChestLootTableGenerator);
    }

    public List<Double> defaultAngles(Entity class_12972) {
        return this.angles(this.defaultRotation(class_12972));
    }

    public List<Double> current() {
        Rotation rotation = this.handler().getEffectiveRotation();
        return this.angles(rotation);
    }

    public List<Double> player() {
        Rotation rotation = this.handler().getPlayerRotation();
        return this.angles(rotation);
    }

    public boolean idling() {
        return this.handler().isIdle();
    }

    public List<Double> toPoint(double d, double d2, double d3) {
        Rotation rotation = AimRotationMath.getRotationToPoint(new Vec3d(d, d2, d3));
        return this.angles(rotation);
    }

    public List<Double> to(Entity class_12972) {
        if (class_12972 == null) {
            return this.current();
        }
        Vec3d VanillaChestLootTableGenerator = AimRotationMath.getClosestPointOnEntityBounds(class_12972);
        Rotation rotation = AimRotationMath.getRotationToPoint(VanillaChestLootTableGenerator);
        return this.angles(rotation);
    }

    public List<Double> gcd(double d, double d2, double d3, double d4) {
        Rotation rotation = AimRotationMath.snapRotationToMouseStep(new Rotation(d, d2), new Rotation(d3, d4));
        return this.angles(rotation);
    }

    public List<Double> gcdStep(double d, double d2, double d3, double d4, double d5) {
        return this.gcdSteps(d, d2, d3, d4, d5, d5);
    }

    public List<Double> gcdSteps(double d, double d2, double d3, double d4, double d5, double d6) {
        Rotation rotation = new Rotation(this.snapDelta((float)d, (float)d3, (float)d5, true), MathHelper.clamp((float)this.snapDelta((float)d2, (float)d4, (float)d6, false), (float)-90.0f, (float)90.0f));
        return this.angles(rotation);
    }

    public float vanillaStep() {
        return AimRotationMath.getMouseRotationStep();
    }

    public void apply(double d, double d2) {
        this.apply(d, d2, "silent", "normal", 180.0, 180.0);
    }

    public void apply(double d, double d2, String string, String string2) {
        this.apply(d, d2, string, string2, 180.0, 180.0);
    }

    public void apply(double d, double d2, String string, String string2, double d3, double d4) {
        this.apply(d, d2, string, string2, d3, d4, 180.0, true);
    }

    public void apply(double d, double d2, String string, String string2, double d3, double d4, double d5, boolean bl) {
        this.handler().requestRotationInternal(new Rotation(d, d2), PyRotations.parseCorrection(string), (float)d3, (float)d4, (float)d5, PyRotations.parsePriority(string2), bl);
    }

    public static RotationCorrectionMode parseCorrection(String string) {
        if (string == null) {
            return RotationCorrectionMode.UNSPECIFIED;
        }
        return switch (string.toLowerCase()) {
            case "none", "off" -> RotationCorrectionMode.NONE;
            case "direct" -> RotationCorrectionMode.DIRECT;
            case "strict" -> RotationCorrectionMode.STRICT;
            case "smooth", "smooth_silent" -> RotationCorrectionMode.SMOOTH;
            case "change_look", "changelook", "change-look" -> RotationCorrectionMode.CHANGE_LOOK;
            case "targeted" -> RotationCorrectionMode.TARGETED;
            default -> RotationCorrectionMode.UNSPECIFIED;
        };
    }

    public static RotationPriority parsePriority(String string) {
        if (string == null) {
            return RotationPriority.STANDARD_PRIORITY;
        }
        return switch (string.toLowerCase()) {
            case "target", "to_target" -> RotationPriority.TARGET_PRIORITY;
            case "override" -> RotationPriority.OVERRIDE_PRIORITY;
            case "use_item" -> RotationPriority.ITEM_USE_PRIORITY;
            case "max" -> RotationPriority.MAXIMUM_PRIORITY;
            case "low", "not_important" -> RotationPriority.LOW_PRIORITY;
            default -> RotationPriority.STANDARD_PRIORITY;
        };
    }

    private List<Double> angles(Rotation rotation) {
        return List.of(Double.valueOf(rotation.getYaw()), Double.valueOf(rotation.getPitch()));
    }

    private float snapDelta(float f, float f2, float f3, boolean bl) {
        float f4;
        float f5 = f4 = bl ? MathHelper.wrapDegrees((float)(f2 - f)) : f2 - f;
        if (f3 <= 0.0f || Float.isNaN(f3) || Float.isInfinite(f3)) {
            return f + f4;
        }
        return f + (float)Math.round(f4 / f3) * f3;
    }
}

