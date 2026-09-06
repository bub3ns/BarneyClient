/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.combat.rotation;

import ua.mintantileak.spk.Compile;

public enum RotationCorrectionMode {
    NONE(false, false, false, false, false),
    DIRECT(true, true, false, false, false),
    STRICT(true, true, false, false, false),
    UNSPECIFIED(true, true, true, false, false),
    SMOOTH(true, true, false, true, false),
    CHANGE_LOOK(true, true, false, false, true),
    TARGETED(true, true, false, false, false);
    private final boolean movementCorrectionEnabled;
    private final boolean jumpDirectionCorrectionEnabled;
    private final boolean directYawEnabled;
    private final boolean smoothYawEnabled;
    private final boolean cameraLookEnabled;

    private RotationCorrectionMode(boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
        this.movementCorrectionEnabled = bl;
        this.jumpDirectionCorrectionEnabled = bl2;
        this.directYawEnabled = bl3;
        this.smoothYawEnabled = bl4;
        this.cameraLookEnabled = bl5;
    }

    @Compile(obfuscation=1)
    public boolean correctsMovement() {
        return this.movementCorrectionEnabled;
    }

    @Compile(obfuscation=1)
    public boolean correctsJumpDirection() {
        return this.jumpDirectionCorrectionEnabled;
    }

    @Compile(obfuscation=1)
    public boolean appliesDirectYaw() {
        return this.directYawEnabled;
    }

    @Compile(obfuscation=1)
    public boolean appliesSmoothYaw() {
        return this.smoothYawEnabled;
    }

    @Compile(obfuscation=1)
    public boolean changesCameraLook() {
        return this.cameraLookEnabled;
    }
}

