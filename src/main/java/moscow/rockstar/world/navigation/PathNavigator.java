/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Vec3d
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.world.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Generated;
import moscow.rockstar.api.validation.ScreenStateService;
import moscow.rockstar.combat.rotation.RotationRequestHelper;
import moscow.rockstar.combat.rotation.RotationState;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.entity.CollisionProbe;
import moscow.rockstar.events.dispatch.EventPublisher;
import moscow.rockstar.math.Rotation;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.ui.notifications.NotificationBridge;
import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.CollisionPath;
import moscow.rockstar.world.mining.ExcavationController;
import moscow.rockstar.world.navigation.BlockNavigationFactory;
import moscow.rockstar.world.navigation.CollisionPositionFinder;
import moscow.rockstar.world.navigation.PathExecutor;
import moscow.rockstar.world.navigation.Pathfinder;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class PathNavigator
implements ScreenStateService {
    private final CollisionProbe collisionProbe;
    @Nullable
    private PathExecutor pathExecutor;
    @Nullable
    private CompletableFuture<Optional<CollisionPath>> initialPathSearch;
    private boolean paused;
    private boolean targetReached;
    @Nullable
    private String lastErrorMessage;
    private long lastSearchRequestTime = 0L;
    private int stalledNavigationTicks = 0;
    private static final int SHORT_PATH_THRESHOLD = 8;
    private static final int REPLAN_LOOKAHEAD_STEPS = 12;
    private static final long REPLAN_COOLDOWN_MILLIS = 2500L;
    @Nullable
    private CompletableFuture<Optional<CollisionPath>> replanFuture;
    @Nullable
    private CollisionPath pendingReplannedPath;
    @Nullable
    private BlockPositionOffset replanAnchor;
    private int replanStepIndex = -1;
    private long lastReplanTime;
    @Nullable
    private AtomicBoolean replanCancellation;
    private boolean replanUnavailable;
    private static final double DIRECT_APPROACH_RADIUS = 3.5;
    private static final int DIRECT_APPROACH_TIMEOUT_TICKS = 60;
    private int directApproachStep = -1;
    private int directApproachStallTicks;
    private boolean directApproachActive;
    private int finalApproachTicks;
    private static final double DIRECT_APPROACH_DISTANCE = 2.0;
    private static final int AIR_SUPPLY_THRESHOLD = 80;

    public PathNavigator(CollisionProbe collisionProbe) {
        this.collisionProbe = collisionProbe;
    }

    @Override
    public String getCommandName() {
        return "goto";
    }

    @Override
    public String getStatusMessage() {
        if (this.paused) {
            return "\u043f\u0430\u0443\u0437\u0430";
        }
        if (this.targetReached) {
            return "\u0433\u043e\u0442\u043e\u0432\u043e";
        }
        if (this.initialPathSearch != null && !this.initialPathSearch.isDone()) {
            return "\u043f\u043e\u0438\u0441\u043a \u043f\u0443\u0442\u0438...";
        }
        if (this.pathExecutor == null) {
            return "\u043f\u0443\u0442\u044c \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d";
        }
        return "\u0448\u0430\u0433 " + (this.pathExecutor.getCurrentStep() + 1) + "/" + this.pathExecutor.getPath().getMovements().size();
    }

    @Override
    public boolean tickNavigation() {
        Object object;
        if (this.paused) {
            return false;
        }
        if (this.targetReached) {
            return true;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }
        RotationRequestHelper.refreshRotation();
        if (this.handleElytraApproach(client)) {
            return false;
        }
        if (this.directApproachActive) {
            return this.handleFinalApproach(client);
        }
        if (this.initialPathSearch != null) {
            if (!this.initialPathSearch.isDone()) {
                this.resetRotation(client);
                return false;
            }
            Optional optional = this.initialPathSearch.getNow(Optional.empty());
            this.initialPathSearch = null;
            if (optional.isEmpty()) {
                NotificationBridge.showPersistentMessage("\u041f\u0443\u0442\u044c \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d");
                this.lastErrorMessage = "\u043f\u0443\u0442\u044c \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d";
                this.stopNavigation();
                return true;
            }
            object = (CollisionPath)optional.get();
            this.pathExecutor = new PathExecutor((CollisionPath)object, this.collisionProbe);
            ClientServiceRegistry.getInstance().setPathExecutor(this.pathExecutor);
            NotificationBridge.showLoggedMessage("\u041f\u0443\u0442\u044c \u043d\u0430\u0439\u0434\u0435\u043d: " + ((CollisionPath)object).getMovements().size() + " \u0448\u0430\u0433\u043e\u0432");
            EventPublisher.publishNewtonPathProgress(this.getCommandName(), ((CollisionPath)object).getMovements().size());
        }
        if (this.pathExecutor == null) {
            this.requestInitialPath();
            this.resetRotation(client);
            return false;
        }
        if (this.directApproachStep >= 0) {
            this.advanceDirectApproach(client);
            return false;
        }
        if (this.isCurrentStepFarAway()) {
            if (this.findDirectApproachStep(client)) {
                return false;
            }
            this.requestInitialPath();
            this.pathExecutor = null;
            ClientServiceRegistry.getInstance().setPathExecutor(null);
            return false;
        }
        this.requestPathReplan();
        this.applyReplanResult();
        int n = this.pathExecutor.getCurrentStep();
        PathExecutor.MovementAction movementAction = this.pathExecutor.getMovementAction();
        this.publishPathProgress(n, this.pathExecutor);
        switch (movementAction) {
            case WAITING: {
                return false;
            }
            case ADVANCE: {
                if (this.collisionProbe.matchesCoordinates((int)Math.floor(client.player.getX()), (int)Math.floor(client.player.getY()), (int)Math.floor(client.player.getZ()))) {
                    NotificationBridge.showMessage("\u0414\u043e\u0448\u043b\u0438 \u0434\u043e \u0446\u0435\u043b\u0438");
                    this.targetReached = true;
                    this.resetNavigation();
                    return true;
                }
                if (this.pendingReplannedPath != null && this.replanAnchor != null && this.replanAnchor.equals(this.pathExecutor.getPath().getFirstMovement())) {
                    CollisionPath collisionPath = this.pendingReplannedPath;
                    this.pendingReplannedPath = null;
                    this.replanAnchor = null;
                    this.replanUnavailable = false;
                    this.pathExecutor = new PathExecutor(collisionPath, this.collisionProbe);
                    ClientServiceRegistry.getInstance().setPathExecutor(this.pathExecutor);
                    EventPublisher.publishNewtonPathProgress(this.getCommandName(), collisionPath.getMovements().size());
                    return false;
                }
                this.cancelReplan();
                this.pathExecutor = null;
                ClientServiceRegistry.getInstance().setPathExecutor(null);
                Vec3d VanillaChestLootTableGenerator = this.collisionProbe.toBlockCenter();
                double d = Math.hypot(VanillaChestLootTableGenerator.x - client.player.getX(), VanillaChestLootTableGenerator.z - client.player.getZ());
                double d2 = VanillaChestLootTableGenerator.y - client.player.getY();
                if (d <= 2.0 && d2 > -3.5 && d2 < 0.62) {
                    this.directApproachActive = true;
                    this.finalApproachTicks = 0;
                    return false;
                }
                this.requestInitialPath();
                return false;
            }
            case ARRIVED: {
                ++this.stalledNavigationTicks;
                if (this.stalledNavigationTicks > 5) {
                    NotificationBridge.showPersistentMessage("\u0421\u043b\u0438\u0448\u043a\u043e\u043c \u043c\u043d\u043e\u0433\u043e \u0441\u0431\u043e\u0435\u0432, \u043e\u0441\u0442\u0430\u043d\u0430\u0432\u043b\u0438\u0432\u0430\u0435\u043c\u0441\u044f");
                    this.lastErrorMessage = "\u0441\u043b\u0438\u0448\u043a\u043e\u043c \u043c\u043d\u043e\u0433\u043e \u0441\u0431\u043e\u0435\u0432";
                    this.stopNavigation();
                    return true;
                }
                this.cancelReplan();
                this.pathExecutor = null;
                ClientServiceRegistry.getInstance().setPathExecutor(null);
                this.requestInitialPath();
                return false;
            }
        }
        return false;
    }

    private void requestPathReplan() {
        if (this.pathExecutor == null || this.replanFuture != null || this.pendingReplannedPath != null) {
            return;
        }
        List<ExcavationController> list = this.pathExecutor.getPath().getMovements();
        int n = list.size() - this.pathExecutor.getCurrentStep();
        BlockPositionOffset blockPositionOffset = this.pathExecutor.getPath().getFirstMovement();
        boolean bl = this.collisionProbe.matchesCoordinates(blockPositionOffset.getX(), blockPositionOffset.getY(), blockPositionOffset.getZ());
        if (n <= 8) {
            if (bl || this.replanUnavailable) {
                return;
            }
            this.replanStepIndex = -1;
            this.replanAnchor = blockPositionOffset;
            this.replanCancellation = new AtomicBoolean();
            this.replanFuture = Pathfinder.findPathAsync(blockPositionOffset, this.collisionProbe, this.replanCancellation);
            return;
        }
        if (bl) {
            return;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastReplanTime < 2500L) {
            return;
        }
        int n2 = this.pathExecutor.getCurrentStep() + 12;
        if (n2 >= list.size()) {
            return;
        }
        this.lastReplanTime = l;
        this.replanStepIndex = n2;
        this.replanAnchor = this.pathExecutor.getPath().getNodes().get(n2);
        this.replanCancellation = new AtomicBoolean();
        this.replanFuture = Pathfinder.findPathAsync(this.replanAnchor, this.collisionProbe, this.replanCancellation);
    }

    private void applyReplanResult() {
        boolean bl;
        if (this.replanFuture == null || !this.replanFuture.isDone()) {
            return;
        }
        Optional optional = this.replanFuture.getNow(Optional.empty());
        this.replanFuture = null;
        boolean bl2 = bl = this.replanStepIndex >= 0;
        if (optional.isEmpty() || ((CollisionPath)optional.get()).getMovements().isEmpty()) {
            if (!bl) {
                this.replanUnavailable = true;
            }
            this.replanStepIndex = -1;
            if (bl) {
                this.replanAnchor = null;
            }
            return;
        }
        if (bl) {
            this.replacePathFromReplan((CollisionPath)optional.get());
            this.replanStepIndex = -1;
            this.replanAnchor = null;
        } else {
            this.pendingReplannedPath = (CollisionPath)optional.get();
        }
    }

    private void replacePathFromReplan(CollisionPath collisionPath) {
        if (this.pathExecutor == null || this.replanAnchor == null) {
            return;
        }
        List<BlockPositionOffset> list = this.pathExecutor.getPath().getNodes();
        List<ExcavationController> list2 = this.pathExecutor.getPath().getMovements();
        int n = this.replanStepIndex;
        int n2 = this.pathExecutor.getCurrentStep();
        if (n <= n2 || n >= list.size() || !list.get(n).equals(this.replanAnchor)) {
            return;
        }
        if (!collisionPath.getFirstNode().equals(this.replanAnchor)) {
            return;
        }
        BlockPositionOffset blockPositionOffset = this.pathExecutor.getPath().getFirstMovement();
        BlockPositionOffset blockPositionOffset2 = collisionPath.getFirstMovement();
        boolean bl = this.collisionProbe.matchesCoordinates(blockPositionOffset2.getX(), blockPositionOffset2.getY(), blockPositionOffset2.getZ());
        if (!bl && this.collisionProbe.distanceSquaredTo(blockPositionOffset2.getX(), blockPositionOffset2.getY(), blockPositionOffset2.getZ()) >= this.collisionProbe.distanceSquaredTo(blockPositionOffset.getX(), blockPositionOffset.getY(), blockPositionOffset.getZ()) - 1.0) {
            return;
        }
        ArrayList<BlockPositionOffset> arrayList = new ArrayList<BlockPositionOffset>(list.subList(n2, n + 1));
        arrayList.addAll(collisionPath.getNodes().subList(1, collisionPath.getNodes().size()));
        ArrayList<ExcavationController> arrayList2 = new ArrayList<ExcavationController>(list2.subList(n2, n));
        arrayList2.addAll(collisionPath.getMovements());
        this.pathExecutor = new PathExecutor(new CollisionPath(arrayList, arrayList2), this.collisionProbe);
        ClientServiceRegistry.getInstance().setPathExecutor(this.pathExecutor);
        EventPublisher.publishNewtonPathProgress(this.getCommandName(), arrayList2.size());
    }

    private void cancelReplan() {
        if (this.replanCancellation != null) {
            this.replanCancellation.set(true);
        }
        this.replanCancellation = null;
        this.replanFuture = null;
        this.pendingReplannedPath = null;
        this.replanAnchor = null;
        this.replanStepIndex = -1;
        this.replanUnavailable = false;
    }

    private boolean findDirectApproachStep(MinecraftClient client) {
        BlockNavigationFactory blockNavigationFactory;
        if (this.pathExecutor == null || client.player == null) {
            return false;
        }
        try {
            blockNavigationFactory = new BlockNavigationFactory();
        }
        catch (IllegalStateException illegalStateException) {
            return false;
        }
        List<BlockPositionOffset> list = this.pathExecutor.getPath().getNodes();
        double d = client.player.getX();
        double d2 = client.player.getY();
        double d3 = client.player.getZ();
        int n = this.pathExecutor.getCurrentStep();
        int n2 = Math.min(list.size() - 1, n + 15);
        int n3 = -1;
        double d4 = 3.5;
        for (int i = n; i <= n2; ++i) {
            double d5;
            BlockPositionOffset blockPositionOffset = list.get(i);
            double d6 = (double)blockPositionOffset.getY() - d2;
            if (d6 > 1.2 || d6 < -2.5 || (d5 = Math.hypot((double)blockPositionOffset.getX() + 0.5 - d, (double)blockPositionOffset.getZ() + 0.5 - d3)) >= d4) continue;
            double d7 = Math.max(d2, (double)blockPositionOffset.getY()) + 0.05;
            if (!blockNavigationFactory.isClearPath((int)Math.floor(d), (int)Math.floor(d3), blockPositionOffset.getX(), blockPositionOffset.getZ(), d7, d2 + 1.85)) continue;
            n3 = i;
            d4 = d5;
        }
        if (n3 < 0) {
            return false;
        }
        this.directApproachStep = n3;
        this.directApproachStallTicks = 0;
        ClientServiceRegistry.getInstance().setPathExecutor(null);
        return true;
    }

    private void advanceDirectApproach(MinecraftClient client) {
        if (client.player == null || this.pathExecutor == null) {
            this.directApproachStep = -1;
            return;
        }
        List<BlockPositionOffset> list = this.pathExecutor.getPath().getNodes();
        if (this.directApproachStep >= list.size()) {
            this.directApproachStep = -1;
            return;
        }
        BlockPositionOffset blockPositionOffset = list.get(this.directApproachStep);
        double d = client.player.getX();
        double d2 = client.player.getY();
        double d3 = client.player.getZ();
        double d4 = Math.hypot((double)blockPositionOffset.getX() + 0.5 - d, (double)blockPositionOffset.getZ() + 0.5 - d3);
        double d5 = d2 - (double)blockPositionOffset.getY();
        if (d4 < 0.5 && d5 > -1.2 && d5 < 1.2) {
            this.pathExecutor.jumpToStep(this.directApproachStep);
            ClientServiceRegistry.getInstance().setPathExecutor(this.pathExecutor);
            this.directApproachStep = -1;
            return;
        }
        if (++this.directApproachStallTicks > 60 || d4 > 6.0) {
            this.directApproachStep = -1;
            this.pathExecutor = null;
            this.requestInitialPath();
            return;
        }
        float f = (float)Math.toDegrees(Math.atan2((double)blockPositionOffset.getZ() + 0.5 - d3, (double)blockPositionOffset.getX() + 0.5 - d)) - 90.0f;
        RotationRequestHelper.requestAdaptiveRotation(new Rotation(f, 0.0f));
        RotationState rotationState = ClientServiceRegistry.getInstance().getRotationState();
        rotationState.enableMovementOverride();
        rotationState.setSprintPressed(false);
        rotationState.setBackwardPressed(false);
        rotationState.setStrafeRightPressed(false);
        rotationState.setStrafeLeftPressed(false);
        rotationState.setSneakPressed(false);
        rotationState.setForwardPressed(true);
        boolean bl = client.player.isTouchingWater() ? d2 < (double)blockPositionOffset.getY() + 0.2 : client.player.horizontalCollision && client.player.isOnGround();
        rotationState.setJumpPressed(bl);
    }

    private boolean handleElytraApproach(MinecraftClient client) {
        if (client.player == null) {
            return false;
        }
        if (!client.player.isSubmergedInWater()) {
            return false;
        }
        if (client.player.getAir() >= 80) {
            return false;
        }
        RotationRequestHelper.requestAdaptiveRotation(new Rotation(client.player.getYaw(), -90.0f));
        RotationState rotationState = ClientServiceRegistry.getInstance().getRotationState();
        rotationState.enableMovementOverride();
        rotationState.setForwardPressed(true);
        rotationState.setBackwardPressed(false);
        rotationState.setStrafeRightPressed(false);
        rotationState.setStrafeLeftPressed(false);
        rotationState.setSneakPressed(false);
        rotationState.setSprintPressed(true);
        rotationState.setJumpPressed(true);
        return true;
    }

    private void resetRotation(MinecraftClient client) {
        RotationState rotationState = ClientServiceRegistry.getInstance().getRotationState();
        rotationState.enableMovementOverride();
        rotationState.setForwardPressed(false);
        rotationState.setBackwardPressed(false);
        rotationState.setStrafeRightPressed(false);
        rotationState.setStrafeLeftPressed(false);
        rotationState.setSprintPressed(false);
        rotationState.setSneakPressed(false);
        rotationState.setJumpPressed(client.player != null && client.player.isTouchingWater());
    }

    private boolean isCurrentStepFarAway() {
        double d;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || this.pathExecutor == null) {
            return false;
        }
        ExcavationController excavationController = this.pathExecutor.getCurrentMovement();
        if (excavationController == null) {
            return false;
        }
        double d2 = client.player.getX();
        double d3 = client.player.getY();
        double d4 = client.player.getZ();
        BlockPositionOffset blockPositionOffset = BlockPositionOffset.fromBlockPosition(excavationController.getStartPosition());
        BlockPositionOffset blockPositionOffset2 = BlockPositionOffset.fromBlockPosition(excavationController.getEndPosition());
        double d5 = Math.hypot(d2 - ((double)blockPositionOffset.getX() + 0.5), d4 - ((double)blockPositionOffset.getZ() + 0.5)) + Math.abs(d3 - (double)blockPositionOffset.getY());
        return Math.min(d5, d = Math.hypot(d2 - ((double)blockPositionOffset2.getX() + 0.5), d4 - ((double)blockPositionOffset2.getZ() + 0.5)) + Math.abs(d3 - (double)blockPositionOffset2.getY())) > 2.5;
    }

    private void requestInitialPath() {
        long l = System.currentTimeMillis();
        if (l - this.lastSearchRequestTime < 250L) {
            return;
        }
        this.lastSearchRequestTime = l;
        if (this.initialPathSearch != null && !this.initialPathSearch.isDone()) {
            return;
        }
        this.cancelReplan();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        BlockPositionOffset blockPositionOffset = CollisionPositionFinder.findPlayerStandingPosition(client);
        this.initialPathSearch = Pathfinder.findPathAsync(blockPositionOffset, this.collisionProbe);
    }

    @Override
    public void stopNavigation() {
        this.resetNavigation();
    }

    @Override
    public void pauseNavigation() {
        this.paused = true;
        RotationState rotationState = ClientServiceRegistry.getInstance().getRotationState();
        rotationState.setForwardPressed(false);
        rotationState.setBackwardPressed(false);
        rotationState.setStrafeRightPressed(false);
        rotationState.setStrafeLeftPressed(false);
        rotationState.setJumpPressed(false);
        rotationState.setSprintPressed(false);
    }

    @Override
    public void resumeNavigation() {
        this.paused = false;
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public boolean isTargetReached() {
        return this.targetReached;
    }

    @Override
    @Nullable
    public String getErrorMessage() {
        return this.lastErrorMessage;
    }

    private void publishPathProgress(int n, PathExecutor pathExecutor) {
        List<ExcavationController> list = pathExecutor.getPath().getMovements();
        int n2 = list.size();
        int n3 = Math.min(pathExecutor.getCurrentStep(), n2);
        for (int i = Math.max(n, 0); i < n3; ++i) {
            BlockPositionOffset blockPositionOffset = BlockPositionOffset.fromBlockPosition(list.get(i).getEndPosition());
            EventPublisher.publishNewtonNodeProgress(this.getCommandName(), blockPositionOffset.getX(), blockPositionOffset.getY(), blockPositionOffset.getZ(), i + 1, n2);
        }
    }

    private boolean handleFinalApproach(MinecraftClient client) {
        double d;
        int n;
        int n2;
        if (client.player == null) {
            return false;
        }
        int n3 = (int)Math.floor(client.player.getX());
        if (this.collisionProbe.matchesCoordinates(n3, n2 = (int)Math.floor(client.player.getY()), n = (int)Math.floor(client.player.getZ()))) {
            NotificationBridge.showMessage("\u0414\u043e\u0448\u043b\u0438 \u0434\u043e \u0446\u0435\u043b\u0438");
            this.targetReached = true;
            this.directApproachActive = false;
            this.resetNavigation();
            return true;
        }
        Vec3d VanillaChestLootTableGenerator = this.collisionProbe.toBlockCenter();
        double d2 = VanillaChestLootTableGenerator.x - client.player.getX();
        double d3 = Math.hypot(d2, d = VanillaChestLootTableGenerator.z - client.player.getZ());
        if (d3 > 3.0 || this.finalApproachTicks++ > 100) {
            this.directApproachActive = false;
            this.requestInitialPath();
            return false;
        }
        RotationState rotationState = ClientServiceRegistry.getInstance().getRotationState();
        rotationState.enableMovementOverride();
        rotationState.setSprintPressed(false);
        rotationState.setJumpPressed(false);
        rotationState.setBackwardPressed(false);
        rotationState.setStrafeRightPressed(false);
        rotationState.setStrafeLeftPressed(false);
        rotationState.setSneakPressed(false);
        if (d3 < 0.15) {
            rotationState.setForwardPressed(false);
        } else {
            if (d3 > 0.35) {
                float f = (float)Math.toDegrees(Math.atan2(d, d2)) - 90.0f;
                RotationRequestHelper.requestAdaptiveRotation(new Rotation(f, 0.0f));
            }
            rotationState.setForwardPressed(true);
        }
        return false;
    }

    private void resetNavigation() {
        RotationState rotationState = ClientServiceRegistry.getInstance().getRotationState();
        rotationState.disableMovementOverride();
        this.pathExecutor = null;
        this.initialPathSearch = null;
        this.directApproachActive = false;
        this.directApproachStep = -1;
        this.cancelReplan();
        ClientServiceRegistry.getInstance().setPathExecutor(null);
    }

    @Generated
    public CollisionProbe getCollisionProbe() {
        return this.collisionProbe;
    }
}
