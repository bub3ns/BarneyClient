package moscow.rockstar.modules.visuals.hud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.items.assist.AssistItemProvider;
import moscow.rockstar.mixin.accessors.ItemCooldownEntryAccessor;
import moscow.rockstar.mixin.accessors.ItemCooldownManagerAccessor;
import moscow.rockstar.modules.other.assist.Assist;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.input.KeyBindingUtil;
import moscow.rockstar.ui.input.KeyDisplayFormatter;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import pyrock.utility.render.ColorRGBA;

/** Displays the item binds and active item cooldowns used by Assist. */
public final class ItemBindsHud extends HudElement {
    private final Map<Integer, Animation> bindAnimations = new HashMap<>();
    private final Map<Integer, Animation> mouseBindAnimations = new HashMap<>();
    private final NumberSetting columnsSetting = new NumberSetting(this, "hud.item_binds.per_row")
        .setMinValue(1.0f)
        .setMaxValue(5.0f)
        .setStep(1.0f)
        .setValue(4.0f);

    public ItemBindsHud() {
        super("hud.item_binds", "hud/clock");
    }

    @Override
    public void renderComponent(RockstarDrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) {
            return;
        }
        boolean chatScreen = client.currentScreen instanceof ChatScreen;
        Assist assist = RockstarClient.create().getModuleRegistry().getModule(Assist.class);
        if (assist == null && !chatScreen) {
            return;
        }
        List<ItemBindEntry> entries = assist == null
            ? new ArrayList<>()
            : collectAssistItems(assist);
        if (entries.isEmpty() && chatScreen) {
            entries = collectChatPreviewItems();
        }
        if (entries.isEmpty()) {
            return;
        }
        drawEntries(drawContext, entries);
    }

    private List<ItemBindEntry> collectAssistItems(Assist assist) {
        Map<Integer, ItemBindEntry> boundItems = new HashMap<>();
        for (AssistItemProvider provider : assist.getAssistItems()) {
            if (!provider.isAvailable() || provider.getKeyCode() < 0) {
                continue;
            }
            ItemStack stack = provider.getItemStack();
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            boundItems.putIfAbsent(provider.getKeyCode(),
                new ItemBindEntry(stack.getItem(), provider.getKeyCode()));
        }

        List<ItemBindEntry> entries = new ArrayList<>(boundItems.values());
        Set<Item> knownItems = new HashSet<>();
        for (ItemBindEntry entry : entries) {
            knownItems.add(entry.getItem());
        }
        appendCooldownItems(entries, knownItems);
        return entries;
    }

    private List<ItemBindEntry> collectChatPreviewItems() {
        Item previewItem = ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD)
            && !ServerDetector.serverAddressContains("holytime")
            ? Items.POPPED_CHORUS_FRUIT
            : Items.NETHERITE_SCRAP;
        return List.of(
            new ItemBindEntry(previewItem, -1),
            new ItemBindEntry(Items.ENDER_EYE, -1),
            new ItemBindEntry(Items.SUGAR, -1)
        );
    }

    private void appendCooldownItems(List<ItemBindEntry> entries, Set<Item> knownItems) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        ItemCooldownManagerAccessor cooldowns = (ItemCooldownManagerAccessor)client.player.getItemCooldownManager();
        for (Identifier groupId : cooldowns.rockstar$getEntries().keySet()) {
            Item item = Registries.ITEM.get(groupId);
            appendCooldownItem(entries, knownItems, item);
        }
        for (int slot = 0; slot < client.player.getInventory().size(); slot++) {
            appendCooldownStack(entries, knownItems, client.player.getInventory().getStack(slot));
        }
        appendCooldownStack(entries, knownItems, client.player.getOffHandStack());
    }

    private void appendCooldownStack(List<ItemBindEntry> entries, Set<Item> knownItems, ItemStack stack) {
        if (stack != null && !stack.isEmpty() && isCoolingDown(stack)) {
            appendCooldownItem(entries, knownItems, stack.getItem());
        }
    }

    private void appendCooldownItem(List<ItemBindEntry> entries, Set<Item> knownItems, Item item) {
        if (item == null || item == Items.AIR || knownItems.contains(item) || calculateCooldownSeconds(item) <= 0.0f) {
            return;
        }
        if (!isInPlayerInventory(item)) {
            return;
        }
        entries.add(new ItemBindEntry(item, -1));
        knownItems.add(item);
    }

    private boolean isInPlayerInventory(Item item) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return false;
        }
        for (int slot = 0; slot < client.player.getInventory().size(); slot++) {
            if (client.player.getInventory().getStack(slot).getItem() == item) {
                return true;
            }
        }
        return client.player.getOffHandStack().getItem() == item;
    }

    private boolean isCoolingDown(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null
            && client.player.getItemCooldownManager().isCoolingDown(stack);
    }

    private float calculateCooldownSeconds(Item item) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return 0.0f;
        }
        ItemCooldownManagerAccessor cooldowns = (ItemCooldownManagerAccessor)client.player.getItemCooldownManager();
        Identifier groupId = cooldowns.rockstar$getGroup(item.getDefaultStack());
        Object entryObject = cooldowns.rockstar$getEntries().get(groupId);
        if (!(entryObject instanceof ItemCooldownEntryAccessor entry)) {
            return 0.0f;
        }
        return Math.max(0.0f, (entry.rockstar$getEndTick() - cooldowns.rockstar$getTick()) / 20.0f);
    }

    private void drawEntries(RockstarDrawContext drawContext, List<ItemBindEntry> entries) {
        int columns = Math.max(1, Math.min((int)columnsSetting.getValue(), entries.size()));
        int rows = (entries.size() + columns - 1) / columns;
        this.width = columns * 26.0f - 5.0f;
        this.height = rows * 32.0f - 4.0f;
        FontMetrics font = Font.MEDIUM.metrics(6.0f);

        for (int index = 0; index < entries.size(); index++) {
            int column = index % columns;
            int row = index / columns;
            float left = this.x + column * 26.0f;
            float top = this.y + row * 32.0f;
            drawContext.drawClientRect(left, top, 22.0f, 23.0f,
                255.0f * this.animation.getValue() * Interface.getLiquidGlassAlpha(),
                this.dragAnim.getValue(), 7.0f, 4.0f);

            ItemBindEntry entry = entries.get(index);
            drawContext.drawItem(entry.getItem(), left + 3.0f, top + 3.5f, 1.0f);
            drawCooldownOverlay(drawContext, entry.getItem(), left + 3.0f, top + 3.5f);

            String label = entry.getKey() >= 0
                ? formatBindLabel(entry.getKey())
                : String.format("%.1f", calculateCooldownSeconds(entry.getItem()));
            if (label.length() > 3) {
                label = label.substring(0, 3);
            }
            float labelWidth = font.measureText(label);
            float labelLeft = Math.round(left + 3.5f + 7.5f - calculateEntryWidth(labelWidth) / 2.0f);
            drawContext.drawRoundedRect(labelLeft, top + 21.0f,
                calculateEntryWidth(labelWidth), 8.0f,
                WidgetState.uniform(1.0f), ColorPalette.getAccentColor());
            drawContext.drawText(font, label, labelLeft + 2.0f, top + 22.0f,
                ColorPalette.getHighlightColor());
        }
    }

    private void drawCooldownOverlay(RockstarDrawContext drawContext, Item item, float left, float top) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || !isCoolingDown(item.getDefaultStack())) {
            return;
        }
        float progress = client.player.getItemCooldownManager().getCooldownProgress(
            item.getDefaultStack(), drawContext.tickDelta());
        float visibleHeight = 16.0f * Math.max(0.0f, Math.min(1.0f, progress));
        drawContext.drawRoundedRect(left, top + 16.0f - visibleHeight,
            16.0f, visibleHeight, WidgetState.uniform(3.0f),
            ColorRGBA.BLACK.mulAlpha(0.48f * this.animation.getValue()));
        drawContext.drawCircleProgress(left + 8.0f, top + 8.0f, 14.0f / 3.0f,
            1.35f, progress, ColorPalette.getAccentColor());
    }

    private float calculateEntryWidth(float textWidth) {
        return Math.round(textWidth / 2.0f) * 2.0f + 4.0f;
    }

    private String formatBindLabel(int binding) {
        if (KeyBindingUtil.isMouseBinding(binding)) {
            return KeyDisplayFormatter.formatRawKey(KeyBindingUtil.keyCode(binding));
        }
        return KeyDisplayFormatter.formatKey(binding);
    }

    @SuppressWarnings("unused")
    private Animation bindAnimation(int binding) {
        Map<Integer, Animation> animations = KeyBindingUtil.isMouseBinding(binding)
            ? mouseBindAnimations : bindAnimations;
        return animations.computeIfAbsent(binding,
            ignored -> new Animation(200L, 0.0f, Easing.easeOutBack));
    }
}
