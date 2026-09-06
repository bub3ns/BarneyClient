/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.TranslationStorage
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Language
 *  net.minecraft.NbtCompound
 *  net.minecraft.NbtElement
 *  net.minecraft.Text
 *  net.minecraft.Identifier
 *  net.minecraft.Resource
 *  net.minecraft.ResourceManager
 *  net.minecraft.TextColor
 *  net.minecraft.DynamicRegistryManager
 *  net.minecraft.RegistryWrapper$WrapperLookup
 *  net.minecraft.BundleContentsComponent
 *  net.minecraft.NbtComponent
 *  net.minecraft.ContainerComponent
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.items;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.items.catalog.ItemCatalogType;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.server.ServerDetector;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Language;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.TextColor;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.DataComponentTypes;
import pyrock.utility.render.ColorRGBA;

public final class ItemMetadataUtils
implements ClientAccess {
    private static Language clientItemData;
    private static boolean metadataLoadFailed;

    public static List<ItemStack> getContainerContents(ItemStack class_17992) {
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        ContainerComponent class_92882 = (ContainerComponent)class_17992.get(DataComponentTypes.CONTAINER);
        if (class_92882 == null) {
            BundleContentsComponent class_92762 = (BundleContentsComponent)class_17992.get(DataComponentTypes.BUNDLE_CONTENTS);
            if (class_92762 == null) {
                return arrayList;
            }
            for (ItemStack class_17993 : class_92762.iterate()) {
                arrayList.add(class_17993);
            }
            return arrayList;
        }
        for (ItemStack class_17994 : class_92882.iterateNonEmpty()) {
            arrayList.add(class_17994);
        }
        return arrayList;
    }

    public static NbtCompound getCustomData(ItemStack class_17992) {
        try {
            NbtCompound class_24872;
            NbtCompound class_24873;
            DynamicRegistryManager class_54552 = ItemMetadataUtils.minecraftClient.world.getRegistryManager();
            NbtElement class_25202 = class_17992.toNbtAllowEmpty((RegistryWrapper.WrapperLookup)class_54552);
            if (class_17992.isEmpty()) {
                return null;
            }
            NbtComponent class_92792 = (NbtComponent)class_17992.get(DataComponentTypes.CUSTOM_DATA);
            if (class_92792 != null) {
                return class_92792.copyNbt();
            }
            if (class_25202 instanceof NbtCompound && (class_24873 = (NbtCompound)class_25202).contains("components", 10) && (class_24872 = class_24873.getCompound("components")).contains("minecraft:custom_data", 10)) {
                return class_24872.getCompound("minecraft:custom_data");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    public static boolean hasDonationTagValue(ItemStack class_17992, String string) {
        NbtCompound class_24872;
        NbtCompound class_24873 = ItemMetadataUtils.getCustomData(class_17992);
        if (class_24873 == null) {
            return false;
        }
        if (class_24873.contains("PublicBukkitValues", 10) && (class_24872 = class_24873.getCompound("PublicBukkitValues")).contains("minecraft:don-item", 8)) {
            return class_24872.getString("minecraft:don-item").contains(string);
        }
        if (class_24873.contains("don-item")) {
            return class_24873.getString("don-item").contains(string);
        }
        return false;
    }

    public static String readItemModelJson(String string) {
        ResourceManager class_33002 = minecraftClient.getResourceManager();
        Identifier class_29602 = Identifier.of((String)"minecraft", (String)("models/item/" + string.replace("minecraft:", "") + ".json"));
        Optional optional = class_33002.getResource(class_29602);
        if (optional.isEmpty()) {
            return null;
        }
        try (BufferedReader bufferedReader = ((Resource)optional.get()).getReader()) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        }
        catch (IOException exception) {
            System.err.println("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u0438 \u0441\u0435\u0440\u0432\u0435\u0440\u043d\u043e\u0439 \u043c\u043e\u0434\u0435\u043b\u0438: " + exception.getMessage());
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String readItemModelWithCustomData(ItemStack class_17992) {
        Language class_24772 = ItemMetadataUtils.loadClientItemData();
        if (class_24772 == null) {
            return ItemMetadataUtils.cleanDisplayName(class_17992);
        }
        Language class_24773 = Language.getInstance();
        try {
            Language.setInstance((Language)class_24772);
            String string = ItemMetadataUtils.cleanDisplayName(class_17992);
            return string;
        }
        finally {
            Language.setInstance((Language)class_24773);
        }
    }

    private static Language loadClientItemData() {
        if (clientItemData == null && !metadataLoadFailed) {
            try {
                clientItemData = TranslationStorage.load((ResourceManager)minecraftClient.getResourceManager(), List.of("en_us"/*, "ru_ru"*/), (boolean)false);
            }
            catch (Exception exception) {
                metadataLoadFailed = true;
            }
        }
        return clientItemData;
    }

    public static String cleanDisplayName(ItemStack class_17992) {
        return class_17992.getName().getString().replace("[", "").replace("] ", "").replace("- ", "").replace(" -", "").replace("xxx ", "").replace(" xxx", "").replace("ggg ", "").replace(" ggg", "").replace("gg ", "").replace(" gg", "").replace("123 ", "").replace(" 123", "").replace("\u2605", "");
    }

    public static ColorRGBA getTextColor(Text class_25612) {
        for (Text class_25613 : class_25612.getSiblings()) {
            ColorRGBA colorRGBA = ItemMetadataUtils.findNestedTextColor(class_25613);
            if (colorRGBA == null) continue;
            return colorRGBA.withAlpha(255.0f);
        }
        TextColor class_52512 = class_25612.getStyle().getColor();
        if (class_52512 != null) {
            return ColorRGBA.fromInt(class_52512.getRgb()).withAlpha(255.0f);
        }
        return ColorPalette.getAccentColor();
    }

    private static ColorRGBA findNestedTextColor(Text class_25612) {
        TextColor class_52512 = class_25612.getStyle().getColor();
        if (class_52512 != null) {
            return ColorRGBA.fromInt(class_52512.getRgb());
        }
        for (Text class_25613 : class_25612.getSiblings()) {
            ColorRGBA colorRGBA = ItemMetadataUtils.findNestedTextColor(class_25613);
            if (colorRGBA == null) continue;
            return colorRGBA;
        }
        return null;
    }

    public static boolean hasDonationMetadata(ItemStack class_17992) {
        NbtCompound class_24872 = ItemMetadataUtils.getCustomData(class_17992);
        if (class_24872 == null) {
            return false;
        }
        if (class_24872.contains("PublicBukkitValues", 10)) {
            NbtCompound class_24873 = class_24872.getCompound("PublicBukkitValues");
            return class_24873.contains("minecraft:don-item", 8);
        }
        if (class_24872.contains("sixitem", 8)) {
            return true;
        }
        return class_24872.contains("don-item");
    }

    public static String getDonationIdentifier(ItemStack class_17992) {
        NbtCompound class_24872 = ItemMetadataUtils.getCustomData(class_17992);
        if (class_24872 == null) {
            return "";
        }
        NbtCompound class_24873 = class_24872.getCompound("sphereEffect");
        if (class_24872.contains("PublicBukkitValues", 10)) {
            NbtCompound class_24874 = class_24872.getCompound("PublicBukkitValues");
            if (class_24874.contains("minecraft:don-item", 8)) {
                return class_24874.getString("minecraft:don-item");
            }
            if (class_24874.contains("minecraft:spooky-item", 8)) {
                return class_24874.getString("minecraft:spooky-item");
            }
        }
        if (class_24872.contains("don-item")) {
            return class_24872.getString("don-item");
        }
        if (class_24872.contains("spooky-item")) {
            return class_24872.getString("spooky-item");
        }
        if (ServerDetector.serverAddressContains("holyworld") && class_24872.contains("sphereEffect", 10) && class_17992.getItem() == Items.TOTEM_OF_UNDYING && class_24873.contains("rank")) {
            if (class_24873.getString("rank").equals("ETERNITY")) {
                return class_24873.getString("name");
            }
            return class_24873.getString("rank");
        }
        return "";
    }

    public static ItemCatalogType findCatalogType(ItemStack class_17992) {
        for (ItemCatalogType itemCatalogType : ItemCatalogType.values()) {
            for (String string : itemCatalogType.getAliases()) {
                if (!ItemMetadataUtils.getDonationIdentifier(class_17992).equals(string)) continue;
                return itemCatalogType;
            }
        }
        return null;
    }

    public static int getCatalogPriority(ItemStack class_17992) {
        if (class_17992.hasEnchantments()) {
            for (ItemCatalogType itemCatalogType : ItemCatalogType.values()) {
                for (String string : itemCatalogType.getAliases()) {
                    if (!ItemMetadataUtils.getDonationIdentifier(class_17992).equals(string)) continue;
                    return 12 - itemCatalogType.getCategoryIndex();
                }
            }
            return 0;
        }
        return -1;
    }

    public static int getCatalogSlot(ItemStack class_17992) {
        if (class_17992.hasEnchantments() || ItemMetadataUtils.hasDonationMetadata(class_17992)) {
            for (ItemCatalogType itemCatalogType : ItemCatalogType.values()) {
                for (String string : itemCatalogType.getAliases()) {
                    if (!ItemMetadataUtils.getDonationIdentifier(class_17992).equals(string)) continue;
                    return 15 - itemCatalogType.getItemSlot();
                }
            }
            return 16;
        }
        return 17;
    }

    @Generated
    private ItemMetadataUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
