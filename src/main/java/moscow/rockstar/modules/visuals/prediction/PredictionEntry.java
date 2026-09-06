package moscow.rockstar.modules.visuals.prediction;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

final class PredictionEntry {
    public final int slot;
    private final ItemStack stack;
    private final String sourceName;
    private Entity targetEntity;
    private String displayName;
    private Vec3d position;
    private long timestamp;

    PredictionEntry(int slot, ItemStack stack, String sourceName, Entity targetEntity,
                    Vec3d position, long timestamp) {
        this.slot = slot;
        this.stack = stack;
        this.sourceName = sourceName;
        this.targetEntity = targetEntity;
        this.displayName = targetEntity.getName().getString();
        this.position = position;
        this.timestamp = timestamp;
    }

    void recordPrediction(Entity targetEntity, Vec3d position, long timestamp) {
        this.targetEntity = targetEntity;
        this.displayName = targetEntity.getName().getString();
        this.position = position;
        this.timestamp = timestamp;
    }

    boolean isActive(long timestamp) {
        return timestamp > this.timestamp;
    }

    Vec3d getPosition() {
        if (this.targetEntity != null && this.targetEntity.isAlive()) {
            return this.targetEntity.getBoundingBox().getCenter()
                    .add(0.0, this.targetEntity.getHeight() * 0.5, 0.0);
        }
        return this.position;
    }

    ItemStack getStack() {
        return this.stack;
    }

    String getDisplayLabel() {
        return this.sourceName + " -> " + this.displayName;
    }
}
