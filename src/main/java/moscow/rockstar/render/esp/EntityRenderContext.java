/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Hand
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Slot
 *  net.minecraft.ArmorItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ItemConvertible
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.BufferBuilder
 *  net.minecraft.PlayerInteractItemC2SPacket
 *  net.minecraft.MatrixStack
 *  net.minecraft.EquipmentType
 */
package moscow.rockstar.render.esp;

import java.util.List;
import lombok.Generated;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.PlayerMovementPredictor;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.InventorySlotRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.combat.targeting.ElytraTarget;
import moscow.rockstar.render.esp.EntityOverlayGeometry;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.api.access.ArmorItemAccess;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.equipment.EquipmentType;
import pyrock.utility.render.ColorRGBA;

public final class EntityRenderContext
implements ClientAccess {
    private static Vec3d lastVisibleOffset;
    private static final Timer attackCooldownTimer;

    public static void useTargetingItem(boolean bl) {
        HotbarSlot hotbarSlot;
        ItemRuleCollection<HotbarSlot> itemRuleCollection = ItemRuleSets.getHotbarRules();
        HotbarSlot hotbarSlot2 = hotbarSlot = bl ? itemRuleCollection.findByStack(class_17992 -> {
            ArmorItem class_17382;
            Item class_17922 = class_17992.getItem();
            return class_17922 instanceof ArmorItem && ((ArmorItemAccess)(class_17382 = (ArmorItem)class_17922)).rockstar$getType() == EquipmentType.CHESTPLATE;
        }) : itemRuleCollection.findByItem(Items.ELYTRA);
        if (hotbarSlot != null) {
            HotbarSlot hotbarSlot3 = InventoryUtils.getSelectedHotbarSlot();
            EntityRenderContext.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(hotbarSlot.getSlotIndex()));
            InventoryUtils.setSelectedHotbarSlot(hotbarSlot);
            EntityRenderContext.minecraftClient.interactionManager.interactItem((PlayerEntity)EntityRenderContext.minecraftClient.player, Hand.MAIN_HAND);
            ((Slot)EntityRenderContext.minecraftClient.player.currentScreenHandler.slots.get(6)).setStack(new ItemStack((ItemConvertible)(bl ? Items.NETHERITE_CHESTPLATE : Items.ELYTRA)));
            InventoryUtils.setSelectedHotbarSlot(hotbarSlot3);
            EntityRenderContext.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(EntityRenderContext.minecraftClient.player.getInventory().selectedSlot));
        }
    }

    public static void renderBoundingBox(MatrixStack class_45872, BufferBuilder class_2872, Box HorizontalFacingBlock, ColorRGBA colorRGBA) {
        RenderUtils.drawBoxOutline(class_45872, class_2872, HorizontalFacingBlock, colorRGBA);
        RenderUtils.drawBoxCorners(class_45872, class_2872, HorizontalFacingBlock, colorRGBA);
    }

    public static void attackTargetWithItem(float f) {
        ItemRuleCollection<HotbarSlot> itemRuleCollection = ItemRuleSets.getHotbarRules();
        HotbarSlot hotbarSlot = itemRuleCollection.findByItem(Items.FIREWORK_ROCKET);
        if (hotbarSlot != null) {
            LivingEntity class_13092 = RockstarClient.create().getFriendManager().getTargetLivingEntity();
            Rotation rotation = RockstarClient.create().getRotationManager().getEffectiveRotation();
            EntityRenderContext.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(hotbarSlot.getSlotIndex()));
            ((moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)EntityRenderContext.minecraftClient.interactionManager).rockstar$sendSequencedPacket(EntityRenderContext.minecraftClient.world, n -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, n, rotation.getYaw(), rotation.getPitch()));
            EntityRenderContext.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(EntityRenderContext.minecraftClient.player.getInventory().selectedSlot));
            attackCooldownTimer.reset();
            return;
        }
        ItemRuleCollection<InventorySlotRule> itemRuleCollection2 = ItemRuleSets.getInventoryRules();
        InventorySlotRule inventorySlotRule = itemRuleCollection2.findByItem(Items.FIREWORK_ROCKET);
        if (inventorySlotRule != null) {
            InventoryUtils.dropItem(inventorySlotRule.getClickSlot(), (int)(f - 1.0f));
            attackCooldownTimer.reset();
        }
    }

    public static Vec3d calculateAimPoint(LivingEntity class_13092) {
        Vec3d VanillaChestLootTableGenerator;
        if (RockstarClient.create().getModuleRegistry().getModule(ElytraTarget.class).isEnabled() && class_13092 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_13092;
            VanillaChestLootTableGenerator = PlayerMovementPredictor.predictPlayerPosition(class_16572);
        } else {
            VanillaChestLootTableGenerator = class_13092.getPos();
        }
        return AimRotationMath.translateAimPoint(class_13092, VanillaChestLootTableGenerator);
    }

    public static Vec3d findVisibleAimOffset(LivingEntity class_13092) {
        List<Vec3d> list = List.of(new Vec3d(0.0, 20.0, 0.0), new Vec3d(0.0, -20.0, 0.0), new Vec3d(20.0, 0.0, 0.0), new Vec3d(-20.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 20.0), new Vec3d(0.0, 0.0, -20.0));
        if (EntityOverlayGeometry.getAttackHotbarSlot() != null) {
            list = List.of(new Vec3d(0.0, 20.0, 0.0));
        }
        Vec3d VanillaChestLootTableGenerator = Vec3d.ZERO;
        for (Vec3d WallPlayerSkullBlock : list) {
            if (!MathUtils.hasClearLineOfSight(class_13092.getEyePos().add(WallPlayerSkullBlock)) || WallPlayerSkullBlock.equals((Object)lastVisibleOffset)) continue;
            VanillaChestLootTableGenerator = WallPlayerSkullBlock;
            break;
        }
        return VanillaChestLootTableGenerator;
    }

    public static float[] getAdjacentYawAngles(float f) {
        float f2;
        float f3 = f - f % 360.0f;
        float f4 = f % 360.0f;
        float lowerYaw;
        float upperYaw;
        if (f4 < 0.0f) {
            f4 += 360.0f;
            f3 -= 360.0f;
        }
        if ((f2 = (float)Math.round(f4 / 45.0f) * 45.0f) % 90.0f == 0.0f) {
            float f5 = (f2 - 45.0f) % 360.0f;
            float f6 = (f2 + 45.0f) % 360.0f;
            lowerYaw = f5 < f4 ? f5 : f5 - 45.0f;
            upperYaw = f6 > f4 ? f6 : f6 + 45.0f;
        } else if (f2 < f4) {
            lowerYaw = f2;
            upperYaw = f2 + 90.0f;
        } else {
            lowerYaw = f2 - 90.0f;
            upperYaw = f2;
        }
        return new float[]{lowerYaw + f3, upperYaw + f3};
    }

    @Generated
    private EntityRenderContext() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static void setLastVisibleOffset(Vec3d VanillaChestLootTableGenerator) {
        lastVisibleOffset = VanillaChestLootTableGenerator;
    }

    @Generated
    public static Timer getAttackCooldownTimer() {
        return attackCooldownTimer;
    }

    static {
        attackCooldownTimer = new Timer();
    }
}
