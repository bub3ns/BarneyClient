package moscow.rockstar.mixin.accessors;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes Minecraft's sequencing helper to code that must preserve packet ordering. */
@Mixin(ClientPlayerInteractionManager.class)
public interface ClientPlayerInteractionManagerAccessor {
    @Mutable
    @Accessor("blockBreakingCooldown")
    int rockstar$getBlockBreakingCooldown();

    @Mutable
    @Accessor("blockBreakingCooldown")
    void rockstar$setBlockBreakingCooldown(int cooldownTicks);

    @Invoker("sendSequencedPacket")
    void rockstar$sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);
}
