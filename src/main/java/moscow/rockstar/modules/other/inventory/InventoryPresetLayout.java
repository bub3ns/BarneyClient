package moscow.rockstar.modules.other.inventory;

public class InventoryPresetLayout {
    public String layoutName;
    public final InventoryPresetEntry[] slotEntries = new InventoryPresetEntry[41];
    public String description = "";

    public InventoryPresetLayout(String layoutName) {
        this.layoutName = layoutName;
    }

    public int getFilledSlotCount() {
        int count = 0;
        for (InventoryPresetEntry entry : slotEntries) {
            if (entry != null) {
                count++;
            }
        }
        return count;
    }

    public InventoryPresetLayout copyWithName(String name) {
        InventoryPresetLayout copy = new InventoryPresetLayout(name);
        for (int i = 0; i < 41; i++) {
            if (slotEntries[i] != null) {
                copy.slotEntries[i] = slotEntries[i].copy();
            }
        }
        return copy;
    }

    public void clearSlots() {
        for (int i = 0; i < 41; i++) {
            slotEntries[i] = null;
        }
    }
}
