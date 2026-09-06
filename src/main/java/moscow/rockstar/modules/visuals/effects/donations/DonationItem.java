package moscow.rockstar.modules.visuals.effects.donations;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public record DonationItem(Vec3d position, ItemStack itemStack) {
    public Vec3d getPosition() {
        return position;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
