package moscow.rockstar.items.assist.providers;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.items.assist.AssistFeatureAvailability;
import moscow.rockstar.inventory.ItemSwapManager;
import moscow.rockstar.items.assist.KeywordItemProvider;
import moscow.rockstar.ui.input.KeyBindingUtil;
import moscow.rockstar.util.Timer;
import net.minecraft.item.Items;

public final class ExperienceScrollProvider extends KeywordItemProvider implements ClientAccess {
    private final Timer activationTimer = new Timer();
    private boolean active;

    public ExperienceScrollProvider() {
        super("modules.settings.assist.experience_scroll", Items.FLOWER_BANNER_PATTERN, moscow.rockstar.items.ItemCategory.CONSUMABLES, "свит", "опыт");
    }

    @Override
    public void onSelected() {
        this.active = true;
        this.activationTimer.reset();
        this.consume();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean isAvailable() {
        return AssistFeatureAvailability.commonConsumable();
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void tick() {
        if (!this.active) {
            return;
        }
        if (minecraftClient.player == null || minecraftClient.world == null || minecraftClient.currentScreen != null || !this.keyStillPressed()) {
            this.active = false;
            return;
        }
        if (this.activationTimer.hasElapsed(50L)) {
            this.activationTimer.reset();
            this.consume();
        }
    }

    private void consume() {
        if (!ItemSwapManager.getInstance().isBusy()) {
            ItemSwapManager.getInstance().swap(this.getItem(), this::matches, this.getDisplayName());
        }
    }

    private boolean keyStillPressed() {
        return KeyBindingUtil.isPressed(this.getKeyCode());
    }
}
