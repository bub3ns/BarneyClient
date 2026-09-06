/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 */
package moscow.rockstar.settings;

import com.google.gson.JsonElement;
import java.util.Map;
import java.util.function.BooleanSupplier;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.core.Component;

public interface Setting {
    public String getName();

    public String getDescriptionKey();

    public BooleanSupplier getVisibilityCondition();

    public void registerWithOwner(SettingOwner var1);

    default public boolean hasValidSettingValue() {
        return !this.getVisibilityCondition().getAsBoolean();
    }

    public JsonElement serialize();

    public void deserialize(JsonElement var1);

    default public boolean isValidJson(JsonElement jsonElement) {
        return Setting.jsonEquals(this.serialize(), jsonElement);
    }

    private static boolean jsonEquals(JsonElement jsonElement, JsonElement jsonElement2) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return jsonElement2 == null || jsonElement2.isJsonNull();
        }
        if (jsonElement2 == null || jsonElement2.isJsonNull()) {
            return false;
        }
        if (jsonElement.isJsonPrimitive()) {
            if (!jsonElement2.isJsonPrimitive()) {
                return false;
            }
            if (jsonElement.getAsJsonPrimitive().isBoolean()) {
                return jsonElement2.getAsJsonPrimitive().isBoolean();
            }
            if (jsonElement.getAsJsonPrimitive().isNumber()) {
                return jsonElement2.getAsJsonPrimitive().isNumber() && Double.isFinite(jsonElement2.getAsDouble());
            }
            return jsonElement2.getAsJsonPrimitive().isString();
        }
        if (jsonElement.isJsonArray()) {
            if (!jsonElement2.isJsonArray()) {
                return false;
            }
            if (jsonElement.getAsJsonArray().size() == 0) {
                return true;
            }
            for (JsonElement jsonElement3 : jsonElement2.getAsJsonArray()) {
                if (Setting.jsonEquals(jsonElement.getAsJsonArray().get(0), jsonElement3)) continue;
                return false;
            }
            return true;
        }
        if (!jsonElement2.isJsonObject()) {
            return false;
        }
        for (Map.Entry entry : jsonElement.getAsJsonObject().entrySet()) {
            if (!jsonElement2.getAsJsonObject().has((String)entry.getKey()) || Setting.jsonEquals((JsonElement)entry.getValue(), jsonElement2.getAsJsonObject().get((String)entry.getKey()))) continue;
            return false;
        }
        return true;
    }

    default public Component buildComponent() {
        return null;
    }
}

