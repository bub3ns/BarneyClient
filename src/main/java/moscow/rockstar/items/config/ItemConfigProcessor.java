/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.authlib.properties.Property
 *  com.mojang.authlib.properties.PropertyMap
 *  lombok.Generated
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.PotionContentsComponent
 *  net.minecraft.NbtCompound
 *  net.minecraft.NbtElement
 *  net.minecraft.Identifier
 *  net.minecraft.RegistryEntry
 *  net.minecraft.Registries
 *  net.minecraft.NbtComponent
 *  net.minecraft.ProfileComponent
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.items.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.component.DataComponentTypes;

public final class ItemConfigProcessor {
    private static final String[] CATEGORY_KEYS = new String[]{"consumable", "talisman", "sphere", "tools", "armor", "potions", "other"};
    private static JsonObject itemConfigJson;

    public static void loadConfiguration() {
        try {
            InputStream inputStream = ItemConfigProcessor.class.getResourceAsStream("/assets/rockstar/donate_items.json");
            if (inputStream == null) {
                return;
            }
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            itemConfigJson = (JsonObject)new Gson().fromJson((Reader)inputStreamReader, JsonObject.class);
            inputStreamReader.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static List<CategoryDefinition> getCategoryDefinitions(ConfigProfile configProfile) {
        if (configProfile == ConfigProfile.ALL_ITEMS) {
            return ItemConfigProcessor.getFallbackDefinitions();
        }
        if (itemConfigJson == null) {
            ItemConfigProcessor.loadConfiguration();
        }
        if (itemConfigJson == null || !itemConfigJson.has(configProfile.getProfileKey())) {
            return List.of();
        }
        ArrayList<CategoryDefinition> arrayList = new ArrayList<CategoryDefinition>();
        JsonObject jsonObject = itemConfigJson.getAsJsonObject(configProfile.getProfileKey());
        for (String string : CATEGORY_KEYS) {
            List<ItemDefinition> list;
            if (!jsonObject.has(string) || (list = ItemConfigProcessor.parseCategoryItems(jsonObject, string, configProfile)).isEmpty()) continue;
            arrayList.add(new CategoryDefinition(string, ItemConfigProcessor.normalizeItemId(string), list));
        }
        return arrayList;
    }

    public static Map<String, String> getItemDefinitionsById(ConfigProfile configProfile) {
        if (itemConfigJson == null) {
            ItemConfigProcessor.loadConfiguration();
        }
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
        if (itemConfigJson == null || !itemConfigJson.has(configProfile.getProfileKey())) {
            return linkedHashMap;
        }
        JsonObject jsonObject = itemConfigJson.getAsJsonObject(configProfile.getProfileKey());
        for (String string : CATEGORY_KEYS) {
            if (!jsonObject.has(string)) continue;
            for (JsonElement jsonElement : jsonObject.getAsJsonArray(string)) {
                JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                if (!jsonObject2.has("name") || !jsonObject2.has("lore")) continue;
                linkedHashMap.put(jsonObject2.get("name").getAsString(), jsonObject2.get("lore").getAsString());
            }
        }
        return linkedHashMap;
    }

    public static List<ItemDefinition> getAllItemDefinitions() {
        ArrayList<ItemDefinition> arrayList = new ArrayList<ItemDefinition>();
        HashSet<String> hashSet = new HashSet<String>();
        for (ConfigProfile configProfile : new ConfigProfile[]{ConfigProfile.FUNTIME}) {
            for (CategoryDefinition categoryDefinition : ItemConfigProcessor.getCategoryDefinitions(configProfile)) {
                for (ItemDefinition itemDefinition3 : categoryDefinition.getItems()) {
                    String string = itemDefinition3.getCustomName();
                    if (string == null || string.isBlank() || !hashSet.add(string.toLowerCase())) continue;
                    arrayList.add(itemDefinition3);
                }
            }
        }
        arrayList.sort((itemDefinition, itemDefinition2) -> itemDefinition.getCustomName().compareToIgnoreCase(itemDefinition2.getCustomName()));
        return arrayList;
    }

    private static List<ItemDefinition> parseCategoryItems(JsonObject jsonObject, String string, ConfigProfile configProfile) {
        ArrayList<ItemDefinition> arrayList = new ArrayList<ItemDefinition>();
        jsonObject.getAsJsonArray(string).forEach(jsonElement -> {
            JsonObject itemObject = jsonElement.getAsJsonObject();
            String string2 = itemObject.get("item").getAsString();
            String string3 = itemObject.has("name") ? itemObject.get("name").getAsString() : null;
            String string4 = itemObject.has("id") ? itemObject.get("id").getAsString() : null;
            String string5 = itemObject.has("nbtKey") ? itemObject.get("nbtKey").getAsString() : null;
            String string6 = itemObject.has("texture") ? itemObject.get("texture").getAsString() : null;
            String string7 = configProfile.getProfileKey() + "_" + string + "_" + string4;
            try {
                Item class_17922;
                Identifier class_29602 = Identifier.tryParse((String)string2);
                if (class_29602 != null && (class_17922 = (Item)Registries.ITEM.get(class_29602)) != Items.AIR) {
                    ItemStack class_17992 = class_17922.getDefaultStack();
                    if (string5 != null) {
                        ItemConfigProcessor.attachDonationMetadata(class_17992, string5, configProfile);
                    }
                    if (string6 != null && !string6.isBlank()) {
                        ItemConfigProcessor.attachTextureMetadata(class_17992, string6);
                    }
                    arrayList.add(new ItemDefinition(class_17992, string3, string7));
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
        return arrayList;
    }

    private static String normalizeItemId(String string) {
        return switch (string) {
            case "consumable" -> "\u0420\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a\u0438";
            case "talisman" -> "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d\u044b";
            case "sphere" -> "\u0421\u0444\u0435\u0440\u044b";
            case "tools" -> "\u0418\u043d\u0441\u0442\u0440\u0443\u043c\u0435\u043d\u0442\u044b";
            case "armor" -> "\u0411\u0440\u043e\u043d\u044f";
            case "potions" -> "\u0417\u0435\u043b\u044c\u044f";
            case "other" -> "\u0414\u0440\u0443\u0433\u043e\u0435";
            default -> string;
        };
    }

    private static List<CategoryDefinition> getFallbackDefinitions() {
        ArrayList<ItemDefinition> arrayList = new ArrayList<ItemDefinition>();
        Registries.ITEM.stream().filter(class_17922 -> class_17922 != Items.AIR).forEach(class_17922 -> {
            if (ItemConfigProcessor.isSpecialItem(class_17922)) {
                ItemConfigProcessor.addPotionVariants(class_17922, arrayList);
            } else {
                ItemConfigProcessor.addBasicItemDefinition(class_17922, arrayList);
            }
        });
        return List.of(new CategoryDefinition("all", "\u0412\u0441\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b", arrayList));
    }

    private static boolean isSpecialItem(Item class_17922) {
        return class_17922 == Items.POTION || class_17922 == Items.SPLASH_POTION || class_17922 == Items.LINGERING_POTION;
    }

    private static void addPotionVariants(Item class_17922, List<ItemDefinition> list) {
        Registries.POTION.streamEntries().forEach(class_68832 -> {
            try {
                ItemStack class_17992 = class_17922.getDefaultStack();
                class_17992.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(class_68832));
                String string = Registries.ITEM.getId(class_17922).toString() + "_" + class_68832.getIdAsString();
                list.add(new ItemDefinition(class_17992, null, string));
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
    }

    private static void addBasicItemDefinition(Item class_17922, List<ItemDefinition> list) {
        try {
            list.add(new ItemDefinition(class_17922.getDefaultStack(), null, Registries.ITEM.getId(class_17922).toString()));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void attachDonationMetadata(ItemStack class_17992, String string, ConfigProfile configProfile) {
        try {
            NbtCompound class_24872 = new NbtCompound();
            if (configProfile == ConfigProfile.FUNTIME) {
                NbtCompound class_24873 = new NbtCompound();
                class_24873.putString("minecraft:don-item", string);
                if (string.startsWith("potion-")) {
                    class_24873.putBoolean("minecraft:is-tshop", true);
                }
                class_24872.put("PublicBukkitValues", (NbtElement)class_24873);
            }
            class_17992.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(class_24872));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void attachTextureMetadata(ItemStack class_17992, String string) {
        try {
            PropertyMap propertyMap = new PropertyMap();
            propertyMap.put("textures", new Property("textures", string));
            class_17992.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.of(UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8))), propertyMap));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Generated
    private ItemConfigProcessor() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static enum ConfigProfile {
        ALL_ITEMS("all", "\u0412\u0441\u0435"),
        FUNTIME("funtime", "Funtime");
        private final String profileKey;
        private final String displayName;

        private ConfigProfile(String string2, String string3) {
            this.profileKey = string2;
            this.displayName = string3;
        }

        @Generated
        public String getProfileKey() {
            return this.profileKey;
        }

        @Generated
        public String getDisplayName() {
            return this.displayName;
        }
}

    public static final class CategoryDefinition {
        private final String type;
        private final String displayName;
        private final List<ItemDefinition> items;

        public CategoryDefinition(String string, String string2, List<ItemDefinition> list) {
            this.type = string;
            this.displayName = string2;
            this.items = list;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "type", "displayName", "items");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "type", "displayName", "items");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "type", "displayName", "items");
        }

        public String getType() {
            return this.type;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public List<ItemDefinition> getItems() {
            return this.items;
        }
    }

    public static final class ItemDefinition {
        private final ItemStack stack;
        private final String customName;
        private final String itemId;

        public ItemDefinition(ItemStack class_17992, String string, String string2) {
            this.stack = class_17992;
            this.customName = string;
            this.itemId = string2;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "stack", "customName", "itemId");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "stack", "customName", "itemId");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "stack", "customName", "itemId");
        }

        public ItemStack getStack() {
            return this.stack;
        }

        public String getCustomName() {
            return this.customName;
        }

        public String getItemId() {
            return this.itemId;
        }
    }
}

