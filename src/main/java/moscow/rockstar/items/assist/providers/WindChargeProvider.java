package moscow.rockstar.items.assist.providers;

import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistItemProviderBase;
import moscow.rockstar.items.assist.sequence.ActionSequence;
import moscow.rockstar.items.assist.sequence.DelayAction;
import moscow.rockstar.items.assist.sequence.UseItemAction;
import moscow.rockstar.math.Rotation;
import net.minecraft.item.Items;

public final class WindChargeProvider extends AssistItemProviderBase {
    private final ActionSequence sequence = new ActionSequence();
    private boolean pending;

    public WindChargeProvider() {
        super("modules.settings.assist.wind_charge", Items.WIND_CHARGE.getDefaultStack(), ItemCategory.OTHER);
    }

    @Override
    public void onSelected() {
        float yaw = ClientServiceRegistry.getInstance().getRotationManager().getCurrentRotation().getYaw();
        this.pending = true;
        this.sequence.addAction(new DelayAction(100L))
            .addAction(new UseItemAction(Items.WIND_CHARGE))
            .addAction(new DelayAction(100L))
            .start();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean isActive() {
        return this.sequence.isRunning() || this.pending;
    }

    @Override
    public void tick() {
        if (this.pending && !this.sequence.isRunning()) {
            this.pending = false;
        }
        if (this.pending) {
            float yaw = ClientServiceRegistry.getInstance().getRotationManager().getCurrentRotation().getYaw();
            ClientServiceRegistry.getInstance().getRotationManager().requestRotation(
                new Rotation(yaw, 90.0f),
                RotationCorrectionMode.UNSPECIFIED,
                180.0f,
                180.0f,
                180.0f,
                RotationPriority.ITEM_USE_PRIORITY
            );
        }
        this.sequence.tick();
    }
}
