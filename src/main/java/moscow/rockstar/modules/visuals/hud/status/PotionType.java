package moscow.rockstar.modules.visuals.hud.status;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

enum PotionType {
    FURNACE(200, Items.FURNACE),
    BLAST_FURNACE(100, Items.BLAST_FURNACE),
    SMOKER(100, Items.SMOKER),
    BREWING_STAND(400, Items.BREWING_STAND);

    final int durationTicks;
    final Item item;

    PotionType(int durationTicks, Item item) {
        this.durationTicks = durationTicks;
        this.item = item;
    }

    Item getItem() {
        return this.item;
    }
}
