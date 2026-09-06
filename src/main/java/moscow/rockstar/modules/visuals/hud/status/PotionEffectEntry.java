package moscow.rockstar.modules.visuals.hud.status;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

final class PotionEffectEntry {
    final PotionType potionType;
    final int durationTicks;
    ItemStack itemStack = ItemStack.EMPTY;
    long startTimeMillis;
    long lastUpdateMillis;
    float progressTicks;
    float progressDirection;
    boolean active = true;

    PotionEffectEntry(PotionType potionType) {
        this.potionType = potionType;
        this.durationTicks = potionType.durationTicks;
    }

    void updateProgress(int currentProgress, int nextProgress, ItemStack stack, long timestamp) {
        int progress = nextProgress >= 0 ? nextProgress : currentProgress;
        if (progress < 0) {
            progress = 0;
        }
        this.progressTicks = progress;
        if (currentProgress >= 0 && nextProgress >= 0) {
            int difference = nextProgress - currentProgress;
            if (this.potionType == PotionType.BREWING_STAND) {
                this.progressDirection = difference < 0 ? -1.0f : 0.0f;
            } else {
                this.progressDirection = difference > 0 ? 1.0f : 0.0f;
            }
            this.active = this.potionType == PotionType.BREWING_STAND
                    ? nextProgress == 0 || difference >= 0
                    : progress == 0 || difference <= 0;
        } else {
            this.progressDirection = this.potionType == PotionType.BREWING_STAND ? -1.0f : 1.0f;
            this.active = progress == 0;
        }
        if (stack != null && !stack.isEmpty()) {
            this.itemStack = stack;
        }
        this.startTimeMillis = timestamp;
        this.lastUpdateMillis = timestamp;
    }

    void updateTimestamp(long timestamp) {
        if (this.active || this.progressDirection == 0.0f) {
            this.lastUpdateMillis = timestamp;
            return;
        }
        float elapsedTicks = (float) (timestamp - this.lastUpdateMillis) / 50.0f;
        this.progressTicks += this.progressDirection * elapsedTicks;
        this.progressTicks = MathHelper.clamp(this.progressTicks, 0.0f, this.durationTicks);
        if (this.potionType == PotionType.BREWING_STAND && this.progressTicks <= 0.0f) {
            this.active = true;
        } else if (this.potionType != PotionType.BREWING_STAND
                && this.progressTicks >= this.durationTicks) {
            this.progressTicks = 0.0f;
        }
        this.lastUpdateMillis = timestamp;
    }

    float getProgressFraction() {
        if (this.durationTicks <= 0) {
            return 0.0f;
        }
        if (this.potionType == PotionType.BREWING_STAND) {
            return 1.0f - this.progressTicks / this.durationTicks;
        }
        return this.progressTicks / this.durationTicks;
    }

    long getRemainingSeconds() {
        float remaining = this.potionType == PotionType.BREWING_STAND
                ? this.progressTicks
                : this.durationTicks - this.progressTicks;
        return Math.max(0L, Math.round(remaining / 20.0f));
    }

    boolean isActive() {
        return !this.active && this.progressDirection != 0.0f;
    }
}
