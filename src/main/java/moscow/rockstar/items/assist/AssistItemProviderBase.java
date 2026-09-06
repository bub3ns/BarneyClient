package moscow.rockstar.items.assist;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.item.ItemStack;

/** Common state held by the original Assist item providers. */
public abstract class AssistItemProviderBase implements AssistItemProvider, SettingOwner {
    private final String settingKey;
    private final ItemStack itemStack;
    private final ItemCategory category;
    private int keyCode;
    private final List<Setting> settings = new ArrayList<>();

    protected AssistItemProviderBase(String settingKey, ItemStack itemStack, ItemCategory category) {
        this.settingKey = settingKey;
        this.itemStack = itemStack;
        this.category = category;
    }

    @Override
    public void onSelected() {
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public final String getSettingKey() {
        return this.settingKey;
    }

    @Override
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Override
    public final ItemCategory getCategory() {
        return this.category;
    }

    @Override
    public final int getKeyCode() {
        return this.keyCode;
    }

    @Override
    public final void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    @Override
    public final List<Setting> getSettings() {
        return this.settings;
    }

}
