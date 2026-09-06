/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.Items
 *  net.minecraft.Blocks
 *  net.minecraft.Direction
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.PlayerInteractBlockC2SPacket
 *  net.minecraft.PlayerInteractItemC2SPacket
 *  net.minecraft.BlockHitResult
 */
package moscow.rockstar.modules.player.automation.server;

import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import pyrock.events.player.ClientPlayerTickEvent;

@ModuleInfo(name="Clan Upgrade", category=ModuleCategory.PLAYER)
public class ClanUpgrade
extends Module {
    private final Timer placementCooldown = new Timer();
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        ItemRuleCollection<ItemRule> itemRuleCollection = ItemRuleSets.getOffhandRules().combineRules(ItemRuleSets.getHotbarRules()).combineRules(ItemRuleSets.getInventoryRules());
        ItemRule itemRule = itemRuleCollection.findByItem(Items.TORCH);
        ItemRule itemRule2 = itemRuleCollection.findByItem(Items.REDSTONE);
        if (ClanUpgrade.minecraftClient.player.getMainHandStack().getItem() != Items.TORCH && itemRule != null && itemRule2 == null) {
            if (itemRule instanceof HotbarSlot) {
                HotbarSlot selectedHotbarSlot = (HotbarSlot)itemRule;
                if (InventoryUtils.getSelectedHotbarSlot().getItem() != Items.TORCH) {
                    InventoryUtils.setSelectedHotbarSlot(selectedHotbarSlot);
                }
            }
        } else if (ClanUpgrade.minecraftClient.player.getMainHandStack().getItem() != Items.REDSTONE && itemRule == null && itemRule2 != null) {
            if (itemRule2 instanceof HotbarSlot) {
                HotbarSlot selectedHotbarSlot = (HotbarSlot)itemRule2;
                if (InventoryUtils.getSelectedHotbarSlot().getItem() != Items.REDSTONE) {
                    InventoryUtils.setSelectedHotbarSlot(selectedHotbarSlot);
                }
            }
        } else if (itemRule != null && itemRule2 != null) {
            if (itemRule instanceof HotbarSlot) {
                HotbarSlot selectedHotbarSlot = (HotbarSlot)itemRule;
                if (InventoryUtils.getSelectedHotbarSlot().getItem() != Items.TORCH) {
                    InventoryUtils.setSelectedHotbarSlot(selectedHotbarSlot);
                }
            }
        } else if (itemRule == null && itemRule2 == null) {
            return;
        }
        BlockPos playerPosition = ClanUpgrade.minecraftClient.player.getBlockPos();
        RockstarClient.create().getRotationManager().requestRotation(new Rotation(ClanUpgrade.minecraftClient.player.getYaw(), 88.0f), RotationCorrectionMode.DIRECT, 180.0f, 80.0f, 80.0f, RotationPriority.STANDARD_PRIORITY);
        if (ClanUpgrade.minecraftClient.player.getMainHandStack().getItem() == Items.TORCH && ClanUpgrade.minecraftClient.world.getBlockState(playerPosition).getBlock() != Blocks.TORCH || ClanUpgrade.minecraftClient.player.getMainHandStack().getItem() == Items.REDSTONE && ClanUpgrade.minecraftClient.world.getBlockState(playerPosition).getBlock() != Blocks.REDSTONE_WIRE && ClanUpgrade.minecraftClient.player.isOnGround() && this.placementCooldown.hasElapsed(50L) && ClanUpgrade.minecraftClient.world.getBlockState(playerPosition.down()).isSolid()) {
            ((ClientPlayerInteractionManagerAccessor)(Object)ClanUpgrade.minecraftClient.interactionManager).rockstar$sendSequencedPacket(ClanUpgrade.minecraftClient.world, sequence -> ClanUpgrade.createBlockPlacementPacket(playerPosition, sequence));
            ClanUpgrade.minecraftClient.player.swingHand(Hand.MAIN_HAND);
            this.placementCooldown.reset();
        }
        if ((ClanUpgrade.minecraftClient.world.getBlockState(playerPosition).getBlock() == Blocks.TORCH || ClanUpgrade.minecraftClient.world.getBlockState(playerPosition).getBlock() == Blocks.REDSTONE_WIRE) && this.placementCooldown.hasElapsed(50L)) {
            Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)playerPosition.down()).subtract(ClanUpgrade.minecraftClient.player.getEyePos());
            double d = Math.sqrt(VanillaChestLootTableGenerator.x * VanillaChestLootTableGenerator.x + VanillaChestLootTableGenerator.z * VanillaChestLootTableGenerator.z);
            float f = (float)Math.toDegrees(Math.atan2(VanillaChestLootTableGenerator.z, VanillaChestLootTableGenerator.x)) - 90.0f + MathUtils.interpolateRandomDouble(-2.0, 2.0);
            float f2 = (float)(-Math.toDegrees(Math.atan2(VanillaChestLootTableGenerator.y, d))) + MathUtils.interpolateRandomDouble(-1.0, 1.0);
            ClanUpgrade.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, f, f2));
            ClanUpgrade.minecraftClient.interactionManager.updateBlockBreakingProgress(playerPosition, Direction.UP);
            ClanUpgrade.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        }
    };

    private static /* synthetic */ Packet createBlockPlacementPacket(BlockPos adminsky, int n) {
        return new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter((Vec3i)ClanUpgrade.minecraftClient.player.getBlockPos()), Direction.UP, adminsky.down(), false), n);
    }
}
