/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Identifier
 */
package moscow.rockstar.render.texture;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.util.Identifier;

public final class TextureRegion {
    private final Identifier texture;
    private final float u1;
    private final float v1;
    private final float u2;
    private final float v2;
    private final int width;
    private final int height;

    public TextureRegion(Identifier class_29602, float f, float f2, float f3, float f4, int n, int n2) {
        this.texture = class_29602;
        this.u1 = f;
        this.v1 = f2;
        this.u2 = f3;
        this.v2 = f4;
        this.width = n;
        this.height = n2;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "texture", "u1", "v1", "u2", "v2", "width", "height");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "texture", "u1", "v1", "u2", "v2", "width", "height");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "texture", "u1", "v1", "u2", "v2", "width", "height");
    }

    public Identifier getTexture() {
        return this.texture;
    }

    public float getU1() {
        return this.u1;
    }

    public float getV1() {
        return this.v1;
    }

    public float getU2() {
        return this.u2;
    }

    public float getV2() {
        return this.v2;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}

