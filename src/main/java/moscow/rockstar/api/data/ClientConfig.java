/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.Vec3d
 */
package moscow.rockstar.api.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import moscow.rockstar.api.scripts.ScriptRegistry;
import moscow.rockstar.api.settings.SettingEntry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.config.ModuleConfigurationStore;
import moscow.rockstar.modules.other.auth.AutoAuth;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.colors.ThemeColorSettings;
import moscow.rockstar.render.hand.HandSwingPreset;
import moscow.rockstar.render.hand.HandSwingPresetManager;
import moscow.rockstar.render.hand.SwingPresetFile;
import moscow.rockstar.render.hand.SwingPresetFileManager;
import moscow.rockstar.server.staff.StaffListManager;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.ui.color.ColorPickerWidget;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.localization.Language;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.theme.ColorTheme;
import moscow.rockstar.world.waypoints.WaypointStore;
import net.minecraft.util.math.Vec3d;
import pyrock.utility.render.ColorRGBA;

/**
 * The "client" settings document, {@code Rockstar/client.rock}.  It carries the
 * user-owned state that is not part of the module config: HUD element geometry
 * and visibility, colour-picker swatches, friends, the staff roster, waypoints,
 * macros, enabled scripts, the theme and the selected swing.
 *
 * <p>ORIGINAL: {@code rockstar/ilIlil/IiIIiIiI}, annotated
 * {@code @IiIIiIIi(I="client")}.</p>
 */
@ConfigName(value="client")
public class ClientConfig
extends ConfigEntry
implements ClientAccess {
    private static final Map<String, JsonObject> CACHED_HUD_STATE = new ConcurrentHashMap<String, JsonObject>();
    private int hitIslandBestScore;
    private int hitIslandGamesPlayed;
    /**
     * Set when the document could not be read, or when one of its sections threw
     * while loading.  Only the (omitted) cloud sync consumes it in the original.
     */
    private volatile boolean broken;
    /**
     * When set, {@link #save()} writes an empty document instead of the real one.
     * Only the (omitted) cloud sync sets it in the original.
     */
    private volatile boolean suppressed;

    @Override
    public void save() {
        try {
            ClientConfigManager.writeJson(this.getFile(), this.suppressed ? new JsonObject() : this.serialize());
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * ORIGINAL: {@code I()Lcom/google/gson/JsonObject;}
     *
     * <p>The original also writes {@code lastConfig} here, taken from the cloud
     * config-list service ({@code rockstar/ilIlil/IIiiiiiI}, a {@code globals}
     * consumer).  That service is excluded from this tree, so the key is not
     * written.  {@code autoSaveConfigs} comes from the same service in the
     * original and is read here from {@link ModuleConfigurationStore}, which is
     * where the rest of this tree already keeps that flag.</p>
     */
    public JsonObject serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", minecraftClient.getSession().getUsername());
        jsonObject.addProperty("language", Localization.getLanguage().name());
        jsonObject.addProperty("themeMode", RockstarClient.create().getColorTheme().name());
        jsonObject.add("themeData", ClientConfig.serializeThemeData(ColorPalette.getThemeColorSettings()));
        jsonObject.addProperty("swing", RockstarClient.create().getHandSwingPresetManager().getSelectedPresetName());
        jsonObject.add("hudElements", this.serializeHudElements());
        jsonObject.add("friends", this.serializeFriends());
        jsonObject.add("staff", this.serializeStaff());
        jsonObject.add("colorPickerPresets", this.serializeColorPickerPresets());
        jsonObject.add("password", this.serializePasswords());
        jsonObject.add("waypoints", this.serializeWaypoints());
        jsonObject.add("macros", this.serializeMacros());
        jsonObject.add("enabledScripts", this.serializeEnabledScripts());
        jsonObject.addProperty("autoSaveConfigs", Boolean.valueOf(ModuleConfigurationStore.isAutoSaveEnabled()));
        jsonObject.addProperty("hitIslandBestScore", (Number)Integer.valueOf(this.hitIslandBestScore));
        jsonObject.addProperty("hitIslandGamesPlayed", (Number)Integer.valueOf(this.hitIslandGamesPlayed));
        return jsonObject;
    }

    @Override
    public void load() {
        try (FileReader fileReader = new FileReader(this.getFile());){
            JsonObject jsonObject = ClientConfigManager.GSON.fromJson((Reader)fileReader, JsonObject.class);
            if (jsonObject == null) {
                this.preserveBrokenFile();
                return;
            }
            LoadResult loadResult = this.deserialize(jsonObject);
            if (loadResult.cleanedFriends() && !loadResult.failed()) {
                this.save();
            }
        }
        catch (Exception exception) {
            this.preserveBrokenFile();
            RockstarClient.LOGGER.error("Failed to read client data", (Throwable)exception);
        }
    }

    /** ORIGINAL: {@code i(Lcom/google/gson/JsonObject;)Lrockstar/ilIlil/IiIIiIiI$I;} */
    public LoadResult deserialize(JsonObject jsonObject) {
        if (jsonObject == null) {
            this.broken = true;
            return new LoadResult(false, true);
        }
        boolean bl = false;
        boolean bl2 = false;
        if (jsonObject.has("password")) {
            try {
                this.loadPasswords(jsonObject.getAsJsonArray("password"));
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("password", jsonObject, exception);
            }
        }
        if (jsonObject.has("swing")) {
            try {
                String string = jsonObject.get("swing").getAsString();
                HandSwingPresetManager handSwingPresetManager = RockstarClient.create().getHandSwingPresetManager();
                SwingPresetFileManager swingPresetFileManager = RockstarClient.create().getSwingPresetFileManager();
                swingPresetFileManager.reload();
                SwingPresetFile swingPresetFile = swingPresetFileManager.find(string);
                if (swingPresetFile != null) {
                    swingPresetFile.load();
                } else {
                    for (HandSwingPreset handSwingPreset : handSwingPresetManager.getPresets()) {
                        if (!handSwingPreset.getName().equals(string)) continue;
                        handSwingPresetManager.selectPreset(handSwingPreset);
                    }
                }
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("swing", jsonObject, exception);
            }
        }
        if (jsonObject.has("language")) {
            try {
                try {
                    Localization.setLanguage(Language.valueOf(jsonObject.get("language").getAsString()));
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    Localization.setLanguage(Language.EN_US);
                }
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("language", jsonObject, exception);
            }
        }
        if (jsonObject.has("themeMode") || jsonObject.has("theme")) {
            try {
                String string = jsonObject.has("themeMode") ? jsonObject.get("themeMode").getAsString() : null;
                if (string != null) {
                    try {
                        RockstarClient.create().setColorTheme(ColorTheme.valueOf(string));
                    }
                    catch (IllegalArgumentException illegalArgumentException) {
                        RockstarClient.LOGGER.warn("Unknown theme mode in client data: {}", (Object)string);
                    }
                } else {
                    try {
                        RockstarClient.create().setColorTheme(ColorTheme.valueOf(jsonObject.get("theme").getAsString()));
                    }
                    catch (IllegalArgumentException illegalArgumentException) {}
                }
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("theme", jsonObject, exception);
            }
        }
        if (jsonObject.has("friends")) {
            try {
                JsonArray jsonArray = jsonObject.getAsJsonArray("friends");
                ArrayList<String> arrayList = new ArrayList<String>();
                for (JsonElement jsonElement : jsonArray) {
                    arrayList.add(jsonElement == null || jsonElement.isJsonNull() ? null : jsonElement.getAsString());
                }
                bl = RockstarClient.create().getFriendListManager().replaceCustomFriends(arrayList);
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("friends", jsonObject, exception);
            }
        }
        if (jsonObject.has("staff")) {
            try {
                JsonArray jsonArray = jsonObject.getAsJsonArray("staff");
                ConfigEntry configEntry = ClientConfigManager.getInstance().get("staff");
                if (configEntry == null || !configEntry.getFile().exists()) {
                    ArrayList<StaffListManager.StaffEntry> arrayList = new ArrayList<StaffListManager.StaffEntry>();
                    for (JsonElement jsonElement : jsonArray) {
                        if (jsonElement.isJsonObject()) {
                            JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                            arrayList.add(new StaffListManager.StaffEntry(jsonObject2.has("name") ? jsonObject2.get("name").getAsString() : "", jsonObject2.has("prefix") ? jsonObject2.get("prefix").getAsString() : ""));
                            continue;
                        }
                        if (!jsonElement.isJsonPrimitive()) continue;
                        arrayList.add(new StaffListManager.StaffEntry(jsonElement.getAsString(), "MODER"));
                    }
                    RockstarClient.create().getStaffListManager().loadStaffMembers(arrayList);
                    if (configEntry != null) {
                        ClientConfigManager.getInstance().save(configEntry);
                    }
                }
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("staff", jsonObject, exception);
            }
        }
        if (jsonObject.has("colorPickerPresets")) {
            try {
                this.loadColorPickerPresets(jsonObject.getAsJsonArray("colorPickerPresets"));
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("colorPickerPresets", jsonObject, exception);
            }
        }
        if (jsonObject.has("waypoints")) {
            try {
                this.loadWaypoints(jsonObject.getAsJsonArray("waypoints"));
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("waypoints", jsonObject, exception);
            }
        }
        if (jsonObject.has("macros")) {
            try {
                this.loadMacros(jsonObject.getAsJsonArray("macros"));
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("macros", jsonObject, exception);
            }
        }
        if (jsonObject.has("enabledScripts")) {
            try {
                ArrayList<String> arrayList = new ArrayList<String>();
                for (JsonElement jsonElement : jsonObject.getAsJsonArray("enabledScripts")) {
                    if (jsonElement == null || !jsonElement.isJsonPrimitive()) continue;
                    arrayList.add(jsonElement.getAsString());
                }
                ScriptRegistry scriptRegistry = RockstarClient.create().getScriptRegistry();
                if (scriptRegistry != null) {
                    scriptRegistry.setEnabledScripts(arrayList);
                }
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("enabledScripts", jsonObject, exception);
            }
        }
        if (jsonObject.has("hudElements")) {
            try {
                JsonArray jsonArray = jsonObject.getAsJsonArray("hudElements");
                for (JsonElement jsonElement : jsonArray) {
                    this.applyHudElementJson(jsonElement.getAsJsonObject());
                }
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("hudElements", jsonObject, exception);
            }
        }
        if (jsonObject.has("hitIslandBestScore")) {
            try {
                this.hitIslandBestScore = jsonObject.get("hitIslandBestScore").getAsInt();
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("hitIslandBestScore", jsonObject, exception);
            }
        }
        if (jsonObject.has("hitIslandGamesPlayed")) {
            try {
                this.hitIslandGamesPlayed = jsonObject.get("hitIslandGamesPlayed").getAsInt();
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("hitIslandGamesPlayed", jsonObject, exception);
            }
        }
        if (jsonObject.has("themeData")) {
            try {
                ColorPalette.applyThemeColorSettings(ClientConfig.deserializeThemeData(jsonObject.getAsJsonObject("themeData")));
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("themeData", jsonObject, exception);
            }
        }
        if (jsonObject.has("autoSaveConfigs")) {
            try {
                ModuleConfigurationStore.setAutoSaveEnabled(jsonObject.get("autoSaveConfigs").getAsBoolean());
            }
            catch (Exception exception) {
                bl2 = true;
                this.reportBrokenSection("autoSaveConfigs", jsonObject, exception);
            }
        }
        return new LoadResult(bl, bl2);
    }

    /** ORIGINAL: {@code II()V} */
    public void markClean() {
        this.broken = false;
    }

    /** ORIGINAL: {@code I(Ljava/lang/String;Lcom/google/gson/JsonObject;Ljava/lang/Exception;)V} */
    private void reportBrokenSection(String string, JsonObject jsonObject, Exception exception) {
        this.broken = true;
        this.writeBrokenCopy(jsonObject);
        RockstarClient.LOGGER.error("Failed to load {} from client data", (Object)string, (Object)exception);
    }

    /** ORIGINAL: {@code I(Lcom/google/gson/JsonObject;)V} */
    private void writeBrokenCopy(JsonObject jsonObject) {
        try {
            ClientConfigManager.writeJson(new File(this.getFile().getPath() + ".broken"), jsonObject);
        }
        catch (IOException iOException) {
            RockstarClient.LOGGER.error("Failed to preserve broken client data", (Throwable)iOException);
        }
    }

    /** ORIGINAL: {@code Ii()V} */
    private void preserveBrokenFile() {
        this.broken = true;
        try {
            Files.copy(this.getFile().toPath(), new File(this.getFile().getPath() + ".broken").toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException iOException) {
            RockstarClient.LOGGER.error("Failed to preserve broken client data", (Throwable)iOException);
        }
    }

    /** ORIGINAL: {@code I()Lcom/google/gson/JsonArray;} */
    private JsonArray serializeHudElements() {
        JsonArray jsonArray = new JsonArray();
        HashSet<String> hashSet = new HashSet<String>();
        for (HudElement hudElement : RockstarClient.create().getHudElementRegistry().elements()) {
            hashSet.add(hudElement.getName());
            jsonArray.add(this.serializeHudElement(hudElement));
        }
        for (Map.Entry<String, JsonObject> entry : CACHED_HUD_STATE.entrySet()) {
            if (hashSet.contains(entry.getKey())) continue;
            jsonArray.add(entry.getValue().deepCopy());
        }
        return jsonArray;
    }

    /** ORIGINAL: static {@code I(Ljava/lang/String;)V} */
    public static void invalidateHudElement(String string) {
        if (string != null) {
            CACHED_HUD_STATE.remove(string);
        }
    }

    /** ORIGINAL: {@code I(Lrockstar/ilIlil/IiIiIIiII;)Lcom/google/gson/JsonObject;} */
    private JsonObject serializeHudElement(HudElement hudElement) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", hudElement.getName());
        jsonObject.addProperty("x", (Number)Float.valueOf(hudElement.getX()));
        jsonObject.addProperty("y", (Number)Float.valueOf(hudElement.getY()));
        jsonObject.addProperty("showing", Boolean.valueOf(hudElement.isShowing()));
        jsonObject.add("settings", this.serializeHudElementSettings(hudElement));
        return jsonObject;
    }

    /** ORIGINAL: static {@code I(Lrockstar/ilIlil/IiIiIIiII;)V} */
    public static void cacheHudElement(HudElement hudElement) {
        if (hudElement == null) {
            return;
        }
        ConfigEntry configEntry = ClientConfigManager.getInstance().get("client");
        if (configEntry instanceof ClientConfig) {
            ClientConfig clientConfig = (ClientConfig)configEntry;
            try {
                CACHED_HUD_STATE.put(hudElement.getName(), clientConfig.serializeHudElement(hudElement));
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.warn("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0441\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0435 hud-\u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 {}", (Object)hudElement.getName(), (Object)exception);
            }
        }
    }

    /** ORIGINAL: {@code I(Lrockstar/ilIlil/IiIiIIiII;)Z} */
    public boolean applySavedState(HudElement hudElement) {
        if (hudElement == null) {
            return false;
        }
        JsonObject jsonObject = CACHED_HUD_STATE.get(hudElement.getName());
        if (jsonObject != null) {
            return this.applyHudElementState(jsonObject, hudElement);
        }
        if (this.getFile() == null) {
            return false;
        }
        if (!this.getFile().exists()) {
            return false;
        }
        try (FileReader fileReader = new FileReader(this.getFile());){
            JsonObject jsonObject2 = ClientConfigManager.GSON.fromJson((Reader)fileReader, JsonObject.class);
            if (jsonObject2 == null || !jsonObject2.has("hudElements")) {
                return false;
            }
            JsonArray jsonArray = jsonObject2.getAsJsonArray("hudElements");
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject3 = jsonElement.getAsJsonObject();
                if (!jsonObject3.has("name")) continue;
                String string = jsonObject3.get("name").getAsString();
                if (!hudElement.getName().equalsIgnoreCase(string)) continue;
                return this.applyHudElementState(jsonObject3, hudElement);
            }
            return false;
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("Failed to apply saved HUD state for {}", (Object)hudElement.getName(), (Object)exception);
        }
        return false;
    }

    /** ORIGINAL: {@code I(Ljava/lang/Iterable;)V} */
    public void applySavedState(Iterable<? extends HudElement> iterable) {
        if (iterable == null) {
            return;
        }
        ArrayList<HudElement> arrayList = new ArrayList<HudElement>();
        for (HudElement hudElement : iterable) {
            if (hudElement == null) continue;
            JsonObject jsonObject = CACHED_HUD_STATE.get(hudElement.getName());
            if (jsonObject != null) {
                this.applyHudElementState(jsonObject, hudElement);
                continue;
            }
            arrayList.add(hudElement);
        }
        if (arrayList.isEmpty() || this.getFile() == null || !this.getFile().exists()) {
            return;
        }
        try (FileReader fileReader = new FileReader(this.getFile());){
            JsonObject jsonObject = ClientConfigManager.GSON.fromJson((Reader)fileReader, JsonObject.class);
            if (jsonObject == null || !jsonObject.has("hudElements")) {
                return;
            }
            JsonArray jsonArray = jsonObject.getAsJsonArray("hudElements");
            block9: for (HudElement hudElement : arrayList) {
                Iterator<JsonElement> iterator = jsonArray.iterator();
                while (iterator.hasNext()) {
                    JsonElement jsonElement = iterator.next();
                    JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                    if (!jsonObject2.has("name") || !hudElement.getName().equalsIgnoreCase(jsonObject2.get("name").getAsString())) continue;
                    this.applyHudElementState(jsonObject2, hudElement);
                    continue block9;
                }
            }
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("Failed to apply saved HUD settings", (Throwable)exception);
        }
    }

    /** ORIGINAL: {@code I(Lcom/google/gson/JsonObject;)Z} */
    private boolean applyHudElementJson(JsonObject jsonObject) {
        if (jsonObject == null || !jsonObject.has("name")) {
            return false;
        }
        String string = jsonObject.get("name").getAsString();
        CACHED_HUD_STATE.put(string, jsonObject.deepCopy());
        HudElement hudElement = RockstarClient.create().getHudElementRegistry().getByName(string);
        return hudElement != null && this.applyHudElementState(jsonObject, hudElement);
    }

    /** ORIGINAL: {@code I(Lcom/google/gson/JsonObject;Lrockstar/ilIlil/IiIiIIiII;)Z} */
    private boolean applyHudElementState(JsonObject jsonObject, HudElement hudElement) {
        if (jsonObject == null || hudElement == null) {
            return false;
        }
        if (jsonObject.has("x")) {
            hudElement.setX(jsonObject.get("x").getAsFloat());
        }
        if (jsonObject.has("y")) {
            hudElement.setY(jsonObject.get("y").getAsFloat());
        }
        if (jsonObject.has("showing")) {
            hudElement.setShowing(jsonObject.get("showing").getAsBoolean());
        }
        if (jsonObject.has("settings")) {
            JsonObject jsonObject2 = jsonObject.getAsJsonObject("settings");
            for (Setting setting : hudElement.getSettings()) {
                if (!jsonObject2.has(setting.getName())) continue;
                setting.deserialize(jsonObject2.get(setting.getName()));
            }
        }
        return true;
    }

    /** ORIGINAL: {@code i(Lrockstar/ilIlil/IiIiIIiII;)Lcom/google/gson/JsonObject;} */
    private JsonObject serializeHudElementSettings(HudElement hudElement) {
        JsonObject jsonObject = new JsonObject();
        for (Setting setting : hudElement.getSettings()) {
            jsonObject.add(setting.getName(), setting.serialize());
        }
        return jsonObject;
    }

    /** ORIGINAL: {@code i()Lcom/google/gson/JsonArray;} */
    private JsonArray serializeStaff() {
        JsonArray jsonArray = new JsonArray();
        for (StaffListManager.StaffEntry staffEntry : RockstarClient.create().getStaffListManager().getStaffMembers()) {
            if (staffEntry.getName() == null || staffEntry.getName().isBlank()) continue;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", staffEntry.getName());
            jsonObject.addProperty("prefix", staffEntry.getPrefix());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /** ORIGINAL: {@code II()Lcom/google/gson/JsonArray;} */
    private JsonArray serializeWaypoints() {
        JsonArray jsonArray = new JsonArray();
        WaypointStore waypointStore = RockstarClient.create().getWaypointStore();
        if (waypointStore == null) {
            return jsonArray;
        }
        for (Map.Entry<String, Vec3d> entry : waypointStore.getWaypoints()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", entry.getKey());
            jsonObject.addProperty("x", (Number)Double.valueOf(entry.getValue().x));
            jsonObject.addProperty("y", (Number)Double.valueOf(entry.getValue().y));
            jsonObject.addProperty("z", (Number)Double.valueOf(entry.getValue().z));
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /** ORIGINAL: {@code Ii()Lcom/google/gson/JsonArray;} */
    private JsonArray serializeEnabledScripts() {
        JsonArray jsonArray = new JsonArray();
        ScriptRegistry scriptRegistry = RockstarClient.create().getScriptRegistry();
        if (scriptRegistry != null) {
            for (String string : scriptRegistry.getEnabledScriptNames()) {
                jsonArray.add(string);
            }
        }
        return jsonArray;
    }

    /** ORIGINAL: {@code iI()Lcom/google/gson/JsonArray;} */
    private JsonArray serializeMacros() {
        JsonArray jsonArray = new JsonArray();
        if (RockstarClient.create().getSettingDataStore() == null) {
            return jsonArray;
        }
        for (SettingEntry settingEntry : RockstarClient.create().getSettingDataStore().getBindings()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("key", (Number)Integer.valueOf(settingEntry.getKeyCode()));
            jsonObject.addProperty("command", settingEntry.getCommand());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /** ORIGINAL: {@code I(Lcom/google/gson/JsonArray;)V} */
    private void loadWaypoints(JsonArray jsonArray) {
        WaypointStore waypointStore = RockstarClient.create().getWaypointStore();
        if (waypointStore == null) {
            return;
        }
        HashMap<String, Vec3d> hashMap = new HashMap<String, Vec3d>();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String string = jsonObject.get("name").getAsString();
            double d = jsonObject.get("x").getAsDouble();
            double d2 = jsonObject.get("y").getAsDouble();
            double d3 = jsonObject.get("z").getAsDouble();
            hashMap.put(string, new Vec3d(d, d2, d3));
        }
        waypointStore.replaceWaypoints(hashMap);
    }

    /** ORIGINAL: {@code ii()Lcom/google/gson/JsonArray;} */
    private JsonArray serializeFriends() {
        JsonArray jsonArray = new JsonArray();
        for (String string : RockstarClient.create().getFriendListManager().getFriends()) {
            if (string == null || string.isBlank()) continue;
            jsonArray.add(string);
        }
        return jsonArray;
    }

    /** ORIGINAL: {@code III()Lcom/google/gson/JsonArray;} */
    private JsonArray serializePasswords() {
        JsonArray jsonArray = new JsonArray();
        for (Map.Entry<String, String> entry : RockstarClient.create().getModuleRegistry().getModule(AutoAuth.class).getSavedPasswords().entrySet()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("nick", entry.getKey());
            jsonObject.addProperty("pass", entry.getValue());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /** ORIGINAL: {@code IIi()Lcom/google/gson/JsonArray;} */
    private JsonArray serializeColorPickerPresets() {
        JsonArray jsonArray = new JsonArray();
        for (ColorPickerWidget.SavedColor savedColor : ColorPickerWidget.SAVED_COLORS) {
            if (!savedColor.isEnabled()) continue;
            JsonObject jsonObject = new JsonObject();
            ColorRGBA colorRGBA = savedColor.getColor();
            jsonObject.addProperty("red", (Number)Float.valueOf(colorRGBA.getRed()));
            jsonObject.addProperty("green", (Number)Float.valueOf(colorRGBA.getGreen()));
            jsonObject.addProperty("blue", (Number)Float.valueOf(colorRGBA.getBlue()));
            jsonObject.addProperty("alpha", (Number)Float.valueOf(colorRGBA.getAlpha()));
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /** ORIGINAL: {@code i(Lcom/google/gson/JsonArray;)V} */
    private void loadColorPickerPresets(JsonArray jsonArray) {
        ArrayList<ColorPickerWidget.SavedColor> arrayList = new ArrayList<ColorPickerWidget.SavedColor>();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            float f = jsonObject.get("red").getAsFloat();
            float f2 = jsonObject.get("green").getAsFloat();
            float f3 = jsonObject.get("blue").getAsFloat();
            float f4 = jsonObject.get("alpha").getAsFloat();
            ColorRGBA colorRGBA = new ColorRGBA(f, f2, f3, f4);
            arrayList.add(new ColorPickerWidget.SavedColor(colorRGBA));
        }
        ColorPickerWidget.setSavedColors(arrayList);
    }

    /** ORIGINAL: {@code II(Lcom/google/gson/JsonArray;)V} */
    private void loadPasswords(JsonArray jsonArray) {
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String string = jsonObject.get("nick").getAsString();
            String string2 = jsonObject.get("pass").getAsString();
            RockstarClient.create().getModuleRegistry().getModule(AutoAuth.class).storePassword(string, string2);
        }
    }

    /** ORIGINAL: {@code Ii(Lcom/google/gson/JsonArray;)V} */
    private void loadMacros(JsonArray jsonArray) {
        if (RockstarClient.create().getSettingDataStore() == null) {
            return;
        }
        ArrayList<SettingEntry> arrayList = new ArrayList<SettingEntry>();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int n = jsonObject.get("key").getAsInt();
            String string = jsonObject.get("command").getAsString();
            arrayList.add(new SettingEntry(n, string));
        }
        RockstarClient.create().getSettingDataStore().replaceBindings(arrayList);
    }

    /**
     * ORIGINAL: {@code rockstar/ilIlil/IiIIIiiIi#I ()Lcom/google/gson/JsonObject;}.
     * The theme value object carries no JSON of its own in this tree, so the
     * document owns the mapping.
     */
    private static JsonObject serializeThemeData(ThemeColorSettings themeColorSettings) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("accent", themeColorSettings.getAccentColor().toJson());
        jsonObject.add("background", themeColorSettings.getPanelColor().toJson());
        jsonObject.add("additional", themeColorSettings.getPanelBackgroundColor().toJson());
        jsonObject.add("text", themeColorSettings.getPrimaryTextColor().toJson());
        jsonObject.add("outline", themeColorSettings.getBorderColor().toJson());
        jsonObject.add("flat", themeColorSettings.getDarkBackgroundColor().toJson());
        jsonObject.add("icons", themeColorSettings.getSecondaryTextColor().toJson());
        jsonObject.add("enabledModules", themeColorSettings.getHighlightColor().toJson());
        jsonObject.addProperty("hudRounding", (Number)Float.valueOf(themeColorSettings.getCornerRadius()));
        jsonObject.addProperty("blurOffset", (Number)Float.valueOf(themeColorSettings.getPanelOpacity()));
        jsonObject.addProperty("hudAlpha", (Number)Float.valueOf(themeColorSettings.getOverlayAlphaMinimum()));
        jsonObject.addProperty("disalphaGlass", (Number)Float.valueOf(themeColorSettings.getOverlayAlphaMaximum()));
        jsonObject.addProperty("glassPower", (Number)Float.valueOf(themeColorSettings.getBlurRadius()));
        jsonObject.addProperty("glassStreng", (Number)Float.valueOf(themeColorSettings.getGlassDistortion()));
        jsonObject.addProperty("many", (Number)Float.valueOf(themeColorSettings.getGlassOffset()));
        jsonObject.addProperty("padding", (Number)Float.valueOf(themeColorSettings.getGlassScale()));
        jsonObject.addProperty("splitters", (Number)Float.valueOf(themeColorSettings.getGlassOpacity()));
        jsonObject.addProperty("albomColor", (Number)Float.valueOf(themeColorSettings.getGlassSaturation()));
        return jsonObject;
    }

    /** ORIGINAL: static {@code rockstar/ilIlil/IiIIIiiIi#I (Lcom/google/gson/JsonObject;)Lrockstar/ilIlil/IiIIIiiIi;} */
    private static ThemeColorSettings deserializeThemeData(JsonObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        float f = jsonObject.has("hudRounding") ? jsonObject.get("hudRounding").getAsFloat() : 0.0f;
        float f2 = jsonObject.has("blurOffset") ? jsonObject.get("blurOffset").getAsFloat() : 0.0f;
        float f3 = jsonObject.has("hudAlpha") ? jsonObject.get("hudAlpha").getAsFloat() : 0.0f;
        float f4 = jsonObject.has("disalphaGlass") ? jsonObject.get("disalphaGlass").getAsFloat() : 0.0f;
        float f5 = jsonObject.has("glassPower") ? jsonObject.get("glassPower").getAsFloat() : 0.0f;
        float f6 = jsonObject.has("glassStreng") ? jsonObject.get("glassStreng").getAsFloat() : 0.0f;
        float f7 = jsonObject.has("many") ? jsonObject.get("many").getAsFloat() : 0.0f;
        float f8 = jsonObject.has("padding") ? jsonObject.get("padding").getAsFloat() : 0.0f;
        float f9 = jsonObject.has("splitters") ? jsonObject.get("splitters").getAsFloat() : 0.0f;
        float f10 = jsonObject.has("albomColor") ? jsonObject.get("albomColor").getAsFloat() : 0.0f;
        ColorRGBA colorRGBA = jsonObject.has("accent") ? ColorRGBA.fromJson(jsonObject.getAsJsonObject("accent")) : new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        ColorRGBA colorRGBA2 = jsonObject.has("background") ? ColorRGBA.fromJson(jsonObject.getAsJsonObject("background")) : new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        ColorRGBA colorRGBA3 = jsonObject.has("additional") ? ColorRGBA.fromJson(jsonObject.getAsJsonObject("additional")) : new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        ColorRGBA colorRGBA4 = jsonObject.has("text") ? ColorRGBA.fromJson(jsonObject.getAsJsonObject("text")) : new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f);
        ColorRGBA colorRGBA5 = jsonObject.has("outline") ? ColorRGBA.fromJson(jsonObject.getAsJsonObject("outline")) : new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        ColorRGBA colorRGBA6 = jsonObject.has("flat") ? ColorRGBA.fromJson(jsonObject.getAsJsonObject("flat")) : new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        ColorRGBA colorRGBA7 = jsonObject.has("icons") ? ColorRGBA.fromJson(jsonObject.getAsJsonObject("icons")) : colorRGBA4;
        ColorRGBA colorRGBA8 = jsonObject.has("enabledModules") ? ColorRGBA.fromJson(jsonObject.getAsJsonObject("enabledModules")) : colorRGBA4;
        return new ThemeColorSettings(colorRGBA, colorRGBA2, colorRGBA3, colorRGBA4, colorRGBA5, colorRGBA6, colorRGBA7, colorRGBA8, f, f2, f3, f4, f5, f6, f8, f7, f9, f10);
    }

    @Generated
    public int getHitIslandBestScore() {
        return this.hitIslandBestScore;
    }

    @Generated
    public int getHitIslandGamesPlayed() {
        return this.hitIslandGamesPlayed;
    }

    @Generated
    public boolean isBroken() {
        return this.broken;
    }

    @Generated
    public boolean isSuppressed() {
        return this.suppressed;
    }

    @Generated
    public void setHitIslandBestScore(int n) {
        this.hitIslandBestScore = n;
    }

    @Generated
    public void setHitIslandGamesPlayed(int n) {
        this.hitIslandGamesPlayed = n;
    }

    @Generated
    public void setBroken(boolean bl) {
        this.broken = bl;
    }

    @Generated
    public void setSuppressed(boolean bl) {
        this.suppressed = bl;
    }

    /** ORIGINAL: {@code rockstar/ilIlil/IiIIiIiI$I} - record {@code (cleanedFriends, failed)}. */
    public record LoadResult(boolean cleanedFriends, boolean failed) {
    }
}
