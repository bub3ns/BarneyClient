/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 */
package moscow.rockstar.api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.core.ClientPaths;
import ua.mintantileak.spk.Compile;

/**
 * Owns the named client documents under {@code <gamedir>/Rockstar} and reads and
 * writes them.  ORIGINAL: {@code rockstar/ilIlil/IiIIiIII}, reached in the
 * original as {@code Ii.I().I()}.
 */
public class ClientConfigManager {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final File CONFIG_DIRECTORY = ClientPaths.resolve("Barney");
    public static final String DEFAULT_EXTENSION = "rock";
    private static ClientConfigManager instance;
    private final List<ConfigEntry> configs = new ArrayList<ConfigEntry>();

    public ClientConfigManager() {
        instance = this;
        try {
            if (!CONFIG_DIRECTORY.exists()) {
                Files.createDirectories(Path.of(CONFIG_DIRECTORY.toURI()), new FileAttribute[0]);
            }
        }
        catch (IOException iOException) {
            System.err.println("Error creating directory: " + iOException.getMessage());
        }
    }

    /**
     * The manager the client is using.  In the original this lives on the client
     * singleton; the remapped client does not expose it yet, so the first caller
     * creates it and registers the default documents.
     */
    public static ClientConfigManager getInstance() {
        ClientConfigManager clientConfigManager = instance;
        if (clientConfigManager == null) {
            clientConfigManager = new ClientConfigManager();
            clientConfigManager.registerDefaults();
        }
        return clientConfigManager;
    }

    /** ORIGINAL: static {@code I(Ljava/io/File;Lcom/google/gson/JsonElement;)V} */
    public static void writeJson(File file, JsonElement jsonElement) throws IOException {
        ClientConfigManager.writeText(file, GSON.toJson(jsonElement));
    }

    /** ORIGINAL: static {@code I(Ljava/io/File;Ljava/lang/String;)V} */
    public static void writeText(File file, String string) throws IOException {
        Path path = file.toPath();
        Path path2 = path.getParent();
        if (path2 != null) {
            Files.createDirectories(path2, new FileAttribute[0]);
        }
        Path path3 = path2 == null ? Files.createTempFile(file.getName(), ".tmp", new FileAttribute[0]) : Files.createTempFile(path2, file.getName(), ".tmp", new FileAttribute[0]);
        try {
            Files.writeString(path3, (CharSequence)string, StandardCharsets.UTF_8, new OpenOption[0]);
            try {
                Files.move(path3, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (AtomicMoveNotSupportedException atomicMoveNotSupportedException) {
                Files.move(path3, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally {
            Files.deleteIfExists(path3);
        }
    }

    /** ORIGINAL: {@code I()V} */
    @Compile(obfuscation=4)
    public void registerDefaults() {
        this.configs.add(new ClientConfig());
        this.configs.add(new StaffConfig());
    }

    /** ORIGINAL: {@code I(Ljava/lang/String;)Lrockstar/ilIlil/IiIIIiii;} */
    public ConfigEntry get(String string) {
        return this.configs.stream().filter(configEntry -> configEntry.getConfig().value().equalsIgnoreCase(string)).findFirst().orElse(null);
    }

    /** ORIGINAL: {@code I(Lrockstar/ilIlil/IiIIIiii;)V} */
    public void load(ConfigEntry configEntry) {
        try {
            if (configEntry.getFile().exists()) {
                configEntry.load();
            }
        }
        catch (Exception exception) {
            System.err.println("Error reading file: " + exception.getMessage());
        }
    }

    /** ORIGINAL: {@code I(Ljava/lang/String;)V} */
    public void load(String string) {
        ConfigEntry configEntry = this.get(string);
        if (configEntry != null) {
            this.load(configEntry);
        }
    }

    /** ORIGINAL: {@code i(Lrockstar/ilIlil/IiIIIiii;)V} */
    public void save(ConfigEntry configEntry) {
        try {
            configEntry.save();
        }
        catch (Exception exception) {
            System.err.println("Error saving file: " + exception.getMessage());
        }
    }

    /**
     * ORIGINAL: {@code i(Ljava/lang/String;)V} - the call every "mark the client
     * document dirty" site makes.
     *
     * <p>The original also flushes the cloud client-data sync before the local
     * write when the entry is the client document; that service lives in the
     * excluded {@code globals} package and has no counterpart here, so the
     * branch is dropped.</p>
     */
    public void save(String string) {
        ConfigEntry configEntry = this.get(string);
        if (configEntry != null) {
            this.save(configEntry);
        }
    }

    /** ORIGINAL: {@code i()V} */
    @Compile(obfuscation=4)
    public void loadAll() {
        Iterator<ConfigEntry> iterator = this.configs.iterator();
        while (iterator.hasNext()) {
            ConfigEntry configEntry = iterator.next();
            this.load(configEntry);
        }
    }

    /** ORIGINAL: {@code II()V} */
    public void saveAll() {
        Iterator<ConfigEntry> iterator = this.configs.iterator();
        while (iterator.hasNext()) {
            ConfigEntry configEntry = iterator.next();
            this.save(configEntry);
        }
    }

    @Generated
    public List<ConfigEntry> getConfigs() {
        return this.configs;
    }
}
