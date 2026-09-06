package moscow.rockstar.render.hand;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import moscow.rockstar.core.ClientPaths;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationBridge;
import net.minecraft.text.Text;

/** Discovers and owns user-created hand-swing preset files (original rockstar/ilIlil/iiIIiIIi). */
public final class SwingPresetFileManager {
    private final List<SwingPresetFile> presets = new ArrayList<>();
    private SwingPresetFile activePreset;
    private boolean initialized = false;

    public SwingPresetFileManager() {
        reload();
    }

    private static File presetDirectory() {
        return new File(ClientPaths.gameDirectory(), "presets/swing");
    }

    /** Original I()V - ensures the autosave preset exists and performs the first scan. */
    public void initialize() {
        if (getAutosave() == null) {
            createPreset("autosave");
        }
        if (!initialized) {
            rescan();
            initialized = true;
        }
    }

    /** Original i()V - opens the presets/swing folder in the system file browser. */
    public void openPresetsFolder() {
        String[] command = new String[]{"explorer " + presetDirectory().getAbsolutePath()};
        try {
            Runtime.getRuntime().exec(command);
        } catch (Exception exception) {
            RockstarClient.LOGGER.error("\u0432\u0441\u0435 \u043d\u0430\u0435\u0431\u043d\u0443\u043b\u043e\u0441\u044c \u0432 dir \u043a\u043e\u043d\u0444\u0438\u0433\u0435 {}", exception.getMessage());
        }
    }

    /** Original I(Ljava/lang/String;)V - creates and persists a new preset file. */
    public void createPreset(String name) {
        if (name == null) {
            return;
        }
        if (find(name, false) != null) {
            RockstarClient.LOGGER.warn("Preset {} already exists", name);
            return;
        }
        SwingPresetFile preset = new SwingPresetFile(name);
        if (name.equals("autosave")) {
            preset.load();
        }
        preset.save();
        presets.add(preset);
    }

    /** Original II()V - prints the numbered preset list. */
    public void printPresetList() {
        NotificationBridge.showNotification(Text.of(Localization.translate("swing_anim.preset_list")));
        for (SwingPresetFile preset : presets) {
            int index = presets.indexOf(preset) + 1;
            NotificationBridge.showNotification(Text.of(
                Localization.translateFormatted("swing_anim.preset_item", index, preset.getName())));
        }
    }

    /** Original iI()V - rebuilds the preset list from disk. */
    private void rescan() {
        presets.clear();
        Path directory = presetDirectory().toPath();
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
                return;
            } catch (IOException exception) {
                RockstarClient.LOGGER.error("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0437\u0434\u0430\u0442\u044c \u0434\u0438\u0440\u0435\u043a\u0442\u043e\u0440\u0438\u044e \u043f\u0440\u0435\u0441\u0435\u0442\u043e\u0432: {}", exception.getMessage());
                return;
            }
        }
        try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(path -> Files.isRegularFile(path))
                .filter(path -> path.toString().endsWith(".rock"))
                .forEach(path -> {
                    String filename = path.getFileName().toString();
                    presets.add(new SwingPresetFile(filename.substring(0, filename.lastIndexOf(46))));
                });
        } catch (IOException exception) {
            RockstarClient.LOGGER.error("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0441\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0438 \u0434\u0438\u0440\u0435\u043a\u0442\u043e\u0440\u0438\u0438 \u043a\u043e\u043d\u0444\u0438\u0433\u043e\u0432: {}", exception.getMessage());
        }
    }

    /** Original I(Ljava/lang/String;Z)Lrockstar/ilIlil/iiIIiIII; */
    public SwingPresetFile find(String name, boolean rescanFirst) {
        if (rescanFirst) {
            rescan();
        }
        return presets.stream().filter(preset -> preset.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /** Original I(Ljava/lang/String;)Lrockstar/ilIlil/iiIIiIII; */
    public SwingPresetFile find(String name) {
        return find(name, false);
    }

    /** Original I()Lrockstar/ilIlil/iiIIiIII; - the autosave preset, rescanning first. */
    public SwingPresetFile getAutosave() {
        return find("autosave", true);
    }

    /** Original Ii()V */
    public void reload() {
        rescan();
    }

    public void loadAutosave() {
        SwingPresetFile autosave = find("autosave");
        if (autosave != null) {
            autosave.load();
        }
    }

    public void removePreset(SwingPresetFile preset) {
        presets.remove(preset);
    }

    public void saveActivePreset() {
        if (activePreset != null) {
            activePreset.save();
        }
    }

    public List<SwingPresetFile> getPresets() {
        return presets;
    }

    public SwingPresetFile getActivePreset() {
        return activePreset;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setActivePreset(SwingPresetFile preset) {
        this.activePreset = preset;
    }
}
