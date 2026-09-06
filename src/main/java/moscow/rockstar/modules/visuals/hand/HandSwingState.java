/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.modules.visuals.hand;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;

public final class HandSwingState {
    private final float anchorX;
    private final float anchorY;
    private final float anchorZ;
    private final float moveX;
    private final float moveY;
    private final float moveZ;
    private final float rotateX;
    private final float rotateY;
    private final float rotateZ;

    public HandSwingState(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9) {
        this.anchorX = f;
        this.anchorY = f2;
        this.anchorZ = f3;
        this.moveX = f4;
        this.moveY = f5;
        this.moveZ = f6;
        this.rotateX = f7;
        this.rotateY = f8;
        this.rotateZ = f9;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "anchorX", "anchorY", "anchorZ", "moveX", "moveY", "moveZ", "rotateX", "rotateY", "rotateZ");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "anchorX", "anchorY", "anchorZ", "moveX", "moveY", "moveZ", "rotateX", "rotateY", "rotateZ");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "anchorX", "anchorY", "anchorZ", "moveX", "moveY", "moveZ", "rotateX", "rotateY", "rotateZ");
    }

    public float getAnchorX() {
        return this.anchorX;
    }

    public float getAnchorY() {
        return this.anchorY;
    }

    public float getAnchorZ() {
        return this.anchorZ;
    }

    public float getMoveX() {
        return this.moveX;
    }

    public float getMoveY() {
        return this.moveY;
    }

    public float getMoveZ() {
        return this.moveZ;
    }

    public float getRotateX() {
        return this.rotateX;
    }

    public float getRotateY() {
        return this.rotateY;
    }

    public float getRotateZ() {
        return this.rotateZ;
    }
}

