package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

abstract class PotionProvider extends ServerGatedItemProvider {
    private final String displayName;

    protected PotionProvider(String settingKey, String displayName, java.util.function.BooleanSupplier availability) {
        super(settingKey, Items.SPLASH_POTION.getDefaultStack(), ItemCategory.POTIONS, availability);
        this.displayName = displayName;
    }

    @Override
    public final boolean matches(ItemStack stack) {
        return stack != null
            && !stack.isEmpty()
            && stack.getItem() == Items.SPLASH_POTION
            && stack.getName().getString().contains(this.displayName);
    }
}
