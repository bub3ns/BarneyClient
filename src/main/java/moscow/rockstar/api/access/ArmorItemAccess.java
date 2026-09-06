package moscow.rockstar.api.access;

import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;

/** Runtime access to the constructor metadata stored by the armor-item mixin. */
public interface ArmorItemAccess {
    EquipmentType rockstar$getType();

    ArmorMaterial rockstar$getMaterial();
}
