package moscow.rockstar.modules.combat.defense.armor;

import net.minecraft.entity.EquipmentSlot;

/** Compiler-generated ordinal table kept as a named helper for the armor module. */
final class ArmorSlotSwitch {
    static final int[] VALUES;

    static {
        VALUES = new int[EquipmentSlot.values().length];
        try {
            VALUES[EquipmentSlot.FEET.ordinal()] = 1;
        } catch (NoSuchFieldError ignored) {
        }
        try {
            VALUES[EquipmentSlot.LEGS.ordinal()] = 2;
        } catch (NoSuchFieldError ignored) {
        }
        try {
            VALUES[EquipmentSlot.CHEST.ordinal()] = 3;
        } catch (NoSuchFieldError ignored) {
        }
        try {
            VALUES[EquipmentSlot.HEAD.ordinal()] = 4;
        } catch (NoSuchFieldError ignored) {
        }
    }

    private ArmorSlotSwitch() {
    }
}
