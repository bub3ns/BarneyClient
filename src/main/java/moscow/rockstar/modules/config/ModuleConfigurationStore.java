/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package moscow.rockstar.modules.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import moscow.rockstar.core.ClientPaths;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.other.assist.Assist;
import moscow.rockstar.items.assist.AssistItemProvider;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.modules.ModuleNotFoundException;
import moscow.rockstar.render.esp.OverlayRegistry;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.util.JsonFiles;

public final class ModuleConfigurationStore {
    private static final Map<String, JsonObject> moduleConfigsByName = new ConcurrentHashMap<String, JsonObject>();
    private static final Map<String, JsonObject> pendingSettingsByModule = new ConcurrentHashMap<String, JsonObject>();
    private static final File CONFIGURATION_FILE = ClientPaths.resolve("Barney", "config.json");
    private static volatile boolean autoSaveEnabled = true;

    private ModuleConfigurationStore() {
    }

    public static File getConfigurationFile() {
        return CONFIGURATION_FILE;
    }

    public static boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public static void setAutoSaveEnabled(boolean enabled) {
        autoSaveEnabled = enabled;
    }

    public static void saveConfiguration() {
        JsonFiles.write(CONFIGURATION_FILE, serializeConfiguration());
    }

    public static LoadStatus loadConfigurationFromDisk() {
        if (!CONFIGURATION_FILE.isFile()) {
            return LoadStatus.ERROR;
        }
        try {
            JsonObject configuration = JsonParser.parseString(
                    Files.readString(CONFIGURATION_FILE.toPath(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
            return loadConfiguration(configuration);
        } catch (IOException | RuntimeException exception) {
            RockstarClient.LOGGER.warn("Config: unable to read {}", CONFIGURATION_FILE, exception);
            return LoadStatus.ERROR;
        }
    }

    private static Collection<ModuleContract> getModules() {
        return RockstarClient.create().getModuleRegistry().getModules();
    }

    public static JsonObject serializeConfiguration() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("modules", (JsonElement)ModuleConfigurationStore.serializeModules());
        jsonObject.add("espElements", (JsonElement)OverlayRegistry.getInstance().serializeOverlayConfig());
        jsonObject.add("macroMenu", (JsonElement)ModuleConfigurationStore.serializeMacroMenu());
        return jsonObject;
    }

    private static JsonArray serializeModules() {
        JsonArray jsonArray = new JsonArray();
        HashSet<String> hashSet = new HashSet<String>();
        for (ModuleContract object : ModuleConfigurationStore.getModules()) {
            hashSet.add(object.getName());
            jsonArray.add((JsonElement)ModuleConfigurationStore.serializeModule(object));
        }
        for (Map.Entry entry : moduleConfigsByName.entrySet()) {
            if (hashSet.contains(entry.getKey())) continue;
            jsonArray.add((JsonElement)((JsonObject)entry.getValue()).deepCopy());
        }
        return jsonArray;
    }

    private static JsonObject serializeModule(ModuleContract moduleContract) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", moduleContract.getName());
        jsonObject.addProperty("enabled", Boolean.valueOf(moduleContract.isEnabled() && !ModuleConfigurationStore.isHiddenModule(moduleContract)));
        jsonObject.addProperty("key", (Number)moduleContract.getKeyBind());
        jsonObject.add("settings", (JsonElement)ModuleConfigurationStore.serializeSettings(moduleContract.getSettings(), moduleContract.getName()));
        return jsonObject;
    }

    public static void cacheModuleConfiguration(ModuleContract moduleContract) {
        if (moduleContract == null) {
            return;
        }
        moduleConfigsByName.put(moduleContract.getName(), ModuleConfigurationStore.serializeModule(moduleContract));
    }

    public static void cacheSettingConfiguration(ModuleContract moduleContract, Setting setting) {
        if (moduleContract == null || setting == null) {
            return;
        }
        try {
            ModuleConfigurationStore.cacheUnknownSetting(moduleContract.getName(), setting.getName(), setting.serialize());
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.warn("Config: \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0443 {} \u043c\u043e\u0434\u0443\u043b\u044f {}", new Object[]{setting.getName(), moduleContract.getName(), exception});
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static JsonObject serializeSettings(List<Setting> list, String string) {
        JsonObject jsonObject = new JsonObject();
        for (Setting setting : list) {
            jsonObject.add(setting.getName(), setting.serialize());
        }
        JsonObject jsonObject2 = pendingSettingsByModule.get(string);
        if (jsonObject2 != null) {
            synchronized (jsonObject2) {
                for (Map.Entry entry : jsonObject2.entrySet()) {
                    if (jsonObject.has((String)entry.getKey())) continue;
                    jsonObject.add((String)entry.getKey(), ((JsonElement)entry.getValue()).deepCopy());
                }
            }
        }
        return jsonObject;
    }

    private static JsonArray serializeMacroMenu() {
        JsonArray jsonArray = new JsonArray();
        Assist assist = RockstarClient.create().getModuleRegistry().getModule(Assist.class);
        if (assist == null) {
            return jsonArray;
        }
        for (AssistItemProvider provider : assist.getAssistItems()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", provider.getSettingKey());
            jsonObject.addProperty("key", provider.getKeyCode());
            jsonObject.add("settings", ModuleConfigurationStore.serializeSettings(provider.getSettings(), provider.getSettingKey()));
            jsonArray.add((JsonElement)jsonObject);
        }
        return jsonArray;
    }

    public static LoadStatus loadConfiguration(JsonObject jsonObject) {
        if (jsonObject == null) {
            return LoadStatus.ERROR;
        }
        JsonArray jsonArray = ModuleConfigurationStore.getModuleArray(jsonObject);
        if (jsonArray == null) {
            RockstarClient.LOGGER.warn("Config: \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u043c\u0430\u0441\u0441\u0438\u0432 \u043c\u043e\u0434\u0443\u043b\u0435\u0439");
            return LoadStatus.ERROR;
        }
        moduleConfigsByName.clear();
        pendingSettingsByModule.clear();
        try {
            ModuleConfigurationStore.refreshModuleSettings();
            boolean bl = true;
            for (JsonElement jsonElement : jsonArray) {
                if (jsonElement.isJsonObject()) {
                    if (ModuleConfigurationStore.applyModuleConfiguration(jsonElement.getAsJsonObject())) continue;
                    bl = false;
                    continue;
                }
                bl = false;
            }
            if (jsonObject.has("espElements")) {
                if (jsonObject.get("espElements").isJsonArray()) {
                    try {
                        OverlayRegistry.getInstance().loadOverlayConfig(jsonObject.getAsJsonArray("espElements"));
                    }
                    catch (Exception exception) {
                        RockstarClient.LOGGER.warn("Config: \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u044b ESP", (Throwable)exception);
                        bl = false;
                    }
                } else {
                    bl = false;
                }
            }
            if (jsonObject.has("macroMenu")) {
                if (jsonObject.get("macroMenu").isJsonArray()) {
                    if (!ModuleConfigurationStore.loadMacroMenuConfiguration(jsonObject.getAsJsonArray("macroMenu"))) {
                        bl = false;
                    }
                } else {
                    bl = false;
                }
            }
            return bl ? LoadStatus.SUCCESS : LoadStatus.INVALID;
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("Config: \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u0438\u043c\u0435\u043d\u0438\u0442\u044c \u043a\u043e\u043d\u0444\u0438\u0433", (Throwable)exception);
            moduleConfigsByName.clear();
            pendingSettingsByModule.clear();
            return LoadStatus.ERROR;
        }
    }

    public static boolean applyPendingModuleConfiguration(ModuleContract moduleContract) {
        if (moduleContract == null) {
            return false;
        }
        JsonObject jsonObject = moduleConfigsByName.remove(moduleContract.getName());
        if (jsonObject == null) {
            return false;
        }
        return ModuleConfigurationStore.applyModuleConfiguration(jsonObject);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean applyPendingSetting(ModuleContract moduleContract, Setting setting) {
        JsonElement jsonElement;
        if (moduleContract == null || setting == null) {
            return false;
        }
        JsonObject jsonObject = pendingSettingsByModule.get(moduleContract.getName());
        if (jsonObject == null) {
            return false;
        }
        JsonObject jsonObject2 = jsonObject;
        synchronized (jsonObject2) {
            jsonElement = jsonObject.remove(setting.getName());
        }
        if (jsonElement == null || !setting.isValidJson(jsonElement)) {
            return false;
        }
        try {
            setting.deserialize(jsonElement);
            return true;
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.warn("Config: \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0443 {} \u043c\u043e\u0434\u0443\u043b\u044f {}", new Object[]{setting.getName(), moduleContract.getName(), exception});
            return false;
        }
    }

    public static List<String> getCachedModuleNames() {
        return new ArrayList<String>(moduleConfigsByName.keySet());
    }

    public static void loadModuleConfiguration(JsonObject jsonObject, ModuleContract moduleContract) {
        if (jsonObject == null || moduleContract == null) {
            return;
        }
        JsonArray jsonArray = ModuleConfigurationStore.getModuleArray(jsonObject);
        if (jsonArray == null) {
            return;
        }
        for (JsonElement jsonElement : jsonArray) {
            String string;
            JsonObject jsonObject2;
            if (!jsonElement.isJsonObject() || !(jsonObject2 = jsonElement.getAsJsonObject()).has("name") || !(string = jsonObject2.get("name").getAsString()).equalsIgnoreCase(moduleContract.getName()) && !string.replace(" ", "").equalsIgnoreCase(moduleContract.getName().replace(" ", ""))) continue;
            if (!(moduleContract instanceof Menu) && moduleContract instanceof Module) {
                Module module = (Module)moduleContract;
                module.loadSettings();
            }
            ModuleConfigurationStore.applyModuleConfiguration(jsonObject2);
            return;
        }
    }

    private static boolean applyModuleConfiguration(JsonObject jsonObject) {
        if (!jsonObject.has("name") || !jsonObject.get("name").isJsonPrimitive()) {
            return false;
        }
        String string = jsonObject.get("name").getAsString();
        try {
            JsonElement jsonElement;
            ModuleContract module = RockstarClient.create().getModuleRegistry().findModuleByName(string);
            boolean bl = false;
            if (jsonObject.has("enabled")) {
                JsonElement jsonElement2 = jsonObject.get("enabled");
                if (!jsonElement2.isJsonPrimitive() || !jsonElement2.getAsJsonPrimitive().isBoolean()) {
                    return false;
                }
                bl = jsonElement2.getAsBoolean() && !ModuleConfigurationStore.isHiddenModule(module);
            }
            int n = -1;
            if (jsonObject.has("key")) {
                jsonElement = jsonObject.get("key");
                if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isNumber()) {
                    return false;
                }
                double d = jsonElement.getAsDouble();
                if (!Double.isFinite(d) || d < -2.147483648E9 || d > 2.147483647E9) {
                    return false;
                }
                n = jsonElement.getAsInt();
            }
            if (!(module instanceof Menu)) {
                module.setEnabled(bl, true);
            }
            module.setKeyBind(n);
            if (jsonObject.has("settings") && jsonObject.get("settings").isJsonObject()) {
                jsonElement = jsonObject.getAsJsonObject("settings");
                return ModuleConfigurationStore.applySettingConfiguration(module.getSettings(), (JsonObject)jsonElement, module.getName());
            }
            return !jsonObject.has("settings");
        }
        catch (ModuleNotFoundException moduleNotFoundException) {
            moduleConfigsByName.put(string, jsonObject.deepCopy());
            RockstarClient.LOGGER.warn("Config: \u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u044b\u0439 \u043c\u043e\u0434\u0443\u043b\u044c {}", (Object)string);
            return false;
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.warn("Config: \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u043c\u043e\u0434\u0443\u043b\u044c {}", (Object)string, (Object)exception);
            return false;
        }
    }

    private static boolean applySettingConfiguration(List<Setting> list, JsonObject jsonObject, String string) {
        boolean bl = true;
        HashSet<String> hashSet = new HashSet<String>();
        for (Setting object : list) {
            hashSet.add(object.getName());
        }
        for (Map.Entry entry : jsonObject.entrySet()) {
            if (hashSet.contains(entry.getKey())) continue;
            ModuleConfigurationStore.cacheUnknownSetting(string, (String)entry.getKey(), (JsonElement)entry.getValue());
        }
        for (Setting setting : list) {
            if (!jsonObject.has(setting.getName())) continue;
            if (!setting.isValidJson(jsonObject.get(setting.getName()))) {
                RockstarClient.LOGGER.warn("Config: \u043f\u0440\u043e\u043f\u0443\u0449\u0435\u043d\u0430 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430 {} \u043c\u043e\u0434\u0443\u043b\u044f {} \u0438\u0437-\u0437\u0430 \u043d\u0435\u0432\u0435\u0440\u043d\u043e\u0433\u043e \u0444\u043e\u0440\u043c\u0430\u0442\u0430", (Object)setting.getName(), (Object)string);
                ModuleConfigurationStore.cacheUnknownSetting(string, setting.getName(), jsonObject.get(setting.getName()));
                bl = false;
                continue;
            }
            try {
                setting.deserialize(jsonObject.get(setting.getName()));
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.warn("Config: \u043f\u0440\u043e\u043f\u0443\u0449\u0435\u043d\u0430 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430 {} \u043c\u043e\u0434\u0443\u043b\u044f {}", new Object[]{setting.getName(), string, exception});
                ModuleConfigurationStore.cacheUnknownSetting(string, setting.getName(), jsonObject.get(setting.getName()));
                bl = false;
            }
        }
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void cacheUnknownSetting(String string2, String string3, JsonElement jsonElement) {
        JsonObject jsonObject;
        if (string2 == null || jsonElement == null) {
            return;
        }
        JsonObject jsonObject2 = jsonObject = pendingSettingsByModule.computeIfAbsent(string2, string -> new JsonObject());
        synchronized (jsonObject2) {
            jsonObject.add(string3, jsonElement.deepCopy());
        }
    }

    public static void refreshModuleSettings() {
        for (ModuleContract moduleContract : ModuleConfigurationStore.getModules()) {
            if (moduleContract instanceof Menu || !(moduleContract instanceof Module)) continue;
            Module module = (Module)moduleContract;
            module.loadSettings();
        }
    }

    private static boolean loadMacroMenuConfiguration(JsonArray jsonArray) {
        Assist assist = RockstarClient.create().getModuleRegistry().getModule(Assist.class);
        if (assist == null || jsonArray == null) {
            return false;
        }
        HashMap<String, AssistItemProvider> providersByName = new HashMap<String, AssistItemProvider>();
        for (AssistItemProvider provider : assist.getAssistSettings()) {
            providersByName.put(provider.getSettingKey(), provider);
        }
        ArrayList arrayList = new ArrayList();
        boolean bl = true;
        for (JsonElement jsonElement : jsonArray) {
            if (!jsonElement.isJsonObject()) {
                bl = false;
                continue;
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (!jsonObject.has("name") || !jsonObject.get("name").isJsonPrimitive()) {
                bl = false;
                continue;
            }
            AssistItemProvider provider = providersByName.get(jsonObject.get("name").getAsString());
            if (provider == null) {
                bl = false;
                continue;
            }
            if (jsonObject.has("key")) {
                JsonElement jsonElement2 = jsonObject.get("key");
                if (!jsonElement2.isJsonPrimitive() || !jsonElement2.getAsJsonPrimitive().isNumber()) {
                    bl = false;
                    continue;
                }
                double d = jsonElement2.getAsDouble();
                if (!Double.isFinite(d) || d < -2.147483648E9 || d > 2.147483647E9) {
                    bl = false;
                    continue;
                }
                provider.setKeyCode(jsonElement2.getAsInt());
            }
            if (jsonObject.has("settings")) {
                if (!jsonObject.get("settings").isJsonObject()) {
                    bl = false;
                    continue;
                }
                if (!ModuleConfigurationStore.applySettingConfiguration(provider.getSettings(), jsonObject.getAsJsonObject("settings"), provider.getSettingKey())) {
                    bl = false;
                }
            }
            arrayList.add(provider);
        }
        assist.replaceAssistEntries(arrayList);
        return bl;
    }

    private static JsonArray getModuleArray(JsonObject jsonObject) {
        if (jsonObject.has("modules") && jsonObject.get("modules").isJsonArray()) {
            return jsonObject.getAsJsonArray("modules");
        }
        return null;
    }

    private static boolean isHiddenModule(ModuleContract moduleContract) {
        ModuleInfo moduleInfo = moduleContract.getClass().getAnnotation(ModuleInfo.class);
        return moduleInfo != null && moduleInfo.hidden();
    }

    public static enum LoadStatus {
        SUCCESS,
        INVALID,
        ERROR;
}
}

