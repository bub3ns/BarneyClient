/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Difficulty
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.BlockView
 *  net.minecraft.World
 *  net.minecraft.Blocks
 *  net.minecraft.Direction
 *  net.minecraft.Box
 *  net.minecraft.Vec3i
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.BlockState
 *  net.minecraft.Property
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.ShapeContext
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  net.minecraft.RespawnAnchorBlock
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.modules.combat.attacks;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.attacks.anchor.AnchorCandidate;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationType;
import moscow.rockstar.util.Timer;
import net.minecraft.world.Difficulty;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.block.ShapeContext;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.block.RespawnAnchorBlock;
import org.jetbrains.annotations.Nullable;
import pyrock.events.network.SendPacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Anchor", category=ModuleCategory.COMBAT, description="modules.descriptions.auto_anchor")
public class AutoAnchor
extends Module {
    private BooleanSetting place;
    private NumberSetting minDamage;
    private NumberSetting maxSelfDamage;
    private static final float SEARCH_RADIUS = 4.5f;
    private static final float MIN_DAMAGE = 0.5f;
    private static final float MAX_SELF_DAMAGE = 5.0f;
    private static final double MAX_TARGET_DISTANCE = 5.0;
    private static final int MAX_ANCHOR_CHARGE = 4;
    private static final float MAX_ROTATION_ANGLE = 180.0f;
    private static final long ANCHOR_TIMEOUT = 4000L;
    private static final long PLACEMENT_DELAY = 25L;
    private static final long PLACEMENT_INTERVAL = 1L;
    private static final long SEARCH_TIMEOUT = 5000L;
    private final Map<BlockPos, AnchorCandidate> anchorCandidates = new ConcurrentHashMap<BlockPos, AnchorCandidate>();
    private final Timer cooldownTimer = new Timer();
    private final Timer actionTimer = new Timer();
    private final Timer targetTimer = new Timer();
    private int previousHotbarSlot = Integer.MIN_VALUE;
    private final EventListener<SendPacketEvent> onSendPacketEvent = sendPacketEvent -> {
        if (!this.isEnabled()) {
            return;
        }
        if (sendPacketEvent.isCancelled()) {
            return;
        }
        Packet<?> class_25962 = sendPacketEvent.getPacket();
        if (!(class_25962 instanceof UpdateSelectedSlotC2SPacket)) {
            return;
        }
        UpdateSelectedSlotC2SPacket class_28682 = (UpdateSelectedSlotC2SPacket)class_25962;
        this.previousHotbarSlot = class_28682.getSelectedSlot();
    };
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        if (AutoAnchor.minecraftClient.player == null || AutoAnchor.minecraftClient.world == null || AutoAnchor.minecraftClient.interactionManager == null || minecraftClient.getNetworkHandler() == null) {
            return;
        }
        if (RespawnAnchorBlock.isNether((World)AutoAnchor.minecraftClient.world)) {
            return;
        }
        this.findAnchor();
        BlockPos adminsky = this.getBestAnchor();
        if (adminsky != null) {
            this.evaluateAnchorBlock(adminsky);
            return;
        }
        if (this.place.isEnabled()) {
            this.resetAnchorSearch();
        }
    };

    public AutoAnchor() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.place = new BooleanSetting(this, "modules.settings.auto_anchor.place").setActiveExtra(true);
        this.minDamage = new NumberSetting((SettingOwner)this, "modules.settings.auto_anchor.min_damage", "modules.settings.auto_anchor.min_damage.desc").setMinValue(1.0f).setMaxValue(20.0f).setStep(0.5f).setValue(4.0f);
        this.maxSelfDamage = new NumberSetting((SettingOwner)this, "modules.settings.auto_anchor.max_self_damage", "modules.settings.auto_anchor.max_self_damage.desc").setMinValue(0.0f).setMaxValue(36.0f).setStep(0.5f).setValue(12.0f);
    }

    @Override
    public void onEnable() {
        this.previousHotbarSlot = AutoAnchor.minecraftClient.player != null ? AutoAnchor.minecraftClient.player.getInventory().selectedSlot : Integer.MIN_VALUE;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.anchorCandidates.clear();
        this.previousHotbarSlot = Integer.MIN_VALUE;
    }

    private void findAnchor() {
        long l = System.currentTimeMillis();
        Iterator<Map.Entry<BlockPos, AnchorCandidate>> iterator = this.anchorCandidates.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, AnchorCandidate> entry = iterator.next();
            if (l - entry.getValue().createdAtMillis > 4000L) {
                iterator.remove();
                continue;
            }
            if (AutoAnchor.minecraftClient.world.getBlockState(entry.getKey()).getBlock() instanceof RespawnAnchorBlock || l - entry.getValue().createdAtMillis <= 500L) continue;
            iterator.remove();
        }
    }

    private void evaluateAnchorBlock(BlockPos adminsky2) {
        BlockState class_26802 = AutoAnchor.minecraftClient.world.getBlockState(adminsky2);
        if (!(class_26802.getBlock() instanceof RespawnAnchorBlock)) {
            return;
        }
        AnchorCandidate anchorCandidate = this.anchorCandidates.computeIfAbsent(adminsky2.toImmutable(), adminsky -> new AnchorCandidate());
        int n = Math.max((Integer)class_26802.get((Property)RespawnAnchorBlock.CHARGES), anchorCandidate.chargeCount);
        boolean bl = n >= 4;
        int n2 = this.getItemSlot(Items.GLOWSTONE);
        boolean bl2 = AutoAnchor.minecraftClient.player.getMainHandStack().isOf(Items.GLOWSTONE);
        boolean bl3 = AutoAnchor.minecraftClient.player.getOffHandStack().isOf(Items.GLOWSTONE);
        if (!(bl || bl2 || bl3 || n2 != -1)) {
            this.notifyPlacement("auto_anchor.no_glowstone", Items.GLOWSTONE.getName().getString());
            return;
        }
        if (!this.cooldownTimer.hasElapsed(25L)) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = new Vec3d((double)adminsky2.getX() + 0.5, (double)adminsky2.getY() + 1.0, (double)adminsky2.getZ() + 0.5);
        if (AutoAnchor.minecraftClient.player.getEyePos().distanceTo(VanillaChestLootTableGenerator) > 4.5) {
            return;
        }
        if (!this.isPlacementValid(VanillaChestLootTableGenerator)) {
            return;
        }
        this.renderAnchorDamage(VanillaChestLootTableGenerator);
        Hand class_12682 = Hand.MAIN_HAND;
        int n3 = AutoAnchor.minecraftClient.player.getInventory().selectedSlot;
        boolean bl4 = false;
        if (bl2) {
            class_12682 = Hand.MAIN_HAND;
        } else if (bl3) {
            class_12682 = Hand.OFF_HAND;
        } else if (n2 != -1 && !bl) {
            AutoAnchor.minecraftClient.player.getInventory().selectedSlot = n2;
            this.handlePlacementResult(n2);
            bl4 = true;
        }
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky2, false);
        AutoAnchor.minecraftClient.interactionManager.interactBlock(AutoAnchor.minecraftClient.player, class_12682, class_39652);
        AutoAnchor.minecraftClient.player.swingHand(class_12682);
        if (!bl) {
            ++anchorCandidate.chargeCount;
        }
        anchorCandidate.createdAtMillis = System.currentTimeMillis();
        this.cooldownTimer.reset();
        if (bl4) {
            AutoAnchor.minecraftClient.player.getInventory().selectedSlot = n3;
            this.handlePlacementResult(n3);
        }
    }

    @Nullable
    private BlockPos getBestAnchor() {
        BlockPos adminsky = AutoAnchor.minecraftClient.player.getBlockPos();
        int n = (int)Math.ceil(4.5);
        float f = -1.0f;
        double d = Double.MAX_VALUE;
        BlockPos adminsky2 = null;
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    Vec3d VanillaChestLootTableGenerator;
                    BlockPos adminsky3 = adminsky.add(i, j, k);
                    if (!(AutoAnchor.minecraftClient.world.getBlockState(adminsky3).getBlock() instanceof RespawnAnchorBlock)) continue;
                    Vec3d WallPlayerSkullBlock = new Vec3d((double)adminsky3.getX() + 0.5, (double)adminsky3.getY() + 1.0, (double)adminsky3.getZ() + 0.5);
                    if (AutoAnchor.minecraftClient.player.getEyePos().distanceTo(WallPlayerSkullBlock) > 4.5 || !this.isPlacementValid(WallPlayerSkullBlock) || !this.isAnchorActive(adminsky3) || this.isAnchorLoaded(adminsky3) || !this.isEntityAlive(VanillaChestLootTableGenerator = adminsky3.toCenterPos())) continue;
                    float f2 = this.calculateAnchorDamage(VanillaChestLootTableGenerator);
                    if (!this.isAnchorPositionValid(VanillaChestLootTableGenerator)) continue;
                    double d2 = AutoAnchor.minecraftClient.player.squaredDistanceTo(VanillaChestLootTableGenerator);
                    if (!(f2 > f) && (f2 != f || !(d2 < d))) continue;
                    f = f2;
                    d = d2;
                    adminsky2 = adminsky3.toImmutable();
                }
            }
        }
        return adminsky2;
    }

    private void resetAnchorSearch() {
        Hand class_12682;
        boolean bl;
        int n = this.getItemSlot(Items.RESPAWN_ANCHOR);
        boolean bl2 = bl = AutoAnchor.minecraftClient.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR) || AutoAnchor.minecraftClient.player.getOffHandStack().isOf(Items.RESPAWN_ANCHOR);
        if (n == -1 && !bl) {
            this.notifyPlacement("auto_anchor.no_anchor", Items.RESPAWN_ANCHOR.getName().getString());
            return;
        }
        if (this.getItemCount(Items.GLOWSTONE) < 4) {
            this.notifyPlacement("auto_anchor.no_glowstone", Items.GLOWSTONE.getName().getString());
            return;
        }
        BlockPos adminsky = this.getPreviousAnchor();
        if (adminsky == null) {
            return;
        }
        if (!this.actionTimer.hasElapsed(1L)) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = new Vec3d((double)adminsky.getX() + 0.5, (double)adminsky.getY() + 1.0, (double)adminsky.getZ() + 0.5);
        this.renderAnchorDamage(VanillaChestLootTableGenerator);
        int n2 = AutoAnchor.minecraftClient.player.getInventory().selectedSlot;
        boolean bl3 = false;
        Hand class_12683 = class_12682 = AutoAnchor.minecraftClient.player.getOffHandStack().isOf(Items.RESPAWN_ANCHOR) ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (class_12682 == Hand.MAIN_HAND && !AutoAnchor.minecraftClient.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR)) {
            AutoAnchor.minecraftClient.player.getInventory().selectedSlot = n;
            this.handlePlacementResult(n);
            bl3 = true;
        }
        BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky, false);
        AutoAnchor.minecraftClient.interactionManager.interactBlock(AutoAnchor.minecraftClient.player, class_12682, class_39652);
        AutoAnchor.minecraftClient.player.swingHand(class_12682);
        this.anchorCandidates.put(adminsky.up().toImmutable(), new AnchorCandidate());
        this.actionTimer.reset();
        if (bl3) {
            AutoAnchor.minecraftClient.player.getInventory().selectedSlot = n2;
            this.handlePlacementResult(n2);
        }
    }

    @Nullable
    private BlockPos getPreviousAnchor() {
        BlockPos adminsky = AutoAnchor.minecraftClient.player.getBlockPos();
        int n = (int)Math.ceil(4.5);
        BlockState class_26802 = Blocks.RESPAWN_ANCHOR.getDefaultState();
        float f = -1.0f;
        double d = Double.MAX_VALUE;
        BlockPos adminsky2 = null;
        for (int i = -n; i <= n; ++i) {
            for (int j = -4; j <= 2; ++j) {
                for (int k = -n; k <= n; ++k) {
                    Vec3d VanillaChestLootTableGenerator;
                    BlockPos adminsky3 = adminsky.add(i, j, k);
                    BlockPos adminsky4 = adminsky3.up();
                    if (!AutoAnchor.minecraftClient.world.getBlockState(adminsky3).isSolidBlock((BlockView)AutoAnchor.minecraftClient.world, adminsky3) || !AutoAnchor.minecraftClient.world.getBlockState(adminsky4).isReplaceable() || !AutoAnchor.minecraftClient.world.canPlace(class_26802, adminsky4, ShapeContext.absent())) continue;
                    Vec3d WallPlayerSkullBlock = new Vec3d((double)adminsky3.getX() + 0.5, (double)adminsky3.getY() + 1.0, (double)adminsky3.getZ() + 0.5);
                    if (AutoAnchor.minecraftClient.player.getEyePos().distanceTo(WallPlayerSkullBlock) > 4.5 || !this.isPlacementValid(WallPlayerSkullBlock) || !this.isAnchorActive(adminsky4) || this.isAnchorLoaded(adminsky4) || !this.isEntityAlive(VanillaChestLootTableGenerator = adminsky4.toCenterPos()) || !this.isAnchorPositionValid(VanillaChestLootTableGenerator)) continue;
                    float f2 = this.calculateAnchorDamage(VanillaChestLootTableGenerator);
                    double d2 = AutoAnchor.minecraftClient.player.squaredDistanceTo(VanillaChestLootTableGenerator);
                    if (!(f2 > f) && (f2 != f || !(d2 < d))) continue;
                    f = f2;
                    d = d2;
                    adminsky2 = adminsky3.toImmutable();
                }
            }
        }
        return adminsky2;
    }

    private boolean isAnchorPositionValid(Vec3d VanillaChestLootTableGenerator) {
        float f = this.minDamage.getValue();
        LivingEntity class_13092 = RockstarClient.create().getFriendManager().getTargetLivingEntity();
        if (class_13092 != null && class_13092.isAlive() && class_13092 != AutoAnchor.minecraftClient.player) {
            if (class_13092 instanceof PlayerEntity) {
                PlayerEntity class_16572 = (PlayerEntity)class_13092;
                if (RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString())) {
                    return this.calculateAnchorDamage(VanillaChestLootTableGenerator) >= f;
                }
            }
            return this.calculateEntityDamage(VanillaChestLootTableGenerator, class_13092) >= f;
        }
        return this.calculateAnchorDamage(VanillaChestLootTableGenerator) >= f;
    }

    private boolean isEntityAlive(Vec3d VanillaChestLootTableGenerator) {
        float f = this.calculateEntityDamage(VanillaChestLootTableGenerator, (LivingEntity)AutoAnchor.minecraftClient.player);
        if (f > this.maxSelfDamage.getValue()) {
            return false;
        }
        return f < AutoAnchor.minecraftClient.player.getHealth() + AutoAnchor.minecraftClient.player.getAbsorptionAmount();
    }

    private float calculateAnchorDamage(Vec3d VanillaChestLootTableGenerator) {
        float f = 0.0f;
        for (PlayerEntity class_16572 : AutoAnchor.minecraftClient.world.getPlayers()) {
            if (class_16572 == AutoAnchor.minecraftClient.player || !class_16572.isAlive() || RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString())) continue;
            f = Math.max(f, this.calculateEntityDamage(VanillaChestLootTableGenerator, (LivingEntity)class_16572));
        }
        return f;
    }

    private float calculateEntityDamage(Vec3d VanillaChestLootTableGenerator, LivingEntity class_13092) {
        Vec3d WallPlayerSkullBlock = class_13092.getBoundingBox().getCenter();
        double d = WallPlayerSkullBlock.distanceTo(VanillaChestLootTableGenerator);
        if (d > 5.0) {
            return 0.0f;
        }
        double d2 = this.isLineOfSightValid(VanillaChestLootTableGenerator, WallPlayerSkullBlock) ? 1.0 : 0.35;
        double d3 = (1.0 - d / 5.0) * d2;
        float f = (float)((d3 * d3 + d3) / 2.0 * 7.0 * 10.0 + 1.0);
        Difficulty class_12672 = AutoAnchor.minecraftClient.world.getDifficulty();
        f *= (switch (class_12672) {
            case Difficulty.PEACEFUL -> 0.0f;
            case Difficulty.EASY -> 0.5f;
            case Difficulty.HARD -> 1.5f;
            default -> 1.0f;
        });
        float f2 = class_13092.getArmor();
        return Math.max(0.0f, f *= 1.0f - Math.min(f2 / (f2 + 20.0f), 0.8f));
    }

    private boolean isAnchorActive(BlockPos adminsky) {
        return (double)adminsky.getY() + 0.5 - AutoAnchor.minecraftClient.player.getY() >= 0.5;
    }

    private boolean isAnchorLoaded(BlockPos adminsky) {
        Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)adminsky);
        Box HorizontalFacingBlock = new Box(VanillaChestLootTableGenerator, VanillaChestLootTableGenerator).expand(5.0);
        for (PlayerEntity class_16573 : AutoAnchor.minecraftClient.world.getEntitiesByClass(PlayerEntity.class, HorizontalFacingBlock, class_16572 -> class_16572 != AutoAnchor.minecraftClient.player)) {
            if (!class_16573.isAlive() || !RockstarClient.create().getFriendListManager().containsFriend(class_16573.getName().getString())) continue;
            return true;
        }
        return false;
    }

    private boolean isLineOfSightValid(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock) {
        return AutoAnchor.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, WallPlayerSkullBlock, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)AutoAnchor.minecraftClient.player)).getType() == HitResult.Type.MISS;
    }

    private boolean isPlacementValid(Vec3d VanillaChestLootTableGenerator) {
        BlockHitResult class_39652 = AutoAnchor.minecraftClient.world.raycast(new RaycastContext(AutoAnchor.minecraftClient.player.getEyePos(), VanillaChestLootTableGenerator, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)AutoAnchor.minecraftClient.player));
        return class_39652.getType() == HitResult.Type.MISS;
    }

    private int getItemSlot(Item class_17922) {
        for (int i = 0; i < 9; ++i) {
            if (!AutoAnchor.minecraftClient.player.getInventory().getStack(i).isOf(class_17922)) continue;
            return i;
        }
        return -1;
    }

    private int getItemCount(Item class_17922) {
        int n = 0;
        for (int i = 0; i < 9; ++i) {
            ItemStack class_17992 = AutoAnchor.minecraftClient.player.getInventory().getStack(i);
            if (!class_17992.isOf(class_17922)) continue;
            n += class_17992.getCount();
        }
        ItemStack class_17993 = AutoAnchor.minecraftClient.player.getOffHandStack();
        if (class_17993.isOf(class_17922)) {
            n += class_17993.getCount();
        }
        return n;
    }

    private void notifyPlacement(String string, String string2) {
        if (!this.targetTimer.hasElapsed(5000L)) {
            return;
        }
        this.targetTimer.reset();
        RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.ERROR, Localization.translate(string), Localization.translateFormatted("auto_anchor.need_item", string2));
    }

    private void handlePlacementResult(int n) {
        if (minecraftClient.getNetworkHandler() == null || n < 0 || n > 8) {
            return;
        }
        if (n == this.previousHotbarSlot) {
            return;
        }
        minecraftClient.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(n));
    }

    private void renderAnchorDamage(Vec3d VanillaChestLootTableGenerator) {
        Rotation rotation = AimRotationMath.getRotationToPoint(VanillaChestLootTableGenerator);
        RockstarClient.create().getRotationManager().requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, 180.0f, 180.0f, 180.0f, RotationPriority.MAXIMUM_PRIORITY);
    }
}

