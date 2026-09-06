package moscow.rockstar.modules.player.interaction;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.BooleanSupplier;
import moscow.rockstar.modules.player.movement.NoInteract;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public final class InteractionListener extends BooleanSetting {
    private final NoInteract noInteractModule;

    public InteractionListener(NoInteract noInteractModule, SettingOwner owner, String key,
                               BooleanSupplier activeCondition) {
        super(owner, key, activeCondition);
        this.noInteractModule = noInteractModule;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(false);
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
        super.deserialize(jsonElement);
        if (this.isEnabled()) {
            this.noInteractModule.blocks.selectRegistryId(Registries.ITEM.getId(Items.ARMOR_STAND));
        }
    }
}
