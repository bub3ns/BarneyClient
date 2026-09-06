package moscow.rockstar.items.assist;

import moscow.rockstar.items.ItemCategory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Provider matching a server item by its normalized display-name keywords. */
public abstract class KeywordItemProvider extends AssistItemProviderBase {
    private final Item item;
    private final String[] aliases;

    protected KeywordItemProvider(String settingKey, Item item, ItemCategory category, String... aliases) {
        super(settingKey, item.getDefaultStack(), category);
        this.item = item;
        this.aliases = aliases;
    }

    @Override
    public boolean matches(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() != this.item) {
            return false;
        }
        String displayName = normalize(stack.getName().getString());
        for (String alias : this.aliases) {
            if (!displayName.contains(normalize(alias))) {
                return false;
            }
        }
        return true;
    }

    protected final Item getItem() {
        return this.item;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase().replace('\u0451', '\u0435');
    }
}
