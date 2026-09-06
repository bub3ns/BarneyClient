/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Identifier
 *  net.minecraft.SkinTextures$Model
 */
package pyrock.classes;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.texture.RemoteTextureLoader;
import moscow.rockstar.render.texture.TextureOverrideRegistry;
import moscow.rockstar.render.texture.TextureOverrideState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.SkinTextures;
import pyrock.utility.render.PyAssets;
import pyrock.utility.render.PyDynamicTexture;

public class PySkins
implements ClientAccess {
    private final ScriptDescriptor creator = ScriptDescriptor.getCurrentScript();

    public void set(Object object, Object object2, Object object3, Object object4, Object object5) {
        if (object2 != null) {
            this.skin(object, object2);
        }
        if (object3 != null) {
            this.cape(object, object3);
        }
        if (object4 != null) {
            this.elytra(object, object4);
        }
        if (object5 != null) {
            this.model(object, object5);
        }
    }

    public void skin(Object object, Object object2) {
        String string = this.target(object);
        if (object2 == null) {
            TextureOverrideState textureOverrideState = TextureOverrideRegistry.getOrCreateOverride(this.owner(), string);
            textureOverrideState.setPrimaryTexture(null);
            textureOverrideState.setModelVariant(null);
            TextureOverrideRegistry.removeEmptyOverride(string);
            return;
        }
        Identifier class_29603 = this.texture(object2);
        if (class_29603 != null) {
            TextureOverrideRegistry.getOrCreateOverride(this.owner(), string).setPrimaryTexture(class_29603);
            return;
        }
        String string2 = String.valueOf(object2);
        if (this.isPath(string2)) {
            this.await(RemoteTextureLoader.loadMinecraftSkin(this.file(string2)), class_29602 -> TextureOverrideRegistry.getOrCreateOverride(this.owner(), string).setPrimaryTexture((Identifier)class_29602), string2);
            return;
        }
        this.await(RemoteTextureLoader.fetchGameProfile(string2), class_86852 -> {
            if (class_86852 == null) {
                throw new IllegalStateException("\u043d\u0435\u0442 \u0438\u0433\u0440\u043e\u043a\u0430 \u0441 \u043d\u0438\u043a\u043e\u043c " + string2);
            }
            TextureOverrideState textureOverrideState = TextureOverrideRegistry.getOrCreateOverride(this.owner(), string);
            textureOverrideState.setPrimaryTexture(class_86852.texture());
            textureOverrideState.setModelVariant(class_86852.model());
        }, string2);
    }

    public void cape(Object object, Object object2) {
        String string = this.target(object);
        if (object2 == null) {
            TextureOverrideState textureOverrideState = TextureOverrideRegistry.getOrCreateOverride(this.owner(), string);
            textureOverrideState.setUseDefaultModel(false);
            textureOverrideState.setSecondaryTexture(null);
            TextureOverrideRegistry.removeEmptyOverride(string);
            return;
        }
        if (Boolean.FALSE.equals(object2)) {
            TextureOverrideRegistry.getOrCreateOverride(this.owner(), string).setUseDefaultModel(true);
            return;
        }
        Identifier class_29603 = this.texture(object2);
        if (class_29603 != null) {
            TextureOverrideRegistry.getOrCreateOverride(this.owner(), string).setSecondaryTexture(class_29603);
            return;
        }
        String string2 = String.valueOf(object2);
        if (string2.toLowerCase(Locale.ROOT).startsWith("optifine:")) {
            String string3 = string2.substring("optifine:".length()).trim();
            this.await(RemoteTextureLoader.loadOptiFineCape(string3), class_29602 -> TextureOverrideRegistry.getOrCreateOverride(this.owner(), string).setSecondaryTexture((Identifier)class_29602), string2);
            return;
        }
        if (this.isPath(string2)) {
            this.await(RemoteTextureLoader.loadMinecraftCape(this.file(string2)), class_29602 -> TextureOverrideRegistry.getOrCreateOverride(this.owner(), string).setSecondaryTexture((Identifier)class_29602), string2);
            return;
        }
        this.await(RemoteTextureLoader.fetchGameProfile(string2), class_86852 -> {
            if (class_86852 == null || class_86852.capeTexture() == null) {
                throw new IllegalStateException("\u0443 \u0438\u0433\u0440\u043e\u043a\u0430 " + string2 + " \u043d\u0435\u0442 \u043f\u043b\u0430\u0449\u0430");
            }
            TextureOverrideRegistry.getOrCreateOverride(this.owner(), string).setSecondaryTexture(class_86852.capeTexture());
        }, string2);
    }

    public void elytra(Object object, Object object2) {
        String string = this.target(object);
        if (object2 == null) {
            TextureOverrideRegistry.getOrCreateOverride(this.owner(), string).setTertiaryTexture(null);
            TextureOverrideRegistry.removeEmptyOverride(string);
            return;
        }
        Identifier class_29603 = this.texture(object2);
        if (class_29603 != null) {
            TextureOverrideRegistry.getOrCreateOverride(this.owner(), string).setTertiaryTexture(class_29603);
            return;
        }
        String string2 = String.valueOf(object2);
        if (this.isPath(string2)) {
            this.await(RemoteTextureLoader.loadMinecraftCape(this.file(string2)), class_29602 -> TextureOverrideRegistry.getOrCreateOverride(this.owner(), string).setTertiaryTexture((Identifier)class_29602), string2);
            return;
        }
        this.await(RemoteTextureLoader.fetchGameProfile(string2), class_86852 -> {
            if (class_86852 == null || class_86852.elytraTexture() == null) {
                throw new IllegalStateException("\u0443 \u0438\u0433\u0440\u043e\u043a\u0430 " + string2 + " \u043d\u0435\u0442 \u0441\u0432\u043e\u0435\u0439 \u044d\u043b\u0438\u0442\u0440\u044b");
            }
            TextureOverrideRegistry.getOrCreateOverride(this.owner(), string).setTertiaryTexture(class_86852.elytraTexture());
        }, string2);
    }

    public void model(Object object, Object object2) {
        String string;
        String string2 = this.target(object);
        if (object2 == null) {
            TextureOverrideRegistry.getOrCreateOverride(this.owner(), string2).setModelVariant(null);
            TextureOverrideRegistry.removeEmptyOverride(string2);
            return;
        }
        SkinTextures.Model class_79202 = switch (string = String.valueOf(object2).trim().toLowerCase(Locale.ROOT)) {
            case "slim", "alex", "thin" -> SkinTextures.Model.SLIM;
            case "wide", "steve", "classic", "default" -> SkinTextures.Model.WIDE;
            default -> throw new IllegalArgumentException("\u043c\u043e\u0434\u0435\u043b\u044c \u0431\u044b\u0432\u0430\u0435\u0442 slim \u0438\u043b\u0438 wide, \u0430 \u043d\u0435 " + String.valueOf(object2));
        };
        TextureOverrideRegistry.getOrCreateOverride(this.owner(), string2).setModelVariant(class_79202);
    }

    public void reset(Object object) {
        TextureOverrideRegistry.removeOverride(this.target(object));
    }

    public void clear() {
        TextureOverrideRegistry.removeOverridesOwnedBy(this.owner());
    }

    public List<String> targets() {
        return TextureOverrideRegistry.getOverrideKeys();
    }

    public Map<String, Object> get(Object object) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        TextureOverrideState textureOverrideState = TextureOverrideRegistry.findOverride(this.target(object));
        if (textureOverrideState == null) {
            return hashMap;
        }
        if (textureOverrideState.getPrimaryTexture() != null) {
            hashMap.put("skin", textureOverrideState.getPrimaryTexture().toString());
        }
        if (textureOverrideState.getSecondaryTexture() != null) {
            hashMap.put("cape", textureOverrideState.getSecondaryTexture().toString());
        }
        if (textureOverrideState.getTertiaryTexture() != null) {
            hashMap.put("elytra", textureOverrideState.getTertiaryTexture().toString());
        }
        if (textureOverrideState.getModelVariant() != null) {
            hashMap.put("model", textureOverrideState.getModelVariant().getName());
        }
        hashMap.put("hide_cape", textureOverrideState.usesDefaultModel());
        return hashMap;
    }

    private String target(Object object) {
        if (object == null) {
            return this.self();
        }
        if (object instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)object;
            return class_16572.getGameProfile().getName();
        }
        if (object instanceof Entity) {
            Entity class_12972 = (Entity)object;
            return class_12972.getName().getString();
        }
        String string = String.valueOf(object).trim();
        if (string.isEmpty() || string.equalsIgnoreCase("self") || string.equalsIgnoreCase("me")) {
            return this.self();
        }
        if (string.equals("*") || string.equalsIgnoreCase("all")) {
            return "*";
        }
        return string;
    }

    private String self() {
        return PySkins.minecraftClient.player != null ? PySkins.minecraftClient.player.getGameProfile().getName() : minecraftClient.getSession().getUsername();
    }

    private Identifier texture(Object object) {
        if (object instanceof Identifier) {
            Identifier class_29602 = (Identifier)object;
            return class_29602;
        }
        if (object instanceof PyDynamicTexture) {
            PyDynamicTexture pyDynamicTexture = (PyDynamicTexture)object;
            return pyDynamicTexture.identifier();
        }
        return null;
    }

    private boolean isPath(String string) {
        String string2 = string.toLowerCase(Locale.ROOT);
        return string2.startsWith("http://") || string2.startsWith("https://") || string2.contains("/") || string2.contains("\\") || string2.endsWith(".png");
    }

    private String file(String string) {
        String string2 = string.toLowerCase(Locale.ROOT);
        if (string2.startsWith("http://") || string2.startsWith("https://")) {
            return string;
        }
        Path path = PyAssets.resolve(string);
        if (!Files.isRegularFile(path, new LinkOption[0])) {
            throw new IllegalArgumentException("\u0444\u0430\u0439\u043b\u0430 \u043d\u0435\u0442: " + String.valueOf(path));
        }
        return path.toString();
    }

    private <T> void await(CompletableFuture<T> completableFuture, Consumer<T> consumer, String string) {
        completableFuture.thenAccept(consumer).exceptionally(throwable -> {
            Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
            RockstarClient.LOGGER.warn("\u0421\u043a\u0438\u043d\u044b: \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0432\u0437\u044f\u0442\u044c \u043e\u0431\u043b\u0438\u043a \u0438\u0437 {}: {}", string, cause.getMessage());
            return null;
        });
    }

    private Object owner() {
        ScriptDescriptor scriptDescriptor = ScriptDescriptor.getCurrentScript();
        if (scriptDescriptor != null) {
            return scriptDescriptor;
        }
        return this.creator != null ? this.creator : this;
    }
}
