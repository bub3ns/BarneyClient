package moscow.rockstar.modules.other.inventory;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class InventoryPresetEntry {
    public Identifier itemId = Identifier.of("stone");
    public String categoryName = "";
    public String searchQuery = "";
    public String matchText = "";
    public String textureValue = "";
    public String potionEffectId = "";
    public int colorValue = -1;
    public boolean customNameMatching;
    public boolean exactNameMatch;
    public int requiredAmount = 1;
    public int minimumDurabilityPercent = 75;
    public int minimumDuration;
    public long maximumPrice;
    public final Map<String, Integer> requiredTraitCounts = new LinkedHashMap<>();
    public final List<InventoryTraitLimit> traitLimits = new ArrayList<>();
    public int requiredDuration;
    public int budgetLimit;
    public final List<Map<String, Integer>> variantProperties = new ArrayList<>();
    public int requiredStackCount;
    public String alternateSearchQuery = "";

    public InventoryPresetEntry(Identifier itemId, String searchQuery) {
        this.itemId = itemId;
        this.searchQuery = searchQuery;
    }

    public InventoryPresetEntry() {
    }

    public String getFullSearchText() {
        return matchText.isBlank() ? searchQuery : searchQuery + " " + matchText;
    }

    public String getFormattedDisplayName() {
        return InventoryBuilder.formatPresetName(getFullSearchText());
    }

    public String getMatchText() {
        if (!matchText.isBlank()) {
            return matchText;
        }
        return customNameMatching ? searchQuery : "";
    }

    public ItemStack createItemStack() {
        Item item = Registries.ITEM.get(itemId);
        ItemStack stack = item.getDefaultStack();
        stack.setCount(Math.max(1, Math.min(requiredAmount, item.getMaxCount())));
        if (!textureValue.isBlank()) {
            PropertyMap properties = new PropertyMap();
            properties.put("textures", new Property("textures", textureValue));
            stack.set(DataComponentTypes.PROFILE,
                new ProfileComponent(Optional.empty(),
                    Optional.of(UUID.nameUUIDFromBytes(textureValue.getBytes(StandardCharsets.UTF_8))),
                    properties));
        }
        if (!potionEffectId.isBlank()) {
            Identifier potionId = Identifier.tryParse(potionEffectId);
            Potion potion = potionId == null ? null : Registries.POTION.get(potionId);
            if (potion != null) {
                stack.set(DataComponentTypes.POTION_CONTENTS,
                    new PotionContentsComponent(Registries.POTION.getEntry(potion)));
            }
        } else if (colorValue >= 0) {
            stack.set(DataComponentTypes.POTION_CONTENTS,
                new PotionContentsComponent(Optional.empty(), Optional.of(colorValue), List.of(), Optional.empty()));
        }
        return stack;
    }

    public boolean isItemDamageable() {
        return Registries.ITEM.get(itemId).getDefaultStack().isDamageable();
    }

    public int getStackLimit() {
        if (requiredStackCount > 0) {
            return requiredStackCount;
        }
        ItemStack stack = Registries.ITEM.get(itemId).getDefaultStack();
        if (stack.isOf(Items.POTION)) {
            return 64;
        }
        return stack.getMaxCount();
    }

    public InventoryPresetEntry copy() {
        InventoryPresetEntry copy = new InventoryPresetEntry(itemId, searchQuery);
        copy.categoryName = categoryName;
        copy.matchText = matchText;
        copy.textureValue = textureValue;
        copy.potionEffectId = potionEffectId;
        copy.colorValue = colorValue;
        copy.customNameMatching = customNameMatching;
        copy.exactNameMatch = exactNameMatch;
        copy.requiredAmount = requiredAmount;
        copy.minimumDurabilityPercent = minimumDurabilityPercent;
        copy.minimumDuration = minimumDuration;
        copy.maximumPrice = maximumPrice;
        copy.requiredDuration = requiredDuration;
        copy.budgetLimit = budgetLimit;
        copy.requiredStackCount = requiredStackCount;
        copy.alternateSearchQuery = alternateSearchQuery;
        copy.requiredTraitCounts.putAll(requiredTraitCounts);
        copy.traitLimits.addAll(traitLimits);
        for (Map<String, Integer> variant : variantProperties) {
            copy.variantProperties.add(new LinkedHashMap<>(variant));
        }
        return copy;
    }
}
