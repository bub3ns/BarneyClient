/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package moscow.rockstar.api.scripts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import moscow.rockstar.api.data.SettingDataStore;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.api.scripts.ScriptRegistry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.items.config.ItemConfigStore;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.config.ModuleConfigurationStore;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.network.http.HttpClientAdapter;
import moscow.rockstar.render.hand.HandSwingPreset;
import moscow.rockstar.render.hand.HandSwingPresetManager;
import moscow.rockstar.render.hand.SwingPresetFile;
import moscow.rockstar.render.hand.SwingPresetFileManager;
import moscow.rockstar.settings.ActionSetting;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.RangeSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.StringSetting;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.input.KeyDisplayFormatter;
import pyrock.utility.render.ColorRGBA;

public final class ScriptConfigCodec {
    private ScriptConfigCodec() {
    }

    public static ModuleContract findModule(String string) {
        if (string == null || string.isBlank()) {
            throw new ScriptConfigurationException("\u043d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d \u043c\u043e\u0434\u0443\u043b\u044c");
        }
        String string2 = ScriptConfigCodec.normalizeIdentifier(string);
        for (ModuleContract object : RockstarClient.create().getModuleRegistry().getModules()) {
            if (!ScriptConfigCodec.normalizeIdentifier(object.getName()).equals(string2)) continue;
            return object;
        }
        ArrayList arrayList = new ArrayList();
        for (ModuleContract moduleContract : RockstarClient.create().getModuleRegistry().getModules()) {
            String string3 = ScriptConfigCodec.normalizeIdentifier(moduleContract.getName());
            boolean bl = string3.contains(string2) || string2.contains(string3) || string2.length() >= 3 && string3.startsWith(string2.substring(0, 3));
            if (!bl) continue;
            arrayList.add(moduleContract.getName());
        }
        throw new ScriptConfigurationException("\u043d\u0435\u0442 \u043c\u043e\u0434\u0443\u043b\u044f \"" + string + "\"" + (String)(arrayList.isEmpty() ? "" : ", \u043f\u043e\u0445\u043e\u0436\u0438\u0435: " + String.join((CharSequence)", ", arrayList)));
    }

    public static JsonObject setModuleEnabled(String string, String string2) {
        ModuleContract moduleContract = ScriptConfigCodec.findModule(string);
        boolean bl = switch (string2 == null ? "toggle" : string2.toLowerCase(Locale.ROOT)) {
            case "enable", "on", "true" -> true;
            case "disable", "off", "false" -> false;
            default -> !moduleContract.isEnabled();
        };
        moduleContract.setEnabled(bl, false);
        ScriptConfigCodec.saveClientSettings();
        return ItemConfigStore.serializeModule(moduleContract, false);
    }

    public static JsonObject setModuleKeyBinding(String string, JsonElement jsonElement) {
        ModuleContract moduleContract = ScriptConfigCodec.findModule(string);
        moduleContract.setKeyBind(ScriptConfigCodec.parseKeyBinding(jsonElement));
        ScriptConfigCodec.saveClientSettings();
        return ItemConfigStore.serializeModule(moduleContract, false);
    }

    public static Setting findSetting(ModuleContract moduleContract, String string) {
        if (string == null || string.isBlank()) {
            throw new ScriptConfigurationException("\u043d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u0430 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430");
        }
        String string2 = ScriptConfigCodec.normalizeIdentifier(string);
        for (Setting setting2 : moduleContract.getSettings()) {
            if (!ScriptConfigCodec.normalizeIdentifier(setting2.getName()).equals(string2) && !ScriptConfigCodec.normalizeIdentifier(Localization.translate(setting2.getName())).equals(string2)) continue;
            return setting2;
        }
        for (Setting setting2 : moduleContract.getSettings()) {
            if (!ScriptConfigCodec.normalizeIdentifier(setting2.getName()).endsWith("." + string2)) continue;
            return setting2;
        }
        ArrayList arrayList = new ArrayList();
        moduleContract.getSettings().forEach(setting -> arrayList.add(setting.getName()));
        throw new ScriptConfigurationException("\u0443 \u043c\u043e\u0434\u0443\u043b\u044f " + moduleContract.getName() + " \u043d\u0435\u0442 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \"" + string + "\"; \u0435\u0441\u0442\u044c: " + String.join((CharSequence)", ", arrayList));
    }

    public static JsonObject setSettingValue(String string, String string2, JsonElement jsonElement) {
        ModuleContract moduleContract = ScriptConfigCodec.findModule(string);
        Setting setting = ScriptConfigCodec.findSetting(moduleContract, string2);
        ScriptConfigCodec.deserializeSettingValue(setting, jsonElement);
        ScriptConfigCodec.saveClientSettings();
        return ItemConfigStore.serializeSetting(setting);
    }

    public static void deserializeSettingValue(Setting setting, JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            throw new ScriptConfigurationException("\u043d\u0435 \u043f\u0435\u0440\u0435\u0434\u0430\u043d\u043e \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435");
        }
        if (setting instanceof BooleanSetting) {
            BooleanSetting booleanSetting = (BooleanSetting)setting;
            if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString() && "toggle".equalsIgnoreCase(jsonElement.getAsString())) {
                booleanSetting.setValueInternal(!booleanSetting.isEnabled());
            } else {
                booleanSetting.setValueInternal(ScriptConfigCodec.parseBoolean(jsonElement));
            }
            return;
        }
        if (setting instanceof NumberSetting) {
            NumberSetting numberSetting = (NumberSetting)setting;
            numberSetting.updateValue(ScriptConfigCodec.parseNumber(jsonElement));
            return;
        }
        if (setting instanceof RangeSetting) {
            RangeSetting rangeSetting = (RangeSetting)setting;
            if (jsonElement.isJsonArray() && jsonElement.getAsJsonArray().size() == 2) {
                rangeSetting.updateFirstValue(jsonElement.getAsJsonArray().get(0).getAsFloat());
                rangeSetting.updateSecondValue(jsonElement.getAsJsonArray().get(1).getAsFloat());
                return;
            }
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has("first")) {
                    rangeSetting.updateFirstValue(jsonObject.get("first").getAsFloat());
                }
                if (jsonObject.has("second")) {
                    rangeSetting.updateSecondValue(jsonObject.get("second").getAsFloat());
                }
                return;
            }
            throw new ScriptConfigurationException("\u0434\u043b\u044f range \u043d\u0443\u0436\u0435\u043d [min, max] \u0438\u043b\u0438 {\"first\":.., \"second\":..}");
        }
        if (setting instanceof ModeSetting) {
            ModeSetting modeSetting = (ModeSetting)setting;
            ModeSetting.Option option = null;
            if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
                int n = jsonElement.getAsInt();
                if (n < 0 || n >= modeSetting.getOptions().size()) {
                    throw new ScriptConfigurationException("\u0440\u0435\u0436\u0438\u043c \u2116" + n + " \u0432\u043d\u0435 \u0441\u043f\u0438\u0441\u043a\u0430");
                }
                option = modeSetting.getOptions().get(n);
            } else {
                String string = ScriptConfigCodec.normalizeIdentifier(jsonElement.getAsString());
                for (ModeSetting.Option option2 : modeSetting.getOptions()) {
                    if (!ScriptConfigCodec.normalizeIdentifier(option2.getName()).equals(string) && !ScriptConfigCodec.normalizeIdentifier(Localization.translate(option2.getName())).equals(string) && !ScriptConfigCodec.normalizeIdentifier(option2.getName()).endsWith("." + string)) continue;
                    option = option2;
                    break;
                }
            }
            if (option == null) {
                throw new ScriptConfigurationException("\u043d\u0435\u0442 \u0442\u0430\u043a\u043e\u0433\u043e \u0440\u0435\u0436\u0438\u043c\u0430; \u0435\u0441\u0442\u044c: " + ScriptConfigCodec.listModeOptions(modeSetting));
            }
            option.select();
            return;
        }
        if (setting instanceof MultiBooleanSetting) {
            MultiBooleanSetting multiBooleanSetting = (MultiBooleanSetting)setting;
            JsonArray jsonArray = new JsonArray();
            if (jsonElement.isJsonArray()) {
                jsonArray = jsonElement.getAsJsonArray();
            } else {
                jsonArray.add(jsonElement.getAsString());
            }
            ArrayList<MultiBooleanSetting.Option> arrayList = new ArrayList<MultiBooleanSetting.Option>();
            for (JsonElement object : jsonArray) {
                String string = ScriptConfigCodec.normalizeIdentifier(object.getAsString());
                MultiBooleanSetting.Option option = null;
                for (MultiBooleanSetting.Option option2 : multiBooleanSetting.getOptions()) {
                    if (!ScriptConfigCodec.normalizeIdentifier(option2.getName()).equals(string) && !ScriptConfigCodec.normalizeIdentifier(Localization.translate(option2.getName())).equals(string) && !ScriptConfigCodec.normalizeIdentifier(option2.getName()).endsWith("." + string)) continue;
                    option = option2;
                    break;
                }
                if (option == null) {
                    throw new ScriptConfigurationException("\u043d\u0435\u0442 \u043f\u0443\u043d\u043a\u0442\u0430 \"" + object.getAsString() + "\"; \u0435\u0441\u0442\u044c: " + ScriptConfigCodec.listBooleanOptions(multiBooleanSetting));
                }
                arrayList.add(option);
            }
            for (MultiBooleanSetting.Option option : new ArrayList<MultiBooleanSetting.Option>(multiBooleanSetting.getOptions())) {
                if (arrayList.contains(option)) continue;
                option.deselect();
            }
            arrayList.forEach(MultiBooleanSetting.Option::select);
            return;
        }
        if (setting instanceof ColorSetting) {
            ColorSetting colorSetting = (ColorSetting)setting;
            colorSetting.setColor(ColorRGBA.fromHex(jsonElement.getAsString()));
            return;
        }
        if (setting instanceof IntegerSetting) {
            IntegerSetting integerSetting = (IntegerSetting)setting;
            integerSetting.setValue(ScriptConfigCodec.parseKeyBinding(jsonElement));
            return;
        }
        if (setting instanceof StringSetting) {
            StringSetting stringSetting = (StringSetting)setting;
            stringSetting.setValue(jsonElement.getAsString());
            return;
        }
        if (setting instanceof ActionSetting) {
            ActionSetting actionSetting = (ActionSetting)setting;
            Runnable runnable = actionSetting.getAction();
            if (runnable == null) {
                throw new ScriptConfigurationException("\u0443 \u043a\u043d\u043e\u043f\u043a\u0438 \u043d\u0435\u0442 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f");
            }
            runnable.run();
            return;
        }
        setting.deserialize(jsonElement);
    }

    public static JsonArray bindMacro(String string, JsonElement jsonElement) {
        if (string == null || string.isBlank()) {
            throw new ScriptConfigurationException("\u043d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u0430 \u043a\u043e\u043c\u0430\u043d\u0434\u0430 \u043c\u0430\u043a\u0440\u043e\u0441\u0430");
        }
        int n = ScriptConfigCodec.parseKeyBinding(jsonElement);
        if (n <= 0) {
            throw new ScriptConfigurationException("\u043d\u0435 \u0440\u0430\u0437\u043e\u0431\u0440\u0430\u043b \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \u043c\u0430\u043a\u0440\u043e\u0441\u0430");
        }
        SettingDataStore settingDataStore = RockstarClient.create().getSettingDataStore();
        settingDataStore.registerBinding(string.trim(), n);
        ScriptConfigCodec.saveClientSettings();
        return ItemConfigStore.collectCommandBindings();
    }

    public static JsonArray removeMacro(String string, JsonElement jsonElement) {
        boolean bl;
        SettingDataStore settingDataStore = RockstarClient.create().getSettingDataStore();
        if (string != null && !string.isBlank() && jsonElement != null && !jsonElement.isJsonNull()) {
            bl = settingDataStore.removeBinding(string.trim(), ScriptConfigCodec.parseKeyBinding(jsonElement));
        } else if (string != null && !string.isBlank()) {
            bl = settingDataStore.removeBindingByCommand(string.trim());
        } else if (jsonElement != null && !jsonElement.isJsonNull()) {
            bl = settingDataStore.removeBindingByKey(ScriptConfigCodec.parseKeyBinding(jsonElement));
        } else {
            throw new ScriptConfigurationException("\u043d\u0443\u0436\u043d\u0430 \u043a\u043e\u043c\u0430\u043d\u0434\u0430 \u0438\u043b\u0438 \u043a\u043b\u0430\u0432\u0438\u0448\u0430 \u043c\u0430\u043a\u0440\u043e\u0441\u0430");
        }
        if (!bl) {
            throw new ScriptConfigurationException("\u0442\u0430\u043a\u043e\u0433\u043e \u043c\u0430\u043a\u0440\u043e\u0441\u0430 \u043d\u0435\u0442");
        }
        ScriptConfigCodec.saveClientSettings();
        return ItemConfigStore.collectCommandBindings();
    }

    public static JsonObject manageConfig(String string, String string2, String string3) {
        switch (string == null ? "list" : string.toLowerCase(Locale.ROOT)) {
            case "list": {
                break;
            }
            case "save": {
                String name = ScriptConfigCodec.requireText(string2, "\u0438\u043c\u044f \u043a\u043e\u043d\u0444\u0438\u0433\u0430");
                ScriptConfigCodec.requireLocalConfiguration(name);
                ModuleConfigurationStore.saveConfiguration();
                break;
            }
            case "load": {
                ScriptConfigCodec.requireLocalConfiguration(string2);
                ModuleConfigurationStore.loadConfigurationFromDisk();
                break;
            }
            case "delete": {
                throw new ScriptConfigurationException("named local configuration cannot be deleted");
            }
            case "rename": {
                throw new ScriptConfigurationException("named local configuration cannot be renamed");
            }
            case "duplicate": {
                throw new ScriptConfigurationException("named local configuration cannot be duplicated");
            }
            case "reset": {
                ModuleConfigurationStore.loadConfigurationFromDisk();
                break;
            }
            default: {
                throw new ScriptConfigurationException("\u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u043e\u0435 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435: " + string);
            }
        }
        return ItemConfigStore.collectConfigurationSnapshot();
    }

    public static JsonObject manageScript(String string, String string2, String string3) {
        ScriptRegistry scriptRegistry = RockstarClient.create().getScriptRegistry();
        JsonObject jsonObject = new JsonObject();
        switch (string == null ? "list" : string.toLowerCase(Locale.ROOT)) {
            case "list": {
                break;
            }
            case "read": {
                jsonObject.addProperty("content", ScriptConfigCodec.readScriptSource(ScriptConfigCodec.requireScript(ScriptConfigCodec.requireText(string2, "\u0438\u043c\u044f \u0441\u043a\u0440\u0438\u043f\u0442\u0430"))));
                break;
            }
            case "write": 
            case "create": {
                String string4 = ScriptConfigCodec.requireText(string2, "\u0438\u043c\u044f \u0441\u043a\u0440\u0438\u043f\u0442\u0430");
                boolean bl = ScriptConfigCodec.findScript(string4) == null;
                ScriptConfigCodec.writeScriptSource(string4, string3 == null ? "" : string3);
                scriptRegistry.discoverScripts();
                if (bl) {
                    scriptRegistry.setScriptEnabled(string4, true);
                }
                jsonObject.addProperty("written", string4);
                ScriptDescriptor scriptDescriptor = ScriptConfigCodec.findScript(string4);
                if (scriptDescriptor == null) break;
                jsonObject.addProperty("loaded", Boolean.valueOf(scriptDescriptor.isLoaded()));
                if (scriptDescriptor.getLoadError() == null) break;
                jsonObject.addProperty("error", scriptDescriptor.getLoadError());
                break;
            }
            case "delete": {
                ScriptDescriptor scriptDescriptor = ScriptConfigCodec.requireScript(ScriptConfigCodec.requireText(string2, "\u0438\u043c\u044f \u0441\u043a\u0440\u0438\u043f\u0442\u0430"));
                if (!scriptDescriptor.deleteScriptFile()) {
                    throw new ScriptConfigurationException("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0441\u043a\u0440\u0438\u043f\u0442 " + string2);
                }
                jsonObject.addProperty("deleted", string2);
                break;
            }
            case "enable": 
            case "load": {
                scriptRegistry.setScriptEnabled(ScriptConfigCodec.requireText(string2, "\u0438\u043c\u044f \u0441\u043a\u0440\u0438\u043f\u0442\u0430"), true);
                break;
            }
            case "disable": 
            case "unload": {
                scriptRegistry.setScriptEnabled(ScriptConfigCodec.requireText(string2, "\u0438\u043c\u044f \u0441\u043a\u0440\u0438\u043f\u0442\u0430"), false);
                break;
            }
            case "reload": {
                scriptRegistry.discoverScripts();
                break;
            }
            default: {
                throw new ScriptConfigurationException("\u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u043e\u0435 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435: " + string);
            }
        }
        jsonObject.add("scripts", (JsonElement)ItemConfigStore.collectScriptSummaries());
        return jsonObject;
    }

    private static ScriptDescriptor requireScript(String string) {
        ScriptDescriptor scriptDescriptor = ScriptConfigCodec.findScript(string);
        if (scriptDescriptor == null) {
            throw new ScriptConfigurationException("\u043d\u0435\u0442 \u0441\u043a\u0440\u0438\u043f\u0442\u0430 \"" + string + "\"");
        }
        return scriptDescriptor;
    }

    private static ScriptDescriptor findScript(String string) {
        for (ScriptDescriptor scriptDescriptor : RockstarClient.create().getScriptRegistry().getScripts()) {
            if (!ScriptConfigCodec.normalizeIdentifier(scriptDescriptor.getScriptName()).equals(ScriptConfigCodec.normalizeIdentifier(string))) continue;
            return scriptDescriptor;
        }
        return null;
    }

    private static String readScriptSource(ScriptDescriptor scriptDescriptor) {
        if (scriptDescriptor.getScriptFile() == null) {
            throw new ScriptConfigurationException("\u0441\u043a\u0440\u0438\u043f\u0442 " + scriptDescriptor.getScriptName() + " \u0437\u0430\u0449\u0438\u0449\u0451\u043d\u043d\u044b\u0439 (\u043a\u0443\u043f\u043b\u0435\u043d \u043d\u0430 \u0441\u0430\u0439\u0442\u0435) \u2014 \u0438\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0430 \u043d\u0435\u0442");
        }
        try {
            return Files.readString(scriptDescriptor.getScriptFile().toPath(), StandardCharsets.UTF_8);
        }
        catch (IOException iOException) {
            throw new ScriptConfigurationException("\u043d\u0435 \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u043b \u0441\u043a\u0440\u0438\u043f\u0442: " + iOException.getMessage());
        }
    }

    private static void writeScriptSource(String string, String string2) {
        File file = new File(moscow.rockstar.core.ClientPaths.gameDirectory(), "scripts");
        if (!file.exists() && !file.mkdirs()) {
            throw new ScriptConfigurationException("\u043d\u0435 \u0441\u043e\u0437\u0434\u0430\u043b \u043f\u0430\u043f\u043a\u0443 \u0441\u043a\u0440\u0438\u043f\u0442\u043e\u0432");
        }
        Object object = string.endsWith(".py") ? string : string + ".py";
        try {
            Files.writeString(new File(file, (String)object).toPath(), (CharSequence)string2, StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (IOException iOException) {
            throw new ScriptConfigurationException("\u043d\u0435 \u0437\u0430\u043f\u0438\u0441\u0430\u043b \u0441\u043a\u0440\u0438\u043f\u0442: " + iOException.getMessage());
        }
    }

    public static JsonObject manageSwingPreset(String string, String string2, String string3, String string4, JsonElement jsonElement) {
        HandSwingPresetManager handSwingPresetManager = RockstarClient.create().getHandSwingPresetManager();
        switch (string == null ? "state" : string.toLowerCase(Locale.ROOT)) {
            case "state": 
            case "list": {
                break;
            }
            case "apply": {
                ScriptConfigCodec.selectSwingPreset(handSwingPresetManager, ScriptConfigCodec.requireText(string2, "\u0438\u043c\u044f \u043f\u0440\u0435\u0441\u0435\u0442\u0430"));
                break;
            }
            case "set": {
                List<Setting> list = switch (string3 == null ? "shared" : string3.toLowerCase(Locale.ROOT)) {
                    case "start" -> handSwingPresetManager.getInitialSwing().getSettings();
                    case "end" -> handSwingPresetManager.getFinalSwing().getSettings();
                    case "shared" -> handSwingPresetManager.getSettings().getSettings();
                    default -> throw new ScriptConfigurationException("phase \u0431\u044b\u0432\u0430\u0435\u0442 shared, start \u0438\u043b\u0438 end");
                };
                Object object = null;
                String string5 = ScriptConfigCodec.normalizeIdentifier(ScriptConfigCodec.requireText(string4, "\u0438\u043c\u044f \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438"));
                for (Setting setting : list) {
                    if (!ScriptConfigCodec.normalizeIdentifier(setting.getName()).equals(string5) && !ScriptConfigCodec.normalizeIdentifier(setting.getName()).endsWith("." + string5)) continue;
                    object = setting;
                    break;
                }
                if (object == null) {
                    throw new ScriptConfigurationException("\u0432 \u0444\u0430\u0437\u0435 \u043d\u0435\u0442 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \"" + string4 + "\"");
                }
                ScriptConfigCodec.deserializeSettingValue((Setting)object, jsonElement);
                ScriptConfigCodec.saveClientSettings();
                break;
            }
            default: {
                throw new ScriptConfigurationException("\u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u043e\u0435 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435: " + string);
            }
        }
        return ItemConfigStore.collectPresetSnapshot();
    }

    private static void selectSwingPreset(HandSwingPresetManager handSwingPresetManager, String string) {
        String string2 = ScriptConfigCodec.normalizeIdentifier(string);
        for (HandSwingPreset preset : handSwingPresetManager.getPresets()) {
            if (!ScriptConfigCodec.normalizeIdentifier(preset.getName()).equals(string2) && !ScriptConfigCodec.normalizeIdentifier(Localization.translate(preset.getName())).equals(string2) && !ScriptConfigCodec.normalizeIdentifier(preset.getName()).endsWith("." + string2)) continue;
            handSwingPresetManager.selectPreset(preset);
            ScriptConfigCodec.saveClientSettings();
            return;
        }
        SwingPresetFileManager fileManager = RockstarClient.create().getSwingPresetFileManager();
        fileManager.reload();
        for (SwingPresetFile preset : fileManager.getPresets()) {
            if (!ScriptConfigCodec.normalizeIdentifier(preset.getName()).equals(string2)) continue;
            preset.load();
            ScriptConfigCodec.saveClientSettings();
            return;
        }
        throw new ScriptConfigurationException("\u043d\u0435\u0442 \u043f\u0440\u0435\u0441\u0435\u0442\u0430 \u0441\u0432\u0438\u043d\u0433\u0430 \"" + string + "\"");
    }

    public static JsonObject sendChatMessage(String string) {
        if (string == null || string.isBlank()) {
            throw new ScriptConfigurationException("\u043f\u0443\u0441\u0442\u043e\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435");
        }
        if (ClientAccess.minecraftClient.player == null || ClientAccess.minecraftClient.getNetworkHandler() == null) {
            throw new ScriptConfigurationException("\u0438\u0433\u0440\u043e\u043a \u043d\u0435 \u0432 \u0438\u0433\u0440\u0435 \u2014 \u043f\u0438\u0441\u0430\u0442\u044c \u043d\u0435\u043a\u0443\u0434\u0430");
        }
        String string2 = string.trim();
        JsonObject jsonObject = new JsonObject();
        String string3 = RockstarClient.create().getNavigationCommandService().getCommandPrefix();
        if (!string3.isEmpty() && string2.startsWith(string3 + string3)) {
            ClientAccess.minecraftClient.getNetworkHandler().sendChatMessage(string2.substring(string3.length()));
            jsonObject.addProperty("sent", "\u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0432 \u0447\u0430\u0442");
        } else if (!string3.isEmpty() && string2.startsWith(string3)) {
            RockstarClient.create().getNavigationCommandService().executeCommand(string2);
            jsonObject.addProperty("sent", "\u043a\u043b\u0438\u0435\u043d\u0442\u0441\u043a\u0430\u044f \u043a\u043e\u043c\u0430\u043d\u0434\u0430");
        } else if (string2.startsWith("/")) {
            ClientAccess.minecraftClient.getNetworkHandler().sendChatCommand(string2.substring(1));
            jsonObject.addProperty("sent", "\u043a\u043e\u043c\u0430\u043d\u0434\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0430");
        } else {
            ClientAccess.minecraftClient.getNetworkHandler().sendChatMessage(string2);
            jsonObject.addProperty("sent", "\u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0432 \u0447\u0430\u0442");
        }
        jsonObject.addProperty("text", string2);
        return jsonObject;
    }

    public static JsonObject manageMenu(String string) {
        Menu menu = RockstarClient.create().getModuleRegistry().getModule(Menu.class);
        switch (string == null ? "open" : string.toLowerCase(Locale.ROOT)) {
            case "open": {
                if (menu.isEnabled()) break;
                menu.setEnabled(true, true);
                break;
            }
            case "close": {
                if (menu.isEnabled()) {
                    menu.setEnabled(false, true);
                }
                ClientAccess.minecraftClient.setScreen(null);
                break;
            }
            case "toggle": {
                menu.toggle();
                break;
            }
            default: {
                throw new ScriptConfigurationException("\u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u043e\u0435 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435: " + string);
            }
        }
        return ItemConfigStore.collectScreenSnapshot();
    }

    public static int parseKeyBinding(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return 0;
        }
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsInt();
        }
        String string = jsonElement.getAsString().trim();
        if (string.isEmpty() || string.equalsIgnoreCase("none") || string.equalsIgnoreCase("\u043d\u0435\u0442")) {
            return 0;
        }
        int n = KeyDisplayFormatter.parseKey(string);
        if (n == -1) {
            throw new ScriptConfigurationException("\u043d\u0435 \u0437\u043d\u0430\u044e \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \"" + string + "\"");
        }
        return n;
    }

    private static boolean parseBoolean(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isBoolean()) {
            return jsonElement.getAsBoolean();
        }
        String string = jsonElement.getAsString().trim().toLowerCase(Locale.ROOT);
        return string.equals("true") || string.equals("on") || string.equals("1") || string.equals("\u0434\u0430");
    }

    private static float parseNumber(JsonElement jsonElement) {
        try {
            return jsonElement.getAsFloat();
        }
        catch (Exception exception) {
            throw new ScriptConfigurationException("\u043e\u0436\u0438\u0434\u0430\u043b\u043e\u0441\u044c \u0447\u0438\u0441\u043b\u043e, \u043f\u0440\u0438\u0448\u043b\u043e " + String.valueOf(jsonElement));
        }
    }

    private static String listModeOptions(ModeSetting modeSetting) {
        ArrayList arrayList = new ArrayList();
        modeSetting.getOptions().forEach(option -> arrayList.add(option.getName()));
        return String.join((CharSequence)", ", arrayList);
    }

    private static String listBooleanOptions(MultiBooleanSetting multiBooleanSetting) {
        ArrayList arrayList = new ArrayList();
        multiBooleanSetting.getOptions().forEach(option -> arrayList.add(option.getName()));
        return String.join((CharSequence)", ", arrayList);
    }

    private static String requireText(String string, String string2) {
        if (string == null || string.isBlank()) {
            throw new ScriptConfigurationException("\u043d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u043e: " + string2);
        }
        return string.trim();
    }

    private static void requireLocalConfiguration(String name) {
        if (name == null || !"local".equalsIgnoreCase(name.trim())) {
            throw new ScriptConfigurationException("only the local configuration is available");
        }
    }

    private static void saveClientSettings() {
        ModuleConfigurationStore.saveConfiguration();
    }

    static String normalizeIdentifier(String string) {
        return string == null ? "" : string.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "");
    }
}
