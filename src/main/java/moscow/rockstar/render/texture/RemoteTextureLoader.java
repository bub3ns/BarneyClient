/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.NativeImage
 *  net.minecraft.NativeImageBackedTexture
 *  net.minecraft.AbstractTexture
 *  net.minecraft.SkullBlockEntity
 *  net.minecraft.Identifier
 *  net.minecraft.SkinTextures
 *  net.minecraft.ColorHelper
 */
package moscow.rockstar.render.texture;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.network.http.HttpResourceClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.math.ColorHelper;

public final class RemoteTextureLoader {
    private static final Map<String, Identifier> loadedTextures = new ConcurrentHashMap<String, Identifier>();
    private static final AtomicInteger textureSequence = new AtomicInteger();
    private static final Executor imageDecodeExecutor = runnable -> {
        Thread thread = new Thread(runnable, "rockstar-skin");
        thread.setDaemon(true);
        thread.start();
    };

    private RemoteTextureLoader() {
    }

    public static CompletableFuture<SkinTextures> fetchGameProfile(String string) {
        return SkullBlockEntity.fetchProfileByName(string).thenCompose(profileOptional -> profileOptional
            .map(gameProfile -> MinecraftClient.getInstance().getSkinProvider().fetchSkinTextures(gameProfile)
                .thenApply(skinTexturesOptional -> skinTexturesOptional.orElse(null)))
            .orElseGet(() -> CompletableFuture.completedFuture(null)));
    }

    public static CompletableFuture<Identifier> loadMinecraftSkin(String string) {
        return RemoteTextureLoader.loadRemoteTexture(string, "skin", RemoteTextureLoader::normalizeSkinImage);
    }

    public static CompletableFuture<Identifier> loadMinecraftCape(String string) {
        return RemoteTextureLoader.loadRemoteTexture(string, "cape", RemoteTextureLoader::normalizeCapeImage);
    }

    public static CompletableFuture<Identifier> loadOptiFineCape(String string) {
        return RemoteTextureLoader.loadRemoteTexture("http://s.optifine.net/capes/" + string + ".png", "cape", RemoteTextureLoader::normalizeCapeImage);
    }

    private static CompletableFuture<Identifier> loadRemoteTexture(String string, String string2, ImageTransformer imageTransformer) {
        if (string == null || string.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }
        String string3 = string2 + ":" + string;
        Identifier class_29603 = loadedTextures.get(string3);
        if (class_29603 != null) {
            return CompletableFuture.completedFuture(class_29603);
        }
        return ((CompletableFuture)CompletableFuture.supplyAsync(() -> imageTransformer.apply(RemoteTextureLoader.readImage(string)), imageDecodeExecutor).thenCompose(LootTableData -> RemoteTextureLoader.registerTexture(LootTableData, string2))).thenApply(class_29602 -> {
            loadedTextures.put(string3, (Identifier)class_29602);
            return class_29602;
        });
    }

    private static NativeImage readImage(String string) {
        try {
            String string2 = string.toLowerCase(Locale.ROOT);
            if (string2.startsWith("http://") || string2.startsWith("https://")) {
                return NativeImage.read((byte[])HttpResourceClient.getBytes(string, new String[0]));
            }
            Path path = Path.of(string, new String[0]).toAbsolutePath().normalize();
            if (!Files.isRegularFile(path, new LinkOption[0])) {
                throw new IllegalArgumentException("\u0444\u0430\u0439\u043b\u0430 \u043d\u0435\u0442: " + String.valueOf(path));
            }
            return NativeImage.read((byte[])Files.readAllBytes(path));
        }
        catch (Exception exception) {
            throw new RuntimeException("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c " + string + ": " + exception.getMessage(), exception);
        }
    }

    private static CompletableFuture<Identifier> registerTexture(NativeImage LootTableData, String string) {
        CompletableFuture<Identifier> completableFuture = new CompletableFuture<Identifier>();
        Identifier class_29602 = RockstarClient.resourceId("skins/" + string + "/" + textureSequence.incrementAndGet());
        MinecraftClient.getInstance().execute(() -> {
            try {
                MinecraftClient.getInstance().getTextureManager().registerTexture(class_29602, (AbstractTexture)new NativeImageBackedTexture(LootTableData));
                completableFuture.complete(class_29602);
            }
            catch (Throwable throwable) {
                LootTableData.close();
                completableFuture.completeExceptionally(throwable);
            }
        });
        return completableFuture;
    }

    private static NativeImage normalizeSkinImage(NativeImage LootTableData) {
        if (LootTableData.getWidth() == LootTableData.getHeight() && LootTableData.getWidth() != 64) {
            return LootTableData;
        }
        if (LootTableData.getWidth() != 64 || LootTableData.getHeight() != 32 && LootTableData.getHeight() != 64) {
            throw new IllegalArgumentException("\u0441\u043a\u0438\u043d \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c 64x64 \u0438\u043b\u0438 64x32, \u0430 \u043d\u0435 " + LootTableData.getWidth() + "x" + LootTableData.getHeight());
        }
        boolean bl = LootTableData.getHeight() == 32;
        NativeImage BlockFamilyRecipeFactory = LootTableData;
        if (bl) {
            BlockFamilyRecipeFactory = new NativeImage(64, 64, true);
            BlockFamilyRecipeFactory.copyFrom(LootTableData);
            LootTableData.close();
            BlockFamilyRecipeFactory.fillRect(0, 32, 64, 32, 0);
            BlockFamilyRecipeFactory.copyRect(4, 16, 16, 32, 4, 4, true, false);
            BlockFamilyRecipeFactory.copyRect(8, 16, 16, 32, 4, 4, true, false);
            BlockFamilyRecipeFactory.copyRect(0, 20, 24, 32, 4, 12, true, false);
            BlockFamilyRecipeFactory.copyRect(4, 20, 16, 32, 4, 12, true, false);
            BlockFamilyRecipeFactory.copyRect(8, 20, 8, 32, 4, 12, true, false);
            BlockFamilyRecipeFactory.copyRect(12, 20, 16, 32, 4, 12, true, false);
            BlockFamilyRecipeFactory.copyRect(44, 16, -8, 32, 4, 4, true, false);
            BlockFamilyRecipeFactory.copyRect(48, 16, -8, 32, 4, 4, true, false);
            BlockFamilyRecipeFactory.copyRect(40, 20, 0, 32, 4, 12, true, false);
            BlockFamilyRecipeFactory.copyRect(44, 20, -8, 32, 4, 12, true, false);
            BlockFamilyRecipeFactory.copyRect(48, 20, -16, 32, 4, 12, true, false);
            BlockFamilyRecipeFactory.copyRect(52, 20, -8, 32, 4, 12, true, false);
        }
        RemoteTextureLoader.copyImageRegion(BlockFamilyRecipeFactory, 0, 0, 32, 16);
        if (bl) {
            RemoteTextureLoader.copyImagePixels(BlockFamilyRecipeFactory, 32, 0, 64, 32);
        }
        RemoteTextureLoader.copyImageRegion(BlockFamilyRecipeFactory, 0, 16, 64, 32);
        RemoteTextureLoader.copyImageRegion(BlockFamilyRecipeFactory, 16, 48, 48, 64);
        return BlockFamilyRecipeFactory;
    }

    private static NativeImage normalizeCapeImage(NativeImage LootTableData) {
        if (LootTableData.getWidth() == LootTableData.getHeight() * 2) {
            return LootTableData;
        }
        int n = Math.max(64, RemoteTextureLoader.scaleImageCoordinate(LootTableData.getWidth()));
        int n2 = n / 2;
        NativeImage BlockFamilyRecipeFactory = new NativeImage(n, n2, true);
        BlockFamilyRecipeFactory.fillRect(0, 0, n, n2, 0);
        BlockFamilyRecipeFactory.copyFrom(LootTableData);
        LootTableData.close();
        return BlockFamilyRecipeFactory;
    }

    private static void copyImageRegion(NativeImage LootTableData, int n, int n2, int n3, int n4) {
        for (int i = n; i < n3; ++i) {
            for (int j = n2; j < n4; ++j) {
                LootTableData.setColorArgb(i, j, LootTableData.getColorArgb(i, j) | 0xFF000000);
            }
        }
    }

    private static void copyImagePixels(NativeImage LootTableData, int n, int n2, int n3, int n4) {
        int n5;
        int n6;
        for (n6 = n; n6 < n3; ++n6) {
            for (n5 = n2; n5 < n4; ++n5) {
                if (ColorHelper.getAlpha((int)LootTableData.getColorArgb(n6, n5)) >= 128) continue;
                return;
            }
        }
        for (n6 = n; n6 < n3; ++n6) {
            for (n5 = n2; n5 < n4; ++n5) {
                LootTableData.setColorArgb(n6, n5, LootTableData.getColorArgb(n6, n5) & 0xFFFFFF);
            }
        }
    }

    private static int scaleImageCoordinate(int n) {
        int n2;
        for (n2 = 1; n2 < n; n2 <<= 1) {
        }
        return n2;
    }

    static interface ImageTransformer {
        public NativeImage apply(NativeImage var1);
    }
}
