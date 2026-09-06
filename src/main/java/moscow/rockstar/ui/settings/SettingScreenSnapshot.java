package moscow.rockstar.ui.settings;

import java.util.List;
import net.minecraft.client.gui.screen.Screen;

public record SettingScreenSnapshot(Screen screen, List<SettingValueSnapshot> settingValues,
                                    long capturedAtMillis) {
    public Screen getScreen() {
        return this.screen;
    }

    public List<SettingValueSnapshot> getSettingValues() {
        return this.settingValues;
    }

    public long getCapturedAtMillis() {
        return this.capturedAtMillis;
    }
}
