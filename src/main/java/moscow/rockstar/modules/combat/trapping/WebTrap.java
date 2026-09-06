/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  moscow.rockstar.modules.other.admin.BlockPos$Mutable
 *  net.minecraft.Hand
 *  net.minecraft.ActionResult
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Items
 *  net.minecraft.BlockView
 *  net.minecraft.Blocks
 *  net.minecraft.Direction
 *  net.minecraft.Box
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockState
 *  net.minecraft.MathHelper
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.modules.combat.trapping;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.InventorySlotRule;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.items.rules.OffhandRule;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.attacks.Aura;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.world.BlockView;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Web Utils", category=ModuleCategory.OTHER)
public class WebTrap
extends Module {
    private MultiBooleanSetting webModeSetting;
    private MultiBooleanSetting.Option noWebOption;
    private MultiBooleanSetting.Option trapWebOption;
    private MultiBooleanSetting placementModeSetting;
    private MultiBooleanSetting.Option defaultPlacementOption;
    private MultiBooleanSetting.Option fullBodyPlacementOption;
    private MultiBooleanSetting.Option sidePlacementOption;
    private NumberSetting placementCountSetting;
    private NumberSetting placementDelaySetting;
    private NumberSetting noWebSpeedSetting;
    private final Timer placementCooldown = new Timer();
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        LivingEntity class_13092;
        if (WebTrap.minecraftClient.player == null || WebTrap.minecraftClient.world == null) {
            return;
        }
        if (this.noWebOption.isSelected()) {
            this.applyNoWebMovement();
        }
        if (this.trapWebOption.isSelected() && (class_13092 = RockstarClient.create().getFriendManager().getTargetLivingEntity()) != null && WebTrap.minecraftClient.player.getPos().distanceTo(class_13092.getPos()) <= (double)RockstarClient.create().getModuleRegistry().getModule(Aura.class).getAttackDistanceSetting().getValue()) {
            if (this.isEntityInWeb(class_13092)) {
                return;
            }
            List<BlockPos> list = this.getPlacementPositions(class_13092);
            int n = Math.min((int)this.placementCountSetting.getValue(), list.size());
            int n2 = 0;
            for (BlockPos adminsky : list) {
                if (n2 >= n) break;
                ++n2;
                PlacementTarget placementTarget = this.findWebPlacement(adminsky);
                if (placementTarget == null) continue;
                this.applyTargetRotation(placementTarget.getRotation());
                if (!this.isRotationAligned(placementTarget.getRotation()) || !this.placementCooldown.hasElapsed(this.getPlacementDelayMillis())) {
                    return;
                }
                if (this.getPlacementHitResult(placementTarget) == null) {
                    return;
                }
                WebItemSelection webItemSelection = this.selectWebItem();
                if (webItemSelection == null) {
                    return;
                }
                try {
                    if (this.placeWebAgainstTarget(webItemSelection.getWebItemRule(), placementTarget)) {
                        this.placementCooldown.reset();
                    }
                }
                finally {
                    this.restoreWebItem(webItemSelection);
                }
                return;
            }
        }
    };

    public WebTrap() {
        this.initializeTrapSettings();
    }

    @Compile(obfuscation=4)
    private void initializeTrapSettings() {
        this.webModeSetting = new MultiBooleanSetting(this, "modules.settings.web_utils");
        this.noWebOption = new MultiBooleanSetting.Option(this.webModeSetting, "modules.settings.web_utils.no_web");
        this.trapWebOption = new MultiBooleanSetting.Option(this.webModeSetting, "modules.settings.web_utils.trap_web");
        this.placementModeSetting = new MultiBooleanSetting((SettingOwner)this, "modules.settings.web_utils.placement_mode", () -> !this.trapWebOption.isSelected());
        this.defaultPlacementOption = new MultiBooleanSetting.Option(this.placementModeSetting, "modules.settings.web_utils.placement_mode.default").select();
        this.fullBodyPlacementOption = new MultiBooleanSetting.Option(this.placementModeSetting, "modules.settings.web_utils.placement_mode.full_body");
        this.sidePlacementOption = new MultiBooleanSetting.Option(this.placementModeSetting, "modules.settings.web_utils.placement_mode.sides");
        this.placementCountSetting = new NumberSetting((SettingOwner)this, "modules.settings.web_utils.count", () -> !this.trapWebOption.isSelected()).setMinValue(1.0f).setMaxValue(6.0f).setStep(1.0f).setValue(1.0f);
        this.placementDelaySetting = new NumberSetting((SettingOwner)this, "modules.settings.web_utils.delay", () -> !this.trapWebOption.isSelected()).setMinValue(100.0f).setMaxValue(1000.0f).setStep(50.0f).setValue(150.0f).setUnit("ms");
        this.noWebSpeedSetting = new NumberSetting((SettingOwner)this, "modules.settings.web_utils.no_web_speed", () -> !this.noWebOption.isSelected()).setMinValue(0.1f).setMaxValue(1.0f).setStep(0.01f).setValue(0.57f);
    }

    private void applyNoWebMovement() {
        if (WebTrap.minecraftClient.player == null || WebTrap.minecraftClient.world == null || !this.isPlayerInWeb()) {
            return;
        }
        double d = WebTrap.minecraftClient.options.jumpKey.isPressed() ? 1.3 : (WebTrap.minecraftClient.options.sneakKey.isPressed() ? -1.3 : 0.0);
        float f = WebTrap.minecraftClient.player.getYaw() * ((float)Math.PI / 180);
        float f2 = this.noWebSpeedSetting.getValue();
        float f3 = WebTrap.minecraftClient.player.forwardSpeed * f2;
        float f4 = WebTrap.minecraftClient.player.sidewaysSpeed * f2;
        if (f3 != 0.0f || f4 != 0.0f) {
            WebTrap.minecraftClient.player.setVelocity((double)(-MathHelper.sin((float)f) * f3 + MathHelper.cos((float)f) * f4), d, (double)(MathHelper.cos((float)f) * f3 + MathHelper.sin((float)f) * f4));
        } else {
            WebTrap.minecraftClient.player.setVelocity(0.0, d, 0.0);
        }
    }

    private boolean isPlayerInWeb() {
        return this.isEntityInWeb((LivingEntity)WebTrap.minecraftClient.player);
    }

    private boolean isEntityInWeb(LivingEntity class_13092) {
        Box HorizontalFacingBlock = class_13092.getBoundingBox();
        int n = MathHelper.floor((double)HorizontalFacingBlock.minX);
        int n2 = MathHelper.floor((double)HorizontalFacingBlock.minY);
        int n3 = MathHelper.floor((double)HorizontalFacingBlock.minZ);
        int n4 = MathHelper.ceil((double)HorizontalFacingBlock.maxX);
        int n5 = MathHelper.ceil((double)HorizontalFacingBlock.maxY);
        int n6 = MathHelper.ceil((double)HorizontalFacingBlock.maxZ);
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        for (int i = n; i < n4; ++i) {
            for (int j = n2; j < n5; ++j) {
                for (int k = n3; k < n6; ++k) {
                    if (!WebTrap.minecraftClient.world.getBlockState((BlockPos)class_23392.set(i, j, k)).isOf(Blocks.COBWEB)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private WebItemSelection selectWebItem() {
        int n = WebTrap.minecraftClient.player.getInventory().selectedSlot;
        OffhandRule offhandRule = ItemRuleSets.getOffhandRules().findByStack(class_17992 -> class_17992.getItem() == Items.COBWEB);
        if (offhandRule != null) {
            return new WebItemSelection(offhandRule, -1, -1);
        }
        HotbarSlot hotbarSlot = ItemRuleSets.getHotbarRules().findByStack(class_17992 -> class_17992.getItem() == Items.COBWEB);
        if (hotbarSlot != null) {
            InventoryUtils.setSelectedHotbarSlot(hotbarSlot.getSlotIndex());
            return new WebItemSelection(hotbarSlot, n, -1);
        }
        InventorySlotRule inventorySlotRule = ItemRuleSets.getInventoryRules().findByStack(class_17992 -> class_17992.getItem() == Items.COBWEB);
        if (inventorySlotRule != null) {
            InventoryUtils.dropItem(inventorySlotRule.getClickSlot(), WebTrap.minecraftClient.player.getInventory().selectedSlot);
            return new WebItemSelection(new HotbarSlot(n), n, inventorySlotRule.getClickSlot());
        }
        return null;
    }

    private void restoreWebItem(WebItemSelection webItemSelection) {
        if (WebTrap.minecraftClient.player == null || webItemSelection == null) {
            return;
        }
        if (webItemSelection.getSwappedInventorySlot() != -1) {
            InventoryUtils.dropItem(webItemSelection.getSwappedInventorySlot(), webItemSelection.getPreviousHotbarSlot());
            return;
        }
        if (webItemSelection.getPreviousHotbarSlot() != -1) {
            InventoryUtils.setSelectedHotbarSlot(webItemSelection.getPreviousHotbarSlot());
        }
    }

    private boolean placeWebAgainstTarget(ItemRule itemRule, PlacementTarget placementTarget) {
        if (WebTrap.minecraftClient.player == null || WebTrap.minecraftClient.interactionManager == null || WebTrap.minecraftClient.world == null) {
            return false;
        }
        Hand class_12682 = this.getInteractionHand(itemRule);
        if (class_12682 == null) {
            return false;
        }
        BlockHitResult class_39652 = this.getPlacementHitResult(placementTarget);
        if (class_39652 == null) {
            return false;
        }
        ActionResult class_12692 = WebTrap.minecraftClient.interactionManager.interactBlock(WebTrap.minecraftClient.player, class_12682, class_39652);
        if (!class_12692.isAccepted()) {
            return false;
        }
        WebTrap.minecraftClient.player.swingHand(class_12682);
        return true;
    }

    private Hand getInteractionHand(ItemRule itemRule) {
        if (itemRule instanceof OffhandRule) {
            return Hand.OFF_HAND;
        }
        if (itemRule instanceof HotbarSlot) {
            HotbarSlot hotbarSlot = (HotbarSlot)itemRule;
            InventoryUtils.setSelectedHotbarSlot(hotbarSlot.getSlotIndex());
            return Hand.MAIN_HAND;
        }
        return null;
    }

    private PlacementTarget findWebPlacement(BlockPos adminsky) {
        if (WebTrap.minecraftClient.player == null || WebTrap.minecraftClient.world == null || !this.isPlaceableBlock(adminsky)) {
            return null;
        }
        Vec3d VanillaChestLootTableGenerator = WebTrap.minecraftClient.player.getEyePos();
        double d = WebTrap.minecraftClient.player.getBlockInteractionRange();
        for (Direction class_23502 : Direction.values()) {
            Vec3d WallPlayerSkullBlock;
            BlockPos adminsky2 = adminsky.offset(class_23502);
            Direction class_23503 = class_23502.getOpposite();
            if (!this.isSolidSupportBlock(adminsky2) || VanillaChestLootTableGenerator.squaredDistanceTo(WallPlayerSkullBlock = this.getHitPosition(adminsky2, class_23503)) > d * d) continue;
            Rotation rotation = this.calculateRotationToTarget(VanillaChestLootTableGenerator, WallPlayerSkullBlock);
            return new PlacementTarget(adminsky, adminsky2, class_23503, rotation);
        }
        return null;
    }

    private boolean isPlaceableBlock(BlockPos adminsky) {
        BlockState class_26802 = WebTrap.minecraftClient.world.getBlockState(adminsky);
        return class_26802.isAir() || class_26802.getCollisionShape((BlockView)WebTrap.minecraftClient.world, adminsky).isEmpty() && WebTrap.minecraftClient.world.getFluidState(adminsky).isEmpty();
    }

    private boolean isSolidSupportBlock(BlockPos adminsky) {
        BlockState class_26802 = WebTrap.minecraftClient.world.getBlockState(adminsky);
        return !class_26802.isAir();
    }

    private Vec3d getHitPosition(BlockPos adminsky, Direction class_23502) {
        return adminsky.toCenterPos().add((double)class_23502.getOffsetX() * 0.5, (double)class_23502.getOffsetY() * 0.5, (double)class_23502.getOffsetZ() * 0.5);
    }

    private Rotation calculateRotationToTarget(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock) {
        double d = WallPlayerSkullBlock.x - VanillaChestLootTableGenerator.x;
        double d2 = WallPlayerSkullBlock.y - VanillaChestLootTableGenerator.y;
        double d3 = WallPlayerSkullBlock.z - VanillaChestLootTableGenerator.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        return new Rotation((float)Math.toDegrees(Math.atan2(d3, d)) - 90.0f, (float)(-Math.toDegrees(Math.atan2(d2, d4))));
    }

    private void applyTargetRotation(Rotation rotation) {
        RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, 90.0f, 90.0f, 75.0f, RotationPriority.ITEM_USE_PRIORITY);
    }

    private boolean isRotationAligned(Rotation rotation) {
        Rotation rotation2 = RockstarClient.create().getRotationManager().getCurrentRotation();
        float f = Math.abs(MathHelper.wrapDegrees((float)(rotation2.getYaw() - rotation.getYaw())));
        float f2 = Math.abs(rotation2.getPitch() - rotation.getPitch());
        return f <= 5.0f && f2 <= 5.0f;
    }

    private BlockHitResult getPlacementHitResult(PlacementTarget placementTarget) {
        Rotation rotation = RockstarClient.create().getRotationManager().getCurrentRotation();
        BlockHitResult class_39652 = this.raycastPlacementTarget(rotation);
        if (!this.isExpectedHitResult(class_39652, placementTarget)) {
            return null;
        }
        return class_39652;
    }

    private BlockHitResult raycastPlacementTarget(Rotation rotation) {
        Vec3d VanillaChestLootTableGenerator = WebTrap.minecraftClient.player.getEyePos();
        Vec3d WallPlayerSkullBlock = VanillaChestLootTableGenerator.add(WebTrap.minecraftClient.player.getRotationVector(rotation.getPitch(), rotation.getYaw()).multiply(WebTrap.minecraftClient.player.getBlockInteractionRange()));
        return WebTrap.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, WallPlayerSkullBlock, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)WebTrap.minecraftClient.player));
    }

    private boolean isExpectedHitResult(BlockHitResult class_39652, PlacementTarget placementTarget) {
        if (class_39652 == null || class_39652.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        return class_39652.getBlockPos().equals(placementTarget.getSupportBlockPosition()) && class_39652.getSide() == placementTarget.getHitSide() && class_39652.getBlockPos().offset(class_39652.getSide()).equals(placementTarget.getTargetBlockPosition()) && this.isPlaceableBlock(placementTarget.getTargetBlockPosition()) && this.isSolidSupportBlock(placementTarget.getSupportBlockPosition());
    }

    private long getPlacementDelayMillis() {
        return Math.max(100L, (long)this.placementDelaySetting.getValue());
    }

    @NotNull
    private List<BlockPos> getPlacementPositions(LivingEntity class_13092) {
        BlockPos adminsky = class_13092.getBlockPos();
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        if (this.defaultPlacementOption.isSelected()) {
            this.addUniquePlacementPosition(arrayList, adminsky);
        }
        if (this.fullBodyPlacementOption.isSelected()) {
            this.addUniquePlacementPosition(arrayList, adminsky);
            this.addUniquePlacementPosition(arrayList, adminsky.up());
        }
        if (this.sidePlacementOption.isSelected()) {
            this.addUniquePlacementPosition(arrayList, adminsky.north());
            this.addUniquePlacementPosition(arrayList, adminsky.south());
            this.addUniquePlacementPosition(arrayList, adminsky.east());
            this.addUniquePlacementPosition(arrayList, adminsky.west());
            this.addUniquePlacementPosition(arrayList, adminsky.north().up());
            this.addUniquePlacementPosition(arrayList, adminsky.south().up());
        }
        return arrayList;
    }

    private void addUniquePlacementPosition(List<BlockPos> list, BlockPos adminsky) {
        if (!list.contains(adminsky)) {
            list.add(adminsky);
        }
    }

    @Generated
    public MultiBooleanSetting.Option getNoWebOption() {
        return this.noWebOption;
    }

    static final class WebItemSelection {
        private final ItemRule webItemRule;
        private final int previousHotbarSlot;
        private final int swappedInventorySlot;

        WebItemSelection(ItemRule itemRule, int n, int n2) {
            this.webItemRule = itemRule;
            this.previousHotbarSlot = n;
            this.swappedInventorySlot = n2;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "webItemRule", "previousHotbarSlot", "swappedInventorySlot");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "webItemRule", "previousHotbarSlot", "swappedInventorySlot");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "webItemRule", "previousHotbarSlot", "swappedInventorySlot");
        }

        public ItemRule getWebItemRule() {
            return this.webItemRule;
        }

        public int getPreviousHotbarSlot() {
            return this.previousHotbarSlot;
        }

        public int getSwappedInventorySlot() {
            return this.swappedInventorySlot;
        }
    }

    static final class PlacementTarget {
        private final BlockPos targetBlockPosition;
        private final BlockPos supportBlockPosition;
        private final Direction hitSide;
        private final Rotation rotation;

        PlacementTarget(BlockPos adminsky, BlockPos adminsky2, Direction class_23502, Rotation rotation) {
            this.targetBlockPosition = adminsky;
            this.supportBlockPosition = adminsky2;
            this.hitSide = class_23502;
            this.rotation = rotation;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "targetBlockPosition", "supportBlockPosition", "hitSide", "rotation");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "targetBlockPosition", "supportBlockPosition", "hitSide", "rotation");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "targetBlockPosition", "supportBlockPosition", "hitSide", "rotation");
        }

        public BlockPos getTargetBlockPosition() {
            return this.targetBlockPosition;
        }

        public BlockPos getSupportBlockPosition() {
            return this.supportBlockPosition;
        }

        public Direction getHitSide() {
            return this.hitSide;
        }

        public Rotation getRotation() {
            return this.rotation;
        }
    }
}

