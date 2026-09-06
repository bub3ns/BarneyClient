/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.EquipmentSlot
 *  net.minecraft.EquipmentSlot$Type
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.Vec3d
 *  net.minecraft.NbtCompound
 *  net.minecraft.NbtElement
 *  net.minecraft.Text
 *  net.minecraft.Identifier
 *  net.minecraft.Resource
 *  net.minecraft.ResourceManager
 *  net.minecraft.MutableText
 *  net.minecraft.ClientPlayNetworkHandler
 *  net.minecraft.ClientWorld
 *  net.minecraft.PlayerListEntry
 *  net.minecraft.RegistryWrapper$WrapperLookup
 *  net.minecraft.DisplayEntity$TextDisplayEntity
 *  net.minecraft.PlainTextContent
 */
package moscow.rockstar.ui.tooltip;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.math.Rotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.MutableText;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.PlainTextContent;

public final class TooltipRenderer
implements ClientAccess {
    public static Text removeText(Text class_25612, String string) {
        if (class_25612 instanceof MutableText) {
            MutableText class_52502 = (MutableText)class_25612;
            return TooltipRenderer.removeText(class_52502.copy(), string);
        }
        return TooltipRenderer.removeText(class_25612.copy(), string);
    }

    private static MutableText removeText(MutableText class_52502, String string) {
        MutableText cleanedText = Text.empty().setStyle(class_52502.getStyle());
        String literalText = TooltipRenderer.getLiteralText(class_52502);
        if (!literalText.isEmpty()) {
            String cleanedLiteral = literalText.replace(string, "");
            if (!cleanedLiteral.isEmpty()) {
                cleanedText.append(Text.literal(cleanedLiteral).setStyle(class_52502.getStyle()));
            }
        }
        for (Text sibling : class_52502.getSiblings()) {
            Text cleanedSibling = TooltipRenderer.removeText(sibling, string);
            if (!cleanedSibling.getString().isEmpty()) {
                cleanedText.append(cleanedSibling);
            }
        }
        return cleanedText;
    }

    private static String getLiteralText(Text class_25612) {
        if (class_25612 instanceof PlainTextContent plainTextContent) {
            return plainTextContent.string();
        }
        return "";
    }

    public static void hidePlayerHelmets() {
        for (Entity class_12972 : TooltipRenderer.minecraftClient.world.getPlayers()) {
            if (!(class_12972 instanceof PlayerEntity)) continue;
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            TooltipRenderer.moveHelmetToInventory(class_16572);
        }
    }

    private static void moveArmorToInventory(PlayerEntity class_16572) {
        for (EquipmentSlot class_13042 : EquipmentSlot.values()) {
            ItemStack class_17992;
            if (class_13042.getType() != EquipmentSlot.Type.HUMANOID_ARMOR && class_13042 != EquipmentSlot.MAINHAND && class_13042 != EquipmentSlot.OFFHAND || (class_17992 = class_16572.getEquippedStack(class_13042)).isEmpty()) continue;
            class_16572.getInventory().insertStack(class_17992.copy());
            class_16572.equipStack(class_13042, ItemStack.EMPTY);
        }
    }

    private static void moveHelmetToInventory(PlayerEntity class_16572) {
        for (EquipmentSlot class_13042 : EquipmentSlot.values()) {
            ItemStack class_17992;
            if (class_13042.getType() != EquipmentSlot.Type.HUMANOID_ARMOR || (class_17992 = class_16572.getEquippedStack(class_13042)).isEmpty()) continue;
            class_16572.getInventory().insertStack(class_17992.copy());
            class_16572.equipStack(class_13042, ItemStack.EMPTY);
        }
    }

    public static boolean isPlayerOnline(String string) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler TrapezoidHeightProvider = client.getNetworkHandler();
        if (TrapezoidHeightProvider == null) {
            return false;
        }
        for (PlayerListEntry ServerSamplerSource : TrapezoidHeightProvider.getPlayerList()) {
            if (!ServerSamplerSource.getProfile().getName().equals(string)) continue;
            return true;
        }
        return false;
    }

    public static String readItemModelJson(String string) {
        ResourceManager class_33002 = minecraftClient.getResourceManager();
        Identifier class_29602 = Identifier.of((String)"minecraft", (String)("models/item/" + string.replace("minecraft:", "") + ".json"));
        Optional<Resource> resource = class_33002.getResource(class_29602);
        if (resource.isEmpty()) return null;
        try {
            try (BufferedReader reader = resource.get().getReader()) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
        catch (Exception exception) {
            System.err.println("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u0438 \u0441\u0435\u0440\u0432\u0435\u0440\u043d\u043e\u0439 \u043c\u043e\u0434\u0435\u043b\u0438: " + exception.getMessage());
            return null;
        }
    }

    public static String getItemModelId(ItemStack class_17992, RegistryWrapper.WrapperLookup class_78742) {
        NbtCompound class_24872;
        NbtCompound class_24873;
        NbtElement class_25202 = class_17992.toNbtAllowEmpty(class_78742);
        if (class_25202 instanceof NbtCompound && (class_24873 = (NbtCompound)class_25202).contains("components", 10) && (class_24872 = class_24873.getCompound("components")).contains("minecraft:item_model", 8)) {
            return class_24872.getString("minecraft:item_model");
        }
        return null;
    }

    public static boolean hasNamedEntityWithinDistance(Entity class_12972, ClientWorld NarrationMessageBuilder, double d) {
        for (Entity class_12973 : NarrationMessageBuilder.getEntities()) {
            DisplayEntity.TextDisplayEntity class_81232;
            if (!(class_12973 instanceof DisplayEntity.TextDisplayEntity) || (class_81232 = (DisplayEntity.TextDisplayEntity)class_12973).getText() == null || class_81232.getText().getString().isEmpty() || !((double)class_12972.distanceTo((Entity)class_81232) < d)) continue;
            return true;
        }
        return false;
    }

    public static Rotation rotationToPosition(Vec3d VanillaChestLootTableGenerator) {
        Vec3d WallPlayerSkullBlock = TooltipRenderer.minecraftClient.player.getEyePos();
        double d = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d2 = VanillaChestLootTableGenerator.y - WallPlayerSkullBlock.y;
        double d3 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = (float)(Math.toDegrees(Math.atan2(d3, d)) - 90.0);
        float f2 = (float)(-Math.toDegrees(Math.atan2(d2, d4)));
        return new Rotation(f, f2);
    }

    @Generated
    private TooltipRenderer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
