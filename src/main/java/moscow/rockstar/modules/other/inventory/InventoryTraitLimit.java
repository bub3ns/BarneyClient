package moscow.rockstar.modules.other.inventory;

/** A configured maximum for one inventory trait. */
public record InventoryTraitLimit(String traitName, int maximumCount) {
    public String getTraitName() {
        return traitName;
    }

    public int getMaximumCount() {
        return maximumCount;
    }
}
