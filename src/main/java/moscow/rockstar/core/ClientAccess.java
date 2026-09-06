/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.core;

import net.minecraft.client.MinecraftClient;

public interface ClientAccess {
    /** Shared Minecraft client instance used by the original client code. */
    public static final MinecraftClient minecraftClient = MinecraftClient.getInstance();
}
