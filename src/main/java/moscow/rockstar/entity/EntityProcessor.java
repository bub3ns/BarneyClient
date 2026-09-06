/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.entity;

import java.util.Comparator;
import java.util.function.Function;
import moscow.rockstar.core.ClientAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class EntityProcessor
implements ClientAccess {
    public static final Comparator<Entity> DISTANCE_COMPARATOR = Comparator.comparingDouble(class_12972 -> class_12972.distanceTo((Entity)EntityProcessor.minecraftClient.player));
    public static final Comparator<Entity> HEALTH_COMPARATOR = Comparator.comparingDouble(class_12972 -> {
        double d;
        if (class_12972 instanceof LivingEntity) {
            LivingEntity class_13092 = (LivingEntity)class_12972;
            d = class_13092.getHealth();
        } else {
            d = 0.0;
        }
        return d;
    });
    public static final Comparator<Entity> FIELD_OF_VIEW_COMPARATOR = Comparator.comparingDouble(class_12972 -> {
        if (EntityProcessor.minecraftClient.player == null) {
            return Double.MAX_VALUE;
        }
        Vec3d VanillaChestLootTableGenerator = EntityProcessor.minecraftClient.player.getPos();
        Vec3d WallPlayerSkullBlock = class_12972.getPos();
        Vec3d VanillaEntityLootTableGenerator = EntityProcessor.minecraftClient.player.getRotationVec(1.0f);
        Vec3d PlayerSkullBlock = WallPlayerSkullBlock.subtract(VanillaChestLootTableGenerator).normalize();
        double d = VanillaEntityLootTableGenerator.dotProduct(PlayerSkullBlock);
        return Math.acos(MathHelper.clamp((double)d, (double)-1.0, (double)1.0)) * 57.29577951308232;
    });
    public static final Comparator<Entity> ARMOR_VALUE_COMPARATOR = Comparator.comparingDouble(class_12972 -> {
        if (!(class_12972 instanceof PlayerEntity)) {
            return Double.MAX_VALUE;
        }
        PlayerEntity class_16572 = (PlayerEntity)class_12972;
        double d = 0.0;
        for (ItemStack class_17992 : class_16572.getAllArmorItems()) {
            if (class_17992 == null || class_17992.isEmpty()) continue;
            d += (double)class_17992.getItem().getDefaultStack().getCount();
        }
        return d;
    });
    public static final Comparator<Entity> ARMOR_VALUE_DESCENDING_COMPARATOR = ARMOR_VALUE_COMPARATOR.reversed();

    public static Comparator<Entity> createAscendingComparator(Function<Entity, Double> function) {
        return Comparator.comparingDouble(function::apply);
    }

    public static Comparator<Entity> createDescendingComparator(Function<Entity, Double> function) {
        return Comparator.comparingDouble(function::apply).reversed();
    }
}

