/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PlayerRotationS2CPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.network.packet.s2c.play.PlayerRotationS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={PlayerRotationS2CPacket.class})
public interface PlayerRotationS2CPacketAccessor {
    @Mutable
    @Accessor(value="yRot")
    public void setYRot(float var1);

    @Mutable
    @Accessor(value="xRot")
    public void setXRot(float var1);
}

