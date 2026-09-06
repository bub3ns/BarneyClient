/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package pyrock.classes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.assets.RemoteAssetCache;
import moscow.rockstar.ui.localization.Language;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.localization.TranslationOverrideRegistry;
import pyrock.utility.render.PyAssets;

public class PyLang {
    private static final Gson GSON = new Gson();

    public void add(String string, Map<String, String> map) {
        Language language = Language.fromLocaleCode(string);
        if (language == null) {
            language = Language.EN_US;
            // throw new IllegalArgumentException("\u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u044b\u0439 \u044f\u0437\u044b\u043a: " + string + " (\u0431\u044b\u0432\u0430\u044e\u0442 ru_ru, en_us, uk_ua, pl_pl)");
        }
        if (map == null) {
            return;
        }
        Object object = this.owner();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            TranslationOverrideRegistry.register(object, language, entry.getKey(), String.valueOf(entry.getValue()));
        }
    }

    public void loadFile(String string) {
        if (RemoteAssetCache.isRemoteUrl(string)) {
            Path path2 = RemoteAssetCache.findCachedAsset(string);
            if (path2 != null) {
                this.apply(this.read(path2), string);
                return;
            }
            Object object = this.owner();
            RemoteAssetCache.fetchAsset(string, path -> {
                try {
                    this.apply(this.read((Path)path), string, object);
                }
                catch (Exception exception) {
                    RockstarClient.LOGGER.warn("\u041f\u0435\u0440\u0435\u0432\u043e\u0434\u044b: {}", (Object)exception.getMessage());
                }
            });
            return;
        }
        Path path3 = PyAssets.resolve(string);
        if (!Files.isRegularFile(path3, new LinkOption[0])) {
            throw new IllegalArgumentException("\u0444\u0430\u0439\u043b\u0430 \u0441 \u043f\u0435\u0440\u0435\u0432\u043e\u0434\u0430\u043c\u0438 \u043d\u0435\u0442: " + String.valueOf(path3));
        }
        this.apply(this.read(path3), string);
    }

    public String get(String string) {
        return Localization.translate(string);
    }

    public String current() {
        return Localization.getLanguage().getLocaleCode();
    }

    public boolean has(String string) {
        return TranslationOverrideRegistry.contains(string) || !Localization.translate(string).equals(string);
    }

    public void clear() {
        TranslationOverrideRegistry.clearOwner(this.owner());
    }

    public String plural(double d, String string, String string2, String string3) {
        long l = Math.abs(Math.round(d));
        if (this.current().startsWith("en")) {
            return l == 1L ? string : string2;
        }
        long l2 = l % 100L;
        long l3 = l % 10L;
        if (l2 >= 11L && l2 <= 14L) {
            return string3;
        }
        if (l3 == 1L) {
            return string;
        }
        if (l3 >= 2L && l3 <= 4L) {
            return string2;
        }
        return string3;
    }

    private void apply(JsonObject jsonObject, String string) {
        this.apply(jsonObject, string, this.owner());
    }

    private void apply(JsonObject jsonObject, String string, Object object) {
        if (jsonObject == null) {
            return;
        }
        for (Map.Entry entry : jsonObject.entrySet()) {
            Language language = Language.fromLocaleCode((String)entry.getKey());
            if (language == null || !((JsonElement)entry.getValue()).isJsonObject()) {
                RockstarClient.LOGGER.warn("\u041f\u0435\u0440\u0435\u0432\u043e\u0434\u044b: \u043d\u0435\u043f\u043e\u043d\u044f\u0442\u043d\u044b\u0439 \u044f\u0437\u044b\u043a {} \u0432 {}", entry.getKey(), (Object)string);
                continue;
            }
            for (Map.Entry entry2 : ((JsonElement)entry.getValue()).getAsJsonObject().entrySet()) {
                if (!((JsonElement)entry2.getValue()).isJsonPrimitive()) continue;
                TranslationOverrideRegistry.register(object, language, (String)entry2.getKey(), ((JsonElement)entry2.getValue()).getAsString());
            }
        }
    }

    private JsonObject read(Path path) {
        try {
            return (JsonObject)GSON.fromJson(Files.readString(path), JsonObject.class);
        }
        catch (Exception exception) {
            throw new IllegalArgumentException("\u043d\u0435 \u0447\u0438\u0442\u0430\u0435\u0442\u0441\u044f \u0444\u0430\u0439\u043b \u043f\u0435\u0440\u0435\u0432\u043e\u0434\u043e\u0432 " + String.valueOf(path) + ": " + exception.getMessage(), exception);
        }
    }

    private Object owner() {
        ScriptDescriptor scriptDescriptor = ScriptDescriptor.getCurrentScript();
        return scriptDescriptor != null ? scriptDescriptor : this;
    }
}
