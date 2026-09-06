/*
 * Small, deliberately boring JSON file writer used by client persistence.
 */
package moscow.rockstar.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Persists JSON documents without coupling persistence to an unrelated service class. */
public final class JsonFiles {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonFiles() {
    }

    public static void write(File file, JsonElement json) {
        if (file == null) {
            throw new IllegalArgumentException("file");
        }
        File parent = file.getParentFile();
        try {
            if (parent != null) {
                Files.createDirectories(parent.toPath());
            }
            Files.writeString(file.toPath(), GSON.toJson(json), StandardCharsets.UTF_8);
        }
        catch (IOException exception) {
            throw new UncheckedIOException("Unable to write JSON file " + file, exception);
        }
    }
}
