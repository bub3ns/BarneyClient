/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Hand
 *  net.minecraft.ActionResult$Success
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Slot
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ItemConvertible
 *  net.minecraft.Blocks
 *  net.minecraft.Direction
 *  net.minecraft.Position
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.PlayerPositionLookS2CPacket
 *  net.minecraft.ClientCommandC2SPacket
 *  net.minecraft.ClientCommandC2SPacket$Mode
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.PlayerInteractBlockC2SPacket
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  net.minecraft.ClientPlayerEntity
 */
package moscow.rockstar.modules.movement.speed;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.tracking.EntityPositionCache;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.inventory.armor.ArmorSwapController;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.modules.combat.targeting.TargetMovementController;
import moscow.rockstar.modules.movement.speed.SpeedState;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.TextLabelSetting;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemConvertible;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerEntity;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.player.ClientPlayerTickEndEvent;
import pyrock.events.player.EventOnMovePost;
import pyrock.events.player.InputEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Speed", category=ModuleCategory.MOVEMENT, description="modules.descriptions.speed")
public class Speed
extends Module {
    private int speedResetTicks;
    private final List<SpeedState> speedModes = new ArrayList<SpeedState>();
    private ModeSetting mode;
    private ModeSetting.Option vanilla;
    private ModeSetting.Option spookyElytra;
    private ModeSetting.Option collision;
    private ModeSetting.Option collisionTarget;
    private ModeSetting.Option ice;
    private ModeSetting.Option reallyworld;
    private ModeSetting.Option holyworld;
    private NumberSetting distance;
    private TextLabelSetting speedInfo;
    private NumberSetting onGround;
    private NumberSetting onJump;
    private NumberSetting onFall;
    private NumberSetting power;
    private NumberSetting boostRange;
    private NumberSetting predictionTicks;
    private BooleanSetting elytraSwap;
    private int reallyWorldTicks;
    private int movementTicks;
    private boolean speedActive;
    private static final long lastSpeedUpdate = 500L;
    private final TargetMovementController speedState = new TargetMovementController();
    private final ArmorSwapController collisionState = ArmorSwapController.getInstance();
    private final EventListener<EventOnMovePost> onEventOnMovePostListener = eventOnMovePost -> {
        if (!this.reallyworld.isSelected() || Speed.minecraftClient.player == null) {
            return;
        }
        this.applySpeed(1.7f);
        if (this.reallyWorldTicks > 3) {
            double d = 0.03;
            if (this.reallyWorldTicks % 2 == 0) {
                Speed.minecraftClient.player.addVelocity(0.0, (double)0.03f, 0.0);
                d = Speed.minecraftClient.player.isOnGround() ? 0.085 : 0.03;
            }
            Vec3d VanillaChestLootTableGenerator = this.getPosition();
            Speed.minecraftClient.player.addVelocity(VanillaChestLootTableGenerator.x * d, 0.0, VanillaChestLootTableGenerator.z * d);
        }
        ++this.reallyWorldTicks;
    };
    private final EventListener<InputEvent> onInputEvent = inputEvent -> {
        if (this.holyworld.isSelected()) {
            this.speedState.applyInputOverrides((InputEvent)inputEvent);
        }
        if (this.reallyworld.isSelected()) {
            if (Speed.minecraftClient.player == null) {
                return;
            }
            this.movementTicks = Speed.minecraftClient.player.verticalCollision ? ++this.movementTicks : 0;
            if (this.movementTicks >= 1) {
                Speed.minecraftClient.player.jump();
            }
        }
        if (this.collisionTarget.isSelected()) {
            Entity targetEntity = RockstarClient.create().getFriendManager().getTargetEntity();
            if (!(targetEntity instanceof LivingEntity livingTarget)) {
                return;
            }
            Vec3d predictedPosition = livingTarget.getPos()
                .add(livingTarget.getPos()
                .subtract(new Vec3d(livingTarget.prevX, livingTarget.prevY, livingTarget.prevZ))
                .multiply((double)this.predictionTicks.getValue()));
            if ((EntityUtils.isEntityOverlapping(livingTarget, this.boostRange.getValue())
                    || EntityUtils.isOffsetOverlapping(livingTarget,
                        EntityPositionCache.getTrackedPosition(livingTarget), this.boostRange.getValue()))
                    && Speed.minecraftClient.options.forwardKey.isPressed()
                    && Speed.minecraftClient.player.hurtTime <= 0
                    && RockstarClient.create().getModuleRegistry().getModule(InventoryMove.class).getModeEntries().isEmpty()) {
                Vec3d playerPosition = Speed.minecraftClient.player.getPos();
                if (Speed.minecraftClient.world.raycast(new RaycastContext(Speed.minecraftClient.player.getPos(), predictedPosition,
                        RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,
                        Speed.minecraftClient.player)).getType() != HitResult.Type.MISS) {
                    return;
                }
                Speed.minecraftClient.options.sprintKey.setPressed(false);
                inputEvent.setSprint(false);
                Speed.minecraftClient.player.setSprinting(false);
            }
        }
    };
    private final EventListener<ClientPlayerTickEndEvent> onClientPlayerTickEndEvent = clientPlayerTickEndEvent -> {
        if (!this.reallyworld.isSelected() || Speed.minecraftClient.player == null || Speed.minecraftClient.player.networkHandler == null) {
            return;
        }
        if (this.reallyWorldTicks % 2 == 0) {
            this.applySpeed(0.3f);
            Speed.minecraftClient.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)Speed.minecraftClient.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    };
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = receivePacketEvent -> {
        if (!this.reallyworld.isSelected()) {
            return;
        }
        if (receivePacketEvent.getPacket() instanceof PlayerPositionLookS2CPacket) {
            if (this.reallyWorldTicks % 2 == 1) {
                ++this.reallyWorldTicks;
            }
            this.applySpeed(1.0f);
        }
    };

    public Speed() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.mode = new ModeSetting(this, "modules.settings.speed.mode");
        this.vanilla = new ModeSetting.Option(this.mode, "modules.settings.speed.vanilla");
        this.spookyElytra = new ModeSetting.Option(this.mode, "modules.settings.speed.spooky_elytra");
        this.collision = new ModeSetting.Option(this.mode, "modules.settings.speed.collision");
        this.collisionTarget = new ModeSetting.Option(this.mode, "modules.settings.speed.collision_target");
        this.ice = new ModeSetting.Option(this.mode, "modules.settings.speed.ice");
        this.reallyworld = new ModeSetting.Option(this.mode, "ReallyWorld");
        this.holyworld = new ModeSetting.Option(this.mode, "HolyWorld");
        this.distance = new NumberSetting((SettingOwner)this, "modules.settings.speed.distance", () -> !this.collision.isSelected()).setMinValue(0.05f).setMaxValue(2.0f).setStep(0.05f).setValue(0.3f);
        this.speedInfo = new TextLabelSetting((SettingOwner)this, "modules.settings.speed.speed_info", () -> !this.collision.isSelected());
        this.onGround = new NumberSetting((SettingOwner)this, "modules.settings.speed.on_ground", () -> !this.collision.isSelected()).setMinValue(0.5f).setMaxValue(3.0f).setStep(0.05f).setValue(1.1f);
        this.onJump = new NumberSetting((SettingOwner)this, "modules.settings.speed.on_jump", () -> !this.collision.isSelected()).setMinValue(0.5f).setMaxValue(3.0f).setStep(0.05f).setValue(1.1f);
        this.onFall = new NumberSetting((SettingOwner)this, "modules.settings.speed.on_fall", () -> !this.collision.isSelected()).setMinValue(0.5f).setMaxValue(3.0f).setStep(0.05f).setValue(1.1f);
        this.power = new NumberSetting((SettingOwner)this, "modules.settings.speed.power", () -> !this.collisionTarget.isSelected()).setMinValue(0.01f).setMaxValue(0.1f).setStep(0.01f).setValue(0.06f);
        this.boostRange = new NumberSetting((SettingOwner)this, "modules.settings.speed.boost_range", () -> !this.collisionTarget.isSelected()).setMinValue(0.0f).setMaxValue(2.0f).setStep(0.1f).setValue(1.0f);
        this.predictionTicks = new NumberSetting((SettingOwner)this, "Predict", () -> !this.collisionTarget.isSelected()).setMinValue(0.0f).setMaxValue(10.0f).setStep(1.0f).setValue(5.0f);
        this.elytraSwap = new BooleanSetting((SettingOwner)this, "Elytra swap", () -> !this.holyworld.isSelected()).enable();
    }

    @Override
    public void onTick() {
        if (Speed.minecraftClient.player == null || Speed.minecraftClient.world == null
                || Speed.minecraftClient.interactionManager == null) {
            return;
        }
        if (this.speedActive && !this.reallyworld.isSelected()) {
            this.resetSpeed();
        }
        if (this.holyworld.isSelected()) {
            this.resetMovementState();
        } else if (this.elytraSwap.isEnabled() && Speed.minecraftClient.player != null && InventoryUtils.chestplateRule().getItem() == Items.ELYTRA) {
            this.collisionState.endArmorSwap();
        }
        if (this.ice.isSelected() && InventoryUtils.getSelectedHotbarSlot().getItem() == Items.ICE) {
            RockstarClient.create().getRotationManager().requestRotation(new Rotation(Speed.minecraftClient.player.getYaw(), 90.0f), RotationCorrectionMode.DIRECT, 180.0f, 180.0f, 180.0f, RotationPriority.MAXIMUM_PRIORITY);
            HitResult raycastResult = MathUtils.raycastFromCamera(10.0, Speed.minecraftClient.player.getYaw(), 90.0f, Speed.minecraftClient.player);
            if (raycastResult instanceof BlockHitResult blockHit
                    && Speed.minecraftClient.interactionManager.interactBlock(Speed.minecraftClient.player, Hand.MAIN_HAND, blockHit) instanceof ActionResult.Success) {
                Speed.minecraftClient.player.swingHand(Hand.MAIN_HAND);
            }
        }
        if (this.collision.isSelected()) {
            for (Entity entity : Speed.minecraftClient.world.getEntities()) {
                if (!(entity instanceof LivingEntity livingEntity) || !livingEntity.isAlive()
                        || livingEntity.isSpectator() || livingEntity == Speed.minecraftClient.player
                        || !EntityUtils.isEntityOverlapping(livingEntity, this.distance.getValue())) continue;
                float f2 = Speed.minecraftClient.world.getBlockState(Speed.minecraftClient.player.getBlockPos().add((int)Speed.minecraftClient.player.getVelocity().x, (int)Speed.minecraftClient.player.getVelocity().y, (int)Speed.minecraftClient.player.getVelocity().z)).getBlock().getSlipperiness();
                float movementFactor = Speed.minecraftClient.player.isOnGround() ? f2 : 0.57f;
                float speedMultiplier = Speed.minecraftClient.player.isOnGround() ? this.onGround.getValue() : (Speed.minecraftClient.player.fallDistance > 0.0f ? this.onFall.getValue() : this.onJump.getValue());
                Speed.minecraftClient.player.setVelocity(Speed.minecraftClient.player.getVelocity().x * (double)speedMultiplier, Speed.minecraftClient.player.getVelocity().y, Speed.minecraftClient.player.getVelocity().z * (double)speedMultiplier);
                break;
            }
        }
        if (this.vanilla.isSelected()) {
            Speed.minecraftClient.options.sneakKey.setPressed(false);
            RockstarClient.create().getRotationManager().setRotation(new Rotation(Speed.minecraftClient.player.getYaw(), 90.0f));
            if (Speed.minecraftClient.player.isOnGround() && !Speed.minecraftClient.options.jumpKey.isPressed()) {
                Speed.minecraftClient.player.jump();
                Vec3d currentVelocity = Speed.minecraftClient.player.getVelocity();
                Speed.minecraftClient.player.setVelocity(currentVelocity.x, currentVelocity.y - 0.085f, currentVelocity.z);
                BlockPos adminsky = Speed.minecraftClient.player.getBlockPos();
                BlockPos adminsky2 = Speed.minecraftClient.player.getBlockPos().add(0, -1, 0);
                BlockPos adminsky3 = Speed.minecraftClient.player.getBlockPos().add(0, -2, 0);
                Speed.minecraftClient.world.setBlockState(adminsky2, Blocks.BLUE_ICE.getDefaultState());
                Vec3d VanillaChestLootTableGenerator = new Vec3d((double)adminsky.getX(), (double)adminsky.getY(), (double)adminsky.getZ());
                BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky, false);
                Vec3d WallPlayerSkullBlock = new Vec3d((double)adminsky2.getX(), (double)adminsky2.getY(), (double)adminsky2.getZ());
                BlockHitResult class_39653 = new BlockHitResult(WallPlayerSkullBlock, Direction.UP, adminsky2, false);
                Vec3d VanillaEntityLootTableGenerator = new Vec3d((double)adminsky3.getX(), (double)adminsky3.getY(), (double)adminsky3.getZ());
                BlockHitResult class_39654 = new BlockHitResult(VanillaEntityLootTableGenerator, Direction.UP, adminsky3, false);
                Speed.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, class_39653, 0));
            }
        } else if (this.spookyElytra.isSelected()) {
            HotbarSlot elytraSlot = ItemRuleSets.getHotbarRules().findByItem(Items.ELYTRA);
            if (elytraSlot != null && Speed.minecraftClient.player.fallDistance > 1.0f) {
                HotbarSlot previousSlot = InventoryUtils.getSelectedHotbarSlot();
                Speed.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(elytraSlot.getSlotIndex()));
                InventoryUtils.setSelectedHotbarSlot(elytraSlot);
                Speed.minecraftClient.interactionManager.interactItem((PlayerEntity)Speed.minecraftClient.player, Hand.MAIN_HAND);
                ((Slot)Speed.minecraftClient.player.currentScreenHandler.slots.get(6)).setStack(new ItemStack((ItemConvertible)Items.ELYTRA));
                if (Speed.minecraftClient.player.isSprinting() && Speed.minecraftClient.player.input.hasForwardMovement() && Speed.minecraftClient.player.checkGliding()) {
                    Speed.minecraftClient.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)Speed.minecraftClient.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
                InventoryUtils.setSelectedHotbarSlot(previousSlot);
                Speed.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(Speed.minecraftClient.player.getInventory().selectedSlot));
            }
        } else if (this.collisionTarget.isSelected()) {
            Entity targetEntity = RockstarClient.create().getFriendManager().getTargetEntity();
            if (!(targetEntity instanceof LivingEntity livingTarget)) {
                return;
            }
            Vec3d predictedPosition = livingTarget.getPos().add(livingTarget.getPos()
                .subtract(new Vec3d(livingTarget.prevX, livingTarget.prevY, livingTarget.prevZ))
                .multiply((double)this.predictionTicks.getValue()));
            if ((EntityUtils.isEntityOverlapping(livingTarget, this.boostRange.getValue())
                    || EntityUtils.isOffsetOverlapping(livingTarget, EntityPositionCache.getTrackedPosition(livingTarget), this.boostRange.getValue()))
                    && Speed.minecraftClient.options.forwardKey.isPressed()
                    && Speed.minecraftClient.player.hurtTime <= 0
                    && RockstarClient.create().getModuleRegistry().getModule(InventoryMove.class).getModeEntries().isEmpty()) {
                Vec3d playerPosition = Speed.minecraftClient.player.getPos();
                if (Speed.minecraftClient.world.raycast(new RaycastContext(Speed.minecraftClient.player.getPos(), predictedPosition,
                        RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,
                        Speed.minecraftClient.player)).getType() != HitResult.Type.MISS) {
                    return;
                }
                Vec3d directionToTarget = predictedPosition.subtract(playerPosition).normalize();
                float f6 = Speed.minecraftClient.world.getBlockState(BlockPos.ofFloored((Position)Speed.minecraftClient.player.getPos().add(Speed.minecraftClient.player.getVelocity().x, Speed.minecraftClient.player.getVelocity().y, Speed.minecraftClient.player.getVelocity().z))).getBlock().getSlipperiness();
                float f7 = Speed.minecraftClient.player.isOnGround() ? f6 : 0.79f;
                float f8 = Speed.minecraftClient.player.isOnGround() ? f6 : 0.99f;
                double d = Speed.minecraftClient.player.getVelocity().y;
                float f9 = this.power.getValue() * 3.0f;
                double d2 = directionToTarget.x * (double)f9 * (double)f8 / (double)f7;
                double d3 = directionToTarget.z * (double)f9 * (double)f8 / (double)f7;
                Speed.minecraftClient.player.setVelocity(Speed.minecraftClient.player.getVelocity().x + d2, d, Speed.minecraftClient.player.getVelocity().z + d3);
            }
        }
    }

    private Vec3d getPosition() {
        if (Speed.minecraftClient.player.input == null) {
            return Vec3d.ZERO;
        }
        float f = Speed.minecraftClient.player.input.movementForward;
        float f2 = Speed.minecraftClient.player.input.movementSideways;
        if (f == 0.0f && f2 == 0.0f) {
            return Vec3d.ZERO;
        }
        double d = EntityUtils.getDirectionRadians(Speed.minecraftClient.player.getYaw(), f, f2);
        return new Vec3d(-Math.sin(d), 0.0, Math.cos(d));
    }

    private void applySpeed(float f) {
        EntityUtils.setMovementFactor(f);
        this.speedActive = true;
    }

    private void resetSpeed() {
        this.reallyWorldTicks = 0;
        this.movementTicks = 0;
        this.speedActive = false;
        EntityUtils.resetMovementFactor();
    }

    private void resetMovementState() {
        boolean bl;
        if (Speed.minecraftClient.player == null) {
            return;
        }
        boolean bl2 = bl = !RockstarClient.create().getModuleRegistry().getModule(InventoryMove.class).getModeEntries().isEmpty();
        if (this.collisionState.isSwapPending() || bl || !this.collisionState.hasCooldownElapsed(500L)) {
            this.speedState.resetMovementState();
            return;
        }
        this.speedState.updateMovementState();
    }

    public static boolean isSpeedModeSelected(ClientPlayerEntity class_7462) {
        Speed speed = RockstarClient.create().getModuleRegistry().getModule(Speed.class);
        return speed != null && speed.isEnabled() && speed.holyworld.isSelected() && speed.speedState.shouldApplyMovement(class_7462);
    }

    public static boolean isSpeedReady() {
        Speed speed = RockstarClient.create().getModuleRegistry().getModule(Speed.class);
        return speed != null && speed.isEnabled() && speed.holyworld.isSelected();
    }

    public static boolean isCollisionReady() {
        Speed speed = RockstarClient.create().getModuleRegistry().getModule(Speed.class);
        return speed != null && speed.isEnabled() && speed.holyworld.isSelected();
    }

    private void updateSpeedState() {
        this.speedState.resetJumpState();
    }

    @Override
    public void onEnable() {
        this.updateSpeedState();
        if (this.holyworld.isSelected() && this.elytraSwap.isEnabled()) {
            this.collisionState.beginArmorSwap();
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.resetSpeed();
        this.updateSpeedState();
        if (this.holyworld.isSelected() && this.elytraSwap.isEnabled()) {
            this.collisionState.endArmorSwap();
        }
        super.onDisable();
    }

    @Generated
    public ModeSetting.Option getCollisionTarget() {
        return this.collisionTarget;
    }

    @Generated
    public NumberSetting getDistanceSetting() {
        return this.predictionTicks;
    }
}
