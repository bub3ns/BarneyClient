/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.EndCrystalEntity
 *  net.minecraft.ItemEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Item
 *  net.minecraft.Items
 *  net.minecraft.SwordItem
 *  net.minecraft.Blocks
 *  net.minecraft.Direction
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 *  net.minecraft.BlockHitResult
 */
package moscow.rockstar.modules.combat.attacks;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.hit.BlockHitResult;
import pyrock.events.window.MouseEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Explosion", description="modules.descriptions.auto_explosion", category=ModuleCategory.COMBAT)
public class AutoExplosion
extends Module {
    private MultiBooleanSetting settingsGroup;
    private MultiBooleanSetting.Option selfTarget;
    private MultiBooleanSetting.Option friendTargets;
    private MultiBooleanSetting.Option itemTargets;
    private final Timer cooldownTimer = new Timer();
    private final Timer actionTimer = new Timer();
    private BlockPos targetBlock;
    private BlockPos previousBlock;
    private int lastAttackedEntityId = -1;
    private final EventListener<MouseEvent> onMouseEvent = mouseEvent -> {
        if (AutoExplosion.minecraftClient.player == null || AutoExplosion.minecraftClient.world == null) {
            return;
        }
        if (AutoExplosion.minecraftClient.currentScreen != null) {
            return;
        }
        if (mouseEvent.getButton() != 1 || mouseEvent.getAction() != 1) {
            return;
        }
        if (!AutoExplosion.minecraftClient.player.getMainHandStack().isEmpty() && !(AutoExplosion.minecraftClient.player.getMainHandStack().getItem() instanceof SwordItem)) {
            return;
        }
        Object object = AutoExplosion.minecraftClient.crosshairTarget;
        if (!(object instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult class_39652 = (BlockHitResult)object;
        if (!AutoExplosion.minecraftClient.world.getBlockState(class_39652.getBlockPos()).isOf(Blocks.OBSIDIAN)) {
            return;
        }
        BlockPos targetPosition = class_39652.getBlockPos().up();
        if (!AutoExplosion.minecraftClient.world.getBlockState(targetPosition).isAir()) {
            return;
        }
        if (this.isValidCondition(targetPosition.getY())) {
            return;
        }
        this.targetBlock = targetPosition.toImmutable();
        this.cooldownTimer.reset();
    };

    public AutoExplosion() {
        this.initializeTargetFilters();
    }

    @Compile(obfuscation=4)
    private void initializeTargetFilters() {
        this.settingsGroup = new MultiBooleanSetting(this, "Don't explode");
        this.selfTarget = new MultiBooleanSetting.Option(this.settingsGroup, "Self").select();
        this.friendTargets = new MultiBooleanSetting.Option(this.settingsGroup, "Friends").select();
        this.itemTargets = new MultiBooleanSetting.Option(this.settingsGroup, "Items").select();
    }

    @Override
    public void onDisable() {
        this.targetBlock = null;
        this.previousBlock = null;
    }

    @Override
    public void onTick() {
        EndCrystalEntity class_15112;
        if (AutoExplosion.minecraftClient.player == null || AutoExplosion.minecraftClient.world == null) {
            return;
        }
        ItemRuleCollection<HotbarSlot> itemRuleCollection = ItemRuleSets.getHotbarRules();
        HotbarSlot hotbarSlot = itemRuleCollection.findByItem(Items.END_CRYSTAL);
        if (hotbarSlot == null) {
            this.targetBlock = null;
            this.previousBlock = null;
            return;
        }
        if (this.targetBlock != null && this.cooldownTimer.hasElapsed(1L)) {
            if (!this.isValidCondition(this.targetBlock.getY())) {
                this.clearStateForIntAndclass2338(hotbarSlot.getClickSlot(), this.targetBlock);
                this.previousBlock = this.targetBlock;
            }
            this.targetBlock = null;
            this.cooldownTimer.reset();
        }
        if ((class_15112 = this.findPrimedTnt(this.previousBlock)) != null) {
            Vec3d VanillaChestLootTableGenerator = class_15112.getPos().add(0.0, 0.5, 0.0);
            float[] fArray = this.calculateTargetRotation(VanillaChestLootTableGenerator);
            RockstarClient.create().getRotationManager().setRotation(new Rotation(fArray[0], fArray[1]));
            this.clearStateForclass1511(class_15112);
        }
        super.onTick();
    }

    private void clearStateForIntAndclass2338(int n, BlockPos adminsky) {
        if (AutoExplosion.minecraftClient.player == null || AutoExplosion.minecraftClient.world == null) {
            return;
        }
        int n2 = n - 36;
        if (n2 < 0 || n2 > 8) {
            return;
        }
        BlockPos adminsky2 = adminsky.down();
        Vec3d VanillaChestLootTableGenerator = new Vec3d((double)adminsky2.getX() + 0.5, (double)adminsky2.getY() + 1.0, (double)adminsky2.getZ() + 0.5);
        float[] fArray = this.calculateTargetRotation(VanillaChestLootTableGenerator);
        RockstarClient.create().getRotationManager().setRotation(new Rotation(fArray[0], fArray[1]));
        int n3 = AutoExplosion.minecraftClient.player.getInventory().selectedSlot;
        AutoExplosion.minecraftClient.player.getInventory().selectedSlot = n2;
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky2, false);
        AutoExplosion.minecraftClient.interactionManager.interactBlock(AutoExplosion.minecraftClient.player, Hand.MAIN_HAND, class_39652);
        AutoExplosion.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        AutoExplosion.minecraftClient.player.getInventory().selectedSlot = n3;
        for (Entity class_12972 : AutoExplosion.minecraftClient.world.getEntities()) {
            EndCrystalEntity class_15112;
            if (!(class_12972 instanceof EndCrystalEntity) || !((class_15112 = (EndCrystalEntity)class_12972).squaredDistanceTo(VanillaChestLootTableGenerator) < 1.0)) continue;
            return;
        }
    }

    private EndCrystalEntity findPrimedTnt(BlockPos adminsky) {
        if (adminsky == null) {
            return null;
        }
        Box HorizontalFacingBlock = new Box((double)adminsky.getX(), (double)adminsky.getY(), (double)adminsky.getZ(), (double)adminsky.getX() + 1.0, (double)adminsky.getY() + 2.0, (double)adminsky.getZ() + 1.0);
        for (Entity class_12972 : AutoExplosion.minecraftClient.world.getOtherEntities(null, HorizontalFacingBlock)) {
            EndCrystalEntity class_15112;
            if (!(class_12972 instanceof EndCrystalEntity) || !(class_15112 = (EndCrystalEntity)class_12972).isAlive()) continue;
            return class_15112;
        }
        return null;
    }

    private void clearStateForclass1511(EndCrystalEntity class_15112) {
        if (!this.isValidCondition(class_15112)) {
            return;
        }
        if (!this.actionTimer.hasElapsed(80L)) {
            return;
        }
        AutoExplosion.minecraftClient.interactionManager.attackEntity((PlayerEntity)AutoExplosion.minecraftClient.player, (Entity)class_15112);
        AutoExplosion.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        this.lastAttackedEntityId = class_15112.getId();
        this.actionTimer.reset();
    }

    private boolean isValidCondition(EndCrystalEntity class_15112) {
        if (class_15112 == null || !class_15112.isAlive()) {
            return false;
        }
        if (this.isWithinRange(class_15112)) {
            return false;
        }
        if ((double)AutoExplosion.minecraftClient.player.distanceTo((Entity)class_15112) > 4.0) {
            return false;
        }
        if (this.lastAttackedEntityId == class_15112.getId() && !this.actionTimer.hasElapsed(300L)) {
            return false;
        }
        return AutoExplosion.minecraftClient.player.getAttackCooldownProgress(1.0f) >= 1.0f;
    }

    private boolean isWithinRange(EndCrystalEntity class_15112) {
        if (this.isValidCondition(class_15112.getY())) {
            return true;
        }
        if (this.friendTargets.isSelected() && this.isEntityActive(class_15112)) {
            return true;
        }
        return this.itemTargets.isSelected() && this.isValidConditionPrimary(class_15112);
    }

    private boolean isValidCondition(double d) {
        return this.selfTarget.isSelected() && d <= AutoExplosion.minecraftClient.player.getY() + 0.1;
    }

    private boolean isEntityActive(EndCrystalEntity class_15112) {
        Box HorizontalFacingBlock = class_15112.getBoundingBox().expand(6.0);
        for (PlayerEntity class_16573 : AutoExplosion.minecraftClient.world.getEntitiesByClass(PlayerEntity.class, HorizontalFacingBlock, class_16572 -> class_16572 != AutoExplosion.minecraftClient.player)) {
            if (!class_16573.isAlive() || !RockstarClient.create().getFriendListManager().containsFriend(class_16573.getName().getString())) continue;
            return true;
        }
        return false;
    }

    private boolean isValidConditionPrimary(EndCrystalEntity class_15112) {
        Box HorizontalFacingBlock = class_15112.getBoundingBox().expand(6.0);
        for (ItemEntity class_15423 : AutoExplosion.minecraftClient.world.getEntitiesByClass(ItemEntity.class, HorizontalFacingBlock, class_15422 -> true)) {
            Item class_17922;
            if (class_15423 == null || class_15423.getStack() == null || (class_17922 = class_15423.getStack().getItem()) != Items.TOTEM_OF_UNDYING && class_17922 != Items.END_CRYSTAL && class_17922 != Items.ENCHANTED_GOLDEN_APPLE && class_17922 != Items.NETHERITE_HELMET && class_17922 != Items.NETHERITE_CHESTPLATE && class_17922 != Items.NETHERITE_LEGGINGS && class_17922 != Items.NETHERITE_BOOTS && class_17922 != Items.NETHERITE_SWORD && class_17922 != Items.DIAMOND_SWORD && class_17922 != Items.ELYTRA && class_17922 != Items.TRIDENT) continue;
            return true;
        }
        return false;
    }

    private float[] calculateTargetRotation(Vec3d VanillaChestLootTableGenerator) {
        Vec3d WallPlayerSkullBlock = new Vec3d(AutoExplosion.minecraftClient.player.getX(), AutoExplosion.minecraftClient.player.getY() + (double)AutoExplosion.minecraftClient.player.getEyeHeight(AutoExplosion.minecraftClient.player.getPose()), AutoExplosion.minecraftClient.player.getZ());
        double d = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d2 = VanillaChestLootTableGenerator.y - WallPlayerSkullBlock.y;
        double d3 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = (float)Math.toDegrees(Math.atan2(d3, d)) - 90.0f;
        float f2 = (float)(-Math.toDegrees(Math.atan2(d2, d4)));
        return new float[]{AutoExplosion.minecraftClient.player.getYaw() + MathHelper.wrapDegrees((float)(f - AutoExplosion.minecraftClient.player.getYaw())), AutoExplosion.minecraftClient.player.getPitch() + MathHelper.wrapDegrees((float)(f2 - AutoExplosion.minecraftClient.player.getPitch()))};
    }
}
