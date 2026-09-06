/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  net.minecraft.StatusEffect
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.SharedConstants
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.DefaultedList
 *  net.minecraft.Vec3d
 *  net.minecraft.ChatHudLine
 *  net.minecraft.PlayerListEntry
 *  net.minecraft.ServerInfo
 *  net.minecraft.Registries
 */
package moscow.rockstar.items.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.api.settings.SettingEntry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.mixin.accessors.ChatHudAccessor;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.config.ModuleConfigurationStore;
import moscow.rockstar.network.http.HttpClientAdapter;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.render.hand.HandSwingPreset;
import moscow.rockstar.render.hand.HandSwingPresetManager;
import moscow.rockstar.render.hand.SwingPresetFile;
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
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.SharedConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.registry.Registries;

public final class ItemConfigStore {
    private ItemConfigStore() {
    }

    public static JsonObject collectPlayerSnapshot() {
        JsonObject jsonObject = new JsonObject();
        if (ClientAccess.minecraftClient.player == null || ClientAccess.minecraftClient.world == null) {
            jsonObject.addProperty("inGame", Boolean.valueOf(false));
            jsonObject.addProperty("account", ClientAccess.minecraftClient.getSession() == null ? "?" : ClientAccess.minecraftClient.getSession().getUsername());
            return jsonObject;
        }
        jsonObject.addProperty("inGame", Boolean.valueOf(true));
        jsonObject.addProperty("name", ClientAccess.minecraftClient.player.getName().getString());
        jsonObject.addProperty("uuid", ClientAccess.minecraftClient.player.getUuidAsString());
        Vec3d VanillaChestLootTableGenerator = ClientAccess.minecraftClient.player.getPos();
        jsonObject.add("pos", (JsonElement)ItemConfigStore.serializePosition(VanillaChestLootTableGenerator));
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("x", (Number)ClientAccess.minecraftClient.player.getBlockX());
        jsonObject2.addProperty("y", (Number)ClientAccess.minecraftClient.player.getBlockY());
        jsonObject2.addProperty("z", (Number)ClientAccess.minecraftClient.player.getBlockZ());
        jsonObject.add("blockPos", (JsonElement)jsonObject2);
        jsonObject.addProperty("yaw", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(ClientAccess.minecraftClient.player.getYaw(), 2)));
        jsonObject.addProperty("pitch", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(ClientAccess.minecraftClient.player.getPitch(), 2)));
        jsonObject.addProperty("facing", ClientAccess.minecraftClient.player.getHorizontalFacing().asString());
        jsonObject.add("velocity", (JsonElement)ItemConfigStore.serializePosition(ClientAccess.minecraftClient.player.getVelocity()));
        jsonObject.addProperty("health", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(ClientAccess.minecraftClient.player.getHealth(), 2)));
        jsonObject.addProperty("maxHealth", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(ClientAccess.minecraftClient.player.getMaxHealth(), 2)));
        jsonObject.addProperty("absorption", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(ClientAccess.minecraftClient.player.getAbsorptionAmount(), 2)));
        jsonObject.addProperty("armor", (Number)ClientAccess.minecraftClient.player.getArmor());
        jsonObject.addProperty("food", (Number)ClientAccess.minecraftClient.player.getHungerManager().getFoodLevel());
        jsonObject.addProperty("saturation", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(ClientAccess.minecraftClient.player.getHungerManager().getSaturationLevel(), 2)));
        jsonObject.addProperty("air", (Number)ClientAccess.minecraftClient.player.getAir());
        jsonObject.addProperty("xpLevel", (Number)ClientAccess.minecraftClient.player.experienceLevel);
        jsonObject.addProperty("gameMode", ClientAccess.minecraftClient.interactionManager == null ? "unknown" : ClientAccess.minecraftClient.interactionManager.getCurrentGameMode().getName());
        jsonObject.addProperty("onGround", Boolean.valueOf(ClientAccess.minecraftClient.player.isOnGround()));
        jsonObject.addProperty("sneaking", Boolean.valueOf(ClientAccess.minecraftClient.player.isSneaking()));
        jsonObject.addProperty("sprinting", Boolean.valueOf(ClientAccess.minecraftClient.player.isSprinting()));
        jsonObject.addProperty("inWater", Boolean.valueOf(ClientAccess.minecraftClient.player.isTouchingWater()));
        jsonObject.addProperty("flying", Boolean.valueOf(ClientAccess.minecraftClient.player.getAbilities().flying));
        jsonObject.addProperty("alive", Boolean.valueOf(ClientAccess.minecraftClient.player.isAlive()));
        jsonObject.add("hands", (JsonElement)ItemConfigStore.serializeHeldItems());
        jsonObject.add("armorItems", (JsonElement)ItemConfigStore.serializeArmorItems());
        jsonObject.add("effects", (JsonElement)ItemConfigStore.serializeStatusEffects(ClientAccess.minecraftClient.player.getStatusEffects()));
        LivingEntity class_13092 = RockstarClient.create().getFriendManager().getTargetLivingEntity();
        if (class_13092 != null) {
            JsonObject jsonObject3 = new JsonObject();
            jsonObject3.addProperty("name", class_13092.getName().getString());
            jsonObject3.addProperty("health", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(class_13092.getHealth(), 2)));
            jsonObject3.addProperty("distance", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(ClientAccess.minecraftClient.player.distanceTo((Entity)class_13092), 2)));
            jsonObject.add("combatTarget", (JsonElement)jsonObject3);
        }
        return jsonObject;
    }

    public static JsonObject collectWorldSnapshot() {
        JsonObject jsonObject = new JsonObject();
        if (ClientAccess.minecraftClient.world == null) {
            jsonObject.addProperty("loaded", Boolean.valueOf(false));
            return jsonObject;
        }
        jsonObject.addProperty("loaded", Boolean.valueOf(true));
        jsonObject.addProperty("dimension", ClientAccess.minecraftClient.world.getRegistryKey().getValue().toString());
        jsonObject.addProperty("time", (Number)(ClientAccess.minecraftClient.world.getTimeOfDay() % 24000L));
        jsonObject.addProperty("day", (Number)(ClientAccess.minecraftClient.world.getTimeOfDay() / 24000L));
        jsonObject.addProperty("raining", Boolean.valueOf(ClientAccess.minecraftClient.world.isRaining()));
        jsonObject.addProperty("thundering", Boolean.valueOf(ClientAccess.minecraftClient.world.isThundering()));
        jsonObject.addProperty("difficulty", ClientAccess.minecraftClient.world.getDifficulty().getName());
        jsonObject.addProperty("playersAround", (Number)ClientAccess.minecraftClient.world.getPlayers().size());
        if (ClientAccess.minecraftClient.player != null) {
            jsonObject.addProperty("biome", ClientAccess.minecraftClient.world.getBiome(ClientAccess.minecraftClient.player.getBlockPos()).getIdAsString());
            jsonObject.addProperty("light", (Number)ClientAccess.minecraftClient.world.getLightLevel(ClientAccess.minecraftClient.player.getBlockPos()));
        }
        return jsonObject;
    }

    public static JsonObject collectServerSnapshot() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("connected", Boolean.valueOf(ClientAccess.minecraftClient.getNetworkHandler() != null));
        jsonObject.addProperty("singleplayer", Boolean.valueOf(ClientAccess.minecraftClient.isInSingleplayer()));
        ServerInfo class_6422 = ClientAccess.minecraftClient.getCurrentServerEntry();
        if (class_6422 != null) {
            jsonObject.addProperty("address", class_6422.address);
            jsonObject.addProperty("name", class_6422.name);
            jsonObject.addProperty("ping", (Number)class_6422.ping);
            if (class_6422.version != null) {
                jsonObject.addProperty("version", class_6422.version.getString());
            }
        }
        if (ClientAccess.minecraftClient.getNetworkHandler() != null) {
            PlayerListEntry ServerSamplerSource;
            Collection collection = ClientAccess.minecraftClient.getNetworkHandler().getPlayerList();
            jsonObject.addProperty("online", (Number)collection.size());
            PlayerListEntry CpuUsageFetcher = ServerSamplerSource = ClientAccess.minecraftClient.player == null ? null : ClientAccess.minecraftClient.getNetworkHandler().getPlayerListEntry(ClientAccess.minecraftClient.player.getUuid());
            if (ServerSamplerSource != null) {
                jsonObject.addProperty("ownPing", (Number)ServerSamplerSource.getLatency());
            }
        }
        jsonObject.addProperty("tps", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(RockstarClient.create().getServerTickRateTracker().getTicksPerSecond(), 2)));
        return jsonObject;
    }

    public static JsonObject collectClientSnapshot() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("client", "Barney 2.1");
        jsonObject.addProperty("minecraft", SharedConstants.getGameVersion().getName());
        jsonObject.addProperty("account", ClientAccess.minecraftClient.getSession() == null ? "?" : ClientAccess.minecraftClient.getSession().getUsername());
        jsonObject.addProperty("fps", (Number)ClientAccess.minecraftClient.getCurrentFps());
        jsonObject.addProperty("language", Localization.getLanguage().name());
        jsonObject.addProperty("theme", RockstarClient.create().getColorTheme().name());
        jsonObject.addProperty("config", "local");
        jsonObject.addProperty("commandPrefix", RockstarClient.create().getNavigationCommandService().getCommandPrefix());
        jsonObject.addProperty("swingPreset", RockstarClient.create().getHandSwingPresetManager().getSelectedPresetName());
        List<ModuleContract> list = RockstarClient.create().getModuleRegistry().getModules();
        JsonArray jsonArray = new JsonArray();
        for (ModuleContract object : list) {
            if (!object.isEnabled()) continue;
            jsonArray.add(object.getName());
        }
        jsonObject.addProperty("modulesTotal", (Number)list.size());
        jsonObject.add("modulesEnabled", (JsonElement)jsonArray);
        JsonArray jsonArray2 = new JsonArray();
        for (ScriptDescriptor scriptDescriptor : RockstarClient.create().getScriptRegistry().getScripts()) {
            if (!scriptDescriptor.isLoaded()) continue;
            jsonArray2.add(scriptDescriptor.getScriptName());
        }
        jsonObject.add("scriptsLoaded", (JsonElement)jsonArray2);
        return jsonObject;
    }

    public static JsonObject collectScreenSnapshot() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("framebufferWidth", (Number)ClientAccess.minecraftClient.getWindow().getFramebufferWidth());
        jsonObject.addProperty("framebufferHeight", (Number)ClientAccess.minecraftClient.getWindow().getFramebufferHeight());
        jsonObject.addProperty("guiWidth", (Number)ClientAccess.minecraftClient.getWindow().getScaledWidth());
        jsonObject.addProperty("guiHeight", (Number)ClientAccess.minecraftClient.getWindow().getScaledHeight());
        jsonObject.addProperty("guiScale", (Number)ClientAccess.minecraftClient.getWindow().getScaleFactor());
        jsonObject.addProperty("cursorLocked", Boolean.valueOf(ClientAccess.minecraftClient.mouse.isCursorLocked()));
        jsonObject.addProperty("hudHidden", Boolean.valueOf(ClientAccess.minecraftClient.options != null && ClientAccess.minecraftClient.options.hudHidden));
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("x", (Number)Float.valueOf(ItemConfigStore.roundToDecimals((float)ClientAccess.minecraftClient.mouse.getX(), 1)));
        jsonObject2.addProperty("y", (Number)Float.valueOf(ItemConfigStore.roundToDecimals((float)ClientAccess.minecraftClient.mouse.getY(), 1)));
        jsonObject.add("cursor", (JsonElement)jsonObject2);
        if (ClientAccess.minecraftClient.currentScreen == null) {
            jsonObject.add("screen", (JsonElement)JsonNull.INSTANCE);
            jsonObject.add("screenType", (JsonElement)JsonNull.INSTANCE);
        } else {
            jsonObject.addProperty("screen", ClientAccess.minecraftClient.currentScreen.getTitle() == null ? "" : ClientAccess.minecraftClient.currentScreen.getTitle().getString());
            jsonObject.addProperty("screenType", ClientAccess.minecraftClient.currentScreen.getClass().getSimpleName());
        }
        return jsonObject;
    }

    public static JsonArray collectNearbyEntities(double d, int n, String string) {
        JsonArray jsonArray = new JsonArray();
        if (ClientAccess.minecraftClient.world == null || ClientAccess.minecraftClient.player == null) {
            return jsonArray;
        }
        ArrayList<Entity> arrayList = new ArrayList<Entity>();
        for (Entity class_12974 : ClientAccess.minecraftClient.world.getEntities()) {
            boolean bl;
            if (class_12974 == ClientAccess.minecraftClient.player || class_12974.isRemoved() || (double)class_12974.distanceTo((Entity)ClientAccess.minecraftClient.player) > d) continue;
            if (!(bl = (switch (string) {
                case "players" -> class_12974 instanceof PlayerEntity;
                case "living" -> class_12974 instanceof LivingEntity;
                default -> true;
            }))) continue;
            arrayList.add(class_12974);
        }
        arrayList.sort((class_12972, class_12973) -> Float.compare(class_12972.distanceTo((Entity)ClientAccess.minecraftClient.player), class_12973.distanceTo((Entity)ClientAccess.minecraftClient.player)));
        for (Entity class_12974 : arrayList.subList(0, Math.min(n, arrayList.size()))) {
            String string2;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", class_12974.getName().getString());
            jsonObject.addProperty("type", Registries.ENTITY_TYPE.getId(class_12974.getType()).toString());
            jsonObject.addProperty("distance", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(class_12974.distanceTo((Entity)ClientAccess.minecraftClient.player), 2)));
            jsonObject.add("pos", (JsonElement)ItemConfigStore.serializePosition(class_12974.getPos()));
            if (class_12974 instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)class_12974;
                jsonObject.addProperty("health", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(livingEntity.getHealth(), 1)));
                jsonObject.addProperty("maxHealth", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(livingEntity.getMaxHealth(), 1)));
            }
            if (class_12974 instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)class_12974;
                jsonObject.addProperty("player", Boolean.valueOf(true));
                jsonObject.addProperty("friend", Boolean.valueOf(RockstarClient.create().getFriendListManager().containsFriend(player.getName().getString())));
                jsonObject.add("hand", (JsonElement)ItemConfigStore.serializeItemStack(player.getMainHandStack()));
            }
            jsonArray.add((JsonElement)jsonObject);
        }
        return jsonArray;
    }

    public static JsonArray collectRecentChatMessages(int n) {
        JsonArray jsonArray = new JsonArray();
        if (ClientAccess.minecraftClient.inGameHud == null) {
            return jsonArray;
        }
        List<ChatHudLine> list = ((ChatHudAccessor)ClientAccess.minecraftClient.inGameHud.getChatHud()).getMessages();
        int n2 = Math.max(0, list.size() - n);
        for (int i = list.size() - 1; i >= n2; --i) {
            jsonArray.add(list.get(i).content().getString());
        }
        return jsonArray;
    }

    public static JsonArray collectModuleSummaries(String string, boolean bl, String string2, boolean bl2) {
        JsonArray jsonArray = new JsonArray();
        for (ModuleContract moduleContract : RockstarClient.create().getModuleRegistry().getModules()) {
            if (!bl2 && (moduleContract.isVisible() || !moduleContract.isAvailable()) || bl && !moduleContract.isEnabled() || string != null && !string.isBlank() && moduleContract.getCategory() != ModuleCategory.fromName(string) || string2 != null && !string2.isBlank() && !moduleContract.getName().toLowerCase(Locale.ROOT).contains(string2.toLowerCase(Locale.ROOT))) continue;
            jsonArray.add((JsonElement)ItemConfigStore.serializeModule(moduleContract, false));
        }
        return jsonArray;
    }

    public static JsonObject serializeModule(ModuleContract moduleContract, boolean bl) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", moduleContract.getName());
        jsonObject.addProperty("category", moduleContract.getCategory().name());
        jsonObject.addProperty("enabled", Boolean.valueOf(moduleContract.isEnabled()));
        jsonObject.addProperty("hidden", Boolean.valueOf(moduleContract.isVisible()));
        jsonObject.addProperty("key", (Number)moduleContract.getKeyBind());
        jsonObject.addProperty("keyName", moduleContract.getKeyBind() <= 0 ? null : moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(moduleContract.getKeyBind()));
        jsonObject.addProperty("description", moduleContract.getDescription());
        if (bl) {
            JsonArray jsonArray = new JsonArray();
            for (Setting setting : moduleContract.getSettings()) {
                jsonArray.add((JsonElement)ItemConfigStore.serializeSetting(setting));
            }
            jsonObject.add("settings", (JsonElement)jsonArray);
        } else {
            jsonObject.addProperty("settings", (Number)moduleContract.getSettings().size());
        }
        return jsonObject;
    }

    public static JsonObject serializeSetting(Setting setting) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", setting.getName());
        jsonObject.addProperty("label", Localization.translate(setting.getName()));
        jsonObject.addProperty("type", ItemConfigStore.getSettingType(setting));
        jsonObject.addProperty("visible", Boolean.valueOf(setting.hasValidSettingValue()));
        if (setting instanceof BooleanSetting) {
            BooleanSetting booleanSetting = (BooleanSetting)setting;
            jsonObject.addProperty("value", Boolean.valueOf(booleanSetting.isEnabled()));
        } else if (setting instanceof NumberSetting) {
            NumberSetting numberSetting = (NumberSetting)setting;
            jsonObject.addProperty("value", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(numberSetting.getValue(), 3)));
            jsonObject.addProperty("min", (Number)Float.valueOf(numberSetting.getMinValue()));
            jsonObject.addProperty("max", (Number)Float.valueOf(numberSetting.getMaxValue()));
            jsonObject.addProperty("step", (Number)Float.valueOf(numberSetting.getStep()));
        } else if (setting instanceof RangeSetting) {
            RangeSetting rangeSetting = (RangeSetting)setting;
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("first", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(rangeSetting.getFirstValue(), 3)));
            jsonObject2.addProperty("second", (Number)Float.valueOf(ItemConfigStore.roundToDecimals(rangeSetting.getSecondValue(), 3)));
            jsonObject.add("value", (JsonElement)jsonObject2);
            jsonObject.addProperty("min", (Number)Float.valueOf(rangeSetting.getMinimum()));
            jsonObject.addProperty("max", (Number)Float.valueOf(rangeSetting.getMaximum()));
            jsonObject.addProperty("step", (Number)Float.valueOf(rangeSetting.getStep()));
        } else if (setting instanceof ModeSetting) {
            ModeSetting modeSetting = (ModeSetting)setting;
            jsonObject.addProperty("value", modeSetting.getSelectedOption() == null ? null : modeSetting.getSelectedOption().getName());
            jsonObject.addProperty("valueLabel", modeSetting.getSelectedOption() == null ? null : Localization.translate(modeSetting.getSelectedOption().getName()));
            jsonObject.add("options", (JsonElement)ItemConfigStore.getModeOptionNames(modeSetting));
            jsonObject.add("optionLabels", (JsonElement)ItemConfigStore.translateOptionLabels(ItemConfigStore.getModeOptionNames(modeSetting)));
        } else if (setting instanceof MultiBooleanSetting) {
            MultiBooleanSetting multiBooleanSetting = (MultiBooleanSetting)setting;
            JsonArray jsonArray = new JsonArray();
            for (MultiBooleanSetting.Option option : multiBooleanSetting.getOptions()) {
                if (!option.isSelected()) continue;
                jsonArray.add(option.getName());
            }
            jsonObject.add("value", (JsonElement)jsonArray);
            jsonObject.add("options", (JsonElement)ItemConfigStore.getMultiBooleanOptionNames(multiBooleanSetting));
            jsonObject.add("optionLabels", (JsonElement)ItemConfigStore.translateOptionLabels(ItemConfigStore.getMultiBooleanOptionNames(multiBooleanSetting)));
        } else if (setting instanceof ColorSetting) {
            ColorSetting colorSetting = (ColorSetting)setting;
            jsonObject.addProperty("value", colorSetting.getColor().toHex());
        } else if (setting instanceof IntegerSetting) {
            IntegerSetting integerSetting = (IntegerSetting)setting;
            jsonObject.addProperty("value", (Number)integerSetting.getValue());
            jsonObject.addProperty("keyName", integerSetting.getValue() <= 0 ? null : moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(integerSetting.getValue()));
        } else if (setting instanceof StringSetting) {
            StringSetting stringSetting = (StringSetting)setting;
            jsonObject.addProperty("value", stringSetting.getValue());
        } else if (setting instanceof ActionSetting) {
            jsonObject.addProperty("value", "\u043a\u043d\u043e\u043f\u043a\u0430: setting_set \u0441\u043e \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435\u043c \"click\"");
        } else {
            jsonObject.add("raw", setting.serialize());
        }
        return jsonObject;
    }

    public static String getSettingType(Setting setting) {
        if (setting instanceof BooleanSetting) {
            return "boolean";
        }
        if (setting instanceof NumberSetting) {
            return "slider";
        }
        if (setting instanceof RangeSetting) {
            return "range";
        }
        if (setting instanceof ModeSetting) {
            return "mode";
        }
        if (setting instanceof MultiBooleanSetting) {
            return "select";
        }
        if (setting instanceof ColorSetting) {
            return "color";
        }
        if (setting instanceof IntegerSetting) {
            return "bind";
        }
        if (setting instanceof StringSetting) {
            return "text";
        }
        if (setting instanceof ActionSetting) {
            return "button";
        }
        return "raw";
    }

    public static JsonObject collectKeyBindingsSnapshot() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (ModuleContract object : RockstarClient.create().getModuleRegistry().getModules()) {
            if (object.getKeyBind() <= 0) continue;
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("module", object.getName());
            jsonObject2.addProperty("key", (Number)object.getKeyBind());
            jsonObject2.addProperty("keyName", moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(object.getKeyBind()));
            jsonObject2.addProperty("enabled", Boolean.valueOf(object.isEnabled()));
            jsonArray.add((JsonElement)jsonObject2);
        }
        jsonObject.add("modules", (JsonElement)jsonArray);
        JsonArray jsonArray2 = new JsonArray();
        for (ModuleContract moduleContract : RockstarClient.create().getModuleRegistry().getModules()) {
            for (Setting setting : moduleContract.getSettings()) {
                IntegerSetting integerSetting;
                if (!(setting instanceof IntegerSetting) || (integerSetting = (IntegerSetting)setting).getValue() <= 0) continue;
                JsonObject jsonObject3 = new JsonObject();
                jsonObject3.addProperty("module", moduleContract.getName());
                jsonObject3.addProperty("setting", setting.getName());
                jsonObject3.addProperty("key", (Number)integerSetting.getValue());
                jsonObject3.addProperty("keyName", moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(integerSetting.getValue()));
                jsonArray2.add((JsonElement)jsonObject3);
            }
        }
        jsonObject.add("settings", (JsonElement)jsonArray2);
        jsonObject.add("macros", (JsonElement)ItemConfigStore.collectCommandBindings());
        return jsonObject;
    }

    public static JsonArray collectCommandBindings() {
        JsonArray jsonArray = new JsonArray();
        for (SettingEntry settingEntry : RockstarClient.create().getSettingDataStore().getBindings()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("command", settingEntry.getCommand());
            jsonObject.addProperty("key", (Number)settingEntry.getKeyCode());
            jsonObject.addProperty("keyName", moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(settingEntry.getKeyCode()));
            jsonArray.add((JsonElement)jsonObject);
        }
        return jsonArray;
    }

    public static JsonObject collectConfigurationSnapshot() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("active", "local");
        jsonObject.addProperty("autoSave", Boolean.valueOf(ModuleConfigurationStore.isAutoSaveEnabled()));
        JsonArray jsonArray = new JsonArray();
        JsonObject localConfig = new JsonObject();
        localConfig.addProperty("id", "local");
        localConfig.addProperty("name", "Local");
        localConfig.addProperty("active", true);
        jsonArray.add(localConfig);
        jsonObject.add("configs", (JsonElement)jsonArray);
        return jsonObject;
    }

    public static JsonArray collectScriptSummaries() {
        JsonArray jsonArray = new JsonArray();
        for (ScriptDescriptor scriptDescriptor : RockstarClient.create().getScriptRegistry().getScripts()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", scriptDescriptor.getScriptName());
            jsonObject.addProperty("loaded", Boolean.valueOf(scriptDescriptor.isLoaded()));
            jsonObject.addProperty("protected", Boolean.valueOf(scriptDescriptor.getScriptFile() == null));
            if (scriptDescriptor.getLoadError() != null) {
                jsonObject.addProperty("error", scriptDescriptor.getLoadError());
            }
            if (scriptDescriptor.getScriptFile() != null) {
                jsonObject.addProperty("path", scriptDescriptor.getScriptFile().getAbsolutePath());
            }
            JsonArray jsonArray2 = new JsonArray();
            scriptDescriptor.getRegisteredModules().forEach(scriptModule -> jsonArray2.add(scriptModule.getName()));
            jsonObject.add("modules", (JsonElement)jsonArray2);
            jsonArray.add((JsonElement)jsonObject);
        }
        return jsonArray;
    }

    public static JsonObject collectPresetSnapshot() {
        JsonObject jsonObject;
        HandSwingPresetManager handSwingPresetManager = RockstarClient.create().getHandSwingPresetManager();
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("current", handSwingPresetManager.getSelectedPresetName());
        JsonArray jsonArray = new JsonArray();
        for (HandSwingPreset object : handSwingPresetManager.getPresets()) {
            jsonObject = new JsonObject();
            jsonObject.addProperty("name", object.getName());
            jsonObject.addProperty("label", Localization.translate(object.getName()));
            jsonObject.addProperty("source", "builtin");
            jsonArray.add((JsonElement)jsonObject);
        }
        for (SwingPresetFile preset : RockstarClient.create().getSwingPresetFileManager().getPresets()) {
            jsonObject = new JsonObject();
            jsonObject.addProperty("name", preset.getName());
            jsonObject.addProperty("label", preset.getName());
            jsonObject.addProperty("source", "file");
            jsonArray.add((JsonElement)jsonObject);
        }
        jsonObject2.add("presets", (JsonElement)jsonArray);
        jsonObject2.add("shared", (JsonElement)ItemConfigStore.serializeSettings(handSwingPresetManager.getSettings().getSettings()));
        jsonObject2.add("start", (JsonElement)ItemConfigStore.serializeSettings(handSwingPresetManager.getInitialSwing().getSettings()));
        jsonObject2.add("end", (JsonElement)ItemConfigStore.serializeSettings(handSwingPresetManager.getFinalSwing().getSettings()));
        return jsonObject2;
    }

    public static JsonArray serializeSettings(List<Setting> list) {
        JsonArray jsonArray = new JsonArray();
        for (Setting setting : list) {
            jsonArray.add((JsonElement)ItemConfigStore.serializeSetting(setting));
        }
        return jsonArray;
    }

    private static JsonArray translateOptionLabels(JsonArray jsonArray) {
        JsonArray jsonArray2 = new JsonArray();
        jsonArray.forEach(jsonElement -> jsonArray2.add(Localization.translate(jsonElement.getAsString())));
        return jsonArray2;
    }

    private static JsonArray getModeOptionNames(ModeSetting modeSetting) {
        JsonArray jsonArray = new JsonArray();
        for (ModeSetting.Option option : modeSetting.getOptions()) {
            jsonArray.add(option.getName());
        }
        return jsonArray;
    }

    private static JsonArray getMultiBooleanOptionNames(MultiBooleanSetting multiBooleanSetting) {
        JsonArray jsonArray = new JsonArray();
        for (MultiBooleanSetting.Option option : multiBooleanSetting.getOptions()) {
            jsonArray.add(option.getName());
        }
        return jsonArray;
    }

    private static JsonObject serializeHeldItems() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("main", (JsonElement)ItemConfigStore.serializeItemStack(ClientAccess.minecraftClient.player.getMainHandStack()));
        jsonObject.add("off", (JsonElement)ItemConfigStore.serializeItemStack(ClientAccess.minecraftClient.player.getOffHandStack()));
        jsonObject.addProperty("selectedSlot", (Number)ClientAccess.minecraftClient.player.getInventory().selectedSlot);
        return jsonObject;
    }

    private static JsonArray serializeArmorItems() {
        JsonArray jsonArray = new JsonArray();
        for (ItemStack class_17992 : ClientAccess.minecraftClient.player.getInventory().armor) {
            jsonArray.add((JsonElement)ItemConfigStore.serializeItemStack(class_17992));
        }
        return jsonArray;
    }

    public static JsonArray serializeInventoryItems() {
        JsonArray jsonArray = new JsonArray();
        if (ClientAccess.minecraftClient.player == null) {
            return jsonArray;
        }
        DefaultedList class_23712 = ClientAccess.minecraftClient.player.getInventory().main;
        for (int i = 0; i < class_23712.size(); ++i) {
            ItemStack class_17992 = (ItemStack)class_23712.get(i);
            if (class_17992.isEmpty()) continue;
            JsonObject jsonObject = ItemConfigStore.serializeItemStack(class_17992);
            jsonObject.addProperty("slot", (Number)i);
            jsonObject.addProperty("hotbar", Boolean.valueOf(i < 9));
            jsonArray.add((JsonElement)jsonObject);
        }
        return jsonArray;
    }

    private static JsonObject serializeItemStack(ItemStack class_17992) {
        JsonObject jsonObject = new JsonObject();
        if (class_17992 == null || class_17992.isEmpty()) {
            jsonObject.addProperty("id", "minecraft:air");
            jsonObject.addProperty("count", (Number)0);
            return jsonObject;
        }
        jsonObject.addProperty("id", Registries.ITEM.getId(class_17992.getItem()).toString());
        jsonObject.addProperty("name", class_17992.getName().getString());
        jsonObject.addProperty("count", (Number)class_17992.getCount());
        if (class_17992.isDamageable()) {
            jsonObject.addProperty("durability", (Number)(class_17992.getMaxDamage() - class_17992.getDamage()));
            jsonObject.addProperty("maxDurability", (Number)class_17992.getMaxDamage());
        }
        return jsonObject;
    }

    private static JsonArray serializeStatusEffects(Collection<StatusEffectInstance> collection) {
        JsonArray jsonArray = new JsonArray();
        for (StatusEffectInstance class_12932 : collection) {
            JsonObject jsonObject = new JsonObject();
            StatusEffect statusEffect = class_12932.getEffectType().value();
            jsonObject.addProperty("name", statusEffect.getName().getString());
            jsonObject.addProperty("id", String.valueOf(Registries.STATUS_EFFECT.getId(statusEffect)));
            jsonObject.addProperty("amplifier", (Number)(class_12932.getAmplifier() + 1));
            jsonObject.addProperty("ticks", (Number)(class_12932.isInfinite() ? -1 : class_12932.getDuration()));
            jsonArray.add((JsonElement)jsonObject);
        }
        return jsonArray;
    }

    private static JsonObject serializePosition(Vec3d VanillaChestLootTableGenerator) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", (Number)Float.valueOf(ItemConfigStore.roundToDecimals((float)VanillaChestLootTableGenerator.x, 3)));
        jsonObject.addProperty("y", (Number)Float.valueOf(ItemConfigStore.roundToDecimals((float)VanillaChestLootTableGenerator.y, 3)));
        jsonObject.addProperty("z", (Number)Float.valueOf(ItemConfigStore.roundToDecimals((float)VanillaChestLootTableGenerator.z, 3)));
        return jsonObject;
    }

    private static float roundToDecimals(float f, int n) {
        float f2 = (float)Math.pow(10.0, n);
        return (float)Math.round(f * f2) / f2;
    }

    static JsonElement wrapJsonProperty(String string, JsonElement jsonElement) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(string, jsonElement);
        return jsonObject;
    }
}
