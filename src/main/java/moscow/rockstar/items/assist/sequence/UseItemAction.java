package moscow.rockstar.items.assist.sequence;

import java.util.function.Predicate;
import moscow.rockstar.inventory.ItemSwapManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class UseItemAction implements SequenceAction {
    private final Item item;
    private final Predicate<ItemStack> matcher;
    private boolean complete;

    public UseItemAction(Item item) {
        this(item, null);
    }

    public UseItemAction(Item item, Predicate<ItemStack> matcher) {
        this.item = item;
        this.matcher = matcher;
    }

    @Override
    public void start() {
        if (this.matcher != null) {
            ItemSwapManager.getInstance().swap(this.item, this.matcher);
        } else {
            ItemSwapManager.getInstance().swap(this.item);
        }
        this.complete = true;
    }

    @Override
    public boolean isComplete() {
        return this.complete;
    }

    @Override
    public void reset() {
        this.complete = false;
    }
}
