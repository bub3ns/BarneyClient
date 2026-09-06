/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Vec3d
 */
package pyrock.events.game;

import java.util.Collections;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.events.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AncientDebrisEvent
extends Event {
    private final List<BlockPos> positions;
    private final Vec3d explosionCenter;

    public AncientDebrisEvent(List<BlockPos> list, Vec3d VanillaChestLootTableGenerator) {
        this.positions = Collections.unmodifiableList(list);
        this.explosionCenter = VanillaChestLootTableGenerator;
    }

    @Generated
    public List<BlockPos> getPositions() {
        return this.positions;
    }

    @Generated
    public Vec3d getExplosionCenter() {
        return this.explosionCenter;
    }
}

