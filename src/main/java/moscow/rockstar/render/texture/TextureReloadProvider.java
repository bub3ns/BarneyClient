/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.render.texture;

import moscow.rockstar.render.texture.TextureReloadTask;

public final class TextureReloadProvider {
    private static volatile TextureReloadTask textureReloadTask;

    private TextureReloadProvider() {
    }

    public static TextureReloadTask getTextureReloadTask() {
        TextureReloadTask textureReloadTask = TextureReloadProvider.textureReloadTask;
        if (textureReloadTask == null) {
            throw new IllegalStateException("Newton API is not initialised yet (initialise after Newton mod is loaded)");
        }
        return textureReloadTask;
    }

    public static boolean isInstalled() {
        return textureReloadTask != null;
    }

    public static void install(TextureReloadTask textureReloadTask) {
        if (TextureReloadProvider.textureReloadTask != null) {
            throw new IllegalStateException("Newton API already installed");
        }
        TextureReloadProvider.textureReloadTask = textureReloadTask;
    }
}

