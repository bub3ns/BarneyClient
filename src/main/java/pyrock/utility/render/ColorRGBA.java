/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  lombok.Generated
 *  net.minecraft.MathHelper
 *  org.lwjgl.opengl.GL11
 */
package pyrock.utility.render;

import com.google.gson.JsonObject;
import java.nio.ByteBuffer;
import java.util.Objects;
import lombok.Generated;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.render.gl.OpenGlStateReset;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class ColorRGBA {
    public static final ColorRGBA WHITE = new ColorRGBA(255.0f, 255.0f, 255.0f);
    public static final ColorRGBA BLACK = new ColorRGBA(0.0f, 0.0f, 0.0f);
    public static final ColorRGBA GREEN = new ColorRGBA(0.0f, 255.0f, 0.0f);
    public static final ColorRGBA RED = new ColorRGBA(255.0f, 0.0f, 0.0f);
    public static final ColorRGBA BLUE = new ColorRGBA(0.0f, 0.0f, 255.0f);
    public static final ColorRGBA YELLOW = new ColorRGBA(255.0f, 255.0f, 0.0f);
    private transient float[] hsbValues;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private static final ByteBuffer PIXEL_BUFFER = ByteBuffer.allocateDirect(4);

    public ColorRGBA(float f, float f2, float f3) {
        this(f, f2, f3, 255.0f);
    }

    public ColorRGBA(float f, float f2, float f3, float f4) {
        f = MathHelper.clamp((float)f, (float)0.0f, (float)255.0f);
        f2 = MathHelper.clamp((float)f2, (float)0.0f, (float)255.0f);
        f3 = MathHelper.clamp((float)f3, (float)0.0f, (float)255.0f);
        f4 = MathHelper.clamp((float)f4, (float)0.0f, (float)255.0f);
        this.red = f;
        this.green = f2;
        this.blue = f3;
        this.alpha = f4;
    }

    public int getRGB() {
        int n = Math.round(this.clamp(this.alpha));
        int n2 = Math.round(this.clamp(this.red));
        int n3 = Math.round(this.clamp(this.green));
        int n4 = Math.round(this.clamp(this.blue));
        return (n & 0xFF) << 24 | (n2 & 0xFF) << 16 | (n3 & 0xFF) << 8 | n4 & 0xFF;
    }

    public String toHex() {
        return String.format("#%02x%02x%02x%02x", Math.round(this.clamp(this.red)), Math.round(this.clamp(this.green)), Math.round(this.clamp(this.blue)), Math.round(this.clamp(this.alpha)));
    }

    private float clamp(float f) {
        return Math.max(0.0f, Math.min(255.0f, f));
    }

    public static ColorRGBA fromHex(String string) {
        String string2;
        String string3 = string2 = string.startsWith("#") ? string.substring(1) : string;
        if (string2.length() != 6 && string2.length() != 8) {
            throw new IllegalArgumentException("Hex color must be in the format #RRGGBB or #RRGGBBAA");
        }
        float f = Integer.parseInt(string2.substring(0, 2), 16);
        float f2 = Integer.parseInt(string2.substring(2, 4), 16);
        float f3 = Integer.parseInt(string2.substring(4, 6), 16);
        float f4 = string2.length() == 8 ? (float)Integer.parseInt(string2.substring(6, 8), 16) : 255.0f;
        return new ColorRGBA(f, f2, f3, f4);
    }

    public static ColorRGBA fromInt(int n) {
        float f = n >> 24 & 0xFF;
        float f2 = n >> 16 & 0xFF;
        float f3 = n >> 8 & 0xFF;
        float f4 = n & 0xFF;
        return new ColorRGBA(f2, f3, f4, f);
    }

    public ColorRGBA withAlpha(float f) {
        return new ColorRGBA(this.red, this.green, this.blue, f);
    }

    public ColorRGBA mulAlpha(float f) {
        return this.withAlpha(this.alpha * f);
    }

    public ColorRGBA mix(ColorRGBA colorRGBA, float f) {
        f = Math.min(1.0f, Math.max(0.0f, f));
        return new ColorRGBA(MathUtils.interpolateDouble(this.getRed(), colorRGBA.getRed(), f), MathUtils.interpolateDouble(this.getGreen(), colorRGBA.getGreen(), f), MathUtils.interpolateDouble(this.getBlue(), colorRGBA.getBlue(), f), MathUtils.interpolateDouble(this.getAlpha(), colorRGBA.getAlpha(), f));
    }

    public static ColorRGBA fromHSB(float f, float f2, float f3) {
        if (f2 == 0.0f) {
            int n = (int)(f3 * 255.0f + 0.5f);
            return new ColorRGBA(n, n, n);
        }
        float f4 = (f - (float)Math.floor(f)) * 6.0f;
        float f5 = f4 - (float)Math.floor(f4);
        float f6 = f3 * (1.0f - f2);
        float f7 = f3 * (1.0f - f2 * f5);
        float f8 = f3 * (1.0f - f2 * (1.0f - f5));
        float f9 = 0.0f;
        float f10 = 0.0f;
        float f11 = 0.0f;
        switch ((int)f4) {
            case 0: {
                f9 = f3;
                f10 = f8;
                f11 = f6;
                break;
            }
            case 1: {
                f9 = f7;
                f10 = f3;
                f11 = f6;
                break;
            }
            case 2: {
                f9 = f6;
                f10 = f3;
                f11 = f8;
                break;
            }
            case 3: {
                f9 = f6;
                f10 = f7;
                f11 = f3;
                break;
            }
            case 4: {
                f9 = f8;
                f10 = f6;
                f11 = f3;
                break;
            }
            case 5: {
                f9 = f3;
                f10 = f6;
                f11 = f7;
            }
        }
        return new ColorRGBA(f9 * 255.0f, f10 * 255.0f, f11 * 255.0f);
    }

    public float getHue() {
        return this.getHSBValues()[0];
    }

    public float getSaturation() {
        return this.getHSBValues()[2];
    }

    public float getBrightness() {
        return this.getHSBValues()[1];
    }

    private float[] getHSBValues() {
        if (this.hsbValues == null) {
            this.hsbValues = this.calculateHSB();
        }
        return this.hsbValues;
    }

    private float[] calculateHSB() {
        float f = this.red / 255.0f;
        float f2 = this.green / 255.0f;
        float f3 = this.blue / 255.0f;
        float f4 = Math.max(f, Math.max(f2, f3));
        float f5 = Math.min(f, Math.min(f2, f3));
        float f6 = f4 - f5;
        float f7 = 0.0f;
        if (f6 != 0.0f) {
            f7 = f4 == f ? (f2 - f3) / f6 : (f4 == f2 ? (f3 - f) / f6 + 2.0f : (f - f2) / f6 + 4.0f);
            if ((f7 /= 6.0f) < 0.0f) {
                f7 += 1.0f;
            }
        }
        float f8 = f4 == 0.0f ? 0.0f : f6 / f4;
        float f9 = f4;
        return new float[]{f7, f8, f9};
    }

    public static ColorRGBA fromPixel(float f, float f2) {
        PIXEL_BUFFER.clear();
        OpenGlStateReset.resetPixelUnpackState();
        GL11.glReadPixels((int)((int)f), (int)((int)f2), (int)1, (int)1, (int)6408, (int)5121, (ByteBuffer)PIXEL_BUFFER);
        int n = PIXEL_BUFFER.get(0) & 0xFF;
        int n2 = PIXEL_BUFFER.get(1) & 0xFF;
        int n3 = PIXEL_BUFFER.get(2) & 0xFF;
        return new ColorRGBA(n, n2, n3);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        ColorRGBA colorRGBA = (ColorRGBA)object;
        return Float.compare(this.red, colorRGBA.red) == 0 && Float.compare(this.green, colorRGBA.green) == 0 && Float.compare(this.blue, colorRGBA.blue) == 0 && Float.compare(this.alpha, colorRGBA.alpha) == 0;
    }

    public float difference(ColorRGBA colorRGBA) {
        return Math.abs(this.getHue() - colorRGBA.getHue()) + Math.abs(this.getBrightness() - colorRGBA.getBrightness()) + Math.abs(this.getSaturation() - colorRGBA.getSaturation());
    }

    public static ColorRGBA applyOpacity(int n, float f) {
        ColorRGBA colorRGBA = ColorRGBA.fromInt(n);
        float f2 = MathHelper.clamp((float)f, (float)0.0f, (float)1.0f);
        return new ColorRGBA(colorRGBA.getRed(), colorRGBA.getGreen(), colorRGBA.getBlue(), colorRGBA.getAlpha() * f2);
    }

    public static ColorRGBA darken(int n, float f) {
        ColorRGBA colorRGBA = ColorRGBA.fromInt(n);
        float f2 = MathHelper.clamp((float)f, (float)0.0f, (float)1.0f);
        return new ColorRGBA(colorRGBA.getRed() * f2, colorRGBA.getGreen() * f2, colorRGBA.getBlue() * f2, colorRGBA.getAlpha());
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.red), Float.valueOf(this.green), Float.valueOf(this.blue), Float.valueOf(this.alpha));
    }

    public String toString() {
        return String.format("RGBA(%.1f, %.1f, %.1f, %.1f)", Float.valueOf(this.red), Float.valueOf(this.green), Float.valueOf(this.blue), Float.valueOf(this.alpha));
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("r", (Number)Float.valueOf(this.getRed()));
        jsonObject.addProperty("g", (Number)Float.valueOf(this.getGreen()));
        jsonObject.addProperty("b", (Number)Float.valueOf(this.getBlue()));
        jsonObject.addProperty("a", (Number)Float.valueOf(this.getAlpha()));
        return jsonObject;
    }

    public static ColorRGBA fromJson(JsonObject jsonObject) {
        if (jsonObject == null) {
            return new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        }
        int n = jsonObject.has("r") ? jsonObject.get("r").getAsInt() : 0;
        int n2 = jsonObject.has("g") ? jsonObject.get("g").getAsInt() : 0;
        int n3 = jsonObject.has("b") ? jsonObject.get("b").getAsInt() : 0;
        int n4 = jsonObject.has("a") ? jsonObject.get("a").getAsInt() : 255;
        return new ColorRGBA(n, n2, n3, n4);
    }

    @Generated
    public float getRed() {
        return this.red;
    }

    @Generated
    public float getGreen() {
        return this.green;
    }

    @Generated
    public float getBlue() {
        return this.blue;
    }

    @Generated
    public float getAlpha() {
        return this.alpha;
    }
}

