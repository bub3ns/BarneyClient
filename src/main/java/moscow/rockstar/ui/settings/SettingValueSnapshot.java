package moscow.rockstar.ui.settings;

import com.google.gson.JsonElement;
import moscow.rockstar.settings.Setting;

public record SettingValueSnapshot(Setting setting, JsonElement value) {
    public Setting getSetting() {
        return this.setting;
    }

    public JsonElement getJsonElement() {
        return this.value;
    }
}
