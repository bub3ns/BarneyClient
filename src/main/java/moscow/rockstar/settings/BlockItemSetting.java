/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonPrimitive
 *  javax.validation.constraints.NotNull
 *  lombok.Generated
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Block
 *  net.minecraft.Text
 *  net.minecraft.Identifier
 *  net.minecraft.DefaultedRegistry
 *  net.minecraft.Registries
 */
package moscow.rockstar.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Generated;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.AnimatedColor;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.ui.text.EditableTextComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;

public class BlockItemSetting
extends AbstractSetting {
    private final List<Option> availableOptions = new ArrayList<Option>();
    private final Map<Identifier, Option> optionsById = new ConcurrentHashMap<Identifier, Option>();
    private final LinkedHashSet<Identifier> selectedIds = new LinkedHashSet();
    private final List<Option> filteredOptions = new ArrayList<Option>();
    private String searchText = "";
    private String normalizedSearchText = "";
    private Set<Identifier> allowedBlockIds = null;
    private final Map<Identifier, Item> explicitItemsById = new LinkedHashMap<Identifier, Item>();
    private boolean includeAllItems;
    private Predicate<Block> blockFilter = null;

    public BlockItemSetting(@NotNull SettingOwner settingOwner, String string, String string2, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
        this.rebuildOptions();
    }

    public BlockItemSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
        this.rebuildOptions();
    }

    public BlockItemSetting(@NotNull SettingOwner settingOwner, String string, String string2) {
        super(settingOwner, string);
        this.rebuildOptions();
    }

    public BlockItemSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
        this.rebuildOptions();
    }

    public BlockItemSetting includeBlocks(Block ... class_2248Array) {
        this.allowedBlockIds = class_2248Array == null || class_2248Array.length == 0 ? null : (Set)Arrays.stream(class_2248Array).map(arg_0 -> ((DefaultedRegistry)Registries.BLOCK).getId(arg_0)).collect(Collectors.toCollection(LinkedHashSet::new));
        this.blockFilter = null;
        this.rebuildOptions();
        return this;
    }

    public BlockItemSetting filterBlocks(Predicate<Block> predicate) {
        this.blockFilter = predicate;
        this.allowedBlockIds = null;
        this.rebuildOptions();
        return this;
    }

    public BlockItemSetting includeBlock(Block class_22482) {
        if (class_22482 == null) {
            return this;
        }
        if (this.allowedBlockIds == null) {
            this.allowedBlockIds = new LinkedHashSet<Identifier>();
        }
        this.allowedBlockIds.add(Registries.BLOCK.getId(class_22482));
        this.blockFilter = null;
        this.rebuildOptions();
        return this;
    }

    public BlockItemSetting includeItem(Item class_17922) {
        if (class_17922 == null) {
            return this;
        }
        this.explicitItemsById.put(Registries.ITEM.getId(class_17922), class_17922);
        this.rebuildOptions();
        return this;
    }

    public BlockItemSetting enableAllItems() {
        this.includeAllItems = true;
        this.rebuildOptions();
        return this;
    }

    private void rebuildOptions() {
        Option option;
        Identifier class_29602;
        this.availableOptions.clear();
        this.optionsById.clear();
        for (Block block : Registries.BLOCK) {
            class_29602 = Registries.BLOCK.getId(block);
            if (this.allowedBlockIds != null && !this.allowedBlockIds.contains(class_29602) || this.blockFilter != null && !this.blockFilter.test(block) || (option = Option.fromBlock(block)) == null) continue;
            this.availableOptions.add(option);
            this.optionsById.put(option.getRegistryId(), option);
        }
        Iterable<Item> itemSource = this.includeAllItems ? Registries.ITEM : this.explicitItemsById.values();
        for (Item item : itemSource) {
            class_29602 = Registries.ITEM.getId(item);
            option = Option.fromItem(item);
            if (option == null || this.optionsById.containsKey(option.getRegistryId())) continue;
            this.availableOptions.add(option);
            this.optionsById.put(option.getRegistryId(), option);
        }
        this.availableOptions.sort(Comparator.comparing(Option::getDisplayName, String.CASE_INSENSITIVE_ORDER));
        this.selectedIds.retainAll(this.optionsById.keySet());
        this.filterOptions();
    }

    public final void setSearchText(String string) {
        String string2;
        String string3 = string2 = string == null ? "" : string;
        if (Objects.equals(this.searchText, string2)) {
            return;
        }
        this.searchText = string2;
        this.normalizedSearchText = string2.trim().toLowerCase(Locale.ROOT);
        this.filterOptions();
    }

    public final List<Option> getFilteredOptions() {
        this.filterOptions();
        return Collections.unmodifiableList(this.filteredOptions);
    }

    private void filterOptions() {
        this.filteredOptions.clear();
        if (this.normalizedSearchText.isEmpty()) {
            this.filteredOptions.addAll(this.availableOptions);
        } else {
            for (Option option2 : this.availableOptions) {
                if (!option2.getDisplayName().toLowerCase(Locale.ROOT).contains(this.normalizedSearchText) && !option2.getRegistryId().toString().toLowerCase(Locale.ROOT).contains(this.normalizedSearchText)) continue;
                this.filteredOptions.add(option2);
            }
        }
        this.filteredOptions.sort(Comparator.comparing(option -> !this.isOptionSelected((Option)option)));
    }

    public final void toggleOption(Option option) {
        if (option == null) {
            return;
        }
        Identifier class_29602 = option.getRegistryId();
        if (!this.optionsById.containsKey(class_29602)) {
            return;
        }
        this.notifyChange();
        if (!this.selectedIds.remove(class_29602)) {
            this.selectedIds.add(class_29602);
        }
    }

    public final void toggleBlock(Block class_22482) {
        if (class_22482 == null) {
            return;
        }
        Identifier class_29602 = Registries.BLOCK.getId(class_22482);
        if (this.optionsById.containsKey(class_29602)) {
            this.notifyChange();
            if (!this.selectedIds.remove(class_29602)) {
                this.selectedIds.add(class_29602);
            }
        }
    }

    public final BlockItemSetting selectBlock(Block class_22482) {
        if (class_22482 == null) {
            return this;
        }
        Identifier class_29602 = Registries.BLOCK.getId(class_22482);
        if (this.optionsById.containsKey(class_29602)) {
            if (this.selectedIds.contains(class_29602)) {
                return this;
            }
            this.notifyChange();
            this.selectedIds.add(class_29602);
        }
        return this;
    }

    public final BlockItemSetting selectRegistryId(Identifier class_29602) {
        if (class_29602 != null && this.optionsById.containsKey(class_29602)) {
            if (this.selectedIds.contains(class_29602)) {
                return this;
            }
            this.notifyChange();
            this.selectedIds.add(class_29602);
        }
        return this;
    }

    public final boolean isOptionSelected(Option option) {
        return option != null && this.selectedIds.contains(option.getRegistryId());
    }

    public final boolean isBlockSelected(Block class_22482) {
        if (class_22482 == null) {
            return false;
        }
        return this.selectedIds.contains(Registries.BLOCK.getId(class_22482));
    }

    public final boolean isRegistryIdSelected(Identifier class_29602) {
        return class_29602 != null && this.selectedIds.contains(class_29602);
    }

    public final Set<Identifier> getSelectedIds() {
        return Collections.unmodifiableSet(this.selectedIds);
    }

    public final List<Option> getSelectedOptions() {
        return this.selectedIds.stream().map(this.optionsById::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public final Set<Block> getSelectedBlocks() {
        return this.selectedIds.stream().map(this.optionsById::get).filter(option -> option != null && option.getBlock() != null).map(Option::getBlock).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public final Set<Item> getSelectedItems() {
        return this.selectedIds.stream().map(this.optionsById::get).filter(Objects::nonNull).map(Option::getItem).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public final int getSelectedCount() {
        return this.selectedIds.size();
    }

    @Override
    public final JsonElement serialize() {
        JsonArray jsonArray = new JsonArray();
        for (Identifier class_29602 : this.selectedIds) {
            jsonArray.add((JsonElement)new JsonPrimitive(class_29602.toString()));
        }
        return jsonArray;
    }

    @Override
    public final void deserialize(JsonElement jsonElement) {
        if (jsonElement == null) {
            return;
        }
        if (jsonElement.isJsonArray()) {
            for (JsonElement element : jsonElement.getAsJsonArray()) {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) continue;
                return;
            }
        } else if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isString()) {
            return;
        }
        this.selectedIds.clear();
        if (jsonElement.isJsonArray()) {
            for (JsonElement element : jsonElement.getAsJsonArray()) {
                this.selectRegistryIdFromString(element.getAsString());
            }
        } else if (jsonElement.isJsonPrimitive()) {
            this.selectRegistryIdFromString(jsonElement.getAsString());
        }
    }

    @Override
    public boolean isValidJson(JsonElement jsonElement) {
        if (jsonElement == null) {
            return false;
        }
        if (jsonElement.isJsonPrimitive()) {
            return this.isValidRegistryId(jsonElement);
        }
        if (!jsonElement.isJsonArray()) {
            return false;
        }
        for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
            if (this.isValidRegistryId(jsonElement2)) continue;
            return false;
        }
        return true;
    }

    private boolean isValidRegistryId(JsonElement jsonElement) {
        if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isString()) {
            return false;
        }
        Identifier class_29602 = Identifier.tryParse((String)jsonElement.getAsString());
        return class_29602 != null && this.optionsById.containsKey(class_29602);
    }

    private void selectRegistryIdFromString(String string) {
        Identifier class_29602 = Identifier.tryParse((String)string);
        if (class_29602 != null && this.optionsById.containsKey(class_29602)) {
            this.selectedIds.add(class_29602);
        }
    }

    @Override
    public Component buildComponent() {
        FontMetrics fontMetrics = Font.REGULAR.metrics(8.0f);
        FontMetrics fontMetrics2 = Font.MEDIUM.metrics(7.0f);
        FontMetrics fontMetrics3 = Font.REGULAR.metrics(7.0f);
        Component component = new Component().add(new ScrollingTextComponent(fontMetrics, () -> Localization.translate(this.key)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).fadeOut().fill()).add(new TextComponent().text(fontMetrics2, () -> this.getSelectedCount() + "/" + this.availableOptions.size(), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.6f + 0.4f * textComponent.hover())).textAlign(Alignment.END)).layout(Layout.ROW).alignment(Alignment.CENTER).gap(6.0f).padding(Insets.of(6.0f, 0.0f, 0.0f, 0.0f)).fillWidth();
        EditableTextComponent searchEditor = new EditableTextComponent(fontMetrics3, this.searchText, this::setSearchText);
        searchEditor.placeholder(() -> Localization.translate("search"));
        searchEditor.background(ColorPalette.MUTED_PANEL_COLOR);
        searchEditor.radius(4.0f);
        searchEditor.padding(4.0f);
        searchEditor.height(15.0f);
        Component component2 = new Component().add(searchEditor).add(new ItemGrid<Option>(this::getFilteredOptions, Option::getItemStack, this::isOptionSelected, this::toggleOption).setCellSize(18.0f).fillWidth()).layout(Layout.COLUMN).gap(3.0f).fillWidth();
        return new Component().layout(Layout.COLUMN).gap(4.0f).add(component).add(component2);
    }

    @Generated
    public List<Option> getAllOptions() {
        return this.availableOptions;
    }

    @Generated
    public Map<Identifier, Option> getOptionsById() {
        return this.optionsById;
    }

    @Generated
    public String getSearchText() {
        return this.searchText;
    }

    @Generated
    public String getNormalizedSearchText() {
        return this.normalizedSearchText;
    }

    @Generated
    public Set<Identifier> getAllowedBlockIds() {
        return this.allowedBlockIds;
    }

    @Generated
    public Map<Identifier, Item> getExplicitItemsById() {
        return this.explicitItemsById;
    }

    @Generated
    public boolean isAllItemsEnabled() {
        return this.includeAllItems;
    }

    @Generated
    public Predicate<Block> getBlockFilter() {
        return this.blockFilter;
    }

    public static final class Option {
        private final Block block;
        private final Item item;
        private final Identifier registryId;
        private final ItemStack itemStack;
        private final AnimatedColor hoverColor;
        private final Animation hoverAnimation;

        public Option(Block class_22482, Item class_17922, Identifier class_29602, ItemStack class_17992) {
            this.block = class_22482;
            this.item = class_17922;
            this.registryId = class_29602;
            this.itemStack = class_17992;
            this.hoverColor = new AnimatedColor(300L);
            this.hoverAnimation = new Animation(300L, Easing.easeInOutSine);
        }

        public String getDisplayName() {
            return Text.translatable((String)(this.block != null ? this.block.getTranslationKey() : this.item.getTranslationKey())).getString();
        }

        static Option fromBlock(Block class_22482) {
            if (class_22482 == null) {
                return null;
            }
            Identifier class_29602 = Registries.BLOCK.getId(class_22482);
            ItemStack class_17992 = class_22482.asItem().getDefaultStack();
            if (class_17992.isEmpty()) {
                return null;
            }
            return new Option(class_22482, class_17992.getItem(), class_29602, class_17992);
        }

        static Option fromItem(Item class_17922) {
            if (class_17922 == null) {
                return null;
            }
            Identifier class_29602 = Registries.ITEM.getId(class_17922);
            ItemStack class_17992 = class_17922.getDefaultStack();
            if (class_17992.isEmpty()) {
                return null;
            }
            return new Option(null, class_17922, class_29602, class_17992);
        }

        @Generated
        public Block getBlock() {
            return this.block;
        }

        @Generated
        public Item getItem() {
            return this.item;
        }

        @Generated
        public Identifier getRegistryId() {
            return this.registryId;
        }

        @Generated
        public ItemStack getItemStack() {
            return this.itemStack;
        }

        @Generated
        public AnimatedColor getHoverColor() {
            return this.hoverColor;
        }

        @Generated
        public Animation getHoverAnimation() {
            return this.hoverAnimation;
        }
    }
}
