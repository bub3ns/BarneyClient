/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.NativeImage
 *  net.minecraft.NativeImageBackedTexture
 *  net.minecraft.AbstractTexture
 *  net.minecraft.Identifier
 *  net.minecraft.Resource
 *  net.minecraft.ResourceManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package moscow.rockstar.render.texture;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.render.texture.TextureAnimationManifest;
import moscow.rockstar.render.texture.TextureRegion;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextureAnimationAtlas {
    private static final Logger ATLAS_LOGGER = LoggerFactory.getLogger(TextureAnimationAtlas.class);
    private static final List<TextureAnimationAtlas> registeredAnimations = new ArrayList<TextureAnimationAtlas>();
    private final Map<Identifier, Animation> animationsByTexture = new HashMap<Identifier, Animation>();
    private final List<Frame> animationFrames = new ArrayList<Frame>();
    private Identifier atlasTextureId;
    private boolean atlasBuilt = false;
    private final int atlasWidth;
    private final int atlasHeight;

    public static TextureAnimationAtlas getOrCreateAtlas(int n, int n2) {
        for (TextureAnimationAtlas textureAnimationAtlas : registeredAnimations) {
            if (textureAnimationAtlas.atlasWidth != n || textureAnimationAtlas.atlasHeight != n2 || textureAnimationAtlas.isBuilt()) continue;
            return textureAnimationAtlas;
        }
        TextureAnimationAtlas textureAnimationAtlas = new TextureAnimationAtlas(n, n2);
        registeredAnimations.add(textureAnimationAtlas);
        return textureAnimationAtlas;
    }

    private TextureAnimationAtlas(int n, int n2) {
        this.atlasWidth = n;
        this.atlasHeight = n2;
    }

    public void registerAnimationFrames(Identifier class_29602, TextureAnimationManifest textureAnimationManifest, List<NativeImage> list) {
        if (this.atlasBuilt) {
            throw new RuntimeException("\u0410\u0442\u043b\u0430\u0441 \u0443\u0436\u0435 \u0441\u043e\u0431\u0440\u0430\u043d! \u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u0443\u0439\u0442\u0435 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 \u0434\u043e \u0432\u044b\u0437\u043e\u0432\u0430 buildAtlas()");
        }
        if (list.isEmpty()) {
            ATLAS_LOGGER.warn("\u041f\u0443\u0441\u0442\u0430\u044f \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u044f: {}", (Object)class_29602);
            return;
        }
        for (NativeImage LootTableData : list) {
            if (LootTableData.getWidth() == this.atlasWidth && LootTableData.getHeight() == this.atlasHeight) continue;
            throw new RuntimeException(String.format("\u0420\u0430\u0437\u043c\u0435\u0440 \u043a\u0430\u0434\u0440\u043e\u0432 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 %s (%dx%d) \u043d\u0435 \u0441\u043e\u0432\u043f\u0430\u0434\u0430\u0435\u0442 \u0441 \u0440\u0430\u0437\u043c\u0435\u0440\u043e\u043c \u044d\u0442\u043e\u0433\u043e \u0430\u0442\u043b\u0430\u0441\u0430 (%dx%d)", class_29602, LootTableData.getWidth(), LootTableData.getHeight(), this.atlasWidth, this.atlasHeight));
        }
        int n = this.animationFrames.size();
        for (int i = 0; i < list.size(); ++i) {
            this.animationFrames.add(new Frame(class_29602, i, list.get(i)));
        }
        Animation animation = new Animation(class_29602, textureAnimationManifest, n, list.size(), null);
        this.animationsByTexture.put(class_29602, animation);
        ATLAS_LOGGER.info("\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u0430 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u044f {} \u0441 {} \u043a\u0430\u0434\u0440\u0430\u043c\u0438 \u0432 \u0430\u0442\u043b\u0430\u0441\u0435 {}x{}", new Object[]{class_29602, list.size(), this.atlasWidth, this.atlasHeight});
    }

    public void loadAnimationArchive(Identifier class_29602) {
        try {
            ResourceManager class_33002 = MinecraftClient.getInstance().getResourceManager();
            Optional optional = class_33002.getResource(class_29602);
            if (optional.isEmpty()) {
                throw new RuntimeException("\u0424\u0430\u0439\u043b \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d: " + String.valueOf(class_29602));
            }
            Resource class_32982 = (Resource)optional.get();
            TextureAnimationManifest textureAnimationManifest = null;
            ArrayList<NativeImage> arrayList = new ArrayList<NativeImage>();
            try (InputStream inputStream = class_32982.getInputStream();
                 ZipInputStream zipInputStream = new ZipInputStream(inputStream);){
                ZipEntry zipEntry;
                TreeMap<String, byte[]> frameDataByName = new TreeMap<String, byte[]>();
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String entryName = zipEntry.getName();
                    if ("meta.json".equals(entryName)) {
                        ByteArrayOutputStream metadata = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) > 0) {
                            metadata.write(buffer, 0, bytesRead);
                        }
                        String string = metadata.toString(StandardCharsets.UTF_8);
                        textureAnimationManifest = TextureAnimationManifest.fromJson(string);
                    } else if (entryName.startsWith("frames/") && entryName.endsWith(".png")) {
                        ByteArrayOutputStream frameData = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) > 0) {
                            frameData.write(buffer, 0, bytesRead);
                        }
                        frameDataByName.put(entryName, frameData.toByteArray());
                    }
                    zipInputStream.closeEntry();
                }
                for (byte[] frameData : frameDataByName.values()) {
                    arrayList.add(NativeImage.read(new ByteArrayInputStream(frameData)));
                }
            }
            if (textureAnimationManifest == null) {
                throw new RuntimeException("\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d meta.json \u0432 " + String.valueOf(class_29602));
            }
            if (arrayList.isEmpty()) {
                throw new RuntimeException("\u041d\u0435\u0442 \u043a\u0430\u0434\u0440\u043e\u0432 \u0434\u043b\u044f \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 " + String.valueOf(class_29602));
            }
            if (((NativeImage)arrayList.get(0)).getWidth() != this.atlasWidth || ((NativeImage)arrayList.get(0)).getHeight() != this.atlasHeight) {
                throw new RuntimeException(String.format("\u0420\u0430\u0437\u043c\u0435\u0440 \u043a\u0430\u0434\u0440\u043e\u0432 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 %s (%dx%d) \u043d\u0435 \u0441\u043e\u0432\u043f\u0430\u0434\u0430\u0435\u0442 \u0441 \u0440\u0430\u0437\u043c\u0435\u0440\u043e\u043c \u044d\u0442\u043e\u0433\u043e \u0430\u0442\u043b\u0430\u0441\u0430 (%dx%d)", class_29602, ((NativeImage)arrayList.get(0)).getWidth(), ((NativeImage)arrayList.get(0)).getHeight(), this.atlasWidth, this.atlasHeight));
            }
            this.registerAnimationFrames(class_29602, textureAnimationManifest, arrayList);
        }
        catch (Exception exception) {
            throw new RuntimeException("\u041e\u0448\u0438\u0431\u043a\u0430 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0438 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 \u0438\u0437 " + String.valueOf(class_29602), exception);
        }
    }

    public void buildAtlas() {
        int n;
        int n2;
        int n3;
        int n4;
        int n5;
        if (this.atlasBuilt) {
            ATLAS_LOGGER.warn("\u0410\u0442\u043b\u0430\u0441 \u0443\u0436\u0435 \u0441\u043e\u0431\u0440\u0430\u043d!");
            return;
        }
        if (this.animationFrames.isEmpty()) {
            ATLAS_LOGGER.warn("\u041d\u0435\u0442 \u043a\u0430\u0434\u0440\u043e\u0432 \u0434\u043b\u044f \u0441\u043e\u0437\u0434\u0430\u043d\u0438\u044f \u0430\u0442\u043b\u0430\u0441\u0430!");
            return;
        }
        int n6 = this.animationFrames.size();
        int n7 = (int)Math.ceil(Math.sqrt(n6));
        int n8 = (int)Math.ceil((double)n6 / (double)n7);
        int n9 = n7 * this.atlasWidth;
        int n10 = n8 * this.atlasHeight;
        NativeImage LootTableData = new NativeImage(n9, n10, false);
        for (n5 = 0; n5 < n9; ++n5) {
            for (n4 = 0; n4 < n10; ++n4) {
                LootTableData.setColorArgb(n5, n4, 0);
            }
        }
        for (n5 = 0; n5 < n6; ++n5) {
            n4 = n5 % n7;
            int n11 = n5 / n7;
            int n12 = n4 * this.atlasWidth;
            n3 = n11 * this.atlasHeight;
            NativeImage BlockFamilyRecipeFactory = this.animationFrames.get((int)n5).image;
            for (n2 = 0; n2 < this.atlasWidth; ++n2) {
                for (n = 0; n < this.atlasHeight; ++n) {
                    LootTableData.setColorArgb(n12 + n2, n3 + n, BlockFamilyRecipeFactory.getColorArgb(n2, n));
                }
            }
        }
        this.atlasTextureId = Identifier.of((String)"rockstar", (String)("global_animation_atlas_" + this.atlasWidth + "x" + this.atlasHeight));
        NativeImageBackedTexture BundleSelectedItemModel = new NativeImageBackedTexture(LootTableData);
        MinecraftClient.getInstance().getTextureManager().registerTexture(this.atlasTextureId, (AbstractTexture)BundleSelectedItemModel);
        for (Animation animation : this.animationsByTexture.values()) {
            animation.atlasTextureId = this.atlasTextureId;
            ArrayList<TextureRegion> arrayList = new ArrayList<TextureRegion>();
            for (n3 = 0; n3 < animation.frameCount; ++n3) {
                int n13 = animation.startFrameIndex + n3;
                n2 = n13 % n7;
                n = n13 / n7;
                float f = (float)n2 / (float)n7;
                float f2 = (float)n / (float)n8;
                float f3 = (float)(n2 + 1) / (float)n7;
                float f4 = (float)(n + 1) / (float)n8;
                TextureRegion textureRegion = new TextureRegion(this.atlasTextureId, f, f2, f3, f4, this.atlasWidth, this.atlasHeight);
                arrayList.add(textureRegion);
            }
            animation.regions = arrayList;
        }
        this.atlasBuilt = true;
        ATLAS_LOGGER.info("\u0410\u0442\u043b\u0430\u0441 {}x{} \u0441\u043e\u0431\u0440\u0430\u043d \u0441 {} \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u044f\u043c\u0438 \u0438 {} \u043a\u0430\u0434\u0440\u0430\u043c\u0438", new Object[]{this.atlasWidth, this.atlasHeight, this.animationsByTexture.size(), n6});
    }

    public static Animation findAnimation(Identifier class_29602) {
        for (TextureAnimationAtlas textureAnimationAtlas : registeredAnimations) {
            Animation animation = textureAnimationAtlas.animationsByTexture.get(class_29602);
            if (animation == null) continue;
            return animation;
        }
        return null;
    }

    public Identifier getAtlasTexture() {
        return this.atlasTextureId;
    }

    public boolean isBuilt() {
        return this.atlasBuilt;
    }

    public void clearAtlas() {
        if (this.atlasTextureId != null) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(this.atlasTextureId);
        }
        for (Frame frame : this.animationFrames) {
            try {
                frame.image.close();
            }
            catch (Exception exception) {}
        }
        this.animationsByTexture.clear();
        this.animationFrames.clear();
        this.atlasBuilt = false;
    }

    public static void clearAllAtlases() {
        for (TextureAnimationAtlas textureAnimationAtlas : registeredAnimations) {
            textureAnimationAtlas.clearAtlas();
        }
        registeredAnimations.clear();
    }

    static class Frame {
        public final Identifier textureId;
        public final int frameIndex;
        public final NativeImage image;

        public Frame(Identifier class_29602, int n, NativeImage LootTableData) {
            this.textureId = class_29602;
            this.frameIndex = n;
            this.image = LootTableData;
        }
    }

    public static class Animation {
        public final Identifier textureId;
        public final TextureAnimationManifest manifest;
        public final int startFrameIndex;
        public final int frameCount;
        public Identifier atlasTextureId;
        public List<TextureRegion> regions;

        public Animation(Identifier class_29602, TextureAnimationManifest textureAnimationManifest, int n, int n2, Identifier class_29603) {
            this.textureId = class_29602;
            this.manifest = textureAnimationManifest;
            this.startFrameIndex = n;
            this.frameCount = n2;
            this.atlasTextureId = class_29603;
        }

        public TextureRegion getRegion(int n) {
            if (this.regions == null || n < 0 || n >= this.regions.size()) {
                return null;
            }
            return this.regions.get(n);
        }

        public TextureRegion getFrameAt(long elapsedMillis) {
            if (this.regions == null || this.regions.isEmpty() || this.manifest == null) {
                return null;
            }
            long frameDurationMillis = Math.max(1L, this.manifest.getFrameDurationMillis());
            int frameIndex = (int)Math.max(0L, elapsedMillis / frameDurationMillis);
            if (this.manifest.isLooping()) {
                frameIndex %= this.regions.size();
            } else {
                frameIndex = Math.min(frameIndex, this.regions.size() - 1);
            }
            return this.regions.get(frameIndex);
        }
    }
}
