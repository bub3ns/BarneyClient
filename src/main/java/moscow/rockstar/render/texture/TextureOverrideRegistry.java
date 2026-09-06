/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SkinTextures
 */
package moscow.rockstar.render.texture;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import moscow.rockstar.render.texture.TextureOverrideState;
import net.minecraft.client.util.SkinTextures;

public final class TextureOverrideRegistry {
    public static final String DEFAULT_OVERRIDE_KEY = "*";
    private static final Map<String, TextureOverrideState> overridesByKey = new ConcurrentHashMap<String, TextureOverrideState>();

    private TextureOverrideRegistry() {
    }

    public static TextureOverrideState getOrCreateOverride(Object object, String string2) {
        return overridesByKey.compute(TextureOverrideRegistry.normalizeOverrideKey(string2), (string, textureOverrideState) -> textureOverrideState != null && textureOverrideState.owner == object ? textureOverrideState : new TextureOverrideState(object));
    }

    public static TextureOverrideState findOverride(String string) {
        if (overridesByKey.isEmpty() || string == null || string.isEmpty()) {
            return null;
        }
        TextureOverrideState textureOverrideState = overridesByKey.get(TextureOverrideRegistry.normalizeOverrideKey(string));
        return textureOverrideState != null ? textureOverrideState : overridesByKey.get(DEFAULT_OVERRIDE_KEY);
    }

    public static void removeEmptyOverride(String string2) {
        overridesByKey.computeIfPresent(TextureOverrideRegistry.normalizeOverrideKey(string2), (string, textureOverrideState) -> textureOverrideState.isEmpty() ? null : textureOverrideState);
    }

    public static void removeOverride(String string) {
        overridesByKey.remove(TextureOverrideRegistry.normalizeOverrideKey(string));
    }

    public static void removeOverridesOwnedBy(Object object) {
        overridesByKey.values().removeIf(textureOverrideState -> textureOverrideState.owner == object);
    }

    public static void clearOverrides() {
        overridesByKey.clear();
    }

    public static boolean hasNoOverrides() {
        return overridesByKey.isEmpty();
    }

    public static List<String> getOverrideKeys() {
        return new ArrayList<String>(overridesByKey.keySet());
    }

    public static SkinTextures applyOverride(String string, SkinTextures class_86852) {
        if (class_86852 == null) {
            return null;
        }
        TextureOverrideState textureOverrideState = TextureOverrideRegistry.findOverride(string);
        if (textureOverrideState == null || textureOverrideState.isEmpty()) {
            return class_86852;
        }
        return new SkinTextures(textureOverrideState.primaryTextureId != null ? textureOverrideState.primaryTextureId : class_86852.texture(), class_86852.textureUrl(), textureOverrideState.useDefaultModel ? null : (textureOverrideState.secondaryTextureId != null ? textureOverrideState.secondaryTextureId : class_86852.capeTexture()), textureOverrideState.tertiaryTextureId != null ? textureOverrideState.tertiaryTextureId : class_86852.elytraTexture(), textureOverrideState.modelVariant != null ? textureOverrideState.modelVariant : class_86852.model(), class_86852.secure());
    }

    public static boolean hasSecondaryTexture(String string) {
        TextureOverrideState textureOverrideState = TextureOverrideRegistry.findOverride(string);
        return textureOverrideState != null && textureOverrideState.secondaryTextureId != null;
    }

    private static String normalizeOverrideKey(String string) {
        if (string == null) {
            return DEFAULT_OVERRIDE_KEY;
        }
        String string2 = string.trim();
        return string2.isEmpty() ? DEFAULT_OVERRIDE_KEY : string2.toLowerCase(Locale.ROOT);
    }
}
