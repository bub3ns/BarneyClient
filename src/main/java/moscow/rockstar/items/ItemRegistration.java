/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 */
package moscow.rockstar.items;

import java.util.function.Predicate;
import moscow.rockstar.util.scheduling.RegistrationLifecycle;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemRegistration
implements RegistrationLifecycle {
    private final Item item;
    private final Predicate<ItemStack> itemPredicate;
    private boolean registered = false;

    public ItemRegistration(Item class_17922, Predicate<ItemStack> predicate) {
        this.item = class_17922;
        this.itemPredicate = predicate;
    }

    public ItemRegistration(Item class_17922) {
        this(class_17922, null);
    }

    @Override
    public void register() {
        this.registered = true;
    }

    @Override
    public boolean isRegistered() {
        return this.registered;
    }

    @Override
    public void unregister() {
        this.registered = false;
    }
}
