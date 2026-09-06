package moscow.rockstar.modules.visuals.object;

import moscow.rockstar.ui.localization.Localization;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

enum ObjectType {
    TRAP("object_info.trap", Items.NETHERITE_SCRAP, 15000L),
    DRAGON_STANDARD("object_info.dragon", Items.NETHERITE_SCRAP, 30000L),
    DRAGON_EXTENDED("object_info.dragon", Items.NETHERITE_SCRAP, 60000L),
    BOOM_TRAP("object_info.boom_trap", Items.PRISMARINE_SHARD, 11000L),
    STUN("object_info.stan", Items.NETHER_STAR, 15000L),
    PLASTIC("object_info.plast", Items.DRIED_KELP, 20000L);

    final String translationKey;
    final Item item;
    final long lifetimeMillis;

    ObjectType(String translationKey, Item item, long lifetimeMillis) {
        this.translationKey = translationKey;
        this.item = item;
        this.lifetimeMillis = lifetimeMillis;
    }

    public String getDisplayName() {
        return Localization.translate(this.translationKey);
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public Item getItem() {
        return this.item;
    }

    public long getLifetimeMillis() {
        return this.lifetimeMillis;
    }
}
