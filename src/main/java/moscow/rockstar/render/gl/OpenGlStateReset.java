/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  org.lwjgl.opengl.GL15
 */
package moscow.rockstar.render.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL15;

public final class OpenGlStateReset {
    private OpenGlStateReset() {
    }

    public static void resetPixelUnpackState() {
        GL15.glBindBuffer((int)35052, (int)0);
        GlStateManager._pixelStore((int)3317, (int)1);
        GlStateManager._pixelStore((int)3314, (int)0);
        GlStateManager._pixelStore((int)3315, (int)0);
        GlStateManager._pixelStore((int)3316, (int)0);
    }

    public static void resetPixelPackState() {
        GL15.glBindBuffer((int)35051, (int)0);
        GlStateManager._pixelStore((int)3333, (int)1);
        GlStateManager._pixelStore((int)3330, (int)0);
        GlStateManager._pixelStore((int)3331, (int)0);
        GlStateManager._pixelStore((int)3332, (int)0);
    }
}
