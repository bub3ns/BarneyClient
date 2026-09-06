/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Hand
 *  net.minecraft.StatusEffects
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.AxeItem
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ShieldItem
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.RegistryKey
 *  net.minecraft.ClientPlayerEntity
 *  net.minecraft.MaceItem
 */
package moscow.rockstar.render.esp;

import lombok.Generated;
import moscow.rockstar.combat.critical.CriticalHitTiming;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.tracking.EntityPositionCache;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.modules.combat.attacks.Criticals;
import moscow.rockstar.modules.combat.targeting.BackTrack;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.modules.combat.targeting.BacktrackAccess;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationType;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.MaceItem;

public final class EntityOverlayGeometry
implements ClientAccess {
    public static HotbarSlot getAttackHotbarSlot() {
        ItemRuleCollection<HotbarSlot> itemRuleCollection = ItemRuleSets.getHotbarRules();
        RegistryKey<Enchantment> preferredEnchantment = EntityOverlayGeometry.minecraftClient.player.fallDistance > 2.0f
            ? Enchantments.WIND_BURST
            : Enchantments.BREACH;
        HotbarSlot hotbarSlot = itemRuleCollection.findByStack(itemStack -> EntityOverlayGeometry.isItemInRegistry(preferredEnchantment, itemStack));
        if (hotbarSlot == null) {
            hotbarSlot = itemRuleCollection.findByItem(Items.MACE);
        }
        return hotbarSlot;
    }

    public static Vec3d getTargetAimPoint(Entity class_12972, boolean bl) {
        Vec3d VanillaChestLootTableGenerator;
        BackTrack backTrack = RockstarClient.create().getModuleRegistry().getModule(BackTrack.class);
        if (backTrack.isEnabled() && class_12972 instanceof BacktrackAccess && (VanillaChestLootTableGenerator = backTrack.getPosition(class_12972)) != null) {
            return VanillaChestLootTableGenerator;
        }
        VanillaChestLootTableGenerator = EntityPositionCache.getTrackedPosition(class_12972);
        Vec3d WallPlayerSkullBlock = class_12972.getPos();
        return EntityOverlayGeometry.minecraftClient.player.getEyePos().distanceTo(VanillaChestLootTableGenerator) < EntityOverlayGeometry.minecraftClient.player.getEyePos().distanceTo(WallPlayerSkullBlock) && bl ? VanillaChestLootTableGenerator : WallPlayerSkullBlock;
    }

    public static Box getTargetBoundingBox(Entity class_12972, boolean bl) {
        return class_12972.getBoundingBox().offset(-class_12972.getX(), -class_12972.getY(), -class_12972.getZ()).offset(EntityOverlayGeometry.getTargetAimPoint(class_12972, bl));
    }

    public static double getTargetDistance(Entity class_12972, boolean bl) {
        return class_12972.getEyeY() - class_12972.getY() + EntityOverlayGeometry.getTargetAimPoint((Entity)class_12972, (boolean)bl).y;
    }

    public static float getEntityHeight(LivingEntity class_13092) {
        Aura aura = RockstarClient.create().getModuleRegistry().getModule(Aura.class);
        return ServerDetector.isInventoryServer() || ServerDetector.isServerProfileSupported(ServerProfile.FUNSKY) || ServerDetector.serverAddressContains("cakeworld") || ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY) || ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) ? aura.getAttackCooldownValue() : 0.0f;
    }

    public static boolean isOverlayEnabled() {
        Aura aura = RockstarClient.create().getModuleRegistry().getModule(Aura.class);
        if (EntityUtils.getBlockAtOffset(0.0, 2.0, 0.0) != Blocks.AIR && EntityUtils.getBlockAtOffset(0.0, -1.0, 0.0) != Blocks.AIR && ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY) && aura.getAttackCycle() % 5 == 0) {
            return false;
        }
        return EntityOverlayGeometry.minecraftClient.player.fallDistance > 1.2f || EntityOverlayGeometry.minecraftClient.player.fallDistance < 0.76f;
    }

    public static boolean isValidTarget(LivingEntity class_13092, boolean bl) {
        if (EntityOverlayGeometry.minecraftClient.world == null || EntityOverlayGeometry.minecraftClient.player == null) {
            return false;
        }
        Block class_22482 = EntityOverlayGeometry.minecraftClient.world.getBlockState(EntityOverlayGeometry.minecraftClient.player.getBlockPos().up(2)).getBlock();
        Aura aura = RockstarClient.create().getModuleRegistry().getModule(Aura.class);
        Criticals criticals = RockstarClient.create().getModuleRegistry().getModule(Criticals.class);
        double d = (double)((int)EntityOverlayGeometry.minecraftClient.player.getY()) - EntityOverlayGeometry.minecraftClient.player.getY();
        boolean bl2 = d == -0.01250004768371582;
        boolean bl3 = d == -0.1875;
        return EntityOverlayGeometry.minecraftClient.player.isClimbing() || EntityOverlayGeometry.minecraftClient.player.isTouchingWater() && EntityUtils.getBlockAtOffset(0.0, 1.0, 0.0) == Blocks.WATER && EntityOverlayGeometry.minecraftClient.player.fallDistance <= 0.0f || EntityOverlayGeometry.minecraftClient.player.isSwimming() || criticals.isCriticalsCooldownReady() || EntityOverlayGeometry.minecraftClient.player.isInLava() || aura.getRotationModeOption().isSelected() || EntityOverlayGeometry.minecraftClient.player.getAbilities().flying || EntityOverlayGeometry.minecraftClient.player.hasStatusEffect(StatusEffects.BLINDNESS) || EntityOverlayGeometry.minecraftClient.player.hasStatusEffect(StatusEffects.LEVITATION) || EntityOverlayGeometry.minecraftClient.player.hasStatusEffect(StatusEffects.SLOW_FALLING) || EntityOverlayGeometry.minecraftClient.player.hasVehicle() || EntityOverlayGeometry.minecraftClient.player.fallDistance > EntityOverlayGeometry.getEntityHeight(class_13092) && !EntityOverlayGeometry.minecraftClient.player.isOnGround() && EntityOverlayGeometry.isOverlayEnabled() || criticals.isCriticalsEnvironmentReady() || CriticalHitTiming.isCriticalWindowReady(EntityOverlayGeometry.minecraftClient.player, EntityOverlayGeometry.getEntityHeight(class_13092), 2);
    }

    public static boolean isValidLivingEntity(LivingEntity class_13092) {
        if (EntityOverlayGeometry.minecraftClient.world == null || EntityOverlayGeometry.minecraftClient.player == null) {
            return false;
        }
        Aura aura = RockstarClient.create().getModuleRegistry().getModule(Aura.class);
        Criticals criticals = RockstarClient.create().getModuleRegistry().getModule(Criticals.class);
        double d = (double)((int)EntityOverlayGeometry.minecraftClient.player.getY()) - EntityOverlayGeometry.minecraftClient.player.getY();
        return EntityOverlayGeometry.minecraftClient.player.isClimbing() || EntityOverlayGeometry.minecraftClient.player.isTouchingWater() && EntityUtils.getBlockAtOffset(0.0, 1.0, 0.0) == Blocks.WATER && EntityOverlayGeometry.minecraftClient.player.fallDistance <= 0.0f || EntityOverlayGeometry.minecraftClient.player.isSwimming() || criticals.isCriticalsCooldownReady() || EntityOverlayGeometry.minecraftClient.player.isInLava() || aura.getRotationModeOption().isSelected() || EntityOverlayGeometry.minecraftClient.player.getAbilities().flying || EntityOverlayGeometry.minecraftClient.player.hasStatusEffect(StatusEffects.BLINDNESS) || EntityOverlayGeometry.minecraftClient.player.hasStatusEffect(StatusEffects.LEVITATION) || EntityOverlayGeometry.minecraftClient.player.hasStatusEffect(StatusEffects.SLOW_FALLING) || EntityOverlayGeometry.minecraftClient.player.hasVehicle() || CriticalHitTiming.isCriticalWindowReady(EntityOverlayGeometry.minecraftClient.player, EntityOverlayGeometry.getEntityHeight(class_13092), 2) || criticals.isCriticalsEnvironmentReady();
    }

    public static boolean isEntityTargetable(LivingEntity class_13092) {
        if (!(class_13092 instanceof PlayerEntity)) {
            return false;
        }
        PlayerEntity class_16572 = (PlayerEntity)class_13092;
        if (!class_16572.isUsingItem()) {
            return false;
        }
        return class_16572.getActiveItem().getItem() instanceof ShieldItem;
    }

    public static boolean isEntityAlive(LivingEntity class_13092) {
        if (EntityOverlayGeometry.minecraftClient.player == null || EntityOverlayGeometry.minecraftClient.player.isDead()) {
            return false;
        }
        if (class_13092.isDead()) {
            return false;
        }
        HotbarSlot hotbarSlot = ItemRuleSets.getHotbarRules().findByStack(class_17992 -> class_17992.getItem() instanceof AxeItem);
        if (hotbarSlot == null) {
            return false;
        }
        Vec3d VanillaChestLootTableGenerator = class_13092.getRotationVector(0.0f, class_13092.getYaw());
        Vec3d WallPlayerSkullBlock = new Vec3d(EntityOverlayGeometry.minecraftClient.player.getX() - class_13092.getX(), 0.0, EntityOverlayGeometry.minecraftClient.player.getZ() - class_13092.getZ());
        double d = WallPlayerSkullBlock.length();
        if (d < 0.01) {
            return true;
        }
        return WallPlayerSkullBlock.dotProduct(VanillaChestLootTableGenerator) > 0.0;
    }

    public static boolean isEntityVisible(LivingEntity class_13092) {
        boolean bl;
        if (EntityOverlayGeometry.minecraftClient.player == null || EntityOverlayGeometry.minecraftClient.interactionManager == null) {
            return false;
        }
        if (!EntityOverlayGeometry.isEntityTargetable(class_13092)) {
            return false;
        }
        HotbarSlot hotbarSlot = ItemRuleSets.getHotbarRules().findByStack(class_17992 -> class_17992.getItem() instanceof AxeItem);
        if (hotbarSlot == null) {
            return false;
        }
        int n = EntityOverlayGeometry.minecraftClient.player.getInventory().selectedSlot;
        boolean bl2 = bl = hotbarSlot.getSlotIndex() != n;
        if (bl) {
            EntityOverlayGeometry.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(hotbarSlot.getSlotIndex()));
        }
        EntityOverlayGeometry.minecraftClient.interactionManager.attackEntity((PlayerEntity)EntityOverlayGeometry.minecraftClient.player, (Entity)class_13092);
        EntityOverlayGeometry.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        if (bl) {
            EntityOverlayGeometry.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(n));
        }
        RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.SUCCESS, Localization.translate("shieldbreaker.title"), Localization.translate("shieldbreaker.desc"));
        return true;
    }

    public static boolean isEntityWithinDistance(LivingEntity class_13092) {
        Vec3d VanillaChestLootTableGenerator = class_13092.getPos();
        Box HorizontalFacingBlock = class_13092.getBoundingBox();
        float f = 0.05f;
        return !EntityOverlayGeometry.isPointWithinRange(HorizontalFacingBlock.minX - (double)f, VanillaChestLootTableGenerator.y, HorizontalFacingBlock.minZ - (double)f) || !EntityOverlayGeometry.isPointWithinRange(HorizontalFacingBlock.maxX + (double)f, VanillaChestLootTableGenerator.y, HorizontalFacingBlock.minZ - (double)f) || !EntityOverlayGeometry.isPointWithinRange(HorizontalFacingBlock.minX - (double)f, VanillaChestLootTableGenerator.y, HorizontalFacingBlock.maxZ + (double)f) || !EntityOverlayGeometry.isPointWithinRange(HorizontalFacingBlock.maxX + (double)f, VanillaChestLootTableGenerator.y, HorizontalFacingBlock.maxZ + (double)f);
    }

    private static boolean isPointWithinRange(double d, double d2, double d3) {
        return EntityOverlayGeometry.minecraftClient.world.getBlockState(new BlockPos((int)d, (int)d2, (int)d3)).getBlock() == Blocks.AIR;
    }

    @Generated
    private EntityOverlayGeometry() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static /* synthetic */ boolean isItemInRegistry(RegistryKey class_53212, ItemStack class_17992) {
        if (!(class_17992.getItem() instanceof MaceItem)) {
            return false;
        }
        return EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)class_53212) > 0;
    }
}
