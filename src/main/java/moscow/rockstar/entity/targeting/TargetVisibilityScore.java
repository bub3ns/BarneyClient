/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 */
package moscow.rockstar.entity.targeting;

import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;

public final class TargetVisibilityScore
implements ClientAccess {
    public static float getVisibilityScore(Entity class_12972, PlayerEntity class_16572) {
        Vec3d VanillaChestLootTableGenerator = new Vec3d(class_12972.getX(), class_12972.getY(), class_12972.getZ());
        Vec3d WallPlayerSkullBlock = class_16572.getBoundingBox().getCenter();
        double d = WallPlayerSkullBlock.distanceTo(VanillaChestLootTableGenerator);
        if (d < 0.5) {
            d = 0.0;
        }
        double d2 = 1.0 - MathHelper.clamp((double)(d / 6.0), (double)0.0, (double)1.0);
        boolean bl = class_16572.getWorld().raycast(new RaycastContext(VanillaChestLootTableGenerator, WallPlayerSkullBlock, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)class_16572)).getType() != HitResult.Type.MISS;
        float f = bl ? 0.7f : 1.0f;
        return (float)((double)f * (d2 * 24.0 + 1.0));
    }

    @Generated
    private TargetVisibilityScore() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

