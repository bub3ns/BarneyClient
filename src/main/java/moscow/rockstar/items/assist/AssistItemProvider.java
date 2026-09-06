package moscow.rockstar.items.assist;

import java.util.List;
import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.ui.localization.Localization;
import net.minecraft.item.ItemStack;

/** A bindable item action exposed by the Assist module. */
public interface AssistItemProvider {
    String getSettingKey();

    ItemStack getItemStack();

    ItemCategory getCategory();

    int getKeyCode();

    void setKeyCode(int keyCode);

    void onSelected();

    boolean isAvailable();

    default boolean isActive() {
        return false;
    }

    default void tick() {
    }

    default boolean isSpecial() {
        return false;
    }

    default String getDisplayName() {
        return Localization.translate(this.getSettingKey());
    }

    default boolean matches(ItemStack stack) {
        return stack != null
            && !stack.isEmpty()
            && stack.getItem() == this.getItemStack().getItem();
    }

    List<Setting> getSettings();
}
