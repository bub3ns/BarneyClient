/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  jep.python.PyCallable
 *  net.minecraft.NativeImage
 *  net.minecraft.NativeImage$Format
 *  net.minecraft.NativeImageBackedTexture
 *  net.minecraft.AbstractTexture
 *  net.minecraft.Identifier
 */
package pyrock.utility.render;

import com.google.gson.Gson;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import jep.python.PyCallable;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.render.texture.ImageTextureConverter;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.text.FontRenderer;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.render.assets.RemoteAssetCache;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import pyrock.utility.render.PyDynamicTexture;
import pyrock.utility.render.PyPcmStream;

public class PyAssets {
    private static final Gson GSON = new Gson();
    private static final Map<String, TextureEntry> TEXTURES = new ConcurrentHashMap<String, TextureEntry>();
    private static final Map<String, FontRenderer> FONTS = new ConcurrentHashMap<String, FontRenderer>();
    private static final Map<String, Identifier> WEB_TEXTURES = new ConcurrentHashMap<String, Identifier>();

    public Identifier resource(String string) {
        return RockstarClient.resourceId(PyAssets.normalizeIdentifierPath(string));
    }

    public Identifier image(String string) {
        return this.image(null, string);
    }

    public Identifier texture(String string) {
        return this.image(string);
    }

    public PyDynamicTexture dynamicTexture(String string, int n, int n2) {
        return new PyDynamicTexture(string, n, n2);
    }

    public PyPcmStream pcmStream(float f, int n) {
        return new PyPcmStream(f, n);
    }

    public Identifier image(String string, String string2) {
        if (RemoteAssetCache.isRemoteUrl(string2)) {
            return this.webImage(string, string2);
        }
        Path path = PyAssets.resolve(string2);
        if (!Files.isRegularFile(path, new LinkOption[0])) {
            throw new IllegalArgumentException("image file not found: " + String.valueOf(path));
        }
        try {
            Path path2 = path.toAbsolutePath().normalize();
            long l = Files.getLastModifiedTime(path2, new LinkOption[0]).toMillis();
            String string3 = path2.toString();
            TextureEntry textureEntry = TEXTURES.get(string3);
            if (textureEntry != null && textureEntry.modified == l) {
                return textureEntry.id;
            }
            BufferedImage bufferedImage = ImageIO.read(path2.toFile());
            if (bufferedImage == null) {
                throw new IllegalArgumentException("unsupported image file: " + String.valueOf(path2));
            }
            NativeImage nativeImage = ImageTextureConverter.fromBufferedImage(bufferedImage);
            Identifier class_29602 = RockstarClient.resourceId("scripts/images/" + PyAssets.textureName(string, path2));
            MinecraftClient client = MinecraftClient.getInstance();
            if (textureEntry != null && !textureEntry.id.equals((Object)class_29602)) {
                client.getTextureManager().destroyTexture(textureEntry.id);
            }
            client.getTextureManager().registerTexture(class_29602, (AbstractTexture)new NativeImageBackedTexture(nativeImage));
            TEXTURES.put(string3, new TextureEntry(class_29602, l));
            return class_29602;
        }
        catch (IOException iOException) {
            throw new RuntimeException("failed to load image: " + String.valueOf(path), iOException);
        }
    }

    private Identifier webImage(String string, String string2) {
        Identifier class_29602 = WEB_TEXTURES.get(string2);
        if (class_29602 != null) {
            return class_29602;
        }
        Identifier class_29603 = RockstarClient.resourceId("scripts/web/" + PyAssets.sanitize(string == null || string.isBlank() ? "image" : string) + "_" + Integer.toHexString(string2.hashCode()) + ".png");
        Identifier class_29604 = WEB_TEXTURES.putIfAbsent(string2, class_29603);
        if (class_29604 != null) {
            return class_29604;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        client.getTextureManager().registerTexture(class_29603, (AbstractTexture)new NativeImageBackedTexture(new NativeImage(NativeImage.Format.RGBA, 1, 1, false)));
        RemoteAssetCache.fetchAsset(string2, path -> {
            try {
                BufferedImage bufferedImage = ImageIO.read(path.toFile());
                if (bufferedImage == null) {
                    throw new IllegalArgumentException("\u044d\u0442\u043e \u043d\u0435 \u043a\u0430\u0440\u0442\u0438\u043d\u043a\u0430");
                }
                client.getTextureManager().registerTexture(class_29603, (AbstractTexture)new NativeImageBackedTexture(ImageTextureConverter.fromBufferedImage(bufferedImage)));
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.warn("\u0410\u0441\u0441\u0435\u0442\u044b: \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u043a\u0430\u0440\u0442\u0438\u043d\u043a\u0443 {}: {}", (Object)string2, (Object)exception.getMessage());
            }
        });
        return class_29603;
    }

    public String download(String string, PyCallable pyCallable) {
        if (!RemoteAssetCache.isRemoteUrl(string)) {
            Path path2 = PyAssets.resolve(string);
            if (!Files.isRegularFile(path2, new LinkOption[0])) {
                throw new IllegalArgumentException("file not found: " + String.valueOf(path2));
            }
            String string2 = path2.toString();
            PyAssets.deliver(pyCallable, string2);
            return string2;
        }
        Path path3 = RemoteAssetCache.findCachedAsset(string);
        if (path3 != null) {
            String string3 = path3.toString();
            PyAssets.deliver(pyCallable, string3);
            return string3;
        }
        RemoteAssetCache.fetchAsset(string, path -> PyAssets.deliver(pyCallable, path.toString()));
        return null;
    }

    private static void deliver(PyCallable pyCallable, String string) {
        if (pyCallable == null) {
            return;
        }
        ScriptDescriptor scriptDescriptor = ScriptDescriptor.getCurrentScript();
        MinecraftClient.getInstance().execute(() -> {
            if (scriptDescriptor != null && !scriptDescriptor.isLoaded()) {
                return;
            }
            if (!moscow.rockstar.scripts.python.PythonRuntime.isAvailable()) {
                return;
            }
            try (AutoCloseable autoCloseable = ScriptDescriptor.pushCurrentScript(scriptDescriptor);){
                pyCallable.call(new Object[]{string});
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.error("Python error in download callback:", (Throwable)exception);
            }
        });
    }

    public FontMetrics font(String string, float f) {
        return PyAssets.slugFont(string).metrics(f);
    }

    public static FontRenderer slugFont(String string) {
        FontRenderer fontRenderer = FONTS.get(PyAssets.fontName(string));
        return fontRenderer != null ? fontRenderer : moscow.rockstar.ui.text.Font.byName(string);
    }

    public FontRenderer ttfFamily(String string, String string2) {
        String string3 = PyAssets.fontName(string);
        FontRenderer fontRenderer = FONTS.get(string3);
        if (fontRenderer != null) {
            return fontRenderer;
        }
        if (RemoteAssetCache.isRemoteUrl(string2)) {
            return this.webFont(string3, string2);
        }
        Path path = PyAssets.resolve(string2);
        if (!Files.isRegularFile(path, new LinkOption[0])) {
            throw new IllegalArgumentException("font file not found: " + String.valueOf(path));
        }
        try {
            Font font = Font.createFont(0, path.toFile());
            FontRenderer created = FontRenderer.fromAwtFont(string3, font);
            FONTS.put(string3, created);
            return created;
        }
        catch (Exception exception) {
            throw new RuntimeException("failed to load font: " + string, exception);
        }
    }

    private FontRenderer webFont(String string, String string2) {
        FontRenderer fontRenderer;
        Path path2 = RemoteAssetCache.findCachedAsset(string2);
        if (path2 != null && (fontRenderer = this.readFont(string, path2)) != null) {
            FONTS.put(string, fontRenderer);
            return fontRenderer;
        }
        fontRenderer = FontRenderer.empty(string);
        FONTS.put(string, fontRenderer);
        FontRenderer targetFont = fontRenderer;
        RemoteAssetCache.fetchAsset(string2, path -> {
            try {
                targetFont.setAwtFont(Font.createFont(Font.TRUETYPE_FONT, path.toFile()));
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.warn("\u0410\u0441\u0441\u0435\u0442\u044b: \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u0448\u0440\u0438\u0444\u0442 {}: {}", (Object)string2, (Object)exception.getMessage());
            }
        });
        return fontRenderer;
    }

    private FontRenderer readFont(String string, Path path) {
        try {
            return FontRenderer.fromAwtFont(string, Font.createFont(Font.TRUETYPE_FONT, path.toFile()));
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.warn("\u0410\u0441\u0441\u0435\u0442\u044b: \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c \u0448\u0440\u0438\u0444\u0442 {}: {}", (Object)path, (Object)exception.getMessage());
            return null;
        }
    }

    public String scriptsDir() {
        return PyAssets.scriptsRoot().toString();
    }

    public String assetsDir() {
        return PyAssets.scriptsRoot().toString();
    }

    public static Path resolve(String string) {
        Path path = Path.of(string, new String[0]);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        Path path2 = PyAssets.scriptsRoot().resolve(path).normalize();
        if (Files.exists(path2, new LinkOption[0])) {
            return path2;
        }
        return Path.of(moscow.rockstar.core.ClientPaths.gameDirectory().toURI()).resolve(path).normalize();
    }

    private static Path scriptsRoot() {
        return Path.of(moscow.rockstar.core.ClientPaths.gameDirectory().toURI()).resolve("scripts");
    }

    private static String fontName(String string) {
        return PyAssets.sanitize(string == null || string.isBlank() ? "custom" : string);
    }

    private static String textureName(String string, Path path) {
        String string2 = string == null || string.isBlank() ? PyAssets.stripExtension(path.getFileName().toString()) : string;
        String string3 = PyAssets.extension(path.getFileName().toString());
        String string4 = Integer.toHexString(path.toString().hashCode());
        return PyAssets.sanitize(string2) + "_" + string4 + (string3.isBlank() ? ".png" : string3);
    }

    private static String stripExtension(String string) {
        int n = string.lastIndexOf(46);
        return n <= 0 ? string : string.substring(0, n);
    }

    private static String extension(String string) {
        int n = string.lastIndexOf(46);
        return n <= 0 ? "" : string.substring(n).toLowerCase(Locale.ROOT);
    }

    private static String normalizeIdentifierPath(String string) {
        return string.replace('\\', '/').replaceAll("^/+", "");
    }

    private static String sanitize(String string) {
        String string2 = string.toLowerCase(Locale.ROOT).replace('\\', '/').replaceAll("[^a-z0-9/._-]", "_").replaceAll("_+", "_").replaceAll("^_+|_+$", "");
        return string2.isBlank() ? "asset" : string2;
    }

    static final class TextureEntry {
        final Identifier id;
        final long modified;

        TextureEntry(Identifier class_29602, long l) {
            this.id = class_29602;
            this.modified = l;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "id", "modified");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "id", "modified");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "id", "modified");
        }

        public Identifier id() {
            return this.id;
        }

        public long modified() {
            return this.modified;
        }
    }
}
