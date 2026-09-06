/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Window
 */
package moscow.rockstar.platform;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public interface WindowHandle {
    public static final Window WINDOW = MinecraftClient.getInstance().getWindow();
}

