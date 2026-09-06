/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  lombok.Generated
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.NbtCompound
 *  net.minecraft.NbtList
 *  net.minecraft.NbtElement
 *  net.minecraft.Text
 *  net.minecraft.RegistryWrapper$WrapperLookup
 *  net.minecraft.LoreComponent
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.items.ItemMetadataUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.DataComponentTypes;
import pyrock.utility.render.ColorRGBA;

public final class DonorItemParser
implements ClientAccess {
    static final Set<String> KNOWN_DONOR_NAMES = Set.of("Eternity", "Infinity", "Stinger", "Immortal", "Armortality", "Flash", "Cerber");
    private static final Map<String, String> DONOR_NAME_ALIASES = Map.ofEntries(Map.entry("eternity", "Eternity"), Map.entry("\u044d\u0442\u0435\u0440\u043d\u0438\u0442\u0438", "Eternity"), Map.entry("infinity", "Infinity"), Map.entry("\u0438\u043d\u0444\u0438\u043d\u0438\u0442\u0438", "Infinity"), Map.entry("stinger", "Stinger"), Map.entry("\u0441\u0442\u0438\u043d\u0433\u0435\u0440", "Stinger"), Map.entry("immortal", "Immortal"), Map.entry("\u0438\u043c\u043c\u043e\u0440\u0442\u0430\u043b\u0438\u0442\u0438", "Immortal"), Map.entry("\u0438\u043c\u043c\u043e\u0440\u0442\u0430\u043b", "Immortal"), Map.entry("armortality", "Armortality"), Map.entry("\u0430\u0440\u043c\u043e\u0440\u0442\u0430\u043b\u0438\u0442\u0438", "Armortality"), Map.entry("flash", "Flash"), Map.entry("\u0444\u043b\u0435\u0448", "Flash"), Map.entry("cerber", "Cerber"), Map.entry("\u0446\u0435\u0440\u0431\u0435\u0440", "Cerber"));
    private static final Map<String, String> TALISMAN_DESCRIPTION_ALIASES = Map.ofEntries(Map.entry("\u0414\u0443\u0445 \u0410\u0440\u0435\u0441\u0430 \u043f\u044b\u043b\u0430\u0435\u0442", "\u0410\u0440\u0435\u0441\u0430"), Map.entry("\u0416\u0438\u0432\u0443\u0447\u0435\u0441\u0442\u044c \u0442\u0435\u043c\u043d\u044b\u0445 \u0433\u043b\u0443\u0431\u0438\u043d", "\u0413\u0438\u0434\u0440\u044b"), Map.entry("\u0412\u0435\u0447\u043d\u0430\u044f \u043c\u0435\u0440\u0437\u043b\u043e\u0442\u0430 \u0441\u043a\u043e\u0432\u044b\u0432\u0430\u0435\u0442", "\u041c\u043e\u0440\u043e\u0437\u0430"), Map.entry("\u0428\u0451\u043f\u043e\u0442 \u0421\u0430\u0442\u0438\u0440\u0430 \u0437\u0432\u0443\u0447\u0438\u0442", "\u0421\u0430\u0442\u0438\u0440\u0430"), Map.entry("\u041c\u043e\u0449\u044c \u0422\u0438\u0442\u0430\u043d\u043e\u0432 \u043a\u0440\u0435\u043f\u043a\u0430", "\u0422\u0438\u0442\u0430\u043d\u0430"), Map.entry("\u0425\u0430\u043e\u0441 \u0438\u0441\u043a\u0430\u0436\u0430\u0435\u0442 \u0440\u0435\u0430\u043b\u044c\u043d\u043e\u0441\u0442\u044c", "\u0425\u0430\u043e\u0441\u0430"), Map.entry("\u0425\u043e\u043b\u043e\u0434 \u042d\u0440\u0438\u0434\u044b \u0432\u0435\u0447\u0435\u043d", "\u042d\u0440\u0438\u0434\u0430"), Map.entry("\u0417\u0432\u0435\u0440\u0438\u043d\u0430\u044f \u0434\u0438\u043a\u0430\u044f \u043c\u043e\u0449\u044c", "\u0411\u0435\u0441\u0442\u0438\u0438"), Map.entry("\u0425\u0440\u0430\u043d\u0438\u0442 \u0432\u043e\u043b\u044e \u0418\u043a\u0430\u0440\u0430", "\u0418\u043a\u0430\u0440\u0430"));
    private static final Map<String, String> TALISMAN_LORE_ALIASES = Map.ofEntries(Map.entry("\u0412\u0438\u0445\u0440\u044c \u043d\u0435 \u0437\u043d\u0430\u0435\u0442 \u043f\u043e\u043a\u043e\u044f", "\u0412\u0438\u0445\u0440\u044f"), Map.entry("\u041f\u043e\u0445\u0438\u0442\u0438\u0442\u0435\u043b\u044c \u043f\u0440\u0430\u0437\u0434\u043d\u0438\u043a\u0430 \u043b\u0435\u0433\u043e\u043a", "\u0413\u0440\u0438\u043d\u0447\u0430"), Map.entry("\u041f\u0435\u0447\u0430\u0442\u044c \u0440\u0430\u0437\u0436\u0438\u0433\u0430\u0435\u0442 \u044f\u0440\u043e\u0441\u0442\u044c", "\u0414\u0435\u043c\u043e\u043d\u0430"), Map.entry("\u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044c \u043d\u0435 \u0437\u043d\u0430\u0435\u0442 \u043f\u043e\u0449\u0430\u0434\u044b", "\u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f"), Map.entry("\u041b\u043e\u043c\u0430\u044e\u0449\u0430\u044f \u043f\u0440\u0435\u0433\u0440\u0430\u0434\u044b", "\u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f"), Map.entry("\u041d\u0435\u0441\u0451\u0442 \u0441\u0442\u0440\u043e\u0433\u0438\u0439 \u043f\u0440\u0438\u0433\u043e\u0432\u043e\u0440", "\u041a\u0430\u0440\u0430\u0442\u0435\u043b\u044f"), Map.entry("\u041c\u0440\u0430\u043a \u0441\u0433\u0443\u0449\u0430\u0435\u0442\u0441\u044f \u0440\u044f\u0434\u043e\u043c", "\u041c\u0440\u0430\u043a\u0430"), Map.entry("\u0420\u0430\u0437\u0434\u043e\u0440 \u0436\u0430\u0436\u0434\u0435\u0442 \u0445\u0430\u043e\u0441\u0430", "\u0420\u0430\u0437\u0434\u043e\u0440\u0430"), Map.entry("\u0422\u0438\u0440\u0430\u043d \u043f\u043e\u0434\u0430\u0432\u043b\u044f\u0435\u0442 \u0441\u043b\u0430\u0431\u044b\u0445", "\u0422\u0438\u0440\u0430\u043d\u0430"), Map.entry("\u0427\u0438\u0441\u0442\u0430\u044f, \u0434\u0438\u043a\u0430\u044f \u0430\u0433\u0440\u0435\u0441\u0441\u0438\u044f", "\u042f\u0440\u043e\u0441\u0442\u0438"));
    private static final Map<String, String> TALISMAN_SHORT_ALIASES = Map.ofEntries(Map.entry("tal-vihrya", "\u0412\u0438\u0445\u0440\u044f"), Map.entry("tal-vihra", "\u0412\u0438\u0445\u0440\u044f"), Map.entry("tal-grincha", "\u0413\u0440\u0438\u043d\u0447\u0430"), Map.entry("tal-demona", "\u0414\u0435\u043c\u043e\u043d\u0430"), Map.entry("tal-krush", "\u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f"), Map.entry("tal-karatel", "\u041a\u0430\u0440\u0430\u0442\u0435\u043b\u044f"), Map.entry("tal-mraka", "\u041c\u0440\u0430\u043a\u0430"), Map.entry("tal-razdora", "\u0420\u0430\u0437\u0434\u043e\u0440\u0430"), Map.entry("tal-tirana", "\u0422\u0438\u0440\u0430\u043d\u0430"), Map.entry("tal-yarosti", "\u042f\u0440\u043e\u0441\u0442\u0438"), Map.entry("tal-jarosti", "\u042f\u0440\u043e\u0441\u0442\u0438"));
    private static final Map<String, String> SPHERE_SHORT_ALIASES = Map.ofEntries(Map.entry("sph-aresa", "\u0410\u0440\u0435\u0441\u0430"), Map.entry("sph-gidra", "\u0413\u0438\u0434\u0440\u044b"), Map.entry("sph-moroza", "\u041c\u043e\u0440\u043e\u0437\u0430"), Map.entry("sph-satira", "\u0421\u0430\u0442\u0438\u0440\u0430"), Map.entry("sph-titana", "\u0422\u0438\u0442\u0430\u043d\u0430"), Map.entry("sph-haosa", "\u0425\u0430\u043e\u0441\u0430"), Map.entry("sph-erida", "\u042d\u0440\u0438\u0434\u0430"), Map.entry("sph-bestia", "\u0411\u0435\u0441\u0442\u0438\u0438"), Map.entry("sph-ikara", "\u0418\u043a\u0430\u0440\u0430"));
    static final Map<String, String> ITEM_DISPLAY_ALIASES = Map.ofEntries(Map.entry("EXPLOSIVE_TRAP", "\u0412\u0437\u0440\u044b\u0432\u043d\u0430\u044f \u0442\u0440\u0430\u043f\u043a\u0430"), Map.entry("explosivetrap", "\u0412\u0437\u0440\u044b\u0432\u043d\u0430\u044f \u0442\u0440\u0430\u043f\u043a\u0430"), Map.entry("STUN_STAR", "\u0421\u0442\u0430\u043d"), Map.entry("ALTERNATIVE_TRAP", "\u0422\u0440\u0430\u043f\u043a\u0430"), Map.entry("ExplosiveStuff", "\u0412\u0437\u0440\u044b\u0432\u043d\u0430\u044f \u0448\u0442\u0443\u0447\u043a\u0430"), Map.entry("SnowBall", "\u0421\u043d\u0435\u0436\u043e\u043a"), Map.entry("SunHelmet", "\u0428\u043b\u0435\u043c \u0421\u043e\u043b\u043d\u0446\u0430"), Map.entry("desorientation", "\u0414\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442\u0430\u0446\u0438\u044f"), Map.entry("sheerdust", "\u042f\u0432\u043d\u0430\u044f \u043f\u044b\u043b\u044c"), Map.entry("godsaura", "\u0410\u0443\u0440\u0430 \u0431\u043e\u0433\u0430"), Map.entry("effect-item-diz", "\u0414\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442\u0430\u0446\u0438\u044f"), Map.entry("effect-item-dust", "\u042f\u0432\u043d\u0430\u044f \u043f\u044b\u043b\u044c"), Map.entry("effect-item-god", "\u0410\u0443\u0440\u0430 \u0431\u043e\u0433\u0430"), Map.entry("effect-item-trap", "\u0422\u0440\u0430\u043f\u043a\u0430"), Map.entry("effect-item-explosivetrap", "\u0412\u0437\u0440\u044b\u0432\u043d\u0430\u044f \u0442\u0440\u0430\u043f\u043a\u0430"), Map.entry("effect-item-snowball", "\u0421\u043d\u0435\u0436\u043e\u043a"), Map.entry("effect-item-stun", "\u0421\u0442\u0430\u043d"), Map.entry("attribute-item-tkryshitela", "\u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f"), Map.entry("attribute-item-tkaratela", "\u041a\u0430\u0440\u0430\u0442\u0435\u043b\u044f"), Map.entry("attribute-item-tvihra", "\u0412\u0438\u0445\u0440\u044f"), Map.entry("attribute-item-ttirana", "\u0422\u0438\u0440\u0430\u043d\u0430"), Map.entry("attribute-item-tgrincha", "\u0413\u0440\u0438\u043d\u0447\u0430"), Map.entry("attribute-item-tdemona", "\u0414\u0435\u043c\u043e\u043d\u0430"), Map.entry("attribute-item-tmraka", "\u041c\u0440\u0430\u043a\u0430"), Map.entry("attribute-item-trazdora", "\u0420\u0430\u0437\u0434\u043e\u0440\u0430"), Map.entry("attribute-item-tyarosti", "\u042f\u0440\u043e\u0441\u0442\u0438"), Map.entry("attribute-item-saresa", "\u0410\u0440\u0435\u0441\u0430"), Map.entry("attribute-item-shydra", "\u0413\u0438\u0434\u0440\u044b"), Map.entry("attribute-item-smoroza", "\u041c\u043e\u0440\u043e\u0437\u0430"), Map.entry("attribute-item-ssatira", "\u0421\u0430\u0442\u0438\u0440\u0430"), Map.entry("attribute-item-stitana", "\u0422\u0438\u0442\u0430\u043d\u0430"), Map.entry("attribute-item-shaosa", "\u0425\u0430\u043e\u0441\u0430"), Map.entry("attribute-item-serida", "\u042d\u0440\u0438\u0434\u0430"), Map.entry("attribute-item-sbestii", "\u0411\u0435\u0441\u0442\u0438\u0438"), Map.entry("attribute-item-sikara", "\u0418\u043a\u0430\u0440\u0430"), Map.entry("attribute-item-safina", "\u0410\u0444\u0438\u043d\u044b"), Map.entry("trap", "\u0422\u0440\u0430\u043f\u043a\u0430"), Map.entry("plast", "\u041f\u043b\u0430\u0441\u0442"), Map.entry("disorientation", "\u0414\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442\u0430\u0446\u0438\u044f"), Map.entry("gods", "\u0411\u043e\u0436\u044c\u044f \u0430\u0443\u0440\u0430"), Map.entry("tornado", "\u041e\u0433\u043d\u0435\u043d\u043d\u044b\u0439 \u0441\u043c\u0435\u0440\u0447"), Map.entry("dust", "\u042f\u0432\u043d\u0430\u044f \u043f\u044b\u043b\u044c"), Map.entry("snowball", "\u0421\u043d\u0435\u0436\u043e\u043a"), Map.entry("krush", "\u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f"), Map.entry("karatelya", "\u041a\u0430\u0440\u0430\u0442\u0435\u043b\u044f"), Map.entry("dedala", "\u0414\u0435\u0434\u0430\u043b\u0430"), Map.entry("grani", "\u0413\u0440\u0430\u043d\u0438"), Map.entry("garmonii", "\u0413\u0430\u0440\u043c\u043e\u043d\u0438\u0438"), Map.entry("ehidna", "\u0415\u0445\u0438\u0434\u043d\u044b"), Map.entry("tritona", "\u0422\u0440\u0438\u0442\u043e\u043d\u0430"), Map.entry("fenixa", "\u0424\u0435\u043d\u0438\u043a\u0441\u0430"), Map.entry("yarosti", "\u042f\u0440\u043e\u0441\u0442\u0438"), Map.entry("andromeda", "\u0410\u043d\u0434\u0440\u043e\u043c\u0435\u0434\u044b"), Map.entry("titana", "\u0422\u0438\u0442\u0430\u043d\u0430"), Map.entry("apollona", "\u0410\u043f\u043e\u043b\u043b\u043e\u043d\u0430"), Map.entry("astreya", "\u0410\u0441\u0442\u0440\u0435\u044f"), Map.entry("osirisa", "\u041e\u0441\u0438\u0440\u0438\u0441\u0430"), Map.entry("satira", "\u0421\u0430\u0442\u0438\u0440\u0430"), Map.entry("pandori", "\u041f\u0430\u043d\u0434\u043e\u0440\u044b"), Map.entry("himeri", "\u0425\u0438\u043c\u0435\u0440\u044b"));
    static final Map<String, ColorRGBA> DONOR_ITEM_COLORS = Map.ofEntries(Map.entry("ExplosiveStuff", ColorRGBA.fromHex("FF0000")), Map.entry("STUN_STAR", ColorRGBA.fromHex("E4E4E4")), Map.entry("EXPLOSIVE_TRAP", ColorRGBA.fromHex("7F83A5")), Map.entry("ALTERNATIVE_TRAP", ColorRGBA.fromHex("CE7FDA")), Map.entry("Eternity", ColorRGBA.fromHex("CD0078")), Map.entry("Infinity", ColorRGBA.fromHex("00B412")), Map.entry("Stinger", ColorRGBA.fromHex("FC0F00")), Map.entry("Immortal", ColorRGBA.fromHex("7700DF")), Map.entry("Armortality", ColorRGBA.fromHex("3E4E73")), Map.entry("Flash", ColorRGBA.fromHex("0080FA")), Map.entry("Cerber", ColorRGBA.fromHex("0080FA")), Map.entry("SunHelmet", ColorRGBA.fromHex("FCC700")));
    public static final Map<Item, String> VANILLA_ITEM_LABELS = Map.ofEntries(Map.entry(Items.NETHERITE_SCRAP, "\u0422\u0440\u0430\u043f\u043a\u0430"), Map.entry(Items.ENDER_EYE, "\u0414\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442\u0430\u0446\u0438\u044f"), Map.entry(Items.FIRE_CHARGE, "\u0421\u043c\u0435\u0440\u0447"), Map.entry(Items.DRIED_KELP, "\u041f\u043b\u0430\u0441\u0442"), Map.entry(Items.PHANTOM_MEMBRANE, "\u0410\u0443\u0440\u0430 \u0431\u043e\u0433\u0430"), Map.entry(Items.SUGAR, "\u041f\u044b\u043b\u044c"), Map.entry(Items.POPPED_CHORUS_FRUIT, "\u0422\u0440\u0430\u043f\u043a\u0430"), Map.entry(Items.NETHER_STAR, "\u0421\u0442\u0430\u043d"), Map.entry(Items.SNOWBALL, "\u0421\u043d\u0435\u0436\u043e\u043a"), Map.entry(Items.PRISMARINE_SHARD, "\u0412\u0437\u0440\u044b\u0432\u043d\u0430\u044f \u0442\u0440\u0430\u043f\u043a\u0430"), Map.entry(Items.FIREWORK_STAR, "\u0413\u0443\u043b\u044c"));
    static final ColorRGBA ARTIFACT_COLOR = ColorRGBA.fromHex("A9C7D8");
    static final ColorRGBA LEGENDARY_COLOR = ColorRGBA.fromHex("0080FA");
    static final ColorRGBA MYTHICAL_COLOR = ColorRGBA.fromHex("CD0078");
    static final ColorRGBA EPIC_COLOR = ColorRGBA.fromHex("E700FA");
    static final ColorRGBA NORMAL_COLOR = ColorRGBA.fromHex("F397FA");

    public static DonorItem parseDonorItem(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return null;
        }
        NbtCompound class_24872 = ItemMetadataUtils.getCustomData(class_17992);
        if (class_24872 != null) {
            DonorItem donorItem = DonorItemParser.parseSpookyItem(class_24872);
            if (donorItem != null) {
                return donorItem;
            }
            donorItem = DonorItemParser.parseLegacyDonorData(class_17992, class_24872);
            if (donorItem != null) {
                return donorItem;
            }
            donorItem = DonorItemParser.parseTalismanMetadata(class_17992, class_24872);
            if (donorItem != null) {
                return donorItem;
            }
            donorItem = DonorItemParser.parseStructuredDonorData(class_17992, class_24872);
            if (donorItem != null) {
                return donorItem;
            }
            donorItem = DonorItemParser.parseDonationTag(class_17992, class_24872);
            if (donorItem != null) {
                return donorItem;
            }
        }
        return DonorItemParser.parseLoreMetadata(class_17992);
    }

    private static DonorItem parseStructuredDonorData(ItemStack class_17992, NbtCompound class_24872) {
        if (class_24872.contains("sixtrap", 8)) {
            String string = class_24872.getString("sixtrap");
            String string2 = ITEM_DISPLAY_ALIASES.getOrDefault(string, string);
            return new DonorItem(DonorCategory.CONSUMABLE, string2, MetadataSource.LEGACY_TAG);
        }
        if (class_24872.contains("sixitem", 8)) {
            String string = class_24872.getString("sixitem");
            String string3 = ITEM_DISPLAY_ALIASES.getOrDefault(string, string);
            return new DonorItem(DonorCategory.CONSUMABLE, string3, MetadataSource.LEGACY_TAG);
        }
        if (class_24872.contains("bettertalismans-talisman", 8)) {
            String string = class_24872.getString("bettertalismans-talisman");
            if (class_17992.getItem() == Items.PLAYER_HEAD) {
                String string4 = ITEM_DISPLAY_ALIASES.getOrDefault(string, string);
                return new DonorItem(DonorCategory.SPHERE, string4, MetadataSource.LEGACY_TAG);
            }
            if (class_17992.getItem() == Items.TOTEM_OF_UNDYING) {
                String string5 = ITEM_DISPLAY_ALIASES.getOrDefault(string, string);
                return new DonorItem(DonorCategory.TALISMAN, string5, MetadataSource.LEGACY_TAG);
            }
        }
        return null;
    }

    private static DonorItem parseTalismanMetadata(ItemStack class_17992, NbtCompound class_24872) {
        if (class_24872.contains("Donat Item", 3)) {
            return new DonorItem(DonorCategory.ARTIFACT, "SunHelmet", MetadataSource.DONOR_TAG);
        }
        if (class_24872.contains("snowball", 1)) {
            return new DonorItem(DonorCategory.CONSUMABLE, "SnowBall", MetadataSource.DONOR_TAG);
        }
        if (class_24872.contains("explosive-thing", 1)) {
            return new DonorItem(DonorCategory.CONSUMABLE, "ExplosiveStuff", MetadataSource.DONOR_TAG);
        }
        String string = DonorItemParser.readNestedString(class_24872, "PublicBukkitValues", "litetraps:item");
        if (string != null) {
            String string2 = switch (string) {
                case "stun" -> "STUN_STAR";
                case "default" -> "ALTERNATIVE_TRAP";
                case "custom" -> "EXPLOSIVE_TRAP";
                default -> string;
            };
            return new DonorItem(DonorCategory.CONSUMABLE, string2, MetadataSource.DONOR_TAG);
        }
        String string3 = DonorItemParser.readNestedString(class_24872, "PublicBukkitValues", "minecraft:talisman_type");
        if (string3 != null) {
            Rarity rarity = Rarity.fromCode(string3);
            HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
            NbtCompound class_24873 = class_24872.getCompound("PublicBukkitValues");
            if (class_24873.contains("minecraft:talisman_effect_speed", 3)) {
                hashMap.put("hms-speed", class_24873.getInt("minecraft:talisman_effect_speed"));
            }
            if (class_24873.contains("minecraft:talisman_effect_damage", 3)) {
                hashMap.put("hms-damage", class_24873.getInt("minecraft:talisman_effect_damage"));
            }
            if (class_24873.contains("minecraft:talisman_effect_armor", 3)) {
                hashMap.put("hms-armor", class_24873.getInt("minecraft:talisman_effect_armor"));
            }
            DonorCategory donorCategory = class_17992.getItem() == Items.TOTEM_OF_UNDYING ? DonorCategory.TALISMAN : DonorCategory.SPHERE;
            String string4 = DonorItemParser.findKnownDonorName(class_17992);
            return new DonorItem(donorCategory, string4, rarity, MetadataSource.DONOR_TAG, hashMap);
        }
        return null;
    }

    private static String findKnownDonorName(ItemStack class_17992) {
        String string = ItemMetadataUtils.cleanDisplayName(class_17992).toLowerCase();
        Map map = Map.ofEntries(Map.entry("eternity", "Eternity"), Map.entry("\u1d07\u1d1b\u1d07\u0280\u0274\u026a\u1d1b\u028f", "Eternity"), Map.entry("\u044d\u0442\u0435\u0440\u043d\u0438\u0442\u0438", "Eternity"), Map.entry("infinity", "Infinity"), Map.entry("\u026a\u0274\ua730\u026a\u0274\u026a\u1d1b\u028f", "Infinity"), Map.entry("\u026a\u0274\u0493\u026a\u0274\u026a\u1d1b\u028f", "Infinity"), Map.entry("\u0438\u043d\u0444\u0438\u043d\u0438\u0442\u0438", "Infinity"), Map.entry("stinger", "Stinger"), Map.entry("\ua731\u1d1b\u026a\u0274\u0262\u1d07\u0280", "Stinger"), Map.entry("s\u1d1b\u026a\u0274\u0262\u1d07\u0280", "Stinger"), Map.entry("\u0441\u0442\u0438\u043d\u0433\u0435\u0440", "Stinger"), Map.entry("immortal", "Immortal"), Map.entry("\u026a\u1d0d\u1d0d\u1d0f\u0280\u1d1b\u1d00\u029f", "Immortal"), Map.entry("\u026a\u1d0d\u1d0d\u1d0f\u0280\u1d1b\u1d00\u029f\u026a\u1d1b\u028f", "Immortal"), Map.entry("\u0438\u043c\u043c\u043e\u0440\u0442\u0430\u043b", "Immortal"), Map.entry("armortality", "Armortality"), Map.entry("\u1d00\u0280\u1d0d\u1d0f\u0280\u1d1b\u1d00\u029f\u026a\u1d1b\u028f", "Armortality"), Map.entry("\u0430\u0440\u043c\u043e\u0440\u0442\u0430\u043b\u0438\u0442\u0438", "Armortality"), Map.entry("flash", "Flash"), Map.entry("\ua730\u029f\u1d00\ua731\u029c", "Flash"), Map.entry("\u0493\u029f\u1d00s\u029c", "Flash"), Map.entry("\u0444\u043b\u0435\u0448", "Flash"), Map.entry("cerber", "Cerber"), Map.entry("\u1d04\u1d07\u0280\u0299\u1d07\u0280", "Cerber"), Map.entry("\u0446\u0435\u0440\u0431\u0435\u0440", "Cerber"));
        for (Object entryObject : map.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>)entryObject;
            if (!string.contains(String.valueOf(entry.getKey()))) continue;
            return String.valueOf(entry.getValue());
        }
        return null;
    }

    public static String normalizeDonorIdentifier(String string) {
        if (string == null) {
            return "";
        }
        String string2 = string.toLowerCase(Locale.ROOT);
        if (string2.startsWith("sphere-")) {
            return "sph-" + string2.substring("sphere-".length());
        }
        if (string2.startsWith("talisman-")) {
            return "tal-" + string2.substring("talisman-".length());
        }
        return string2;
    }

    private static DonorItem parseLoreMetadata(ItemStack class_17992) {
        boolean bl;
        boolean bl2 = class_17992.getItem() == Items.TOTEM_OF_UNDYING;
        boolean bl3 = bl = class_17992.getItem() == Items.PLAYER_HEAD;
        if (!bl2 && !bl) {
            return null;
        }
        List<String> list = DonorItemParser.getLoreLines(class_17992);
        if (list.isEmpty()) {
            return null;
        }
        DonorCategory donorCategory = bl2 ? DonorCategory.TALISMAN : DonorCategory.SPHERE;
        Map<String, String> map = bl2 ? TALISMAN_LORE_ALIASES : TALISMAN_DESCRIPTION_ALIASES;
        for (String string : list) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (!string.contains(entry.getKey())) continue;
                return new DonorItem(donorCategory, entry.getValue(), null, MetadataSource.LORE_TEXT);
            }
        }
        if (!class_17992.hasEnchantments()) {
            return null;
        }
        return new DonorItem(donorCategory, null, null, MetadataSource.LORE_TEXT);
    }

    private static List<String> getLoreLines(ItemStack class_17992) {
        LoreComponent class_92902 = (LoreComponent)class_17992.get(DataComponentTypes.LORE);
        if (class_92902 == null) {
            return List.of();
        }
        ArrayList<String> arrayList = new ArrayList<String>(class_92902.lines().size());
        for (Text class_25612 : class_92902.lines()) {
            arrayList.add(class_25612.getString());
        }
        return arrayList;
    }

    public static DonorItem parseDonorItemFallback(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return null;
        }
        DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        if (donorItem != null) {
            return donorItem;
        }
        DonorItem donorItem2 = DonorItemParser.parseItemComponents(class_17992);
        if (donorItem2 != null) {
            return donorItem2;
        }
        if (class_17992.getItem() == Items.TOTEM_OF_UNDYING && class_17992.hasEnchantments()) {
            return new DonorItem(DonorCategory.TALISMAN, null, null, MetadataSource.LORE_TEXT);
        }
        if (class_17992.getItem() == Items.PLAYER_HEAD && class_17992.hasEnchantments()) {
            return new DonorItem(DonorCategory.SPHERE, null, null, MetadataSource.LORE_TEXT);
        }
        return null;
    }

    private static DonorItem parseItemComponents(ItemStack class_17992) {
        try {
            NbtCompound class_24872;
            NbtElement class_25202 = class_17992.toNbt((RegistryWrapper.WrapperLookup)DonorItemParser.minecraftClient.world.getRegistryManager());
            if (!(class_25202 instanceof NbtCompound)) {
                return null;
            }
            NbtCompound class_24873 = (NbtCompound)class_25202;
            boolean bl = class_17992.getItem() == Items.PLAYER_HEAD;
            boolean bl2 = class_17992.getItem() == Items.TOTEM_OF_UNDYING;
            DonorItem donorItem = DonorItemParser.parseItemServiceMetadata(class_24873, bl, bl2);
            if (donorItem != null) {
                return donorItem;
            }
            if (class_24873.contains("components", 10) && (class_24872 = class_24873.getCompound("components")).contains("minecraft:custom_data", 10)) {
                String string;
                NbtCompound class_24874 = class_24872.getCompound("minecraft:custom_data");
                donorItem = DonorItemParser.parseItemServiceMetadata(class_24874, bl, bl2);
                if (donorItem != null) {
                    return donorItem;
                }
                if (bl && class_24874.contains("display", 10) && (string = DonorItemParser.readCustomDataName(class_24874)) != null) {
                    return new DonorItem(DonorCategory.SPHERE, string, null, MetadataSource.LORE_TEXT);
                }
            }
            if (bl && class_17992.hasEnchantments()) {
                return new DonorItem(DonorCategory.SPHERE, null, null, MetadataSource.LORE_TEXT);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static DonorItem parseItemServiceMetadata(NbtCompound class_24872, boolean bl, boolean bl2) {
        if (!class_24872.contains("itemServiceId", 10)) {
            return null;
        }
        NbtCompound class_24873 = class_24872.getCompound("itemServiceId");
        if (!class_24873.contains("name", 8)) {
            return null;
        }
        String string = class_24873.getString("name").toLowerCase();
        String string2 = DonorItemParser.lookupDonorAlias(string);
        Rarity rarity = DonorItemParser.parseRarity(string);
        boolean bl3 = string.startsWith("\u0442\u0430\u043b\u0438\u0441\u043c\u0430\u043d");
        if (bl2 || bl3) {
            return new DonorItem(DonorCategory.TALISMAN, string2, rarity, MetadataSource.STRUCTURED_DATA);
        }
        if (bl) {
            return new DonorItem(DonorCategory.SPHERE, string2, rarity, MetadataSource.STRUCTURED_DATA);
        }
        return null;
    }

    private static String lookupDonorAlias(String string) {
        for (Map.Entry<String, String> entry : DONOR_NAME_ALIASES.entrySet()) {
            if (!string.contains(entry.getKey())) continue;
            return entry.getValue();
        }
        return null;
    }

    private static Rarity parseRarity(String string) {
        if (string.contains("\u043c\u0438\u0444\u0438\u0447\u0435\u0441\u043a") || string.contains("mythic")) {
            return Rarity.MYTHICAL;
        }
        if (string.contains("\u043b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d") || string.contains("legendary")) {
            return Rarity.LEGENDARY;
        }
        if (string.contains("\u044d\u043f\u0438\u0447\u0435\u0441\u043a") || string.contains("epic")) {
            return Rarity.EPIC;
        }
        if (string.contains("\u043e\u0431\u044b\u0447\u043d") || string.contains("normal")) {
            return Rarity.NORMAL;
        }
        for (String string2 : KNOWN_DONOR_NAMES) {
            if (!string.contains(string2.toLowerCase())) continue;
            return Rarity.ETERNITY;
        }
        return null;
    }

    private static DonorItem parseSpookyItem(NbtCompound class_24872) {
        String string = DonorItemParser.readNestedString(class_24872, "PublicBukkitValues", "spookyitems:spooky-item");
        if (string == null) {
            string = DonorItemParser.readStringValue(class_24872, "spooky-item");
        }
        if (string == null) {
            return null;
        }
        DonorCategory donorCategory = DonorItemParser.classifyDonorCategory(string);
        return new DonorItem(donorCategory, string, MetadataSource.SPOOKY_DATA);
    }

    private static DonorCategory classifyDonorCategory(String string) {
        if (string.startsWith("effect-item-") || string.startsWith("schematic-item-")) {
            return DonorCategory.CONSUMABLE;
        }
        if (string.startsWith("attribute-item-t")) {
            return DonorCategory.TALISMAN;
        }
        if (string.startsWith("attribute-item-s")) {
            return DonorCategory.SPHERE;
        }
        return DonorCategory.UNKNOWN;
    }

    private static DonorItem parseLegacyDonorData(ItemStack class_17992, NbtCompound class_24872) {
        String string;
        String string2;
        if (class_24872.contains("pyrotechnic-item", 10) && !(string2 = class_24872.getCompound("pyrotechnic-item").getString("name")).isEmpty()) {
            return new DonorItem(DonorCategory.CONSUMABLE, string2, MetadataSource.STRUCTURED_DATA);
        }
        if (class_24872.contains("kringeItems", 10) && !(string2 = class_24872.getCompound("kringeItems").getString("type")).isEmpty()) {
            if (string2.equals("SunHelmet")) {
                return new DonorItem(DonorCategory.ARTIFACT, string2, MetadataSource.STRUCTURED_DATA);
            }
            return new DonorItem(DonorCategory.CONSUMABLE, string2, MetadataSource.STRUCTURED_DATA);
        }
        if (class_24872.contains("kringeEffect", 10) && !(string2 = class_24872.getCompound("kringeEffect").getString("type")).isEmpty()) {
            return new DonorItem(DonorCategory.ARTIFACT, string2, MetadataSource.STRUCTURED_DATA);
        }
        if (class_24872.contains("sphereEffect", 10)) {
            NbtCompound sphereData = class_24872.getCompound("sphereEffect");
            String string3 = sphereData.getString("name");
            String string4 = sphereData.getString("rank");
            boolean bl = sphereData.getBoolean("isMascot");
            String string5 = sphereData.getString("effects");
            HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
            if (string5 != null && !string5.isEmpty()) {
                DonorItemParser.parseBonusValues(string5, hashMap);
            }
            Rarity rarity = Rarity.fromCode(string4);
            DonorCategory donorCategory = class_17992.getItem() == Items.TOTEM_OF_UNDYING ? DonorCategory.TALISMAN : (class_17992.getItem() == Items.PLAYER_HEAD ? DonorCategory.SPHERE : (bl ? DonorCategory.TALISMAN : DonorCategory.SPHERE));
            return new DonorItem(donorCategory, string3.isEmpty() ? null : string3, rarity, MetadataSource.STRUCTURED_DATA, hashMap);
        }
        if (class_24872.contains("itemServiceId", 10) && !(string = class_24872.getCompound("itemServiceId").getString("name")).isEmpty()) {
            Rarity rarity;
            String string6 = string.toLowerCase();
            String string7 = DonorItemParser.lookupDonorAlias(string6);
            Rarity rarity2 = rarity = string7 != null ? Rarity.ETERNITY : DonorItemParser.parseRarity(string6);
            DonorCategory donorCategory = class_17992.getItem() == Items.TOTEM_OF_UNDYING ? DonorCategory.TALISMAN : (class_17992.getItem() == Items.PLAYER_HEAD ? DonorCategory.SPHERE : DonorCategory.UNKNOWN);
            return new DonorItem(donorCategory, string7, rarity, MetadataSource.STRUCTURED_DATA);
        }
        return null;
    }

    private static DonorItem parseDonationTag(ItemStack class_17992, NbtCompound class_24872) {
        String string = DonorItemParser.readNestedString(class_24872, "PublicBukkitValues", "minecraft:don-item");
        if (string == null) {
            string = DonorItemParser.readStringValue(class_24872, "don-item");
        }
        if (string != null) {
            String string2 = DonorItemParser.normalizeDonorIdentifier(string);
            if (string2.startsWith("tal-")) {
                String string3 = TALISMAN_SHORT_ALIASES.get(string2);
                return new DonorItem(DonorCategory.TALISMAN, string3, null, MetadataSource.LORE_TEXT);
            }
            if (string2.startsWith("sph-")) {
                String string4 = SPHERE_SHORT_ALIASES.get(string2);
                return new DonorItem(DonorCategory.SPHERE, string4, null, MetadataSource.LORE_TEXT);
            }
            if (class_17992.getItem() == Items.PLAYER_HEAD) {
                String string5 = DonorItemParser.readCustomDataName(class_24872);
                if (string5 == null) {
                    string5 = DonorItemParser.readComponentIdentifier(class_17992);
                }
                if (string5 != null) {
                    return new DonorItem(DonorCategory.SPHERE, string5, null, MetadataSource.LORE_TEXT);
                }
                return new DonorItem(DonorCategory.SPHERE, null, null, MetadataSource.LORE_TEXT);
            }
            return new DonorItem(DonorCategory.CONSUMABLE, string, MetadataSource.LORE_TEXT);
        }
        if (class_17992.getItem() == Items.TOTEM_OF_UNDYING && class_17992.hasEnchantments()) {
            String string6 = DonorItemParser.readStructuredDataName(class_24872);
            if (string6 == null) {
                string6 = DonorItemParser.readLegacyDonorIdentifier(class_17992);
            }
            return new DonorItem(DonorCategory.TALISMAN, string6, null, MetadataSource.LORE_TEXT);
        }
        if (class_17992.getItem() == Items.PLAYER_HEAD) {
            String string7 = DonorItemParser.readCustomDataName(class_24872);
            if (string7 == null) {
                string7 = DonorItemParser.readComponentIdentifier(class_17992);
            }
            if (string7 != null) {
                return new DonorItem(DonorCategory.SPHERE, string7, null, MetadataSource.LORE_TEXT);
            }
        }
        return null;
    }

    private static String readComponentIdentifier(ItemStack class_17992) {
        try {
            NbtElement class_25202 = class_17992.toNbt((RegistryWrapper.WrapperLookup)DonorItemParser.minecraftClient.world.getRegistryManager());
            if (!(class_25202 instanceof NbtCompound)) {
                return null;
            }
            NbtCompound class_24872 = (NbtCompound)class_25202;
            if (!class_24872.contains("components", 10)) {
                return null;
            }
            NbtCompound class_24873 = class_24872.getCompound("components");
            if (!class_24873.contains("minecraft:lore", 9)) {
                return null;
            }
            NbtList class_24992 = class_24873.getList("minecraft:lore", 8);
            if (class_24992.isEmpty()) {
                return null;
            }
            String string = class_24992.getString(0);
            for (Map.Entry<String, String> entry : TALISMAN_DESCRIPTION_ALIASES.entrySet()) {
                if (!string.contains(entry.getKey())) continue;
                return entry.getValue();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static String readLegacyDonorIdentifier(ItemStack class_17992) {
        try {
            NbtElement class_25202 = class_17992.toNbt((RegistryWrapper.WrapperLookup)DonorItemParser.minecraftClient.world.getRegistryManager());
            if (!(class_25202 instanceof NbtCompound)) {
                return null;
            }
            NbtCompound class_24872 = (NbtCompound)class_25202;
            if (!class_24872.contains("components", 10)) {
                return null;
            }
            NbtCompound class_24873 = class_24872.getCompound("components");
            if (!class_24873.contains("minecraft:lore", 9)) {
                return null;
            }
            NbtList class_24992 = class_24873.getList("minecraft:lore", 8);
            if (class_24992.isEmpty()) {
                return null;
            }
            String string = class_24992.getString(0);
            for (Map.Entry<String, String> entry : TALISMAN_LORE_ALIASES.entrySet()) {
                if (!string.contains(entry.getKey())) continue;
                return entry.getValue();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    @Nullable
    public static String getDonorItemLabel(ItemStack class_17992) {
        NbtCompound class_24872;
        NbtCompound class_24873 = ItemMetadataUtils.getCustomData(class_17992);
        if (class_24873 == null) {
            return null;
        }
        if (class_24873.contains("PublicBukkitValues", 10) && (class_24872 = class_24873.getCompound("PublicBukkitValues")).contains("minecraft:don-item", 8)) {
            return class_24872.getString("minecraft:don-item");
        }
        if (class_24873.contains("minecraft:don-item", 8)) {
            return class_24873.getString("minecraft:don-item");
        }
        if (class_24873.contains("don-item", 8)) {
            return class_24873.getString("don-item");
        }
        return null;
    }

    private static String readCustomDataName(NbtCompound class_24872) {
        NbtList class_24992;
        NbtCompound class_24873;
        if (class_24872.contains("display", 10) && (class_24873 = class_24872.getCompound("display")).contains("Lore", 9) && !(class_24992 = class_24873.getList("Lore", 8)).isEmpty()) {
            String string = class_24992.getString(0);
            for (Map.Entry<String, String> entry : TALISMAN_DESCRIPTION_ALIASES.entrySet()) {
                if (!string.contains(entry.getKey())) continue;
                return entry.getValue();
            }
        }
        return null;
    }

    private static String readStructuredDataName(NbtCompound class_24872) {
        if (!class_24872.contains("display", 10)) {
            return null;
        }
        NbtCompound class_24873 = class_24872.getCompound("display");
        if (!class_24873.contains("Lore", 9)) {
            return null;
        }
        NbtList class_24992 = class_24873.getList("Lore", 8);
        if (class_24992.isEmpty()) {
            return null;
        }
        String string = class_24992.getString(0);
        for (Map.Entry<String, String> entry : TALISMAN_LORE_ALIASES.entrySet()) {
            if (!string.contains(entry.getKey())) continue;
            return entry.getValue();
        }
        return null;
    }

    private static void parseBonusValues(String string, Map<String, Integer> map) {
        String[] stringArray;
        for (String string2 : stringArray = new String[]{"hms-speed", "hms-damage", "hms-armor"}) {
            int n;
            int n2;
            String string3;
            int n3;
            String string4 = "\"nbtName\":\"" + string2 + "\"";
            int n4 = string.indexOf(string4);
            if (n4 == -1) continue;
            int n5 = string.lastIndexOf("{", n4);
            int n6 = string.indexOf("}", n4);
            if (n5 == -1 || n6 == -1 || (n3 = (string3 = string.substring(n5, n6 + 1)).indexOf("\"lvl\":")) == -1) continue;
            for (n2 = n = n3 + 6; n2 < string3.length() && Character.isDigit(string3.charAt(n2)); ++n2) {
            }
            if (n2 <= n) continue;
            map.put(string2, Integer.parseInt(string3.substring(n, n2)));
        }
    }

    private static String readNestedString(NbtCompound class_24872, String string, String string2) {
        if (!class_24872.contains(string, 10)) {
            return null;
        }
        NbtCompound class_24873 = class_24872.getCompound(string);
        if (!class_24873.contains(string2, 8)) {
            return null;
        }
        String string3 = class_24873.getString(string2);
        return string3.isEmpty() ? null : string3;
    }

    private static String readStringValue(NbtCompound class_24872, String string) {
        if (!class_24872.contains(string, 8)) {
            return null;
        }
        String string2 = class_24872.getString(string);
        return string2.isEmpty() ? null : string2;
    }

    public static DonorCategory classifyItemCategory(ItemStack class_17992) {
        DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        return donorItem != null ? donorItem.getCategory() : null;
    }

    public static boolean isDonorItem(ItemStack class_17992) {
        return DonorItemParser.parseDonorItem(class_17992) != null;
    }

    public static boolean hasDamageOrArmorBonus(ItemStack class_17992) {
        DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        return donorItem != null && donorItem.isConsumableCategory();
    }

    public static boolean hasKnownDonorName(ItemStack class_17992) {
        DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        return donorItem != null && donorItem.isSphereCategory();
    }

    public static boolean isSphere(ItemStack class_17992) {
        DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        return donorItem != null && donorItem.isTalismanCategory();
    }

    public static boolean isTalisman(ItemStack class_17992) {
        DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        return donorItem != null && donorItem.isArtifactCategory();
    }

    @Generated
    private DonorItemParser() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static class DonorItem {
        private final DonorCategory category;
        private final String displayName;
        private final Rarity rarity;
        private final MetadataSource metadataSource;
        private final Map<String, Integer> bonuses;

        public DonorItem(DonorCategory donorCategory, String string, Rarity rarity, MetadataSource metadataSource, Map<String, Integer> map) {
            this.category = donorCategory;
            this.displayName = string;
            this.rarity = rarity;
            this.metadataSource = metadataSource;
            this.bonuses = map != null ? map : Map.of();
        }

        public DonorItem(DonorCategory donorCategory, String string, Rarity rarity, MetadataSource metadataSource, String string2, int n) {
            this(donorCategory, string, rarity, metadataSource, string2 != null ? Map.of(string2, n) : Map.of());
        }

        public DonorItem(DonorCategory donorCategory, String string, Rarity rarity, MetadataSource metadataSource) {
            this(donorCategory, string, rarity, metadataSource, Map.of());
        }

        public DonorItem(DonorCategory donorCategory, String string, MetadataSource metadataSource) {
            this(donorCategory, string, null, metadataSource, Map.of());
        }

        public boolean hasSpeedBonus() {
            return this.bonuses.containsKey("hms-speed");
        }

        public boolean hasDamageOrArmorBonus() {
            return this.bonuses.containsKey("hms-damage") || this.bonuses.containsKey("hms-armor");
        }

        public int getSpeedBonus() {
            return this.bonuses.getOrDefault("hms-speed", 0);
        }

        public int getDamageBonus() {
            return this.bonuses.getOrDefault("hms-damage", 0);
        }

        public int getArmorBonus() {
            return this.bonuses.getOrDefault("hms-armor", 0);
        }

        public int getMaximumBonus() {
            return this.bonuses.values().stream().max(Integer::compareTo).orElse(0);
        }

        public boolean hasKnownDonorName() {
            return this.displayName != null && KNOWN_DONOR_NAMES.contains(this.displayName);
        }

        public boolean isSphereCategory() {
            return this.category == DonorCategory.SPHERE;
        }

        public boolean isTalismanCategory() {
            return this.category == DonorCategory.TALISMAN;
        }

        public boolean isConsumableCategory() {
            return this.category == DonorCategory.CONSUMABLE;
        }

        public boolean isArtifactCategory() {
            return this.category == DonorCategory.ARTIFACT;
        }

        public String getDisplayName(ItemStack class_17992) {
            String string;
            if (this.category == DonorCategory.ARTIFACT) {
                String string2;
                if (this.displayName != null && (string2 = ITEM_DISPLAY_ALIASES.get(this.displayName)) != null) {
                    return string2;
                }
                return this.category.getLabel();
            }
            if (this.category == DonorCategory.SPHERE || this.category == DonorCategory.TALISMAN) {
                if (this.hasKnownDonorName()) {
                    return this.category.getLabel() + " " + this.displayName;
                }
                if (this.rarity != null) {
                    String string3 = this.category == DonorCategory.SPHERE ? this.rarity.getFeminineLabel() : this.rarity.getMasculineLabel();
                    return string3 + " " + this.category.getLabel().toLowerCase();
                }
                if (this.displayName != null) {
                    String string4 = ITEM_DISPLAY_ALIASES.getOrDefault(this.displayName, this.displayName);
                    return this.category.getLabel() + " " + string4;
                }
                return this.category.getLabel();
            }
            if (this.category == DonorCategory.CONSUMABLE && this.displayName != null && (string = ITEM_DISPLAY_ALIASES.get(this.displayName)) != null) {
                return string;
            }
            return ItemMetadataUtils.cleanDisplayName(class_17992);
        }

        public ColorRGBA getCategoryColor() {
            ColorRGBA colorRGBA;
            if (this.metadataSource != MetadataSource.STRUCTURED_DATA) {
                return null;
            }
            if (this.displayName != null && (colorRGBA = DONOR_ITEM_COLORS.get(this.displayName)) != null) {
                return colorRGBA;
            }
            if (this.category == DonorCategory.ARTIFACT) {
                return ARTIFACT_COLOR;
            }
            if (this.rarity != null) {
                return switch (this.rarity.ordinal()) {
                    case 3, 4 -> MYTHICAL_COLOR;
                    case 2 -> LEGENDARY_COLOR;
                    case 1 -> EPIC_COLOR;
                    default -> NORMAL_COLOR;
                };
            }
            return NORMAL_COLOR;
        }

        public ColorRGBA getDisplayColor(ItemStack class_17992) {
            if (this.getCategoryColor() != null) {
                return this.getCategoryColor();
            }
            return ItemMetadataUtils.getTextColor(class_17992.getName());
        }

        public boolean matchesIdentity(DonorItem donorItem) {
            if (donorItem == null) {
                return false;
            }
            return this.category == donorItem.category && Objects.equals(this.displayName, donorItem.displayName) && this.rarity == donorItem.rarity;
        }

        @Generated
        public DonorCategory getCategory() {
            return this.category;
        }

        @Generated
        public String getRawName() {
            return this.displayName;
        }

        @Generated
        public Rarity getRarity() {
            return this.rarity;
        }

        @Generated
        public MetadataSource getMetadataSource() {
            return this.metadataSource;
        }

        @Generated
        public Map<String, Integer> getBonuses() {
            return this.bonuses;
        }
    }

    public static enum DonorCategory {
        CONSUMABLE("\u0420\u0430\u0441\u0445\u043e\u0434\u043d\u0438\u043a"),
        SPHERE("\u0421\u0444\u0435\u0440\u0430"),
        TALISMAN("\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d"),
        ARTIFACT("\u0410\u0440\u0442\u0435\u0444\u0430\u043a\u0442"),
        UNKNOWN("\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u043e");
        private final String label;

        private DonorCategory(String string2) {
            this.label = string2;
        }

        @Generated
        public String getLabel() {
            return this.label;
        }
}

    public static enum MetadataSource {
        STRUCTURED_DATA,
        DONOR_TAG,
        LORE_TEXT,
        LEGACY_TAG,
        SPOOKY_DATA,
        UNCLASSIFIED;
}

    public static enum Rarity {
        NORMAL("NORMAL", "\u041e\u0431\u044b\u0447\u043d\u0430\u044f", "\u041e\u0431\u044b\u0447\u043d\u044b\u0439"),
        EPIC("EPIC", "\u042d\u043f\u0438\u0447\u0435\u0441\u043a\u0430\u044f", "\u042d\u043f\u0438\u0447\u0435\u0441\u043a\u0438\u0439"),
        LEGENDARY("LEGENDARY", "\u041b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u0430\u044f", "\u041b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u044b\u0439"),
        MYTHICAL("MYTHICAL", "\u041c\u0438\u0444\u0438\u0447\u0435\u0441\u043a\u0430\u044f", "\u041c\u0438\u0444\u0438\u0447\u0435\u0441\u043a\u0438\u0439"),
        ETERNITY("ETERNITY", "Eternity", "Eternity");
        private final String code;
        private final String feminineLabel;
        private final String masculineLabel;

        private Rarity(String string2, String string3, String string4) {
            this.code = string2;
            this.feminineLabel = string3;
            this.masculineLabel = string4;
        }

        public static Rarity fromCode(String string) {
            if (string == null) {
                return null;
            }
            for (Rarity rarity : Rarity.values()) {
                if (!rarity.code.equalsIgnoreCase(string)) continue;
                return rarity;
            }
            return null;
        }

        @Generated
        public String getCode() {
            return this.code;
        }

        @Generated
        public String getFeminineLabel() {
            return this.feminineLabel;
        }

        @Generated
        public String getMasculineLabel() {
            return this.masculineLabel;
        }
}
}

