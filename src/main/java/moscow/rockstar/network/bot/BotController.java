/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  lombok.Generated
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.SlotActionType
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.BlockView
 *  net.minecraft.Direction
 *  net.minecraft.Position
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.BlockState
 *  net.minecraft.EmptyBlockView
 *  net.minecraft.ChatMessageC2SPacket
 *  net.minecraft.ClickSlotC2SPacket
 *  net.minecraft.PlayerInteractEntityC2SPacket
 *  net.minecraft.PlayerActionC2SPacket
 *  net.minecraft.PlayerActionC2SPacket$Action
 *  net.minecraft.ClientCommandC2SPacket
 *  net.minecraft.ClientCommandC2SPacket$Mode
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.HandSwingC2SPacket
 *  net.minecraft.PlayerInteractBlockC2SPacket
 *  net.minecraft.PlayerInteractItemC2SPacket
 *  net.minecraft.MathHelper
 *  net.minecraft.BlockHitResult
 *  net.minecraft.OtherClientPlayerEntity
 *  net.minecraft.CommandExecutionC2SPacket
 *  net.minecraft.LastSeenMessageList$Acknowledgment
 */
package moscow.rockstar.network.bot;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.BitSet;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import moscow.rockstar.inventory.InventoryController;
import moscow.rockstar.items.InventoryState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.network.ConnectionState;
import moscow.rockstar.network.PlayerPacketHandler;
import moscow.rockstar.network.bot.BotBehaviorStrategy;
import moscow.rockstar.network.bot.BotControlState;
import moscow.rockstar.network.session.BotConnection;
import moscow.rockstar.render.shader.ShaderEntitySnapshot;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.BlockState;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.message.LastSeenMessageList;

public class BotController {
    private final String botName;
    private final BotConnection botConnection;
    private final InventoryState inventoryState;
    private final BotWorldState worldState;
    private final InventoryController inventoryController;
    private BotControlState controlState;
    private PlayerPacketHandler packetHandler;
    private BotBehaviorStrategy behaviorStrategy = new IdleBotBehaviorStrategy();
    private boolean primaryActionActive;
    private int botSlotIndex;
    private boolean secondaryActionActive;
    private String serverAddress;
    private int serverPort = 25565;
    private int tickCount = 0;
    private double horizontalTravelDistance;
    private double horizontalOffsetX;
    private double horizontalOffsetZ;
    private double verticalOffset;
    private double targetGroundY = Double.NaN;
    private long lastActionTime;
    private BlockPos breakingBlockPosition;
    private Direction breakingFace = Direction.UP;
    private double blockBreakProgress;
    private long blockBreakUpdatedAt;
    private long lastSwingTime;
    private int actionSequence;

    public BotController(String string, BotControlState botControlState) {
        this.botName = string;
        this.controlState = botControlState;
        this.inventoryState = new InventoryState();
        this.worldState = new BotWorldState(string);
        this.inventoryController = new InventoryController(this, this.worldState);
        this.botConnection = new BotConnection(this);
        this.botSlotIndex = botControlState.getBotSlotIndex();
    }

    public void connect(String string, int n) {
        this.serverAddress = string;
        this.serverPort = n;
        this.secondaryActionActive = false;
        this.worldState.configureEndpoint(string, n);
        this.botConnection.connect(string, n);
    }

    public void disconnect() {
        this.secondaryActionActive = true;
        this.setBehaviorStrategy(new IdleBotBehaviorStrategy());
        this.abortBlockBreaking();
        this.worldState.stop();
        this.botConnection.disconnectGracefully();
    }

    public void tick() {
        this.botConnection.tickConnection();
        if (!this.botConnection.isPlaying()) {
            return;
        }
        ++this.tickCount;
        if (this.inventoryController.isSimulationActive()) {
            this.inventoryController.updateMovementAndPackets(MinecraftClient.getInstance());
        } else if (this.behaviorStrategy != null) {
            this.behaviorStrategy.applyBehavior(this);
        }
        this.updateVerticalMovement();
        this.worldState.update(this.inventoryState);
        this.inventoryController.updateControllerState();
    }

    public void processInteractionCycle() {
        this.processInventoryActions();
    }

    public void processInventoryActions() {
        if (!this.botConnection.isPlaying()) {
            return;
        }
        if (this.inventoryState.isFlying()) {
            Vec3d VanillaChestLootTableGenerator = this.inventoryState.getPosition();
            this.inventoryState.setPosition(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y + Math.max(this.controlState.getVerticalMovementStep(), (double)this.inventoryState.getFlySpeed()), VanillaChestLootTableGenerator.z);
            this.inventoryState.setOnGround(false);
            return;
        }
        if (!this.inventoryState.isOnGround()) {
            return;
        }
        this.resolveGroundHeight();
        this.verticalOffset = this.controlState.getVerticalMovementStep();
        this.inventoryState.setOnGround(false);
    }

    public void synchronizePrimaryAction() {
        this.setPrimaryActionActive(!this.inventoryState.isPrimaryActionActive());
    }

    public void setPrimaryActionActive(boolean bl) {
        if (!this.botConnection.isPlaying()) {
            return;
        }
        if (this.inventoryState.isPrimaryActionActive() == bl) {
            return;
        }
        this.inventoryState.setPrimaryActionActive(bl);
        this.sendPlayerAction(bl ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY);
    }

    public void setSecondaryActionActive(boolean bl) {
        if (!this.botConnection.isPlaying()) {
            return;
        }
        if (this.inventoryState.isSecondaryActionActive() == bl) {
            return;
        }
        this.inventoryState.setSecondaryActionActive(bl);
        this.sendPlayerAction(bl ? ClientCommandC2SPacket.Mode.START_SPRINTING : ClientCommandC2SPacket.Mode.STOP_SPRINTING);
    }

    public void swingMainHand() {
        if (!this.botConnection.isPlaying()) {
            return;
        }
        this.botConnection.sendPacket((Packet<?>)new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    public void attackEntity(Entity class_12972) {
        if (!this.botConnection.isPlaying() || class_12972 == null) {
            return;
        }
        this.botConnection.sendPacket((Packet<?>)PlayerInteractEntityC2SPacket.attack((Entity)class_12972, (boolean)this.inventoryState.isPrimaryActionActive()));
        this.botConnection.sendPacket((Packet<?>)new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    public void attackEntityById(int n) {
        if (!this.botConnection.isPlaying() || n < 0) {
            return;
        }
        Entity class_12972 = this.resolveEntityById(n);
        if (class_12972 == null) {
            this.swingMainHand();
            return;
        }
        this.attackEntity(class_12972);
    }

    public void sendLookPacket() {
        if (!this.botConnection.isPlaying()) {
            return;
        }
        this.botConnection.sendPacket((Packet<?>)new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.inventoryState.getYaw(), this.inventoryState.getPitch()));
    }

    public void interactWithBlock(BlockHitResult class_39652) {
        if (!this.botConnection.isPlaying() || class_39652 == null) {
            return;
        }
        this.botConnection.sendPacket((Packet<?>)new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, class_39652, this.nextSequenceId()));
        this.botConnection.sendPacket((Packet<?>)new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    public void breakBlockAtHit(BlockHitResult class_39652) {
        if (!this.botConnection.isPlaying() || class_39652 == null) {
            return;
        }
        BlockPos adminsky = class_39652.getBlockPos();
        Direction class_23502 = class_39652.getSide();
        this.startBlockBreaking(adminsky, class_23502);
        this.stopBlockBreaking(adminsky, class_23502);
    }

    public void startBlockBreaking(BlockPos adminsky, Direction class_23502) {
        if (!this.botConnection.isPlaying() || adminsky == null) {
            return;
        }
        this.botConnection.sendPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, adminsky, class_23502 == null ? Direction.UP : class_23502, this.nextSequenceId()));
        this.botConnection.sendPacket((Packet<?>)new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    public void stopBlockBreaking(BlockPos adminsky, Direction class_23502) {
        if (!this.botConnection.isPlaying() || adminsky == null) {
            return;
        }
        this.botConnection.sendPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, adminsky, class_23502 == null ? Direction.UP : class_23502, this.nextSequenceId()));
    }

    public boolean attackTargetAtCrosshair() {
        if (!this.botConnection.isPlaying()) {
            return false;
        }
        BlockHitCandidate blockHitCandidate = this.findBlockHit(this.controlState.getTargetReachDistance());
        EntityHitCandidate entityHitCandidate = this.findEntityHit(blockHitCandidate == null ? this.controlState.getTargetReachDistance() : blockHitCandidate.getDistanceSquared());
        if (entityHitCandidate != null) {
            this.abortBlockBreaking();
            this.attackEntityById(entityHitCandidate.getSnapshot().getId());
            return true;
        }
        if (blockHitCandidate != null) {
            this.breakBlockOverTime(blockHitCandidate.getHitResult());
            return true;
        }
        this.abortBlockBreaking();
        this.swingMainHand();
        return false;
    }

    public boolean interactWithTargetBlock() {
        if (!this.botConnection.isPlaying()) {
            return false;
        }
        BlockHitCandidate blockHitCandidate = this.findBlockHit(this.controlState.getTargetReachDistance());
        if (blockHitCandidate != null) {
            this.interactWithBlock(blockHitCandidate.getHitResult());
            return true;
        }
        this.sendLookPacket();
        return false;
    }

    public void setUsingItem(boolean bl) {
        if (!this.botConnection.isPlaying()) {
            return;
        }
        this.botConnection.sendPacket((Packet<?>)new PlayerActionC2SPacket(bl ? PlayerActionC2SPacket.Action.DROP_ALL_ITEMS : PlayerActionC2SPacket.Action.DROP_ITEM, BlockPos.ORIGIN, Direction.DOWN));
    }

    public void selectHotbarSlot(int n) {
        if (!this.botConnection.isPlaying()) {
            return;
        }
        if (n < 0 || n > 8) {
            return;
        }
        this.inventoryState.setSelectedHotbarSlot(n);
        this.botConnection.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(n));
    }

    public boolean selectHotbarItem(Item class_17922) {
        if (!this.botConnection.isPlaying() || class_17922 == null) {
            return false;
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack class_17992 = this.inventoryState.getInventoryItems()[i];
            if (class_17992 == null || class_17992.isEmpty() || class_17992.getItem() != class_17922) continue;
            this.selectHotbarSlot(i);
            return true;
        }
        return false;
    }

    public boolean clickInventorySlot(int n) {
        return this.clickInventorySlotWithAction(n, 0, SlotActionType.PICKUP);
    }

    public boolean clickInventorySlotWithAction(int n, int n2, SlotActionType class_17132) {
        if (!this.botConnection.isPlaying() || !this.inventoryState.hasOpenContainer() || n < 0) {
            return false;
        }
        SlotActionType class_17133 = class_17132 == null ? SlotActionType.PICKUP : class_17132;
        Int2ObjectOpenHashMap int2ObjectOpenHashMap = new Int2ObjectOpenHashMap();
        ItemStack class_17992 = this.inventoryState.getCursorStack();
        if (class_17133 == SlotActionType.PICKUP && n2 == 0) {
            ItemStack class_17993 = this.inventoryState.getContainerItem(n).copy();
            ItemStack class_17994 = class_17992 == null ? ItemStack.EMPTY : class_17992.copy();
            ItemStack class_17995 = class_17993 == null ? ItemStack.EMPTY : class_17993.copy();
            this.inventoryState.updateContainerSlot(this.inventoryState.getContainerId(), this.inventoryState.getContainerRevision(), n, class_17994);
            this.inventoryState.setCursorStack(class_17995);
            int2ObjectOpenHashMap.put(n, (Object)class_17994.copy());
            class_17992 = class_17995;
        }
        this.botConnection.sendPacket((Packet<?>)new ClickSlotC2SPacket(this.inventoryState.getContainerId(), this.inventoryState.getContainerRevision(), n, n2, class_17133, class_17992 == null ? ItemStack.EMPTY : class_17992.copy(), (Int2ObjectMap)int2ObjectOpenHashMap));
        return true;
    }

    public boolean isBlockTargetReachable() {
        BlockHitCandidate blockHitCandidate = this.findBlockHit(this.controlState.getTargetReachDistance());
        return blockHitCandidate != null && this.isBlockBreakable(blockHitCandidate.getHitResult().getBlockPos());
    }

    public void abortBlockBreaking() {
        if (this.breakingBlockPosition == null || !this.botConnection.isPlaying()) {
            this.resetBlockBreaking();
            return;
        }
        this.botConnection.sendPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.breakingBlockPosition, this.breakingFace == null ? Direction.UP : this.breakingFace, this.nextSequenceId()));
        this.resetBlockBreaking();
    }

    private void breakBlockOverTime(BlockHitResult class_39652) {
        Direction class_23502;
        if (class_39652 == null || !this.botConnection.isPlaying()) {
            return;
        }
        BlockPos adminsky = class_39652.getBlockPos();
        Direction class_23503 = class_23502 = class_39652.getSide() == null ? this.getBlockFacing(adminsky) : class_39652.getSide();
        if (!this.isBlockBreakable(adminsky)) {
            this.abortBlockBreaking();
            return;
        }
        if (this.inventoryState.isCreativeMode()) {
            this.abortBlockBreaking();
            this.breakBlockAtHit(class_39652);
            return;
        }
        long l = System.currentTimeMillis();
        if (!adminsky.equals(this.breakingBlockPosition)) {
            this.abortBlockBreaking();
            this.breakingBlockPosition = adminsky.toImmutable();
            this.breakingFace = class_23502;
            this.blockBreakProgress = 0.0;
            this.blockBreakUpdatedAt = l;
            this.lastSwingTime = 0L;
            this.startBlockBreaking(this.breakingBlockPosition, this.breakingFace);
            return;
        }
        this.breakingFace = class_23502;
        BlockState class_26802 = this.worldState.getBlockState(adminsky);
        double d = Math.max(1.0, (double)(l - this.blockBreakUpdatedAt) / 50.0);
        this.blockBreakUpdatedAt = l;
        double d2 = Math.max(this.controlState.getMinimumBreakRate(), (double)class_26802.getHardness((BlockView)EmptyBlockView.INSTANCE, adminsky) * this.controlState.getBlockHardnessMultiplier());
        this.blockBreakProgress += d / d2;
        if (l - this.lastSwingTime >= this.controlState.getSwingIntervalMillis()) {
            this.botConnection.sendPacket((Packet<?>)new HandSwingC2SPacket(Hand.MAIN_HAND));
            this.lastSwingTime = l;
        }
        if (this.blockBreakProgress >= 1.0) {
            this.stopBlockBreaking(adminsky, this.breakingFace);
            this.resetBlockBreaking();
        }
    }

    private boolean isBlockBreakable(BlockPos adminsky) {
        if (adminsky == null) {
            return false;
        }
        BlockState class_26802 = this.worldState.getBlockState(adminsky);
        return !class_26802.isAir() && class_26802.getHardness((BlockView)EmptyBlockView.INSTANCE, adminsky) >= 0.0f;
    }

    private void resetBlockBreaking() {
        this.breakingBlockPosition = null;
        this.breakingFace = Direction.UP;
        this.blockBreakProgress = 0.0;
        this.blockBreakUpdatedAt = 0L;
        this.lastSwingTime = 0L;
    }

    public void sendChatOrCommand(String string) {
        if (!this.botConnection.isPlaying()) {
            return;
        }
        if (string.startsWith("/")) {
            this.sendCommand(string.substring(1));
        } else {
            this.botConnection.sendPacket((Packet<?>)new ChatMessageC2SPacket(string, Instant.now(), ThreadLocalRandom.current().nextLong(), null, new LastSeenMessageList.Acknowledgment(0, new BitSet())));
        }
    }

    public void sendCommand(String string) {
        if (!this.botConnection.isPlaying()) {
            return;
        }
        this.botConnection.sendPacket((Packet<?>)new CommandExecutionC2SPacket(string));
    }

    public void setBehaviorStrategy(BotBehaviorStrategy botBehaviorStrategy) {
        this.behaviorStrategy = botBehaviorStrategy == null ? new IdleBotBehaviorStrategy() : botBehaviorStrategy;
    }

    public void resetControlState() {
        this.setBehaviorStrategy(new IdleBotBehaviorStrategy());
        this.abortBlockBreaking();
        this.horizontalTravelDistance = 0.0;
        this.horizontalOffsetX = 0.0;
        this.horizontalOffsetZ = 0.0;
    }

    public void lookAtPosition(double d, double d2, double d3) {
        Vec3d VanillaChestLootTableGenerator = this.inventoryState.getPosition();
        double d4 = d - VanillaChestLootTableGenerator.x;
        double d5 = d2 - (VanillaChestLootTableGenerator.y + this.controlState.getEyeHeightOffset());
        double d6 = d3 - VanillaChestLootTableGenerator.z;
        double d7 = Math.sqrt(d4 * d4 + d6 * d6);
        float f = (float)Math.toDegrees(Math.atan2(-d4, d6));
        float f2 = (float)Math.toDegrees(-Math.atan2(d5, d7));
        this.inventoryState.setRotation(this.approachAngle(this.inventoryState.getYaw(), f, (float)this.controlState.getYawTurnRateDegrees()), this.approachAngle(this.inventoryState.getPitch(), f2, (float)this.controlState.getPitchTurnRateDegrees()));
    }

    public void setPositionAndEnableUse(double d, double d2, double d3) {
        this.inventoryState.setPosition(d, d2, d3);
        this.verticalOffset = 0.0;
        this.targetGroundY = d2;
        this.inventoryState.setOnGround(true);
    }

    public void moveTowardPosition(Vec3d VanillaChestLootTableGenerator, double d) {
        if (VanillaChestLootTableGenerator == null) {
            return;
        }
        Vec3d WallPlayerSkullBlock = this.inventoryState.getPosition();
        double d2 = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d3 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        double d4 = Math.sqrt(d2 * d2 + d3 * d3);
        this.lookAtPosition(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y + this.controlState.getEyeHeightOffset(), VanillaChestLootTableGenerator.z);
        if (d4 <= d || d4 < 1.0E-4) {
            this.applyMotionDamping();
            return;
        }
        double d5 = this.controlState.getBaseMovementSpeed() * this.controlState.getMovementStepScale();
        double d6 = Math.min(d5, Math.max(0.0, d4 - d));
        this.horizontalTravelDistance = this.approachValue(this.horizontalTravelDistance, d6, this.controlState.getMovementAcceleration());
        this.horizontalOffsetX = d2 / d4 * this.horizontalTravelDistance;
        this.horizontalOffsetZ = d3 / d4 * this.horizontalTravelDistance;
        double d7 = VanillaChestLootTableGenerator.y - WallPlayerSkullBlock.y;
        if (d7 > this.controlState.getCameraVerticalThreshold() && this.inventoryState.isOnGround()) {
            this.processInventoryActions();
        }
        this.inventoryState.setPosition(WallPlayerSkullBlock.x + this.horizontalOffsetX, WallPlayerSkullBlock.y, WallPlayerSkullBlock.z + this.horizontalOffsetZ);
    }

    public void applyMotionDamping() {
        if (Math.abs(this.horizontalOffsetX) < 1.0E-5 && Math.abs(this.horizontalOffsetZ) < 1.0E-5) {
            this.horizontalTravelDistance = 0.0;
            this.horizontalOffsetX = 0.0;
            this.horizontalOffsetZ = 0.0;
            return;
        }
        this.horizontalOffsetX *= this.controlState.getMotionDampingFactor();
        this.horizontalOffsetZ *= this.controlState.getMotionDampingFactor();
        this.horizontalTravelDistance *= this.controlState.getMotionDampingFactor();
        Vec3d VanillaChestLootTableGenerator = this.inventoryState.getPosition();
        this.inventoryState.setPosition(VanillaChestLootTableGenerator.x + this.horizontalOffsetX, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z + this.horizontalOffsetZ);
    }

    public void applyVerticalOffset(double d) {
        double d2 = Math.toRadians(this.inventoryState.getYaw());
        Vec3d VanillaChestLootTableGenerator = this.inventoryState.getPosition();
        this.inventoryState.setPosition(VanillaChestLootTableGenerator.x - Math.sin(d2) * d, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z + Math.cos(d2) * d);
    }

    public double distanceTo(Vec3d VanillaChestLootTableGenerator) {
        return this.inventoryState.getPosition().distanceTo(VanillaChestLootTableGenerator);
    }

    public double horizontalDistanceTo(Vec3d VanillaChestLootTableGenerator) {
        Vec3d WallPlayerSkullBlock = this.inventoryState.getPosition();
        double d = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d2 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        return Math.sqrt(d * d + d2 * d2);
    }

    public boolean isActionCooldownElapsed(long l) {
        return l - this.lastActionTime >= this.controlState.getActionCooldownMillis();
    }

    public void setLastActionTime(long l) {
        this.lastActionTime = l;
    }

    public void setVerticalMovementOverride(boolean bl) {
        this.inventoryState.setOnGround(bl);
        if (bl) {
            this.verticalOffset = 0.0;
            this.targetGroundY = this.inventoryState.getPositionY();
        }
    }

    private void updateVerticalMovement() {
        if (this.inventoryState.isFlying()) {
            this.verticalOffset = 0.0;
            this.inventoryState.setOnGround(false);
            return;
        }
        if (this.inventoryState.isOnGround() && Math.abs(this.verticalOffset) <= this.controlState.getMovementEpsilon()) {
            this.resolveGroundHeight();
            return;
        }
        Vec3d VanillaChestLootTableGenerator = this.inventoryState.getPosition();
        double d = VanillaChestLootTableGenerator.y + this.verticalOffset;
        OptionalDouble optionalDouble = this.findGroundHeight(VanillaChestLootTableGenerator.x, Math.max(VanillaChestLootTableGenerator.y, d), VanillaChestLootTableGenerator.z);
        boolean bl = optionalDouble.isPresent();
        double d2 = optionalDouble.orElse(this.targetGroundY);
        if (this.verticalOffset <= 0.0 && !Double.isNaN(d2) && d <= d2 + this.controlState.getTargetHeightTolerance() && VanillaChestLootTableGenerator.y >= d2 - this.controlState.getTargetHeightTolerance()) {
            this.inventoryState.setPosition(VanillaChestLootTableGenerator.x, d2, VanillaChestLootTableGenerator.z);
            this.inventoryState.setOnGround(true);
            this.verticalOffset = 0.0;
            this.targetGroundY = d2;
            return;
        }
        this.inventoryState.setPosition(VanillaChestLootTableGenerator.x, d, VanillaChestLootTableGenerator.z);
        this.inventoryState.setOnGround(false);
        this.verticalOffset = Math.max((this.verticalOffset - this.controlState.getVerticalSmoothingStep()) * this.controlState.getVerticalSmoothingFactor(), -this.controlState.getVerticalSmoothingLimit());
        if (bl && Math.abs(d - optionalDouble.getAsDouble()) <= this.controlState.getTargetHeightTolerance()) {
            this.targetGroundY = optionalDouble.getAsDouble();
        }
    }

    private void resolveGroundHeight() {
        Vec3d VanillaChestLootTableGenerator = this.inventoryState.getPosition();
        OptionalDouble optionalDouble = this.findGroundHeight(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z);
        if (optionalDouble.isEmpty()) {
            if (Double.isNaN(this.targetGroundY)) {
                this.targetGroundY = VanillaChestLootTableGenerator.y;
            }
            return;
        }
        this.targetGroundY = optionalDouble.getAsDouble();
        if (Math.abs(VanillaChestLootTableGenerator.y - this.targetGroundY) <= this.controlState.getTargetHeightTolerance()) {
            this.inventoryState.setPosition(VanillaChestLootTableGenerator.x, this.targetGroundY, VanillaChestLootTableGenerator.z);
        }
    }

    private OptionalDouble findGroundHeight(double d, double d2, double d3) {
        int n = MathHelper.floor((double)d2);
        int n2 = MathHelper.floor((double)(d2 - this.controlState.getBlockSearchDistance()));
        for (int i = n; i >= n2; --i) {
            BlockPos adminsky = BlockPos.ofFloored((double)d, (double)i, (double)d3);
            if (!this.worldState.isKnownSolid(adminsky)) continue;
            return OptionalDouble.of((double)i + 1.0);
        }
        return OptionalDouble.empty();
    }

    public BlockHitCandidate findBlockHit(double d) {
        double d2 = Math.max(0.0, d);
        double d3 = Math.max(this.controlState.getBlockRayStep(), this.controlState.getMovementEpsilon());
        Vec3d VanillaChestLootTableGenerator = this.getEyePosition();
        Vec3d WallPlayerSkullBlock = this.getLookDirection();
        BlockPos adminsky = BlockPos.ofFloored((Position)VanillaChestLootTableGenerator);
        for (double d4 = d3; d4 <= d2; d4 += d3) {
            Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock.multiply(d4));
            BlockPos adminsky2 = BlockPos.ofFloored((Position)VanillaEntityLootTableGenerator);
            if (adminsky2.equals(adminsky)) continue;
            BlockState class_26802 = this.worldState.getBlockState(adminsky2);
            if (!class_26802.isAir()) {
                Direction class_23502 = this.getHitFace(adminsky, adminsky2, WallPlayerSkullBlock);
                return new BlockHitCandidate(new BlockHitResult(VanillaEntityLootTableGenerator, class_23502, adminsky2, false), d4);
            }
            adminsky = adminsky2;
        }
        return null;
    }

    public EntityHitCandidate findEntityHit(double d) {
        Vec3d VanillaChestLootTableGenerator = this.getEyePosition();
        Vec3d WallPlayerSkullBlock = this.getLookDirection();
        double d2 = this.controlState.getEntityHitboxRadius();
        long l = this.worldState.getTick();
        EntityHitCandidate entityHitCandidate = null;
        for (ShaderEntitySnapshot shaderEntitySnapshot : this.worldState.getEntitySnapshots()) {
            Vec3d VanillaEntityLootTableGenerator;
            Vec3d PlayerSkullBlock;
            Vec3d RedstoneBlock;
            double d3;
            if (shaderEntitySnapshot.getId() == this.worldState.getTrackedEntityId() || shaderEntitySnapshot.getId() == this.inventoryState.getPlayerEntityId() || l - shaderEntitySnapshot.getLastSeenTick() > this.controlState.getEntitySnapshotMaxAgeTicks() || (d3 = (RedstoneBlock = (PlayerSkullBlock = shaderEntitySnapshot.getPosition().add(0.0, this.controlState.getEntityVerticalOffset(), 0.0)).subtract(VanillaChestLootTableGenerator)).dotProduct(WallPlayerSkullBlock)) < 0.0 || d3 > d || PlayerSkullBlock.distanceTo(VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock.multiply(d3))) > d2 || entityHitCandidate != null && !(d3 < entityHitCandidate.getDistanceSquared())) continue;
            entityHitCandidate = new EntityHitCandidate(shaderEntitySnapshot, d3);
        }
        return entityHitCandidate;
    }

    public Vec3d getEyePosition() {
        return this.inventoryState.getPosition().add(0.0, this.controlState.getEyeHeightOffset(), 0.0);
    }

    public Vec3d getLookDirection() {
        return Vec3d.fromPolar((float)this.inventoryState.getPitch(), (float)this.inventoryState.getYaw()).normalize();
    }

    public boolean isAimWithinTolerance(Vec3d VanillaChestLootTableGenerator, double d) {
        if (VanillaChestLootTableGenerator == null) {
            return false;
        }
        Vec3d WallPlayerSkullBlock = this.getEyePosition();
        double d2 = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d3 = VanillaChestLootTableGenerator.y - WallPlayerSkullBlock.y;
        double d4 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        double d5 = Math.sqrt(d2 * d2 + d4 * d4);
        float f = (float)Math.toDegrees(Math.atan2(-d2, d4));
        float f2 = (float)Math.toDegrees(-Math.atan2(d3, d5));
        double d6 = Math.abs(MathHelper.wrapDegrees((float)(f - this.inventoryState.getYaw())));
        double d7 = Math.abs(f2 - this.inventoryState.getPitch());
        return d6 <= d && d7 <= d;
    }

    public Direction getBlockFacing(BlockPos adminsky) {
        if (adminsky == null) {
            return Direction.UP;
        }
        Vec3d VanillaChestLootTableGenerator = this.getEyePosition();
        Vec3d WallPlayerSkullBlock = adminsky.toCenterPos();
        return Direction.getFacing((double)(VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x), (double)(VanillaChestLootTableGenerator.y - WallPlayerSkullBlock.y), (double)(VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z));
    }

    private Direction getHitFace(BlockPos adminsky, BlockPos adminsky2, Vec3d VanillaChestLootTableGenerator) {
        int n = adminsky2.getX() - adminsky.getX();
        int n2 = adminsky2.getY() - adminsky.getY();
        int n3 = adminsky2.getZ() - adminsky.getZ();
        if (n > 0) {
            return Direction.WEST;
        }
        if (n < 0) {
            return Direction.EAST;
        }
        if (n2 > 0) {
            return Direction.DOWN;
        }
        if (n2 < 0) {
            return Direction.UP;
        }
        if (n3 > 0) {
            return Direction.NORTH;
        }
        if (n3 < 0) {
            return Direction.SOUTH;
        }
        return Direction.getFacing((double)(-VanillaChestLootTableGenerator.x), (double)(-VanillaChestLootTableGenerator.y), (double)(-VanillaChestLootTableGenerator.z));
    }

    private Entity resolveEntityById(int n) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return null;
        }
        Entity class_12972 = client.world.getEntityById(n);
        if (class_12972 != null) {
            return class_12972;
        }
        UUID uUID = UUID.nameUUIDFromBytes(("BarneyBotTarget:" + this.botName + ":" + n).getBytes(StandardCharsets.UTF_8));
        OtherClientPlayerEntity SonicBoomParticle = new OtherClientPlayerEntity(client.world, new GameProfile(uUID, "BotTarget"));
        SonicBoomParticle.setId(n);
        return SonicBoomParticle;
    }

    private void sendPlayerAction(ClientCommandC2SPacket.Mode class_28492) {
        if (class_28492 == null || this.inventoryState.getPlayerEntityId() < 0) {
            return;
        }
        Entity class_12972 = this.resolveEntityById(this.inventoryState.getPlayerEntityId());
        if (class_12972 != null) {
            this.botConnection.sendPacket((Packet<?>)new ClientCommandC2SPacket(class_12972, class_28492));
        }
    }

    private int nextSequenceId() {
        return this.actionSequence++;
    }

    private double approachValue(double d, double d2, double d3) {
        if (d < d2) {
            return Math.min(d + d3, d2);
        }
        return Math.max(d - d3, d2);
    }

    private float approachAngle(float f, float f2, float f3) {
        float f4 = MathHelper.wrapDegrees((float)(f2 - f));
        float f5 = MathHelper.clamp((float)f4, (float)(-f3), (float)f3);
        return MathHelper.wrapDegrees((float)(f + f5));
    }

    public boolean isConnected() {
        return this.botConnection.isPlaying();
    }

    public ConnectionState getConnectionState() {
        return this.botConnection.getConnectionState();
    }

    @Generated
    public String getBotName() {
        return this.botName;
    }

    @Generated
    public BotConnection getConnection() {
        return this.botConnection;
    }

    @Generated
    public InventoryState getInventoryState() {
        return this.inventoryState;
    }

    @Generated
    public BotWorldState getWorldState() {
        return this.worldState;
    }

    @Generated
    public InventoryController getInventoryController() {
        return this.inventoryController;
    }

    @Generated
    public BotControlState getControlState() {
        return this.controlState;
    }

    @Generated
    public PlayerPacketHandler getPacketHandler() {
        return this.packetHandler;
    }

    @Generated
    public BotBehaviorStrategy getBehaviorStrategy() {
        return this.behaviorStrategy;
    }

    @Generated
    public boolean isPrimaryActionActive() {
        return this.primaryActionActive;
    }

    @Generated
    public int getActionSequence() {
        return this.actionSequence;
    }

    @Generated
    public boolean isSecondaryActionActive() {
        return this.secondaryActionActive;
    }

    @Generated
    public String getServerAddress() {
        return this.serverAddress;
    }

    @Generated
    public int getServerPort() {
        return this.serverPort;
    }

    @Generated
    public int getTickCount() {
        return this.tickCount;
    }

    @Generated
    public double getHorizontalTravelDistance() {
        return this.horizontalTravelDistance;
    }

    @Generated
    public double getHorizontalOffsetX() {
        return this.horizontalOffsetX;
    }

    @Generated
    public double getHorizontalOffsetZ() {
        return this.horizontalOffsetZ;
    }

    @Generated
    public double getVerticalOffset() {
        return this.verticalOffset;
    }

    @Generated
    public double getTargetGroundY() {
        return this.targetGroundY;
    }

    @Generated
    public long getLastActionTime() {
        return this.lastActionTime;
    }

    @Generated
    public BlockPos getBreakingBlockPosition() {
        return this.breakingBlockPosition;
    }

    @Generated
    public Direction getBreakingFace() {
        return this.breakingFace;
    }

    @Generated
    public double getBlockBreakProgress() {
        return this.blockBreakProgress;
    }

    @Generated
    public long getBlockBreakUpdatedAt() {
        return this.blockBreakUpdatedAt;
    }

    @Generated
    public long getLastSwingTime() {
        return this.lastSwingTime;
    }

    @Generated
    public int getBotSlotIndex() {
        return this.botSlotIndex;
    }

    @Generated
    public void setControlState(BotControlState botControlState) {
        this.controlState = botControlState;
    }

    @Generated
    public void setPacketHandler(PlayerPacketHandler playerPacketHandler) {
        this.packetHandler = playerPacketHandler;
    }

    @Generated
    public void setActive(boolean bl) {
        this.primaryActionActive = bl;
    }

    @Generated
    public void setActionSequence(int n) {
        this.actionSequence = n;
    }

    public static final class BlockHitCandidate {
        private final BlockHitResult hitResult;
        private final double distanceSquared;

        public BlockHitCandidate(BlockHitResult class_39652, double d) {
            this.hitResult = class_39652;
            this.distanceSquared = d;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "hitResult", "distanceSquared");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "hitResult", "distanceSquared");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "hitResult", "distanceSquared");
        }

        public BlockHitResult getHitResult() {
            return this.hitResult;
        }

        public double getDistanceSquared() {
            return this.distanceSquared;
        }
    }

    public static final class EntityHitCandidate {
        private final ShaderEntitySnapshot snapshot;
        private final double distanceSquared;

        public EntityHitCandidate(ShaderEntitySnapshot shaderEntitySnapshot, double d) {
            this.snapshot = shaderEntitySnapshot;
            this.distanceSquared = d;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "snapshot", "distanceSquared");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "snapshot", "distanceSquared");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "snapshot", "distanceSquared");
        }

        public ShaderEntitySnapshot getSnapshot() {
            return this.snapshot;
        }

        public double getDistanceSquared() {
            return this.distanceSquared;
        }
    }
}
