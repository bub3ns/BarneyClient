package moscow.rockstar.render.hand;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;

/**
 * Persistent representation of one hand-swing preset (original rockstar/ilIlil/iiIIiIII).
 *
 * <p>Besides the file handle and the name the original carries two per-preset animations
 * that the preset-list UI drives directly: {@code I:IiiiIiIii} (hover) and
 * {@code i:IiiiIiIii} (selected/active), both {@code new IiiiIiIii(300L, IiiiIiiII.III)}.</p>
 */
public final class SwingPresetFile implements ClientAccess {
    private final File file;
    private final String name;
    /** Original field {@code I:IiiiIiIii}. */
    private final Animation hoverAnimation = new Animation(300L, Easing.easeInOutCubicBezier);
    /** Original field {@code i:IiiiIiIii}. */
    private final Animation activeAnimation = new Animation(300L, Easing.easeInOutCubicBezier);

    public SwingPresetFile(String name) {
        this.name = name;
        File directory = new File(moscow.rockstar.core.ClientPaths.gameDirectory(), "presets/swing");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        this.file = new File(directory, name + ".rock");
    }

    public void load() {
        if (!file.isFile()) {
            RockstarClient.LOGGER.warn("Swing preset file not found: {}", file.getAbsolutePath());
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            JsonObject root = JsonParser.parseReader((Reader) reader).getAsJsonObject();
            JsonObject animation = objectOrEmpty(root, "animation");
            for (Setting setting : RockstarClient.create().getHandSwingPresetManager().getSettings().getSettings()) {
                if (animation.has(setting.getName())) {
                    setting.deserialize(animation.get(setting.getName()));
                }
            }
            JsonObject start = objectOrEmpty(root, "startPhase");
            for (Setting setting : RockstarClient.create().getHandSwingPresetManager().getInitialSwing().getSettings()) {
                if (start.has(setting.getName())) {
                    setting.deserialize(start.get(setting.getName()));
                }
            }
            JsonObject end = objectOrEmpty(root, "endPhase");
            for (Setting setting : RockstarClient.create().getHandSwingPresetManager().getFinalSwing().getSettings()) {
                if (end.has(setting.getName())) {
                    setting.deserialize(end.get(setting.getName()));
                }
            }
            RockstarClient.create().getHandSwingPresetManager().setSelectedPresetName(name);
            RockstarClient.create().getSwingPresetFileManager().setActivePreset(this);
        } catch (Exception exception) {
            RockstarClient.LOGGER.error("Failed to load swing preset {}: {}", name, exception.getMessage());
        }
    }

    public void save() {
        try {
            JsonObject root = new JsonObject();
            root.add("animation", serializeSettings(
                    RockstarClient.create().getHandSwingPresetManager().getSettings().getSettings()));
            root.add("startPhase", serializeSettings(
                    RockstarClient.create().getHandSwingPresetManager().getInitialSwing().getSettings()));
            root.add("endPhase", serializeSettings(
                    RockstarClient.create().getHandSwingPresetManager().getFinalSwing().getSettings()));
            moscow.rockstar.util.JsonFiles.write(file, root);
            RockstarClient.create().getHandSwingPresetManager().setSelectedPresetName(name);
            RockstarClient.create().getSwingPresetFileManager().setActivePreset(this);
        } catch (RuntimeException exception) {
            RockstarClient.LOGGER.error("Failed to save swing preset {}", name, exception);
        }
    }

    /** Stores a validated JSON document received from the sharing service. */
    public void saveSerializedData(String serializedData) {
        if (serializedData == null || serializedData.isBlank()) {
            throw new IllegalArgumentException("Preset data is empty");
        }
        JsonElement root = JsonParser.parseString(serializedData);
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("Preset data must be a JSON object");
        }
        moscow.rockstar.util.JsonFiles.write(file, root);
    }

    public void delete() {
        Path path = file.toPath();
        try {
            Files.delete(path);
            SwingPresetFileManager manager = RockstarClient.create().getSwingPresetFileManager();
            manager.removePreset(this);
            if (this == manager.getActivePreset()) {
                manager.setActivePreset(null);
            }
            RockstarClient.LOGGER.info("Swing preset deleted: {}", path);
        } catch (NoSuchFileException exception) {
            RockstarClient.LOGGER.warn("Tried to delete a swing preset that does not exist: {}", path);
        } catch (IOException exception) {
            Notification.error(Text.of(Localization.translate("swing_anim.delete_error")));
            RockstarClient.LOGGER.warn("Failed to delete swing preset {}: {}", path, exception.getMessage());
        }
    }

    private static JsonObject objectOrEmpty(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : new JsonObject();
    }

    private static JsonObject serializeSettings(Iterable<Setting> settings) {
        JsonObject result = new JsonObject();
        for (Setting setting : settings) {
            result.add(setting.getName(), setting.serialize());
        }
        return result;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    /** Original {@code I()Lrockstar/ilIlil/IiiiIiIii;} - the preset-row hover animation. */
    public Animation getHoverAnimation() {
        return hoverAnimation;
    }

    /** Original {@code i()Lrockstar/ilIlil/IiiiIiIii;} - the preset-row selected animation. */
    public Animation getActiveAnimation() {
        return activeAnimation;
    }
}
