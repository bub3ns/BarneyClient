package moscow.rockstar.api.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import ua.mintantileak.spk.Compile;

/**
 * ORIGINAL: {@code rockstar/ilIlil/IIiIiiii}, slot 8 of {@code rockstar/ilIlil/IIIiiiIi#I()V}.
 */
public class InventoryCommandService implements ClientAccess {
    private final Map<String, Map<Integer, Integer>> snapshots = new HashMap<String, Map<Integer, Integer>>();

    @Compile
    public ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("inv")
            .aliases("inventory", "slot", "\u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c")
            .description("commands.inventory.description")
            .argument("action", argument -> argument
                .choicesAndValidator("save", "create", "add", "\u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c", "load", "use", "\u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c")
                .choices("save", "load"))
            .argument("name", argument -> argument
                .optional()
                .validator(value -> value.length() < 2
                    ? PluginResolver.resolveMessage("commands.prefix.invalid_length")
                    : PluginResolver.resolveValue(value)))
            .handler(this::executeInventoryCommand)
            .build();
    }

    @Compile
    private void executeInventoryCommand(DispatchContext dispatchContext) {
        String action = (String) dispatchContext.getArguments().get(0);
        String name = (String) dispatchContext.getArguments().get(1);
        switch (action.toLowerCase(Locale.ROOT)) {
            case "save", "create", "add", "\u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c" -> {
                this.saveSnapshot(name);
                Notification.info(Text.of((String) Localization.translateFormatted("commands.inventory.saved", name)));
            }
            case "load", "use", "\u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c" -> this.loadSnapshot(name);
            default -> Notification.error(Text.of((String) Localization.translate("commands.inventory.invalid_action")));
        }
    }

    @Compile
    private void saveSnapshot(String name) {
        if (minecraftClient.player == null) {
            return;
        }
        HashMap<Integer, Integer> slots = new HashMap<Integer, Integer>();
        for (int slot = 0; slot <= 45; ++slot) {
            ItemStack stack = minecraftClient.player.currentScreenHandler.getSlot(slot).getStack();
            if (stack.isEmpty()) {
                continue;
            }
            slots.put(slot, Item.getRawId(stack.getItem()));
        }
        this.snapshots.put(name, slots);
    }

    private void loadSnapshot(String name) {
        if (!this.snapshots.containsKey(name)) {
            Notification.error(Text.of((String) Localization.translateFormatted("commands.inventory.not_found", name)));
            return;
        }
        Map<Integer, Integer> slots = this.snapshots.get(name);
        boolean restored = false;
        for (Map.Entry<Integer, Integer> entry : slots.entrySet()) {
            int slot = entry.getKey();
            Item item = Item.byRawId(entry.getValue());
            ItemStack stack = new ItemStack((ItemConvertible) item);
            stack.setCount(1);
            minecraftClient.player.currentScreenHandler.getSlot(slot).setStack(stack);
            restored = true;
        }
        if (restored) {
            Notification.info(Text.of((String) Localization.translate("commands.inventory.loaded")));
        } else {
            Notification.error(Text.of((String) Localization.translate("commands.inventory.empty")));
        }
    }

    @Compile
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, Map<Integer, Integer>> entry : this.snapshots.entrySet()) {
            JsonObject slots = new JsonObject();
            for (Map.Entry<Integer, Integer> slot : entry.getValue().entrySet()) {
                slots.addProperty(slot.getKey().toString(), (Number) slot.getValue());
            }
            root.add(entry.getKey(), slots);
        }
        return root;
    }

    @Compile
    public void fromJson(JsonElement jsonElement) {
        this.snapshots.clear();
        JsonObject root = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            JsonObject slots = entry.getValue().getAsJsonObject();
            HashMap<Integer, Integer> parsed = new HashMap<Integer, Integer>();
            for (Map.Entry<String, JsonElement> slot : slots.entrySet()) {
                parsed.put(Integer.valueOf(slot.getKey()), slot.getValue().getAsInt());
            }
            this.snapshots.put(entry.getKey(), parsed);
        }
    }
}
