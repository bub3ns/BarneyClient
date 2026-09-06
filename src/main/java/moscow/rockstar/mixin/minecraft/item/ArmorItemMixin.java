/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ArmorItem
 *  net.minecraft.ArmorMaterial
 *  net.minecraft.Item$Settings
 *  net.minecraft.EquipmentType
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.item;

import moscow.rockstar.api.access.ArmorItemAccess;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.EquipmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ArmorItem.class})
public abstract class ArmorItemMixin
implements ArmorItemAccess {
    @Unique
    private EquipmentType rockstar$type;
    @Unique
    private ArmorMaterial rockstar$material;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    public void saveArgs(ArmorMaterial class_17412, EquipmentType class_80512, Item.Settings class_17932, CallbackInfo callbackInfo) {
        this.rockstar$type = class_80512;
        this.rockstar$material = class_17412;
    }

    public ArmorMaterial rockstar$getMaterial() {
        return this.rockstar$material;
    }

    public EquipmentType rockstar$getType() {
        return this.rockstar$type;
    }
}
