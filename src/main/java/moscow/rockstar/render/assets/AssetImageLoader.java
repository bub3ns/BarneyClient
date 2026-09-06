/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.NativeImage
 *  net.minecraft.NativeImageBackedTexture
 *  net.minecraft.AbstractTexture
 *  net.minecraft.Identifier
 */
package moscow.rockstar.render.assets;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.texture.ImageTextureConverter;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;

public final class AssetImageLoader {
    private static final String FALLBACK_IMAGE_RESOURCE = "image/mainmenu/fallback.jpg";
    private static final Map<String, Identifier> TEXTURES_BY_PATH = new ConcurrentHashMap<String, Identifier>();
    private static final Map<String, Float> ASPECT_RATIOS_BY_PATH = new ConcurrentHashMap<String, Float>();
    private static final Set<String> LOADING_PATHS = ConcurrentHashMap.newKeySet();
    private static final ExecutorService imageLoadExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Barney-AssetPack");
        thread.setDaemon(true);
        return thread;
    });
    private static volatile Identifier fallbackTextureId;

    private AssetImageLoader() {
    }

    public static File getAssetDirectory() {
        return moscow.rockstar.core.ClientPaths.resolve("Barney", "assets");
    }

    public static boolean isAssetLoaded(String string) {
        if (TEXTURES_BY_PATH.containsKey(string)) {
            return true;
        }
        AssetImageLoader.getTexture(string);
        return false;
    }

    public static Identifier getTexture(String string) {
        if (string == null) {
            return AssetImageLoader.getFallbackTextureId();
        }
        Identifier class_29602 = TEXTURES_BY_PATH.get(string);
        if (class_29602 != null) {
            return class_29602;
        }
        if (AssetImageLoader.tryLoadClasspathTexture(string)) {
            class_29602 = TEXTURES_BY_PATH.get(string);
            if (class_29602 != null) {
                return class_29602;
            }
        }
        File file = AssetImageLoader.resolveAssetFile(string);
        if (file != null && LOADING_PATHS.add(string)) {
            imageLoadExecutor.execute(() -> AssetImageLoader.loadImageFile(string, file));
        }
        return AssetImageLoader.getFallbackTextureId();
    }

    private static boolean tryLoadClasspathTexture(String string) {
        for (String ext : new String[]{".jpg", ".png"}) {
            String path = "/assets/rockstar/" + string + ext;
            try (InputStream inputStream = AssetImageLoader.class.getResourceAsStream(path)) {
                if (inputStream != null) {
                    BufferedImage bufferedImage = ImageIO.read(inputStream);
                    if (bufferedImage != null) {
                        AssetImageLoader.uploadTextureSyncOrAsync(string, bufferedImage, RockstarClient.resourceId("pack/" + string + ".png"));
                        return true;
                    }
                }
            } catch (Throwable throwable) {
                RockstarClient.LOGGER.warn("[Assets] Error loading classpath texture {}: {}", path, throwable.toString());
            }
        }
        return false;
    }

    public static float getTextureAspectRatio(String string) {
        return ASPECT_RATIOS_BY_PATH.getOrDefault(string, Float.valueOf(0.0f)).floatValue();
    }

    public static void loadAssets() {
        ArrayList<Identifier> arrayList = new ArrayList<Identifier>(TEXTURES_BY_PATH.values());
        TEXTURES_BY_PATH.clear();
        ASPECT_RATIOS_BY_PATH.clear();
        LOADING_PATHS.clear();
        ClientAccess.minecraftClient.execute(() -> arrayList.forEach(class_29602 -> ClientAccess.minecraftClient.getTextureManager().destroyTexture(class_29602)));
    }

    private static void loadImageFile(String string, File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage == null) {
                RockstarClient.LOGGER.warn("[Assets] \u043d\u0435 \u043f\u043e\u043d\u044f\u043b \u0444\u043e\u0440\u043c\u0430\u0442 {}", (Object)file);
                return;
            }
            AssetImageLoader.uploadTextureSyncOrAsync(string, bufferedImage, RockstarClient.resourceId("pack/" + string + ".png"));
        }
        catch (Throwable throwable) {
            RockstarClient.LOGGER.warn("[Assets] \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c {}: {}", (Object)file, (Object)throwable.toString());
            LOADING_PATHS.remove(string);
        }
    }

    private static Identifier getFallbackTextureId() {
        Identifier class_29602 = fallbackTextureId;
        if (class_29602 != null) {
            return class_29602;
        }
        fallbackTextureId = class_29602 = RockstarClient.resourceId("pack/fallback.png");
        try (InputStream inputStream = AssetImageLoader.class.getResourceAsStream("/assets/rockstar/image/mainmenu/fallback.jpg");){
            BufferedImage bufferedImage;
            if (inputStream != null && (bufferedImage = ImageIO.read(inputStream)) != null) {
                AssetImageLoader.uploadTextureSyncOrAsync(FALLBACK_IMAGE_RESOURCE, bufferedImage, class_29602);
            }
        }
        catch (Throwable throwable) {
            RockstarClient.LOGGER.warn("[Assets] \u0432\u0441\u0442\u0440\u043e\u0435\u043d\u043d\u0430\u044f \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u043b\u0430\u0441\u044c: {}", (Object)throwable.toString());
        }
        return class_29602;
    }

    private static void uploadTextureSyncOrAsync(String string, BufferedImage bufferedImage, Identifier class_29602) {
        NativeImage nativeImage = ImageTextureConverter.fromBufferedImage(bufferedImage);
        float f = (float)bufferedImage.getWidth() / (float)Math.max(1, bufferedImage.getHeight());
        Runnable action = () -> {
            ClientAccess.minecraftClient.getTextureManager().registerTexture(class_29602, (AbstractTexture)new NativeImageBackedTexture(nativeImage));
            ASPECT_RATIOS_BY_PATH.put(string, Float.valueOf(f));
            TEXTURES_BY_PATH.put(string, class_29602);
            LOADING_PATHS.remove(string);
        };
        if (ClientAccess.minecraftClient.isOnThread()) {
            action.run();
        } else {
            ClientAccess.minecraftClient.execute(action);
        }
    }

    private static void uploadTexture(String string, BufferedImage bufferedImage, Identifier class_29602) {
        AssetImageLoader.uploadTextureSyncOrAsync(string, bufferedImage, class_29602);
    }

    private static File resolveAssetFile(String string) {
        for (String string2 : new String[]{".jpg", ".png"}) {
            File file = new File(AssetImageLoader.getAssetDirectory(), string + string2);
            if (!file.isFile()) continue;
            return file;
        }
        return null;
    }
}
